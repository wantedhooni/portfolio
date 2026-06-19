package com.revy.example.order.client

import com.revy.example.order.client.dto.Product
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "product-service",
    url="\${PRODUCT_SERVICE_URL:http://localhost:8081}"

)   // Eureka에 등록된 이름으로 호출
interface ProductClient {
    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: Long): Product
}