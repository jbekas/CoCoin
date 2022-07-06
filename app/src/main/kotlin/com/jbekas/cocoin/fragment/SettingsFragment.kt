package com.jbekas.cocoin.fragment

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.listener.DeleteListener
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import cn.bmob.v3.listener.UploadFileListener
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.StackingBehavior
import com.afollestad.materialdialogs.Theme
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.afollestad.materialdialogs.color.ColorChooserDialog.ColorCallback
import com.github.johnpersano.supertoasts.SuperActivityToast
import com.github.johnpersano.supertoasts.SuperToast
import com.google.android.material.button.MaterialButton
import com.jbekas.cocoin.BuildConfig
import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.activity.TagSettingActivity
import com.jbekas.cocoin.databinding.FragmentSettingsBinding
import com.jbekas.cocoin.model.AppUpdateManager
import com.jbekas.cocoin.model.Logo
import com.jbekas.cocoin.model.RecordManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.model.User
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.EmailValidator
import com.jbekas.cocoin.util.ToastUtil
import com.jbekas.cocoin.util.ToastUtil.showToast
import com.koushikdutta.ion.Ion
import com.rengwuxian.materialedittext.MaterialEditText
import com.rey.material.widget.Switch
import net.steamcrafted.materialiconlib.MaterialIconView
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class SettingsFragment : Fragment(), View.OnClickListener, ColorCallback,
    Switch.OnCheckedChangeListener {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val UPDATE_LOGO = 0
    private val UPDATE_IS_MONTH_LIMIT = 1
    private val UPDATE_MONTH_LIMIT = 2
    private val UPDATE_IS_COLOR_REMIND = 3
    private val UPDATE_MONTH_WARNING = 4
    private val UPDATE_REMIND_COLOR = 5
    private val UPDATE_IS_FORBIDDEN = 6
    private val UPDATE_ACCOUNT_BOOK_NAME = 7
    private val UPDATE_ACCOUNT_BOOK_PASSWORD = 8
    private val UPDATE_SHOW_PICTURE = 9
    private val UPDATE_IS_HOLLOW = 10
    private val UPDATE_LOGO_ID = 11
    private var logoBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val currentapiVersion = Build.VERSION.SDK_INT
        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
            Timber.w("Window tweaks disabled during refactoring.")
//            val window = this.window
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.statusBarColor = ContextCompat.getColor(mContext!!, R.color.statusBarColor)
        } else {
            // do something for phones running an SDK before lollipop
//            val statusBarView = findViewById<View>(R.id.status_bar_view)
            binding.statusBarView.layoutParams.height = CoCoinUtil.getStatusBarHeight()
        }
        init()

        return binding.root
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        onBackPressed()
//        return super.onOptionsItemSelected(item)
//    }

