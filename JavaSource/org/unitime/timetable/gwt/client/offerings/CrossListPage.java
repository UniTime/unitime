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
package org.unitime.timetable.gwt.client.offerings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListGetRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListGetResponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListUpdateRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListedCourse;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class CrossListPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	private static final GwtResources RES = GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iPanel;
	private Label iLimit;
	private Label iError;
	private ListBox iCourses;
	private Button iAddCourse;
	private UniTimeHeaderPanel iHeader, iFooter;
	private CrossListGetResponse iCrossList;
	private UniTimeTable<CrossListedCourse> iTable;
	private int iLimitRow = -1;

	
	public CrossListPage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-CrossListPage");
		iHeader = new UniTimeHeaderPanel();
		
		iPanel.addHeaderRow(iHeader);
		
		P limitPanel = new P("offering-limit");
		iLimit = new Label();
		limitPanel.add(iLimit);
		iError = new Label(); iError.addStyleName("unitime-ErrorMessage");
		iError.setVisible(false);
		limitPanel.add(iError);
		iLimitRow = iPanel.addRow(COURSE.propertyIOLimit(), limitPanel);
		
		iCourses = new ListBox();
		iCourses.getElement().getStyle().setMarginRight(10, Unit.PX);
		iAddCourse = new Button(COURSE.actionAddCourseToCrossList());
		iAddCourse.setTitle(COURSE.titleAddCourseToCrossList(COURSE.accessAddCourseToCrossList()));
		iAddCourse.setAccessKey(COURSE.accessAddCourseToCrossList().charAt(0));
		iAddCourse.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				if (iCourses.getSelectedIndex() <= 0) return;
				CrossListedCourse clc = iCrossList.getAvailableCourse(Long.valueOf(iCourses.getSelectedValue()));
				int controllingLine = -1;
				for (int i = 1; i < iTable.getRowCount() - 1; i++) {
					RadioButton control = (RadioButton)iTable.getWidget(i, 1);
					if (control.getValue()) {
						controllingLine = i; break;
					}
				}
				for (int col = 0; col < iTable.getCellCount(iTable.getRowCount() - 2); col++)
					iTable.getCellFormatter().addStyleName(iTable.getRowCount() - 2, col, "BottomBorderGray");
				iTable.setRow(iTable.insertRow(iTable.getRowCount() - 1), clc, toLine(clc));
				if (controllingLine >= 0)
					((RadioButton)iTable.getWidget(controllingLine, 1)).setValue(true);
				for (int col = 0; col < iTable.getCellCount(iTable.getRowCount() - 2); col++)
					iTable.getCellFormatter().addStyleName(iTable.getRowCount() - 2, col, "BottomBorderGray");
				updateAvaliableCourses();
					
			}
		});
		
		P panel = new P("course-selection");
		panel.add(iCourses);
		panel.add(iAddCourse);
		P hint = new P("unitime-Hint");
		hint.setText(COURSE.hintCrossLists());
		panel.add(hint);
		iPanel.addRow(COURSE.propertyCourseOfferings(), panel);
		
		iTable = new UniTimeTable<CrossListedCourse>();
		iPanel.addRow("", iTable);
		
		iHeader.addButton("update", COURSE.actionUpdateCrossLists(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				CrossListUpdateRequest request = new CrossListUpdateRequest();
				request.setOfferingId(iCrossList.getOfferingId());
				request.setCourses(iTable.getData());
				LoadingWidget.getInstance().show(MESSAGES.waitSavingData());
				RPC.execute(request, new AsyncCallback<GwtRpcResponseNull>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(caught.getMessage());
						UniTimeNotifications.error(caught.getMessage(), caught);
					}

					@Override
					public void onSuccess(GwtRpcResponseNull response) {
						LoadingWidget.getInstance().hide();
						ToolBox.open(GWT.getHostPageBaseURL() + "offering?id=" + iCrossList.getOfferingId());
					}
				});
			}
		});
		iHeader.setEnabled("update", false);
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdateCrossLists().charAt(0));
		iHeader.getButton("update").setTitle(COURSE.titleUpdateCrossLists(COURSE.accessUpdateCrossLists()));
		
		iHeader.addButton("back", COURSE.actionBackToIODetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "offering?id=" + iCrossList.getOfferingId());
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToIODetail().charAt(0));
		iHeader.getButton("back").setTitle(COURSE.titleBackToIODetail(COURSE.accessBackToIODetail()));
		
		iFooter = iHeader.clonePanel();
		iPanel.addBottomRow(iFooter);
		initWidget(iPanel);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("instrOfferingId");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoOfferingId());
		} else {
			load(Long.valueOf(id));	
		}
	}
	
	static class RightLabel extends Label implements HasCellAlignment {
		RightLabel(String label) {
			super(label);
		}
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	static class Control extends RadioButton implements HasCellAlignment {
		Control() {
			super("controlling-course");
		}
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
	
	static class MyNumberBox extends NumberBox implements HasCellAlignment {
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	protected List<Widget> toLine(final CrossListedCourse clc) {
		List<Widget> line = new ArrayList<Widget>();
		line.add(new Label(clc.getCourseName()));
		
		RadioButton control = new Control();
		control.setValue(clc.isControl());
		control.setEnabled(clc.isCanEdit());
		control.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> arg0) {
				updateCounts();
			}
		});
		line.add(control);
		
		NumberBox limit = new MyNumberBox(); limit.setNegative(false); limit.setDecimal(false);
		limit.setValue(clc.getReserved());
		limit.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				updateCounts();
			}
		});
		line.add(limit);
		
		Label projected = new RightLabel(clc.hasProjected() ? clc.getProjected().toString() : "-");
		line.add(projected);
		
		Label lastterm = new RightLabel(clc.hasLastTerm() ? clc.getLastTerm().toString() : "-");
		line.add(lastterm);
		
		ImageButton delete = new ImageButton(RES.delete());
		delete.setEnabled(clc.isCanDelete());
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				if (iTable.getRowCount() <= 3) return;
				for (int i = 1; i < iTable.getRowCount() - 1; i++) {
					CrossListedCourse d = iTable.getData(i);
					if (clc.equals(d)) {
						iTable.removeRow(i);
						break;
					}
				}
				updateAvaliableCourses();
				updateCounts();
			}
		});
		line.add(delete);
		
		return line;
	}
	
	protected void updateAvaliableCourses() {
		iCourses.clear();
		Set<Long> listed = new HashSet<Long>();
		for (CrossListedCourse clc: iTable.getData())
			if (clc != null && clc.getCourseId() != null)
				listed.add(clc.getCourseId());
		iCourses.addItem(COURSE.itemSelect());
		if (iCrossList.hasAvailableCourses())
			for (CrossListedCourse clc: iCrossList.getAvailableCourses())
				if (!listed.contains(clc.getCourseId()))
					iCourses.addItem(clc.getCourseName(), clc.getCourseId().toString());
		updateCounts();
	}
	
	protected void updateCounts() {
		boolean hasControl = false;
		int total = 0;
		boolean hasLimit = false;
		boolean hasNoLimit = false;
		int projTotal = 0;
		int lastTermTotal = 0;
		int firstEditable = -1;
		for (int i = 1; i < iTable.getRowCount() - 1; i++) {
			CrossListedCourse clc = iTable.getData(i);
			RadioButton control = (RadioButton)iTable.getWidget(i, 1);
			if (control.getValue()) hasControl = true;
			NumberBox reserved = (NumberBox)iTable.getWidget(i, 2);
			Integer r = reserved.toInteger();
			if (r != null) { total += r; hasLimit = true; }
			else { hasNoLimit = true; }
			clc.setReserved(r);
			clc.setControl(control.getValue());
			ImageButton delete = (ImageButton)iTable.getWidget(i, 5);
			control.setEnabled(clc.isCanEdit() && (i > 1 || iTable.getRowCount() > 3));
			delete.setEnabled(clc.isCanDelete() && (i > 1 || iTable.getRowCount() > 3));
			delete.setVisible(clc.isCanDelete() && (i > 1 || iTable.getRowCount() > 3));
			reserved.setEnabled(iCrossList.isSingleCourseLimitAllowed() || i > 1 || iTable.getRowCount() > 3);
			reserved.setVisible(iCrossList.isSingleCourseLimitAllowed() || i > 1 || iTable.getRowCount() > 3);
			if (clc.getProjected() != null) projTotal += clc.getProjected();
			if (clc.getLastTerm() != null) lastTermTotal += clc.getLastTerm();
			if (firstEditable < 0 && clc.isCanEdit())
				firstEditable = i;
		}
		Label totalLabel = (Label)iTable.getWidget(iTable.getRowCount() - 1, 2);
		totalLabel.setText(total <= 0 ? "" : String.valueOf(total));
		Label projLabel = (Label)iTable.getWidget(iTable.getRowCount() - 1, 3);
		projLabel.setText(projTotal <= 0 ? "" : String.valueOf(projTotal));
		Label lastTermLabel = (Label)iTable.getWidget(iTable.getRowCount() - 1, 4);
		lastTermLabel.setText(lastTermTotal <= 0 ? "" : String.valueOf(lastTermTotal));
		if (!hasControl && firstEditable >= 0) {
			CrossListedCourse clc = iTable.getData(1);
			RadioButton control = (RadioButton)iTable.getWidget(firstEditable, 1);
			control.setValue(true);
			clc.setControl(true);
			hasControl = true;
		}
		if (!hasControl) {
			iError.setText(COURSE.errorRequiredControllingCourse());
			iError.setVisible(true);
			iHeader.setEnabled("update", false);
		} else if (!iCrossList.isUnlimited() && hasLimit && !hasNoLimit && iCrossList.getLimit() > total
			&& (iCrossList.isSingleCourseLimitAllowed() || iTable.getRowCount() > 3)) {
			iError.setText(COURSE.errorCrossListsLimitsDoNotMatch());
			iError.setVisible(true);
			iHeader.setEnabled("update", true);
		} else {
			iError.setText(""); iError.setVisible(false);
			iHeader.setEnabled("update", true);
		}
		if (!iCrossList.isUnlimited()) {
			if (total >= iCrossList.getLimit())
				totalLabel.getElement().getStyle().setColor("green");
			else
				totalLabel.getElement().getStyle().setColor("red");
		}
		iTable.getRowFormatter().setVisible(iTable.getRowCount() - 1, iTable.getRowCount() > 3);
		for (int col = 0; col < iTable.getCellCount(iTable.getRowCount() - 2); col++)
			iTable.getCellFormatter().removeStyleName(iTable.getRowCount() - 2, col, "BottomBorderGray");
	}
	
	protected void load(Long offeringId) {
		CrossListGetRequest request = new CrossListGetRequest();
		request.setOfferingId(offeringId);
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<CrossListGetResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(CrossListGetResponse response) {
				iCrossList = response;
				iHeader.setHeaderTitle(response.getOfferingName());
				if (response.isUnlimited()) {
					iLimit.setText("\u221E");
				} else if (response.getLimit() != null) {
					iLimit.setText(response.getLimit().toString());
				} else {
					iPanel.getRowFormatter().setVisible(iLimitRow, false);
				}
				iTable.clearTable();
				List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
				UniTimeTableHeader hCourse = new UniTimeTableHeader(COURSE.columnCrossListsOffering()); 
				header.add(hCourse);
				UniTimeTableHeader hControl = new UniTimeTableHeader(COURSE.columnCrossListsControlling());
				hControl.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				header.add(hControl);
				UniTimeTableHeader hReserved = new UniTimeTableHeader(COURSE.columnCrossListsReserved());
				hReserved.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				header.add(hReserved);
				UniTimeTableHeader hProjected = new UniTimeTableHeader(COURSE.columnCrossListsProjected());
				hProjected.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				header.add(hProjected);
				UniTimeTableHeader hLastTerm = new UniTimeTableHeader(COURSE.columnCrossListsLastTerm());
				hLastTerm.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				header.add(hLastTerm);
				header.add(new UniTimeTableHeader("&nbsp;"));
				iTable.addRow(null, header);
				
				int projTotal = 0;
				int lastTermTotal = 0;
				int controllingLine = -1;
				for (CrossListedCourse crs: response.getCourses()) {
					iTable.addRow(crs, toLine(crs));
					for (int col = 0; col < iTable.getCellCount(iTable.getRowCount() - 1); col++)
						iTable.getCellFormatter().addStyleName(iTable.getRowCount() - 1, col, "BottomBorderGray");
					if (crs.getProjected() != null) projTotal += crs.getProjected();
					if (crs.getLastTerm() != null) lastTermTotal += crs.getLastTerm();
					if (crs.isControl()) controllingLine = iTable.getRowCount() - 1;
				}
				if (controllingLine >= 0)
					((RadioButton)iTable.getWidget(controllingLine, 1)).setValue(true);
				for (int col = 0; col < iTable.getCellCount(iTable.getRowCount() - 1); col++)
					iTable.getCellFormatter().removeStyleName(iTable.getRowCount() - 1, col, "BottomBorderGray");
				
				List<Widget> totals = new ArrayList<Widget>();
				totals.add(new Label(COURSE.rowCrossListsTotal()));
				totals.add(new Label(""));
				RightLabel total = new RightLabel("" + projTotal);
				total.getElement().getStyle().setPaddingRight(5, Unit.PX);
				totals.add(total);
				totals.add(new RightLabel("" + lastTermTotal));
				totals.add(new RightLabel(""));
				totals.add(new Label(""));
				iTable.addRow(null, totals);
				for (int col = 0; col < totals.size(); col++) {
					iTable.getCellFormatter().addStyleName(iTable.getRowCount() - 1, col, "rowTotal");
				}
				updateAvaliableCourses();
				updateCounts();
				LoadingWidget.getInstance().hide();
				
				iHeader.setEnabled("update", true);
				iHeader.setEnabled("back", true);
			}
		});
	}

}
