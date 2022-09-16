package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.florent37.materialviewpager.MaterialViewPagerHelper
import com.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter
import com.jbekas.cocoin.adapter.TagViewRecyclerViewAdapter
import com.jbekas.cocoin.databinding.FragmentTagViewBinding
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.RecordManager

//import com.squareup.leakcanary.RefWatcher;
/**
 * Created by 伟平 on 2015/10/20.
 */
class TagViewFragment : Fragment() {
    private var _binding: FragmentTagViewBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var position = 0
    private val list: MutableList<CoCoinRecord> = ArrayList()
    private var mAdapter: RecyclerView.Adapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = if (arguments != null) requireArguments().getInt("POSITION", 1) else 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTagViewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mRecyclerView = binding.recyclerView
        mRecyclerView.layoutManager = LinearLayoutManager(activity)
        mRecyclerView.setHasFixedSize(true)
        if (position == 0) {
            for (coCoinRecord in RecordManager.RECORDS) {
                list.add(coCoinRecord)
            }
        }
        if (position == 1) {
            for (coCoinRecord in RecordManager.RECORDS) {
                list.add(coCoinRecord)
            }
        } else {
            for (coCoinRecord in RecordManager.RECORDS) {
                if (coCoinRecord.tag == RecordManager.TAGS[position].id) {
                    list.add(coCoinRecord)
                }
            }
        }
        mAdapter = RecyclerViewMaterialAdapter(TagViewRecyclerViewAdapter(list, context, position))
        mRecyclerView.adapter = mAdapter
        MaterialViewPagerHelper.registerRecyclerView(activity, mRecyclerView, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        fun newInstance(position: Int): TagViewFragment {
            val fragment = TagViewFragment()
            val args = Bundle()
            args.putInt("POSITION", position)
            fragment.arguments = args
            return fragment
        }
    }
}