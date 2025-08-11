package com.jbekas.cocoin.activity

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager

import com.dev.sacot41.scviewpager.DotsView
import com.dev.sacot41.scviewpager.SCPositionAnimation
import com.dev.sacot41.scviewpager.SCViewAnimation
import com.dev.sacot41.scviewpager.SCViewAnimationUtil
import com.dev.sacot41.scviewpager.SCViewPager
import com.dev.sacot41.scviewpager.SCViewPagerAdapter
import com.github.johnpersano.supertoasts.SuperToast
import com.jbekas.cocoin.R
import com.jbekas.cocoin.adapter.PasswordChangeButtonGridViewAdapter
import com.jbekas.cocoin.adapter.PasswordChangeFragmentAdapter
import com.jbekas.cocoin.fragment.CoCoinFragmentManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.model.User
import com.jbekas.cocoin.ui.MyGridView
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.ToastUtil

import com.jbekas.cocoin.databinding.ActivityShowBinding
import cn.bmob.v3.BmobUser
import cn.bmob.v3.listener.UpdateListener
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
import lecho.lib.hellocharts.view.ColumnChartView
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.view.PieChartView
import timber.log.Timber

class ShowActivity : AppCompatActivity() {

    companion object {
        private const val NUM_PAGES = 5
        private const val NEW_PASSWORD = 0
        private const val PASSWORD_AGAIN = 1
    }

    private lateinit var binding: ActivityShowBinding
    private lateinit var mViewPager: SCViewPager
    private lateinit var mDotsView: DotsView
    private var mPageAdapter: SCViewPagerAdapter? = null
    private var toolbarLayout: View? = null
    private var mContext: Context? = null
    private var myGridView: MyGridView? = null
    private var myGridViewAdapter: PasswordChangeButtonGridViewAdapter? = null

    private var currentState = NEW_PASSWORD
    private var newPassword = ""
    private var againPassword = ""

    private var viewPager: ViewPager? = null
    private var passwordAdapter: PasswordChangeFragmentAdapter? = null
    private var title: TextView? = null
    private var superToast: SuperToast? = null

    private var x1 = 0f
    private var y1 = 0f
    private var x2 = 0f
    private var y2 = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("Starting ShowActivity")

        window.requestFeature(Window.FEATURE_ACTION_BAR)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        super.onCreate(savedInstanceState)
        binding = ActivityShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this

        // Initialize views from binding
        mViewPager = binding.viewpagerMainActivity
        mDotsView = binding.dotsviewMain

        title = findViewById(R.id.title)
        CoCoinUtil.init(mContext!!)
        title?.text = resources.getString(R.string.app_name)

        mDotsView.setDotRessource(R.drawable.dot_selected, R.drawable.dot_unselected)
        mDotsView.setNumberOfPage(NUM_PAGES)

        mPageAdapter = SCViewPagerAdapter(supportFragmentManager)
        mPageAdapter?.setNumberOfPage(NUM_PAGES)
        mPageAdapter?.setFragmentBackgroundColor(R.color.my_blue)
        mViewPager.adapter = mPageAdapter
        mViewPager.overScrollMode = View.OVER_SCROLL_NEVER

        mViewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                mDotsView.selectDot(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        val size = SCViewAnimationUtil.getDisplaySize(this)

        val iconOffsetX = CoCoinUtil.dpToPx(28)
        val iconOffsetY = CoCoinUtil.dpToPx(28)

        val sc0 = SCViewAnimation(findViewById(R.id.icon_4))
        sc0.startToPosition(size.x / 4 - iconOffsetX, size.y * 2 / 7 - iconOffsetY)
        sc0.addPageAnimation(SCPositionAnimation(this, 0, 0, size.y))
        mViewPager.addAnimation(sc0)

        val sc1 = SCViewAnimation(findViewById(R.id.icon_11))
        sc1.startToPosition(size.x * 3 / 4 - iconOffsetX, size.y * 3 / 7 - iconOffsetY)
        sc1.addPageAnimation(SCPositionAnimation(this, 0, -size.x, 0))
        mViewPager.addAnimation(sc1)

        val sc2 = SCViewAnimation(findViewById(R.id.icon_12))
        sc2.startToPosition(size.x / 4 - iconOffsetX, size.y * 4 / 7 - iconOffsetY)
        sc2.addPageAnimation(SCPositionAnimation(this, 0, size.x, 0))
        mViewPager.addAnimation(sc2)

        val sc3 = SCViewAnimation(findViewById(R.id.icon_19))
        sc3.startToPosition(size.x * 3 / 4 - iconOffsetX, size.y * 5 / 7 - iconOffsetY)
        sc3.addPageAnimation(SCPositionAnimation(this, 0, 0, -size.y))
        mViewPager.addAnimation(sc3)

        val sc4 = SCViewAnimation(findViewById(R.id.text_0))
        sc4.addPageAnimation(SCPositionAnimation(this, 0, -size.x, 0))
        mViewPager.addAnimation(sc4)

        val pie = findViewById<PieChartView>(R.id.pie)
        val values = mutableListOf<SliceValue>()
        for (i in 0 until 5) {
            val sliceValue = SliceValue((Math.random() * 30 + 15).toFloat(), ContextCompat.getColor(CoCoinApplication.getAppContext(), R.color.white))
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

        val line = findViewById<LineChartView>(R.id.line)
        val lines = mutableListOf<Line>()
        for (i in 0 until 1) {
            val pointValues = mutableListOf<PointValue>()
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
            aLine.color = ContextCompat.getColor(CoCoinApplication.getAppContext(), R.color.white)
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

        val histogram = findViewById<ColumnChartView>(R.id.histogram)
        val columns = mutableListOf<Column>()
        for (i in 0 until 5) {
            val subcolumnValues = mutableListOf<SubcolumnValue>()
            for (j in 0 until 1) {
                subcolumnValues.add(SubcolumnValue((Math.random() * 50f + 5).toFloat(), ContextCompat.getColor(CoCoinApplication.getAppContext(), R.color.white)))
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
        sc7.startToPosition(size.x / 2 - CoCoinUtil.dpToPx(140), size.y * 8 / 9 - CoCoinUtil.dpToPx(140) + size.y)
        sc7.addPageAnimation(SCPositionAnimation(this, 0, 0, -size.y))
        sc7.addPageAnimation(SCPositionAnimation(this, 1, 0, size.y))
        mViewPager.addAnimation(sc7)

        val sc8 = SCViewAnimation(findViewById(R.id.text_1))
        sc8.startToPosition(size.x, null)
        sc8.addPageAnimation(SCPositionAnimation(this, 0, -size.x, 0))
        sc8.addPageAnimation(SCPositionAnimation(this, 1, -size.x, 0))
        mViewPager.addAnimation(sc8)

        val sc9 = SCViewAnimation(findViewById(R.id.cloud))
        sc9.startToPosition(size.x / 2 - CoCoinUtil.dpToPx(100) + size.x, size.y / 7)
        sc9.addPageAnimation(SCPositionAnimation(this, 1, -size.x, 0))
        sc9.addPageAnimation(SCPositionAnimation(this, 2, 0, size.y))
        mViewPager.addAnimation(sc9)

        val sc10 = SCViewAnimation(findViewById(R.id.mobile))
        sc10.startToPosition(size.x / 2 - size.x, size.y * 6 / 7 - CoCoinUtil.dpToPx(100))
        sc10.addPageAnimation(SCPositionAnimation(this, 1, size.x, 0))
        sc10.addPageAnimation(SCPositionAnimation(this, 2, 0, -size.y))
        mViewPager.addAnimation(sc10)

        val sc11 = SCViewAnimation(findViewById(R.id.text_2))
        sc11.startToPosition(size.x, null)
        sc11.addPageAnimation(SCPositionAnimation(this, 1, -size.x, 0))
        sc11.addPageAnimation(SCPositionAnimation(this, 2, -size.x, 0))
        mViewPager.addAnimation(sc11)

        val remind1 = findViewById<ImageView>(R.id.remind_1)
        remind1.layoutParams.width = size.x / 3
        remind1.layoutParams.height = size.x / 3 * 653 / 320
        val sc12 = SCViewAnimation(findViewById(R.id.remind_1))
        sc12.startToPosition(size.x / 2 - size.x, size.y / 11)
        sc12.addPageAnimation(SCPositionAnimation(this, 2, size.x, 0))
        sc12.addPageAnimation(SCPositionAnimation(this, 3, size.x, 0))
        mViewPager.addAnimation(sc12)

        val remind2 = findViewById<ImageView>(R.id.remind_2)
        remind2.layoutParams.width = size.x / 3
        remind2.layoutParams.height = size.x / 3 * 653 / 320
        val sc13 = SCViewAnimation(findViewById(R.id.remind_2))
        sc13.startToPosition(size.x / 2 + size.x - size.x / 3, size.y * 10 / 11 - remind1.layoutParams.height)
        sc13.addPageAnimation(SCPositionAnimation(this, 2, -size.x, 0))
        sc13.addPageAnimation(SCPositionAnimation(this, 3, -size.x, 0))
        mViewPager.addAnimation(sc13)

        val sc14 = SCViewAnimation(findViewById(R.id.text_3))
        sc14.startToPosition(size.x, null)
        sc14.addPageAnimation(SCPositionAnimation(this, 2, -size.x, 0))
        sc14.addPageAnimation(SCPositionAnimation(this, 3, -size.x, 0))
        mViewPager.addAnimation(sc14)

        val statusBar = findViewById<View>(R.id.status_bar)
        statusBar.layoutParams = RelativeLayout.LayoutParams(statusBar.layoutParams.width, getStatusBarHeight())
        val statusBarAnimation = SCViewAnimation(statusBar)
        statusBarAnimation.startToPosition(null, -getStatusBarHeight())
        statusBarAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, getStatusBarHeight()))
        mViewPager.addAnimation(statusBarAnimation)

        toolbarLayout = findViewById(R.id.toolbar_layout)
        val toolbarLayoutAnimation = SCViewAnimation(toolbarLayout!!)
        toolbarLayoutAnimation.startToPosition(null, -size.y / 2)
        toolbarLayoutAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, size.y / 2))
        mViewPager.addAnimation(toolbarLayoutAnimation)

        passwordAdapter = PasswordChangeFragmentAdapter(supportFragmentManager)

        viewPager = findViewById(R.id.viewpager)
        viewPager?.scrollBarFadeDuration = 700
        viewPager?.adapter = passwordAdapter

        myGridView = findViewById(R.id.gridview)
        myGridViewAdapter = PasswordChangeButtonGridViewAdapter(this)
        myGridView?.adapter = myGridViewAdapter

        myGridView?.onItemClickListener = gridViewClickListener
        myGridView?.onItemLongClickListener = gridViewLongClickListener

        myGridView?.viewTreeObserver?.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    myGridView?.viewTreeObserver?.removeGlobalOnLayoutListener(this)
                    val lastChild = myGridView?.getChildAt(myGridView?.childCount?.minus(1) ?: 0)
                    val relativeLayout = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, lastChild?.bottom ?: 0)
                    relativeLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    myGridView?.layoutParams = relativeLayout

