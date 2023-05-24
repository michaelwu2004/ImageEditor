package com.example.imageeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;
import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubFilter;

import com.example.imageeditor.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditActivity extends AppCompatActivity implements View.OnClickListener{
    OkHttpClient client;
    String uploadUrl;
    EditText editText;
    TextView textView;
    Button saveBtn, placeBtn;
    String url = "https://eulerity-hackathon.appspot.com/upload";
    String imageUrl;
    ImageView imageMain, image1, image2, image3, image4;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        initializeFields();
        setUpButtons();
    }

    /**
     * Sets up listener for the view
     * **/
    @Override
    public void onClick(View view) {
        Filter myFilter = new Filter();
        BitmapDrawable drawable;
        Bitmap bitmap, image, output;
        int id = view.getId();
        if (id == R.id.image1) {
            myFilter.addSubFilter(new BrightnessSubFilter(30));
            myFilter.addSubFilter(new SaturationSubFilter(1.3f));

            drawable = (BitmapDrawable) imageMain.getDrawable();
            bitmap = drawable.getBitmap();
            image = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            output = myFilter.processFilter(image);

            imageMain.setImageBitmap(output);
        }
        if (id == R.id.image2) {
            myFilter = SampleFilters.getBlueMessFilter();

            drawable = (BitmapDrawable) imageMain.getDrawable();
            bitmap = drawable.getBitmap();
            image = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            output = myFilter.processFilter(image);

            imageMain.setImageBitmap(output);
        }

        if (id == R.id.image3) {
            myFilter = SampleFilters.getLimeStutterFilter();

            drawable = (BitmapDrawable) imageMain.getDrawable();
            bitmap = drawable.getBitmap();
            image = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            output = myFilter.processFilter(image);

            imageMain.setImageBitmap(output);
        }

        if (id == R.id.image4) {
            myFilter = SampleFilters.getNightWhisperFilter();

            drawable = (BitmapDrawable) imageMain.getDrawable();
            bitmap = drawable.getBitmap();
            image = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            output = myFilter.processFilter(image);

            imageMain.setImageBitmap(output);
        }
    }

    /**
     * Initializes the field variables
     * **/
    public void initializeFields() {
        Intent intent = getIntent();
        client = new OkHttpClient();
        imageUrl = intent.getStringExtra("url");

        imageMain = findViewById(R.id.imageMain);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);

        saveBtn = findViewById(R.id.saveBtn);
        placeBtn = findViewById(R.id.placeBtn);

        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.imageText);

        Picasso.get().load(imageUrl).into(imageMain);

        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        image4.setOnClickListener(this);

        Picasso.get().load(url).into(image1);
        Picasso.get().load(url).into(image2);
        Picasso.get().load(url).into(image3);
        Picasso.get().load(url).into(image4);
    }

    /**
     * Sets up the listeners for the buttons on
     * the edit activity
     * **/
    public void setUpButtons() {
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get();
                upload();
            }
        });

        placeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                textView.setText(text);
            }
        });
    }

    /**
     * makes GET request to get
     * upload url
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
                            String json = response.body().string();
                            JSONObject jsonObject = new JSONObject(json);
                            uploadUrl = jsonObject.getString("url");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * Combines two bitmaps and returns the combination of the
     * two bitmaps
     * **/
    public Bitmap combineImages(Bitmap background, Bitmap foreground) {

        int width = 0, height = 0;
        Bitmap cs;

        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);
        background = Bitmap.createScaledBitmap(background, width, height, true);
        comboImage.drawBitmap(background, 0, 0, null);
        comboImage.drawBitmap(foreground, 0, 0, null);

        return cs;
    }

    public File toFile() {
        Bitmap bmp = Bitmap.createBitmap(textView.getDrawingCache());
        BitmapDrawable drawable = (BitmapDrawable) imageMain.getDrawable();
        Bitmap imgBmp = drawable.getBitmap();
        Bitmap textOverImage = combineImages(bmp, imgBmp);

        File f = new File(this.getCacheDir(), "editedImage");

        try {
            f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            textOverImage.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

    public void upload() {
        String appid = "michaelwu";
        String originalImageUrl = imageUrl;
        File imageFile = toFile();
        // Create the request body with multipart/form-data encoding
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("appid", appid)
                .addFormDataPart("original", originalImageUrl)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                .build();

        // Create the request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // Execute the request
        Response response;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
