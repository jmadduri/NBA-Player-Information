package com.example.mobileappfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TeamStats extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Button confirm;
    TextView fullTeamName;
    TextView nickName;
    TextView teamCity;
    TextView shortName;
    TextView conference;
    ImageView imageView;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.teamstats);
        teamCity = findViewById(R.id.teamStats_city);
        nickName = findViewById(R.id.teamStats_nickName);
        shortName = findViewById(R.id.teamStats_shortName);
        conference = findViewById(R.id.teamStats_conference);
        fullTeamName = findViewById(R.id.teamStats_fullName);
        imageView = findViewById(R.id.teamStats_image);
        confirm = findViewById(R.id.teamStats_confirm);
        editText = findViewById(R.id.teamStats_editText);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.teamstats:
                        return true;

                    case R.id.playerstats:
                        startActivity(new Intent(getApplicationContext(), PlayerStats.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.imagesearch:
                        startActivity(new Intent(getApplicationContext(), ImageSearch.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TeamInfo teamInfo = new TeamInfo(editText.getText().toString());
                teamInfo.execute();
            }
        });
    }
    public class TeamInfo extends AsyncTask<String, Void, String> {
        private String data;
        private String nickNam;

        public TeamInfo(String nickName) {
            data = "";
            nickNam = nickName;
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api-nba-v1.p.rapidapi.com/teams/nickName/"+nickNam)
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
                            TeamStats.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    data = myResponse;
                                    try {
                                        JSONObject jsonObject = new JSONObject(data);
                                        JSONObject j = jsonObject.getJSONObject("api");
                                        JSONArray ja = j.getJSONArray("teams");
                                        JSONObject jo = ja.getJSONObject(0);
                                        JSONObject jsonObject1 = jo.getJSONObject("leagues");//Only for conference
                                        JSONObject jsonObject2 = jsonObject1.getJSONObject("standard");//Only for conference
                                        fullTeamName.setText("Team Full Name: "+ String.valueOf(jo.getString("fullName")));
                                        nickName.setText("Nickname: "+ String.valueOf(jo.getString("nickname")));
                                        shortName.setText("Short Name: "+ String.valueOf(jo.getString("shortName")));
                                        teamCity.setText("City: "+ String.valueOf(jo.getString("city")));
                                        conference.setText("Conference: "+ String.valueOf(jsonObject2.getString("confName")));
                                        DownloadImageTask downloadImageTask = new DownloadImageTask(imageView, String.valueOf(jo.getString("logo")));
                                        downloadImageTask.execute();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
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
        public String getNickName()
        {
            return nickNam;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        ImageView imageView;
        String url;

        public DownloadImageTask(ImageView imageView, String url)
        {
            this.imageView=imageView;
            this.url=url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap logo = null;
            try {
                InputStream is = new URL(url).openStream();
                logo = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                Log.d("TAG",e.getMessage());
            }
            return logo;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}