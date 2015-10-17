package com.ilikelabs.commonUtils.utils;

import android.os.Environment;

import java.text.SimpleDateFormat;

/**
 * Created by Yulu on 2014/12/8.
 */
public class ImageSaveUtil {
    private final static String ALBUM_PATH
            = Environment.getExternalStorageDirectory() + "/DCIM/meifujia/";
    private final static String DEFAULT_PATH
            = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";

    public static String path (){
        return ALBUM_PATH;
    }

    public static String imageSavePath (){
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd" );
        String imageName = format.format(System.currentTimeMillis())+ "_" + System.currentTimeMillis() +".jpg";
        return DEFAULT_PATH + imageName;
    }

    public static String capturedImagePath (){
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHHmmss" );
        String imageName = "postImage" + format.format(System.currentTimeMillis()) + ".jpg";
        return ALBUM_PATH + imageName;
    }
}
