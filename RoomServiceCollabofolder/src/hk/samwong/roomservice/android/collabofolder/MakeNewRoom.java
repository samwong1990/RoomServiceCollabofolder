package hk.samwong.roomservice.android.collabofolder;

import java.io.IOException;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

public class MakeNewRoom extends Activity {
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	static final int CAPTURE_IMAGE = 3;

	private static Drive service;
	private GoogleAccountCredential credential;

	private String folderName;
	private int permissionViewID;
	private int accessViewID;

	private AutoCompleteTextView roomNameView;
	private TextView folderNameView;
	private RadioGroup permissionGroup;
	private RadioGroup accessGroup;

	private static final String tag = "MakeNewRoom";
	private static final String failureMessage = "Failed to create the folder, check your data connection and disk quota, then try again.";
	private static final String successMessage = "Your Folder is ready.";

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null
					&& data.getExtras() != null) {
				String accountName = data
						.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					service = getDriveService(credential);
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				createFolderInDrive(folderName, permissionViewID, accessViewID);
			} else {
				startActivityForResult(credential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
			}
			break;
		}
	}

	private void createFolderInDrive(final String title,
			final int visibilityViewID, final int accessViewID) {

		try {
			File folder = new File();
			folder.setMimeType("application/vnd.google-apps.folder");
			folder.setTitle(title);
			File file = getDriveService(credential).files().insert(folder)
					.execute();
			
			if(visibilityViewID == R.id.radioPrivate){
				// nothing to do for private folder
				success();
				return;
			}
			
			Permission newPermission = new Permission();
			newPermission.setValue("me");
			switch (visibilityViewID) {
			case R.id.radioPublic:
				newPermission.setId("anyone");
				newPermission.setType("anyone");
				break;
			case R.id.radioAnyone:
				newPermission.setId("anyoneWithLink");
				newPermission.setType("anyone");
				newPermission.setWithLink(true);
				break;
			}

			switch (accessViewID) {
			case R.id.radioEdit:
				newPermission.setRole("writer");
				break;

			case R.id.radioView:
				newPermission.setRole("reader");
				break;
			}
			service.permissions().insert(file.getId(), newPermission).execute();
			success();
		} catch (UserRecoverableAuthIOException e) {
			startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
		} catch (IOException e) {
			failure();
		}

		return;
	}

	private void failure() {
		showToast(failureMessage);
		roomNameView.setEnabled(true);
		folderNameView.setEnabled(true);
		permissionGroup.setEnabled(true);
		accessGroup.setEnabled(true);
	}

	private void success() {
		showToast(successMessage);
		finish();
	}

	public void createFolder(View view) {
		roomNameView = (AutoCompleteTextView) findViewById(R.id.roomName);
		folderNameView = (TextView) findViewById(R.id.folderName);
		permissionGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		accessGroup = (RadioGroup) findViewById(R.id.accessRadioGroup);

		showToast("Creating a new folder...");
		// Lock all fields:
		roomNameView.setEnabled(false);
		folderNameView.setEnabled(false);
		permissionGroup.setEnabled(false);
		accessGroup.setEnabled(false);

		// Create folder
		folderName = folderNameView.getText().toString();
		permissionViewID = permissionGroup.getCheckedRadioButtonId();
		accessViewID = accessGroup.getCheckedRadioButtonId();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				createFolderInDrive(folderName, permissionViewID, accessViewID);
				return null;
			}
		}.execute();

		// TODO send folderID, owner, roomName back to server
		String roomName = roomNameView.getText().toString();
	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_new_room);
		credential = GoogleAccountCredential.usingOAuth2(this,
				DriveScopes.DRIVE);
		// Let user pick which account to use
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.make_new_room, menu);
		return true;
	}

}
