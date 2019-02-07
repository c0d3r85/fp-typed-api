ThisBuild / organization := "ru.tinkoff"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / scalacOptions ++= Seq("-feature",
                                  "-deprecation",
                                  "-unchecked",
                                  "-language:postfixOps",
                                  "-language:higherKinds",
                                  "-Ypartial-unification")

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val core = (project in file("core"))
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
  .settings(
    name := "api-dsl",
    libraryDependencies += "ru.tinkoff" %% "typed-schema" % Version.tschema % Provided,
  )

lazy val akkaHttp = (project in file("akka-http"))
  .dependsOn(apiDsl, core)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "akka-http",
    libraryDependencies += "ru.tinkoff" %% "typed-schema" % Version.tschema,
    libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCirce,
    libraryDependencies += "org.typelevel" %% "cats-effect" % Version.catsEffect,
    libraryDependencies += "com.github.mpilquist" %% "simulacrum" % Version.simulacrum,
  )

lazy val codefest = (project in file("."))
  .aggregate(akkaHttp)
