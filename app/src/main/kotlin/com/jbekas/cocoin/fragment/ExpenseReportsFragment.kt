package com.jbekas.cocoin.fragment

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.github.johnpersano.supertoasts.SuperToast
import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.adapter.DialogSelectListDataAdapter
import com.jbekas.cocoin.adapter.ReportDayAdapter
import com.jbekas.cocoin.adapter.ReportMonthAdapter
import com.jbekas.cocoin.adapter.ReportTagAdapter
import com.jbekas.cocoin.databinding.FragmentExpenseReportsBinding
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.ui.ExpandedListView
import com.jbekas.cocoin.ui.MyGridView
import com.jbekas.cocoin.util.CoCoinUtil
import com.melnykov.fab.FloatingActionButton
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
import com.nispok.snackbar.listeners.ActionClickListener
import dagger.hilt.android.AndroidEntryPoint
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.AxisValue
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.model.SelectedValue
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.model.ValueShape
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.view.PieChartView
import net.steamcrafted.materialiconlib.MaterialIconView
import timber.log.Timber
import java.util.*
import javax.inject.Inject

//import com.squareup.leakcanary.RefWatcher;
/**
 * report is to show the expense of some time for user
 *
 * if the user select a year
 * we show
 * expense and sum(compare with last year)
 * the number of tags used in(compare with last year)
 * a pie for all expense on diff tags
 * the highest expense on some tags(and percent), click for more
 * the lowest expense on some tags(and percent), click for more
 * the percent used in food, clothes, house and traffic
 * the highest expense on some tags except the four kinds of tags above
 * a line chart of every month expense
 * the highest expense of some months(and percent), click for more
 * the lowest expense of some months(and percent), click for more
 * the average value of expense of a month
 * the highest expense of some days, click for more(@param MAX_DAY_EXPENSE days)
 * the lowest expense of some days, click for more(@param MAX_DAY_EXPENSE days)
 * the average value of expense of a day
 *
 * if the user select a month
 * we show
 * expense and sum(compare with last month)
 * the number of tags used in(compare with last month)
 * a pie for all expense on diff tags
 * the highest expense on some tags(and percent), click for more
 * the lowest expense on some tags(and percent), click for more
 * the percent used in food, clothes, house and traffic
 * the highest expense on some tags except the four kinds of tags above
 * a line chart of every day expense
 * the highest expense of some days(and percent), click for more
 * the lowest expense of some days(and percent), click for more
 * the average value of expense of a day
 */
@AndroidEntryPoint
class ExpenseReportsFragment : Fragment(), View.OnClickListener, AdapterView.OnItemClickListener {
    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var _binding: FragmentExpenseReportsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var reportPeriodDialog: MaterialDialog? = null

    private var mContext: Context? = null
    private var button: FloatingActionButton? = null
    private val fromDate: TextView? = null
    private var expenseTV: TextView? = null
    private var isEmpty = false
    private var emptyTip: TextView? = null
    private var tagsTV: TextView? = null
    private val from = Calendar.getInstance()
    private val to = Calendar.getInstance()
    private var superToast: SuperToast? = null
    private var IS_EMPTY = false

    // the string shown in the dialog
    private var dialogTitle: String? = null

    // the selected tag in pie
    private var tagId = -1

    // select list
    // year, month(-1 means the whole year), records, expenses
    private var selectListData: ArrayList<DoubleArray>? = null

    // title
    private var title: TextView? = null

    // pie
    private var pieLayout: LinearLayout? = null
    private var pieTitle: TextView? = null
    private var pie: PieChartView? = null
    private var pieSelectedPosition = 0 // the selected position of one part of the pie
    private var lastPieSelectedPosition = -1 // the last selected position of one part of the pie
    private var iconRight: MaterialIconView? = null
    private var iconLeft: MaterialIconView? = null

    // highest tag list
    private var highestTagLayout: LinearLayout? = null
    private var highestTagTitle: TextView? = null
    private var highestFirst: LinearLayout? = null
    private var highestTagIcon: ImageView? = null
    private var highestTagText: TextView? = null
    private var highestTagExpenseTV: TextView? = null
    private var highestTagRecord: TextView? = null
    private var highestTags: ExpandedListView? = null
    private var highestTagsAdapter: ReportTagAdapter? = null
    private var highestTagsLayout: RelativeLayout? = null
    private var highestTagMore: LinearLayout? = null
    private var highestTagMoreText: TextView? = null

    // lowest tag list
    private var lowestTagLayout: LinearLayout? = null
    private var lowestTagTitle: TextView? = null
    private var lowestFirst: LinearLayout? = null
    private var lowestTagIcon: ImageView? = null
    private var lowestTagText: TextView? = null
    private var lowestTagExpenseTV: TextView? = null
    private var lowestTagRecord: TextView? = null
    private var lowestTags: ExpandedListView? = null
    private var lowestTagsAdapter: ReportTagAdapter? = null
    private var lowestTagsLayout: RelativeLayout? = null
    private var lowestTagMore: LinearLayout? = null
    private var lowestTagMoreText: TextView? = null

    // line
    private var lineLayout: LinearLayout? = null
    private var lineTitle: TextView? = null
    private var line: LineChartView? = null
    private var lineSelectedPosition = 0 // the selected position of one part of the line
    private var lastLineSelectedPosition = -1 // the last selected position of one part of the line
    private var iconRightLine: MaterialIconView? = null
    private var iconLeftLine: MaterialIconView? = null

    // month
    private var highestMonthLayout: LinearLayout? = null
    private var monthTitle: TextView? = null
    private var highestFirstMonth: LinearLayout? = null
    private var highestFirstIcon: TextView? = null
    private var highestFirstText: TextView? = null
    private var highestFirstExpenseTV: TextView? = null
    private var highestFirstRecord: TextView? = null
    private var highestMonths: ExpandedListView? = null
    private var highestMonthsAdapter: ReportMonthAdapter? = null
    private var highestMonthsLayout: RelativeLayout? = null
    private var highestLast: LinearLayout? = null
    private var highestLastIcon: TextView? = null
    private var highestLastText: TextView? = null
    private var highestLastExpenseTV: TextView? = null
    private var highestLastRecord: TextView? = null
    private var highestMonthMore: LinearLayout? = null
    private var highestMonthMoreText: TextView? = null

    // average month
    private var averageMonthText: TextView? = null
    private var averageMonthExpenseTV: TextView? = null
    private var averageMonthRecordTV: TextView? = null

    // highest day
    private var highestDayLayout: LinearLayout? = null
    private var highestDayTitle: TextView? = null
    private var highestFirstDay: LinearLayout? = null
    private var highestDayIcon: TextView? = null
    private var highestDayText: TextView? = null
    private var highestDayExpenseTV: TextView? = null
    private var highestDayRecord: TextView? = null
    private var highestDays: ExpandedListView? = null
    private var highestDaysAdapter: ReportDayAdapter? = null
    private var highestDaysLayout: RelativeLayout? = null
    private var highestDayMore: LinearLayout? = null
    private var highestDayMoreText: TextView? = null

    // lowest day
    private var lowestDayLayout: LinearLayout? = null
    private var lowestDayTitle: TextView? = null
    private var lowestFirstDay: LinearLayout? = null
    private var lowestDayIcon: TextView? = null
    private var lowestDayText: TextView? = null
    private var lowestDayExpenseTV: TextView? = null
    private var lowestDayRecord: TextView? = null
    private var lowestDays: ExpandedListView? = null
    private var lowestDaysAdapter: ReportDayAdapter? = null
    private var lowestDaysLayout: RelativeLayout? = null
    private var lowestDayMore: LinearLayout? = null
    private var lowestDayMoreText: TextView? = null

    // average day
    private var averageDayText: TextView? = null
    private var averageDayExpenseTV: TextView? = null
    private var averageDayRecordTV: TextView? = null

