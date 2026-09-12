package com.revy.example.entity.securities


import com.revy.example.entity.common.BaseEntity
import com.revy.example.entity.securities.enums.ProductCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "TB_PRODUCT")
class Product protected constructor(

    @Column(name = "PRODUCT_CODE", length = 20)
    val productCode: String,

    @Column(name = "PRODUCT_NAME_KR", nullable = false, length = 100)
    var productNameKr: String,

    @Column(name = "PRODUCT_NAME_EN", length = 100)
    var productNameEn: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "PRODUCT_CATEGORY", nullable = false, length = 30)
    val productCategory: ProductCategory,

    @Column(name = "PRODUCT_FEATURE", length = 500)
    var productFeature: String?,

    @Column(name = "SALES_COUNTRY", length = 50)
    var salesCountry: String?,

    @Column(name = "EFFECTIVE_START_DATE")
    var effectiveStartDate: LocalDate?,

    @Column(name = "EFFECTIVE_END_DATE")
    var effectiveEndDate: LocalDate?

): BaseEntity() {
    companion object {
        fun of(
            productCode: String,
            productNameKr: String,
            productNameEn: String?,
            productCategory: ProductCategory,
            productFeature: String? = null,
            salesCountry: String? = null,
            effectiveStartDate: LocalDate? = null,
            effectiveEndDate: LocalDate? = null
        ): Product = Product(
            productCode, productNameKr, productNameEn, productCategory,
            productFeature, salesCountry, effectiveStartDate, effectiveEndDate
        )
    }
}