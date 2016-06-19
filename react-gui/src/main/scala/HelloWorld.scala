/**
  * Created by marius on 17.06.16.
  */


import scala.scalajs.js.JSApp

object TutorialApp extends JSApp {
  def main(): Unit = {
    println("Hello world!")
    val res = GjensidigeBankImporter.parseCSVString(
      Seq("Dato\tType\tBeskrivelse\tBeløp",
        "01.01.2016\tVarekjøp\t28.04 COOP OBS! LADE HAAKON VII-S TRONDHEIM\t-384.56",
        "02.01.2016\tVarekjøp\t28.04 COOP OBS! BYGG HAAKON VIIGT TRONDHEIM\t-343.9").iterator)
    println("Res: \n" + res.map(_.toString()).getOrElse("Ingen"))
  }
}
