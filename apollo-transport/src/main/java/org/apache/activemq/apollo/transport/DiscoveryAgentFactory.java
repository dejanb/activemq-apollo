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
package org.apache.activemq.apollo.transport;

import org.apache.activemq.apollo.util.JavaClassFinder;

import java.util.List;

public class DiscoveryAgentFactory {

    public interface Provider {
        public DiscoveryAgent create(String uri) throws Exception;
    }

    static public List<Provider> providers;

    static {
        JavaClassFinder<Provider> finder = new JavaClassFinder<Provider>("META-INF/services/org.apache.activemq.apollo/discovery-agent-factory.index");
        providers = finder.new_instances();
    }

    /**
     * Creates a DiscoveryAgent
     */
    public static DiscoveryAgent create(String uri) throws Exception {
        for( Provider provider : providers) {
          DiscoveryAgent rc = provider.create(uri);
          if( rc!=null ) {
            return rc;
          }
        }
        throw new IllegalArgumentException("Unknown discovery agent uri: "+uri);
    }

}
