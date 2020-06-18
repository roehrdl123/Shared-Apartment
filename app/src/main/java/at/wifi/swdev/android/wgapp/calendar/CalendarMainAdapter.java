package at.wifi.swdev.android.wgapp.calendar;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.SimpleDateFormat;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Calendar;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

public class CalendarMainAdapter extends FirebaseRecyclerAdapter<Calendar, CalendarMainAdapter.CalendarViewHolder>
{
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
    private onListItemClickListener<Calendar> clickListener;

    CalendarMainAdapter(FirebaseRecyclerOptions<Calendar> options)
    {
        super(options);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new CalendarViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_calendar_list_item, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull CalendarViewHolder holder, int position, @NonNull final Calendar entry)
    {

        holder.startDateTV.setText(format.format(entry.getDateStart()));
        holder.endDateTV.setText(format.format(entry.getDateEnd()));
        holder.titleTV.setText(entry.getTitle());
        holder.itemView.setOnClickListener(v -> clickListener.onListItemClick(entry, 0));

        final java.util.Calendar entryDate = java.util.Calendar.getInstance();
        entryDate.setTimeInMillis(entry.getDateStart());

        holder.deleteIV.setOnClickListener(v -> clickListener.onListItemClick(entry, 1));
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder
    {
        private TextView endDateTV;
        private TextView startDateTV;
        private TextView titleTV;
        private ImageView deleteIV;

        CalendarViewHolder(@NonNull View itemView)
        {
            super(itemView);
            endDateTV = itemView.findViewById(R.id.tvEndDate);
            startDateTV = itemView.findViewById(R.id.tvStartDate);
            titleTV = itemView.findViewById(R.id.tvTitelCal);
            deleteIV = itemView.findViewById(R.id.ivDeleteEvent);
        }
    }

    void setClickListener(onListItemClickListener<Calendar> clickListener)
    {
        this.clickListener = clickListener;
    }


}
