package com.jbekas.cocoin.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
import com.balysv.materialripple.MaterialRippleLayout
import com.bmob.BmobProFile
import com.bmob.btp.callback.DeleteFileListener
import com.bmob.btp.callback.DownloadListener
import com.bmob.btp.callback.UploadListener
import com.daimajia.slider.library.SliderLayout
import com.github.johnpersano.supertoasts.SuperToast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jbekas.cocoin.BuildConfig
import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.adapter.TodayViewFragmentAdapter
import com.jbekas.cocoin.databinding.FragmentDateRangeReportBinding
import com.jbekas.cocoin.model.Logo
import com.jbekas.cocoin.db.RecordManager
import com.jbekas.cocoin.model.SettingManager
import com.jbekas.cocoin.model.TaskManager
import com.jbekas.cocoin.model.UploadInfo
import com.jbekas.cocoin.model.User
import com.jbekas.cocoin.ui.MyQuery
import com.jbekas.cocoin.ui.RiseNumberTextView
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.ToastUtil.showToast
import com.koushikdutta.ion.Ion
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import net.steamcrafted.materialiconlib.MaterialIconView
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DateRangeReportFragment : Fragment() {

    @Inject
    lateinit var coCoinUtil: CoCoinUtil

    private var _binding: FragmentDateRangeReportBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var mViewPager: ViewPager2? = null
    private var mDrawer: DrawerLayout? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var toolbar: Toolbar? = null
    private var todayModeAdapter: TodayViewFragmentAdapter? = null

    private var custom: MaterialRippleLayout? = null
    private var tags: MaterialRippleLayout? = null
    private var months: MaterialRippleLayout? = null
    private var list: MaterialRippleLayout? = null
    private var report: MaterialRippleLayout? = null
    private var sync: MaterialRippleLayout? = null
    private var settings: MaterialRippleLayout? = null
    private var help: MaterialRippleLayout? = null
    private var feedback: MaterialRippleLayout? = null
    private var about: MaterialRippleLayout? = null
    private var syncIcon: MaterialIconView? = null
    private var userName: TextView? = null
    private var userEmail: TextView? = null
    private var title: TextView? = null
    private var monthExpenseTip: TextView? = null
    private var monthExpense: RiseNumberTextView? = null
    private var profileImage: CircleImageView? = null
    private var mDemoSlider: SliderLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        _binding = FragmentDateRangeReportBinding.inflate(inflater, container, false)

        SuperToast.cancelAllSuperToasts()

        val view = binding.root

        mViewPager = view.findViewById<View>(R.id.today_view_pager) as ViewPager2
/*
        toolbar?.let { toolbar ->
//            toolbar.title = SettingManager.getInstance().accountBookName

            (activity!! as AppCompatActivity).setSupportActionBar(toolbar)
            (activity!! as AppCompatActivity).supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setDisplayShowHomeEnabled(true)
                it.setDisplayShowTitleEnabled(true)
                it.setDisplayUseLogoEnabled(false)
                it.setHomeButtonEnabled(true)
            }
        }
*/

        mDrawerToggle = object : ActionBarDrawerToggle(requireActivity(), mDrawer, 0, 0) {
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                monthExpense!!.text = "0"
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                monthExpense!!.withNumber(
                    RecordManager.currentMonthExpense).setDuration(500).start()
            }
        }
//        mDrawer!!.setDrawerListener(mDrawerToggle)
//        val logo = view.findViewById<View>(R.id.logo_white)
//        logo?.setOnClickListener {
//            Timber.e("onClick not implemented.")
//        }
        todayModeAdapter = TodayViewFragmentAdapter(requireActivity())
        mViewPager?.adapter = todayModeAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, mViewPager!!) { tab, position ->
            tab.text = when (position) {
                TodayViewFragment.TODAY -> getString(R.string.today_view_today)
                TodayViewFragment.YESTERDAY -> getString(R.string.today_view_yesterday)
                TodayViewFragment.THIS_WEEK -> getString(R.string.today_view_this_week)
                TodayViewFragment.LAST_WEEK -> getString(R.string.today_view_last_week)
                TodayViewFragment.THIS_MONTH -> getString(R.string.today_view_this_month)
                TodayViewFragment.LAST_MONTH -> getString(R.string.today_view_last_month)
                TodayViewFragment.THIS_YEAR -> getString(R.string.today_view_this_year)
                TodayViewFragment.LAST_YEAR -> getString(R.string.today_view_last_year)
                else -> ""
            }

        }.attach()

        setListeners()
