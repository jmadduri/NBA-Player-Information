package com.example.mobileappfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlayerStats extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    EditText playerLName;
    Button confirm;
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
        setContentView(R.layout.activity_player_stats);
        playerLName = findViewById(R.id.editText_PlayerLName);
        confirm = findViewById(R.id.playerStats_button);
        firstName = findViewById(R.id.playerstats_firstName);
        lastNametv = findViewById(R.id.playerStats_lastName);
        dateOfBirth = findViewById(R.id.playerStats_DOB);
        yearsPlayed = findViewById(R.id.playerStats_YearsPlayed);
        college = findViewById(R.id.playerStats_college);
        yearStarted = findViewById(R.id.playerStats_start);
        height = findViewById(R.id.playerStats_height);
        weight = findViewById(R.id.playerStats_weight);
        jerseyNum = findViewById(R.id.playerStats_jerseyNum);
        playingPosition = findViewById(R.id.playerStats_position);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.playerstats);
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
                        startActivity(new Intent(getApplicationContext(), TeamStats.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.playerstats:
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
                PlayerInfo playerInfo = new PlayerInfo(playerLName.getText().toString());
                playerInfo.execute();
            }
        });
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
                            PlayerStats.this.runOnUiThread(new Runnable() {
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
                                        firstName.setText("First Name: "+ String.valueOf(jo.getString("firstName")));
                                        lastNametv.setText("Last Name: "+ String.valueOf(jo.getString("lastName")));
                                        dateOfBirth.setText("Birthday: "+ String.valueOf(jo.getString("dateOfBirth")));
                                        yearsPlayed.setText("Years Played: "+ String.valueOf(jo.getString("yearsPro")));
                                        college.setText("College: "+ String.valueOf(jo.getString("collegeName")));
                                        yearStarted.setText("Year Started: "+ String.valueOf(jo.getString("startNba")));
                                        height.setText("Height(m): "+ String.valueOf(jo.getString("heightInMeters")));//Change to customary system
                                        weight.setText("Weight(kg): "+ String.valueOf(jo.getString("weightInKilograms")));//Change to customary system
                                        jerseyNum.setText("Jersey Number: "+ String.valueOf(jsonObject2.getString("jersey")));
                                        playingPosition.setText("Position: "+ String.valueOf(jsonObject2.getString("pos")));
                                    } catch (JSONException e) {
                                        Log.d("TAG", e.getMessage());
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