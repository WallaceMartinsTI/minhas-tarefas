package com.wcsm.minhastarefas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.wcsm.minhastarefas.adapter.TaskAdapter
import com.wcsm.minhastarefas.database.TaskDAO
import com.wcsm.minhastarefas.databinding.ActivityMainBinding
import com.wcsm.minhastarefas.model.Task

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var taskList = emptyList<Task>()
    private var taskAdapter: TaskAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            fabAddTask.setOnClickListener {
                startActivity(Intent(applicationContext, AddTaskActivity::class.java))
            }

            btnCompletedTasks.setOnClickListener {
                startActivity(Intent(applicationContext, CompletedTasksActivity::class.java))
            }

            taskAdapter = TaskAdapter(
                {taskId -> confirmDelete(taskId)},
                {task -> edit(task)}
            )
            rvTasks.adapter = taskAdapter
            rvTasks.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    private fun edit(task: Task) {
        val intent = Intent(applicationContext, AddTaskActivity::class.java)
        intent.putExtra("task", task)
        startActivity(intent)
    }

    private fun confirmDelete(id: Int) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("Confirmar exclusão")
        alertBuilder.setMessage("Deseja realmente excluir a tarefa?")



        alertBuilder.setNegativeButton("Não") {_, _ ->

        }

        alertBuilder.create().show()
    }

    private fun updateTaskList() {
        val taskDAO = TaskDAO(this)
        taskList = taskDAO.listTasks()
        taskAdapter?.addList(taskList)
    }

    override fun onStart() {
        super.onStart()
        updateTaskList()
    }
}

