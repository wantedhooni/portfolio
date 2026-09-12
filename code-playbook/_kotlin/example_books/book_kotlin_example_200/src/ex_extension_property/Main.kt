package ex_extension_property

// 문자열이 숫자로만 이루어져있는지 판단하는 확장 프로퍼티
val String.isNumber: Boolean
	get()
	{
		var i = 0
		while (i < this.length)
		{
			// 숫자가 아닌 문자가 하나라도 들어있으면 false 반환
			if (!('0' <= this[i] && this[i] <= '9'))
				return false
			i += 1
		}
		// 모든 조건을 통과하면 true 반환
		return true
	}

fun main(args: Array<String>)
{
	println("1234567890".isNumber)
	println("500 원".isNumber)
}
