package at.wifi.swdev.android.wgapp.qr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.data.QRItems;
import at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeListEditBinding;
import at.wifi.swdev.android.wgapp.onListItemClickListener;
import at.wifi.swdev.android.wgapp.shoppinglist.CustomShoppingListActivity;

public class QrCodeListEditActivity extends AppCompatActivity implements onListItemClickListener<Artikel>
{
    public static final int RQ_CODE_EDIT = 1;
    public static final int REQUEST_CODE_NEW_QR_ITEM = 1234;
    public static final String EXTRA_INC = "incRQ";
    public static final String EXTRA_INC_MODEL = "incRQModel";
    private QRItems qrItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeListEditBinding binding = ActivityQrCodeListEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        qrItem = (QRItems) getIntent().getSerializableExtra(EXTRA_INC_MODEL);

        String s = getResources().getString(R.string.id) + qrItem.getQrId();
        binding.tvQrEditId.setText(s);


        binding.tvEdit.setText(R.string.edit);
        getSupportActionBar().setTitle(R.string.editQr);

        Query artikelQuery = FirebaseDatabase.getInstance().getReference("qrcodes").child(qrItem.getKey()).child("items");

        FirebaseRecyclerOptions<Artikel> options = new FirebaseRecyclerOptions.Builder<Artikel>().setLifecycleOwner(this).setQuery(artikelQuery, Artikel.class).build();

        QrCodeListEditAdapter adapter = new QrCodeListEditAdapter(options);
        adapter.setOnListItemClickListener(this);

        binding.rvQrItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvQrItems.setAdapter(adapter);
    }

    @Override
    public void onListItemClick(Artikel model, int requestCode)
    {
        FirebaseDatabase.getInstance().getReference("qrcodes").child(qrItem.getKey()).child("items").child(model.getId()).removeValue();
    }

    public void addQrArtikel(View view)
    {
        Intent intent = new Intent(this, CustomShoppingListActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_QR_ITEM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == RESULT_OK && data != null)
        {
            if (requestCode == REQUEST_CODE_NEW_QR_ITEM)
            {
                addToDatabase((Artikel) data.getSerializableExtra(CustomShoppingListActivity.ITEM_EXTRA));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addToDatabase(Artikel artikel)
    {
        String key = FirebaseDatabase.getInstance().getReference("qrcodes").child(FirebaseAuth.getInstance().getUid()).child(qrItem.getKey()).child("items").push().getKey();
        artikel.setId(key);
        FirebaseDatabase.getInstance().getReference("qrcodes").child(FirebaseAuth.getInstance().getUid()).child(qrItem.getKey()).child("items").child(key).setValue(artikel);
    }

    @Override
    public void onBackPressed()
    {
        onFertig(null);
    }

    @Override
    protected void onDestroy()
    {
        onFertig(null);
        super.onDestroy();
    }

    public void onFertig(View view)
    {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("qrcodes").child(FirebaseAuth.getInstance().getUid()).child(qrItem.getKey());
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.hasChild("items"))
                {
                    ref.removeValue();
                    Toast.makeText(QrCodeListEditActivity.this, "Der Qr-Code wurde gelöscht, da keine Artikel hinzugefügt wurden", Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.print_qr, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.print)
        {
            printQr();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void printQr()
    {
        final PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference("qrs").child(qrItem.getQrId() + ".png").getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes ->
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            photoPrinter.printBitmap(qrItem.getQrId()+"-print", bitmap);
        }).addOnFailureListener(exception -> {});
    }
}
