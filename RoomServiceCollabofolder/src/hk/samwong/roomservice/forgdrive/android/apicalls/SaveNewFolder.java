package hk.samwong.roomservice.forgdrive.android.apicalls;

import hk.samwong.roomservice.android.library.apicalls.APICaller;
import hk.samwong.roomservice.android.library.constants.HttpVerb;
import hk.samwong.roomservice.android.library.constants.LogTag;
import hk.samwong.roomservice.commons.dataFormat.Response;
import hk.samwong.roomservice.commons.parameterEnums.ReturnCode;
import hk.samwong.roomservice.forgdrive.android.constants.Defaults;
import hk.samwong.roomservice.forgdrive.commons.enums.GDriveOperation;
import hk.samwong.roomservice.forgdrive.commons.enums.GDriveParameterKey;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public abstract class SaveNewFolder extends APICaller<Void, Void, Response> {
	private String accountName;
	private String roomName;
	private String folderName;
	private String alternateLink;
	private Activity activity;
	
	
	public SaveNewFolder(Activity activity, String accountName, String roomName,
			String folderName, String alternateLink) {
		super();
		this.activity = activity;
		this.accountName = accountName;
		this.roomName = roomName;
		this.folderName = folderName;
		this.alternateLink = alternateLink;
	}

	@Override
	protected Response doInBackground(Void... params) {
		SERVLET_URL = Defaults.GDRIVE_SERVLET_URL;
				// prepare parameters
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs
				.add(new BasicNameValuePair(GDriveParameterKey.OPERATION.toString(),
						GDriveOperation.PutFolder.toString()));
		nameValuePairs
		.add(new BasicNameValuePair(GDriveParameterKey.ACCOUNT_NAME.toString(),
				accountName));
		nameValuePairs
		.add(new BasicNameValuePair(GDriveParameterKey.ROOM_NAME.toString(),
				roomName));
		nameValuePairs
		.add(new BasicNameValuePair(GDriveParameterKey.FOLDER_NAME.toString(),
				folderName));
		nameValuePairs
		.add(new BasicNameValuePair(GDriveParameterKey.ALTERNATE_LINK.toString(),
				alternateLink));
		
		Log.d(LogTag.APICALL.toString(), nameValuePairs.toString());

		try {
			String result = getJsonResponseFromAPICall(HttpVerb.PUT, nameValuePairs, activity);
			return new Gson().fromJson(result, new TypeToken<Response>() {
			}.getType());
		} catch (Exception e) {
			addException(e);
		}
		return new Response().setExplanation("Failed to complete API call").setReturnCode(ReturnCode.UNRECOVERABLE_EXCEPTION);
		
	}
	
	@Override
	protected abstract void onPostExecute(Response result);

}
