/*
 * Copyright (c) 2011 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License. See accompanying LICENSE file. 
 */
package io.s4.comm;

import io.s4.base.Emitter;
import io.s4.base.Hasher;
import io.s4.base.Listener;
import io.s4.base.SerializerDeserializer;
import io.s4.comm.serialize.KryoSerDeser;
import io.s4.comm.topology.Assignment;
import io.s4.comm.topology.AssignmentFromFile;
import io.s4.comm.topology.Cluster;
import io.s4.comm.topology.Topology;
import io.s4.comm.topology.TopologyFromFile;
import io.s4.comm.udp.UDPEmitter;
import io.s4.comm.udp.UDPListener;
import io.s4.comm.DefaultHasher;

import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.name.Names;

/**
 * Default comm layer module. To change the module, modify property
 * <tt>comm.module</tt> in <tt>/s4-comm.properties</tt>
 * 
 * @author Leo Neumeyer
 */
public class Module extends AbstractModule {

    protected PropertiesConfiguration config = null;

    private void loadProperties(Binder binder) {

        try {
            InputStream is = this.getClass().getResourceAsStream(
                    "/s4-comm.properties");
            config = new PropertiesConfiguration();
            config.load(is);

            System.out.println(ConfigurationUtils.toString(config));
            // TODO - validate properties.

            /* Make all properties injectable. Do we need this? */
            Names.bindProperties(binder,
                    ConfigurationConverter.getProperties(config));
        } catch (ConfigurationException e) {
            binder.addError(e);
            e.printStackTrace();
        }
    }

    @Override
    protected void configure() {
        if (config == null)
            loadProperties(binder());

        int numHosts = config.getList("cluster.hosts").size();
        boolean isCluster = numHosts > 1 ? true : false;
        bind(Boolean.class).annotatedWith(Names.named("isCluster"))
                .toInstance(Boolean.valueOf(isCluster));

        bind(Cluster.class);

        /* Configure static assignment using a configuration file. */
        bind(Assignment.class).to(AssignmentFromFile.class);

        /* Configure a static cluster topology using a configuration file. */
        bind(Topology.class).to(TopologyFromFile.class);

        // bind(Emitter.class).annotatedWith(Names.named("ll")).to(NettyEmitter.class);
        // bind(Listener.class).annotatedWith(Names.named("ll")).to(NettyListener.class);
        //
        // bind(Emitter.class).to(QueueingEmitter.class);
        // bind(Listener.class).to(QueueingListener.class);

        /* Use the Netty comm layer implementation. */
        // bind(Emitter.class).to(NettyEmitter.class);
        // bind(Listener.class).to(NettyListener.class);

        /* Use a simple UDP comm layer implementation. */
        bind(Emitter.class).to(UDPEmitter.class);
        bind(Listener.class).to(UDPListener.class);

        /* The hashing function to map keys top partitions. */
        bind(Hasher.class).to(DefaultHasher.class);

        /* Use Kryo to serialize events. */
        bind(SerializerDeserializer.class).to(KryoSerDeser.class);
    }
}