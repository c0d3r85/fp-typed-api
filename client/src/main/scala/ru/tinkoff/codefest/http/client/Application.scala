package ru.tinkoff.codefest.http.client

import org.scalajs.dom.raw.Event
import scalatags.JsDom.all._

object Components {
  def hello(who: String) = {
    span(s"Hello, $who!")
  }
}

object Application {

  import Components._

  def main(args: Array[String]): Unit = {
    import org.scalajs.dom
    import dom.document

    def initApp() = {
      val appRoot = document.getElementById("app")
      val rendered = hello("Peka").render
      appRoot.appendChild(rendered)
    }

    document.onreadystatechange = e => {
      if (document.readyState == "interactive") { initApp() }
    }
  }
}
