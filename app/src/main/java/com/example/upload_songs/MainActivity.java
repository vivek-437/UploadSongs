package com.example.upload_songs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.upload_songs.Model.UploadSongs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView TextimageView;
    ProgressBar progressBar;
    Uri audiouri;
    StorageReference mstorage;
    StorageTask mUploadTask;
    DatabaseReference reference;
    String songsCategory;
    MediaMetadataRetriever mediaMetadataRetriever;
    byte[] art;
    String title1,artist1,album_art1="",duration1;
    TextView title,artist,durations,album,dataa;
    ImageView album_art;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextimageView=findViewById(R.id.songfileselected);
        progressBar=findViewById(R.id.progressbar);
        title=findViewById(R.id.title);
        artist=findViewById(R.id.artist);
        durations=findViewById(R.id.duration);
        album=findViewById(R.id.album);
        dataa=findViewById(R.id.data);
        album_art=findViewById(R.id.imageview);
        mediaMetadataRetriever=new MediaMetadataRetriever();
        reference= FirebaseDatabase.getInstance().getReference().child("firebase_songs");
        mstorage= FirebaseStorage.getInstance().getReference().child("firebase_songs");
        Spinner spinner=findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                songsCategory=spinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), "Selected"+songsCategory, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        List<String> categories=new ArrayList<>();
        categories.add("Happy");
        categories.add("Relaxation");
        categories.add("Meditation");
        categories.add("Sadness");
        categories.add("Workout");
        categories.add("Motivational");
        ArrayAdapter<String> dataAdapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }


    public  void openAudioFiles(View v){
        Intent i=new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101&& resultCode==RESULT_OK && data.getData()!=null){
            audiouri=data.getData();
            String fileNames=getFileName(audiouri);
            TextimageView.setText(fileNames);
            mediaMetadataRetriever.setDataSource(this,audiouri);
            art=mediaMetadataRetriever.getEmbeddedPicture();
            Bitmap bitmap= BitmapFactory.decodeByteArray(art,0,art.length);
            album_art.setImageBitmap(bitmap);
            album.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            artist.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            dataa.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            durations.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            title.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            artist1=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title1=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            duration1=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);


        }
    }
    @SuppressLint("Range")
    private  String getFileName(Uri uri){
        String result=null;
        if(uri.getScheme().equals("content")){
            Cursor cursor=getContentResolver().query(uri,null,null,null,null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }
            }finally {
                cursor.close();
            }
        }
        if(result==null){
            result=uri.getPath();
            int cut=result.lastIndexOf('/');
            if(cut!=-1){
                result=result.substring(cut+1);
            }
        }
        return  result;
    }
    public  void uploadFileToFirebase(View v){
        if(TextimageView.equals("No file Selected")){
            Toast.makeText(this, "please select an image!", Toast.LENGTH_SHORT).show();

        }else{
            if(mUploadTask!=null&& mUploadTask.isInProgress()){
                Toast.makeText(this, "song upload is in progress !", Toast.LENGTH_SHORT).show();

            }else{
                uploadFiles();



            }
        }
    }

    private void uploadFiles() {
        if(audiouri!=null) {
            Toast.makeText(this, "Song Is Uploading Please Wait", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference storageReference = mstorage.child(System.currentTimeMillis() + "." + getfileextension(audiouri));
            mUploadTask = storageReference.putFile(audiouri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            UploadSongs uploadSong = new UploadSongs(songsCategory, title1, duration1, uri.toString());
                            String uploadId = reference.push().getKey();
                            reference.child(uploadId).setValue(uploadSong);
                            passmessage();
                        }

                    });

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                }
            });

        }else{
            Toast.makeText(this, "No file Selected to Upload", Toast.LENGTH_SHORT).show();


        }




    }

    private void passmessage() {
        Toast.makeText(this,"Song uploaded Successfully",Toast.LENGTH_SHORT).show();

    }

    private  String getfileextension(Uri audiouri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(audiouri));

    }
}