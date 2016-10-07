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
package org.unitime.timetable.gwt.client.widgets;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class FilterPanel extends P {
	P iLeft = null, iMiddle = null, iRight = null;
	
	public FilterPanel()  {
		super("unitime-FilterPanel");
		iLeft = new P("filter-left"); super.add(iLeft);
		iRight = new P("filter-right"); super.add(iRight);
		iMiddle = new P("filter-middle"); super.add(iMiddle);
	}
	
	@Override
	public void add(Widget w) {
		w.addStyleName("filter-item");
		super.add(w);
	}
	
	public void addMiddle(Widget w) {
		w.addStyleName("filter-item");
		iMiddle.add(w);
	}
	
	public void addLeft(Widget w) {
		w.addStyleName("filter-item");
		iLeft.add(w);
	}
	
	public void addRight(Widget w) {
		w.addStyleName("filter-item");
		iRight.add(w);
	}

}
