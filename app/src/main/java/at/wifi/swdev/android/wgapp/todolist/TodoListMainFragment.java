package at.wifi.swdev.android.wgapp.todolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import at.wifi.swdev.android.wgapp.MainActivity;
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

        setHasOptionsMenu(true);

        RecyclerView recyclerView = root.findViewById(R.id.rvTodoList);

        Query todoQuery = FirebaseDatabase.getInstance().getReference("todos").child(FirebaseAuth.getInstance().getUid());

        FirebaseRecyclerOptions<Todo> options = new FirebaseRecyclerOptions.Builder<Todo>().setLifecycleOwner(this).setQuery(todoQuery, Todo.class).build();

        ToDoListAdapter adapter = new ToDoListAdapter(options);
        adapter.setContext(getContext());
        adapter.setOnListItemClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        root.findViewById(R.id.btnAddToTodo).setOnClickListener(v -> onAddToDoList());
        return root;
    }

    private void onAddToDoList()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());

        builder.setTitle("Wähle aus!");
        builder.setView(R.layout.popup_todolist);

        dialogAdd = builder.create();
        dialogAdd.show();
        bindListeners();
    }

    private void bindListeners()
    {
        dialogAdd.findViewById(R.id.btnAddTodoCustom).setOnClickListener(v -> onAddCustomTodo());
        dialogAdd.findViewById(R.id.btnAddTodoTemp).setOnClickListener(v -> onAddTemplateTodo());
    }

    private void onAddTemplateTodo()
    {
        FirebaseDatabase.getInstance().getReference("templates").child(FirebaseAuth.getInstance().getUid()).child("todos").orderByChild("title").addListenerForSingleValueEvent(new ValueEventListener()
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
        String key = FirebaseDatabase.getInstance().getReference("todos").child(FirebaseAuth.getInstance().getUid()).push().getKey();
        todo.setId(key);
        FirebaseDatabase.getInstance().getReference("todos").child(FirebaseAuth.getInstance().getUid()).child(key).setValue(todo);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.delete_done, menu);
        inflater.inflate(R.menu.signout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.menuDelete)
        {
            removeAll();
            return true;
        }
        else if (item.getItemId() == R.id.menu_signout)
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

    private void removeAll()
    {
        FirebaseDatabase.getInstance().getReference("todos").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                List<String> artikelKeys = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    if (data.getValue(Todo.class).isDone())
                    {
                        artikelKeys.add(data.getKey());
                    }
                }

                for (String artKey : artikelKeys)
                {
                    FirebaseDatabase.getInstance().getReference("todos").child(FirebaseAuth.getInstance().getUid()).child(artKey).removeValue();
                }
                Toast.makeText(getContext(), R.string.delete_all_todo, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
