package com.jbekas.cocoin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.model.RecordManager
import com.jbekas.cocoin.util.CoCoinUtil

import java.util.Calendar

import com.jbekas.cocoin.databinding.ItemMonthViewDrawerBinding

class DrawerMonthViewRecyclerViewAdapter(private val mContext: Context) :
    RecyclerView.Adapter<DrawerMonthViewRecyclerViewAdapter.DMVviewHolder>() {

    private val expenses = mutableListOf<Double>()
    private val records = mutableListOf<Int>()
    private val months = mutableListOf<Int>()
    private val years = mutableListOf<Int>()

    private var onItemClickListener: OnItemClickListener? = null

    init {
        if (RecordManager.RECORDS.isNotEmpty()) {
            var currentYear = RecordManager.RECORDS[RecordManager.RECORDS.size - 1].calendar.get(Calendar.YEAR)
            var currentMonth = RecordManager.RECORDS[RecordManager.RECORDS.size - 1].calendar.get(Calendar.MONTH)
            var currentMonthSum = 0.0
            var currentMonthRecord = 0

            for (i in RecordManager.RECORDS.size - 1 downTo 0) {
                if (RecordManager.RECORDS[i].calendar.get(Calendar.YEAR) == currentYear &&
                    RecordManager.RECORDS[i].calendar.get(Calendar.MONTH) == currentMonth
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
                    currentYear = RecordManager.RECORDS[i].calendar.get(Calendar.YEAR)
                    currentMonth = RecordManager.RECORDS[i].calendar.get(Calendar.MONTH)
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_view_drawer, parent, false)
        return DMVviewHolder(view)
    }

    override fun onBindViewHolder(holder: DMVviewHolder, position: Int) {
        holder.month.text = CoCoinUtil.GetMonthShort(months[position] + 1)
        holder.year.text = years[position].toString()
        holder.sum.text = CoCoinUtil.GetInRecords(records[position])
        holder.money.text = CoCoinUtil.GetInMoney(expenses[position].toInt())
    }

    inner class DMVviewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val binding = ItemMonthViewDrawerBinding.bind(view)
        val month: TextView = binding.month
        val year: TextView = binding.year
        val money: TextView = binding.money
        val sum: TextView = binding.sum

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onItemClickListener?.onItemClick(v, adapterPosition)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        this.onItemClickListener = itemClickListener
    }

    // Java compatibility method with original naming convention
    fun SetOnItemClickListener(itemClickListener: OnItemClickListener) {
        this.onItemClickListener = itemClickListener
    }
}