package com.revy.petclinic.owner.entity

import com.revy.petclinic.model.NamedEntity
import com.revy.petclinic.visit.Visit
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.LinkedHashSet

@Entity
@Table(name = "pets")
class Pet : NamedEntity() {

    @Column(name = "birth_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    var birthDate: LocalDate? = null

    @ManyToOne
    @JoinColumn(name = "type_id")
    var type: PetType? = null

    @ManyToOne
    @JoinColumn(name = "owner_id")
    var owner: Owner? = null

    @Transient
    var visits: MutableSet<Visit> = LinkedHashSet()


    fun getVisits(): List<Visit> =
        visits.sortedWith(compareBy { it.date })

    fun addVisit(visit: Visit) {
        visits.add(visit)
        visit.petId = this.id
    }
}