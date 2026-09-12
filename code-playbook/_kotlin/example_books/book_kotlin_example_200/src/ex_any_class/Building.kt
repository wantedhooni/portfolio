package ex_any_class

class Building(	val name: String = "",	// 건물명
				val date: String = "",	// 건축일자
				val area: Int = 0)		// 면적 (㎡)
{
	override fun toString() =
			"이름: ${this.name}\n" +
			"건축일자: ${this.date}\n" +
			"면적: ${this.area} ㎡"
}
