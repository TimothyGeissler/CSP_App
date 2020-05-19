package za.co.cspapp.viewholders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import za.co.cspapp.R;
import za.co.cspapp.StockActivity;
import za.co.cspapp.objects.DealerObject;
import za.co.cspapp.objects.StockObject;


public class PhotoViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    private static final String TAG = "PhotoViewHolder";
    private static final String API_PHOTO_DELETE = "https://iris.carsalesportal.co.za/api/photo/remove/";
    private static final String SHARED_PREFS = "MyDetails";
    public int photo_id;
    public int stock_id;
    public StockObject stock;
    public ArrayList<DealerObject> dealers;
    public DealerObject current_dealer;
    public ImageView photo;

    OkHttpClient client = new OkHttpClient();

    Call doGetRequest(String url, Callback callback) throws IOException {

        SharedPreferences settings = itemView.getContext().getSharedPreferences(SHARED_PREFS, 0);
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

    public PhotoViewHolders(View itemView) {
        super(itemView);

        Button btn_del = itemView.findViewById(R.id.btn_delete);
        btn_del.setOnClickListener(this);
        photo = itemView.findViewById(R.id.photo);
    }

    @Override
    public void onClick(final View view) {

        try {
            doGetRequest(API_PHOTO_DELETE + Integer.toString(photo_id), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {

                    try {
                        String responseStr = response.body().string();
                        JSONObject jObj = new JSONObject(responseStr);
                        boolean error = jObj.getBoolean("error");

                        if (!error) {
                            Bundle bundle = new Bundle();
    //                        bundle.putSerializable("stock", stock);
                            bundle.putSerializable("dealers", dealers);
                            bundle.putSerializable("current_dealer", current_dealer);
                            Intent intent = new Intent(view.getContext(), StockActivity.class);
                            intent.putExtras(bundle);
                            intent.putExtra("stock_id", stock_id);
                            intent.putExtra("reload", true);
                            view.getContext().startActivity(intent);
                        } else {
                            String errorMsg = jObj.getString("error_message");
    //                        Toast.makeText(view.getContext(),
    //                                errorMsg, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}