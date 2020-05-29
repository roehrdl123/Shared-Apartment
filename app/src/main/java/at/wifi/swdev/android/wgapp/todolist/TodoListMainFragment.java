package at.wifi.swdev.android.wgapp.todolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import at.wifi.swdev.android.wgapp.onListItemClickListener;

import static android.app.Activity.RESULT_OK;

public class TodoListMainFragment extends Fragment implements onListItemClickListener<Todo>
{
    private static final int TODOLIST_CUSTOM_REQUEST_CODE = 123;
    private Dialog dialogAdd;
    private Dialog dialogTemplate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_todo_list_main, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.rvTodoList);

        Query todoQuery = FirebaseDatabase.getInstance().getReference("todos");

        FirebaseRecyclerOptions<Todo> options = new FirebaseRecyclerOptions.Builder<Todo>().setLifecycleOwner(this).setQuery(todoQuery, Todo.class).build();

        ToDoListAdapter adapter = new ToDoListAdapter(options);
        adapter.setContext(getContext());
        adapter.setOnListItemClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        root.findViewById(R.id.btnAddToTodo).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAddToDoList();
            }
        });
        return root;
    }

    private void onAddToDoList()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
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
        bindListeners();
    }

    private void bindListeners()
    {
        dialogAdd.findViewById(R.id.btnAddTodoCustom).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAddTemplateTodo();
            }
        });
        dialogAdd.findViewById(R.id.btnAddTodoCustom).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAddCustomTodo();
            }
        });
    }

    private void onAddTemplateTodo()
    {
        FirebaseDatabase.getInstance().getReference("templates").child("todos").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChildren())
                {
                    dialogAdd.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
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

                    Spinner spinner = dialogTemplate.findViewById(R.id.spCalendar);

                    List<Todo> todos = new ArrayList<>();
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        todos.add(data.getValue(Todo.class));
                    }
                    ArrayAdapter<Todo> spinnerArrayAdapter = new ArrayAdapter<Todo>(getContext(), android.R.layout.simple_spinner_item, todos);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerArrayAdapter);
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

    private void onAddCustomTodo()
    {
        dialogAdd.dismiss();
        Intent intent = new Intent(getContext(), CustomTodoListActivity.class);
        startActivityForResult(intent, TODOLIST_CUSTOM_REQUEST_CODE);
    }

    private void onAddTemplate()
    {
        Spinner spinner = dialogTemplate.findViewById(R.id.spCalendar);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == RESULT_OK && data != null)
        {
            if (requestCode == TODOLIST_CUSTOM_REQUEST_CODE)
            {
                addToDatabase((Todo) data.getSerializableExtra(CustomTodoListActivity.EXTRA_ID));

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(Todo model, int requestCode)
    {
        Intent intent = new Intent(getContext(), ShowItemTodoListActivity.class);
        intent.putExtra(ShowItemTodoListActivity.REQUEST_CODE_EXTRA, requestCode);
        intent.putExtra(ShowItemTodoListActivity.SERIALIZABLE_EXTRA, model);
        startActivity(intent);
    }
}
