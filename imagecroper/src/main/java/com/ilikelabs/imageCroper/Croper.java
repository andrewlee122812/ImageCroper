package com.ilikelabs.imageCroper;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilikelabs.imageCroper.commonUtils.ContentProviderUriUtil;
import com.ilikelabs.imageCroper.commonUtils.LoadingBlockDialog;
import com.ilikelabs.imageCroper.commonUtils.SDCardUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class Croper extends Activity {

    private FrameLayout content;
    private TextView fullImageButtonText;
    private TextView cropImageButtonText;
    private ImageView cropModeIcon;
    private ImageView fullModeIcon;
    private ImageCropView surfaceView;
    public static final String INPUT_IMAGE_PATH= "input_image_path";
    public static final String OUTPUT_IMAGE_PATH= "output_image_path";
    public static final int REQUEST_CROP_IMAGE = 5731;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_croper);
        content = (FrameLayout) findViewById(R.id.contentPanel);
        final Handler handler = new Handler();

        final LoadingBlockDialog loadingBlockDialog = new LoadingBlockDialog(this, "加载中...", false);
        loadingBlockDialog.show();
        if(getIntent().getExtras()!= null && getIntent().getExtras().getParcelable(INPUT_IMAGE_PATH) != null){
            final Uri imageUri = getIntent().getExtras().getParcelable(INPUT_IMAGE_PATH);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
                    bfoOptions.inScaled = false;
                    bfoOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(ContentProviderUriUtil.getPath(Croper.this, imageUri), bfoOptions);
                    if(bfoOptions.outWidth <= 0 || bfoOptions.outHeight <= 0){
                        setResult(RESULT_FIRST_USER);
                        Croper.this.finish();
                        return;
                    }
                    RotateBitmapUtil rotateBitmap = new RotateBitmapUtil(bfoOptions.outWidth, bfoOptions.outHeight
                            , ContentProviderUriUtil.getOrientation(Croper.this, imageUri));
                    bfoOptions.inJustDecodeBounds = false;
                    bfoOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    final Bitmap icon = Bitmap.createBitmap(BitmapFactory.decodeFile(ContentProviderUriUtil.getPath(Croper.this, imageUri), bfoOptions), 0,0, bfoOptions.outWidth, bfoOptions.outHeight, rotateBitmap.getRotateMatrix(), true);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            surfaceView = new ImageCropView(Croper.this, icon);
                            content.addView(surfaceView);
                            loadingBlockDialog.dismiss();
                        }
                    }, 700);

                }
            }).start();

        }else {
            setResult(RESULT_CANCELED);
            this.finish();
        }
        setUpActionbar();
        cropModeIcon = (ImageView) findViewById(R.id.crop_mode_icon);
        fullModeIcon = (ImageView) findViewById(R.id.full_mode_icon);
        fullImageButtonText = (TextView) findViewById(R.id.full_button);
        cropImageButtonText = (TextView) findViewById(R.id.crop_button);
        cropImageButtonText.setTextColor(getResources().getColor(R.color.text_choosen_red));
        cropModeIcon.setImageResource(R.drawable.ic_crop_mode_selected);
    }

    private String saveBitmap(Bitmap bitmap){
        String savePath = SDCardUtils.getSDCardPath() + "/" + System.currentTimeMillis() + ".jpg";
        if( getIntent().getExtras()!=null && getIntent().getExtras().getString(OUTPUT_IMAGE_PATH) != null){
            savePath = getIntent().getExtras().getString(OUTPUT_IMAGE_PATH) + "/" + System.currentTimeMillis() + ".jpg";
        }
        File file = new File(savePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            bitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return savePath;

    }

    private void setUpActionbar(){
        View actionbarView = getLayoutInflater().inflate(R.layout.actionbar_layout, null);
        ActionBar.LayoutParams params=new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER );
        getActionBar().setCustomView(actionbarView, params);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setDisplayShowCustomEnabled(true);
        View cancelButton = actionbarView.findViewById(R.id.left_button);
        View confirmButton = actionbarView.findViewById(R.id.right_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Croper.this.finish();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = saveBitmap(surfaceView.outPut());
                Log.d("path", path);
                Bundle bundle = new Bundle();
                bundle.putString(OUTPUT_IMAGE_PATH, path);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                Croper.this.setResult(RESULT_OK, intent);
                Croper.this.finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(surfaceView != null){
            surfaceView.pauseDraw();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(surfaceView != null){
            surfaceView.startDraw();
        }

    }


    public void turnModeFull(View view){

        fullImageButtonText.setTextColor(getResources().getColor(R.color.text_choosen_red));
        fullModeIcon.setImageResource(R.drawable.ic_full_mode_selected);
        cropImageButtonText.setTextColor(getResources().getColor(R.color.light_gray));
        cropModeIcon.setImageResource(R.drawable.ic_crop_mode);
        surfaceView.switchToFullMode();
    }

    public void turnModeCrop(View view){

        cropImageButtonText.setTextColor(getResources().getColor(R.color.text_choosen_red));
        cropModeIcon.setImageResource(R.drawable.ic_crop_mode_selected);
        fullImageButtonText.setTextColor(getResources().getColor(R.color.light_gray));
        fullModeIcon.setImageResource(R.drawable.ic_full_mode);
        surfaceView.switchToCropMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(surfaceView != null){
            surfaceView.recycleSorce();
        }

//        surfaceView.release();

    }
}
