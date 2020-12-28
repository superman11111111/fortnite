package com.example.cringe;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cringe.audio.AudioInterceptorFrag;
import com.example.cringe.shell.ShellFrag;
import com.example.cringe.wifi.WifiP2PFrag;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "===MainActivity";
    private final String[] PAGE_TITLES = new String[]{
            "Audio recorder",
            "Wifi P2P",
            "Root shell"
    };

    Fragment[] PAGES = new Fragment[]{
            new AudioInterceptorFrag(),
            new WifiP2PFrag(),
            new ShellFrag(),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewPager2 viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), getLifecycle(), this));

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                getSupportActionBar().setTitle(PAGE_TITLES[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, true, (tab, position) -> {});
        tabLayoutMediator.attach();

    }

    public class MyPagerAdapter extends FragmentStateAdapter {

        final Context context;

        public MyPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle, Context context) {
            super(fragmentManager, lifecycle);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return PAGES[position];
        }

        @Override
        public int getItemCount() {
            return PAGES.length;
        }
    }

}