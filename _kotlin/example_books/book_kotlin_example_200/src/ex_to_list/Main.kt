package ex_to_list

fun main(args: Array<String>)
{
	// NOTE: Pair 객체 내부의 타입이 모두 같아야 한다.
	val list: List<Int> = Pair(10, 20).toList()

	// NOTE: Triple 객체 내부의 타입이 모두 같아야 한다.
	val list2: List<Double> = Triple(3.1, 6.25, 8.15).toList()
}
