import sbt._
import sbt.Keys._

object BuildSettings {
  val buildVersion = "0.10.7-THIB"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.reactivemongo",
    version := buildVersion,
    scalaVersion := "2.10.2",
    crossVersion := CrossVersion.binary,
    shellPrompt := ShellPrompt.buildShellPrompt
  ) ++ Publish.settings ++ Format.settings
}

object Publish {
  object TargetRepository {
    // def local: Project.Initialize[Option[sbt.Resolver]] = version { (version: String) =>
    //   val localPublishRepo = "/Volumes/Data/code/repository"
    //   if(version.trim.endsWith("SNAPSHOT"))
    //     Some(Resolver.file("snapshots", new File(localPublishRepo + "/snapshots")))
    //   else Some(Resolver.file("releases", new File(localPublishRepo + "/releases")))
    // }
    // def sonatype: Project.Initialize[Option[sbt.Resolver]] = version { (version: String) =>
    //   val nexus = "https://oss.sonatype.org/"
    //   if (version.trim.endsWith("SNAPSHOT"))
    //     Some("snapshots" at nexus + "content/repositories/snapshots")
    //   else
    //     Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    // }
    def iliaz: Project.Initialize[Option[sbt.Resolver]] = version { (v: String) ⇒
      Some(Resolver.sftp(
        "iliaz",
        "scala.iliaz.com"
      ) as ("scala_iliaz_com", Path.userHome / ".ssh" / "id_rsa"))
    }
  }
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishTo <<= TargetRepository.iliaz,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("http://reactivemongo.org"))
    // pomExtra := (
    //   <scm>
    //     <url>git://github.com/zenexity/Play-ReactiveMongo.git</url>
    //     <connection>scm:git://github.com/zenexity/Play-ReactiveMongo.git</connection>
    //   </scm>
    //   <developers>
    //     <developer>
    //       <id>sgodbillon</id>
    //       <name>Stephane Godbillon</name>
    //       <url>http://stephane.godbillon.com</url>
    //     </developer>
    //     <developer>
    //       <id>mandubian</id>
    //       <name>Pascal Voitot</name>
    //       <url>http://mandubian.com</url>
    //     </developer>
    //   </developers>)
  )
}

object Format {
  import com.typesafe.sbt.SbtScalariform._

  lazy val settings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := formattingPreferences
  )

  lazy val formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences().
      setPreference(AlignParameters, true).
      setPreference(AlignSingleLineCaseStatements, true).
      setPreference(CompactControlReadability, false).
      setPreference(CompactStringConcatenation, false).
      setPreference(DoubleIndentClassDeclaration, true).
      setPreference(FormatXml, true).
      setPreference(IndentLocalDefs, false).
      setPreference(IndentPackageBlocks, true).
      setPreference(IndentSpaces, 2).
      setPreference(MultilineScaladocCommentsStartOnFirstLine, false).
      setPreference(PreserveSpaceBeforeArguments, false).
      setPreference(PreserveDanglingCloseParenthesis, false).
      setPreference(RewriteArrowSymbols, false).
      setPreference(SpaceBeforeColon, false).
      setPreference(SpaceInsideBrackets, false).
      setPreference(SpacesWithinPatternBinders, true)
  }
}

// Shell prompt which show the current project,
// git branch and build version
object ShellPrompt {

  object devnull extends ProcessLogger {
    def info(s: => String) {}

    def error(s: => String) {}

    def buffer[T](f: => T): T = f
  }

  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
    )

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract(state).currentProject.id
      "%s:%s:%s> ".format(
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object ReactiveMongoBuild extends Build {
  import BuildSettings._

  lazy val reactivemongo = Project(
    "Play2-ReactiveMongo",
    file("."),
    settings = buildSettings ++ Seq(
      resolvers := Seq(
        "iliaz.com" at "http://scala.iliaz.com/",
        "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
        //"local repo" at "file:///Volumes/Data/code/repository/snapshots",
        "Sonatype" at "http://oss.sonatype.org/content/groups/public/",
        "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
        "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
      ),
      libraryDependencies ++= Seq(
        "org.reactivemongo" %% "reactivemongo" % "0.10.7-THIB" cross CrossVersion.binary,
        "com.typesafe.play" %% "play" % "2.2.0" % "provided" cross CrossVersion.binary,
        "org.specs2" % "specs2" % "1.13" % "test" cross CrossVersion.binary,
        "junit" % "junit" % "4.8" % "test" cross CrossVersion.Disabled
      )
    )
  )
}
