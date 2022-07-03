package com.jbekas.cocoin.fragment

import com.jbekas.cocoin.adapter.TagChooseGridViewAdapter
import com.jbekas.cocoin.ui.MyGridView
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.jbekas.cocoin.R
import com.jbekas.cocoin.fragment.CoCoinFragmentManager
import com.jbekas.cocoin.fragment.TagChooseFragment
import android.widget.AdapterView
import com.jbekas.cocoin.model.RecordManager
import android.widget.BaseAdapter
import androidx.fragment.app.Fragment
import java.lang.ClassCastException

class TagChooseFragment : Fragment() {
    var tagAdapter: TagChooseGridViewAdapter? = null
    private var fragmentPosition = 0
    var myGridView: MyGridView? = null
    var activity: Activity? = null
    var tagSelectionListener: TagChooseFragment.OnTagItemSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            activity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tag_choose_fragment, container, false)
        myGridView = view.findViewById<View>(R.id.gridview) as MyGridView
        fragmentPosition = arguments!!.getInt("position")
        if (fragmentPosition >= CoCoinFragmentManager.tagChooseFragments.size) {
            while (fragmentPosition >= CoCoinFragmentManager.tagChooseFragments.size) {
                CoCoinFragmentManager.tagChooseFragments.add(TagChooseFragment())
            }
        }
        CoCoinFragmentManager.tagChooseFragments[fragmentPosition] = this
        tagAdapter = TagChooseGridViewAdapter(getActivity(), fragmentPosition)
        myGridView!!.adapter = tagAdapter
        myGridView!!.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                try {
                    tagSelectionListener?.onTagItemPicked(position)
                    tagSelectionListener?.onAnimationStart(
                        RecordManager.TAGS[fragmentPosition * 8 + position + 2].id)
                } catch (cce: ClassCastException) {
                    cce.printStackTrace()
                }
            }
        return view
    }

    interface OnTagItemSelectedListener {
        fun onTagItemPicked(position: Int)
        fun onAnimationStart(id: Int)
    }

    fun updateTags() {
        (myGridView!!.adapter as BaseAdapter).notifyDataSetChanged()
        (myGridView!!.adapter as BaseAdapter).notifyDataSetInvalidated()
        myGridView!!.invalidateViews()
    }

    companion object {
        fun newInstance(
            tagSelectionListener: OnTagItemSelectedListener,
            position: Int
        ): TagChooseFragment {

            val fragment = TagChooseFragment()
            val args = Bundle()
            args.putInt("position", position)
            fragment.arguments = args

            fragment.tagSelectionListener = tagSelectionListener

            return fragment
        }
    }
}