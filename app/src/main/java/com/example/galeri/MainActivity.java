package com.example.galeri;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private ArrayList<String> imagePaths;
    private RecyclerView imagesRV;
    private RecyclerViewAdapter imageRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagePaths = new ArrayList<>();
        imagesRV = findViewById(R.id.idRVImages);

        // Запрос разрешений при создании активности
        requestPermissions();
        prepareRecyclerView();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (checkPermission()) {
            Toast.makeText(this, "Permissions granted..", Toast.LENGTH_SHORT).show();
            new GetImagesTask().execute(getContentResolver());
        } else {
            // Запрашиваем разрешение, если оно не предоставлено
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private void prepareRecyclerView() {
        imageRVAdapter = new RecyclerViewAdapter(MainActivity.this, imagePaths);
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 4);
        imagesRV.setLayoutManager(manager);
        imagesRV.setAdapter(imageRVAdapter);
    }

    private class GetImagesTask extends AsyncTask<ContentResolver, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(ContentResolver... params) {
            ContentResolver contentResolver = params[0];
            ArrayList<String> images = new ArrayList<>();

            boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            if (isSDPresent) {
                String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
                String orderBy = MediaStore.Images.Media._ID;

                Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, orderBy);
                if (cursor != null) {
                    int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    while (cursor.moveToNext()) {
                        images.add(cursor.getString(dataColumnIndex));
                    }
                    cursor.close();
                } else {
                    return null; // No images found
                }
            } else {
                return null; // SD Card is not available
            }
            return images;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null) {
                imagePaths.clear();
                imagePaths.addAll(result);
                imageRVAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this, "No images found or SD Card is not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
                        new GetImagesTask().execute(getContentResolver());
                    } else {
                        Toast.makeText(this, "Permissions denied, Permissions are required to use the app..", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
