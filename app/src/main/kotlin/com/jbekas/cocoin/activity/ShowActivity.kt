package com.jbekas.cocoin.activity

import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import cn.bmob.v3.BmobUser
import cn.bmob.v3.listener.UpdateListener
import com.dev.sacot41.scviewpager.DotsView
import com.dev.sacot41.scviewpager.SCPositionAnimation
import com.dev.sacot41.scviewpager.SCViewAnimation
import com.dev.sacot41.scviewpager.SCViewAnimationUtil
import com.dev.sacot41.scviewpager.SCViewPager
import com.dev.sacot41.scviewpager.SCViewPagerAdapter
import com.jbekas.cocoin.R
import com.jbekas.cocoin.adapter.PasswordChangeButtonGridViewAdapter
import com.jbekas.cocoin.adapter.PasswordChangeFragmentAdapter
import com.jbekas.cocoin.databinding.ActivityShowBinding
import com.jbekas.cocoin.fragment.CoCoinFragmentManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.model.User
import com.jbekas.cocoin.service.ToastService
import com.jbekas.cocoin.util.CoCoinUtil
import dagger.hilt.android.AndroidEntryPoint
import lecho.lib.hellocharts.gesture.ContainerScrollType
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.model.SubcolumnValue
import lecho.lib.hellocharts.model.ValueShape
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ShowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowBinding

    @Inject
    lateinit var toastService: ToastService

    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var CURRENT_STATE = NEW_PASSWORD
    private var newPassword = ""
    private var againPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)

        binding = ActivityShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mViewPager: SCViewPager = binding.viewpagerMainActivity
        val mDotsView: DotsView = binding.dotsviewMain

        binding.title.text = resources.getString(R.string.app_name)

        mDotsView.setDotRessource(R.drawable.dot_selected, R.drawable.dot_unselected)
        mDotsView.setNumberOfPage(NUM_PAGES)

        val mPageAdapter = SCViewPagerAdapter(supportFragmentManager)
        mPageAdapter.setNumberOfPage(NUM_PAGES)
        mPageAdapter.setFragmentBackgroundColor(R.color.my_blue)

        mViewPager.adapter = mPageAdapter
        mViewPager.overScrollMode = View.OVER_SCROLL_NEVER
        mViewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
            }

            override fun onPageSelected(position: Int) {
                mDotsView.selectDot(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        val size = SCViewAnimationUtil.getDisplaySize(this)
        val iconOffsetX = coCoinUtil.dpToPx(28)
        val iconOffsetY = coCoinUtil.dpToPx(28)
        val sc0 = SCViewAnimation(binding.icon4)
        sc0.startToPosition(size.x / 4 - iconOffsetX, size.y * 2 / 7 - iconOffsetY)
        sc0.addPageAnimation(SCPositionAnimation(this, 0, 0, size.y))
        mViewPager.addAnimation(sc0)
        val sc1 = SCViewAnimation(binding.icon11)
        sc1.startToPosition(size.x * 3 / 4 - iconOffsetX, size.y * 3 / 7 - iconOffsetY)
        sc1.addPageAnimation(SCPositionAnimation(this, 0, -size.x, 0))
        mViewPager.addAnimation(sc1)
        val sc2 = SCViewAnimation(binding.icon12)
        sc2.startToPosition(size.x / 4 - iconOffsetX, size.y * 4 / 7 - iconOffsetY)
        sc2.addPageAnimation(SCPositionAnimation(this, 0, size.x, 0))
        mViewPager.addAnimation(sc2)
        val sc3 = SCViewAnimation(binding.icon19)
        sc3.startToPosition(size.x * 3 / 4 - iconOffsetX, size.y * 5 / 7 - iconOffsetY)
        sc3.addPageAnimation(SCPositionAnimation(this, 0, 0, -size.y))
        mViewPager.addAnimation(sc3)

        val sc4 = SCViewAnimation(binding.text0)
        sc4.addPageAnimation(SCPositionAnimation(this, 0, -size.x, 0))
        mViewPager.addAnimation(sc4)
        val pie = binding.pie
        val values: MutableList<SliceValue> = ArrayList()
        for (i in 0..4) {
            val sliceValue = SliceValue(Math.random().toFloat() * 30 + 15,
                ContextCompat.getColor(this, R.color.white))
            values.add(sliceValue)
        }
        val pieData = PieChartData(values)
        pieData.setHasLabels(false)
        pieData.setHasLabelsOnlyForSelected(false)
        pieData.setHasLabelsOutside(false)
        pieData.setHasCenterCircle(true)
        pie.pieChartData = pieData
        pie.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
        val sc5 = SCViewAnimation(pie)
        sc5.startToPosition(size.x / 2, size.y / 9 - size.y)
        sc5.addPageAnimation(SCPositionAnimation(this, 0, 0, size.y))
        sc5.addPageAnimation(SCPositionAnimation(this, 1, 0, size.y))
        mViewPager.addAnimation(sc5)
        val line = binding.line
        val lines: MutableList<Line> = ArrayList()
        for (i in 0..0) {
            val pointValues: MutableList<PointValue> = ArrayList()
            pointValues.add(PointValue(0f, 50f))
            pointValues.add(PointValue(1f, 100f))
            pointValues.add(PointValue(2f, 20f))
            pointValues.add(PointValue(3f, 0f))
            pointValues.add(PointValue(4f, 10f))
            pointValues.add(PointValue(5f, 15f))
            pointValues.add(PointValue(6f, 40f))
            pointValues.add(PointValue(7f, 60f))
            pointValues.add(PointValue(8f, 100f))
            val aLine = Line(pointValues)
            aLine.color = ContextCompat.getColor(this, R.color.white)
            aLine.shape = ValueShape.CIRCLE
            aLine.isCubic = false
            aLine.isFilled = false
            aLine.setHasLabels(false)
            aLine.setHasLabelsOnlyForSelected(false)
            aLine.setHasLines(true)
            aLine.setHasPoints(true)
            lines.add(aLine)
        }
        val linedata = LineChartData(lines)
        linedata.baseValue = Float.NEGATIVE_INFINITY
        line.lineChartData = linedata
        line.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
        val sc6 = SCViewAnimation(line)
        sc6.startToPosition(-size.x, null)
        sc6.addPageAnimation(SCPositionAnimation(this, 0, size.x, 0))
        sc6.addPageAnimation(SCPositionAnimation(this, 1, size.x, 0))
        mViewPager.addAnimation(sc6)
        val histogram = binding.histogram
        val columns: MutableList<Column> = ArrayList()
        var subcolumnValues: MutableList<SubcolumnValue?>
        for (i in 0..4) {
            subcolumnValues = ArrayList()
            for (j in 0..0) {
                subcolumnValues.add(SubcolumnValue(Math.random().toFloat() * 50f + 5,
                    ContextCompat.getColor(this, R.color.white)))
            }
            val column = Column(subcolumnValues)
            column.setHasLabels(false)
            column.setHasLabelsOnlyForSelected(false)
            columns.add(column)
        }
        val histogramData = ColumnChartData(columns)
        histogram.columnChartData = histogramData
        histogram.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
        val sc7 = SCViewAnimation(histogram)
        sc7.startToPosition(size.x / 2 - coCoinUtil.dpToPx(140),
            size.y * 8 / 9 - coCoinUtil.dpToPx(140) + size.y)
        sc7.addPageAnimation(SCPositionAnimation(this, 0, 0, -size.y))
        sc7.addPageAnimation(SCPositionAnimation(this, 1, 0, size.y))
        mViewPager.addAnimation(sc7)

        val sc8 = SCViewAnimation(binding.text1)
        sc8.startToPosition(size.x, null)
        sc8.addPageAnimation(SCPositionAnimation(this, 0, -size.x, 0))
        sc8.addPageAnimation(SCPositionAnimation(this, 1, -size.x, 0))
        mViewPager.addAnimation(sc8)
        val sc9 = SCViewAnimation(binding.cloud)
        sc9.startToPosition(size.x / 2 - coCoinUtil.dpToPx(100) + size.x, size.y / 7)
        sc9.addPageAnimation(SCPositionAnimation(this, 1, -size.x, 0))
        sc9.addPageAnimation(SCPositionAnimation(this, 2, 0, size.y))
        mViewPager.addAnimation(sc9)
        val sc10 = SCViewAnimation(binding.mobile)
        sc10.startToPosition(size.x / 2 - size.x, size.y * 6 / 7 - coCoinUtil.dpToPx(100))
        sc10.addPageAnimation(SCPositionAnimation(this, 1, size.x, 0))
        sc10.addPageAnimation(SCPositionAnimation(this, 2, 0, -size.y))
        mViewPager.addAnimation(sc10)

        val sc11 = SCViewAnimation(binding.text2)
        sc11.startToPosition(size.x, null)
        sc11.addPageAnimation(SCPositionAnimation(this, 1, -size.x, 0))
        sc11.addPageAnimation(SCPositionAnimation(this, 2, -size.x, 0))
        mViewPager.addAnimation(sc11)
        val remind1 = binding.remind1
        remind1.layoutParams.width = size.x / 3
        remind1.layoutParams.height = size.x / 3 * 653 / 320
        val sc12 = SCViewAnimation(remind1)
        sc12.startToPosition(size.x / 2 - size.x, size.y / 11)
        sc12.addPageAnimation(SCPositionAnimation(this, 2, size.x, 0))
        sc12.addPageAnimation(SCPositionAnimation(this, 3, size.x, 0))
        mViewPager.addAnimation(sc12)
        val remind2 = binding.remind2
        remind2.layoutParams.width = size.x / 3
        remind2.layoutParams.height = size.x / 3 * 653 / 320
        val sc13 = SCViewAnimation(remind2)
        sc13.startToPosition(size.x / 2 + size.x - size.x / 3,
            size.y * 10 / 11 - remind1.layoutParams.height)
        sc13.addPageAnimation(SCPositionAnimation(this, 2, -size.x, 0))
        sc13.addPageAnimation(SCPositionAnimation(this, 3, -size.x, 0))
        mViewPager.addAnimation(sc13)

        val sc14 = SCViewAnimation(binding.text3)
        sc14.startToPosition(size.x, null)
        sc14.addPageAnimation(SCPositionAnimation(this, 2, -size.x, 0))
        sc14.addPageAnimation(SCPositionAnimation(this, 3, -size.x, 0))
        mViewPager.addAnimation(sc14)
        val statusBar = binding.statusBar
        statusBar.layoutParams =
            RelativeLayout.LayoutParams(statusBar.layoutParams.width, statusBarHeight)
        val statusBarAnimation = SCViewAnimation(statusBar)
        statusBarAnimation.startToPosition(null, -statusBarHeight)
        statusBarAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, statusBarHeight))
        mViewPager.addAnimation(statusBarAnimation)

        val toolbarLayoutAnimation = SCViewAnimation(binding.toolbarLayout)
        toolbarLayoutAnimation.startToPosition(null, -size.y / 2)
        toolbarLayoutAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, size.y / 2))
        mViewPager.addAnimation(toolbarLayoutAnimation)

        val passwordAdapter = PasswordChangeFragmentAdapter(supportFragmentManager)

        binding.viewpager.scrollBarFadeDuration = 700
        binding.viewpager.adapter = passwordAdapter

        binding.gridview.adapter = PasswordChangeButtonGridViewAdapter(this, coCoinUtil)
        binding.gridview.onItemClickListener = gridViewClickListener
        binding.gridview.onItemLongClickListener = gridViewLongClickListener
        binding.gridview.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.gridview.viewTreeObserver.removeGlobalOnLayoutListener(this)
                val lastChild = binding.gridview.getChildAt(binding.gridview.childCount - 1)
                val relativeLayout = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    lastChild.bottom)
                relativeLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                binding.gridview.layoutParams = relativeLayout
                val displaymetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displaymetrics)
                val height = displaymetrics.heightPixels
                val viewPagerLayoutParams =
                    RelativeLayout.LayoutParams(binding.viewpager.layoutParams.width, 800)
                viewPagerLayoutParams.topMargin =
                    statusBarHeight + coCoinUtil.getToolBarHeight(this@ShowActivity) / 2
                binding.viewpager.layoutParams = viewPagerLayoutParams
            }
        })
        val gridViewAnimation = SCViewAnimation(binding.gridview)
        gridViewAnimation.startToPosition(null, size.y)
        gridViewAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, -size.y))
        mViewPager.addAnimation(gridViewAnimation)
        val viewpagerAnimation = SCViewAnimation(binding.viewpager)
        viewpagerAnimation.startToPosition(null, -size.y)
        viewpagerAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, size.y))
        mViewPager.addAnimation(viewpagerAnimation)
        val background = binding.background
        val backgroundAnimation = SCViewAnimation(background)
        backgroundAnimation.startToPosition(null, -size.y - 100)
        backgroundAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, size.y + 100))
        mViewPager.addAnimation(backgroundAnimation)
    }

    override fun onBackPressed() {}

    private val gridViewClickListener =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            buttonClickOperation(false, position)
        }
    private val gridViewLongClickListener = OnItemLongClickListener { parent, view, position, id ->
        buttonClickOperation(true, position)
        true
    }

    private fun buttonClickOperation(longClick: Boolean, position: Int) {
        when (CURRENT_STATE) {
            NEW_PASSWORD -> if (coCoinUtil.clickButtonDelete(position)) {
                if (longClick) {
                    CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].init()
                    newPassword = ""
                } else {
                    CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].clear(newPassword.length - 1)
                    if (newPassword.length != 0) newPassword =
                        newPassword.substring(0, newPassword.length - 1)
                }
            } else if (coCoinUtil.clickButtonCommit(position)) {
            } else {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].set(newPassword.length)
                newPassword += coCoinUtil.BUTTONS[position]
                if (newPassword.length == 4) {
                    // finish the new password input
                    CURRENT_STATE = PASSWORD_AGAIN
                    binding.viewpager.setCurrentItem(PASSWORD_AGAIN, true)
                }
            }
            PASSWORD_AGAIN -> if (coCoinUtil.clickButtonDelete(position)) {
                if (longClick) {
                    CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].init()
                    againPassword = ""
                } else {
                    CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].clear(againPassword.length - 1)
                    if (againPassword.length != 0) againPassword =
                        againPassword.substring(0, againPassword.length - 1)
                }
            } else if (coCoinUtil.clickButtonCommit(position)) {
            } else {
                CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].set(againPassword.length)
                againPassword += coCoinUtil.BUTTONS[position]
                if (againPassword.length == 4) {
                    // if the password again is equal to the new password
                    if (againPassword == newPassword) {
                        CURRENT_STATE = -1
                        showToast(2)
                        SettingManager.getInstance().password = newPassword
                        SettingManager.getInstance().firstTime = false
                        if (SettingManager.getInstance().loggenOn) {
                            val currentUser = BmobUser.getCurrentUser(this, User::class.java)
                            currentUser.accountBookPassword = newPassword
                            currentUser.update(this,
                                currentUser.objectId,
                                object : UpdateListener() {
                                    override fun onSuccess() {
                                        Timber.d("Set password successfully.")
                                    }

                                    override fun onFailure(code: Int, msg: String) {
                                        Timber.d("Set password failed.")
                                    }
                                })
                        }
                        val handler = Handler()
                        handler.postDelayed({ finish() }, 1000)
                    } else {
                        CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE].clear(4)
                        CoCoinFragmentManager.passwordChangeFragment[CURRENT_STATE - 1].init()
                        CURRENT_STATE = NEW_PASSWORD
                        binding.viewpager.setCurrentItem(NEW_PASSWORD, true)
                        newPassword = ""
                        againPassword = ""
                        showToast(1)
                    }
                }
            }
            else -> {}
        }
    }

    private fun showToast(toastType: Int) {
        when (toastType) {
            0 -> {
                var text = resources.getString(R.string.toast_password_wrong)
                toastService.showErrorToast(text)
            }
            1 -> {
                var text = resources.getString(R.string.different_password)
                toastService.showErrorToast(text)
            }
            2 -> {
                var text = resources.getString(R.string.set_password_successfully)
                toastService.showSuccessToast(text)
            }
        }
    }

    override fun onDestroy() {
        for (i in 0..2) {
            CoCoinFragmentManager.passwordChangeFragment[i].onDestroy()
            CoCoinFragmentManager.passwordChangeFragment[i] = null
        }
        super.onDestroy()
    }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    companion object {
        private const val NUM_PAGES = 5
        private const val NEW_PASSWORD = 0
        private const val PASSWORD_AGAIN = 1
    }
}