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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.QRItems;

public class QrCodeListAdapter extends RecyclerView.Adapter<QrCodeListAdapter.QrListViewHolder>
{
    private Bitmap bitmap;
    private List<QRItems> items;
    private at.wifi.swdev.android.wgapp.onListItemClickListener<QRItems> onListItemClickListener;
    private Resources resources;

    @NotNull
    @Override
    public QrListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new QrListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_qr_list_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final QrListViewHolder holder, int position)
    {
        final QRItems model = items.get(position);

        String string = resources.getString(R.string.id) + model.getQrId();
        holder.tvId.setText(string);
        holder.viewOccupied.setVisibility(model.isOccupied() ? View.VISIBLE : View.INVISIBLE);

        if(model.isOccupied())
        {
            holder.llActions.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(view -> onListItemClickListener.onListItemClick(model, QrCodeListEditActivity.RQ_CODE_EDIT));

            holder.ivDelete.setOnClickListener(view -> FirebaseDatabase.getInstance().getReference("qrcodes").child(FirebaseAuth.getInstance().getUid()).child(model.getKey()).removeValue());

            holder.ivEdit.setOnClickListener(view -> onListItemClickListener.onListItemClick(model, QrCodeListEditActivity.RQ_CODE_EDIT));
        }
        else
        {
            holder.llActions.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
        }

        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference("qrs").child(model.getQrId() + ".png").getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes ->
        {
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.ivQr.setImageBitmap(bitmap);
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    static class QrListViewHolder extends RecyclerView.ViewHolder
    {
        private LinearLayout viewOccupied;
        private TextView tvId;
        private ImageView ivQr;
        private ImageView ivEdit;
        private ImageView ivDelete;
        private LinearLayout llActions;

        QrListViewHolder(@NonNull View itemView)
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

    void setItems(List<QRItems> items)
    {
        this.items = items;
    }

    public void setOnListItemClickListener(at.wifi.swdev.android.wgapp.onListItemClickListener<QRItems> onListItemClickListener) {
        this.onListItemClickListener = onListItemClickListener;
    }

    void setResources(Resources resources)
    {
        this.resources = resources;
    }
}
