package at.wifi.swdev.android.wgapp.qr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.data.QRItems;
import at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeTableAddBinding;
import at.wifi.swdev.android.wgapp.onListItemClickListener;
import at.wifi.swdev.android.wgapp.shoppinglist.CustomShoppingListActivity;

public class QrCodeAddTableActivity extends AppCompatActivity implements onListItemClickListener<Artikel>
{
    public static final int REQUEST_CODE_NEW_QR_ITEM = 1234;
    private ActivityQrCodeTableAddBinding binding;
    private static final String TAG = QrCodeAddTableActivity.class.getSimpleName();
    private QRItems qr;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityQrCodeTableAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle(R.string.newQr);

        findQr();
    }

    private void findQr()
    {

        StorageReference listRef = FirebaseStorage.getInstance().getReference("qrs");

        listRef.listAll().addOnSuccessListener(listResult -> FirebaseDatabase.getInstance().getReference("qrcodes").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                HashSet<Integer> setData = new HashSet<>();
                HashSet<Integer> setPics = new HashSet<>();
                Map<Integer, StorageReference> map = new HashMap<>();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    QRItems qrItem = data.getValue(QRItems.class);
                    setData.add(qrItem.getQrId());
                }
                for (StorageReference item : listResult.getItems())
                {
                    int id = Integer.parseInt(item.getName().split("\\.")[0]);
                    setPics.add(id);
                    map.put(id, item);
                }

                setPics.removeAll(setData);

                if (setPics.size() != 0)
                {
                    int id = setPics.iterator().next();
                    newQrCode(map.get(id));
                } else
                {
                    Toast.makeText(QrCodeAddTableActivity.this, "Kein QR-Code verfügbar. Kontaktieren sie den Service :)", Toast.LENGTH_SHORT).show();
                    onFinish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        }));

    }

    private void onFinish()
    {
        binding.floatingActionButton3.setClickable(false);
        finish();
    }

    public void newQrCode(StorageReference item)
    {
        Log.d(TAG, "onSuccess: " + item.getName());
        qr = new QRItems();
        qr.setOccupied(true);
        String helpi = item.getName().split("\\.")[0];
        qr.setQrId(Integer.parseInt(helpi));
        item.getDownloadUrl().addOnSuccessListener(uri -> qr.setQrCodeURL(uri.getPath())).addOnFailureListener(e -> qr.setQrCodeURL("error"));

        if (qr != null)
        {
            String string = getString(R.string.id) + qr.getQrId();
            binding.tvIdQrList.setText(string);

            String key = FirebaseDatabase.getInstance().getReference("qrcodes").push().getKey();
            qr.setKey(key);
            FirebaseDatabase.getInstance().getReference("qrcodes").child(key).setValue(qr);
            Query qrcodeQuery = FirebaseDatabase.getInstance().getReference("qrcodes").child(key).child("items");

            FirebaseRecyclerOptions<Artikel> options = new FirebaseRecyclerOptions.Builder<Artikel>().setLifecycleOwner(QrCodeAddTableActivity.this).setQuery(qrcodeQuery, Artikel.class).build();

            QrCodeAddTableListAdapter adapter = new QrCodeAddTableListAdapter(options);
            adapter.setListener(this);
            binding.rvTable.setLayoutManager(new LinearLayoutManager(QrCodeAddTableActivity.this));
            binding.rvTable.setAdapter(adapter);
        }
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
        String key = FirebaseDatabase.getInstance().getReference("qrcodes").child(qr.getKey()).child("items").push().getKey();
        artikel.setId(key);
        FirebaseDatabase.getInstance().getReference("qrcodes").child(qr.getKey()).child("items").child(key).setValue(artikel);
    }

    @Override
    public void onBackPressed()
    {
        onFinish(null);
    }

    @Override
    protected void onDestroy()
    {
        onFinish(null);
        super.onDestroy();
    }

    public void onFinish(View view)
    {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("qrcodes").child(qr.getKey());
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.hasChild("items"))
                {
                    ref.removeValue();
                    Toast.makeText(QrCodeAddTableActivity.this, "Der Qr-Code wurde gelöscht, da keine Artikel hinzugefügt wurden", Toast.LENGTH_SHORT).show();
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
        FirebaseStorage.getInstance().getReference("qrs").child(qr.getQrId() + ".png").getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            photoPrinter.printBitmap(qr.getQrId()+"-print", bitmap);
        }).addOnFailureListener(exception -> {
        });
    }

    @Override
    public void onListItemClick(Artikel model, int requestCode)
    {
        FirebaseDatabase.getInstance().getReference("qrcodes").child(qr.getKey()).child("items").child(model.getId()).removeValue();
    }
}
