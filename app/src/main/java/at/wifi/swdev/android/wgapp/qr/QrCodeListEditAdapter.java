package at.wifi.swdev.android.wgapp.qr;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.data.QRItems;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class QrCodeListEditAdapter extends FirebaseRecyclerAdapter<Artikel, QrCodeListEditAdapter.EditViewHolder>
{
    private boolean editable;
    private onListItemClickListener<Artikel> listener;

    public QrCodeListEditAdapter(@NonNull FirebaseRecyclerOptions<Artikel> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull EditViewHolder holder, int position, @NonNull final Artikel model)
    {
        holder.tvQuantity.setText(String.valueOf(model.getQuantity()));
        holder.tvTitle.setText(String.valueOf(model.getTitle()));
        holder.tvContent.setText(String.valueOf(model.getContent()));

        holder.ivDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                listener.onListItemClick(model, 0);
            }
        });
    }

    @NonNull
    @Override
    public EditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new EditViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_qr_edit_item, parent, false));
    }

    class EditViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView ivDelete;
        public TextView tvTitle;
        public TextView tvContent;
        public TextView tvQuantity;

        public EditViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvTitle = itemView.findViewById(R.id.tvTitel);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    public void setOnListItemClickListener(onListItemClickListener<Artikel> onListItemClickListener)
    {
        this.listener = onListItemClickListener;
    }
}
