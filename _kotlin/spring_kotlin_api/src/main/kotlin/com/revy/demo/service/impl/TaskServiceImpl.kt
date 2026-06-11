package com.revy.demo.service.impl

import com.revy.demo.entity.TaskEntity
import com.revy.demo.entity.TaskEntityRepository
import com.revy.demo.model.TaskDtoRequest
import com.revy.demo.model.TaskDtoResponse
import com.revy.demo.service.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TaskServiceImpl(private val repository: TaskEntityRepository) : TaskService{


    override fun createTask(newTask: TaskDtoRequest): TaskDtoResponse {
        var save: TaskEntity = TaskEntity(
            id = null, name = newTask.name, description = newTask.description, done = newTask.done
        );
        save = repository.save(save);
        return TaskDtoResponse(id = save.id!!, name = save.name, description = save.description, done = save.done);
    }

    @Transactional(readOnly = true)
    override fun getTask(id: Long): TaskDtoResponse {
        return repository.findById(id).map {
                TaskDtoResponse(
                    id = it.id!!, name = it.name, description = it.description, done = it.done
                )
            }.orElseThrow {
                NoSuchElementException("Task with id $id not found")
            }
    }

    override fun updateTask(id: Long, updateTask: TaskDtoRequest): TaskDtoResponse {
        return repository.findById(id).map {
                val save = repository.save(
                    TaskEntity(
                        id = it.id, name = updateTask.name, description = updateTask.description, done = updateTask.done
                    )
                )
                TaskDtoResponse(id = save.id!!, name = save.name, description = save.description, done = save.done)
            }.orElseThrow {
                NoSuchElementException("Task with id $id not found")
            }
    }

    override fun deleteTask(id: Long) {
        repository.deleteById(id);
    }


}
