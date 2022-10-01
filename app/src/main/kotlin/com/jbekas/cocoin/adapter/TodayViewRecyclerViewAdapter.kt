package com.jbekas.cocoin.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.jbekas.cocoin.R
import com.jbekas.cocoin.databinding.ItemTodayViewBodyBinding
import com.jbekas.cocoin.databinding.ItemTodayViewHeadBinding
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.db.RecordManager.Companion.getInstance
import com.jbekas.cocoin.fragment.RecordCheckDialogFragment
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.util.CoCoinUtil
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
import com.nispok.snackbar.listeners.ActionClickListener
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.AxisValue
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SelectedValue
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.model.SubcolumnValue
import java.util.*
import java.util.function.Function

class TodayViewRecyclerViewAdapter(
    private val activity: Activity,
    private val coCoinUtil: CoCoinUtil,
    start: Int,
    end: Int,
    private val fragmentPosition: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // the data of this fragment
    private val allData: ArrayList<CoCoinRecord>

    // store the sum of expenses of each tag
    private val TagExpanse = mutableMapOf<Int, Double>()

    // store the records of each tag
    private var Expanse = mutableMapOf<Int, MutableList<CoCoinRecord>>()

    // the original target value of the whole pie
    private lateinit var originalTargets: FloatArray

    // whether the data of this fragment is empty
    private val IS_EMPTY: Boolean

    // the sum of the whole pie
    private var Sum = 0.0

    // the number of columns in the histogram
    private var columnNumber = 0

    // the axis date value of the histogram(hour, day of week and month, month)
    private var axis_date = 0

    // the month number
    private var month = 0

    // the selected position of one part of the pie
    private var pieSelectedPosition = 0

    // the last selected position of one part of the pie
    private var lastPieSelectedPosition = -1

    // the last selected position of one part of the histogram
    private var lastHistogramSelectedPosition = -1

    // the date string on the footer and header
    private var dateString: String? = null

    // the date string shown in the dialog
    private var dateShownString: String? = null

    // the string shown in the dialog
    private var dialogTitle: String? = null

    // the selected tag in pie
    private var tagId = -1

    // the selected column in histogram
    private var timeIndex = 0
    private var dialogView: View? = null

    init {
        val recordManager = getInstance(activity.applicationContext)
        allData = ArrayList()
        if (start != -1) for (i in start downTo end) allData.add(RecordManager.RECORDS[i])
        IS_EMPTY = allData.isEmpty()
        setDateString()
        if (!IS_EMPTY) {
            if (fragmentPosition == TODAY || fragmentPosition == YESTERDAY) {
                columnNumber = 24
                axis_date = Calendar.HOUR_OF_DAY
            }
            if (fragmentPosition == THIS_WEEK || fragmentPosition == LAST_WEEK) {
                columnNumber = 7
                axis_date = Calendar.DAY_OF_WEEK
            }
            if (fragmentPosition == THIS_MONTH || fragmentPosition == LAST_MONTH) {
                columnNumber = allData[0].calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                axis_date = Calendar.DAY_OF_MONTH
            }
            if (fragmentPosition == THIS_YEAR || fragmentPosition == LAST_YEAR) {
                columnNumber = 12
                axis_date = Calendar.MONTH
            }
            originalTargets = FloatArray(columnNumber)
            for (i in 0 until columnNumber) originalTargets[i] = 0f
            var size = RecordManager.TAGS.size
            for (j in 2 until size) {
                TagExpanse.put(RecordManager.TAGS[j].id, java.lang.Double.valueOf(0.0))
                Expanse[RecordManager.TAGS[j].id] = ArrayList()
            }
            size = allData.size
            for (i in 0 until size) {
                val coCoinRecord = allData[i]
                var currentValue = 0.0
                if (TagExpanse.get(coCoinRecord.tag) != null) {
                    currentValue = TagExpanse.get(coCoinRecord.tag)!!
                }
                TagExpanse.put(coCoinRecord.tag,
                    currentValue + coCoinRecord.money)
                Expanse.computeIfAbsent(coCoinRecord.tag,
                    Function<Int, MutableList<CoCoinRecord>> { k: Int? -> ArrayList() })
                Expanse.get(coCoinRecord.tag)!!.add(coCoinRecord)
                Sum += coCoinRecord.money
                if (axis_date == Calendar.DAY_OF_WEEK) {
                    if (coCoinUtil.WEEK_START_WITH_SUNDAY) originalTargets[coCoinRecord.calendar[axis_date] - 1] += coCoinRecord.money.toFloat() else originalTargets[(coCoinRecord.calendar[axis_date] + 5) % 7] += coCoinRecord.money.toFloat()
                } else if (axis_date == Calendar.DAY_OF_MONTH) {
                    originalTargets[coCoinRecord.calendar[axis_date] - 1] += coCoinRecord.money.toFloat()
                } else {
                    originalTargets[coCoinRecord.calendar[axis_date]] += coCoinRecord.money.toFloat()
                }
            }
            TagExpanse.clear()
            TagExpanse.putAll(coCoinUtil.sortTreeMapByValues(TagExpanse))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (fragmentPosition == TODAY || fragmentPosition == YESTERDAY) {
            if (position == 0) TYPE_HEADER else TYPE_BODY
        } else TYPE_HEADER
    }

    override fun getItemCount(): Int {
        return if (fragmentPosition == TODAY || fragmentPosition == YESTERDAY) {
            allData.size + 1
        } else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemTodayViewHeadBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return HeaderViewHolder(binding)
            }
            TYPE_BODY -> {
                val binding = ItemTodayViewBodyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return BodyViewHolder(binding)
            }
        }
        throw IllegalArgumentException("Unable to create ViewHolder for viewType($viewType)")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> bindHeaderViewHolder(holder as HeaderViewHolder?, position)
            TYPE_BODY -> bindBodyViewHolder(holder as BodyViewHolder?, position)
        }
    }

    private fun bindHeaderViewHolder(holder: HeaderViewHolder?, position: Int) {
        holder!!.binding.date.text = dateString
        holder.binding.dateBottom.text = dateString
        holder.binding.expanse.text = coCoinUtil.getInMoney(Sum.toInt())

//                holder.date.setTypeface(coCoinUtil.GetTypeface());
//                holder.dateBottom.setTypeface(coCoinUtil.GetTypeface());
//                holder.expanseSum.setTypeface(coCoinUtil.typefaceLatoLight);
        if (IS_EMPTY) {
            holder.binding.emptyTip.visibility = View.VISIBLE
            holder.binding.emptyTip.setText(coCoinUtil.getTodayViewEmptyTip(fragmentPosition))
            //                    holder.emptyTip.setTypeface(coCoinUtil.GetTypeface());
            holder.binding.iconReset.visibility = View.GONE
            holder.binding.chartPie.visibility = View.GONE
            holder.binding.iconLeft.visibility = View.GONE
            holder.binding.iconRight.visibility = View.GONE
            holder.binding.histogram.visibility = View.GONE
            holder.binding.histogramIconLeft.visibility = View.GONE
            holder.binding.histogramIconRight.visibility = View.GONE
            holder.binding.all.visibility = View.GONE
            holder.binding.dateBottom.visibility = View.GONE
        } else {
            holder.binding.emptyTip.visibility = View.GONE
            val sliceValues = ArrayList<SliceValue>()
            for ((key, value) in TagExpanse!!) {
                if (value >= 1) {
                    val sliceValue = SliceValue(value.toFloat(),
                        activity.applicationContext.resources.getColor(coCoinUtil.getTagColorResource(
                            key)))
                    sliceValue.setLabel(key.toString())
                    sliceValues.add(sliceValue)
                }
            }
            val pieChartData = PieChartData(sliceValues)
            pieChartData.setHasLabels(false)
            pieChartData.setHasLabelsOnlyForSelected(false)
            pieChartData.setHasLabelsOutside(false)
            pieChartData.setHasCenterCircle(SettingManager.getInstance().isHollow)
            holder.binding.chartPie.pieChartData = pieChartData
            holder.binding.chartPie.isChartRotationEnabled = false

            // two control button of pie
            holder.binding.iconRight.visibility = View.VISIBLE
            holder.binding.iconRight.setOnClickListener {
                if (lastPieSelectedPosition != -1) {
                    pieSelectedPosition = lastPieSelectedPosition
                }
                pieSelectedPosition = ((pieSelectedPosition - 1 + sliceValues.size)
                        % sliceValues.size)
                val selectedValue = SelectedValue(
                    pieSelectedPosition,
                    0,
                    SelectedValue.SelectedValueType.NONE)
                holder.binding.chartPie.selectValue(selectedValue)
            }
            holder.binding.iconLeft.visibility = View.VISIBLE
            holder.binding.iconLeft.setOnClickListener {
                if (lastPieSelectedPosition != -1) {
                    pieSelectedPosition = lastPieSelectedPosition
                }
                pieSelectedPosition = ((pieSelectedPosition + 1)
                        % sliceValues.size)
                val selectedValue = SelectedValue(
                    pieSelectedPosition,
                    0,
                    SelectedValue.SelectedValueType.NONE)
                holder.binding.chartPie.selectValue(selectedValue)
            }
            val columns: MutableList<Column> = ArrayList()
            val columnChartData = ColumnChartData(columns)
            if (!(fragmentPosition == TODAY || fragmentPosition == YESTERDAY)) {
                for (i in 0 until columnNumber) {
                    if (lastHistogramSelectedPosition == -1 && originalTargets[i] == 0f) {
                        lastHistogramSelectedPosition = i
                    }
                    val value = SubcolumnValue(
                        originalTargets[i], coCoinUtil.getRandomColor())
                    val subcolumnValues: MutableList<SubcolumnValue> = ArrayList()
                    subcolumnValues.add(value)
                    val column = Column(subcolumnValues)
                    column.setHasLabels(false)
                    column.setHasLabelsOnlyForSelected(false)
                    columns.add(column)
                }
                val axisX = Axis()
                val axisValueList: MutableList<AxisValue> = ArrayList()
                for (i in 0 until columnNumber) {
                    axisValueList.add(
                        AxisValue(i.toFloat()).setLabel(coCoinUtil.getAxisDateName(axis_date, i)))
                }
                axisX.values = axisValueList
                val axisY = Axis().setHasLines(true)
                columnChartData.axisXBottom = axisX
                columnChartData.axisYLeft = axisY
                columnChartData.isStacked = true
                holder.binding.histogram.columnChartData = columnChartData
                holder.binding.histogram.isZoomEnabled = false

                // two control button of histogram
                holder.binding.histogramIconLeft.visibility = View.VISIBLE
                holder.binding.histogramIconLeft.setOnClickListener {
                    do {
                        lastHistogramSelectedPosition =
                            ((lastHistogramSelectedPosition - 1 + columnNumber)
                                    % columnNumber)
                    } while (columnChartData.columns[lastHistogramSelectedPosition]
                            .values[0].value == 0f
                    )
                    val selectedValue = SelectedValue(
                        lastHistogramSelectedPosition,
                        0,
                        SelectedValue.SelectedValueType.NONE)
                    holder.binding.histogram.selectValue(selectedValue)
                }
                holder.binding.histogramIconRight.visibility = View.VISIBLE
                holder.binding.histogramIconRight.setOnClickListener {
                    do {
                        lastHistogramSelectedPosition = ((lastHistogramSelectedPosition + 1)
                                % columnNumber)
                    } while (columnChartData.columns[lastHistogramSelectedPosition]
                            .values[0].value == 0f
                    )
                    val selectedValue = SelectedValue(
                        lastHistogramSelectedPosition,
                        0,
                        SelectedValue.SelectedValueType.NONE)
                    holder.binding.histogram.selectValue(selectedValue)
                }
            }
            if (fragmentPosition == TODAY || fragmentPosition == YESTERDAY) {
                holder.binding.histogramIconLeft.visibility = View.INVISIBLE
                holder.binding.histogramIconRight.visibility = View.INVISIBLE
                holder.binding.histogram.visibility = View.GONE
                holder.binding.dateBottom.visibility = View.GONE
                holder.binding.iconReset.visibility = View.GONE
            }

            // set value touch listener of pie
            holder.binding.chartPie.onValueTouchListener = object : PieChartOnValueSelectListener {
                override fun onValueSelected(p: Int, sliceValue: SliceValue) {
                    // snack bar
                    val recordManager = getInstance(activity.applicationContext)
                    val text: String
                    tagId = Integer.valueOf(String(sliceValue.labelAsChars))
                    val percent = sliceValue.value / Sum * 100
                    text = """Spend ${sliceValue.value.toInt()} (takes ${
                        String.format("%.2f",
                            percent)
                    }%)
in ${coCoinUtil.getTagName(tagId)}"""
                    dialogTitle = """
                Spend ${sliceValue.value.toInt()}$dateShownString
                in ${coCoinUtil.getTagName(tagId)}
                """.trimIndent()
                    val snackbar = Snackbar
                        .with(activity)
                        .type(SnackbarType.MULTI_LINE)
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .position(Snackbar.SnackbarPosition.BOTTOM)
                        .margin(15, 15)
                        .backgroundDrawable(coCoinUtil.getSnackBarBackground(
                            fragmentPosition - 2))
                        .text(text)
                        .textTypeface(coCoinUtil.getTypeface())
                        .textColor(Color.WHITE)
                        .actionLabelTypeface(coCoinUtil.getTypeface())
                        .actionLabel(activity.resources
                            .getString(R.string.check))
                        .actionColor(Color.WHITE)
                        .actionListener(mActionClickListenerForPie())
                    SnackbarManager.show(snackbar)
                    lastPieSelectedPosition = if (p == lastPieSelectedPosition) {
                        return
                    } else {
                        p
                    }
                    if (!(fragmentPosition == TODAY || fragmentPosition == YESTERDAY)) {

                        // histogram data
                        val targets = FloatArray(columnNumber)
                        for (i in 0 until columnNumber) targets[i] = 0f
                        for (i in Expanse!![tagId]!!.indices.reversed()) {
                            val coCoinRecord = Expanse!![tagId]!![i]
                            if (axis_date == Calendar.DAY_OF_WEEK) {
                                if (coCoinUtil.WEEK_START_WITH_SUNDAY) {
                                    targets[coCoinRecord.calendar[axis_date] - 1] += coCoinRecord.money.toFloat()
                                } else {
                                    targets[(coCoinRecord.calendar[axis_date] + 5) % 7] += coCoinRecord.money.toFloat()
                                }
                            } else if (axis_date == Calendar.DAY_OF_MONTH) {
                                targets[coCoinRecord.calendar[axis_date] - 1] += coCoinRecord.money.toFloat()
                            } else {
                                targets[coCoinRecord.calendar[axis_date]] += coCoinRecord.money.toFloat()
                            }
                        }
                        lastHistogramSelectedPosition = -1
                        for (i in 0 until columnNumber) {
                            if (lastHistogramSelectedPosition == -1 && targets[i] != 0f) {
                                lastHistogramSelectedPosition = i
                            }
                            columnChartData.columns[i].values[0].setTarget(targets[i])
                        }
                        holder.binding.histogram.startDataAnimation()
                    }
                }

                override fun onValueDeselected() {}
            }
            if (!(fragmentPosition == TODAY || fragmentPosition == YESTERDAY)) {

                // set value touch listener of histogram
                holder.binding.histogram.onValueTouchListener =
                    object : ColumnChartOnValueSelectListener {
                        override fun onValueSelected(
                            columnIndex: Int,
                            subcolumnIndex: Int, value: SubcolumnValue
                        ) {
                            lastHistogramSelectedPosition = columnIndex
                            timeIndex = columnIndex
                            // snack bar
                            val recordManager = getInstance(activity.applicationContext)
                            var text = coCoinUtil.getSpendString(value.value.toInt())
                            text += if (tagId != -1) // belongs a tag
                                """
                    ${getSnackBarDateString()}
                    in ${coCoinUtil.getTagName(tagId)}
                    """.trimIndent() else  // don't belong to any tag
                                """
                    
                    ${getSnackBarDateString()}
                    """.trimIndent()

                            // setting the snack bar and dialog title of histogram
                            dialogTitle = text
                            val snackbar = Snackbar
                                .with(activity)
                                .type(SnackbarType.MULTI_LINE)
                                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                .position(Snackbar.SnackbarPosition.BOTTOM)
                                .margin(15, 15)
                                .backgroundDrawable(coCoinUtil.getSnackBarBackground(
                                    fragmentPosition - 2))
                                .text(text)
                                .textTypeface(coCoinUtil.getTypeface())
                                .textColor(Color.WHITE)
                                .actionLabelTypeface(coCoinUtil.getTypeface())
                                .actionLabel(activity.resources
                                    .getString(R.string.check))
                                .actionColor(Color.WHITE)
                                .actionListener(mActionClickListenerForHistogram())
                            SnackbarManager.show(snackbar)
                        }

                        override fun onValueDeselected() {}
                    }
            }

            // set the listener of the reset button
            if (!(fragmentPosition == TODAY || fragmentPosition == YESTERDAY)) {
                holder.binding.iconReset.setOnClickListener {
                    tagId = -1
                    lastHistogramSelectedPosition = -1
                    for (i in 0 until columnNumber) {
                        if (lastHistogramSelectedPosition == -1
                            && originalTargets[i] != 0f
                        ) {
                            lastHistogramSelectedPosition = i
                        }
                        columnChartData.columns[i].values[0].setTarget(originalTargets[i])
                    }
                    holder.binding.histogram.startDataAnimation()
                }
            }

            // set the listener of the show all button
            holder.binding.all.setOnClickListener {
                (activity as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .add(RecordCheckDialogFragment(
                        activity, allData, getAllDataDialogTitle()), "MyDialog")
                    .commit()
            }
        }
    }

    private fun bindBodyViewHolder(holder: BodyViewHolder?, position: Int) {
        holder!!.binding.tagImage.setImageResource(
            coCoinUtil.getTagIcon(allData[position - 1].tag))
        holder.binding.money.text = allData[position - 1].money.toInt().toString() + ""
        holder.binding.cellDate.text = allData[position - 1].getCalendarString(coCoinUtil)
        holder.binding.remark.text = allData[position - 1].remark
        holder.binding.index.text = position.toString() + ""
        holder.binding.materialRippleLayout.setOnClickListener {
            val spend = allData[position - 1].money
            val tagId = allData[position - 1].tag
            val subTitle = "Spend " + spend.toInt() +
                    "in " + coCoinUtil.getTagName(tagId)
            val dialog = MaterialDialog.Builder(activity)
                .icon(coCoinUtil.getTagIconDrawable(allData[position - 1].tag)!!)
                .limitIconToDefaultSize()
                .title(subTitle)
                .customView(R.layout.dialog_a_record, true)
                .positiveText(android.R.string.ok)
                .show()
            dialogView = dialog.getCustomView()
            val remark = dialogView!!.findViewById<View>(R.id.remark) as TextView
            val date = dialogView!!.findViewById<View>(R.id.date) as TextView
            remark.text = allData[position - 1].remark
            date.text = allData[position - 1].getCalendarString(coCoinUtil)
        }
    }

    // Header view holder class
    class HeaderViewHolder internal constructor(var binding: ItemTodayViewHeadBinding) :
        RecyclerView.ViewHolder(
            binding.root)

    // Body view holder class
    class BodyViewHolder internal constructor(var binding: ItemTodayViewBodyBinding) :
        RecyclerView.ViewHolder(
            binding.root)

    // set the listener of the check button on the snack bar of pie
    private inner class mActionClickListenerForPie : ActionClickListener {
        override fun onActionClicked(snackbar: Snackbar) {
            val shownCoCoinRecords: List<CoCoinRecord> = Expanse!![tagId]!!
            (activity as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .add(RecordCheckDialogFragment(
                    activity, shownCoCoinRecords, dialogTitle), "MyDialog")
                .commit()
        }
    }

    // set the listener of the check button on the snack bar of histogram
    private inner class mActionClickListenerForHistogram : ActionClickListener {
        override fun onActionClicked(snackbar: Snackbar) {
            val shownCoCoinRecords = ArrayList<CoCoinRecord>()
            var index = timeIndex
            if (axis_date == Calendar.DAY_OF_WEEK) {
                if (coCoinUtil.WEEK_START_WITH_SUNDAY) index++ else if (index == 6) index =
                    1 else index += 2
            }
            if (fragmentPosition == THIS_MONTH || fragmentPosition == LAST_MONTH) index++
            if (tagId != -1) {
                for (i in Expanse!![tagId]!!.indices) if (Expanse!![tagId]!![i].calendar[axis_date] == index) shownCoCoinRecords.add(
                    Expanse!![tagId]!![i])
            } else {
                for (i in allData.indices) if (allData[i].calendar[axis_date] == index) shownCoCoinRecords.add(
                    allData[i])
            }
            (activity as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .add(RecordCheckDialogFragment(
                    activity, shownCoCoinRecords, dialogTitle), "MyDialog")
                .commit()
        }
    }// in Jan. 1// in Jan. 1// 在1月1日

    // on Jan. 1
// 在1月1日
    // on Jan. 1
// 在上周一
    // on last Monday
// 在周一
    // on Monday
// at 9 o'clock yesterday// at 9 o'clock today
    // set the dateString shown in snack bar in this fragment
    fun getSnackBarDateString() : String {
        when (fragmentPosition) {
            TODAY -> {
                // at 9 o'clock today
                    return activity.resources.getString(R.string.at) +
                            timeIndex + " " +
                            activity.resources.getString(R.string.o_clock) + " " +
                            activity.resources.getString(R.string.today_date_string)
            }
            YESTERDAY -> {
                // at 9 o'clock yesterday
                    return activity.resources.getString(R.string.at) +
                            timeIndex + " " +
                            activity.resources.getString(R.string.o_clock) + " " +
                            activity.resources.getString(R.string.yesterday_date_string)
            }
            THIS_WEEK -> {
                // 在周一
                // on Monday
                return activity.resources.getString(R.string.on) + " " + coCoinUtil.getWeekDay(timeIndex)
            }
            LAST_WEEK -> {
                // 在上周一
                // on last Monday
                return activity.resources.getString(R.string.on) + " " + activity.resources.getString(R.string.last) + " " + coCoinUtil.getWeekDay(timeIndex)
            }
            THIS_MONTH -> {
                // 在1月1日
                // on Jan. 1
                return activity.resources.getString(R.string.on) + " " +
                        coCoinUtil.getMonthShort(month) + " " +
                        (timeIndex + 1)
            }
            LAST_MONTH -> {
                // 在1月1日
                // on Jan. 1
                return activity.resources.getString(R.string.on) + " " +
                        coCoinUtil.getMonthShort(month) + " " +
                        (timeIndex + 1)
            }
            THIS_YEAR -> {
                // in Jan. 1
                    return activity.resources.getString(R.string.`in`) + " " +
                            coCoinUtil.getMonthShort(timeIndex + 1) + " " +
                            activity.resources.getString(R.string.this_year_date_string)
            }
            LAST_YEAR -> {
                // in Jan. 1
                    return activity.resources.getString(R.string.`in`) + " " +
                            coCoinUtil.getMonthShort(timeIndex + 1) + " " +
                            activity.resources.getString(R.string.last_year_date_string)
            }
            else -> {
                return ""
            }
        }
    }
    
    // set the dateString of this fragment
    private fun setDateString() {
        var basicTodayDateString: String
        var basicYesterdayDateString: String
        val today = Calendar.getInstance()
        val yesterday = coCoinUtil.getYesterdayLeftRange(today)
        basicTodayDateString = "--:-- "
        basicTodayDateString += (coCoinUtil.getMonthShort(today[Calendar.MONTH] + 1)
                + " " + today[Calendar.DAY_OF_MONTH] + " " +
                today[Calendar.YEAR])
        basicYesterdayDateString = "--:-- "
        basicYesterdayDateString += (coCoinUtil.getMonthShort(today[Calendar.MONTH] + 1)
                + " " + yesterday[Calendar.DAY_OF_MONTH] + " " +
                yesterday[Calendar.YEAR])
        when (fragmentPosition) {
            TODAY -> {
                dateString = basicTodayDateString.substring(6, basicTodayDateString.length)
                dateShownString = activity.resources.getString(R.string.today_date_string)
                month = today[Calendar.MONTH]
            }
            YESTERDAY -> {
                dateString = basicYesterdayDateString.substring(6, basicYesterdayDateString.length)
                dateShownString = activity.resources.getString(R.string.yesterday_date_string)
                month = yesterday[Calendar.MONTH]
            }
            THIS_WEEK -> {
                val leftWeekRange = coCoinUtil.getThisWeekLeftRange(today)
                val rightWeekRange = coCoinUtil.getThisWeekRightShownRange(today)
                dateString = (coCoinUtil.getMonthShort(leftWeekRange[Calendar.MONTH] + 1)
                        + " " + leftWeekRange[Calendar.DAY_OF_MONTH] + " " +
                        leftWeekRange[Calendar.YEAR] + " - " +
                        coCoinUtil.getMonthShort(rightWeekRange[Calendar.MONTH] + 1)
                        + " " + rightWeekRange[Calendar.DAY_OF_MONTH] + " " +
                        rightWeekRange[Calendar.YEAR])
                dateShownString = activity.resources.getString(R.string.this_week_date_string)
                month = -1
            }
            LAST_WEEK -> {
                val leftLastWeekRange = coCoinUtil.getLastWeekLeftRange(today)
                val rightLastWeekRange = coCoinUtil.getLastWeekRightShownRange(today)
                dateString = (coCoinUtil.getMonthShort(leftLastWeekRange[Calendar.MONTH] + 1)
                        + " " + leftLastWeekRange[Calendar.DAY_OF_MONTH] + " " +
                        leftLastWeekRange[Calendar.YEAR] + " - " +
                        coCoinUtil.getMonthShort(rightLastWeekRange[Calendar.MONTH] + 1)
                        + " " + rightLastWeekRange[Calendar.DAY_OF_MONTH] + " " +
                        rightLastWeekRange[Calendar.YEAR])
                dateShownString = activity.resources.getString(R.string.last_week_date_string)
                month = -1
            }
            THIS_MONTH -> {
                dateString = (coCoinUtil.getMonthShort(today[Calendar.MONTH] + 1)
                        + " " + today[Calendar.YEAR])
                dateShownString = activity.resources.getString(R.string.this_month_date_string)
                month = today[Calendar.MONTH] + 1
            }
            LAST_MONTH -> {
                val lastMonthCalendar = coCoinUtil.getLastMonthLeftRange(today)
                dateString = (coCoinUtil.getMonthShort(lastMonthCalendar[Calendar.MONTH] + 1)
                        + " " + lastMonthCalendar[Calendar.YEAR])
                dateShownString = activity.resources.getString(R.string.last_month_date_string)
                month = lastMonthCalendar[Calendar.MONTH] + 1
            }
            THIS_YEAR -> {
                dateString = today[Calendar.YEAR].toString() + ""
                dateShownString = activity.resources.getString(R.string.this_year_date_string)
                month = -1
            }
            LAST_YEAR -> {
                val lastYearCalendar = coCoinUtil.getLastYearLeftRange(today)
                dateString = lastYearCalendar[Calendar.YEAR].toString() + ""
                dateShownString = activity.resources.getString(R.string.last_year_date_string)
                month = -1
            }
        }
    }

    private fun getAllDataDialogTitle(): String {
        val prefix = coCoinUtil.getSpendString(Sum.toInt())
        val postfix = ""
        return when (fragmentPosition) {
            TODAY -> prefix + activity.resources.getString(R.string.today_date_string) + postfix
            YESTERDAY -> prefix + activity.resources.getString(R.string.yesterday_date_string) + postfix
            THIS_WEEK -> prefix + activity.resources.getString(R.string.this_week_date_string) + postfix
            LAST_WEEK -> prefix + activity.resources.getString(R.string.last_week_date_string) + postfix
            THIS_MONTH -> prefix + activity.resources.getString(R.string.this_month_date_string) + postfix
            LAST_MONTH -> prefix + activity.resources.getString(R.string.last_month_date_string) + postfix
            THIS_YEAR -> prefix + activity.resources.getString(R.string.this_year_date_string) + postfix
            LAST_YEAR -> prefix + activity.resources.getString(R.string.last_year_date_string) + postfix
            else -> ""
        }
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_BODY = 1
        const val TODAY = 0
        const val YESTERDAY = 1
        const val THIS_WEEK = 2
        const val LAST_WEEK = 3
        const val THIS_MONTH = 4
        const val LAST_MONTH = 5
        const val THIS_YEAR = 6
        const val LAST_YEAR = 7
    }
}