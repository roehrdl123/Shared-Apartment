package at.wifi.swdev.android.wgapp.shoppinglist;

import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.databinding.ActivityShowItemShoppingListBinding;

public class ShowItemShoppingListActivity extends AppCompatActivity
{
    private ActivityShowItemShoppingListBinding binding;
    public static final String REQUEST_CODE_EXTRA = "requestCode";
    public static final String SERIALIZABLE_EXTRA = "item";
    public static final int REQUEST_CODE_EDIT = 1;
    public static final int REQUEST_CODE_INFO = 2;
    private Artikel artikel;
    private boolean isEditing;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityShowItemShoppingListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle(R.string.shoppingList);

        artikel = (Artikel) getIntent().getSerializableExtra(SERIALIZABLE_EXTRA);

        binding.swDone.setChecked(artikel.isDone());
        binding.etAmount.setText(artikel.getQuantity() + "");
        binding.etTitle.setText(artikel.getTitle());
        binding.etContent.setText(artikel.getContent());

        int requestCode = getIntent().getIntExtra(REQUEST_CODE_EXTRA, 0);
        if (requestCode == REQUEST_CODE_EDIT)
        {
            isEditing = true;
            showEdit();
        }
        else if (requestCode == REQUEST_CODE_INFO)
        {
            isEditing = false;
            showInfo();
        }
    }

    private void showEdit()
    {
        binding.edit.setText(R.string.save);
        binding.tvHeadline.setText(R.string.edit);
        binding.swDone.setClickable(true);
        binding.etAmount.setKeyListener((KeyListener) binding.etAmount.getTag());
        binding.etTitle.setKeyListener((KeyListener) binding.etTitle.getTag());
        binding.etContent.setKeyListener((KeyListener) binding.etContent.getTag());
    }

    private void showInfo()
    {
        binding.edit.setText(R.string.edit);
        binding.tvHeadline.setText(R.string.info);
        binding.swDone.setClickable(false);
        EditText text = binding.etAmount;
        text.setTag(text.getKeyListener());
        text.setKeyListener(null);

        text = binding.etTitle;
        text.setTag(text.getKeyListener());
        text.setKeyListener(null);

        text = binding.etContent;
        text.setTag(text.getKeyListener());
        text.setKeyListener(null);
    }


    public void onClose(View view)
    {
        finish();
    }

    public void onEdit(View view)
    {
        if (!isEditing)
        {
            isEditing = true;
            Toast.makeText(this, R.string.changeAllowed, Toast.LENGTH_SHORT).show();
            showEdit();
        }
        else
        {
            if(binding.etAmount.getText() != null && Integer.parseInt(binding.etAmount.getText().toString()) >= 0)
            {
                if (binding.etTitle.getText() != null && binding.etContent.getText() != null)
                {
                    artikel.setQuantity(Integer.parseInt(binding.etAmount.getText().toString()));
                    artikel.setTitle(binding.etTitle.getText().toString());
                    artikel.setContent(binding.etContent.getText().toString());
                    artikel.setDone(binding.swDone.isChecked());

                    FirebaseDatabase.getInstance().getReference("shoppinglist").child(artikel.getId()).setValue(artikel);
                    Toast.makeText(this, R.string.article_edited, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else
            {
                Toast.makeText(this, R.string.amount_zero, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onDelete(View view)
    {
        FirebaseDatabase.getInstance().getReference("shoppinglist").child(artikel.getId()).removeValue();
        Toast.makeText(this, "Artikel wurde gelöscht", Toast.LENGTH_SHORT).show();
        finish();
    }
}
