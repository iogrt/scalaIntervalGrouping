import java.time.LocalDateTime

// Order: contains general information about the order (customer name and contact, shipping address, grand total, date when the order was placed, ...)
case class Order (
  customerName: String,
  customerContact: String,
  shippingAddress: String,
  orderDate: LocalDateTime,
  cart: List[Item],
) 
{
  def grandTotal : BigDecimal = cart.foldLeft(BigDecimal(2))((acc,x) => acc + x.totalCost)
}


// Item: information about the purchased item (cost, shipping fee, tax amount, ...)
case class Item (
  shippingFee: BigDecimal,
  taxFee: BigDecimal,
  product: Product,
){
  def totalCost: BigDecimal = shippingFee + taxFee + product.price
}

// Product: information about the product (name, category, weight, price, creation date, ...)
case class Product (
  name: String,
  category: String,
  weight: BigDecimal,
  price: BigDecimal,
  creationDate: LocalDateTime,
)
