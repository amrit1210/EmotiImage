package com.google.android.gms.samples.vision.face.facetracker.gallery;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.samples.vision.face.facetracker.ImageUtils;
import com.google.android.gms.samples.vision.face.facetracker.R;

import java.util.ArrayList;

/**
 * Created by amritkaur on 19-04-2017.
 */

public class CustomImageAdapter extends BaseAdapter {
    private static final String TAG = "ProjectAlex_CustomImageAdapter";

    private Context context;
    private LayoutInflater mInflater;
    private ArrayList<CustomGalleryItem> data = new ArrayList<CustomGalleryItem>();
    private boolean isCheckBoxVisible = true;
    private boolean isSingleImageSelected = false;
    private boolean isUploadMode = false;

    public CustomImageAdapter(Context context) {
        Log.d(TAG, "CustomImageAdapter");
        this.context = context;
        mInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount " + data.size());
        return data.size();
    }

    @Override
	public CustomGalleryItem getItem(int position) {
        Log.d(TAG, "getItem");
		return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG, "getItemId");
        return position;
    }

    public void setCheckBoxVisibility(boolean isCheckBoxVisible) {
        Log.d(TAG, "setCheckBoxVisibility " + isCheckBoxVisible);
        this.isCheckBoxVisible = isCheckBoxVisible;
    }

    public void selectAll(boolean selection) {
        Log.d(TAG, "selectAll");
        for (int i = 0; i < data.size(); i++) {
            data.get(i).isSelected = selection;
        }
        notifyDataSetChanged();
    }

    public ArrayList<CustomGalleryItem> getSelected() {
        Log.d(TAG, "getSelected");
        ArrayList<CustomGalleryItem> dataList = new ArrayList<CustomGalleryItem>();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSelected) {
                dataList.add(data.get(i));
            }
        }
        Log.d(TAG, "getSelected Returns " + dataList.size());
        return dataList;
    }

    public void addAll(ArrayList<CustomGalleryItem> files) {
        Log.d(TAG, "addAll");
        Log.d(TAG, "addAll files Size " + files.size());
        try {
            this.data.clear();
            this.data.addAll(files);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "notifyDataSetChanged");
        notifyDataSetChanged();
    }

    public void changeSelection(View v, int position) {
        Log.d(TAG, "changeSelection");
        if (data.get(position).isSelected) {
            data.get(position).isSelected = false;
        } else {
            data.get(position).isSelected = true;
        }

        ((ViewHolder) v.getTag()).checkBoxImage.setSelected(data.get(position).isSelected);
    }

    public void setImageSelectMode(boolean isSingleImageSelected) {
        this.isSingleImageSelected = isSingleImageSelected;
    }

    public void setImageUploadMode(boolean isUploadMode) {
        this.isUploadMode = isUploadMode;
    }

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView");
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_gallery_item, null);
            holder = new ViewHolder();

            holder.imageThumbnail = (ImageView) convertView.findViewById(R.id.imgThumb);
            holder.checkBoxImage = (ImageView) convertView.findViewById(R.id.chkImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageThumbnail.setTag(position);
        try {
            setBitmap(holder.imageThumbnail, data.get(position).sdcardPath);
        } catch (Throwable e) {
        }
        holder.checkBoxImage.setSelected(data.get(position).isSelected);



        if (isUploadMode) {
            holder.imageThumbnail.getWidth();
            holder.imageThumbnail.getHeight();
            Log.d(TAG, "GOGO " + holder.imageThumbnail.getWidth() + "  " + holder.imageThumbnail.getHeight());
            /*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
            holder.imageThumbnail.setLayoutParams(layoutParams);
            */
      /*      holder.imageThumbnail.setMaxWidth(200);
            holder.imageThumbnail.setMaxHeight(200);*/
        } /*else {
            holder.imageThumbnail.setMaxWidth(ViewGroup.LayoutParams.FILL_PARENT);
            holder.imageThumbnail.setMaxHeight(ViewGroup.LayoutParams.FILL_PARENT);
        }*/
        if(isCheckBoxVisible) {
            holder.checkBoxImage.setVisibility(View.VISIBLE);
        } else {
            holder.checkBoxImage.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    private void setBitmap(final ImageView iv, final String path) {
        Log.d(TAG, "setBitmap");
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {

                Cursor ca = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
                if (ca != null && ca.moveToFirst()) {
                    int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
                    ca.close();
                    Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null );
                    bitmap = ImageUtils.adjustOrientation(bitmap, path);
                    return bitmap;
                }
                ca.close();
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                iv.setImageBitmap(result);
            }
        }.execute();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder {
        ImageView imageThumbnail;
        ImageView checkBoxImage;
    }
}
