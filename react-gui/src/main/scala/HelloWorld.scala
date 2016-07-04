/**
  * Created by marius on 17.06.16.
  */


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
            props(e.target.files(0))
          }
          )
        )
      }.build

  val Transactions = ReactComponentB[List[BankTransaction]]("Results")
    .render_P { props =>
      <.div(<.b("Transactions:"), <.br,
        props.map { tx =>
          <.span(tx.toString, <.br)
        }
      )
    }.build

  val Results = ReactComponentB[Seq[(String, BigDecimal)]]("Results")
    .render_P { props =>
      <.div(<.b("Results:"),
        <.table(
          props.map { cat =>
            <.tr(
              <.td(cat._1),
              <.td(cat._2.toString())
            )
          }
        )
      )
    }.build

  case class MainState(selectedFile: Option[File], transactions: List[BankTransaction], results: Seq[(String, BigDecimal)])

  class MainBackend($: BackendScope[Unit, MainState]) {
    def handleFileSelected(file: File): Callback = {
      $.modState(_.copy(selectedFile = Some(file))) >>
        Callback {
          val fr = new FileReader
          fr.readAsText(file)
          fr.onloadend = { ev: ProgressEvent =>
            val fileContent = fr.result.asInstanceOf[String].lines
            GjensidigeBankImporter.parseCSVString(fileContent)
              .map { transactions =>
                val res = ExpensesCalculation.calculateExpenses(transactions)
                $.modState(_.copy(transactions = transactions, results = res)).runNow()
              }
          }
        }
    }
  }

  val MainView = ReactComponentB[Unit]("Main view")
      .initialState[MainState](MainState(None, List.empty, Seq.empty))
      .backend(new MainBackend(_))
      .render { $ =>
        <.div(
          SelectFile($.backend.handleFileSelected),
          Transactions($.state.transactions),
          Results($.state.results)
        )
    }.build

  def main(): Unit = {
    ReactDOM.render(MainView(), document.body)
  }
}
