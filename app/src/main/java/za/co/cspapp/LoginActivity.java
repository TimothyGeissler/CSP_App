package za.co.cspapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import za.co.cspapp.objects.DealerObject;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String SHARED_PREFS = "MyDetails";
    private static final String API_LOGIN = "https://iris.carsalesportal.co.za/api/login";
    private static final String API_DEALER = "https://iris.carsalesportal.co.za/api/dealers";
    private static JSONArray DEALERS = null;
    //private static String api_token = "KfTG19JlExn8JjdrGbrTitQw36h7mgXgTZvYly6gkKlhcQpk9zlPKwGk0TK513Id";

    private EditText input_email, input_password;

    OkHttpClient client = new OkHttpClient();

    Call doGetRequest(String url, Callback callback) throws IOException {

        SharedPreferences settings = getApplicationContext().getSharedPreferences(SHARED_PREFS, 0);
        String api_token = settings.getString("api_token", "0");
        System.out.println("API TOKEN: " + api_token);

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

    Call doPostRequest(String url, String email, String password, Callback callback) throws IOException {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", email)
                .addFormDataPart("password", password)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        Button btn_next = findViewById(R.id.btn_next);

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(input_email.getText().toString(),
                        input_password.getText().toString());
            }
        });
    }


    private void loginUser(final String email, final String password) {
        try {
            doPostRequest(API_LOGIN, email, password, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseStr = response.body().string();
                        System.out.println("ResponseString: " + responseStr);
                        JSONObject jObj = new JSONObject(responseStr);
                        boolean error = jObj.getBoolean("error");

                        if (!error) {
//                        int id = jObj.getJSONObject("user").getInt("id");
                            String name = jObj.getJSONObject("user").getString("name");
                            String api_token = jObj.getJSONObject("user").getString("api_token");

                            SharedPreferences settings = getApplicationContext().getSharedPreferences(SHARED_PREFS, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("name", name);
                            editor.putString("api_token", "Bearer " + api_token);
                            // Apply the edits!
                            editor.apply();
                            getDealers();

                        } else {
                            String errorMsg = jObj.getString("error_message");
//                            Toast.makeText(getApplicationContext(),
//                                    errorMsg, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void getDealers() {
        try {
            doGetRequest(API_DEALER, new Callback() {
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

                            ArrayList<DealerObject> dealers = new ArrayList<>();
                            for(int x=0; x < DEALERS.length(); x++){
                                try{
                                    int id = DEALERS.getJSONObject(x).getInt("id");
                                    String name = DEALERS.getJSONObject(x).getString("name");
                                    dealers.add(new DealerObject(id, name));
                                } catch (JSONException e){

                                }
                            }
                            DealerObject current_dealer = dealers.get(0);

                            Bundle bundle = new Bundle();
                            bundle.putSerializable("dealers", dealers);
                            bundle.putSerializable("current_dealer", current_dealer);
                            final Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);

                        } else {
                            String errorMsg = jObj.getString("error_message");
    //                        Toast.makeText(getApplicationContext(),
    //                                errorMsg, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {

                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}