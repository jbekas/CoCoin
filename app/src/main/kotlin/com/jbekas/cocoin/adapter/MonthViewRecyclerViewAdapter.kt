package com.jbekas.cocoin.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.adapter.MonthViewRecyclerViewAdapter.MVviewHolder
import com.jbekas.cocoin.databinding.ItemMonthListViewBinding
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.fragment.RecordCheckDialogFragment
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.util.CoCoinUtil
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SelectedValue
import lecho.lib.hellocharts.model.SliceValue
import java.util.*

// Todo optimise this
class MonthViewRecyclerViewAdapter(
    private val activity: Activity,
    private val coCoinUtil: CoCoinUtil,
    start: Int,
    end: Int,
    position: Int,
    monthNumber: Int,
) : RecyclerView.Adapter<MVviewHolder?>() {
    //    private val mContext: Context
    private val list: MutableList<CoCoinRecord>
    private val fragmentPosition: Int
    private var sliceValuesList: ArrayList<List<SliceValue>>? = null
    private var TagExpanseList: ArrayList<Map<Int, Double>>? = null
    private var ExpanseList: ArrayList<Map<Int, MutableList<CoCoinRecord>>>? = null
    private var pieChartDataList: ArrayList<PieChartData>? = null
    private var SumList: ArrayList<Double>? = null
    private var records = 0
    private var tags = 0
    private var selectedPositionList: ArrayList<Int>? = null
    private var dateStringList: ArrayList<String>? = null
    private var dateShownStringList: ArrayList<String>? = null
    private var dialogTitle: String? = null
    private var startYear = 0
    private var startMonth = 0
    private var IS_EMPTY = false

    init {
        list = ArrayList()
        fragmentPosition = position
        val recordManager = RecordManager.getInstance(activity.applicationContext)
        if (start != -1) {
            for (i in start downTo end) {
                list.add(RecordManager.RECORDS[i])
            }
        }
        IS_EMPTY = list.isEmpty()
        if (!IS_EMPTY) {
            startYear = RecordManager.RECORDS[0].calendar[Calendar.YEAR]
            startMonth = RecordManager.RECORDS[0].calendar[Calendar.MONTH]
            sliceValuesList = ArrayList()
            TagExpanseList = ArrayList()
            ExpanseList = ArrayList()
            pieChartDataList = ArrayList()
            SumList = ArrayList()
            selectedPositionList = ArrayList()
            dateStringList = ArrayList()
            dateShownStringList = ArrayList()
            val nowYear = startYear + (startMonth + (monthNumber - fragmentPosition - 1)) / 12
            val nowMonth = (startMonth + (monthNumber - fragmentPosition - 1)) % 12
            var TagExpanse: MutableMap<Int, Double>
            var Expanse: MutableMap<Int, MutableList<CoCoinRecord>>
            var sliceValues: MutableList<SliceValue>
            var pieChartData: PieChartData
            var Sum = 0.0
            TagExpanse = TreeMap()
            Expanse = HashMap()
            sliceValues = ArrayList()

            // for this month
            dateStringList!!.add(coCoinUtil.getMonthShort(nowMonth + 1) + " " + nowYear)
            dateShownStringList!!.add(" in " + coCoinUtil.MONTHS_SHORT[nowMonth + 1] + " " + nowYear)
            selectedPositionList!!.add(0)
            for (j in 2 until RecordManager.TAGS.size) {
                TagExpanse[RecordManager.TAGS[j].id] = java.lang.Double.valueOf(0.0)
                Expanse[RecordManager.TAGS[j].id] = ArrayList()
            }
            for (coCoinRecord in list) {
                if (coCoinRecord.calendar[Calendar.MONTH] == nowMonth) {
                    TagExpanse[coCoinRecord.tag] =
                        (TagExpanse[coCoinRecord.tag] ?: 0.toDouble()) + java.lang.Double.valueOf(
                            coCoinRecord.money)
                    val expanseList = Expanse[coCoinRecord.tag] ?: mutableListOf()
                    expanseList.add(coCoinRecord)
                    Expanse[coCoinRecord.tag] = expanseList
                    Sum += coCoinRecord.money
                    records++
                }
            }
            TagExpanse = coCoinUtil.sortTreeMapByValues(TagExpanse).toMutableMap()
            tags = 0
            for (entry in TagExpanse.entries) {
                //Timber.d("entry: %s", entry)
                if (entry.value >= 1) {
                    // Todo optimize the GetTagColorResource
                    val sliceValue = SliceValue(entry.value.toFloat(),
                        activity.resources.getColor(coCoinUtil.getTagColorResource(entry.key)))
                    sliceValue.setLabel(entry.key.toString())
                    sliceValues.add(sliceValue)
                    tags++
                }
            }
            sliceValuesList!!.add(sliceValues)
            TagExpanseList!!.add(TagExpanse)
            ExpanseList!!.add(Expanse)
            SumList!!.add(Sum)
            pieChartData = PieChartData(sliceValues)
            pieChartData.setHasLabels(false)
            pieChartData.setHasLabelsOnlyForSelected(false)
            pieChartData.setHasLabelsOutside(false)
            pieChartData.setHasCenterCircle(SettingManager.getInstance().isHollow)
            pieChartDataList!!.add(pieChartData)

            // for each week
            var now = Calendar.getInstance()
            now[nowYear, nowMonth, 1, 0, 0] = 0
            now.add(Calendar.SECOND, 0)
            val monthEnd = Calendar.getInstance()
            monthEnd[nowYear, nowMonth, now.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59] = 59
            monthEnd.add(Calendar.SECOND, 0)
            while (!now.after(monthEnd)) {
                val leftWeekRange = coCoinUtil.getThisWeekLeftRange(now)
                val rightWeekRange = coCoinUtil.getThisWeekRightRange(now)
                val rightShownWeekRange = coCoinUtil.getThisWeekRightShownRange(now)
                val dateString = coCoinUtil.getMonthShort(leftWeekRange[Calendar.MONTH] + 1) + " " +
                        leftWeekRange[Calendar.DAY_OF_MONTH] + " " +
                        leftWeekRange[Calendar.YEAR] + " - " +
                        coCoinUtil.getMonthShort(rightShownWeekRange[Calendar.MONTH] + 1) + " " +
                        rightShownWeekRange[Calendar.DAY_OF_MONTH] + " " +
                        rightShownWeekRange[Calendar.YEAR]
                dateStringList!!.add(dateString)
                dateShownStringList!!.add(" from " +
                        coCoinUtil.getMonthShort(leftWeekRange[Calendar.MONTH] + 1) + " " +
                        leftWeekRange[Calendar.DAY_OF_MONTH] + " to " +
                        coCoinUtil.getMonthShort(rightShownWeekRange[Calendar.MONTH] + 1) + " " +
                        rightShownWeekRange[Calendar.DAY_OF_MONTH])
                selectedPositionList!!.add(0)
                Sum = 0.0
                TagExpanse = TreeMap()
                Expanse = HashMap()
                sliceValues = ArrayList()
                for (j in 2 until RecordManager.TAGS.size) {
                    TagExpanse[RecordManager.TAGS[j].id] = java.lang.Double.valueOf(0.0)
                    Expanse[RecordManager.TAGS[j].id] = ArrayList()
                }
                for (coCoinRecord in list) {
                    if (!coCoinRecord.calendar.before(leftWeekRange) &&
                        coCoinRecord.calendar.before(rightWeekRange)
                    ) {
                        TagExpanse[coCoinRecord.tag] =
                            (TagExpanse[coCoinRecord.tag] ?: 0.toDouble()) + coCoinRecord.money
                        val expanseList = Expanse[coCoinRecord.tag] ?: mutableListOf()
                        expanseList.add(coCoinRecord)
                        Expanse[coCoinRecord.tag] = expanseList
                        Sum += coCoinRecord.money
                    }
                }
                TagExpanse = coCoinUtil.sortTreeMapByValues(TagExpanse).toMutableMap()
                for ((key, value) in TagExpanse) {
                    if (value >= 1) {
                        // Todo optimize the GetTagColorResource
                        val sliceValue = SliceValue(value.toFloat(),
                            activity.resources.getColor(coCoinUtil.getTagColorResource(key)))
                        sliceValue.setLabel(key.toString())
                        sliceValues.add(sliceValue)
                    }
                }
                sliceValuesList!!.add(sliceValues)
                TagExpanseList!!.add(TagExpanse)
                ExpanseList!!.add(Expanse)
                SumList!!.add(Sum)
                pieChartData = PieChartData(sliceValues)
                pieChartData.setHasLabels(false)
                pieChartData.setHasLabelsOnlyForSelected(false)
                pieChartData.setHasLabelsOutside(false)
                pieChartData.setHasCenterCircle(SettingManager.getInstance().isHollow)
                pieChartDataList!!.add(pieChartData)
                now = coCoinUtil.getNextWeekLeftRange(now)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            else -> TYPE_CELL
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MVviewHolder {
        var view: View? = null
        when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemMonthListViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MVviewHolder(binding)
            }
            TYPE_CELL -> {
                val binding = ItemMonthListViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MVviewHolder(binding)
            }
        }

        throw IllegalArgumentException("Unable to create ViewHolder for viewType($viewType)")
    }

    override fun onBindViewHolder(holder: MVviewHolder, position: Int) {
        if (IS_EMPTY) {
            holder.binding.expanse.text = "¥0"
            //            holder.expanseSum.setTypeface( coCoinUtil.typefaceLatoLight);
            holder.binding.emptyTip.text = activity.resources.getString(R.string.tag_empty)
            //            holder.emptyTip.setTypeface( coCoinUtil.GetTypeface());
            holder.binding.tags.visibility = View.GONE
            holder.binding.date.visibility = View.INVISIBLE
            holder.binding.chartPie.visibility = View.INVISIBLE
            holder.binding.iconLeft.visibility = View.INVISIBLE
            holder.binding.iconRight.visibility = View.INVISIBLE
            holder.binding.all.visibility = View.GONE
        } else {
            holder.binding.date.text = dateStringList!![position]
            holder.binding.expanse.text = coCoinUtil.getInMoney(SumList!![position].toInt())

//            holder.date.setTypeface( coCoinUtil.GetTypeface());
//            holder.expanseSum.setTypeface( coCoinUtil.typefaceLatoLight);

//            holder.tags.setTypeface( coCoinUtil.typefaceLatoLight);
            if ("zh" == coCoinUtil.getLanguage()) {
                holder.binding.tags.text =
                    " ● " + records + CoCoinApplication.getAppContext().resources.getString(R.string.report_view_records) + tags + CoCoinApplication.getAppContext().resources.getString(
                        R.string.report_view_tags)
            } else {
                holder.binding.tags.text =
                    " ● " + records + " " + CoCoinApplication.getAppContext().resources.getString(R.string.report_view_records) + " " + tags + " " + CoCoinApplication.getAppContext().resources.getString(
                        R.string.report_view_tags)
            }
            if (SumList!![position] == java.lang.Double.valueOf(0.0)) {
                holder.binding.emptyTip.visibility = View.VISIBLE
                //                holder.emptyTip.setTypeface( coCoinUtil.typefaceLatoLight);
            } else {
                holder.binding.emptyTip.visibility = View.GONE
            }
            holder.binding.chartPie.pieChartData = pieChartDataList!![position]
            holder.binding.chartPie.onValueTouchListener = PieValueTouchListener(position)
            holder.binding.chartPie.isChartRotationEnabled = false
            if (SumList!![position] != java.lang.Double.valueOf(0.0)) {
                holder.binding.iconRight.visibility = View.VISIBLE
                holder.binding.iconRight.setOnClickListener {
                    selectedPositionList!![position] = (
                            (selectedPositionList!![position] + 1)
                                    % sliceValuesList!![position].size)
                    val selectedValue = SelectedValue(
                        selectedPositionList!![position],
                        0,
                        SelectedValue.SelectedValueType.NONE)
                    holder.binding.chartPie.selectValue(selectedValue)
                }
                holder.binding.iconLeft.visibility = View.VISIBLE
                holder.binding.iconLeft.setOnClickListener {
                    selectedPositionList!![position] = (
                            (selectedPositionList!![position] - 1
                                    + sliceValuesList!![position].size)
                                    % sliceValuesList!![position].size)
                    val selectedValue = SelectedValue(
                        selectedPositionList!![position],
                        0,
                        SelectedValue.SelectedValueType.NONE)
                    holder.binding.chartPie.selectValue(selectedValue)
                }
            } else {
                holder.binding.iconLeft.visibility = View.GONE
                holder.binding.iconRight.visibility = View.GONE
            }
            holder.binding.all.setOnClickListener {
                val tagName = coCoinUtil.getSpendString(SumList!![position].toInt())
                val dateString = dateStringList!![position]
                dialogTitle = "$tagName in $dateString"
                (activity as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .add(RecordCheckDialogFragment(
                        activity, list, dialogTitle),
                        "MyDialog")
                    .commit()
            }
        }
    }

    class MVviewHolder constructor(val binding: ItemMonthListViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private inner class PieValueTouchListener(private val position: Int) :
        PieChartOnValueSelectListener {
        override fun onValueSelected(i: Int, sliceValue: SliceValue) {
            val tagId = Integer.valueOf(String(sliceValue.labelAsChars))
            val percent = sliceValue.value / SumList!![position] * 100
            val spendString = coCoinUtil.getSpendString(sliceValue.value.toInt())
            val percentString = coCoinUtil.getPercentString(percent)
            val tag = coCoinUtil.getTagById(tagId)
            val tagName = if (tag != null) {
                coCoinUtil.getTagName(tag.id)
            } else {
                "N/A"
            }
            val dateString = dateStringList!![position]
            val text = "$spendString $percentString in $tagName"
            dialogTitle = "$spendString in $dateString in $tagName"

            val snackbar = Snackbar
                .with(activity)
                .type(SnackbarType.MULTI_LINE)
                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                .position(Snackbar.SnackbarPosition.BOTTOM)
                .margin(15, 15)
                .backgroundDrawable(coCoinUtil.getSnackBarBackground(fragmentPosition - 2))
                .text(text)
                .textTypeface(coCoinUtil.getTypeface())
                .textColor(Color.WHITE)
                .actionLabelTypeface(coCoinUtil.getTypeface())
                .actionLabel(activity.resources.getString(R.string.check))
                .actionColor(Color.WHITE)
                .actionListener {
                    val shownCoCoinRecords: List<CoCoinRecord> = ExpanseList!![position][tagId]!!
                    (activity as FragmentActivity).supportFragmentManager
                        .beginTransaction()
                        .add(RecordCheckDialogFragment(
                            activity, shownCoCoinRecords, dialogTitle),
                            "MyDialog")
                        .commit()
                }
            SnackbarManager.show(snackbar)
        }

        override fun onValueDeselected() {}
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_CELL = 1
    }
}