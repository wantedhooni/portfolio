package com.revy.petclinic.owner.entity

import com.revy.petclinic.model.NamedEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "types")
open class PetType : NamedEntity()