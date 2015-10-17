/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.ilikelabs.imageCroper;

import android.graphics.Matrix;

/*
 * Modified from original in AOSP.
 */
public class RotateBitmapUtil {

    private int rotation;
    private int height;
    private int width;

    public RotateBitmapUtil(int width, int height, int rotation) {
        this.width = width;
        this.height = height;
        this.rotation = rotation % 360;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

//    public Bitmap getBitmap() {
//        return bitmap;
//    }

//    public void setBitmap(Bitmap bitmap) {
//        this.bitmap = bitmap;
//    }

    public Matrix getRotateMatrix() {
        // By default this is an identity matrix
        Matrix matrix = new Matrix();
        if (rotation != 0) {
            // We want to do the rotation at origin, but since the bounding
            // rectangle will be changed after rotation, so the delta values
            // are based on old & new width/height respectively.
            int cx = width / 2;
            int cy = height / 2;
            matrix.preTranslate(-cx, -cy);
            matrix.postRotate(rotation);
            matrix.postTranslate(getWidth() / 2, getHeight() / 2);
        }
        return matrix;
    }

    public boolean isOrientationChanged() {
        return (rotation / 90) % 2 != 0;
    }

    public int getHeight() {
        if (isOrientationChanged()) {
            return width;
        } else {
            return height;
        }
    }

    public int getWidth() {
        if (isOrientationChanged()) {
            return height;
        } else {
            return width;
        }
    }

}

