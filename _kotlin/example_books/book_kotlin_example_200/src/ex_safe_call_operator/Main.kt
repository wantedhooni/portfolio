package ex_safe_call_operator

// 멤버 함수 예제의 Building 클래스 재활용
import ex_member_function.Building

fun main(args: Array<String>)
{
	var obj: Building? = null
	obj?.print()
	obj?.name = "건물"

	obj = Building()
	obj?.name = "서울월드컵경기장"
	obj?.date = "2001년 11월 10일"
	obj?.area = 21_6712
	obj?.print()
}
