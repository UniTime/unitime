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
package org.unitime.timetable.authenticate.jaas;

import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * Configure JAAS using tmtbl.authenticate.modules rather than
 * .java.login.config file.
 *
 * @author Tomas Muller
 */
@Deprecated
public class LoginConfiguration extends Configuration {
	private static AppConfigurationEntry[] sEntries = null;

	public LoginConfiguration() {
		super();
	}

	public void init() {
		Debug.info("Configuring authentication service ...");
		String m = ApplicationProperty.AuthenticationModules.value();
		String[] modules = (m == null || m.isEmpty() ? new String[] {} : m.split(";"));
		sEntries = new AppConfigurationEntry[modules.length];
		for (int idx = 0; idx < modules.length; idx++) {
			HashMap<String, Object> options = new HashMap<String, Object>();
			String[] module = modules[idx].split(" ");
			LoginModuleControlFlag flag = LoginModuleControlFlag.SUFFICIENT;
			String name = module[module.length == 1 ? 0 : 1];
			if (module.length > 1) {
				String f = module[0];
				if (f.equalsIgnoreCase("sufficient")) flag = LoginModuleControlFlag.SUFFICIENT;
				else if (f.equalsIgnoreCase("optional")) flag = LoginModuleControlFlag.OPTIONAL;
				else if (f.equalsIgnoreCase("required")) flag = LoginModuleControlFlag.REQUIRED;
				else if (f.equalsIgnoreCase("requisite")) flag = LoginModuleControlFlag.REQUISITE;
			}
			if (module.length > 2)
				for (int i = 2; i < module.length; i++) {
					String[] option = module[i].split("=");
					if (option.length == 1)
						options.put(option[0], "true");
					else
						options.put(option[0], option[1]);
				}
			Debug.info("  Using " + flag + " " + name + " " + options);
			sEntries[idx] = new AppConfigurationEntry(name, flag, options);
		}
	}

	public void refresh() {
		init();
	}

	public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
		if (sEntries == null) init();
		return sEntries;
	}
}