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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * View which displays a bitmap containing a face along with overlay graphics that identify the
 * locations of detected facial landmarks.
 */
public class FaceView extends View {
    private static final String TAG = "ProjectAlex_FaceView";

    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;
    EmotionEnums mood;
    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;
    final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

    public FaceView(Context context) {
        super(context);
    }

    /**
         * Sets the bitmap background and the associated face detections.
         */

    public String setContent(Bitmap bitmap, SparseArray<Face> faces, EmotionEnums mood) {
        Log.d(TAG, "setContent");
        float average = 0.0f;
        mBitmap = bitmap;
        mFaces = faces;
        this.mood = mood;
        EmotionEnums emotion = mood;
        if(mood == EmotionEnums.WINKING)
        {
            average = checkWink();
            invalidate();
            return (EmotionEnums.WINKING +" :"+ average);
        }
        if(mood ==  EmotionEnums.HAPPY) {
            average = checkMood();
            if (average > 0.6f ) {
                emotion = EmotionEnums.HAPPY;
            }
        }
        if(mood ==  EmotionEnums.GRINNING) {
            average = checkMood();
            if(average >= 0.95f)
            {
                emotion=  EmotionEnums.GRINNING;
            }
        }
        if(mood ==  EmotionEnums.SAD) {
            average = checkMood();
            if( average < 0.2f)
            {
                emotion = EmotionEnums.SAD;
            }

        }
//            if(average >= 0.95f)
//        {
//
//            emotion=  EmotionEnums.GRINNING;
//        }
//        else if(average >= 0.7f && average < 0.95f)
//        {
//            emotion = EmotionEnums.HAPPY;
//        }
//        else if(average > 0.3f && average < 0.7f)
//        {
//           emotion = EmotionEnums.NEUTRAL;
//        }
//        else if( average < 0.2f)
//        {
//            emotion = EmotionEnums.SAD;
//        }
            invalidate();
            return (emotion+" :"+ average);
        }


    float checkMood() {
    float average = 0;
    for (int i = 0; i < mFaces.size(); i++) {
        Face face = mFaces.valueAt(i);
        average += face.getIsSmilingProbability();
    }
    average/=mFaces.size();
        return average;
}
    float checkWink() {
        float average = 0;
        for (int i = 0; i < mFaces.size(); i++) {
            Face face = mFaces.valueAt(i);
            if(face.getIsSmilingProbability() > 0.2f && (face.getIsRightEyeOpenProbability() < 0.1f && face.getIsLeftEyeOpenProbability() > 0.3f))
            {
                average += face.getIsRightEyeOpenProbability();
            }
            else if (face.getIsSmilingProbability() > 0.2f &&(face.getIsLeftEyeOpenProbability() < 0.1f && face.getIsRightEyeOpenProbability() > 0.3f))
            average += face.getIsLeftEyeOpenProbability();
        }
        average/=mFaces.size();
        return  average;
    }
    /**
     * Draws the bitmap background and the associated face landmarks.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");
        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
            drawFaceAnnotations(canvas, scale);
            // Draws a bounding box around the face.
                drawFaceBox(canvas, scale);

        }
    }

    /**
     * Draws the bitmap background, scaled to the device size.  Returns the scale for future use in
     * positioning the facial landmark graphics.
     */
    public void drawFaceBox(Canvas canvas, double scale) {
        //paint should be defined as a member variable rather than
        //being created on each onDraw request, but left here for
        //emphasis.
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(BOX_STROKE_WIDTH);
        paint.setTextSize(ID_TEXT_SIZE);
        paint.setColor(selectedColor);

        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;
        int i;
        for(  i = 0; i < mFaces.size(); i++ ) {
            Face face = mFaces.valueAt(i);
            left = (float) ( face.getPosition().x * scale );
            top = (float) ( face.getPosition().y * scale );
            right = (float) scale * ( face.getPosition().x + face.getWidth() );
            bottom = (float) scale * ( face.getPosition().y + face.getHeight() );

            float x = face.getPosition().x + face.getWidth() / 2;
            float y = face.getPosition().y + face.getHeight() / 2;
            canvas.drawRect( left, top, right, bottom, paint );
            emotionDetection(face,canvas,x,y,paint);
            //logFaceData();
        }
    }

    public EmotionEnums emotionDetection(Face face, Canvas canvas, float x, float y, Paint paint)
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

         if(face.getIsSmilingProbability() > 0.2f && ((face.getIsRightEyeOpenProbability() < 0.1f && face.getIsLeftEyeOpenProbability() > 0.3f) || (face.getIsLeftEyeOpenProbability() < 0.1f && face.getIsRightEyeOpenProbability() > 0.3f)))
        {
            canvas.drawText(EmotionEnums.WINKING +"right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, paint);
            canvas.drawText(EmotionEnums.WINKING +"left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, paint);
            return  EmotionEnums.WINKING ;
        }
    else if(face.getIsSmilingProbability() >= 0.95f)
    {
        canvas.drawText(EmotionEnums.GRINNING + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, paint);
        return EmotionEnums.GRINNING;
    }
    else if(face.getIsSmilingProbability() >= 0.7f)
    {
        canvas.drawText(EmotionEnums.HAPPY + String.format("%.2f",face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, paint);
        return  EmotionEnums.HAPPY;
    }
    else if(face.getIsSmilingProbability() > 0.3f && face.getIsSmilingProbability() < 0.6f)
    {
        canvas.drawText(EmotionEnums.NEUTRAL + String.format("%.2f",face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, paint);
        return EmotionEnums.NEUTRAL;
    }
    else if(face.getIsSmilingProbability() < 0.2f)
    {
        canvas.drawText(EmotionEnums.SAD + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, paint);
        return EmotionEnums.SAD;
    }
        Log.d(" isSmilingProbability",  face.getIsSmilingProbability()+"");
        return null;
    }


    private double drawBitmap(Canvas canvas) {
        Log.d(TAG, "drawBitmap");
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        Rect destBounds = new Rect(0, 0, (int)(imageWidth * scale), (int)(imageHeight * scale));
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }

    /**
     * Draws a small circle for each detected landmark, centered at the detected landmark position.
     * <p>
     *
     * Note that eye landmarks are defined to be the midpoint between the detected eye corner
     * positions, which tends to place the eye landmarks at the lower eyelid rather than at the
     * pupil position.
     */
    private void drawFaceAnnotations(Canvas canvas, double scale) {
        Log.d(TAG, "drawFaceAnnotations");
        Paint paint = new Paint();
        paint.setColor(selectedColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (int i = 0; i < mFaces.size(); ++i) {
            Face face = mFaces.valueAt(i);

            for (Landmark landmark : face.getLandmarks()) {
                double cx = (landmark.getPosition().x * scale);
                double cy = (landmark.getPosition().y * scale);
                canvas.drawCircle((float) cx, (float) cy, 10, paint);
            }
        }
    }
}
