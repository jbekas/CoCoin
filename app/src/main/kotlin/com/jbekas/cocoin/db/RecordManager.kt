package com.jbekas.cocoin.db

import android.content.Context
import android.widget.Toast
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import com.jbekas.cocoin.BuildConfig
import com.jbekas.cocoin.R
import com.jbekas.cocoin.activity.CoCoinApplication
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.Tag
import com.jbekas.cocoin.model.User
import com.jbekas.cocoin.util.CoCoinUtil
import com.jbekas.cocoin.util.Constants
import com.jbekas.cocoin.util.getThisMonthLeftRange
import timber.log.Timber
import java.io.IOException
import java.util.*

class RecordManager private constructor(context: Context) {
    private val RANDOM_DATA_NUMBER_ON_EACH_DAY = 3
    private val RANDOM_DATA_EXPENSE_ON_EACH_DAY = 30


    // constructor//////////////////////////////////////////////////////////////////////////////////////
    init {
        try {
            db = DB.getInstance(context)
            if (BuildConfig.DEBUG) {
                Timber.d("db.getInstance(context)")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (FIRST_TIME) {
// if the app starts firstly, create tags///////////////////////////////////////////////////////////
            val preferences = context.getSharedPreferences("Values", Context.MODE_PRIVATE)
            if (preferences.getBoolean("FIRST_TIME", true)) {
                createTags()
                val editor = context.getSharedPreferences("Values", Context.MODE_PRIVATE).edit()
                editor.putBoolean("FIRST_TIME", false)
                editor.commit()
            }
        }

        TAGS.clear()
        TAGS.addAll(db!!.loadTags())

        RECORDS.clear()
        RECORDS.addAll(db!!.loadRecords())

        Timber.d("Loaded ${RECORDS.size} records")
        Timber.d("Loaded ${TAGS.size} tags")

        if (RANDOM_DATA) {
            val preferences = context.getSharedPreferences("Values", Context.MODE_PRIVATE)
            if (preferences.getBoolean("RANDOM", false)) {
                // do nothing
                // return
            } else {
                randomDataCreater()
                val editor = context.getSharedPreferences("Values", Context.MODE_PRIVATE).edit()
                editor.putBoolean("RANDOM", true)
                editor.commit()
            }
        }

        // TODO We should not be injecting UI logic into our actual list of tag categories.
        TAGS.add(0, Tag(-1, "Sum Histogram", -4))
        TAGS.add(0, Tag(-2, "Sum Pie", -5))
        for (tag in TAGS) {
            TAG_NAMES[tag.id] = tag.name
        }
        sortTAGS()
    }

    private fun createTags() {
        saveTag(Tag(-1, "Meal", -1))
        saveTag(Tag(-1, "Clothing & Footwear", 1))
        saveTag(Tag(-1, "Home", 2))
        saveTag(Tag(-1, "Traffic", 3))
        saveTag(Tag(-1, "Vehicle Maintenance", 4))
        saveTag(Tag(-1, "Book", 5))
        saveTag(Tag(-1, "Hobby", 6))
        saveTag(Tag(-1, "Internet", 7))
        saveTag(Tag(-1, "Friend", 8))
        saveTag(Tag(-1, "Education", 9))
        saveTag(Tag(-1, "Entertainment", 10))
        saveTag(Tag(-1, "Medical", 11))
        saveTag(Tag(-1, "Insurance", 12))
        saveTag(Tag(-1, "Donation", 13))
        saveTag(Tag(-1, "Sport", 14))
        saveTag(Tag(-1, "Snack", 15))
        saveTag(Tag(-1, "Music", 16))
        saveTag(Tag(-1, "Fund", 17))
        saveTag(Tag(-1, "Drink", 18))
        saveTag(Tag(-1, "Fruit", 19))
        saveTag(Tag(-1, "Film", 20))
        saveTag(Tag(-1, "Baby", 21))
        saveTag(Tag(-1, "Partner", 22))
        saveTag(Tag(-1, "Housing Loan", 23))
        saveTag(Tag(-1, "Pet", 24))
        saveTag(Tag(-1, "Telephone Bill", 25))
        saveTag(Tag(-1, "Travel", 26))
        saveTag(Tag(-1, "Lunch", -2))
        saveTag(Tag(-1, "Breakfast", -3))
        saveTag(Tag(-1, "MidnightSnack", 0))
        sortTAGS()
    }

    private fun randomDataCreater() {
        val random = Random()
        val createdCoCoinRecords: MutableList<CoCoinRecord> = ArrayList()
        val now = Calendar.getInstance()
        val c = Calendar.getInstance()
        c[2015, 0, 1, 0, 0] = 0
        c.add(Calendar.SECOND, 1)
        while (c.before(now)) {
            for (i in 0 until RANDOM_DATA_NUMBER_ON_EACH_DAY) {
                val r = c.clone() as Calendar
                val hour = random.nextInt(24)
                val minute = random.nextInt(60)
                val second = random.nextInt(60)
                r[Calendar.HOUR_OF_DAY] = hour
                r[Calendar.MINUTE] = minute
                r[Calendar.SECOND] = second
                r.add(Calendar.SECOND, 0)
                val tag = random.nextInt(TAGS.size - 1)
                val expense = random.nextInt(RANDOM_DATA_EXPENSE_ON_EACH_DAY) + 1
                val coCoinRecord = CoCoinRecord()
                coCoinRecord.calendar = r
                coCoinRecord.setMoney(expense.toFloat())
                coCoinRecord.tag = tag
                coCoinRecord.currency = Constants.USD
                coCoinRecord.remark = "Sample Data"
                createdCoCoinRecords.add(coCoinRecord)
            }
            c.add(Calendar.DATE, 1)
        }
        createdCoCoinRecords.sortWith(Comparator { lhs, rhs ->
            if (lhs.calendar.before(rhs.calendar)) {
                -1
            } else if (lhs.calendar.after(rhs.calendar)) {
                1
            } else {
                0
            }
        })
        for (coCoinRecord in createdCoCoinRecords) {
            saveRecord(coCoinRecord)
        }
    }

    companion object {
        private var instance: RecordManager? = null

        private var db: DB? = null

        // the selected values in list activity
        @JvmField
        var SELECTED_SUM: Double = 0.toDouble()
        @JvmField
        var SELECTED_RECORDS = mutableListOf<CoCoinRecord>()
        @JvmField
        var SUM: Int = 0
        @JvmField
        val RECORDS = mutableListOf<CoCoinRecord>()
        @JvmField
        val TAGS = mutableListOf<Tag>()
        val TAG_NAMES = mutableMapOf<Int, String>()
        var RANDOM_DATA = true
        private const val FIRST_TIME = true
        var SAVE_TAG_ERROR_DATABASE_ERROR = -1
        var SAVE_TAG_ERROR_DUPLICATED_NAME = -2
        var DELETE_TAG_ERROR_DATABASE_ERROR = -1
        var DELETE_TAG_ERROR_TAG_REFERENCE = -2

        // getInstance//////////////////////////////////////////////////////////////////////////////////////
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): RecordManager {
            if (instance == null) {
                instance = RecordManager(context)
            }
            return instance!!
        }

        // saveRecord///////////////////////////////////////////////////////////////////////////////////////
        fun saveRecord(coCoinRecord: CoCoinRecord): Long {
            var insertId: Long = -1
            // this is a new coCoinRecord, which is not uploaded
            coCoinRecord.isUploaded = false
            //        User user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User.class);
//        if (user != null) coCoinRecord.setUserId(user.getObjectId());
//        else coCoinRecord.setUserId(null);
            insertId = db!!.saveRecord(coCoinRecord)
            if (insertId == -1L) {
                Timber.d("recordManager.saveRecord: Save the above coCoinRecord FAIL!")
                //            SuperToast.getInstance()
//                    .showToast(R.string.save_failed_locale, SuperToast.Background.RED);
//                Toast.makeText(CoCoinApplication.getAppContext(),
//                    R.string.save_failed_locale,
//                    Toast.LENGTH_SHORT).show()
            } else {
                Timber.d("recordManager.saveRecord: Save the above coCoinRecord SUCCESSFULLY!")
                RECORDS.add(coCoinRecord)
                SUM += coCoinRecord.money.toInt()
                //            if (user != null) {
//                // already login
//                coCoinRecord.save(CoCoinApplication.getAppContext(), new SaveListener() {
//                    @Override
//                    public void onSuccess() {
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Timber.d( "recordManager.saveRecord: Save online " + coCoinRecord.toString() + " S");
//                        coCoinRecord.setIsUploaded(true);
//                        coCoinRecord.setLocalObjectId(coCoinRecord.getObjectId());
//                        db.updateRecord(coCoinRecord);
//                        ToastService.getInstance()
//                                .showToast(R.string.save_successfully_online, SuperToast.Background.BLUE);
//                    }
//                    @Override
//                    public void onFailure(int code, String msg) {
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Timber.d( "recordManager.saveRecord: Save online " + coCoinRecord.toString() + " F");
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Timber.d( "recordManager.saveRecord: Save online msg: " + msg + " code " + code);
//                        ToastService.getInstance()
//                                .showToast(R.string.save_failed_online, SuperToast.Background.RED);
//                    }
//                });
//            } else {
//                ToastService.getInstance()
//                        .showToast(R.string.save_successfully_locale, SuperToast.Background.BLUE);
//            }
//            ToastService.getInstance()
//                    .showToast(R.string.save_successfully_locale, SuperToast.Background.BLUE);
//                Toast.makeText(CoCoinApplication.getAppContext(),
//                    R.string.save_successfully_locale,
//                    Toast.LENGTH_SHORT).show()
            }
            return insertId
        }

        // save tag/////////////////////////////////////////////////////////////////////////////////////////
        fun saveTag(tag: Tag): Int {
            var insertId = -1
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Timber.d( "recordManager.saveTag: $tag")
            }
            var duplicatedName = false
            for (t in TAGS!!) {
                if (t.name == tag.name) {
                    duplicatedName = true
                    break
                }
            }
            if (duplicatedName) {
                return SAVE_TAG_ERROR_DUPLICATED_NAME
            }
            insertId = db!!.saveTag(tag)
            if (insertId == -1) {
                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Timber.d( "Save the above tag FAIL!")
                    return SAVE_TAG_ERROR_DATABASE_ERROR
                }
            } else {
                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Timber.d( "Save the above tag SUCCESSFULLY!")
                }
                TAGS!!.add(tag)
                TAG_NAMES!![tag.id] = tag.name
                sortTAGS()
            }
            return insertId
        }

        // delete a coCoinRecord//////////////////////////////////////////////////////////////////////////////////
        @JvmStatic
        fun deleteRecord(coCoinRecord: CoCoinRecord, deleteInList: Boolean): Long {
            val deletedNumber = db!!.deleteRecord(coCoinRecord.id)
            if (deletedNumber > 0) {
                Timber.d("recordManager.deleteRecord: Delete $coCoinRecord S")
                // update RECORDS list and SUM
                SUM -= coCoinRecord.money.toInt()
                if (deleteInList) {
//                    val size = RECORDS.size
                    for (i in RECORDS.indices) {
                        if (RECORDS[i].id == coCoinRecord.id) {
                            RECORDS.removeAt(i)
                            Timber.d("recordManager.deleteRecord: Delete in RECORD $coCoinRecord S")
                            break
                        }
                    }
                }
            } else {
                Timber.d("recordManager.deleteRecord: Delete $coCoinRecord F")
            }
            return coCoinRecord.id
        }

        fun deleteTag(id: Int): Int {
            var deletedId = -1
            if (BuildConfig.DEBUG) Timber.d(
                "Manager: Delete tag: Tag(id = $id, deletedId = $deletedId)")
            var tagReference = false
            for (coCoinRecord in RECORDS!!) {
                if (coCoinRecord.tag == id) {
                    tagReference = true
                    break
                }
            }
            if (tagReference) {
                return DELETE_TAG_ERROR_TAG_REFERENCE
            }
            deletedId = db!!.deleteTag(id)
            if (deletedId == -1) {
                if (BuildConfig.DEBUG) Timber.d( "Delete the above tag FAIL!")
                return DELETE_TAG_ERROR_DATABASE_ERROR
            } else {
                if (BuildConfig.DEBUG) Timber.d( "Delete the above tag SUCCESSFULLY!")
                for (tag in TAGS!!) {
                    if (tag.id == deletedId) {
                        TAGS!!.remove(tag)
                        break
                    }
                }
                TAG_NAMES!!.remove(id)
                sortTAGS()
            }
            return deletedId
        }

        private var p = 0
        @JvmStatic
        fun updateRecord(coCoinRecord: CoCoinRecord): Long {
            val updateNumber = db!!.updateRecord(coCoinRecord)
            if (updateNumber <= 0) {
                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Timber.d(
                        "recordManager.updateRecord $coCoinRecord F")
                }
                //            ToastService.getInstance().showToast(R.string.update_failed_locale, SuperToast.Background.RED);
                Toast.makeText(CoCoinApplication.getAppContext(),
                    R.string.update_failed_locale,
                    Toast.LENGTH_SHORT).show()
            } else {
                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Timber.d(
                        "recordManager.updateRecord $coCoinRecord S")
                }
                p = RECORDS.size - 1
                while (p >= 0) {
                    if (RECORDS[p].id == coCoinRecord.id) {
                        SUM -= RECORDS[p].money.toInt()
                        SUM += coCoinRecord.money.toInt()
                        RECORDS[p].set(coCoinRecord)
                        break
                    }
                    p--
                }
                coCoinRecord.isUploaded = false
                //            User user = BmobUser
