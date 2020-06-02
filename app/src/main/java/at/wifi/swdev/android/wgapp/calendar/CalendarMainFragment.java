package at.wifi.swdev.android.wgapp.calendar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.onListItemClickListener;

import static android.app.Activity.RESULT_OK;

public class CalendarMainFragment extends Fragment implements onListItemClickListener<at.wifi.swdev.android.wgapp.data.Calendar>
{
    public static final int REQUEST_CODE_PERMISSION = 2;
    public static final String CAL_EXTRA = "calExtra";
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

        super.onCreate(savedInstanceState);

        calendarView = root.findViewById(R.id.calendar);
        calendarView.showCurrentMonthPage();
        calendarView.setSwipeEnabled(true);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
        {
            //Permission already granted
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
        }
        else
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, REQUEST_CODE_PERMISSION);
        }
        return root;
    }

    private void request()
    {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
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

    private void onChooser(final Map<String, String> map)
    {
        if (map.size() > 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.dismiss();
                }
            });

            builder.setPositiveButton(R.string.string_add, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    onCalendarChosen(map);
                }
            });

            builder.setTitle("Wähle aus einer Vorlage!");
            builder.setView(R.layout.popup_calendar_chooser);

            dialog = builder.create();
            dialog.show();

            Spinner spinner = dialog.findViewById(R.id.spCalendar);
            List<String> list = new ArrayList<>(map.values());
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
        }
        else
        {
            Toast.makeText(getContext(), "Kein Kalender vorhanden!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCalendarChosen(Map<String, String> map)
    {
        Spinner spinner = dialog.findViewById(R.id.spCalendar);
        int position = spinner.getSelectedItemPosition();

        String id = (String) map.keySet().toArray()[position];
        myID = Integer.parseInt(id);
        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getUid()).child("calId").setValue(id);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

        if (requestCode == REQUEST_CODE_PERMISSION)
        {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                //Permission granted
                request();
            }
            else
            {
                Toast.makeText(getContext(), R.string.no_permission_cal, Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onNewCalendarEntry(View view)
    {
        Intent intent = new Intent(getContext(), CalendarAddActivity.class);
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
}


//            if(myID == -1)
//            {
//                ContentResolver cr = getContentResolver();
//                Uri uri = CalendarContract.Calendars.CONTENT_URI;
//
//                Cursor cursor = cr.query(uri, EVENT_PROJECTION, null, null, null);
//
//                Map<String, String> map = new HashMap<>();
//
//                try
//                {
//                    while (cursor.moveToNext())
//                    {
//                        map.put(cursor.getString(0), cursor.getString(2));
//                    }
//                    onChooser(map);
//                }
//                catch (Exception e)
//                {
//                    Log.d(TAG, "request: " + e.getMessage());
//                }
//            }