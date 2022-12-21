import java.time.LocalDateTime
import java.time.ZoneOffset

object Seeder {
  private val _rand = new scala.util.Random(100)
  private def _pickRandom[A](arr: Array[A]): A = 
    arr(_rand.nextInt(arr.length))

  private def _randomPrice(max: Long):BigDecimal =
    BigDecimal(_rand.nextLong(max-5))/100d + 0.05d // minimum price is 0.05d
  private def _randomDate(min:LocalDateTime):LocalDateTime =
      LocalDateTime.ofEpochSecond(
        _rand.between(
          min.toEpochSecond(ZoneOffset.UTC),
          LocalDateTime.now.toEpochSecond(ZoneOffset.UTC)
        ),
        0, ZoneOffset.UTC)
  

  def productSeeder(quantity:Int, maxProductYears:Int) : Array[Product] =
    Array.tabulate(quantity)(_ => Product(
      name = _pickRandom(Array("Pen","Paper","Eraser","Computer","Mouse","Speakers")),
      category = _pickRandom(Array("Technology","Office","School")),
      weight = BigDecimal(_rand.nextInt(50)) / 100d + 0.01d,
      price = _randomPrice(500),
      creationDate = _randomDate(LocalDateTime.now.minusYears(maxProductYears))
    ))

  def orderSeeder(quantity: Int, maxCartSize: Int, allProducts: Array[Product]) : Array[Order] =
    Array.tabulate(quantity)(_ => {
      val cart = List.tabulate(_rand.nextInt(maxCartSize)+1)(_ => Item(
        shippingFee = _randomPrice(30),
        taxFee = _randomPrice(30),
        product = _pickRandom(allProducts)
      ))
      Order(
        customerName = _pickRandom(Array("John", "Mary", "Oliver", "Bart", "Finn")),
        customerContact = "+3519"+_rand.nextInt(99999999),
        shippingAddress = _pickRandom(Array("Lisbon", "Washington DC", "Miami", "New York", "Paris")),

        // orderDate can't be older than newest item created, for realism
        orderDate = {
          val newestItemDate = cart.foldLeft(LocalDateTime.now)((accDate,item) => {
            val itemDate = item.product.creationDate
            if (itemDate.compareTo(accDate) < 1) itemDate else accDate
          })
          _randomDate(newestItemDate)
        },
        cart
      )
    })
}
