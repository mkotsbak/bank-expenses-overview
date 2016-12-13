name := "bank-expenses-overview"

version := "1.0"

val scalaV = "2.12.1" //"2.11.8"
scalaVersion := scalaV
val reactVersion = "15.4.1"

resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.sonatypeRepo("snapshots")

// libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
// libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

lazy val engine = (crossProject.crossType(CrossType.Pure) in file("engine")).settings(
  scalaVersion := scalaV,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-java-time" % "0.2.0",
    "org.typelevel" %%% "cats" % "0.8.1"
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
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.11.3"
  ),

  jsDependencies ++= Seq(
  "org.webjars.bower" % "react" % reactVersion / "react-with-addons.js"
    minified "react-with-addons.min.js"
    commonJSName "React",

  "org.webjars.bower" % "react" % reactVersion / "react-dom.js"
    minified  "react-dom.min.js"
    dependsOn "react-with-addons.js"
    commonJSName "ReactDOM",

  "org.webjars.bower" % "react" % reactVersion
    /         "react-dom-server.js"
    minified  "react-dom-server.min.js"
    dependsOn "react-dom.js"
    commonJSName "ReactDOMServer")

).enablePlugins(ScalaJSPlugin).dependsOn(engineJs)
