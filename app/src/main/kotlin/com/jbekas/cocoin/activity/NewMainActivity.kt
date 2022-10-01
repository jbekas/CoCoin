package com.jbekas.cocoin.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobUser
import com.google.android.material.navigation.NavigationBarView
import com.jbekas.cocoin.R
import com.jbekas.cocoin.databinding.ActivityNewMainBinding
import com.jbekas.cocoin.model.CoCoin
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.model.User
import com.jbekas.cocoin.service.ToastService
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NewMainActivity : AppCompatActivity() {

    @Inject
    lateinit var toastService: ToastService

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNewMainBinding

    companion object {
        val PASSWORD_WRONG_TOAST = 2
        val PASSWORD_CORRECT_TOAST = 3
        val SAVE_SUCCESSFULLY_TOAST = 4
        val SAVE_FAILED_TOAST = 5
        val PRESS_AGAIN_TO_EXIT = 6
        val WELCOME_BACK = 7
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
                R.id.action_FirstFragment_to_ReportsFragment -> {
                    Timber.d(" NavigationBarView.OnItemSelectedListener")
                    findNavController(R.id.bottom_navigation).navigate(R.id.action_FirstFragment_to_ReportsFragment)
                    true
                }
                R.string.settings_fragment_label -> {
                    Timber.d(" NavigationBarView.OnItemSelectedListener")
                    findNavController(R.id.bottom_navigation).navigate(R.id.action_FirstFragment_to_ReportsFragment)
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
        } else {
            SettingManager.getInstance().loggenOn = false
        }

        binding.bottomNavigation.apply {
            setOnNavigationItemSelectedListener { item ->
                Timber.d("item: %s", item.toString())
                val navController = findNavController(R.id.nav_host_fragment_content_new_main)

                when (item.itemId) {
                    R.id.page_1 -> {
                        navController.popBackStack(R.id.AddEditRecordFragment, false)
                        true
                    }
                    R.id.reports_view -> {
                        navController.navigate(R.id.ReportsFragment)
                        true
                    }
                    R.id.settings_view -> {
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

    fun showToast(toastType: Int) {
        Timber.d("showToast: %d", toastType)
        when (toastType) {
            PASSWORD_WRONG_TOAST -> {
                toastService.showErrorToast(
                    text = getString(R.string.toast_password_wrong)
                )
            }
            PASSWORD_CORRECT_TOAST -> {
                Timber.d("PASSWORD_CORRECT_TOAST start")
                toastService.showErrorToast(
                    text = getString(R.string.toast_password_correct)
                )
                Timber.d("PASSWORD_CORRECT_TOAST finish")
            }
            SAVE_SUCCESSFULLY_TOAST -> {}
            SAVE_FAILED_TOAST -> {}
            PRESS_AGAIN_TO_EXIT -> {
                toastService.showInfoToast(
                    text = getString(R.string.toast_press_again_to_exit,)
                )
            }
            WELCOME_BACK -> {
                toastService.showInfoToast(
                    text = getString(R.string.welcome_back, SettingManager.getInstance().userName)
                )
            }
            else -> {}
        }
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Timber.d("onOptionsItemSelected")
//        return item.onNavDestinationSelected(findNavController(R.id.bottom_navigation))
//                || super.onOptionsItemSelected(item)
//    }
}