    // foot
    private var foot: TextView? = null
    private var activity: Activity? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            activity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        CoCoinFragmentManager.reportViewFragment = this;
        mContext = context
        superToast = SuperToast(mContext)
        superToast!!.animations = SuperToast.Animations.POPUP
        superToast!!.duration = SuperToast.Duration.SHORT
        superToast!!.textColor = Color.parseColor("#ffffff")
        superToast!!.setTextSize(SuperToast.TextSize.SMALL)
        superToast!!.background = SuperToast.Background.RED
        //        superToast.getTextView().setTypeface(coCoinUtil.typefaceLatoLight);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val dialog: MaterialDialog? = null
    private val dialogView: View? = null
    private val myGridView: MyGridView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IS_EMPTY = RecordManager.RECORDS.isEmpty()
        expenseTV = view.findViewById<View>(R.id.expense) as TextView
        //        expenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        expenseTV!!.text = coCoinUtil.getInMoney(0)
        tagsTV = view.findViewById<View>(R.id.tags) as TextView
        //        tagsTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        tagsTV!!.text = ""
        title = view.findViewById<View>(R.id.title) as TextView
        //        title.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        pieLayout = view.findViewById<View>(R.id.pie_layout) as LinearLayout
        pieLayout!!.visibility = View.GONE
        pieTitle = view.findViewById<View>(R.id.pie_title) as TextView
        //        pieTitle.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        pie = view.findViewById<View>(R.id.chart_pie) as PieChartView
        pie!!.isChartRotationEnabled = false
        pie!!.onValueTouchListener = object : PieChartOnValueSelectListener {
            override fun onValueSelected(p: Int, sliceValue: SliceValue) {
                // snack bar
                val text: String
                tagId = Integer.valueOf(String(sliceValue.labelAsChars))
                val percent = sliceValue.value / expense * 100
                if (("zh" == coCoinUtil.getLanguage())) {
                    text = (coCoinUtil.getSpendString(sliceValue.value.toInt()) +
                            coCoinUtil.getPercentString(percent) + "\n" +
                            "于" + coCoinUtil.getTagName(tagId))
                } else {
                    text = (coCoinUtil.getSpendString(sliceValue.value.toInt())
                            + " (takes " + String.format("%.2f", percent) + "%)\n"
                            + "in " + coCoinUtil.getTagName(tagId))
                }
                if (("zh" == coCoinUtil.getLanguage())) {
                    if (selectYear) {
                        dialogTitle = (from[Calendar.YEAR].toString() + "年" + "\n" +
                                coCoinUtil.getSpendString(sliceValue.value.toInt()) +
                                "于" + coCoinUtil.getTagName(tagId))
                    } else {
                        dialogTitle =
                            (from[Calendar.YEAR].toString() + "年" + (from[Calendar.MONTH] + 1) + "月" + "\n" +
                                    coCoinUtil.getSpendString(sliceValue.value.toInt()) +
                                    "于" + coCoinUtil.getTagName(tagId))
                    }
                } else {
                    if (selectYear) {
                        dialogTitle =
                            (coCoinUtil.getSpendString(sliceValue.value.toInt()) + " in " + from[Calendar.YEAR] + "\n" +
                                    "on " + coCoinUtil.getTagName(tagId))
                    } else {
                        dialogTitle =
                            (coCoinUtil.getSpendString(sliceValue.value.toInt()) + " in " + coCoinUtil.getMonthShort(
                                from[Calendar.MONTH] + 1) + " " + from[Calendar.YEAR] + "\n" +
                                    "on " + coCoinUtil.getTagName(tagId))
                    }
                }
                val snackbar = Snackbar
                    .with(mContext)
                    .type(SnackbarType.MULTI_LINE)
                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                    .position(Snackbar.SnackbarPosition.BOTTOM)
                    .margin(15, 15)
                    .backgroundDrawable(coCoinUtil.getSnackBarBackground(-3))
                    .text(text)
                    .textTypeface(coCoinUtil.getTypeface())
                    .textColor(Color.WHITE)
                    .actionLabelTypeface(coCoinUtil.getTypeface())
                    .actionLabel(mContext!!.resources
                        .getString(R.string.check))
                    .actionColor(Color.WHITE)
                    .actionListener(ActionClickListener {
                        GetData(from,
                            to,
                            tagId,
                            dialogTitle!!).execute()
                    })
                SnackbarManager.show(snackbar)
                if (p == lastPieSelectedPosition) {
                    return
                } else {
                    lastPieSelectedPosition = p
                }
            }

            override fun onValueDeselected() {}
        }
        iconRight = view.findViewById<View>(R.id.icon_right) as MaterialIconView
        iconRight!!.setOnClickListener(this)
        iconLeft = view.findViewById<View>(R.id.icon_left) as MaterialIconView
        iconLeft!!.setOnClickListener(this)
        emptyTip = view.findViewById<View>(R.id.empty_tip) as TextView
        emptyTip!!.typeface = coCoinUtil.getTypeface()
        if (RecordManager.RECORDS.size != 0) {
            emptyTip!!.text =
                mContext!!.resources.getString(R.string.report_view_please_select_data)
        } else {
            emptyTip!!.text = mContext!!.resources.getString(R.string.report_view_no_data)
            isEmpty = true
        }
        highestTagLayout = view.findViewById<View>(R.id.highest_tag_layout) as LinearLayout
        highestTagLayout!!.visibility = View.GONE
        highestTagTitle = view.findViewById<View>(R.id.highest_tag_title) as TextView
        //        highestTagTitle.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestFirst = view.findViewById<View>(R.id.highest_first) as LinearLayout
        highestFirst!!.setOnClickListener(this)
        highestTagIcon = view.findViewById<View>(R.id.highest_tag_icon) as ImageView
        highestTagText = view.findViewById<View>(R.id.highest_tag_text) as TextView
        //        highestTagText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestTagExpenseTV = view.findViewById<View>(R.id.highest_tag_expense) as TextView
        //        highestTagExpenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestTagRecord = view.findViewById<View>(R.id.highest_tag_sum) as TextView
        //        highestTagRecord.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestTags = view.findViewById<View>(R.id.highest_tags) as ExpandedListView
        highestTagsLayout = view.findViewById<View>(R.id.expand_highest_tag) as RelativeLayout
        highestTagMore = view.findViewById<View>(R.id.highest_tag_more) as LinearLayout
        highestTagMore!!.setOnClickListener(this)
        highestTagMoreText = view.findViewById<View>(R.id.highest_tag_more_text) as TextView
        //        highestTagMoreText.setTypeface(coCoinUtil.getInstance().GetTypeface());
        highestTags!!.onItemClickListener = this
        lowestTagLayout = view.findViewById<View>(R.id.lowest_tag_layout) as LinearLayout
        lowestTagLayout!!.visibility = View.GONE
        lowestTagTitle = view.findViewById<View>(R.id.lowest_tag_title) as TextView
        //        lowestTagTitle.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestFirst = view.findViewById<View>(R.id.lowest_first) as LinearLayout
        lowestFirst!!.setOnClickListener(this)
        lowestTagIcon = view.findViewById<View>(R.id.lowest_tag_icon) as ImageView
        lowestTagText = view.findViewById<View>(R.id.lowest_tag_text) as TextView
        //        lowestTagText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestTagExpenseTV = view.findViewById<View>(R.id.lowest_tag_expense) as TextView
        //        lowestTagExpenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestTagRecord = view.findViewById<View>(R.id.lowest_tag_sum) as TextView
        //        lowestTagRecord.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestTags = view.findViewById<View>(R.id.lowest_tags) as ExpandedListView
        lowestTagsLayout = view.findViewById<View>(R.id.expand_lowest_tag) as RelativeLayout
        lowestTagMore = view.findViewById<View>(R.id.lowest_tag_more) as LinearLayout
        lowestTagMore!!.setOnClickListener(this)
        lowestTagMoreText = view.findViewById<View>(R.id.lowest_tag_more_text) as TextView
        //        lowestTagMoreText.setTypeface(coCoinUtil.getInstance().GetTypeface());
        lowestTags!!.onItemClickListener = this
        lineLayout = view.findViewById<View>(R.id.line_layout) as LinearLayout
        lineLayout!!.visibility = View.GONE
        lineTitle = view.findViewById<View>(R.id.line_title) as TextView
        //        lineTitle.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        line = view.findViewById<View>(R.id.chart_line) as LineChartView
        line!!.isZoomEnabled = false
        line!!.onValueTouchListener = object : LineChartOnValueSelectListener {
            override fun onValueSelected(lineIndex: Int, pointIndex: Int, value: PointValue) {
                // snack bar
                val text: String
                val percent = value.y / expense * 100
                if (("zh" == coCoinUtil.getLanguage())) {
                    if (selectYear) {
                        text =
                            ("在" + reportYear + " " + coCoinUtil.getMonthShort(value.x.toInt() + 1) + "\n" +
                                    coCoinUtil.getSpendString(value.y.toInt()) +
                                    coCoinUtil.getPercentString(percent))
                    } else {
                        text =
                            ("在" + coCoinUtil.getMonthShort(reportMonth) + " " + (value.x.toInt() + 1) + "\n" +
                                    coCoinUtil.getSpendString(value.y.toInt()) +
                                    coCoinUtil.getPercentString(percent))
                    }
                } else {
                    if (selectYear) {
                        text = (coCoinUtil.getSpendString(value.y.toInt()) +
                                coCoinUtil.getPercentString(percent) + "\n" +
                                "in " + reportYear + " " + coCoinUtil.getMonthShort(value.x.toInt() + 1))
                    } else {
                        text = (coCoinUtil.getSpendString(value.y.toInt()) +
                                coCoinUtil.getPercentString(percent) + "\n" +
                                "on " + coCoinUtil.getMonthShort(reportMonth) + " " + (value.x.toInt() + 1))
                    }
                }
                if (("zh" == coCoinUtil.getLanguage())) {
                    if (selectYear) {
                        dialogTitle =
                            ("在" + reportYear + " " + coCoinUtil.getMonthShort(value.x.toInt() + 1) + "\n" +
                                    coCoinUtil.getSpendString(value.y.toInt()) +
                                    coCoinUtil.getPercentString(percent))
                    } else {
                        dialogTitle =
                            ("在" + coCoinUtil.getMonthShort(reportMonth) + " " + (value.x.toInt() + 1) + "\n" +
                                    coCoinUtil.getSpendString(value.y.toInt()) +
                                    coCoinUtil.getPercentString(percent))
                    }
                } else {
                    if (selectYear) {
                        dialogTitle = (coCoinUtil.getSpendString(value.y.toInt()) +
                                coCoinUtil.getPercentString(percent) + "\n" +
                                "in " + reportYear + " " + coCoinUtil.getMonthShort(value.x.toInt() + 1))
                    } else {
                        dialogTitle = (coCoinUtil.getSpendString(value.y.toInt()) +
                                coCoinUtil.getPercentString(percent) + "\n" +
                                "on " + coCoinUtil.getMonthShort(reportMonth) + " " + (value.x.toInt() + 1))
                    }
                }
                val tempFrom = Calendar.getInstance()
                val tempTo = Calendar.getInstance()
                if (selectYear) {
                    tempFrom[reportYear, value.x.toInt(), 1, 0, 0] = 0
                    tempFrom.add(Calendar.SECOND, 0)
                    tempTo[reportYear, value.x.toInt(), tempFrom.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59] =
                        59
                    tempTo.add(Calendar.SECOND, 0)
                } else {
                    tempFrom[reportYear, reportMonth - 1, value.x.toInt() + 1, 0, 0] = 0
                    tempFrom.add(Calendar.SECOND, 0)
                    tempTo[reportYear, reportMonth - 1, value.x.toInt() + 1, 23, 59] = 59
                    tempTo.add(Calendar.SECOND, 0)
                }
                val snackbar = Snackbar
                    .with(mContext)
                    .type(SnackbarType.MULTI_LINE)
                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                    .position(Snackbar.SnackbarPosition.BOTTOM)
                    .margin(15, 15)
                    .backgroundDrawable(coCoinUtil.getSnackBarBackground(-3))
                    .text(text)
                    .textTypeface(coCoinUtil.getTypeface())
                    .textColor(Color.WHITE)
                    .actionLabelTypeface(coCoinUtil.getTypeface())
                    .actionLabel(mContext!!.resources
                        .getString(R.string.check))
                    .actionColor(Color.WHITE)
                    .actionListener(object : ActionClickListener {
                        override fun onActionClicked(snackbar: Snackbar) {
                            GetData(tempFrom, tempTo, Int.MIN_VALUE, dialogTitle!!).execute()
                        }
                    })
                SnackbarManager.show(snackbar)
                if (pointIndex == lastLineSelectedPosition) {
                    return
                } else {
                    lastLineSelectedPosition = pointIndex
                }
            }

            override fun onValueDeselected() {}
        }
        iconRightLine = view.findViewById<View>(R.id.icon_right_line) as MaterialIconView
        iconRightLine!!.setOnClickListener(this)
        iconLeftLine = view.findViewById<View>(R.id.icon_left_line) as MaterialIconView
        iconLeftLine!!.setOnClickListener(this)
        highestMonthLayout = view.findViewById<View>(R.id.highest_month_layout) as LinearLayout
        highestMonthLayout!!.visibility = View.GONE
        monthTitle = view.findViewById<View>(R.id.month_title) as TextView
        //        monthTitle.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestFirstMonth = view.findViewById<View>(R.id.highest_first_month) as LinearLayout
        highestFirstMonth!!.setOnClickListener(this)
        highestFirstIcon = view.findViewById<View>(R.id.highest_month_icon) as TextView
        //        highestFirstIcon.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestFirstText = view.findViewById<View>(R.id.highest_month_text) as TextView
        //        highestFirstText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestFirstExpenseTV = view.findViewById<View>(R.id.highest_month_expense) as TextView
        //        highestFirstExpenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestFirstRecord = view.findViewById<View>(R.id.highest_month_sum) as TextView
        //        highestFirstRecord.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestMonths = view.findViewById<View>(R.id.highest_month) as ExpandedListView
        highestMonths!!.onItemClickListener = this
        highestMonthsLayout = view.findViewById<View>(R.id.expand_highest_month) as RelativeLayout
        highestLast = view.findViewById<View>(R.id.highest_last_month) as LinearLayout
        highestLast!!.setOnClickListener(this)
        highestLastIcon = view.findViewById<View>(R.id.lowest_month_icon) as TextView
        //        highestLastIcon.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestLastText = view.findViewById<View>(R.id.lowest_month_text) as TextView
        //        highestLastText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestLastExpenseTV = view.findViewById<View>(R.id.lowest_month_expense) as TextView
        //        highestLastExpenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestLastRecord = view.findViewById<View>(R.id.lowest_month_sum) as TextView
        //        highestLastRecord.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestMonthMore = view.findViewById<View>(R.id.highest_month_more) as LinearLayout
        highestMonthMore!!.setOnClickListener(this)
        highestMonthMoreText = view.findViewById<View>(R.id.highest_month_more_text) as TextView
        //        highestMonthMoreText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        averageMonthText = view.findViewById<View>(R.id.average_month_text) as TextView
        //        averageMonthText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        averageMonthExpenseTV = view.findViewById<View>(R.id.average_month_expense) as TextView
        //        averageMonthExpenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        averageMonthRecordTV = view.findViewById<View>(R.id.average_month_sum) as TextView
        //        averageMonthRecordTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestDayLayout = view.findViewById<View>(R.id.highest_day_layout) as LinearLayout
        highestDayLayout!!.visibility = View.GONE
        highestDayTitle = view.findViewById<View>(R.id.highest_day_title) as TextView
        //        highestDayTitle.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestFirstDay = view.findViewById<View>(R.id.highest_first_day) as LinearLayout
        highestFirstDay!!.setOnClickListener(this)
        highestDayIcon = view.findViewById<View>(R.id.highest_day_icon) as TextView
        //        highestDayIcon.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestDayText = view.findViewById<View>(R.id.highest_day_text) as TextView
        //        highestDayText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestDayExpenseTV = view.findViewById<View>(R.id.highest_day_expense) as TextView
        //        highestDayExpenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestDayRecord = view.findViewById<View>(R.id.highest_day_sum) as TextView
        //        highestDayRecord.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        highestDays = view.findViewById<View>(R.id.highest_days) as ExpandedListView
        highestDaysLayout = view.findViewById<View>(R.id.expand_highest_day) as RelativeLayout
        highestDayMore = view.findViewById<View>(R.id.highest_day_more) as LinearLayout
        highestDayMore!!.setOnClickListener(this)
        highestDayMoreText = view.findViewById<View>(R.id.highest_day_more_text) as TextView
        //        highestDayMoreText.setTypeface(coCoinUtil.getInstance().GetTypeface());
        highestDays!!.onItemClickListener = this
        lowestDayLayout = view.findViewById<View>(R.id.lowest_day_layout) as LinearLayout
        lowestDayLayout!!.visibility = View.GONE
        lowestDayTitle = view.findViewById<View>(R.id.lowest_day_title) as TextView
        //        lowestDayTitle.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestFirstDay = view.findViewById<View>(R.id.lowest_first_day) as LinearLayout
        lowestFirstDay!!.setOnClickListener(this)
        lowestDayIcon = view.findViewById<View>(R.id.lowest_day_icon) as TextView
        //        lowestDayIcon.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestDayText = view.findViewById<View>(R.id.lowest_day_text) as TextView
        //        lowestDayText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestDayExpenseTV = view.findViewById<View>(R.id.lowest_day_expense) as TextView
        //        lowestDayExpenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestDayRecord = view.findViewById<View>(R.id.lowest_day_sum) as TextView
        //        lowestDayRecord.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        lowestDays = view.findViewById<View>(R.id.lowest_days) as ExpandedListView
        lowestDaysLayout = view.findViewById<View>(R.id.expand_lowest_day) as RelativeLayout
        lowestDayMore = view.findViewById<View>(R.id.lowest_day_more) as LinearLayout
        lowestDayMore!!.setOnClickListener(this)
        lowestDayMoreText = view.findViewById<View>(R.id.lowest_day_more_text) as TextView
        lowestDayMoreText!!.typeface = coCoinUtil.getTypeface()
        lowestDays!!.onItemClickListener = this
        averageDayText = view.findViewById<View>(R.id.average_day_text) as TextView
        //        averageDayText.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        averageDayExpenseTV = view.findViewById<View>(R.id.average_day_expense) as TextView
        //        averageDayExpenseTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        averageDayRecordTV = view.findViewById<View>(R.id.average_day_sum) as TextView
        //        averageDayRecordTV.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        foot = view.findViewById<View>(R.id.foot) as TextView
        //        foot.setTypeface(coCoinUtil.getInstance().typefaceLatoLight);
        foot!!.visibility = View.GONE
        if (IS_EMPTY) {
            emptyTip!!.visibility = View.GONE
        }
//        button = view.findViewById<View>(R.id.button) as FloatingActionButton
        binding.button.setOnClickListener(this)
        GetSelectListData(false).execute()
    }

    fun showDataDialog() {
        if (selectListData == null) GetSelectListData(true).execute() else showSelectListDataDialog()
    }

    override fun onDestroy() {
        super.onDestroy()

        reportPeriodDialog?.dismiss()
//        RefWatcher refWatcher = CoCoinApplication.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onClick(v: View) {
        var selectedValue: SelectedValue? = null
        when (v.id) {
            R.id.icon_right -> {
                if (lastPieSelectedPosition != -1) {
                    pieSelectedPosition = lastPieSelectedPosition
                }
                pieSelectedPosition = ((pieSelectedPosition - 1 + pieChartData!!.values.size)
                        % pieChartData!!.values.size)
                selectedValue = SelectedValue(
                    pieSelectedPosition,
                    0,
                    SelectedValue.SelectedValueType.NONE)
                pie!!.selectValue(selectedValue)
            }
            R.id.icon_left -> {
                if (lastPieSelectedPosition != -1) {
                    pieSelectedPosition = lastPieSelectedPosition
                }
                pieSelectedPosition = ((pieSelectedPosition + 1)
                        % pieChartData!!.values.size)
                selectedValue = SelectedValue(
                    pieSelectedPosition,
                    0,
                    SelectedValue.SelectedValueType.NONE)
                pie!!.selectValue(selectedValue)
            }
            R.id.highest_first -> onItemClick(highestTags, highestTags!!.getChildAt(0), -1, -1)
            R.id.highest_tag_more -> {}
            R.id.lowest_first -> onItemClick(lowestTags, lowestTags!!.getChildAt(0), -1, -1)
            R.id.lowest_tag_more -> {}
            R.id.icon_left_line -> {
                if (lastLineSelectedPosition != -1) {
                    lineSelectedPosition = lastLineSelectedPosition
                }
                lineSelectedPosition =
                    ((lineSelectedPosition - 1 + lineChartData!!.lines[0].values.size)
                            % lineChartData!!.lines[0].values.size)
                selectedValue = SelectedValue(
                    0,
                    lineSelectedPosition,
                    SelectedValue.SelectedValueType.NONE)
                line!!.selectValue(selectedValue)
            }
            R.id.icon_right_line -> {
                if (lastLineSelectedPosition != -1) {
                    lineSelectedPosition = lastLineSelectedPosition
                }
                lineSelectedPosition = ((lineSelectedPosition + 1)
                        % lineChartData!!.lines[0].values.size)
                selectedValue = SelectedValue(
                    0,
                    lineSelectedPosition,
                    SelectedValue.SelectedValueType.NONE)
                line!!.selectValue(selectedValue)
            }
            R.id.highest_first_month -> onItemClick(highestMonths,
                highestMonths!!.getChildAt(0),
                -1,
                -1)
            R.id.highest_last_month -> onItemClick(highestMonths,
                highestMonths!!.getChildAt(0),
                10,
                -1)
            R.id.highest_month_more -> {}
            R.id.highest_first_day -> onItemClick(highestDays, highestDays!!.getChildAt(0), -1, -1)
            R.id.highest_day_more -> {}
            R.id.lowest_first_day -> onItemClick(lowestDays, lowestDays!!.getChildAt(0), -1, -1)
            R.id.lowest_day_more -> {}
            R.id.button -> if (!isEmpty) showSelectListDataDialog()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        var dialogTitle = ""
        val expense: Int
        val tagId: Int
        val month: Int
        val day: Int
        val tempFrom = Calendar.getInstance()
        val tempTo = Calendar.getInstance()
        when (parent!!.id) {
            R.id.highest_tags -> {
                if (gettingData) return
                tagId = highestTagExpense!![position + 1][2].toInt()
                expense = highestTagExpense!![position + 1][0].toInt()
                dialogTitle = if ("zh" == coCoinUtil.getLanguage()) {
                    if (selectYear) {
                        from[Calendar.YEAR].toString() + "年" + "\n" +
                                coCoinUtil.getSpendString(expense) +
                                "于" + coCoinUtil.getTagName(tagId)
                    } else {
                        from[Calendar.YEAR].toString() + "年" + (from[Calendar.MONTH] + 1) + "月" + "\n" +
                                coCoinUtil.getSpendString(expense) +
                                "于" + coCoinUtil.getTagName(tagId)
                    }
                } else {
                    if (selectYear) {
                        coCoinUtil.getSpendString(expense) + " in " + from[Calendar.YEAR] + "\n" +
                                "on " + coCoinUtil.getTagName(tagId)
                    } else {
                        coCoinUtil.getSpendString(expense) + " in " + coCoinUtil.getMonthShort(
                            from[Calendar.MONTH] + 1) + " " + from[Calendar.YEAR] + "\n" +
                                "on " + coCoinUtil.getTagName(tagId)
                    }
                }
                GetData(from, to, tagId, dialogTitle).execute()
            }
            R.id.lowest_tags -> {
                if (gettingData) return
                tagId = lowestTagExpense!![position + 1][2].toInt()
                expense = lowestTagExpense!![position + 1][0].toInt()
                dialogTitle = if ("zh" == coCoinUtil.getLanguage()) {
                    if (selectYear) {
                        from[Calendar.YEAR].toString() + "年" + "\n" +
                                coCoinUtil.getSpendString(expense) +
                                "于" + coCoinUtil.getTagName(tagId)
                    } else {
                        from[Calendar.YEAR].toString() + "年" + (from[Calendar.MONTH] + 1) + "月" + "\n" +
                                coCoinUtil.getSpendString(expense) +
                                "于" + coCoinUtil.getTagName(tagId)
                    }
                } else {
                    if (selectYear) {
                        coCoinUtil.getSpendString(expense) + " in " + from[Calendar.YEAR] + "\n" +
                                "on " + coCoinUtil.getTagName(tagId)
                    } else {
                        coCoinUtil.getSpendString(expense) + " in " + coCoinUtil.getMonthShort(
                            from[Calendar.MONTH] + 1) + " " + from[Calendar.YEAR] + "\n" +
                                "on " + coCoinUtil.getTagName(tagId)
                    }
                }
                GetData(from, to, tagId, dialogTitle).execute()
            }
            R.id.highest_month -> {
                if (gettingData) return
                expense = highestMonthExpense!![position + 1][3].toInt()
                month = highestMonthExpense!![position + 1][1].toInt()
                dialogTitle = if ("zh" == coCoinUtil.getLanguage()) {
                    from[Calendar.YEAR].toString() + "年" + coCoinUtil.getMonthShort(month + 1) + "\n" + coCoinUtil.getSpendString(
                        expense)
                } else {
                    coCoinUtil.getSpendString(expense) + "\nin " + from[Calendar.YEAR] + " " + coCoinUtil.getMonthShort(
                        month + 1)
                }
                tempFrom[reportYear, month, 1, 0, 0] = 0
                tempFrom.add(Calendar.SECOND, 0)
                tempTo[reportYear, month, tempFrom.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59] =
                    59
                tempTo.add(Calendar.SECOND, 0)
                GetData(tempFrom, tempTo, Int.MIN_VALUE, dialogTitle).execute()
            }
            R.id.highest_days -> {
                if (gettingData) return
                expense = highestDayExpense!![position + 1][3].toInt()
                month = highestDayExpense!![position + 1][1].toInt()
                day = highestDayExpense!![position + 1][2].toInt()
                dialogTitle = if ("zh" == coCoinUtil.getLanguage()) {
                    from[Calendar.YEAR].toString() + "年" + coCoinUtil.getMonthShort(month + 1) + day + "\n" + coCoinUtil.getSpendString(
                        expense)
                } else {
                    coCoinUtil.getSpendString(expense) + "\nin " + from[Calendar.YEAR] + " " + coCoinUtil.getMonthShort(
                        month + 1) + " " + day
                }
                tempFrom[reportYear, month, day, 0, 0] = 0
                tempFrom.add(Calendar.SECOND, 0)
                tempTo[reportYear, month, day, 23, 59] = 59
                tempTo.add(Calendar.SECOND, 0)
                GetData(tempFrom, tempTo, Int.MIN_VALUE, dialogTitle).execute()
            }
            R.id.lowest_days -> {
                if (gettingData) return
                expense = lowestDayExpense!![position + 1][3].toInt()
                month = lowestDayExpense!![position + 1][1].toInt()
                day = lowestDayExpense!![position + 1][2].toInt()
                dialogTitle = if ("zh" == coCoinUtil.getLanguage()) {
                    from[Calendar.YEAR].toString() + "年" + coCoinUtil.getMonthShort(month + 1) + day + "\n" + coCoinUtil.getSpendString(
                        expense)
                } else {
                    coCoinUtil.getSpendString(expense) + "\nin " + from[Calendar.YEAR] + " " + coCoinUtil.getMonthShort(
                        month + 1) + " " + day
                }
                tempFrom[reportYear, month, day, 0, 0] = 0
                tempFrom.add(Calendar.SECOND, 0)
                tempTo[reportYear, month, day, 23, 59] = 59
                tempTo.add(Calendar.SECOND, 0)
                GetData(tempFrom, tempTo, Int.MIN_VALUE, dialogTitle).execute()
            }
        }
    }

    // get select list for dialog
    private lateinit var progressDialog: MaterialDialog

    inner class GetSelectListData(private val openDialog: Boolean) :
        AsyncTask<String?, Void?, String?>() {
        init {
            progressDialog = MaterialDialog.Builder(mContext!!)
                .title(R.string.report_loading_select_list_title)
                .content(R.string.report_loading_select_list_content)
                .cancelable(false)
                .progress(true, 0)
                .show()
        }

        protected override fun doInBackground(vararg params: String?): String? {
            selectListData = ArrayList()
            val size = RecordManager.RECORDS.size
            var currentYearSelectListPosition = -1
            var currentMonthSelectListPosition = -1
            var currentYear = -1
            var currentMonth = -1
            for (i in size - 1 downTo 0) {
                val record = RecordManager.RECORDS[i]
                if (record.calendar[Calendar.YEAR] != currentYear) {
                    val newYearSelectList = doubleArrayOf(record.calendar[Calendar.YEAR].toDouble(),
                        -1.0,
                        1.0,
                        record.money)
                    selectListData!!.add(newYearSelectList)
                    currentYearSelectListPosition = selectListData!!.size - 1
                    currentYear = record.calendar[Calendar.YEAR]
                    // if the year is different, we have to add new year and month
                    val newMonthSelectList =
                        doubleArrayOf(record.calendar[Calendar.YEAR].toDouble(),
                            (record.calendar[Calendar.MONTH] + 1).toDouble(),
                            1.0,
                            record.money)
                    selectListData!!.add(newMonthSelectList)
                    currentMonthSelectListPosition = selectListData!!.size - 1
                    currentMonth = record.calendar[Calendar.MONTH]
                } else {
                    if (record.calendar[Calendar.MONTH] != currentMonth) {
                        selectListData!![currentYearSelectListPosition][2]++
                        selectListData!![currentYearSelectListPosition][3] += record.money
                        val newMonthSelectList =
                            doubleArrayOf(record.calendar[Calendar.YEAR].toDouble(),
                                (record.calendar[Calendar.MONTH] + 1).toDouble(),
                                1.0,
                                record.money)
                        selectListData!!.add(newMonthSelectList)
                        currentMonthSelectListPosition = selectListData!!.size - 1
                        currentMonth = record.calendar[Calendar.MONTH]
                    } else {
                        selectListData!![currentYearSelectListPosition][2]++
                        selectListData!![currentYearSelectListPosition][3] += record.money
                        selectListData!![currentMonthSelectListPosition][2]++
                        selectListData!![currentMonthSelectListPosition][3] += record.money
                    }
                }
            }
            //            if (BuildConfig.DEBUG) {
//                for (int i = 0; i < selectListData.size(); i++) {
//                    Log.d("CoCoin", "Select List Data: " + selectListData.get(i)[0] + " " + selectListData.get(i)[1] + " " + selectListData.get(i)[2] + " " + selectListData.get(i)[3]);
//                }
//            }
            return null
        }

        override fun onPostExecute(result: String?) {
            if (progressDialog != null) progressDialog!!.cancel()
            if (openDialog) showSelectListDataDialog()
        }
    }

//    private var selectListDataAdapter: DialogSelectListDataAdapter? = null
    private fun showSelectListDataDialog() {
        val selectListDataAdapter =
            DialogSelectListDataAdapter(coCoinUtil, selectListData, object :
                DialogSelectListDataAdapter.ListItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    Timber.d("onItemClick, view: $view, position: $position")
                    reportPeriodDialog?.dismiss()
                    makeReport(position)
                }
            })
