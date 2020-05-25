package at.wifi.swdev.android.wgapp.qr;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.QRItems;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class QrCodeListAdapter extends RecyclerView.Adapter<QrCodeListAdapter.QrListViewHolder>
{
    private Bitmap bitmap;
    private List<QRItems> items;
    private onListItemClickListener onListItemClickListener;
    private Resources resources;

    @Override
    public QrListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new QrListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_qr_list_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final QrListViewHolder holder, int position)
    {
        final QRItems model = items.get(position);


        holder.tvId.setText(resources.getString(R.string.id) + String.valueOf(model.getQrId()));
        holder.viewOccupied.setVisibility(model.isOccupied() ? View.VISIBLE : View.INVISIBLE);

        if(model.isOccupied())
        {
            holder.llActions.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onListItemClickListener.onListItemClick(model, QrCodeListEditActivity.RQ_CODE_EDIT);
                }
            });

            holder.ivDelete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    FirebaseDatabase.getInstance().getReference("qrcodes").child(model.getKey()).removeValue();
                }
            });

            holder.ivEdit.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onListItemClickListener.onListItemClick(model, QrCodeListEditActivity.RQ_CODE_EDIT);
                }
            });
        }
        else
        {
            holder.llActions.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
        }

        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference("qrs").child(model.getQrId() + ".png").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>()
        {
            @Override
            public void onSuccess(byte[] bytes)
            {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.ivQr.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    class QrListViewHolder extends RecyclerView.ViewHolder
    {
        private LinearLayout viewOccupied;
        private TextView tvId;
        private ImageView ivQr;
        private ImageView ivEdit;
        private ImageView ivDelete;
        private LinearLayout llActions;

        public QrListViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvIdQrList);
            viewOccupied = itemView.findViewById(R.id.viewOccupiedEdit);
            ivQr = itemView.findViewById(R.id.ivQrCode);
            ivEdit = itemView.findViewById(R.id.ivEditQr);
            ivDelete = itemView.findViewById(R.id.ivDeleteQr);
            llActions = itemView.findViewById(R.id.llActions);
        }
    }

    public void setItems(List<QRItems> items)
    {
        this.items = items;
    }

    public void setOnListItemClickListener(at.wifi.swdev.android.wgapp.onListItemClickListener onListItemClickListener)
    {
        this.onListItemClickListener = onListItemClickListener;
    }

    public void setResources(Resources resources)
    {
        this.resources = resources;
    }
}
