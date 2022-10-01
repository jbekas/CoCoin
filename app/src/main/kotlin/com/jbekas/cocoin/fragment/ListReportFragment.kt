package com.jbekas.cocoin.fragment

import android.app.Activity
import com.jbekas.cocoin.util.ToastUtil.showToast
import com.jbekas.cocoin.db.RecordManager.Companion.deleteRecord
import com.jbekas.cocoin.adapter.MySwipeableItemAdapter.OnItemDeleteListener
import com.jbekas.cocoin.adapter.MySwipeableItemAdapter
import com.miguelcatalan.materialsearchview.MaterialSearchView
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller
import com.daimajia.slider.library.SliderLayout
import com.jbekas.cocoin.util.CoCoinUtil
import android.os.Bundle
import com.jbekas.cocoin.R
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.model.CoCoinRecord
import android.os.Build
import com.afollestad.materialdialogs.MaterialDialog
import com.miguelcatalan.materialsearchview.MaterialSearchView.SearchViewListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator
import androidx.core.content.ContextCompat
import android.graphics.drawable.NinePatchDrawable
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import android.os.AsyncTask
import com.nispok.snackbar.enums.SnackbarType
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.github.johnpersano.supertoasts.SuperToast
import com.afollestad.materialdialogs.MaterialDialog.InputCallback
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
import com.jbekas.cocoin.ui.MyGridView
import com.jbekas.cocoin.adapter.DialogTagChooseGridViewAdapter
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jbekas.cocoin.databinding.FragmentListReportBinding
import com.jbekas.cocoin.service.ToastService
import com.jbekas.cocoin.ui.DoubleSliderClickListener
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.listeners.EventListener
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber
import java.lang.NumberFormatException
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ListReportFragment : Fragment(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener, OnItemDeleteListener,
    MySwipeableItemAdapter.OnItemClickListener {

    @Inject
    lateinit var toastService: ToastService

    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var _binding: FragmentListReportBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var searchView: MaterialSearchView? = null
    //private var mContext: Context? = null
//    private var mDrawer: DrawerLayout? = null
//    private var mDrawerToggle: ActionBarDrawerToggle? = null
//    private var toolbar: Toolbar? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null
    private var recyclerViewSwipeManager: RecyclerViewSwipeManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var mAdapter: MySwipeableItemAdapter? = null
    private var emptyTip: TextView? = null
    private var lastPosition = 0
    private var undid = false
    private val EDITTING_RECORD = 0
    private var verticalRecyclerViewFastScroller: VerticalRecyclerViewFastScroller? = null
    private var originalSum: Double = 0.0
    private var profileImage: CircleImageView? = null
//    private var mDemoSlider: SliderLayout? = null
//    private var infoLayout: FrameLayout? = null
    private var titleExpense: TextView? = null
    private var titleSum: TextView? = null
    private val titleSlider: SliderLayout? = null
//    private var userName: TextView? = null
//    private var userEmail: TextView? = null
    private val MIN_MONEY = 0.0
    private val MAX_MONEY = 99999.0
    private var leftMoney: Double = 0.toDouble()
    private var rightMoney: Double = 0.toDouble()
    private var TAG_ID = -1
    private var LEFT_CALENDAR: Calendar? = null
    private var RIGHT_CALENDAR: Calendar? = null
    private var setMoney: TextView? = null
    private var noMoney: TextView? = null
    private var setTime: TextView? = null
    private var noTime: TextView? = null
    private var setTag: TextView? = null
    private var noTag: TextView? = null
    private var select: TextView? = null
    private var leftExpense: TextView? = null
    private var rightExpense: TextView? = null
    private var leftTime: TextView? = null
    private var rightTime: TextView? = null
    private var tagImage: ImageView? = null
    private var tagName: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentListReportBinding.inflate(inflater, container, false)

        val view = binding.root

        leftMoney = coCoinUtil.INPUT_MIN_EXPENSE
        rightMoney = coCoinUtil.INPUT_MAX_EXPENSE

//        setContentView(R.layout.fragment_list_report)
        //mContext = this
//        userName = view.findViewById<View>(R.id.user_name) as TextView
//        userEmail = view.findViewById<View>(R.id.user_email) as TextView
        //        userName.setTypeface(coCoinUtil.typefaceLatoRegular);
//        userEmail.setTypeface(coCoinUtil.typefaceLatoLight);

//        User user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User.class);
//        if (user != null) {
//            userName.setText(user.getUsername());
//            userEmail.setText(user.getEmail());
//        }
        val size = RecordManager.RECORDS.size
        if (RecordManager.SELECTED_RECORDS == null) {
            RecordManager.SELECTED_RECORDS = LinkedList()
        }
        RecordManager.SELECTED_RECORDS.clear()
        for (i in 0 until size) {
            val record = CoCoinRecord()
            record.set(RecordManager.RECORDS[i])
            RecordManager.SELECTED_RECORDS.add(record)
        }
        RecordManager.SELECTED_SUM = RecordManager.SUM.toDouble()
        originalSum = RecordManager.SELECTED_SUM
//        toolbar = view.findViewById<View>(R.id.toolbar) as Toolbar
//        setSupportActionBar(toolbar)
//        title = ""
//        mDrawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val currentapiVersion = Build.VERSION.SDK_INT
//        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
//            // Do something for lollipop and above versions
//            val window = this.window
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            //            window.setStatusBarColor(ContextCompat.getColor(mContext, R.color.statusBarColor));
//        } else {
//            // do something for phones running an SDK before lollipop
//            val statusBarView = findViewById(R.id.status_bar_view) as View
//            statusBarView.layoutParams.height = statusBarHeight
//        }
//        if (toolbar != null) {
//            setSupportActionBar(toolbar)
//            val actionBar = supportActionBar
//            if (actionBar != null) {
//                actionBar.setDisplayHomeAsUpEnabled(true)
//                actionBar.setDisplayShowHomeEnabled(true)
//                actionBar.setDisplayShowTitleEnabled(true)
//                actionBar.setDisplayUseLogoEnabled(false)
//                actionBar.setHomeButtonEnabled(true)
//            }
//        }
//        mDrawerToggle = ActionBarDrawerToggle(this, mDrawer, 0, 0)
//        mDrawer!!.setDrawerListener(mDrawerToggle)
        searchView = view.findViewById<View>(R.id.search_view) as MaterialSearchView
        searchView!!.setVoiceSearch(false)
        searchView!!.setHint(getResources().getString(R.string.input_remark_to_search))
        searchView!!.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                progressDialog = MaterialDialog.Builder(requireContext())
                    .title(R.string.selecting_title)
                    .content(R.string.selecting_content)
                    .cancelable(false)
                    .progress(true, 0)
                    .show()
                SelectRecordsByRemark(query).execute()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchView!!.setOnSearchViewListener(object : SearchViewListener {
            override fun onSearchViewShown() {
                //Do some magic
                Timber.d( "onSearchViewShown")
            }

            override fun onSearchViewClosed() {
                //Do some magic
                Timber.d( "onSearchViewClosed")
            }
        })
        emptyTip = view.findViewById<View>(R.id.empty_tip) as TextView
        //        emptyTip.setTypeface(coCoinUtil.GetTypeface());
        recyclerView = view.findViewById<View>(R.id.recycler_view) as RecyclerView
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewTouchActionGuardManager!!.setInterceptVerticalScrollingWhileAnimationRunning(
            true)
        recyclerViewTouchActionGuardManager!!.isEnabled = true
        recyclerViewSwipeManager = RecyclerViewSwipeManager()
        mAdapter = MySwipeableItemAdapter(
            context = requireContext(),
            coCoinUtil = coCoinUtil,
            records = RecordManager.SELECTED_RECORDS,
            onItemDeleteListener = this,
            onItemClickListener = this
        )
        mAdapter!!.eventListener = object : MySwipeableItemAdapter.EventListener {
            override fun onItemRemoved(position: Int) {
                activityOnItemRemoved(position)
            }

            override fun onItemPinned(position: Int) {
                activityOnItemPinned(position)
            }

            override fun onItemViewClicked(v: View?, pinned: Boolean) {
                v?.let {
                    val position = recyclerView!!.getChildAdapterPosition(v)
                    if (position != RecyclerView.NO_POSITION) {
                        activityOnItemClicked(position)
                    }
                }
            }
        }
        adapter = mAdapter
        wrappedAdapter = recyclerViewSwipeManager!!.createWrappedAdapter(mAdapter!!)
        val animator: GeneralItemAnimator = SwipeDismissItemAnimator()
        animator.supportsChangeAnimations = false
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = wrappedAdapter
        recyclerView!!.itemAnimator = animator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            recyclerView!!.addItemDecoration(
                ItemShadowDecorator(
                    (ContextCompat.getDrawable(
                        requireContext(), R.drawable.material_shadow_z1) as NinePatchDrawable?)!!))
        }
        recyclerView!!.addItemDecoration(SimpleListDividerDecorator(
            ContextCompat.getDrawable(requireContext(), R.drawable.list_divider_h), true))

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        recyclerViewTouchActionGuardManager!!.attachRecyclerView(recyclerView!!)
        recyclerViewSwipeManager!!.attachRecyclerView(recyclerView!!)
        verticalRecyclerViewFastScroller =
            view.findViewById<View>(R.id.fast_scroller) as VerticalRecyclerViewFastScroller

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        verticalRecyclerViewFastScroller!!.setRecyclerView(recyclerView)

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recyclerView!!.setOnScrollListener(
            verticalRecyclerViewFastScroller!!.onScrollListener)
        coCoinUtil.backupCoCoinRecord = null
        if (RecordManager.SELECTED_RECORDS.size == 0) {
            emptyTip!!.visibility = View.VISIBLE
            verticalRecyclerViewFastScroller!!.visibility = View.INVISIBLE
        } else {
            emptyTip!!.visibility = View.GONE
            verticalRecyclerViewFastScroller!!.visibility = View.VISIBLE
        }
//        infoLayout = mDrawer!!.findViewById<View>(R.id.info_layout) as FrameLayout
//        val infoLayoutParams = LinearLayout.LayoutParams(infoLayout!!.layoutParams)
//        infoLayoutParams.setMargins(0, statusBarHeight - dpToPx(30), 0, 0)
//        infoLayout!!.layoutParams = infoLayoutParams
//        profileImage = mDrawer!!.findViewById<View>(R.id.profile_image) as CircleImageView
//        profileImage!!.setOnClickListener {
//            if (SettingManager.getInstance().loggenOn) {
//                showToast(this@ListReportFragment, R.string.change_logo_tip, null, null)
//            } else {
//                showToast(this@ListReportFragment, R.string.login_tip, null, null)
//            }
//        }
//        mDemoSlider = findViewById<View>(R.id.slider) as SliderLayout
//        val urls = GetDrawerTopUrl()
//        for (name in urls.keys) {
//            val customSliderView = CustomSliderView(this)
//            // initialize a SliderLayout
//            customSliderView
//                .image(urls[name]!!).scaleType = BaseSliderView.ScaleType.Fit
//            mDemoSlider!!.addSlider(customSliderView)
//        }
//        mDemoSlider!!.setPresetTransformer(SliderLayout.Transformer.ZoomOut)
//        mDemoSlider!!.setCustomAnimation(DescriptionAnimation())
//        mDemoSlider!!.setDuration(4000)
//        mDemoSlider!!.setCustomIndicator(findViewById<View>(R.id.custom_indicator) as PagerIndicator)
        titleExpense = view.findViewById<View>(R.id.title_expense) as TextView
        //        titleExpense.setTypeface(coCoinUtil.typefaceLatoLight);
        titleExpense!!.text = coCoinUtil.getInMoney(RecordManager.SELECTED_SUM.toInt())
        Timber.d("titleExpense!!.text: ${titleExpense!!.text}")
        titleSum = view.findViewById<View>(R.id.title_sum) as TextView
        //        titleSum.setTypeface(coCoinUtil.typefaceLatoLight);
        titleSum!!.text = RecordManager.SELECTED_RECORDS.size.toString() + "'s"

//        titleSlider = (SliderLayout)findViewById(R.id.title_slider);
//        titleSlider.getLayoutParams().height = 48;
//        titleSlider.getLayoutParams().width = 400 - coCoinUtil.dpToPx(60 * 2);
//
//        HashMap<String, Integer> urls2 = coCoinUtil.getTransparentUrls();
//
//        CustomTitleSliderView customTitleSliderView = new CustomTitleSliderView(0 + "'s", CoCoinFragmentManager.NUMBER_SLIDER);
//        customTitleSliderView
//                .image(urls2.get("0"))
//                .setScaleType(BaseSliderView.ScaleType.Fit);
//        titleSlider.addSlider(customTitleSliderView);
//
//        customTitleSliderView = new CustomTitleSliderView(coCoinUtil.GetInMoney(0), CoCoinFragmentManager.EXPENSE_SLIDER);
//        customTitleSliderView
//                .image(urls2.get("1"))
//                .setScaleType(BaseSliderView.ScaleType.Fit);
//        titleSlider.addSlider(customTitleSliderView);
//
//        titleSlider.setPresetTransformer(SliderLayout.Transformer.ZoomOut);
//        titleSlider.setCustomAnimation(new DescriptionAnimation());
//        titleSlider.setDuration(3000);
//        titleSlider.setCustomIndicator((PagerIndicator) findViewById(R.id.custom_indicator));

//        ((TextView)findViewById(R.id.tag_title)).setTypeface(coCoinUtil.GetTypeface());
//        ((TextView)findViewById(R.id.tag_title_expense)).setTypeface(coCoinUtil.GetTypeface());
//        ((TextView)findViewById(R.id.tag_title_time)).setTypeface(coCoinUtil.GetTypeface());
//        ((TextView)findViewById(R.id.tag_title_tag)).setTypeface(coCoinUtil.GetTypeface());

        // TODO - Fix this filter logic
/*
        setMoney = view.findViewById<View>(R.id.select_expense) as TextView
        setMoney!!.setTypeface(GetTypeface())
        setMoney!!.setOnClickListener(this)
        noMoney = view.findViewById<View>(R.id.no_expense) as TextView
        noMoney!!.setTypeface(GetTypeface())
        noMoney!!.setOnClickListener(this)
        setTime = view.findViewById<View>(R.id.select_time) as TextView
        setTime!!.setTypeface(GetTypeface())
        setTime!!.setOnClickListener(this)
        noTime = view.findViewById<View>(R.id.no_time) as TextView
        noTime!!.setTypeface(GetTypeface())
        noTime!!.setOnClickListener(this)
        setTag = view.findViewById<View>(R.id.select_tag) as TextView
        setTag!!.setTypeface(GetTypeface())
        setTag!!.setOnClickListener(this)
        noTag = view.findViewById<View>(R.id.no_tag) as TextView
        noTag!!.setTypeface(GetTypeface())
        noTag!!.setOnClickListener(this)
        select = view.findViewById<View>(R.id.select) as TextView
        select!!.setTypeface(GetTypeface())
        select!!.setOnClickListener(this)
        leftExpense = view.findViewById<View>(R.id.left_expense) as TextView
        leftExpense!!.setTypeface(GetTypeface())
        rightExpense = view.findViewById<View>(R.id.right_expense) as TextView
        rightExpense!!.setTypeface(GetTypeface())
        leftTime = view.findViewById<View>(R.id.left_time) as TextView
        leftTime!!.setTypeface(GetTypeface())
        rightTime = view.findViewById<View>(R.id.right_time) as TextView
        rightTime!!.setTypeface(GetTypeface())
        tagImage = view.findViewById<View>(R.id.tag_image) as ImageView
        tagName = view.findViewById<View>(R.id.tag_name) as TextView
        tagName!!.setTypeface(GetTypeface())
        setConditions()
        loadLogo()
*/

        return view
    }

    private fun setConditions() {
        if (leftMoney == MIN_MONEY) leftExpense!!.text =
            resources.getString(R.string.any) else leftExpense!!.text =
            coCoinUtil.getInMoney(leftMoney.toInt())
        if (rightMoney == MAX_MONEY) rightExpense!!.text =
            resources.getString(R.string.any) else rightExpense!!.text =
            coCoinUtil.getInMoney(rightMoney.toInt())
        if (LEFT_CALENDAR == null) leftTime!!.text =
            resources.getString(R.string.any) else {
            val dateString = (coCoinUtil.getMonthShort(LEFT_CALENDAR!![Calendar.MONTH] + 1)
                    + " " + LEFT_CALENDAR!![Calendar.DAY_OF_MONTH] + " " +
                    LEFT_CALENDAR!![Calendar.YEAR])
            leftTime!!.text = dateString
        }
        if (RIGHT_CALENDAR == null) rightTime!!.text =
            resources.getString(R.string.any) else {
            val dateString = (coCoinUtil.getMonthShort(RIGHT_CALENDAR!![Calendar.MONTH] + 1)
                    + " " + RIGHT_CALENDAR!![Calendar.DAY_OF_MONTH] + " " +
                    RIGHT_CALENDAR!![Calendar.YEAR])
            rightTime!!.text = dateString
        }
        if (TAG_ID == -1) {
            tagImage!!.setImageResource(R.drawable.tags_icon)
            tagName!!.text = resources.getString(R.string.any)
        } else {
            tagImage!!.setImageDrawable(coCoinUtil.getTagIconDrawable(TAG_ID))
            tagName!!.text = coCoinUtil.getTagName(TAG_ID)
        }
    }

    private fun changeTitleSlider() {
//        titleExpense = findViewById<View>(R.id.title_expense) as TextView
        binding.titleExpense.text =
            coCoinUtil.getInMoney(RecordManager.SELECTED_SUM.toInt())
//        titleSum = findViewById<View>(R.id.title_sum) as TextView
        binding.titleSum.text = RecordManager.SELECTED_RECORDS.size.toString() + "'s"

//        titleSlider.stopAutoCycle();
//
//        if (CoCoinFragmentManager.numberCustomTitleSliderView != null)
//            CoCoinFragmentManager.numberCustomTitleSliderView.setTitle(RecordManager.getInstance(CoCoinApplication.getAppContext()).SELECTED_RECORDS.size() + "'s");
//        if (CoCoinFragmentManager.expenseCustomTitleSliderView != null)
//            CoCoinFragmentManager.expenseCustomTitleSliderView.setTitle(coCoinUtil.GetInMoney((int)(double)RecordManager.getInstance(CoCoinApplication.getAppContext()).SELECTED_SUM));
//
//        titleSlider.startAutoCycle();
    }

    private var progressDialog: MaterialDialog? = null
    override fun onSelectSumChanged() {
        changeTitleSlider()
    }

