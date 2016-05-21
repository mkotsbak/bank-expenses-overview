/**
  * Created by marius on 23.04.16.
  */

trait CSVImporter {
    def importFromCSV(header: Seq[String], csvInput: Seq[Seq[String]]): Seq[BankTransaction]
}
