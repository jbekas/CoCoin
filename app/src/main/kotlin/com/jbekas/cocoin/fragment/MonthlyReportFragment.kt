package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jbekas.cocoin.adapter.MonthViewFragmentAdapter
import com.jbekas.cocoin.databinding.FragmentMonthlyReportBinding
import com.jbekas.cocoin.util.CoCoinUtil
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MonthlyReportFragment : Fragment() {

    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var _binding: FragmentMonthlyReportBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        Timber.w("TODO: Fix MonthlyReportFragment to view more than current month.")

        _binding = FragmentMonthlyReportBinding.inflate(inflater, container, false)

        //val user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)

        val monthModeAdapter = MonthViewFragmentAdapter(this)
        binding.viewPager.offscreenPageLimit = monthModeAdapter.itemCount
        binding.viewPager.adapter = monthModeAdapter
        //mViewPager!!.pagerTitleStrip.setViewPager(mViewPager!!.viewPager)
        // TODO Find out if this is how months are switched.
//        if (monthModeAdapter!!.count == 1) {
//            mViewPager!!.pagerTitleStrip.visibility = View.INVISIBLE
//        }
//        mViewPager!!.setMaterialViewPagerListener { page ->
//            HeaderDesign.fromColorAndDrawable(
//                CoCoinUtil.GetTagColor(RecordManager.TAGS[page].id),
//                CoCoinUtil.GetTagDrawable(-3)
//            )
//        }
//        recyclerView = mDrawer!!.findViewById<View>(R.id.recycler_view) as RecyclerView
//        drawerMonthViewRecyclerViewAdapter = DrawerMonthViewRecyclerViewAdapter(coCoinUtil, object:
//            DrawerMonthViewRecyclerViewAdapter.OnItemClickListener {
//            mViewPager!!.viewPager.currentItem = position
//            val handler = Handler()
//            handler.postDelayed({ mDrawer!!.closeDrawers() }, 700)
//        })
//        recyclerView!!.adapter = drawerMonthViewRecyclerViewAdapter
//        recyclerView!!.setHasFixedSize(true)
//        recyclerView!!.layoutManager = LinearLayoutManager(context)
//        recyclerView!!.itemAnimator = DefaultItemAnimator()
//        profileImage = mDrawer!!.findViewById<View>(R.id.profile_image) as CircleImageView
//        profileImage!!.setOnClickListener {
//            if (SettingManager.getInstance().loggenOn) {
//                showToast(this@MonthlyReportFragment, R.string.change_logo_tip, null, null)
//            } else {
//                showToast(this@MonthlyReportFragment, R.string.login_tip, null, null)
//            }
//        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}