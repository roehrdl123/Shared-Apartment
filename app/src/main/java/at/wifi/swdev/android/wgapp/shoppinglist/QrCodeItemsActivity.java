package at.wifi.swdev.android.wgapp.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeItemsBinding;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class QrCodeItemsActivity extends AppCompatActivity implements onListItemClickListener<Artikel>
{
    public static final String QR_ARTIKEL_ID = "Items";
    private QrCodeItemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActivityQrCodeItemsBinding binding = ActivityQrCodeItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle(R.string.qrscanner);

        Intent intent = getIntent();
        ArrayList artikel = intent.getParcelableArrayListExtra(QrCodeScannerActivity.ARTIKEL_EXTRA);

        RecyclerView recyclerView = binding.rvQrItems;
        //Adapter
        adapter = new QrCodeItemsAdapter(this);
        adapter.setList(artikel);
        adapter.setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public void onFinished(View view)
    {
        ArrayList<Artikel> artikels = new ArrayList<>(adapter.getArtikel());
        setResult(RESULT_OK, new Intent().putExtra(QR_ARTIKEL_ID, artikels));

        finish();
    }

    @Override
    public void onListItemClick(Artikel model, int requestCode)
    {
        Toast.makeText(this, "Alle Artikel wurden gelöscht!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
