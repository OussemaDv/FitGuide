package com.example.fitguide;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDoB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDoB, textGender, textMobile;
    private FirebaseAuth authProfile;
    private FirebaseFirestore fStore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().setTitle("Update Profile Details");

        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);
        radioGroupUpdateGender = findViewById(R.id.radio_group_update_profile_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        String userId = getIntent().getStringExtra("userId");

        DocumentReference docRef = fStore.collection("users").document(userId);

        showProfile(userId, docRef);

        Button buttonUploadProfilePic = findViewById(R.id.button_upload_profile_pic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Setting up DatePicker on EditText
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String textSADoB[] = editTextUpdateDoB.getText().toString().split("/");

                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1])-1;
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                //Date Picker Dialog
                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){
                        editTextUpdateDoB.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
    });

    Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        //User finalUser = user;
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Log.d("update", "before");
            int selectedGenderId = radioGroupUpdateGender.getCheckedRadioButtonId();
            radioButtonUpdateGenderSelected = findViewById(selectedGenderId);
            Log.d("update", "after1");

            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();
            Log.d("update", "after2");

            if(TextUtils.isEmpty(textFullName)){
                Toast.makeText(UpdateProfileActivity.this, "Please enter your full name", Toast.LENGTH_LONG).show();
                editTextUpdateName.setError("Full Name is required");
                editTextUpdateName.requestFocus();
            } else if (TextUtils.isEmpty(textDoB)) {
                Toast.makeText(UpdateProfileActivity.this, "Please enter your date of birth", Toast.LENGTH_LONG).show();
                editTextUpdateDoB.setError("Date of birth is required");
                editTextUpdateDoB.requestFocus();
            } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
                Toast.makeText(UpdateProfileActivity.this, "Please select your gender", Toast.LENGTH_LONG).show();
                radioButtonUpdateGenderSelected.setError("Gender is required");
                radioButtonUpdateGenderSelected.requestFocus();
            } else if (TextUtils.isEmpty(textMobile)) {
                Toast.makeText(UpdateProfileActivity.this, "Please enter your mobile no.", Toast.LENGTH_LONG).show();
                editTextUpdateMobile.setError("Mobile No. is required");
                editTextUpdateMobile.requestFocus();
            }else if (textMobile.length()!=8){
                Toast.makeText(UpdateProfileActivity.this, "Please re-enter your mobile number", Toast.LENGTH_LONG).show();
                editTextUpdateMobile.setError("Mobile No. should be 8 digits");
                editTextUpdateMobile.requestFocus();
            } else {
                textGender = radioButtonUpdateGenderSelected.getText().toString();
                textFullName = editTextUpdateName.getText().toString();
                textDoB = editTextUpdateDoB.getText().toString();
                textMobile = editTextUpdateMobile.getText().toString();
                updateProfileData(userId, docRef);
            }


        }
    });


    }

    private void showProfile(String userId, DocumentReference docRef) {
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        editTextUpdateName.setText(document.getString("fName"));
                        editTextUpdateDoB.setText(document.getString("dob"));
                        editTextUpdateMobile.setText(document.getString("mobile"));

                        String gender = document.getString("gender");
                        if (gender != null) {
                            if (gender.equals("Male")) {
                                radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                            } else if (gender.equals("Female")) {
                                radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                            }
                            if (radioButtonUpdateGenderSelected != null) {
                                radioButtonUpdateGenderSelected.setChecked(true);
                            }
                        }
                    } else {
                        Log.d("UpdateProfileActivity", "No such document");
                        Toast.makeText(UpdateProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("UpdateProfileActivity", "get failed with ", task.getException());
                    Toast.makeText(UpdateProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void updateProfileData(String userId, DocumentReference docRef) {
        docRef.update("fName", textFullName, "dob", textDoB, "gender", textGender, "mobile", textMobile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UpdateProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
}});
    }
}