package com.revy.petclinic.model

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
open class BaseEntity : Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open var id: Long? = null;

    val isNew: Boolean
        get() = this.id != null
}