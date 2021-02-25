package com.jimboidin.patsays.Social;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jimboidin.patsays.R;

public class SocialActivity extends AppCompatActivity implements InvitationsFragment.LeaveSocialListener {
    private final String TAG = "SocialActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        SocialPagerAdapter socialPagerAdapter = new SocialPagerAdapter(getSupportFragmentManager(), getLifecycle());
        ViewPager2 pager = findViewById(R.id.social_pager);
        pager.setAdapter(socialPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.social_tabs);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout,
                pager, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText("Friends");
                        break;
                    case 1:
                        tab.setText("Invitations");
                        break;
                    case 2:
                        tab.setText("Recent Players");
                        break;
                    default:
                        tab.setText("");
                }
            }
        });
        tabLayoutMediator.attach();
    }


    @Override
    public void onLeave() {
        finish();
    }
}