                    val displaymetrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(displaymetrics)
                    val viewPagerLayoutParams = RelativeLayout.LayoutParams(
                        viewPager?.layoutParams?.width ?: 0, 800)
                    viewPagerLayoutParams.topMargin = 
                        getStatusBarHeight() + CoCoinUtil.getToolBarHeight(mContext!!) / 2
                    viewPager?.layoutParams = viewPagerLayoutParams
                }
            })

        superToast = SuperToast(this)

        val gridViewAnimation = SCViewAnimation(myGridView!!)
        gridViewAnimation.startToPosition(null, size.y)
        gridViewAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, -size.y))
        mViewPager.addAnimation(gridViewAnimation)

        val viewpagerAnimation = SCViewAnimation(viewPager!!)
        viewpagerAnimation.startToPosition(null, -size.y)
        viewpagerAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, size.y))
        mViewPager.addAnimation(viewpagerAnimation)

        val background = findViewById<View>(R.id.background)
        val backgroundAnimation = SCViewAnimation(background)
        backgroundAnimation.startToPosition(null, -size.y - 100)
        backgroundAnimation.addPageAnimation(SCPositionAnimation(this, 3, 0, size.y + 100))
        mViewPager.addAnimation(backgroundAnimation)
    }

    override fun onBackPressed() {
        // Empty implementation
    }

    override fun finish() {
        SuperToast.cancelAllSuperToasts()
        super.finish()
    }

    private val gridViewClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        buttonClickOperation(false, position)
    }

    private val gridViewLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
        buttonClickOperation(true, position)
        true
    }

    private fun buttonClickOperation(longClick: Boolean, position: Int) {
        when (currentState) {
            NEW_PASSWORD -> {
                if (CoCoinUtil.ClickButtonDelete(position)) {
                    if (longClick) {
                        CoCoinFragmentManager.passwordChangeFragment[currentState].init()
                        newPassword = ""
                    } else {
                        CoCoinFragmentManager.passwordChangeFragment[currentState].clear(newPassword.length - 1)
                        if (newPassword.isNotEmpty()) {
                            newPassword = newPassword.substring(0, newPassword.length - 1)
                        }
                    }
                } else if (CoCoinUtil.ClickButtonCommit(position)) {
                    // Empty
                } else {
                    CoCoinFragmentManager.passwordChangeFragment[currentState].set(newPassword.length)
                    newPassword += CoCoinUtil.BUTTONS[position]
                    if (newPassword.length == 4) {
                        currentState = PASSWORD_AGAIN
                        viewPager?.setCurrentItem(PASSWORD_AGAIN, true)
                    }
                }
            }
            PASSWORD_AGAIN -> {
                if (CoCoinUtil.ClickButtonDelete(position)) {
                    if (longClick) {
                        CoCoinFragmentManager.passwordChangeFragment[currentState].init()
                        againPassword = ""
                    } else {
                        CoCoinFragmentManager.passwordChangeFragment[currentState].clear(againPassword.length - 1)
                        if (againPassword.isNotEmpty()) {
                            againPassword = againPassword.substring(0, againPassword.length - 1)
                        }
                    }
                } else if (CoCoinUtil.ClickButtonCommit(position)) {
                    // Empty
                } else {
                    CoCoinFragmentManager.passwordChangeFragment[currentState].set(againPassword.length)
                    againPassword += CoCoinUtil.BUTTONS[position]
                    if (againPassword.length == 4) {
                        if (againPassword == newPassword) {
                            currentState = -1
                            showToast(2)
                            SettingManager.getInstance().setPassword(newPassword)
                            SettingManager.getInstance().setFirstTime(false)
                            if (SettingManager.getInstance().getLoggenOn()) {
                                val currentUser = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
                                currentUser?.accountBookPassword = newPassword
                                currentUser?.update(CoCoinApplication.getAppContext(), currentUser.objectId, object : UpdateListener() {
                                    override fun onSuccess() {
                                        Log.d("Saver", "Set password successfully.")
                                    }

                                    override fun onFailure(code: Int, msg: String?) {
                                        Log.d("Saver", "Set password failed.")
                                    }
                                })
                            }
                            val handler = Handler()
                            handler.postDelayed({
                                finish()
                            }, 1000)
                        } else {
                            CoCoinFragmentManager.passwordChangeFragment[currentState].clear(4)
                            CoCoinFragmentManager.passwordChangeFragment[currentState - 1].init()
                            currentState = NEW_PASSWORD
                            viewPager?.setCurrentItem(NEW_PASSWORD, true)
                            newPassword = ""
                            againPassword = ""
                            showToast(1)
                        }
                    }
                }
            }
        }
    }

    private fun showToast(toastType: Int) {
        SuperToast.cancelAllSuperToasts()

        superToast?.setAnimations(ToastUtil.TOAST_ANIMATION)
        superToast?.duration = SuperToast.Duration.SHORT
        superToast?.setTextColor(Color.parseColor("#ffffff"))
        superToast?.setTextSize(SuperToast.TextSize.SMALL)

        when (toastType) {
            0 -> {
                superToast?.text = resources.getString(R.string.toast_password_wrong)
                superToast?.background = SuperToast.Background.RED
            }
            1 -> {
                superToast?.text = resources.getString(R.string.different_password)
                superToast?.background = SuperToast.Background.RED
            }
            2 -> {
                superToast?.text = resources.getString(R.string.set_password_successfully)
                superToast?.background = SuperToast.Background.GREEN
            }
        }

        superToast?.show()
    }

    override fun onDestroy() {
        for (i in 0 until 3) {
            CoCoinFragmentManager.passwordChangeFragment[i].onDestroy()
            CoCoinFragmentManager.passwordChangeFragment[i] = null
        }
        super.onDestroy()
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}