package com.google.android.gms.samples.vision.face.facetracker.ui.camera;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.samples.vision.face.facetracker.FaceModel;
import com.google.android.gms.samples.vision.face.facetracker.FaceView;
import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.samples.vision.face.facetracker.patch.SafeFaceDetector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.ArrayList;

/**
 * Created by amritkaur on 5/10/2017.
 */

public class CustomPhotoAdapter extends ArrayAdapter<FaceModel> {
    private static final String TAG = "ProjectAlex_CustomPhotoAdapter";
    private final Context context;
    private ArrayList<FaceModel> arrayList;
    FaceDetector detector;
    SafeFaceDetector safeDetector;

    public CustomPhotoAdapter(Context context, ArrayList<FaceModel> arrayList) {
        super(context, R.layout.custom_list_item, arrayList);
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        if (arrayList.size() <= 0) {
            return 1;
        }
        return arrayList.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FaceModel faceModel = arrayList.get(position);


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_list_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imgView);
        TextView textView = (TextView) convertView.findViewById(R.id.average);
        SparseArray<Face>faces = detectFaces(faceModel.getBitmap());
        FaceView faceView = new FaceView(context);
        Log.d(TAG, "path: " + faceModel.getBitmap() + ":" + faces);
        String s = faceView.setContent(faceModel.getBitmap(), faces, faceModel.getMood());
        imageView.setImageBitmap(faceModel.getBitmap());
        textView.setText(s);
        safeDetector.release();
        return convertView;
    }

    SparseArray<Face> detectFaces(Bitmap bitmap) {
        // A new face detector is created for detecting the face and its landmarks.
        //
        // Setting "tracking enabled" to false is recommended for detection with unrelated
        // individual images (as opposed to video or a series of consecutively captured still
        // images).  For detection on unrelated individual images, this will give a more accurate
        // result.  For detection on consecutive images (e.g., live video), tracking gives a more
        // accurate (and faster) result.
        //
        // By default, landmark detection is not enabled since it increases detection time.  We
        // enable it here in order to visualize detected landmarks.

        detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        // This is a temporary workaround for a bug in the face detector with respect to operating
        // on very small images.  This will be fixed in a future release.  But in the near term, use
        // of the SafeFaceDetector class will patch the issue.
        safeDetector = new SafeFaceDetector(detector);
        // Create a frame from the bitmap and run face detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = safeDetector.detect(frame);

        if (!safeDetector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = context.registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(context, R.string.low_storage_error, Toast.LENGTH_LONG).show();
            }
        } return faces;
    }
}