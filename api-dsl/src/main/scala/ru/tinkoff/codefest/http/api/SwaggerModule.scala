package ru.tinkoff.codefest.http.api

import java.util.Locale

import scalatags.Text.all._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import ru.tinkoff.tschema.swagger.{OpenApiInfo, PathDescription, SwaggerBuilder}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

object SwaggerModule {

  def cssref(s: String) = link(href := s, rel := "stylesheet")
  def js(s: String) = script(src := s)

  def webjar(s: String) = s"/webjars/swagger-ui-dist/3.17.2/$s"
  val index = html(
    meta(charset := "UTF-8"),
    tag("title")("Typed Schema Swagger UI"),
    cssref(
      "https://fonts.googleapis.com/css?family=Open+Sans:400,700|Source+Code+Pro:300,600|Titillium+Web:400,600,700"
    ),
    cssref(webjar("swagger-ui.css")),
    tag("style")(indexStyle),
    body(
      div(id := "swagger-ui"),
      js(webjar("swagger-ui-bundle.js")),
      js(webjar("swagger-ui-standalone-preset.js")),
      script(onload)
    )
  )

  def indexStyle =
    raw("""
            |html{
            |      box-sizing: border-box;
            |      overflow: -moz-scrollbars-vertical;
            |      overflow-y: scroll;
            |    }
            |    *,
            |    *:before,
            |    *:after
            |    {
            |      box-sizing: inherit;
            |    }
            |
            |    body {
            |      margin:0;
            |      background: #fafafa;
            |    }""".stripPrefix("|"))

  def onload =
    raw("""
            |window.onload = function() {
            |
            |  // Build a system
            |  const ui = SwaggerUIBundle({
            |    url: "/swagger",
            |    dom_id: '#swagger-ui',
            |    deepLinking: true,
            |    presets: [
            |      SwaggerUIBundle.presets.apis,
            |      SwaggerUIStandalonePreset
            |    ],
            |    plugins: [
            |      SwaggerUIBundle.plugins.DownloadUrl
            |    ],
            |    layout: "StandaloneLayout"
            |  })
            |
            |  window.ui = ui
            |}
          """.stripMargin)

  val descriptions = PathDescription.utf8I18n("swagger", Locale.forLanguageTag("ru"))

  def routes(builder: SwaggerBuilder) =
    path("swagger")(
      get(
        complete(
          builder
            .describe(descriptions)
            .make(OpenApiInfo())
            .addServer("/api")
        )
      )
    ) ~
    pathPrefix("webjars")(getFromResourceDirectory("META-INF/resources/webjars")) ~
    path("swagger.html")(
      complete(
        HttpResponse(
          entity = HttpEntity(contentType = ContentTypes.`text/html(UTF-8)`, string = index.render)
        )
      )
    )
}
