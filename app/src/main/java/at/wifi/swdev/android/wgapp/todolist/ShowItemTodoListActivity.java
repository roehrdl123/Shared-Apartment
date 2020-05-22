package at.wifi.swdev.android.wgapp.todolist;

import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Todo;
import at.wifi.swdev.android.wgapp.databinding.ActivityShowItemTodoListBinding;

public class ShowItemTodoListActivity extends AppCompatActivity
{
    public static final String REQUEST_CODE_EXTRA = "requestCode";
    public static final String SERIALIZABLE_EXTRA = "item";
    public static final int REQUEST_CODE_EDIT = 1;
    public static final int REQUEST_CODE_INFO = 2;
    private ActivityShowItemTodoListBinding binding;
    private Todo todo;
    private boolean isEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityShowItemTodoListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        todo = (Todo) getIntent().getSerializableExtra(SERIALIZABLE_EXTRA);

        binding.swDone.setChecked(todo.isDone());
        binding.etTitle.setText(todo.getTitle());
        binding.etContent.setText(todo.getContent());

        int requestCode = getIntent().getIntExtra(REQUEST_CODE_EXTRA, 0);
        if (requestCode == REQUEST_CODE_EDIT)
        {
            isEditing = true;
            showEdit();
        } else if (requestCode == REQUEST_CODE_INFO)
        {
            isEditing = false;
            showInfo();
        }
    }

    private void showEdit()
    {
        binding.tvHeadline.setText(R.string.edit);
        binding.swDone.setClickable(true);
        binding.etTitle.setKeyListener((KeyListener) binding.etTitle.getTag());
        binding.etContent.setKeyListener((KeyListener) binding.etContent.getTag());
    }

    private void showInfo()
    {
        binding.tvHeadline.setText(R.string.info);
        binding.swDone.setClickable(false);

        EditText text = binding.etTitle;
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
        } else
        {
            if (binding.etTitle.getText() != null && binding.etContent.getText() != null)
            {
                todo.setTitle(binding.etTitle.getText().toString());
                todo.setContent(binding.etContent.getText().toString());
                todo.setDone(binding.swDone.isChecked());

                FirebaseDatabase.getInstance().getReference("todos").child(todo.getId()).setValue(todo);
                Toast.makeText(this, "Der Todo-Eintrag wurde geändert!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void onDelete(View view)
    {
        FirebaseDatabase.getInstance().getReference("todos").child(todo.getId()).removeValue();
        Toast.makeText(this, "Der Todo-Eintrag wurde gelöscht", Toast.LENGTH_SHORT).show();
        finish();
    }
}
