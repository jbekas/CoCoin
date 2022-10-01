package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.jbekas.cocoin.R
import com.jbekas.cocoin.adapter.ButtonGridViewAdapter
import com.jbekas.cocoin.adapter.TagChooseFragmentAdapter
import com.jbekas.cocoin.databinding.FragmentAddEditRecordBinding
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.service.ToastService
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.Constants
import com.jbekas.cocoin.viewmodel.AddEditTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddEditRecordFragment : Fragment(), TagChooseFragment.OnTagItemSelectedListener {

    @Inject
    lateinit var toastService: ToastService

    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var _binding: FragmentAddEditRecordBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: AddEditTransactionViewModel by viewModels()

    private var tagAdapter: FragmentStateAdapter? = null
    private var myGridViewAdapter: ButtonGridViewAdapter? = null
    private var isLoading = false

    companion object {
        const val ZERO = "0"
        const val MAX_EXPENSE_DIGITS = 5
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentAddEditRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Tag selection
        activity?.let {
            tagAdapter = TagChooseFragmentAdapter(it, this, RecordManager.getNumberOfTagPages(8))
            binding.viewpager.adapter = tagAdapter
            tagAdapter?.notifyDataSetChanged()
        }

        // Custom Numeric keypad
        myGridViewAdapter = ButtonGridViewAdapter(activity, coCoinUtil)
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

//        if (SettingManager.getInstance().mainActivityTagShouldChange) {
//            // change the tag fragment
//            var i = 0
//            while (i < tagAdapter!!.itemCount && i < CoCoinFragmentManager.tagChooseFragments.size) {
//                if (CoCoinFragmentManager.tagChooseFragments[i] != null) CoCoinFragmentManager.tagChooseFragments[i].updateTags()
//                i++
//            }
//            // and tell others that main activity has changed
//            SettingManager.getInstance().mainActivityTagShouldChange = false
//        }
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
        if (binding.editMoney.money.text.toString() == ZERO && !coCoinUtil.clickButtonCommit(position)) {
            if (coCoinUtil.clickButtonDelete(position) || coCoinUtil.clickButtonIsZero(position)) {
            } else {
                Timber.d("%s", coCoinUtil.BUTTONS[position])
                viewModel.amount.postValue(coCoinUtil.BUTTONS[position])
            }
        } else {
            if (coCoinUtil.clickButtonDelete(position)) {
                if (longClick) {
                    viewModel.amount.postValue(ZERO)
                } else {
                    var amount = viewModel.amount.value.toString().dropLast(1)
                    if (amount.isEmpty()) {
                        amount = ZERO
                    }
                    viewModel.amount.postValue(amount)
                }
            } else if (coCoinUtil.clickButtonCommit(position)) {
                commit()
            } else {
                if (viewModel.amount.value.toString().length < MAX_EXPENSE_DIGITS) {
                    val amount = viewModel.amount.value.toString() + coCoinUtil.BUTTONS[position]
                    viewModel.amount.postValue(amount)
                } else {
                    Timber.w("Too many digits")
                }
            }
        }
    }

    private fun commit() {
        val tagId = viewModel.tagId.value ?: -1
        if (tagId == -1) {
            tagAnimation()
            toastService.showErrorToast(text = context?.getText(R.string.toast_no_tag).toString())
        } else if (viewModel.amount.value.toString() == ZERO) {
            toastService.showErrorToast(text = context?.getText(R.string.toast_no_amount).toString())
        } else {
            val calendar = Calendar.getInstance()
            val coCoinRecord = CoCoinRecord(
                -1,
                viewModel.amount.value.toString().toFloat(),
                Constants.USD,
                tagId,
                calendar)
//            coCoinRecord.remark = CoCoinFragmentManager.mainActivityEditRemarkFragment.remark
            coCoinRecord.remark = ""
            val saveId = RecordManager.saveRecord(coCoinRecord)
            if (saveId == -1L) {
                toastService.showErrorToast(resources.getString(R.string.save_failed_locale))
            } else {
                toastService.showSuccessToast(resources.getString(R.string.save_successfully_locale))

//                if (!superToast!!.isShowing) {
//                    changeColor()
//                }
                viewModel.tagId.postValue(-1)
                binding.editMoney.tagImage.setImageResource(R.color.transparent)
                binding.editMoney.tagName.text = ""
            }
            viewModel.amount.postValue(ZERO)
        }
    }

    override fun onTagItemPicked(position: Int) {
        val tagId = viewModel.tagId.value ?: -1
        val newTagId = RecordManager.TAGS[position].id

        if (tagId == newTagId) {
            viewModel.tagId.postValue(-1)
            binding.editMoney.tagName.text = ""
            binding.editMoney.tagImage.setImageResource(R.color.transparent)
        } else {
            viewModel.tagId.postValue(newTagId)
            binding.editMoney.tagName.text = coCoinUtil.getTagName(RecordManager.TAGS[position].id)
            binding.editMoney.tagImage.setImageResource(coCoinUtil.getTagIcon(RecordManager.TAGS[position].id))
        }
    }

    override fun onAnimationStart(id: Int) {
        Timber.w("onAnimationStart: Not yet implemented")
    }

    private fun tagAnimation() {
        YoYo.with(Techniques.Shake).duration(1000).playOn(binding.viewpager)
    }
}