package com.wcsm.minhastarefas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
                val intent = Intent(applicationContext, AddTaskActivity::class.java)
                intent.putExtra("screen_title", "Adicionar Tarefa")
                intent.putExtra("button_text", "ADICIONAR")
                startActivity(intent)
            }

            fabCompletedTasks.setOnClickListener {
                startActivity(Intent(applicationContext, CompletedTasksActivity::class.java))
            }

            taskAdapter = TaskAdapter(
                {taskId -> confirmDelete(taskId)},
                {task -> edit(task)}
            )
            rvTasks.adapter = taskAdapter
            rvTasks.addItemDecoration(
                DividerItemDecoration(applicationContext, RecyclerView.VERTICAL)
            )
            rvTasks.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    private fun edit(task: Task) {
        val intent = Intent(applicationContext, AddTaskActivity::class.java)
        intent.putExtra("task", task)
        intent.putExtra("screen_title", "Editar Tarefa")
        intent.putExtra("button_text", "ATUALIZAR")
        startActivity(intent)
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

