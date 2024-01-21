package com.shubham.webrtc_videochat.auth;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.shubham.webrtc_videochat.databinding.ActivityPhoneAuthenticationBinding;
import com.shubham.webrtc_videochat.ui.HomeActivity;
import com.shubham.webrtc_videochat.utils.helperFunctions;

import java.util.concurrent.TimeUnit;

public class PhoneAuthentication extends AppCompatActivity {
     String verificationId;
     helperFunctions func = new helperFunctions();
    ActivityPhoneAuthenticationBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

   }

    private  void init()
    {

        binding.getotp.setOnClickListener(view -> {
            // if connected to internet
            if(func.isConnectedToInternet(this))
            {
                String  phonenumber = binding.inputMobileno.getText().toString();
                if (phonenumber == null || phonenumber.length() != 10) {
                    Toast.makeText(PhoneAuthentication.this, "Please enter correct number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendVerificationCode(phonenumber);
                    Toast.makeText(PhoneAuthentication.this, "Sending OTP", Toast.LENGTH_SHORT).show();


                }
            }
            else
            {
                Toast.makeText(this, "Please connect to internet before login.", Toast.LENGTH_SHORT).show();
            }


        });

        binding.submitOTP.setOnClickListener(view -> {
            String otp = binding.inputOTP.getText().toString();
            if (otp == null ) {
                Toast.makeText(PhoneAuthentication.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            } else{
                verifyCode(otp);
            }
        });

    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

            String code = credential.getSmsCode();
            if(code!=null)
            {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            Toast.makeText(PhoneAuthentication.this, "Verification Failed : "+e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "onVerificationFailed: error in verification : "+ e+" message: "+ e.getMessage());
        }

        @Override
        public void onCodeSent(@NonNull String vId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(vId, token);
            verificationId = vId;

            // OTP has been sent successfully
            Toast.makeText(PhoneAuthentication.this, "OTP Sent Successfully", Toast.LENGTH_SHORT).show();
            showOtpUtils();

        }
    };



    public void sendVerificationCode(String number)
    {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+91"+number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Toast.makeText(PhoneAuthentication.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();

                            // Update UI
                            startActivity(new Intent(PhoneAuthentication.this, HomeActivity.class));
                        } else {
                            // Sign in failed, display a message and update the UI
                            Toast.makeText(PhoneAuthentication.this, "Sign in Failed ! please try again.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(PhoneAuthentication.this, "Wrong OTP entered! please enter the correct OTP.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void verifyCode (String code)
    {
        // user entered code and orginal sent code verification
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentuser != null)
        {
            startActivity(new Intent(PhoneAuthentication.this, HomeActivity.class));
            finish();
        }
    }

    public void showOtpUtils()
    {
        binding.inputOTP.setVisibility(View.VISIBLE);
        binding.Otptext.setVisibility(View.VISIBLE);
        binding.submitOTP.setVisibility(View.VISIBLE);
    }
}