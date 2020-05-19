package za.co.cspapp.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import za.co.cspapp.R;
import za.co.cspapp.SearchActivity;
import za.co.cspapp.objects.ColourObject;
import za.co.cspapp.objects.DealerObject;
import za.co.cspapp.objects.StateObject;
import za.co.cspapp.objects.StockObject;


public class StockEditFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    private static final String SHARED_PREFS = "MyDetails";
    private static final String STOCK_UPDATE_URL = "https://iris.carsalesportal.co.za/api/stock/update/";
    private static final String COLOUR_URL = "https://iris.carsalesportal.co.za/api/colours";
    private DealerObject current_dealer;
    private ArrayList<DealerObject> dealers;
    private StockObject stock;
    private Handler handler;

    ViewGroup container;

    EditText mileage;
    EditText year;
    EditText stockNo;
    EditText price;
    EditText costPrice;
    EditText vin;
    EditText reg;
    Spinner colour;
    Spinner state;
    List<ColourObject> colours = new ArrayList<>();
    List<String> cnames = new ArrayList<>();
    List<StateObject> states = new ArrayList<>();
    List<String> snames = new ArrayList<>();

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

    Call doPostRequest(String url, StockObject stock, Callback callback) throws IOException {
        SharedPreferences settings = getContext().getSharedPreferences(SHARED_PREFS, 0);
        String api_token = settings.getString("api_token", "0");

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", String.valueOf(stock.getId()))
                .addFormDataPart("mileage", String.valueOf(stock.getMileage()))
                .addFormDataPart("stock_num", stock.getStockNum())
                .addFormDataPart("year", String.valueOf(stock.getYear()))
                .addFormDataPart("price", String.valueOf(stock.getPrice()))
                .addFormDataPart("cost_price", String.valueOf(stock.getCostPrice()))
                .addFormDataPart("vin", stock.getVin())
                .addFormDataPart("reg_num", stock.getRegNo())
                .addFormDataPart("state", String.valueOf(stock.getState()))
                .addFormDataPart("colour", String.valueOf(stock.getColour()))
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", api_token)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public StockEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            stock = (StockObject) bundle.getSerializable("stock");
            dealers = (ArrayList<DealerObject>) bundle.getSerializable("dealers");
            current_dealer = (DealerObject) bundle.getSerializable("current_dealer");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stock_edit, container, false);

        getColours();

        TextView vehicle = root.findViewById(R.id.txt_vehicle);
        mileage = root.findViewById(R.id.inp_mileage);
        year = root.findViewById(R.id.inp_year);
        stockNo = root.findViewById(R.id.inp_stockNo);
        price = root.findViewById(R.id.inp_price);
        costPrice = root.findViewById(R.id.inp_costPrice);
        vin = root.findViewById(R.id.inp_vin);
        reg = root.findViewById(R.id.inp_reg);
        Button save = root.findViewById(R.id.btn_save);
        Button cancel = root.findViewById(R.id.btn_cancel);

        colour = root.findViewById(R.id.sel_colour);
        colour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ColourObject selected = colours.get(pos);
                stock.setColour(selected.getId()-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView)
            {

            }
        });

        state = root.findViewById(R.id.sel_published);
        StateObject state1 = new StateObject(1, "Un-Published");
        states.add(state1);
        StateObject state2 = new StateObject(2, "Published");
        states.add(state2);
        StateObject state3 = new StateObject(3, "Sold");
        states.add(state3);

        snames.add(state1.getState());
        snames.add(state2.getState());
        snames.add(state3.getState());
        // Creating adapter for spinner
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, snames);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state.setAdapter(stateAdapter);
        state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                StateObject selected = states.get(pos);
                stock.setState(selected.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView)
            {

            }
        });

        mileage.setText(Integer.toString(stock.getMileage()));
        year.setText(Integer.toString(stock.getYear()));
        stockNo.setText(stock.getStockNum());
        price.setText(Integer.toString(stock.getPrice()));
        costPrice.setText(Integer.toString(stock.getCostPrice()));
        state.setSelection(stock.getState()-1);
        vin.setText(stock.getVin());
        reg.setText(stock.getRegNo());
        vehicle.setText(stock.getTrim());

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });

        return root;

    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    public void save(){
        stock.setMileage(Integer.parseInt(mileage.getText().toString()));
        stock.setYear(Integer.parseInt(year.getText().toString()));
        stock.setStockNum(stockNo.getText().toString());
        stock.setPrice(Integer.parseInt(price.getText().toString()));
        stock.setCostPrice(Integer.parseInt(costPrice.getText().toString()));
        stock.setVin(vin.getText().toString());
        stock.setRegNo(reg.getText().toString());


        try {
            doPostRequest(STOCK_UPDATE_URL+stock.getId(), stock, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    try {
                        String responseStr = response.body().string();
                        JSONObject jObj = new JSONObject(responseStr);
                        boolean error = jObj.getBoolean("error");

                        if (!error) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("dealers", dealers);
                            bundle.putSerializable("current_dealer", current_dealer);
                            Intent intent = new Intent(getContext(), SearchActivity.class);
                            intent.putExtras(bundle);
                            intent.putExtra("reload", true);
                            getContext().startActivity(intent);
                        } else {
                            String errorMsg = jObj.getString("error_message");
    //                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
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

    public void getColours(){
        handler = new Handler(Looper.getMainLooper());

        try {
            doGetRequest(COLOUR_URL, new Callback() {
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
                            JSONArray cols = jData.getJSONArray("colours");
                            cnames.add("Select....");
                            for(int x = 0; x < cols.length(); x++){
                                ColourObject colour = new ColourObject(cols.getJSONObject(x));
                                colours.add(colour);
                                cnames.add(colour.getColour());
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter<String> colourAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, cnames);
                                    colourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    colour.setAdapter(colourAdapter);
                                    colour.setSelection(colourAdapter.getPosition(colours.get(stock.getColour()).getColour()) - 1);
                                }
                            });
                        } else {
                            String errorMsg = jObj.getString("error_message");
//                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
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
