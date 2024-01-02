package com.wcsm.minhastarefas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wcsm.minhastarefas.databinding.ListItemBinding
import com.wcsm.minhastarefas.model.Task

class CompletedTasksAdapter(
    private var completedTasksList: List<Task> = listOf(),
    val onClickDelete: (Int) -> Unit
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

}