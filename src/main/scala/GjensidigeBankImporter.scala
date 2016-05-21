import java.time.LocalDate
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.Locale

/**
  * Created by marius on 23.04.16.
  */


object GjensidigeBankImporter extends CSVImporter {
  def parseCSVString(csvInput: Iterator[String]): Seq[BankTransaction] = {
    val res = csvInput.filter(_.trim.length > 0).
      map(_.split('\t').toSeq).toSeq

    importFromCSV(res.head, res.tail)
  }

  override def importFromCSV(header: Seq[String], csvInput: Seq[Seq[String]]): Seq[BankTransaction] = {
        csvInput.flatMap { line =>
            def value(field: String) = line(header.indexOf(field))

            val date = line.head
            val descriptionRaw = value("Beskrivelse")
            val amount = BigDecimal(value("Beløp"))

            val BuyDate = "(\\*\\d+)?.?(\\d+\\.\\d+)? (.*)".r
            val (buyDate, description) = descriptionRaw match {
              case BuyDate(_, aBuyDate, text) => (Some(aBuyDate), text)
              case _ => (None, descriptionRaw)
            }

            val kjopt = "KJ.PT".r
              value("Type") match {
                case "Varekjøp" => Seq(
                    GoodsBuy(transactionDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy")), buyDate = buyDate, description = description, amount = amount))
                case _ => Seq.empty
            }
        }
    }
}
