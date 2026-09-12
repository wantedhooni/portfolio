package ex_functional_literal_with_receiver

fun main(args: Array<String>)
{
	val square: Int.() -> Int
	square = { this * this }
	println(10.square())

	val power: Int.(exponent: Int) -> Int
	power = { exponent: Int ->
		var result: Int = 1
		var i = 0
		while (i < exponent)
		{
			result *= this
			i += 1
		}

		result
	}
	println(3.power(4))
	println(2.power(0))

	val pow: (Int, Int) -> Int = power
	println(pow(2, 3))
}
