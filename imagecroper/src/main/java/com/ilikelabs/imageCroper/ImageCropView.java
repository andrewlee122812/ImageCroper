package com.ilikelabs.imageCroper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


import com.ilikelabs.imageCroper.commonUtils.DensityUtil;
import com.ilikelabs.imageCroper.commonUtils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by taXer on 15/7/6.
 */
public class ImageCropView extends SurfaceView implements SurfaceHolder.Callback,Runnable,View.OnTouchListener {
    private Context context;
    private SurfaceHolder holder;
    private Bitmap icon;
    private Paint paint;
    private boolean running=true;

    private RectF imageRect;//图片的rect
    private float offsetX=0,offsetY=0;//点击点离图片左上角的距离
    private float iconHeight=0, iconWidth = 0;

    private Path topMask;
    private Path leftMask;
    private Path rightMask;
    private Path bottomMask;

    private Paint maskPaint;
    private RectF windowRect;

    private float iconWDH;

    private Thread drawThread;

    private boolean drawing;

    private Map<Integer, TouchPoint> pointPool;
    private Map<Integer, Point> zoomInitialPoints;

    private final int NonTouch = 0;
    private final int SingleTouch = 1;

    private final int DRAG = 0;
    private final int ZOOM = 1;

    private int touchMode;

    private Point moveBasePoint = new Point();

    private Paint textPaint = new Paint();

    private int imageState;

    private final int ZoomingAtX = 0;
    private final int ZoomingAtY = 1;
    private int imageZoomState = -1 ;

    //缩放时的参考参数
    private Vector zoomReferenceVector;//开始缩放时，所有手指以缩放基准点为坐标原点的向量差
    private float zoomReferenceOffsetX=0,zoomReferenceOffsetY=0;//开始缩放时，缩放基准点的offset
    private float zoomReferenceIconWidth=0,zoomReferenceIconHeight=0;//开始缩放时，图片的尺寸

    private int cropMode = 0;
    private int cropImage = 0;
    private int fullImage = 1;

    private RectF fullImageModeRect ;

    private Paint imageBackgroundPaint;
    private Paint circlePaint;
    private int textSizeInDp = 15;
    private final String tip = "点击图片切换背景颜色";

    private Uri imageUri;


    public ImageCropView(Context context, Bitmap bitmap) {
        super(context);
        this.context=context;
        icon = bitmap;
        holder = this.getHolder();//获取holder
        holder.addCallback(this);
        this.setOnTouchListener(this);

    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        iconWDH = ((float)icon.getWidth())/((float)icon.getHeight());
        if(iconWDH < 1){
            iconWidth = getMeasuredWidth();
            iconHeight = iconWidth / iconWDH;
            imageRect = new RectF(0,0, iconWidth, iconHeight);
        }else {
            iconHeight = getMeasuredWidth();
            iconWidth = iconHeight * iconWDH;
            imageRect = new RectF(-(iconWidth - getMeasuredWidth())/2,0, iconWidth - (iconWidth - getMeasuredWidth())/2, iconHeight);
        }

        initFullImageModeRect();
        initMaskArea();

        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);

        textPaint.setTextSize(DensityUtil.dip2px(context, textSizeInDp));
        textPaint.setAntiAlias(true);
        textPaint.setColor(context.getResources().getColor(R.color.normal_gray));

        running=true;
        drawing = true;
        zoomInitialPoints = new HashMap<>();


        imageBackgroundPaint = new Paint();
        imageBackgroundPaint.setColor(context.getResources().getColor(R.color.background_white));

        pointPool = new HashMap<>();
        drawThread = new Thread(this);