//        profileImage = mDrawer!!.findViewById<View>(R.id.profile_image) as CircleImageView
//        profileImage!!.setOnClickListener {
//            if (SettingManager.getInstance().loggenOn) {
//                showToast(activity!!, R.string.change_logo_tip, null, null)
//            } else {
//                showToast(activity!!, R.string.login_tip, null, null)
//            }
//        }
//        mDemoSlider = view.findViewById<View>(R.id.slider) as SliderLayout
//        val urls = coCoinUtil.GetDrawerTopUrl()
//        for (name in urls.keys) {
//            val customSliderView = CustomSliderView(activity!!)
//            // initialize a SliderLayout
//            customSliderView
//                .image(urls[name]!!).scaleType = BaseSliderView.ScaleType.Fit
//            mDemoSlider!!.addSlider(customSliderView)
//        }
//        mDemoSlider!!.setPresetTransformer(SliderLayout.Transformer.ZoomOut)
//        mDemoSlider!!.setCustomAnimation(DescriptionAnimation())
//        mDemoSlider!!.setDuration(4000)
//        mDemoSlider!!.setCustomIndicator(view.findViewById<View>(R.id.custom_indicator) as PagerIndicator)
//        loadLogo()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

/*
    private fun loadRangeMode() {
        Timber.d("RANGE_MODE")
        val intent = Intent(activity!!, AccountBookCustomViewActivity::class.java)
        startActivity(intent)
    }

    private fun loadTagMode() {
        Timber.d("TAG_MODE")
        val intent = Intent(activity!!, AccountBookTagViewActivity::class.java)
        startActivity(intent)
    }

    private fun loadMonthMode() {
        Timber.d("MONTH_MODE")
        val intent = Intent(activity!!, AccountBookMonthViewActivity::class.java)
        startActivity(intent)
    }

    private fun loadListMode() {
        Timber.d("LIST_MODE")
        val intent = Intent(activity!!, AccountBookListViewActivity::class.java)
        startActivity(intent)
    }
*/

    private var syncSuccessNumber = 0
    private var syncFailedNumber = 0
    private var cloudRecordNumber = 0
    private var cloudOldDatabaseUrl: String? = null
    private var cloudOldDatabaseFileName: String? = null
    private val uploadObjectId: String? = null
    var syncQueryDialog: MaterialDialog? = null
    var syncChooseDialog: MaterialDialog? = null
    var syncProgressDialog: MaterialDialog? = null
    private fun sync() {
        if (!SettingManager.getInstance().loggenOn) {
            showToast(
                activity = requireActivity(),
                textId = R.string.login_tip)
        } else {
            syncSuccessNumber = 0
            syncFailedNumber = 0
            syncQueryDialog = MaterialDialog.Builder(requireActivity())
                .title(R.string.sync_querying_title)
                .content(R.string.sync_querying_content)
                .negativeText(R.string.cancel)
                .progress(true, 0)
                .onAny { dialog, which ->
                    if (which == DialogAction.NEGATIVE) {
                    }
                }
                .show()
            val user = BmobUser
                .getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
            val myQuery = MyQuery()
            myQuery.task = ++TaskManager.QUERY_UPDATE_TASK
            myQuery.query = BmobQuery()
            myQuery.query.addWhereEqualTo("userId", user.objectId)
            myQuery.query.setLimit(1)
            myQuery.query.findObjects(CoCoinApplication.getAppContext(),
                object : FindListener<UploadInfo>() {
                    override fun onSuccess(`object`: List<UploadInfo>) {
                        if (myQuery.task != TaskManager.QUERY_UPDATE_TASK) return else {
                            syncQueryDialog?.dismiss()
                            cloudRecordNumber = 0
                            var cal: Calendar? = null
                            if (`object`.size == 0) {
                            } else {
                                cloudRecordNumber = `object`[0].recordNumber
                                cloudOldDatabaseUrl = `object`[0].databaseUrl
                                cloudOldDatabaseFileName = `object`[0].fileName
                                //                            uploadObjectId = object.get(0).getObjectId();
                                cal = Calendar.getInstance()
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                Timber.e("cal.setTime not enabled.")
//                                try {
//                                    cal.setTime(sdf.parse(object.get(0).getUpdatedAt()));
//                                } catch (ParseException p) {
//
//                                }
                            }
                            val content = (resources.getString(R.string.sync_info_cloud_record_0)
                                    + cloudRecordNumber
                                    + resources.getString(R.string.sync_info_cloud_record_1)
                                    + (if (cal == null) resources.getString(R.string.sync_info_cloud_time_2) else resources.getString(R.string.sync_info_cloud_time_0) + coCoinUtil.getCalendarString(
                                CoCoinApplication.getAppContext(),
                                cal) + resources.getString(R.string.sync_info_cloud_time_1))
                                    + resources.getString(R.string.sync_info_mobile_record_0)
                                    + RecordManager.RECORDS.size
                                    + resources.getString(R.string.sync_info_mobile_record_1)
                                    + (if (SettingManager.getInstance().recentlySyncTime == null) resources.getString(R.string.sync_info_mobile_time_2) else resources.getString(R.string.sync_info_mobile_time_0) + coCoinUtil.getCalendarString(
                                CoCoinApplication.getAppContext(),
                                SettingManager.getInstance().recentlySyncTime) + resources.getString(R.string.sync_info_mobile_time_1))
                                    + resources.getString(R.string.sync_choose_content))
                            syncChooseDialog =
                                MaterialDialog.Builder(activity!!)
                                    .title(R.string.sync_choose_title)
                                    .content(content)
                                    .positiveText(R.string.sync_to_cloud)
                                    .negativeText(R.string.sync_to_mobile)
                                    .neutralText(R.string.cancel)
                                    .onAny(SingleButtonCallback { dialog, which ->
                                        syncChooseDialog!!.dismiss()
                                        if (which == DialogAction.POSITIVE) {
                                            // sync to cloud
                                            var subContent: String? = ""
                                            if (RecordManager.RECORDS.size == 0) {
                                                subContent =
                                                    resources.getString(R.string.mobile_record_empty)
                                                MaterialDialog.Builder(activity!!)
                                                    .title(R.string.sync)
                                                    .content(subContent)
                                                    .positiveText(R.string.ok_1)
                                                    .show()
                                                return@SingleButtonCallback
                                            } else {
                                                subContent =
                                                    (resources.getString(R.string.sure_to_cloud_0)
                                                            + RecordManager.RECORDS.size
                                                            + resources.getString(R.string.sure_to_cloud_1))
                                            }
                                            MaterialDialog.Builder(activity!!)
                                                .title(R.string.sync)
                                                .content(subContent)
                                                .positiveText(R.string.ok_1)
                                                .negativeText(R.string.cancel)
                                                .onAny { dialog, which ->
                                                    if (which == DialogAction.POSITIVE) {
                                                        syncProgressDialog =
                                                            MaterialDialog.Builder(activity!!)
                                                                .title(R.string.syncing)
                                                                .content(resources.getString(R.string.uploading_0) + "1" + resources.getString(R.string.uploading_1))
                                                                .progress(false,
                                                                    RecordManager.RECORDS.size,
                                                                    true)
                                                                .cancelable(false)
                                                                .show()
                                                        val databasePath =
                                                            coCoinUtil.getRecordDatabasePath(
                                                                CoCoinApplication.getAppContext())
                                                        //                                                                final BmobFile bmobFile = new BmobFile(new File(databasePath));
//                                                                bmobFile.uploadblock(mContext, new UploadFileListener() {
//
//                                                                    @Override
//                                                                    public void onSuccess() {
//                                                                        if (BuildConfig.DEBUG) {
//                                                                            Timber.d("Upload successfully fileName: " + databasePath);
//                                                                            Timber.d("Upload successfully url: " + bmobFile.getFileUrl(mContext));
//                                                                        }
//                                                                        // the new database is uploaded successfully
//                                                                        // delete the old database(if there is)
//                                                                        if (cloudOldDatabaseUrl != null) {
//                                                                            deleteOldDatabaseOnCloud(cloudOldDatabaseUrl);
//                                                                        }
//                                                                        // update the UploadInfo record for the new url
//                                                                        if (uploadObjectId == null) {
//                                                                            // first time
//                                                                            UploadInfo uploadInfo = new UploadInfo();
//                                                                            uploadInfo.setUserId(user.getObjectId());
//                                                                            uploadInfo.setRecordNumber(RecordManager.getInstance(mContext).RECORDS.size());
//                                                                            uploadInfo.setDatabaseUrl(bmobFile.getFileUrl(mContext));
//                                                                            uploadInfo.save(mContext, new SaveListener() {
//                                                                                @Override
//                                                                                public void onSuccess() {
//                                                                                    // upload successfully
//                                                                                    syncProgressDialog.dismiss();
//                                                                                    new MaterialDialog.Builder(mContext)
//                                                                                            .title(R.string.sync_completely_title)
//                                                                                            .content(RecordManager.getInstance(mContext).RECORDS.size() + coCoinUtil.GetString(mContext, R.string.uploading_fail_1))
//                                                                                            .positiveText(R.string.ok_1)
//                                                                                            .show();
//                                                                                }
//                                                                                @Override
//                                                                                public void onFailure(int code, String arg0) {
//                                                                                    // 添加失败
//                                                                                }
//                                                                            });
//                                                                        } else {
//                                                                            UploadInfo uploadInfo = new UploadInfo();
//                                                                            uploadInfo.setUserId(user.getObjectId());
//                                                                            uploadInfo.setRecordNumber(RecordManager.getInstance(mContext).RECORDS.size());
//                                                                            uploadInfo.setDatabaseUrl(bmobFile.getFileUrl(mContext));
//                                                                            uploadInfo.update(mContext, uploadObjectId, new UpdateListener() {
//                                                                                @Override
//                                                                                public void onSuccess() {
//                                                                                    // upload successfully
//                                                                                    syncProgressDialog.dismiss();
//                                                                                    new MaterialDialog.Builder(mContext)
//                                                                                            .title(R.string.sync_completely_title)
//                                                                                            .content(RecordManager.getInstance(mContext).RECORDS.size() + coCoinUtil.GetString(mContext, R.string.uploading_fail_1))
//                                                                                            .positiveText(R.string.ok_1)
//                                                                                            .show();
//                                                                                }
//                                                                                @Override
//                                                                                public void onFailure(int code, String msg) {
//                                                                                    // upload failed
//                                                                                    Log.i("bmob","更新失败："+msg);
//                                                                                }
//                                                                            });
//                                                                        }
//                                                                    }
//
//                                                                    @Override
//                                                                    public void onProgress(Integer value) {
//                                                                        syncProgressDialog.setProgress(value);
//                                                                    }
//
//                                                                    @Override
//                                                                    public void onFailure(int code, String msg) {
//                                                                        // upload failed
//                                                                        if (BuildConfig.DEBUG) Log.d("CoCoin", "Upload database failed " + code + " " + msg);
//                                                                        syncProgressDialog.dismiss();
//                                                                        new MaterialDialog.Builder(mContext)
//                                                                                .title(R.string.sync_failed)
//                                                                                .content(R.string.uploading_fail_0)
//                                                                                .positiveText(R.string.ok_1)
//                                                                                .show();
//                                                                    }
//                                                                });
                                                        BmobProFile.getInstance(CoCoinApplication.getAppContext())
                                                            .upload(databasePath,
                                                                object : UploadListener {
                                                                    override fun onSuccess(
                                                                        fileName: String,
                                                                        url: String,
                                                                        file: BmobFile,
                                                                    ) {
                                                                        coCoinUtil.deleteBmobUploadCach(
                                                                            CoCoinApplication.getAppContext())
                                                                        if (BuildConfig.DEBUG) {
                                                                            Timber.d("Upload successfully fileName: $fileName")
                                                                            Timber.d("Upload successfully url: $url")
                                                                        }
                                                                        // the new database is uploaded successfully
                                                                        // delete the old database(if there is)
                                                                        if (cloudOldDatabaseFileName != null) {
                                                                            deleteOldDatabaseOnCloud(
                                                                                cloudOldDatabaseFileName!!)
                                                                        }
                                                                        // update the UploadInfo record for the new url
                                                                        val uploadInfo =
                                                                            UploadInfo()
                                                                        uploadInfo.userId =
                                                                            user.objectId
                                                                        uploadInfo.recordNumber =
                                                                            RecordManager.RECORDS.size
                                                                        uploadInfo.databaseUrl =
                                                                            file.getFileUrl(
                                                                                CoCoinApplication.getAppContext())
                                                                        uploadInfo.fileName =
                                                                            fileName
                                                                        if (uploadObjectId == null) {
                                                                            // insert
                                                                            uploadInfo.save(
                                                                                CoCoinApplication.getAppContext(),
                                                                                object :
                                                                                    SaveListener() {
                                                                                    override fun onSuccess() {
                                                                                        // upload successfully
                                                                                        syncProgressDialog?.dismiss()
                                                                                        MaterialDialog.Builder(activity!!)
                                                                                            .title(R.string.sync_completely_title)
                                                                                            .content(
                                                                                                RecordManager.RECORDS.size.toString() + resources.getString(R.string.uploading_fail_1))
                                                                                            .positiveText(
                                                                                                R.string.ok_1)
                                                                                            .cancelable(
                                                                                                false)
                                                                                            .show()
                                                                                    }

                                                                                    override fun onFailure(
                                                                                        code: Int,
                                                                                        arg0: String,
                                                                                    ) {
                                                                                        uploadFailed(
                                                                                            code,
                                                                                            arg0)
                                                                                    }
                                                                                })
                                                                        } else {
                                                                            // update
                                                                            uploadInfo.update(
                                                                                CoCoinApplication.getAppContext(),
                                                                                uploadObjectId,
                                                                                object :
                                                                                    UpdateListener() {
                                                                                    override fun onSuccess() {
                                                                                        // upload successfully
                                                                                        syncProgressDialog?.dismiss()
                                                                                        MaterialDialog.Builder(
                                                                                            activity!!)
                                                                                            .title(R.string.sync_completely_title)
                                                                                            .content(
                                                                                                RecordManager.RECORDS.size.toString() + resources.getString(R.string.uploading_fail_1))
                                                                                            .positiveText(
                                                                                                R.string.ok_1)
                                                                                            .cancelable(
                                                                                                false)
                                                                                            .show()
                                                                                    }

                                                                                    override fun onFailure(
                                                                                        code: Int,
                                                                                        msg: String,
                                                                                    ) {
                                                                                        uploadFailed(
                                                                                            code,
                                                                                            msg)
                                                                                    }
                                                                                })
                                                                        }
                                                                    }

                                                                    override fun onProgress(progress: Int) {
                                                                        syncProgressDialog?.setProgress(
                                                                            (progress * 1.0 / 100 * RecordManager.RECORDS.size).toInt())
                                                                    }

                                                                    override fun onError(
                                                                        statuscode: Int,
                                                                        errormsg: String,
                                                                    ) {
                                                                        // upload failed
                                                                        uploadFailed(statuscode,
                                                                            errormsg)
                                                                    }
                                                                })
                                                    }
                                                }
                                                .show()
                                        } else if (which == DialogAction.NEGATIVE) {
                                            // sync to mobile
                                            var subContent: String? = ""
                                            if (cloudRecordNumber == 0) {
                                                subContent =
                                                    resources.getString(R.string.cloud_record_empty)
                                                MaterialDialog.Builder(activity!!)
                                                    .title(R.string.sync)
                                                    .content(subContent)
                                                    .positiveText(R.string.ok_1)
                                                    .show()
                                                return@SingleButtonCallback
                                            } else {
                                                subContent =
                                                    (resources.getString(R.string.sure_to_mobile_0)
                                                            + cloudRecordNumber
                                                            + resources.getString(R.string.sure_to_mobile_1))
                                            }
                                            MaterialDialog.Builder(activity!!)
                                                .title(R.string.sync)
                                                .content(subContent)
                                                .positiveText(R.string.ok_1)
                                                .negativeText(R.string.cancel)
                                                .onAny { dialog, which ->
                                                    if (which == DialogAction.POSITIVE) {
                                                        syncProgressDialog =
                                                            MaterialDialog.Builder(activity!!)
                                                                .title(R.string.syncing)
                                                                .content(resources.getString(R.string.downloading_0) + "1" + resources.getString(R.string.downloading_1))
                                                                .progress(false,
                                                                    cloudRecordNumber,
                                                                    true)
                                                                .cancelable(false)
                                                                .show()
                                                        // download the database file to mobile
                                                        BmobProFile.getInstance(CoCoinApplication.getAppContext())
                                                            .download(cloudOldDatabaseFileName,
                                                                object : DownloadListener {
                                                                    override fun onSuccess(fullPath: String) {
                                                                        // download completely
                                                                        // delete the original database in mobile
                                                                        // copy the new database to mobile
                                                                        try {
                                                                            Timber.d("Download successfully $fullPath")
                                                                            syncProgressDialog?.setContent(
                                                                                R.string.sync_completely_content)
                                                                            val buffer =
                                                                                ByteArray(1024)
                                                                            val file =
                                                                                File(fullPath)
                                                                            val inputStream: InputStream =
                                                                                FileInputStream(file)
                                                                            val outFileNameString =
                                                                                coCoinUtil.getRecordDatabasePath(
                                                                                    CoCoinApplication.getAppContext())
                                                                            val outputStream: OutputStream =
                                                                                FileOutputStream(
                                                                                    outFileNameString)
                                                                            var length: Int
                                                                            while (inputStream.read(
                                                                                    buffer).also {
                                                                                    length = it
                                                                                } > 0
                                                                            ) {
                                                                                outputStream.write(
                                                                                    buffer,
                                                                                    0,
                                                                                    length)
                                                                            }
                                                                            Timber.d("Download successfully copy completely")
                                                                            outputStream.flush()
                                                                            outputStream.close()
                                                                            inputStream.close()
                                                                            file.delete()
                                                                            Timber.d("Download successfully delete completely")
                                                                            // refresh data
                                                                            RecordManager.RECORDS.clear()
//                                                                            RecordManager.RECORDS =
//                                                                                null
                                                                            RecordManager.getInstance(
                                                                                CoCoinApplication.getAppContext())
                                                                            todayModeAdapter!!.notifyDataSetChanged()
                                                                            Timber.d("Download successfully refresh completely")
                                                                            syncProgressDialog?.dismiss()
                                                                            MaterialDialog.Builder(
                                                                                activity!!)
                                                                                .title(R.string.sync_completely_title)
                                                                                .content(
                                                                                    cloudRecordNumber.toString() + resources.getString(R.string.downloading_fail_1))
                                                                                .positiveText(R.string.ok_1)
                                                                                .cancelable(false)
                                                                                .show()
                                                                        } catch (i: IOException) {
                                                                            i.printStackTrace()
                                                                        }
                                                                    }

                                                                    override fun onProgress(
                                                                        localPath: String,
                                                                        percent: Int,
                                                                    ) {
                                                                        syncProgressDialog?.setProgress(
                                                                            (percent.toFloat() / 100 * RecordManager.RECORDS.size).toInt())
                                                                    }

                                                                    override fun onError(
                                                                        statuscode: Int,
                                                                        errormsg: String,
                                                                    ) {
                                                                        downloadFailed(statuscode,
                                                                            errormsg)
                                                                    }
                                                                })
                                                    }
                                                }
                                                .show()
                                        } else {
                                        }
                                    })
                                    .show()
                        }
                    }

                    override fun onError(code: Int, msg: String) {
                        syncQueryDialog?.dismiss()
                        if (BuildConfig.DEBUG) Timber.d("Query: $msg")
                        if (syncQueryDialog != null) syncQueryDialog!!.dismiss()
                        MaterialDialog.Builder(activity!!)
                            .title(R.string.sync_querying_fail_title)
                            .content(R.string.sync_querying_fail_content)
                            .positiveText(R.string.ok_1)
                            .show()
                    }
                })
        }
    }

    private fun deleteOldDatabaseOnCloud(fileName: String) {
        BmobProFile.getInstance(CoCoinApplication.getAppContext())
            .deleteFile(fileName, object : DeleteFileListener {
                override fun onError(errorcode: Int, errormsg: String) {
                    if (BuildConfig.DEBUG) Timber.d("Delete old cloud database failed $cloudOldDatabaseUrl")
                }

                override fun onSuccess() {
                    if (BuildConfig.DEBUG) Timber.d("Delete old cloud database successfully $cloudOldDatabaseUrl")
                }
            })
    }

    private fun uploadFailed(code: Int, msg: String) {
        // upload failed
        if (BuildConfig.DEBUG) Timber.d("Upload database failed $code $msg")
        syncProgressDialog!!.dismiss()
        MaterialDialog.Builder(requireActivity())
            .title(R.string.sync_failed)
            .content(R.string.uploading_fail_0)
            .positiveText(R.string.ok_1)
            .cancelable(false)
            .show()
    }

    private fun downloadFailed(code: Int, msg: String) {
        // upload failed
        if (BuildConfig.DEBUG) Timber.d("Download database failed $code $msg")
        syncProgressDialog!!.dismiss()
        MaterialDialog.Builder(requireActivity())
            .title(R.string.sync_failed)
            .content(R.string.downloading_fail_0)
            .positiveText(R.string.ok_1)
            .cancelable(false)
            .show()
    }

    private val uploadCounter: SaveListener = object : SaveListener() {
        override fun onSuccess() {
            syncSuccessNumber++
            syncProgressDialog!!.incrementProgress(1)
            if (syncSuccessNumber == RecordManager.RECORDS.size) {
                syncProgressDialog!!.setContent(R.string.sync_completely_content)
            } else {
                syncProgressDialog!!.setContent(resources.getString(R.string.uploading_0) + (syncSuccessNumber + 1) + resources.getString(R.string.uploading_1))
            }
            if (syncSuccessNumber + syncFailedNumber == RecordManager.RECORDS.size) {
                syncProgressDialog!!.dismiss()
                MaterialDialog.Builder(activity!!)
                    .title(R.string.sync_completely_title)
                    .content(syncSuccessNumber.toString() + resources.getString(R.string.uploading_fail_1))
                    .positiveText(R.string.ok_1)
                    .show()
            }
        }

        override fun onFailure(code: Int, arg0: String) {
            syncFailedNumber++
            syncProgressDialog!!.incrementProgress(1)
            if (syncSuccessNumber + syncFailedNumber == RecordManager.RECORDS.size) {
                syncProgressDialog!!.dismiss()
                MaterialDialog.Builder(activity!!)
                    .title(R.string.sync_completely_title)
                    .content(syncSuccessNumber.toString() + resources.getString(R.string.uploading_fail_1))
                    .positiveText(R.string.ok_1)
                    .show()
            }
        }
    }

    private fun loadSettings() {
        Timber.e("TODO: When TodayView is converted to Fragment, hook up with nav controller.")
        Toast.makeText(requireActivity(), "Disabled", Toast.LENGTH_SHORT).show()
        //val intent = Intent(this, AccountBookSettingActivity::class.java)
        //startActivity(intent)
    }

    public override fun onResume() {
        if (mDemoSlider != null) mDemoSlider!!.startAutoCycle()
        super.onResume()
        if (SettingManager.getInstance().todayViewPieShouldChange) {
            todayModeAdapter!!.notifyDataSetChanged()
            SettingManager.getInstance().todayViewPieShouldChange = java.lang.Boolean.FALSE
        }
        if (SettingManager.getInstance().todayViewTitleShouldChange) {
            title!!.text = SettingManager.getInstance().accountBookName
            SettingManager.getInstance().todayViewTitleShouldChange = false
        }
        if (SettingManager.getInstance().recordIsUpdated) {
            todayModeAdapter!!.notifyDataSetChanged()
            SettingManager.getInstance().recordIsUpdated = false
        }
        if (SettingManager.getInstance().todayViewMonthExpenseShouldChange) {
            Timber.w("todayViewMonthExpenseShouldChange is set to true")
            Timber.w("isMonthLimit: ${SettingManager.getInstance().isMonthLimit}")
            if (SettingManager.getInstance().isMonthLimit) {
                monthExpenseTip?.visibility = View.VISIBLE
                monthExpense?.withNumber(
                    RecordManager.currentMonthExpense)?.setDuration(500)?.start()
            } else {
                monthExpenseTip?.visibility = View.INVISIBLE
                monthExpense?.visibility = View.INVISIBLE
            }
        }
        if (SettingManager.getInstance().todayViewLogoShouldChange) {
            loadLogo()
            SettingManager.getInstance().todayViewLogoShouldChange = false
        }
//        if (SettingManager.getInstance().todayViewInfoShouldChange) {
//            setIconEnable(syncIcon, SettingManager.getInstance().loggenOn)
//            val user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
//            if (user != null) {
//                userName!!.text = user.username
//                userEmail!!.text = user.email
//                loadLogo()
//            } else {
//                userName!!.text = ""
//                userEmail!!.text = ""
//                loadLogo()
//            }
//            SettingManager.getInstance().todayViewInfoShouldChange = false
//        }
    }

