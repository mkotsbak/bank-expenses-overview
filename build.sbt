name := "bank-expenses-overview"

version := "1.0"

val scalaV = "2.11.8"
scalaVersion := scalaV

resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

lazy val engine = (crossProject.crossType(CrossType.Pure) in file("engine")).settings(
  scalaVersion := scalaV,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-java-time" % "0.1.0",
    "org.typelevel" %%% "cats" % "0.6.0"
    //"threetenbpcross" %%% "threetenbpcross" % "0.1-SNAPSHOT"
    //"org.mdedetrich" %%% "soda-time" % "0.0.1-SNAPSHOT"
  )
).enablePlugins(ScalaJSPlugin)

lazy val engineJvm = engine.jvm
lazy val engineJs = engine.js

lazy val cli = (project in file("cli")).settings(
  scalaVersion := scalaV
).dependsOn(engineJvm)

lazy val reactGui = (project in file("react-gui")).settings(
  scalaVersion := scalaV,
  persistLauncher in Compile := true,
  persistLauncher in Test := false,

  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.0",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.11.1"
  ),

  jsDependencies ++= Seq(
  "org.webjars.bower" % "react" % "15.1.0" / "react-with-addons.js"
    minified "react-with-addons.min.js"
    commonJSName "React",

  "org.webjars.bower" % "react" % "15.1.0" / "react-dom.js"
    minified  "react-dom.min.js"
    dependsOn "react-with-addons.js"
    commonJSName "ReactDOM",

  "org.webjars.bower" % "react" % "15.1.0"
    /         "react-dom-server.js"
    minified  "react-dom-server.min.js"
    dependsOn "react-dom.js"
    commonJSName "ReactDOMServer")

).enablePlugins(ScalaJSPlugin).dependsOn(engineJs)
