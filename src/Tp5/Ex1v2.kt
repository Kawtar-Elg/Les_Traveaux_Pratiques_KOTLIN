package Tp6

sealed class Payment

class CashPayment(val amount: Double) : Payment()
class CardPayment(val amount: Double, val cardNumber: String) : Payment()
class DigitalPayment(val amount: Double, val cardNumber: String) : Payment()

fun print_Details(payment : Payment )  {
    val Payment = when (payment) {
        is CashPayment -> print( "type de paiment en Cash : ${payment.amount}" )
        is CardPayment ->  print( "type de paiment en Carte : ${payment.cardNumber}")
        is DigitalPayment -> print( "type de paiment onlin : ${payment.cardNumber} ,  ${payment.amount}")
        else ->  print("no payment")

    }

}
fun main ()  {
    val payment1 = CashPayment(300.0)
    val payment2 = CardPayment ( 20.0 , "3454P")
    val payment3 = DigitalPayment ( 56.0 , "34256M")

    print_Details(payment1)
    print_Details(payment2)
    print_Details(payment3)

}