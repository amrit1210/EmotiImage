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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.face.facetracker.gallery.CustomPhotoGalleryActivity;
import com.google.android.gms.samples.vision.face.facetracker.settings.CustomAdapter;
import com.google.android.gms.samples.vision.face.facetracker.settings.Emoticons;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "ProjectAlex_FaceTrackerActivity";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private int PICK_IMAGE_REQUEST = 1;
    private static final int RC_HANDLE_PERMISSIONS = 2;

    public static boolean mIsFrontFacing = true;
    private ImageView backpress;
    private ImageView captureImage;
    private ImageView switchCamera;
    private ImageView gallery;
    private ImageView groupSuggestion;
    private TextView selectedMood;
    private ImageView auto;
    private Spinner spinner;
    private CustomAdapter customAdapter;
    private TextView faceCount;

    public static boolean isGroupSuggestion = false;
    private int faceDetectedCount = 0;

    private static Handler mHandler;

    EmotionEnums mood;
    public static EmotionEnums currentMood;


    private LinearLayout chooseMoodLayout;
    private GridView gridViewMood;
    private boolean isPreviouslyAutoSet = false;
    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        backpress = (ImageView) findViewById(R.id.backpress);
        captureImage = (ImageView) findViewById(R.id.captureImage);
        switchCamera = (ImageView) findViewById(R.id.switchCamera);
        gallery = (ImageView) findViewById(R.id.gallery);
        groupSuggestion = (ImageView) findViewById(R.id.groupSuggestion);
        selectedMood = (TextView) findViewById(R.id.selected_mood);
        auto = (ImageView) findViewById(R.id.auto);
        faceCount = (TextView) findViewById(R.id.textview_facecount);

        backpress.setOnClickListener(mBackPressListener);
        switchCamera.setOnClickListener(mFlipButtonListener);
        captureImage.setOnClickListener(mCaptureImageListener);
        gallery.setOnClickListener(mAcessGalleryListener);
        groupSuggestion.setOnClickListener(mGroupSuggestionListener);
        selectedMood.setOnClickListener(mChooseMoodListener);

        auto.setOnClickListener(mAutoSelectListener);
        spinner = (Spinner) findViewById(R.id.simpleSpinner);

        customAdapter = new CustomAdapter(getApplicationContext());
        //spinner.setOnItemSelectedListener(onItemSelectListener);
        spinner.setAdapter(customAdapter);


        chooseMoodLayout = (LinearLayout) findViewById(R.id.select_mood);
        gridViewMood = (GridView) chooseMoodLayout.findViewById(R.id.gridview_mood);
        gridViewMood.setOnItemClickListener(onMoodSelectListener);

        gridViewMood.setAdapter(customAdapter);
        if (savedInstanceState != null) {
            mIsFrontFacing = savedInstanceState.getBoolean("IsFrontFacing");
        }

        chooseMoodLayout.setVisibility(View.INVISIBLE);
        setCurrentMood(EmotionEnums.NEUTRAL);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                handleEmotionMessage(msg);
            }
        };
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rCameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int rReadStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int rWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rCameraPermission == PackageManager.PERMISSION_GRANTED && rReadStoragePermission == PackageManager.PERMISSION_GRANTED && rWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
            //adjustCameraParameters(mCameraSource);
        } else {
            requestPermissions();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestPermissions() {
        Log.d(TAG, "requestPermissions");
        Log.w(TAG, "Permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_PERMISSIONS);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_PERMISSIONS);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {
        Log.d(TAG, "createCameraSource");
        Context context = getApplicationContext();
      /*  FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();*/
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }


        int facing = CameraSource.CAMERA_FACING_FRONT;
        if (!mIsFrontFacing) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }
        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(facing)
                .setRequestedPreviewSize(1600, 1200)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    public static Camera getCamera(CameraSource cameraSource) {
        Log.d(TAG, "getCamera");
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    public static void adjustCameraParameters(CameraSource mCameraSource) {
        Log.d(TAG, "adjustCameraParameters");
        Camera mCamera = getCamera(mCameraSource);
        Camera.Parameters camParams = mCamera.getParameters();
        camParams.setExposureCompensation(camParams.getMaxExposureCompensation());
        mCamera.setParameters(camParams);
    }


    static Handler getHandler() {
        Log.d(TAG, "getHandler");
        return mHandler;
    }

    void handleEmotionMessage(Message msg) {
        Log.d(TAG, "handleEmotionMessage");
        EmotionEnums emotion = (EmotionEnums) msg.obj;
        switch (emotion) {

            case NEUTRAL:
                Log.d(TAG, "handleEmotionMessage Received " + EmotionEnums.NEUTRAL);
                if (auto.isSelected() && !groupSuggestion.isSelected() && getCurrentMood() == EmotionEnums.NEUTRAL) {
                    clickPhoto();
                    auto.setSelected(false);
                }
                break;

            case HAPPY:
                Log.d(TAG, "handleEmotionMessage Received " + EmotionEnums.HAPPY);
                if (auto.isSelected() && !groupSuggestion.isSelected() && getCurrentMood() == EmotionEnums.HAPPY) {
                    clickPhoto();
                    auto.setSelected(false);
                }
                break;

            case SAD:
                if (auto.isSelected() && !groupSuggestion.isSelected() && getCurrentMood() == EmotionEnums.SAD) {
                    clickPhoto();
                    auto.setSelected(false);
                }
                Log.d(TAG, "handleEmotionMessage Received " + EmotionEnums.SAD);
                break;

            case WINKING:
                if (auto.isSelected() && !groupSuggestion.isSelected() && getCurrentMood() == EmotionEnums.WINKING) {
                    clickPhoto();
                    auto.setSelected(false);
                }
                Log.d(TAG, "handleEmotionMessage Received " + EmotionEnums.WINKING);
                break;


            case GRINNING:
                if (auto.isSelected() && !groupSuggestion.isSelected() && getCurrentMood() == EmotionEnums.GRINNING) {
                    clickPhoto();
                    auto.setSelected(false);
                }
                Log.d(TAG, "handleEmotionMessage Received " + EmotionEnums.GRINNING);
                break;

            case POUT:
                if (auto.isSelected() && !groupSuggestion.isSelected() && getCurrentMood() == EmotionEnums.POUT) {
                    clickPhoto();
                    auto.setSelected(false);
                }
                Log.d(TAG, "handleEmotionMessage Received " + EmotionEnums.GRINNING);
                break;

            case BLINKING:
                if (auto.isSelected() && !groupSuggestion.isSelected() && getCurrentMood() == EmotionEnums.BLINKING) {
                    clickPhoto();
                    auto.setSelected(false);
                }
                Log.d(TAG, "handleEmotionMessage Received " + EmotionEnums.BLINKING);
                break;

            /*case SURPRISED:
                if (auto.isSelected() && !groupSuggestion.isSelected() && getCurrentMood() == EmotionEnums.SURPRISED) {
                    clickPhoto();
                    auto.setSelected(false);
                }
                Log.d(TAG, "handleEmotionMessage Received " + EmotionEnums.SURPRISED);
                break;*/

        }
        Log.d(TAG, "Msg Recieved " + msg.what);
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode != RC_HANDLE_PERMISSIONS) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        String message = null;
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        } else {
            message = message + R.string.no_camera_permission;
        }

        if (grantResults.length != 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Read External Storage permission granted");
            return;
        } else {
            message = message + R.string.no_read_external_storage_permission;
        }

        if (grantResults.length != 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Write External Storage  permission granted - initialize the camera source");
            return;
        } else {
            message = message + R.string.no_write_external_storage_permission;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(message)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        Log.d(TAG, "startCameraSource");
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // UI
    //==============================================================================================

    /**
     * Saves the camera facing mode, so that it can be restored after the device is rotated.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("IsFrontFacing", mIsFrontFacing);
    }

    /**
     * Toggles between front-facing and rear-facing modes.
     */
    private View.OnClickListener mFlipButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "Switch Camera");
            mIsFrontFacing = !mIsFrontFacing;

            if (mCameraSource != null) {
                mCameraSource.release();
                mCameraSource = null;
            }
            createCameraSource();
            startCameraSource();
        }
    };

    private View.OnClickListener mCaptureImageListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "Take Picture");
            clickPhoto();
            //mCameraSource.takePicture(mShutterCallback, mPictureCallback);
        }
    };

    private View.OnClickListener mBackPressListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "Back Key Pressed");
            onBackPressed();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed");
    }

    private View.OnClickListener mAcessGalleryListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "Access Gallery");// Create intent to Open Image applications like Gallery, Google Photos
            Intent i = new Intent(FaceTrackerActivity.this, CustomPhotoGalleryActivity.class);
            startActivity(i);
        }
    };

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "onActivityResult");
//        if (data == null) {
//            Log.d(TAG, "data is NULL");
//        }
//        if (data.getData() == null) {
//            Log.d(TAG, "data 2 is NULL");
//        }
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            Uri uri = data.getData();
//            Log.v(TAG, "URI " + uri);
//            Intent i = new Intent(FaceTrackerActivity.this, PhotoViewerActivity.class);
//            i.putExtra("Uri", uri.toString());
//            startActivity(i);
//        }
//    }



    private View.OnClickListener mChooseMoodListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "Choose Mood");
            if (chooseMoodLayout.getVisibility() != View.VISIBLE) {
                chooseMoodLayout.setVisibility(View.VISIBLE);
            } else {
                chooseMoodLayout.setVisibility(View.INVISIBLE);
            }
        }
    };

    private View.OnClickListener mAutoSelectListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "Auto Clicked");
            if (auto.isSelected()) {
                auto.setSelected(false);
            } else {
                auto.setSelected(true);
            }
        }
    };

    private View.OnClickListener mGroupSuggestionListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "Group Suggestion Clicked");
            if (groupSuggestion.isSelected()) {
                groupSuggestion.setSelected(false);
                isGroupSuggestion = false;
            } else {
                groupSuggestion.setSelected(true);
                isGroupSuggestion = true;
            }
        }
    };

    AdapterView.OnItemClickListener onMoodSelectListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            Log.d(TAG, "onItemSelected " + position);
            setCurrentMood(EmotionEnums.values()[position]);
            chooseMoodLayout.setVisibility(View.INVISIBLE);
        }
    };

    private EmotionEnums getCurrentMood() {
        return mood;
    }

    private void setCurrentMood(EmotionEnums mood) {
        this.mood = mood;
        currentMood = mood;
        String emojiUnicode = Emoticons.getEmoticon(mood);
        selectedMood.setText("  Mood  " + emojiUnicode + "  ");
    }

    private void clickPhoto () {
        Log.d(TAG, "clickPhoto");
        isPreviouslyAutoSet = auto.isSelected();
        mCameraSource.takePicture(mShutterCallback, mPictureCallback);
        if (isPreviouslyAutoSet) {
            setAutoEnable();
        }
    }

    private void setAutoEnable() {
        Log.d(TAG, "setAutoEnable");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        Log.d(TAG, "Seconds Remaining To Enable Auto Again" + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        Log.d(TAG, "Set Auto Selected");
                        auto.setSelected(true);
                    }
                }.start();
            }
        });
    }


    private CameraSource.PictureCallback mPictureCallback = new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes) {
            Log.d(TAG, "onPictureTaken");
            new SaveImageTask().execute(bytes);
            startCameraSource();
        }
    };


    private CameraSource.ShutterCallback mShutterCallback = new CameraSource.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutter");
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            Log.d(TAG, "onPictureTaken");
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.e(TAG, "Error Creating Media File, Check Storage Permissions");
                return null;
            }
            FileOutputStream outStream = null;
            // Write to SD Card
            try {
                outStream = new FileOutputStream(pictureFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + pictureFile.getAbsolutePath());
                refreshGallery(pictureFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }

        private File getOutputMediaFile() {
            Log.d(TAG, "getOutputMediaFile");
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "/ProjectAlex");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            return mediaFile;
        }

        private void refreshGallery(File file) {
            Log.d(TAG, "refreshGallery");
            Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(file));
            sendBroadcast(mediaScanIntent);
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            Log.d(TAG, "GraphicFaceTracker");
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            Log.d(TAG, "onNewItem");
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            Log.d(TAG, "onUpdate");
            updateFaceCount(mOverlay.getSize());
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            Log.d(TAG, "GroupSuggestion Selected " + groupSuggestion.isSelected());
            if(auto.isSelected() && groupSuggestion.isSelected() && checkSuggestedEmotion()) {
                clickPhoto();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        auto.setSelected(false);
                        //groupSuggestion.setSelected(false);
                    }
                });

            }
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            Log.d(TAG, "onMissing");
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            Log.d(TAG, "onDone");
            updateFaceCount(mOverlay.getSize());
            mOverlay.remove(mFaceGraphic);
        }

        private boolean checkSuggestedEmotion() {
            Log.d(TAG, "checkSuggestedEmotion");
            boolean isAllFaceSameEmotion = false;
            Set<GraphicOverlay.Graphic> graphics = mOverlay.getFaceGraphics();

            for (GraphicOverlay.Graphic graphic: graphics) {
                FaceGraphic faceGraphic = (FaceGraphic) graphic;
                if (faceGraphic.isEmotionDetected()) {
                    isAllFaceSameEmotion = true;
                } else {
                    isAllFaceSameEmotion = false;
                    break;
                }
            }
            Log.d(TAG, "checkSuggestedEmotion returns " + isAllFaceSameEmotion);
            return isAllFaceSameEmotion;
        }

        private void updateFaceCount(int count) {
            faceDetectedCount = count;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    faceCount.setText("Faces " + faceDetectedCount);
                }
            });

        }
    }
}
