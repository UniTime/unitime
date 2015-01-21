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
package org.unitime.timetable.util;

import java.net.URL;
import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.listeners.SessionListener;


/**
 * Custom Implementation of MessageResources using Commons Configuration
 * Searches for file messages_{locale language}.properties. 
 * If not found uses messages.properties. 
 * Used mainly in development to avoid reloading of application.
 * 
 * @author Heston Fernandes, Tomas Muller
 */
public class MessageResources extends org.apache.struts.util.MessageResources {
	private static final long serialVersionUID = -3234198544455822319L;

	private static PropertiesConfiguration resource;
	private static String resourceFile;
	
	public MessageResources(
			MessageResourcesFactory factory, 
			String config, boolean returnNull ) {
		super(factory, config, returnNull);
	}

	public MessageResources(
			MessageResourcesFactory factory, String config ) {
		super(factory, config);
	}

	@Override
	public String getMessage(Locale locale, String key) {
		
		// See MessageResource documentation for complete list and order of search of respurce bundles
		// For our purpose only 2 file names are searched
		if (resource==null) {
			
			// get the configuration for the specified locale
			resource = (PropertiesConfiguration) getConfiguration(
					this.config + "_" + locale.getLanguage() + ".properties");
			resourceFile = this.config + "_" + locale.getLanguage() + ".properties";
	
			if (resource == null || !resource.containsKey(key)) {
				// look for the key in the root configuration
				resource = (PropertiesConfiguration) getConfiguration(this.config + ".properties");
				resourceFile = this.config + ".properties";
			}

			if (resource==null)
				return null;
		}
		System.out.println(key + " - " + resource.getString(key));
		return resource.getString(key);
	}

	private Configuration getConfiguration(String name) {
		Configuration configuration = null;
		URL url = Thread.currentThread().getContextClassLoader().getResource(name);
		if (url != null) {
			PropertiesConfiguration pc = new PropertiesConfiguration();
			pc.setURL(url);
			
			// Set reloading strategy 
			String dynamicReload = ApplicationProperties.getProperty("tmtbl.properties.dynamic_reload", null);
			if (dynamicReload!=null && dynamicReload.equalsIgnoreCase("true")) {
				long refreshDelay = Constants.getPositiveInteger(
						ApplicationProperties.getProperty("tmtbl.properties.dynamic_reload_interval"), 15000 );
				
				FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
				strategy.setRefreshDelay(refreshDelay); 
				pc.setReloadingStrategy(strategy);
				
				pc.addConfigurationListener(new MessageResourcesCfgListener(pc.getBasePath()));
			}			
			
			try {
				pc.load();
				configuration = pc;
			} catch (ConfigurationException e) {
				Debug.error("Message Resources configuration exception: " + e.getMessage());
			}
		}

		return configuration;
	}

	public static PropertiesConfiguration getResource() {
		return resource;
	}

	public static void setResource(PropertiesConfiguration resource) {
		MessageResources.resource = resource;
	}

	public static String getResourceFile() {
		return resourceFile;
	}

	public static void setResourceFile(String resourceFile) {
		MessageResources.resourceFile = resourceFile;
	}
	
	class MessageResourcesCfgListener implements ConfigurationListener {

		private String basePath;
		
		public MessageResourcesCfgListener(String basePath) {
			this.basePath = basePath;
		}
		
		public void configurationChanged(ConfigurationEvent arg0) {
			SessionListener.reloadMessageResources(basePath);
		}
		
	}
}

