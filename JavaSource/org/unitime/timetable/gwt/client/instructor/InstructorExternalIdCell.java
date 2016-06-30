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

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;

import com.google.gwt.core.client.GWT;

/**
 * @author Tomas Muller
 */
public class InstructorExternalIdCell extends P {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected TeachingRequestsPagePropertiesResponse iProperties;
	
	public InstructorExternalIdCell(TeachingRequestsPagePropertiesResponse properties) {
		super();
		iProperties = properties;
	}
	
	public InstructorExternalIdCell(TeachingRequestsPagePropertiesResponse properties, InstructorInfo instructor) {
		this(properties);
		setValue(instructor);
	}
	
	public void setValue(InstructorInfo instructor) {
		if (instructor == null) {
			setStyleName("not-assigned");
			setText(MESSAGES.notAssigned());
			getElement().getStyle().clearColor();
		} else {
			setStyleName(instructor.hasExternalId() ? "extid" : "no-extid");
			setText(instructor.hasExternalId() ? instructor.getExternalId() : MESSAGES.noExternalId());
			getElement().getStyle().clearColor();
			setTitle(instructor.getInstructorName() + (instructor.hasExternalId() ? " (" + instructor.getExternalId() + ")" : ""));
			if (instructor.getTeachingPreference() != null && !"0".equals(instructor.getTeachingPreference())) {
				PreferenceInterface pref = iProperties.getPreference(instructor.getTeachingPreference());
				if (pref != null) {
					setTitle(pref.getName() + " " + instructor.getInstructorName() + (instructor.hasExternalId() ? " (" + instructor.getExternalId() + ")" : ""));
					getElement().getStyle().setColor(pref.getColor());
				}
			}
		}
	}
}
