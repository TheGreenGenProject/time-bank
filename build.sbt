ThisBuild / version := "1.0.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "time-bank",
    libraryDependencies := Seq(
      "org.typelevel" %% "cats-core" % "2.8.0",
      // Start with this one
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
      // And add any of these as needed
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC1", // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1", // Postgres driver 42.3.1 + type mappings.
      "org.tpolecat" %% "doobie-specs2" % "1.0.0-RC1" % "test", // Specs2 support for typechecking statements.
      "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC1" % "test" // ScalaTest support for typechecking statements.
    )
  )
