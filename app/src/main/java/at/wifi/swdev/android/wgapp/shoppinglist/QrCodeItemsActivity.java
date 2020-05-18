package at.wifi.swdev.android.wgapp.shoppinglist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeItemsBinding;

public class QrCodeItemsActivity extends AppCompatActivity
{
    public static final String QR_ARTIKEL_ID = "Items";
    private ActivityQrCodeItemsBinding binding;
    private String itemsId;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityQrCodeItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        ArrayList artikel = intent.getParcelableArrayListExtra(QrCodeScannerActivity.ARTIKEL_EXTRA);

        //TODO: anzeigen der Elemente und änderbar machen

        setResult(RESULT_OK, new Intent().putExtra(QR_ARTIKEL_ID, intent.getParcelableArrayListExtra(QrCodeScannerActivity.ARTIKEL_EXTRA)));

        finish();
    }
}
