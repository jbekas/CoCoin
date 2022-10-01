package com.jbekas.cocoin.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jbekas.cocoin.databinding.ItemDialogSelectListDataBinding
import com.jbekas.cocoin.util.CoCoinUtil

class DialogSelectListDataAdapter(
    private val coCoinUtil: CoCoinUtil,
    private val data: ArrayList<DoubleArray>?,
    private val clickListener: ListItemClickListener? = null,
) : RecyclerView.Adapter<DialogSelectListDataAdapter.SelectListViewHolder>() {

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectListViewHolder {
        val binding = ItemDialogSelectListDataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SelectListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectListViewHolder, position: Int) {
        val monthNum = data!![position][1]
        if (monthNum == -1.0) {
            holder.binding.year.text = data[position][0].toInt().toString()
            holder.binding.year.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            holder.binding.month.text = "--"
        } else {
            holder.binding.month.text = coCoinUtil.getMonthShort(data[position][1].toInt())
            holder.binding.year.text = data[position][0].toInt().toString()
        }
        holder.binding.expense.text = coCoinUtil.getInMoney(data[position][3].toInt())
        holder.binding.sum.text = "${data[position][2].toInt()}'s"

        holder.binding.root.setOnClickListener {
            clickListener?.onItemClick(it, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    class SelectListViewHolder constructor(val binding: ItemDialogSelectListDataBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface ListItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}