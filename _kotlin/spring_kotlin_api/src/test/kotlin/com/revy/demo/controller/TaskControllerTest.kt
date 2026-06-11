package com.revy.demo.controller

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class TaskControllerTest {
    private val log = LoggerFactory.getLogger(this::class.java)


    @Test
    fun createTask() {
        log.info("createTask")
    }

    @Test
    fun getTask() {
        log.info("getTask")
    }

    @Test
    fun deleteTask() {
        log.info("deleteTask")
    }

    @Test
    fun updateTask() {
        log.info("updateTask")
    }

}

/*
@SpringBootTest(
    classes = [SpringBootCrudApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class TaskDTOResponseCRUDIntegrationTest(
    @Autowired var restTemplate: TestRestTemplate
) {

    var taskId: Long = 0

    @Test
    fun createTask() {
        val taskDTORequest = TaskDTORequest("Task", "description", false)
        val result = this.restTemplate.postForEntity("/tasks/create", taskDTORequest, TaskDTOResponse::class.java)
        taskId = result.body?.id!!
        assertTrue { result.body?.name.equals("Task") }
        assertTrue { result.body?.description.equals("description") }
    }

    @Test
    fun returnTaskSuccessfully() {
        createTask()
        val result = this.restTemplate.getForEntity("/tasks/{id}", TaskDTOResponse::class.java, taskId)
        assertTrue { result.body?.name.equals("Task") }
    }

    @Test
    fun deleteTaskSuccessfully() {
        createTask()
        this.restTemplate.delete("/tasks/{id}", taskId)
        val result = this.restTemplate.getForEntity("/tasks/{id}", String::class.java, taskId)
        assertTrue { result.statusCode.equals(HttpStatus.NOT_FOUND) }
    }

    @Test
    fun putTaskSuccessfully() {
        createTask()
        val taskDTORequest = TaskDTORequest("Task", "description", true)
        this.restTemplate.put("/tasks/{id}", taskDTORequest, taskId)
        val result = this.restTemplate.getForEntity("/tasks/{id}", TaskDTOResponse::class.java, taskId)
        assertTrue { result.statusCode.is2xxSuccessful }
        assertTrue { result.body?.done!! }
    }
}
 */