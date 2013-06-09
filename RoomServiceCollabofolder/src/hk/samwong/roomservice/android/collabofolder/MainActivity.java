package hk.samwong.roomservice.android.collabofolder;

import hk.samwong.roomservice.forgdrive.android.apicalls.GetGDriveFolders;
import hk.samwong.roomservice.forgdrive.android.helpers.GDriveFoldersArrayAdapter;
import hk.samwong.roomservice.forgdrive.commons.dataFormat.GDriveFolder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainActivity extends Activity {
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	static final int CAPTURE_IMAGE = 3;

	private static Drive service;
	private GoogleAccountCredential credential;

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
					saveFileToDrive();
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				File folder = new File();
				folder.setMimeType("application/vnd.google-apps.folder");
				folder.setTitle("Test folder");
				Drive.Files.Insert insert;
				try {
					insert = service.files().insert(folder);
					insert.execute();
				} catch (IOException e) {
					Log.e("RoomDetection", "IOException", e);
				}
			} else {
				startActivityForResult(credential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
			}
			break;
		}
	}

	private void saveFileToDrive() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					File folder = new File();
					folder.setMimeType("application/vnd.google-apps.folder");
					folder.setTitle("Test folder");
					File file = getDriveService(credential).files().insert(
							folder).execute();
					if (file != null) {
						showToast(file.getTitle() + ":" + file.getAlternateLink());
						
					}
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
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

	private LinkedList<GDriveFolder> mListItems = new LinkedList<GDriveFolder>();
	private PullToRefreshListView mPullRefreshListView;
	private ArrayAdapter<GDriveFolder> mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// DRIVE
		credential = GoogleAccountCredential.usingOAuth2(this,
				DriveScopes.DRIVE);
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
		// END DRIVE

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_roomlist);

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(
								getApplicationContext(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);

						// Do work to refresh the list here.
						new GetGDriveFolders(thisActivity) {

							@Override
							protected void onPostExecute(
									List<GDriveFolder> result) {
								mListItems.addAll(result);
								mAdapter.notifyDataSetChanged();

								// Call onRefreshComplete when the list has been
								// refreshed.
								mPullRefreshListView.onRefreshComplete();
							}
						}.execute(thisActivity);
					}
				});

		ListView actualListView = mPullRefreshListView.getRefreshableView();

		// Need to use the Actual ListView when registering for Context Menu
		registerForContextMenu(actualListView);

		Random r = new Random();
		for (int i = 0; i < 15; i++) {
			mListItems.add(new GDriveFolder().withUrl(r.nextInt() + "")
					.withBrains(r.nextInt()).withInviteOnly(r.nextBoolean())
					.withName(r.nextInt() + "").withOwner(r.nextInt() + "")
					.withRoom(r.nextInt() + "").withStarred(r.nextBoolean())
					.withUrl(r.nextInt() + ""));
		}

		mAdapter = new GDriveFoldersArrayAdapter(this,
				R.layout.gdrivefolder_row,
				mListItems.toArray(new GDriveFolder[0]));

		// You can also just use setListAdapter(mAdapter) or
		// mPullRefreshListView.setAdapter(mAdapter)
		actualListView.setAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private Activity thisActivity = this;
}