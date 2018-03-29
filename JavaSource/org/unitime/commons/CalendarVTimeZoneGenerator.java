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
package org.unitime.commons;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.unitime.timetable.defaults.ApplicationProperty;

import biweekly.component.VTimezone;
import biweekly.io.TimezoneInfo;
import biweekly.io.TzUrlDotOrgGenerator;
import biweekly.io.VTimezoneGenerator;
import biweekly.io.text.ICalReader;
import biweekly.property.TimezoneId;
import biweekly.util.IOUtils;

/**
 * A variant of the {@link TzUrlDotOrgGenerator} that has been made configurable using unitime.calendar.timezone property. 
 *
 */
public class CalendarVTimeZoneGenerator implements VTimezoneGenerator {
	private static final Map<URI, VTimezone> cache = Collections.synchronizedMap(new HashMap<URI, VTimezone>());

	@Override
	public VTimezone generate(TimeZone timezone) throws IllegalArgumentException {
		URI uri;
		try {
			String tz = ApplicationProperty.CalendarVTimeZoneID.value(timezone.getID());
			if (tz != null)
				uri = new URI(tz);
			else
				uri = new URI(ApplicationProperty.CalendarVTimeZone.value().replace("{id}", timezone.getID()));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}

		VTimezone component = cache.get(uri);
		if (component != null) {
			return component;
		}

		ICalReader reader = null;
		try {
			reader = new ICalReader(uri.toURL().openStream());
			reader.readNext();

			TimezoneInfo tzinfo = reader.getTimezoneInfo();
			component = tzinfo.getComponents().iterator().next();

			TimezoneId componentId = component.getTimezoneId();
			if (componentId == null) {
				/*
				 * There should always be a TZID property, but just in case
				 * there there isn't one, create one.
				 */
				component.setTimezoneId(timezone.getID());
			} else if (!timezone.getID().equals(componentId.getValue())) {
				/*
				 * Ensure that the value of the TZID property is identical to
				 * the ID of the Java TimeZone object. This is to ensure that
				 * the values of the TZID parameters throughout the iCal match
				 * the value of the VTIMEZONE component's TZID property.
				 * 
				 * For example, if tzurl.org is queried for the "PRC" timezone,
				 * then a VTIMEZONE component with a TZID of "Asia/Shanghai" is
				 * *actually* returned. This is a problem because iCal
				 * properties use the value of the Java TimeZone object to get
				 * the value of the TZID parameter, so the values of the TZID
				 * parameters and the VTIMEZONE component's TZID property will
				 * not be the same.
				 */
				componentId.setValue(timezone.getID());
			}

			cache.put(uri, component);
			return component;
		} catch (FileNotFoundException e) {
			throw notFound(e, timezone);
		} catch (NoSuchElementException e) {
			throw notFound(e, timezone);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	private IllegalArgumentException notFound(Exception e, TimeZone timezone) {
		return new IllegalArgumentException("Timezone " + timezone.getID() + " not recognized.", e);
	}
}
