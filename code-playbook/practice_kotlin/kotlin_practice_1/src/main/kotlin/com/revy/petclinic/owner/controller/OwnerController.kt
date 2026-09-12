package com.revy.petclinic.owner.controller

import com.revy.petclinic.owner.entity.Owner
import com.revy.petclinic.owner.entity.OwnerRepository
import com.revy.petclinic.visit.VisitRepository
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class OwnerController(
    val owners: OwnerRepository, var visits: VisitRepository

) {
    val VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm"

    @InitBinder
    fun setAllowedFields(dataBinder: WebDataBinder) {
        dataBinder.setDisallowedFields("id")
    }

    @GetMapping("/owners/new")
    fun initCreationForm(model: MutableMap<String, Any>): String {
        val owners = Owner()
        model["owners"] = owners
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM
    }

    @PostMapping("/owners/new")
    fun processCreationForm(@Valid owner: Owner, result: BindingResult): String {
        return if (result.hasErrors()) {
            VIEWS_OWNER_CREATE_OR_UPDATE_FORM
        } else {
            owners.save(owner)
            "redirect:/owners/" + owner.id
        }
    }

    @GetMapping("/owners/find")
    fun initFindForm(model: MutableMap<String, Any>): String {
        model["owner"] = Owner()
        return "owners/findOwners"
    }

    @GetMapping("/owners")
    fun processFindForm(owner: Owner, result: BindingResult, model: MutableMap<String, Any>): String {
        // find owners by last name
        val results = owners.findByLastName(owner.lastName)
        return when {
            results.isEmpty() -> {
                // no owners found
                result.rejectValue("lastName", "notFound", "not found")
                "owners/findOwners"
            }

            results.size == 1 -> {
                // 1 owner found
                "redirect:/owners/" + results.first().id
            }

            else -> {
                // multiple owners found
                model["selections"] = results
                "owners/ownersList"
            }
        }
    }

    @GetMapping("/owners/{ownerId}/edit")
    fun initUpdateOwnerForm(@PathVariable("ownerId") ownerId: Long, model: Model): String {
        val owner = owners.findById(ownerId)
        model.addAttribute(owner)
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM
    }

    @PostMapping("/owners/{ownerId}/edit")
    fun processUpdateOwnerForm(
        @Valid owner: Owner, result: BindingResult, @PathVariable("ownerId") ownerId: Long
    ): String {
        return if (result.hasErrors()) {
            VIEWS_OWNER_CREATE_OR_UPDATE_FORM
        } else {
            owner.id = ownerId
            this.owners.save(owner)
            "redirect:/owners/{ownerId}"
        }
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return the view
     */
    @GetMapping("/owners/{ownerId}")
    fun showOwner(@PathVariable("ownerId") ownerId: Long, model: Model): String {
        val owner = this.owners.findById(ownerId)
        for (pet in owner.getPets()) {
            pet.visits = visits.findByPetId(pet.id!!)
        }
        model.addAttribute(owner)
        return "owners/ownerDetails"
    }

}