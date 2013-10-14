package com.github.suikwasha

import sbt._
import sbt.Keys._

import org.eclipse.jgit.api.{ListBranchCommand, Git}
import scala.collection.JavaConversions
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.pgm.Main

trait GithubRepoKeys {
  val githubRepo = SettingKey[String]("gitHubRepo")
}

case class GitWrapper(val jgit: Git, val remote: String, val logger: Logger)

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
    if(refs.find(_.getName.endsWith(branchName)).isEmpty) {
      val branchCreateCommand = jgit.branchCreate
      branchCreateCommand.setName(branchName)
      val ref = branchCreateCommand.call()
      ref.getName.equals(branchName)
      gw.logger.info("branch " + branchName + " created")
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
      gw.logger.info("added remote repository " + remote + " " + url)
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
    gw.logger.info("checked out branch " + branchName)
  }

  def push(remote: String, branch: String)(implicit gw: GitWrapper) = {
    val jgit = gw.jgit
    val pushCommand = jgit.push
    pushCommand.setRemote(remote)
    pushCommand.setRefSpecs(new RefSpec(branch + ":" + branch))
    pushCommand.call
    gw.logger.info("pushed to " + remote + ":" + branch)
  }

  def add()(implicit gw: GitWrapper) = {
    val jgit = gw.jgit
    jgit.add.addFilepattern(".").call()
    gw.logger.info("added files to commit.")
  }

  def commit(message: String)(implicit gw: GitWrapper) = {
    val jgit = gw.jgit
    jgit.commit().setMessage(message).call()
    gw.logger.info("commit created.")
  }

  private def pushToGithub(file: File, remote: String, logger: Logger = ConsoleLogger()) = {
    val jgit = createRepositoryTask(file)
    implicit val gw = new GitWrapper(jgit, remote, logger)
    add()
    // TODO: use version number and build date for commit message
    commit("commit")
    createBranch("gh-pages")
    checkout("gh-pages")
    addRemoteRepository(Origin, remote)
    push(Origin, "gh-pages")

    // TODO: use jgit output
    gw.logger.success("pushed to gh-pages on " + remote + " successfully.")
  }

  def publishTargetDir(state: State, args: Seq[String]) = {
    val extracted = Project.extract(state)
    val repoDirOpt: Option[File] = extracted.getOpt(target).map(_ / "repo")
    repoDirOpt match {
      case Some(file) => {
        val publishToSetting = Seq(publishTo := repoDirOpt.map(t => Resolver.file("file", t)))
        val newState = extracted.append(publishToSetting, state)
        Command.process("publish", newState)

        args.headOption match {
          case Some(remote) => pushToGithub(file, remote)
          case None => throw new IllegalStateException()
        }
      }
      case None => throw new IllegalStateException()
    }
    state
  }

  override val settings = Seq(commands += Command.args("publish-ghrepo", "<github_url>")(publishTargetDir))
}
