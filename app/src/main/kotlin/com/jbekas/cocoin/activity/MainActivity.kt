package com.jbekas.cocoin.activity

import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobUser
import com.afollestad.materialdialogs.MaterialDialog
import com.github.johnpersano.supertoasts.SuperActivityToast
import com.github.johnpersano.supertoasts.SuperToast
import com.jbekas.cocoin.R
import com.jbekas.cocoin.adapter.ButtonGridViewAdapter
import com.jbekas.cocoin.adapter.EditMoneyRemarkFragmentAdapter
import com.jbekas.cocoin.databinding.ActivityMainBinding
import com.jbekas.cocoin.fragment.CoCoinFragmentManager
import com.jbekas.cocoin.model.AppUpdateManager
import com.jbekas.cocoin.model.CoCoin
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.model.User
import com.jbekas.cocoin.service.ToastService
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.Constants
import com.jbekas.cocoin.util.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var coCoinToast: ToastService

    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    companion object {
        private const val SHAKE_ACCELERATED_SPEED = 15f

        private val SETTING_TAG = 0
        private val PIN_TAG = 1

        val LOGIN_SUCCESSFUL = "login_successful"
    }

    private var menuToolBarTitle: TextView? = null
    private var passwordTip: TextView? = null
    private var superToast: SuperToast? = null
    private var superActivityToast: SuperActivityToast? = null
    private var myGridViewAdapter: ButtonGridViewAdapter? = null
    private var inputPassword = ""
//    private var tagViewPager: ViewPager? = null
//    private var editViewPager: CoCoinScrollableViewPager? = null
    private var tagAdapter: FragmentPagerAdapter? = null
    private var editAdapter: FragmentPagerAdapter? = null
    private var isLoading = false
    private val NO_TAG_TOAST = 0
    private val NO_MONEY_TOAST = 1
    private val PASSWORD_WRONG_TOAST = 2
    private val PASSWORD_CORRECT_TOAST = 3
    private val SAVE_SUCCESSFULLY_TOAST = 4
    private val SAVE_FAILED_TOAST = 5
    private val PRESS_AGAIN_TO_EXIT = 6
    private val WELCOME_BACK = 7
    var doubleBackToExitPressedOnce = false

    private var appUpdateManager: AppUpdateManager? = null

    private var sensorManager: SensorManager? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        Bmob.initialize(CoCoinApplication.getAppContext(), CoCoin.APPLICATION_ID)

        //        CrashReport.initCrashReport(CoCoinApplication.getAppContext(), "900016815", false);

        RecordManager.getInstance(CoCoinApplication.getAppContext())
