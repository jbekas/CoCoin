package com.jbekas.cocoin.adapter

import com.jbekas.cocoin.util.CoCoinUtil
import androidx.recyclerview.widget.RecyclerView
import com.jbekas.cocoin.db.RecordManager
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.jbekas.cocoin.databinding.ItemMonthViewDrawerBinding
import java.util.*

class DrawerMonthViewRecyclerViewAdapter(
    private val coCoinUtil: CoCoinUtil,
    private val onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<DrawerMonthViewRecyclerViewAdapter.DMVviewHolder>() {
    private val expenses: ArrayList<Double> = ArrayList()
    private val records: ArrayList<Int> = ArrayList()
    private val months: ArrayList<Int> = ArrayList()
    private val years: ArrayList<Int> = ArrayList()

    init {
        if (RecordManager.RECORDS.size != 0) {
            var currentYear =
                RecordManager.RECORDS[RecordManager.RECORDS.size - 1].calendar[Calendar.YEAR]
            var currentMonth =
                RecordManager.RECORDS[RecordManager.RECORDS.size - 1].calendar[Calendar.MONTH]
            var currentMonthSum = 0.0
            var currentMonthRecord = 0
            for (i in RecordManager.RECORDS.indices.reversed()) {
                if (RecordManager.RECORDS[i].calendar[Calendar.YEAR] == currentYear
                    && RecordManager.RECORDS[i].calendar[Calendar.MONTH] == currentMonth
                ) {
                    currentMonthSum += RecordManager.RECORDS[i].money
                    currentMonthRecord++
                } else {
                    expenses.add(currentMonthSum)
                    records.add(currentMonthRecord)
                    years.add(currentYear)
                    months.add(currentMonth)
                    currentMonthSum = RecordManager.RECORDS[i].money
                    currentMonthRecord = 1
                    currentYear = RecordManager.RECORDS[i].calendar[Calendar.YEAR]
                    currentMonth = RecordManager.RECORDS[i].calendar[Calendar.MONTH]
                }
            }
            expenses.add(currentMonthSum)
            records.add(currentMonthRecord)
            years.add(currentYear)
            months.add(currentMonth)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DMVviewHolder {
        val binding = ItemMonthViewDrawerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DMVviewHolder(binding, onItemClickListener)
    }

    override fun onBindViewHolder(holder: DMVviewHolder, position: Int) {
        holder.month.text = coCoinUtil.getMonthShort(months[position] + 1)
        holder.year.text = years[position].toString() + ""
        holder.sum.text = coCoinUtil.getInRecords(records[position])
        holder.money.text = coCoinUtil.getInMoney(expenses[position].toInt())
    }

    inner class DMVviewHolder internal constructor(
        binding: ItemMonthViewDrawerBinding,
        private val onItemClickListener: OnItemClickListener? = null
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        @JvmField
        var month: TextView
        @JvmField
        var year: TextView
        @JvmField
        var money: TextView
        @JvmField
        var sum: TextView

        init {
            binding.root.setOnClickListener(this)
            month = binding.month
            year = binding.year
            money = binding.money
            sum = binding.sum
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, position)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}