//    override fun onPostCreate(savedInstanceState: Bundle?) {
//        super.onPostCreate(savedInstanceState)
//        mDrawerToggle!!.syncState()
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return mDrawerToggle!!.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item)
    }

//    override fun onBackPressed() {
//        if (mDrawer!!.isDrawerOpen(GravityCompat.START)) {
//            mDrawer!!.closeDrawers()
//            return
//        }
//        super.onBackPressed()
//    }

    private fun setListeners() {
//        custom!!.setOnClickListener { loadRangeMode() }
//        tags!!.setOnClickListener { loadTagMode() }
//        months!!.setOnClickListener { loadMonthMode() }
//        settings!!.setOnClickListener { loadSettings() }
//        list!!.setOnClickListener { loadListMode() }
//        report!!.setOnClickListener {
//            startActivity(Intent(activity!!,
//                AccountBookReportViewActivity::class.java))
//        }
//        sync!!.setOnClickListener { sync() }
    }

    private fun loadLogo() {
        val user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
        if (user != null) {
            try {
                val logoFile =
                    File(CoCoinApplication.getAppContext().filesDir.toString() + coCoinUtil.LOGO_NAME)
                if (!logoFile.exists()) {
                    // the local logo file is missed
                    // try to get from the server
                    val bmobQuery: BmobQuery<Logo> = BmobQuery<Logo>()
                    Timber.d(user.logoObjectId)
                    bmobQuery.addWhereEqualTo("objectId", user.logoObjectId)
                    bmobQuery.findObjects(CoCoinApplication.getAppContext(),
                        object : FindListener<Logo>() {
                            override fun onSuccess(`object`: List<Logo>) {
                                // there has been an old logo in the server/////////////////////////////////////////////////////////
                                val url =
                                    `object`[0].file.getFileUrl(CoCoinApplication.getAppContext())
                                if (BuildConfig.DEBUG) Timber.d("Logo in server: $url")
                                Ion.with(CoCoinApplication.getAppContext()).load(url)
                                    .write(File(CoCoinApplication.getAppContext().filesDir
                                        .toString() + coCoinUtil.LOGO_NAME))
                                    .setCallback { e, file ->
                                        profileImage!!.setImageBitmap(BitmapFactory.decodeFile(
                                            CoCoinApplication.getAppContext().filesDir
                                                .toString() + coCoinUtil.LOGO_NAME))
                                    }
                            }

                            override fun onError(code: Int, msg: String) {
                                // the picture is lost
                                if (BuildConfig.DEBUG) Timber.d("Can't find the old logo in server.")
                            }
                        })
                } else {
                    // the user logo is in the storage
                    val b = BitmapFactory.decodeStream(FileInputStream(logoFile))
                    profileImage!!.setImageBitmap(b)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            // use the default logo
            profileImage!!.setImageResource(R.drawable.default_user_logo)
        }
    }

    private fun setIconEnable(icon: MaterialIconView?, enable: Boolean) {
        if (enable) icon!!.setColor(this@DateRangeReportFragment.resources.getColor(R.color.my_blue)) else icon!!.setColor(
            this@DateRangeReportFragment.resources.getColor(R.color.my_gray))
    }

    companion object {
        private const val FILE_SEPARATOR = "/"
        private val FILE_PATH = Environment.getExternalStorageDirectory()
            .toString() + FILE_SEPARATOR + "CoCoin" + FILE_SEPARATOR
        private val FILE_NAME = FILE_PATH + "CoCoin Database.db"
    }
}