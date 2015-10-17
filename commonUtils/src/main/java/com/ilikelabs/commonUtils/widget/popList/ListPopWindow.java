package com.ilikelabs.commonUtils.widget.popList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;


import com.ilikelabs.commonUtils.R;
import com.ilikelabs.commonUtils.utils.ListHeightUtil;
import com.ilikelabs.commonUtils.utils.joanzapata.BaseAdapterHelper;
import com.ilikelabs.commonUtils.utils.joanzapata.QuickAdapter;

import java.util.List;

/**
 * Created by Yulu on 2015/6/15.
 */
public class ListPopWindow extends PopupWindow {

    private Activity mContext;
//    private FrameLayout rootView;
    private ListView listView;


    private ListPopWindow(Activity context) {
        // 设置布局的参数
        mContext = context;
        View contentView = LayoutInflater.from(context).inflate(R.layout.list_pop_window, null);
        setContentView(contentView);

//        rootView  = (FrameLayout) contentView.findViewById(R.id.root_view);
        listView = (ListView) contentView.findViewById(R.id.listView);

        setBackgroundDrawable(new BitmapDrawable());
        setTouchable(true);
        setOutsideTouchable(true);
//        setFocusable(false);



//        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
//        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

    }




    public abstract static class Builder<T> extends QuickAdapter<T> {
        ListPopWindow listPopWindow;


        public Builder(Activity context, int resId, List<T> data){
            super(context, resId, data);
            listPopWindow = new ListPopWindow(context) ;
            listPopWindow.setAdapter(this);
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener){
            listPopWindow.setOnItemClickListener(onItemClickListener);

        }



        @Override
        protected abstract void convert(BaseAdapterHelper helper, T item);

        public void showAtLocation(View parent, int gravity, int x, int y){
            listPopWindow.showAtLocation(parent, gravity, x, y);
        }

        public void showAsDropDown(View anchor){
            listPopWindow.showAsDropDown(anchor);
        }

        public void showAsDropDown(View anchor, int xoff, int yoff){
            listPopWindow.showAsDropDown(anchor, xoff, yoff);
        }

        @TargetApi(19)
        public void showAsDropDown(View anchor, int xoff, int yoff, int gravity){
            listPopWindow.showAsDropDown(anchor, xoff, yoff, gravity);
        }

        public void setAnimationStyle(int style){
            listPopWindow.setAnimationStyle(style);
        }

        public void dismiss(){
//            listPopWindow.setListOutAnim(gravity, listPopWindow.listView);
//            listPopWindow.setBackgroundOutAnim(listPopWindow.rootView);
            listPopWindow.dismiss();
        }

    }




    private void setAdapter(QuickAdapter quickAdapter){
        listView.setAdapter(quickAdapter);
        setHeight(ListHeightUtil.getHeightOfListView(listView));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener){
        listView.setOnItemClickListener(onItemClickListener);
    }

//    protected void setListInAnim(int gravity, View View){
//        if(gravity == Gravity.BOTTOM){
//            View.animate().setDuration(animDuration).translationY(-getHeightOfListView());
//        }else {
//            View.animate().setDuration(animDuration).translationY(getHeightOfListView());
//        }
//
//    }
//
//    protected void setListOutAnim(int gravity,View view){
//        if(gravity == Gravity.BOTTOM){
//            view.animate().setDuration(animDuration).translationY(getHeightOfListView());
//        }else {
//            view.animate().setDuration(animDuration).translationY(-getHeightOfListView());
//        }
//
//    }
//
//    protected void setBackgroundInAnim(View background){
//        rootView.setAlpha(0f);
//        background.animate().alpha(1f).setDuration(animDuration);
//    }
//
//    protected void setBackgroundOutAnim(View background){
//        background.animate().alpha(0f).setDuration(animDuration);
//    }
//    public void showFullScreen(){
//
//        initListPosition();
//
//        super.showAtLocation(mContext.getWindow().getDecorView(), Gravity.TOP, 0, 0);
////        rootView.animate().start();
//        listView.animate().start();
//        listView.animate().start();
//
//    }
//    public void dismiss(){
////        rootView.animate().start();
//        listView.animate().setListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                ListPopWindow.super.dismiss();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        }).start();
//
//    }


}
