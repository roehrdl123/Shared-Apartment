package at.wifi.swdev.android.wgapp.calendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import at.wifi.swdev.android.wgapp.MainActivity;
import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

import static android.app.Activity.RESULT_OK;

public class CalendarMainFragment extends Fragment implements onListItemClickListener<at.wifi.swdev.android.wgapp.data.Calendar>
{
    public static final String CAL_EXTRA = "calExtra";
    public static final String DAY_EXTRA = "DAY";
    private AlertDialog dialog;
    private int myID = -1;
    private View root;
    private CalendarView calendarView;
    private Calendar currDate = Calendar.getInstance();
    private Calendar helperCal = Calendar.getInstance();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_calendar_main, container, false);

        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);

        calendarView = root.findViewById(R.id.calendar);
        calendarView.showCurrentMonthPage();
        calendarView.setSwipeEnabled(true);

            FirebaseDatabase.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.hasChild("calId"))
                    {
                        myID = dataSnapshot.getValue(Integer.class);
                    }
                    root.findViewById(R.id.btnAddCal).setOnClickListener(v -> onNewCalendarEntry(v));
                    request();
                    currDate = Calendar.getInstance();
                    setEvents();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });

        return root;
    }

    private void request()
    {

            String textHelper = getResources().getString(R.string.week_number) + " " + String.valueOf(currDate.get(Calendar.WEEK_OF_YEAR));
            SpannableString underlinedString = new SpannableString(textHelper);
            underlinedString.setSpan(new UnderlineSpan(), 0, underlinedString.length(), 0);
            ((TextView)root.findViewById(R.id.tvWeekNumber)).setText(underlinedString);

            Query query = FirebaseDatabase.getInstance().getReference("cal").child(currDate.get(Calendar.YEAR) + "/" + currDate.get(Calendar.WEEK_OF_YEAR)).orderByChild("dateStart");

            FirebaseRecyclerOptions<at.wifi.swdev.android.wgapp.data.Calendar> options = new FirebaseRecyclerOptions.Builder<at.wifi.swdev.android.wgapp.data.Calendar>().setLifecycleOwner(this).setQuery(query, at.wifi.swdev.android.wgapp.data.Calendar.class).build();

            final CalendarMainAdapter adapter = new CalendarMainAdapter(options);

            adapter.setClickListener(this);

            RecyclerView recyclerView = root.findViewById(R.id.rvCal);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);


            setListeners(adapter);

    }

    private void setEvents()
    {
        List<EventDay> events = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("cal").child(currDate.get(Calendar.YEAR) + "").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                List<Integer> weeks = new ArrayList<>();
                int month = currDate.get(Calendar.MONTH);
                final Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                while (cal.get(Calendar.MONTH) == month)
                {
                    weeks.add(cal.get(Calendar.WEEK_OF_YEAR));
                    cal.add(Calendar.DAY_OF_MONTH, 7);
                }

                for (Integer i : weeks)
                {
                    FirebaseDatabase.getInstance().getReference("cal").child(cal.get(Calendar.YEAR) + "/" + i).orderByChild("dateStart").addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            for(DataSnapshot data : dataSnapshot.getChildren())
                            {
                                Calendar calendar = Calendar.getInstance();
                                long timestamp = data.child("dateStart").getValue(Long.class);
                                calendar.setTimeInMillis(timestamp);
                                events.add(new EventDay(calendar, R.drawable.ic_event_black_24dp));
                            }
                            if(i.equals(weeks.get(weeks.size() - 1))) {
                                calendarView.setEvents(events);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void setListeners(final CalendarMainAdapter adapter)
    {
        calendarView.setOnDayClickListener(eventDay ->
        {
            Calendar cal = eventDay.getCalendar();

            Query query = FirebaseDatabase.getInstance().getReference("cal").child(cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.WEEK_OF_YEAR)).orderByChild("dateStart");
            FirebaseRecyclerOptions<at.wifi.swdev.android.wgapp.data.Calendar> options = new FirebaseRecyclerOptions.Builder<at.wifi.swdev.android.wgapp.data.Calendar>().setLifecycleOwner(CalendarMainFragment.this).setQuery(query, at.wifi.swdev.android.wgapp.data.Calendar.class).build();
            adapter.updateOptions(options);
            adapter.notifyDataSetChanged();
            String textHelper = getResources().getString(R.string.week_number) + " " +  String.valueOf(cal.get(Calendar.WEEK_OF_YEAR));
            SpannableString underlinedString = new SpannableString(textHelper);
            underlinedString.setSpan(new UnderlineSpan(), 0, underlinedString.length(), 0);
            ((TextView)root.findViewById(R.id.tvWeekNumber)).setText(underlinedString);
        });

        calendarView.setOnForwardPageChangeListener(() -> setNothingRV(adapter));

        calendarView.setOnPreviousPageChangeListener(() -> setNothingRV(adapter));
    }

    private void setNothingRV(CalendarMainAdapter adapter)
    {
        Query query1 = FirebaseDatabase.getInstance().getReference("1");
        FirebaseRecyclerOptions<at.wifi.swdev.android.wgapp.data.Calendar> options = new FirebaseRecyclerOptions.Builder<at.wifi.swdev.android.wgapp.data.Calendar>().setLifecycleOwner(CalendarMainFragment.this).setQuery(query1, at.wifi.swdev.android.wgapp.data.Calendar.class).build();
        adapter.updateOptions(options);
        adapter.notifyDataSetChanged();
        ((TextView)root.findViewById(R.id.tvWeekNumber)).setText("");
        currDate = calendarView.getCurrentPageDate();
        setEvents();
    }

    private void onNewCalendarEntry(View view)
    {
        Intent intent = new Intent(getContext(), CalendarAddActivity.class);
        intent.putExtra(DAY_EXTRA, calendarView.getFirstSelectedDate());
        startActivityForResult(intent, CalendarAddActivity.REQUEST_CODE_CAL_ADD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == RESULT_OK && data != null)
        {
            if (requestCode == CalendarAddActivity.REQUEST_CODE_CAL_ADD)
            {
                at.wifi.swdev.android.wgapp.data.Calendar cal = (at.wifi.swdev.android.wgapp.data.Calendar) data.getSerializableExtra(CalendarAddActivity.EXTRA_RESULT);
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.setTimeInMillis(cal.getDateStart());
                currDate.setTimeInMillis(cal.getDateStart());
                setEvents();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onListItemClick(at.wifi.swdev.android.wgapp.data.Calendar model, int requestCode)
    {
        if( requestCode == 0)
        {
            Intent intent = new Intent(getContext(), CalendarEditActivity.class);
            intent.putExtra(CalendarEditActivity.CAL_EXTRA, model);
            startActivity(intent);
        }
        else if(requestCode == 1)
        {
            helperCal.setTimeInMillis(model.getDateStart());
            FirebaseDatabase.getInstance().getReference("cal").child(String.valueOf(helperCal.get(java.util.Calendar.YEAR))).child(String.valueOf(helperCal.get(java.util.Calendar.WEEK_OF_YEAR))).child(model.getId()).removeValue();
            currDate.setTimeInMillis(model.getDateStart());
            setEvents();
            Toast.makeText(getContext(), R.string.remove_cal, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.signout, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.menu_signout)
        {
            signout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signout()
    {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}