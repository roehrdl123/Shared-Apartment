package at.wifi.swdev.android.wgapp.calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.data.Calendar;
import at.wifi.swdev.android.wgapp.databinding.ActivityCalendarEditBinding;

public class CalendarEditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    public static final String CAL_EXTRA = "CalExtra";
    private java.util.Calendar startCal = java.util.Calendar.getInstance();
    private java.util.Calendar endCal = java.util.Calendar.getInstance();
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private boolean timeClickStart; //IF true - start, if false - end
    private boolean dateClickStart; //IF true - start, if false - end
    private ActivityCalendarEditBinding binding;
    private Calendar cal;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cal = (Calendar) getIntent().getSerializableExtra(CAL_EXTRA);

        if (cal != null)
        {
            startCal.setTimeInMillis(cal.getDateStart());
            endCal.setTimeInMillis(cal.getDateEnd());

            binding.etStartDate.setText(formatDate.format(new Date(cal.getDateStart())));
            binding.etEndDate.setText(formatDate.format(new Date(cal.getDateEnd())));
            binding.etStartTime.setText(formatTime.format(new Date(cal.getDateStart())));
            binding.etEndTime.setText(formatTime.format(new Date(cal.getDateEnd())));

            binding.etTitleCal.setText(cal.getTitle());
            binding.etContent.setText(cal.getContent());

            datePickerDialog = new DatePickerDialog(this, this, startCal.get(java.util.Calendar.YEAR), startCal.get(java.util.Calendar.MONTH), startCal.get(java.util.Calendar.DAY_OF_MONTH));
            timePickerDialog = new TimePickerDialog(this, this, startCal.get(java.util.Calendar.HOUR_OF_DAY), startCal.get(java.util.Calendar.MINUTE), true);

            setListeners();
        }
        else
        {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void onEdit(View view)
    {
        if ((!(binding.etTitleCal.getText().toString().matches("") || binding.etContent.getText().toString().matches(""))) && allDates())
        {
            if (endCal.getTimeInMillis() >= startCal.getTimeInMillis())
            {
                String title = binding.etTitleCal.getText().toString();
                String content = binding.etContent.getText().toString();

                long start = startCal.getTimeInMillis();
                long end = endCal.getTimeInMillis();

                cal.setTitle(title);
                cal.setContent(content);
                cal.setDateStart(start);
                cal.setDateEnd(end);

                FirebaseDatabase.getInstance().getReference("cal").child(String.valueOf(startCal.get(java.util.Calendar.YEAR))).child(String.valueOf(startCal.get(java.util.Calendar.WEEK_OF_YEAR))).child(cal.getId()).setValue(cal);

                finish();
            } else
            {
                Toast.makeText(this, R.string.end_bigger_start, Toast.LENGTH_SHORT).show();
            }
        } else
        {
            Toast.makeText(this, R.string.fill_all, Toast.LENGTH_SHORT).show();
        }
    }

    public void onClose(View view)
    {
        finish();
    }

    private boolean allDates()
    {
        if (!startCal.isSet(java.util.Calendar.MONTH))
            return false;
        if (!startCal.isSet(java.util.Calendar.HOUR_OF_DAY))
            return false;
        if (!endCal.isSet(java.util.Calendar.MONTH))
            return false;
        return endCal.isSet(java.util.Calendar.HOUR_OF_DAY);
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
            startCal.set(java.util.Calendar.YEAR, i);
            startCal.set(java.util.Calendar.MONTH, i1);
            startCal.set(java.util.Calendar.DAY_OF_MONTH, i2);
            binding.etStartDate.setText(formatDate.format(startCal.getTime()));
        } else
        {
            endCal.set(java.util.Calendar.YEAR, i);
            endCal.set(java.util.Calendar.MONTH, i1);
            endCal.set(java.util.Calendar.DAY_OF_MONTH, i2);
            binding.etEndDate.setText(formatDate.format(endCal.getTime()));
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1)
    {
        if (timeClickStart)
        {
            startCal.set(java.util.Calendar.HOUR_OF_DAY, i);
            startCal.set(java.util.Calendar.MINUTE, i1);
            binding.etStartTime.setText(formatTime.format(startCal.getTime()));
        } else
        {
            endCal.set(java.util.Calendar.HOUR_OF_DAY, i);
            endCal.set(java.util.Calendar.MINUTE, i1);
            binding.etEndTime.setText(formatTime.format(endCal.getTime()));
        }
    }
}