//    private var dialog: MaterialDialog? = null
    private var dialogView: View? = null
    override fun onItemClick(position: Int) {
        var position = position
        position = RecordManager.SELECTED_RECORDS.size - 1 - position
        val subTitle: String
        val spend = RecordManager.SELECTED_RECORDS[position].money
        val tagId = RecordManager.SELECTED_RECORDS[position].tag
        subTitle = "Spent ${coCoinUtil.getInMoney(spend.toInt())} in ${coCoinUtil.getTagName(tagId)}"
        val dialog = MaterialDialog.Builder(requireContext())
            .icon(coCoinUtil.getTagIconDrawable(RecordManager.SELECTED_RECORDS[position].tag)!!)
            .limitIconToDefaultSize()
            .title(subTitle)
            .customView(R.layout.dialog_a_record, true)
            .positiveText(android.R.string.ok)
            .show()
        dialogView = dialog.getCustomView()
        val remark = dialogView!!.findViewById<View>(R.id.remark) as TextView
        val date = dialogView!!.findViewById<View>(R.id.date) as TextView
        remark.text = RecordManager.SELECTED_RECORDS[position].remark
        date.text = RecordManager.SELECTED_RECORDS[position].getCalendarString(coCoinUtil)
    }

//    inner class SelectRecordsByRemark(private val sub: String) :
    inner class SelectRecordsByRemark(private val sub: String) :
        AsyncTask<String?, Void?, String?>() {
        protected override fun doInBackground(vararg params: String?): String? {
            RecordManager.SELECTED_SUM = 0.toDouble()
            if (RecordManager.SELECTED_RECORDS == null) {
                RecordManager.SELECTED_RECORDS = LinkedList()
            } else {
                RecordManager.SELECTED_RECORDS.clear()
            }
            val size = RecordManager.RECORDS.size
            for (i in 0 until size) {
                val record = CoCoinRecord()
                record.set(RecordManager.RECORDS[i])
                if (inRemark(record, sub)) {
                    RecordManager.SELECTED_SUM += record.money
                    RecordManager.SELECTED_RECORDS.add(record)
                }
            }
            originalSum = RecordManager.SELECTED_SUM
            return null
        }

        override fun onPostExecute(result: String?) {
            mAdapter!!.notifyDataSetChanged()
            changeTitleSlider()
            if (RecordManager.SELECTED_RECORDS.size == 0) {
                emptyTip!!.visibility = View.VISIBLE
                verticalRecyclerViewFastScroller!!.visibility = View.INVISIBLE
            } else {
                emptyTip!!.visibility = View.GONE
                verticalRecyclerViewFastScroller!!.visibility = View.VISIBLE
            }
            if (progressDialog != null) progressDialog!!.cancel()
        }
    }

    inner class SelectRecords : AsyncTask<String?, Void?, String?>() {
        protected override fun doInBackground(vararg params: String?): String? {
            RecordManager.SELECTED_SUM = 0.0
            if (RecordManager.SELECTED_RECORDS == null) {
                RecordManager.SELECTED_RECORDS = LinkedList()
            } else {
                RecordManager.SELECTED_RECORDS.clear()
            }
            val size = RecordManager.RECORDS.size
            for (i in 0 until size) {
                val record = CoCoinRecord()
                record.set(RecordManager.RECORDS[i])
                if (inMoney(record) && inTag(record) && inTime(record)) {
                    RecordManager.SELECTED_SUM += record.money
                    RecordManager.SELECTED_RECORDS.add(record)
                }
            }
            originalSum = RecordManager.SELECTED_SUM
            return null
        }

        override fun onPostExecute(result: String?) {
            mAdapter!!.notifyDataSetChanged()
            changeTitleSlider()
            if (RecordManager.SELECTED_RECORDS.size == 0) {
                emptyTip!!.visibility = View.VISIBLE
                verticalRecyclerViewFastScroller!!.visibility = View.INVISIBLE
            } else {
                emptyTip!!.visibility = View.GONE
                verticalRecyclerViewFastScroller!!.visibility = View.VISIBLE
            }
            if (progressDialog != null) progressDialog!!.cancel()
        }
    }

    private fun selectRecords() {
        Timber.e("selectRecords() not implemented")
//        mDrawer!!.closeDrawers()
//        progressDialog = MaterialDialog.Builder(this)
//            .title(R.string.selecting_title)
//            .content(R.string.selecting_content)
//            .cancelable(false)
//            .progress(true, 0)
//            .show()
//        SelectRecords().execute()
    }

    private fun inMoney(record: CoCoinRecord): Boolean {
        return leftMoney <= record.money && record.money <= rightMoney
    }

    private fun inTag(record: CoCoinRecord): Boolean {
        return if (TAG_ID == -1) true else record.tag == TAG_ID
    }

    private fun inTime(record: CoCoinRecord): Boolean {
        return if (LEFT_CALENDAR == null || RIGHT_CALENDAR == null) true else !record.calendar.before(
            LEFT_CALENDAR) && !record.calendar.after(RIGHT_CALENDAR)
    }

    private fun inRemark(record: CoCoinRecord, sub: String): Boolean {
        return record.remark.contains(sub)
    }

    override fun onStop() {
//        mDemoSlider!!.stopAutoCycle()
        //        titleSlider.stopAutoCycle();
        super.onStop()
    }

    public override fun onResume() {
//        if (mDemoSlider != null) mDemoSlider!!.startAutoCycle()
        if (RecordManager.SELECTED_RECORDS == null) selectRecords() else {
//            if (titleSlider != null) titleSlider.startAutoCycle();
        }
        super.onResume()
    }

    private fun activityOnItemRemoved(position: Int) {
        if (RecordManager.SELECTED_RECORDS.size == 0) {
            emptyTip!!.visibility = View.VISIBLE
            verticalRecyclerViewFastScroller!!.visibility = View.INVISIBLE
        }
        lastPosition = RecordManager.SELECTED_RECORDS.size - position
        undid = false
        val snackbar = Snackbar
            .with(requireActivity())
            .type(SnackbarType.MULTI_LINE)
            .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
            .position(Snackbar.SnackbarPosition.BOTTOM)
            .margin(15, 15)
            .backgroundDrawable(coCoinUtil.getSnackBarBackground(-3))
            .text(resources.getString(R.string.deleting))
            .textTypeface(coCoinUtil.getTypeface())
            .textColor(Color.WHITE)
            .actionLabelTypeface(coCoinUtil.getTypeface())
            .actionLabel(resources.getString(R.string.undo))
            .actionColor(Color.WHITE)
            .actionListener {
                RecordManager.SELECTED_RECORDS.add(lastPosition, coCoinUtil.backupCoCoinRecord!!)
                RecordManager.SELECTED_SUM += coCoinUtil.backupCoCoinRecord!!.money
                changeTitleSlider()
                coCoinUtil.backupCoCoinRecord = null
                val linearLayoutManager = recyclerView!!.layoutManager as LinearLayoutManager?
                val firstVisiblePosition = linearLayoutManager
                    ?.findFirstCompletelyVisibleItemPosition() ?: RecyclerView.NO_POSITION
                val lastVisiblePosition = linearLayoutManager
                    ?.findLastCompletelyVisibleItemPosition()  ?: RecyclerView.NO_POSITION
                val insertPosition = RecordManager.SELECTED_RECORDS.size - 1 - lastPosition
                if (firstVisiblePosition < insertPosition
                    && insertPosition <= lastVisiblePosition
                ) {
                } else {
                    recyclerView!!.scrollToPosition(insertPosition)
                }
                mAdapter!!.notifyItemInserted(insertPosition)
                mAdapter!!.notifyDataSetChanged()
                if (RecordManager.SELECTED_RECORDS.size != 0) {
                    emptyTip!!.visibility = View.GONE
                    verticalRecyclerViewFastScroller!!.visibility = View.VISIBLE
                }
            }
            .eventListener(object : EventListener {
                override fun onShow(snackbar: Snackbar) {}
                override fun onShowByReplace(snackbar: Snackbar) {}
                override fun onShown(snackbar: Snackbar) {}
                override fun onDismiss(snackbar: Snackbar) {
                    deleteRecord()
                }

                override fun onDismissByReplace(snackbar: Snackbar) {
                    deleteRecord()
                }

                override fun onDismissed(snackbar: Snackbar) {
                    deleteRecord()
                }

                fun deleteRecord() {
                    if (coCoinUtil.backupCoCoinRecord != null) {
                        val id = deleteRecord(coCoinUtil.backupCoCoinRecord!!, true)
                        if (id > 0) {
                            toastService.showSuccessToast(resources.getString(R.string.delete_successfully_locale))
                            // TODO Find out why Toast.makeText() throws the following error
                            /*
                            Failed to open APK '/data/app/~~9do2zu6vBoJlVF4Phuwpbw==/com.jbekas.cocoin-8_b8uMaRFu_IAgVppMEOaw==/base.apk': I/O error
2022-09-26 22:30:21.164   734-734   ndroid.systemu          com.android.systemui                 E  Failed to open APK '/data/app/~~9do2zu6vBoJlVF4Phuwpbw==/com.jbekas.cocoin-8_b8uMaRFu_IAgVppMEOaw==/base.apk': I/O error
2022-09-26 22:30:21.164   734-734   ResourcesManager        com.android.systemui                 E  failed to add asset path '/data/app/~~9do2zu6vBoJlVF4Phuwpbw==/com.jbekas.cocoin-8_b8uMaRFu_IAgVppMEOaw==/base.apk'
                                                                                                    java.io.IOException: Failed to load asset path /data/app/~~9do2zu6vBoJlVF4Phuwpbw==/com.jbekas.cocoin-8_b8uMaRFu_IAgVppMEOaw==/base.apk
                                                                                                    	at android.content.res.ApkAssets.nativeLoad(Native Method)
                                                                                                    	at android.content.res.ApkAssets.<init>(ApkAssets.java:295)
                                                                                                    	at android.content.res.ApkAssets.loadFromPath(ApkAssets.java:144)
                                                                                                    	at android.app.ResourcesManager.loadApkAssets(ResourcesManager.java:454)
                                                                                                    	at android.app.ResourcesManager.access$000(ResourcesManager.java:72)
                                                                                                    	at android.app.ResourcesManager$ApkAssetsSupplier.load(ResourcesManager.java:168)
                                                                                                    	at android.app.ResourcesManager.createAssetManager(ResourcesManager.java:530)
                                                                                                    	at android.app.ResourcesManager.createResourcesImpl(ResourcesManager.java:612)
                                                                                                    	at android.app.ResourcesManager.findOrCreateResourcesImplForKeyLocked(ResourcesManager.java:664)
                                                                                                    	at android.app.ResourcesManager.createResources(ResourcesManager.java:1011)
                                                                                                    	at android.app.ResourcesManager.getResources(ResourcesManager.java:1114)
                                                                                                    	at android.app.ActivityThread.getTopLevelResources(ActivityThread.java:2372)
                                                                                                    	at android.app.ApplicationPackageManager.getResourcesForApplication(ApplicationPackageManager.java:1751)
                                                                                                    	at android.app.ApplicationPackageManager.getResourcesForApplication(ApplicationPackageManager.java:1737)
                                                                                                    	at android.app.ApplicationPackageManager.getDrawable(ApplicationPackageManager.java:1506)
                                                                                                    	at android.app.ApplicationPackageManager.loadUnbadgedItemIcon(ApplicationPackageManager.java:3029)
                                                                                                    	at android.content.pm.PackageItemInfo.loadUnbadgedIcon(PackageItemInfo.java:290)
                                                                                                    	at com.android.systemui.toast.SystemUIToast.getBadgedIcon(SystemUIToast.java:284)
                                                                                                    	at com.android.systemui.toast.SystemUIToast.inflateToastView(SystemUIToast.java:198)
                                                                                                    	at com.android.systemui.toast.SystemUIToast.<init>(SystemUIToast.java:90)
                                                                                                    	at com.android.systemui.toast.SystemUIToast.<init>(SystemUIToast.java:77)
                                                                                                    	at com.android.systemui.toast.ToastFactory.createToast(ToastFactory.java:78)
                                                                                                    	at com.android.systemui.toast.ToastUI.lambda$showToast$0(ToastUI.java:113)
                                                                                                    	at com.android.systemui.toast.ToastUI.$r8$lambda$w_gPCh3F8Xxn1jN4lkQZoUci71c(Unknown Source:0)
                                                                                                    	at com.android.systemui.toast.ToastUI$$ExternalSyntheticLambda0.run(Unknown Source:16)
                                                                                                    	at com.android.systemui.toast.ToastUI.showToast(ToastUI.java:140)
                                                                                                    	at com.android.systemui.statusbar.CommandQueue$H.handleMessage(CommandQueue.java:1431)
                                                                                                    	at android.os.Handler.dispatchMessage(Handler.java:106)
                                                                                                    	at android.os.Looper.loopOnce(Looper.java:201)
                                                                                                    	at android.os.Looper.loop(Looper.java:288)
                                                                                                    	at android.app.ActivityThread.main(ActivityThread.java:7839)
                                                                                                    	at java.lang.reflect.Method.invoke(Native Method)
                                                                                                    	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
                                                                                                    	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1003)
                             */
/*
                            Toast.makeText(activity,
                                R.string.delete_successfully_locale,
                                Toast.LENGTH_SHORT).show()
*/
                        } else {
                            toastService.showErrorToast(resources.getString(R.string.delete_failed_locale))
                        }
                    }
                    coCoinUtil.backupCoCoinRecord = null
                }
            })
        SnackbarManager.show(snackbar)
    }

    private fun activityOnItemPinned(position: Int) {
        mAdapter!!.notifyItemChanged(position)
        toastService.showErrorToast(text = "Edit is not yet available.")
        val handler = Handler()
        handler.postDelayed({
            mAdapter!!.setPinned(false, position)
            mAdapter!!.notifyItemChanged(position)
        }, 250)

//        val intent = Intent(requireContext(), EditRecordActivity::class.java)
//        intent.putExtra("POSITION", position)
        //startActivityForResult(intent, EDITTING_RECORD)
    }

    private fun activityOnItemClicked(position: Int) {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EDITTING_RECORD -> if (resultCode == Activity.RESULT_OK) {
                val position = data!!.getIntExtra("POSITION", -1)
                val handler = Handler()
                handler.postDelayed({
                    mAdapter!!.setPinned(false, position)
                    mAdapter!!.notifyDataSetChanged()
                }, 500)
                changeTitleSlider()
            }
            else -> {}
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_account_book_list_view, menu)
//        val item = menu.findItem(R.id.action_search)
//        searchView!!.setMenuItem(item)
//        return true
//    }
//
//    override fun onPostCreate(savedInstanceState: Bundle?) {
//        super.onPostCreate(savedInstanceState)
//        mDrawerToggle!!.syncState()
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return mDrawerToggle!!.onOptionsItemSelected(item) ||
//                super.onOptionsItemSelected(item)
//    }
//
//    override fun onBackPressed() {
//        if (mDrawer!!.isDrawerOpen(GravityCompat.START)) {
//            mDrawer!!.closeDrawers()
//            return
//        }
//        if (searchView!!.isSearchOpen) {
//            searchView!!.closeSearch()
//            return
//        }
//        super.onBackPressed()
//    }

