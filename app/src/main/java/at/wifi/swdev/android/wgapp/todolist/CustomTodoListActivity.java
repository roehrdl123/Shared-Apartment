package at.wifi.swdev.android.wgapp.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import at.wifi.swdev.android.wgapp.data.Todo;
import at.wifi.swdev.android.wgapp.databinding.ActivityCustomTodoListBinding;

public class CustomTodoListActivity extends AppCompatActivity
{
    public static final String EXTRA_ID = "item";
    private ActivityCustomTodoListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomTodoListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    public void onAddCustomTodo(View view)
    {
        if(binding.tvTitleTodo.getText() != null && binding.tvContentTodo.getText() != null)
        {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_ID, new Todo(binding.tvTitleTodo.getText().toString(), binding.tvContentTodo.getText().toString())));
            finish();
        }
    }

    public void onSaveAndAddTodo(View view)
    {
        if(binding.tvTitleTodo.getText() != null && binding.tvContentTodo.getText() != null)
        {
            Todo todo = new Todo(binding.tvTitleTodo.getText().toString(), binding.tvContentTodo.getText().toString());

            FirebaseDatabase.getInstance().getReference("templates").child("todos").push().setValue(todo);

            setResult(RESULT_OK, new Intent().putExtra(EXTRA_ID, todo));
            finish();
        }
    }
}
