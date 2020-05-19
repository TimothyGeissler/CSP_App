package za.co.cspapp;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import za.co.cspapp.fragments.StocksFragment;
import za.co.cspapp.objects.DealerObject;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public DealerObject current_dealer;
    public ArrayList<DealerObject> dealers;
    Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Bundle bundle = getIntent().getExtras();
        dealers = (ArrayList<DealerObject>) bundle.getSerializable("dealers");
        current_dealer = (DealerObject) bundle.getSerializable("current_dealer");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle("CSP : " + current_dealer.getName());

        initPage();

    }

    public void initPage(){
        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
//        for (int i = 0; i < tabLayout.getTabCount(); i++) {
//            tabLayout.getTabAt(i).setIcon(R.drawable.icon_stocklist);
//        }


    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new StocksFragment(), "UN_PUBLISHED", dealers, current_dealer, "1");
        adapter.addFragment(new StocksFragment(), "PUBLISHED", dealers, current_dealer, "2");
//        int defaultValue = 1;
//        int page = getIntent().getIntExtra("page", defaultValue);
        viewPager.setAdapter(adapter);
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

        public void addFragment(Fragment fragment, String title, ArrayList<DealerObject> dealers, DealerObject current_dealer, String state_id) {
            Bundle data = new Bundle();
            data.putSerializable("dealers", dealers);
            data.putSerializable("current_dealer", current_dealer);
            data.putString("state_id", state_id);
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
