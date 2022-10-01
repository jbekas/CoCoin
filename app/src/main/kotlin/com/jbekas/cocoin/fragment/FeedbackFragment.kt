package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cn.bmob.v3.listener.SaveListener
import com.afollestad.materialdialogs.MaterialDialog
import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.databinding.FragmentFeedbackBinding
import com.jbekas.cocoin.model.Feedback
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeedbackFragment : Fragment() {
    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private val chineseIsDoubleCount = true
    private var title: TextView? = null
    private var input: EditText? = null
    private var help: TextView? = null
    private var number: TextView? = null
    private var send: TextView? = null
    private val min = 1
    private val max = 400
    private var exceed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentFeedbackBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = view.findViewById<View>(R.id.title) as TextView
        //        title.setTypeface(coCoinUtil.typefaceLatoLight);
        input = view.findViewById<View>(R.id.edittext) as EditText
        //        input.setTypeface(coCoinUtil.typefaceLatoLight);
        help = view.findViewById<View>(R.id.helper) as TextView
        //        help.setTypeface(coCoinUtil.typefaceLatoLight);
        number = view.findViewById<View>(R.id.number) as TextView
        //        number.setTypeface(coCoinUtil.typefaceLatoLight);
        send = view.findViewById<View>(R.id.send) as TextView
        send!!.setOnClickListener {
            if (exceed) {
                MaterialDialog.Builder(requireContext())
                    .title(R.string.help_feedback_dialog_title)
                    .content(R.string.help_feedback_dialog_content)
                    .positiveText(R.string.ok_1)
                    .show()
            } else {
                ToastUtil.showToast(
                    requireActivity(),
                    R.string.help_feedback_sent
                )
                val feedback = Feedback()
                feedback.content = input!!.text.toString()
                feedback.save(CoCoinApplication.getAppContext(), object : SaveListener() {
                    override fun onSuccess() {
                        ToastUtil.showToast(
                            activity = requireActivity(),
                            text = resources.getString(R.string.help_feedback_sent_successfully)
                        )
                    }

                    override fun onFailure(code: Int, arg0: String) {
                        ToastUtil.showToast(
                            activity = requireActivity(),
                            text = resources.getString(R.string.help_feedback_sent_fail)
                        )
                    }
                })
            }
        }
        input!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setNumberText()
                try {
                    (activity as OnTextChangeListener)
                        .onTextChange(input!!.text.toString(), exceed)
                } catch (cce: ClassCastException) {
                    cce.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        send!!.requestFocus()
        setNumberText()
    }

    private fun setNumberText() {
        var count = -1
        count = if (chineseIsDoubleCount) {
            coCoinUtil.textCounter(input!!.text.toString())
        } else {
            input!!.text.toString().length
        }
        number!!.text = "$count/$min-$max"
        exceed = if (min <= count && count <= max) {
            number!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.my_blue))
            false
        } else {
            number!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            true
        }
    }

    interface OnTextChangeListener {
        fun onTextChange(text: String, exceed: Boolean)
    }

    companion object {
        fun newInstance(): FeedbackFragment {
            return FeedbackFragment()
        }
    }
}