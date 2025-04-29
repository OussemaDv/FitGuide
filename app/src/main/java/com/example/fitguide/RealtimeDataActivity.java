package com.example.fitguide;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class RealtimeDataActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    private TextView dataTextView;
    private DatabaseReference myRef;
    private Button buttonExportPDF;
    private List<Map<String, Object>> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_data); // Replace with your layout

        int permissionStatus = ContextCompat.checkSelfPermission(RealtimeDataActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d("permissionsPdf", "Initial permission status: " + permissionStatus);
        if (ContextCompat.checkSelfPermission(RealtimeDataActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RealtimeDataActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }


        dataTextView = findViewById(R.id.text_temperature); // Replace with your TextView ID
        buttonExportPDF = findViewById(R.id.button_export_pdf);

        // Initialize Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("data/t"); // Replace "your_data_path" with your database path

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("temperature_data")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            data.add(document.getData());
                        }
                    } else {
                        Log.w("Firebase", "Error getting documents.", task.getException());
                    }
                });
        buttonExportPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean readCheck = ContextCompat.checkSelfPermission(RealtimeDataActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=  PackageManager.PERMISSION_GRANTED;
                boolean writeCheck = ContextCompat.checkSelfPermission(RealtimeDataActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=  PackageManager.PERMISSION_GRANTED;
                if (readCheck && writeCheck){
                    Log.d("pdfgeneration", data.toString());
                    generatePdf(data); // Call generatePdf after permission is confirmed.
                } else {
                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                }
            }
        });

        // Add a ValueEventListener to listen for changes
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.exists()) {
                    String value = dataSnapshot.getValue(String.class); // Or other appropriate type
                    if(value != null){
                        dataTextView.setText("Temperature: " + value);
                        float temp = Float.parseFloat(value);
                        if(temp>39){
                            sendNotification("Temperature Exeeded threshold", "Temperature is now: " + value + "°C");

                        }

                    } else{
                        dataTextView.setText("Data is null");
                    }

                } else {
                    dataTextView.setText("Data does not exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                dataTextView.setText("Failed to read data: " + error.getMessage());
            }
        });
    }
    private void generatePdf(List<Map<String, Object>> data) {
        FileOutputStream file = null;
        try {
            Document document = new Document();
            /*String filePath = Environment.getExternalStorageDirectory() + "/TemperatureData.pdf"; // Path to save the PDF
            PdfWriter.getInstance(document, new FileOutputStream(filePath));*/
            File f = new File(Environment.getExternalStoragePublicDirectory("Documents"), "temperature.pdf");
            f.createNewFile();
            file = new FileOutputStream(f);
            PdfWriter.getInstance(document, file);
            document.open();
            Log.d("pdf", "document opened");
            PdfPTable table = new PdfPTable(2); // 2 columns: time and temperature
            table.addCell("Time");
            table.addCell("Temperature");

            for (Map<String, Object> row : data) {
                String time = (String) row.get("time");
                int temperature = ((Long) row.get("temperature")).intValue(); // Assuming temperature is stored as a Long
                table.addCell(time);
                table.addCell(String.valueOf(temperature));
            }

            document.add(table);
            document.close();
            Toast.makeText(this, "PDF generated successfully!", Toast.LENGTH_SHORT).show();
        } catch (DocumentException | FileNotFoundException e) {
            Log.e("PDF", "Error generating PDF", e);
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "IO Exception", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendNotification(String title, String message) {
        NotificationHelper.sendNotification(this, title, message);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        generatePdf(data);
                }
                return;
        }
    }
}