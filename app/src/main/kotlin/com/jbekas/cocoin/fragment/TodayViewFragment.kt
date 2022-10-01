package com.jbekas.cocoin.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jbekas.cocoin.R
import com.jbekas.cocoin.adapter.TodayViewRecyclerViewAdapter
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.getThisMonthLeftRange
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject

//import com.squareup.leakcanary.RefWatcher;
@AndroidEntryPoint
class TodayViewFragment : Fragment() {
    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var position = 0
    private var mContext: Context? = null
    private var mRecyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = context
        position = if (arguments != null) requireArguments().getInt("POSITION") else 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.today_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView
        layoutManager = LinearLayoutManager(activity)
        mRecyclerView!!.layoutManager = layoutManager
        mRecyclerView!!.setHasFixedSize(true)
        val now = Calendar.getInstance()
        val leftRange: Calendar
        val rightRange: Calendar
        var start = -1
        var end = 0
        when (position) {
            TODAY -> {
                leftRange = coCoinUtil.getTodayLeftRange(now)
                var i = RecordManager.RECORDS.size - 1
                while (i >= 0) {
                    if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                        end = i + 1
                        break
                    }
                    if (start == -1) {
                        start = i
                    }
                    i--
                }
            }
            YESTERDAY -> {
                leftRange = coCoinUtil.getYesterdayLeftRange(now)
                rightRange = coCoinUtil.getYesterdayRightRange(now)
                var i = RecordManager.RECORDS.size - 1
                while (i >= 0) {
                    if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                        end = i + 1
                        break
                    } else if (!RecordManager.RECORDS[i].calendar.after(rightRange)) {
                        if (start == -1) {
                            start = i
                        }
                    }
                    i--
                }
            }
            THIS_WEEK -> {
                leftRange = coCoinUtil.getThisWeekLeftRange(now)
                var i = RecordManager.RECORDS.size - 1
                while (i >= 0) {
                    if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                        end = i + 1
                        break
                    }
                    if (start == -1) {
                        start = i
                    }
                    i--
                }
            }
            LAST_WEEK -> {
                leftRange = coCoinUtil.getLastWeekLeftRange(now)
                rightRange = coCoinUtil.getLastWeekRightRange(now)
                var i = RecordManager.RECORDS.size - 1
                while (i >= 0) {
                    if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                        end = i + 1
                        break
                    } else if (RecordManager.RECORDS[i].calendar.before(rightRange)) {
                        if (start == -1) {
                            start = i
                        }
                    }
                    i--
                }
            }
            THIS_MONTH -> {
                leftRange = now.getThisMonthLeftRange() //coCoinUtil.GetThisMonthLeftRange(now);
                Timber.d("leftRange: $leftRange")
                var i = RecordManager.RECORDS.size - 1
                while (i >= 0) {
                    if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                        end = i + 1
                        break
                    }
                    if (start == -1) {
                        start = i
                    }
                    i--
                }
            }
            LAST_MONTH -> {
                leftRange = coCoinUtil.getLastMonthLeftRange(now)
                rightRange = coCoinUtil.getLastMonthRightRange(now)
                var i = RecordManager.RECORDS.size - 1
                while (i >= 0) {
                    if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                        end = i + 1
                        break
                    } else if (RecordManager.RECORDS[i].calendar.before(rightRange)) {
                        if (start == -1) {
                            start = i
                        }
                    }
                    i--
                }
            }
            THIS_YEAR -> {
                leftRange = coCoinUtil.getThisYearLeftRange(now)
                var i = RecordManager.RECORDS.size - 1
                while (i >= 0) {
                    if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                        end = i + 1
                        break
                    }
                    if (start == -1) {
                        start = i
                    }
                    i--
                }
            }
            LAST_YEAR -> {
                leftRange = coCoinUtil.getLastYearLeftRange(now)
                rightRange = coCoinUtil.getLastYearRightRange(now)
                var i = RecordManager.RECORDS.size - 1
                while (i >= 0) {
                    if (RecordManager.RECORDS[i].calendar.before(leftRange)) {
                        end = i + 1
                        break
                    } else if (RecordManager.RECORDS[i].calendar.before(rightRange)) {
                        if (start == -1) {
                            start = i
                        }
                    }
                    i--
                }
            }
        }
        val adapter =
            TodayViewRecyclerViewAdapter(requireActivity(), coCoinUtil, start, end, position)
        mRecyclerView!!.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()

//        RefWatcher refWatcher = CoCoinApplication.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    companion object {
        const val TODAY = 0
        const val YESTERDAY = 1
        const val THIS_WEEK = 2
        const val LAST_WEEK = 3
        const val THIS_MONTH = 4
        const val LAST_MONTH = 5
        const val THIS_YEAR = 6
        const val LAST_YEAR = 7

        fun newInstance(position: Int): TodayViewFragment {
            val fragment = TodayViewFragment()
            val args = Bundle()
            args.putInt("POSITION", position)
            fragment.arguments = args
            return fragment
        }
    }
}