//    override fun finish() {
//        SettingManager.getInstance().recordIsUpdated = true
//        if (RecordManager.SELECTED_SUM != originalSum) {
//            SettingManager.getInstance().todayViewMonthExpenseShouldChange = true
//        }
//        if (coCoinUtil.backupCoCoinRecord != null) {
//            deleteRecord(coCoinUtil.backupCoCoinRecord!!, true)
//        }
//        coCoinUtil.backupCoCoinRecord = null
//        super.finish()
//    }

    public override fun onDestroy() {
        if (recyclerViewSwipeManager != null) {
            recyclerViewSwipeManager!!.release()
            recyclerViewSwipeManager = null
        }
        if (recyclerViewTouchActionGuardManager != null) {
            recyclerViewTouchActionGuardManager!!.release()
            recyclerViewTouchActionGuardManager = null
        }
        if (recyclerView != null) {
            recyclerView!!.itemAnimator = null
            recyclerView!!.adapter = null
            recyclerView = null
        }
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            wrappedAdapter = null
        }
        mAdapter = null
        layoutManager = null
        SuperToast.cancelAllSuperToasts()

//        titleSlider.stopAutoCycle();
//        titleSlider.removeAllSliders();
//        titleSlider.destroyDrawingCache();
//        titleSlider = null;

