package com.practice.taxer.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ilikelabs.imageCroper.CropHelper;
import com.ilikelabs.imageCroper.Croper;

import java.io.File;

public class MainActivity extends Activity {

    final static int OPEN_GELLARY = 1;

    TextView path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = (TextView)findViewById(R.id.path);
        Intent intent;
        intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_GELLARY);
    }

    public void onChoosePic(View v){
        Intent intent;
        intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_GELLARY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String outputPath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case OPEN_GELLARY:
                    new CropHelper(data.getData()).outPut(outputPath).start(this);
                    break;
                case Croper.REQUEST_CROP_IMAGE:
                    outputPath = "file://" + data.getExtras().getString(Croper.OUTPUT_IMAGE_PATH);
                    path.setText("OutputPath:" + outputPath);
                    break;
            }
        } else if (resultCode == Activity.RESULT_FIRST_USER) {
            switch (requestCode) {
                case Croper.REQUEST_CROP_IMAGE:
                    Toast.makeText(this,"图片不存在", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


}
