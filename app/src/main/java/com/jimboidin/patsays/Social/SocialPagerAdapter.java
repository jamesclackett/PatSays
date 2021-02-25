package com.jimboidin.patsays.Social;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SocialPagerAdapter  extends FragmentStateAdapter {

    public SocialPagerAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
        super(fm, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new FriendsFragment();
            case 1:
                return new InvitationsFragment();
            case 2:
                return new RecentPlayersFragment();
        }
        throw new IllegalArgumentException("no item");
    }

    @Override
    public int getItemCount() {
        return 3;
    }


}
