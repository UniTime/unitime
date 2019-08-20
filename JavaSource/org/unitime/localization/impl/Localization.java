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
package org.unitime.localization.impl;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.util.Formats;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Messages;

/**
 * @author Tomas Muller
 */
public class Localization {
	private static Log sLog = LogFactory.getLog(Localization.class);
	public static final String ROOT = "org.unitime.localization.messages.";
	public static final String GWTROOT = "org.unitime.timetable.gwt.resources.";
	private static Map<Class, Object> sBundles = new Hashtable<Class, Object>();
	
	private static final ThreadLocal<String> sLocale = new ThreadLocal<String>() {
		 @Override
		 protected String initialValue() {
			 return ApplicationProperty.Locale.value();
		 }
	};
	private static final ThreadLocal<Locale> sJavaLocale = new ThreadLocal<Locale>() {
		 @Override
		 protected Locale initialValue() {
            return guessJavaLocale(ApplicationProperty.Locale.value());
		 }
	};
	
	public static void setLocale(String locale) { 
		sLocale.set(locale);
		sJavaLocale.set(guessJavaLocale(locale));
	}
	
	public static void removeLocale() {
		sLocale.remove();
		sJavaLocale.remove();
	}
	
	public static String getLocale() { return sLocale.get(); }
	
	public static Locale getJavaLocale() { return sJavaLocale.get(); }
	
	public static DateFormat getDateFormat(String pattern) { return new SimpleDateFormat(pattern, getJavaLocale()); }
	
	public static NumberFormat getNumberFormat(String pattern) { return new DecimalFormat(pattern, new DecimalFormatSymbols(getJavaLocale())); }

	public static String getFirstLocale() {
		String locale = getLocale();
		if (locale.indexOf(',') >= 0) locale = locale.substring(0, locale.indexOf(','));
		if (locale.indexOf(';') >= 0) locale = locale.substring(0, locale.indexOf(';'));
		return locale.trim();
	}
	
	private static Locale guessJavaLocale(String locale) {
		for (StringTokenizer s = new StringTokenizer(locale, ",;"); s.hasMoreTokens(); ) {
			String lang = s.nextToken();
			String cc = null;
			if (lang.indexOf('_') >= 0) {
				cc = lang.substring(lang.indexOf('_') + 1);
				lang = lang.substring(0, lang.indexOf('_'));
			}
			for (Locale loc: Locale.getAvailableLocales())
				if ((lang == null || lang.isEmpty() || lang.equals(loc.getLanguage())) && (cc == null || cc.isEmpty() || cc.equals(loc.getCountry()))) {
					return loc;
				}
		}
		return Locale.getDefault();
	}
	
	public static <T> T create(Class<T> bundle) {
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
		private Class<?> iMessages = null;

		public Bundle(Class<?> messages) {
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
		
		private String fillArgumentsIn(String value, Object[] args, int firstIndex) {
			if (value == null || args == null) return value;
			for (int i = 0; i + firstIndex < args.length; i++) {
				value = value.replace("{" + i + "}", (args[i + firstIndex] == null ? "" : args[i + firstIndex].toString()));
				if (value.indexOf("{" + i + ",number,") >= 0) {
					int idx = value.indexOf("{" + i + ",number,");
					String pattern = value.substring(idx + ("{" + i + ",number,").length(), value.indexOf('}', idx));
					String number = (args[i + firstIndex] == null ? "" : Formats.getNumberFormat(pattern).format((Number)args[i + firstIndex]));
					value = value.replace("{" + i + ",number," + pattern + "}", number);
				}
			}
			return value;
		}
		
		private String[] string2array(String value) {
			return value.split("(?<=^.*[^\\\\]),(?=.*$)");
		}
		
		private Map<String, String> array2map(String[] value) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < value.length - 1; i += 2)
				map.put(value[i], value[i + 1]);
			return map;
		}
		
