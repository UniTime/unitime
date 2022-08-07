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
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingPreferencesDialog extends UniTimeDialogBox implements TakesValue<StudentSchedulingPreferencesInterface> {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iForm;
	private UniTimeHeaderPanel iFooter;
	private SingleDateSelector iDateFrom, iDateTo;
	private int iDatesLine;
	private ListBox iModality, iBackToBack;
	private P iModalityDesc, iBackToBackDesc;
	private StudentSchedulingPreferencesInterface iPreferences;
	private HTML iCustomNote;

	public StudentSchedulingPreferencesDialog(AcademicSessionProvider sessionProvider) {
		super(true, false);
		setEscapeToHide(true);
		addStyleName("unitime-StudentSchedulingPreferencesDialog");
		setHTML("<img src='" + RESOURCES.preferences().getSafeUri().asString() + "' class='gwt-Image'></img><span class='gwt-Label' style='padding-left: 5px; vertical-align: top;'>" + MESSAGES.dialogStudentSchedulingPreferences() + "</span>");
		iForm = new SimpleForm();
		
		iModality = new ListBox();
		iModality.addItem(MESSAGES.itemSchedulingModalityNoPreference(), "NoPreference");
		iModality.addItem(MESSAGES.itemSchedulingModalityPreferFaceToFace(), "DiscouragedOnline");
		iModality.addItem(MESSAGES.itemSchedulingModalityPreferOnline(), "PreferredOnline");
		iModality.addItem(MESSAGES.itemSchedulingModalityRequireOnline(), "RequiredOnline");
		iModality.addStyleName("selection");
		AbsolutePanel p = new AbsolutePanel(); p.setStyleName("modality");
		p.add(iModality);
		iModalityDesc = new P("description");
		iModalityDesc.setText(MESSAGES.descSchedulingModalityPreferFaceToFace());
		p.add(iModalityDesc);
		iModality.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				modalityChanged();
			}
		});
		iForm.addRow(MESSAGES.propSchedulingPrefModality(), p);
		
		iBackToBack = new ListBox();
		iBackToBack.addItem(MESSAGES.itemSchedulingBackToBackNoPreference(), "NoPreference");
		iBackToBack.addItem(MESSAGES.itemSchedulingBackToBackPrefer(), "PreferBackToBack");
		iBackToBack.addItem(MESSAGES.itemSchedulingBackToBackDiscourage(), "DiscourageBackToBack");
		iBackToBack.addStyleName("selection");
		AbsolutePanel q = new AbsolutePanel(); q.setStyleName("back-to-back");
		q.add(iBackToBack);
		iBackToBackDesc = new P("description");
		iBackToBackDesc.setText(MESSAGES.descSchedulingBackToBackNoPreference());
		q.add(iBackToBackDesc);
		iBackToBack.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				backToBackChanged();
			}
		});
		iForm.addRow(MESSAGES.propSchedulingPrefBackToBack(), q);
		
		AbsolutePanel m = new AbsolutePanel();
		m.setStyleName("dates");
		P from = new P("from"); from.setText(MESSAGES.propSchedulingPrefDatesFrom()); m.add(from);
		iDateFrom = new SingleDateSelector(sessionProvider);
		m.add(iDateFrom);
		P to = new P("to"); to.setText(MESSAGES.propSchedulingPrefDatesTo()); m.add(to);
		iDateTo = new SingleDateSelector(sessionProvider);
		m.add(iDateTo);
		P desc = new P("description"); desc.setText(MESSAGES.propSchedulingPrefDatesDescription()); m.add(desc);
		iDatesLine = iForm.addRow(MESSAGES.propSchedulingPrefDates(), m);
		
		iCustomNote = new HTML(); iCustomNote.addStyleName("custom-note"); iCustomNote.setText(""); iCustomNote.setVisible(false);
		iForm.addRow(iCustomNote);
		
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
	
	protected void modalityChanged() {
		if ("DiscouragedOnline".equals(iModality.getSelectedValue())) {
			iModalityDesc.setText(MESSAGES.descSchedulingModalityPreferFaceToFace());
		} else if ("PreferredOnline".equals(iModality.getSelectedValue())) {
			iModalityDesc.setText(MESSAGES.descSchedulingModalityPreferOnline());
		} else if ("RequiredOnline".equals(iModality.getSelectedValue())) {
			iModalityDesc.setText(MESSAGES.descSchedulingModalityRequireOnline());
		} else if ("NoPreference".equals(iModality.getSelectedValue())) {
			iModalityDesc.setText(MESSAGES.descSchedulingModalityNoPreference());
		}
	}
	
	protected void backToBackChanged() {
		if ("PreferBackToBack".equals(iBackToBack.getSelectedValue())) {
			iBackToBackDesc.setText(MESSAGES.descSchedulingBackToBackPrefer());
		} else if ("DiscourageBackToBack".equals(iBackToBack.getSelectedValue())) {
			iBackToBackDesc.setText(MESSAGES.descSchedulingBackToBackDiscourage());
		} else if ("NoPreference".equals(iBackToBack.getSelectedValue())) {
			iBackToBackDesc.setText(MESSAGES.descSchedulingBackToBackNoPreference());
		}
	}
	
	@Override
	public void center() {
		super.center();
		iModality.setFocus(true);
	}
	
	protected void doApply() {
		hide();
	}

	@Override
	public StudentSchedulingPreferencesInterface getValue() {
		iPreferences.setClassModality(StudentSchedulingPreferencesInterface.ClassModality.valueOf(iModality.getSelectedValue()));
		iPreferences.setScheduleGaps(StudentSchedulingPreferencesInterface.ScheduleGaps.valueOf(iBackToBack.getSelectedValue()));
		iPreferences.setClassDateFrom(iPreferences.isAllowClassDates() ? iDateFrom.getValueInServerTimeZone() : null);
		iPreferences.setClassDateTo(iPreferences.isAllowClassDates() ? iDateTo.getValueInServerTimeZone() : null);
		return new StudentSchedulingPreferencesInterface(iPreferences);
	}

	@Override
	public void setValue(StudentSchedulingPreferencesInterface value) {
		iPreferences = value;
		iForm.getRowFormatter().setVisible(iDatesLine, iPreferences.isAllowClassDates());
		iModality.clear();
		iModality.addItem(MESSAGES.itemSchedulingModalityNoPreference(), "NoPreference");
		iModality.addItem(MESSAGES.itemSchedulingModalityPreferFaceToFace(), "DiscouragedOnline");
		iModality.addItem(MESSAGES.itemSchedulingModalityPreferOnline(), "PreferredOnline");
		if (iPreferences.isAllowRequireOnline())
			iModality.addItem(MESSAGES.itemSchedulingModalityRequireOnline(), "RequiredOnline");
		if (iPreferences.getClassModality() != null)
			for (int i = 0; i < iModality.getItemCount(); i++)
				if (iPreferences.getClassModality().name().equals(iModality.getValue(i))) {
					iModality.setSelectedIndex(i);
					modalityChanged();
					break;
				}
		if (iPreferences.getScheduleGaps() != null) {
			for (int i = 0; i < iBackToBack.getItemCount(); i++)
				if (iPreferences.getScheduleGaps().name().equals(iBackToBack.getValue(i))) {
					iBackToBack.setSelectedIndex(i);
					backToBackChanged();
					break;
				}
		}
		iDateFrom.setValueInServerTimeZone(iPreferences.isAllowRequireOnline() ? iPreferences.getClassDateFrom() : null);
		iDateTo.setValueInServerTimeZone(iPreferences.isAllowRequireOnline() ? iPreferences.getClassDateTo() : null);
		if (iPreferences.hasCustomNote()) {
			iCustomNote.setHTML(iPreferences.getCustomNote());
			iCustomNote.setVisible(true);
		} else {
			iCustomNote.setHTML("");
			iCustomNote.setVisible(false);
		}
	}
}
