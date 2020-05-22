package at.wifi.swdev.android.wgapp.shoppinglist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.databinding.ActivityShoopingListBinding;
import at.wifi.swdev.android.wgapp.onListItemClickListener;
import at.wifi.swdev.android.wgapp.qr.QrCodeListActivity;

public class ShoppingListActivity extends AppCompatActivity implements onListItemClickListener<Artikel>
{
    public static final int REQUEST_CODE_QR = 11;
    public static final int REQUEST_CODE_ADD = 12;
    private ActivityShoopingListBinding binding;
    private ShoppingListAdapter adapter;
    private AlertDialog dialogAdd;
    private AlertDialog dialogTemplate;
    private RecyclerView recyclerView;
    private Spinner templateSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityShoopingListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = binding.rvShoppingList;

        Query shoppinglistQuery = FirebaseDatabase.getInstance().getReference("shoppinglist");

        FirebaseRecyclerOptions<Artikel> options = new FirebaseRecyclerOptions.Builder<Artikel>().setLifecycleOwner(this).setQuery(shoppinglistQuery, Artikel.class).build();

        adapter = new ShoppingListAdapter(options);
        adapter.setOnClickListener(this);
        adapter.setContext(this);
        binding.rvShoppingList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvShoppingList.setAdapter(adapter);
    }

    public void addShoppingList(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        builder.setTitle("Wähle aus!");
        builder.setView(R.layout.popup_shoppinglist);

        dialogAdd = builder.create();
        dialogAdd.show();
    }

    public void onAddQRShoppingList(View view)
    {
        Intent intent = new Intent(this, QrCodeScannerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_QR);
        dialogAdd.dismiss();
    }

    public void onAddCustomShoppingList(View view)
    {
        Intent intent = new Intent(this, CustomShoppingListActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ADD);
        dialogAdd.dismiss();
    }

    public void onAddTemplateShoppingList(View view)
    {

        FirebaseDatabase.getInstance().getReference("templates").child("shoppinglist").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChildren())
                {
                    dialogAdd.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingListActivity.this);

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                        }
                    });

                    builder.setPositiveButton(R.string.string_add, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            onAddTemplate();
                        }
                    });

                    builder.setTitle("Wähle aus einer Vorlage!");
                    builder.setView(R.layout.popup_template_add_shopping);

                    dialogTemplate = builder.create();
                    dialogTemplate.show();

                    templateSpinner = dialogTemplate.findViewById(R.id.spTemplateTodo);
                    List<Artikel> artikel = new ArrayList<>();
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        artikel.add(data.getValue(Artikel.class));
                    }
                    ArrayAdapter<Artikel> spinnerArrayAdapter = new ArrayAdapter<Artikel>(ShoppingListActivity.this, android.R.layout.simple_spinner_item, artikel);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    templateSpinner.setAdapter(spinnerArrayAdapter);
                }
                else
                {
                    Toast.makeText(ShoppingListActivity.this, R.string.no_template, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }


    public void onAddTemplate()
    {

        Spinner spinner = dialogTemplate.findViewById(R.id.spTemplateTodo);
        if (spinner.getSelectedItem() != null)
        {
            Artikel a = (Artikel) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
            TextView tvAmountTemplate = dialogTemplate.findViewById(R.id.tvAnzahlTemplate);
            if (tvAmountTemplate.getText().length() != 0 && tvAmountTemplate.getText() != null)
            {
                a.setQuantity(Integer.parseInt(tvAmountTemplate.getText().toString()));
            }
            addToDatabase(a);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (RESULT_OK == resultCode && data != null)
        {
            switch (requestCode)
            {
                case REQUEST_CODE_QR:
                    qrCodesInList(data);
                    break;
                case REQUEST_CODE_ADD:
                    addToDatabase((Artikel) data.getSerializableExtra(CustomShoppingListActivity.ITEM_EXTRA));
                    break;
                default:
                    Toast.makeText(this, "Fehler!", Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addToDatabase(Artikel artikel)
    {
        String key = FirebaseDatabase.getInstance().getReference("shoppinglist").push().getKey();
        artikel.setId(key);
        FirebaseDatabase.getInstance().getReference("shoppinglist").child(key).setValue(artikel);
    }

    private void qrCodesInList(Intent data)
    {
        Map<String, Object> updates = new HashMap<>();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("shoppinglist");
        ArrayList artikel = data.getParcelableArrayListExtra(QrCodeItemsActivity.QR_ARTIKEL_ID);
        for (Object art : artikel)
        {
            if (art instanceof Artikel)
            {
                Artikel currentArtikel = (Artikel) art;
                String key = database.push().getKey();
                updates.put(key, currentArtikel);
            }
        }

        database.updateChildren(updates);
    }

    @Override
    public void onListItemClick(Artikel model, int requestcode)
    {
        Intent intent = new Intent(this, ShowItemShoppingListActivity.class);
        intent.putExtra(ShowItemShoppingListActivity.REQUEST_CODE_EXTRA, requestcode);
        intent.putExtra(ShowItemShoppingListActivity.SERIALIZABLE_EXTRA, model);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.qr_codes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuQrTable:
                startQrMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startQrMenu()
    {
        Intent intent = new Intent(this, QrCodeListActivity.class);
        startActivity(intent);
    }
}
