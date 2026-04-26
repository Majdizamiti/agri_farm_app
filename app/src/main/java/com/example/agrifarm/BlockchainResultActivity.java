package com.example.agrifarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

public class BlockchainResultActivity extends AppCompatActivity {

    private static final String BLOCKCHAIN_API_URL = "http://10.0.2.2:3001/api/blockchain/process-ai-result";
    private TextView tvStatus, tvBatchId, tvTxHash, tvQualityScore;
    private JSONObject analysisResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockchain_result);

        tvStatus = findViewById(R.id.tvStatus);
        tvBatchId = findViewById(R.id.tvBatchId);
        tvTxHash = findViewById(R.id.tvTxHash);
        tvQualityScore = findViewById(R.id.tvQualityScore);
        MaterialButton btnViewScore = findViewById(R.id.btnViewScore);
        MaterialButton btnHome = findViewById(R.id.btnHome);

        String jsonString = getIntent().getStringExtra("analysis_json");
        try {
            analysisResult = new JSONObject(jsonString);
            sendToBlockchain(analysisResult);
        } catch (JSONException e) {
            e.printStackTrace();
            tvStatus.setText("Error: Invalid Data");
        }

        btnViewScore.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScoreActivity.class);
            // Pass the same data to ScoreActivity
            try {
                intent.putExtra("risk_score", analysisResult.getDouble("risk_score"));
                intent.putExtra("risk_band", analysisResult.getString("risk_band"));
                JSONObject subScores = analysisResult.getJSONObject("sub_scores");
                intent.putExtra("plant", subScores.getDouble("plant"));
                intent.putExtra("soil", subScores.getDouble("soil"));
                intent.putExtra("water", subScores.getDouble("water"));
                intent.putExtra("geo", subScores.getDouble("geo"));
                intent.putExtra("disclaimer", analysisResult.getString("disclaimer"));
                startActivity(intent);
            } catch (JSONException e) { e.printStackTrace(); }
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void sendToBlockchain(JSONObject data) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BLOCKCHAIN_API_URL, data,
                response -> {
                    try {
                        tvStatus.setText("Successfully Registered on Blockchain!");
                        tvBatchId.setText(response.getString("batchId"));
                        tvTxHash.setText(response.getString("transactionHash"));
                        
                        JSONObject aiProcessing = response.getJSONObject("aiProcessing");
                        tvQualityScore.setText(aiProcessing.getInt("convertedQualityScore") + "/100");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        tvStatus.setText("Blockchain Error: Success but failed to parse response");
                    }
                },
                error -> {
                    String errorMsg = "Unreachable";
                    if (error.networkResponse != null) {
                        errorMsg = "Error Code: " + error.networkResponse.statusCode;
                    }
                    Log.e("Blockchain", "Error connecting to " + BLOCKCHAIN_API_URL + ": " + error.getMessage());
                    tvStatus.setText("Blockchain Error: " + errorMsg);
                    Toast.makeText(this, "Connect to: " + BLOCKCHAIN_API_URL + " failed. Check if server is running.", Toast.LENGTH_LONG).show();
                });
        queue.add(request);
    }
}