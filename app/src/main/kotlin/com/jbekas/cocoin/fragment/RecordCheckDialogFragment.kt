package com.jbekas.cocoin.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import com.jbekas.cocoin.adapter.RecordCheckDialogRecyclerViewAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jbekas.cocoin.model.CoCoinRecord
import android.os.Bundle
import android.view.LayoutInflater
import com.jbekas.cocoin.R
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.jbekas.cocoin.util.CoCoinUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordCheckDialogFragment : DialogFragment,
    RecordCheckDialogRecyclerViewAdapter.OnItemClickListener {

    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var recyclerView: RecyclerView? = null
    private var list: List<CoCoinRecord>? = null
    private var mContext: Context? = null
    private var title: String? = null

    constructor(context: Context?, list: List<CoCoinRecord>?, title: String?) {
        this.list = list
        this.title = title
        mContext = context
    }

    constructor() {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context: Context? = activity
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_dialog_list, null, false)
        recyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        val adapter = RecordCheckDialogRecyclerViewAdapter(context, coCoinUtil, list, this)
        recyclerView!!.adapter = adapter
        builder.setTitle("Title")
        builder.setView(view)
        builder.setPositiveButton(mContext!!.resources.getString(android.R.string.ok)
        ) { dialog, which -> }
        val alert = builder.create()
        val title = TextView(mContext)
        title.height = 120
        title.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        //        title.setTypeface(CoCoinUtil.typefaceLatoLight);
        title.text = this.title
        alert.setCustomTitle(title)
        alert.setOnShowListener {
            val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
            //                btnPositive.setTypeface(CoCoinUtil.typefaceLatoLight);
        }
        return alert
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        recyclerView = null
        mContext = null
        list = null
        title = null
    }

    override fun onItemClick(view: View, position: Int) {
        val subTitle: String
        val spend = list!![position].money
        val tagId = list!![position].tag
        subTitle = "Spent ${spend.toInt()} in ${coCoinUtil.getTagName(tagId)}"
        val dialog = MaterialDialog.Builder(mContext!!)
            .icon(coCoinUtil.getTagIconDrawable(list!![position].tag)!!)
            .limitIconToDefaultSize()
            .title(subTitle)
            .customView(R.layout.dialog_a_record, true)
            .positiveText(android.R.string.ok)
            .show()
        val dialogView = dialog.getCustomView()
        val remark = dialogView!!.findViewById<View>(R.id.remark) as TextView
        val date = dialogView.findViewById<View>(R.id.date) as TextView
        remark.text = list!![position].remark
        date.text =
            coCoinUtil.getCalendarStringRecordCheckDialog(mContext!!, list!![position].calendar)
    }
}