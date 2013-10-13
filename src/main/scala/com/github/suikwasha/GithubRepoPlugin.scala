package com.github.suikwasha

import sbt._
import sbt.Keys._

import com.typesafe.sbt.git.GitRunner
import org.eclipse.jgit.api.{ListBranchCommand, Git}
import scala.collection.JavaConversions
import org.eclipse.jgit.transport.RefSpec

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

  def listRemoteRepository(implicit gw: GitWrapper): Map[String, String] = {
    val jgit = gw.jgit
    val config = jgit.getRepository.getConfig
    val remotes = JavaConversions.asScalaSet(config.getSubsections("remote"))
    remotes.map(n => n -> config.getString("remote", n, "url")).toMap
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
    if(remotes.get(remote).isEmpty) {
      val jgit = gw.jgit
      val config = jgit.getRepository.getConfig
      config.setString("remote", remote, "url", url)
      true
    } else {
      false
    }
  }

  def checkout(branchName: String)(implicit gw: GitWrapper) = {
    val jgit = gw.jgit
    val checkoutCommand = jgit.checkout
    checkoutCommand.setName(branchName)
    checkoutCommand.call
  }

  def push(remote: String, branch: String)(implicit gw: GitWrapper) = {
    val jgit = gw.jgit
    val pushCommand = jgit.push
    pushCommand.setRemote(remote)
    pushCommand.setRefSpecs(new RefSpec(branch + ":" + branch))
    pushCommand.call
  }
}
