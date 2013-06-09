package hk.samwong.roomservice.forgdrive.android.helpers;

import hk.samwong.roomservice.android.collabofolder.R;
import hk.samwong.roomservice.forgdrive.commons.dataFormat.GDriveFolder;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GDriveFoldersArrayAdapter extends ArrayAdapter<GDriveFolder>{
	Context context; 
    int layoutResourceId;    
    GDriveFolder folders[] = null;
    
    public GDriveFoldersArrayAdapter(Context context, int layoutResourceId, GDriveFolder[] folders) {
        super(context, layoutResourceId, folders);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.folders = folders;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;    
        GDriveFolderHolder holder = null;
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new GDriveFolderHolder();
            holder.star = (RatingBar)row.findViewById(R.id.starring);
            holder.name = (TextView)row.findViewById(R.id.gfoldername);
            holder.room = (TextView)row.findViewById(R.id.gfolderroom);
            holder.brains = (TextView)row.findViewById(R.id.gfolderbrains);
            holder.owner = (TextView)row.findViewById(R.id.gfolderowner);
            
            row.setTag(holder);
        }
        else
        {
            holder = (GDriveFolderHolder)row.getTag();
        }
        
        GDriveFolder folder = folders[position];
        holder.star.setActivated(folder.isStarred());
        holder.name.setText(folder.getName());
        holder.room.setText(folder.getRoom());
        holder.brains.setText(folder.getBrains() + "");
        holder.owner.setText(folder.getOwner());
        
        return row;
    }
    
    static class GDriveFolderHolder
    {
        RatingBar star;
        TextView name;
        TextView room;
        TextView brains;
        TextView owner;
    }

}
