package com.payable.scan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.payable.scan.databinding.ActivityDetectBinding;

import java.io.IOException;
import java.util.Random;

public class DetectActivity extends AppCompatActivity {

    ActivityDetectBinding binding;
    boolean isDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detect);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            binding.mainLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 900)
                .setAutoFocusEnabled(true)
                .build();

        binding.sView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                isDone = false;

                try {

                    cameraSource.start(binding.sView.getHolder());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                if (isDone) return;

                SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() > 0) {

                    // isDone = true;
                    //
                    // Intent intent = new Intent(DetectActivity.this, SalesPadActivity.class);
                    // intent.putExtra("scan_code", barcodes.valueAt(0).displayValue);
                    // intent.putExtra("scan_amount", 500.00);
                    // startActivity(intent);

                    Log.e("ASLAM", barcodes.valueAt(0).displayValue);

                    int x = 0;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.dLine.getLayoutParams();
                            params.leftMargin = barcodes.valueAt(x).getBoundingBox().left;
                            params.topMargin = barcodes.valueAt(x).getBoundingBox().top;
                            params.width = barcodes.valueAt(x).getBoundingBox().width();
                            binding.dLine.setLayoutParams(params);
                            binding.dText.setText(barcodes.valueAt(x).displayValue);
                        }
                    });

                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.dLine.getLayoutParams();
                            params.leftMargin = 0;
                            params.topMargin = -35;
                            params.width = binding.mainLayout.getWidth();
                            binding.dLine.setLayoutParams(params);
                            binding.dText.setText("");
                        }
                    });
                }
            }
        });

        ScanAnimLayout.slideToDown(binding.trBox.midLine);
    }

}
