package at.wifi.swdev.android.wgapp.qr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class QrCodeAddTableListAdapter extends FirebaseRecyclerAdapter<Artikel, QrCodeAddTableListAdapter.QrTableViewHolder>
{
    private onListItemClickListener<Artikel> listener;


    QrCodeAddTableListAdapter(@NonNull FirebaseRecyclerOptions<Artikel> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull QrTableViewHolder holder, int position, @NonNull Artikel model)
    {
        holder.tvTitle.setText(model.getTitle());
        holder.tvContent.setText(model.getContent());
        holder.tvQuantity.setText(String.valueOf(model.getQuantity()));
        holder.delIV.setOnClickListener(view -> {
            listener.onListItemClick(model, 0);
        });
    }

    @NonNull
    @Override
    public QrTableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new QrTableViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_qr_edit_item, parent, false));
    }

    static class QrTableViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tvTitle;
        private TextView tvContent;
        private TextView tvQuantity;
        private ImageView delIV;
        QrTableViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitel);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            delIV = itemView.findViewById(R.id.ivDelete);
        }
    }


    public void setListener(onListItemClickListener<Artikel> listener) {
        this.listener = listener;
    }
}
