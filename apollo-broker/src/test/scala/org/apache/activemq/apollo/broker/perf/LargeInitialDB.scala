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

package org.apache.activemq.apollo.broker.perf

import org.apache.activemq.apollo.util.metric.MetricAggregator
import org.apache.activemq.apollo.util.{ServiceControl, FileSupport}
import FileSupport._
import java.io.File
import org.apache.activemq.apollo.dto.DestinationDTO

trait LargeInitialDB extends PersistentScenario {
  PURGE_STORE = false
  MULTI_BROKER = false

  var original: File = null
  var backup: File = null;

  // Keep it simple.. we are only creating 1 queue with a large number of entries.
  override def partitionedLoad = List(1)

  override def reportResourceTemplate() = { classOf[PersistentScenario].getResource("largedb-persistent-report.html") }

  // delete existing data file and copy new data file over
  override protected def beforeEach() = {
    println("Restoring DB")
    restoreDB
    super.beforeEach
  }

  // start a broker connect a producer and dump a bunch of messages
  // into a destination
  override protected def beforeAll(configMap: Map[String, Any]) = {
    super.beforeAll(configMap)

    initBrokers

    original = storeDirectory
    if (original.exists) {
      original.recursive_delete
      original.mkdirs
    }
    backup = storeDirectory.getParentFile / "backup"
    cleanBackup

    println("Using store at " + original + " and backup at " + backup)

    ServiceControl.start(sendBroker, "initial db broker startup")

    PTP = true
    val dests: Array[DestinationDTO] = createDestinations(1)

    totalProducerRate = new MetricAggregator().name("Aggregate Producer Rate").unit("items")

    val producer: RemoteProducer = _createProducer(0, 20, dests(0))

    producer.persistent = true
    producer.sync_persistent_send = false // this should speed things up.

    ServiceControl.start(producer, "initial db producer startup")

    val messages = 1000000L

    println("Sending %d messages".format(messages))
    while (producer.rate.counter() < messages) {
      println("Waiting for producer " + producer.rate.counter() + "/" + messages)
      Thread.sleep(5000)
    }

    ServiceControl.stop(producer, "producer shutdown")

    ServiceControl.stop(sendBroker, "broker shutdown")

    saveDB
  }

  def saveDB {
    println("Copying contents of " + original + " to " + backup)
    cleanBackup
    original.recursive_copy_to(backup)
    printStores
  }

  def printStores {
    println("\nOriginal store")
    original.recursive_list.foreach(println)
    println("\n\nBackup store")
    backup.recursive_list.foreach(println)
  }

  def restoreDB {
    original.recursive_delete
    println("Copying contents of " + backup + " to " + original)
    backup.recursive_copy_to(original)
    printStores
  }

  def cleanBackup {
    if (backup.exists) {
      backup.recursive_delete
    }
    backup.mkdirs
    printStores
  }

}