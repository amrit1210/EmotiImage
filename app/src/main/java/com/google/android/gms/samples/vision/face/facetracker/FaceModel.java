package com.google.android.gms.samples.vision.face.facetracker;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by amritkaur on 5/10/2017.
 */

public class FaceModel {
    private Bitmap bitmap;
    private EmotionEnums mood;
    private Uri uri;

    public FaceModel(Bitmap bitmap, EmotionEnums mood, Uri uri)
    {
        this.bitmap = bitmap;
        this.mood = mood;
        this.uri = uri;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public EmotionEnums getMood() {
        return mood;
    }

    public void setMood(EmotionEnums mood) {
        this.mood = mood;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
