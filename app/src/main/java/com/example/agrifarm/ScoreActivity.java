package com.example.agrifarm;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        TextView tvRiskScore = findViewById(R.id.tvRiskScore);
        TextView tvRiskBand = findViewById(R.id.tvRiskBand);
        TextView tvPlantScore = findViewById(R.id.tvPlantScore);
        TextView tvSoilScore = findViewById(R.id.tvSoilScore);
        TextView tvWaterScore = findViewById(R.id.tvWaterScore);
        TextView tvGeoScore = findViewById(R.id.tvGeoScore);
        TextView tvDisclaimer = findViewById(R.id.tvDisclaimer);
        MaterialButton btnDone = findViewById(R.id.btnDone);

        // Get data from intent
        double riskScore = getIntent().getDoubleExtra("risk_score", 0.0);
        String riskBand = getIntent().getStringExtra("risk_band");
        double plant = getIntent().getDoubleExtra("plant", 0.0);
        double soil = getIntent().getDoubleExtra("soil", 0.0);
        double water = getIntent().getDoubleExtra("water", 0.0);
        double geo = getIntent().getDoubleExtra("geo", 0.0);
        String disclaimer = getIntent().getStringExtra("disclaimer");

        // Set data to views
        tvRiskScore.setText(String.format(Locale.getDefault(), "%.1f", riskScore));
        tvRiskBand.setText(riskBand);
        tvPlantScore.setText(String.format(Locale.getDefault(), "%.3f", plant));
        tvSoilScore.setText(String.format(Locale.getDefault(), "%.3f", soil));
        tvWaterScore.setText(String.format(Locale.getDefault(), "%.3f", water));
        tvGeoScore.setText(String.format(Locale.getDefault(), "%.3f", geo));
        tvDisclaimer.setText(disclaimer);

        btnDone.setOnClickListener(v -> finish());
    }
}
