package com.github.suikwasha

import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtGit.GitKeys._
import com.typesafe.sbt.git.GitRunner
import org.eclipse.jgit.api.{ListBranchCommand, InitCommand, Git}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import scala.collection.JavaConversions
import org.eclipse.jgit.lib.{RefUpdate, ObjectId, Ref}
import org.eclipse.jgit.transport.RemoteConfig

trait GithubRepoKeys {
  val localRepo = SettingKey[File]("local-repo")
  val githubRepo = SettingKey[String]("github-repo")
  val createRepo = TaskKey[Unit]("create-repo")
  val publishToGithubRepo = TaskKey[Unit]("publish-to-github-repo")
}

case class GitWrapper(val jgit: Git, val repo: File, val remote: String, val taskStreams: TaskStreams)

object GithubRepoPlugin extends Plugin with GithubRepoKeys {

  val Origin = "origin"

  def createRepositoryTask(repo: File): Git = {
    val init = Git.init()
    init.setDirectory(repo)
    init.call()
  }

  def listRemoteRepository(implicit gw: GitWrapper): Seq[String] = {
    val jgit = gw.jgit
    val config = jgit.getRepository.getConfig
    val remotes = JavaConversions.asScalaBuffer(RemoteConfig.getAllRemoteConfigs(config))
    remotes.map(_.getName)
  }

  def createBranch(branchName: String)(implicit gw: GitWrapper) = {
    val jgit = gw.jgit
    val branches: ListBranchCommand = jgit.branchList
    val refs = JavaConversions.asScalaIterator(branches.call().iterator())
    if(refs.find(_.getName.equals(branchName)).isEmpty) {
      val branchCreateCommand = jgit.branchCreate
      branchCreateCommand.setName(branchName)
      val ref = branchCreateCommand.call()
      ref.getName.equals(branchName)
    } else {
      false
    }
  }

  def addRemoteRepository(remote: String, url: String)(implicit gw: GitWrapper) = {
    val remotes = listRemoteRepository
    if(!remotes.contains(Origin)) {
      val jgit = gw.jgit
      val config = jgit.getRepository.getConfig
      config.setString("remote", Origin, "url", gw.remote)
      true
    } else {
      false
    }
  }

  private def gitRunnerWithLogger(gitRunner: GitRunner, repo: File, s: TaskStreams)(args: String*) =
    gitRunner(args: _*)(repo, s.log)
}
