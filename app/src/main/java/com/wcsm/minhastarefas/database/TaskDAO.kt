package com.wcsm.minhastarefas.database

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.wcsm.minhastarefas.model.Task

class TaskDAO(context: Context): ITaskDAO {

    private val writing = DatabaseHelper(context).writableDatabase
    private val reading = DatabaseHelper(context).readableDatabase
    override fun save(task: Task): Boolean {
        val content = ContentValues()
        content.put(DatabaseHelper.COL_TASK_TITLE, task.title)
        content.put(DatabaseHelper.COL_TASK_DESCRIPTION, task.description)
        content.put(DatabaseHelper.COL_TASK_CREATED_AT, task.createdAt)
        content.put(DatabaseHelper.COL_TASK_DUE_DATE, task.dueDate)
        content.put(DatabaseHelper.COL_ALLOW_NOTIFICATION, task.allowNotification)
        content.put(DatabaseHelper.COL_TASK_COMPLETED, task.completed)

        try {
            writing.insert(
                DatabaseHelper.TABLE_NAME,
                null,
                content
            )
            Log.i("info_db", "TASK saved successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("info_db", "Error saving TASK")
            return false
        }

        return true
    }

    override fun update(task: Task): Boolean {
        val args = arrayOf(task.id.toString())
        val content = ContentValues()
        content.put(DatabaseHelper.COL_TASK_TITLE, task.title)
        content.put(DatabaseHelper.COL_TASK_DESCRIPTION, task.description)
        content.put(DatabaseHelper.COL_TASK_CREATED_AT, task.createdAt)
        content.put(DatabaseHelper.COL_TASK_DUE_DATE, task.dueDate)
        content.put(DatabaseHelper.COL_ALLOW_NOTIFICATION, task.allowNotification)
        content.put(DatabaseHelper.COL_TASK_COMPLETED, task.completed)

        try {
            writing.update(
                DatabaseHelper.TABLE_NAME,
                content,
                "${DatabaseHelper.COL_TASK_ID} = ?",
                args
            )
            Log.i("info_db", "TASK updated successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("info_db", "Error updating TASK")
            return false
        }

        return true
    }

    override fun delete(taskId: Int): Boolean {
        val args = arrayOf(taskId.toString())
        try {
            writing.delete(
                DatabaseHelper.TABLE_NAME,
                "${DatabaseHelper.COL_TASK_ID} = ?",
                args
            )
            Log.i("info_db", "TASK deleted successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("info_db", "Error deleting TASK")
            return false
        }

        return true
    }

    override fun listTasks(): List<Task> {
        val taskList = mutableListOf<Task>()

        val sql = "SELECT" +
                " ${DatabaseHelper.COL_TASK_ID}, ${DatabaseHelper.COL_TASK_TITLE}," +
                " ${DatabaseHelper.COL_TASK_DESCRIPTION}," +
                " strftime('%d/%m/%Y - %H:%M', ${DatabaseHelper.COL_TASK_CREATED_AT})" +
                " AS ${DatabaseHelper.COL_TASK_CREATED_AT}," +
                " strftime('%d/%m/%Y - %H:%M', ${DatabaseHelper.COL_TASK_DUE_DATE})" +
                " AS ${DatabaseHelper.COL_TASK_DUE_DATE}," +
                " ${DatabaseHelper.COL_ALLOW_NOTIFICATION}, ${DatabaseHelper.COL_TASK_COMPLETED}" +
                " FROM ${DatabaseHelper.TABLE_NAME};"

        val cursor = reading.rawQuery(sql, null)

        val idIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_ID)
        val titleIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_TITLE)
        val descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_DESCRIPTION)
        val createdAtIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_CREATED_AT)
        val dueDateIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_DUE_DATE)
        val allowNotificationIndex = cursor.getColumnIndex(DatabaseHelper.COL_ALLOW_NOTIFICATION)
        val completedIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_COMPLETED)

        while(cursor.moveToNext()) {
            val id = cursor.getInt(idIndex)
            val title = cursor.getString(titleIndex)
            val description = cursor.getString(descriptionIndex)
            val createdAt = cursor.getString(createdAtIndex) //null
            val dueDate = cursor.getString(dueDateIndex)    //null
            val allowNotification = cursor.getInt(allowNotificationIndex);
            val completed = cursor.getInt(completedIndex);

            Log.i("teste", "$id - $title - $description - $createdAt - $dueDate - $allowNotification - $completed")

            taskList.add(Task(id, title, description, createdAt, dueDate, allowNotification, completed))
        }

        return taskList
    }
}