package com.jbekas.cocoin.adapter

import android.content.Context
import android.widget.BaseAdapter
import android.view.LayoutInflater
import android.view.View
import com.jbekas.cocoin.model.RecordManager
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jbekas.cocoin.R
import com.jbekas.cocoin.fragment.TagChooseFragment
import com.jbekas.cocoin.util.CoCoinUtil
import timber.log.Timber

class TagChooseGridViewAdapter(
    context: Context,
    fragmentPosition: Int,
    private val tagSelectedListener: TagChooseFragment.OnTagItemSelectedListener? = null
) : BaseAdapter() {

    private val inflater: LayoutInflater
    private val mContext: Context
    private val fragmentPosition: Int

    init {
        inflater = LayoutInflater.from(context)
        mContext = context
        this.fragmentPosition = fragmentPosition
    }

    override fun getCount(): Int {
        return if ((fragmentPosition + 1) * 8 >= RecordManager.TAGS.size - 2) {
            (RecordManager.TAGS.size - 2) % 8
        } else {
            8
        }
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {

        val view = if (convertView != null) {
            convertView
        } else {
            val view = inflater.inflate(R.layout.item_tag_choose, null)
            view.tag = ViewHolder()
            view
        }

        val holder = view.tag as ViewHolder

        holder.tagName = view.findViewById<View>(R.id.tag_name) as TextView
        holder.tagImage = view.findViewById<View>(R.id.tag_image) as ImageView

        view.setOnClickListener {
            val tagPosition = fragmentPosition * 8 + position + 2
            tagSelectedListener?.onTagItemPicked(tagPosition)
            tagSelectedListener?.onAnimationStart(tagPosition)
        }

        holder.tagName!!.text =
            CoCoinUtil.GetTagName(RecordManager.TAGS[fragmentPosition * 8 + position + 2].id)
        holder.tagName!!.setTypeface(CoCoinUtil.typefaceLatoLight)
        holder.tagImage!!.setImageResource(
            CoCoinUtil.GetTagIcon(RecordManager.TAGS[fragmentPosition * 8 + position + 2].id))

        return view
    }

    private inner class ViewHolder {
        var tagName: TextView? = null
        var tagImage: ImageView? = null
    }
}