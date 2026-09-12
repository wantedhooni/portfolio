package com.example.rag_with_kotlin

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.chat.client.ChatClient

@SpringBootTest
class RagWithKotlinApplicationTests {

	@MockitoBean
	private lateinit var vectorStore: VectorStore

	@MockitoBean
	private lateinit var chatClientBuilder: ChatClient.Builder

	@Test
	fun contextLoads() {
	}

}
