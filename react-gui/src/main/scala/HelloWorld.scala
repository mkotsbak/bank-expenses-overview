/**
  * Created by marius on 17.06.16.
  */


import ExpensesCalculation.{Category, CatogoryExpense, ShopExpense}
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

  val ShopResult = ReactComponentB[ShopExpense]("Category result")
    .initialState(false)
    .renderPS { ($, props, state) =>
      <.tr(
        <.td(props.shopName),
        <.td(props.sum.toString()),
        <.td(
          <.button(s"Details (${props.transactions.size}) ${if (state) "-" else "+"} >",
            ^.onClick --> $.modState(!_)),
          state ?= Transactions(props.transactions)
        )
      )}.build

  val ShopResults = ReactComponentB[List[ShopExpense]]("Results")
    .render_P { props =>
      <.div(<.b("Results:"),
        <.table(
          props.map(
            ShopResult(_)
          )
        )
      )
    }.build

  val CategoryResult = ReactComponentB[CatogoryExpense]("Category result")
    .initialState_P(_.category == Category.Unknown)
    .renderPS { ($, props, state) =>
      <.tr(
        <.td(props.category.toString),
        <.td(props.sum.toString()),
        <.td(
          <.button(s"Details (${props.shopExpenses.size}) ${if (state) "-" else "+"} >",
            ^.onClick --> $.modState(!_)),
          state ?= ShopResults(props.shopExpenses)
        )
      )}.build

  val Results = ReactComponentB[List[CatogoryExpense]]("Results")
    .render_P { props =>
      <.div(<.b("Results:"),
        <.table(
          props.map(
            CategoryResult(_)
          )
        )
      )
    }.build

  case class MainState(selectedFile: Option[File], transactions: List[BankTransaction], results: List[CatogoryExpense])

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
      .initialState[MainState](MainState(None, List.empty, List.empty))
      .backend(new MainBackend(_))
      .render { $ =>
        <.div(
          SelectFile($.backend.handleFileSelected),
          Results($.state.results),
          Transactions($.state.transactions)
        )
    }.build

  def main(): Unit = {
    ReactDOM.render(MainView(), document.body)
  }
}
