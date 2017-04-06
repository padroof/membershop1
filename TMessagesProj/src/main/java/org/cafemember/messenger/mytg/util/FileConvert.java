package org.cafemember.messenger.mytg.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Masoud on 8/15/2016.
 */
public class FileConvert {

    private static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    public static String getStringFromBitmap(Bitmap bitmap) {
        return getStringFromByteArray(getBytesFromBitmap(bitmap));
    }

    private static String getStringFromByteArray(byte[] b){
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    private static Bitmap getBitmapFromByteArray(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    public static Bitmap getBitmapFromString(String jsonString) {
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

}