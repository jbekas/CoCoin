package com.jbekas.cocoin

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobUser
import com.github.johnpersano.supertoasts.SuperToast
import com.google.android.material.navigation.NavigationBarView
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.databinding.ActivityNewMainBinding
import com.jbekas.cocoin.fragment.CoCoinFragmentManager
import com.jbekas.cocoin.model.CoCoin
import com.jbekas.cocoin.model.RecordManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.model.User
import com.jbekas.cocoin.util.ToastUtil
import timber.log.Timber

class NewMainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNewMainBinding

    companion object {
        public val NO_TAG_TOAST = 0
        public val NO_MONEY_TOAST = 1
        public val PASSWORD_WRONG_TOAST = 2
        public val PASSWORD_CORRECT_TOAST = 3
        public val SAVE_SUCCESSFULLY_TOAST = 4
        public val SAVE_FAILED_TOAST = 5
        public val PRESS_AGAIN_TO_EXIT = 6
        public val WELCOME_BACK = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_new_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        NavigationBarView.OnItemSelectedListener { item ->
            Timber.d("OnItemSelected, itemId: %d", item.itemId)

            when (item.itemId) {
                R.id.action_FirstFragment_to_SecondFragment -> {
                    Timber.d(" NavigationBarView.OnItemSelectedListener")
                    findNavController(R.id.bottom_navigation).navigate(R.id.action_FirstFragment_to_SecondFragment)
                    true
                }
                R.string.settings_fragment_label -> {
                    Timber.d(" NavigationBarView.OnItemSelectedListener")
                    findNavController(R.id.bottom_navigation).navigate(R.id.action_FirstFragment_to_SecondFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.bottomNavigation.setOnClickListener(View.OnClickListener {
            Timber.d("binding.bottomNavigation.setOnClickListener")
        })
//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }

        Bmob.initialize(CoCoinApplication.getAppContext(), CoCoin.APPLICATION_ID)
        //RecordManager.getInstance(CoCoinApplication.getAppContext())

        val user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
        if (user != null) {
            SettingManager.getInstance().loggenOn = true
            SettingManager.getInstance().userName = user.username
            SettingManager.getInstance().userEmail = user.email
            showToast(WELCOME_BACK)
            // 允许用户使用应用
        } else {
            SettingManager.getInstance().loggenOn = false
            //缓存用户对象为空时， 可打开用户注册界面…
        }

        binding.bottomNavigation.apply {
            setOnNavigationItemSelectedListener { item ->
                Timber.d("item: %s", item.toString())
                val navController = findNavController(R.id.nav_host_fragment_content_new_main)

                when (item.itemId) {
                    R.id.page_1 -> {
                        navController.popBackStack(R.id.FirstFragment, false)
                        true
                    }
                    R.id.page_2 -> {
                        navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                        true
                    }
                    R.id.page_3 -> {
                        navController.navigate(R.id.SettingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_new_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    public fun showToast(toastType: Int) {
        Timber.d("showToast: %d", toastType)
        when (toastType) {
            NO_TAG_TOAST -> {
                ToastUtil.showToast(
                    context = this,
                    textId = R.string.toast_no_tag,
                    textColor = null,
                    color = SuperToast.Background.RED)
//                tagAnimation()
            }
            NO_MONEY_TOAST -> ToastUtil.showToast(
                context = this,
                textId = R.string.toast_no_money,
                textColor = null,
                color = SuperToast.Background.RED)
            PASSWORD_WRONG_TOAST -> ToastUtil.showToast(
                context = this,
                textId = R.string.toast_password_wrong,
                textColor = null,
                color = SuperToast.Background.RED)
            PASSWORD_CORRECT_TOAST -> {
                Timber.d("PASSWORD_CORRECT_TOAST start")
                ToastUtil.showToast(
                    context = this,
                    textId = R.string.toast_password_correct,
                    textColor = null,
                    color = SuperToast.Background.BLUE)
                Timber.d("PASSWORD_CORRECT_TOAST finish")
            }
            SAVE_SUCCESSFULLY_TOAST -> {}
            SAVE_FAILED_TOAST -> {}
            PRESS_AGAIN_TO_EXIT -> ToastUtil.showToast(
                context = this,
                textId = R.string.toast_press_again_to_exit,
                textColor = null,
                color = SuperToast.Background.BLUE)
            WELCOME_BACK -> ToastUtil.showToast(
                context = this,
                text = this.resources.getString(R.string.welcome_back, SettingManager.getInstance().userName),
                textColor = null,
                color = SuperToast.Background.BLUE)
            else -> {}
        }
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Timber.d("onOptionsItemSelected")
//        return item.onNavDestinationSelected(findNavController(R.id.bottom_navigation))
//                || super.onOptionsItemSelected(item)
//    }
}