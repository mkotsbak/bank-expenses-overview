
import scala.io.{Codec, Source}

/**
  * Created by marius on 23.04.16.
  */
object TestGjensidige extends App {
    val inputLines = Source.fromFile(args.head)(Codec.UTF8).getLines()
    val transRes = GjensidigeBankImporter.parseCSVString(inputLines)

    val res = transRes map { trans =>
        println(s"Name: ${trans.headOption.map(_.description).getOrElse("no transactions")}\n" +
          s"Trans:\n" + trans.mkString("\n"))

        ExpensesCalculation.calculateExpenses(trans)
    }
    println(res.map(_.mkString("\n")).getOrElse("Error"))
}
