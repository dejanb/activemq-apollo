/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.apollo.broker.security

import java.security.Principal
import javax.security.auth.Subject
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.UnsupportedCallbackException
import javax.security.auth.login.FailedLoginException
import javax.security.auth.login.LoginException
import java.security.cert.X509Certificate
import java.{util => ju}
import java.io.{FileInputStream, File, IOException}
import org.yaml.snakeyaml.Yaml
import org.apache.activemq.apollo.util.{FileSupport, Log}
import java.lang.String
import org.apache.activemq.jaas.{UserPrincipal, CertificateCallback}
import java.util.{LinkedList, Properties, HashSet}

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object CertificateLoginModule extends Log {
  val LOGIN_CONFIG = "java.security.auth.login.config"
  val FILE_OPTION = "dn_file"
}

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class CertificateLoginModule {

  import CertificateLoginModule._

  var callback_handler: CallbackHandler = _
  var subject: Subject = _

  var certificates: Array[X509Certificate] = _
  var principals = new LinkedList[Principal]()

  var file: Option[File] = None

  /**
   * Overriding to allow for proper initialization. Standard JAAS.
   */
  def initialize(subject: Subject, callback_handler: CallbackHandler, shared_state: ju.Map[String, _], options: ju.Map[String, _]): Unit = {
    this.subject = subject
    this.callback_handler = callback_handler

    val base_dir = if (System.getProperty(LOGIN_CONFIG) != null) {
      new File(System.getProperty(LOGIN_CONFIG)).getParentFile()
    } else {
      new File(".")
    }

    file = Option(options.get(FILE_OPTION)).map(x=> new File(base_dir,x.asInstanceOf[String]))
    debug("Initialized file=%s", file)
  }

  def login: Boolean = {
    val cert_callback = new CertificateCallback()
    try {
      callback_handler.handle(Array(cert_callback))
    } catch {
      case ioe: IOException =>
        throw new LoginException(ioe.getMessage())
      case uce: UnsupportedCallbackException =>
        throw new LoginException(uce.getMessage() + " Unable to obtain client certificates.")
    }

    certificates = cert_callback.getCertificates()
    if (certificates == null || certificates.isEmpty) {
      throw new FailedLoginException("No associated certificates")
    }

    // Are we restricting the logins to known DNs?
    file match {
      case None =>
        for (cert <- certificates) {
          principals.add(cert.getSubjectX500Principal)
        }

      case Some(file)=>
        val users = try {
          import FileSupport._
          using( new FileInputStream(file) ) { in=>
            (new Yaml().load(in)).asInstanceOf[java.util.Map[String, AnyRef]]
          }
        } catch {
          case e: Throwable =>
            warn(e, "Unable to load the distinguished name file: " + file)
            e.printStackTrace
            throw new LoginException("Invalid login module configuration")
        }

        for (cert <- certificates) {
          val dn: String = cert.getSubjectX500Principal.getName
          if( users.containsKey(dn) ) {
            val alias = users.get(dn)
            if( alias!=null ) {
              principals.add(new UserPrincipal(alias.toString))
            }
            principals.add(cert.getSubjectX500Principal)
          }
        }

        if (principals.isEmpty) {
          throw new FailedLoginException("Does not have a listed distinguished name")
        }
    }

    return true
  }

  def commit: Boolean = {
    subject.getPrincipals().addAll(principals)
    certificates = null;
    debug("commit")
    return true
  }

  def abort: Boolean = {
    principals.clear
    certificates = null;
    debug("abort")
    return true
  }

  def logout: Boolean = {
    subject.getPrincipals().removeAll(principals)
    principals.clear
    debug("logout")
    return true
  }

}