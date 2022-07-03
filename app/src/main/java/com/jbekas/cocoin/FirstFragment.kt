package com.jbekas.cocoin

import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.LinearLayout
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jbekas.cocoin.adapter.ButtonGridViewAdapter
import com.jbekas.cocoin.adapter.EditMoneyRemarkFragmentAdapter
import com.jbekas.cocoin.adapter.TagChooseFragmentAdapter
import com.jbekas.cocoin.databinding.FragmentFirstBinding
import com.jbekas.cocoin.fragment.CoCoinFragmentManager
import com.jbekas.cocoin.model.RecordManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.ui.CoCoinScrollableViewPager
import com.jbekas.cocoin.ui.MyGridView
import com.jbekas.cocoin.util.CoCoinUtil
import timber.log.Timber

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var editAdapter: FragmentPagerAdapter? = null
    private var tagAdapter: FragmentStateAdapter? = null
    private var myGridViewAdapter: ButtonGridViewAdapter? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }

        // edit viewpager///////////////////////////////////////////////////////////////////////////////////
//        editViewPager = findViewById<View>(R.id.edit_pager) as CoCoinScrollableViewPager
//        editAdapter = EditMoneyRemarkFragmentAdapter(fragmentManager,
//            CoCoinFragmentManager.MAIN_ACTIVITY_FRAGMENT)
//        binding.editPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int,
//            ) {
//                if (position == 1) {
//                    if (CoCoinFragmentManager.mainActivityEditRemarkFragment != null) CoCoinFragmentManager.mainActivityEditRemarkFragment.editRequestFocus()
//                } else {
//                    if (CoCoinFragmentManager.mainActivityEditMoneyFragment != null) CoCoinFragmentManager.mainActivityEditMoneyFragment.editRequestFocus()
//                }
//            }
//
//            override fun onPageSelected(position: Int) {}
//            override fun onPageScrollStateChanged(state: Int) {}
//        })
//        binding.editPager.adapter = editAdapter

        // tag viewpager////////////////////////////////////////////////////////////////////////////////////
//        tagViewPager = findViewById<View>(R.id.viewpager) as ViewPager
//        tagAdapter = if (RecordManager.TAGS.size % 8 == 0) TagChooseFragmentAdapter(
//            fragmentManager, RecordManager.TAGS.size / 8) else TagChooseFragmentAdapter(
//            fragmentManager, RecordManager.TAGS.size / 8 + 1)
        tagAdapter = TagChooseFragmentAdapter(activity, RecordManager.getNumberOfTagPages(8))
        binding.viewpager.adapter = tagAdapter
        tagAdapter?.notifyDataSetChanged()

        for (tag in RecordManager.TAGS) {
            Timber.d(tag.toString())
        }

        // button grid view/////////////////////////////////////////////////////////////////////////////////
        //myGridView = findViewById<View>(R.id.gridview) as MyGridView
        myGridViewAdapter = ButtonGridViewAdapter(activity)
        binding.gridview.adapter = myGridViewAdapter
        binding.gridview.onItemClickListener = gridViewClickListener
        binding.gridview.onItemLongClickListener = gridViewLongClickListener
        binding.gridview.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.gridview.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    val lastChild = binding.gridview.getChildAt(binding.gridview.childCount - 1)
                    binding.gridview.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT, lastChild.bottom)
                }
            })
        myGridViewAdapter!!.notifyDataSetInvalidated()
    }

    override fun onResume() {
        super.onResume()


        Timber.d("is tag adapter empty? %s", (tagAdapter == null))

        if (SettingManager.getInstance().mainActivityTagShouldChange) {
            // change the tag fragment
            var i = 0
            while (i < tagAdapter!!.itemCount && i < CoCoinFragmentManager.tagChooseFragments.size) {
                if (CoCoinFragmentManager.tagChooseFragments[i] != null) CoCoinFragmentManager.tagChooseFragments[i].updateTags()
                i++
            }
            // and tell others that main activity has changed
            SettingManager.getInstance().mainActivityTagShouldChange = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val gridViewLongClickListener = OnItemLongClickListener { parent, view, position, id ->
        if (!isLoading) {
            buttonClickOperation(true, position)
        }
        true
    }

    private val gridViewClickListener =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            if (!isLoading) {
                buttonClickOperation(false, position)
            }
        }

    private fun buttonClickOperation(longClick: Boolean, position: Int) {
//        if (editViewPager!!.currentItem == 1) return
        if (CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString() == "0" && !CoCoinUtil.ClickButtonCommit(
                position)
        ) {
            if (CoCoinUtil.ClickButtonDelete(position)
                || CoCoinUtil.ClickButtonIsZero(position)
            ) {
            } else {
                CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText =
                    CoCoinUtil.BUTTONS[position]
            }
        } else {
            if (CoCoinUtil.ClickButtonDelete(position)) {
                if (longClick) {
                    CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText = "0"
                    CoCoinFragmentManager.mainActivityEditMoneyFragment.helpText =
                        CoCoinUtil.FLOATINGLABELS[CoCoinFragmentManager.mainActivityEditMoneyFragment
                            .numberText.toString().length]
                } else {
                    CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText =
                        CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString()
                            .substring(0, CoCoinFragmentManager.mainActivityEditMoneyFragment
                                .numberText.toString().length - 1)
                    if (CoCoinFragmentManager.mainActivityEditMoneyFragment
                            .numberText.toString().length == 0
                    ) {
                        CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText = "0"
                        CoCoinFragmentManager.mainActivityEditMoneyFragment.helpText = " "
                    }
                }
            } else if (CoCoinUtil.ClickButtonCommit(position)) {
                //commit()
            } else {
                CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText = (
                        CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString()
                                + CoCoinUtil.BUTTONS[position])
            }
        }
        CoCoinFragmentManager.mainActivityEditMoneyFragment.helpText =
            CoCoinUtil.FLOATINGLABELS[CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString().length]
    }
}