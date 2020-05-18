package at.wifi.swdev.android.wgapp.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.databinding.ActivityCustomShoppingListBinding;

public class CustomShoppingListActivity extends AppCompatActivity
{
    public static final String ITEM_EXTRA = "Item";
    ActivityCustomShoppingListBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomShoppingListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    public void onAddToShoppingList(View view)
    {
        if(binding.tvAnzahl.getText() != null && binding.txtTitel.getText() != null)
        {
            int anz = Integer.parseInt(binding.tvAnzahlTemplate.getText().toString());
            String title = binding.txtTitel.getText().toString();

            Artikel a = new Artikel(anz, title);
            if (binding.txtBezeichnung.getText() != null)
            {
                String bez = binding.txtBezeichnung.getText().toString();
                a.setContent(bez);
            }
            setResult(RESULT_OK, new Intent().putExtra(ITEM_EXTRA, a));

            finish();
        }
    }

    public void onSaveAndAdd(View view)
    {

        if(binding.tvAnzahl.getText() != null && binding.txtTitel.getText() != null)
        {
            int anz = Integer.parseInt(binding.tvAnzahlTemplate.getText().toString());
            String title = binding.txtTitel.getText().toString();

            Artikel a = new Artikel(anz, title);
            if (binding.txtBezeichnung.getText() != null)
            {
                String bez = binding.txtBezeichnung.getText().toString();
                a.setContent(bez);
            }
            setResult(RESULT_OK, new Intent().putExtra(ITEM_EXTRA, a));

            String key = FirebaseDatabase.getInstance().getReference("templates").child("shoppinglist").push().getKey();
            a.setId(key);
            FirebaseDatabase.getInstance().getReference("templates").child("shoppinglist").child(key).setValue(a);

            finish();
        }
    }
}
