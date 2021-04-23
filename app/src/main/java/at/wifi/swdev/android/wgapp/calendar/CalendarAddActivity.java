package at.wifi.swdev.android.wgapp.calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.databinding.ActivityCalendarAddBinding;


public class CalendarAddActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    public static final int REQUEST_CODE_CAL_ADD = 123;
    public static final String EXTRA_RESULT = "extraResult";
    private ActivityCalendarAddBinding binding;
    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private boolean timeClickStart; //IF true - start, if false - end
    private boolean dateClickStart; //IF true - start, if false - end
    final Calendar c = Calendar.getInstance();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Calendar cal = (Calendar) getIntent().getSerializableExtra(CalendarMainFragment.DAY_EXTRA);

        datePickerDialog = new DatePickerDialog(this, this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        timePickerDialog = new TimePickerDialog(this, this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);

        getSupportActionBar().setTitle(R.string.calendar);

        endCal.clear();
        startCal.clear();

        setListeners();
    }

    public void onAdd(View view)
    {
        if (!binding.etTitleCal.getText().toString().matches("")  && allDates())
        {
            if (endCal.getTimeInMillis() >= startCal.getTimeInMillis())
            {
                String title = binding.etTitleCal.getText().toString();
                String content = binding.etContent.getText().toString();

                long start = startCal.getTimeInMillis();
                long end = endCal.getTimeInMillis();

                at.wifi.swdev.android.wgapp.data.Calendar cal = new at.wifi.swdev.android.wgapp.data.Calendar();
                cal.setTitle(title);
                cal.setContent(content);
                cal.setDateStart(start);
                cal.setDateEnd(end);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("cal").child(FirebaseAuth.getInstance().getUid()).child(String.valueOf(startCal.get(Calendar.YEAR))).child(String.valueOf(startCal.get(Calendar.WEEK_OF_YEAR)));

                String key = ref.push().getKey();

                cal.setId(key);

                ref.child(key).setValue(cal);

                setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT, cal));
                finish();
            }
            else
            {
                Toast.makeText(this, "Das End-Datum muss nach dem Anfangsdatum sein!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean allDates()
    {
        if (!startCal.isSet(Calendar.MONTH))
            return false;
        if (!startCal.isSet(Calendar.HOUR_OF_DAY))
            return false;
        if (!endCal.isSet(Calendar.MONTH))
            return false;
        return endCal.isSet(Calendar.HOUR_OF_DAY);
    }

    private void setListeners()
    {
        binding.etStartTime.setOnClickListener(view -> {
            timeClickStart = true;
            timePickerDialog.show();
        });
        binding.etStartDate.setOnClickListener(view -> {
            dateClickStart = true;
            datePickerDialog.show();
        });
        binding.etEndTime.setOnClickListener(view -> {
            timeClickStart = false;
            timePickerDialog.show();
        });
        binding.etEndDate.setOnClickListener(view -> {
            dateClickStart = false;
            datePickerDialog.show();
        });
    }


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2)
    {
        if (dateClickStart)
        {
            startCal.set(Calendar.YEAR, i);
            startCal.set(Calendar.MONTH, i1);
            startCal.set(Calendar.DAY_OF_MONTH, i2);
            binding.etStartDate.setText(formatDate.format(startCal.getTime()));
        }
        else
        {
            endCal.set(Calendar.YEAR, i);
            endCal.set(Calendar.MONTH, i1);
            endCal.set(Calendar.DAY_OF_MONTH, i2);
            binding.etEndDate.setText(formatDate.format(endCal.getTime()));
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1)
    {
        if (timeClickStart)
        {
            startCal.set(Calendar.HOUR_OF_DAY, i);
            startCal.set(Calendar.MINUTE, i1);
            binding.etStartTime.setText(formatTime.format(startCal.getTime()));
        }
        else
        {
            endCal.set(Calendar.HOUR_OF_DAY, i);
            endCal.set(Calendar.MINUTE, i1);
            binding.etEndTime.setText(formatTime.format(endCal.getTime()));
        }
    }

    public void onClose(View view)
    {
        finish();
    }
}
