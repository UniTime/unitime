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

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.ApplicationProperties;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Tomas Muller
 */
public class Localization {
	private static Log sLog = LogFactory.getLog(Localization.class);
	public static final String ROOT = "org.unitime.localization.messages.";
	private static Map<Class, Messages> sBundles = new Hashtable<Class, Messages>();
	
	private static final ThreadLocal<String> sLocale = new ThreadLocal<String>() {
		 @Override
		 protected String initialValue() {
             return ApplicationProperties.getProperty("unitime.locale", "en");
		 }
	};
	
	public static void setLocale(String locale) { sLocale.set(locale); }
	public static String getLocale() { return sLocale.get(); }
	public static String getFirstLocale() {
		String locale = getLocale();
		if (locale.indexOf(',') >= 0) locale = locale.substring(0, locale.indexOf(','));
		if (locale.indexOf(';') >= 0) locale = locale.substring(0, locale.indexOf(';'));
		return locale.trim();
	}
	
	public static <T extends Messages> T create(Class<T> bundle) {
		synchronized (sBundles) {
			T ret = (T)sBundles.get(bundle);
			if (ret == null) {
				ret = (T)Proxy.newProxyInstance(Localization.class.getClassLoader(), new Class[] {bundle, StrutsActionsRetriever.class}, new Bundle(bundle));
				sBundles.put(bundle, ret);
			}
			return ret;
		}
	}
		
	public static class Bundle implements InvocationHandler {
		private Map<String, Properties> iProperties = new Hashtable<String, Properties>();
		private Class<? extends Messages> iMessages = null;

		public Bundle(Class<? extends Messages> messages) {
			iMessages = messages;
		}
		
		private synchronized String getProperty(String locale, String name) {
			Properties properties = iProperties.get(locale);
			if (properties == null) {
				properties = new Properties();
				String resource = iMessages.getName().replace('.', '/') + (locale.isEmpty() ? "" : "_" + locale) + ".properties"; 
				try {
					InputStream is = Localization.class.getClassLoader().getResourceAsStream(resource);
					if (is != null)
						properties.load(is);
				} catch (Exception e) {
					sLog.warn("Failed to load message bundle " + iMessages.getName().substring(iMessages.getName().lastIndexOf('.') + 1) + " for " + locale + ": "  + e.getMessage(), e);
				}
				iProperties.put(locale, properties);
			}
			return properties.getProperty(name);
		}
		
		private String getProperty(String name) {
			for (String locale: getLocale().split(",")) {
				if (locale.indexOf(';') >= 0) locale = locale.substring(0, locale.indexOf(';'));
				String value = getProperty(locale.trim(), name);
				if (value != null) return value;
				if (locale.indexOf('_') >= 0) {
					locale = locale.substring(0, locale.indexOf('_'));
					value = getProperty(locale.trim(), name);
					if (value != null) return value;
				}
			}
			return getProperty("", name); // try default message bundle
		}
		
		private String fillArgumentsIn(String value, Object[] args) {
			if (value == null || args == null) return value;
			for (int i = 0; i < args.length; i++)
				value = value.replaceAll("\\{" + i + "\\}", (args[i] == null ? "" : args[i].toString()));
			return value;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getStrutsActions".equals(method.getName()) && method.getParameterTypes().length == 1)
				return getStrutsActions(proxy, (Class<? extends LocalizedLookupDispatchAction>) args[0]);
			String value = getProperty(method.getName());
			if (value != null) 
				return fillArgumentsIn(value, args);
			Messages.DefaultMessage dm = method.getAnnotation(Messages.DefaultMessage.class);
			if (dm != null)
				return fillArgumentsIn(dm.value(), args);
			return method.getName();
		}
		
		private Map<String, String> getStrutsActions(Object proxy, Class<? extends LocalizedLookupDispatchAction> apply) throws Throwable {
			Map<String, String> ret = new HashMap<String, String>();
			for (Method m: iMessages.getDeclaredMethods()) {
				if (m.getParameterTypes().length > 0) continue;
				org.unitime.localization.messages.Messages.StrutsAction action = m.getAnnotation(org.unitime.localization.messages.Messages.StrutsAction.class);
				if (action != null) {
					Messages.DefaultMessage dm = m.getAnnotation(Messages.DefaultMessage.class);
					if (action.apply() == null || action.apply().length == 0) {
						try {
							if (apply.getMethod(action.value(), new Class<?>[] {
								ActionMapping.class, ActionForm.class, HttpServletRequest.class, HttpServletResponse.class
								}) != null) {
								ret.put((String)invoke(proxy, m, new Object[] {}), action.value());
								if (dm != null)
									ret.put(dm.value(), action.value());
							}
						} catch (NoSuchMethodException e) {}
					} else {
						for (Class<? extends LocalizedLookupDispatchAction> a: action.apply())
							if (a.equals(apply)) {
								ret.put((String)invoke(proxy, m, new Object[] {}), action.value());
								if (dm != null)
									ret.put(dm.value(), action.value());
							}
					}
				}
			}
			return ret;
		}
	}
	
	public static interface StrutsActionsRetriever {
		Map<String, String> getStrutsActions(Class<? extends LocalizedLookupDispatchAction> apply);
	}

}
