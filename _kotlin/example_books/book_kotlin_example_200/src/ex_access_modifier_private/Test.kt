package ex_access_modifier_private

// private 전역 변수 선언. Test.kt 파일 내에서만 접근 가능
private var num = 10

// private 함수 선언. Test.kt 파일 내에서만 접근 가능
private fun print()
{
	// num 전역 변수는 print 함수와 같은 파일에 있으므로 접근 가능
	println(num)
}

// public 함수 선언
public fun hello(value: Int)
{
	// num 전역 변수는 hello 함수와 같은 파일에 있으므로 접근 가능
	num = value
	// print 함수는 hello 함수와 같은 파일에 있으므로 접근 가능
	print()
}

// public 클래스 선언
public class Person(age: Int)
{
	// 디폴트 Setter를 private으로 지정. Setter는 Person 클래스 내부에서만 접근 가능
	public var age = age
		private set

	// Getter를 public으로 지정
	public val isYoung public get() = age < 30
}
