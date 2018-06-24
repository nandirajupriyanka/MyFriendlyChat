package com.priyankanandiraju.friendlychat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.priyankanandiraju.friendlychat.auth.AuthUIHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1000;
    private static final int REQUEST_IMAGE_CAPTURE = 2000;
    private static final int REQUEST_VIDEO_CAPTURE = 2001;
    private static final int VIDEO_RECORD_DURATION_LIMIT = 15000; //15sec
    private ImageView imageView;
    private VideoView videoView;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        Button captureImage = findViewById(R.id.capture_image);
        captureImage.setOnClickListener(this);

        Button recordVideo = findViewById(R.id.record_video);
        recordVideo.setOnClickListener(this);

        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // user is signed in
                    Toast.makeText(MainActivity.this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(AuthUIHelper.getSignInIntent(), RC_SIGN_IN);
                }
            }
        };
    }

    private void dispatchRecordVideoIntent() {
        Log.v(TAG, "dispatchRecordVideoIntent()");
        Intent recordVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // limit the video duration
        recordVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_RECORD_DURATION_LIMIT);
        if (recordVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(recordVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void dispatchCaptureImageIntent() {
        Log.v(TAG, "dispatchCaptureImageIntent()");
        Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureImageIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(captureImageIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, R.string.signed_in, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, R.string.sign_in_cancelled, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                setCapturedImage(intent.getExtras());
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, R.string.failed_to_capture_image, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                setRecordedVideo(intent.getData());
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, R.string.failed_to_record_video, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setRecordedVideo(Uri videoUri) {
        videoView.setVideoURI(videoUri);
        videoView.start();
    }

    private void setCapturedImage(Bundle extras) {
        Bitmap imageBitmap = null;
        if (extras != null) {
            imageBitmap = (Bitmap) extras.get("data");
        }
        imageView.setImageBitmap(imageBitmap);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUIHelper.performSignOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture_image:
                dispatchCaptureImageIntent();
                break;
            case R.id.record_video:
                dispatchRecordVideoIntent();
                break;
        }
    }
}
