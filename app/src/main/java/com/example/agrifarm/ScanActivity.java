package com.example.agrifarm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    // Option A: For Emulator testing (points to your PC's localhost)
    // private static final String API_URL = "http://10.0.2.2:8000/health";

    // Option B: For Physical Device (Replace with your PC's actual IP)
    // private static final String API_URL = "http://192.168.1.5:8000/health";

    // Option C: Public Test URL (Use this to test if the app's logic works)
    private static final String API_URL = "https://httpbin.org/post";
    private String currentScanType = "";
    
    // Variables to store Base64 strings
    private String base64Water = "";
    private String base64Plant = "";
    private String base64Soil = "";
    
    private double latitude = 0.0;
    private double longitude = 0.0;
    
    private MaterialButton btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Get location from intent
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        MaterialButton btnScanWater = findViewById(R.id.btnScanWater);
        MaterialButton btnScanPlant = findViewById(R.id.btnScanPlant);
        MaterialButton btnScanSoil = findViewById(R.id.btnScanSoil);
        MaterialButton btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnScanWater.setOnClickListener(v -> {
            currentScanType = "Water";
            checkPermissionAndOpneCamera();
        });
        btnScanPlant.setOnClickListener(v -> {
            currentScanType = "Plant";
            checkPermissionAndOpneCamera();
        });
        btnScanSoil.setOnClickListener(v -> {
            currentScanType = "Soil";
            checkPermissionAndOpneCamera();
        });

        btnBack.setOnClickListener(v -> finish());
        
        btnSubmit.setOnClickListener(v -> sendAnalysisRequest());
    }

    private void checkPermissionAndOpneCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Scan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            if (image != null) {
                String base64Image = encodeImage(image);
                
                switch (currentScanType) {
                    case "Water":
                        base64Water = base64Image;
                        break;
                    case "Plant":
                        base64Plant = base64Image;
                        break;
                    case "Soil":
                        base64Soil = base64Image;
                        break;
                }
                
                Toast.makeText(this, currentScanType + " Scan Saved", Toast.LENGTH_SHORT).show();
                checkIfAllScansDone();
            }
        }
    }

    private void checkIfAllScansDone() {
        if (!base64Water.isEmpty() && !base64Plant.isEmpty() && !base64Soil.isEmpty()) {
            btnSubmit.setVisibility(View.VISIBLE);
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private void sendAnalysisRequest() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("plant_image_url", base64Plant);
            jsonBody.put("soil_image_url", base64Soil);
            jsonBody.put("water_image_url", base64Water);
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, jsonBody,
                response -> {
                    Log.d("API_RESPONSE", response.toString());
                    Toast.makeText(ScanActivity.this, "Analysis Sent Successfully!", Toast.LENGTH_LONG).show();
                },
                error -> {
                    Log.e("API_ERROR", error.toString());
                    Toast.makeText(ScanActivity.this, "Failed to send analysis. Check connection.", Toast.LENGTH_LONG).show();
                });

        queue.add(jsonObjectRequest);
    }
}
