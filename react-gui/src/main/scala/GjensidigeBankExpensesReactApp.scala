/**
  * Created by marius on 17.06.16.
  */


import java.util.Locale

import ExpensesCalculation.{Category, CatogoryExpense, ShopExpense}
import org.scalajs.dom
import org.scalajs.dom.raw.FileReader
import org.scalajs.dom.{File, ProgressEvent, document}
import java.time.Month
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit

import dom.window

object GjensidigeBankExpensesReactApp {

  import japgolly.scalajs.react.vdom.html_<^._
  import japgolly.scalajs.react._

  val SelectFile =
    ScalaComponent.builder[File => Callback]("No args")
        .render_P { props =>
        <.div("Hello!",
          "Select input file:",
          <.input(^.`type` := "file", ^.onChange ==> { e: ReactEventFromInput =>
            props(e.target.files(0))
          }
          )
        )
      }.build

  val Transactions = ScalaComponent.builder[List[BankTransaction]]("Results")
    .render_P { props =>
      <.div(<.b("Transactions:"), <.br,
        props.toVdomArray { tx =>
          <.span(tx.toString, <.br)
        }
      )
    }.build

  val ExpensesPerMonth = ScalaComponent.builder[Map[Month, BigDecimal]]("Sums per month")
    .render_P { props =>
      <.table(
        <.thead(
          <.td("Month"),
          <.td("Sum")
        ),
        <.tbody(
          props.toList.sortBy(_._1.getValue).toVdomArray { case (month, sum) =>
            <.tr(
              <.td(month.getDisplayName(TextStyle.FULL, Locale.getDefault).capitalize), <.td(sum.toString)
            )
          }
        )
      )
    }.build

  val ShopResult = ScalaComponent.builder[ShopExpense]("Category result")
    .initialState(false)
    .renderPS { ($, props, state) =>
      <.tr(
        <.td(props.shopName),
        <.td(props.sum.toString()),
        <.td(
          <.button(s"Details (${props.transactions.size}) ${if (state) "<-<" else "++>"}",
            ^.onClick --> $.modState(!_)),
          Transactions(props.transactions).when(state)
        )
      )}.build

  val ShopResults = ScalaComponent.builder[List[ShopExpense]]("Results")
    .render_P { props =>
      <.div(<.b("Results:"),
        <.table(
          props.toVdomArray(
            ShopResult(_)
          )
        )
      )
    }.build

  val CategoryResult = ScalaComponent.builder[(CatogoryExpense, Float)]("Category result")
    .initialStateFromProps(_._1.category == Category.Unknown)
    .renderPS { ($, props, state) =>
      val category = props._1
      <.tr(
        <.td(category.category.toString),
        <.td(category.sum.toString()),
        <.td(category.avgPerMonth(props._2)),
        <.td(
          <.button(s"Details (${category.shopExpenses.size}) ${if (state) "<--" else "++>"}",
            ^.onClick --> $.modState(!_)),
          ShopResults(category.shopExpenses).when(state),
          ExpensesPerMonth(category.perMonth).when(state)
        )
      )}.build

  val Results = ScalaComponent.builder[(List[CatogoryExpense], Float)]("Results")
    .render_P { props =>
      <.div(<.b("Results:"),
        <.table(
          <.thead(
            <.tr(
              <.td("Category"), <.td("Sum"), <.td("Per month"), <.td("Details")
            )
          ),
          <.tbody(
            props._1.toVdomArray( category =>
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
            val fileContent = fr.result.asInstanceOf[String].linesIterator
            GjensidigeBankImporter.parseCSVString(fileContent)
              .left.map(window.alert)
              .foreach { transactions =>
                val res = ExpensesCalculation.calculateExpenses(transactions)
                $.modState(_.copy(transactions = transactions, results = res)).runNow()
              }
          }
        }
    }
  }

  val MainView = ScalaComponent.builder[Unit]("Main view")
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

  def main(args: Array[String]): Unit = {

    //import locales.cldr.data.nb_NO

    // TODO: use https://github.com/cquiroz/sbt-locales to generate more locales?
    // Install the locale
    //LocaleRegistry.installLocale(nb_NO)
    //Locale.setDefault(Locale.forLanguageTag("nb-NO"))

    try {
      MainView().renderIntoDOM(document.getElementById("reactapp"))
    }
    catch {
      case ex: Exception => ex.printStackTrace()
    }
  }
}
