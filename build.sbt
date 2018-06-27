/**
 * Copyright 2018 Midas Technologies AG
 *
 */
lazy val Versions = new {
  val scalaMain = "2.12"
  val scalaSub  = "6"
  val scala     = s"${scalaMain}.${scalaSub}"
}

val minimalSettings: Seq[Def.Setting[_]] = Seq(
  organization := "social.midas",
  scalaVersion := Versions.scala,
  version := "0.1.0",
  // Enable full stack-traces when running tests:
  //testOptions in Test += Tests.Argument("-oF"),
  parallelExecution in Test := true,
  autoAPIMappings := true,
  apiURL := Some(
    url(s"file:${baseDirectory.value.getAbsolutePath}/target/scala-${Versions.scalaMain}/api")
  ),
  scalacOptions in Compile ++= Seq(
    "-deprecation",
    "-feature",
    "-language:higherKinds",
    "-Xlint",
    "-Xmigration",
    "-Ypartial-unification",
  ),
  scalacOptions in (Compile, doc) += "-groups",
  scalacOptions in (Compile, console) ~= (_.filterNot (_ == "-Xlint")),
  doctestTestFramework := DoctestTestFramework.Specs2,
  fork in Test := true,
  testOptions in Test +=
    Tests.Argument(TestFrameworks.Specs2, "junitxml", "console"),
  libraryDependencies ++= Seq(
    log4jCore,
    log4jApi,
    log4jApiScala,
    circeLiteral     % Test,
    circeParser      % Test,
    scalacheck       % Test,
    specs2Core       % Test,
    specs2Junit      % Test,
    specs2Scalacheck % Test,
  ),
  apiMappings ++= {
    def findManagedDependency(organization: String, name: String): Option[File] = {
      ( for {
        entry <- (fullClasspath in Compile).value
        module <- entry.get(moduleID.key)
        if module.organization == organization
        if module.name.startsWith(name)
        jarFile = entry.data
      } yield jarFile
      ).headOption
    }

    val links = Seq(
      findManagedDependency("org.scalacheck", "scalacheck")
        .map(_ -> url(Seq(
          "https://www.scalacheck.org/files",
          s"scalacheck_${Versions.scalaMain}-${scalacheck.revision}-api/index.html",
        ).mkString("/"))),
      findManagedDependency("org.typelevel", "cats-effect")
        .map(_ -> url("https://typelevel.org/cats-effect/api/")),
    )
    links.collect({ case Some(d) => d }).toMap
  }
)

lazy val `aws-wrapper` = (project in file("aws-wrapper"))
  .settings(
    minimalSettings,
    libraryDependencies ++= Seq(
      catsEffect,
      circeGeneric,
      config,
      log4jSlf4jImpl,
      newAwsSdkCore,
      newAwsSdkEc2,
      newAwsSdkEcs,
      newAwsSdkUtils,
    ),
  )

lazy val `discovery-aws` = (project in file("discovery-aws"))
  .settings(
    minimalSettings,
    libraryDependencies ++= Seq(
      sangria,
      sangriaCirce,
    ),
  ).dependsOn(
    `aws-wrapper`,
  )

lazy val catsEffect     = "org.typelevel"            %% "cats-effect"      % "0.10.1"
lazy val circeCore      = "io.circe"                 %% "circe-core"       % "0.9.3"
lazy val circeGeneric   = "io.circe"                 %% "circe-generic"    % circeCore.revision
lazy val circeLiteral   = "io.circe"                 %% "circe-literal"    % circeCore.revision
lazy val circeParser    = "io.circe"                 %% "circe-parser"     % circeCore.revision
lazy val config         = "com.typesafe"             %  "config"           % "1.3.3"
lazy val log4jApi       = "org.apache.logging.log4j" %  "log4j-api"        % log4jCore.revision
lazy val log4jApiScala  = "org.apache.logging.log4j" %% "log4j-api-scala"  % "11.0"
lazy val log4jCore      = "org.apache.logging.log4j" %  "log4j-core"       % "2.11.0"
lazy val log4jSlf4jImpl = "org.apache.logging.log4j" %  "log4j-slf4j-impl" % log4jCore.revision
lazy val newAwsSdkCore  = "software.amazon.awssdk"   %  "core"             % "2.0.0-preview-10"
lazy val newAwsSdkEc2   = "software.amazon.awssdk"   %  "ec2"              % newAwsSdkCore.revision
lazy val newAwsSdkEcs   = "software.amazon.awssdk"   %  "ecs"              % newAwsSdkCore.revision
lazy val newAwsSdkUtils = "software.amazon.awssdk"   %  "utils"            % newAwsSdkCore.revision
lazy val sangria        = "org.sangria-graphql"      %% "sangria"          % "1.4.0"
lazy val sangriaCirce   = "org.sangria-graphql"      %% "sangria-circe"    % "1.2.1"

// Testing libraries:
lazy val scalacheck       = "org.scalacheck" %% "scalacheck"        % "1.14.0"
lazy val specs2Core       = "org.specs2"     %% "specs2-core"       % "4.2.0"
lazy val specs2Junit      = "org.specs2"     %% "specs2-junit"      % specs2Core.revision
lazy val specs2Scalacheck = "org.specs2"     %% "specs2-scalacheck" % specs2Core.revision
