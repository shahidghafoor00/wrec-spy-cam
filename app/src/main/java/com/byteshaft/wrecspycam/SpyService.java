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
        take();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void take() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mCamera = Camera.open();
                    mCameraSurface = new CameraSurface(SpyService.this, mCamera);
                    mCameraSurface.create();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }, 10000);
    }
}
