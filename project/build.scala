import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.earldouglas.xwp.JettyPlugin
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object UserServiceBuild extends Build {
  val Organization = "com.shuttleql"
  val Name = "User Service"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.7"
  val ScalatraVersion = "2.4.1"

  lazy val project = Project (
    "user-service",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
      resolvers += "RoundEights" at "http://maven.spikemark.net/roundeights",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.2.15.v20160210" % "container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.json4s" %% "json4s-jackson" % "3.3.0",
        "com.typesafe.slick" %% "slick" % "3.1.1",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "org.postgresql" % "postgresql" % "9.4.1211",
        "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
        "com.roundeights" %% "hasher" % "1.2.0",
        "org.mindrot" % "jbcrypt" % "0.3m",
        "com.typesafe" % "config" % "1.3.1",
        "com.shuttleql" %% "gandalf" % "1.0"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  ).enablePlugins(JettyPlugin)
}
