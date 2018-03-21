
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    Try {
      LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }.toEither.left.map(_ => "Invalid date: " + date)

    /*
    val pattern = "(\\d+)\\.(\\d+)\\.(\\d+)".r
    date match {
      case pattern(day, month, year) =>
        Try {
          LocalDate.of(year.toInt, month.toInt, day.toInt)
        }.toEither.left.map(_.toString)
      case _ => Left("Invalid date: " + date)
    }
    */
  }

  override def importFromCSV(header: Seq[String], csvInput: List[Seq[String]]): String Either List[BankTransaction] = {
    import cats.implicits._

    def value(field: String) = {
      val idx = header.indexOf(field)
      if (idx < 0) Left(s"Field $field not found")
      else Right((line: Seq[String]) => line(idx))
    }

    val BuyDate = "(\\*\\d+)?.?(\\d+\\.\\d+)? (.*)".r

    for (
      descriptionRaw <- value("Beskrivelse");
      amountRow <- value("Beløp");
      tekstkode <- value("Tekstkode");
      transactions <- {
        csvInput.flatMap { line =>

        val date = line.head
        val amount = BigDecimal(amountRow(line).replace(',', '.'))

        val (buyDate, description: String) = descriptionRaw(line) match {
          case BuyDate(_, aBuyDate, text) => (Some(aBuyDate), text)
          case _ => (None, descriptionRaw)
        }

        val kjopt = "KJ.PT".r
        tekstkode(line) match {
          case "VARER" | "VISA VARE" => Some(
            parseDate(date).map(parsedDate =>
              GoodsBuy(transactionDate = parsedDate, buyDate = buyDate, description = description, amount = amount)
            )
          )
          case _ => None
        }
      }.sequenceU
    }) yield transactions
  }
}
