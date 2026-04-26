package com.example.agrifarm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import androidx.preference.PreferenceManager;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView map = null;
    private Marker userLocationMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvLocationDisplay;
    
    private Polygon farmPolygon;
    private List<GeoPoint> farmPoints = new ArrayList<>();
    private List<Marker> pointMarkers = new ArrayList<>();
    private MaterialButton btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvLocationDisplay = findViewById(R.id.tvLocationDisplay);
        btnScan = findViewById(R.id.btnScan);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(36.8065, 10.1815); 
        mapController.setCenter(startPoint);

        // Initialize user location marker
        userLocationMarker = new Marker(map);
        userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userLocationMarker.setTitle("My Location");

        // Initialize Farm Polygon
        farmPolygon = new Polygon();
        farmPolygon.setFillColor(Color.argb(75, 255, 0, 0)); // Transparent red
        farmPolygon.setStrokeColor(Color.RED);
        farmPolygon.setStrokeWidth(2.0f);
        map.getOverlays().add(farmPolygon);

        // Map Click Listener to add points
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                addFarmPoint(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(mReceive));

        MaterialButton btnLocation = findViewById(R.id.btnLocation);
        MaterialButton btnClear = findViewById(R.id.btnClear);

        btnLocation.setOnClickListener(v -> getLastLocation());

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanActivity.class);
            // Get user current location to pass to ScanActivity
            if (userLocationMarker.getPosition() != null) {
                intent.putExtra("latitude", userLocationMarker.getPosition().getLatitude());
                intent.putExtra("longitude", userLocationMarker.getPosition().getLongitude());
            }
            startActivity(intent);
        });

        btnClear.setOnClickListener(v -> clearFarmZone());
    }

    private void addFarmPoint(GeoPoint point) {
        farmPoints.add(point);
        
        // Add a small marker for each corner
        Marker pMarker = new Marker(map);
        pMarker.setPosition(point);
        pMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        pMarker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.osm_ic_follow_me)); // Small dot
        map.getOverlays().add(pMarker);
        pointMarkers.add(pMarker);

        updatePolygon();
    }

    private void updatePolygon() {
        farmPolygon.setPoints(farmPoints);
        map.invalidate();
        
        if (farmPoints.size() >= 3) {
            double area = calculateArea(farmPoints);
            tvLocationDisplay.setText(String.format(Locale.getDefault(), "Farm Area: %.4f km²", area));
            btnScan.setVisibility(View.VISIBLE);
        } else {
            tvLocationDisplay.setText("Tap map to add points (need 3 for area)");
            btnScan.setVisibility(View.GONE);
        }
    }

    private void clearFarmZone() {
        farmPoints.clear();
        for (Marker m : pointMarkers) {
            map.getOverlays().remove(m);
        }
        pointMarkers.clear();
        farmPolygon.setPoints(new ArrayList<>());
        tvLocationDisplay.setText("Location not set");
        map.invalidate();
    }

    private double calculateArea(List<GeoPoint> points) {
        if (points.size() < 3) return 0;
        
        double area = 0;
        double radius = 6378.137; // Earth's radius in km
        
        for (int i = 0; i < points.size(); i++) {
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get((i + 1) % points.size());
            
            double lat1 = Math.toRadians(p1.getLatitude());
            double lon1 = Math.toRadians(p1.getLongitude());
            double lat2 = Math.toRadians(p2.getLatitude());
            double lon2 = Math.toRadians(p2.getLongitude());
            
            area += (lon2 - lon1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }
        
        area = Math.abs(area * radius * radius / 2.0);
        return area;
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                GeoPoint myPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                if (!map.getOverlays().contains(userLocationMarker)) map.getOverlays().add(userLocationMarker);
                userLocationMarker.setPosition(myPoint);
                map.getController().animateTo(myPoint);
                map.getController().setZoom(18.0);
                
                // Update text display with Lat/Lng
                tvLocationDisplay.setText(String.format(Locale.getDefault(), 
                    "Lat: %.6f, Lng: %.6f", location.getLatitude(), location.getLongitude()));
                
                map.invalidate();
            } else {
                Toast.makeText(this, "Unable to get location. Make sure GPS is on.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() { super.onResume(); map.onResume(); }
    @Override
    public void onPause() { super.onPause(); map.onPause(); }
}
