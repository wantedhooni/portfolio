package com.revy.demo.controller

import com.revy.demo.model.TaskDtoRequest
import com.revy.demo.model.TaskDtoResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class TaskControllerTest(
    @Autowired private val client: RestTestClient
) {
    private val log = LoggerFactory.getLogger(this::class.java)


    @Test
    @DisplayName("테스크 생성 요청 - 성공")
    fun createTask() {
        log.info("Test createTask start")

        val testUri = "/tasks/create"
        val request = TaskDtoRequest(
            name = "Task",
            description = "junit test",
            done = false
        );
        log.info("createTask request: $request")

        val response = client.post()
            .uri(testUri)
            .body(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(TaskDtoResponse::class.java)
            .returnResult().responseBody

        log.info("createTask response: $response")

        assertThat(response).isNotNull
        assertThat(response!!.id).isNotNull
        assertThat(response.name).isEqualTo(request.name)
        assertThat(response.description).isEqualTo(request.description)
        assertThat(response.done).isEqualTo(request.done)

        log.info("Test createTask end")
    }


    @Test
    @DisplayName("GET /tasks/{id} - task 단건 조회")
    fun returnTaskSuccessfully() {
        val created = createTaskFixture()

        val response = client.get()
            .uri("/tasks/{id}", created.id)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TaskDtoResponse::class.java)
            .returnResult()
            .responseBody

        assertThat(response).isNotNull
        assertThat(response!!.id).isEqualTo(created.id)
        assertThat(response.name).isEqualTo("Task")
        assertThat(response.description).isEqualTo("description")
        assertThat(response.done).isFalse()
    }

    @Test
    @DisplayName("PUT /tasks/{id} - task 수정")
    fun putTaskSuccessfully() {
        val created = createTaskFixture()

        val updateRequest = TaskDtoRequest(
            name = "Task",
            description = "description",
            done = true
        )

        client.put()
            .uri("/tasks/{id}", created.id)
            .contentType(MediaType.APPLICATION_JSON)
            .body(updateRequest)
            .exchange()
            .expectStatus().isOk()

        val response = client.get()
            .uri("/tasks/{id}", created.id)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TaskDtoResponse::class.java)
            .returnResult()
            .responseBody

        assertThat(response).isNotNull
        assertThat(response!!.id).isEqualTo(created.id)
        assertThat(response.done).isTrue()
    }

    @Test
    @DisplayName("DELETE /tasks/{id} - task 삭제")
    fun deleteTaskSuccessfully() {
        val created = createTaskFixture()

        client.delete()
            .uri("/tasks/{id}", created.id)
            .exchange()
            .expectStatus().isNoContent()

        client.get()
            .uri("/tasks/{id}", created.id)
            .exchange()
            .expectStatus().isNotFound()
    }

    private fun createTaskFixture(): TaskDtoResponse {
        val request = TaskDtoRequest(
            name = "Task",
            description = "description",
            done = false
        )

        return client.post()
            .uri("/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(TaskDtoResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Task creation response body must not be null")
    }


}
