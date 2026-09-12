package com.revy.petclinic.model

import jakarta.persistence.MappedSuperclass

@MappedSuperclass
open class NamedEntity : BaseEntity(){

    open var name: String? = null;

    override fun toString(): String = this.name ?: ""
}