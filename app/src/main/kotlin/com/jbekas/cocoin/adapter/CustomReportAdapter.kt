package com.jbekas.cocoin.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jbekas.cocoin.fragment.CustomViewFragment

class CustomReportAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return CustomViewFragment.newInstance()
    }

    override fun getItemCount(): Int {
        return 1
    }
}