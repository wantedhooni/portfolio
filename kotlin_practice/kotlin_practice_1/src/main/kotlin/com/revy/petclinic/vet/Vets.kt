package com.revy.petclinic.vet

import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement
data class Vets(var vetList: Collection<Vet>? = null)
