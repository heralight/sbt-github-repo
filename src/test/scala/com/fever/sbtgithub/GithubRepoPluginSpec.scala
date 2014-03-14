package com.fever.sbtgithub

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

  val tempDirectoryPath = Files.createTempDirectory("temp")

  lazy val tearDown = FileUtils.deleteDirectory(tempDirectoryPath.toFile)

  override def map(fs: => Fragments) = fs ^ Step(tearDown)

  args(sequential = true)
  "createRepositoryTask" should {
    "ディレクトリを渡して正しくレポジトリを作成できる" in {
      val emptyDir: sbt.File = Files.createTempDirectory(tempDirectoryPath, "temp").toFile

      (emptyDir / ".git").exists.mustEqual(false)

      val git = GithubRepoPlugin.createRepositoryTask(emptyDir)

      (emptyDir / ".git").exists.mustEqual(true)

      emptyDir.createNewFile()
      git.add().addFilepattern("*").call()
      git.commit().setMessage("initial commit").call()

      val tsMock = mock[TaskStreams]
      implicit val gw = new GitWrapper(git, emptyDir, "remote", tsMock)

      "空のブランチを正しく作成できる" in {

        val beforeCreateBranch = git.branchList()
        JavaConversions.asScalaBuffer(beforeCreateBranch.call()) must have size 1

        GithubRepoPlugin.createBranch("gh-pages")

        val afterCreateBranch = git.branchList()
        JavaConversions.asScalaBuffer(afterCreateBranch.call()) must have size 2
      }

      "リモートレポジトリを登録できる" in {
        val beforeAddRemote = GithubRepoPlugin.listRemoteRepository
        val testRemote = "origin"
        val testUrl = "git@example.com:testrepo"

        beforeAddRemote.get(testRemote) must beNone

        GithubRepoPlugin.addRemoteRepository(testRemote, testUrl)

        val afterAddRemote = GithubRepoPlugin.listRemoteRepository

        afterAddRemote.get(testRemote) must beSome(testUrl)
      }

      "ブランチを変更できる" in {
        val beforeBranch = git.getRepository.getBranch

        GithubRepoPlugin.checkout("gh-pages")

        val afterBranch = git.getRepository.getBranch

        afterBranch mustNotEqual(beforeBranch)
        afterBranch mustEqual("gh-pages")
      }

      "リモートブランチにpushすることができる." in {
        val remoteRepoDir: sbt.File = Files.createTempDirectory(tempDirectoryPath, "temp").toFile

        (remoteRepoDir / ".git").exists.mustEqual(false)

        val remoteRepo = GithubRepoPlugin.createRepositoryTask(remoteRepoDir)
        GithubRepoPlugin.addRemoteRepository("test", remoteRepoDir.getAbsolutePath)

        GithubRepoPlugin.push("test", "master")

        val afterPushCommits = JavaConversions.iterableAsScalaIterable(remoteRepo.log.all().call)
      }
    }
  }
}
