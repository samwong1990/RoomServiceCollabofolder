package hk.samwong.roomservice.android.collabofolder;

import hk.samwong.roomservice.forgdrive.android.apicalls.GetGDriveFolders;
import hk.samwong.roomservice.forgdrive.android.helpers.GDriveFoldersArrayAdapter;
import hk.samwong.roomservice.forgdrive.commons.dataFormat.GDriveFolder;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainActivity extends Activity {
	

	private LinkedList<GDriveFolder> mListItems = new LinkedList<GDriveFolder>();
	private PullToRefreshListView mPullRefreshListView;
	private ArrayAdapter<GDriveFolder> mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_roomlist);

		// Set a listener to be invoked when the list should be refreshed.
		OnRefreshListener<ListView> onRefreshListener = new OnRefreshListener<ListView>() {
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
						mListItems.clear();
						mListItems.addAll(result);
						mAdapter.notifyDataSetChanged();

						// Call onRefreshComplete when the list has been
						// refreshed.
						mPullRefreshListView.onRefreshComplete();
					}
				}.execute();
			}
		};
		
		mPullRefreshListView
				.setOnRefreshListener(onRefreshListener);

		ListView actualListView = mPullRefreshListView.getRefreshableView();

		// Need to use the Actual ListView when registering for Context Menu
		registerForContextMenu(actualListView);

		mAdapter = new GDriveFoldersArrayAdapter(this,
				R.layout.gdrivefolder_row,
				mListItems);

		// You can also just use setListAdapter(mAdapter) or
		// mPullRefreshListView.setAdapter(mAdapter)
		actualListView.setAdapter(mAdapter);
		
		// Populate the graph for the first time.
		onRefreshListener.onRefresh(mPullRefreshListView);
	}

	public void showMyRooms(View view) {
		//TODO
	}
	public void createNewRoom(View view) {
	    startActivity(new Intent(this, MakeNewRoom.class));
	}
	
	
	private static final int MANUAL_REFRESH = 0;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case MANUAL_REFRESH:
				new UpdateFolderList().execute();
				mPullRefreshListView.setRefreshing(false);
				break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class UpdateFolderList extends GetGDriveFolders{
		public UpdateFolderList() {
			super(thisActivity);
		}

		@Override
		protected void onPostExecute(
				List<GDriveFolder> result) {
			mListItems.clear();
			mListItems.addAll(result);
			mAdapter.notifyDataSetChanged();

			// Call onRefreshComplete when the list has been
			// refreshed.
			mPullRefreshListView.onRefreshComplete();
		}
	}
	
	private Activity thisActivity = this;
}