        drawThread.start();

    }


    /**
     * 初始化表层半透明框
     *
     */
    private void initMaskArea(){
        maskPaint = new Paint();
        maskPaint.setColor(context.getResources().getColor(R.color.mask_area));

        topMask = new Path();
        leftMask = new Path();
        rightMask = new Path();
        bottomMask = new Path();

        float windowWidth = ((float)getMeasuredWidth());
        float topBorder = 0;
        float leftBorder = (getMeasuredWidth() - windowWidth) / 2f;


        topMask.moveTo(0,0);
        topMask.lineTo(0, topBorder);
        topMask.lineTo(getMeasuredWidth(), topBorder);
        topMask.lineTo(getMeasuredWidth(), 0);
        topMask.close();

        leftMask.moveTo(0, topBorder);
        leftMask.lineTo(0, topBorder + windowWidth);
        leftMask.lineTo(leftBorder, topBorder + windowWidth);
        leftMask.lineTo(leftBorder, topBorder);
        leftMask.close();

        rightMask.moveTo(windowWidth + leftBorder, topBorder);
        rightMask.lineTo(windowWidth + leftBorder, topBorder + windowWidth);
        rightMask.lineTo(getMeasuredWidth(), topBorder + windowWidth);
        rightMask.lineTo(getMeasuredWidth(), topBorder);
        rightMask.close();

        bottomMask.moveTo(0, topBorder + windowWidth);
        bottomMask.lineTo(0, getMeasuredHeight());
        bottomMask.lineTo(getMeasuredWidth(), getMeasuredHeight());
        bottomMask.lineTo(getMeasuredWidth(), windowWidth + topBorder);
        bottomMask.close();

        windowRect = new RectF(leftBorder, topBorder,windowWidth+ leftBorder, topBorder + windowWidth);

    }

    private void initFullImageModeRect(){
        float fullModeIconHeight ;
        float fullModeIconWidth ;
        if(iconWDH > 1){
            fullModeIconWidth = getMeasuredWidth();
            fullModeIconHeight = fullModeIconWidth/iconWDH;
            fullImageModeRect = new RectF(0, (fullModeIconWidth - fullModeIconHeight)/2f,fullModeIconWidth,  fullModeIconWidth - (fullModeIconWidth - fullModeIconHeight)/2f);
        }else {
            fullModeIconHeight = getMeasuredWidth();
            fullModeIconWidth = fullModeIconHeight*iconWDH;
            fullImageModeRect = new RectF((fullModeIconHeight - fullModeIconWidth)/2f, 0,fullModeIconHeight - (fullModeIconHeight - fullModeIconWidth)/2f,  fullModeIconHeight);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running=false;
//        if(icon != null){
//            icon.recycle();
//        }


    }

    @Override
    public void run() {
        int SLEEP_TIME=100;

        while (running ) {
            if(drawing){
                //开始画的时间    long start=System.currentTimeMillis();
                Canvas canvas = holder.lockCanvas();//获取画布
                if(canvas == null){
                    break;
                }
                canvas.drawColor(Color.WHITE);
                if(cropMode == cropImage){
                    canvas.drawBitmap(icon, null, imageRect, paint);
                }else {
                    canvas.drawRect(0,0,getMeasuredWidth(), getMeasuredWidth(), imageBackgroundPaint);
                    canvas.drawBitmap(icon, null, fullImageModeRect, paint);
                }
                canvas.drawPath(topMask, maskPaint);
                canvas.drawPath(leftMask, maskPaint);
                canvas.drawPath(rightMask, maskPaint);
                canvas.drawPath(bottomMask, maskPaint);
                if(cropMode == fullImage) {
                    canvas.drawText(tip,((float)getMeasuredWidth()/2f) - textPaint.measureText(tip)/2f, getMeasuredWidth() + DensityUtil.dip2px(context, 35), textPaint);
                }

                holder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
                //结束的时间   long end=System.currentTimeMillis();
            }

        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(cropMode == fullImage){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN: {
                    drawing = true;

                    if (imageBackgroundPaint.getColor() == context.getResources().getColor(R.color.background_white)) {
                        imageBackgroundPaint.setColor(context.getResources().getColor(R.color.background_black));
                    }else {
                        imageBackgroundPaint.setColor(context.getResources().getColor(R.color.background_white));
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {

                    drawing = false;
                    break;
                }
            }
            return true;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            //手按下的时候
            case MotionEvent.ACTION_DOWN: {
                paint.setAntiAlias(false);
                paint.setFilterBitmap(false);
                drawing = true;
                touchMode = SingleTouch;
                imageState = DRAG;

                TouchPoint touchPoint = new TouchPoint();
                touchPoint.x = (int) event.getX(0);
                touchPoint.y = (int) event.getY(0);

                int id = event.getPointerId(0);
                pointPool.put(id, touchPoint);
                initPoint((int)getCentroid()[0], (int)getCentroid()[1]);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                imageState = ZOOM;
                TouchPoint downTouchPoint = new TouchPoint();
                int index = event.getActionIndex();
                int id = event.getPointerId(index);
                downTouchPoint.x = (int) event.getX(index);
                downTouchPoint.y = (int) event.getY(index);
                pointPool.put(id, downTouchPoint);

                initPoint((int) getCentroid()[0], (int) getCentroid()[1]);
                initZoomBasePoints((int) getCentroid()[0], (int) getCentroid()[1]);


                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {


                int index = event.getActionIndex();
                int id = event.getPointerId(index);
                pointPool.remove(id);

                initPoint((int) getCentroid()[0], (int) getCentroid()[1]);
                if(pointPool.size() < 2){
                    imageState = DRAG;
                }else {
                    imageState = ZOOM;
                    initZoomBasePoints((int) getCentroid()[0], (int) getCentroid()[1]);
                }


                break;
            }
            //移动的时候
            case MotionEvent.ACTION_MOVE: {

                    //实时更新触摸点的坐标
                    for (int index = 0; index < event.getPointerCount(); index++) {
                        // get pointer id for data stored at this index
                        int id = event.getPointerId(index);

                        // get the data stored externally about this pointer.
                        pointPool.get(id).x = (int) event.getX(index);
                        pointPool.get(id).y = (int) event.getY(index);
                    }
                    moveBasePoint.x = (int)getCentroid()[0];
                    moveBasePoint.y = (int)getCentroid()[1];

                    if(imageState == ZOOM){
                        int vectorX ;
                        int vectorY ;
                        if(getZoomVector(moveBasePoint.x, moveBasePoint.y).getX() >=0)
                            vectorX = (int)getZoomVector(moveBasePoint.x, moveBasePoint.y).getX();
                        else vectorX = (int)-getZoomVector(moveBasePoint.x, moveBasePoint.y).getX();
                        if(getZoomVector(moveBasePoint.x, moveBasePoint.y).getY() >=0)
                            vectorY = (int)getZoomVector(moveBasePoint.x, moveBasePoint.y).getY();
                        else vectorY = (int)-getZoomVector(moveBasePoint.x, moveBasePoint.y).getY();

                        if(vectorX>=vectorY)
                            setZoomingRect(vectorX, ZoomingAtX,moveBasePoint.x, moveBasePoint.y);
                        else setZoomingRect(vectorY, ZoomingAtY,moveBasePoint.x, moveBasePoint.y);

                    }else if(imageState == DRAG){
                        drag(moveBasePoint.x, moveBasePoint.y);
                    }
                break;
            }
            case MotionEvent.ACTION_UP: {
                paint.setAntiAlias(true);
                paint.setFilterBitmap(true);
                int id = event.getPointerId(0);
                pointPool.remove(id);
                touchMode = NonTouch;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drawing = false;
                break;
            }
            default:
                break;
        }
        return true;
    }



    private void setZoomingRect(float zoomSize, int zoomingAt, int basePointX, int basePointY){
        float offsetXDY = zoomReferenceOffsetX/zoomReferenceOffsetY;
        float zoomYSize ;
        float zoomXSize ;
        float currentOffsetX ;
        float currentOffsetY ;

        float originWidth = ScreenUtils.getScreenWidth(context);



        if(imageZoomState != - 1 && imageZoomState != zoomingAt){
            initZoomBasePoints(basePointX, basePointY);
        }
        imageZoomState = zoomingAt;

        if(zoomingAt == ZoomingAtX){
            if(zoomReferenceVector.getX() > 0){
                zoomXSize = zoomSize - zoomReferenceVector.getX();
            }else {
                zoomXSize = zoomSize + zoomReferenceVector.getX();
            }
            if(zoomSize > 50){
                zoomXSize = zoomXSize * iconWidth/originWidth * 1.5f;
            }

            zoomYSize = zoomXSize/offsetXDY;
        }else {
            if(zoomReferenceVector.getY() > 0){
                zoomYSize = zoomSize - zoomReferenceVector.getY();
            }else {
                zoomYSize = zoomSize + zoomReferenceVector.getY();
            }
            if(zoomSize > 50){
                zoomYSize = zoomYSize * iconWidth/originWidth * 1.5f;
            }
            zoomXSize = zoomYSize * offsetXDY;

        }
//        Log.d("ZoomVector", "referenceVector:" + zoomReferenceVector.getX() + "," + zoomReferenceVector.getY());
//        Log.d("ZoomVector", "zoomSize:" + zoomXSize+ "," +zoomYSize + "----" + zoomSize + "----" + zoomingAt);



        if(zoomXSize >= 0){
            if(icon.getWidth() > originWidth*3){
                if(iconWidth >= icon.getWidth()){
                    initZoomBasePoints(basePointX, basePointY);
                    drag(basePointX,basePointY);
                    return;
                }
            }else if(iconWidth >= originWidth*3){
                initZoomBasePoints(basePointX, basePointY);
                drag(basePointX,basePointY);
                return;
            }

        }
//        else {
//            if (iconWidth <= (windowRect.right - windowRect.left) || iconHeight <= (windowRect.bottom - windowRect.top)) {
//                drag(basePointX,basePointY);
//                return;
//            }
//        }

        currentOffsetX = zoomReferenceOffsetX + zoomXSize / 2f;
        currentOffsetY = zoomReferenceOffsetY + zoomYSize / 2f;

//        Log.d("ZoomVector", "currentOffset" + currentOffsetX+ "," +currentOffsetY);


        imageRect.left = basePointX - currentOffsetX;
        imageRect.top = basePointY - currentOffsetY;

        iconWidth = zoomReferenceIconWidth + zoomXSize;
        iconHeight = iconWidth/iconWDH;
        imageRect.right = imageRect.left + iconWidth;
        imageRect.bottom = imageRect.top + iconHeight;

        offsetX = currentOffsetX;
        offsetY = currentOffsetY;


        if(imageRect.left > windowRect.left){
            imageRect.left = windowRect.left;
            imageRect.right =  imageRect.left+iconWidth;
            zoomInBorderCritical(zoomXSize);
            offsetX=basePointX-imageRect.left;
            offsetY=basePointY-imageRect.top;
        }
        if (imageRect.right <  windowRect.right) {
            imageRect.right =  windowRect.right;
            imageRect.left = imageRect.right-iconWidth;
            zoomInBorderCritical(zoomXSize);
            offsetX=basePointX-imageRect.left;
            offsetY=basePointY-imageRect.top;
        }
        if (imageRect.top > windowRect.top) {
            imageRect.top = windowRect.top;
            imageRect.bottom = imageRect.top+iconHeight;
            zoomInBorderCritical(zoomXSize);
            offsetX=basePointX-imageRect.left;
            offsetY=basePointY-imageRect.top;
        }
        if (imageRect.bottom < windowRect.bottom) {
            imageRect.bottom = windowRect.bottom;
            imageRect.top = imageRect.bottom-iconHeight;
            zoomInBorderCritical(zoomXSize);
            offsetX=basePointX-imageRect.left;
            offsetY=basePointY-imageRect.top;
        }



    }

    /**
     * 限制缩放的边界
     *
     * @Param zoomXSize 图像缩放的通量
     */
    private void zoomInBorderCritical(float zoomXSize){
        if(zoomXSize < 0){

            if(iconWidth <= (windowRect.right - windowRect.left)){

                imageRect.right = windowRect.right;
                imageRect.left = windowRect.left;
                iconWidth = windowRect.right - windowRect.left;
                iconHeight = iconWidth/iconWDH;
//                imageZoomState = DRAG;
                imageRect.bottom = imageRect.top + iconHeight;
            }
            if(iconHeight <= (windowRect.right - windowRect.left)){
                imageRect.bottom = windowRect.bottom;
                imageRect.top = windowRect.top;
                iconHeight = windowRect.right - windowRect.left;
                iconWidth = iconHeight*iconWDH;
//                imageZoomState = DRAG;
                imageRect.right = imageRect.left + iconWidth;
            }
        }
    }

    /**
     * 实时计算出缩放的向量
     *
     * @Params x y 图像位移的基准坐标点
     */
    private Vector getZoomVector(int x, int y){
        List<Vector> vectors = new ArrayList<>();
        for(int id : pointPool.keySet()){
            TouchPoint point = pointPool.get(id);
            Vector vector = new Vector(point.x - x, point.y - y);
            vectors.add(vector);
        }
        Vector myVector = new Vector(0,0);
        for(Vector vector: vectors){
            myVector = vector.substract(myVector);
        }

        return myVector;
    }

    /**
     * 重置缩放过程中的基准参数
     *
     * @Params x y 图像位移的基准坐标点
     */
    private void initZoomBasePoints(int x, int y){
        zoomInitialPoints.clear();
        for(int pointId: pointPool.keySet()){
            TouchPoint point = pointPool.get(pointId);
            Point startPoint = new Point();
            startPoint.x = point.x;
            startPoint.y = point.y;
            zoomInitialPoints.put(pointId, startPoint);
        }

        List<Vector> vectors = new ArrayList<>();
        for(int id : zoomInitialPoints.keySet()){
            Point point = zoomInitialPoints.get(id);
            Vector vector = new Vector(point.x - x, point.y - y);
            vectors.add(vector);
        }
        Vector myVector = new Vector(0,0);
        for(Vector vector: vectors){
            myVector = vector.substract(myVector);
        }
        zoomReferenceOffsetX = offsetX;
        zoomReferenceOffsetY = offsetY;
        zoomReferenceVector = myVector;
        zoomReferenceIconHeight = iconHeight;
        zoomReferenceIconWidth = iconWidth;

    }

    /**
     * 获得当前所有触摸点所围成图形的质心
     *
     * @return the centroid
     */
    private float[] getCentroid(){
        float[] points = new float[pointPool.size()*2];
        int i = 0;
        for(int id: pointPool.keySet()){
            points[i] = pointPool.get(id).x;
            i++;
            points[i] = pointPool.get(id).y;
            i++;
        }
        return computeCentroid(points);
    }


    /**
     * 移动图形的方法。
     *
     * @Params x y 图像位移的基准坐标点
     */
    private void drag(int x, int y){
        imageRect.left=x-offsetX;
        imageRect.top=y-offsetY;
        imageRect.right=imageRect.left+iconWidth;
        imageRect.bottom=imageRect.top+iconHeight;


        if(imageRect.left > windowRect.left){
            imageRect.left = windowRect.left;
            imageRect.right =  imageRect.left+iconWidth;
            offsetX=x-imageRect.left;
            offsetY=y-imageRect.top;
        }
        if (imageRect.right <  windowRect.right) {
            imageRect.right =  windowRect.right;
            imageRect.left = imageRect.right-iconWidth;
            offsetX=x-imageRect.left;
            offsetY=y-imageRect.top;
        }
        if (imageRect.top > windowRect.top) {
            imageRect.top = windowRect.top;
            imageRect.bottom = imageRect.top+iconHeight;
            offsetX=x-imageRect.left;
            offsetY=y-imageRect.top;
        }
        if (imageRect.bottom < windowRect.bottom) {
            imageRect.bottom = windowRect.bottom;
            imageRect.top = imageRect.bottom-iconHeight;
            offsetX=x-imageRect.left;
            offsetY=y-imageRect.top;
        }
    }

    /**
     * 在移动的基准坐标发生变化时，重置图形与坐标点的相对位置，以保持图片位置
     *
     * @Params x y 图像位移的基准坐标点
     */
    private void initPoint(int x, int y){
        offsetX=x-imageRect.left;
        offsetY=y-imageRect.top;
        iconHeight = imageRect.bottom - imageRect.top;
        iconWidth = imageRect.right - imageRect.left;
    }



    public Bitmap outPut(){
        float outPutImageWidth;
        if (iconWDH > 0) {
            outPutImageWidth =  (float)getMeasuredWidth()/iconHeight * icon.getHeight();
        }else {
            outPutImageWidth =  (float)getMeasuredWidth()/ iconWidth  * icon.getWidth();
        }

        if(outPutImageWidth > 1080){
            outPutImageWidth = 1080;
        }
        Bitmap bitmap = Bitmap.createBitmap((int)outPutImageWidth,(int)outPutImageWidth, Bitmap.Config.ARGB_8888);
        Canvas outPutCanvas = new Canvas(bitmap);
        Paint outPutPaint = new Paint();
        outPutPaint.setAntiAlias(true);
        outPutPaint.setFilterBitmap(true);
        outPutCanvas.drawColor(imageBackgroundPaint.getColor());
        RectF originRect ;
        if(cropMode == fullImage){
            originRect = fullImageModeRect;
        }else {

            originRect = imageRect;
        }
        float ratio = ((float)ScreenUtils.getScreenWidth(context))/outPutImageWidth;
        float leftPosition = originRect.left/ratio;
        float topPosition = originRect.top/ratio;
        float rightPosition = ((originRect.right - (float)ScreenUtils.getScreenWidth(context))/ratio) + outPutImageWidth;
        float bottomPosition = ((originRect.bottom - (float)ScreenUtils.getScreenWidth(context))/ratio) + outPutImageWidth;
        RectF newRect = new RectF(leftPosition, topPosition, rightPosition, bottomPosition);
        outPutCanvas.drawBitmap(icon, null, newRect, outPutPaint);
        return bitmap;
    }

    public void switchToFullMode(){
        drawing = true;
        cropMode = fullImage;
        try {
            Thread.sleep(50);
            drawing = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void switchToCropMode(){
        drawing = true;
        cropMode = cropImage;
        try {
            Thread.sleep(50);
            drawing = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startDraw(){
        drawing = true;


    }

    public void pauseDraw(){
        drawing = false;

    }

    class TouchPoint{
        int radius = 100;
        int x;
        int y;
    }

    /**
     * Calculates the centroid of a set of points.
     *
     * @param points the points in the form of [x1, y1, x2, y2, ..., xn, yn]
     * @return the centroid
     */
    private float[] computeCentroid(float[] points) {
        float centerX = 0;
        float centerY = 0;
        int count = points.length;
        for (int i = 0; i < count; i++) {
            centerX += points[i];
            i++;
            centerY += points[i];
        }
        float[] center = new float[2];
        center[0] = 2 * centerX / count;
        center[1] = 2 * centerY / count;

        return center;
    }

    public void recycleSorce(){
        if(icon != null){
            icon.recycle();
//            if(icon.isRecycled()){
//                System.gc();
//            }
        }
    }

    public void release() {
        getHolder().getSurface().release();
    }
}
