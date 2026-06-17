package ex_anonymous_function

fun main(args: Array<String>)
{
	val instantFunc: (Int) -> Unit
	instantFunc = fun(number: Int): Unit
	{
		println("Hello $number")
	}

	instantFunc(33)
	instantFunc.invoke(33)
}
