import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.PrivateMethodTester
import java.time.LocalDateTime
    
class ProductGrouperTest extends AnyFunSuite with PrivateMethodTester {
    val productAge = (age:LocalDateTime) =>  
        (ProductGrouper invokePrivate 
            (PrivateMethod[Int]('_productAge)
            (simpleProduct(age)))
        )
        
    val simpleProduct = creationDate => Product(
        name = "product",
        category = "Tech",
        weight = 1,
        price = 2,
        creationDate,
    )

    test("productAge of new item is 1") {
        assert(productAge(LocalDateTime.now) == 1)
    }
    test("productAge of 1 month old item is 1") {
        assert(productAge(LocalDateTime.now.minusMonths(1)) == 1 )
    }
    test("productAge of 1 month and 1 day old item is 2") {
        assert(
            productAge(LocalDateTime.now.minusMonths(1).minusDays(1))
            == 2
        )
    }
    test("productAge works with year differences") {
        assert(
            (productAge(LocalDateTime.now.minusYears(2)))
            == 24
        )
    }

    val makeProductGrouping = (intList: List[Int]) =>  
        (ProductGrouper invokePrivate 
            (PrivateMethod[Either[ProductGrouper.GroupingError,Array[Range.Inclusive]]]
            ('_makeProductGrouping)(intList))
        )

    test("product grouping: range starts where previous ends") {
        assert(
            (makeProductGrouping(List(1,5)))
            .map(arr => arr(0).end == 4 && arr(1).start == 5)
            .getOrElse(false)
        )
    }
    test("product grouping: Array is same size as list given") {
        assert(
            (makeProductGrouping(List(1,3,7,10,15,20,30,40,50)))
            .map(arr => arr.size)
            .getOrElse(-1)
            == 9
        )
    }
    test("product grouping: works with close intervals") {
        assert(
            (makeProductGrouping(List(1,2,3)))
            .map(arr => arr(0).start == arr(0).end)
            .getOrElse(false)
        )
    }
    test("product grouping: handles IncompleteInterval") {
        assert(
            (makeProductGrouping(List(2,4)))
            == Left(ProductGrouper.IncompleteInterval(2))
        )
    }
    test("product grouping: detects emptyList") {
        assert(
            (makeProductGrouping(List()))
            == Left(ProductGrouper.EmptyList)
        )
    }
    test("product grouping: detects unsorted list") {
        assert(
            (makeProductGrouping(List(1,5,4)))
            == Left(ProductGrouper.UnsortedList(5,4))
        )
    }
    test("product grouping: detects duplicate numbers") {
        assert(
            (makeProductGrouping(List(1,2,3,3)))
            == Left(ProductGrouper.Duplicate(3))
        )
    }

    val productFindGroup= (groups: ProductGrouper.ProductGrouping, age:Int) =>  
        (ProductGrouper invokePrivate 
            (PrivateMethod[Int]
            ('_productFindGroup)(groups,age))
        )
    test("productFindGroup: same output as naive implementation") {
        val valuesToFind = Array(10,1,9,2,8,3,7,4,7,5,6)
        assert(
            (makeProductGrouping(List(1,4,8,10,12)))
            .map(grouping => {
                valuesToFind.forall(age => {
                    val naiveImpl = grouping.indexWhere(r => r.contains(age))
                    naiveImpl == productFindGroup(grouping,age)
                })
            })
            .getOrElse(false)
        )
    }
    val orderFilter = (minDate: LocalDateTime,maxDate: LocalDateTime) => (order:Order) =>
        (ProductGrouper invokePrivate 
            (PrivateMethod[(Order => Boolean)]
            ('_orderFilter)(minDate,maxDate))
        )(order)
    val makeOrder = (orderDate:LocalDateTime) => Order("a","b","c",orderDate, List())

    test("orderFilter: filters orders before and after but keeps exact date limits") {
        val minDate = LocalDateTime.now.minusYears(1).minusMonths(1)
        val maxDate = LocalDateTime.now.minusMonths(1)
        val testOrders = List(
            // filter these
            minDate.minusYears(1),
            minDate.minusMonths(5),
            minDate.minusSeconds(1),

            // keep these
            minDate, // minDate
            minDate.plusWeeks(1),
            minDate.plusMonths(5),
            maxDate,

            // filter these
            maxDate.plusSeconds(1),
            maxDate.plusDays(1),
            maxDate.plusWeeks(1),
            maxDate.plusMonths(1),
        ).map(makeOrder)
        val filteredTestOrders = List(
                    minDate,
                    minDate.plusWeeks(1),
                    minDate.plusMonths(5),
                    maxDate,
                ).map(makeOrder)
        assert(
            testOrders.filter(orderFilter(minDate,maxDate))
            == filteredTestOrders
        )
    }
}
