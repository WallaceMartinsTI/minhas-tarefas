package com.wcsm.minhastarefas.database

import com.wcsm.minhastarefas.model.Task

interface ITaskDAO {
    fun save(task: Task): Boolean
    fun update(task: Task): Boolean
    fun delete(taskId: Int): Boolean
    fun listTasks(): List<Task>
}