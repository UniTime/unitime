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
package org.unitime.timetable.onlinesectioning.custom;

import org.apache.log4j.Logger;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;

/**
 * @author Tomas Muller
 */
public class Holder<T> {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	String iName;
	ApplicationProperty iProperty;
	T iProvider;
	String iProviderClass;
	Logger iLog;
	
	public Holder(Class<T> name, ApplicationProperty property) {
		iLog = Logger.getLogger(name);
		iName = name.getSimpleName().replaceAll("(?<=[^A-Z])([A-Z])"," $1");
		iProperty = property;
	}
	
	private void disposeProvider() {
		if (iProvider != null) {
			try {
				iLog.info("Disposing old provider");
				iProvider.getClass().getMethod("dispose").invoke(iProvider);
			} catch (Exception e) {
				iLog.warn("Failed to dispose: " + e.getMessage(), e);
			}
		}
		iProvider = null;
	}
	
	public synchronized T getProvider() {
		String providerClass = iProperty.value();
		if (providerClass == null || providerClass.isEmpty()) {
			if (iProvider != null)
				disposeProvider();
		} else if (!providerClass.equals(iProviderClass)) {
			if (iProvider != null)
				disposeProvider();
			iProviderClass = providerClass;
			iLog.info("Creating an instance of " + iProviderClass);
			try {
				iProvider = (T)Class.forName(iProviderClass).getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				iLog.error("Failed to create an instance of " + iProviderClass + ": " + e.getMessage(), e);
				throw new SectioningException(MSG.exceptionCustomProvider(iName, e.getMessage()), e);
			}
		}
		return iProvider;
	}
	
	public synchronized void release() {
		disposeProvider();
	}
	
	public synchronized boolean hasProvider() {
		return getProvider() != null;
	}
}
