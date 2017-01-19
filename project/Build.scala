import com.typesafe.sbt.{GitBranchPrompt, GitVersioning}
import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbt.SbtGhPages._
import com.typesafe.sbt.SbtGit.git

import com.banno.license.Plugin.LicenseKeys._
import com.banno.license.Licenses._
import sbtbuildinfo.Plugin._
import com.typesafe.sbt.SbtGit.useJGit

object BuildSettings {
  val buildOrganization = "ch.unibas.cs.gravis"
  val buildScalaVersion = "2.11.7"

  val publishURL = Resolver.file("file", new File("/export/contrib/statismo/repo/public"))

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    scalaVersion := buildScalaVersion,
    crossScalaVersions := Seq("2.10.5", "2.11.7"),
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    scalacOptions ++= Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-target:jvm-1.6")
  )

  // nativelibs implementation to use (e.g., "linux64"). If not explicitly set, use "all"
  // which contains all supported platforms.
  val scalismoPlatform = {
    val env = System.getenv("SCALISMO_PLATFORM")
    if (env != null) env else "all"
  }
}

object Resolvers {
   val stkResolvers = Seq(Resolver.jcenterRepo, Resolver.sonatypeRepo("releases"), Resolver.sonatypeRepo("snapshots"))
}

object Dependencies {
  import BuildSettings.scalismoPlatform
  val scalatest = "org.scalatest" %% "scalatest" % "2.2+" % "test"
  val breezeMath = "org.scalanlp" %% "breeze" % "0.12"
  val breezeNative = "org.scalanlp" %% "breeze-natives" % "0.12"
  val sprayJson = "io.spray" %% "spray-json" % "1.2.6"
  val scalismoNativeStub = "ch.unibas.cs.gravis" % "scalismo-native-stub" % "4.0.0"
  val scalismoNativeImpl = "ch.unibas.cs.gravis" % s"scalismo-native-$scalismoPlatform" % "4.0.0" % "test"
  val slf4jNop = "org.slf4j" % "slf4j-nop" % "1.6.0" // this silences slf4j complaints in registration classes
}

object STKBuild extends Build {

  import Resolvers._
  import Dependencies._
  import BuildSettings._

  lazy val lic = com.banno.license.Plugin.licenseSettings ++ Seq(license := apache2("Copyright 2015 University of Basel, Graphics and Vision Research Group"), removeExistingHeaderBlock := false)

  lazy val scalismo = Project(
    "scalismo",
    file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= commonDeps,
      resolvers ++= stkResolvers,
      parallelExecution in Test := false,
      publishTo := Some(publishURL),
      EclipseKeys.withSource := true)
      ++ site.settings
      ++ site.includeScaladoc()
      ++ ghpages.settings ++
      Seq(
        git.remoteRepo := "git@github.com:unibas-gravis/scalismo.git"
      )++
      Seq(
        git.baseVersion := "develop",
        git.useGitDescribe := false,
        useJGit
      ) ++
      buildInfoSettings ++
      Seq(
      sourceGenerators in Compile <+= buildInfo,
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion),
      buildInfoPackage := "scalismo"
      )
).enablePlugins(GitBranchPrompt, GitVersioning)


  // Sub-project specific dependencies
  val commonDeps = Seq(
    scalatest,
    breezeMath,
    breezeNative,
    scalismoNativeStub,
    scalismoNativeImpl,
    sprayJson,
    slf4jNop
  )
}
