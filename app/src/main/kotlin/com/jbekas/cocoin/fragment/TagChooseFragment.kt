package com.jbekas.cocoin.fragment

import com.jbekas.cocoin.adapter.TagChooseGridViewAdapter
import com.jbekas.cocoin.ui.MyGridView
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.jbekas.cocoin.R
import android.widget.BaseAdapter
import androidx.fragment.app.Fragment
import com.jbekas.cocoin.util.CoCoinUtil
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TagChooseFragment : Fragment() {
    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    var tagAdapter: TagChooseGridViewAdapter? = null
    private var fragmentPosition = 0
    var tagGridView: MyGridView? = null
    var tagSelectionListener: TagChooseFragment.OnTagItemSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView called")
        val view = inflater.inflate(R.layout.tag_choose_fragment, container, false)
        tagGridView = view.findViewById<View>(R.id.tag_grid_view) as MyGridView
        fragmentPosition = arguments!!.getInt("position")
//        if (fragmentPosition >= CoCoinFragmentManager.tagChooseFragments.size) {
//            while (fragmentPosition >= CoCoinFragmentManager.tagChooseFragments.size) {
//                CoCoinFragmentManager.tagChooseFragments.add(TagChooseFragment())
//            }
//        }
//        CoCoinFragmentManager.tagChooseFragments[fragmentPosition] = this
        context?.let { context ->
            tagAdapter = TagChooseGridViewAdapter(
                context = context,
                coCoinUtil = coCoinUtil,
                fragmentPosition,
                tagSelectionListener
            )
            tagGridView!!.adapter = tagAdapter
        }

        return view
    }

    interface OnTagItemSelectedListener {
        fun onTagItemPicked(position: Int)
        fun onAnimationStart(id: Int)
    }

    fun updateTags() {
        (tagGridView!!.adapter as BaseAdapter).notifyDataSetChanged()
        (tagGridView!!.adapter as BaseAdapter).notifyDataSetInvalidated()
        tagGridView!!.invalidateViews()
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