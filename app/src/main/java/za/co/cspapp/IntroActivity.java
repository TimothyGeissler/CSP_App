package za.co.cspapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import za.co.cspapp.objects.DealerObject;

public class IntroActivity extends AppCompatActivity {
    private static final String TAG = "IntroActivity";
    private static final String SHARED_PREFS = "MyDetails";
    private static final String API_DEALER_STATS = "https://iris.carsalesportal.co.za/api/dealerstats/";

    private Toolbar toolbar;
    public DealerObject current_dealer;
    public ArrayList<DealerObject> dealers;
    Menu menu;
//    private TextView used_unpub;
//    private TextView val_used_unpub;
//    private TextView used_pub;
//    private TextView val_used_pub;
//    private TextView demo_unpub;
//    private TextView val_demo_unpub;
//    private TextView demo_pub;
//    private TextView val_demo_pub;
    private Handler handler;

    OkHttpClient client = new OkHttpClient();

    Call doGetRequest(String url, Callback callback) throws IOException {

        SharedPreferences settings = getApplicationContext().getSharedPreferences(SHARED_PREFS, 0);
        String api_token = settings.getString("api_token", "0");

        okhttp3.Request request = new okhttp3.Request.Builder()
                .header("Authorization", api_token)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        final Bundle bundle = getIntent().getExtras();
        dealers = (ArrayList<DealerObject>) bundle.getSerializable("dealers");
        current_dealer = (DealerObject) bundle.getSerializable("current_dealer");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle("CSP : " + current_dealer.getName());

        Button btn = findViewById(R.id.btn_viewStock);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroActivity.this, SearchActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        getStats(current_dealer.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchactivity, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateOptionsMenu();

        current_dealer.setId(item.getItemId());
        current_dealer.setName(item.getTitle().toString());
        Bundle bundle = new Bundle();
        bundle.putSerializable("dealers", dealers);
        bundle.putSerializable("current_dealer", current_dealer);
        final Intent intent = new Intent(this, IntroActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();
        menu.add("My Dealerships");
        getData(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void getData(Menu menu) {
        for (int i = 0; i < dealers.size(); i++) {
            menu.add(0, dealers.get(i).getId(), 0, dealers.get(i).getName());
        }
    }

    public void getStats(final int dealer_id){
        handler = new Handler(Looper.getMainLooper());

        try {
            doGetRequest(API_DEALER_STATS+dealer_id, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    try {
                        String responseStr = response.body().string();
                        JSONObject jObj = new JSONObject(responseStr);
                        JSONObject jData = jObj.getJSONObject("data");
                        boolean error = jData.getBoolean("error");

                        if (!error) {

                            JSONArray stats = jData.getJSONArray("stats");
                            for(int i = 0; i < stats.length(); i++) {
                                final JSONObject stat = stats.getJSONObject(i);
                                final TextView used_unpub = findViewById(R.id.used_unpublished);
                                final TextView val_used_unpub = findViewById(R.id.val_used_unpublished);
                                final TextView used_pub = findViewById(R.id.used_published);
                                final TextView val_used_pub = findViewById(R.id.val_used_published);

                                final TextView demo_unpub = findViewById(R.id.demo_unpublished);
                                final TextView val_demo_unpub = findViewById(R.id.val_demo_unpublished);
                                final TextView demo_pub = findViewById(R.id.demo_published);
                                final TextView val_demo_pub = findViewById(R.id.val_demo_published);

                                if(stat.getInt("state_id") == 1 && stat.getInt("type") == 1){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                used_unpub.setText(String.valueOf(stat.getInt("count")));
                                                val_used_unpub.setText(stat.getString("total"));
                                            } catch (JSONException e){

                                            }
                                            used_unpub.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("dealers", dealers);
                                                    bundle.putSerializable("current_dealer", current_dealer);
                                                    Intent intent = new Intent(IntroActivity.this, SearchActivity.class);
                                                    intent.putExtras(bundle);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    });
                                } else if(stat.getInt("state_id") == 2 && stat.getInt("type") == 1){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                used_pub.setText(String.valueOf(stat.getInt("count")));
                                                val_used_pub.setText(stat.getString("total"));
                                            } catch (JSONException e){

                                            }
                                            used_pub.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("dealers", dealers);
                                                    bundle.putSerializable("current_dealer", current_dealer);
                                                    int page = 1;
                                                    Intent intent = new Intent(IntroActivity.this, SearchActivity.class);
                                                    intent.putExtras(bundle);
                                                    intent.putExtra("page", page);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    });
                                } else if(stat.getInt("state_id") == 1 && stat.getInt("type") == 2){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                demo_unpub.setText(String.valueOf(stat.getInt("count")));
                                                val_demo_unpub.setText(stat.getString("total"));
                                            } catch (JSONException e){

                                            }
                                            demo_unpub.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("dealers", dealers);
                                                    bundle.putSerializable("current_dealer", current_dealer);
                                                    Intent intent = new Intent(IntroActivity.this, SearchActivity.class);
                                                    intent.putExtras(bundle);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    });
                                } else if(stat.getInt("state_id") == 2 && stat.getInt("type") == 2){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                demo_pub.setText(String.valueOf(stat.getInt("count")));
                                                val_demo_pub.setText(stat.getString("total"));
                                            } catch (JSONException e){

                                            }
                                            demo_pub.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("dealers", dealers);
                                                    bundle.putSerializable("current_dealer", current_dealer);
                                                    int page = 1;
                                                    Intent intent = new Intent(IntroActivity.this, SearchActivity.class);
                                                    intent.putExtras(bundle);
                                                    intent.putExtra("page", page);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                        } else {
                            String errorMsg = jObj.getString("error_message");
                            //                        Toast.makeText(getApplicationContext(),
                            //                                "There has been an error fetching data", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        //                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
