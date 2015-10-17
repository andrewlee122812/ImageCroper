package com.ilikelabs.commonUtils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by taXer on 15/8/4.
 */
public class ExtendBundle implements Parcelable{
   private String transitionFrom;
    private Bundle extras;
    private Context context;

    public ExtendBundle(Context context) {
        this.context = context;
    }

    public ExtendBundle(Parcel source) {
        //先读取mId，再读取mDate
        transitionFrom = source.readString();
        extras = source.readBundle();
    }

    public String from(){
        return context.getApplicationInfo().className;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //实现Parcelable的方法writeToParcel，将ParcelableDate序列化为一个Parcel对象
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //先写入mId，再写入mDate
        dest.writeString(transitionFrom);
        dest.writeBundle(extras);
    }

    //实例化静态内部对象CREATOR实现接口Parcelable.Creator
    public static final Parcelable.Creator<ExtendBundle> CREATOR = new Creator<ExtendBundle>() {

        @Override
        public ExtendBundle[] newArray(int size) {
            return new ExtendBundle[size];
        }

        //将Parcel对象反序列化为ParcelableDate
        @Override
        public ExtendBundle createFromParcel(Parcel source) {
            return new ExtendBundle(source);
        }
    };

    public Bundle getExtras() {
        return extras;
    }

    public void setExtras(Bundle extras) {
        this.extras = extras;
    }
}
