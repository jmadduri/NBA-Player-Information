package com.example.mobileappfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Spinner spinner;
    List<String> teams;
    //TextView tempText;
    String str = "";
    int teamId = 0;
    TextView fullTeamName;
    TextView nickName;
    TextView teamCity;
    TextView shortName;
    TextView conference;
    List list = new ArrayList<String>();
    ImageView imageView;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    private String text;
    Button confirmFavTeam;
    Button changeFavTeam;
    TextView textView;
    boolean aBoolean = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        teams = new ArrayList<>();
        spinner = findViewById(R.id.id_main_spinner);
        //tempText = findViewById(R.id.textView);
        teamCity = findViewById(R.id.textView_city);
        nickName = findViewById(R.id.textView_nickName);
        shortName = findViewById(R.id.textView_shortname);
        conference = findViewById(R.id.textView_conference);
        fullTeamName = findViewById(R.id.textView_fullteamname);
        imageView = findViewById(R.id.imageView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        changeFavTeam = findViewById(R.id.changeFavTeamButton);
        confirmFavTeam = findViewById(R.id.confirmFavTeamButton);
        textView = findViewById(R.id.id_main_text);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.home:
                        return true;

                    case R.id.teamstats:
                        startActivity(new Intent(getApplicationContext(), TeamStats.class));
                        overridePendingTransition(0,0);
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

        try {
            teams.add("Atlanta Hawks");
            teams.add("Boston Celtics");
            teams.add("Brooklyn Nets");
            teams.add("Charlotte Hornets");
            teams.add("Chicago Bulls");
            teams.add("Cleveland Cavaliers");
            teams.add("Dallas Mavericks");
            teams.add("Denver Nuggets");
            teams.add("Detroit Pistons");
            teams.add("Golden State Warriors");
            teams.add("Houston Rockets");
            teams.add("Indiana Pacers");
            teams.add("Los Angeles Clippers");
            teams.add("Los Angeles Lakers");
            teams.add("Memphis Grizzlies");
            teams.add("Miami Heat");
            teams.add("Milwaukee Bucks");
            teams.add("Minnesota Timberwolves");
            teams.add("New Orleans Pelicans");
            teams.add("New York Knicks");
            teams.add("Oklahoma City Thunder");
            teams.add("Orlando Magic");
            teams.add("Philadelphia 76ers");
            teams.add("Phoenix Suns");
            teams.add("Portland Trail Blazers");
            teams.add("Sacramento Kings");
            teams.add("San Antonio Spurs");
            teams.add("Toronto Raptors");
            teams.add("Utah Jazz");
            teams.add("Washington Wizards");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, teams);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    str = parent.getItemAtPosition(position).toString();
                    teamId = position + 1;
                    //tempText.setText(str + " " + teamId);
                    StringTokenizer tokenizer = new StringTokenizer(str);
                    while(tokenizer.hasMoreTokens())
                    {
                        list.add(tokenizer.nextToken());
                    }
                    try
                    {
                        FavTeam favTeam = new FavTeam(loadFavTeamData());
                        favTeam.execute();

                    }catch (Exception e)
                    {
                        Log.d("TAGFavTeam", e.getMessage());
                        FavTeam favTeam = new FavTeam(list.get(list.size()-1).toString());
                        favTeam.execute();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            confirmFavTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textView.setVisibility(View.INVISIBLE);
                    spinner.setVisibility(View.INVISIBLE);
                    confirmFavTeam.setVisibility(View.INVISIBLE);
                    changeFavTeam.setVisibility(View.VISIBLE);
                    aBoolean = true;
                    FavTeam favTeam = new FavTeam(list.get(list.size()-1).toString());
                    favTeam.execute();
                    saveFavTeamData();
                }
            });

            changeFavTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeFavTeam.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.VISIBLE);
                    confirmFavTeam.setVisibility(View.VISIBLE);
                }
            });

        }catch (Exception e)
        {
            Log.d("TAG", e.getMessage());
        }
    }

    public class FavTeam extends AsyncTask<String, Void, String> {
        private String data;
        private String nickNam;

        public FavTeam(String nickName) {
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
                            MainActivity.this.runOnUiThread(new Runnable() {
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
    public void saveFavTeamData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT, list.get(list.size()-1).toString());
        editor.apply();
    }

    public String loadFavTeamData()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        text = sharedPreferences.getString(TEXT, "");
        return text;
    }
}