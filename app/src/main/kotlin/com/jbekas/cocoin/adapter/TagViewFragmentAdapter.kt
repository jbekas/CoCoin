package com.jbekas.cocoin.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jbekas.cocoin.fragment.TagViewFragment
import com.jbekas.cocoin.db.RecordManager

class TagViewFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return TagViewFragment.newInstance(position)
    }

    override fun getItemCount(): Int {
        return RecordManager.TAGS.size
    }
}