		private Object type(String value, Class returnType) {
			if (value == null) return value;
			if (String.class.equals(returnType))
				return value;
			
			if (Boolean.class.equals(returnType) || boolean.class.equals(returnType))
				return "true".equalsIgnoreCase(value);
			if (Double.class.equals(returnType) || double.class.equals(returnType))
				return Double.valueOf(value);
			if (Float.class.equals(returnType) || float.class.equals(returnType))
				return Float.valueOf(value);
			if (Integer.class.equals(returnType) || int.class.equals(returnType))
				return Integer.valueOf(value);

			if (String[].class.equals(returnType))
				return string2array(value);
			
			if (Map.class.equals(returnType)) {
				Map<String, String> map = new HashMap<String, String>();
				for (String key: string2array(value)) {
					String val = getProperty(key.trim());
					if (val != null) map.put(key.trim(), val);
				}
				if (map.isEmpty())
					return array2map(string2array(value));
				return map;
			}

			return value;
		}
		
		private String[] fixStringArray(String[] value, String[] defaults) {
			if (value != null && value.length < defaults.length) {
				String[] fixed = Arrays.copyOf(value, defaults.length);
				for (int i = value.length; i < defaults.length; i++)
					fixed[i] = defaults[i];
				return fixed;
			}
			return value;
		}
		
		private Map<String, String> fixStringMap(Map<String, String> value, Map<String, String> defaults) {
			if (value != null && !value.keySet().equals(defaults.keySet())) {
				Map<String, String> fixed = new HashMap<String, String>(value);
				for (Map.Entry<String, String> e: defaults.entrySet()) {
					if (!value.containsKey(e.getKey()))
						fixed.put(e.getKey(), e.getValue());
				}
				return fixed;
			}
			return value;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getStrutsActions".equals(method.getName()) && method.getParameterTypes().length == 1)
				return getStrutsActions(proxy, (Class<? extends LocalizedLookupDispatchAction>) args[0]);
			if ("translateMessage".equals(method.getName()) && method.getParameterTypes().length >= 2) {
				String value = (args[0] == null ? null : getProperty((String) args[0]));
				return (value == null ? (String) args[1] : fillArgumentsIn(value, args, 2));
			}
			String value = getProperty(method.getName());
			if (value != null) {
				Object ret = type(fillArgumentsIn(value, args, 0), method.getReturnType());
				if (String[].class.equals(method.getReturnType())) {
					Constants.DefaultStringArrayValue dsa = method.getAnnotation(Constants.DefaultStringArrayValue.class);
					if (dsa != null)
						return fixStringArray((String[])ret, dsa.value());
				}
				if (Map.class.equals(method.getReturnType())) {
					Constants.DefaultStringMapValue dsm = method.getAnnotation(Constants.DefaultStringMapValue.class);
					if (dsm != null)
						return fixStringMap((Map<String, String>)ret, array2map(dsm.value()));
				}
				return ret;
			}
			Messages.DefaultMessage dm = method.getAnnotation(Messages.DefaultMessage.class);
			if (dm != null)
				return fillArgumentsIn(dm.value(), args, 0);
			Constants.DefaultBooleanValue db = method.getAnnotation(Constants.DefaultBooleanValue.class);
			if (db != null)
				return db.value();
			Constants.DefaultDoubleValue dd = method.getAnnotation(Constants.DefaultDoubleValue.class);
			if (dd != null)
				return dd.value();
			Constants.DefaultFloatValue df = method.getAnnotation(Constants.DefaultFloatValue.class);
			if (df != null)
				return df.value();
			Constants.DefaultIntValue di = method.getAnnotation(Constants.DefaultIntValue.class);
			if (di != null)
				return di.value();
			Constants.DefaultStringValue ds = method.getAnnotation(Constants.DefaultStringValue.class);
			if (ds != null)
				return ds.value();
			Constants.DefaultStringArrayValue dsa = method.getAnnotation(Constants.DefaultStringArrayValue.class);
			if (dsa != null)
				return dsa.value();
			Constants.DefaultStringMapValue dsm = method.getAnnotation(Constants.DefaultStringMapValue.class);
			if (dsm != null)
				return array2map(dsm.value());
			
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
