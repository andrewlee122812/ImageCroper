package com.ilikelabs.commonUtils.utils.frescoUtils;

import android.graphics.Bitmap;
import android.net.Uri;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.IOException;

/**
 * Created by taXer on 15/8/6.
 */
public class FrescoUtils {
    public static void getRequestBitmap(Uri uri, RequestBitmapListener requestBitmapListener){
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchImageFromBitmapCache(ImageRequest.fromUri(uri), null);
        CloseableReference<CloseableImage> imageReference = null;
        try {
            imageReference = dataSource.getResult();
            if (imageReference != null) {
                CloseableBitmap image = (CloseableBitmap)imageReference.get();
                requestBitmapListener.getBitmap(image.getUnderlyingBitmap());
                // do something with the image
            }
        } finally {
            dataSource.close();
            CloseableReference.closeSafely(imageReference);
        }
    }


}
