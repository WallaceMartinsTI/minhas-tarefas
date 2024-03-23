package com.wcsm.minhastarefas

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wcsm.minhastarefas.adapter.TaskAdapter
import com.wcsm.minhastarefas.database.TaskDAO
import com.wcsm.minhastarefas.databinding.ActivityMainBinding
import com.wcsm.minhastarefas.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var taskList = emptyList<Task>()
    private var completedTasksList = emptyList<Task>()

    private var pendingIntentsMap = mutableMapOf<Int, PendingIntent>()

    private var taskAdapter: TaskAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        createNotificationChannel()

        with(binding) {
            fabAddTask.setOnClickListener {
                val intent = Intent(applicationContext, AddTaskActivity::class.java)
                intent.putExtra("screen_title", "Adicionar Tarefa")
                intent.putExtra("button_text", "ADICIONAR")
                startActivity(intent)
            }

            fabCompletedTasks.setOnClickListener {
                getCompletedTasks()
                val intent = Intent(applicationContext, CompletedTasksActivity::class.java)
                intent.putParcelableArrayListExtra("tasks", ArrayList(completedTasksList))
                startActivity(intent)
            }

            fabDeleteAllTasks.setOnClickListener {
                deleteAllTasks()
            }

            taskAdapter = TaskAdapter(
                applicationContext,
                {updateTaskList()},
                {taskId -> confirmDelete(taskId)},
                {task -> edit(task)},
                fabCompletedTasks
            )
            rvTasks.adapter = taskAdapter
            rvTasks.addItemDecoration(
                DividerItemDecoration(applicationContext, RecyclerView.VERTICAL)
            )
            rvTasks.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(taskTitle: String, date: Calendar, notificationID: Int) {
        val intent = Intent(applicationContext, Notification::class.java)
        intent.putExtra("taskTitle", taskTitle)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        pendingIntentsMap[notificationID] = pendingIntent

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime(date)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }

    private fun getTime(date: Calendar): Long {
        date.add(Calendar.HOUR_OF_DAY, -1)
        return date.timeInMillis
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel() {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val name = "Tarefa Pendente"
        val description = "Notificações de tarefas pendentes"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = description
        channel.setSound(defaultSoundUri, null)
        channel.lightColor = Color.WHITE
        channel.lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun edit(task: Task) {
        val intent = Intent(applicationContext, AddTaskActivity::class.java)
        intent.putExtra("task", task)
        intent.putExtra("screen_title", "Editar Tarefa")
        intent.putExtra("button_text", "ATUALIZAR")
        startActivity(intent)
    }

    private fun deleteAllTasks() {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("Confirmar exclusão de TODAS AS TAREFAS")
        alertBuilder.setMessage("Deseja realmente excluir todas as tarefa?")
        alertBuilder.setPositiveButton("Sim") {_, _ ->
            var deletingErrors = 0
            if(taskList.isNotEmpty()) {
                val taskDAO = TaskDAO(this)
                taskList.forEach {
                    if(!taskDAO.delete(it.id)) {
                        deletingErrors++
                    }
                }
                if(deletingErrors > 0) {
                    Toast.makeText(this, "Erro ao remover todas as tarefas", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Todas as tarefas foram removidas.", Toast.LENGTH_SHORT).show()
                    updateTaskList()
                }
            } else {
                Toast.makeText(this, "Não há tarefas para serem removidas.", Toast.LENGTH_SHORT).show()
            }
        }
        alertBuilder.setNegativeButton("Não") {_, _ ->}
        alertBuilder.create().show()
    }

    private fun confirmDelete(id: Int) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("Confirmar exclusão")
        alertBuilder.setMessage("Deseja realmente excluir a tarefa?")
        alertBuilder.setPositiveButton("Sim") {_, _ ->
            val taskDAO = TaskDAO(this)
            if(taskDAO.delete(id)) {
                updateTaskList()
                Toast.makeText(this, "Tarefa removida", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao remover tarefa", Toast.LENGTH_SHORT).show()
            }
        }
        alertBuilder.setNegativeButton("Não") {_, _ ->}
        alertBuilder.create().show()
    }

    private fun getCompletedTasks() {
        val taskDAO = TaskDAO(this)
        val tasks = taskDAO.listTasks()
        var completedTasks = mutableListOf<Task>()
        tasks.forEach {
            if(it.completed > 0) {
                completedTasks.add(it)
            }
        }
        completedTasksList = completedTasks
    }

    private fun updateTaskList() {
        val pendingTasks = mutableListOf<Task>()

        val taskDAO = TaskDAO(this)
        taskList = taskDAO.listTasks()

        taskList.forEach {
            if(it.completed == 0) {
                pendingTasks.add(it)

                val date = convertDueDateToCalendar(it.dueDate)
                if(it.allowNotification > 0) {
                    if(it.notified == 0) {
                        val notifiedTask = Task(it.id, it.title, it.description, convertToSQLiteFormat(it.createdAt), convertToSQLiteFormat(it.updatedAt), convertToSQLiteFormat(it.dueDate), it.allowNotification, 1, 0)
                        val taskDAO = TaskDAO(this)

                        if(taskDAO.update(notifiedTask)) {
                            scheduleNotification(it.title, date, it.id)
                        }
                    }
                } else { // Turn off notification when allowNotification is 0 (false)
                    pendingIntentsMap[it.id]?.let { pendingIntent ->
                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.cancel(pendingIntent)
                        pendingIntentsMap.remove(it.id)
                    }
                }
            } else { // Turn off notification when task is completed
                pendingIntentsMap[it.id]?.let { pendingIntent ->
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(pendingIntent)
                    pendingIntentsMap.remove(it.id)
                }
            }
        }

        val sortedList = pendingTasks.sortedByDescending { it.updatedAt }
        taskAdapter?.addList(sortedList)
    }

    private fun convertDueDateToCalendar(dateTimeString: String) : Calendar {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm")
        val date = dateFormat.parse(dateTimeString)
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    private fun convertToSQLiteFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val date = inputFormat.parse(inputDate) ?: Date()
        return outputFormat.format(date)
    }

    override fun onStart() {
        super.onStart()
        taskList.forEach {
            Log.i("TAREFAS_VER_O_ID", "$it \n")
        }
        updateTaskList()
    }
}