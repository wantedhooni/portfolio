package com.revy.petclinic.owner

import com.revy.petclinic.owner.entity.PetRepository
import com.revy.petclinic.owner.entity.PetType
import org.springframework.format.Formatter
import org.springframework.stereotype.Component
import java.text.ParseException
import java.util.Locale


@Component
class PetTypeFormatter(
    val pets: PetRepository
) : Formatter<PetType> {
    override fun print(petType: PetType, locale: Locale): String {
        return petType.name ?: "";
    }

    override fun parse(text: String, locale: Locale): PetType {
        val findPetTypes = this.pets.findPetTypes()
        return findPetTypes.find { it.name == text } ?:
        throw ParseException("type not found: " + text, 0)
    }


}