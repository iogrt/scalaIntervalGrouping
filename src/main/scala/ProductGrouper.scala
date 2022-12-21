import java.time.temporal.ChronoUnit
import java.time.LocalDateTime


object ProductGrouper {
  // I considered using Map[Range.Inclusive,Int], but this would make it so we couldn't do binary searching
  // (TreeMap is a BST but doesn't do binary searching with find so it is also unfit)
  type ProductGrouping = Array[Range.Inclusive]
  type GroupedProductsCount = Array[Int]

  // I decided to not have side effects (IO) outside of main, so I capture errors with this trait
  sealed trait GroupingError
  case class Duplicate(x:Int) extends GroupingError
  case class UnsortedList (x:Int, y: Int) extends GroupingError
  case object EmptyList extends GroupingError

  // for when interval provided doesn't start at 1, also catches negative values
  case class IncompleteInterval (intervalStart:Int) extends GroupingError

  // the "rounding up" is done by removing one day from the interval and adding 1 month to the total
  // Example: 01-01-2022,01-02-2022 ->(+1 day) 02-01-2022,01-02-2022 ->(+1total) 0+1 = 1
  // Example: 01-01-2022,02-02-2022 ->(+1 day) 02-01-2022,01-02-2022 ->(+1total) 0+1 = 1
  private def _productAge (p:Product):Int =
    // toInt conversion is reasonable since month difference won't reach such high values
    // (supports up to a million years)
    ChronoUnit.MONTHS.between(p.creationDate.plusDays(1), LocalDateTime.now).toInt + 1


  // Builds the sequence of ranges -> [1,3,7,12] -> [1-2,3-6,>12]
  private def _makeProductGrouping (intList:List[Int]):Either[GroupingError,Array[Range.Inclusive]] = {
    def loop (startingFrom:Int, intList:List[Int]):Either[GroupingError,List[Range.Inclusive]] =  {
      intList match {
        case x :: xs if x>startingFrom => 
          loop(x,xs).map((Range.inclusive(startingFrom,x-1)) :: _)
        case x :: xs if x==startingFrom => Left(Duplicate(x))
        case x :: xs => Left(UnsortedList(startingFrom,x))
        case Nil => Right ((Range.inclusive(startingFrom, Int.MaxValue)) :: Nil)
      }
    }
    intList match {
      case 1 :: xs => (loop(1,xs).map(_.toArray))
      case x :: xs => Left(IncompleteInterval(x))
      case _ => Left(EmptyList)
    }
  }
  
  // binary search, implementing a binary tree is out of scope so instead we search on the Array 
  private def _productFindGroup (groups:ProductGrouping, age:Int):Int = {
    val middle = groups.size/2
    if (age < groups(middle).start)
      _productFindGroup(groups.take(middle),age)
    else if (age > groups(middle).end)
      middle + 1 + _productFindGroup(groups.drop(middle+1),age)
    else middle
  }

  // is inclusive
  private def _orderFilter (minDate:LocalDateTime,maxDate:LocalDateTime) = 
    (order:Order) => (!order.orderDate.isBefore(minDate)) && (!order.orderDate.isAfter(maxDate))

  def productsWithOrdersByCreationDateInMonthIntervals 
    ( orders: Array[Order]
    , intervals: Array[Int]
    , minDate: LocalDateTime
    , maxDate: LocalDateTime
    ): Either[GroupingError,Array[(Int,Range)]] =
    _makeProductGrouping(intervals.toList).map(grouping => {
      orders 
      .filter(_orderFilter(minDate,maxDate))
      .flatMap(order => order.cart.map(item => item.product))
      .foldLeft(Array.fill(intervals.size)(0))((acc, product) => {
        val idx = _productFindGroup(grouping,_productAge(product))
        acc.updated(idx,acc(idx)+1)
      })
      .zip(grouping)
    })
}
