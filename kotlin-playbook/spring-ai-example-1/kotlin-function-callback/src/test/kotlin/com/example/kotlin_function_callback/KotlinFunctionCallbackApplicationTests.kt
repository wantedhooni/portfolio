package com.example.kotlin_function_callback

import org.junit.jupiter.api.Test
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class KotlinFunctionCallbackApplicationTests {

	@MockitoBean
	private lateinit var init: CommandLineRunner

	@Test
	fun contextLoads() {
	}

}
