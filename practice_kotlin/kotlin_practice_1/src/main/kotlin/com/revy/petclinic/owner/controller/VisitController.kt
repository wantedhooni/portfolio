package com.revy.petclinic.owner.controller

import com.revy.petclinic.owner.entity.PetRepository
import com.revy.petclinic.visit.Visit
import com.revy.petclinic.visit.VisitRepository
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class VisitController(
    val visits: VisitRepository,
    val pets: PetRepository,
) {
    @InitBinder
    fun setAllowedFields(dataBinder: WebDataBinder) {
        dataBinder.setDisallowedFields("id")
    }

    @ModelAttribute("visit")
    fun loadPetWithVisit(@PathVariable("petId") petId: Long, model: MutableMap<String, Any>): Visit {
        val pet = pets.findById(petId)
        model["pet"] = pet
        val visit = Visit()
        pet.addVisit(visit)
        return visit
    }

    // Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
    @GetMapping("/owners/*/pets/{petId}/visits/new")
    fun initNewVisitForm(@PathVariable("petId") petId: Int, model: Map<String, Any>): String =
        "pets/createOrUpdateVisitForm"

    // Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    fun processNewVisitForm(@Valid visit: Visit, result: BindingResult): String {
        return if (result.hasErrors()) {
            "pets/createOrUpdateVisitForm"
        } else {
            visits.save(visit)
            "redirect:/owners/{ownerId}"
        }
    }
}