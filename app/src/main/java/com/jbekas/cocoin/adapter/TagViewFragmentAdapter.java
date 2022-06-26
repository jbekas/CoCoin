package com.jbekas.cocoin.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.jbekas.cocoin.activity.CoCoinApplication;
import com.jbekas.cocoin.fragment.TagViewFragment;
import com.jbekas.cocoin.model.RecordManager;
import com.jbekas.cocoin.util.CoCoinUtil;

/**
 * Created by 伟平 on 2015/10/20.
 */
public class TagViewFragmentAdapter extends FragmentStatePagerAdapter {

    public TagViewFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return TagViewFragment.newInstance(i);
    }

    @Override
    public int getCount() {
        return RecordManager.getInstance(CoCoinApplication.getAppContext()).TAGS.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return CoCoinUtil.GetTagName(
                RecordManager.getInstance(CoCoinApplication.getAppContext()).TAGS.get(position % RecordManager.TAGS.size()).getId());
    }
}
