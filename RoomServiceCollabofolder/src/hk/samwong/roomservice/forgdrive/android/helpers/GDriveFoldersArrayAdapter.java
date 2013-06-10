package hk.samwong.roomservice.forgdrive.android.helpers;

import hk.samwong.roomservice.android.collabofolder.R;
import hk.samwong.roomservice.forgdrive.commons.dataFormat.GDriveFolder;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GDriveFoldersArrayAdapter extends ArrayAdapter<GDriveFolder> {
	
	Context context;
	int layoutResourceId;
	List<GDriveFolder> folders = null;

	public GDriveFoldersArrayAdapter(Context context, int layoutResourceId,
			List<GDriveFolder> folders) {
		super(context, layoutResourceId, folders);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.folders = folders;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		GDriveFolderHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new GDriveFolderHolder();
			holder.star = (ToggleButton) row.findViewById(R.id.starring);
			holder.name = (TextView) row.findViewById(R.id.gfoldername);
			holder.room = (TextView) row.findViewById(R.id.gfolderroom);
			row.setTag(holder);
		} else {
			holder = (GDriveFolderHolder) row.getTag();
		}

		final GDriveFolder folder = folders.get(position);
		holder.star.setActivated(folder.isStarred());
		
		holder.name.setEllipsize(TruncateAt.MARQUEE);
		holder.name.setText(folder.getName() + " by " + folder.getOwner());
		holder.room.setEllipsize(TruncateAt.MARQUEE);
		holder.room.setText(folder.getRoom() + " (" + folder.getBrains()
				+ " present)");

		OnClickListener openDriveLink = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(folder
						.getUrl()));
				getContext().startActivity(i);
				Toast.makeText(getContext(),
						"Looking for suitable app to open folder",
						Toast.LENGTH_SHORT).show();
			}
		};

		holder.room.setOnClickListener(openDriveLink);
		holder.name.setOnClickListener(openDriveLink);

		return row;
	}

	static class GDriveFolderHolder {
		ToggleButton star;
		TextView name;
		TextView room;
	}

}
