ThisBuild / organization := "ru.tinkoff"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ypartial-unification"
)

val commons = addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9") ::
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) ::
  (resolvers += Resolver.sonatypeRepo("releases")) ::
  (libraryDependencies += "com.github.mpilquist" %% "simulacrum" % Version.simulacrum) ::
  Nil

lazy val core = (project in file("core"))
  .settings(commons: _*)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value
    ),
    libraryDependencies += "com.github.mauricio" %% "postgresql-async" % "0.2.21",
    libraryDependencies += "io.getquill" %% "quill" % Version.quill,
    libraryDependencies += "org.typelevel" %% "cats-core" % Version.cats,
    libraryDependencies += "org.typelevel" %% "cats-effect" % Version.catsEffect,
    libraryDependencies += "com.bot4s" %% "telegram-core" % Version.telegramBot,
  )

lazy val apiDsl = (project in file("api-dsl"))
  .dependsOn(core)
  .settings(commons: _*)
  .settings(
    name := "api-dsl",
    libraryDependencies += "ru.tinkoff" %% "typed-schema" % Version.tschema,
    libraryDependencies += "com.bot4s" %% "telegram-core" % Version.telegramBot,
  )

lazy val akkaHttp = (project in file("akka-http"))
  .dependsOn(apiDsl, core)
  .enablePlugins(JavaAppPackaging)
  .settings(commons: _*)
  .settings(
    name := "akka-http",
    libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCirce,
    libraryDependencies += "org.typelevel" %% "cats-effect" % Version.catsEffect,
    libraryDependencies += "io.circe" %% "circe-config" % Version.circeConfig,
    libraryDependencies += "com.softwaremill.sttp" %% "async-http-client-backend-cats" % Version.sttpBackend,
  )

lazy val codefest = (project in file("."))
  .aggregate(akkaHttp)
