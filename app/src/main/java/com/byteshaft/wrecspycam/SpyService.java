package com.byteshaft.wrecspycam;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;

@SuppressWarnings("deprecation")
public class SpyService extends Service {

    private Camera mCamera = null;
    private CameraSurface mCameraSurface = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        takeDelayedPhoto(4000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void takeDelayedPhoto(int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mCamera = Camera.open();
                    mCameraSurface = new CameraSurface(getApplicationContext(), mCamera);
                    mCameraSurface.create();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }, delay);
    }
}
