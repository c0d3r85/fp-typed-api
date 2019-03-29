package ru.tinkoff.codefest.http
import java.security.Permission

class BotSecurityManager extends SecurityManager {

  private def throwSecurityException(): Unit = throw new SecurityException("wow!")

  override def checkExit(status: Int): Unit = throwSecurityException()

  override def checkPermission(perm: Permission): Unit = ()
}
