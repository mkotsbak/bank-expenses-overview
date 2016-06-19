
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

    def calculateExpenses(transactions: List[BankTransaction]): Seq[(String, BigDecimal)] = {
        val groupedByShop = groupExpenses(transactions.map { trans =>
            trans.description -> trans.amount
        })

        println("By shop:\n" + groupedByShop.mkString("\n"))
        val groupedByCategory = groupedByShop.map { case (shop, amount) =>
            mapShopToCategory(shop).toString -> amount
        }

        groupExpenses(groupedByCategory)
    }

    def groupExpenses(transactions: Seq[(String, BigDecimal)]): Seq[(String, BigDecimal)] = {
        transactions.groupBy(_._1).mapValues(_.map(_._2).sum).toSeq.sortBy(_._2)
    }

    object Category extends Enumeration {
        val Groceries = Value("Dagligvare")
        val Gasoline = Value("Bensin")
        val Clothes = Value("Klær")
        val Unknown = Value("Ukjent")
    }

    val shopToCategory = {
        import Category._

        Map[String, Category.Value](
            "coop obs" -> Groceries
            , "Coop Mega" -> Groceries
            , "Extra" -> Groceries
            , "Bunnpris" -> Groceries
            , "Rema" -> Groceries
            , "Kiwi" -> Groceries
            , "Europris" -> Groceries

            , "Statoil" -> Gasoline
            , "CIRCLE K" -> Gasoline
            , "Shell" -> Gasoline

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
