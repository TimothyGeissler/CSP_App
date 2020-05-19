package za.co.cspapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import za.co.cspapp.R;
import za.co.cspapp.adapters.StocksViewAdapter;
import za.co.cspapp.objects.DealerObject;
import za.co.cspapp.objects.StockObject;

public class StocksFragment extends Fragment {

    private static final String TAG = "StocksFragment";
    private static final String SHARED_PREFS = "MyDetails";
    private static final String API_STOCK = "https://iris.carsalesportal.co.za/api/dealerstock/";
    private ArrayList<DealerObject> dealers;
    private DealerObject current_dealer;
    private String state_id;
    private LinearLayoutManager lLayout;

    private Handler handler;

    ViewGroup container;

    OkHttpClient client = new OkHttpClient();

    Call doGetRequest(String url, Callback callback) throws IOException {

        SharedPreferences settings = getContext().getSharedPreferences(SHARED_PREFS, 0);
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

    public StocksFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle data = getArguments();
        dealers = (ArrayList<DealerObject>) data.getSerializable("dealers");
        current_dealer = (DealerObject) data.getSerializable("current_dealer");
        state_id = data.getString("state_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_stocks, null);
        return root;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        loadStock(view, current_dealer.getId(), current_dealer.getName(), state_id);
    }

    private void loadStock(final View view, int dealer_id, String dealer_name, String state_id) {
        handler = new Handler(Looper.getMainLooper());
        try {
            doGetRequest(API_STOCK+dealer_id+"/"+state_id, new Callback() {
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
                            final ArrayList<StockObject> stockList = new ArrayList<StockObject>();
                            JSONArray stocks = jData.getJSONArray("stocks");
                            for(int x = 0; x < stocks.length(); x++){
                                StockObject item = new StockObject(stocks.getJSONObject(x));
                                stockList.add(item);
                            }

                            final RecyclerView rView = view.findViewById(R.id.stock_rv);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    lLayout = new GridLayoutManager(getActivity(), 1);

                                    rView.setHasFixedSize(true);
                                    rView.setLayoutManager(lLayout);

                                    StocksViewAdapter rcAdapter = new StocksViewAdapter(getContext(), stockList, dealers, current_dealer);
                                    rView.setAdapter(rcAdapter);
                                }
                            });

                        } else {
                            String errorMsg = jData.getString("error_message");
//                        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
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
