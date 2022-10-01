package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jbekas.cocoin.adapter.TagViewRecyclerViewAdapter
import com.jbekas.cocoin.databinding.FragmentTagViewBinding
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.util.CoCoinUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//import com.squareup.leakcanary.RefWatcher;
@AndroidEntryPoint
class TagViewFragment : Fragment() {

    @Inject
    lateinit var coCoinUtil: CoCoinUtil

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
        mAdapter =
            TagViewRecyclerViewAdapter(
                context = requireContext(),
                activity = requireActivity(),
                coCoinUtil = coCoinUtil,
                coCoinRecords = list,
                fragmentPosition = position
            )
        mRecyclerView.adapter = mAdapter
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