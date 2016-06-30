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
package org.unitime.timetable.gwt.client.instructor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.TakesValue;

/**
 * @author Tomas Muller
 */
public class ObjectivesCell extends P implements TakesValue<Map<String, Double>> {
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static NumberFormat sTeachingLoadFormat = NumberFormat.getFormat(CONSTANTS.teachingLoadFormat());
	protected TeachingRequestsPagePropertiesResponse iProperties;
	private Map<String, Double> iValue = null;
	
	public ObjectivesCell(TeachingRequestsPagePropertiesResponse properties) {
		super("objectives");
		iProperties = properties;
	}
	
	public ObjectivesCell(TeachingRequestsPagePropertiesResponse properties, Map<String, Double> values) {
		this(properties);
		setValue(values);
	}
	
	public ObjectivesCell(TeachingRequestsPagePropertiesResponse properties, Map<String, Double> initial, Map<String, Double> current) {
		this(properties);
		setValue(initial, current);
	}
	
	public static Map<String, Double> diff(Map<String, Double> initial, Map<String, Double> current) {
		Map<String, Double> ret = (current == null ? new HashMap<String, Double>() : new HashMap<String, Double>(current));
		if (initial != null) {
			for (Map.Entry<String, Double> e: initial.entrySet()) {
				String key = e.getKey();
				Double base = e.getValue();
				Double value = (current == null ? null : current.get(key));
				if (value == null)
					ret.put(key, -base);
				else
					ret.put(key, value - base);
			}
		}
		return ret;
	}
	
	@Override
	public void setValue(Map<String, Double> values) {
		iValue = values;
		clear();
		if (values != null) {
			for (String key: new TreeSet<String>(values.keySet())) {
				Double value = values.get(key);
				if (value == null || Math.abs(value) < 0.001) continue;
				P obj = new P("objective");
				obj.setText(key + ": " + (value > 0.0 ? "+": "") + sTeachingLoadFormat.format(value));
				if (key.endsWith(" Preferences")) {
					if (value <= -50.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("R").getColor());
					} else if (value <= -2.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("-2").getColor());
					} else if (value < 0.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("-1").getColor());
					} else if (value >= 50.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("P").getColor());
					} else if (value >= 2.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("2").getColor());
					} else if (value > 0.0) {
						obj.getElement().getStyle().setColor(iProperties.getPreference("1").getColor());
					}
				} else if (value < 0.0) {
					obj.getElement().getStyle().setColor("green");
				} else if (value > 0.0) {
					obj.getElement().getStyle().setColor("red");
				}
				add(obj);
			}
		}
	}
	
	public void setValue(Map<String, Double> initial, Map<String, Double> current) {
		setValue(diff(initial, current));
	}

	@Override
	public Map<String, Double> getValue() {
		return iValue;
	}

}
