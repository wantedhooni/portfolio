package com.revy.batch.chapter_04.batch

import com.revy.batch.chapter_04.common.log
import org.springframework.batch.infrastructure.repeat.CompletionPolicy
import org.springframework.batch.infrastructure.repeat.RepeatContext
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import java.util.Random

/*

 */
class RandomChunkSizePolicy : CompletionPolicy {

    var chunksize: Int = 0
    var totalProcessed: Int = 0
    var random: Random = Random()

    override fun isComplete(
        context: RepeatContext,
        result: RepeatStatus
    ): Boolean {
        return RepeatStatus.FINISHED == result || isComplete(context);
    }

    override fun isComplete(context: RepeatContext): Boolean {
        return totalProcessed >= chunksize
    }

    override fun start(parent: RepeatContext): RepeatContext {
        this.chunksize = random.nextInt(20);
        this.totalProcessed = 0;
        log.info { "totalProcessed: $totalProcessed" }
        log.info { "The chunk size has been set to ${this.chunksize}" }
        return parent;
    }
    override fun update(context: RepeatContext) {
        this.totalProcessed++
    }
}