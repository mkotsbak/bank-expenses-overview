import java.time.LocalDate

import cats.data.Ior

import scala.util.Try

/**
  * Created by marius on 23.04.16.
  */


object GjensidigeBankImporter extends CSVImporter {
  def parseCSVString(csvInput: Iterator[String]): String Either List[BankTransaction] = {
    val res = csvInput.filter(_.trim.length > 0).
      map(_.split('\t').toSeq).toList

    importFromCSV(res.head, res.tail)
  }

  def parseDate(date: String): String Either LocalDate = {
    //LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    val pattern = "(\\d+)\\.(\\d+)\\.(\\d+)".r
    date match {
      case pattern(day, month, year) =>
        Try {
          LocalDate.of(year.toInt, month.toInt, day.toInt)
        }.toEither.left.map(_.toString)
      case _ => Left("Invalid date: " + date)
    }

  }

  override def importFromCSV(header: Seq[String], csvInput: List[Seq[String]]): String Either List[BankTransaction] = {
    import cats.implicits._

    csvInput.flatMap { line =>
      def value(field: String) = line(header.indexOf(field))

      val date = line.head
      val descriptionRaw = value("Beskrivelse")
      val amount = BigDecimal(value("Beløp").replace(',', '.'))

      val BuyDate = "(\\*\\d+)?.?(\\d+\\.\\d+)? (.*)".r
      val (buyDate, description) = descriptionRaw match {
        case BuyDate(_, aBuyDate, text) => (Some(aBuyDate), text)
        case _ => (None, descriptionRaw)
      }

      val kjopt = "KJ.PT".r
      value("Tekstkode") match {
        case "VARER" | "VISA VARE" => Some(
          parseDate(date).map(parsedDate =>
            GoodsBuy(transactionDate = parsedDate, buyDate = buyDate, description = description, amount = amount)
          )
        )
        case _ => None
      }
    }.sequenceU
  }
}
