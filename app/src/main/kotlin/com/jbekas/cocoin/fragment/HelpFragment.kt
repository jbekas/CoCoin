package com.jbekas.cocoin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jbekas.cocoin.R
import com.jbekas.cocoin.databinding.FragmentHelpBinding
import com.jbekas.cocoin.util.CoCoinUtil
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HelpFragment : Fragment() {
    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var imageView1: ImageView? = null
    private var imageView2: ImageView? = null
    private var imageView3: ImageView? = null
    private var imageView4: ImageView? = null
    private var imageView5: ImageView? = null
    private var imageView6: ImageView? = null
    private var imageView7: ImageView? = null
    private var imageView8: ImageView? = null
    private var imageView9: ImageView? = null
    private var title: TextView? = null
    private var textView2: TextView? = null
    private var textView3: TextView? = null
    private var textView4: TextView? = null
    private var textView5: TextView? = null
    private var textView6: TextView? = null
    private var textView7: TextView? = null
    private var textView8: TextView? = null
    private var textView9: TextView? = null
    private var textView10: TextView? = null
    private var foot: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentHelpBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val width = coCoinUtil.getScreenWidth(requireActivity()) - coCoinUtil.dpToPx(20)
        val height = width * 653 / 1280
        val height2 = width * 1306 / 960
        title = view.findViewById<View>(R.id.title) as TextView
        //        title.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView1 = view.findViewById<View>(R.id.help_cocoin_image_1) as ImageView
        var layoutParams = imageView1!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height

        val picasso = Picasso.with(requireContext())

        picasso
            .load("http://file.bmob.cn/M02/7A/CC/oYYBAFaxox2AYPyvAAIBwVjp9Ps450.png")
            .resize(width, height)
            .centerCrop()
            .into(imageView1)
        textView2 = view.findViewById<View>(R.id.help_cocoin_content_2) as TextView
        //        textView2.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView2 = view.findViewById<View>(R.id.help_cocoin_image_2) as ImageView
        layoutParams = imageView2!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        picasso
            .load("http://file.bmob.cn/M02/7A/F2/oYYBAFaxthGAFpc2AALJcSxCKIY003.png")
            .resize(width, height)
            .centerCrop()
            .into(imageView2)
        textView3 = view.findViewById<View>(R.id.help_cocoin_content_3) as TextView
        //        textView3.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView3 = view.findViewById<View>(R.id.help_cocoin_image_3) as ImageView
        layoutParams = imageView3!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        picasso
            .load("http://file.bmob.cn/M02/7A/F3/oYYBAFaxtiOAZDsYAAKtT7PeOP4375.png")
            .resize(width, height)
            .centerCrop()
            .into(imageView3)
        textView4 = view.findViewById<View>(R.id.help_cocoin_content_4) as TextView
        //        textView4.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView4 = view.findViewById<View>(R.id.help_cocoin_image_4) as ImageView
        layoutParams = imageView4!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        picasso
            .load("http://file.bmob.cn/M02/7A/F3/oYYBAFaxtkeAafYNAAa6d5bj-jk765.png")
            .resize(width, height)
            .centerCrop()
            .into(imageView4)
        textView5 = view.findViewById<View>(R.id.help_cocoin_content_5) as TextView
        //        textView5.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView5 = view.findViewById<View>(R.id.help_cocoin_image_5) as ImageView
        layoutParams = imageView5!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        picasso
            .load("http://file.bmob.cn/M02/7A/F3/oYYBAFaxtl2AQQ7PAAO6x9BzDeE570.png")
            .resize(width, height)
            .centerCrop()
            .into(imageView5)
        textView6 = view.findViewById<View>(R.id.help_cocoin_content_6) as TextView
        //        textView6.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView6 = view.findViewById<View>(R.id.help_cocoin_image_6) as ImageView
        layoutParams = imageView6!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        picasso
            .load("http://file.bmob.cn/M02/7A/F3/oYYBAFaxtm2Aar8bAAMk6Ll6aq8839.png")
            .resize(width, height)
            .centerCrop()
            .into(imageView6)
        textView7 = view.findViewById<View>(R.id.help_cocoin_content_7) as TextView
        //        textView7.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView7 = view.findViewById<View>(R.id.help_cocoin_image_7) as ImageView
        layoutParams = imageView7!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height2
        picasso
            .load("http://file.bmob.cn/M02/7A/F3/oYYBAFaxtoaALyf-AAX0r-dNE0M891.png")
            .resize(width, height2)
            .centerCrop()
            .into(imageView7)
        textView8 = view.findViewById<View>(R.id.help_cocoin_content_8) as TextView
        //        textView8.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView8 = view.findViewById<View>(R.id.help_cocoin_image_8) as ImageView
        layoutParams = imageView8!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        picasso
            .load("http://file.bmob.cn/M02/7A/F4/oYYBAFaxtreAYbnKAANJdVJiQs0863.png")
            .resize(width, height)
            .centerCrop()
            .into(imageView8)
        textView9 = view.findViewById<View>(R.id.help_cocoin_content_9) as TextView
        //        textView9.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        imageView9 = view.findViewById<View>(R.id.help_cocoin_image_9) as ImageView
        layoutParams = imageView9!!.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        picasso
            .load("http://file.bmob.cn/M02/7A/F4/oYYBAFaxts-AdUjPAAKhvkkpEtk060.png")
            .resize(width, height)
            .centerCrop()
            .into(imageView9)
        textView10 = view.findViewById<View>(R.id.help_cocoin_content_10) as TextView
        //        textView10.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
        foot = view.findViewById<View>(R.id.foot) as TextView
        //        foot.setTypeface(CoCoinUtil.getInstance().typefaceLatoLight);
    }

    companion object {
        fun newInstance(): HelpFragment {
            return HelpFragment()
        }
    }
}