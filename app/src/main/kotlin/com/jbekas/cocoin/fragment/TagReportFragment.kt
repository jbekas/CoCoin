package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.jbekas.cocoin.adapter.TagViewFragmentAdapter
import com.jbekas.cocoin.databinding.FragmentTagReportBinding
import com.jbekas.cocoin.model.RecordManager
import com.jbekas.cocoin.util.CoCoinUtil

class TagReportFragment : Fragment() {

    private var _binding: FragmentTagReportBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    lateinit var tagModeAdapter: TagViewFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity!! as AppCompatActivity).supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setHomeButtonEnabled(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTagReportBinding.inflate(inflater, container, false)

        tagModeAdapter = TagViewFragmentAdapter(activity!!)
        binding.tagReportPager.offscreenPageLimit = tagModeAdapter.itemCount
        binding.tagReportPager.adapter = tagModeAdapter

        TabLayoutMediator(binding.tabLayout, binding.tagReportPager) { tab, position ->
            tab.text = getPageTitle(position)
        }.attach()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun getPageTitle(position: Int): CharSequence? {
        return CoCoinUtil.GetTagName(
            RecordManager.TAGS[position % RecordManager.TAGS.size].id)
    }
}