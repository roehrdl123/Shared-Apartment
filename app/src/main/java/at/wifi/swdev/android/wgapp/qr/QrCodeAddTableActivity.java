package at.wifi.swdev.android.wgapp.qr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.data.QRItems;
import at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeListBinding;
import at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeTableAddBinding;
import at.wifi.swdev.android.wgapp.shoppinglist.CustomShoppingListActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class QrCodeAddTableActivity extends AppCompatActivity
{
    public static final int REQUEST_CODE_NEW_QR_ITEM = 1234;
    private ActivityQrCodeTableAddBinding binding;
    private static final String TAG = QrCodeAddTableActivity.class.getSimpleName();
    private QRItems qr;
    private QrCodeAddTableListAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityQrCodeTableAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Neuer QrCode");

        findQr();
    }

    private void findQr()
    {

        StorageReference listRef = FirebaseStorage.getInstance().getReference("qrs");

        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>()
        {
            @Override
            public void onSuccess(final ListResult listResult)
            {
                FirebaseDatabase.getInstance().getReference("qrcodes").addListenerForSingleValueEvent(new ValueEventListener()
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
                        }
                        else
                        {
                            Toast.makeText(QrCodeAddTableActivity.this, "Kein QR-Code verfügbar. Kontaktieren sie den Service :)", Toast.LENGTH_SHORT).show();
                            onFinish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                    }
                });
            }
        });

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
        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                qr.setQrCodeURL(uri.getPath());
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                qr.setQrCodeURL("error");
            }
        });

        if (qr != null)
        {
            binding.tvIdQrList.setText(getString(R.string.id) + qr.getQrId());

            recyclerView = binding.rvTable;

            String key = FirebaseDatabase.getInstance().getReference("qrcodes").push().getKey();
            qr.setKey(key);
            FirebaseDatabase.getInstance().getReference("qrcodes").child(key).setValue(qr);
            Query qrcodeQuery = FirebaseDatabase.getInstance().getReference("qrcodes").child(key).child("items");

            FirebaseRecyclerOptions<Artikel> options = new FirebaseRecyclerOptions.Builder<Artikel>().setLifecycleOwner(QrCodeAddTableActivity.this).setQuery(qrcodeQuery, Artikel.class).build();

            adapter = new QrCodeAddTableListAdapter(options);
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

    public void onFinish(View view)
    {
        finish();
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
        switch (item.getItemId())
        {
            case R.id.print:
                printQr();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void printQr()
    {
        final PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference("qrs").child(qr.getQrId() + ".png").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>()
        {
            @Override
            public void onSuccess(byte[] bytes)
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                photoPrinter.printBitmap("test-print", bitmap);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // Handle any errors
            }
        });
    }
}
