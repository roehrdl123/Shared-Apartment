package at.wifi.swdev.android.wgapp.qr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;

public class QrCodeAddTableListAdapter extends FirebaseRecyclerAdapter<Artikel, QrCodeAddTableListAdapter.QrTableViewHolder>
{
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
    }

    @NonNull
    @Override
    public QrTableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new QrTableViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_qr_table_add_items, parent, false));
    }

    static class QrTableViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tvTitle;
        private TextView tvContent;
        private TextView tvQuantity;
        QrTableViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitleQrArticle);
            tvContent = itemView.findViewById(R.id.tvContentQrArticle);
            tvQuantity = itemView.findViewById(R.id.tvQuantityQrArticle);
        }
    }
}
