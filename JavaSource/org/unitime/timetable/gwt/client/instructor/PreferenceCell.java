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

import java.util.List;

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Tomas Muller
 */
public class PreferenceCell extends P implements TakesValue<List<PreferenceInfo>>{
	private TeachingRequestsPagePropertiesResponse iProperties;
	private List<PreferenceInfo> iPreferences = null;
	
	public PreferenceCell(TeachingRequestsPagePropertiesResponse properties) {
		super("preferences");
		iProperties = properties;
	}
	
	public PreferenceCell(TeachingRequestsPagePropertiesResponse properties, List<PreferenceInfo> prefs) {
		this(properties);
		setValue(prefs);
	}
	
	public void setValue(List<PreferenceInfo> prefs) {
		clear();
		iPreferences = prefs;
		if (prefs != null)
			for (PreferenceInfo p: prefs) {
				P prf = new P("prf");
				prf.setText(p.getOwnerName());
				PreferenceInterface preference = iProperties.getPreference(p.getPreference());
				if (preference != null) {
					prf.setTitle(preference.getName() + " " + p.getOwnerName());
					prf.getElement().getStyle().setColor(preference.getColor());
				}
				add(prf);
			}
	}
	
	public List<PreferenceInfo> getValue() {
		return iPreferences;
	}
}
