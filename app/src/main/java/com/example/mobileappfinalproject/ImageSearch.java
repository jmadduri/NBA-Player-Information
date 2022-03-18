package com.example.mobileappfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ImageSearch extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    public final static int PICK_PHOTO_CODE = 2003;
    Button button;
    TextView textView;
    List list = new ArrayList<String>();
    TextView firstName;
    TextView lastNametv;
    TextView dateOfBirth;
    TextView yearsPlayed;
    TextView college;
    TextView yearStarted;
    TextView height;
    TextView weight;
    TextView jerseyNum;
    TextView playingPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.imagesearch);
        button = findViewById(R.id.button);
        firstName = findViewById(R.id.imageSearch_firstname);
        lastNametv = findViewById(R.id.imagesearch_lastName);
        dateOfBirth = findViewById(R.id.imagesearch_birthday);
        yearsPlayed = findViewById(R.id.imagesearch_yearsPlayed);
        college = findViewById(R.id.imagesearch_college);
        yearStarted = findViewById(R.id.imagesearch_yearstarted);
        height = findViewById(R.id.imagesearch_height);
        weight = findViewById(R.id.imagesearch_weight);
        jerseyNum = findViewById(R.id.imagesearch_jerseyNum);
        playingPosition = findViewById(R.id.imagesearch_playingpos);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.teamstats:
                        startActivity(new Intent(getApplicationContext(), TeamStats.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.playerstats:
                        startActivity(new Intent(getApplicationContext(), PlayerStats.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.imagesearch:
                        return true;
                }
                return false;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });
    }

    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Uri photoUri = data.getData();
            // Do something with the photo based on Uri
            Bitmap selectedImage = null;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Load the selected image into a preview
            ImageView ivPreview = (ImageView) findViewById(R.id.imagesearch_image);
            ivPreview.setImageBitmap(selectedImage);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(selectedImage);//Finish this part to get firebase image recognition
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getCloudTextRecognizer();
            FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                    .setLanguageHints(Arrays.asList("en"))
                    .build();

            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    // Task completed successfully
                                    // ...
                                    //textView = findViewById(R.id.textView_temp);
                                    //textView.setText(firebaseVisionText.getText());
                                    StringTokenizer tokenizer = new StringTokenizer(firebaseVisionText.getText());
                                    while(tokenizer.hasMoreTokens())
                                    {
                                        list.add(tokenizer.nextToken());
                                    }
                                    for(int i = 0; i<list.size(); i++)
                                    {
                                            PlayerInfo playerInfo = new PlayerInfo(list.get(i).toString());
                                            playerInfo.execute();
                                    }
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                            Log.d("TAGVisionFailed", e.getMessage());
                                        }
                                    });
        }
    }
    public class PlayerInfo extends AsyncTask<String, Void, String> {
        private String data;
        private String lastName;

        public PlayerInfo(String lastNam) {
            data = "";
            lastName = lastNam;
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://api-nba-v1.p.rapidapi.com/players/lastName/"+lastName)
                        .get()
                        .addHeader("x-rapidapi-host", "api-nba-v1.p.rapidapi.com")
                        .addHeader("x-rapidapi-key", "YOUR_API_KEY_HERE")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String myResponse = response.body().string();
                            ImageSearch.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    data = myResponse;
                                        try {
                                            JSONObject jsonObject = new JSONObject(data);
                                            JSONObject j = jsonObject.getJSONObject("api");
                                            JSONArray ja = j.getJSONArray("players");
                                            JSONObject jo = ja.getJSONObject(0);
                                            JSONObject jsonObject1 = jo.getJSONObject("leagues");//Only Jersey Number, Active, and Position
                                            JSONObject jsonObject2 = jsonObject1.getJSONObject("standard");//Only Jersey Number, Active, and Position
                                            firstName.setText("First Name: " + String.valueOf(jo.getString("firstName")));
                                            lastNametv.setText("Last Name: " + String.valueOf(jo.getString("lastName")));
                                            dateOfBirth.setText("Birthday: " + String.valueOf(jo.getString("dateOfBirth")));
                                            yearsPlayed.setText("Years Played: " + String.valueOf(jo.getString("yearsPro")));
                                            college.setText("College: " + String.valueOf(jo.getString("collegeName")));
                                            yearStarted.setText("Year Started: " + String.valueOf(jo.getString("startNba")));
                                            height.setText("Height(m): " + String.valueOf(jo.getString("heightInMeters")));//Change to customary system
                                            weight.setText("Weight(kg): " + String.valueOf(jo.getString("weightInKilograms")));//Change to customary system
                                            jerseyNum.setText("Jersey Number: " + String.valueOf(jsonObject2.getString("jersey")));
                                            playingPosition.setText("Position: " + String.valueOf(jsonObject2.getString("pos")));
                                        } catch (JSONException e) {
                                            Log.d("TAGImgSearch", e.getMessage());
                                        }
                                    }
                            });
                        }
                    }
                });
            } catch (Exception e) {
                Log.d("TAGBackground", e.getMessage());
            }
            return data;
        }
    }
}