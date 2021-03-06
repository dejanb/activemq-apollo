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
package org.apache.activemq.apollo.web.resources

import java.lang.String
import com.sun.jersey.api.NotFoundException
import javax.ws.rs._
import core.{UriInfo, Response, Context}
import reflect.{BeanProperty}
import com.sun.jersey.api.view.ImplicitProduces
import Response._
import Response.Status._
import collection.JavaConversions._
import com.sun.jersey.api.core.ResourceContext
import java.util.concurrent.TimeUnit
import org.apache.activemq.apollo.dto._
import java.util.{Arrays, Collections}
import org.apache.activemq.apollo.util.Logging
import org.fusesource.hawtdispatch._
import java.net.URI
import org.fusesource.scalate.{NoValueSetException, RenderContext}
import org.apache.activemq.apollo.broker.{Broker, ConfigStore, BrokerRegistry}

/**
 * Defines the default representations to be used on resources
 */
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array("application/json", "application/xml","text/xml"))
abstract class Resource(private val parent:Resource=null) extends Logging {

  @Context
  var uri_info:UriInfo = null

  if( parent!=null ) {
    this.uri_info = parent.uri_info
  }

  def result[T](value:Status, message:Any=null):T = {
    val response = Response.status(value)
    if( message!=null ) {
      response.entity(message)
    }
    throw new WebApplicationException(response.build)
  }

  def result[T](uri:URI):T = {
    throw new WebApplicationException(seeOther(uri).build)
  }

  def path(value:Any) = uri_info.getAbsolutePathBuilder().path(value.toString).build()

  def strip_resolve(value:String) = {
    new URI(uri_info.getAbsolutePath.resolve(value).toString.stripSuffix("/"))
  }

}

object ViewHelper {

  val KB: Long = 1024
  val MB: Long = KB * 1024
  val GB: Long = MB * 1024
  val TB: Long = GB * 1024

  val SECONDS: Long = TimeUnit.SECONDS.toMillis(1)
  val MINUTES: Long = TimeUnit.MINUTES.toMillis(1)
  val HOURS: Long = TimeUnit.HOURS.toMillis(1)
  val DAYS: Long = TimeUnit.DAYS.toMillis(1)
  val YEARS: Long = DAYS * 365


}
class ViewHelper {
  import ViewHelper._

  lazy val uri_info = {
    try {
      RenderContext().attribute[UriInfo]("uri_info")
    } catch {
      case x:NoValueSetException =>
        RenderContext().attribute[Resource]("it").uri_info
    }
  }

  def path(value:Any) = {
    uri_info.getAbsolutePathBuilder().path(value.toString).build()
  }

  def strip_resolve(value:String) = {
    uri_info.getAbsolutePath.resolve(value).toString.stripSuffix("/")
  }


  def memory(value:Int):String = memory(value.toLong)
  def memory(value:Long):String = {

    if( value < KB ) {
      "%d bytes".format(value)
    } else if( value < MB ) {
       "%,.2f kb".format(value.toFloat/KB)
    } else if( value < GB ) {
      "%,.3f mb".format(value.toFloat/MB)
    } else if( value < TB ) {
      "%,.4f gb".format(value.toDouble/GB)
    } else {
      "%,.5f tb".format(value.toDouble/TB)
    }
  }

  def uptime(value:Long):String = {
    def friendly(duration:Long):String = {
      if( duration < SECONDS ) {
        "%d ms".format(duration)
      } else if (duration < MINUTES) {
        "%d seconds".format(duration / SECONDS)
      } else if (duration < HOURS) {
        "%d minutes".format(duration / MINUTES)
      } else if (duration < DAYS) {
        println("<")
        "%d hours %s".format(duration / HOURS, friendly(duration%HOURS))
      } else if (duration < YEARS) {
        "%d days %s".format(duration / DAYS, friendly(duration%DAYS))
      } else {
        "%,d years %s".format(duration / YEARS, friendly(duration%YEARS))
      }
    }
    friendly(System.currentTimeMillis - value)
  }
}

/**
 * Manages a collection of broker resources.
 */
@Path("/")
@Produces(Array("application/json", "application/xml","text/xml", "text/html;qs=5"))
class BrokerResource extends Resource {

  val cs = ConfigStore()
  val config = cs.load(false)

  @GET
  def get = {
    val rc = new BrokerSummaryDTO
    rc.manageable = BrokerRegistry.list.size > 0
    rc.configurable = cs.can_write
    rc
  }

  @Path("config")
  def config_resource = ConfigurationResource(this)

  @Path("runtime")
  def runtime = RuntimeResource(this)
}



