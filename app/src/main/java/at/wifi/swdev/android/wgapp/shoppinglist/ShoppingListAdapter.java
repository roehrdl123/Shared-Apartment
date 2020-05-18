package at.wifi.swdev.android.wgapp.shoppinglist;

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
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class ShoppingListAdapter extends FirebaseRecyclerAdapter<Artikel, ShoppingListAdapter.ShoppingViewHolder>
{
    private onListItemClickListener listItemClickListener;
    private Context context;

    public ShoppingListAdapter(@NonNull FirebaseRecyclerOptions<Artikel> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position, @NonNull final Artikel model)
    {
        holder.titleTV.setText(model.getTitle());
        holder.amountTV.setText("" + model.getQuantity());
        holder.doneIV.setVisibility(model.isDone() ? View.VISIBLE : View.INVISIBLE);

        holder.infoIV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listItemClickListener.onListItemClick(model, ShowItemShoppingListActivity.REQUEST_CODE_INFO);
            }
        });
        holder.editIV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listItemClickListener.onListItemClick(model, ShowItemShoppingListActivity.REQUEST_CODE_EDIT);
            }
        });


        holder.deleteIV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                FirebaseDatabase.getInstance().getReference("shoppinglist").child(model.getId()).removeValue();
                Toast.makeText(context, "Artikel wurde gelöscht", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ShoppingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_shoppinglist_item, parent, false));
    }

    class ShoppingViewHolder extends RecyclerView.ViewHolder
    {
        private final ImageView doneIV;
        private final TextView amountTV;
        private final TextView titleTV;
        private final ImageView infoIV;
        private final ImageView editIV;
        private final ImageView deleteIV;
        private final View divider;

        public ShoppingViewHolder(@NonNull View itemView)
        {
            super(itemView);
            doneIV = itemView.findViewById(R.id.ivDoneToDo);
            amountTV = itemView.findViewById(R.id.tvAmount);
            titleTV = itemView.findViewById(R.id.tvTitleToDo);
            infoIV = itemView.findViewById(R.id.ivInfoToDo);
            editIV = itemView.findViewById(R.id.ivEditToDo);
            deleteIV = itemView.findViewById(R.id.ivDeleteToDo);
            divider = itemView.findViewById(R.id.dividerToDo);
        }
    }

    public void setOnClickListener(onListItemClickListener listener)
    {
        listItemClickListener = listener;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }
}
