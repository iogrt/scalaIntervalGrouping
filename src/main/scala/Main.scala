import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Main {
  // Assumptions made for this solution:
  //
  // Since the example interval starts at 1, it's assumed that months are rounded up,
  // meaning that 10 days ago falls into the 1 month category, but exactly 1 month ago is still 1 month
  //
  // It was implied that the grouping was mutually exclusive (no overlap)
  //
  // since the behaviour of an order that has products on more than one interval wasn't specified, I assumed
  // "groups the orders based on the product age" meant to group products with orders and not orders, meaning that 
  // if the case above occurs, one order will be counted multiple times.
  //
  // It's is assumed that the custom interval list starts also from current time, so it is necessary to provide 1 as first number
  val usage = """
    Usage:
      $ scala orders.jar minimum-date maximum-date [list of intervals]
    Examples:
      $ scala orders.jar "2018-01-01 00:00:00" "2019-01-01 00:00:00"
      $ scala orders.jar "2018-01-01 00:00:00" "2019-01-01 00:00:00" "1,3,7"
    """

  val allProducts = Seeder.productSeeder(
    quantity = 10000,
    maxProductYears = 80
  )
  val allOrders = Seeder.orderSeeder(
    quantity = 10000,
    maxCartSize = 20,
    allProducts
  )

  def parseArgs(args: Array[String]): Option[(LocalDateTime,LocalDateTime,Option[Array[Int]])] = {
    def parseDate (d:String): Option[LocalDateTime] =
      try { Some(LocalDateTime.parse(d,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))}
      catch {case e: DateTimeParseException => println(s"Failed to parse date: '$d'"); None}

    args.toList match {
      case arg1 :: arg2 :: arg3 :: Nil => 
        try {
          val interval = arg3.split(",").map(_.toInt)
          for {
            d1 <- parseDate(arg1)
            d2 <- parseDate(arg2)
          } yield (d1, d2, Some(interval))
        } catch { 
          case e: NumberFormatException => 
            println("Error: failed to parse custom interval")
            None
        }
      case arg1 :: arg2 :: Nil => (
        for {
          d1 <- parseDate(arg1)
          d2 <- parseDate(arg2)
        } yield (d1,d2,None)
      )
      case _ => println("Incorrect usage."); println(usage);
        None 
    }
  }

  def main(args: Array[String]) = {
    parseArgs(args).foreach(
      { case (
        minDate: LocalDateTime,
        maxDate: LocalDateTime,
        customInterval: Option[Array[Int]]
      ) => {
        val defaultIntervals = Array(1,4,7,12)
        ProductGrouper.productsWithOrdersByCreationDateInMonthIntervals(
          orders = allOrders,
          customInterval.getOrElse(defaultIntervals),
          minDate,
          maxDate
        ) match {
          case Right (r) => 
          println("Result:\n")
          r.foreach ({case (count,range) =>
            val monthRangeStr = 
              if (range.end == Int.MaxValue) s">${range.start}"
              else if (range.end == range.start) range.start
              else s"${range.start}-${range.end}"

            println(s"$monthRangeStr months: $count orders\n")
          })
          case Left(ProductGrouper.UnsortedList(x,y)) => println(s"Error: Please provide sorted intervals, '$y' comes after '$x' but is smaller")
          case Left(ProductGrouper.Duplicate(x)) => println(s"Error: Interval list has duplicate value '$x'")
          case Left(ProductGrouper.IncompleteInterval(x)) => println(s"Error: Interval list must start from current time (1 month), $x given")
          // Should never occur since argument parsing doesn't support this
          case Left(ProductGrouper.EmptyList) => println("Error: Empty custom intervals, omit them if that's intented")
        }
      }}
    )
  }
}