class Person
{
	var name: String = ""
	var age: Int = 0
}

fun main(args: Array<String>)
{
	val person: Person = Person()
	person.name = "홍길동"
	person.age = 36

	val person2: Person = Person()
	person2.name = "김지영"
	person2.age = 29

	println(person.name)
	println(person.age)

	println(person2.name)
	println(person2.age)
}
