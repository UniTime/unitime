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
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Tomas Muller
 */
public class AttributesCell extends P implements TakesValue<List<AttributeInterface>> {
	private List<AttributeInterface> iAttributes = null;
	
	public AttributesCell() {
		super("attributes");
	}
	
	public AttributesCell(List<AttributeInterface> attributes) {
		this();
		setValue(attributes);
	}
	
	public AttributesCell(List<AttributeInterface> oldAttributes, List<AttributeInterface> newAttributes) {
		this();
		setValue(oldAttributes, newAttributes);
	}
	
	@Override
	public void setValue(List<AttributeInterface> attributes) {
		clear();
		iAttributes = attributes;
		if (attributes != null) {
			for (AttributeInterface a: attributes) {
				P i = new P("attribute");
				i.setText(a.getName());
				i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
				add(i);
			}
		}
	}
	
	public void setValue(List<AttributeInterface> oldAttributes, List<AttributeInterface> newAttributes) {
		clear();
		iAttributes = newAttributes;
		if (oldAttributes != null) {
			for (AttributeInterface a: oldAttributes) {
				if (newAttributes == null || !newAttributes.contains(a)) {
					P i = new P("attribute", "old");
					i.setText(a.getName());
					i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
					add(i);
				}
			}
		}
		if (newAttributes != null) {
			if (oldAttributes != null)
				for (AttributeInterface a: newAttributes) {
					if (oldAttributes.contains(a)) {
						P i = new P("attribute", "same");
						i.setText(a.getName());
						i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
						add(i);
					}
				}
			for (AttributeInterface a: newAttributes) {
				if (oldAttributes == null || !oldAttributes.contains(a)) {
					P i = new P("attribute", "new");
					i.setText(a.getName());
					i.setTitle(a.getName() + " (" + a.getType().getLabel() + ")");
					add(i);
				}
			}
		}
	}

	@Override
	public List<AttributeInterface> getValue() {
		return iAttributes;
	}
}
