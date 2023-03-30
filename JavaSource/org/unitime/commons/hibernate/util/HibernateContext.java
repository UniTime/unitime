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
package org.unitime.commons.hibernate.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.ServiceRegistry;

/**
 * @author Tomas Muller
 */

public class HibernateContext {
	private ServiceRegistry iServiceRegistry;
	private Metadata iMetadata;
	private LoadedConfig iConfig;
	private SessionFactory iSessionFactory;
	
	public HibernateContext(LoadedConfig config, ServiceRegistry registry, Metadata meta, SessionFactory factory) {
		iServiceRegistry = registry;
		iConfig = config;
		iMetadata = meta;
		iSessionFactory = factory;
	}
	
	public ServiceRegistry getServiceRegistry() { return iServiceRegistry; }
	public void setServiceRegistry(ServiceRegistry serviceRegistry) { iServiceRegistry = serviceRegistry; }

	public Metadata getMetadata() { return iMetadata; }
	public void setMetadata(Metadata metadata) { iMetadata = metadata; }
	
	public LoadedConfig getConfig() { return iConfig; }
	public void setConfig(LoadedConfig config) { iConfig = config; }
	
	public SessionFactory getSessionFactory() { return iSessionFactory; }
	public void setSessionFactory(SessionFactory sessionFactory) { iSessionFactory = sessionFactory; }
	
	public String getProperty(String key) {
		return (String)getConfig().getConfigurationValues().get(key);
	}
	
	public PersistentClass getClassMapping(String entityName) {
		return getMetadata().getEntityBinding(entityName);
	}
}
