package self.robin.examples.hadoop

import java.security.PrivilegedExceptionAction

import org.apache.hadoop.security.UserGroupInformation
import org.slf4j.LoggerFactory

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/2/5 15:53
 */
object HadoopUtil {

  val log = LoggerFactory.getLogger(HadoopUtil.getClass)

  def getCurrentUserName(): String = {
    Option(System.getenv("HADOOP_USER"))
      .getOrElse(UserGroupInformation.getCurrentUser().getShortUserName())
  }

  def runAsHadoopUser(func: () => Unit) {
    createSparkUser().doAs(new PrivilegedExceptionAction[Unit] {
      def run: Unit = func()
    })
  }

  def createSparkUser(): UserGroupInformation = {
    val user = getCurrentUserName()
    log.debug("creating UGI for user: " + user)
    val ugi = UserGroupInformation.createRemoteUser(user)
    transferCredentials(UserGroupInformation.getCurrentUser(), ugi)
    ugi
  }

  def transferCredentials(source: UserGroupInformation, dest: UserGroupInformation) {
    dest.addCredentials(source.getCredentials())
  }

  def main(args: Array[String]): Unit = {

    val run = () => println("abc")
    HadoopUtil.runAsHadoopUser(run)
    println(HadoopUtil.getCurrentUserName());
  }
}
