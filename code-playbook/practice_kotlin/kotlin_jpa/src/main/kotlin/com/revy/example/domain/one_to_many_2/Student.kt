package com.revy.example.domain.one_to_many_2

import com.revy.example.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "student")
class Student(
    var name: String,

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
    val enrollments: MutableList<Enrollment> = mutableListOf()


) : BaseEntity() {

}

@Entity
@Table(name = "course")
class Course(
    var title: String,

    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL])
    val enrollments: MutableList<Enrollment> = mutableListOf()

) : BaseEntity() {

}

@Entity
@Table(
    name = "enrollment",
    uniqueConstraints = [UniqueConstraint(columnNames = ["student_id", "course_id"])]
)
class Enrollment(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    var student: Student? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    var course: Course? = null,

    @Enumerated(EnumType.STRING)
    var status: EnrollmentStatus = EnrollmentStatus.ACTIVE

) : BaseEntity()


enum class EnrollmentStatus {
    ACTIVE, COMPLETED, CANCELLED
}