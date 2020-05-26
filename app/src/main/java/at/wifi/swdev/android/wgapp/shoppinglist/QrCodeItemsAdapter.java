package at.wifi.swdev.android.wgapp.shoppinglist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class QrCodeItemsAdapter extends RecyclerView.Adapter<QrCodeItemsAdapter.QrItemViewHolder>
{
    private List<Artikel> artikel;
    private LayoutInflater inflater;
    private onListItemClickListener<Artikel> onListItemClickListener;

    QrCodeItemsAdapter(Context context)
    {
        this.inflater = LayoutInflater.from(context);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final QrItemViewHolder holder, final int position)
    {
        final Artikel currArtikel = artikel.get(position);
        holder.etQuantity.setText(currArtikel.getQuantity()+"");
        holder.tvTitle.setText(currArtikel.getTitle());
        holder.btnChangeQuantitiy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                currArtikel.setQuantity(Integer.parseInt(holder.etQuantity.getText().toString()));
            }
        });
        holder.ivDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                artikel.remove(position);
                if(artikel.size() == 0)
                {
                    onListItemClickListener.onListItemClick(currArtikel, 0);
                }
            }
        });
    }

    @NonNull
    @Override
    public QrItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new QrItemViewHolder(inflater.inflate(R.layout.view_qr_scanned_item, parent, false));
    }


    @Override
    public int getItemCount()
    {
        return artikel.size();
    }

    static class QrItemViewHolder extends RecyclerView.ViewHolder
    {
        private Button btnChangeQuantitiy;
        private EditText etQuantity;
        private TextView tvTitle;
        private ImageView ivDelete;

        QrItemViewHolder(@NonNull View itemView)
        {
            super(itemView);
            btnChangeQuantitiy = itemView.findViewById(R.id.btnChangeQuantity);
            tvTitle = itemView.findViewById(R.id.tvTitleQrArticle);
            etQuantity = itemView.findViewById(R.id.txAmount);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }

    void setList(List<Artikel> artikel)
    {
        this.artikel = artikel;
    }

    public List<Artikel> getArtikel()
    {
        return artikel;
    }

    public void setOnClickListener(onListItemClickListener<Artikel> onClickListener)
    {
        this.onListItemClickListener = onClickListener;
    }
}
