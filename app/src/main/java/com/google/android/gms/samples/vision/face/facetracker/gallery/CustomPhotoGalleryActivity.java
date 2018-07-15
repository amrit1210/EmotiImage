package com.google.android.gms.samples.vision.face.facetracker.gallery;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Collections;

import com.google.android.gms.samples.vision.face.facetracker.EmotionEnums;
import com.google.android.gms.samples.vision.face.facetracker.FaceModel;
import com.google.android.gms.samples.vision.face.facetracker.ImageUtils;
import com.google.android.gms.samples.vision.face.facetracker.PhotoViewerActivity;
import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.samples.vision.face.facetracker.settings.CustomAdapter;

/**
 * Created by amritkaur on 11-04-2017.
 */

public class CustomPhotoGalleryActivity extends Activity {
    private static final String TAG = "ProjectAlex_CustomGalleryActivity";
    private CustomImageAdapter adapter;
    private GridView gridGallery;
    private Button buttonSelect;
    private Spinner spinnerActionbar;
	private Spinner spinner;
    private CustomAdapter customAdapter;
    EmotionEnums mood;
    Bitmap bitmap;
    Uri uri;
   public static ArrayList<FaceModel> arrayList= new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.custom_gallery);
        spinner = (Spinner) findViewById(R.id.emotSpinner);

        customAdapter = new CustomAdapter(getApplicationContext());
        spinner.setOnItemSelectedListener(onItemSelectListener);
        spinner.setAdapter(customAdapter);

        adapter = new CustomImageAdapter(getApplicationContext());
        gridGallery = (GridView) findViewById(R.id.gridview_images);
        buttonSelect = (Button) findViewById(R.id.button_select);
       /* spinnerActionbar = (Spinner) findViewById(R.id.spinner_actionbar);
        customAdapter = new CustomAdapter(getApplicationContext());
        spinnerActionbar.setOnItemSelectedListener(onItemSelectListener);
        spinnerActionbar.setAdapter(customAdapter);
*/
        gridGallery.setOnItemClickListener(mItemClickListener);
        gridGallery.setFastScrollEnabled(true);

        buttonSelect.setOnClickListener(mSelectClickListener);

        gridGallery.setAdapter(adapter);
        new ImportGalleryPhotosTask().execute();

    }
    private void setCurrentMood(EmotionEnums mood) {
        this.mood = mood;
    }
    private EmotionEnums getCurrentMood() {
        return mood;
    }

    private  AdapterView.OnItemSelectedListener onItemSelectListener =new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "onItemSelected " + position);
            setCurrentMood(EmotionEnums.values()[position]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.d(TAG, "onNothingSelected");
        }
    };

    View.OnClickListener mSelectClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Upload Button Clicked");
            ArrayList<CustomGalleryItem> selected = adapter.getSelected();
            boolean isSingleImageSelected = false;


            Log.d(TAG, "Mood" + mood);

            arrayList.clear();
            mood = getCurrentMood();
            for (int i = 0; i <selected.size(); i++) {
                uri = selected.get(i).imageURI;
                bitmap = ImageUtils.getBitmap(selected.get(i).sdcardPath, 2048, 1232);
                if (bitmap == null) {
                    return;
                }
                //Log.d(TAG, "Path" + selected.get(i).sdcardPath);
              //  Log.d(TAG, "Path" + bitmap);
                Log.d(TAG, "Path: uri"+ uri);
               arrayList.add(new FaceModel(bitmap,mood,uri));
            }

            // Although detector may be used multiple times for different images, it should be released
            // when it is no longer needed in order to free native resources.
            if(arrayList!=null && arrayList.size()!=0) {
                Intent i = new Intent(CustomPhotoGalleryActivity.this, PhotoViewerActivity.class);
                startActivity(i);
            }

//            for (String path : allPath) {
//                CustomGalleryItem item = new CustomGalleryItem();
//                item.sdcardPath = path;
//                dataT.add(item);
//            }
//            if (dataT.size() > 1) {
//                isSingleImageSelected = false;
//            } else {
//                isSingleImageSelected = true;
//            }
/*
            adapter.setCheckBoxVisibility(false);

            adapter.addAll(dataT);
*/

           /* if (isSingleImageSelected) {
                gridGallery.setNumColumns(1);
            } else {
                gridGallery.setNumColumns(2);
            }
            adapter.setImageUploadMode(true);
*/
        }
    };

    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            Log.d(TAG, "onItemClick");
            adapter.changeSelection(v, position);
        }
    };

    private class ImportGalleryPhotosTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... data) {
            Log.d(TAG, "ImportGalleryPhotosTask");
            ArrayList<CustomGalleryItem> galleryList = new ArrayList<CustomGalleryItem>();

            try {
                final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
                final String orderBy = MediaStore.Images.Media._ID;

                Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
            
                if (imageCursor != null && imageCursor.getCount() > 0) {

                    while (imageCursor.moveToNext()) {
                        CustomGalleryItem item = new CustomGalleryItem();
                        Uri imageUri= ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));

                        int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        item.sdcardPath = imageCursor.getString(dataColumnIndex);
                        item.imageURI = imageUri;

                        galleryList.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // show newest photo at beginning of the list
            Collections.reverse(galleryList);

            Log.d(TAG, "Gallery Size " + galleryList.size());
            adapter.addAll(galleryList);
            return null;
        }
    }
}

