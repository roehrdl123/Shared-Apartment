package at.wifi.swdev.android.wgapp.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
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
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        datePickerDialog = new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        timePickerDialog = new TimePickerDialog(this, this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);

        endCal.clear();
        startCal.clear();

        setListeners();
    }

    public void onAdd(View view)
    {
        if ((!(binding.etTitleCal.getText().toString().matches("") || binding.etContent.getText().toString().matches(""))) && allDates())
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

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("cal").child(String.valueOf(startCal.get(Calendar.YEAR))).child(String.valueOf(startCal.get(Calendar.WEEK_OF_YEAR)));
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
        if (!endCal.isSet(Calendar.HOUR_OF_DAY))
            return false;
        return true;
    }

    private void setListeners()
    {
        binding.etStartTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                timeClickStart = true;
                timePickerDialog.show();
            }
        });
        binding.etStartDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dateClickStart = true;
                datePickerDialog.show();
            }
        });
        binding.etEndTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                timeClickStart = false;
                timePickerDialog.show();
            }
        });
        binding.etEndDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dateClickStart = false;
                datePickerDialog.show();
            }
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