package com.revy.demo.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskEntityRepository : JpaRepository<TaskEntity, Long> {
}