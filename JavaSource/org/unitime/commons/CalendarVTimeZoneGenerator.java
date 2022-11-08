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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.unitime.timetable.defaults.ApplicationProperty;

import biweekly.ICalendar;
import biweekly.component.VTimezone;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.TzUrlDotOrgGenerator;
import biweekly.io.text.ICalReader;
import biweekly.property.TimezoneId;
import biweekly.property.ValuedProperty;
import biweekly.util.IOUtils;

/**
 * A variant of the {@link TzUrlDotOrgGenerator} that has been made configurable using unitime.calendar.timezone property. 
 *
 */
public class CalendarVTimeZoneGenerator {
	private static final Map<URI, VTimezone> cache = Collections.synchronizedMap(new HashMap<URI, VTimezone>());

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
			return component.copy();
		}

		ICalendar ical;
		ICalReader reader = null;
		try {
			reader = new ICalReader(getInputStream(uri));
			ical = reader.readNext();
		} catch (FileNotFoundException e) {
			throw notFound(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}

		/*
		 * There should always be exactly one iCalendar object in the file, but
		 * check to be sure.
		 */
		if (ical == null) {
			throw notFound(null);
		}

		/*
		 * There should always be exactly one VTIMEZONE component, but check to
		 * be sure.
		 */
		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		Collection<VTimezone> components = tzinfo.getComponents();
		if (components.isEmpty()) {
			components = ical.getComponents(VTimezone.class); //VTIMEZONE components without TZID properties are treated as ordinary components
			if (components.isEmpty()) {
				throw notFound(null);
			}
		}

		component = components.iterator().next();

		/*
		 * There should always be a TZID property, but just in case there there
		 * isn't one, create one.
		 */
		TimezoneId id = component.getTimezoneId();
		if (id == null) {
			component.setTimezoneId(timezone.getID());
		} else {
			String value = ValuedProperty.getValue(id);
			if (value == null || value.trim().isEmpty()) {
				id.setValue(timezone.getID());
			}
		}

		cache.put(uri, component);
		return component.copy();
	}
	
	InputStream getInputStream(URI uri) throws IOException {
		return uri.toURL().openStream();
	}

	public static void clearCache() {
		cache.clear();
	}

	private static IllegalArgumentException notFound(Exception e) {
		return new IllegalArgumentException("Timezone ID not recognized.", e);
	}

	
	public static TimezoneAssignment download(TimeZone timezone) {
		CalendarVTimeZoneGenerator generator = new CalendarVTimeZoneGenerator();
		VTimezone component = generator.generate(timezone);
		return new TimezoneAssignment(timezone, component);
	}
}
