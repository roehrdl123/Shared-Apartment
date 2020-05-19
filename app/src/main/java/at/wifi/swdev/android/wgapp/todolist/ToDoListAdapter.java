package at.wifi.swdev.android.wgapp.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Todo;

public class ToDoListAdapter extends FirebaseRecyclerAdapter<Todo, ToDoListAdapter.TodoViewHolder>
{
    public ToDoListAdapter(@NonNull FirebaseRecyclerOptions<Todo> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TodoViewHolder holder, int position, @NonNull Todo model)
    {
        holder.titleTodoTV.setText(model.getTitle());
        holder.doneTodoIV.setVisibility(model.isErledigt() ? View.VISIBLE : View.INVISIBLE);
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new TodoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_todolist_item, parent, false));
    }

    class TodoViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView doneTodoIV;
        private ImageView editTodoTV;
        private ImageView infoTodoTV;
        private ImageView deleteTodoTV;
        private TextView titleTodoTV;
        private View dividerTodo;

        public TodoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            doneTodoIV = itemView.findViewById(R.id.ivDoneToDo);
            editTodoTV = itemView.findViewById(R.id.ivEditQr);
            infoTodoTV = itemView.findViewById(R.id.ivInfoQr);
            deleteTodoTV = itemView.findViewById(R.id.ivDeleteQr);
            titleTodoTV = itemView.findViewById(R.id.tvTitleToDo);
            dividerTodo = itemView.findViewById(R.id.dividerToDo);
        }
    }
}


