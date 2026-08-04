package com.revy.petclinic.owner.entity

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.transaction.annotation.Transactional

interface PetRepository : Repository<Pet, Int> {

    @Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
    @Transactional(readOnly = true)
    fun findPetTypes(): List<PetType>

    @Transactional(readOnly = true)
    fun findById(id: Long): Pet

    fun save(pet: Pet)
}