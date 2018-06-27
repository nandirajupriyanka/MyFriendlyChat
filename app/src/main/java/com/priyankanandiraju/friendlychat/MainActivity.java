package com.priyankanandiraju.friendlychat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.priyankanandiraju.friendlychat.auth.AuthUIHelper;
import com.priyankanandiraju.friendlychat.utils.BitmapUtils;

import java.io.File;
import java.io.IOException;

import static com.priyankanandiraju.friendlychat.utils.BitmapUtils.createBitmap;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1000;
    private static final int REQUEST_IMAGE_CAPTURE = 2000;
    private static final int REQUEST_VIDEO_CAPTURE = 2001;
    private static final int VIDEO_RECORD_DURATION_LIMIT = 15000; //15sec
    public static final String CHAT_PHOTOS = "chat_photos";
    private ImageView imageView;
    private VideoView videoView;
    // Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // Storage
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotoStorageReference;
    private File mTempPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPhotoStorageReference = mFirebaseStorage.getReference().child(CHAT_PHOTOS);

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
        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoFile = photoFile;
                Log.v(TAG, "mTempPhotoPath " + mTempPhotoFile.getAbsolutePath());
                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Log.w(TAG, "Temporary photo file is null");
            }
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
                setAndStoreCapturedImage();
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

    private void setAndStoreCapturedImage() {
        Bitmap bitmap = createBitmap(mTempPhotoFile.getAbsolutePath());
        imageView.setImageBitmap(bitmap);

        Uri uri = Uri.fromFile(mTempPhotoFile);
        // Get a reference to store file at CHAT_PHOTOS/<FILENAME>
        StorageReference photoRef = mPhotoStorageReference.child(uri.getLastPathSegment());
        // Upload a file to Firebase Storage
        photoRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.v(TAG, "Successfully uploaded image to Firebase Storage");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to upload image to Firebase Storage");
                    }
                });
    }

    private void setRecordedVideo(Uri videoUri) {
        videoView.setVideoURI(videoUri);
        videoView.start();
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
