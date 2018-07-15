package com.google.android.gms.samples.vision.face.facetracker.gallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.samples.vision.face.facetracker.R;

import java.util.ArrayList;

/**
 * Created by amritkaur on 11-04-2017.
 */

public class GalleryAccessActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "ProjectAlex_FaceTrackerActivity";

    private LinearLayout lnrImages;
    private Button btnAddPhots;
    private Button btnSaveImages;
    private ArrayList<String> imagesPathList;
    private Bitmap yourbitmap;
    private Bitmap resized;
    private final int PICK_IMAGE_MULTIPLE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        lnrImages = (LinearLayout)findViewById(R.id.lnrImages);
        btnAddPhots = (Button)findViewById(R.id.btnAddPhots);
        btnSaveImages = (Button)findViewById(R.id.btnSaveImages);
        btnAddPhots.setOnClickListener(this);
        btnSaveImages.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");
        switch (view.getId()) {
            case R.id.btnAddPhots:
                Intent intent = new Intent(GalleryAccessActivity.this, CustomPhotoGalleryActivity.class);
                startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
                break;
            case R.id.btnSaveImages:
                if(imagesPathList !=null){
                    if(imagesPathList.size()>1) {
                        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                        Toast.makeText(GalleryAccessActivity.this, imagesPathList.size() + " no of images are selected", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(GalleryAccessActivity.this, imagesPathList.size() + " no of image are selected", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(GalleryAccessActivity.this," no images are selected", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivity Result");
            if(resultCode == RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE){
                imagesPathList = new ArrayList<String>();
                String[] imagesPath = data.getStringArrayExtra("all_path");

            }
    }

}