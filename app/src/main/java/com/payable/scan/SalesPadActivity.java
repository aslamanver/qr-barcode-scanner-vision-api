package com.payable.scan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.payable.scan.databinding.ActivitySalesPadBinding;
import com.payable.sdk.Payable;
import com.payable.sdk.PayableListener;
import com.payable.sdk.PayableSale;

import java.text.DecimalFormat;

// import com.paxemvcore.Device;


public class SalesPadActivity extends Activity implements PayableListener {

    ActivitySalesPadBinding binding;

    String tempAmount = "";
    double saleAmount = 0;

    private DecimalFormat decimalFormatAP = new DecimalFormat("###,###,##0.00");

    Payable payableClient;
    private String scanCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scanCode = getIntent().getStringExtra("scan_code");

        payableClient = Payable.createPayableClient(this, "1201", "DARAZ_SCAN", "082a4663b70ef49accecee2a3101619f7006133d468e388978251f1817e5caca28c4407f7d33237b29db204bedba47954f1d115f59e4bbc2a0576425b8076f6c");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sales_pad);
        binding.txtCurrency.setText("LKR");

        binding.rlCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSale();
            }
        });

        binding.rlKeyBackSpace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                tempAmount = "0";
                saleAmount = 0;
                binding.txtAmount.setText(decimalFormatAP.format(saleAmount));
                return false;
            }
        });
    }

    private void startSale() {

        if (saleAmount < 1) {
            Toast.makeText(this, "Please enter minimum 1.00 LKR", Toast.LENGTH_LONG).show();
            return;
        }

        payableClient.startPayment(saleAmount, Payable.METHOD_CARD, "{ \"ORDER_TRACKING\" : \"" + scanCode + "\" }", this);
    }

    public void onKeyPressed(View view) {

        String key = (((TextView) ((RelativeLayout) view).getChildAt(0)).getText().toString());

        // Max amount checking unit
        double _saleAmount = (Long.valueOf(tempAmount + key) / 100.0);
        if (_saleAmount > 999999.99) {
            // errorDialog(0, "Error", getResources().getString(R.string.max_amount_reached) + " " + decimalFormatAP.format(getAlipayClient().getMaxAmount()) + " " + getAlipayClient().getCurrencyName());
            return;
        }

        tempAmount += key;
        saleAmount = _saleAmount;
        binding.txtAmount.setText(decimalFormatAP.format(saleAmount));
    }

    public void onBackSpacePressed(View view) {

        if (tempAmount.length() > 0) {

            if (tempAmount.length() == 1) {

                tempAmount = "";
                saleAmount = 0;
                binding.txtAmount.setText(decimalFormatAP.format(saleAmount));

            } else {

                tempAmount = tempAmount.substring(0, tempAmount.length() - 1);
                saleAmount = Integer.valueOf(tempAmount) / 100.0;
                binding.txtAmount.setText(decimalFormatAP.format(saleAmount));
            }
        }
    }

    @Override
    public boolean onPaymentStart(PayableSale payableSale) {
        return true;
    }

    @Override
    public void onPaymentSuccess(PayableSale payableSale) {
        Toast.makeText(getApplicationContext(), payableSale.getMessage(), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onPaymentFailure(PayableSale payableSale) {

        String message = payableSale.getMessage();

        if (payableSale.getStatusCode() == Payable.APP_NOT_INSTALLED) {
            message = "PAYable app is not installed";
        }

        if (payableSale.getStatusCode() == Payable.INVALID_AMOUNT) {
            message = "Amount is invalid";
        }

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        payableClient.handleResponse(requestCode, data);
    }

}
