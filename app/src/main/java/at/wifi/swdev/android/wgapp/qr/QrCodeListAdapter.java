package at.wifi.swdev.android.wgapp.qr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.QRItems;

public class QrCodeListAdapter extends FirebaseRecyclerAdapter<QRItems, QrCodeListAdapter.QrListViewHolder>
{
    private Bitmap bitmap;

    public QrCodeListAdapter(@NonNull FirebaseRecyclerOptions<QRItems> options)
    {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final QrListViewHolder holder, int position, @NonNull final QRItems model)
    {
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onClick(view);
            }
        });

        holder.tvId.setText(R.string.id + model.getQrId());

        holder.viewOccupied.setVisibility(model.isOccupied() ? View.VISIBLE : View.INVISIBLE);

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
                onClick(view);
            }
        });

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

    @NonNull
    @Override
    public QrListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new QrListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_qr_list_items, parent, false));
    }

    class QrListViewHolder extends RecyclerView.ViewHolder
    {
        private TextView viewOccupied;
        private TextView tvId;
        private ImageView ivQr;
        private ImageView ivEdit;
        private ImageView ivInfo;
        private ImageView ivDelete;

        public QrListViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvIdQrList);
            viewOccupied = itemView.findViewById(R.id.viewOccupied);
            ivQr = itemView.findViewById(R.id.ivQrCode);
            ivEdit = itemView.findViewById(R.id.ivEditQr);
            ivInfo = itemView.findViewById(R.id.ivInfoQr);
            ivDelete = itemView.findViewById(R.id.ivDeleteQr);
        }
    }

    private void onClick(View view)
    {
//TODO: EDIT / INFO ACTIVITY
    }
}
