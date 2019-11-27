package com.payable.scan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.FlashMode;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.payable.scan.databinding.ActivityMainBinding;

import java.io.File;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;

    ImageCapture imageCapture;
    Preview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        textureView = binding.texture;
        binding.lnFlash.setVisibility(View.GONE);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {

        CameraX.unbindAll();

        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen

        preview = new Preview(new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screen)
                .build());

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {

            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) textureView.getParent();
                parent.removeView(textureView);
                parent.addView(textureView, 0);
                textureView.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
                slideToDown();
            }
        });

        imageCapture = new ImageCapture(new ImageCaptureConfig.Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setFlashMode(FlashMode.AUTO)
                .setTargetResolution(new Size(500, 768))
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build());

        binding.btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String path = Environment.getExternalStorageDirectory() + "/PAYableSCAN/";
                File dir = new File(path);
                File file = new File(path + "code.jpg");
                if (!dir.exists()) dir.mkdirs();

                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {

                    @Override
                    public void onImageSaved(@NonNull File file) {
                        // Toast.makeText(getBaseContext(), "Pic captured at " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        Toast.makeText(getBaseContext(), "Pic capture failed : " + message, Toast.LENGTH_LONG).show();
                        if (cause != null) {
                            cause.printStackTrace();
                        }
                    }

                });
            }
        });

        binding.btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageCapture.getFlashMode() == FlashMode.AUTO) {
                    imageCapture.setFlashMode(FlashMode.ON);
                    binding.imgFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on_black_24dp));
                    preview.enableTorch(true);
                } else if (imageCapture.getFlashMode() == FlashMode.ON) {
                    imageCapture.setFlashMode(FlashMode.OFF);
                    binding.imgFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off_black_24dp));
                    preview.enableTorch(false);
                } else if (imageCapture.getFlashMode() == FlashMode.OFF) {
                    imageCapture.setFlashMode(FlashMode.AUTO);
                    binding.imgFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_auto_black_24dp));
                    preview.enableTorch(false);
                }
            }
        });

        CameraX.bindToLifecycle(this, preview, imageCapture);
    }

    public void slideToAbove() {

        Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -110);
        slide.setDuration(2000);

        binding.midLine.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.midLine.clearAnimation();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        binding.midLine.getWidth(), binding.midLine.getHeight());
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                binding.midLine.setLayoutParams(lp);

                slideToDown();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public void slideToDown() {

        Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 110);
        slide.setDuration(2000);

        binding.midLine.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.midLine.clearAnimation();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        binding.midLine.getWidth(), binding.midLine.getHeight());
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                binding.midLine.setLayoutParams(lp);

                slideToAbove();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void updateTransform() {

        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}