//                    .getCurrentUser(CoCoinApplication.getAppContext(), User.class);
//            if (user != null) {
//                // already login
//                if (coCoinRecord.getLocalObjectId() != null) {
//                    // this coCoinRecord has been push to the server
//                    coCoinRecord.setUserId(user.getObjectId());
//                    coCoinRecord.update(CoCoinApplication.getAppContext(),
//                            coCoinRecord.getLocalObjectId(), new UpdateListener() {
//                                @Override
//                                public void onSuccess() {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Timber.d( "recordManager.updateRecord update online " + coCoinRecord.toString() + " S");
//                                    }
//                                    coCoinRecord.setIsUploaded(true);
//                                    RECORDS.get(p).setIsUploaded(true);
//                                    db.updateRecord(coCoinRecord);
//                                    ToastService.getInstance().showToast(R.string.update_successfully_online, SuperToast.Background.BLUE);
//                                }
//
//                                @Override
//                                public void onFailure(int code, String msg) {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Timber.d( "recordManager.updateRecord update online " + coCoinRecord.toString() + " F");
//                                    }
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Timber.d( "recordManager.updateRecord update online code" + code + " msg " + msg );
//                                    }
//                                    ToastService.getInstance().showToast(R.string.update_failed_online, SuperToast.Background.RED);
//                                }
//                            });
//                } else {
//                    // this coCoinRecord has not been push to the server
//                    coCoinRecord.setUserId(user.getObjectId());
//                    coCoinRecord.save(CoCoinApplication.getAppContext(), new SaveListener() {
//                                @Override
//                                public void onSuccess() {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Timber.d( "recordManager.updateRecord save online " + coCoinRecord.toString() + " S");
//                                    }
//                                    coCoinRecord.setIsUploaded(true);
//                                    coCoinRecord.setLocalObjectId(coCoinRecord.getObjectId());
//                                    RECORDS.get(p).setIsUploaded(true);
//                                    RECORDS.get(p).setLocalObjectId(coCoinRecord.getObjectId());
//                                    db.updateRecord(coCoinRecord);
//                                    ToastService.getInstance().showToast(R.string.update_successfully_online, SuperToast.Background.BLUE);
//                                }
//                                @Override
//                                public void onFailure(int code, String msg) {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Timber.d( "recordManager.updateRecord save online " + coCoinRecord.toString() + " F");
//                                    }
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Timber.d( "recordManager.updateRecord save online code" + code + " msg " + msg );
//                                    }
//                                    ToastService.getInstance().showToast(R.string.update_failed_online, SuperToast.Background.RED);
//                                }
//                            });
//                }
//            } else {
//                // has not login
//                db.updateRecord(coCoinRecord);
//                ToastService.getInstance().showToast(R.string.update_successfully_locale, SuperToast.Background.BLUE);
//            }
                db!!.updateRecord(coCoinRecord)
                //            ToastService.getInstance().showToast(R.string.update_successfully_locale, SuperToast.Background.BLUE);
                Toast.makeText(CoCoinApplication.getAppContext(),
                    R.string.update_successfully_locale,
                    Toast.LENGTH_SHORT).show()
            }
            return updateNumber
        }

        // update the records changed to server/////////////////////////////////////////////////////////////
        private var isLastOne = false
        fun updateOldRecordsToServer(): Long {
            var counter: Long = 0
            val user = BmobUser
                .getCurrentUser(CoCoinApplication.getAppContext(), User::class.java)
            if (user != null) {
// already login////////////////////////////////////////////////////////////////////////////////////
                isLastOne = false
                for (i in RECORDS!!.indices) {
                    if (i == RECORDS!!.size - 1) isLastOne = true
                    val coCoinRecord = RECORDS!![i]
                    if (!coCoinRecord.isUploaded) {
// has been changed/////////////////////////////////////////////////////////////////////////////////
                        if (coCoinRecord.localObjectId != null) {
// there is an old coCoinRecord in server, we should update this coCoinRecord///////////////////////////////////
                            coCoinRecord.userId = user.objectId
                            coCoinRecord.update(CoCoinApplication.getAppContext(),
                                coCoinRecord.localObjectId, object : UpdateListener() {
                                    override fun onSuccess() {
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Timber.d(
                                                "recordManager.updateOldRecordsToServer update online $coCoinRecord S")
                                        }
                                        coCoinRecord.isUploaded = true
                                        coCoinRecord.localObjectId = coCoinRecord.objectId
                                        db!!.updateRecord(coCoinRecord)
                                        // after updating, get the old records from server//////////////////////////////////////////////////
                                        if (isLastOne) recordsFromServer
                                    }

                                    override fun onFailure(code: Int, msg: String) {
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Timber.d(
                                                "recordManager.updateOldRecordsToServer update online $coCoinRecord F")
                                        }
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Timber.d(
                                                "recordManager.updateOldRecordsToServer update online code$code msg $msg")
                                        }
                                    }
                                })
                        } else {
                            counter++
                            coCoinRecord.userId = user.objectId
                            coCoinRecord.save(CoCoinApplication.getAppContext(),
                                object : SaveListener() {
                                    override fun onSuccess() {
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Timber.d(
                                                "recordManager.updateOldRecordsToServer save online $coCoinRecord S")
                                        }
                                        coCoinRecord.isUploaded = true
                                        coCoinRecord.localObjectId = coCoinRecord.objectId
                                        db!!.updateRecord(coCoinRecord)
                                        // after updating, get the old records from server//////////////////////////////////////////////////
                                        if (isLastOne) recordsFromServer
                                    }

                                    override fun onFailure(code: Int, msg: String) {
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Timber.d(
                                                "recordManager.updateOldRecordsToServer save online $coCoinRecord F")
                                        }
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Timber.d(
                                                "recordManager.updateOldRecordsToServer save online code$code msg $msg")
                                        }
                                    }
                                })
                        }
                    }
                }
            } else {
            }
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Timber.d(
                    "recordManager.updateOldRecordsToServer update $counter records to server.")
            }
            if (RECORDS!!.size == 0) recordsFromServer
            return counter
        }

        @JvmStatic
        fun updateTag(tag: Tag): Long {
            var updateId = -1
            if (BuildConfig.DEBUG) Timber.d(
                "Manager: Update tag: $tag")
            updateId = db!!.updateTag(tag)
            if (updateId == -1) {
                if (BuildConfig.DEBUG) Timber.d( "Update the above tag FAIL!")
            } else {
                if (BuildConfig.DEBUG) Timber.d(
                    "Update the above tag SUCCESSFULLY! - $updateId")
                for (t in TAGS!!) {
                    if (t.id == tag.id) {
                        t.set(tag)
                        break
                    }
                }
                sortTAGS()
            }
            return updateId.toLong()
        }

        //get records from server to local//////////////////////////////////////////////////////////////////
        private var updateNum: Long = 0
        val recordsFromServer: Long
            get() {
                updateNum = 0
                val query = BmobQuery<CoCoinRecord>()
                query.addWhereEqualTo("userId",
                    BmobUser.getCurrentUser(CoCoinApplication.getAppContext(),
                        User::class.java).objectId)
                query.setLimit(Int.MAX_VALUE)
                query.findObjects(CoCoinApplication.getAppContext(),
                    object : FindListener<CoCoinRecord>() {
                        override fun onSuccess(`object`: List<CoCoinRecord>) {
                            if (BuildConfig.DEBUG) {
                                if (BuildConfig.DEBUG) Timber.d(
                                    "recordManager.getRecordsFromServer get " + `object`.size + " records from server")
                            }
                            updateNum = `object`.size.toLong()
                            for (coCoinRecord in `object`) {
                                var exist = false
                                for (i in RECORDS!!.indices.reversed()) {
                                    if (coCoinRecord.objectId == RECORDS!![i].localObjectId) {
                                        exist = true
                                        break
                                    }
                                }
                                if (!exist) {
                                    val newCoCoinRecord = CoCoinRecord()
                                    newCoCoinRecord.set(coCoinRecord)
                                    newCoCoinRecord.id = -1
                                    RECORDS!!.add(newCoCoinRecord)
                                }
                            }
                            Collections.sort(RECORDS) { lhs, rhs ->
                                if (lhs.calendar.before(rhs.calendar)) {
                                    -1
                                } else if (lhs.calendar.after(rhs.calendar)) {
                                    1
                                } else {
                                    0
                                }
                            }
                            db!!.deleteAllRecords()
                            SUM = 0
                            for (i in RECORDS.indices) {
                                RECORDS[i].localObjectId = RECORDS[i].objectId
                                RECORDS[i].isUploaded = true
                                db!!.saveRecord(RECORDS[i])
                                SUM += RECORDS[i].money.toInt()
                            }
                            if (BuildConfig.DEBUG) {
                                if (BuildConfig.DEBUG) Timber.d(
                                    "recordManager.getRecordsFromServer save " + RECORDS.size + " records")
                            }
                        }

                        override fun onError(code: Int, msg: String) {
                            if (BuildConfig.DEBUG) {
                                if (BuildConfig.DEBUG) Timber.d(
                                    "recordManager.getRecordsFromServer error $msg")
                            }
                        }
                    })
                return updateNum
            }
        @JvmStatic
        val currentMonthExpense: Int
            get() {
                val calendar = Calendar.getInstance()
                val left = calendar.getThisMonthLeftRange() //cocoinUtil.GetThisMonthLeftRange(calendar)
                var monthSum = 0
                for (i in RECORDS!!.indices.reversed()) {
                    if (RECORDS!![i].calendar.before(left)) break
                    monthSum += RECORDS!![i].money.toInt()
                }
                return monthSum
            }

        fun queryRecordByTime(c1: Calendar?, c2: Calendar?): List<CoCoinRecord> {
            val list: MutableList<CoCoinRecord> = LinkedList()
            for (coCoinRecord in RECORDS!!) {
                if (coCoinRecord.isInTime(c1, c2)) {
                    list.add(coCoinRecord)
                }
            }
            return list
        }

        fun queryRecordByCurrency(currency: String): List<CoCoinRecord> {
            val list: MutableList<CoCoinRecord> = LinkedList()
            for (coCoinRecord in RECORDS!!) {
                if (coCoinRecord.currency == currency) {
                    list.add(coCoinRecord)
                }
            }
            return list
        }

        fun queryRecordByTag(tag: Int): List<CoCoinRecord> {
            val list: MutableList<CoCoinRecord> = LinkedList()
            for (coCoinRecord in RECORDS!!) {
                if (coCoinRecord.tag == tag) {
                    list.add(coCoinRecord)
                }
            }
            return list
        }

        fun queryRecordByMoney(
            coCoinUtil: CoCoinUtil,
            money1: Double,
            money2: Double,
            currency: String?
        ): List<CoCoinRecord> {
            val list: MutableList<CoCoinRecord> = LinkedList()
            for (coCoinRecord in RECORDS!!) {
                if (coCoinRecord.isInMoney(coCoinUtil, money1, money2, currency)) {
                    list.add(coCoinRecord)
                }
            }
            return list
        }

        fun queryRecordByRemark(coCoinUtil: CoCoinUtil, remark: String?): List<CoCoinRecord> {
            val list: MutableList<CoCoinRecord> = LinkedList()
            for (coCoinRecord in RECORDS!!) {
                if (coCoinUtil.isStringRelation(coCoinRecord.remark, remark)) {
                    list.add(coCoinRecord)
                }
            }
            return list
        }

        // Todo bug here
        private fun sortTAGS() {
            Collections.sort(TAGS) { lhs, rhs ->
                if (lhs.weight != rhs.weight) {
                    Integer.valueOf(lhs.weight).compareTo(rhs.weight)
                } else if (lhs.name != rhs.name) {
                    lhs.name.compareTo(rhs.name)
                } else {
                    Integer.valueOf(lhs.id).compareTo(rhs.id)
                }
            }
        }

        @JvmStatic
        fun getNumberOfTagPages(pageSize: Int): Int {
            return if (TAGS!!.size % pageSize == 0) {
                TAGS!!.size / pageSize
            } else {
                TAGS!!.size / pageSize + 1
            }
        }
    }
}