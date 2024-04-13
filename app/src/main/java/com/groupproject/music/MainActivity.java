package com.groupproject.music;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //members
    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        //recycler view
        recyclerView = findViewById(R.id.recyclerView);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted->{
            if(granted){
                //fetch song
                fetchSongs();
            }else {
                userResponses();
            }
        });

        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
            storagePermissionLauncher.launch(permission);
        } else {
            fetchSongs();
        }
    }

    private void userResponses() {
        if(ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED){
            fetchSongs();
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(shouldShowRequestPermissionRationale(permission)){
                new AlertDialog.Builder(this)
                        .setTitle("Requesting Permission")
                        .setMessage("Allow us to fetch songs on your device")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "You denied to show songs", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        }
        else {
            Toast.makeText(this, "You canceled to show songs", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSongs() {
        //define list to show
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        //projection
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID
        };

        //order
        String sortOrder = MediaStore.Audio.Media.TITLE + " DESC";

        //get the songs
        try(Cursor cursor = getContentResolver().query(mediaStoreUri,projection,null,null,sortOrder)) {
            //cache cursor indices
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            //clear previous songs before load
            while (cursor.moveToNext()){
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumIdColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);

                Uri albumArtworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);

                name = name.substring(0,name.lastIndexOf('.'));

                Song song = new Song(name,uri,albumArtworkUri,size,duration);

                songs.add(song);
            }
            showSongs(songs);
        }
    }

    private void showSongs(List<Song> songs) {
        if (songs.size() == 0){
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
        }
        allSongs.clear();
        allSongs.addAll(songs);

        String title = getResources().getString(R.string.app_name) + " (" + songs.size() + ")";
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        songAdapter = new SongAdapter(this,songs);

        recyclerView.setAdapter(songAdapter);
    }

}