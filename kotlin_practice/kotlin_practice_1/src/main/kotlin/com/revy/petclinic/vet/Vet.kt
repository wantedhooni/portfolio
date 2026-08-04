package com.revy.petclinic.vet

import com.revy.petclinic.model.Person
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.xml.bind.annotation.XmlElement

@Entity
@Table(name = "vets")
class Vet: Person() {
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="vet_specialties",
        joinColumns =[JoinColumn(name="vet_id")],
        inverseJoinColumns = [JoinColumn(name="specialty_id")]
    )
    var specialties: MutableSet<Specialty> = HashSet()

    @XmlElement
    fun getSpecialties(): List<Specialty> =
        specialties.sortedWith(compareBy { it.name })

    fun getNrOfSpecialties(): Int =
        specialties.size


    fun addSpecialty(specialty: Specialty) =
        specialties.add(specialty)

}