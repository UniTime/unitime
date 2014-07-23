/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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