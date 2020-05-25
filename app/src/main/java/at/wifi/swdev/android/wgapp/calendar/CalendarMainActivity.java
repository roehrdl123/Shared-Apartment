package at.wifi.swdev.android.wgapp.calendar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import at.wifi.swdev.android.wgapp.R;
import at.wifi.swdev.android.wgapp.databinding.ActivityCalendarMainBinding;

public class CalendarMainActivity extends AppCompatActivity
{
    public static final int REQUEST_CODE_PERMISSION = 2;
    private ActivityCalendarMainBinding binding;
    private static final String TAG = CalendarMainActivity.class.getSimpleName();
    private AlertDialog dialog;
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            //Permission already granted
            request();
        } 
        else
        {
            String[] permissions = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, REQUEST_CODE_PERMISSION);
        }
    }


    private void request()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
        {
            ContentResolver cr = getContentResolver();
            Uri uri = CalendarContract.Calendars.CONTENT_URI;

            Cursor cursor = cr.query(uri, EVENT_PROJECTION, null, null, null);

            Map<String, String> map = new HashMap<>();

            try
            {
                while (cursor.moveToNext())
                {
                    map.put(cursor.getString(0), cursor.getString(2));
                }
                onChooser(map);
            }
            catch (Exception e)
            {
                Log.d(TAG, "request: " + e.getMessage());
            }
        }
    }

    private void onChooser(final Map<String, String> map)
    {
        if(map.size() > 0)
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
}