
name := "bank-expenses-overview"

version := "1.0"

val scalaV = "2.13.3" //"2.11.8"
scalaVersion := scalaV
val reactVersion = "16.1.0"

resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.sonatypeRepo("snapshots")

// libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
// libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

lazy val engine = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).settings(
  scalaVersion := scalaV,
  libraryDependencies ++= Seq(
    //"org.scala-js" %%% "scalajs-java-time" % "0.2.0",
    "org.typelevel" %%% "cats-core" % "2.2.0",
    "io.github.cquiroz" %%% "scala-java-time" % "2.0.0"
    //"org.mdedetrich" %%% "soda-time" % "0.0.1-SNAPSHOT"
  )
).in(file("engine"))

lazy val cli = (project in file("cli")).settings(
  scalaVersion := scalaV
).dependsOn(engine.jvm)

lazy val reactGui = (project in file("react-gui")).settings(
  scalaVersion := scalaV,
  scalaJSUseMainModuleInitializer in Compile := true,
  scalaJSUseMainModuleInitializer in Test := false,
  skip in packageJSDependencies := false,

  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "1.1.0",
    "com.github.japgolly.scalajs-react" %%% "core" % "1.7.5",
    "io.github.cquiroz" %%% "scala-java-locales" % "1.0.0"
  ),

  // TODO: migrate to scalajs-bundler
  jsDependencies ++= Seq(
  "org.webjars.bower" % "react" % reactVersion / "react.development.js"
    minified "react.production.min.js"
    commonJSName "React",

  "org.webjars.bower" % "react" % reactVersion / "react-dom.development.js"
    minified  "react-dom.production.min.js"
    dependsOn "react.development.js"
    commonJSName "ReactDOM",

  "org.webjars.bower" % "react" % reactVersion
    /         "react-dom-server.browser.development.js"
    minified  "react-dom-server.browser.production.min.js"
    dependsOn "react-dom.development.js"
    commonJSName "ReactDOMServer")
).enablePlugins(ScalaJSPlugin, JSDependenciesPlugin)
  .dependsOn(engine.js)
