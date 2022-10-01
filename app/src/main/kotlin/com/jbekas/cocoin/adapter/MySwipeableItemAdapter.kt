package com.jbekas.cocoin.adapter

import android.content.Context
import com.jbekas.cocoin.db.RecordManager.Companion.deleteRecord
import com.jbekas.cocoin.model.CoCoinRecord
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder
import android.widget.FrameLayout
import com.jbekas.cocoin.R
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.jbekas.cocoin.ui.SwipeableItemOnClickListener
import com.jbekas.cocoin.util.CoCoinUtil
import androidx.core.content.ContextCompat
import com.jbekas.cocoin.activity.CoCoinApplication
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection
import com.jbekas.cocoin.db.RecordManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault
import java.util.HashMap

class MySwipeableItemAdapter(
    private val context: Context,
    private val coCoinUtil: CoCoinUtil,
    private val records: List<CoCoinRecord>,
    private val onItemDeleteListener: OnItemDeleteListener,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<MySwipeableItemAdapter.MyViewHolder>(),
    SwipeableItemAdapter<MySwipeableItemAdapter.MyViewHolder?> {
    // NOTE: Make accessible with short name
    private interface Swipeable : SwipeableItemConstants

    var eventListener: EventListener? = null

    interface EventListener {
        fun onItemRemoved(position: Int)
        fun onItemPinned(position: Int)
        fun onItemViewClicked(v: View?, pinned: Boolean)
    }

    class MyViewHolder(v: View) : AbstractSwipeableItemViewHolder(v) {
        var mContainer: FrameLayout
        var money: TextView
        var remark: TextView
        var date: TextView
        var tagImage: ImageView
        var index: TextView

        init {
            mContainer = v.findViewById<View>(R.id.container) as FrameLayout
            money = v.findViewById<View>(R.id.money) as TextView
            remark = v.findViewById<View>(R.id.remark) as TextView
            date = v.findViewById<View>(R.id.date) as TextView
            tagImage = v.findViewById<View>(R.id.image_view) as ImageView
            index = v.findViewById<View>(R.id.index) as TextView
        }

        override fun getSwipeableContainerView(): View {
            return mContainer
        }
    }

    init {
        // Todo optimize
        pinned = HashMap()
        for (i in records.indices.reversed()) {
            pinned[records[i].id.toInt()] = false
        }
        setHasStableIds(true)
    }

    private fun onItemViewClick(v: View) {
        if (eventListener != null) {
            eventListener!!.onItemViewClicked(v, true)
        }
    }

    private fun onSwipeableViewContainerClick(v: View) {
        if (eventListener != null) {
            eventListener!!.onItemViewClicked(
                RecyclerViewAdapterUtils.getParentViewHolderItemView(v), false)
        }
    }

    override fun getItemId(position: Int): Long {
        return records[records.size - 1 - position].id
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.account_book_list_view_item, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, pos: Int) {
        val position = pos
        // set listeners
        // (if the item is *not pinned*, click event comes to the itemView)
        holder.itemView.setOnClickListener(object : SwipeableItemOnClickListener(position) {
            override fun onClick(v: View) {
                onItemViewClick(v)
            }
        })
        // (if the item is *pinned*, click event comes to the mContainer)
        holder.mContainer.setOnClickListener(object : SwipeableItemOnClickListener(position) {
            override fun onClick(v: View) {
                onSwipeableViewContainerClick(v)
                onItemClickListener.onItemClick(position)
            }
        })

        // set text
        val tPosition = records.size - 1 - position
        val record = records[tPosition]
        holder.tagImage.setImageResource(
            coCoinUtil.getTagIcon(record.tag))
        holder.date.text = record.getCalendarString(coCoinUtil)
        holder.money.text = record.money.toInt().toString()
        //        holder.date.setTypeface( coCoinUtil.typefaceLatoLight);
//        holder.money.setTypeface( coCoinUtil.typefaceLatoLight);
        holder.money.setTextColor(ContextCompat.getColor(CoCoinApplication.getAppContext(),
            R.color.my_blue))
        holder.index.text = (position + 1).toString() + ""
        //        holder.index.setTypeface( coCoinUtil.typefaceLatoLight);
        holder.remark.text = record.remark
        //        holder.remark.setTypeface( coCoinUtil.typefaceLatoLight);

        // set background resource (target view ID: container)
        val swipeState = holder.swipeStateFlags
        if (swipeState and SwipeableItemConstants.STATE_FLAG_IS_UPDATED != 0) {
            val bgResId: Int
            bgResId = if (swipeState and SwipeableItemConstants.STATE_FLAG_IS_ACTIVE != 0) {
                R.drawable.bg_item_swiping_active_state
            } else if (swipeState and SwipeableItemConstants.STATE_FLAG_SWIPING != 0) {
                R.drawable.bg_item_swiping_state
            } else {
                R.drawable.bg_item_normal_state
            }
            holder.mContainer.setBackgroundResource(bgResId)
        }
        holder.setSwipeItemHorizontalSlideAmount(
            if (pinned[records[records.size - 1 - position].id.toInt()]!!) {
                SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_LEFT
            } else {
                0.toFloat()
            }
        )
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onGetSwipeReactionType(holder: MyViewHolder?, position: Int, x: Int, y: Int): Int {
        return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H
    }

    override fun onSetSwipeBackground(holder: MyViewHolder?, position: Int, type: Int) {
        var bgRes = 0
        when (type) {
            SwipeableItemConstants.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND -> bgRes =
                R.drawable.bg_swipe_item_neutral
            SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND -> bgRes =
                R.drawable.bg_swipe_item_left
            SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND -> bgRes =
                R.drawable.bg_swipe_item_right
        }
        holder?.itemView?.setBackgroundResource(bgRes)
    }

    override fun onSwipeItem(holder: MyViewHolder?, position: Int, result: Int): SwipeResultAction {
        return when (result) {
            SwipeableItemConstants.RESULT_SWIPED_RIGHT -> if (pinned[records[records.size - 1 - position].id.toInt()]!!
            ) {
                UnpinResultAction(this, position)
            } else {
                SwipeRightResultAction(this, coCoinUtil, position, onItemDeleteListener)
            }
            SwipeableItemConstants.RESULT_SWIPED_LEFT -> SwipeLeftResultAction(this, position)
//            SwipeableItemConstants.RESULT_CANCELED -> if (position != RecyclerView.NO_POSITION) {
//                UnpinResultAction(this, position)
//            } else {
//                null
//            }
            else -> {
                UnpinResultAction(this, position)
            }
//                if (position != RecyclerView.NO_POSITION) {
//                    UnpinResultAction(this, position)
//                } else {
//                    null
//                }
//            }
        }
    }

    private class SwipeLeftResultAction internal constructor(
        private var mAdapter: MySwipeableItemAdapter?,
        private val mPosition: Int
    ) : SwipeResultActionMoveToSwipedDirection() {
        private var mSetPinned = false
        override fun onPerformAction() {
            super.onPerformAction()
            if (!pinned[RecordManager.SELECTED_RECORDS[RecordManager.SELECTED_RECORDS.size - 1 - mPosition].id.toInt()]!!
            ) {
                pinned[RecordManager.SELECTED_RECORDS[RecordManager.SELECTED_RECORDS.size - 1 - mPosition].id.toInt()] =
                    true
                mSetPinned = true
                mAdapter!!.notifyItemChanged(mPosition)
            }
        }

        override fun onSlideAnimationEnd() {
            super.onSlideAnimationEnd()
            if (mSetPinned && mAdapter!!.eventListener != null) {
                mAdapter!!.eventListener!!.onItemPinned(mPosition)
            }
        }

        override fun onCleanUp() {
            super.onCleanUp()
            mAdapter = null
        }
    }

    class SwipeRightResultAction internal constructor(
        private var mAdapter: MySwipeableItemAdapter?,
        private val coCoinUtil: CoCoinUtil,
        private val mPosition: Int,
        private val onItemDeleteListener: OnItemDeleteListener
    ) : SwipeResultActionRemoveItem() {
        override fun onPerformAction() {
            super.onPerformAction()
            if ( coCoinUtil.backupCoCoinRecord != null) {
                deleteRecord( coCoinUtil.backupCoCoinRecord!!, true)
            }
             coCoinUtil.backupCoCoinRecord = null
             coCoinUtil.backupCoCoinRecord =
                RecordManager.SELECTED_RECORDS[RecordManager.SELECTED_RECORDS.size - 1 - mPosition]
            RecordManager.SELECTED_RECORDS.removeAt(RecordManager.SELECTED_RECORDS.size - 1 - mPosition)
            RecordManager.SELECTED_SUM -= coCoinUtil.backupCoCoinRecord!!.money
            onItemDeleteListener.onSelectSumChanged()
            mAdapter!!.notifyItemRemoved(mPosition)
        }

        override fun onSlideAnimationEnd() {
            super.onSlideAnimationEnd()
            if (mAdapter!!.eventListener != null) {
                mAdapter!!.eventListener!!.onItemRemoved(mPosition)
            }
        }

        override fun onCleanUp() {
            super.onCleanUp()
            // clear the references
            mAdapter = null
        }
    }

    private class UnpinResultAction internal constructor(
        private var mAdapter: MySwipeableItemAdapter?,
        private val mPosition: Int
    ) : SwipeResultActionDefault() {
        override fun onPerformAction() {
            super.onPerformAction()
            if (pinned[RecordManager.SELECTED_RECORDS[RecordManager.SELECTED_RECORDS.size - 1 - mPosition].id.toInt()]!!
            ) {
                pinned[RecordManager.SELECTED_RECORDS[RecordManager.SELECTED_RECORDS.size - 1 - mPosition].id.toInt()] =
                    false
                mAdapter!!.notifyItemChanged(mPosition)
            }
        }

        override fun onCleanUp() {
            super.onCleanUp()
            // clear the references
            mAdapter = null
        }
    }

    fun setPinned(inPinned: Boolean, position: Int) {
        pinned[RecordManager.SELECTED_RECORDS[RecordManager.SELECTED_RECORDS.size - 1 - position].id.toInt()] =
            inPinned
    }

    interface OnItemDeleteListener {
        fun onSelectSumChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private var pinned = mutableMapOf<Int, Boolean>()
    }
}