package com.example.fitguide;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private EditText editTextLoginEmail, editTextLoginPwd;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";
    private Button fingerprint_button;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Open Register Activity
        Button buttonRegister = findViewById(R.id.register_button);
        buttonRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        editTextLoginEmail = findViewById(R.id.editText_login_email);
        editTextLoginPwd = findViewById(R.id.editText_login_pwd);

        authProfile = FirebaseAuth.getInstance();

        //Reset Password
        Button buttonForgotPassword = findViewById(R.id.button_forgot_password);
        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "You can reset your password now!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
            }
        });

        //Login User

        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPwd = editTextLoginPwd.getText().toString();

                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(MainActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Valid email is required");
                    editTextLoginEmail.requestFocus();
                }else if(TextUtils.isEmpty(textPwd)){
                    Toast.makeText(MainActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    editTextLoginPwd.setError("Password is required");
                    editTextLoginPwd.requestFocus();
                }else{
                    loginUser(textEmail, textPwd);
                }
            }
        });

        fingerprint_button = findViewById(R.id.fingerprint_button);
        checkBioMetricSupported();
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, "Auth error" + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Auth succeeded", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "Auth failed", Toast.LENGTH_SHORT).show();
            }
        });
        fingerprint_button.setOnClickListener(view ->{
            BiometricPrompt.PromptInfo.Builder promptInfo = dialogMetric();
            promptInfo.setNegativeButtonText("Cancel");
            biometricPrompt.authenticate(promptInfo.build());
        });

    }
    BiometricPrompt.PromptInfo.Builder dialogMetric(){
        return new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric login").setSubtitle("Login using your biometric credentials");
    }

    private void loginUser(String email, String pwd) {
        authProfile.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(MainActivity.this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    //Get instance of the current User
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    //Check if email is verified before user can access their profile
                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(MainActivity.this, "You are logged in now", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                        finish();
                    }else{
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        showAlertDialog();
                    }

                }else{
                    try {
                        throw task.getException();
                    }catch(FirebaseAuthInvalidUserException e){
                        editTextLoginEmail.setError("User does not exist or is no longer valid. Please register again.");
                        editTextLoginEmail.requestFocus();
                    }catch(FirebaseAuthInvalidCredentialsException e){
                        editTextLoginEmail.setError("Invalid credentials. Kindly, check and re-enter.");
                        editTextLoginEmail.requestFocus();
                    }catch(Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }

    private void showAlertDialog() {
        //Setup the Alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification");

        //Open Email App if User clicks Continer button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //To email app in new window and not within ou app
                startActivity(intent);
            }
        });
        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(authProfile.getCurrentUser()!=null){
            Toast.makeText(MainActivity.this, "Already logged in!",Toast.LENGTH_SHORT).show();
            //Start the UserProfileActivity
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
            finish(); //close MainActivity
        }
        else{
            Toast.makeText(MainActivity.this, "You can log in now",Toast.LENGTH_SHORT).show();

        }
    }
    private void checkBioMetricSupported(){
        String info;
        BiometricManager manager = BiometricManager.from(this);
        switch(manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK
        | BiometricManager.Authenticators.BIOMETRIC_STRONG)){
            case BiometricManager.BIOMETRIC_SUCCESS:
                info = "App can authentificate using biometrics";
                enableButton(true);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                info = "No biometric features available on this device";
                enableButton(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                info = "Biometric features are currently unavailable";
                enableButton(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                info = "Need register at least one fingerprint.";
                enableButton(false);
                break;
            default:
                info = "Unknown case";
                enableButton(false,true);
                break;
        }
        //TextView txinfo = findViewById(R.if.tx)
    }
    void enableButton(boolean enable){
        fingerprint_button.setEnabled(enable);
    }
    void enableButton(boolean enable, boolean enroll){
        fingerprint_button.setEnabled(enable);
        if(!enroll) return;
        Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
         enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BiometricManager.Authenticators.BIOMETRIC_STRONG
        | BiometricManager.Authenticators.BIOMETRIC_WEAK);
         startActivity(enrollIntent);
    }
}


