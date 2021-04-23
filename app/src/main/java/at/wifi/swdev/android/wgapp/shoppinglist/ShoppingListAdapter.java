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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class ShoppingListAdapter extends FirebaseRecyclerAdapter<Artikel, ShoppingListAdapter.ShoppingViewHolder>
{
    private onListItemClickListener<Artikel> listItemClickListener;
    private Context context;

    ShoppingListAdapter(@NonNull FirebaseRecyclerOptions<Artikel> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position, @NonNull final Artikel model)
    {
        holder.titleTV.setText(model.getTitle());
        holder.amountTV.setText(String.valueOf(model.getQuantity()));
        holder.doneIV.setImageResource(model.isDone() ? R.drawable.ic_check_green_24dp: R.drawable.ic_check_box_outline_blank_black_24dp);
        if(!model.isDone())
        {
            holder.doneIV.setOnClickListener(v -> {
                model.setDone(true);
                FirebaseDatabase.getInstance().getReference("shoppinglist").child(FirebaseAuth.getInstance().getUid()).child(model.getId()).setValue(model);
            });
        }

        holder.itemView.setOnClickListener(v -> listItemClickListener.onListItemClick(model, ShowItemShoppingListActivity.REQUEST_CODE_INFO));
        holder.editIV.setOnClickListener(v -> listItemClickListener.onListItemClick(model, ShowItemShoppingListActivity.REQUEST_CODE_EDIT));


        holder.deleteIV.setOnClickListener(v ->
        {
            FirebaseDatabase.getInstance().getReference("shoppinglist").child(FirebaseAuth.getInstance().getUid()).child(model.getId()).removeValue();
            Toast.makeText(context, "Artikel wurde gelöscht", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ShoppingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_shoppinglist_item, parent, false));
    }

    static class ShoppingViewHolder extends RecyclerView.ViewHolder
    {
        private final ImageView doneIV;
        private final TextView amountTV;
        private final TextView titleTV;
        private final ImageView editIV;
        private final ImageView deleteIV;

        ShoppingViewHolder(@NonNull View itemView)
        {
            super(itemView);
            doneIV = itemView.findViewById(R.id.ivDoneToDo);
            amountTV = itemView.findViewById(R.id.tvAmount);
            titleTV = itemView.findViewById(R.id.tvTitleToDo);
            editIV = itemView.findViewById(R.id.ivEditQr);
            deleteIV = itemView.findViewById(R.id.ivDeleteQr);
        }
    }

    public void setOnClickListener(onListItemClickListener<Artikel> listener)
    {
        listItemClickListener = listener;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }
}
