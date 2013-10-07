package com.github.suikwasha

import sbt._

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import java.io.File
import org.specs2.specification.{Step, Fragments}
import com.typesafe.sbt.git.GitRunner
import sbt.Keys.TaskStreams
import scala.collection.JavaConversions
import java.io.IOException
import org.apache.commons.io.FileUtils
import java.nio.file.Files

class GithubRepoPluginSpec extends Specification with Mockito {

  val tempDirectory = Files.createTempDirectory("temp").toFile

  lazy val tearDown = FileUtils.deleteDirectory(tempDirectory)

  override def map(fs: => Fragments) = fs ^ Step(tearDown)

  args(sequential = true)
  "createRepositoryTask" should {
    "ディレクトリを渡して正しくレポジトリを作成できる" in {
      val emptyDir: sbt.File = tempDirectory

      (emptyDir / ".git").exists.mustEqual(false)

      val git = GithubRepoPlugin.createRepositoryTask(emptyDir)

      (emptyDir / ".git").exists.mustEqual(true)

      emptyDir.createNewFile()
      git.add().addFilepattern("*").call()
      git.commit().setMessage("initial commit").call()

      "空のブランチを正しく作成できる" in {
        val tsMock = mock[TaskStreams]
        implicit val gw = new GitWrapper(git, emptyDir, "remote", tsMock)

        val beforeCreateBranch = git.branchList()
        JavaConversions.asScalaBuffer(beforeCreateBranch.call()) must have size 1

        GithubRepoPlugin.createBranch("gh-pages")

        val afterCreateBranch = git.branchList()
        JavaConversions.asScalaBuffer(afterCreateBranch.call()) must have size 2
      }
    }
  }
}
