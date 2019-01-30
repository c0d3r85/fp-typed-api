ThisBuild / organization := "ru.tinkoff"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ypartial-unification")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0" % Provided,
    libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.15.0",
  )

lazy val apiDsl = (project in file("api-dsl"))
  .dependsOn(core)
  .settings(
    name := "api-dsl",
    libraryDependencies += "ru.tinkoff" %% "typed-schema" % "0.10.7.1" % Provided,
  )

lazy val akkaHttp = (project in file("akka-http"))
  .dependsOn(apiDsl)
  .settings(
    name := "akka-http",
    libraryDependencies += "ru.tinkoff" %% "typed-schema" % "0.10.7.1",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "1.2.0",
  )

lazy val codefest = (project in file("."))
  .aggregate(akkaHttp)
