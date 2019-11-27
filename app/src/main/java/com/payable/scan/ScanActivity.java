package com.payable.scan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.SourceData;
import com.payable.scan.databinding.ActivityScanBinding;
import com.payable.scan.databinding.CodeListBinding;
import com.payable.scan.utils.ExifUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    byte[] data;
    Bitmap imgBitmap;
    ActivityScanBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan);

        File file = new File(Environment.getExternalStorageDirectory() + "/PAYableSCAN/code.jpg");

        if (file.exists()) {

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            Bitmap orientedBitmap = ExifUtil.rotateBitmap(file.getAbsolutePath(), bitmap);
            binding.imgView.setImageBitmap(orientedBitmap);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    tg.startTone(ToneGenerator.TONE_DTMF_S, 150);

                    BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                            .setBarcodeFormats(Barcode.ALL_FORMATS)
                            .build();

                    Frame mFrame = new Frame.Builder().setBitmap(orientedBitmap).build();

                    SparseArray<Barcode> barcodes = barcodeDetector.detect(mFrame);

                    List<String> codeList = new ArrayList<>();

                    for (int x = 0; x < barcodes.size(); x++) {

                        Barcode barcode = barcodes.valueAt(x);

                        Log.e("AB_READ", barcode.displayValue);

                        Canvas canvas = new Canvas(orientedBitmap);

                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setAntiAlias(true);
                        paint.setTextSize(40);

                        RectF rect = new RectF(barcode.getBoundingBox());
                        rect.bottom = rect.top + 5;

                        canvas.drawRect(rect, paint);
                        canvas.drawText((x + 1) + "", rect.left, rect.top - 5, paint);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.imgView.setImageBitmap(orientedBitmap);
                            }
                        });

                        codeList.add((x + 1) + ". " + barcode.displayValue);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CodeAdapter codeListAdapter = new CodeAdapter(getApplicationContext(), codeList);
                            binding.listView.setAdapter(codeListAdapter);
                        }
                    });
                }
            }).start();
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, int scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(a.getX() / scaleFactor,
                    a.getY() / scaleFactor,
                    b.getX() / scaleFactor,
                    b.getY() / scaleFactor,
                    paint);
        }
    }

    class CodeAdapter extends BaseAdapter {

        List<String> list;
        Context context;
        CodeListBinding codeListBinding;
        LayoutInflater layoutInflater;

        CodeAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {
                codeListBinding = CodeListBinding.inflate(layoutInflater, parent, false);
                convertView = codeListBinding.getRoot();
                viewHolder = new ViewHolder(codeListBinding);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.holderBinding.txtName.setText(list.get(position));

            return convertView;
        }

        private class ViewHolder {

            CodeListBinding holderBinding;

            public ViewHolder(CodeListBinding binding) {
                holderBinding = binding;
            }
        }
    }
}
