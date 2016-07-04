import java.time.LocalDate

import cats.data.{Ior, Xor}

import scala.util.Try

/**
  * Created by marius on 23.04.16.
  */


object GjensidigeBankImporter extends CSVImporter {
  def parseCSVString(csvInput: Iterator[String]): String Ior List[BankTransaction] = {
    val res = csvInput.filter(_.trim.length > 0).
      map(_.split('\t').toSeq).toList

    importFromCSV(res.head, res.tail)
  }

  def parseDate(date: String): String Xor LocalDate = {
    //LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    val pattern = "(\\d+)\\.(\\d+)\\.(\\d+)".r
    date match {
      case pattern(day, month, year) =>
        Xor.fromTry(
          Try {
            LocalDate.of(year.toInt, month.toInt, day.toInt)
          }
        ).leftMap(_.toString)
      case _ => Xor.Left("Invalid date: " + date)
    }

  }

  override def importFromCSV(header: Seq[String], csvInput: List[Seq[String]]): String Ior List[BankTransaction] = {
        import cats.Semigroup
        import cats.data.{NonEmptyList, OneAnd, Validated, ValidatedNel, Xor}
        import cats.implicits._
        import cats.std.option

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
            case "Varekjøp" => Some(
              parseDate(date).toIor.map(parsedDate =>
                GoodsBuy(transactionDate = parsedDate, buyDate = buyDate, description = description, amount = amount)
              )
            )
            case _ => None
          }
        }.sequenceU
    }
}
