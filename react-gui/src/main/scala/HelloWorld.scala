/**
  * Created by marius on 17.06.16.
  */


import japgolly.scalajs.react.{Callback, CallbackTo, ReactComponentB, ReactDOM}
import org.scalajs.dom
import org.scalajs.dom.raw.FileReader
import org.scalajs.dom.{File, ProgressEvent, document}

import scala.scalajs.js.JSApp

object TutorialApp extends JSApp {

  import japgolly.scalajs.react.vdom.prefix_<^._
  import japgolly.scalajs.react._

  val SelectFile =
    ReactComponentB[File => Callback]("No args")
        .render_P { props =>
        <.div("Hello!",
          "Select input file:",
          <.input(^.`type` := "file", ^.onChange ==> { e: ReactEventI =>
            Callback.alert("File: " + e.target.value + ", files: " + e.target.files.length + ", first: " + e.target.files(0).name ) >>
            props(e.target.files(0))
          }
          )
        )
      }.build

  val Results = ReactComponentB[List[BankTransaction]]("Results")
    .render_P { props =>
      <.div("Transactions:",
        props.map { tx =>
          <.p(tx.toString)
        }
      )
    }.build

  case class MainState(selectedFile: Option[File], results: List[BankTransaction])
  val MainView = ReactComponentB[Unit]("Main view")
      .initialState[MainState](MainState(None, List.empty))
      .render { $ =>
        <.div(
          SelectFile(
            { file =>
              dom.window.alert("File selected: " + file)
              $.modState(_.copy(selectedFile = Some(file))) >>
                Callback {
                  val fr = new FileReader
                  fr.readAsText(file)
                  fr.onloadend = { ev: ProgressEvent =>
                    val fileContent = fr.result.asInstanceOf[String].lines
                    GjensidigeBankImporter.parseCSVString(fileContent)
                      .map { res =>
                        println("Res: \n" + res.toString())
                        $.modState(_.copy(results = res)).runNow()
                      }
                  }
                }
            }),
          Results($.state.results)
        )
    }.build

  def main(): Unit = {
    println("Hello world!")
    val res = GjensidigeBankImporter.parseCSVString(
      Seq("Dato\tType\tBeskrivelse\tBeløp",
        "01.01.2016\tVarekjøp\t28.04 COOP OBS! LADE HAAKON VII-S TRONDHEIM\t-384.56",
        "02.01.2016\tVarekjøp\t28.04 COOP OBS! BYGG HAAKON VIIGT TRONDHEIM\t-343.9").iterator)
    println("Res: \n" + res.map(_.toString()).getOrElse("Ingen"))

    ReactDOM.render(MainView(), document.body)
  }
}
