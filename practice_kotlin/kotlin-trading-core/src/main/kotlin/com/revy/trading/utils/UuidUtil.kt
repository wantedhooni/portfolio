package com.revy.trading.utils

import com.fasterxml.uuid.Generators
import java.util.UUID

object UuidUtil {
    fun generateUuidV7(): UUID = Generators.timeBasedEpochGenerator().generate()

}