package at.wifi.swdev.android.wgapp.todolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Todo;
import at.wifi.swdev.android.wgapp.shoppinglist.ShowItemShoppingListActivity;

public class ToDoListAdapter extends FirebaseRecyclerAdapter<Todo, ToDoListAdapter.TodoViewHolder>
{
    private at.wifi.swdev.android.wgapp.onListItemClickListener<Todo> onListItemClickListener;
    private Context context;

    ToDoListAdapter(@NonNull FirebaseRecyclerOptions<Todo> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TodoViewHolder holder, int position, @NonNull final Todo model)
    {
        holder.titleTodoTV.setText(model.getTitle());

        holder.doneTodoIV.setImageResource(model.isDone() ? R.drawable.ic_check_green_24dp: R.drawable.ic_check_box_outline_blank_black_24dp);
        if(!model.isDone())
        {
            holder.doneTodoIV.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    model.setDone(true);
                    FirebaseDatabase.getInstance().getReference("todos").child(model.getId()).setValue(model);
                }
            });
        }
        holder.editTodoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                onListItemClickListener.onListItemClick(model, ShowItemShoppingListActivity.REQUEST_CODE_EDIT);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            onListItemClickListener.onListItemClick(model, ShowItemShoppingListActivity.REQUEST_CODE_INFO);
        }
    });
        holder.deleteTodoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                FirebaseDatabase.getInstance().getReference("todos").child(model.getId()).removeValue();
                Toast.makeText(context, "Der Todo-Eintrag wurde gelöscht", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new TodoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_todolist_item, parent, false));
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView doneTodoIV;
        private ImageView editTodoTV;
        private ImageView deleteTodoTV;
        private TextView titleTodoTV;

        TodoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            doneTodoIV = itemView.findViewById(R.id.ivDoneToDo);
            editTodoTV = itemView.findViewById(R.id.ivEditQr);
            deleteTodoTV = itemView.findViewById(R.id.ivDeleteQr);
            titleTodoTV = itemView.findViewById(R.id.tvTitleToDo);
        }
    }

    public void setOnListItemClickListener(at.wifi.swdev.android.wgapp.onListItemClickListener<Todo> onListItemClickListener) {
        this.onListItemClickListener = onListItemClickListener;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }
}


