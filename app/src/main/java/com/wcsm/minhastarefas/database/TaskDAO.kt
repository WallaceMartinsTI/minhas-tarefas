package com.wcsm.minhastarefas.database

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.wcsm.minhastarefas.model.Task

class TaskDAO(context: Context): ITaskDAO {

    private val writting = DatabaseHelper(context).writableDatabase
    private val reading = DatabaseHelper(context).readableDatabase
    override fun save(task: Task): Boolean {
        val content = ContentValues()
        content.put(DatabaseHelper.COL_TASK_TITLE, task.title)
        content.put(DatabaseHelper.COL_TASK_DESCRIPTION, task.description)
        content.put(DatabaseHelper.COL_TASK_CREATED_AT, task.createdAt)
        content.put(DatabaseHelper.COL_TASK_DUE_DATE, task.dueDate)
        content.put(DatabaseHelper.COL_TASK_COMPLETED, task.completed)

        try {
            writting.insert(
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
        content.put(DatabaseHelper.COL_TASK_COMPLETED, task.completed)

        try {
            writting.update(
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
            writting.delete(
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
                " ${DatabaseHelper.COL_TASK_ID} , ${DatabaseHelper.COL_TASK_TITLE}," +
                " ${DatabaseHelper.COL_TASK_DESCRIPTION}," +
                " strftime('%d/%m/%Y - %H:%M', ${DatabaseHelper.COL_TASK_CREATED_AT})" +
                " AS ${DatabaseHelper.COL_TASK_CREATED_AT}," +
                " strftime('%d/%m/%Y - %H:%M', ${DatabaseHelper.COL_TASK_DUE_DATE})" +
                " AS ${DatabaseHelper.COL_TASK_DUE_DATE}, completed" +
                " FROM ${DatabaseHelper.TABLE_NAME};"

        val cursor = reading.rawQuery(sql, null)

        val idIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_ID)
        val titleIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_TITLE)
        val descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_DESCRIPTION)
        val createdAtIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_CREATED_AT)
        val dueDateIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_DUE_DATE)
        val completedIndex = cursor.getColumnIndex(DatabaseHelper.COL_TASK_COMPLETED)

        while(cursor.moveToNext()) {
            val id = cursor.getInt(idIndex)
            val title = cursor.getString(titleIndex)
            val description = cursor.getString(descriptionIndex)
            val createdAt = cursor.getString(createdAtIndex)
            val dueDate = cursor.getString(dueDateIndex)
            val completed = cursor.getInt(completedIndex) > 0;

            taskList.add(Task(id, title, description, createdAt, dueDate, completed))
        }

        return taskList
    }
}