package com.revy.petclinic.model

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotEmpty


@MappedSuperclass
open class Person : BaseEntity(){

    @Column(name="first_name")
    @NotEmpty
    var firstName = "";

    @Column(name="last_name")
    @NotEmpty
    var lastName = "";
}