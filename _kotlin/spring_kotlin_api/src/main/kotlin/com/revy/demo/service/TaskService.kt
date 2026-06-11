package com.revy.demo.service

import com.revy.demo.model.TaskDtoRequest
import com.revy.demo.model.TaskDtoResponse
import org.springframework.transaction.annotation.Transactional

interface TaskService {

    fun createTask(newTask: TaskDtoRequest): TaskDtoResponse

    @Transactional(readOnly = true)
    fun getTask(id: Long): TaskDtoResponse
    fun updateTask(id: Long, updateTask: TaskDtoRequest): TaskDtoResponse
    fun deleteTask(id: Long)
}

