/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.authenticate.jaas;

import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

/**
 * Configure JAAS using tmtbl.authenticate.modules rather than .java.login.config file.
 * @author Tomas Muller
 *
 */
public class LoginConfiguration extends Configuration {
	private static AppConfigurationEntry[] sEntries = null;
	
	public LoginConfiguration() {
		super();
	}
	
	public void init() {
		Debug.info("Configuring authentication service ...");
		HashMap<String, Object> options = new HashMap<String, Object>();
		String[] module = ApplicationProperties.getProperty("tmtbl.authenticate.modules",
				"sufficient " + DbAuthenticateModule.class.getName() + ";" +
				"sufficient " + LdapAuthenticateModule.class.getName()).split(";");
		sEntries = new AppConfigurationEntry[module.length];
		for (int idx = 0; idx < module.length; idx++) {
			LoginModuleControlFlag flag = LoginModuleControlFlag.SUFFICIENT;
			String m = module[idx];
			if (m.indexOf(' ') > 0) {
				String f = m.substring(0, m.indexOf(' '));
				m = m.substring(m.indexOf(' ') + 1);
				if (f.equalsIgnoreCase("sufficient"))
					flag = LoginModuleControlFlag.SUFFICIENT;
				else if (f.equalsIgnoreCase("optional"))
					flag = LoginModuleControlFlag.OPTIONAL;
				else if (f.equalsIgnoreCase("required"))
					flag = LoginModuleControlFlag.REQUIRED;
				else if (f.equalsIgnoreCase("requisite"))
					flag = LoginModuleControlFlag.REQUISITE;
			}
			Debug.info("  Using " + m + " (" + flag + ")");
			sEntries[idx] = new AppConfigurationEntry(m, flag, options);
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
