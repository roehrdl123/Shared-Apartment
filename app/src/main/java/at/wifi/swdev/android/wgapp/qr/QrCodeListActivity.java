package at.wifi.swdev.android.wgapp.qr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.QRItems;
import at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeListBinding;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class QrCodeListActivity extends AppCompatActivity implements onListItemClickListener<QRItems>
{
    private ActivityQrCodeListBinding binding;
    private QrCodeListAdapter adapter;
    private List<QRItems> qrItems;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityQrCodeListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle(R.string.qrcodeActionBar);
        //TODO: RecylcerView Einbauen mit Map Qr

        findQr();
    }


    private void findQr()
    {
        setList();
        FirebaseDatabase.getInstance().getReference("qrcodes").addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                setList();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                setList();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {
                setList();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                setList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setList()
    {
        FirebaseStorage.getInstance().getReference("qrs").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>()
        {
            @Override
            public void onSuccess(final ListResult listResult)
            {
                FirebaseDatabase.getInstance().getReference("qrcodes").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        Map<Integer, QRItems> allQrsMap = new HashMap();
                        for (StorageReference item : listResult.getItems())
                        {
                            int id = Integer.parseInt(item.getName().split("\\.")[0]);
                            QRItems qr = new QRItems();
                            qr.setQrId(id);
                            qr.setOccupied(false);
                            allQrsMap.put(id, qr);
                        }
                        for (DataSnapshot data : dataSnapshot.getChildren())
                        {
                            QRItems qrItem = data.getValue(QRItems.class);
                            allQrsMap.put(qrItem.getQrId(), qrItem);
                        }


                        qrItems = new ArrayList<>(allQrsMap.values());

                        if(adapter == null)
                        {
                            adapter = new QrCodeListAdapter();
                            adapter.setOnListItemClickListener(QrCodeListActivity.this);
                            adapter.setResources(getResources());
                            binding.rvQrs.setLayoutManager(new LinearLayoutManager(QrCodeListActivity.this));
                            binding.rvQrs.setAdapter(adapter);
                        }


                        adapter.setItems(qrItems);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                    }
                });
            }
        });
    }

    public void onNewQr(View view)
    {
        FirebaseStorage.getInstance().getReference("qrs").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>()
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
                            Intent intent = new Intent(QrCodeListActivity.this, QrCodeAddTableActivity.class);
                            startActivity(intent);
                        } else
                        {
                            Toast.makeText(QrCodeListActivity.this, "Kein QR-Code verfügbar. Kontaktieren sie den Service :)", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onListItemClick(QRItems model, int requestCode)
    {
        //TODO: Activity für Editieren und Anzeigen des QR-Codes
    }
}
