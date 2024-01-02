package com.wcsm.minhastarefas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wcsm.minhastarefas.adapter.CompletedTasksAdapter
import com.wcsm.minhastarefas.adapter.TaskAdapter
import com.wcsm.minhastarefas.database.TaskDAO
import com.wcsm.minhastarefas.databinding.ActivityCompletedTasksBinding
import com.wcsm.minhastarefas.model.Task

class CompletedTasksActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCompletedTasksBinding.inflate(layoutInflater)
    }

    private var taskList = emptyList<Task>()
    private var completedTasksAdapter: CompletedTasksAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var tasks = emptyList<Task>()

        val bundle = intent.extras
        if(bundle != null) {
            tasks = intent.getParcelableArrayListExtra("tasks") ?: emptyList()
            //Log.i("teste", "LISTA DE TASKS")
            //Log.i("teste", "$tasks")
        }

        with(binding) {
            btnBackToMain.setOnClickListener {
                finish()
            }

            completedTasksAdapter = CompletedTasksAdapter(tasks) { taskId -> confirmDelete(taskId) }
            rvCompletedTasks.adapter = completedTasksAdapter
            rvCompletedTasks.addItemDecoration(
                DividerItemDecoration(applicationContext, RecyclerView.VERTICAL)
            )
            rvCompletedTasks.layoutManager = LinearLayoutManager(applicationContext)
        }
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
        val completedTasks = mutableListOf<Task>()

        val taskDAO = TaskDAO(this)
        taskList = taskDAO.listTasks()

        taskList.forEach {
            if(it.completed > 0) {
                completedTasks.add(it)
            }
        }

        completedTasksAdapter?.addList(completedTasks)
    }

    override fun onStart() {
        super.onStart()
        updateTaskList()
    }
}