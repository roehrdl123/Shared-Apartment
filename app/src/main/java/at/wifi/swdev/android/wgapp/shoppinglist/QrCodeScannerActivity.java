package at.wifi.swdev.android.wgapp.shoppinglist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import at.wifi.swdev.android.wgapp.data.Artikel;
import at.wifi.swdev.android.wgapp.databinding.ActivityQrCodeScannerBinding;

public class QrCodeScannerActivity extends AppCompatActivity
{
    public static final int CAMERA_REQUEST_CODE = 999;
    public static final int REQUEST_CODE_QR_ITEMS = 900;
    public static final String ARTIKEL_EXTRA = "ID";
    private boolean detectionEnabled = true;
    private ActivityQrCodeScannerBinding binding;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityQrCodeScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            //Permission already granted
            startQRCodeDetection();
        } else
        {
            String[] permissions = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, permissions, CAMERA_REQUEST_CODE);
        }
    }

    private void startQRCodeDetection()
    {
        //Vibrator-Service
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        //Detektor für QR-Codes
        BarcodeDetector detector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();

        //Kamera-Quelle
        int width = binding.svCameraView.getWidth() > 0 ? binding.svCameraView.getWidth() : 640;
        int height = binding.svCameraView.getHeight() > 0 ? binding.svCameraView.getHeight() : 640;
        final CameraSource cameraSource = new CameraSource.Builder(this, detector).setRequestedPreviewSize(width, height).setAutoFocusEnabled(true).build();


        //Kamera-Stream im Surface-View anzeigen
        binding.svCameraView.getHolder().addCallback(new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {
                //Kamera-Feed starten & anzeigen
                try
                {
                    cameraSource.start(holder);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
                //egal
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                cameraSource.stop();
            }
        });

        //In dem Kamera-Feed Barcodes erkennen
        detector.setProcessor(new Detector.Processor<Barcode>()
        {
            @Override
            public void release()
            {
                ///egal
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections)
            {
                final SparseArray<Barcode> detectedItems = detections.getDetectedItems();

                if (detectedItems.size() > 0 && detectionEnabled)
                {
                    scanedItem(detectedItems, vibrator);
                }
            }
        });
    }

    private void scanedItem(final SparseArray<Barcode> detectedItems, final Vibrator vibrator)
    {
        detectionEnabled = false;
        binding.tvAgain.post(new Runnable()
        {
            @Override
            public void run()
            {
                binding.tvAgain.setVisibility(View.VISIBLE);
            }
        });
        binding.tvInfo.post(new Runnable()
        {
            @Override
            public void run()
            {
                final Barcode barcode = detectedItems.valueAt(0);
                vibrator.vibrate(200);
                binding.tvInfo.setText(barcode.displayValue);


                FirebaseDatabase.getInstance().getReference("qrcodes").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                long qrId = (long) snapshot.child("qrId").getValue();
                                String qrIdString = ""+qrId;
                                if (qrIdString.equals(barcode.displayValue))
                                {
                                    ArrayList<Artikel> qrArtikel = new ArrayList<>();
                                    for (DataSnapshot items : snapshot.child("items").getChildren())
                                    {
                                        Artikel artikel = items.getValue(Artikel.class);
                                        qrArtikel.add(artikel);
                                    }
                                    startActivityForResult(new Intent(context, QrCodeItemsActivity.class).putExtra(ARTIKEL_EXTRA, qrArtikel), REQUEST_CODE_QR_ITEMS);
                                }
                            }
                            binding.tvInfo.setText("Diesen QR-Code gibt es nicht. Versuchen sie es erneut!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == CAMERA_REQUEST_CODE)
        {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Permission granted
                startQRCodeDetection();
            } else
            {
                //Permission denied
                binding.tvNoPermission.setVisibility(View.VISIBLE);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onScanAgain(View view)
    {
        detectionEnabled = true;
        binding.tvAgain.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == REQUEST_CODE_QR_ITEMS)
        {
            if (resultCode == RESULT_OK)
            {
                if (data != null)
                {
                    setResult(RESULT_OK, data);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
