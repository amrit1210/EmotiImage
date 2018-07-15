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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final String TAG = "ProjectAlex_FaceGraphic";

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 70.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.GREEN,
            Color.RED
    };

    private static int mCurrentColorIndex = 1;
    private int colorInEmotion = 0;
    private int colorNotInEmotion = 1;
    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;
    private volatile Face mFace;
    private int mFaceId;

    private boolean isEmotionDetected = false;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);
        Log.d(TAG, "FaceGraphic");
        setColor(mCurrentColorIndex);
    }

    void setColor(int mCurrentColorIndex) {
        Log.d(TAG, "setColor " + mCurrentColorIndex);
        int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        Log.d(TAG, "setId");
        mFaceId = id;
    }



    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        Log.d(TAG, "updateFace");
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "draw");
        Face face = mFace;
        if (face == null) {
            return;
        }
        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        drawFaceAnnotations(canvas, face);

        if (face.getIsSmilingProbability() > 0.7f) {
            Log.d(TAG, "getIsSmilingProbability " + face.getIsSmilingProbability());
            Message msg = new Message();
            msg.obj = EmotionEnums.HAPPY;
            FaceTrackerActivity.getHandler().sendMessage(msg);

            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.HAPPY) {
                isEmotionDetected = true;
                setColor(colorInEmotion);
            }
        } else {
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.HAPPY) {
                Log.d(TAG, "NOT HAPPY");
                canvas.drawText("NOT " + EmotionEnums.HAPPY.name(), left, top , mIdPaint);
                isEmotionDetected = false;
                setColor(colorNotInEmotion);
            }
        }

        if (face.getIsSmilingProbability() > 0.2f && face.getIsSmilingProbability() < 0.7f) {
            Log.d(TAG, "getIsSmilingProbability " + face.getIsSmilingProbability());
            Message msg = new Message();
            msg.obj = EmotionEnums.NEUTRAL;
            FaceTrackerActivity.getHandler().sendMessage(msg);

            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.NEUTRAL) {
                isEmotionDetected = true;
                setColor(colorInEmotion);
            }
        } else {
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.NEUTRAL) {
                Log.d(TAG, "NOT NEUTRAL");
                canvas.drawText("NOT " + EmotionEnums.NEUTRAL.name(), left, top , mIdPaint);
                isEmotionDetected = false;
                setColor(colorNotInEmotion);
            }
        }

        if (face.getIsSmilingProbability() < 0.25f) {
            Log.d(TAG, "getIsSmilingProbability " + face.getIsSmilingProbability());
            Message msg = new Message();
            msg.obj = EmotionEnums.SAD;
            FaceTrackerActivity.getHandler().sendMessage(msg);
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.SAD) {
                isEmotionDetected = true;
                setColor(colorInEmotion);
            }
        } else {
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.SAD) {
                Log.d(TAG, "NOT SAD");
                canvas.drawText("NOT " + EmotionEnums.SAD.name(), left, top , mIdPaint);
                isEmotionDetected = false;
                setColor(colorNotInEmotion);
            }
        }

        if (face.getIsSmilingProbability() > 0.2f && isWinkingLandmarksDetected(face.getLandmarks()) && (face.getIsRightEyeOpenProbability() < 0.1f && face.getIsLeftEyeOpenProbability() > 0.4f) || (face.getIsLeftEyeOpenProbability() < 0.1f && face.getIsRightEyeOpenProbability() > 0.4f)) {
            Log.d(TAG, "getIsSmilingProbability " + face.getIsSmilingProbability());
            Log.d(TAG, "getIsRightEyeOpenProbability " + face.getIsRightEyeOpenProbability());
            Log.d(TAG, "getIsLeftEyeOpenProbability " + face.getIsLeftEyeOpenProbability());
            Message msg = new Message();
            msg.obj = EmotionEnums.WINKING;
            FaceTrackerActivity.getHandler().sendMessage(msg);
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.WINKING) {
                isEmotionDetected = true;
                setColor(colorInEmotion);
            }
        } else {
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.WINKING) {
                Log.d(TAG, "NOT WINKING");
                canvas.drawText("NOT " + EmotionEnums.WINKING.name(), left, top , mIdPaint);
                isEmotionDetected = false;
                setColor(colorNotInEmotion);
            }
        }

        if (face.getIsSmilingProbability() >= 0.95f && isOpenMouthLandmarksDetected(face.getLandmarks()) && isMouthOpen(face, canvas)) {
            Log.d(TAG, "getIsSmilingProbability " + face.getIsSmilingProbability());
            Message msg = new Message();
            msg.obj = EmotionEnums.GRINNING;
            FaceTrackerActivity.getHandler().sendMessage(msg);
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.GRINNING) {
                isEmotionDetected = true;
                setColor(colorInEmotion);
            }
        } else {
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.GRINNING) {
                Log.d(TAG, "NOT GRINNING");
                canvas.drawText("NOT " + EmotionEnums.GRINNING.name(), left, top , mIdPaint);
                isEmotionDetected = false;
                setColor(colorNotInEmotion);
            }
        }

        if(isPoutDetected(face, canvas)) {
            Log.d(TAG, "Pout Detected ");
            Message msg = new Message();
            msg.obj = EmotionEnums.POUT;
            FaceTrackerActivity.getHandler().sendMessage(msg);
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.POUT) {
                isEmotionDetected = true;
                setColor(colorInEmotion);
            }
        } else {
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.POUT) {
                Log.d(TAG, "NOT POUT");
                canvas.drawText("NOT " + EmotionEnums.POUT.name(), left, top , mIdPaint);
                isEmotionDetected = false;
                setColor(colorNotInEmotion);
            }
        }



        if(isBlinkingLandmarksDetected(face.getLandmarks()) && face.getIsRightEyeOpenProbability() < 0.09 && face.getIsRightEyeOpenProbability() < 0.09) {
            Log.d(TAG, "Blinking Detected ");
            Message msg = new Message();
            msg.obj = EmotionEnums.BLINKING;
            FaceTrackerActivity.getHandler().sendMessage(msg);
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.BLINKING) {
                isEmotionDetected = true;
                setColor(colorInEmotion);
            }
        } else {
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.BLINKING) {
                Log.d(TAG, "NOT BLINKING");
                canvas.drawText("NOT " + EmotionEnums.BLINKING.name(), left, top , mIdPaint);
                isEmotionDetected = false;
                setColor(colorNotInEmotion);
            }
        }

        /*if(isSurprisedDetected(face, canvas)) {
            Log.d(TAG, "Surprised Detected ");
            Message msg = new Message();
            msg.obj = EmotionEnums.SURPRISED;
            FaceTrackerActivity.getHandler().sendMessage(msg);
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.SURPRISED) {
                isEmotionDetected = true;
                setColor(colorInEmotion);
            }
        } else {
            if (FaceTrackerActivity.isGroupSuggestion && FaceTrackerActivity.currentMood == EmotionEnums.SURPRISED) {
                Log.d(TAG, "NOT SURPRISED");
                canvas.drawText("NOT " + EmotionEnums.SURPRISED.name(), left, top , mIdPaint);
                isEmotionDetected = false;
                setColor(colorNotInEmotion);
            }
        }*/

    }

    private void drawFaceAnnotations(Canvas canvas, Face face) {
        Log.d(TAG, "drawFaceAnnotations");
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (Landmark landmark : face.getLandmarks()) {
            int cx = (int) translateX(landmark.getPosition().x);
            int cy = (int) translateY (landmark.getPosition().y);
            canvas.drawCircle(cx, cy, 10, paint);
        }
    }

    public boolean isEmotionDetected() {
        Log.d(TAG, "isEmotionDetected Returns " + isEmotionDetected);
        return isEmotionDetected;
    }

    public boolean isPoutDetected(Face face, Canvas canvas) {
        Log.d(TAG, "isPoutDetected");

        if (face == null) {
            return false;
        }

        if (isOpenMouthLandmarksDetected(face.getLandmarks())) {
            Log.i(TAG, "draw: Mouth Open >> found all the points");

            int noseBaseLandmarkIndex = getPoutLandmarkIndex(face.getLandmarks(), Landmark.NOSE_BASE);
            Coordinate noseBase = new Coordinate();
            noseBase.setX(translateX(face.getLandmarks().get(noseBaseLandmarkIndex).getPosition().x));
            noseBase.setY(translateY(face.getLandmarks().get(noseBaseLandmarkIndex).getPosition().y));
            canvas.drawCircle(noseBase.getX(), noseBase.getY(), 10, mIdPaint);

            int bottomMouthLandmarkIndex = getPoutLandmarkIndex(face.getLandmarks(), Landmark.BOTTOM_MOUTH);
            Coordinate bottomMouth = new Coordinate();
            bottomMouth.setX(translateX(face.getLandmarks().get(bottomMouthLandmarkIndex).getPosition().x));
            bottomMouth.setY(translateY(face.getLandmarks().get(bottomMouthLandmarkIndex).getPosition().y));
            canvas.drawCircle(bottomMouth.getX(), bottomMouth.getY(), 10, mIdPaint);

            int leftMouthLandmarkIndex = getPoutLandmarkIndex(face.getLandmarks(), Landmark.LEFT_MOUTH);
            Coordinate leftMouth = new Coordinate();
            leftMouth.setX(translateX(face.getLandmarks().get(leftMouthLandmarkIndex).getPosition().x));
            leftMouth.setY(translateY(face.getLandmarks().get(leftMouthLandmarkIndex).getPosition().y));
            canvas.drawCircle(leftMouth.getX(), leftMouth.getY(), 10, mIdPaint);

            int rightMouthLandmarkIndex = getPoutLandmarkIndex(face.getLandmarks(), Landmark.RIGHT_MOUTH);
            Coordinate rightMouth = new Coordinate();
            rightMouth.setX(translateX(face.getLandmarks().get(rightMouthLandmarkIndex).getPosition().x));
            rightMouth.setY(translateY(face.getLandmarks().get(rightMouthLandmarkIndex).getPosition().y));
            canvas.drawCircle(rightMouth.getX(), rightMouth.getY(), 10, mIdPaint);

            Coordinate centerMouth = new Coordinate();
            centerMouth.setX((leftMouth.getX() + rightMouth.getX()) / 2);
            centerMouth.setY((leftMouth.getY() + rightMouth.getY()) / 2);
            canvas.drawCircle(centerMouth.getX(), centerMouth.getY(), 10, mIdPaint);

            double distanceCenterAndBottom = Coordinate.distanceBetweenCoordinates(centerMouth, bottomMouth);
            double distanceNoseAndCenter = Coordinate.distanceBetweenCoordinates(noseBase, centerMouth);

            boolean isMouthClosed = false;
            Log.d(TAG, "distanceCenterAndNose " + distanceCenterAndBottom + " distanceNoseAndCenter " + distanceNoseAndCenter + " Length " + (distanceCenterAndBottom - distanceNoseAndCenter));
            if ((distanceCenterAndBottom - distanceNoseAndCenter) < 0) {
                Log.d(TAG, "Mouth is Closed");
                isMouthClosed = true;
            } else {
                Log.d(TAG, "Mouth is Open");
                isMouthClosed = false;
            }

            double distanceNoseAndBottom = Coordinate.distanceBetweenCoordinates(noseBase, bottomMouth);
            double distanceLeftAndRight = Coordinate.distanceBetweenCoordinates(leftMouth, rightMouth);
            Log.d(TAG, "NEW LOGIC DISTANCE distance 3 " + distanceNoseAndBottom + " distance 4 " + distanceLeftAndRight + " diff " + (distanceNoseAndBottom - distanceLeftAndRight));

            if (isMouthClosed && ((distanceNoseAndBottom - distanceLeftAndRight) < 20)) {
                Log.d(TAG, "Pout is Detected");
                return true;
            }
        }
        Log.d(TAG, "Pout is not Detected");
        return false;
    }

    private boolean isBlinkingLandmarksDetected(List<Landmark> landmarks) {
        Log.d(TAG, "isBlinkingLandmarksDetected");
        boolean isDetected = false;
        int landmarkCount = 0;
        Log.d(TAG, "Landmarks Size " + landmarks.size());
        for (Landmark landmark :landmarks ) {
            Log.d(TAG, "Landmarks name " + landmark.getType());
            if(landmark.getType() == Landmark.LEFT_EYE || landmark.getType() == Landmark.RIGHT_EYE) {
                landmarkCount++;
            }
            if (landmarkCount == 2) {
                isDetected = true;
                break;
            }
        }
        Log.d(TAG, "isBlinkingLandmarksDetected returns " + isDetected);
        return isDetected;
    }

    private boolean isOpenMouthLandmarksDetected(List<Landmark> landmarks) {
        Log.d(TAG, "isOpenMouthLandmarksDetected");
        boolean isDetected = false;
        int landmarkCount = 0;
        Log.d(TAG, "Landmarks Size " + landmarks.size());
        for (Landmark landmark :landmarks ) {
            Log.d(TAG, "Landmarks name " + landmark.getType());
            if(landmark.getType() == Landmark.LEFT_MOUTH || landmark.getType() == Landmark.RIGHT_MOUTH || landmark.getType() == Landmark.BOTTOM_MOUTH || landmark.getType() == Landmark.NOSE_BASE) {
                landmarkCount++;
            }
            if (landmarkCount == 4) {
                isDetected = true;
                break;
            }
        }
        Log.d(TAG, "isOpenMouthLandmarksDetected returns " + isDetected);
        return isDetected;
    }

    private boolean isWinkingLandmarksDetected(List<Landmark> landmarks) {
        Log.d(TAG, "isWinkingLandmarksDetected");
        boolean isDetected = false;
        int landmarkCount = 0;
        Log.d(TAG, "Landmarks Size " + landmarks.size());
        for (Landmark landmark :landmarks ) {
            Log.d(TAG, "Landmarks name " + landmark.getType());
            if(landmark.getType() == Landmark.LEFT_EYE || landmark.getType() == Landmark.RIGHT_EYE) {
                landmarkCount++;
            }
            if (landmarkCount == 2) {
                isDetected = true;
                break;
            }
        }
        Log.d(TAG, "isWinkingLandmarksDetected returns " + isDetected);
        return isDetected;
    }

    private boolean isMouthOpen(Face face, Canvas canvas) {
        Log.d(TAG, "isMouthOpen");
        boolean isMouthOpen;

        int noseBaseLandmarkIndex = getPoutLandmarkIndex(face.getLandmarks(), Landmark.NOSE_BASE);
        Coordinate noseBase = new Coordinate();
        noseBase.setX(translateX(face.getLandmarks().get(noseBaseLandmarkIndex).getPosition().x));
        noseBase.setY(translateY(face.getLandmarks().get(noseBaseLandmarkIndex).getPosition().y));
        canvas.drawCircle(noseBase.getX(), noseBase.getY(), 10, mIdPaint);

        int bottomMouthLandmarkIndex = getPoutLandmarkIndex(face.getLandmarks(), Landmark.BOTTOM_MOUTH);
        Coordinate bottomMouth = new Coordinate();
        bottomMouth.setX(translateX(face.getLandmarks().get(bottomMouthLandmarkIndex).getPosition().x));
        bottomMouth.setY(translateY(face.getLandmarks().get(bottomMouthLandmarkIndex).getPosition().y));
        canvas.drawCircle(bottomMouth.getX(), bottomMouth.getY(), 10, mIdPaint);

        int leftMouthLandmarkIndex = getPoutLandmarkIndex(face.getLandmarks(), Landmark.LEFT_MOUTH);
        Coordinate leftMouth = new Coordinate();
        leftMouth.setX(translateX(face.getLandmarks().get(leftMouthLandmarkIndex).getPosition().x));
        leftMouth.setY(translateY(face.getLandmarks().get(leftMouthLandmarkIndex).getPosition().y));
        canvas.drawCircle(leftMouth.getX(), leftMouth.getY(), 10, mIdPaint);

        int rightMouthLandmarkIndex = getPoutLandmarkIndex(face.getLandmarks(), Landmark.RIGHT_MOUTH);
        Coordinate rightMouth = new Coordinate();
        rightMouth.setX(translateX(face.getLandmarks().get(rightMouthLandmarkIndex).getPosition().x));
        rightMouth.setY(translateY(face.getLandmarks().get(rightMouthLandmarkIndex).getPosition().y));
        canvas.drawCircle(rightMouth.getX(), rightMouth.getY(), 10, mIdPaint);

        Coordinate centerMouth = new Coordinate();
        centerMouth.setX((leftMouth.getX() + rightMouth.getX()) / 2);
        centerMouth.setY((leftMouth.getY() + rightMouth.getY()) / 2);
        canvas.drawCircle(centerMouth.getX(), centerMouth.getY(), 10, mIdPaint);

        double distanceCenterAndBottom = Coordinate.distanceBetweenCoordinates(centerMouth, bottomMouth);
        double distanceNoseAndCenter = Coordinate.distanceBetweenCoordinates(noseBase, centerMouth);


        Log.d(TAG, "distanceCenterAndBootm " + distanceCenterAndBottom + " distanceNoseAndCenter " + distanceNoseAndCenter + " Length " + (distanceCenterAndBottom - distanceNoseAndCenter));

        if (FaceTrackerActivity.currentMood == EmotionEnums.POUT) { //Existing Logic
            if ((distanceCenterAndBottom - distanceNoseAndCenter) < 0) {
                Log.i(TAG, "Logic Pout Mouth is CLOSED ");
                isMouthOpen = false;
            } else {
                Log.i(TAG, "Logic Pout Mouth is OPEN ");
                isMouthOpen = true;
            }
        } else { //For Other Grinning n All
            if ((centerMouth.getY() - bottomMouth.getY()) < (-60)) {
                Log.i(TAG, "Logic Non Pout Mouth is OPENED ");
                isMouthOpen = true;
            } else {
                Log.i(TAG, "Logic Non Pout Mouth is CLOSED ");
                isMouthOpen = false;
            }

        }

        Log.d(TAG, "isMouthOpen returns " + isMouthOpen);
        return isMouthOpen;
    }

    private int getPoutLandmarkIndex(List<Landmark> landmarks, int poutLandmarkName) {
        Log.d(TAG, "getPoutLandmarkIndex");
        for (int index = 0; index < landmarks.size(); index++) {
            if (landmarks.get(index).getType() == poutLandmarkName) {
                return index;
            }
        }
        return -1;
    }

    public boolean isSurprisedDetected(Face face, Canvas canvas) {
        Log.d(TAG, "isSurprisedDetected");
        if (face == null) {
            return false;
        }

        if (isOpenMouthLandmarksDetected(face.getLandmarks())) {
            if (isMouthOpen(face, canvas) && face.getIsLeftEyeOpenProbability() > 0.8  && face.getIsRightEyeOpenProbability() > 0.8) {
                Log.d(TAG, "Surprised is Detected");
                return true;
            }
        }
        Log.d(TAG, "Surprised is not Detected");
        return false;
    }

}