//        CoCoinFragmentManager.numberCustomTitleSliderView = null;
//        CoCoinFragmentManager.expenseCustomTitleSliderView = null;
        doubleSliderClickListener = null
        RecordManager.SELECTED_RECORDS.clear()
//        RecordManager.SELECTED_RECORDS = null
        RecordManager.SELECTED_SUM = 0.0
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun loadLogo() {
//        User user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User.class);
//        if (user != null) {
//            try {
//                File logoFile = new File(CoCoinApplication.getAppContext().getFilesDir() + coCoinUtil.LOGO_NAME);
//                if (!logoFile.exists()) {
//                    // the local logo file is missed
//                    // try to get from the server
//                    BmobQuery<Logo> bmobQuery = new BmobQuery();
//                    bmobQuery.addWhereEqualTo("objectId", user.getLogoObjectId());
//                    bmobQuery.findObjects(CoCoinApplication.getAppContext()
//                            , new FindListener<Logo>() {
//                                @Override
//                                public void onSuccess(List<Logo> object) {
//                                    // there has been an old logo in the server/////////////////////////////////////////////////////////
//                                    String url = object.get(0).getFile().getFileUrl(CoCoinApplication.getAppContext());
//                                    if (BuildConfig.DEBUG) Log.d("CoCoin", "Logo in server: " + url);
//                                    Ion.with(CoCoinApplication.getAppContext()).load(url)
//                                            .write(new File(CoCoinApplication.getAppContext().getFilesDir()
//                                                    + coCoinUtil.LOGO_NAME))
//                                            .setCallback(new FutureCallback<File>() {
//                                                @Override
//                                                public void onCompleted(Exception e, File file) {
//                                                    profileImage.setImageBitmap(BitmapFactory.decodeFile(
//                                                            CoCoinApplication.getAppContext().getFilesDir()
//                                                                    + coCoinUtil.LOGO_NAME));
//                                                }
//                                            });
//                                }
//                                @Override
//                                public void onError(int code, String msg) {
//                                    // the picture is lost
//                                    if (BuildConfig.DEBUG) Log.d("CoCoin", "Can't find the old logo in server.");
//                                }
//                            });
//                } else {
//                    // the user logo is in the storage
//                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(logoFile));
//                    profileImage.setImageBitmap(b);
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        } else {
//            // use the default logo
//            profileImage.setImageResource(R.drawable.default_user_logo);
//        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.select_expense -> setExpense()
            R.id.no_expense -> {
                leftMoney = coCoinUtil.INPUT_MIN_EXPENSE
                rightMoney = coCoinUtil.INPUT_MAX_EXPENSE
                setConditions()
            }
            R.id.select_time -> setCalendar()
            R.id.no_time -> {
                LEFT_CALENDAR = null
                RIGHT_CALENDAR = null
                setConditions()
            }
            R.id.select_tag -> setTag()
            R.id.no_tag -> {
                TAG_ID = -1
                setConditions()
            }
            R.id.select -> selectRecords()
        }
    }

    private var inputNumber = -1.0
    private fun setExpense() {
        inputNumber = -1.0
        MaterialDialog.Builder(requireContext())
            .title(R.string.set_expense)
            .content(R.string.set_left_expense)
            .positiveText(R.string.ok)
            .negativeText(R.string.cancel)
            .inputType(InputType.TYPE_CLASS_NUMBER)
            .input("≥" + coCoinUtil.INPUT_MIN_EXPENSE.toInt(),
                "",
                InputCallback { dialog, input ->
                    try {
                        inputNumber = java.lang.Double.valueOf(input.toString())
                        if (inputNumber < coCoinUtil.INPUT_MIN_EXPENSE || inputNumber > coCoinUtil.INPUT_MAX_EXPENSE) inputNumber =
                            -1.0
                    } catch (n: NumberFormatException) {
                        inputNumber = -1.0
                    }
                    if (inputNumber == -1.0) dialog.getActionButton(DialogAction.POSITIVE).isEnabled =
                        false else dialog.getActionButton(DialogAction.POSITIVE).isEnabled = true
                })
            .onAny(SingleButtonCallback { dialog, which ->
                if (which == DialogAction.POSITIVE) {
                    leftMoney = inputNumber
                    inputNumber = -1.0
                    MaterialDialog.Builder(requireContext())
                        .title(R.string.set_expense)
                        .content(R.string.set_right_expense)
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("≤" + coCoinUtil.INPUT_MAX_EXPENSE.toInt(),
                            "",
                            InputCallback { dialog, input ->
                                try {
                                    inputNumber = java.lang.Double.valueOf(input.toString())
                                    if (inputNumber < coCoinUtil.INPUT_MIN_EXPENSE || inputNumber > coCoinUtil.INPUT_MAX_EXPENSE) inputNumber =
                                        -1.0
                                } catch (n: NumberFormatException) {
                                    inputNumber = -1.0
                                }
                                if (inputNumber == -1.0) dialog.getActionButton(DialogAction.POSITIVE).isEnabled =
                                    false else dialog.getActionButton(DialogAction.POSITIVE).isEnabled =
                                    true
                            })
                        .onAny(SingleButtonCallback { dialog, which ->
                            if (which == DialogAction.POSITIVE) {
                                rightMoney = inputNumber
                                setConditions()
                            }
                        })
                        .alwaysCallInputCallback()
                        .show()
                }
            })
            .alwaysCallInputCallback()
            .show()
    }

    private var isFrom = true
    private fun setCalendar() {
        Timber.e("setCalendar() not implemented correctly.")
        val now = Calendar.getInstance()
        val dpd = DatePickerDialog.newInstance(
            this,
            now[Calendar.YEAR],
            now[Calendar.MONTH],
            now[Calendar.DAY_OF_MONTH]
        )
        dpd.setTitle(resources.getString(R.string.set_left_calendar))
//        dpd.show(fragmentManager, "Datepickerdialog")
        isFrom = true
    }

    private var fromYear = 0
    private var fromMonth = 0
    private var fromDay = 0
    private val to = Calendar.getInstance()
    private val from = Calendar.getInstance()
    override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        Timber.e("onDateSet() not implemented correctly.")
        if (isFrom) {
            fromYear = year
            fromMonth = monthOfYear + 1
            fromDay = dayOfMonth
            val now = Calendar.getInstance()
            val dpd = DatePickerDialog.newInstance(
                this,
                now[Calendar.YEAR],
                now[Calendar.MONTH],
                now[Calendar.DAY_OF_MONTH]
            )
            dpd.setTitle(resources.getString(R.string.set_right_calendar))
            //dpd.show(fragmentManager, "Datepickerdialog")
            isFrom = false
        } else {
            from[fromYear, fromMonth - 1, fromDay, 0, 0] = 0
            from.add(Calendar.SECOND, 0)
            to[year, monthOfYear, dayOfMonth, 23, 59] = 59
            to.add(Calendar.SECOND, 0)
            if (to.before(from)) {
                showToast(
                    activity = requireActivity(),
                    text = resources.getString(R.string.from_invalid),
                    color = SuperToast.Background.RED)
            } else {
                LEFT_CALENDAR = from.clone() as Calendar
                RIGHT_CALENDAR = to.clone() as Calendar
                setConditions()
            }
        }
    }

    private var myGridView: MyGridView? = null
    private var dialogTagChooseGridViewAdapter: DialogTagChooseGridViewAdapter? = null
