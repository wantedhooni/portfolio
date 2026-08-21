package com.revy.batch.chapter06.batch

import com.revy.batch.chapter06.domain.Transaction
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemStreamReader
import org.springframework.batch.infrastructure.item.ParseException
import org.springframework.batch.infrastructure.item.file.transform.FieldSet
import org.springframework.stereotype.Component


@Component
class TransactionReader(
    val fieldSetReader: ItemStreamReader<FieldSet>,
) : ItemStreamReader<Transaction> {
    var recordCount = 0
    var expectedRecordCount: Int? = 0
    lateinit var stepExecution: StepExecution

    @Throws(Exception::class)
    override fun read(): Transaction? {
        return process(fieldSetReader.read())
    }

    private fun process (
        fieldSet: FieldSet?): Transaction? {

        if(this.recordCount == 25) {
            throw ParseException("This isn't what I hoped to happen");
        }
        var result : Transaction? = null

        if(fieldSet == null) {
            expectedRecordCount = fieldSet?.readInt(0)

            if(expectedRecordCount != this.recordCount) {
                this.stepExecution.setTerminateOnly();
            }
            return result
        }

        if(fieldSet.getFieldCount() > 1) {
            result = Transaction();
            result.accountNumber = fieldSet.readString(0)
            result.timestamp = fieldSet.readDate(
                1,
                "yyyy-MM-DD HH:mm:ss"
            )
            result.amount = fieldSet.readDouble(2)
            recordCount++;
        }
        return result;


    }

}