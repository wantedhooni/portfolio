package com.revy.demo.controller

import com.revy.demo.model.TaskDtoRequest
import com.revy.demo.model.TaskDtoResponse
import com.revy.demo.service.TaskService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/tasks")
class TaskController(private val service: TaskService) {

    @PostMapping("/create")
    fun createTask(@RequestBody newTask: TaskDtoRequest) : TaskDtoResponse{
        return service.createTask(newTask);
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long) : TaskDtoResponse{
        return service.getTask(id);
        // throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found")
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long){
        service.deleteTask(id);
    }


    @PutMapping("/{id}")
    fun updateTask(@PathVariable id:Long, updateTask: TaskDtoRequest) : TaskDtoResponse{
        return service.updateTask(id, updateTask);
        //  ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found")
    }
}
