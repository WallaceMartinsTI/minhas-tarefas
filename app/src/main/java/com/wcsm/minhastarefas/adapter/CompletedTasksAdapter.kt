package com.wcsm.minhastarefas.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wcsm.minhastarefas.database.TaskDAO
import com.wcsm.minhastarefas.databinding.ListItemBinding
import com.wcsm.minhastarefas.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CompletedTasksAdapter(
    private val context: Context,
    private var completedTasksList: List<Task> = listOf(),
    val updateCompletedTasksList: () -> Unit,
    val onClickDelete: (Int) -> Unit,
    val btnBackToMain: Button
) : RecyclerView.Adapter<CompletedTasksAdapter.CompletedTasksViewHolder>() {

    fun addList(list: List<Task>) {
        this.completedTasksList = list
        notifyDataSetChanged()
    }

    inner class CompletedTasksViewHolder(itemBinding: ListItemBinding):
        RecyclerView.ViewHolder(itemBinding.root) {
        private val binding: ListItemBinding

        init {
            binding = itemBinding
        }

        fun bind(completedTasksList: Task) {
            with(binding) {
                tvTitle.text = completedTasksList.title
                tvDescription.text = completedTasksList.description
                tvCreatedAt.text = "Criada em: ${completedTasksList.createdAt}"
                tvDueDate.text = "Prazo final: ${completedTasksList.dueDate}"
                swAllowNotification.isChecked = completedTasksList.allowNotification > 0
                tgCompleted.isChecked = completedTasksList.completed > 0

                swAllowNotification.isEnabled = false
                btnEdit.isEnabled = false

                btnDelete.setOnClickListener {
                    onClickDelete(completedTasksList.id)
                }

                tgCompleted.setOnClickListener {
                    val completed = if(tgCompleted.isChecked) 1 else 0
                    val actualDate = getDateTimeCalendar()
                    val updatedTask = Task(
                        completedTasksList.id, completedTasksList.title,
                        completedTasksList.description,
                        convertToSQLiteFormat(completedTasksList.createdAt),
                        convertToSQLiteFormat(actualDate),
                        convertToSQLiteFormat(completedTasksList.dueDate),
                        completedTasksList.allowNotification,
                        completedTasksList.notified,
                        completed
                    )
                    val taskDAO = TaskDAO(context)

                    if(taskDAO.update(updatedTask)) {
                        Toast.makeText(context, "Tarefa concluída!", Toast.LENGTH_SHORT).show()
                        tgCompleted.isClickable = false
                        btnBackToMain.isEnabled = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            updateCompletedTasksList()
                            tgCompleted.isClickable = true
                            btnBackToMain.isEnabled = true
                        }, 1500)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedTasksViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val taskItemBinding = ListItemBinding.inflate(
            layoutInflater, parent, false
        )
        return CompletedTasksViewHolder(taskItemBinding)
    }

    override fun onBindViewHolder(holder: CompletedTasksViewHolder, position: Int) {
        val completedTasks = completedTasksList[position]
        holder.bind(completedTasks)
    }

    override fun getItemCount(): Int {
        return completedTasksList.size
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