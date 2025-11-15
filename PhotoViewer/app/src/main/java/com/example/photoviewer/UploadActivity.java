package com.example.photoviewer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    private static final int SELECT_IMAGE = 1;
    private static final int PERMISSION_REQUEST = 101;
    private ImageView imagePreview;
    private EditText titleEdit, textEdit;
    private Button btnSelect, btnUpload;
    private Uri selectedImageUri;
    private static final String UPLOAD_URL = "http://10.0.2.2:8000/api_root/Post/";
    private static final String TOKEN = "YOUR_TOKEN_HERE";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        imagePreview = findViewById(R.id.imagePreview);
        titleEdit = findViewById(R.id.editTitle);
        textEdit = findViewById(R.id.editText);
        btnSelect = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(v -> checkPermissionAndPick());
        btnUpload.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadData();
            } else {
                Toast.makeText(this, "이미지를 선택해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPermissionAndPick() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ permission }, PERMISSION_REQUEST);
        } else {
            pickImage();
        }
    }


    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                imagePreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void uploadData() {
        String title = titleEdit.getText().toString().trim();
        String text = textEdit.getText().toString().trim();
        if (title.isEmpty() || text.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                try {
                    URL url = new URL(UPLOAD_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestProperty("Authorization", "Token " + TOKEN);
                    String boundary = "----" + UUID.randomUUID().toString();
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    dos = new DataOutputStream(conn.getOutputStream());
                    // 제목 파라미터
                    writeFormField(dos, boundary, "title", title);
                    // 내용 파라미터
                    writeFormField(dos, boundary, "text", text);
                    // 이미지 파일 파라미터
                    InputStream is = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                    byte[] bytes = bos.toByteArray();
                    writeFileField(dos, boundary, "image", "upload.jpg", "image/jpeg", bytes);
                    // 끝 경계문자
                    dos.writeBytes("--" + boundary + "--\r\n");
                    dos.flush();
                    int responseCode = conn.getResponseCode();
                    return responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    try {
                        if (dos != null) dos.close();
                        if (conn != null) conn.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(UploadActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UploadActivity.this, "업로드 실패", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void writeFormField(DataOutputStream dos, String boundary, String name, String value) throws IOException {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        dos.writeBytes(value + "\r\n");
    }

    private void writeFileField(DataOutputStream dos, String boundary, String name, String filename, String mimeType, byte[] data) throws IOException {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n");
        dos.writeBytes("Content-Type: " + mimeType + "\r\n\r\n");
        dos.write(data);
        dos.writeBytes("\r\n");
    }
}
