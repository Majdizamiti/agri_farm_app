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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private static final String API_URL = "https://smog-blighted-jailer.ngrok-free.dev/score-json?fbclid=IwY2xjawRaY-xleHRuA2FlbQIxMABicmlkETFER09sbTMxQWM4YmR4WFE3c3J0YwZhcHBfaWQQMjIyMDM5MTc4ODIwMDg5MgABHtecwENYeTBqK7W_B6Vb4161oRIM3wSSBn0nyIuBl_XR9t_RzAHWjg7dHPwQ_aem_zb8E2FfddQ6eU_L0GYUWwg";
    
    private String currentScanType = "";
    private String base64Water = "";
    private String base64Plant = "";
    private String base64Soil = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    
    private MaterialButton btnSubmit;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mDatabase = FirebaseDatabase.getInstance("https://agrifarm-b8894-default-rtdb.firebaseio.com/").getReference();

        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        MaterialButton btnScanWater = findViewById(R.id.btnScanWater);
        MaterialButton btnScanPlant = findViewById(R.id.btnScanPlant);
        MaterialButton btnScanSoil = findViewById(R.id.btnScanSoil);
        MaterialButton btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnScanWater.setOnClickListener(v -> { currentScanType = "Water"; checkPermissionAndOpneCamera(); });
        btnScanPlant.setOnClickListener(v -> { currentScanType = "Plant"; checkPermissionAndOpneCamera(); });
        btnScanSoil.setOnClickListener(v -> { currentScanType = "Soil"; checkPermissionAndOpneCamera(); });

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            if (image != null) {
                String base64Image = encodeImage(image);
                if (currentScanType.equals("Water")) base64Water = base64Image;
                else if (currentScanType.equals("Plant")) base64Plant = base64Image;
                else if (currentScanType.equals("Soil")) base64Soil = base64Image;
                
                Toast.makeText(this, currentScanType + " Scan Saved", Toast.LENGTH_SHORT).show();
                if (!base64Water.isEmpty() && !base64Plant.isEmpty() && !base64Soil.isEmpty()) {
                    btnSubmit.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
    }

    private void sendAnalysisRequest() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("plant_image_b64", base64Plant);
            jsonBody.put("soil_image_b64", base64Soil);
            jsonBody.put("water_image_b64", base64Water);
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);
        } catch (JSONException e) { e.printStackTrace(); }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, jsonBody,
                response -> {
                    saveToFirebaseAndNavigate(response);
                },
                error -> Toast.makeText(ScanActivity.this, "API Error", Toast.LENGTH_SHORT).show());

        queue.add(jsonObjectRequest);
    }

    private void saveToFirebaseAndNavigate(JSONObject response) {
        try {
            String id = mDatabase.child("analyses").push().getKey();
            if (id == null) return;

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("risk_score", response.getDouble("risk_score"));
            dataMap.put("risk_band", response.getString("risk_band"));
            dataMap.put("disclaimer", response.getString("disclaimer"));
            dataMap.put("latitude", latitude);
            dataMap.put("longitude", longitude);
            dataMap.put("timestamp", System.currentTimeMillis());

            JSONObject subScores = response.getJSONObject("sub_scores");
            Map<String, Double> subScoresMap = new HashMap<>();
            subScoresMap.put("plant", subScores.getDouble("plant"));
            subScoresMap.put("soil", subScores.getDouble("soil"));
            subScoresMap.put("water", subScores.getDouble("water"));
            subScoresMap.put("geo", subScores.getDouble("geo"));
            dataMap.put("sub_scores", subScoresMap);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                dataMap.put("userId", currentUser.getUid());
            }

            // Save to Firebase
            mDatabase.child("analyses").child(id).setValue(dataMap)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data saved successfully"))
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to save data: " + e.getMessage());
                    Toast.makeText(ScanActivity.this, "Firebase Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            // Navigate to BlockchainResultActivity
            Intent intent = new Intent(ScanActivity.this, BlockchainResultActivity.class);
            intent.putExtra("analysis_json", response.toString());
            startActivity(intent);
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Data Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
