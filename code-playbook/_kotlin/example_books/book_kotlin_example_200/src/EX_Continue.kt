fun main(args: Array<String>): Unit
{
	var i = 0

	while (i < 10)
	{
		i += 1

		// 짝수이면
		if (i % 2 == 0)
			continue // continue 아래의 문장들을 모두 skip 한다.

		println(i)
	}
}
