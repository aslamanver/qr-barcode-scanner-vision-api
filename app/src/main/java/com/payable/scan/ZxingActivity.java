package com.payable.scan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.payable.scan.databinding.ActivityZxingBinding;

import java.util.Random;

public class ZxingActivity extends AppCompatActivity {

    ActivityZxingBinding binding;
    IntentIntegrator integrator;
    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_zxing);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getBoolean("startup_scanner", false)) openScanner();
        binding.btnScan.setOnClickListener(v -> openScanner());
        binding.checkboxStartup.setChecked(sharedPrefs.getBoolean("startup_scanner", false));
        binding.checkboxStartup.setOnCheckedChangeListener((view, isChecked) -> sharedPrefs.edit().putBoolean("startup_scanner", isChecked).commit());
    }

    private void openScanner() {
        integrator = new IntentIntegrator(ZxingActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan QR Code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                Intent intent = new Intent(this, SalesPadActivity.class);
                intent.putExtra("scan_code", result.getContents());
                double amount;
                try {
                    String values[] = result.getContents().split(",");
                    String value = values[values.length - 1];
                    amount = Double.parseDouble(value);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                    amount = 0;
                }
                intent.putExtra("scan_amount", amount);
                startActivity(intent);
                Log.e("DarazScan", result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
