package com.example.qrcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;

public class BarCodeScannerActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    TextView textViewValue;
    ExtendedFloatingActionButton button;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    String intentData = "";
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code_scanner);
        connect();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(intentData.length() > 0)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                }
            }
        });
    }

    private void initialisedDetectorAndSources()
    {
        Toast.makeText(this, "Bar Code Scanner Started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS).build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try
                {
                    if(ActivityCompat.checkSelfPermission(BarCodeScannerActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    {
                        cameraSource.start(surfaceView.getHolder());
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(BarCodeScannerActivity.this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(BarCodeScannerActivity.this, "To prevent leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodeSparseArray =detections.getDetectedItems();
                if(barcodeSparseArray.size() != 0)
                {
                    textViewValue.post(new Runnable() {
                        @Override
                        public void run() {
                            button.setText("Launch Url");
                            intentData = barcodeSparseArray.valueAt(0).displayValue;
                            textViewValue.setText(intentData);
                        }
                    });
                }
                else
                {
                    Toast.makeText(BarCodeScannerActivity.this, "It is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialisedDetectorAndSources();
    }

    private void connect()
    {
        surfaceView = findViewById(R.id.surface_view);
        textViewValue = findViewById(R.id.text_view_result);
        button = findViewById(R.id.button_scsnner);
    }
}