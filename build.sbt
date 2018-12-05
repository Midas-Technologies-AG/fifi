/**
 * Copyright 2018 Midas Technologies AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
lazy val Versions = new {
  val scalaMain = "2.12"
  val scalaSub  = "8"
  val scala     = s"${scalaMain}.${scalaSub}"
}

val minimalSettings: Seq[Def.Setting[_]] = Seq(
  scapegoatVersion in ThisBuild := "1.3.8",
  publishMavenStyle := true,
  publishTo := sonatypePublishTo.value,
  organization := "social.midas",
  homepage := Some(url("https://github.com/Midas-Technologies-AG/service-discovery")),
  organizationName := "Midas Technologies AG",
  organizationHomepage := Some(url("https://midas.social/")),
  developers := List(
    Developer("fuzzy-id", "Thomas Bach", "", url("https://github.com/fuzzy-id")),
  ),
  licenses := Seq(
    "Apache License, ASL Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"),
  ),
  scmInfo := Some(ScmInfo(
    url("https://github.com/Midas-Technologies-AG/service-discovery/tree/master"),
    "scm:git:ssh://github.com:Midas-Technologies-AG/service-discovery.git",
    "scm:git:ssh://github.com:Midas-Technologies-AG/service-discovery.git",
  )),
  sonatypeProfileName := "social.midas",
  releaseProcess ++= Seq(
    releaseStepCommand("sonatypeRelease"),
  ),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  apiURL := Some(
    url(s"https://oss.sonatype.org/service/local/repositories/releases/archive/social/midas/${name.value}_${Versions.scalaMain}/${version.value}/${name.value}_${Versions.scalaMain}-${version.value}-javadoc.jar/!/")
  ),
  scalaVersion := Versions.scala,
  // Enable full stack-traces when running tests:
  //testOptions in Test += Tests.Argument("-oF"),
  parallelExecution in Test := true,
  autoAPIMappings := true,
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
  doctestMarkdownEnabled := true,
  doctestMarkdownPathFinder := baseDirectory.value * "doc" ** "*.md",
  fork in Test := true,
  testOptions in Test +=
    Tests.Argument(TestFrameworks.Specs2, "junitxml", "console"),
  libraryDependencies ++= Seq(
    config,
    log4jCore,
    log4jApi,
    log4jApiScala,
    circeLiteral     % Test,
    circeParser      % Test,
    junitInterface   % Test,
    scalaJava8Compat % Test,
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
      findManagedDependency("com.typesafe", "config")
        .map(_ -> url("https://lightbend.github.io/config/latest/api/")),
    ) ++ Seq("core", "ec2", "ecs", "regions").map(mod =>
      findManagedDependency("software.amazon.awssdk", mod)
        .map(_ -> url("http://aws-java-sdk-javadoc.s3-website-us-west-2.amazonaws.com/latest/"))
    )
    links.collect({ case Some(d) => d }).toMap
  }
)

lazy val `test-utils` = (project in file("test-utils"))
  .settings(
    minimalSettings
  )

lazy val `discovery-common` = (project in file("common"))
  .settings(
    minimalSettings,
    libraryDependencies ++= Seq(
      sangria,
    )
  )

lazy val `discovery-common-aws` = (project in file("common-aws"))
  .settings(
    minimalSettings,
    libraryDependencies ++= Seq(
      catsEffect,
      circeGeneric,
      log4jSlf4jImpl,
      awsSdkAwsCore,
      awsSdkCore,
      awsSdkRegions,
      awsSdkUtils,
    ),
  ).dependsOn(
    `discovery-common`,
  )

lazy val `discovery-aws-ec2` = (project in file("aws-ec2"))
  .settings(
    minimalSettings,
    libraryDependencies ++= Seq(
      awsSdkEc2,
      sangriaCirce,
    ),
  ).dependsOn(
    `discovery-common`,
    `discovery-common-aws`,
    `test-utils` % Test,
  )

lazy val `discovery-aws-ecs` = (project in file("aws-ecs"))
  .settings(
    minimalSettings,
    libraryDependencies ++= Seq(
      awsSdkEcs,
      sangriaCirce,
    ),
  ).dependsOn(
    `discovery-aws-ec2`,
    `test-utils` % Test,
  )

lazy val `root` = (project in file("."))
  .settings(
    minimalSettings,
    publishArtifact := false,
    publish := {},
    publishLocal := {},
  ).enablePlugins(ScalaUnidocPlugin)
  .dependsOn(
    `discovery-common`,
    `discovery-common-aws`,
    `discovery-aws-ec2`,
    `discovery-aws-ecs`,
  )
  .aggregate(
    `discovery-common`,
    `discovery-common-aws`,
    `discovery-aws-ec2`,
    `discovery-aws-ecs`,
  )

lazy val catsEffect     = "org.typelevel"            %% "cats-effect"      % "1.1.0"                // https://search.maven.org/search?q=g:org.typelevel%20AND%20a:cats-effect_2.12&core=gav
lazy val circeCore      = "io.circe"                 %% "circe-core"       % "0.10.1"               // https://search.maven.org/search?q=g:io.circe%20AND%20a:circe-core_2.12&core=gav
lazy val circeGeneric   = "io.circe"                 %% "circe-generic"    % circeCore.revision
lazy val circeLiteral   = "io.circe"                 %% "circe-literal"    % circeCore.revision
lazy val circeParser    = "io.circe"                 %% "circe-parser"     % circeCore.revision
lazy val config         = "com.typesafe"             %  "config"           % "1.3.3"                // https://search.maven.org/search?q=g:com.typesafe%20AND%20a:config&core=gav
lazy val log4jApi       = "org.apache.logging.log4j" %  "log4j-api"        % log4jCore.revision
lazy val log4jApiScala  = "org.apache.logging.log4j" %% "log4j-api-scala"  % "11.0"                 // https://search.maven.org/search?q=g:org.apache.logging.log4j%20AND%20a:log4j-api-scala_2.12&core=gav
lazy val log4jCore      = "org.apache.logging.log4j" %  "log4j-core"       % "2.11.1"               // https://search.maven.org/search?q=g:org.apache.logging.log4j%20AND%20a:log4j-core&core=gav
lazy val log4jSlf4jImpl = "org.apache.logging.log4j" %  "log4j-slf4j-impl" % log4jCore.revision
lazy val awsSdkAwsCore  = "software.amazon.awssdk"   %  "aws-core"         % "2.1.3"                // https://search.maven.org/search?q=g:software.amazon.awssdk%20AND%20a:regions&core=gav
lazy val awsSdkCore     = "software.amazon.awssdk"   %  "core"             % awsSdkAwsCore.revision
lazy val awsSdkEc2      = "software.amazon.awssdk"   %  "ec2"              % awsSdkAwsCore.revision
lazy val awsSdkEcs      = "software.amazon.awssdk"   %  "ecs"              % awsSdkAwsCore.revision
lazy val awsSdkRegions  = "software.amazon.awssdk"   %  "regions"          % awsSdkAwsCore.revision
lazy val awsSdkUtils    = "software.amazon.awssdk"   %  "utils"            % awsSdkAwsCore.revision
lazy val sangria        = "org.sangria-graphql"      %% "sangria"          % "1.4.2"                // https://search.maven.org/search?q=g:org.sangria-graphql%20AND%20a:sangria_2.12&core=gav
lazy val sangriaCirce   = "org.sangria-graphql"      %% "sangria-circe"    % "1.2.1"                // https://search.maven.org/search?q=g:org.sangria-graphql%20AND%20a:sangria-circe_2.12&core=gav

// Testing libraries:
lazy val junitInterface   = "com.novocode"           %  "junit-interface"    % "0.11"              // https://search.maven.org/search?q=g:com.novocode%20AND%20a:junit-interface&core=gav
lazy val scalaJava8Compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"             // https://search.maven.org/search?q=g:org.scala-lang.modules%20AND%20a:scala-java8-compat_2.12&core=gav
lazy val scalacheck       = "org.scalacheck"         %% "scalacheck"         % "1.14.0"            // https://search.maven.org/search?q=g:org.scalacheck%20AND%20a:scalacheck_2.12&core=gav
lazy val specs2Core       = "org.specs2"             %% "specs2-core"        % "4.3.5"             // https://search.maven.org/search?q=g:org.specs2%20AND%20a:specs2-core_2.12&core=gav
lazy val specs2Junit      = "org.specs2"             %% "specs2-junit"       % specs2Core.revision
lazy val specs2Scalacheck = "org.specs2"             %% "specs2-scalacheck"  % specs2Core.revision
