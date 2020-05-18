package at.wifi.swdev.android.wgapp.todolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Todo;
import at.wifi.swdev.android.wgapp.databinding.ActivityTodoListMainBinding;

public class TodoListMainActivity extends AppCompatActivity
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
        dialogAdd.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
        Query todotemplates = FirebaseDatabase.getInstance().getReference("templates").child("todos");
        ///TODO: firebase query to array
        spinnerArrayAdapter = new ArrayAdapter<Todo>(this, android.R.layout.simple_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }

    public void onAddCustomTodo(View view)
    {
        dialogAdd.dismiss();
        Intent intent = new Intent(this, CustomTodoListActivity.class);
        startActivityForResult(intent, TODOLIST_CUSTOM_REQUEST_CODE);
    }

    private void onAddTemplate()
    {
        Spinner sp = dialogTemplate.findViewById(R.id.spTemplateTodo);
        if (sp.getSelectedItem() != null)
        {
            ///TODO: get Todo from spinner
//            int pos = sp.getSelectedItemPosition();
//            Todo todo = model.getTemplateAt(pos);
//            model.addTodo(todo);
//            adapter.setTodos(model.getTodos());
//            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == RESULT_OK && data != null)
        {
            switch (requestCode)
            {
                case TODOLIST_CUSTOM_REQUEST_CODE:
                    customRequest(data);
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void customRequest(Intent data)
    {
        FirebaseDatabase.getInstance().getReference("todos").push().setValue((Todo) data.getSerializableExtra(CustomTodoListActivity.EXTRA_ID));

        //TODO: spinner updaten
    }
}
