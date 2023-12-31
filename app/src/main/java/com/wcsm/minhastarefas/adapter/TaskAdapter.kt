package com.wcsm.minhastarefas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wcsm.minhastarefas.databinding.ListItemBinding
import com.wcsm.minhastarefas.model.Task

class TaskAdapter(
    val onClickDelete: (Int) -> Unit,
    val onClickUpdate: (Task) -> Unit
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
        }

        fun bind(task: Task) {}
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
}
