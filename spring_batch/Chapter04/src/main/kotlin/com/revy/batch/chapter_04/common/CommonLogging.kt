package com.revy.batch.chapter_04.common

import io.github.oshai.kotlinlogging.KotlinLogging

val Any.log
    get() = KotlinLogging.logger(javaClass.name)