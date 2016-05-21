import java.time.LocalDate
import java.util.Locale


object NordnetImporter extends CSVImporter {
    override def importFromCSV(header: Seq[String], csvInput: Seq[Seq[String]]): Seq[BankTransaction] = {
        csvInput.flatMap { line =>
            def value(field: String) = line(header.indexOf(field))

            val shareName = value("Verdipapir")
            val isin = value("ISIN")
            val count = value("Antall").toLong
            val buyDate = value("Handelsdag")
            val transactionDate = value("Oppgjørsdag")
            val fees = BigDecimal(value("Avgifter").toDouble)
            val exchangeRate = BigDecimal(value("Vekslingskurs"))
            val price = BigDecimal(value("Kurs").replace(" ", ""))
            val currency = value("Valuta")

            val kjopt = "KJ.PT".r
              value("Transaksjonstype") match {
                case kjopt() => Seq(
                    GoodsBuy(transactionDate = LocalDate.parse(transactionDate), buyDate = Some(buyDate), description = shareName, amount = price * count)
                )
                case _ => Seq.empty
            }
        }
    }
}
