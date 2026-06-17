fun main(args: Array<String>)
{
	val a = -36
	val result = absolute(a)
	println(result)
}

// 입력받은 정수의 절대값을 반환하는 함수
fun absolute(number: Int): Int
{
	return if (number >= 0)
		number
	else -number
}
