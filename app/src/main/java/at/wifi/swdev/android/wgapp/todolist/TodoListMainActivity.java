package at.wifi.swdev.android.wgapp.todolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Todo;
import at.wifi.swdev.android.wgapp.databinding.ActivityTodoListMainBinding;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class TodoListMainActivity extends AppCompatActivity implements onListItemClickListener<Todo>
{
    public static final int TODOLIST_CUSTOM_REQUEST_CODE = 123;
    private ActivityTodoListMainBinding binding;
    private ToDoListAdapter adapter;
    private RecyclerView recyclerView;
    private Dialog dialogAdd;
    private Dialog dialogTemplate;
    private ArrayAdapter<Todo> spinnerArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityTodoListMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Query todoQuery = FirebaseDatabase.getInstance().getReference("todos");

        FirebaseRecyclerOptions<Todo> options = new FirebaseRecyclerOptions.Builder<Todo>().setLifecycleOwner(this).setQuery(todoQuery, Todo.class).build();

        ToDoListAdapter adapter = new ToDoListAdapter(options);
        adapter.setContext(this);
        adapter.setOnListItemClickListener(this);

        binding.rvTodoList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTodoList.setAdapter(adapter);
    }

    public void onAddToDoList(View view)
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
        builder.setView(R.layout.popup_todolist);

        dialogAdd = builder.create();
        dialogAdd.show();
    }

    public void onAddTemplateTodo(View view)
    {
        FirebaseDatabase.getInstance().getReference("templates").child("todos").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChildren())
                {
                    dialogAdd.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(TodoListMainActivity.this);

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
                    builder.setView(R.layout.popup_template_add_todo);

                    dialogTemplate = builder.create();
                    dialogTemplate.show();

                    Spinner spinner = dialogTemplate.findViewById(R.id.spTemplateTodo);

                    List<Todo> todos = new ArrayList<>();
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        todos.add(data.getValue(Todo.class));
                    }
                    ArrayAdapter<Todo> spinnerArrayAdapter = new ArrayAdapter<Todo>(TodoListMainActivity.this, android.R.layout.simple_spinner_item, todos);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerArrayAdapter);
                }
                else
                {
                    Toast.makeText(TodoListMainActivity.this, R.string.no_template, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }

    public void onAddCustomTodo(View view)
    {
        dialogAdd.dismiss();
        Intent intent = new Intent(this, CustomTodoListActivity.class);
        startActivityForResult(intent, TODOLIST_CUSTOM_REQUEST_CODE);
    }

    private void onAddTemplate()
    {
        Spinner spinner = dialogTemplate.findViewById(R.id.spTemplateTodo);
        if (spinner.getSelectedItem() != null)
        {
            Todo todo = (Todo) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
            addToDatabase(todo);
        }
    }

    private void addToDatabase(Todo todo)
    {
        String key = FirebaseDatabase.getInstance().getReference("todos").push().getKey();
        todo.setId(key);
        FirebaseDatabase.getInstance().getReference("todos").child(key).setValue(todo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == RESULT_OK && data != null)
        {
            switch (requestCode)
            {
                case TODOLIST_CUSTOM_REQUEST_CODE:
                    addToDatabase((Todo) data.getSerializableExtra(CustomTodoListActivity.EXTRA_ID));
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(Todo model, int requestCode)
    {
        Intent intent = new Intent(this, ShowItemTodoListActivity.class);
        intent.putExtra(ShowItemTodoListActivity.REQUEST_CODE_EXTRA, requestCode);
        intent.putExtra(ShowItemTodoListActivity.SERIALIZABLE_EXTRA, model);
        startActivity(intent);
    }
}
