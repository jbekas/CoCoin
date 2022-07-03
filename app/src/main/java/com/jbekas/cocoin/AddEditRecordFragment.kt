package com.jbekas.cocoin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jbekas.cocoin.NewMainActivity.Companion.NO_MONEY_TOAST
import com.jbekas.cocoin.NewMainActivity.Companion.NO_TAG_TOAST
import com.jbekas.cocoin.adapter.ButtonGridViewAdapter
import com.jbekas.cocoin.adapter.TagChooseFragmentAdapter
import com.jbekas.cocoin.databinding.FragmentAddEditRecordBinding
import com.jbekas.cocoin.fragment.CoCoinFragmentManager
import com.jbekas.cocoin.fragment.TagChooseFragment
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.RecordManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.util.CoCoinUtil
import timber.log.Timber
import java.lang.Float
import java.util.*
import kotlin.Boolean
import kotlin.Int
import kotlin.let
import kotlin.toString

class AddEditRecordFragment : Fragment(), TagChooseFragment.OnTagItemSelectedListener {

    private var _binding: FragmentAddEditRecordBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var editAdapter: FragmentPagerAdapter? = null
    private var tagAdapter: FragmentStateAdapter? = null
    private var myGridViewAdapter: ButtonGridViewAdapter? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentAddEditRecordBinding.inflate(inflater, container, false)
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
        tagAdapter = TagChooseFragmentAdapter(activity!!, this, RecordManager.getNumberOfTagPages(8))
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
        if (binding.editMoney.money.text.toString() == "0" && !CoCoinUtil.ClickButtonCommit(position)) {
            if (CoCoinUtil.ClickButtonDelete(position) || CoCoinUtil.ClickButtonIsZero(position)) {
            } else {
                Timber.d("%s", CoCoinUtil.BUTTONS[position])
                binding.editMoney.money.setText(CoCoinUtil.BUTTONS[position])
            }
        } else {
            if (CoCoinUtil.ClickButtonDelete(position)) {
                if (longClick) {
                    binding.editMoney.money.setText("0")
                    binding.editMoney.money.setHelperText(
                        CoCoinUtil.FLOATINGLABELS[binding.editMoney.money.toString().length]
                    )
                } else {
                    binding.editMoney.money.setText(
                        binding.editMoney.money.text.toString()
                            .substring(0, binding.editMoney.money.text.toString().length - 1))
                    if (binding.editMoney.money.text?.isEmpty() == true) {
                        binding.editMoney.money.setText("0")
                        binding.editMoney.money.setHelperText("")
                    }
                }
            } else if (CoCoinUtil.ClickButtonCommit(position)) {
                commit()
            } else {
                binding.editMoney.money.setText(
                        binding.editMoney.money.text.toString() + CoCoinUtil.BUTTONS[position]
                )
            }
        }
        binding.editMoney.money.setHelperText(
            CoCoinUtil.FLOATINGLABELS[(binding.editMoney.money.text ?: "").length]
        )
    }

    private fun commit() {
        if (tagId  == -1) {
            activity?.let {
                (activity as NewMainActivity).showToast(NO_TAG_TOAST)
            }
        } else if (binding.editMoney.money.text.toString() == "0") {
            activity?.let {
                (activity as NewMainActivity).showToast(NO_MONEY_TOAST)
            }
        } else {
            val calendar = Calendar.getInstance()
            val coCoinRecord = CoCoinRecord(
                -1,
                Float.valueOf(binding.editMoney.money.text.toString()),
                "RMB",
                tagId,
                calendar)
//            coCoinRecord.remark = CoCoinFragmentManager.mainActivityEditRemarkFragment.remark
            coCoinRecord.remark = "remark"
            val saveId = RecordManager.saveRecord(coCoinRecord)
            if (saveId == -1L) {
            } else {
//                if (!superToast!!.isShowing) {
//                    changeColor()
//                }
                binding.editMoney.tagImage.setImageResource(R.color.transparent)
                binding.editMoney.tagName.setText("")
            }
            binding.editMoney.money.setText("0")
            binding.editMoney.money.setHelperText("")
        }
    }

    private var tagId = -1
    var tagImage: ImageView? = null
    var tagName: TextView? = null


    override fun onTagItemPicked(position: Int) {
        tagId = RecordManager.TAGS[position].id
        binding.editMoney.tagName.text = CoCoinUtil.GetTagName(RecordManager.TAGS[position].id)
        binding.editMoney.tagImage.setImageResource(CoCoinUtil.GetTagIcon(RecordManager.TAGS[position].id))
    }

    override fun onAnimationStart(id: Int) {
        Timber.w("onAnimationStart: Not yet implemented")
    }
}