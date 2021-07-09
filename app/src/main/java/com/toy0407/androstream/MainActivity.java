package com.toy0407.androstream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.OnVideoListener {

    private static final String TAG = "MainActivity";
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Videos");
    ArrayList<VideoClass> downloadarraylist=new ArrayList<>();
    RecyclerView videoListRecyclerView;
    int GET_VIDEO_FROM_MEDIASTORE=1,notificationId=2;
    private static final String CHANNEL_ID = "Upload Task";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        videoListRecyclerView=findViewById(R.id.videoListRecyclerView);

        getVideoList();
    }

    /**
     * Obtain the videos list from the firebase database
     */
    private void getVideoList () {
        myRef.child("Androstream").child("Videos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                downloadarraylist.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    Log.i(TAG, postSnapshot.toString());
                    downloadarraylist.add(new VideoClass(postSnapshot.getKey(),postSnapshot.getValue().toString()));
                    updateRecyclerView();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    /**
     * Updating the recyclerview after obtaining the updated video list
     */
    private void updateRecyclerView () {
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, downloadarraylist,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        videoListRecyclerView.setLayoutManager(linearLayoutManager);
        videoListRecyclerView.setAdapter(adapter);
    }


    /**
     * Select Video from MediaStore using createChooser
     * @param view
     */
    public void getPackageToUpload(View view) {
        Log.i("TAG","FAb pressed");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent,"Select a Video "),GET_VIDEO_FROM_MEDIASTORE );
    }

    /**
     * Getting the file name of the video after importing it from MediaStore
     * @param uri
     * @return
     */
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    /**
     *  Start uploading the video to Firebase Cloud after obtaining filename
     */
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GET_VIDEO_FROM_MEDIASTORE) {
                Uri selectedVideoUri = data.getData();

                StorageReference ref=storageRef.child(getFileName(selectedVideoUri));
                UploadTask uploadTask = ref.putFile(selectedVideoUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sendNotification("Video Uploaded Successfully");
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                updateListInServer(getFileName(selectedVideoUri),uri.toString());
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        sendNotification("Video Upload failed");
                    }
                });
            }
        }
    }


    /**
     * Adding the new video in the database.
     * @param fileName
     * @param downloadUrl
     */
    private void updateListInServer(String fileName, String downloadUrl) {
//        Log.i("TAG", "updateListInServer: "+);
        VideoClass newVideo=new VideoClass(fileName,downloadUrl);
        myRef.child("Androstream").child("Videos").child(fileName.replace('.','-')).setValue(downloadUrl);
    }

    /**
     * Create the Notification channel for Androstream
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Send Notification to the user about the completion/failure of the upload
     * @param ContentText
     */
    void sendNotification(String ContentText){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.defaultthumbnail)
                .setContentTitle("Androstream")
                .setContentText(ContentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(notificationId, builder.build());
    }


    @Override
    public void onVideoClick(int position) {
        Log.i(TAG, "onVideoClick: Clicked");
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(downloadarraylist.get(position).streamlink),"video/*");
        startActivity(intent);
    }
}