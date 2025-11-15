package com.example.photoviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import java.io.OutputStream;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView detailImage;
    private TextView detailTitle;
    private TextView detailText;
    private Button btnDownload;
    private Button btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        detailText = findViewById(R.id.detailText);
        btnDownload = findViewById(R.id.btnDownload);
        btnShare = findViewById(R.id.btnShare);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        String imageUrl = intent.getStringExtra("image");
        detailTitle.setText(title);
        detailText.setText(text);
        Picasso.get().load(imageUrl).into(detailImage);

        btnDownload.setOnClickListener(v -> saveImageToGallery());
        btnShare.setOnClickListener(v -> shareImage());
    }

    private void saveImageToGallery() {
        Drawable drawable = detailImage.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try {
                OutputStream os = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.close();
                Toast.makeText(this, "이미지를 갤러리에 저장했습니다.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "이미지 로딩 중입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        Drawable drawable = detailImage.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "share", null);
            Uri uri = Uri.parse(path);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_TEXT, detailTitle.getText().toString() + "\n" + detailText.getText().toString());
            startActivity(Intent.createChooser(share, "공유하기"));
        } else {
            Toast.makeText(this, "이미지 로딩 중입니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