//        coCoinUtil.init(CoCoinApplication.getAppContext())
        appUpdateManager = AppUpdateManager(this)
        appUpdateManager!!.checkUpdateInfo(false)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        superToast = SuperToast(this)
        superActivityToast = SuperActivityToast(this, SuperToast.Type.PROGRESS_HORIZONTAL)
        val currentapiVersion = Build.VERSION.SDK_INT
        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
            val window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
        } else {
            // do something for phones running an SDK before lollipop
        }
        val user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
        if (user != null) {
            SettingManager.getInstance().loggenOn = true
            SettingManager.getInstance().userName = user.username
            SettingManager.getInstance().userEmail = user.email
            showToast(WELCOME_BACK)
        } else {
            SettingManager.getInstance().loggenOn = false
        }

        editAdapter = EditMoneyRemarkFragmentAdapter(supportFragmentManager,
            CoCoinFragmentManager.MAIN_ACTIVITY_FRAGMENT)
        binding.editPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                if (position == 1) {
                    if (CoCoinFragmentManager.mainActivityEditRemarkFragment != null) CoCoinFragmentManager.mainActivityEditRemarkFragment.editRequestFocus()
                } else {
                    if (CoCoinFragmentManager.mainActivityEditMoneyFragment != null) CoCoinFragmentManager.mainActivityEditMoneyFragment.editRequestFocus()
                }
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.editPager.adapter = editAdapter

        // Button grid view
        val myGridView = binding.gridview
        myGridViewAdapter = ButtonGridViewAdapter(this, coCoinUtil)
        myGridView.adapter = myGridViewAdapter
        myGridView.onItemClickListener = gridViewClickListener
        myGridView.onItemLongClickListener = gridViewLongClickListener
        myGridView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    myGridView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    val lastChild = myGridView.getChildAt(myGridView.childCount - 1)
                    myGridView.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT, lastChild.bottom)
                }
            })

        if (SettingManager.getInstance().firstTime) {
            val intent = Intent(this, ShowActivity::class.java)
            startActivity(intent)
        }

        if (SettingManager.getInstance().showMainActivityGuide) {
            val wrapInScrollView = true
            MaterialDialog.Builder(this)
                .title(R.string.guide)
                .typeface(coCoinUtil.getTypeface(), coCoinUtil.getTypeface())
                .customView(R.layout.main_activity_guide, wrapInScrollView)
                .positiveText(R.string.ok)
                .show()
            SettingManager.getInstance().showMainActivityGuide = false
        }
    }

    private val gridViewLongClickListener = OnItemLongClickListener { parent, view, position, id ->
        if (!isLoading) {
            buttonClickOperation(true, position)
        }
        true
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        Timber.d("requestCode: $requestCode, resultCode: $resultCode, data: $data")
//
//        when (requestCode) {
//            PIN_TAG -> if (resultCode == RESULT_OK) {
//                Timber.e("PIN_TAG is not enabled")
//                if (data?.getBooleanExtra(LOGIN_SUCCESSFUL, false) == true) {
////                    val intent = Intent(this, AccountBookTodayViewActivity::class.java)
////                    startActivityForResult(intent, SETTING_TAG)
//                }
//            }
//            SETTING_TAG -> if (resultCode == RESULT_OK) {
//                if (data!!.getBooleanExtra("IS_CHANGED", false)) {
//                    var i = 0
//                    while (i < tagAdapter!!.count && i < CoCoinFragmentManager.tagChooseFragments.size) {
//                        if (CoCoinFragmentManager.tagChooseFragments[i] != null) CoCoinFragmentManager.tagChooseFragments[i].updateTags()
//                        i++
//                    }
//                }
//            }
//            else -> {}
//        }
//    }

    private val gridViewClickListener =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            if (!isLoading) {
                buttonClickOperation(false, position)
            }
        }

    private fun buttonClickOperation(longClick: Boolean, position: Int) {
        if (binding.editPager.currentItem == 1) return
        if (CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString() == "0" && !coCoinUtil.clickButtonCommit(
                position)
        ) {
            if (coCoinUtil.clickButtonDelete(position)
                || coCoinUtil.clickButtonIsZero(position)
            ) {
            } else {
                CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText =
                    coCoinUtil.BUTTONS[position]
            }
        } else {
            if (coCoinUtil.clickButtonDelete(position)) {
                if (longClick) {
                    CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText = "0"
                    CoCoinFragmentManager.mainActivityEditMoneyFragment.helpText =
                        coCoinUtil.FLOATINGLABELS[CoCoinFragmentManager.mainActivityEditMoneyFragment
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
            } else if (coCoinUtil.clickButtonCommit(position)) {
                commit()
            } else {
                CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText = (
                        CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString()
                                + coCoinUtil.BUTTONS[position])
            }
        }
        CoCoinFragmentManager.mainActivityEditMoneyFragment.helpText =
            coCoinUtil.FLOATINGLABELS[CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString().length]
    }

    private fun commit() {
        if (CoCoinFragmentManager.mainActivityEditMoneyFragment.tagId == -1) {
            showToast(NO_TAG_TOAST)
        } else if (CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString() == "0") {
            showToast(NO_MONEY_TOAST)
        } else {
            val calendar = Calendar.getInstance()
            val coCoinRecord = CoCoinRecord(
                -1,
                java.lang.Float.valueOf(CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText.toString()),
                Constants.USD,
                CoCoinFragmentManager.mainActivityEditMoneyFragment.tagId,
                calendar)
            coCoinRecord.remark = CoCoinFragmentManager.mainActivityEditRemarkFragment.remark
            val saveId = RecordManager.saveRecord(coCoinRecord)
            if (saveId == -1L) {
            } else {
                if (!superToast!!.isShowing) {
                    changeColor()
                }
                CoCoinFragmentManager.mainActivityEditMoneyFragment.setTagImage(R.color.transparent)
                CoCoinFragmentManager.mainActivityEditMoneyFragment.setTagName("")
            }
            CoCoinFragmentManager.mainActivityEditMoneyFragment.numberText = "0"
            CoCoinFragmentManager.mainActivityEditMoneyFragment.helpText = " "
        }
    }

//    private fun tagAnimation() {
//        YoYo.with(Techniques.Shake).duration(1000).playOn(binding.editPager.tag)
//    }

    private fun showToast(toastType: Int) {
        Timber.d("showToast: %d", toastType)
        when (toastType) {
            NO_TAG_TOAST -> {
                ToastUtil.showToast(
                    activity = this,
                    textId = R.string.toast_no_tag,
                    color = SuperToast.Background.RED)
//                tagAnimation()
            }
            NO_MONEY_TOAST -> ToastUtil.showToast(
                activity = this,
                textId = R.string.toast_no_amount,
                color = SuperToast.Background.RED)
            PASSWORD_WRONG_TOAST -> ToastUtil.showToast(
                activity = this,
                textId = R.string.toast_password_wrong,
                color = SuperToast.Background.RED)
            PASSWORD_CORRECT_TOAST -> {
                Timber.d("PASSWORD_CORRECT_TOAST start")
                ToastUtil.showToast(
                    activity = this,
                    textId = R.string.toast_password_correct,
                    color = SuperToast.Background.BLUE)
                Timber.d("PASSWORD_CORRECT_TOAST finish")
            }
            SAVE_SUCCESSFULLY_TOAST -> {}
            SAVE_FAILED_TOAST -> {}
            PRESS_AGAIN_TO_EXIT -> ToastUtil.showToast(
                activity = this,
                textId = R.string.toast_press_again_to_exit,
                color = SuperToast.Background.BLUE)
            WELCOME_BACK -> ToastUtil.showToast(
                activity = this,
                text = this.resources.getString(R.string.welcome_back,
                    SettingManager.getInstance().userName),
                color = SuperToast.Background.BLUE)
            else -> {}
        }
    }

    private fun changeColor() {
        val shouldChange = (SettingManager.getInstance().isMonthLimit
                && SettingManager.getInstance().isColorRemind
                && (RecordManager.currentMonthExpense
                >= SettingManager.getInstance().monthWarning))
        val currentapiVersion = Build.VERSION.SDK_INT
        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
            val window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (shouldChange) {
                window.statusBarColor =
                    coCoinUtil.getDeeperColor(SettingManager.getInstance().remindColor)
            } else {
                window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
            }
        } else {
            // do something for phones running an SDK before lollipop
        }
        if (shouldChange) {
            binding.root.setBackgroundColor(SettingManager.getInstance().remindColor)
            binding.toolbar.setBackgroundColor(SettingManager.getInstance().remindColor)
        } else {
            binding.root.setBackgroundColor(coCoinUtil.MY_BLUE)
            binding.toolbar.setBackgroundColor(coCoinUtil.MY_BLUE)
        }
        if (CoCoinFragmentManager.mainActivityEditMoneyFragment != null) CoCoinFragmentManager.mainActivityEditMoneyFragment.setEditColor(
            shouldChange)
        if (CoCoinFragmentManager.mainActivityEditRemarkFragment != null) CoCoinFragmentManager.mainActivityEditRemarkFragment.setEditColor(
            shouldChange)
        myGridViewAdapter!!.notifyDataSetInvalidated()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            SuperToast.cancelAllSuperToasts()
            return
        }
        showToast(PRESS_AGAIN_TO_EXIT)
        doubleBackToExitPressedOnce = true
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    public override fun onResume() {
        super.onResume()


        // if the tags' order has been changed
//        if (SettingManager.getInstance().mainActivityTagShouldChange) {
//            // change the tag fragment
//            var i = 0
//            while (i < tagAdapter!!.count && i < CoCoinFragmentManager.tagChooseFragments.size) {
//                if (CoCoinFragmentManager.tagChooseFragments[i] != null) CoCoinFragmentManager.tagChooseFragments[i].updateTags()
//                i++
//            }
//            // and tell others that main activity has changed
//            SettingManager.getInstance().mainActivityTagShouldChange = false
//        }

        // if the title should be changed
//        if (SettingManager.getInstance().mainViewTitleShouldChange) {
//            menuToolBarTitle!!.text = SettingManager.getInstance().accountBookName
//            SettingManager.getInstance().mainViewTitleShouldChange = false
//        }
        changeColor()
        isLoading = false
        inputPassword = ""
        System.gc()
    }
}