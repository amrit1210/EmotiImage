/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.net.URI;
import java.util.ArrayList;

import com.google.android.gms.samples.vision.face.facetracker.gallery.CustomPhotoGalleryActivity;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CustomPhotoAdapter;

/**
 * Demonstrates basic usage of the GMS vision face detector by running face landmark detection on a
 * photo and displaying the photo with associated landmarks in the UI.
 */
public class PhotoViewerActivity extends Activity {
    private static final String TAG = "ProjectAlex_PhotoViewerActivity";
    ListView listView;
    CustomPhotoAdapter adapter;
    ArrayList<Uri> imageUris = new ArrayList<Uri>();
    FaceModel faceModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        ArrayList<FaceModel> arrayList = CustomPhotoGalleryActivity.arrayList;
        for(int i = 0; i< arrayList.size() ;i++)
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new CustomPhotoAdapter(PhotoViewerActivity.this, arrayList);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(mItemLongClickListener);
    }

    AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            faceModel = (FaceModel) listView.getItemAtPosition(i);
            Log.d(TAG,"path: "+faceModel.getUri());
            imageUris.add(faceModel.getUri());
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Share images to.."));
            return true;
        }
    };
}