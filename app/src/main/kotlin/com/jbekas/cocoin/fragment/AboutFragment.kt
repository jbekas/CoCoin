package com.jbekas.cocoin.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jbekas.cocoin.R
import com.jbekas.cocoin.databinding.FragmentAboutBinding
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : Fragment() {
    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var _binding: FragmentAboutBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAboutBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layout2.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/Nightonke/CoCoin")))
        }
        binding.layout3.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://blog.csdn.net/u012925008")))
        }
        binding.layout4.setOnClickListener {
            coCoinUtil.copyToClipboard("Nightonke@outlook.com", requireContext())
            ToastUtil.showToast(
                activity = requireActivity(),
                text = resources.getString(R.string.copy_to_clipboard)
            )
        }
    }

    companion object {
        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }
}