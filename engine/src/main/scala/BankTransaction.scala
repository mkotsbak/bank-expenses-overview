
import java.time.LocalDate

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
        s"Date: $transactionDate, buy date: ${buyDate.getOrElse("N/A")}, amount: $amount, description: $description"
    }
}

case class GoodsBuy(transactionDate: LocalDate, buyDate: Option[String], description: String, amount: BigDecimal) extends BankTransaction
case class MoneyTransfer(transactionDate: LocalDate, buyDate: Option[String], description: String, amount: BigDecimal) extends BankTransaction
case class Fee(transactionDate: LocalDate, buyDate: Option[String], description: String, amount: BigDecimal) extends BankTransaction

object ExpensesCalculation {

    def calculateExpenses(transactions: List[BankTransaction]):
      Seq[(ExpensesCalculation.Category.Value, List[BankTransaction], BigDecimal)] = {
        val groupedByShop = groupExpensesByShop(transactions)

        println("By shop:\n" + groupedByShop.mkString("\n"))
        val groupedByCategory = groupedByShop.map { case (shop, txs, amount) =>
            (mapShopToCategory(shop), txs, amount)
        }

        groupExpensesByCategories(groupedByCategory)
    }

    def groupExpensesByShop(transactions: List[BankTransaction]): Seq[(String, List[BankTransaction], BigDecimal)] = {
        transactions.groupBy(_.description).map(txs =>
            (txs._1, txs._2, txs._2.map(_.amount).sum)
        ) .toSeq.sortBy(_._3)
    }

    def groupExpensesByCategories(transactions: Seq[(Category.Value, List[BankTransaction], BigDecimal)]):
      Seq[(ExpensesCalculation.Category.Value, List[BankTransaction], BigDecimal)] ={
        transactions.groupBy(_._1).map(txs =>
            (txs._1 , txs._2.flatMap(_._2).toList, txs._2.map(_._3).sum)
        ).toSeq.sortBy(_._3)
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
