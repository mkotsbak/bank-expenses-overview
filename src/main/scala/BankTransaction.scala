import java.time.LocalDate

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

    def calculateExpenses(transactions: Seq[BankTransaction]): Seq[(String, BigDecimal)] = {
        groupExpenses(transactions.map { trans =>
            trans.description -> trans.amount
        })
    }

    def groupExpenses(transactions: Seq[(String, BigDecimal)]): Seq[(String, BigDecimal)] = {
        transactions.groupBy(_._1).mapValues(_.map(_._2).sum).toSeq.sortBy(_._2)
    }
}
