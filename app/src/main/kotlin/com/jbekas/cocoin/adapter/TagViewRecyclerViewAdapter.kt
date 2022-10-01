package com.jbekas.cocoin.adapter

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.jbekas.cocoin.R
import com.jbekas.cocoin.databinding.TagListViewHeadBinding
import com.jbekas.cocoin.databinding.TagListViewHistogramBodyBinding
import com.jbekas.cocoin.databinding.TagListViewPieBodyBinding
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.fragment.RecordCheckDialogFragment
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.util.CoCoinUtil
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
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

// Todo optimize this
class TagViewRecyclerViewAdapter(
    private val context: Context,
    private val activity: FragmentActivity,
    private val coCoinUtil: CoCoinUtil,
    coCoinRecords: List<CoCoinRecord>,
    private val fragmentPosition: Int,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val contents = mutableListOf<List<CoCoinRecord>>()
    private val type = mutableListOf<Int>()
    private var SumList = mutableListOf<Double>()
    private var allTagExpanses = mutableListOf<Map<Int, Double>>()
    private lateinit var DayExpanseSum: IntArray
    private lateinit var MonthExpanseSum: IntArray
    private lateinit var SelectedPosition: IntArray
    private var Sum: Float
    private var year = 0
    private var month = 0
    private var startYear = 0
    private var startMonth = 0
    private var endYear = 0
    private var endMonth = 0
    private var dialogTitle: String? = null
    private var chartType = 0
    private var fragmentTagId = 0
    private var IS_EMPTY = false

    init {
        chartType = if (fragmentPosition == 0) {
            PIE
        } else if (fragmentPosition == 1) {
            SUM_HISTOGRAM
        } else {
            HISTOGRAM
        }
        IS_EMPTY = coCoinRecords.isEmpty()
        Sum = 0f
        if (!IS_EMPTY) {
            Collections.sort(coCoinRecords) { lhs, rhs -> rhs.calendar.compareTo(lhs.calendar) }
            year = coCoinRecords[0].calendar[Calendar.YEAR]
            month = coCoinRecords[0].calendar[Calendar.MONTH] + 1
            endYear = year
            endMonth = month
            var yearPosition = 0
            var monthSum = 0.0
            var yearSum = 0.0
            var yearSet: MutableList<CoCoinRecord> = ArrayList()
            var monthSet: MutableList<CoCoinRecord> = ArrayList()
            for (coCoinRecord in coCoinRecords) {
                Sum += coCoinRecord.money.toFloat()
                if (coCoinRecord.calendar[Calendar.YEAR] == year) {
                    yearSet.add(coCoinRecord)
                    yearSum += coCoinRecord.money
                    if (coCoinRecord.calendar[Calendar.MONTH] == month - 1) {
                        monthSet.add(coCoinRecord)
                        monthSum += coCoinRecord.money
                    } else {
                        contents.add(monthSet)
                        SumList.add(monthSum)
                        monthSum = coCoinRecord.money
                        type.add(SHOW_IN_MONTH)
                        monthSet = ArrayList()
                        monthSet.add(coCoinRecord)
                        month = coCoinRecord.calendar[Calendar.MONTH] + 1
                    }
                } else {
                    contents.add(monthSet)
                    SumList.add(monthSum)
                    monthSum = coCoinRecord.money
                    type.add(SHOW_IN_MONTH)
                    monthSet = ArrayList()
                    monthSet.add(coCoinRecord)
                    month = coCoinRecord.calendar[Calendar.MONTH] + 1
                    contents.add(yearPosition, yearSet)
                    SumList.add(yearPosition, yearSum)
                    yearSum = coCoinRecord.money
                    type.add(yearPosition, SHOW_IN_YEAR)
                    yearPosition = contents.size
                    yearSet = ArrayList()
                    yearSet.add(coCoinRecord)
                    year = coCoinRecord.calendar[Calendar.YEAR]
                    monthSet = ArrayList()
                    monthSet.add(coCoinRecord)
                    month = coCoinRecord.calendar[Calendar.MONTH] + 1
                }
            }
            contents.add(monthSet)
            SumList.add(monthSum)
            type.add(SHOW_IN_MONTH)
            contents.add(yearPosition, yearSet)
            SumList.add(yearPosition, yearSum)
            type.add(yearPosition, SHOW_IN_YEAR)
            startYear = year
            startMonth = month
            if (chartType == PIE) {
                for (i in contents.indices) {
                    val tagExpanse = mutableMapOf<Int, Double>()
                    for (tag in RecordManager.TAGS) {
                        tagExpanse[tag.id] = java.lang.Double.valueOf(0.0)
                    }
                    for (coCoinRecord in contents.get(i)) {
                        var d = tagExpanse[coCoinRecord.tag] ?: 0.toDouble()
                        d += coCoinRecord.money
                        tagExpanse[coCoinRecord.tag] = d
                    }
                    val sortedTagExpanses = coCoinUtil.sortTreeMapByValues(tagExpanse)
                    allTagExpanses.add(sortedTagExpanses)
                }
            }
            if (chartType == SUM_HISTOGRAM) {
                DayExpanseSum = IntArray((endYear - startYear + 1) * 372)
                for (coCoinRecord in coCoinRecords) {
                    DayExpanseSum[(coCoinRecord.calendar[Calendar.YEAR] - startYear) * 372 + coCoinRecord.calendar[Calendar.MONTH] * 31 +
                            coCoinRecord.calendar[Calendar.DAY_OF_MONTH] - 1] += coCoinRecord.money.toInt()
                }
            }
            MonthExpanseSum = IntArray((endYear - startYear + 1) * 12)
            for (coCoinRecord in coCoinRecords) {
                MonthExpanseSum[(coCoinRecord.calendar[Calendar.YEAR] - startYear) * 12 +
                        coCoinRecord.calendar[Calendar.MONTH]] += coCoinRecord.money.toInt()
            }
            SelectedPosition = IntArray(contents.size + 1)
            for (i in SelectedPosition.indices) {
                SelectedPosition[i] = 0
            }
            fragmentTagId = contents.get(0)[0].tag
            if (fragmentPosition == 0) {
                fragmentTagId = -2
            }
            if (fragmentPosition == 1) {
                fragmentTagId = -1
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
        return if (IS_EMPTY) 1 else contents.size + 1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int,
    ): RecyclerView.ViewHolder {
        val view: View
        when (viewType) {
            TYPE_HEADER -> {
                val binding =
                    TagListViewHeadBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return HeaderViewHolder(binding)
            }
            TYPE_CELL -> {
                when (chartType) {
                    PIE -> {
                        val binding = TagListViewPieBodyBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                        return PieViewHolder(binding)
                    }
                    HISTOGRAM, SUM_HISTOGRAM -> {
                        val binding = TagListViewHistogramBodyBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                        return HistogramViewHolder(binding)
                    }
                }
            }
        }

        throw IllegalArgumentException("Unable to create ViewHolder for viewType($viewType)")
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                val holder = viewHolder as HeaderViewHolder
                if (IS_EMPTY) {
                    holder.binding.sum.text = context.resources.getString(R.string.tag_empty)
                    holder.binding.sum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    holder.binding.from.visibility = View.INVISIBLE
                    holder.binding.to.visibility = View.INVISIBLE
                } else {
                    holder.binding.from.text = context.resources.getString(R.string.from) + " " +
                            startYear + " " + coCoinUtil.getMonthShort(startMonth)
                    holder.binding.sum.text = coCoinUtil.getInMoney(Sum.toInt())
                    holder.binding.to.text = context.resources.getString(R.string.to) + " " +
                            endYear + " " + coCoinUtil.getMonthShort(endMonth)
                }
            }
            TYPE_CELL -> {
                val year = contents[position - 1][0].calendar[Calendar.YEAR]
                val month = contents[position - 1][0].calendar[Calendar.MONTH] + 1
                val pieChartData: PieChartData
                var subcolumnValues: MutableList<SubcolumnValue>
                val columns: MutableList<Column>
                var columnChartData: ColumnChartData
                val sliceValues: MutableList<SliceValue>
                when (chartType) {
                    PIE -> {
                        val holder = viewHolder as PieViewHolder
                        sliceValues = ArrayList()
                        for ((key, value) in allTagExpanses[position - 1]) {
                            if (value >= 1) {
                                val sliceValue = SliceValue(value.toFloat(),
                                    context.resources.getColor(coCoinUtil.getTagColorResource(key)))
                                sliceValue.setLabel(key.toString())
                                sliceValues.add(sliceValue)
                            }
                        }
                        pieChartData = PieChartData(sliceValues)
                        pieChartData.setHasLabels(false)
                        pieChartData.setHasLabelsOnlyForSelected(false)
                        pieChartData.setHasLabelsOutside(false)
                        pieChartData.setHasCenterCircle(SettingManager.getInstance().isHollow)
                        holder.binding.chartPie.pieChartData = pieChartData
                        holder.binding.chartPie.onValueTouchListener =
                            PieValueTouchListener(position - 1)
                        holder.binding.chartPie.isChartRotationEnabled = false
                        if (type[position - 1] == SHOW_IN_MONTH) {
                            holder.binding.date.text =
                                year.toString() + " " + coCoinUtil.getMonthShort(month)
                        } else {
                            holder.binding.date.text = "$year "
                        }
                        holder.binding.expanse.text =
                            coCoinUtil.getInMoney(SumList[position - 1].toInt())
                        holder.binding.iconRight.setOnClickListener {
                            SelectedPosition[position] =
                                (SelectedPosition[position] + 1) % sliceValues.size
                            val selectedValue = SelectedValue(
                                SelectedPosition[position],
                                0,
                                SelectedValue.SelectedValueType.NONE)
                            holder.binding.chartPie.selectValue(selectedValue)
                        }
                        holder.binding.iconLeft.setOnClickListener {
                            SelectedPosition[position] =
                                (SelectedPosition[position] - 1 + sliceValues.size) % sliceValues.size
                            val selectedValue = SelectedValue(
                                SelectedPosition[position],
                                0,
                                SelectedValue.SelectedValueType.NONE)
                            holder.binding.chartPie.selectValue(selectedValue)
                        }
                    }
                    SUM_HISTOGRAM -> {
                        val holder = viewHolder as HistogramViewHolder
                        columns = ArrayList()
                        if (type[position - 1] == SHOW_IN_YEAR) {
                            val numColumns = 12
                            run {
                                var i = 0
                                while (i < numColumns) {
                                    subcolumnValues = ArrayList()
                                    val value = SubcolumnValue(
                                        MonthExpanseSum[(year - startYear) * 12 + i].toFloat(),
                                        coCoinUtil.getRandomColor())
                                    value.setLabel(coCoinUtil.MONTHS_SHORT[month].toString() + " " + year)
                                    subcolumnValues.add(value)
                                    val column = Column(subcolumnValues)
                                    column.setHasLabels(false)
                                    column.setHasLabelsOnlyForSelected(false)
                                    columns.add(column)
                                    i++
                                }
                            }
                            columnChartData = ColumnChartData(columns)
                            val axisX = Axis()
                            val axisValueList: MutableList<AxisValue> = ArrayList()
                            var i = 0
                            while (i < numColumns) {
                                axisValueList.add(AxisValue(i.toFloat())
                                    .setLabel(coCoinUtil.getMonthShort(i + 1)))
                                i++
                            }
                            axisX.values = axisValueList
                            val axisY = Axis().setHasLines(true)
                            columnChartData.axisXBottom = axisX
                            columnChartData.axisYLeft = axisY
                            columnChartData.isStacked = true
                            holder.binding.chart.columnChartData = columnChartData
                            holder.binding.chart.isZoomEnabled = false
                            holder.binding.chart.onValueTouchListener =
                                ValueTouchListener(position - 1)
                            holder.binding.date.text = year.toString() + ""
                            holder.binding.expanse.text =
                                coCoinUtil.getInMoney(SumList[position - 1].toInt())
                        }
                        if (type[position - 1] == SHOW_IN_MONTH) {
                            val tempCal: Calendar = GregorianCalendar(year, month - 1, 1)
                            val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                            run {
                                var i = 0
                                while (i < daysInMonth) {
                                    subcolumnValues = ArrayList()
                                    val value =
                                        SubcolumnValue(DayExpanseSum[(year - startYear) * 372 + (month - 1) * 31 + i].toFloat(),
                                            coCoinUtil.getRandomColor())
                                    value.setLabel(coCoinUtil.MONTHS_SHORT[month]
                                        .toString() + " " + (i + 1) + " " + year)
                                    subcolumnValues.add(value)
                                    val column = Column(subcolumnValues)
                                    column.setHasLabels(false)
                                    column.setHasLabelsOnlyForSelected(false)
                                    columns.add(column)
                                    ++i
                                }
                            }
                            columnChartData = ColumnChartData(columns)
                            val axisX = Axis()
                            val axisValueList: MutableList<AxisValue> = ArrayList()
                            var i = 0
                            while (i < daysInMonth) {
                                axisValueList.add(AxisValue(i.toFloat()).setLabel((i + 1).toString()))
                                i++
                            }
                            axisX.values = axisValueList
                            val axisY = Axis().setHasLines(true)
                            columnChartData.axisXBottom = axisX
                            columnChartData.axisYLeft = axisY
                            columnChartData.isStacked = true
                            holder.binding.chart.columnChartData = columnChartData
                            holder.binding.chart.isZoomEnabled = false
                            holder.binding.chart.onValueTouchListener =
                                ValueTouchListener(position - 1)
                            holder.binding.date.text =
                                year.toString() + " " + coCoinUtil.getMonthShort(month)
                            holder.binding.expanse.text =
                                coCoinUtil.getInMoney(SumList[position - 1].toInt())
                        }
                        holder.binding.iconRight.setOnClickListener {
                            do {
                                SelectedPosition[position] =
                                    (SelectedPosition[position] + 1) % columns.size
                            } while (holder.binding.chart.chartData.columns[SelectedPosition[position]]
                                    .values.size == 0 || holder.binding.chart.chartData.columns[SelectedPosition[position]]
                                    .values[0].value == 0f
                            )
                            val selectedValue = SelectedValue(
                                SelectedPosition[position],
                                0,
                                SelectedValue.SelectedValueType.COLUMN)
                            holder.binding.chart.selectValue(selectedValue)
                        }
                        holder.binding.iconLeft.setOnClickListener {
                            do {
                                SelectedPosition[position] =
                                    ((SelectedPosition[position] - 1 + columns.size)
                                            % columns.size)
                            } while (holder.binding.chart.chartData.columns[SelectedPosition[position]]
                                    .values.size == 0 || holder.binding.chart.chartData.columns[SelectedPosition[position]]
                                    .values[0].value == 0f
                            )
                            val selectedValue = SelectedValue(
                                SelectedPosition[position],
                                0,
                                SelectedValue.SelectedValueType.COLUMN)
                            holder.binding.chart.selectValue(selectedValue)
                        }
                    }
                    HISTOGRAM -> {
                        val holder = viewHolder as HistogramViewHolder
                        columns = ArrayList()
                        if (type[position - 1] == SHOW_IN_YEAR) {
                            val numColumns = 12
                            run {
                                var i = 0
                                while (i < numColumns) {
                                    subcolumnValues = ArrayList()
                                    val value = SubcolumnValue(
                                        MonthExpanseSum[(year - startYear) * 12 + i].toFloat(),
                                        coCoinUtil.getRandomColor())
                                    value.setLabel(coCoinUtil.MONTHS_SHORT[month].toString() + " " + year)
                                    subcolumnValues.add(value)
                                    val column = Column(subcolumnValues)
                                    column.setHasLabels(false)
                                    column.setHasLabelsOnlyForSelected(false)
                                    columns.add(column)
                                    i++
                                }
                            }
                            columnChartData = ColumnChartData(columns)
                            val axisX = Axis()
                            val axisValueList: MutableList<AxisValue> = ArrayList()
                            var i = 0
                            while (i < numColumns) {
                                axisValueList.add(AxisValue(i.toFloat())
                                    .setLabel(coCoinUtil.getMonthShort(i + 1)))
                                i++
                            }
                            axisX.values = axisValueList
                            val axisY = Axis().setHasLines(true)
                            columnChartData.axisXBottom = axisX
                            columnChartData.axisYLeft = axisY
                            columnChartData.isStacked = true
                            holder.binding.chart.columnChartData = columnChartData
                            holder.binding.chart.isZoomEnabled = false
                            holder.binding.chart.onValueTouchListener =
                                ValueTouchListener(position - 1)
                            holder.binding.date.text = year.toString() + ""
                            holder.binding.expanse.text =
                                coCoinUtil.getInMoney(SumList[position - 1].toInt())
                        }
                        if (type[position - 1] == SHOW_IN_MONTH) {
                            val tempCal = Calendar.getInstance()
                            tempCal[year, month - 1] = 1
                            tempCal.add(Calendar.SECOND, 0)
                            val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                            var p = contents[position - 1].size - 1
                            run {
                                var i = 0
                                while (i < daysInMonth) {
                                    subcolumnValues = ArrayList()
                                    val value = SubcolumnValue(0.toFloat(),
                                        coCoinUtil.getRandomColor())
                                    subcolumnValues.add(value)
                                    while (p >= 0
                                        && contents[position - 1][p].calendar[Calendar.DAY_OF_MONTH] == i + 1
                                    ) {
                                        subcolumnValues[0].value =
                                            subcolumnValues[0].value + contents[position - 1][p].money.toFloat()
                                        subcolumnValues[0].setLabel(p.toString() + "")
                                        p--
                                    }
                                    val column = Column(subcolumnValues)
                                    column.setHasLabels(false)
                                    column.setHasLabelsOnlyForSelected(false)
                                    columns.add(column)
                                    ++i
                                }
                            }
                            columnChartData = ColumnChartData(columns)
                            val axisX = Axis()
                            val axisValueList: MutableList<AxisValue> = ArrayList()
                            var i = 0
                            while (i < daysInMonth) {
                                axisValueList.add(AxisValue(i.toFloat()).setLabel((i + 1).toString()))
                                i++
                            }
                            axisX.values = axisValueList
                            val axisY = Axis().setHasLines(true)
                            columnChartData.axisXBottom = axisX
                            columnChartData.axisYLeft = axisY
                            columnChartData.isStacked = true
                            holder.binding.chart.columnChartData = columnChartData
                            holder.binding.chart.isZoomEnabled = false
                            holder.binding.chart.onValueTouchListener =
                                ValueTouchListener(position - 1)
                            holder.binding.date.text =
                                year.toString() + " " + coCoinUtil.getMonthShort(month)
                            holder.binding.expanse.text =
                                coCoinUtil.getInMoney(SumList[position - 1].toInt())
                        }
                        holder.binding.iconRight.setOnClickListener {
                            do {
                                SelectedPosition[position] =
                                    (SelectedPosition[position] + 1) % columns.size
                            } while (holder.binding.chart.chartData.columns[SelectedPosition[position]]
                                    .values.size == 0 || holder.binding.chart.chartData.columns[SelectedPosition[position]]
                                    .values[0].value == 0f
                            )
                            val selectedValue = SelectedValue(
                                SelectedPosition[position],
                                0,
                                SelectedValue.SelectedValueType.NONE)
                            holder.binding.chart.selectValue(selectedValue)
                        }
                        holder.binding.iconLeft.setOnClickListener {
                            do {
                                SelectedPosition[position] =
                                    ((SelectedPosition[position] - 1 + columns.size)
                                            % columns.size)
                            } while (holder.binding.chart.chartData.columns[SelectedPosition[position]]
                                    .values.size == 0 || holder.binding.chart.chartData.columns[SelectedPosition[position]]
                                    .values[0].value == 0f
                            )
                            val selectedValue = SelectedValue(
                                SelectedPosition[position],
                                0,
                                SelectedValue.SelectedValueType.NONE)
                            holder.binding.chart.selectValue(selectedValue)
                        }
                    }
                }
            }
        }
    }

    class HeaderViewHolder constructor(val binding: TagListViewHeadBinding) :
        RecyclerView.ViewHolder(binding.root)

    class PieViewHolder constructor(val binding: TagListViewPieBodyBinding) :
        RecyclerView.ViewHolder(binding.root)

    class HistogramViewHolder constructor(val binding: TagListViewHistogramBodyBinding) :
        RecyclerView.ViewHolder(binding.root)

    private inner class ValueTouchListener(private val position: Int) :
        ColumnChartOnValueSelectListener {
        override fun onValueSelected(columnIndex: Int, subColumnIndex: Int, value: SubcolumnValue) {
            val snackbar = Snackbar.with(activity)
                .type(SnackbarType.MULTI_LINE)
                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                .position(Snackbar.SnackbarPosition.BOTTOM)
                .margin(15, 15)
                .backgroundDrawable(coCoinUtil.getSnackBarBackground(fragmentTagId))
                .textColor(Color.WHITE)
                .textTypeface(coCoinUtil.getTypeface())
                .actionLabel(context.resources.getString(R.string.check)) //                            .actionLabelTypeface( coCoinUtil.typefaceLatoLight)
                .actionColor(Color.WHITE)
            if (fragmentPosition == SUM_HISTOGRAM) {
                if (type[position] == SHOW_IN_MONTH) {
                    var timeString = contents[position][0].getCalendarString(coCoinUtil)
                    val month = contents[position][0].calendar[Calendar.MONTH] + 1
                    timeString = (" " + coCoinUtil.getMonthShort(month)
                            + " " + (columnIndex + 1) + " "
                            + timeString.substring(timeString.length - 4, timeString.length))
                    val text = """
                            ${coCoinUtil.getSpendString(value.value.toInt())}
                            ${context.resources.getString(R.string.on)}$timeString
                            """.trimIndent()
                    dialogTitle = coCoinUtil.getSpendString(value.value.toInt()) +
                            context.resources.getString(R.string.on) + timeString
                    snackbar.text(text)
                    snackbar.actionListener {
                        val shownCoCoinRecords: MutableList<CoCoinRecord> = ArrayList()
                        var isSamed = false
                        for (coCoinRecord in contents[position]) {
                            if (coCoinRecord.calendar[Calendar.DAY_OF_MONTH] == columnIndex + 1) {
                                shownCoCoinRecords.add(coCoinRecord)
                                isSamed = true
                            } else {
                                if (isSamed) {
                                    break
                                }
                            }
                        }
                        activity.supportFragmentManager
                            .beginTransaction()
                            .add(RecordCheckDialogFragment(
                                context, shownCoCoinRecords, dialogTitle), "MyDialog")
                            .commit()
                    }
                    SnackbarManager.show(snackbar)
                }
                if (type[position] == SHOW_IN_YEAR) {
                    var timeString = " " +
                            contents[position][0].calendar[Calendar.YEAR]
                    timeString = (" " + coCoinUtil.getMonthShort(columnIndex + 1) + " "
                            + timeString.substring(timeString.length - 4, timeString.length))
                    val text = """
                            ${coCoinUtil.getSpendString(value.value.toInt())}
                            ${context.resources.getString(R.string.`in`)}$timeString
                            """.trimIndent()
                    dialogTitle = coCoinUtil.getSpendString(value.value.toInt()) +
                            context.resources.getString(R.string.`in`) + timeString
                    snackbar.text(text)
                    snackbar.actionListener {
                        val shownCoCoinRecords: MutableList<CoCoinRecord> = ArrayList()
                        var isSamed = false
                        for (coCoinRecord in contents[position]) {
                            if (coCoinRecord.calendar[Calendar.MONTH] == columnIndex) {
                                shownCoCoinRecords.add(coCoinRecord)
                                isSamed = true
                            } else {
                                if (isSamed) {
                                    break
                                }
                            }
                        }
                        activity.supportFragmentManager
                            .beginTransaction()
                            .add(RecordCheckDialogFragment(
                                context, shownCoCoinRecords, dialogTitle), "MyDialog")
                            .commit()
                    }
                    SnackbarManager.show(snackbar)
                }
            } else {
                if (type[position] == SHOW_IN_MONTH) {
                    var timeString = contents[position][0].getCalendarString(coCoinUtil)
                    timeString = timeString.substring(6, timeString.length)
                    val month = contents[position][0].calendar[Calendar.MONTH] + 1
                    timeString = (" " + coCoinUtil.getMonthShort(month)
                            + " " + (columnIndex + 1) + " "
                            + timeString.substring(timeString.length - 4, timeString.length))
                    val text = """
                            ${coCoinUtil.getSpendString(value.value.toInt())}${
                        context.resources.getString(R.string.on)
                    }$timeString
                            in ${coCoinUtil.getTagName(contents[position][0].tag)}
                            """.trimIndent()
                    dialogTitle = """
                            ${coCoinUtil.getSpendString(value.value.toInt())}${
                        context.resources.getString(R.string.on)
                    }$timeString
                            in ${coCoinUtil.getTagName(contents[position][0].tag)}
                            """.trimIndent()
                    snackbar.text(text)
                    snackbar.actionListener {
                        val shownCoCoinRecords: MutableList<CoCoinRecord> = ArrayList()
                        var isSamed = false
                        for (coCoinRecord in contents[position]) {
                            if (coCoinRecord.calendar[Calendar.DAY_OF_MONTH] == columnIndex + 1) {
                                shownCoCoinRecords.add(coCoinRecord)
                                isSamed = true
                            } else {
                                if (isSamed) {
                                    break
                                }
                            }
                        }
                        activity.supportFragmentManager
                            .beginTransaction()
                            .add(RecordCheckDialogFragment(
                                context, shownCoCoinRecords, dialogTitle), "MyDialog")
                            .commit()
                    }
                    SnackbarManager.show(snackbar)
                }
                if (type[position] == SHOW_IN_YEAR) {
                    var timeString = contents[position][0].calendar[Calendar.YEAR].toString()
                    timeString = (" " + coCoinUtil.getMonthShort(columnIndex + 1) + " "
                            + timeString.substring(timeString.length - 4, timeString.length))
                    val text = """
                            ${coCoinUtil.getSpendString(value.value.toInt())}${
                        context.resources.getString(R.string.`in`)
                    }$timeString
                            in ${coCoinUtil.getTagName(contents[position][0].tag)}
                            """.trimIndent()
                    dialogTitle = """
                            ${coCoinUtil.getSpendString(value.value.toInt())}${
                        context.resources.getString(R.string.`in`)
                    }$timeString
                            in ${coCoinUtil.getTagName(contents[position][0].tag)}
                            """.trimIndent()
                    snackbar.text(text)
                    snackbar.actionListener {
                        val shownCoCoinRecords: MutableList<CoCoinRecord> = ArrayList()
                        var isSamed = false
                        for (coCoinRecord in contents[position]) {
                            if (coCoinRecord.calendar[Calendar.MONTH] == columnIndex) {
                                shownCoCoinRecords.add(coCoinRecord)
                                isSamed = true
                            } else {
                                if (isSamed) {
                                    break
                                }
                            }
                        }
                        activity.supportFragmentManager
                            .beginTransaction()
                            .add(RecordCheckDialogFragment(
                                context, shownCoCoinRecords, dialogTitle), "MyDialog")
                            .commit()
                    }
                    SnackbarManager.show(snackbar)
                }
            }
        }

        override fun onValueDeselected() {}
    }

    private inner class PieValueTouchListener(private val position: Int) :
        PieChartOnValueSelectListener {
        override fun onValueSelected(i: Int, sliceValue: SliceValue) {
            var timeString = contents[position][0].getCalendarString(coCoinUtil)
            val month = contents[position][0].calendar[Calendar.MONTH] + 1
            timeString = timeString.substring(6, timeString.length)
            timeString = if (type[position] == SHOW_IN_YEAR) {
                timeString.substring(timeString.length - 4, timeString.length)
            } else {
                coCoinUtil.getMonthShort(month) + " " +
                        timeString.substring(timeString.length - 4, timeString.length)
            }
            val tagId = Integer.valueOf(String(sliceValue.labelAsChars))
            val percent = sliceValue.value / SumList[position] * 100
            val tag = coCoinUtil.getTagById(tagId)
            var tagName = "N/A"
            if (tag != null) {
                tagName = coCoinUtil.getTagName(tag.id)
            }
            val text = """
                    ${coCoinUtil.getSpendString(sliceValue.value.toInt())}${
                coCoinUtil.getPercentString(percent)
            }
                    in $tagName
                    """.trimIndent()
            dialogTitle = """
                    ${coCoinUtil.getSpendString(sliceValue.value.toInt())}${
                context.resources.getString(R.string.`in`)
            }$timeString
                    in $tagName
                    """.trimIndent()
            val snackbar = Snackbar
                .with(activity)
                .type(SnackbarType.MULTI_LINE)
                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                .position(Snackbar.SnackbarPosition.BOTTOM)
                .margin(15, 15)
                .backgroundDrawable(coCoinUtil.getSnackBarBackground(fragmentTagId))
                .text(text)
                .textTypeface(coCoinUtil.getTypeface())
                .textColor(Color.WHITE) //                            .actionLabelTypeface( coCoinUtil.typefaceLatoLight)
                .actionLabel(context.resources.getString(R.string.check))
                .actionColor(Color.WHITE)
                .actionListener {
                    val shownCoCoinRecords: MutableList<CoCoinRecord> = ArrayList()
                    for (record in contents[position]) {
                        if (record.tag == tagId) {
                            shownCoCoinRecords.add(record)
                        }
                    }
                    activity.supportFragmentManager
                        .beginTransaction()
                        .add(RecordCheckDialogFragment(
                            context, shownCoCoinRecords, dialogTitle), "MyDialog")
                        .commit()
                }
            SnackbarManager.show(snackbar)
        }

        override fun onValueDeselected() {}
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_CELL = 1
        const val SHOW_IN_YEAR = 0
        const val SHOW_IN_MONTH = 1
        const val PIE = 0
        const val SUM_HISTOGRAM = 1
        const val HISTOGRAM = 2
    }
}