package com.jbekas.cocoin.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jbekas.cocoin.fragment.TagChooseFragment;
import com.jbekas.cocoin.model.RecordManager;

/**
 * Created by Weiping on 2016/1/19.
 */

public class TagChooseFragmentAdapter extends FragmentStateAdapter {

    private int count;

//    public TagChooseFragmentAdapter(FragmentManager fm, int count) {
//        super(fm);
//        this.count = count;
//    }

    public TagChooseFragmentAdapter(
            FragmentActivity activity,
            int count
    ) {
        super(activity);
        this.count = count;
    }
//    @Override
//    public Fragment getItem(int position) {
//        return TagChooseFragment.newInstance(position);
//    }

//    @Override
//    public int getCount() {
//        return count;
//    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return TagChooseFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return count;
    }
}
