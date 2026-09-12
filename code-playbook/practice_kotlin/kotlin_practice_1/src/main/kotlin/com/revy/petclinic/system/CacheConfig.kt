package com.revy.petclinic.system

import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import javax.cache.configuration.MutableConfiguration

@Configuration
@EnableCaching
class CacheConfig {

    fun cacheManagerCustomizer(): JCacheManagerCustomizer {
        return JCacheManagerCustomizer {
            it.createCache("vets", createCacheConfiguration())
        }
    }


    private fun createCacheConfiguration(): javax.cache.configuration.Configuration<Any, Any> {
        return MutableConfiguration<Any, Any>().setStatisticsEnabled(true);
    }
}