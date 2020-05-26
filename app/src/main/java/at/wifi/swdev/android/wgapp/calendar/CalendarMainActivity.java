package at.wifi.swdev.android.wgapp.calendar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.databinding.ActivityCalendarMainBinding;

public class CalendarMainActivity extends AppCompatActivity
{
    public static final int REQUEST_CODE_PERMISSION = 2;
    public static final String CAL_EXTRA = "calExtra";
    private ActivityCalendarMainBinding binding;
    private static final String TAG = CalendarMainActivity.class.getSimpleName();
    private AlertDialog dialog;
    private int myID = -1;
    private Calendar currDate = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.calendar.showCurrentMonthPage();
        binding.calendar.setSwipeEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
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
                    request();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, REQUEST_CODE_PERMISSION);
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
    private void request()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
        {
            String textHelper = getResources().getString(R.string.week_number) + String.valueOf(currDate.get(Calendar.WEEK_OF_YEAR));
            SpannableString underlinedString = new SpannableString(textHelper);
            underlinedString.setSpan(new UnderlineSpan(), 0, underlinedString.length(), 0);
            binding.tvWeekNumber.setText(underlinedString);
            Query query = FirebaseDatabase.getInstance().getReference("cal").child(currDate.get(Calendar.YEAR) + "/" + currDate.get(Calendar.WEEK_OF_YEAR)).orderByChild("dateStart");

            FirebaseRecyclerOptions<at.wifi.swdev.android.wgapp.data.Calendar> options = new FirebaseRecyclerOptions.Builder<at.wifi.swdev.android.wgapp.data.Calendar>().setLifecycleOwner(this).setQuery(query, at.wifi.swdev.android.wgapp.data.Calendar.class).build();

            final CalendarMainAdapter adapter = new CalendarMainAdapter(options);

            binding.rvCal.setLayoutManager(new LinearLayoutManager(this));
            binding.rvCal.setAdapter(adapter);
            binding.calendar.setOnDayClickListener(new OnDayClickListener()
            {
                @Override
                public void onDayClick(EventDay eventDay)
                {
                    Calendar cal = eventDay.getCalendar();

                    Query query = FirebaseDatabase.getInstance().getReference("cal").child(cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.WEEK_OF_YEAR)).orderByChild("dateStart");
                    FirebaseRecyclerOptions<at.wifi.swdev.android.wgapp.data.Calendar> options = new FirebaseRecyclerOptions.Builder<at.wifi.swdev.android.wgapp.data.Calendar>().setLifecycleOwner(CalendarMainActivity.this).setQuery(query, at.wifi.swdev.android.wgapp.data.Calendar.class).build();
                    adapter.updateOptions(options);
                    adapter.notifyDataSetChanged();
                    String textHelper = getResources().getString(R.string.week_number) + String.valueOf(cal.get(Calendar.WEEK_OF_YEAR));
                    SpannableString underlinedString = new SpannableString(textHelper);
                    underlinedString.setSpan(new UnderlineSpan(), 0, underlinedString.length(), 0);
                    binding.tvWeekNumber.setText(underlinedString);
                }
            });

            //setEvents();
        }
    }

    private void setEvents()
    {
        final List<EventDay> events = new ArrayList<>();
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
                    FirebaseDatabase.getInstance().getReference("cal").child(cal.get(Calendar.YEAR) + "/" + cal.get(i)).orderByChild("dateStart").addListenerForSingleValueEvent(new ValueEventListener()
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
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
                binding.calendar.setEvents(events);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void onChooser(final Map<String, String> map)
    {
        if (map.size() > 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(CalendarMainActivity.this, android.R.layout.simple_spinner_item, list);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
        }
        else
        {
            Toast.makeText(this, "Kein Kalender vorhanden!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCalendarChosen(Map<String, String> map)
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
                Toast.makeText(this, "Ohne Berechtigung kann das Programm möglicherweise nicht richtig funktionieren.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onNewCalendarEntry(View view)
    {
        Intent intent = new Intent(this, CalendarAddActivity.class);
        startActivityForResult(intent, CalendarAddActivity.REQUEST_CODE_CAL_ADD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == RESULT_OK && data != null)
        {
            if (requestCode == CalendarAddActivity.REQUEST_CODE_CAL_ADD)
            {
                at.wifi.swdev.android.wgapp.data.Calendar cal = (at.wifi.swdev.android.wgapp.data.Calendar) data.getSerializableExtra(CalendarAddActivity.EXTRA_RESULT);
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.setTimeInMillis(cal.getDateStart());
                List<EventDay> eventDays = new ArrayList<>();
                eventDays.add(new EventDay(calendar, R.drawable.ic_event_black_24dp));
                binding.calendar.setEvents(eventDays);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}