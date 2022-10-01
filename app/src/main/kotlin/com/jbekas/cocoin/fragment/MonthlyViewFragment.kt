package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jbekas.cocoin.adapter.MonthViewRecyclerViewAdapter
import com.jbekas.cocoin.databinding.FragmentMonthlyViewBinding
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.util.CoCoinUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

//import com.squareup.leakcanary.RefWatcher;

@AndroidEntryPoint
class MonthlyViewFragment : Fragment() {
    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var _binding: FragmentMonthlyViewBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    var position = 0
    var monthNumber = 0
//    private val list: List<CoCoinRecord> = ArrayList()

    //    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var IS_EMPTY = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = if (arguments != null) requireArguments().getInt("POSITION", 1) else 1
        monthNumber = if (arguments != null) requireArguments().getInt("MONTH_NUMBER", 1) else 1
        IS_EMPTY = monthNumber == -1

        //Timber.d("position: $position, monthNumber: $monthNumber, IS_EMPTY: $IS_EMPTY")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMonthlyViewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mRecyclerView = binding.recyclerView
//        mRecyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView
//        val layoutManager: RecyclerView.LayoutManager =
        mRecyclerView.layoutManager = LinearLayoutManager(activity)
        mRecyclerView.setHasFixedSize(true)
        if (!IS_EMPTY) {
            val recordManager = RecordManager.getInstance(requireContext().applicationContext)
            val startYear = RecordManager.RECORDS[0].calendar[Calendar.YEAR]
            val startMonth = RecordManager.RECORDS[0].calendar[Calendar.MONTH]
            val nowYear = startYear + (startMonth + (monthNumber - position - 1)) / 12
            val nowMonth = (startMonth + (monthNumber - position - 1)) % 12
            val monthStart = Calendar.getInstance()
            val monthEnd = Calendar.getInstance()
            monthStart[nowYear, nowMonth, 1, 0, 0] = 0
            monthStart.add(Calendar.MILLISECOND, 0)
            monthEnd[nowYear, nowMonth, monthStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59] =
                59
            monthEnd.add(Calendar.MILLISECOND, 0)
            val leftRange = coCoinUtil.getThisWeekLeftRange(monthStart)
            val rightRange = coCoinUtil.getThisWeekRightRange(monthEnd)
            var start = -1
            var end = 0
            for (i in RecordManager.RECORDS.indices.reversed()) {
                if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                    end = i + 1
                    break
                } else if (RecordManager.RECORDS[i].calendar.before(rightRange)) {
                    if (start == -1) {
                        start = i
                    }
                }
            }
            mAdapter =
                MonthViewRecyclerViewAdapter(
                    activity = requireActivity(),
                    coCoinUtil = coCoinUtil,
                    start = start,
                    end = end,
                    position = position,
                    monthNumber = monthNumber
                )
            mRecyclerView.adapter = mAdapter
        } else {
            mAdapter =
                MonthViewRecyclerViewAdapter(
                    activity = requireActivity(),
                    coCoinUtil = coCoinUtil,
                    start = -1,
                    end = -1,
                    position = 0,
                    monthNumber = -1
                )
            mRecyclerView.adapter = mAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int, monthNumber: Int): MonthlyViewFragment {
            val fragment = MonthlyViewFragment()
            val args = Bundle()
            args.putInt("POSITION", position)
            args.putInt("MONTH_NUMBER", monthNumber)
            fragment.arguments = args
            fragment.monthNumber = monthNumber
            fragment.position = position
            return fragment
        }
    }
}