import scala.io.{Codec, Source}

/**
  * Created by marius on 23.04.16.
  */
object TestGjensidige extends App {
    val inputLines = Source.fromFile(args.head)(Codec.UTF8).getLines()
    val trans = GjensidigeBankImporter.parseCSVString(inputLines)

    println(s"Name: ${trans.headOption.map(_.description).getOrElse("no transactions")}\n" +
      s"Trans:\n" + trans.toList.mkString("\n"))

    val res = ExpensesCalculation.calculateExpenses(trans)
    println(res.mkString("\n"))
}
