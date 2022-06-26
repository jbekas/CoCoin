package com.jbekas.cocoin.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.jbekas.cocoin.R;
import com.jbekas.cocoin.activity.CoCoinApplication;
import com.jbekas.cocoin.fragment.HelpAboutFragment;
import com.jbekas.cocoin.fragment.HelpCoCoinFragment;
import com.jbekas.cocoin.fragment.HelpFeedbackFragment;

/**
 * Created by Weiping on 2016/2/2.
 */

public class HelpFragmentAdapter extends FragmentStatePagerAdapter {

    private int position = 0;

    public HelpFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public HelpFragmentAdapter(FragmentManager fm, int position) {
        super(fm);
        this.position = position;
    }

    @Override
    public Fragment getItem(int position) {
        switch (this.position) {
            case 0: return HelpCoCoinFragment.newInstance();
            case 1: return HelpFeedbackFragment.newInstance();
            case 2: return HelpAboutFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (this.position) {
            case 0: return CoCoinApplication.getAppContext().getResources().getString(R.string.app_name);
            case 1: return CoCoinApplication.getAppContext().getResources().getString(R.string.feedback);
            case 2: return CoCoinApplication.getAppContext().getResources().getString(R.string.about);
        }
        return "";
    }
}
