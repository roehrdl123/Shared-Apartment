package at.wifi.swdev.android.wgapp.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import at.wifi.swdev.android.wgapp.R;
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

        getSupportActionBar().setTitle(R.string.shoppingList);
    }

    public void onAddToShoppingList(View view)
    {
        if (binding.etAnzahl.getText() != null && Integer.parseInt(binding.etAnzahl.getText().toString()) >= 0)
        {
            if (binding.etTitle.getText() != null)
            {
                int anz = Integer.parseInt(binding.etAnzahl.getText().toString());
                String title = binding.etTitle.getText().toString();

                Artikel a = new Artikel(anz, title);
                if (binding.etBez.getText() != null)
                {
                    String bez = binding.etBez.getText().toString();
                    a.setContent(bez);
                }
                setResult(RESULT_OK, new Intent().putExtra(ITEM_EXTRA, a));

                finish();
            }
        }
        else
        {
            Toast.makeText(this, R.string.amount_zero, Toast.LENGTH_SHORT).show();
        }
    }

    public void onSaveAndAdd(View view)
    {
        if (binding.etAnzahl.getText() != null && Integer.parseInt(binding.etAnzahl.getText().toString()) >= 0)
        {
            if (binding.etTitle.getText() != null)
            {
                int anz = Integer.parseInt(binding.etAnzahl.getText().toString());
                String title = binding.etTitle.getText().toString();

                Artikel a = new Artikel(anz, title);
                if (binding.etBez.getText() != null)
                {
                    String bez = binding.etBez.getText().toString();
                    a.setContent(bez);
                }
                setResult(RESULT_OK, new Intent().putExtra(ITEM_EXTRA, a));

                String key = FirebaseDatabase.getInstance().getReference("templates").child(FirebaseAuth.getInstance().getUid()).child("shoppinglist").push().getKey();
                a.setId(key);
                FirebaseDatabase.getInstance().getReference("templates").child(FirebaseAuth.getInstance().getUid()).child("shoppinglist").child(key).setValue(a);

                finish();
            }
        }
        else
        {
            Toast.makeText(this, R.string.amount_zero, Toast.LENGTH_SHORT).show();
        }
    }

    public void onCancel(View view)
    {
        finish();
    }
}
