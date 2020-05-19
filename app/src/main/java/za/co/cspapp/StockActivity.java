package za.co.cspapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import za.co.cspapp.fragments.PhotoFragment;
import za.co.cspapp.fragments.StockEditFragment;
import za.co.cspapp.objects.DealerObject;
import za.co.cspapp.objects.PhotoObject;
import za.co.cspapp.objects.StockObject;

public class StockActivity extends AppCompatActivity {
    private static final String TAG = "StockActivity";
    private static final String SHARED_PREFS = "MyDetails";
    private static final String API_STOCK = "https://iris.carsalesportal.co.za/api/stock/";

    private TabLayout tabLayout;
    private ViewPager viewPager;
    public StockObject stock;
    public int stock_id;
    public DealerObject current_dealer;
    public ArrayList<DealerObject> dealers;
    public ArrayList<PhotoObject> photos;
    Menu menu;
    private Handler handler;

    OkHttpClient client = new OkHttpClient();

    Call doGetRequest(String url, Callback callback) throws IOException {
//        Looper.prepare();

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        Bundle bundle = getIntent().getExtras();
        dealers = (ArrayList<DealerObject>) bundle.getSerializable("dealers");
        current_dealer = (DealerObject) bundle.getSerializable("current_dealer");
        stock_id = bundle.getInt("stock_id");
        stock = (StockObject) bundle.getSerializable("stock");
        Boolean reload = bundle.getBoolean("reload");

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try{
            if(!reload){
                photos = stock.getPhotos();
                initPage();
            } else {
                getStock(stock_id);
            }
        } catch (NullPointerException e){
            getStock(stock_id);
        }

    }


    public void initPage(){
        setTitle("CSP - Stock No.: " + stock.getStockNum() + " : " + "R" + NumberFormat.getNumberInstance(Locale.UK).format(stock.getPrice()));
        viewPager = findViewById(R.id.stock_vp);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.stock_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void getStock(int stock_id) {
        handler = new Handler(Looper.getMainLooper());

        try {
            doGetRequest(API_STOCK+stock_id, new Callback() {
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
                            stock = new StockObject(jData);
                            photos = stock.getPhotos();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    initPage();
                                }
                            });
                        } else {
                            String errorMsg = jData.getString("error_message");
    //                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addStockFragment(new StockEditFragment(), "EDIT", dealers, current_dealer, stock);
        adapter.addPhotoFragment(new PhotoFragment(), "PHOTOS", dealers, current_dealer, stock);
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Bundle data = new Bundle();
                data.putSerializable("dealers", dealers);
                data.putSerializable("current_dealer", current_dealer);
                Intent upIntent = new Intent(this, IntroActivity.class);
                upIntent.putExtras(data);
                startActivity(upIntent);
                return true;
            default:
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
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();
        getData(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void getData(Menu menu) {
        for (int i = 0; i < dealers.size(); i++) {
            menu.add(0, dealers.get(i).getId(), 0, dealers.get(i).getName());
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private static final String TAG = "ViewPagerActivity";

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addPhotoFragment(Fragment fragment, String title, ArrayList<DealerObject> dealers, DealerObject current_dealer, StockObject stock) {
            Bundle data = new Bundle();
            data.putSerializable("stock", stock);
            data.putSerializable("dealers", dealers);
            data.putSerializable("current_dealer", current_dealer);
            fragment.setArguments(data);
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        public void addStockFragment(Fragment fragment, String title, ArrayList<DealerObject> dealers, DealerObject current_dealer, StockObject stock) {
            Bundle data = new Bundle();
            data.putSerializable("stock", stock);
            data.putSerializable("dealers", dealers);
            data.putSerializable("current_dealer", current_dealer);
            fragment.setArguments(data);
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
