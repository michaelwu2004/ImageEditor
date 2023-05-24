package com.example.imageeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    OkHttpClient client;
    String url = "https://eulerity-hackathon.appspot.com/image";
    String json = "";
    JSONArray jsonImages;

    Button loadBtn, nextBtn, editBtn;
    ImageView imageView;

    ArrayList<String> imageUrls;
    int idx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();
        imageView = findViewById(R.id.image);
        imageUrls = new ArrayList<>();

        initializeButtons();
    }

    /**
     * Sends a GET Request and creates a jsonArray of the provided url
     * **/
    public void get() {
        Request request = new Request.Builder()
                .url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            json = response.body().string();
                            //textView.setText(json);
                            jsonImages = new JSONArray(json);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    /**
     * Iterates over jsonArray to edit and store imageViews into an ArrayList imageUrls
     * so that collection of images can be cycled over
     * **/
    public void loadImages() {
        try {
            if (jsonImages != null) {
                for (int i = 0; i < jsonImages.length(); i++) {
                    String jsonImagesString = jsonImages.getString(i);
                    JSONObject imageJson = new JSONObject(jsonImagesString);
                    String url = imageJson.getString("url");
                    ImageView urlImage = new ImageView(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    urlImage.setLayoutParams(layoutParams);
                    urlImage.setAdjustViewBounds(true);
                    urlImage.setMaxHeight(200);
                    urlImage.setMaxWidth(200);
                    urlImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageUrls.add(url);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initiliazes the button objects
     * **/
    public void initializeButtons() {
        loadBtn = findViewById(R.id.loadBtn);
        nextBtn = findViewById(R.id.nextBtn);
        editBtn = findViewById(R.id.editBtn);



        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get();
                loadImages();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Cycles through the urls of imageUrls and
             * loads them into the imageview
             * **/
            @Override
            public void onClick(View view) {
                String url = imageUrls.get(idx);
                idx = (idx + 1) % imageUrls.size();
                Picasso.get().load(url).into(imageView);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("url", imageUrls.get(idx - 1));
                startActivity(intent);
            }
        });
    }
}