import cats.data.Ior

/**
  * Created by marius on 23.04.16.
  */

trait CSVImporter {
    def importFromCSV(header: Seq[String], csvInput: List[Seq[String]]): String Ior List[BankTransaction]
}
