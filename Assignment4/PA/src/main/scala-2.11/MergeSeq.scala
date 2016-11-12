object Lunch {

  def doMergeSortExample() = {
    val values:Array[Int] = List(5,11,8,4,2).toArray
    sort(values)
    printArray(values)
  }
  private val GLOBAL_THREAD_LIMIT = {
    val ret = Runtime.getRuntime.availableProcessors() / 2
    if (ret > 5) {
      5
    } else {
      ret
    }
  }
  def sort(array:Array[Int]) {
    if (array.length > 1 ){
      var firstArrayLength = (array.length/2)
      var first:Array[Int] = array.slice(0, firstArrayLength)
      var second:Array[Int] = array.slice(firstArrayLength, array.length)
      sort(first)
      sort(second)
      merge(array, first, second)
    }
  }

  def merge(result:Array[Int], first:Array[Int], second:Array[Int]) {
    var i:Int = 0
    var j:Int = 0
    for (k <- 0 until result.length) {
      if(i<first.length && j<second.length){
        if (first(i) < second(j)){
          result(k) = first(i)
          i=i+1
        } else {
          result(k) = second(j)
          j=j+1
        }
      }else if(i>=first.length && j<second.length){
        result(k) = second(j)
        j=j+1
      } else {
        result(k) = first(i)
        i=i+1
      }
    }
  }

  def printArray(array: Array[Int]) = {
    println(array.deep.mkString(", "))
  }

  def main(args: Array[String]) {
    doMergeSortExample();
  }

}