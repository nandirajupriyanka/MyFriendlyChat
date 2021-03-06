package com.priyankanandiraju.friendlychat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.priyankanandiraju.friendlychat.auth.AuthUIHelper;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1000;
    // Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // user is signed in
                    Toast.makeText(LoginActivity.this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                } else {
                    startActivityForResult(AuthUIHelper.getSignInIntent(), RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(LoginActivity.this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(LoginActivity.this, R.string.sign_in_cancelled, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
