package com.example.fitguide;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SensorsData extends AppCompatActivity {

    private BarChart barChart;
    private LineChart lineChart;
    private LineChart lineChartHumidity;
    private DatabaseReference databaseReference;
    private LineDataSet dataSet;
    private LineData lineData;
    private List<Entry> entries = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth authProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        authProfile = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensors_data);


        lineChart = findViewById(R.id.lineChart);
        lineChartHumidity = findViewById(R.id.lineChartHumidity);

        /*
        ArrayList<Entry> visitors = new ArrayList<>();
        visitors.add(new BarEntry(2014, 420));
        visitors.add(new BarEntry(2015, 475));
        visitors.add(new BarEntry(2016, 508));
        visitors.add(new BarEntry(2017, 660));
        visitors.add(new BarEntry(2018, 550));
        visitors.add(new BarEntry(2019, 630));
        visitors.add(new BarEntry(2020, 470));

        LineDataSet  lineDataSet = new LineDataSet(visitors, "visitors");
        //lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(16f);

        LineData lineData = new LineData(lineDataSet);

        //lineChart.setFitBars(true);
        lineChart.setData(lineData);
        lineChart.getDescription().setText("Bar Chart Example");
        lineChart.animateY(2000);*/

        db = FirebaseFirestore.getInstance();

        fetchDataFromFirestore();

        //databaseReference = FirebaseDatabase.getInstance().getReference("data/t");

        /*setupChart();
        listenForNewTemperatures();*/
    }




    private void fetchDataFromFirestore() {
        db.collection("temperature_data") // Replace with your collection name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        entries.clear();
                        float i = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                float temperature = document.getDouble("temperature").floatValue();
                                //Timestamp timestamp = document.getTimestamp("time");
                                //long timeMillis = timestamp.toDate().getTime(); // Convert to milliseconds
                                i++;
                                entries.add(new Entry(i, temperature)); // x=time, y=temperature
                            } catch (Exception e) {
                                Log.e("Firestore", "Error parsing document: " + document.getId(), e);
                            }
                        }
                        if (entries.size() > 0) {
                            Log.d("Firestore", entries.toString());

                            updateChart();
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    private void updateChart() {
        LineDataSet lineDataSet = new LineDataSet(entries, "Temperature Over Time");
        lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(16f);

        LineData lineData = new LineData(lineDataSet);

        lineChart.setData(lineData);
        lineChartHumidity.setData(lineData);

       /* lineChart.getDescription().setText("Temperature vs Time");
        lineChart.animateY(2000);
        lineChart.invalidate();*/
    }




   /* private void setupChart() {
        dataSet = new LineDataSet(entries, "Temperature (°C)");
        dataSet.setColor(getResources().getColor(R.color.purple_200));
        dataSet.setValueTextColor(getResources().getColor(android.R.color.black));

        lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
    private void listenForNewTemperatures() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                try {
                    float temperature = dataSnapshot.getValue(Float.class);
                    long timestamp = System.currentTimeMillis(); // Get Android device timestamp

                    entries.add(new Entry(timestamp, temperature)); // Use device timestamp
                    dataSet.notifyDataSetChanged();
                    lineData.notifyDataChanged();
                    lineChart.notifyDataSetChanged();
                    lineChart.invalidate();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity((getIntent()));
            finish();
            overridePendingTransition(0,0);
        }else if(id == R.id.menu_update_profile){
            Intent intent = new Intent(SensorsData.this, UpdateProfileActivity.class);
            startActivity(intent);
        }/*else if(id == R.id.menu_update_email){
            Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_change_password){
            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        }else if(id == R.id.menu_delete_profile){
            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }*/else if(id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(SensorsData.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SensorsData.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //close UserProfileActivity
        }else{
            Toast.makeText(SensorsData.this, "Something went wrong", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }
}