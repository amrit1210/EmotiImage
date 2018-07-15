package com.google.android.gms.samples.vision.face.facetracker;

/**
 * Created by amritkaur on 10-04-2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class ImageUtils {
    private static final String TAG = "ProjectAlex_ImageUtils";

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        Log.d(TAG, "calculateInSampleSize");
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //Get Path Image file
    public final static String getRealPathFromURI(Context context, Uri contentUri) {
        Log.d(TAG, "getRealPathFromURI");
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                Log.d(TAG, "getRealPathFromURI returns " + cursor.getString(column_index));
                return cursor.getString(column_index);
            } else {
                Log.d(TAG, "Cursor is Null");
                Log.d(TAG, "getRealPathFromURI returns " + contentUri.toString());
                return contentUri.toString();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    //Rotate Bitmap
    public final static Bitmap rotate(Bitmap bitmap, float degrees) {
        Log.d(TAG, "rotate");
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

            Bitmap changedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            if (bitmap != changedBitmap) {
                bitmap.recycle();
                bitmap = changedBitmap;
            }
        }
        return bitmap;
    }


    public static Bitmap getBitmap(String filePath, int width, int height) {
        Log.d(TAG, "getBitmap");
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = ImageUtils.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        bitmap = adjustOrientation(bitmap, filePath);
        return bitmap;
    }

    public static Bitmap adjustOrientation(Bitmap bitmap, String filePath) {
        if (bitmap != null) {
            try {
                ExifInterface ei = new ExifInterface(filePath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Log.d(TAG, "Original Orientation " + orientation);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        bitmap = ImageUtils.rotate(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        bitmap = ImageUtils.rotate(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        bitmap = ImageUtils.rotate(bitmap, 270);
                        break;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
