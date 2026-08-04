package com.revy.petclinic.visit

import com.revy.petclinic.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

@Entity
@Table(name = "visits")
class Visit : BaseEntity() {

    @Column(name = "visit_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var date: LocalDate = LocalDate.now()

    @NotEmpty
    @Column(name = "description")
    var description: String? = null

    @Column(name = "pet_id")
    var petId: Long? = null;
}