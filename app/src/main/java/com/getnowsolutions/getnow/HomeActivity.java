package com.getnowsolutions.getnow;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.getnowsolutions.getnow.fragments.DealsFragment;
import com.getnowsolutions.getnow.fragments.MapFragment;
import com.getnowsolutions.getnow.fragments.PayFragment;
import com.getnowsolutions.getnow.fragments.ScheduleFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.navigation_drawer_top)
    NavigationView navigationDrawerTop;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.tabs)
    TabLayout tabs;
    @BindView(R.id.container)
    ViewPager container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationDrawerTop.setNavigationItemSelectedListener(this);
        container.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        tabs.setupWithViewPager(container);
        container.setCurrentItem(0, true);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0: {
                    MapFragment mapFragment = new MapFragment();
                    return mapFragment;
                }
                case 1: {
                    ScheduleFragment scheduleFragment = new ScheduleFragment();
                    return scheduleFragment;
                }
                case 2: {
                    PayFragment payFragment = new PayFragment();
                    return payFragment;
                }
                case 3: {
                    DealsFragment dealsFragment = new DealsFragment();
                    return dealsFragment;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Find Route";
                case 1:
                    return "Schedule";
                case 2:
                    return "Pay";
                case 3:
                    return "Deals";
            }
            return null;
        }
    }

}
