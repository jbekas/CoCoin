package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jbekas.cocoin.adapter.CustomReportAdapter
import com.jbekas.cocoin.databinding.FragmentCustomReportBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomReportFragment : Fragment() {

    private var _binding: FragmentCustomReportBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    lateinit var customViewFragmentAdapter: CustomReportAdapter

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

        _binding = FragmentCustomReportBinding.inflate(inflater, container, false)

        customViewFragmentAdapter = CustomReportAdapter(activity!!)

        binding.customReportPager.offscreenPageLimit = 1
        binding.customReportPager.adapter = customViewFragmentAdapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}