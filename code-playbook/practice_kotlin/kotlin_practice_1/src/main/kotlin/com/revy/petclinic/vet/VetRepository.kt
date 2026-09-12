package com.revy.petclinic.vet

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.Repository
import org.springframework.transaction.annotation.Transactional


interface VetRepository : Repository<Vet, Long> {


    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ["vets"])
    fun findAll(): Collection<Vet>
}