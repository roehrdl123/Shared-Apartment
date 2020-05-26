package at.wifi.swdev.android.wgapp.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Calendar;

public class CalendarMainAdapter extends FirebaseRecyclerAdapter<Calendar, CalendarMainAdapter.CalendarViewHolder>
{
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy - HH:mm");

    public CalendarMainAdapter(@NonNull FirebaseRecyclerOptions<Calendar> options)
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
    protected void onBindViewHolder(@NonNull CalendarViewHolder holder, int position, @NonNull Calendar entry)
    {

        holder.startDateTV.setText(format.format(entry.getDateStart()));
        holder.endDateTV.setText(format.format(entry.getDateEnd()));
        holder.titleTV.setText(entry.getTitle());
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder
    {
        public TextView endDateTV;
        public TextView startDateTV;
        public TextView titleTV;

        public CalendarViewHolder(@NonNull View itemView)
        {
            super(itemView);
            endDateTV = itemView.findViewById(R.id.tvEndDate);
            startDateTV = itemView.findViewById(R.id.tvStartDate);
            titleTV = itemView.findViewById(R.id.tvTitelCal);
        }
    }
}
