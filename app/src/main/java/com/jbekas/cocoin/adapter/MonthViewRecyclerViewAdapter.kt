package com.jbekas.cocoin.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.fragment.RecordCheckDialogFragment
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.RecordManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.util.CoCoinUtil
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
import com.nispok.snackbar.listeners.ActionClickListener

import net.steamcrafted.materialiconlib.MaterialIconView

import java.util.Calendar

import com.jbekas.cocoin.databinding.ItemMonthListViewBinding
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SelectedValue
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.view.PieChartView

class MonthViewRecyclerViewAdapter(
    start: Int,
    end: Int,
    private val mContext: Context,
    private val fragmentPosition: Int,
    monthNumber: Int
) : RecyclerView.Adapter<MonthViewRecyclerViewAdapter.MVviewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_CELL = 1
    }

    private val list = mutableListOf<CoCoinRecord>()
    private val sliceValuesList = mutableListOf<List<SliceValue>>()
    private val tagExpenseList = mutableListOf<Map<Int, Double>>()
    private val expenseList = mutableListOf<Map<Int, MutableList<CoCoinRecord>>>()
    private val pieChartDataList = mutableListOf<PieChartData>()
    private val sumList = mutableListOf<Double>()
    private var records = 0
    private var tags = 0
    private val selectedPositionList = mutableListOf<Int>()
    private val dateStringList = mutableListOf<String>()
    private val dateShownStringList = mutableListOf<String>()
    private var dialogTitle = ""
    private var startYear = 0
    private var startMonth = 0
    private var isEmpty = false

    init {
        val recordManager = RecordManager.getInstance(mContext.applicationContext)

        if (start != -1) {
            for (i in start downTo end) {
                list.add(RecordManager.RECORDS[i])
            }
        }

        isEmpty = list.isEmpty()

        if (!isEmpty) {
            startYear = RecordManager.RECORDS[0].calendar.get(Calendar.YEAR)
            startMonth = RecordManager.RECORDS[0].calendar.get(Calendar.MONTH)

            val nowYear = startYear + (startMonth + (monthNumber - fragmentPosition - 1)) / 12
            val nowMonth = (startMonth + (monthNumber - fragmentPosition - 1)) % 12

            var tagExpense = mutableMapOf<Int, Double>()
            var expense = mutableMapOf<Int, MutableList<CoCoinRecord>>()
            var sliceValues = mutableListOf<SliceValue>()
            var sum = 0.0

            // for this month
            dateStringList.add("${CoCoinUtil.GetMonthShort(nowMonth + 1)} $nowYear")
            dateShownStringList.add(" in ${CoCoinUtil.MONTHS_SHORT[nowMonth + 1]} $nowYear")
            selectedPositionList.add(0)

            for (j in 2 until RecordManager.TAGS.size) {
                tagExpense[RecordManager.TAGS[j].id] = 0.0
                expense[RecordManager.TAGS[j].id] = mutableListOf()
            }

            for (coCoinRecord in list) {
                if (coCoinRecord.calendar.get(Calendar.MONTH) == nowMonth) {
                    tagExpense[coCoinRecord.tag] = tagExpense[coCoinRecord.tag]!! + coCoinRecord.money
                    expense[coCoinRecord.tag]!!.add(coCoinRecord)
                    sum += coCoinRecord.money
                    records++
                }
            }

            val initialSortedTagExpense = CoCoinUtil.SortTreeMapByValues(tagExpense)
            tagExpense = initialSortedTagExpense.toMutableMap()

            tags = 0
            for ((key, value) in tagExpense) {
                if (value >= 1) {
                    val sliceValue = SliceValue(
                        value.toFloat(),
                        mContext.resources.getColor(CoCoinUtil.GetTagColorResource(key))
                    )
                    sliceValue.setLabel(key.toString())
                    sliceValues.add(sliceValue)
                    tags++
                }
            }
            sliceValuesList.add(sliceValues)

            tagExpenseList.add(tagExpense)
            expenseList.add(expense)
            sumList.add(sum)
            val pieChartData = PieChartData(sliceValues)
            pieChartData.setHasLabels(false)
            pieChartData.setHasLabelsOnlyForSelected(false)
            pieChartData.setHasLabelsOutside(false)
            pieChartData.setHasCenterCircle(SettingManager.getInstance().getIsHollow())
            pieChartDataList.add(pieChartData)

            // for each week
            var now = Calendar.getInstance()
            now.set(nowYear, nowMonth, 1, 0, 0, 0)
            now.add(Calendar.SECOND, 0)

            val monthEnd = Calendar.getInstance()
            monthEnd.set(nowYear, nowMonth, now.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            monthEnd.add(Calendar.SECOND, 0)

            while (!now.after(monthEnd)) {
                val leftWeekRange = CoCoinUtil.GetThisWeekLeftRange(now)
                val rightWeekRange = CoCoinUtil.GetThisWeekRightRange(now)
                val rightShownWeekRange = CoCoinUtil.GetThisWeekRightShownRange(now)
                val dateString = "${CoCoinUtil.GetMonthShort(leftWeekRange.get(Calendar.MONTH) + 1)} " +
                        "${leftWeekRange.get(Calendar.DAY_OF_MONTH)} " +
                        "${leftWeekRange.get(Calendar.YEAR)} - " +
                        "${CoCoinUtil.GetMonthShort(rightShownWeekRange.get(Calendar.MONTH) + 1)} " +
                        "${rightShownWeekRange.get(Calendar.DAY_OF_MONTH)} " +
                        "${rightShownWeekRange.get(Calendar.YEAR)}"
                dateStringList.add(dateString)
                dateShownStringList.add(" from " +
                        "${CoCoinUtil.GetMonthShort(leftWeekRange.get(Calendar.MONTH) + 1)} " +
                        "${leftWeekRange.get(Calendar.DAY_OF_MONTH)} to " +
                        "${CoCoinUtil.GetMonthShort(rightShownWeekRange.get(Calendar.MONTH) + 1)} " +
                        "${rightShownWeekRange.get(Calendar.DAY_OF_MONTH)}")
                selectedPositionList.add(0)

                sum = 0.0
                tagExpense = mutableMapOf()
                expense = mutableMapOf()
                sliceValues = mutableListOf()

                for (j in 2 until RecordManager.TAGS.size) {
                    tagExpense[RecordManager.TAGS[j].id] = 0.0
                    expense[RecordManager.TAGS[j].id] = mutableListOf()
                }

                for (coCoinRecord in list) {
                    if (!coCoinRecord.calendar.before(leftWeekRange) &&
                        coCoinRecord.calendar.before(rightWeekRange)
                    ) {
                        tagExpense[coCoinRecord.tag] = tagExpense[coCoinRecord.tag]!! + coCoinRecord.money
                        expense[coCoinRecord.tag]!!.add(coCoinRecord)
                        sum += coCoinRecord.money
                    }
                }

                val sortedTagExpense = CoCoinUtil.SortTreeMapByValues(tagExpense)
                tagExpense = sortedTagExpense.toMutableMap()

                for ((key, value) in tagExpense) {
                    if (value >= 1) {
                        val sliceValue = SliceValue(
                            value.toFloat(),
                            mContext.resources.getColor(CoCoinUtil.GetTagColorResource(key))
                        )
                        sliceValue.setLabel(key.toString())
                        sliceValues.add(sliceValue)
                    }
                }
                sliceValuesList.add(sliceValues)

                tagExpenseList.add(tagExpense)
                expenseList.add(expense)
                sumList.add(sum)
                val weekPieChartData = PieChartData(sliceValues)
                weekPieChartData.setHasLabels(false)
                weekPieChartData.setHasLabelsOnlyForSelected(false)
                weekPieChartData.setHasLabelsOutside(false)
                weekPieChartData.setHasCenterCircle(SettingManager.getInstance().getIsHollow())
                pieChartDataList.add(weekPieChartData)

                now = CoCoinUtil.GetNextWeekLeftRange(now)
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_list_view, parent, false)
        return MVviewHolder(view)
    }

    override fun onBindViewHolder(holder: MVviewHolder, position: Int) {
        if (isEmpty) {
            holder.expanseSum.text = "¥0"
            holder.emptyTip.text = mContext.resources.getString(R.string.tag_empty)
            holder.tags.visibility = View.GONE
            holder.date.visibility = View.INVISIBLE
            holder.pie.visibility = View.INVISIBLE
            holder.iconLeft.visibility = View.INVISIBLE
            holder.iconRight.visibility = View.INVISIBLE
            holder.all.visibility = View.GONE
        } else {
            holder.date.text = dateStringList[position]
            holder.expanseSum.text = CoCoinUtil.GetInMoney(sumList[position].toInt())

            val tagsText = if (CoCoinUtil.GetLanguage() == "zh") {
                " ● $records${CoCoinApplication.getAppContext().resources.getString(R.string.report_view_records)}$tags${CoCoinApplication.getAppContext().resources.getString(R.string.report_view_tags)}"
            } else {
                " ● $records ${CoCoinApplication.getAppContext().resources.getString(R.string.report_view_records)} $tags ${CoCoinApplication.getAppContext().resources.getString(R.string.report_view_tags)}"
            }
            holder.tags.text = tagsText

            if (sumList[position] == 0.0) {
                holder.emptyTip.visibility = View.VISIBLE
            } else {
                holder.emptyTip.visibility = View.GONE
            }

            holder.pie.pieChartData = pieChartDataList[position]
            holder.pie.setOnValueTouchListener(PieValueTouchListener(position))
            holder.pie.isChartRotationEnabled = false

            if (sumList[position] != 0.0) {
                holder.iconRight.visibility = View.VISIBLE
                holder.iconRight.setOnClickListener {
                    selectedPositionList[position] = (selectedPositionList[position] + 1) % sliceValuesList[position].size
                    val selectedValue = SelectedValue(
                        selectedPositionList[position],
                        0,
                        SelectedValue.SelectedValueType.NONE
                    )
                    holder.pie.selectValue(selectedValue)
                }
                holder.iconLeft.visibility = View.VISIBLE
                holder.iconLeft.setOnClickListener {
                    selectedPositionList[position] = (selectedPositionList[position] - 1 + sliceValuesList[position].size) % sliceValuesList[position].size
                    val selectedValue = SelectedValue(
                        selectedPositionList[position],
                        0,
                        SelectedValue.SelectedValueType.NONE
                    )
                    holder.pie.selectValue(selectedValue)
                }
            } else {
                holder.iconLeft.visibility = View.GONE
                holder.iconRight.visibility = View.GONE
            }

            holder.all.setOnClickListener {
                dialogTitle = if (CoCoinUtil.GetLanguage() == "zh") {
                    "${mContext.resources.getString(R.string.`in`)}${dateStringList[position]} ${CoCoinUtil.GetSpendString(sumList[position].toInt())}"
                } else {
                    "${CoCoinUtil.GetSpendString(sumList[position].toInt())}${mContext.resources.getString(R.string.`in`)} ${dateStringList[position]}"
                }
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .add(
                        RecordCheckDialogFragment(mContext, list, dialogTitle),
                        "MyDialog"
                    )
                    .commit()
            }
        }
    }

    class MVviewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemMonthListViewBinding.bind(view)
        val date: TextView = binding.date
        val expanseSum: TextView = binding.expanse
        val tags: TextView = binding.tags
        val emptyTip: TextView = binding.emptyTip
        val pie: PieChartView = binding.chartPie
        val iconLeft: MaterialIconView = binding.iconLeft
        val iconRight: MaterialIconView = binding.iconRight
        val all: MaterialIconView = binding.all
    }

    private inner class PieValueTouchListener(private val position: Int) : PieChartOnValueSelectListener {

        override fun onValueSelected(i: Int, sliceValue: SliceValue) {
            val tagId = sliceValue.labelAsChars.toString().toInt()
            val percent = sliceValue.value / sumList[position].toFloat() * 100

            val text = if (CoCoinUtil.GetLanguage() == "zh") {
                "${CoCoinUtil.GetSpendString(sliceValue.value.toInt())}${CoCoinUtil.GetPercentString(percent.toDouble())}\n于${CoCoinUtil.GetTagName(tagId)}"
            } else {
                "${CoCoinUtil.GetSpendString(sliceValue.value.toInt())}${CoCoinUtil.GetPercentString(percent.toDouble())}\nin ${CoCoinUtil.GetTagName(RecordManager.TAGS[tagId].id)}"
            }

            dialogTitle = if (CoCoinUtil.GetLanguage() == "zh") {
                "${mContext.resources.getString(R.string.`in`)}${dateStringList[position]} ${CoCoinUtil.GetSpendString(sliceValue.value.toInt())}\n于${CoCoinUtil.GetTagName(tagId)}"
            } else {
                "${CoCoinUtil.GetSpendString(sliceValue.value.toInt())}${mContext.resources.getString(R.string.`in`)} ${dateStringList[position]}\nin ${CoCoinUtil.GetTagName(RecordManager.TAGS[tagId].id)}"
            }

            val snackbar = Snackbar
                .with(mContext)
                .type(SnackbarType.MULTI_LINE)
                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                .position(Snackbar.SnackbarPosition.BOTTOM)
                .margin(15, 15)
                .backgroundDrawable(CoCoinUtil.GetSnackBarBackground(fragmentPosition - 2))
                .text(text)
                .textTypeface(CoCoinUtil.GetTypeface())
                .textColor(Color.WHITE)
                .actionLabelTypeface(CoCoinUtil.GetTypeface())
                .actionLabel(mContext.resources.getString(R.string.check))
                .actionColor(Color.WHITE)
                .actionListener(object : ActionClickListener {
                    override fun onActionClicked(snackbar: Snackbar) {
                        val shownCoCoinRecords = expenseList[position][tagId]!!
                        (mContext as FragmentActivity).supportFragmentManager
                            .beginTransaction()
                            .add(
                                RecordCheckDialogFragment(mContext, shownCoCoinRecords, dialogTitle),
                                "MyDialog"
                            )
                            .commit()
                    }
                })
            SnackbarManager.show(snackbar)
        }

        override fun onValueDeselected() {
            // Empty implementation
        }
    }
}