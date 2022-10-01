package com.jbekas.cocoin.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.johnpersano.supertoasts.SuperToast
import com.jbekas.cocoin.R
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.util.CoCoinUtil
import com.melnykov.fab.FloatingActionButton
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager
import com.nispok.snackbar.enums.SnackbarType
import com.nispok.snackbar.listeners.ActionClickListener
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SelectedValue
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.view.PieChartView
import net.steamcrafted.materialiconlib.MaterialIconView
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//import com.squareup.leakcanary.RefWatcher;
@AndroidEntryPoint
class CustomViewFragment : Fragment() {
    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var onDateSetListener: DatePickerDialog.OnDateSetListener? = null
    private var button: FloatingActionButton? = null
    private var fromYear = 0
    private var fromMonth = 0
    private var fromDay = 0
    private var isFrom = false
    private var fromDate: TextView? = null
    private var expense: TextView? = null
    private var emptyTip: TextView? = null
    private val from = Calendar.getInstance()
    private val to = Calendar.getInstance()
    private var superToast: SuperToast? = null
    private var start = -1
    private var end = -1
    private var sum = 0
    private var pie: PieChartView? = null
    private var iconRight: MaterialIconView? = null
    private var iconLeft: MaterialIconView? = null
    private var all: MaterialIconView? = null
    private var startDayCalendar: Calendar? = null
    private var IS_EMPTY = false

    // store the records of each tag
    private var recordsByTag = mutableMapOf<Int, MutableList<CoCoinRecord>>()

    // the selected position of one part of the pie
    private var pieSelectedPosition = 0

    // the last selected position of one part of the pie
    private var lastPieSelectedPosition = -1

    // the date string on the footer and header
    private val dateString: String? = null

    // the date string shown in the dialog
    private var dateShownString: String? = null

    // the string shown in the dialog
    private var dialogTitle: String? = null

