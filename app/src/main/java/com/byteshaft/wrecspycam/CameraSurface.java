/*
 *
 *  *
 *  *  * (C) Copyright 2015 byteShaft Inc.
 *  *  *
 *  *  * All rights reserved. This program and the accompanying materials
 *  *  * are made available under the terms of the GNU Lesser General Public License
 *  *  * (LGPL) version 2.1 which accompanies this distribution, and is available at
 *  *  * http://www.gnu.org/licenses/lgpl-2.1.html
 *  *  *
 *  *  * This library is distributed in the hope that it will be useful,
 *  *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  *  * Lesser General Public License for more details.
 *  *  
 *
 */

package com.byteshaft.wrecspycam;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraSurface extends ContextWrapper implements SurfaceHolder.Callback,
        Camera.PictureCallback, Camera.ShutterCallback {

    private WindowManager mWindowManager = null;
    private SurfaceView mDummyPreview = null;
    private Camera mCamera = null;
    private Camera.Parameters mParams = null;

    public CameraSurface(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mParams = mCamera.getParameters();
    }

    void create() {
        mDummyPreview = getDummySurface();
        SurfaceHolder holder = getHolderForPreview(mDummyPreview);
        setColorForPreview(mDummyPreview);
        setupCompatibilityForPreHoneycombDevices(holder);
        addCallbackForHolder(holder);
        createSystemOverlayForPreview(mDummyPreview);
    }

    void destroy() {
        if (mWindowManager != null && mDummyPreview != null) {
            mWindowManager.removeView(mDummyPreview);
            mDummyPreview = null;
        }
    }

    private SurfaceView getDummySurface() {
        return new SurfaceView(this);
    }

    private void setColorForPreview(SurfaceView preview) {
        preview.setBackgroundColor(Color.BLACK);
    }

    private SurfaceHolder getHolderForPreview(SurfaceView preview) {
        return preview.getHolder();
    }

    @SuppressWarnings("deprecation")
    private void setupCompatibilityForPreHoneycombDevices(SurfaceHolder holder) {
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void addCallbackForHolder(SurfaceHolder holder) {
        holder.addCallback(this);
    }

    private void createSystemOverlayForPreview(SurfaceView previewForCamera) {
        mWindowManager = getWindowManager();
        WindowManager.LayoutParams params = getCustomWindowManagerParameters();
        mWindowManager.addView(previewForCamera, params);
    }

    private WindowManager getWindowManager() {
        return (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    private WindowManager.LayoutParams getCustomWindowManagerParameters() {
        final int ONE_PIXEL = 1;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = ONE_PIXEL;
        params.width = ONE_PIXEL;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.BOTTOM | Gravity.END;
        return params;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        List<Camera.Size> sizes = mParams.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        mParams.setPreviewSize(selected.width, selected.height);
        mCamera.setParameters(mParams);
        mCamera.startPreview();
        mCamera.takePicture(this, null, null, this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                releaseCamera();
                destroy();
            }
        }, 1000);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        String location = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/pic.jpg";
        try {
            FileOutputStream out = new FileOutputStream (new File(location));
            out.write(data);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void onShutter() {

    }
}