//    private var tagSelectDialog: MaterialDialog? = null
    private var tagSelectDialogView: View? = null
    private fun setTag() {
        val tagSelectDialog = MaterialDialog.Builder(requireContext())
            .title(R.string.set_tag)
            .customView(R.layout.dialog_select_tag, false)
            .negativeText(R.string.cancel)
            .show()
        tagSelectDialogView = tagSelectDialog.getCustomView()
        myGridView = tagSelectDialogView!!.findViewById<View>(R.id.grid_view) as MyGridView
        dialogTagChooseGridViewAdapter = DialogTagChooseGridViewAdapter(requireContext(), coCoinUtil)
        myGridView!!.adapter = dialogTagChooseGridViewAdapter
        myGridView!!.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                tagSelectDialog.dismiss()
                TAG_ID = RecordManager.TAGS[position + 2].id
                tagImage!!.setImageDrawable(coCoinUtil.getTagIconDrawable(TAG_ID))
                tagName!!.text = coCoinUtil.getTagName(TAG_ID)
            }
    }

    private var doubleSliderClickListener: DoubleSliderClickListener? =
        object : DoubleSliderClickListener() {
            override fun onSingleClick(v: BaseSliderView) {}
            override fun onDoubleClick(v: BaseSliderView) {
                if (recyclerView != null) recyclerView!!.scrollToPosition(0)
            }
        }
}