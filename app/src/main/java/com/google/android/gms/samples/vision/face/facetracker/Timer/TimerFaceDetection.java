package com.google.android.gms.samples.vision.face.facetracker.Timer;

import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by amritkaur on 24-04-2017.
 */

public class TimerFaceDetection {

  /*  public void displayStartCounter(int timerCapturedDelay) {
        timerAnimation(false);
        if (timerCapturedDelay == 0) {
            addTimerMessage(TIMER_STOP_COUNTER, 0);
            return;
        }
        updateTimerView(timerCapturedDelay);
    }

    private void updateTimerView(int timerCount) {
        if (mCounterView == null) {
            return;
        }
        int resId = 0;
        RotateImageView iv_timer_num;
        mCurrentTimerCount = timerCount;
        if (timerCount > 0) {
            resId = timerDrawable[timerCount - 1];
            iv_timer_num = (RotateImageView)mCounterView.findViewById(R.id.timer_count);

            if (iv_timer_num != null) {
                setTimerLayoutParam(iv_timer_num);

                iv_timer_num.setImageResource(resId);
                timerAnimation(true);
            }
            if (FunctionProperties.isSupportedRemoteShutter()
                    && mShutterType == CAMERA_TIMER) {
                mGet.sendCountDownForRemoteShutter(mCurrentTimerCount);
            }
        }
    }

    private void setTimerLayoutParam(View timerView) {
        int top = ModelProperties.isLongLCDModel()
                ? RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, TIMER_MARGIN_TOP_LONG_LCD)
                : RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, TIMER_MARGIN_TOP);

        RelativeLayout.LayoutParams lp = (LayoutParams)timerView.getLayoutParams();
        if (lp == null) {
            return;
        }

        // [Tile]
        if (ON.equals(mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
            top += RatioCalcUtil.getSizeCalculatedByPercentage(mGet.getAppContext(), true, THUMBNAIL_LIST_SIZE);
        }

        lp.setMarginsRelative(0, top, 0, 0);
        timerView.setLayoutParams(lp);
    }

    public void startTimerShot(final int time, final int shutterType) {
        mCounterView = mGet.inflateView(R.layout.timer_view);
        ViewGroup vg = (ViewGroup)mGet.findViewById(R.id.camera_controls);
        if (vg != null && mCounterView != null) {
            vg.addView(mCounterView,
                    new RelativeLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        AudioUtil.setAudioFocus(mGet.getAppContext(), true);

        if (time == TIMER_SETTING_VALUE) {
            mTimerCaptureMode = Integer.parseInt(mGet.getSettingValue(Setting.KEY_TIMER));
        } else {
            mTimerCaptureMode = time;
        }
        if (mTimerCaptureMode > 10) {
            mTimerCaptureMode = 10;
        }
        mTimerCount = mTimerCaptureMode;

        mShutterType = shutterType;
        CamLog.d(TAG, "startTimerShot - type : " + mShutterType);

        mIsGestureShotProgress = (shutterType == GESTURE_TIMER) ? true : false;

        // init counting value, because if user cancel counting around 8 sec,
        // mTimerCaptureDelay is started by 9 even if set 10.
        // Guess the cause is taskCountDown minus one after stopTimerShot().
        mGet.playSound(SOUND_TIMER, false, mTimerCount);
        mInTimerShotCountdown = true;
        mTimerCountDown = new Timer("timer_countdown");
        TimerTask taskCountDown = new TimerTask() {
            @Override
            public void run() {
                CamLog.d(TAG, "timer task (count down) " + mTimerCount);
                addTimerMessage(TIMER_START_COUNTER, mTimerCount);

                if (mTimerCount > 0) {
                    mGet.playSound(SOUND_TIMER, false, mTimerCount);
                    mTimerCount--;
                } else {
                    if (mTimerCountDown != null) {
                        mTimerCountDown.purge();
                        mTimerCountDown.cancel();
                        mTimerCountDown = null;
                    }
                    mTimerCount = mTimerCaptureMode;
                }
            }
        };
        addTimerMessage(TIMER_INIT_COUNTER, mTimerCount);
        mTimerCount--;
        mTimerCountDown.scheduleAtFixedRate(taskCountDown, 1000, 1000);
    }

    public boolean stopTimerShot() {
        boolean result = false;
        timerAnimation(false);
        if (mInTimerShotCountdown) {
            if (!ON.equals(mGet.getSettingValue(Setting.KEY_VOICESHUTTER))) {
                AudioUtil.setAudioFocus(getAppContext(), false);
            }

            if (mTimerCountDown != null) {
                mTimerCountDown.purge();
                mTimerCountDown.cancel();
                mTimerCountDown = null;
            }
            mTimerCount = mTimerCaptureMode;

            mHandler.removeMessages(TIMER_START_COUNTER);
            mHandler.removeMessages(TIMER_STOP_COUNTER);
            addTimerMessage(TIMER_CANCEL_COUNTER, 0);
            result = true;
        }
        return result;
    }

    public void displayInitCounter(int startTime) {
        if (mCounterView == null) {
            return;
        }
        int resId = 0;
        if (startTime > timerDrawable.length) {
            startTime = timerDrawable.length;
        }
        resId = timerDrawable[startTime-1];
        mCurrentTimerCount = startTime;
        RotateImageView iv_timer_num = (RotateImageView)mCounterView.findViewById(R.id.timer_count);
        if (resId != 0 && iv_timer_num != null) {
            setTimerLayoutParam(iv_timer_num);
            iv_timer_num.setImageResource(resId);
        }
        setRotateDegree(getOrientationDegree(), false);
        timerAnimation(true);
        if (FunctionProperties.isSupportedRemoteShutter()
                && mShutterType == CAMERA_TIMER) {
            mGet.sendCountDownForRemoteShutter(mCurrentTimerCount);
        }
    }

    public void displayStartCounter(int timerCapturedDelay) {
        timerAnimation(false);
        if (timerCapturedDelay == 0) {
            addTimerMessage(TIMER_STOP_COUNTER, 0);
            return;
        }
        updateTimerView(timerCapturedDelay);
    }

    private void cancelCounter() {
        releaseCounterView();
        mIsGestureShotProgress = false;
        mInTimerShotCountdown = false;
    }

    private void setTimerLayoutParam(View timerView) {
        int top = ModelProperties.isLongLCDModel()
                ? RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, TIMER_MARGIN_TOP_LONG_LCD)
                : RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, TIMER_MARGIN_TOP);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) timerView.getLayoutParams();
        if (lp == null) {
            return;
        }

        // [Tile]
        if (ON.equals(mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
            top += RatioCalcUtil.getSizeCalculatedByPercentage(mGet.getAppContext(), true, THUMBNAIL_LIST_SIZE);
        }

        lp.setMarginsRelative(0, top, 0, 0);
        timerView.setLayoutParams(lp);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_INIT_COUNTER: {
                    CamLog.d(TAG, "timer INIT msg.arg1 = " + msg.arg1);
                    displayInitCounter(msg.arg1);
                    break;
                }
                case TIMER_START_COUNTER: {
                    CamLog.d(TAG, "timer START msg.arg1 = " + msg.arg1);
                    displayStartCounter(msg.arg1);
                    break;
                }
                case TIMER_STOP_COUNTER: {
                    CamLog.d(TAG, "timer STOP msg.arg1 = " + msg.arg1);
                    displayStopCounter();
                    break;
                }
                case TIMER_CANCEL_COUNTER: {
                    CamLog.d(TAG, "timer CANCEL msg.arg1 = " + msg.arg1);
                    cancelCounter();
                    break;
                }
                default :
                    break;
            }
        }
    };

    public void displayInitCounter(int startTime) {
        if (mCounterView == null) {
            return;
        }
        int resId = 0;
        if (startTime > timerDrawable.length) {
            startTime = timerDrawable.length;
        }
        resId = timerDrawable[startTime-1];
        mCurrentTimerCount = startTime;
        RotateImageView iv_timer_num = (RotateImageView)mCounterView.findViewById(R.id.timer_count);
        if (resId != 0 && iv_timer_num != null) {
            setTimerLayoutParam(iv_timer_num);
            iv_timer_num.setImageResource(resId);
        }
        setRotateDegree(getOrientationDegree(), false);
        timerAnimation(true);
        if (FunctionProperties.isSupportedRemoteShutter()
                && mShutterType == CAMERA_TIMER) {
            mGet.sendCountDownForRemoteShutter(mCurrentTimerCount);
        }
    }

    public void displayStartCounter(int timerCapturedDelay) {
        timerAnimation(false);
        if (timerCapturedDelay == 0) {
            addTimerMessage(TIMER_STOP_COUNTER, 0);
            return;
        }
        updateTimerView(timerCapturedDelay);
    }*/

}