//    fun onBackPressed() {
//        SuperToast.cancelAllSuperToasts()
//        super.onBackPressed()
//    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.profile_image -> changeLogo()
        }
    }

    // switch change listener///////////////////////////////////////////////////////////////////////////
    override fun onCheckedChanged(view: Switch, isChecked: Boolean) {
        when (view.id) {
            R.id.month_limit_enable_button -> {
                SettingManager.getInstance().isMonthLimit = isChecked
                updateSettingsToServer(UPDATE_IS_MONTH_LIMIT)
                SettingManager.getInstance().mainViewMonthExpenseShouldChange = true
                SettingManager.getInstance().mainViewRemindColorShouldChange = true
                SettingManager.getInstance().todayViewMonthExpenseShouldChange = true
                setMonthState()
            }
            R.id.month_color_remind_button -> {
                SettingManager.getInstance().isColorRemind = isChecked
                updateSettingsToServer(UPDATE_IS_COLOR_REMIND)
                SettingManager.getInstance().mainViewRemindColorShouldChange = true
                setIconEnable(binding.monthColorIcon, isChecked
                        && SettingManager.getInstance().isMonthLimit)
                setIconEnable(binding.monthColorTypeIcon, isChecked
                        && SettingManager.getInstance().isMonthLimit)
                setIconEnable(binding.warningExpenseIcon, isChecked
                        && SettingManager.getInstance().isMonthLimit)
                if (isChecked && SettingManager.getInstance().isMonthLimit) {
                    binding.monthColorType.isEnabled = true
                    binding.monthColorType.setColorFilter(SettingManager.getInstance().remindColor, android.graphics.PorterDuff.Mode.SRC_IN)
                    binding.warningExpense.isEnabled = true
                    binding.warningExpense.setTextColor(ContextCompat.getColor(activity!!, R.color.drawer_text))
                } else {
                    binding.monthColorType.isEnabled = false
                    binding.monthColorType.setColorFilter(R.color.my_gray, android.graphics.PorterDuff.Mode.SRC_IN)
                    binding.warningExpense.isEnabled = false
                    binding.warningExpense.setTextColor(ContextCompat.getColor(activity!!, R.color.my_gray))
                }
                setTVEnable(binding.monthColorTypeText, isChecked
                        && SettingManager.getInstance().isMonthLimit)
                setTVEnable(binding.warningExpenseText, isChecked
                        && SettingManager.getInstance().isMonthLimit)
            }
            R.id.month_forbidden_button -> {
                SettingManager.getInstance().isForbidden = isChecked
                updateSettingsToServer(UPDATE_IS_FORBIDDEN)
                setIconEnable(binding.monthForbiddenIcon, isChecked
                        && SettingManager.getInstance().isMonthLimit)
            }
            R.id.whether_show_picture_button -> {
                SettingManager.getInstance().showPicture = isChecked
                updateSettingsToServer(UPDATE_SHOW_PICTURE)
                setShowPictureState(isChecked)
            }
            R.id.whether_show_circle_button -> {
                SettingManager.getInstance().isHollow = isChecked
                updateSettingsToServer(UPDATE_IS_HOLLOW)
                setHollowState(isChecked)
                SettingManager.getInstance().todayViewPieShouldChange = java.lang.Boolean.TRUE
            }
            else -> {}
        }
    }

    // Load logo from local/////////////////////////////////////////////////////////////////////////////
    private fun loadLogo() {
        val user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
        if (user != null) {
            try {
                val logoFile =
                    File(CoCoinApplication.getAppContext().filesDir.toString() + CoCoinUtil.LOGO_NAME)
                if (!logoFile.exists()) {
                    // the local logo file is missed
                    // try to get from the server
                    val bmobQuery = BmobQuery<Logo>()
                    bmobQuery.addWhereEqualTo("objectId", user.logoObjectId)
                    bmobQuery.findObjects(CoCoinApplication.getAppContext(),
                        object : FindListener<Logo>() {
                            override fun onSuccess(`object`: List<Logo>) {
                                // there has been an old logo in the server/////////////////////////////////////////////////////////
                                val url =
                                    `object`[0].file.getFileUrl(CoCoinApplication.getAppContext())
                                if (BuildConfig.DEBUG) Log.d("CoCoin", "Logo in server: $url")
                                Ion.with(CoCoinApplication.getAppContext()).load(url)
                                    .write(File(CoCoinApplication.getAppContext().filesDir
                                        .toString() + CoCoinUtil.LOGO_NAME))
                                    .setCallback { e, file ->
                                        binding.profileImage.setImageBitmap(BitmapFactory.decodeFile(
                                            CoCoinApplication.getAppContext().filesDir
                                                .toString() + CoCoinUtil.LOGO_NAME))
                                    }
                            }

                            override fun onError(code: Int, msg: String) {
                                // the picture is lost
                                if (BuildConfig.DEBUG) Log.d("CoCoin",
                                    "Can't find the old logo in server.")
                            }
                        })
                } else {
                    // the user logo is in the storage
                    val b = BitmapFactory.decodeStream(FileInputStream(logoFile))
                    binding.profileImage.setImageBitmap(b)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            // use the default logo
            binding.profileImage.setImageResource(R.drawable.default_user_logo)
        }
    }

    // change the user logo/////////////////////////////////////////////////////////////////////////////
    private fun changeLogo() {
        val user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
        if (user == null) {
            MaterialDialog.Builder(activity!!)
                .iconRes(R.drawable.cocoin_logo)
                .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
                .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                .title(R.string.login_first_title)
                .content(R.string.login_first_content)
                .positiveText(R.string.ok)
                .neutralText(R.string.cancel)
                .onAny { dialog, which ->
                    if (which == DialogAction.POSITIVE) {
                        userOperator()
                    }
                }
                .show()
            return
        }
        MaterialDialog.Builder(activity!!)
            .iconRes(R.drawable.cocoin_logo)
            .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
            .limitIconToDefaultSize() // limits the displayed icon size to 48dp
            .title(R.string.change_logo_title)
            .content(R.string.change_logo_content)
            .positiveText(R.string.from_gallery)
            .negativeText(R.string.from_camera)
            .neutralText(R.string.cancel)
            .onAny { dialog, which ->
                if (which == DialogAction.POSITIVE) {
                    val intent1 = Intent(Intent.ACTION_PICK, null)
                    intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                    startActivityForResult(intent1, 1)
                } else if (which == DialogAction.NEGATIVE) {
                    val intent2 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(File(CoCoinApplication.getAppContext().filesDir
                            .toString() + CoCoinUtil.LOGO_NAME)))
                    startActivityForResult(intent2, 2)
                }
            }
            .show()
    }

    // Crop a picture///////////////////////////////////////////////////////////////////////////////////
    fun cropPhoto(uri: Uri?) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true")
        // aspectX : aspectY
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        // outputX outputY the height and width
        intent.putExtra("outputX", 200)
        intent.putExtra("outputY", 200)
        intent.putExtra("return-data", true)
        startActivityForResult(intent, 3)
    }

    // After select a picture///////////////////////////////////////////////////////////////////////////
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult, requestCode: %d, resultCode: %d, data: %s", requestCode, resultCode, data)
        when (requestCode) {
            1 ->                 // after select from gallery
                if (resultCode == Activity.RESULT_OK) { // TODO Does this still work?
                    cropPhoto(data!!.data)
                }
            2 ->                 // after taking a photo
                if (resultCode == Activity.RESULT_OK) { // TODO Does this still work?
                    val temp =
                        File(CoCoinApplication.getAppContext().filesDir.toString() + CoCoinUtil.LOGO_NAME)
                    cropPhoto(Uri.fromFile(temp))
                }
            3 ->                 // after crop the picture
                if (data != null) {
                    val extras = data.extras
                    logoBitmap = extras!!.getParcelable("data")
                    if (logoBitmap != null) {
                        SettingManager.getInstance().hasLogo = true
                        setPicToView(logoBitmap!!)
                        SettingManager.getInstance().todayViewLogoShouldChange = true
                        binding.profileImage.setImageBitmap(logoBitmap)
                    }
                }
            else -> {}
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // Storage a picture////////////////////////////////////////////////////////////////////////////////
    private fun setPicToView(mBitmap: Bitmap) {
        var b: FileOutputStream? = null
        val file =
            File(CoCoinApplication.getAppContext().filesDir.toString() + CoCoinUtil.LOGO_NAME)
        val fileName = file.absolutePath // get logo position
        try {
            b = FileOutputStream(fileName)
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b) // write the data to file
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                // close
                b!!.flush()
                b.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            uploadLogoToServer()
        }
    }

    // download logo to local///////////////////////////////////////////////////////////////////////////
    private fun downloadLogoFromServer() {
        val user = currentUser
        assert(user != null)
        if (user.logoObjectId == null) {
            // the user has no logo
            return
        }
        val bmobQuery = BmobQuery<Logo>()
        bmobQuery.addWhereEqualTo("objectId", user.logoObjectId)
        bmobQuery.findObjects(CoCoinApplication.getAppContext(), object : FindListener<Logo>() {
            override fun onSuccess(`object`: List<Logo>) {
// there has been an old logo in the server/////////////////////////////////////////////////////////
                Log.d("Saver", "There is an old logo")
                val url = `object`[0].file.url
                Ion.with(CoCoinApplication.getAppContext()).load(url)
                    .write(File(CoCoinApplication.getAppContext().filesDir
                        .toString() + CoCoinUtil.LOGO_NAME))
                    .setCallback { e, file ->
                        val bitmap =
                            BitmapFactory.decodeFile(CoCoinApplication.getAppContext().filesDir
                                .toString() + CoCoinUtil.LOGO_NAME)
                        if (bitmap == null) {
                            Log.d("Saver", "Logo misses")
                        } else {
                            binding.profileImage.setImageBitmap(bitmap)
                        }
                        SettingManager.getInstance().hasLogo = true
                    }
                SettingManager.getInstance().todayViewLogoShouldChange = true
            }

            override fun onError(code: Int, msg: String) {
                // the picture is lost
                Log.d("Saver", "Can't find the old logo in server.")
            }
        })
    }

    // update a logo to server//////////////////////////////////////////////////////////////////////////
    private fun uploadLogoToServer() {
        if (!SettingManager.getInstance().hasLogo) {
            // the user haven't set the logo
            return
        }
        val file =
            File(CoCoinApplication.getAppContext().filesDir.toString() + CoCoinUtil.LOGO_NAME)
        val user = currentUser
        // if login/////////////////////////////////////////////////////////////////////////////////////////
        if (user != null) {
            if (user.logoObjectId !== "") {
// if the logo id is not null, then there must be a logo and a logo file in the server//////////////
// judge whether there is an old logo of the same user//////////////////////////////////////////////
                val bmobQuery = BmobQuery<Logo>()
                bmobQuery.addWhereEqualTo("objectId", user.logoObjectId)
                bmobQuery.findObjects(CoCoinApplication.getAppContext(),
                    object : FindListener<Logo>() {
                        override fun onSuccess(`object`: List<Logo>) {
// there has been an old logo in the server/////////////////////////////////////////////////////////
// then there must be an old logo file in server////////////////////////////////////////////////////
// then we should delete the old one////////////////////////////////////////////////////////////////
                            Log.d("Saver", "There is an old logo")
                            val url = `object`[0].file.url
                            val oldLogoFile = BmobFile()
                            oldLogoFile.url = url
                            oldLogoFile.delete(CoCoinApplication.getAppContext(),
                                object : DeleteListener() {
                                    override fun onSuccess() {
                                        Log.d("Saver", "Successfully delete the old logo.")
                                        // after delete, we should upload a new logo file///////////////////////////////////////////////////
                                        val newLogoFile = BmobFile(file)
                                        newLogoFile.uploadblock(CoCoinApplication.getAppContext(),
                                            object : UploadFileListener() {
                                                override fun onSuccess() {
// after upload the new logo file, we should put the new logo the Logo table////////////////////////
                                                    val newLogo = Logo(newLogoFile)
                                                    newLogo.update(CoCoinApplication.getAppContext(),
                                                        user.logoObjectId,
                                                        object : UpdateListener() {
                                                            override fun onSuccess() {
                                                                Log.d("Saver",
                                                                    "Update logo successfully")
                                                            }

                                                            override fun onFailure(
                                                                arg0: Int,
                                                                arg1: String
                                                            ) {
                                                                Log.d("Saver",
                                                                    "Update logo failed $arg1")
                                                            }
                                                        })
                                                }

                                                override fun onProgress(arg0: Int) {}
                                                override fun onFailure(arg0: Int, arg1: String) {
                                                    Log.d("Saver", "Upload failed $arg1")
                                                }
                                            })
                                    }

                                    override fun onFailure(code: Int, msg: String) {
                                        Log.d("Saver", "Fail to delete the old logo. $msg")
                                    }
                                })
                        }

                        override fun onError(code: Int, msg: String) {
                            // the picture is lost
                            Log.d("Saver", "Can't find the old logo in server.")
                        }
                    })
            } else {
// the user has no logo before//////////////////////////////////////////////////////////////////////
                val newLogoFile = BmobFile(file)
                newLogoFile.uploadblock(CoCoinApplication.getAppContext(),
                    object : UploadFileListener() {
                        override fun onSuccess() {
                            val url = newLogoFile.getFileUrl(CoCoinApplication.getAppContext())
                            Log.d("Saver", "Upload successfully $url")
                            val newLogo = Logo(newLogoFile)
                            newLogo.save(CoCoinApplication.getAppContext(),
                                object : SaveListener() {
                                    override fun onSuccess() {
                                        Log.d("Saver", "Save the new logo successfully.")
                                        SettingManager.getInstance().logoObjectId = newLogo.objectId
                                        updateSettingsToServer(UPDATE_LOGO_ID)
                                    }

                                    override fun onFailure(i: Int, s: String) {
                                        Log.d("Saver", "Save the new logo fail.")
                                    }
                                })
                        }

                        override fun onProgress(arg0: Int) {}
                        override fun onFailure(arg0: Int, arg1: String) {
                            Log.d("Saver", "Upload failed $arg1")
                        }
                    })
            }
        }
    }

    // the user's operation when clicking the first card view///////////////////////////////////////////
    private fun userOperator() {
        if (!SettingManager.getInstance().loggenOn) {
            // register or log on
            MaterialDialog.Builder(activity!!)
                .iconRes(R.drawable.cocoin_logo)
                .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
                .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                .title(R.string.welcome)
                .content(R.string.login_or_register)
                .positiveText(R.string.login)
                .negativeText(R.string.register)
                .neutralText(R.string.cancel)
                .onAny { dialog, which ->
                    if (which == DialogAction.POSITIVE) {
                        userLogin()
                    } else if (which == DialogAction.NEGATIVE) {
                        userRegister()
                    } else {
                        dialog.dismiss()
                    }
                }
                .show()
        } else {
            // log out or user operate
            MaterialDialog.Builder(activity!!)
                .iconRes(R.drawable.cocoin_logo)
                .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
                .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                .title(activity!!.resources.getString(R.string.hi)
                        + SettingManager.getInstance().userName)
                .content(R.string.whether_logout)
                .positiveText(R.string.log_out)
                .neutralText(R.string.cancel)
                .onAny { dialog, which ->
                    if (which == DialogAction.POSITIVE) {
                        userLogout()
                    } else {
                        dialog.dismiss()
                    }
                }
                .show()
        }
    }

    // User log out/////////////////////////////////////////////////////////////////////////////////////
    private fun userLogout() {
        BmobUser.logOut(CoCoinApplication.getAppContext())
        SettingManager.getInstance().todayViewInfoShouldChange = true
        SettingManager.getInstance().loggenOn = false
        SettingManager.getInstance().userName = null
        SettingManager.getInstance().userEmail = null
        binding.profileImage.setImageResource(R.drawable.default_user_logo)
        updateViews()
        showToast(8, "")
    }

    // User login///////////////////////////////////////////////////////////////////////////////////////
    var loginDialog: MaterialDialog? = null
    var loginDialogView: View? = null
    var loginDialogButton: MaterialButton? = null
    private fun userLogin() {
        loginDialog = MaterialDialog.Builder(activity!!)
            .title(R.string.go_login)
            .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
            .customView(R.layout.dialog_user_login, true)
            .build()
        val imm = activity!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        loginDialogView = loginDialog!!.getCustomView()
        loginDialogButton = loginDialogView!!.findViewById<View>(R.id.button) as MaterialButton
        //        loginDialogButton.isIndeterminateProgressMode();
//        loginDialogButton.setProgress(0);
        loginDialogButton!!.setTypeface(CoCoinUtil.GetTypeface())
        loginDialogButton!!.setOnClickListener {
            loginDialog!!.setCancelable(false)
            //                loginDialogButton.setProgress(1);
// the user ask to login////////////////////////////////////////////////////////////////////////////
            val user = User()

            val loginUserName: MaterialEditText = loginDialog!!.getCustomView()!!
                .findViewById<View>(R.id.login_user_name) as MaterialEditText
            val loginPassword: MaterialEditText = loginDialog!!.getCustomView()!!
                .findViewById<View>(R.id.login_password) as MaterialEditText

            user.username = loginUserName.text.toString()
            user.setPassword(loginPassword.text.toString())
            showToast(activity!!, "Login is currently disabled.", null, null)
            if (loginDialog != null) loginDialog!!.dismiss()
            val imm = activity!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)

            /*user.login(CoCoinApplication.getAppContext(), new SaveListener() {
                        // try with user name///////////////////////////////////////////////////////////////////////////////
                        @Override
                        public void onSuccess() {
                            loginDialog.setCancelable(true);
                            loginDialogButton.setProgress(0);
                            loginDialogButton.setIdleText(getResourceString(R.string.login_complete));
    // login successfully through user name/////////////////////////////////////////////////////////////
                            SettingManager.getInstance().setTodayViewInfoShouldChange(true);
                            User loginUser =
                                    BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User.class);
                            if (!CoCoinApplication.getAndroidId().equals(loginUser.getAndroidId())) {
    // 2 users on one mobile////////////////////////////////////////////////////////////////////////////
                                showToast(7, "unique...");
                                return;
                            }
                            SettingManager.getInstance().setLoggenOn(true);
                            SettingManager.getInstance().setUserName(loginUserName.getText().toString());
                            SettingManager.getInstance().setUserEmail(
                                    loginUser.getEmail());
                            updateViews();
                            // use a new method
    //                        RecordManager.updateOldRecordsToServer();
                            whetherSyncSettingsFromServer();
                            showToast(6, loginUserName.getText().toString());
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (loginDialog != null) loginDialog.dismiss();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS,0);
                                }
                            }, 500);
                        }
                        // login fail through user name/////////////////////////////////////////////////////////////////////
                        @Override
                        public void onFailure(int code, String msg) {
    // try with user email//////////////////////////////////////////////////////////////////////////////
                            user.setEmail(loginUserName.getText().toString());
                            user.login(CoCoinApplication.getAppContext(), new SaveListener() {
                                @Override
                                public void onSuccess() {
                                    loginDialog.setCancelable(true);
                                    loginDialogButton.setProgress(0);
                                    loginDialogButton.setIdleText(getResourceString(R.string.login_complete));
    // login successfully through user email////////////////////////////////////////////////////////////
                                    SettingManager.getInstance().setTodayViewInfoShouldChange(true);
                                    User loginUser =
                                            BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User.class);
                                    if (!CoCoinApplication.getAndroidId().equals(loginUser.getAndroidId())) {
    // 2 users on one mobile////////////////////////////////////////////////////////////////////////////
                                        showToast(7, "unique...");
                                        return;
                                    }
                                    String userName = loginUser.getUsername();
                                    SettingManager.getInstance().setLoggenOn(true);
                                    SettingManager.getInstance().setUserName(userName);
                                    SettingManager.getInstance().setUserEmail(loginUserName.getText().toString());
                                    SettingManager.getInstance().setUserPassword(loginPassword.getText().toString());
                                    updateViews();
                                    // use a new method
    //                                RecordManager.updateOldRecordsToServer();
                                    whetherSyncSettingsFromServer();
                                    showToast(6, userName);
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (loginDialog != null) loginDialog.dismiss();
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS,0);
                                        }
                                    }, 500);
                                }
                                // login fail through user name and email///////////////////////////////////////////////////////////
                                @Override
                                public void onFailure(int code, String msg) {
                                    loginDialog.setCancelable(true);
                                    loginDialogButton.setProgress(0);
                                    String tip = getResourceString(R.string.network_disconnection);
                                    if (msg.charAt(0) == 'u') tip = getResourceString(R.string.user_name_or_password_incorrect);
                                    if (msg.charAt(1) == 'n') tip = getResourceString(R.string.user_mobile_exist);
                                    loginDialogButton.setIdleText(tip);
                                }
                            });
                        }
                    })*/
        }

        val loginUserName: MaterialEditText = loginDialog!!.getCustomView()!!
            .findViewById<View>(R.id.login_user_name) as MaterialEditText
        val loginPassword: MaterialEditText = loginDialog!!.getCustomView()!!
            .findViewById<View>(R.id.login_password) as MaterialEditText

        val positiveAction = loginDialog!!.getActionButton(DialogAction.POSITIVE)
        positiveAction.isEnabled = false
        val userNameTV =
            loginDialog!!.getCustomView()!!.findViewById<View>(R.id.login_user_name_text) as TextView
        val userPasswordTV =
            loginDialog!!.getCustomView()!!.findViewById<View>(R.id.login_password_text) as TextView
        userNameTV.setTypeface(CoCoinUtil.GetTypeface())
        userPasswordTV.setTypeface(CoCoinUtil.GetTypeface())
        loginUserName.setTypeface(CoCoinUtil.GetTypeface())
        loginUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                loginDialogButton!!.isEnabled = (
                        0 < loginUserName.text.toString().length
                                && 0 < loginPassword.text.toString().length)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        loginPassword.setTypeface(CoCoinUtil.GetTypeface())
        loginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                loginDialogButton!!.isEnabled = (
                        0 < loginUserName.text.toString().length
                                && 0 < loginPassword.text.toString().length)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        loginDialog!!.show()
    }

    // User register////////////////////////////////////////////////////////////////////////////////////
    var registerDialog: MaterialDialog? = null
    var registerDialogView: View? = null
    var registerDialogButton: MaterialButton? = null
    private fun userRegister() {
        registerDialog = MaterialDialog.Builder(activity!!)
            .title(R.string.go_register)
            .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
            .customView(R.layout.dialog_user_register, true)
            .build()
        val imm = activity!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        registerDialogView = registerDialog!!.getCustomView()
        registerDialogButton =
            registerDialogView!!.findViewById<View>(R.id.button) as MaterialButton
        registerDialogButton!!.setTypeface(CoCoinUtil.GetTypeface())
        registerDialogButton!!.setOnClickListener {
            showToast(activity!!, "Register is currently disabled.", null, null)
            if (registerDialog != null) registerDialog!!.dismiss()
            val imm = activity!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)

/*
                registerDialogButton.setEnabled(false);
                //registerDialogButton.setProgress(1);
                registerDialog.setCancelable(false);
// User register, a new user////////////////////////////////////////////////////////////////////////
                final User user = new User();
                // basic info
                user.setUsername(registerUserName.getText().toString());
                user.setPassword(registerPassword.getText().toString());
                user.setEmail(registerUserEmail.getText().toString());
                user.setAndroidId(CoCoinApplication.getAndroidId());
                if (BuildConfig.DEBUG) Log.d("CoCoin", "Android Id: " + user.getAndroidId());
                // settings info
                // user.setLogo();
                user.setIsMonthLimit(SettingManager.getInstance().getIsMonthLimit());
                user.setMonthLimit(SettingManager.getInstance().getMonthLimit());
                user.setIsColorRemind(SettingManager.getInstance().getIsColorRemind());
                user.setMonthWarning(SettingManager.getInstance().getMonthWarning());
                user.setRemindColor(SettingManager.getInstance().getRemindColor());
                user.setIsForbidden(SettingManager.getInstance().getIsForbidden());
                user.setAccountBookName(SettingManager.getInstance().getAccountBookName());
                user.setAccountBookPassword(SettingManager.getInstance().getPassword());
                // Todo store tag order
                user.setShowPicture(SettingManager.getInstance().getShowPicture());
                user.setIsHollow(SettingManager.getInstance().getIsHollow());
                user.setLogoObjectId("");
*/
/*
                user.signUp(CoCoinApplication.getAppContext(), new SaveListener() {
                    @Override
                    public void onSuccess() {
                        registerDialogButton.setEnabled(true);
//                        registerDialogButton.setProgress(0);
                        registerDialog.setCancelable(true);
                        registerDialogButton.setText(R.string.register_complete);
//                        registerDialogButton.setIdleText(getResourceString(R.string.register_complete));
// if register successfully/////////////////////////////////////////////////////////////////////////
                        SettingManager.getInstance().setLoggenOn(true);
                        SettingManager.getInstance().setUserName(registerUserName.getText().toString());
                        SettingManager.getInstance().setUserEmail(registerUserEmail.getText().toString());
                        SettingManager.getInstance().setUserPassword(registerPassword.getText().toString());
                        showToast(4, registerUserName.getText().toString());
// if login successfully////////////////////////////////////////////////////////////////////////////
                        user.login(CoCoinApplication.getAppContext(), new SaveListener() {
                            @Override
                            public void onSuccess() {
                                SettingManager.getInstance().setTodayViewInfoShouldChange(true);
                                updateViews();
                                // use a new method
//                                RecordManager.updateOldRecordsToServer();
                            }
                            @Override
                            public void onFailure(int code, String msg) {
// if login failed//////////////////////////////////////////////////////////////////////////////////
                            }
                        });
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (registerDialog != null) registerDialog.dismiss();
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS,0);
                            }
                        }, 500);
                    }
                    // if register failed///////////////////////////////////////////////////////////////////////////////
                    @Override
                    public void onFailure(int code, String msg) {
                        if (BuildConfig.DEBUG) Log.d("CoCoin", "Register failed: " + msg);
                        String tip = getResourceString(R.string.network_disconnection);
                        if (msg.charAt(1) == 's') tip = getResourceString(R.string.user_name_exist);
                        if (msg.charAt(0) == 'e') tip = getResourceString(R.string.user_email_exist);
                        if (msg.charAt(1) == 'n') tip = getResourceString(R.string.user_mobile_exist);
                        registerDialogButton.setText(tip);
                        registerDialogButton.setEnabled(true);
//                        registerDialogButton.setIdleText(tip);
//                        registerDialogButton.setProgress(0);
                        registerDialog.setCancelable(true);
                    }
                });
*/
        }

        var registerUserName: MaterialEditText?
        var registerUserEmail: MaterialEditText?
        var registerPassword: MaterialEditText?

        val positiveAction = registerDialog!!.getActionButton(DialogAction.POSITIVE)
        positiveAction.isEnabled = false
        val emailValidator = EmailValidator()
        val userNameTV = registerDialog!!.getCustomView()!!
            .findViewById<View>(R.id.register_user_name_text) as TextView
        val userEmailTV = registerDialog!!.getCustomView()!!
            .findViewById<View>(R.id.register_user_email_text) as TextView
        val userPasswordTV = registerDialog!!.getCustomView()!!
            .findViewById<View>(R.id.register_password_text) as TextView
        userNameTV.setTypeface(CoCoinUtil.GetTypeface())
        userEmailTV.setTypeface(CoCoinUtil.GetTypeface())
        userPasswordTV.setTypeface(CoCoinUtil.GetTypeface())
        registerUserName = registerDialog!!.getCustomView()!!
            .findViewById<View>(R.id.register_user_name) as MaterialEditText
        registerUserEmail = registerDialog!!.getCustomView()!!
            .findViewById<View>(R.id.register_user_email) as MaterialEditText
        registerPassword = registerDialog!!.getCustomView()!!
            .findViewById<View>(R.id.register_password) as MaterialEditText
        registerUserName.setTypeface(CoCoinUtil.GetTypeface())
        registerUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val emailOK = emailValidator.validate(registerUserEmail.text.toString())
                registerDialogButton!!.isEnabled =
                    0 < registerUserName.text.toString().length && registerUserName.text.toString().length <= 16 && registerPassword.text.toString().length > 0 && emailOK
                if (emailValidator.validate(registerUserEmail.text.toString())) {
                    registerUserEmail.validate()
                } else {
                    registerUserEmail.invalidate()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        registerUserEmail.setTypeface(CoCoinUtil.GetTypeface())
        registerUserEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val emailOK = emailValidator.validate(registerUserEmail.text.toString())
                registerDialogButton!!.isEnabled =
                    0 < registerUserName.text.toString().length && registerUserName.text.toString().length <= 16 && registerPassword.text.toString().length > 0 && emailOK
                if (emailValidator.validate(registerUserEmail.text.toString())) {
                    registerUserEmail.validate()
                } else {
                    registerUserEmail.invalidate()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        registerPassword.setTypeface(CoCoinUtil.GetTypeface())
        registerPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val emailOK = emailValidator.validate(registerUserEmail.text.toString())
                registerDialogButton!!.isEnabled =
                    0 < registerUserName.text.toString().length && registerUserName.text.toString().length <= 16 && registerPassword.text.toString().length > 0 && emailOK
                if (emailValidator.validate(registerUserEmail.text.toString())) {
                    registerUserEmail.validate()
                } else {
                    registerUserEmail.invalidate()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        registerDialog!!.show()
    }

    // Change account book name/////////////////////////////////////////////////////////////////////////
    private fun changeAccountBookName() {
        MaterialDialog.Builder(activity!!)
            .theme(Theme.LIGHT)
            .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
            .title(R.string.set_account_book_dialog_title)
            .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            .inputRange(1, 16)
            .positiveText(R.string.submit)
            .input(SettingManager.getInstance().accountBookName, null) { dialog, input ->
                // local change
                SettingManager.getInstance().accountBookName = input.toString()
                SettingManager.getInstance().todayViewTitleShouldChange = true
                SettingManager.getInstance().mainViewTitleShouldChange = true
                binding.accountBookName.text = input.toString()
                // update change
                val user = currentUser
                if (user != null) {
                    updateSettingsToServer(UPDATE_ACCOUNT_BOOK_NAME)
                } else {
                    // the new account book name is changed successfully
                    showToast(2, "")
                }
            }.show()
    }

    // Update some views when login/////////////////////////////////////////////////////////////////////
    private fun updateViews() {
        setIconEnable(binding.userNameIcon, SettingManager.getInstance().loggenOn)
        setIconEnable(binding.userEmailIcon, SettingManager.getInstance().loggenOn)
        if (SettingManager.getInstance().loggenOn) {
            binding.userName.text = SettingManager.getInstance().userName
            binding.userEmail.text = SettingManager.getInstance().userEmail
            binding.loginButton.text = activity!!.resources.getText(R.string.logout_button)
            binding.loginButton.setBackgroundResource(R.drawable.button_logout)
        } else {
            binding.userName.text = ""
            binding.userEmail.text = ""
            binding.loginButton.text = activity!!.resources.getText(R.string.login_button)
            binding.loginButton.setBackgroundResource(R.drawable.button_login)
        }
    }

    // Start change account book password activity//////////////////////////////////////////////////////
    // I put the update to server part in the change password activity but not here/////////////////////
    private fun changePassword() {
        Toast.makeText(activity!!, "Disabled", Toast.LENGTH_SHORT).show()
//        val intent = Intent(activity, EditPasswordActivity::class.java)
//        startActivity(intent)
    }

    // Start sort tags activity/////////////////////////////////////////////////////////////////////////
    // I put the update to server part in the sort tag activity but not here////////////////////////////
    private fun sortTags() {
        val intent = Intent(activity, TagSettingActivity::class.java)
        startActivity(intent)
    }

    // Init the setting activity////////////////////////////////////////////////////////////////////////
    private fun init() {
//        back = findViewById<View>(R.id.icon_left) as MaterialIconView
//        back!!.setOnClickListener { finish() }
//        logo = findViewById<View>(R.id.profile_image) as CircleImageView
//        logo!!.setOnClickListener(this)
          binding.userName.typeface = CoCoinUtil.typefaceLatoLight
          binding.userEmail.typeface = CoCoinUtil.typefaceLatoLight
          binding.loginButton.typeface = CoCoinUtil.typefaceLatoLight
          binding.loginButton.setOnClickListener { userOperator() }
          binding.expense.typeface = CoCoinUtil.typefaceLatoLight
          binding.records.typeface = CoCoinUtil.typefaceLatoLight
          binding.expenseText.typeface = CoCoinUtil.GetTypeface()
          binding.recordsText.typeface = CoCoinUtil.GetTypeface()
          binding.expense.withNumber(RecordManager.SUM).setDuration(1500).start()
          binding.records.withNumber(RecordManager.RECORDS.size).setDuration(1500).start()
          binding.monthColorType.setColorFilter(SettingManager.getInstance().remindColor, android.graphics.PorterDuff.Mode.SRC_IN)
          binding.monthLimitEnableButton.setOnCheckedChangeListener(this)
          binding.monthColorRemindButton.setOnCheckedChangeListener(this)
        binding.monthForbiddenButton.setOnCheckedChangeListener(this)
        if (SettingManager.getInstance().isMonthLimit) binding.monthExpense.withNumber(SettingManager.getInstance()
            .monthLimit).setDuration(1000).start()
        // change the month limit///////////////////////////////////////////////////////////////////////////
        binding.monthExpense.setOnClickListener {
            if (SettingManager.getInstance().isMonthLimit) {
                MaterialDialog.Builder(activity!!)
                    .theme(Theme.LIGHT)
                    .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
                    .title(R.string.set_month_expense_dialog_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .positiveText(R.string.submit)
                    .inputRange(3, 5)
                    .input(SettingManager.getInstance().monthLimit.toString(),
                        null) { dialog, input ->
                        var newExpense = SettingManager.getInstance().monthLimit
                        if (input.length != 0) {
                            newExpense = input.toString().toInt()
                        }
                        // the month limit must be smaller than the month warning
                        if (newExpense < SettingManager.getInstance().monthWarning) {
                            SettingManager.getInstance().monthWarning =
                                (newExpense * 0.8).toInt() / 100 * 100
                            if (SettingManager.getInstance().monthWarning < 100) {
                                SettingManager.getInstance().monthWarning = 100
                            }
                            updateSettingsToServer(UPDATE_MONTH_WARNING)
                            SettingManager.getInstance().mainViewRemindColorShouldChange = true
                            binding.warningExpense.text = SettingManager
                                .getInstance().monthWarning.toString()
                        }
                        SettingManager.getInstance().monthLimit = newExpense
                        updateSettingsToServer(UPDATE_MONTH_LIMIT)
                        SettingManager.getInstance().todayViewMonthExpenseShouldChange = true
                        SettingManager.getInstance().mainViewMonthExpenseShouldChange = true
                        binding.monthExpense.withNumber(SettingManager.getInstance()
                            .monthLimit).setDuration(1000).start()
                    }.show()
            }
        }
        binding.warningExpense.text = SettingManager.getInstance().monthWarning.toString()
        if (SettingManager.getInstance().isMonthLimit
            && SettingManager.getInstance().isColorRemind
        ) binding.warningExpense.withNumber(SettingManager.getInstance()
            .monthWarning).setDuration(1000).start()
        // change month warning/////////////////////////////////////////////////////////////////////////////
        binding.warningExpense.setOnClickListener {
            if (SettingManager.getInstance().isMonthLimit
                && SettingManager.getInstance().isColorRemind
            ) {
                MaterialDialog.Builder(activity!!)
                    .theme(Theme.LIGHT)
                    .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
                    .title(R.string.set_month_expense_dialog_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .positiveText(R.string.submit)
                    .alwaysCallInputCallback()
                    .input(null, null) { dialog, input ->
                        if (input.length == 0) {
                            dialog.setContent(activity!!.resources.getString(
                                R.string.set_warning_expense_dialog_title))
                            dialog.getActionButton(DialogAction.POSITIVE).isEnabled = false
                        } else if (input.toString().toInt() < 100) {
                            dialog.setContent("≥ 100")
                            dialog.getActionButton(DialogAction.POSITIVE).isEnabled = false
                        } else if (input.toString()
                                .toInt() > SettingManager.getInstance().monthLimit
                        ) {
                            dialog.setContent("≤ " + SettingManager.getInstance()
                                .monthLimit.toString())
                            dialog.getActionButton(DialogAction.POSITIVE).isEnabled = false
                        } else {
                            dialog.setContent(activity!!.resources.getString(
                                R.string.set_warning_expense_dialog_title))
                            dialog.getActionButton(DialogAction.POSITIVE).isEnabled = true
                        }
                        dialog.getActionButton(DialogAction.POSITIVE)
                            .setOnClickListener {
                                SettingManager.getInstance().monthWarning = input.toString().toInt()
                                updateSettingsToServer(UPDATE_MONTH_WARNING)
                                SettingManager.getInstance().mainViewRemindColorShouldChange = true
                                binding.warningExpense.withNumber(SettingManager.getInstance()
                                    .monthWarning).setDuration(1000).start()
                                dialog.dismiss()
                            }
                    }.show()
            }
        }
        // change month remind color////////////////////////////////////////////////////////////////////////
        binding.monthColorType.setOnClickListener {
            Timber.d("monthColorType clicked.")
            SettingManager.getInstance().mainViewRemindColorShouldChange = true
            getRemindColorSelectDialog().show(childFragmentManager)
        }
        binding.monthExpense.typeface = CoCoinUtil.typefaceLatoLight
        binding.warningExpense.typeface = CoCoinUtil.typefaceLatoLight
        binding.monthLimitText.typeface = CoCoinUtil.GetTypeface()
        binding.warningExpenseText.typeface = CoCoinUtil.GetTypeface()
        binding.monthExpenseText.typeface = CoCoinUtil.GetTypeface()
        binding.monthColorRemindText.typeface = CoCoinUtil.GetTypeface()
        binding.monthColorTypeText.typeface = CoCoinUtil.GetTypeface()
        binding.monthForbiddenText.typeface = CoCoinUtil.GetTypeface()
        binding.accountBookNameLayout.setOnClickListener { changeAccountBookName() }
        binding.accountBookName.typeface = CoCoinUtil.GetTypeface()
        binding.accountBookName.text = SettingManager.getInstance().accountBookName
        binding.accountBookNameText.typeface = CoCoinUtil.GetTypeface()
        binding.changePasswordLayout.setOnClickListener { changePassword() }
        binding.changePasswordText.typeface = CoCoinUtil.GetTypeface()
        binding.sortTagsLayout.setOnClickListener { sortTags() }
        binding.sortTagsText.typeface = CoCoinUtil.GetTypeface()
        binding.whetherShowPictureButton.setOnCheckedChangeListener(this)
        binding.whetherShowPictureText.typeface = CoCoinUtil.GetTypeface()
        binding.whetherShowCircleButton.setOnCheckedChangeListener(this)
        binding.whetherShowCircleText.typeface = CoCoinUtil.GetTypeface()
        binding.updateLayout.setOnClickListener {
            showToast(activity!!,
                activity!!.resources.getString(R.string.checking_update),
                null,
                SuperToast.Background.BLUE)
            val appUpdateManager = AppUpdateManager(activity)
            appUpdateManager.checkUpdateInfo(true)
        }
        binding.updateText.typeface = CoCoinUtil.GetTypeface()
        binding.updateText.text =
            activity!!.resources.getString(R.string.current_version) + CoCoinUtil.GetCurrentVersion()
        binding.updateTag.typeface = CoCoinUtil.GetTypeface()
        if (SettingManager.getInstance().canBeUpdated) {
            binding.updateTag.visibility = View.VISIBLE
        } else {
            binding.updateTag.visibility = View.GONE
        }
        val loggenOn = SettingManager.getInstance().loggenOn
        if (loggenOn) {
            // is logged on, set the user name and email
            binding.userName.text = SettingManager.getInstance().userName
            binding.userEmail.text = SettingManager.getInstance().userEmail
            binding.loginButton.text = activity!!.resources.getText(R.string.logout_button)
            binding.loginButton.setBackgroundResource(R.drawable.button_logout)
        } else {
            binding.userName.text = ""
            binding.userEmail.text = ""
            binding.loginButton.text = getResourceString(R.string.login_button)
        }
        setIconEnable(binding.userNameIcon, loggenOn)
        setIconEnable(binding.userEmailIcon, loggenOn)
        loadLogo()
        binding.monthLimitEnableButton.setCheckedImmediately(SettingManager.getInstance().isMonthLimit)
        setMonthState()
        binding.whetherShowPictureButton.setCheckedImmediately(SettingManager.getInstance().showPicture)
        setShowPictureState(SettingManager.getInstance().showPicture)
        binding.whetherShowCircleButton.setCheckedImmediately(SettingManager.getInstance().isHollow)
        setHollowState(SettingManager.getInstance().isHollow)
    }

    // Set all states about month limit/////////////////////////////////////////////////////////////////
    private fun setMonthState() {
        val isMonthLimit = SettingManager.getInstance().isMonthLimit
        val isMonthColorRemind = SettingManager.getInstance().isColorRemind
        val isForbidden = SettingManager.getInstance().isForbidden
        setIconEnable(binding.monthLimitIcon, isMonthLimit)
        setIconEnable(binding.monthExpenseIcon, isMonthLimit)
        setTVEnable(binding.monthExpenseText, isMonthLimit)
        setTVEnable(binding.monthExpense, isMonthLimit)
        setTVEnable(binding.monthColorRemindText, isMonthLimit)
        setTVEnable(binding.monthColorTypeText, isMonthLimit && isMonthColorRemind)
        setTVEnable(binding.warningExpenseText, isMonthLimit && isMonthColorRemind)
        setTVEnable(binding.monthForbiddenText, isMonthLimit)
        binding.monthExpense.text = SettingManager.getInstance().monthLimit.toString() + ""
        setIconEnable(binding.monthColorIcon, isMonthLimit && isMonthColorRemind)
        setIconEnable(binding.warningExpenseIcon, isMonthLimit && isMonthColorRemind)
        setIconEnable(binding.monthColorTypeIcon, isMonthLimit && isMonthColorRemind)
        setIconState(binding.monthColorType, isMonthLimit && isMonthColorRemind)
        if (isMonthLimit && isMonthColorRemind) {
            binding.monthColorType.isEnabled = true
            binding.monthColorType.setColorFilter(SettingManager.getInstance().remindColor, android.graphics.PorterDuff.Mode.SRC_IN)
            binding.warningExpense.isEnabled = true
            binding.warningExpense.setTextColor(ContextCompat.getColor(activity!!, R.color.drawer_text))
        } else {
            binding.monthColorType.isEnabled = false
            binding.monthColorType.setColorFilter(R.color.my_gray, android.graphics.PorterDuff.Mode.SRC_IN)
            binding.warningExpense.isEnabled = false
            binding.warningExpense.setTextColor(ContextCompat.getColor(activity!!, R.color.my_gray))
        }
        setIconEnable(binding.monthForbiddenIcon, isMonthLimit && isForbidden)
        binding.monthColorRemindButton.isEnabled = isMonthLimit
        binding.monthColorRemindButton.setCheckedImmediately(SettingManager.getInstance().isColorRemind)
        binding.monthForbiddenButton.isEnabled = isMonthLimit
        binding.monthForbiddenButton.setCheckedImmediately(SettingManager.getInstance().isForbidden)
    }

    private fun setShowPictureState(isChecked: Boolean) {
        setIconEnable(binding.whetherShowPictureIcon, isChecked)
    }

    private fun setHollowState(isChecked: Boolean) {
        setIconEnable(binding.whetherShowCircleIcon, isChecked)
    }

    private fun setIconEnable(icon: MaterialIconView?, enable: Boolean) {
        if (enable) icon!!.setColor(activity!!.resources.getColor(R.color.my_blue)) else icon!!.setColor(
            activity!!.resources.getColor(R.color.my_gray))
    }

    private fun setIconState(icon: ImageView, enable: Boolean) {
        val color = if (enable) {
            R.color.my_blue
        } else {
            R.color.my_gray
        }
        icon.setColorFilter(ContextCompat.getColor(activity!!, color), android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun setTVEnable(tv: TextView?, enable: Boolean) {
        if (enable) tv!!.setTextColor(activity!!.resources.getColor(R.color.drawer_text)) else tv!!.setTextColor(
            activity!!.resources.getColor(R.color.my_gray))
    }

    // choose a color///////////////////////////////////////////////////////////////////////////////////
    override fun onColorSelection(dialog: ColorChooserDialog, @ColorRes selectedColor: Int) {
        binding.monthColorType.setColorFilter(selectedColor, android.graphics.PorterDuff.Mode.SRC_IN)
        SettingManager.getInstance().remindColor = selectedColor
        updateSettingsToServer(UPDATE_REMIND_COLOR)
        SettingManager.getInstance().mainViewRemindColorShouldChange = true
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {}

    private fun getRemindColorSelectDialog() : ColorChooserDialog {
        return ColorChooserDialog.Builder(activity!!, R.string.set_remind_color_dialog_title)
                .titleSub(R.string.set_remind_color_dialog_sub_title)
                .preselect(SettingManager.getInstance().remindColor)
                .doneButton(R.string.submit)
                .cancelButton(R.string.cancel)
                .backButton(R.string.back)
                .customButton(R.string.custom)
                .dynamicButtonColor(true)
                .build()
    }

    // whether sync the settings from server////////////////////////////////////////////////////////////
    private fun whetherSyncSettingsFromServer() {
        MaterialDialog.Builder(activity!!)
            .iconRes(R.drawable.cocoin_logo)
            .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
            .limitIconToDefaultSize() // limits the displayed icon size to 48dp
            .title(R.string.sync_dialog_title)
            .stackingBehavior(StackingBehavior.ALWAYS) //                .forceStacking(true)
            .content(R.string.sync_dialog_content)
            .positiveText(R.string.sync_dialog_sync_to_local)
            .negativeText(R.string.sync_dialog_sync_to_server)
            .cancelable(false)
            .onAny { dialog, which ->
                if (which == DialogAction.POSITIVE) {
                    // sync to local
                    downloadLogoFromServer()
                    val user = currentUser
                    var tip = ""
                    var accountBookPasswordChanged = false
                    if (user.accountBookPassword != SettingManager.getInstance().password) accountBookPasswordChanged =
                        true
                    SettingManager.getInstance().isMonthLimit = user.isMonthLimit
                    binding.monthLimitEnableButton.isChecked = user.isMonthLimit
                    SettingManager.getInstance().monthLimit = user.monthLimit
                    if (SettingManager.getInstance().isMonthLimit) binding.monthExpense.withNumber(
                        SettingManager.getInstance()
                            .monthLimit).setDuration(1000).start()
                    SettingManager.getInstance().isColorRemind = user.isColorRemind
                    binding.monthColorRemindButton.isChecked = user.isColorRemind
                    SettingManager.getInstance().monthWarning = user.monthWarning
                    if (SettingManager.getInstance().isMonthLimit
                        && SettingManager.getInstance().isColorRemind
                    ) binding.warningExpense.withNumber(SettingManager.getInstance()
                        .monthWarning).setDuration(1000).start()
                    SettingManager.getInstance().remindColor = user.remindColor
                    binding.monthColorType.setColorFilter(ContextCompat.getColor(activity!!, SettingManager.getInstance().remindColor), android.graphics.PorterDuff.Mode.SRC_IN)
                    SettingManager.getInstance().isForbidden = user.isForbidden
                    binding.monthColorRemindButton.isChecked = user.isForbidden
                    SettingManager.getInstance().accountBookName = user.accountBookName
                    binding.accountBookName.text = user.accountBookName
                    SettingManager.getInstance().password = user.accountBookPassword
                    // Todo tag sort
                    SettingManager.getInstance().showPicture = user.showPicture
                    binding.whetherShowPictureButton.isChecked = user.showPicture
                    SettingManager.getInstance().isHollow = user.isHollow
                    binding.whetherShowCircleButton.isChecked = user.isHollow
                    SettingManager.getInstance().mainViewMonthExpenseShouldChange = true
                    SettingManager.getInstance().mainViewRemindColorShouldChange = true
                    SettingManager.getInstance().mainViewTitleShouldChange = true
                    SettingManager.getInstance().todayViewMonthExpenseShouldChange = true
                    SettingManager.getInstance().todayViewPieShouldChange = true
                    SettingManager.getInstance().todayViewTitleShouldChange = true
                    // SettingManager.getInstance().getMainActivityTagShouldChange();
                    if (accountBookPasswordChanged) tip = """
     
     ${getString(R.string.your_current_account_book_password_is)}${SettingManager.getInstance().password}
     """.trimIndent()
                    MaterialDialog.Builder(activity!!)
                        .typeface(CoCoinUtil.GetTypeface(), CoCoinUtil.GetTypeface())
                        .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                        .title(R.string.sync_to_local_successfully_dialog_title)
                        .content(getString(R.string.sync_to_local_successfully_dialog_content) + tip)
                        .positiveText(R.string.ok)
                        .show()
                } else if (which == DialogAction.NEGATIVE) {
                    // sync to server
                    uploadLogoToServer()
                    val user = currentUser
                    user.isMonthLimit = SettingManager.getInstance().isMonthLimit
                    user.monthLimit = SettingManager.getInstance().monthLimit
                    user.isColorRemind = SettingManager.getInstance().isColorRemind
                    user.monthWarning = SettingManager.getInstance().monthWarning
                    user.remindColor = SettingManager.getInstance().remindColor
                    user.isForbidden = SettingManager.getInstance().isForbidden
                    user.accountBookName = SettingManager.getInstance().accountBookName
                    user.accountBookPassword = SettingManager.getInstance().password
                    // Todo tag sort
                    user.showPicture = SettingManager.getInstance().showPicture
                    user.isHollow = SettingManager.getInstance().isHollow
                    user.update(CoCoinApplication.getAppContext(),
                        user.objectId, object : UpdateListener() {
                            override fun onSuccess() {
                                showToast(9, "")
                            }

                            override fun onFailure(code: Int, msg: String) {
                                showToast(10, msg)
                            }
                        })
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun updateAllSettings() {
        updateSettingsToServer(0)
        updateSettingsToServer(1)
        updateSettingsToServer(2)
    }

    // update part of settings//////////////////////////////////////////////////////////////////////////
    private fun updateSettingsToServer(setting: Int) {
        val currentUser: User? = currentUser
        if (currentUser == null) {
            Log.d("Saver", "User hasn't log in.")
            return
        }
        when (setting) {
            UPDATE_LOGO -> {}
            UPDATE_IS_MONTH_LIMIT ->                 // is month limit
                currentUser.isMonthLimit = SettingManager.getInstance().isMonthLimit
            UPDATE_MONTH_LIMIT ->                 // month limit
                currentUser.monthLimit = SettingManager.getInstance().monthLimit
            UPDATE_IS_COLOR_REMIND ->                 // is color remind
                currentUser.isColorRemind = SettingManager.getInstance().isColorRemind
            UPDATE_MONTH_WARNING ->                 // month warning
                currentUser.monthWarning = SettingManager.getInstance().monthWarning
            UPDATE_REMIND_COLOR ->                 // remind color
                currentUser.remindColor = SettingManager.getInstance().remindColor
            UPDATE_IS_FORBIDDEN ->                 // is forbidden
                currentUser.isForbidden = SettingManager.getInstance().isForbidden
            UPDATE_ACCOUNT_BOOK_NAME ->                 // account book name
                currentUser.accountBookName = SettingManager.getInstance().accountBookName
            UPDATE_ACCOUNT_BOOK_PASSWORD ->                 // account book password
                currentUser.accountBookPassword = SettingManager.getInstance().password
            UPDATE_SHOW_PICTURE ->                 // show picture
                currentUser.showPicture = SettingManager.getInstance().showPicture
            UPDATE_IS_HOLLOW ->                 // is hollow
                currentUser.isHollow = SettingManager.getInstance().isHollow
            UPDATE_LOGO_ID ->                 // has a logo which has been updated
                currentUser.logoObjectId = SettingManager.getInstance().logoObjectId
        }
        currentUser.update(CoCoinApplication.getAppContext(),
            currentUser.objectId, object : UpdateListener() {
                override fun onSuccess() {
                    Log.d("Saver", "Update $setting successfully.")
                    // the new account book name is updated to server successfully
                    if (setting == UPDATE_ACCOUNT_BOOK_NAME) showToast(0, "")
                }

                override fun onFailure(code: Int, msg: String) {
                    Log.d("Saver", "Update $setting fail.")
                    // the new account book name is failed to updated to server
                    if (setting == UPDATE_ACCOUNT_BOOK_NAME) showToast(1, "")
                }
            })
    }

    private fun syncUserInfo() {
        val user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
    }

    // Get the current user/////////////////////////////////////////////////////////////////////////////
    private val currentUser: User
        get() = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java) ?: User()

    // Get string///////////////////////////////////////////////////////////////////////////////////////
    private fun getResourceString(resourceId: Int): String {
        return CoCoinApplication.getAppContext().resources.getString(resourceId)
    }

    // activity finish//////////////////////////////////////////////////////////////////////////////////
//    override fun finish() {
//        SuperToast.cancelAllSuperToasts()
//        super.finish()
//    }

    // Show toast///////////////////////////////////////////////////////////////////////////////////////
    private fun showToast(toastType: Int, msg: String) {
        Log.d("CoCoin", msg)
        SuperToast.cancelAllSuperToasts()
        val superToast = SuperActivityToast(activity!!)
        superToast.animations = ToastUtil.TOAST_ANIMATION
        superToast.duration = SuperToast.Duration.LONG
        superToast.textColor = Color.parseColor("#ffffff")
        superToast.setTextSize(SuperToast.TextSize.SMALL)
        var tip = ""
        when (toastType) {
            0 -> {
                // the new account book name is updated to server successfully
                superToast.text = CoCoinApplication.getAppContext().resources.getString(
                    R.string.change_and_update_account_book_name_successfully)
                superToast.background = SuperToast.Background.BLUE
            }
            1 -> {
                // the new account book name is failed to updated to server
                superToast.text = CoCoinApplication.getAppContext().resources.getString(
                    R.string.change_and_update_account_book_name_fail)
                superToast.background = SuperToast.Background.RED
            }
            2 -> {
                // the new account book name is changed successfully
                superToast.text = CoCoinApplication.getAppContext().resources.getString(
                    R.string.change_account_book_name_successfully)
                superToast.background = SuperToast.Background.BLUE
            }
            3 -> {
                // the new account book name is failed to change
                superToast.text = CoCoinApplication.getAppContext().resources.getString(
                    R.string.change_account_book_name_fail)
                superToast.background = SuperToast.Background.RED
            }
            4 -> {
                // register successfully
                tip = msg
                superToast.text = getResourceString(R.string.register_successfully) + tip
                superToast.background = SuperToast.Background.BLUE
            }
            5 -> {
                // register failed
                tip = getResourceString(R.string.network_disconnection)
                if (msg[1] == 's') tip = getResourceString(R.string.user_name_exist)
                if (msg[0] == 'e') tip = getResourceString(R.string.user_email_exist)
                if (msg[1] == 'n') tip = getResourceString(R.string.user_mobile_exist)
                superToast.text = getResourceString(R.string.register_fail) + tip
                superToast.background = SuperToast.Background.RED
            }
            6 -> {
                // login successfully
                tip = msg
                superToast.text = getResourceString(R.string.login_successfully) + tip
                superToast.background = SuperToast.Background.BLUE
            }
            7 -> {
                // login failed
                tip = getResourceString(R.string.network_disconnection)
                if (msg[0] == 'u') tip = getResourceString(R.string.user_name_or_password_incorrect)
                if (msg[1] == 'n') tip = getResourceString(R.string.user_mobile_exist)
                superToast.text = getResourceString(R.string.login_fail) + tip
                superToast.background = SuperToast.Background.RED
            }
            8 -> {
                // log out successfully
                superToast.text = getResourceString(R.string.log_out_successfully)
                superToast.background = SuperToast.Background.BLUE
            }
            9 -> {
                // sync settings successfully
                superToast.text = getResourceString(R.string.sync_to_server_successfully)
                superToast.background = SuperToast.Background.BLUE
            }
            10 -> {
                // sync settings failed
                tip = getResourceString(R.string.network_disconnection)
                superToast.text = getResourceString(R.string.sync_to_server_failed) + tip
                superToast.background = SuperToast.Background.RED
            }
        }
        superToast.textView.typeface = CoCoinUtil.GetTypeface()
        superToast.show()
    }
}