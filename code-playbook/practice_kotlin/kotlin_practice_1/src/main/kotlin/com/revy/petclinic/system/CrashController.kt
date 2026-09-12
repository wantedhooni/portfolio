package com.revy.petclinic.system

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CrashController {

    @GetMapping("/oups")
    fun triggerException(){
        throw RuntimeException("에러다!!")
    }
}