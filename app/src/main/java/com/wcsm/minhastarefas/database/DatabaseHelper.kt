package com.wcsm.minhastarefas.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE, null, VERSION) {
    companion object {
        const val DATABASE = "Tasks.db"
        const val VERSION = 1
        const val TABLE_NAME = "tasks"
        const val COL_TASK_ID = "task_id"
        const val COL_TASK_TITLE = "title"
        const val COL_TASK_DESCRIPTION = "description"
        const val COL_TASK_CREATED_AT = "created_at"
        const val COL_TASK_UPDATED_AT = "updated_at"
        const val COL_TASK_DUE_DATE = "due_date"
        const val COL_ALLOW_NOTIFICATION = "allow_notification"
        const val COL_TASK_COMPLETED = "completed"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val sql = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                " $COL_TASK_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " $COL_TASK_TITLE VARCHAR(50) NOT NULL," +
                " $COL_TASK_DESCRIPTION VARCHAR(170)," +
                " $COL_TASK_CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " $COL_TASK_UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " $COL_TASK_DUE_DATE DATETIME NOT NULL," +
                " $COL_ALLOW_NOTIFICATION INTEGER NOT NULL DEFAULT 0," +
                " $COL_TASK_COMPLETED INTEGER NOT NULL DEFAULT 0" +
                ");"

        try {
            db?.execSQL(sql)
            Log.i("info_db", "Created TASKS table successful")
        } catch (e: Exception) {
            Log.i("info_db", "Error creating TASK table")
        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }
}