    // the selected tag in pie
    private var tagId = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        superToast = SuperToast(requireContext())
        superToast!!.animations = SuperToast.Animations.POPUP
        superToast!!.duration = SuperToast.Duration.SHORT
        superToast!!.textColor = Color.parseColor("#ffffff")
        superToast!!.setTextSize(SuperToast.TextSize.SMALL)
        superToast!!.background = SuperToast.Background.RED
        //        superToast.getTextView().setTypeface(CoCoinUtil.typefaceLatoLight);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_custom_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IS_EMPTY = RecordManager.RECORDS.isEmpty()
        fromDate = view.findViewById<View>(R.id.from_date) as TextView
        fromDate!!.typeface = coCoinUtil.getTypeface()
        expense = view.findViewById<View>(R.id.expense) as TextView
        //        expense.setTypeface(CoCoinUtil.typefaceLatoLight);
        expense!!.text = coCoinUtil.getInMoney(0)
        pie = view.findViewById<View>(R.id.chart_pie) as PieChartView
        pie!!.visibility = View.INVISIBLE
        iconRight = view.findViewById<View>(R.id.icon_right) as MaterialIconView
        iconLeft = view.findViewById<View>(R.id.icon_left) as MaterialIconView
        iconRight!!.visibility = View.INVISIBLE
        iconLeft!!.visibility = View.INVISIBLE
        all = view.findViewById<View>(R.id.all) as MaterialIconView
        all!!.visibility = View.INVISIBLE
        emptyTip = view.findViewById<View>(R.id.empty_tip) as TextView
        emptyTip!!.typeface = coCoinUtil.getTypeface()
        if (IS_EMPTY) {
            emptyTip!!.visibility = View.GONE
        }
        isFrom = true
        onDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                if (isFrom) {
                    fromYear = year
                    fromMonth = monthOfYear + 1
                    fromDay = dayOfMonth
                    val now = Calendar.getInstance()
                    val dpd = DatePickerDialog.newInstance(
                        onDateSetListener,
                        now[Calendar.YEAR],
                        now[Calendar.MONTH],
                        now[Calendar.DAY_OF_MONTH]
                    )
                    dpd.setTitle(resources.getString(R.string.set_right_calendar))
                    dpd.show(requireActivity().fragmentManager, "Datepickerdialog")
                    isFrom = false
                } else {
                    from[fromYear, fromMonth - 1, fromDay, 0, 0] = 0
                    from.add(Calendar.SECOND, 0)
                    to[year, monthOfYear, dayOfMonth, 23, 59] = 59
                    to.add(Calendar.SECOND, 0)
                    if (to.before(from)) {
                        superToast!!.text = resources.getString(R.string.from_invalid)
                        superToast!!.text = resources.getString(R.string.to_invalid)
                        SuperToast.cancelAllSuperToasts()
                        superToast!!.show()
                    } else {
                        fromDate!!.text = " ● " +
                                resources.getString(R.string.from) +
                                " " + coCoinUtil.getMonthShort(from[Calendar.MONTH] + 1) +
                                " " + from[Calendar.DAY_OF_MONTH] +
                                " " + from[Calendar.YEAR] +
                                " " + resources.getString(R.string.to) +
                                " " + coCoinUtil.getMonthShort(to[Calendar.MONTH] + 1) +
                                " " + to[Calendar.DAY_OF_MONTH] +
                                " " + to[Calendar.YEAR]
                        select()
                    }
                }
            }
        button = view.findViewById<View>(R.id.button) as FloatingActionButton
        button!!.setOnClickListener {
            val now = Calendar.getInstance()
            val dpd = DatePickerDialog.newInstance(
                onDateSetListener,
                now[Calendar.YEAR],
                now[Calendar.MONTH],
                now[Calendar.DAY_OF_MONTH]
            )
            dpd.setTitle(resources.getString(R.string.set_left_calendar))
            dpd.show(requireActivity().fragmentManager, "Datepickerdialog")
            isFrom = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()

//        RefWatcher refWatcher = CoCoinApplication.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    private fun select() {
        if (RecordManager.RECORDS == null
            || RecordManager.RECORDS.size == 0
        ) {
            return
        }
        start = -1
        end = 0
        sum = 0
        lastPieSelectedPosition = -1
        if (from.after(RecordManager.RECORDS[RecordManager.RECORDS.size - 1].calendar)) {
            return
        }
        if (to.before(RecordManager.RECORDS[0].calendar)) {
            return
        }
        for (i in RecordManager.RECORDS.indices.reversed()) {
            if (RecordManager.RECORDS[i].calendar.before(from)) {
                end = i + 1
                break
            } else if (RecordManager.RECORDS[i].calendar.before(to)) {
                if (start == -1) {
                    start = i
                }
            }
        }
        startDayCalendar = from.clone() as Calendar
        startDayCalendar!![Calendar.HOUR_OF_DAY] = 0
        startDayCalendar!![Calendar.MINUTE] = 0
        startDayCalendar!![Calendar.SECOND] = 0
        val startDay = TimeUnit.MILLISECONDS.toDays(startDayCalendar!!.timeInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(to.timeInMillis) - startDay + 1

        // store the sum of expenses of each tag
        var expensesByTag = TreeMap<Int, Double>()

        // the original target value of the whole pie
        val originalTargets = FloatArray(days.toInt())

        val size = RecordManager.TAGS.size
        for (j in 2 until size) {
            expensesByTag[RecordManager.TAGS[j].id] = 0.toDouble() //java.lang.Double.valueOf(0.0)
            recordsByTag[RecordManager.TAGS[j].id] = ArrayList()
        }
        for (i in start downTo end) {
            val coCoinRecord = RecordManager.RECORDS[i]
            var currentValue = 0.0
            if (expensesByTag[coCoinRecord.tag] != null) {
                currentValue = expensesByTag[coCoinRecord.tag]!!
            }
            //            TagExpanse.put(coCoinRecord.getTag(),
//                    TagExpanse.get(coCoinRecord.getTag()) + Double.valueOf(coCoinRecord.getMoney()));
            expensesByTag[coCoinRecord.tag] = currentValue + coCoinRecord.money
            val recordList = recordsByTag.computeIfAbsent(coCoinRecord.tag) { mutableListOf() }
            recordList.add(coCoinRecord)
            recordsByTag[coCoinRecord.tag] = recordList
            sum += coCoinRecord.money.toInt()
            originalTargets[(TimeUnit.MILLISECONDS.toDays(
                coCoinRecord.calendar.timeInMillis) - startDay).toInt()] += coCoinRecord.money.toFloat()
        }
        expense!!.text = coCoinUtil.getInMoney(sum)
        emptyTip!!.visibility = View.GONE
        expensesByTag = coCoinUtil.sortTreeMapByValues(expensesByTag) as TreeMap<Int, Double>
        val sliceValues = ArrayList<SliceValue>()
        for ((key, value) in expensesByTag) {
            if (value >= 1) {
                val sliceValue = SliceValue(value.toFloat(), coCoinUtil.getTagColor(key))
                sliceValue.setLabel(key.toString())
                sliceValues.add(sliceValue)
            }
        }
        val pieChartData = PieChartData(sliceValues)
        pieChartData.setHasLabels(false)
        pieChartData.setHasLabelsOnlyForSelected(false)
        pieChartData.setHasLabelsOutside(false)
        pieChartData.setHasCenterCircle(SettingManager.getInstance().isHollow)
        pie!!.pieChartData = pieChartData
        pie!!.isChartRotationEnabled = false
        pie!!.visibility = View.VISIBLE
        iconRight!!.visibility = View.VISIBLE
        iconRight!!.setOnClickListener {
            if (lastPieSelectedPosition != -1) {
                pieSelectedPosition = lastPieSelectedPosition
            }
            pieSelectedPosition = ((pieSelectedPosition - 1 + sliceValues.size)
                    % sliceValues.size)
            val selectedValue = SelectedValue(
                pieSelectedPosition,
                0,
                SelectedValue.SelectedValueType.NONE)
            pie!!.selectValue(selectedValue)
        }
        iconLeft!!.visibility = View.VISIBLE
        iconLeft!!.setOnClickListener {
            if (lastPieSelectedPosition != -1) {
                pieSelectedPosition = lastPieSelectedPosition
            }
            pieSelectedPosition = ((pieSelectedPosition + 1)
                    % sliceValues.size)
            val selectedValue = SelectedValue(
                pieSelectedPosition,
                0,
                SelectedValue.SelectedValueType.NONE)
            pie!!.selectValue(selectedValue)
        }

// set value touch listener of pie//////////////////////////////////////////////////////////////////
        dateShownString = resources.getString(R.string.from) + " " +
                coCoinUtil.getMonthShort(from[Calendar.MONTH] + 1) + " " +
                from[Calendar.DAY_OF_MONTH] + " " +
                from[Calendar.YEAR] + " " +
                resources.getString(R.string.to) + " " +
                coCoinUtil.getMonthShort(to[Calendar.MONTH] + 1) + " " +
                to[Calendar.DAY_OF_MONTH] + " " +
                to[Calendar.YEAR]
        pie!!.onValueTouchListener = object : PieChartOnValueSelectListener {
            override fun onValueSelected(p: Int, sliceValue: SliceValue) {
                // snack bar
                tagId = Integer.valueOf(String(sliceValue.labelAsChars))
                val percent = (sliceValue.value / sum * 100).toDouble()
                val text = """${coCoinUtil.getSpendString(sliceValue.value.toInt())} (takes ${
                    String.format("%.2f",
                        percent)
                }%)
in ${coCoinUtil.getTagName(tagId)}"""
                dialogTitle = """${coCoinUtil.getSpendString(sliceValue.value.toInt())} ${
                    resources.getString(R.string.from)
                } ${coCoinUtil.getMonthShort(from[Calendar.MONTH] + 1)} ${from[Calendar.DAY_OF_MONTH]} ${from[Calendar.YEAR]}
${resources.getString(R.string.to)} ${coCoinUtil.getMonthShort(to[Calendar.MONTH] + 1)} ${to[Calendar.DAY_OF_MONTH]} ${to[Calendar.YEAR]} in ${
                    coCoinUtil.getTagName(tagId)
                }"""
                val snackbar = Snackbar
                    .with(requireActivity())
                    .type(SnackbarType.MULTI_LINE)
                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                    .position(Snackbar.SnackbarPosition.BOTTOM)
                    .margin(15, 15)
                    .backgroundDrawable(coCoinUtil.getSnackBarBackground(-3))
                    .text(text)
                    .textTypeface(coCoinUtil.getTypeface())
                    .textColor(Color.WHITE)
                    .actionLabelTypeface(coCoinUtil.getTypeface())
                    .actionLabel(resources.getString(R.string.check))
                    .actionColor(Color.WHITE)
                    .actionListener(mActionClickListenerForPie())
                SnackbarManager.show(snackbar)
                lastPieSelectedPosition = if (p == lastPieSelectedPosition) {
                    return
                } else {
                    p
                }
            }

            override fun onValueDeselected() {}
        }
        all!!.visibility = View.VISIBLE
        all!!.setOnClickListener {
            val data: MutableList<CoCoinRecord> = LinkedList()
            for (i in start downTo end) {
                data.add(RecordManager.RECORDS[i])
            }
            dialogTitle =
                """${coCoinUtil.getSpendString(sum)} ${resources.getString(R.string.from)} ${
                    coCoinUtil.getMonthShort(from[Calendar.MONTH] + 1)
                } ${from[Calendar.DAY_OF_MONTH]} ${from[Calendar.YEAR]}
${resources.getString(R.string.to)} ${coCoinUtil.getMonthShort(to[Calendar.MONTH] + 1)} ${to[Calendar.DAY_OF_MONTH]} ${to[Calendar.YEAR]} in ${
                    coCoinUtil.getTagName(tagId)
                }"""
            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(RecordCheckDialogFragment(requireContext(), data, dialogTitle), "MyDialog")
                .commit()
        }
    }

    private inner class mActionClickListenerForPie : ActionClickListener {
        override fun onActionClicked(snackbar: Snackbar) {
            val shownCoCoinRecords: List<CoCoinRecord> = recordsByTag[tagId]?.toList() ?: listOf()
            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(RecordCheckDialogFragment(requireContext(), shownCoCoinRecords, dialogTitle),
                    "MyDialog")
                .commit()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): CustomViewFragment {
            return CustomViewFragment()
        }
    }
}