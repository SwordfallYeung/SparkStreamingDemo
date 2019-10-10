package com.swordfall.common

import java.io.{BufferedOutputStream, File, FileOutputStream}

import ch.ethz.ssh2.{Connection, Session}

case class RemoteHost(ip: String, post: Int, user: String, password: String)

class RemoteShell(val remoteHost: RemoteHost) {

  private lazy val conn: Connection = new Connection(remoteHost.ip, remoteHost.post)
  private var isLogin = false

  private def login():Boolean = {
    if (isLogin){
      try{
        conn.connect()
        conn.authenticateWithPassword(remoteHost.user, remoteHost.password)
        isLogin = true
        true
      }catch{
        case ex: Exception => {
          ex.printStackTrace()
          false
        }
      }
    }else {
      true
    }
  }

  def exit(): Unit ={
    if (isLogin){
      conn.close()
      isLogin = false
    }
  }

  def exec(cmds: String, logPath: String): Unit = {
    try{
      val file = new File(logPath)
      if (!file.getParentFile.exists()) file.mkdirs()
      val out = new FileOutputStream(file)
      val outBuf = new BufferedOutputStream(out)

      var session: Session = null
      if (login()){
        session = conn.openSession()
        session.execCommand(cmds)

      }
    }catch {
      case ex: Exception=>{

      }
    }
  }

  private def processEcho(session: Session, fStdout: String, fStderr: String): Unit ={

  }
}
