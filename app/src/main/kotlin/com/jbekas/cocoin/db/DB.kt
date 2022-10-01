package com.jbekas.cocoin.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.jbekas.cocoin.BuildConfig
import com.jbekas.cocoin.model.CoCoinRecord
import com.jbekas.cocoin.model.Tag
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat

class DB private constructor(context: Context) {
    private val sqliteDatabase: SQLiteDatabase
    private val dbHelper: DBHelper

    init {
        dbHelper = DBHelper(context, DB_NAME_STRING, null, VERSION)
        sqliteDatabase = dbHelper.writableDatabase
    }

    fun loadTags(): List<Tag> {
        val tags = mutableListOf<Tag>()

        sqliteDatabase
            .query(TAG_DB_NAME_STRING, null, null, null, null, null, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val tag = Tag()
                        tag.id = cursor.getInt(cursor.getColumnIndex("ID")) - 1
                        tag.name = cursor.getString(cursor.getColumnIndex("NAME"))
                        tag.weight = cursor.getInt(cursor.getColumnIndex("WEIGHT"))
                        tags.add(tag)
                    } while (cursor.moveToNext())
                }
            }

//        if (BuildConfig.DEBUG) {
//            tags.forEach { println("Loaded $it}") }
//        }

        return tags
    }

    fun loadRecords(): List<CoCoinRecord> {
        val records = mutableListOf<CoCoinRecord>()

        sqliteDatabase
            .query(RECORD_DB_NAME_STRING, null, null, null, null, null, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val coCoinRecord = CoCoinRecord()
                        coCoinRecord.id = cursor.getLong(cursor.getColumnIndex("ID"))
                        coCoinRecord.setMoney(cursor.getFloat(cursor.getColumnIndex("MONEY")))
                        coCoinRecord.currency = cursor.getString(cursor.getColumnIndex("CURRENCY"))
                        coCoinRecord.tag = cursor.getInt(cursor.getColumnIndex("TAG"))
                        coCoinRecord.setCalendar(cursor.getString(cursor.getColumnIndex("TIME")))
                        coCoinRecord.remark = cursor.getString(cursor.getColumnIndex("REMARK"))
                        coCoinRecord.userId = cursor.getString(cursor.getColumnIndex("USER_ID"))
                        coCoinRecord.localObjectId =
                            cursor.getString(cursor.getColumnIndex("OBJECT_ID"))
                        coCoinRecord.isUploaded =
                            cursor.getInt(cursor.getColumnIndex("IS_UPLOADED")) != 0
                        records.add(coCoinRecord)
                        RecordManager.SUM += coCoinRecord.money.toInt()
                    } while (cursor.moveToNext())
                }
            }

//        if (BuildConfig.DEBUG) {
//            records.forEach { println("Loaded $it}") }
//        }

        return records
    }

    // return the row ID of the newly inserted row, or -1 if an error occurred
    fun saveRecord(coCoinRecord: CoCoinRecord): Long {
        val values = ContentValues()
        values.put("MONEY", coCoinRecord.money)
        values.put("CURRENCY", coCoinRecord.currency)
        values.put("TAG", coCoinRecord.tag)
        values.put("TIME", SimpleDateFormat("yyyy-MM-dd HH:mm")
            .format(coCoinRecord.calendar.time))
        values.put("REMARK", coCoinRecord.remark)
        values.put("USER_ID", coCoinRecord.userId)
        values.put("OBJECT_ID", coCoinRecord.localObjectId)
        values.put("IS_UPLOADED", if (coCoinRecord.isUploaded == false) 0 else 1)
        val insertId = sqliteDatabase.insert(RECORD_DB_NAME_STRING, null, values)
        coCoinRecord.id = insertId
        if (BuildConfig.DEBUG) Timber.d("db.saveRecord $coCoinRecord S")
        return insertId
    }

    // return the row ID of the newly inserted row, or -1 if an error occurred
    fun saveTag(tag: Tag): Int {
        val values = ContentValues()
        values.put("NAME", tag.name)
        values.put("WEIGHT", tag.weight)
        val insertId = sqliteDatabase.insert(TAG_DB_NAME_STRING, null, values).toInt()
        tag.id = insertId
        if (BuildConfig.DEBUG) Timber.d("db.saveTag $tag S")
        return insertId - 1
    }

    // return the id of the record deleted
    fun deleteRecord(id: Long): Long {
        val deletedNumber = sqliteDatabase.delete(RECORD_DB_NAME_STRING,
            "ID = ?", arrayOf(id.toString() + "")).toLong()
        Timber.d("db.deleteRecord id = $id S")
        Timber.d("db.deleteRecord number = $deletedNumber S")
        return id
    }

    // return the id of the tag deleted
    fun deleteTag(id: Int): Int {
        val deletedNumber = sqliteDatabase.delete(TAG_DB_NAME_STRING,
            "ID = ?", arrayOf((id + 1).toString() + ""))
        if (BuildConfig.DEBUG) Timber.d("db.deleteTag id = $id S")
        if (BuildConfig.DEBUG) Timber.d("db.deleteTag number = $deletedNumber S")
        return id
    }

    // return the id of the coCoinRecord update
    fun updateRecord(coCoinRecord: CoCoinRecord): Long {
        val values = ContentValues()
        values.put("ID", coCoinRecord.id)
        values.put("MONEY", coCoinRecord.money)
        values.put("CURRENCY", coCoinRecord.currency)
        values.put("TAG", coCoinRecord.tag)
        values.put("TIME", SimpleDateFormat("yyyy-MM-dd HH:mm")
            .format(coCoinRecord.calendar.time))
        values.put("REMARK", coCoinRecord.remark)
        values.put("USER_ID", coCoinRecord.userId)
        values.put("OBJECT_ID", coCoinRecord.localObjectId)
        values.put("IS_UPLOADED", if (coCoinRecord.isUploaded == false) 0 else 1)
        sqliteDatabase.update(RECORD_DB_NAME_STRING, values,
            "ID = ?", arrayOf(coCoinRecord.id.toString() + ""))
        if (BuildConfig.DEBUG) Timber.d("db.updateRecord $coCoinRecord S")
        return coCoinRecord.id
    }

    // return the id of the tag update
    fun updateTag(tag: Tag): Int {
        val values = ContentValues()
        values.put("NAME", tag.name)
        values.put("WEIGHT", tag.weight)
        sqliteDatabase.update(TAG_DB_NAME_STRING, values,
            "ID = ?", arrayOf((tag.id + 1).toString() + ""))
        if (BuildConfig.DEBUG) Timber.d("db.updateTag $tag S")
        return tag.id
    }

    // delete all the records
    fun deleteAllRecords(): Int {
        val deleteNum = sqliteDatabase.delete(RECORD_DB_NAME_STRING, null, null)
        Timber.d("db.deleteAllRecords $deleteNum S")
        return deleteNum
    }

    companion object {
        const val DB_NAME_STRING = "CoCoin Database.db"
        const val RECORD_DB_NAME_STRING = "Record"
        const val TAG_DB_NAME_STRING = "Tag"
        const val VERSION = 1
        private var db: DB? = null

        @Synchronized
        @Throws(IOException::class)
        fun getInstance(context: Context): DB? {
            if (db == null) db = DB(context)
            return db
        }
    }
}