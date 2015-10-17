# ImageCroper
用于生成正方形图片的图片剪裁工具
#Feature
缩放模式：缩放裁剪图片，固定输出为正方形；
完整模式：将为非正方形图片添加黑色\白色背景并生成正方形图片。
#Usage
将组建作为Library Module导入工程

在AndroidManifest.xml中添加：
```xml
<activity android:name="com.ilikelabs.imageCroper.Croper"
            android:screenOrientation="portrait"/>
```
设置文件输出路径与结果获取：
```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
        switch (requestCode) {
            case OPEN_GELLARY:
                //打开裁剪界面
                new CropHelper(data.getData()).outPut("设置输出文件的文件夹").start(this);
                break;
            case Croper.REQUEST_CROP_IMAGE:
                //获取完整的结果的文件路径
                outputPath = "file://" + data.getExtras().getString(Croper.OUTPUT_IMAGE_PATH);
                break;
        }
    } 
}
```
#ScreenShot
![screen shot](https://raw.githubusercontent.com/taolulu/ImageCroper/master/Screenshot_2015-10-17-17-01-36_com.practice.taxer.png)
