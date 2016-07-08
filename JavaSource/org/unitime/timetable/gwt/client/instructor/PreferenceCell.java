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
	
	public PreferenceCell(TeachingRequestsPagePropertiesResponse properties, List<PreferenceInfo> oldPrefs, List<PreferenceInfo> newPrefs) {
		this(properties);
		setValue(oldPrefs, newPrefs);
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
	
	public void setValue(List<PreferenceInfo> oldPrefs, List<PreferenceInfo> newPrefs) {
		clear();
		iPreferences = newPrefs;
		if (oldPrefs != null) {
			for (PreferenceInfo p: oldPrefs) {
				boolean found = false;
				if (newPrefs != null) {
					for (PreferenceInfo q: newPrefs) {
						if (p.equals(q)) {
							found = true;
							if (p.getPreference().equals(q.getPreference())) {
								P prf = new P("prf", "same");
								prf.setText(p.getOwnerName());
								PreferenceInterface preference = iProperties.getPreference(p.getPreference());
								if (preference != null) {
									prf.setTitle(preference.getName() + " " + p.getOwnerName());
									prf.getElement().getStyle().setColor(preference.getColor());
								}
								add(prf);
							} else {
								P cmp = new P("compare");
								P p1 = new P("prf", "old");
								p1.setText(p.getOwnerName());
								PreferenceInterface preference = iProperties.getPreference(p.getPreference());
								if (preference != null) {
									p1.setTitle(preference.getName() + " " + p.getOwnerName());
									p1.getElement().getStyle().setColor(preference.getColor());
								}
								cmp.add(p1);
								P a = new P("arrow"); a.setHTML("&rarr;"); cmp.add(a);
								cmp.add(a);
								P p2 = new P("prf", "new");
								p2.setText(p.getOwnerName());
								preference = iProperties.getPreference(p.getPreference());
								if (preference != null) {
									p2.setTitle(preference.getName() + " " + p.getOwnerName());
									p2.getElement().getStyle().setColor(preference.getColor());
								}
								cmp.add(p2);
								add(cmp);
							}
						}
					}
				}
				if (!found) {
					P prf = new P("prf", "old");
					prf.setText(p.getOwnerName());
					PreferenceInterface preference = iProperties.getPreference(p.getPreference());
					if (preference != null) {
						prf.setTitle(preference.getName() + " " + p.getOwnerName());
						prf.getElement().getStyle().setColor(preference.getColor());
					}
					add(prf);
				}
			}
		}
		if (newPrefs != null)
			for (PreferenceInfo p: newPrefs) {
				if (oldPrefs != null && oldPrefs.contains(p)) continue;
				P prf = new P("prf", "new");
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