//        if (selectListDataAdapter == null) {
//            selectListDataAdapter = DialogSelectListDataAdapter(coCoinUtil, selectListData)
//        }
    reportPeriodDialog?.dismiss()
        reportPeriodDialog = MaterialDialog.Builder(mContext!!)
            .title(R.string.report_select_list_title)
            .cancelable(false)
            .negativeText(R.string.cancel)
            .adapter(selectListDataAdapter, null)
            .build()

//    object: MaterialDialog.ListCallback() {
//                override fun onSelection(
//                    dialog: MaterialDialog?,
//                    itemView: View?,
//                    position: Int,
//                    text: CharSequence?,
//                ) {
//                    dialog.dismiss();
//                    makeReport(position);
//                }
//
//            })
//                                    new MaterialDialog.ListCallback() {
//                                        @Override
//                                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
//                                            dialog.dismiss();
//                                            makeReport(which);
//                                        }
//                                    })
    reportPeriodDialog?.show()
    }

    private var selectYear = false
    private fun makeReport(p: Int) {
        progressDialog = MaterialDialog.Builder(mContext!!)
            .title(R.string.report_loading_select_list_title)
            .content(R.string.report_loading_select_list_content)
            .cancelable(false)
            .progress(true, 0)
            .show()
        if (selectListData!![p][1] == -1.0) {
            // select year
            selectYear = true
            from[selectListData!![p][0].toInt(), 0, 1, 0, 0] = 0
            from.add(Calendar.SECOND, 0)
            to[selectListData!![p][0].toInt(), 11, 31, 23, 59] = 59
            to.add(Calendar.SECOND, 0)
            GetReport(from, to, true).execute()
        } else {
            // select month
            selectYear = false
            from[selectListData!![p][0].toInt(), selectListData!![p][1].toInt() - 1, 1, 0, 0] = 0
            from.add(Calendar.SECOND, 0)
            to[selectListData!![p][0].toInt(), selectListData!![p][1].toInt() - 1, from.getActualMaximum(
                Calendar.DAY_OF_MONTH), 23, 59] = 59
            to.add(Calendar.SECOND, 0)
            GetReport(from, to, false).execute()
        }
    }

    var reportYear = -1
    var reportMonth = -1
    var expense = 0.0
    var records = 0
    var tags = 0
    var pieChartData: PieChartData? = null

    // expense, percent, tagId, records
    var highestTagExpense: ArrayList<DoubleArray>? = null
    var lowestTagExpense: ArrayList<DoubleArray>? = null

    // expense and percent on clothes, food, house and traffic
    var needExpense: ArrayList<DoubleArray>? = null

    // expense, percent, tagId except the above four tags
    var needlessExpense: ArrayList<DoubleArray>? = null
    var lineChartData: LineChartData? = null

    // year, month, day of month, expense and percent of diff months
    var highestMonthExpense: ArrayList<DoubleArray>? = null
    var lowestMonthExpense: ArrayList<DoubleArray>? = null
    var averageMonthExpense = -1.0
    var averageMonthRecord = 0

    // year, month, day of month, expense and percent of diff days, most @param MAX_DAY_EXPENSE days
    var highestDayExpense: ArrayList<DoubleArray>? = null
    var lowestDayExpense: ArrayList<DoubleArray>? = null
    var averageDayExpense = -1.0
    var averageDayRecord = 0

    inner class GetReport(
        private val from: Calendar,
        private val to: Calendar,
        private val isYear: Boolean
    ) : AsyncTask<String?, Void?, String?>() {
        protected override fun doInBackground(vararg params: String?): String? {
            expense = 0.0
            records = 0
            tags = 0
            highestTagExpense = ArrayList()
            lowestTagExpense = ArrayList()
            needExpense = ArrayList()
            for (i in 0..3) {
                val aTag = doubleArrayOf(0.0, 0.0)
                needExpense!!.add(aTag)
            }
            if (isYear) {
                highestMonthExpense = ArrayList()
                lowestMonthExpense = ArrayList()
            }
            highestDayExpense = ArrayList()
            lowestDayExpense = ArrayList()
            val tagExpense = DoubleArray(RecordManager.TAGS.size + 1)
            for (i in tagExpense.indices.reversed()) tagExpense[i] = 0.0
            val tagRecords = DoubleArray(RecordManager.TAGS.size + 1)
            for (i in tagRecords.indices.reversed()) tagRecords[i] = 0.0

            // month and expense
            val monthExpense = ArrayList<DoubleArray>()
            for (i in 0..11) {
                val aMonth = doubleArrayOf(i.toDouble(), 0.0, 0.0)
                monthExpense.add(aMonth)
            }
            reportYear = to[Calendar.YEAR]
            reportMonth = to[Calendar.MONTH] + 1

            // month, day and expense
            val dayExpense = Array(12) { DoubleArray(32) }
            for (i in 0..11) {
                for (j in 1..31) {
                    dayExpense[i][j] = 0.0
                }
            }
            // month, day and records
            val dayRecord = Array(12) { DoubleArray(32) }
            for (i in 0..11) {
                for (j in 1..31) {
                    dayRecord[i][j] = 0.0
                }
            }
            val size = RecordManager.RECORDS.size
            for (i in size - 1 downTo 0) {
                val record = RecordManager.RECORDS[i]
                if (record.calendar.before(from)) break
                if (!record.calendar.after(to)) {
                    for (j in i downTo 0) {
                        val r = RecordManager.RECORDS[j]
                        if (r.calendar.before(from)) {
                            break
                        }
                        // here is the record we need
                        expense += r.money
                        records++
                        tagExpense[r.tag] += r.money
                        tagRecords[r.tag]++
                        if (isYear) {
                            monthExpense[r.calendar[Calendar.MONTH]][1] += r.money
                            monthExpense[r.calendar[Calendar.MONTH]][2]++
                            dayExpense[r.calendar[Calendar.MONTH]][r.calendar[Calendar.DAY_OF_MONTH]] += r.money
                            dayRecord[r.calendar[Calendar.MONTH]][r.calendar[Calendar.DAY_OF_MONTH]]++
                        } else {
                            dayExpense[r.calendar[Calendar.MONTH]][r.calendar[Calendar.DAY_OF_MONTH]] += r.money
                            dayRecord[r.calendar[Calendar.MONTH]][r.calendar[Calendar.DAY_OF_MONTH]]++
                        }
                    }
                    break
                }
            }
            for (i in tagExpense.indices) {
                if (tagExpense[i] != 0.0) {
                    val cfht = coCoinUtil.IsCFHT(i)
                    if (cfht != -1) {
                        needExpense!![cfht][0] += tagExpense[i]
                    }
                    tags++
                    val aTag = doubleArrayOf(tagExpense[i],
                        tagExpense[i] / expense,
                        i.toDouble(),
                        tagRecords[i])
                    highestTagExpense!!.add(aTag)
                    lowestTagExpense!!.add(aTag)
                }
            }
            for (i in 0..3) needExpense!![i][1] = needExpense!![i][0] / expense
            Collections.sort(highestTagExpense, object : Comparator<DoubleArray> {
                override fun compare(lhs: DoubleArray, rhs: DoubleArray): Int {
                    return java.lang.Double.compare(rhs[0], lhs[0])
                }
            })
            Collections.sort(lowestTagExpense, object : Comparator<DoubleArray> {
                override fun compare(lhs: DoubleArray, rhs: DoubleArray): Int {
                    return java.lang.Double.compare(lhs[0], rhs[0])
                }
            })
            // use tag expense values to generate pie data
            val sliceValues = ArrayList<SliceValue>()
            for (i in lowestTagExpense!!.indices) {
                val sliceValue = SliceValue(lowestTagExpense!![i][0].toFloat(),
                    coCoinUtil.getTagColor(
                        lowestTagExpense!![i][2].toInt()))
                sliceValue.setLabel(lowestTagExpense!![i][2].toInt().toString())
                sliceValues.add(sliceValue)
            }
            pieChartData = PieChartData(sliceValues)
            pieChartData!!.setHasLabels(false)
            pieChartData!!.setHasLabelsOnlyForSelected(false)
            pieChartData!!.setHasLabelsOutside(false)
            pieChartData!!.setHasCenterCircle(SettingManager.getInstance().isHollow)
            if (isYear) {
                Collections.sort(monthExpense, object : Comparator<DoubleArray> {
                    override fun compare(lhs: DoubleArray, rhs: DoubleArray): Int {
                        return java.lang.Double.compare(rhs[1], lhs[1])
                    }
                })
                for (i in 0..11) {
                    val aMonth = doubleArrayOf(reportYear.toDouble(),
                        monthExpense[i][0],
                        -1.0,
                        monthExpense[i][1],
                        monthExpense[i][1] / expense,
                        monthExpense[i][2])
                    highestMonthExpense!!.add(aMonth)
                }
                for (i in 11 downTo 0) {
                    val aMonth = doubleArrayOf(reportYear.toDouble(),
                        monthExpense[i][0],
                        -1.0,
                        monthExpense[i][1],
                        monthExpense[i][1] / expense,
                        monthExpense[i][2])
                    lowestMonthExpense!!.add(aMonth)
                }
                averageMonthExpense = expense / 12
                averageMonthRecord = records / 12
                val dayExpense2 = ArrayList<DoubleArray>()
                for (i in 0..11) {
                    val calendar = Calendar.getInstance()
                    calendar[reportYear, i, 1, 0, 0] = 0
                    calendar.add(Calendar.SECOND, 0)
                    val dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    for (j in 1..dayOfMonth) {
                        val aDay = doubleArrayOf(i.toDouble(),
                            j.toDouble(),
                            dayExpense[i][j],
                            dayRecord[i][j])
                        dayExpense2.add(aDay)
                    }
                }
                Collections.sort(dayExpense2, object : Comparator<DoubleArray> {
                    override fun compare(lhs: DoubleArray, rhs: DoubleArray): Int {
                        return java.lang.Double.compare(rhs[2], lhs[2])
                    }
                })
                for (i in 0 until MAX_DAY_EXPENSE) {
                    if (i >= dayExpense2.size || dayExpense2[i][2] == 0.0) break
                    val aDay = doubleArrayOf(reportYear.toDouble(),
                        dayExpense2[i][0],
                        dayExpense2[i][1],
                        dayExpense2[i][2],
                        dayExpense2[i][2] / expense,
                        dayExpense2[i][3])
                    highestDayExpense!!.add(aDay)
                }
                var counter = min(dayExpense2.size, MAX_DAY_EXPENSE)
                for (i in dayExpense2.indices.reversed()) {
                    if (dayExpense2[i][2] > 0) {
                        val aDay = doubleArrayOf(reportYear.toDouble(),
                            dayExpense2[i][0],
                            dayExpense2[i][1],
                            dayExpense2[i][2],
                            dayExpense2[i][2] / expense,
                            dayExpense2[i][3])
                        lowestDayExpense!!.add(aDay)
                        counter--
                        if (counter == 0) break
                    }
                }
                val calendar = Calendar.getInstance()
                calendar[reportYear, 0, 1, 0, 0] = 0
                calendar.add(Calendar.SECOND, 0)
                averageDayExpense = expense / calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
                averageDayRecord = records / calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
            } else {
                val dayExpense2 = ArrayList<DoubleArray>()
                for (i in 0..11) {
                    val calendar = Calendar.getInstance()
                    calendar[reportYear, i, 1, 0, 0] = 0
                    calendar.add(Calendar.SECOND, 0)
                    val dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    for (j in 1..dayOfMonth) {
                        val aDay = doubleArrayOf(i.toDouble(),
                            j.toDouble(),
                            dayExpense[i][j],
                            dayRecord[i][j])
                        dayExpense2.add(aDay)
                    }
                }
                Collections.sort(dayExpense2, object : Comparator<DoubleArray> {
                    override fun compare(lhs: DoubleArray, rhs: DoubleArray): Int {
                        return java.lang.Double.compare(rhs[2], lhs[2])
                    }
                })
                for (i in 0 until MAX_DAY_EXPENSE) {
                    if (i >= dayExpense2.size || dayExpense2[i][2] == 0.0) break
                    val aDay = doubleArrayOf(reportYear.toDouble(),
                        dayExpense2[i][0],
                        dayExpense2[i][1],
                        dayExpense2[i][2],
                        dayExpense2[i][2] / expense,
                        dayExpense2[i][3])
                    highestDayExpense!!.add(aDay)
                }
                var counter = min(dayExpense2.size, MAX_DAY_EXPENSE)
                for (i in dayExpense2.indices.reversed()) {
                    if (dayExpense2[i][2] > 0) {
                        val aDay = doubleArrayOf(reportYear.toDouble(),
                            dayExpense2[i][0],
                            dayExpense2[i][1],
                            dayExpense2[i][2],
                            dayExpense2[i][2] / expense,
                            dayExpense2[i][3])
                        lowestDayExpense!!.add(aDay)
                        counter--
                        if (counter == 0) break
                    }
                }
                val calendar = Calendar.getInstance()
                calendar[reportYear, reportMonth - 1, 1, 0, 0] = 0
                calendar.add(Calendar.SECOND, 0)
                averageDayExpense = expense / calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                averageDayRecord = records / calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            }

            // use month/day expense values to generate line data
            if (isYear) {
                val lines: MutableList<Line> = ArrayList()
                for (i in 0..0) {
                    val values: MutableList<PointValue> = ArrayList()
                    for (j in 0..11) {
                        for (k in 0..11) {
                            if (monthExpense[k][0] == j.toDouble()) {
                                values.add(PointValue(j.toFloat(), monthExpense[k][1].toFloat()))
                                break
                            }
                        }
                    }
                    val line = Line(values)
                    line.color =
                        ContextCompat.getColor(CoCoinApplication.getAppContext(), R.color.red)
                    line.shape = ValueShape.CIRCLE
                    line.isCubic = false
                    line.isFilled = false
                    line.setHasLabels(false)
                    line.setHasLabelsOnlyForSelected(false)
                    line.setHasLines(true)
                    line.setHasPoints(true)
                    lines.add(line)
                }
                lineChartData = LineChartData(lines)
                val axisX = Axis()
                val axisY = Axis().setHasLines(true)
                axisX.name = reportYear.toString() + ""
                lineChartData!!.axisXBottom = axisX
                lineChartData!!.axisYLeft = axisY
                val axisValues = ArrayList<AxisValue>()
                for (i in 0..11) {
                    val axisValue = AxisValue(i.toFloat())
                    axisValue.setLabel((i + 1).toString() + "")
                    axisValues.add(axisValue)
                }
                lineChartData!!.axisXBottom.values = axisValues
            } else {
                val calendar = Calendar.getInstance()
                calendar[reportYear, reportMonth - 1, 1, 0, 0] = 0
                calendar.add(Calendar.SECOND, 0)
                val days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val lines: MutableList<Line> = ArrayList()
                for (i in 0..0) {
                    val values: MutableList<PointValue> = ArrayList()
                    for (j in 0 until days) {
                        values.add(PointValue(j.toFloat(),
                            dayExpense[reportMonth - 1][j + 1].toFloat()))
                    }
                    val line = Line(values)
                    line.color =
                        ContextCompat.getColor(CoCoinApplication.getAppContext(), R.color.red)
                    line.shape = ValueShape.CIRCLE
                    line.isCubic = false
                    line.isFilled = false
                    line.setHasLabels(false)
                    line.setHasLabelsOnlyForSelected(false)
                    line.setHasLines(true)
                    line.setHasPoints(true)
                    lines.add(line)
                }
                lineChartData = LineChartData(lines)
                val axisX = Axis()
                val axisY = Axis().setHasLines(true)
                axisX.name = reportYear.toString() + " " + coCoinUtil.getMonthShort(reportMonth)
                lineChartData!!.axisXBottom = axisX
                lineChartData!!.axisYLeft = axisY
                val axisValues = ArrayList<AxisValue>()
                for (i in 0 until days) {
                    val axisValue = AxisValue(i.toFloat())
                    axisValue.setLabel((i + 1).toString() + "")
                    axisValues.add(axisValue)
                }
                lineChartData!!.axisXBottom.values = axisValues
            }
            return null
        }

        override fun onPostExecute(result: String?) {

            // for title
            if (isYear) REPORT_TITLE = reportYear.toString() + "" else REPORT_TITLE =
                "$reportYear - $reportMonth"
            try {
                Timber.w("Should we call onTitleChanged()?")
                //(activity as OnTitleChangedListener).onTitleChanged()
            } catch (cce: ClassCastException) {
                cce.printStackTrace()
            }

            // for title
            if (selectYear) {
                if ("zh" == coCoinUtil.getLanguage()) {
                    title!!.text =
                        " ● " + reportYear + "年" + CoCoinApplication.getAppContext().resources.getString(
                            R.string.report_view_foot)
                } else {
                    title!!.text =
                        " ● " + reportYear + " " + CoCoinApplication.getAppContext().resources.getString(
                            R.string.report_view_foot)
                }
            } else {
                if ("zh" == coCoinUtil.getLanguage()) {
                    title!!.text =
                        " ● " + reportYear + "年" + coCoinUtil.getMonthShort(reportMonth) + CoCoinApplication.getAppContext().resources.getString(
                            R.string.report_view_foot)
                } else {
                    title!!.text =
                        " ● " + reportYear + " " + coCoinUtil.getMonthShort(reportMonth) + CoCoinApplication.getAppContext().resources.getString(
                            R.string.report_view_foot)
                }
            }

            // for basic information
            expenseTV!!.text = coCoinUtil.getInMoney(expense.toInt())
            if ("zh" == coCoinUtil.getLanguage()) {
                tagsTV!!.text =
                    records.toString() + CoCoinApplication.getAppContext().resources.getString(R.string.report_view_records) + tags + CoCoinApplication.getAppContext().resources.getString(
                        R.string.report_view_tags)
            } else {
                tagsTV!!.text =
                    records.toString() + " " + CoCoinApplication.getAppContext().resources.getString(
                        R.string.report_view_records) + " " + tags + " " + CoCoinApplication.getAppContext().resources.getString(
                        R.string.report_view_tags)
            }
            emptyTip!!.visibility = View.GONE

            // for pie
            pieLayout!!.visibility = View.VISIBLE
            pie!!.visibility = View.VISIBLE
            pie!!.pieChartData = pieChartData

            // for highest tag expense
            highestTagLayout!!.visibility = View.VISIBLE
            highestTagIcon!!.setImageDrawable(coCoinUtil.getTagIconDrawable(highestTagExpense!![0][2].toInt()))
            highestTagText!!.text =
                coCoinUtil.getTagName(highestTagExpense!![0][2].toInt()) + coCoinUtil.getPurePercentString(
                    highestTagExpense!![0][1] * 100)
            highestTagExpenseTV!!.text = coCoinUtil.getInMoney(highestTagExpense!![0][0].toInt())
            highestTagRecord!!.text = coCoinUtil.getInRecords(highestTagExpense!![0][3].toInt())
            highestTagsAdapter = ReportTagAdapter(coCoinUtil, highestTagExpense)
            highestTags!!.adapter = highestTagsAdapter

            // for lowest tag expense
            lowestTagLayout!!.visibility = View.VISIBLE
            lowestTagIcon!!.setImageDrawable(coCoinUtil.getTagIconDrawable(lowestTagExpense!![0][2].toInt()))
            lowestTagText!!.text =
                coCoinUtil.getTagName(lowestTagExpense!![0][2].toInt()) + coCoinUtil.getPurePercentString(
                    lowestTagExpense!![0][1] * 100)
            lowestTagExpenseTV!!.text = coCoinUtil.getInMoney(lowestTagExpense!![0][0].toInt())
            lowestTagRecord!!.text = coCoinUtil.getInRecords(lowestTagExpense!![0][3].toInt())
            lowestTagsAdapter = ReportTagAdapter(coCoinUtil, lowestTagExpense)
            lowestTags!!.adapter = lowestTagsAdapter

            // for line
            lineLayout!!.visibility = View.VISIBLE
            line!!.visibility = View.VISIBLE
            line!!.lineChartData = lineChartData

            // for month
            if (selectYear) {
                highestMonthLayout!!.visibility = View.VISIBLE
                highestFirstIcon!!.setBackgroundResource(backgroundResource)
                highestFirstIcon!!.text = (highestMonthExpense!![0][1].toInt() + 1).toString() + ""
                highestFirstText!!.text =
                    coCoinUtil.getMonthShort(highestMonthExpense!![0][1].toInt() + 1) + " " + reportYear + coCoinUtil.getPurePercentString(
                        highestMonthExpense!![0][4] * 100)
                highestFirstExpenseTV!!.text =
                    coCoinUtil.getInMoney(highestMonthExpense!![0][3].toInt())
                highestFirstRecord!!.text =
                    coCoinUtil.getInRecords(highestMonthExpense!![0][5].toInt())
                highestLastIcon!!.setBackgroundResource(backgroundResource)
                highestLastIcon!!.text = (highestMonthExpense!![11][1].toInt() + 1).toString() + ""
                highestLastText!!.text =
                    coCoinUtil.getMonthShort(highestMonthExpense!![11][1].toInt() + 1) + " " + reportYear + coCoinUtil.getPurePercentString(
                        highestMonthExpense!![11][4] * 100)
                highestLastExpenseTV!!.text =
                    coCoinUtil.getInMoney(highestMonthExpense!![11][3].toInt())
                highestLastRecord!!.text =
                    coCoinUtil.getInRecords(highestMonthExpense!![11][5].toInt())
                highestMonthsAdapter =
                    ReportMonthAdapter(coCoinUtil, highestMonthExpense, reportYear)
                highestMonths!!.adapter = highestMonthsAdapter

                // for average day expense
                averageMonthExpenseTV!!.text = coCoinUtil.getInMoney(averageMonthExpense.toInt())
                averageMonthRecordTV!!.text = coCoinUtil.getInRecords(averageMonthRecord)
            } else {
                highestMonthLayout!!.visibility = View.GONE
            }

            // for highest day expense
            highestDayLayout!!.visibility = View.VISIBLE
            highestDayIcon!!.setBackgroundResource(backgroundResource)
            highestDayIcon!!.text = highestDayExpense!![0][2].toInt().toString() + ""
            highestDayText!!.text =
                coCoinUtil.getCalendarStringDayExpenseSort(CoCoinApplication.getAppContext(),
                    highestDayExpense!![0][0].toInt(),
                    highestDayExpense!![0][1].toInt() + 1,
                    highestDayExpense!![0][2].toInt()) + coCoinUtil.getPurePercentString(
                    highestDayExpense!![0][4] * 100)
            highestDayExpenseTV!!.text = coCoinUtil.getInMoney(highestDayExpense!![0][3].toInt())
            highestDayRecord!!.text = coCoinUtil.getInRecords(highestDayExpense!![0][5].toInt())
            highestDaysAdapter = ReportDayAdapter(coCoinUtil, highestDayExpense, reportMonth)
            highestDays!!.adapter = highestDaysAdapter

            // for lowest day expense
            lowestDayLayout!!.visibility = View.VISIBLE
            lowestDayIcon!!.setBackgroundResource(backgroundResource)
            lowestDayIcon!!.text = lowestDayExpense!![0][2].toInt().toString() + ""
            lowestDayText!!.text =
                coCoinUtil.getCalendarStringDayExpenseSort(CoCoinApplication.getAppContext(),
                    lowestDayExpense!![0][0].toInt(),
                    lowestDayExpense!![0][1].toInt() + 1,
                    lowestDayExpense!![0][2].toInt()) + coCoinUtil.getPurePercentString(
                    lowestDayExpense!![0][4] * 100)
            lowestDayExpenseTV!!.text = coCoinUtil.getInMoney(lowestDayExpense!![0][3].toInt())
            lowestDayRecord!!.text = coCoinUtil.getInRecords(lowestDayExpense!![0][5].toInt())
            lowestDaysAdapter = ReportDayAdapter(coCoinUtil, lowestDayExpense, reportMonth)
            lowestDays!!.adapter = lowestDaysAdapter

            // for average day expense
            averageDayExpenseTV!!.text = coCoinUtil.getInMoney(averageDayExpense.toInt())
            averageDayRecordTV!!.text = coCoinUtil.getInRecords(averageDayRecord)

            // for foot
            foot!!.visibility = View.VISIBLE
            if (selectYear) {
                if ("zh" == coCoinUtil.getLanguage()) {
                    foot!!.text =
                        " ● " + reportYear + "年" + CoCoinApplication.getAppContext().resources.getString(
                            R.string.report_view_foot)
                } else {
                    foot!!.text =
                        " ● " + reportYear + " " + CoCoinApplication.getAppContext().resources.getString(
                            R.string.report_view_foot)
                }
            } else {
                if ("zh" == coCoinUtil.getLanguage()) {
                    foot!!.text =
                        " ● " + reportYear + "年" + coCoinUtil.getMonthShort(reportMonth) + CoCoinApplication.getAppContext().resources.getString(
                            R.string.report_view_foot)
                } else {
                    foot!!.text =
                        " ● " + reportYear + " " + coCoinUtil.getMonthShort(reportMonth) + CoCoinApplication.getAppContext().resources.getString(
                            R.string.report_view_foot)
                }
            }
            if (progressDialog != null) progressDialog!!.dismiss()
        }
    }

    private var selectedRecord: ArrayList<CoCoinRecord>? = null
    private var gettingData = false

    inner class GetData(fromDate: Calendar, toDate: Calendar, tagId: Int, dialogTitle: String) :
        AsyncTask<String?, Void?, String?>() {
        private val fromDate: Calendar
        private val toDate: Calendar
        private val tagId: Int
        private val dialogTitle: String

        init {
            gettingData = true
            this.fromDate = fromDate
            this.tagId = tagId
            this.toDate = toDate
            this.dialogTitle = dialogTitle
            progressDialog = MaterialDialog.Builder(mContext!!)
                .title(R.string.report_loading_select_list_title)
                .content(R.string.report_loading_select_list_content)
                .cancelable(false)
                .progress(true, 0)
                .show()
        }

        protected override fun doInBackground(vararg params: String?): String? {
            selectedRecord = ArrayList()
            val size = RecordManager.RECORDS.size
            for (i in size - 1 downTo 0) {
                val record = RecordManager.RECORDS[i]
                if (record.calendar.before(fromDate)) break
                if (!record.calendar.after(toDate)) {
                    for (j in i downTo 0) {
                        val r = RecordManager.RECORDS[j]
                        if (r.calendar.before(fromDate)) {
                            break
                        }
                        if (tagId == Int.MIN_VALUE || r.tag == tagId) selectedRecord!!.add(r)
                    }
                    break
                }
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            gettingData = false
            if (progressDialog != null) progressDialog!!.dismiss()
            (mContext as FragmentActivity?)!!.supportFragmentManager
                .beginTransaction()
                .add(RecordCheckDialogFragment(
                    mContext, selectedRecord, dialogTitle), "MyDialog")
                .commit()
        }
    }

    interface OnTitleChangedListener {
        fun onTitleChanged()
    }

    private val backgroundResource: Int
        private get() {
            val random = Random()
            return when (random.nextInt(6)) {
                0 -> R.drawable.bg_month_icon_big_0
                1 -> R.drawable.bg_month_icon_big_1
                2 -> R.drawable.bg_month_icon_big_2
                3 -> R.drawable.bg_month_icon_big_3
                4 -> R.drawable.bg_month_icon_big_4
                5 -> R.drawable.bg_month_icon_big_5
                else -> R.drawable.bg_month_icon_big_0
            }
        }

    private fun min(a: Int, b: Int): Int {
        return if (a < b) a else b
    }

    companion object {
        @JvmField
        var REPORT_TITLE = ""
        const val MAX_TAG_EXPENSE = 8
        const val MAX_DAY_EXPENSE = 10
        @JvmStatic
        fun newInstance(): ExpenseReportsFragment {
            return ExpenseReportsFragment()
        }
    }
}