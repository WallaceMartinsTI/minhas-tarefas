package com.wcsm.minhastarefas.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.wcsm.minhastarefas.database.TaskDAO
import com.wcsm.minhastarefas.databinding.ListItemBinding
import com.wcsm.minhastarefas.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val context: Context,
    val updateTaskList: () -> Unit,
    val onClickDelete: (Int) -> Unit,
    val onClickUpdate: (Task) -> Unit,
    val fabCompletedTasks: ExtendedFloatingActionButton
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var taskList: List<Task> = emptyList()

    fun addList(list: List<Task>) {
        this.taskList = list
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemBinding: ListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root) {
        private val binding: ListItemBinding

        init {
            binding = itemBinding
            Log.i("teste", "$taskList")
        }

        fun bind(task: Task) {
            with(binding) {
                tvTitle.text = task.title
                tvDescription.text = task.description
                tvCreatedAt.text = "Criada em: ${task.createdAt}"
                tvDueDate.text = "Prazo final: ${task.dueDate}"
                swAllowNotification.isChecked = task.allowNotification > 0
                tgCompleted.isChecked = task.completed > 0

                btnDelete.setOnClickListener {
                    onClickDelete(task.id)
                }
                btnEdit.setOnClickListener {
                    onClickUpdate(task)
                }
                tgCompleted.setOnClickListener {
                    val completed = if(tgCompleted.isChecked) 1 else 0
                    val allowNotification = if(swAllowNotification.isChecked) 1 else 0
                    val actualDate = getDateTimeCalendar()
                    val updatedTask = Task(task.id, task.title, task.description, convertToSQLiteFormat(task.createdAt), convertToSQLiteFormat(actualDate), convertToSQLiteFormat(task.dueDate), allowNotification, completed)
                    val taskDAO = TaskDAO(context)

                    if(taskDAO.update(updatedTask)) {
                        Toast.makeText(context, "Tarefa concluída!", Toast.LENGTH_SHORT).show()
                        tgCompleted.isClickable = false
                        fabCompletedTasks.isEnabled = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            updateTaskList()
                            tgCompleted.isClickable = true
                            fabCompletedTasks.isEnabled = true
                        }, 1500)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemTarefaBinding = ListItemBinding.inflate(
            layoutInflater, parent, false
        )
        return TaskViewHolder(itemTarefaBinding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    private fun convertToSQLiteFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val date = inputFormat.parse(inputDate) ?: Date()
        return outputFormat.format(date)
    }

    private fun getDateTimeCalendar(): String {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        return "$day/${month + 1}/$year - $hour:$minute"
    }
}
