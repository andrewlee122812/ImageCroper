package com.ilikelabs.imageCroper;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by taXer on 15/7/13.
 */
public class CropHelper {

    private Uri imageUri;
    private Intent intent;
    private CropHelper cropHelper;
    private Bundle bundle;

    public CropHelper(Uri imageUri) {
        this.imageUri = imageUri;
        bundle = new Bundle();
        intent = new Intent();
        bundle.putParcelable(Croper.INPUT_IMAGE_PATH, imageUri);

    }

    public CropHelper outPut(String outPutPath){
        bundle.putString(Croper.OUTPUT_IMAGE_PATH, outPutPath);
        return this;
    }

    public void start(Activity activity){
        intent.putExtras(bundle);
        intent.setClass(activity, Croper.class);
        activity.startActivityForResult(intent, Croper.REQUEST_CROP_IMAGE);
    }
    public void start(Fragment fragment){
        intent.putExtras(bundle);
        intent.setClass(fragment.getActivity(), Croper.class);
        fragment.startActivityForResult(intent, Croper.REQUEST_CROP_IMAGE);
    }

}
