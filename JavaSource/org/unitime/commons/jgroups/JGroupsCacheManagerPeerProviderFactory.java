/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.commons.jgroups;

import java.util.Properties;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.jgroups.JGroupsCacheManagerPeerProvider;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
public class JGroupsCacheManagerPeerProviderFactory extends CacheManagerPeerProviderFactory {

	@Override
    public CacheManagerPeerProvider createCachePeerProvider(CacheManager cacheManager, Properties properties) {
        try {
        	String configuration = JGroupsUtils.getConfigurationString(ApplicationProperty.HibernateClusterConfiguration.value());
        	JGroupsCacheManagerPeerProvider peerProvider = new JGroupsCacheManagerPeerProvider(cacheManager, configuration);
        	peerProvider.setChannelName("UniTime:hibernate");
            
            return peerProvider;
        } catch (Exception e) {
        	Debug.error(e.getMessage(), e);
        	return null;
        }
    }
}
