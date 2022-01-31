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
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingPreferencesDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iForm;
	private UniTimeHeaderPanel iFooter;
	private SingleDateSelector iDateFrom, iDateTo;
	private ListBox iModality, iBackToBack;
	private P iModalityDesc, iBackToBackDesc;

	public StudentSchedulingPreferencesDialog() {
		super(true, false);
		setEscapeToHide(true);
		addStyleName("unitime-StudentSchedulingPreferencesDialog");
		setHTML("<img src='" + RESOURCES.preferences().getSafeUri().asString() + "' class='gwt-Image'></img><span class='gwt-Label' style='padding-left: 5px; vertical-align: top;'>" + MESSAGES.dialogStudentSchedulingPreferences() + "</span>");
		iForm = new SimpleForm();
		
		iModality = new ListBox();
		iModality.addItem(MESSAGES.itemSchedulingModalityPreferFaceToFace(), "f2f");
		iModality.addItem(MESSAGES.itemSchedulingModalityPreferOnline(), "online");
		iModality.addItem(MESSAGES.itemSchedulingModalityRequireOnline(), "req-online");
		iModality.addItem(MESSAGES.itemSchedulingModalityNoPreference(), "no-pref");
		iModality.addStyleName("selection");
		AbsolutePanel p = new AbsolutePanel(); p.setStyleName("modality");
		p.add(iModality);
		iModalityDesc = new P("description");
		iModalityDesc.setText(MESSAGES.descSchedulingModalityPreferFaceToFace());
		p.add(iModalityDesc);
		iModality.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if ("f2f".equals(iModality.getSelectedValue())) {
					iModalityDesc.setText(MESSAGES.descSchedulingModalityPreferFaceToFace());
				} else if ("online".equals(iModality.getSelectedValue())) {
					iModalityDesc.setText(MESSAGES.descSchedulingModalityPreferOnline());
				} else if ("req-online".equals(iModality.getSelectedValue())) {
					iModalityDesc.setText(MESSAGES.descSchedulingModalityRequireOnline());
				} else if ("no-pref".equals(iModality.getSelectedValue())) {
					iModalityDesc.setText(MESSAGES.descSchedulingModalityNoPreference());
				}
			}
		});
		iForm.addRow(MESSAGES.propSchedulingPrefModality(), p);
		
		iBackToBack = new ListBox();
		iBackToBack.addItem(MESSAGES.itemSchedulingBackToBackNoPreference(), "no-ref");
		iBackToBack.addItem(MESSAGES.itemSchedulingBackToBackPrefer(), "prefer");
		iBackToBack.addItem(MESSAGES.itemSchedulingBackToBackDiscourage(), "discourage");
		iBackToBack.addStyleName("selection");
		AbsolutePanel q = new AbsolutePanel(); q.setStyleName("back-to-back");
		q.add(iBackToBack);
		iBackToBackDesc = new P("description");
		iBackToBackDesc.setText(MESSAGES.descSchedulingBackToBackNoPreference());
		q.add(iBackToBackDesc);
		iBackToBack.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if ("prefer".equals(iBackToBack.getSelectedValue())) {
					iBackToBackDesc.setText(MESSAGES.descSchedulingBackToBackPrefer());
				} else if ("discourage".equals(iBackToBack.getSelectedValue())) {
					iBackToBackDesc.setText(MESSAGES.descSchedulingBackToBackDiscourage());
				} else if ("no-pref".equals(iBackToBack.getSelectedValue())) {
					iBackToBackDesc.setText(MESSAGES.descSchedulingBackToBackNoPreference());
				}
			}
		});
		iForm.addRow(MESSAGES.propSchedulingPrefBackToBack(), q);
		
		AbsolutePanel m = new AbsolutePanel();
		m.setStyleName("dates");
		P from = new P("from"); from.setText(MESSAGES.propSchedulingPrefDatesFrom()); m.add(from);
		iDateFrom = new SingleDateSelector();
		m.add(iDateFrom);
		P to = new P("to"); to.setText(MESSAGES.propSchedulingPrefDatesTo()); m.add(to);
		iDateTo = new SingleDateSelector();
		m.add(iDateTo);
		P desc = new P("description"); desc.setText(MESSAGES.propSchedulingPrefDatesDescription()); m.add(desc);
		iForm.addRow(MESSAGES.propSchedulingPrefDates(), m);
		
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("apply", MESSAGES.buttonSchedulingPrefApply(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doApply();
			}
		});
		
		iFooter.addButton("close", MESSAGES.buttonSchedulingPrefClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iForm.addBottomRow(iFooter);
		setWidget(iForm);
		
	}
	
	@Override
	public void center() {
		super.center();
		iModality.setFocus(true);
	}
	
	protected void doApply() {
		hide();
	}

}
