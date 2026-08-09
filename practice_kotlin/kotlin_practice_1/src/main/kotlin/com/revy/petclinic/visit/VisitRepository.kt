package com.revy.petclinic.visit

import org.springframework.data.jpa.repository.JpaRepository

interface VisitRepository : JpaRepository<Visit, Long> {

    fun save(visit: Visit);

    fun findByPetId(petId: Long): MutableSet<Visit>

}