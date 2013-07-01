package hk.samwong.roomservice.forgdrive.android.apicalls;

import hk.samwong.roomservice.android.library.apicalls.APICaller;
import hk.samwong.roomservice.android.library.apicalls.GetListOfRooms;
import hk.samwong.roomservice.android.library.constants.HttpVerb;
import hk.samwong.roomservice.android.library.constants.LogTag;
import hk.samwong.roomservice.commons.parameterEnums.ReturnCode;
import hk.samwong.roomservice.forgdrive.android.constants.Defaults;
import hk.samwong.roomservice.forgdrive.commons.dataFormat.GDriveFolder;
import hk.samwong.roomservice.forgdrive.commons.dataFormat.ResponseWithGDriveFolders;
import hk.samwong.roomservice.forgdrive.commons.enums.GDriveOperation;
import hk.samwong.roomservice.forgdrive.commons.enums.GDriveParameterKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public abstract class GetGDriveFolders extends
		APICaller<Void, Void, ResponseWithGDriveFolders> {

	private Activity activity;

	public GetGDriveFolders(Activity activity) {
		this.activity = activity;
	}

	private List<String> rooms = null;
	private final CountDownLatch blocker = new CountDownLatch(1);

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		GetListOfRooms getListOfRooms = new GetListOfRooms(activity) {
			@Override
			protected void onPostExecute(List<String> result) {
				rooms = result;
				blocker.countDown();
			}
		};
		getListOfRooms.execute(activity);
	}

	@Override
	protected ResponseWithGDriveFolders doInBackground(Void... params) {

		while (blocker.getCount() > 0) {
			try {
				blocker.await();
			} catch (InterruptedException e) {
				Log.w(LogTag.APICALL.toString(),
						"Interrupted while waiting for blocker to clear.");
			}
		}
		SERVLET_URL = Defaults.GDRIVE_SERVLET_URL;

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(
				GDriveParameterKey.OPERATION.toString(),
				GDriveOperation.GetSurroundingFolders.toString()));
		nvps.add(new BasicNameValuePair(GDriveParameterKey.ROOMS.toString(),
				new Gson().toJson(rooms)));
		try {
			String jsonResult = getJsonResponseFromAPICall(HttpVerb.GET, nvps,
					activity);
			// UGLY hack for unresolved bug
			SERVLET_URL = hk.samwong.roomservice.android.library.constants.Defaults.ROOMSERVICE_SERVLET_URL;
			ResponseWithGDriveFolders response = new Gson().fromJson(
					jsonResult, new TypeToken<ResponseWithGDriveFolders>() {
					}.getType());
			return response;
		} catch (Exception e) {
			Log.w(LogTag.APICALL.toString(),
					"No response for the GetGDriveFolders query");
			return (ResponseWithGDriveFolders) new ResponseWithGDriveFolders()
					.withGDriveFolders(Collections.<GDriveFolder> emptyList())
					.setReturnCode(ReturnCode.UNRECOVERABLE_EXCEPTION)
					.setExplanation("Failed to complete the api call");
		}

	}

	@Override
	abstract protected void onPostExecute(ResponseWithGDriveFolders result);

}
