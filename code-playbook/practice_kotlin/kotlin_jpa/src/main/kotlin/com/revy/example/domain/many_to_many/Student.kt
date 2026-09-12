package com.revy.example.domain.many_to_many

import com.revy.example.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "student_2")
class Student2(
    var name: String,

    @ManyToMany
    @JoinTable(
        name = "student_course_2",
        joinColumns = [JoinColumn(name = "student_id")],
        inverseJoinColumns = [JoinColumn(name = "course_id")]
    )
    val courses: MutableList<Course2> = mutableListOf()

) : BaseEntity() {

    fun addCourse(course: Course2) {
        courses.add(course)
        course.students.add(this)
    }

    fun removeCourse(course: Course2) {
        courses.remove(course)
        course.students.remove(this)
    }
}

@Entity
@Table(name = "course_2")
class Course2(
    var title: String,

    @ManyToMany(mappedBy = "courses")
    val students: MutableList<Student2> = mutableListOf()

) : BaseEntity()