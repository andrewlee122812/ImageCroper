package com.ilikelabs.commonUtils.utils.clickTimer;

import android.os.CountDownTimer;



/**
 * Created by taXer on 15/8/6.
 */
public class ClickTimer {
    private CountDownTimer timer;
    private CountDownFinishListener countDownFinishListener;
    public ClickTimer(int totalTime, int separateTime, final CountDownFinishListener countDownFinishListener){
        this.countDownFinishListener = countDownFinishListener;
        timer = new CountDownTimer(totalTime, separateTime) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                countDownFinishListener.countDownFinish();
            }
        };
    }

    public void start(){
        timer.start();
    }

    public void reset(){
        timer.cancel();
        timer.start();
    }

    public void stop(){
        timer.cancel();
    }

    public void forceFinish(){
        timer.cancel();
        countDownFinishListener.countDownFinish();
    }
}
