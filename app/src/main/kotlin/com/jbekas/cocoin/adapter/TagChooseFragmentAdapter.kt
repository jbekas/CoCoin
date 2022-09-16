package com.jbekas.cocoin.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jbekas.cocoin.fragment.TagChooseFragment

class TagChooseFragmentAdapter(
    activity: FragmentActivity,
    private val tagSelectionListener: TagChooseFragment.OnTagItemSelectedListener,
    private val count: Int,
) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return TagChooseFragment.newInstance(tagSelectionListener, position)
    }

    override fun getItemCount(): Int {
        return count
    }
}