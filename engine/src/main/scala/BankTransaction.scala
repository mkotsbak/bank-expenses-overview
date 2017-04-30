
import org.threeten.bp.{LocalDate, Month}
import org.threeten.bp.format.{DateTimeFormatter, FormatStyle}

import scala.util.matching.Regex

/**
  * Created by marius on 23.04.16.
  */

sealed abstract class BankTransaction {
    val transactionDate: LocalDate
    val buyDate: Option[String]
    val description: String
    //val currency: String
    val amount: BigDecimal

    override def toString = {
        s"Date: ${transactionDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}, buy date: ${buyDate.getOrElse("N/A")}, amount: $amount, description: $description"
    }
}

case class GoodsBuy(transactionDate: LocalDate, buyDate: Option[String], description: String, amount: BigDecimal) extends BankTransaction
case class MoneyTransfer(transactionDate: LocalDate, buyDate: Option[String], description: String, amount: BigDecimal) extends BankTransaction
case class Fee(transactionDate: LocalDate, buyDate: Option[String], description: String, amount: BigDecimal) extends BankTransaction

object ExpensesCalculation {

    def calculateExpenses(transactions: List[BankTransaction]): List[CatogoryExpense] = {
        val groupedByShop = groupExpensesByShop(transactions)

        println("By shop:\n" + groupedByShop.mkString("\n"))

        groupExpensesByCategories(groupedByShop)
    }

    case class ShopExpense(shopName: String, transactions: List[BankTransaction]) {
        lazy val sum = transactions.map(_.amount).sum
    }
    case class CatogoryExpense(category: Category.Value, shopExpenses: List[ShopExpense]) {
      lazy val sum = shopExpenses.map(_.sum).sum
      lazy val allTransactions = shopExpenses.flatMap(_.transactions)

      def avgPerMonth(monthsInPeriod: Float): Float = sum.toFloat / monthsInPeriod
      lazy val avg: Float = allTransactions.map(_.amount.toFloat).sum / allTransactions.size
        lazy val perMonth: Map[Month, BigDecimal] = {
            allTransactions.groupBy(_.transactionDate.getMonth).mapValues(_.map(_.amount).sum)
        }
    }

    def groupExpensesByShop(transactions: List[BankTransaction]): List[ShopExpense] = {
        transactions.groupBy(_.description).map(ShopExpense.tupled)
          .toList.sortBy(_.sum)
    }

    def groupExpensesByCategories(shopExpenses: List[ShopExpense]): List[CatogoryExpense] = {
        shopExpenses.groupBy(shopExpense => mapShopToCategory(shopExpense.shopName)).map(
          CatogoryExpense.tupled)
            .toList.sortBy(_.sum)
    }

    object Category extends Enumeration {
        val Groceries = Value("Dagligvare")
        val Gasoline = Value("Bensin")
        val Parking = Value("Parkering")
        val Clothes = Value("Klær")
        val Taxi = Value("Taxi")
        val Unknown = Value("Ukjent")
    }

    val shopToCategory = {
        import Category._

        Map[String, Category.Value](
            "coop obs" -> Groceries
            , "Coop Mega" -> Groceries
            , "Coop Prix" -> Groceries
            , "Coop Byggmix" -> Groceries
            , "Extra" -> Groceries
            , "Bunnpris" -> Groceries
            , "Rema" -> Groceries
            , "Kiwi" -> Groceries
            , "Europris" -> Groceries
            , "Meny" -> Groceries
            , "Joker" -> Groceries

            , "Statoil" -> Gasoline
            , "Esso" -> Gasoline
            , "Best" -> Gasoline
            , "CIRCLE K" -> Gasoline
            , "Shell" -> Gasoline

            , "Parkering" -> Parking
            , "Parker" -> Parking
            , "Time park" -> Parking

            , "Taxi" -> Taxi

            , "H&M" -> Clothes
        )
    }
    def mapShopToCategory(shop: String): ExpensesCalculation.Category.Value = {
        shopToCategory.filterKeys { shopNameKey: String =>
            val regex = new Regex(s".*${shopNameKey.toLowerCase}.*")
            shop.toLowerCase match {
                case regex() => true
                case _ => false
            }
        }.values.headOption.getOrElse(Category.Unknown)
    }
}
