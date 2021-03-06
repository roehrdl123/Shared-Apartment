package at.wifi.swdev.android.wgapp.shoppinglist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
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

import at.wifi.swdev.android.wgapp.MainActivity;
import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.onListItemClickListener;
import at.wifi.swdev.android.wgapp.qr.QrCodeListActivity;

import static android.app.Activity.RESULT_OK;

public class ShoppingListFragment extends Fragment implements onListItemClickListener<Artikel>
{
    private static final int REQUEST_CODE_QR = 11;
    private static final int REQUEST_CODE_ADD = 12;
    private AlertDialog dialogAdd;
    private AlertDialog dialogTemplate;
    private Spinner templateSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_shooping_list, container, false);

        setHasOptionsMenu(true);

        RecyclerView recyclerView = root.findViewById(R.id.rvShoppingList);
        root.findViewById(R.id.btnAddToShop).setOnClickListener(view -> addShoppingList());
        Query shoppinglistQuery = FirebaseDatabase.getInstance().getReference("shoppinglist").orderByChild("title");

        FirebaseRecyclerOptions<Artikel> options = new FirebaseRecyclerOptions.Builder<Artikel>().setLifecycleOwner(this).setQuery(shoppinglistQuery, Artikel.class).build();

        ShoppingListAdapter adapter = new ShoppingListAdapter(options);
        adapter.setOnClickListener(this);
        adapter.setContext(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        return root;
    }

    private void addShoppingList()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());

        builder.setTitle("Wähle aus!");
        builder.setView(R.layout.popup_shoppinglist);

        dialogAdd = builder.create();
        dialogAdd.show();
        addListeners();

    }

    private void onAddQRShoppingList()
    {
        Intent intent = new Intent(getContext(), QrCodeScannerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_QR);
        dialogAdd.dismiss();
    }

    private void onAddCustomShoppingList()
    {
        Intent intent = new Intent(getContext(), CustomShoppingListActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ADD);
        dialogAdd.dismiss();
    }

    private void onAddTemplateShoppingList()
    {

        FirebaseDatabase.getInstance().getReference("templates").child(FirebaseAuth.getInstance().getUid()).child("shoppinglist").orderByChild("title").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChildren())
                {
                    dialogAdd.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());

                    builder.setPositiveButton(R.string.string_add, (dialog, id) -> onAddTemplate());

                    builder.setTitle(R.string.choose_template);
                    builder.setView(R.layout.popup_template_add_shopping);

                    dialogTemplate = builder.create();
                    dialogTemplate.show();

                    templateSpinner = dialogTemplate.findViewById(R.id.spCalendar);
                    List<Artikel> artikel = new ArrayList<>();
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        artikel.add(data.getValue(Artikel.class));
                    }
                    ArrayAdapter<Artikel> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, artikel);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    templateSpinner.setAdapter(spinnerArrayAdapter);
                    templateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                        {
                            ((EditText) dialogTemplate.findViewById(R.id.etAnzahl)).setText(String.valueOf(artikel.get(i).getQuantity()));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView)
                        {

                        }
                    });
                } else
                {
                    Toast.makeText(getContext(), R.string.no_template, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }


    private void onAddTemplate()
    {

        Spinner spinner = dialogTemplate.findViewById(R.id.spCalendar);
        if (spinner.getSelectedItem() != null)
        {
            Artikel a = (Artikel) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
            TextView tvAmountTemplate = dialogTemplate.findViewById(R.id.etAnzahl);
            if (tvAmountTemplate.getText().length() != 0 && tvAmountTemplate.getText() != null)
            {
                a.setQuantity(Integer.parseInt(tvAmountTemplate.getText().toString()));
            }
            addToDatabase(a);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
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
                    Toast.makeText(getContext(), "Fehler!", Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addToDatabase(Artikel artikel)
    {
        String key = FirebaseDatabase.getInstance().getReference("shoppinglist").child(FirebaseAuth.getInstance().getUid()).push().getKey();
        artikel.setId(key);
        FirebaseDatabase.getInstance().getReference("shoppinglist").child(FirebaseAuth.getInstance().getUid()).child(key).setValue(artikel);
    }

    private void qrCodesInList(Intent data)
    {
        Map<String, Object> updates = new HashMap<>();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("shoppinglist").child(FirebaseAuth.getInstance().getUid());
        ArrayList artikel = data.getParcelableArrayListExtra(QrCodeItemsActivity.QR_ARTIKEL_ID);
        for (Object art : artikel)
        {
            if (art instanceof Artikel)
            {
                Artikel currentArtikel = (Artikel) art;
                String key = database.push().getKey();
                currentArtikel.setId(key);
                updates.put(key, currentArtikel);
            }
        }

        database.updateChildren(updates);
    }

    @Override
    public void onListItemClick(Artikel model, int requestcode)
    {
        Intent intent = new Intent(getContext(), ShowItemShoppingListActivity.class);
        intent.putExtra(ShowItemShoppingListActivity.REQUEST_CODE_EXTRA, requestcode);
        intent.putExtra(ShowItemShoppingListActivity.SERIALIZABLE_EXTRA, model);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        MenuCompat.setGroupDividerEnabled(menu, true);

        inflater.inflate(R.menu.qr_codes, menu);
        inflater.inflate(R.menu.delete_done, menu);
        inflater.inflate(R.menu.signout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.menuQrTable)
        {
            startQrMenu();
            return true;
        } else if (item.getItemId() == R.id.menuDelete)
        {
            removeAll();
            return true;
        } else if (item.getItemId() == R.id.menu_signout)
        {
            signout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signout()
    {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void startQrMenu()
    {
        Intent intent = new Intent(getContext(), QrCodeListActivity.class);
        startActivity(intent);
    }

    private void removeAll()
    {
        FirebaseDatabase.getInstance().getReference("shoppinglist").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                List<String> artikelKeys = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    if (data.getValue(Artikel.class).isDone())
                    {
                        artikelKeys.add(data.getKey());
                    }
                }

                for (String artKey : artikelKeys)
                {
                    FirebaseDatabase.getInstance().getReference("shoppinglist").child(FirebaseAuth.getInstance().getUid()).child(artKey).removeValue();
                }
                Toast.makeText(getContext(), R.string.delete_all_art, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void addListeners()
    {
        dialogAdd.findViewById(R.id.btnQrShopping).setOnClickListener(view -> onAddQRShoppingList());
        dialogAdd.findViewById(R.id.btnCustomShopping).setOnClickListener(view -> onAddCustomShoppingList());
        dialogAdd.findViewById(R.id.btnVorlageShopping).setOnClickListener(view -> onAddTemplateShoppingList());
    }
}
