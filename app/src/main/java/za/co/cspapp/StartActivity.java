package za.co.cspapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import za.co.cspapp.objects.DealerObject;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    private static final String SHARED_PREFS = "MyDetails";
    private static final String API_DEALER = "https://iris.carsalesportal.co.za/api/dealers";
    private static JSONArray DEALERS;
    boolean connected = false;
    private ProgressBar spinner;
    OkHttpClient client = new OkHttpClient();

    Call doGetRequest(String url, Callback callback) throws IOException {

        SharedPreferences settings = getApplicationContext().getSharedPreferences(SHARED_PREFS, 0);
        String api_token = settings.getString("api_token", "0");

        Request request = new Request.Builder()
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
        setContentView(R.layout.activity_start);
        spinner = findViewById(R.id.progressBar1);

        SharedPreferences settings = getApplicationContext().getSharedPreferences(SHARED_PREFS, 0);
        String api_token = settings.getString("api_token", "0");

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        try {
            NetworkInfo.State connectedMobileState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            NetworkInfo.State connectedWifiState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

            if (connectedMobileState == NetworkInfo.State.CONNECTED || connectedWifiState == NetworkInfo.State.CONNECTED) {
                connected = true;
            } else {
                Toast.makeText(getApplicationContext(), "Please connect to the internet first.", Toast.LENGTH_LONG).show();
            }
        } catch(NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Your connectivity state could not be established.  Please ensure you have internet accesss.", Toast.LENGTH_LONG).show();
        }

        if(!api_token.equals("0") && connected){
            getDealers();
        } else {
            spinner.setVisibility(View.GONE);
            Button btn = findViewById(R.id.btn_enter);
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    private void getDealers() {

        try {
            Call doResponse = this.doGetRequest(API_DEALER, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseStr = response.body().string();
                        JSONObject jObj = new JSONObject(responseStr);
                        JSONObject jData = jObj.getJSONObject("data");
                        boolean error = jData.getBoolean("error");

                        if (!error) {
                            DEALERS = jData.getJSONArray("dealers");

                            ArrayList<DealerObject> dealers = new ArrayList<DealerObject>();
                            for(int x=0; x < DEALERS.length(); x++){
                                try{
                                    int id = DEALERS.getJSONObject(x).getInt("id");
                                    String name = DEALERS.getJSONObject(x).getString("name");
                                    dealers.add(new DealerObject(id, name));
                                } catch (JSONException e){
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                            DealerObject current_dealer = dealers.get(0);

                            Bundle bundle = new Bundle();
                            bundle.putSerializable("dealers", dealers);
                            bundle.putSerializable("current_dealer", current_dealer);
                            final Intent intent = new Intent(StartActivity.this, IntroActivity.class);
                            intent.putExtras(bundle);
//                            spinner.setVisibility(View.GONE);
                            startActivity(intent);

                        } else {
                            String errorMsg = jObj.getString("error_message");
    //                        Toast.makeText(getApplicationContext(),
    //                                errorMsg, Toast.LENGTH_LONG).show();
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

