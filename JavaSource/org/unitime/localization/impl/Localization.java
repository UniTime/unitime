/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.localization.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.localization.messages.Messages;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Tomas Muller
 */
public class Localization {
	private static Log sLog = LogFactory.getLog(Localization.class);
	public static final String ROOT = "org.unitime.localization.messages.";
	
	public static <T> T create(Class<? extends Messages> bundle) {
		return create(bundle, ApplicationProperties.getProperty("unitime.locale", "en"));
	}

	public static <T> T create(Class<? extends Messages> bundle, String locale) {
		return (T)Proxy.newProxyInstance(Localization.class.getClassLoader(), new Class[] {bundle, StrutsActionsRetriever.class}, new Bundle(bundle, locale));
	}
		
	public static class Bundle implements InvocationHandler {
		List<Properties> iProperties = new ArrayList<Properties>();
		Class<? extends Messages> iMessages = null;

		public Bundle(Class<? extends Messages> messages, String locale) {
			iMessages = messages;
			for (String loc: locale.split(",")) {
				String resource = messages.getName().replace('.', '/') + "_" + loc + ".properties"; 
				try {
					InputStream is = Localization.class.getClassLoader().getResourceAsStream(resource);
					if (is != null)
						load(is);
				} catch (Exception e) {
					sLog.warn("Failed to load bundle " + messages.getName().substring(messages.getName().lastIndexOf('.') + 1) + " for " + loc + ": "  + e.getMessage(), e);
				}
			}
			String resource = messages.getName().replace('.', '/') + ".properties";
			try {
				InputStream is = Localization.class.getClassLoader().getResourceAsStream(resource);
				if (is != null)
					load(is);
			} catch (Exception e) {
				sLog.warn("Failed to load default bundle " + messages.getName().substring(messages.getName().lastIndexOf('.') + 1) + ": "  + e.getMessage(), e);
			}
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getStrutsActions".equals(method.getName()) && method.getParameterTypes().length == 0)
				return getStrutsActions(proxy);
			for (Properties p: iProperties) {
				String val = p.getProperty(method.getName());
				if (val != null) {
					if (args != null)
						for (int i = 0; i < args.length; i++)
							val = val.replaceAll("\\{" + (1 + i) + "\\}", (args[i] == null ? "" : args[i].toString()));
					return val;
				}
			}
			Messages.DefaultMessage dm = method.getAnnotation(Messages.DefaultMessage.class);
			if (dm != null) {
				String val = dm.value();
				if (args != null)
					for (int i = 0; i < args.length; i++)
						val = val.replaceAll("\\{" + (1 + i) + "\\}", (args[i] == null ? "" : args[i].toString()));
				return val;
			}				
			return method.getName();
		}
		
		private Map<String, String> getStrutsActions(Object proxy) throws Throwable {
			Map<String, String> ret = new HashMap<String, String>();
			for (Method m: iMessages.getDeclaredMethods()) {
				if (m.getParameterTypes().length > 0) continue;
				Messages.StrutsAction action = m.getAnnotation(Messages.StrutsAction.class);
				if (action != null)
					ret.put((String)invoke(proxy, m, new Object[] {}), action.value());
			}
			return ret;
		}
		
		void load(InputStream in) throws IOException {
			Properties p = new Properties();
			p.load(in);
			iProperties.add(p);
		}
	}
	
	public static interface StrutsActionsRetriever {
		Map<String, String> getStrutsActions();
	}

}
