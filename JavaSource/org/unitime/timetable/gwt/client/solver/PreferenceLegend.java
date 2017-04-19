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
package org.unitime.timetable.gwt.client.solver;

import java.util.List;

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;

/**
 * @author Tomas Muller
 */
public class PreferenceLegend extends P {
	
	public PreferenceLegend(List<PreferenceInterface> preferences) {
		super("unitime-PreferenceLegend");
		for (PreferenceInterface pref: preferences) {
			add(new PreferenceCell(pref));
		}
	}
	
	public static class PreferenceCell extends P {
		
		public PreferenceCell(PreferenceInterface pref) {
			super("legend-line");
			P box = new P("box"); box.getElement().getStyle().setBackgroundColor(pref.getColor());
			add(box);
			P text = new P("text"); text.setText(pref.getName());
			add(text);
		}
		
	}

}
