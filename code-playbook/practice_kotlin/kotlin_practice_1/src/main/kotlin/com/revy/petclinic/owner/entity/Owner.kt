package com.revy.petclinic.owner.entity

import com.revy.petclinic.model.Person
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotEmpty
import java.util.HashSet

@Entity
@Table(name = "owners")
class Owner : Person(){

    @Column(name = "address")
    @NotEmpty
    var address = ""

    @Column(name = "city")
    @NotEmpty
    var city = ""

    @Column(name = "telephone")
    @NotEmpty
    @Digits(fraction = 0, integer = 10)
    var telephone = ""

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "owner")
    var pets: MutableSet<Pet> = HashSet()


    fun getPets(): List<Pet> =
        pets.sortedWith(compareBy({ it.name }))


    fun addPet(pet: Pet) {
        if (pet.isNew) {
            pets.add(pet)
        }
        pet.owner = this
    }

    fun getPet(name: String): Pet? =
        getPet(name, false)

    fun getPet(name: String, ignoreNew: Boolean): Pet? {
        val lname = name.lowercase()
        for (pet in pets) {
            if (!ignoreNew || !pet.isNew) {
                val compName = pet.name?.lowercase()
                if (compName == lname) {
                    return pet
                }
            }
        }
        return null
    }
}