package com.revy.petclinic.owner

import com.revy.petclinic.owner.entity.Pet
import org.springframework.util.StringUtils
import org.springframework.validation.Errors
import org.springframework.validation.Validator

class PetValidator : Validator {
    companion object {
        const val REQUIRED = "required"
    }

    override fun validate(obj: Any, errors: Errors) {
        TODO("Not yet implemented")
        var pet = obj as Pet
        var name = pet.name

        // name validation
        if (!StringUtils.hasLength(name)) {
            errors.rejectValue("name", REQUIRED, REQUIRED)
        }

        // type validation
        if (pet.isNew && pet.type == null) {
            errors.rejectValue("type", REQUIRED, REQUIRED)
        }

        // birth date validation
        if (pet.birthDate == null) {
            errors.rejectValue("birthDate", REQUIRED, REQUIRED)
        }

    }

    override fun supports(clazz: Class<*>): Boolean {
        return Pet::class.java.isAssignableFrom(clazz)
    }


}