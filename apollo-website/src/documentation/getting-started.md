<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  Architecture
-->
# Getting Started Guide

This guide will help you install, setup and run an Apollo broker and validate
that the broker is operating correctly.

## Installation

1. [Download](../download.html) the ${project_name} distribution that is 
   most appropriate for your operating system.

2. Extract the distribution archive:
   {pygmentize_and_compare::wide=true}
-----------------------------
text: Unix/Linux/OS X
-----------------------------
tar -zxvf apache-apollo-${project_version}-unix-distro.tar.gz
-----------------------------
text: Windows
-----------------------------
jar -xvf apache-apollo-${project_version}-windows-distro.zip
{pygmentize_and_compare}

3. Set your shell's `APOLLO_HOME` environment variable to
   where the extracted `apache-apollo-${project_version}` directory 
   is located.
   
4. Add the extracted `apache-apollo-${project_version}/bin` directory
   to your shell's `PATH` environment variable.

## Install the BDB library

Apollo's most robust message store implementation is the BDB based message store.  
Unfortunately, BDB cannot be redistributed by Apache.  It is highly recommended
that you add it to your apollo installation. You can download it from Oracle at
[je-4.1.6.jar](http://download.oracle.com/maven/com/sleepycat/je/4.1.6/je-4.1.6.jar) and
then copy it into the `${APOLLO_HOME}/lib` directory.

For those of you with curl installed, you can just run:

    curl http://download.oracle.com/maven/com/sleepycat/je/4.1.6/je-4.1.6.jar > ${APOLLO_HOME}/lib/je-4.1.6.jar

## Creating a Broker Instance

A broker instance is the directory containing all the configuration and runtime
data such as logs and data files associated with a broker process.  It is recommended that
you do *not* create the instance directory under the directory where the ${project_name} 
distribution is installed.

On unix systems, it is a common convention to store this kind of runtime data under 
the `/var/lib` directory.  For example, to create an instance at '/var/lib/mybroker', run:

    cd /var/lib
    apollo create mybroker

A broker instance directory will contain the following sub directories:

 * `bin`: holds execution scripts associated with this instance.
 * `etc`; hold the instance configuration files
 * `data`: holds the data files used for storing persistent messages
 * `log`: holds rotating log files
 * `tmp`: holds temporary files that are safe to delete between broker runs

At this point you may want to adjust the default configuration located in
etc directory.

## Updating the Configuration to use BDB

The default configuration used a jdbm2 based store.  It has known performance issues so
it is recommend you change the configuration to use the BDB store instead.  To do that,
just update the generated configuration by editing the `etc/apollo.xml` file and then
replace `jdbm2_store` with `bdb_store`

## Running a Broker Instance

Assuming you created the broker instance under `/var/lib/mybroker` all you need
to do start running the broker instance is execute:

    /var/lib/mybroker/bin/apollo-broker run

## Verification

You can use the ruby examples included in the distribution to verify that the 
broker is operating correctly.

If you have not already done so, install the `stomp` Ruby gem.

    gem install stomp

Change to the `examples/ruby` directory that was included in the ${project_name} 
distribution.  Then in a terminal window, run:

{pygmentize_and_compare::}
-----------------------------
text: Unix/Linux/OS X
-----------------------------
cd ${APOLLO_HOME}/examples/ruby
ruby listener.rb
-----------------------------
text: Windows
-----------------------------
cd %APOLLO_HOME%\examples\ruby
ruby listener.rb
{pygmentize_and_compare}

Then in a separate terminal window, run:
{pygmentize_and_compare::}
-----------------------------
text: Unix/Linux/OS X
-----------------------------
cd ${APOLLO_HOME}/examples/ruby
ruby publisher.rb
-----------------------------
text: Windows
-----------------------------
cd %APOLLO_HOME%\examples\ruby
ruby publisher.rb
{pygmentize_and_compare}

If everything is working well, the publisher should produce output similar to:

    Sent 1000 messages
    Sent 1000 messages
    ...

The consumer's output should look like:
    
    Received 1000 messages.
    Received 2000 messages.
    ...

## Web Administration

Apollo provides a simple web interface to monitor the status of the broker.  Once
the admin interface will be accessible at:

* [http://127.0.0.1:61680/](http://127.0.0.1:61680/)

The default login id and password is `admin` and `password`.
