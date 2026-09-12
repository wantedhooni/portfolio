package com.revy.petclinic.vet

import com.revy.petclinic.model.NamedEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table


@Entity
@Table(name = "specialties")
class Specialty: NamedEntity() {
}