package com.revy.batch.chapter06.batch.job

import com.revy.batch.chapter06.batch.TransactionReader
import com.revy.batch.chapter06.domain.Transaction
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.infrastructure.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.infrastructure.item.file.mapping.PassThroughFieldSetMapper
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.infrastructure.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class TransactionProcessingJob(
    val jobRepository: JobRepository
) {

    @Bean
    @StepScope
    fun transactionReader(
        fileItemReader: FlatFileItemReader<FieldSet>,
    ): TransactionReader {
        return TransactionReader(fileItemReader)
    }

    @Bean
    @StepScope
    fun fileItemReader(
        @Value("#{jobParameters['transactionFile']}")
        inputFile: Resource,
    ): FlatFileItemReader<FieldSet> {
        return FlatFileItemReaderBuilder<FieldSet>().name("fileItemReader")
            .resource(inputFile).lineTokenizer(DelimitedLineTokenizer()).fieldSetMapper(PassThroughFieldSetMapper())
            .build()
    }

    @Bean
    fun transactionWriter(
        dataSource: DataSource,
    ): JdbcBatchItemWriter<Transaction> {
        return JdbcBatchItemWriterBuilder<Transaction>().itemSqlParameterSourceProvider(
            BeanPropertyItemSqlParameterSourceProvider(),
            ).sql(
                """
                INSERT INTO TRANSACTION (
                    ACCOUNT_SUMMARY_ID,
                    TIMESTAMP,
                    AMOUNT
                )
                VALUES (
                    (
                        SELECT ID
                        FROM ACCOUNT_SUMMARY
                        WHERE ACCOUNT_NUMBER = :accountNumber
                    ),
                    :timestamp,
                    :amount
                )
                """.trimIndent(),
            ).dataSource(dataSource).build()
    }
}

