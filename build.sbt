ThisBuild / organization := "ru.tinkoff"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / scalacOptions ++= Seq("-feature",
                                  "-deprecation",
                                  "-unchecked",
                                  "-language:postfixOps",
                                  "-language:higherKinds",
                                  "-language:implicitConversions",
                                  "-Ypartial-unification")

val commons = addCompilerPlugin(
  "org.spire-math" %% "kind-projector" % "0.9.9") ::
  addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) ::
  (resolvers += Resolver.sonatypeRepo("releases")) ::
  Nil

lazy val core = (project in file("core"))
  .settings(commons: _*)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value
    ),
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % Version.catsEffect,
    libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.15.0"
  )

lazy val apiDsl = (project in file("api-dsl"))
  .dependsOn(core)
  .settings(commons: _*)
  .settings(
    name := "api-dsl",
    libraryDependencies += "ru.tinkoff" %% "typed-schema" % Version.tschema % Provided,
  )

lazy val akkaHttp = (project in file("akka-http"))
  .dependsOn(apiDsl, core, client)
  .enablePlugins(JavaAppPackaging)
  .settings(commons: _*)
  .settings(
    name := "akka-http",
    libraryDependencies += "ru.tinkoff" %% "typed-schema" % Version.tschema,
    libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCirce,
    libraryDependencies += "org.typelevel" %% "cats-effect" % Version.catsEffect,
    libraryDependencies += "com.github.mpilquist" %% "simulacrum" % Version.simulacrum,
    libraryDependencies += "com.lihaoyi" %% "scalatags" % Version.scalaTags,
    libraryDependencies += "io.circe" %% "circe-config" % Version.circeConfig,
  )
  .settings(
    resourceGenerators in Compile += Def.task {
      val fullOpt = (fullOptJS in Compile in client).value
      fullOpt.data :: Nil
    }.taskValue
  )

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "client",
    scalaJSUseMainModuleInitializer := true
  )
  .settings(
    libraryDependencies += "com.lihaoyi" %%% "scalatags" % Version.scalaTags,
  )

lazy val codefest = (project in file("."))
  .aggregate(akkaHttp)
