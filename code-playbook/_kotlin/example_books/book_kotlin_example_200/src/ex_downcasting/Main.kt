package ex_downcasting

// 업캐스팅 예제의 Person, Student 클래스 재활용
import ex_upcasting.Person
import ex_upcasting.Student

fun main(args: Array<String>)
{
	val person: Person = Student("John", 32, 20171218)
	val person2: Person = Person("Jack", 29)

	var person3: Student = person as Student
	person3 = person2 as Student
}
