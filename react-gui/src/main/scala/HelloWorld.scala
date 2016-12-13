/**
  * Created by marius on 17.06.16.
  */


import java.time.temporal.ChronoUnit

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
          <.button(s"Details (${props.transactions.size}) ${if (state) "<-<" else "++>"}",
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

  val CategoryResult = ReactComponentB[(CatogoryExpense, Float)]("Category result")
    .initialState_P(_._1.category == Category.Unknown)
    .renderPS { ($, props, state) =>
      val category = props._1
      <.tr(
        <.td(category.category.toString),
        <.td(category.sum.toString()),
        <.td(category.sum.toFloat / props._2),
        <.td(
          <.button(s"Details (${category.shopExpenses.size}) ${if (state) "<--" else "++>"}",
            ^.onClick --> $.modState(!_)),
          state ?= ShopResults(category.shopExpenses)
        )
      )}.build

  val Results = ReactComponentB[(List[CatogoryExpense], Float)]("Results")
    .render_P { props =>
      <.div(<.b("Results:"),
        <.table(
          <.thead(
            <.tr(
              <.td("Category"), <.td("Sum"), <.td("Per month"), <.td("Details")
            )
          ),
          <.tbody(
            props._1.map( category =>
              CategoryResult( (category, props._2) )
            )
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
          fr.readAsText(file, "LATIN1")
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
        val monthsInPeriod = {
          for {
            last <- $.state.transactions.lastOption
            first <- $.state.transactions.headOption
          } yield ChronoUnit.DAYS.between(last.transactionDate, first.transactionDate) / 30.5f
        }.getOrElse(0f)

        <.div(
          SelectFile($.backend.handleFileSelected),
          Results( ($.state.results, monthsInPeriod) ),
          Transactions($.state.transactions)
        )
    }.build

  def main(): Unit = {
    ReactDOM.render(MainView(), document.body)
  }
}
