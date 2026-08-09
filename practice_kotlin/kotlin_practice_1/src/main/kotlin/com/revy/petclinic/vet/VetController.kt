package com.revy.petclinic.vet

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class VetController(
    val vetRepository: VetRepository,
) {

    @GetMapping("/vets.html")
    fun showHtmlVetList(model: MutableMap<String, Any>): String {
        val vets = Vets(vetRepository.findAll())
        model.put("vets", vets)
        return "vets/vetList"
    }

    @GetMapping("vets.json", produces = ["application/json"])
    @ResponseBody
    fun showJsonVetList(): Vets =
    // Here we are returning an object of type 'Vets' rather than a collection of Vet
        // objects so it is simpler for Json/Object mapping
        Vets(vetRepository.findAll())


    @GetMapping("vets.xml")
    @ResponseBody
    fun showXmlVetList(): Vets =
        Vets(vetRepository.findAll())
}