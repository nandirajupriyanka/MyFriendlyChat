package com.priyankanandiraju.friendlychat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by priyankanandiraju on 6/26/18.
 */

public class BitmapUtils {

    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";

    /**
     * Creates the temporary image file in the cache directory.
     *
     * @return The temporary image file.
     * @throws IOException Thrown if there is an error creating the file
     */
    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalCacheDir();

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /**
     * Create a Bitmap object
     * @param imagePath which needs to converted to Bitmap
     * @return Bitmap
     */
    @Nullable
    public static Bitmap createBitmap(String imagePath) {
        return BitmapFactory.decodeFile(imagePath);
    }

}
