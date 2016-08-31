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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.Clazz;
import org.unitime.timetable.gwt.shared.ReservationInterface.Config;
import org.unitime.timetable.gwt.shared.ReservationInterface.Subpart;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.GetRequestsRpcRequest;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.GetRequestsRpcResponse;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.IncludeLine;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.MultiRequest;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.Properties;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.Request;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.RequestedClass;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.Responsibility;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.SaveRequestsRpcRequest;
import org.unitime.timetable.gwt.shared.TeachingRequestInterface.SingleRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class SetupTeachingRequestsPage extends SimpleForm {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final StudentSectioningMessages SECTMSG = GWT.create(StudentSectioningMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private Long iOfferingId = null;
	private Properties iProperties;
	private UniTimeHeaderPanel iHeader, iFooter;
	private List<RequestPanel> iRequests = new ArrayList<RequestPanel>();

	public SetupTeachingRequestsPage() {
		addStyleName("unitime-SetupTeachingRequests");
		iOfferingId = Long.valueOf(Location.getParameter("offeringId"));
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("add", MESSAGES.buttonAddTeachingRequest(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RequestPanel rp = new RequestPanel(null, iRequests.size() + 1);
				iRequests.add(rp);
				int row = insertRow(getRowCount() - 1);
				getFlexCellFormatter().setColSpan(row, 0, getColSpan());
				setWidget(row, 0, rp);
				requestsChanged();
				ToolBox.scrollToElement(rp.getElement());
			}
		});
		iHeader.addButton("save", MESSAGES.buttonSaveTeachingRequests(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				SaveRequestsRpcRequest request = new SaveRequestsRpcRequest();
				request.setOfferingId(iOfferingId);
				for (RequestPanel rp: iRequests) {
					Request r = rp.getRequest();
					if (r != null) request.addRequest(r);
				}
				LoadingWidget.getInstance().show(MESSAGES.waitSaveTeachingRequests(iProperties == null ? iOfferingId.toString() : iProperties.getOffering().getAbbv()));
				RPC.execute(request, new AsyncCallback<GwtRpcResponseNull>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedSave(caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(GwtRpcResponseNull result) {
						LoadingWidget.getInstance().hide();
						ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?io=" + iOfferingId + "&op=view#instructors");
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?io=" + iOfferingId + "&op=view#instructors");
			}
		});
		iHeader.setEnabled("add", false);
		addHeaderRow(iHeader);
		iFooter = iHeader.clonePanel();
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
		GetRequestsRpcRequest request = new GetRequestsRpcRequest(); request.setOfferingId(iOfferingId);
		RPC.execute(request, new AsyncCallback<GetRequestsRpcResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);				
			}

			@Override
			public void onSuccess(GetRequestsRpcResponse result) {
				LoadingWidget.getInstance().hide();
				iProperties = result;
				iHeader.setHeaderTitle(result.getOffering().getAbbv() + " - " + result.getOffering().getName());
				for (Request req: result.getRequests()) {
					RequestPanel rp = new RequestPanel(req, iRequests.size() + 1);
					iRequests.add(rp);
					addRow(rp);
				}
				if (iRequests.isEmpty()) {
					RequestPanel rp = new RequestPanel(null, iRequests.size() + 1);
					iRequests.add(rp);
					addRow(rp);
				}
				addBottomRow(iFooter);
				iHeader.setEnabled("add", true);
				requestsChanged();
			}
		});
	}
	
	protected void requestsChanged() {
		for (int i = 0; i < iRequests.size(); i++)
			iRequests.get(i).setIndex(1 + i);
		if (iRequests.isEmpty()) {
			iHeader.setMessage(MESSAGES.messageNoTeachingRequests(iProperties == null ? iOfferingId.toString() : iProperties.getOffering().getName()));
		} else {
			iHeader.clearMessage();
		}
	}
	
	public class RequestPanel extends SimpleForm {
		private UniTimeHeaderPanel iRequestHead;
		private Request iRequest;
		private NumberBox iNbrInstructors;
		private int iNbrInstructorsRow;
		private NumberBox iTeachingLoad;
		private CheckBox iSimple;
		private ListBox iSubpart;
		private int iSubpartRow;
		private ListBox iSameCouse;
		private ListBox iSameCommon;
		private int iSameCommonRow;
		private ListBox iResponsibility;
		private int iResponsibilityRow;
		private UniTimeTable<Clazz> iClasses;
		private int iClassesRow;
		private CheckBox iCoordinator;
		private int iCoordinatorRow;
		private UniTimeTable<Subpart> iSubparts;
		private int iSubpartsLine;
		private Map<String, UniTimeTable<Preference>> iAttributes;
		private UniTimeTable<Preference> iInstructors;
		
		RequestPanel(Request request, int index) {
			removeStyleName("unitime-NotPrintableBottomLine");
			iRequestHead = new UniTimeHeaderPanel(MESSAGES.sectTeachingRequest(index));
			iRequestHead.addButton("remove", MESSAGES.buttonRemoveTeachingRequest(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					int row = SetupTeachingRequestsPage.this.getCellForEvent(event).getRowIndex();//getRowForWidget(RequestPanel.this);
					if (row > 0) {
						SetupTeachingRequestsPage.this.removeRow(row);
						iRequests.remove(RequestPanel.this);
						requestsChanged();
					}
				}
			});
			addHeaderRow(iRequestHead);
			iRequest = request;
			if (request == null) {
				iSimple = new CheckBox();
				iSimple.setValue(true);
				addRow(MESSAGES.propSetupTeachingRequestMulti(), iSimple);
				iSimple.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						simpleChanged();
					}
				});
			}
			iNbrInstructors = new NumberBox(); iNbrInstructors.setWidth("20px");
			iNbrInstructors.setDecimal(false); iNbrInstructors.setNegative(false);
			if (request != null && request instanceof SingleRequest)
				iNbrInstructors.setValue(((SingleRequest)request).getNbrInstructors());
			else
				iNbrInstructors.setValue(1);
			iNbrInstructorsRow = addRow(MESSAGES.propNbrInstructors(), iNbrInstructors);
			getRowFormatter().setVisible(iNbrInstructorsRow, request != null && request instanceof SingleRequest);
			iTeachingLoad = new NumberBox(); iTeachingLoad.setWidth("50px");
			iTeachingLoad.setDecimal(false); iTeachingLoad.setNegative(false);
			if (request != null) iTeachingLoad.setValue(request.getTeachingLoad());
			addRow(MESSAGES.propTeachingLoad(), iTeachingLoad);
			iCoordinator = new CheckBox();
			if (request != null)
				iCoordinator.setValue(request.isAssignCoordinator());
			iCoordinator.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					// simpleChanged();
					changeResponsibilities();
				}
			});
			iCoordinatorRow = addRow(MESSAGES.propAssignCoordinator(), iCoordinator);
			getRowFormatter().setVisible(iCoordinatorRow, request != null && request instanceof SingleRequest);
			if (!iProperties.getResponsibilities().isEmpty()) {
				iResponsibility = new ListBox();
				iResponsibilityRow = addRow(MESSAGES.propTeachingResponsibility(), iResponsibility);
				changeResponsibilities();
			}
			if (request == null || request instanceof MultiRequest) {
				iSubpart = new ListBox();
				iSubpart.addItem(MESSAGES.teachingRequestNoSubpart(), "-1");
				SelectElement select = iSubpart.getElement().cast();
				for (Config config: iProperties.getOffering().getConfigs())
					for (Subpart subpart: config.getSubparts()) {
						iSubpart.addItem(subpart.getName() + (config.hasInstructionalMethod() ? " (" + config.getInstructionalMethod() + ")" : ""), subpart.getId().toString());
						int indent = config.getIndent(subpart);
						if (indent > 0)
							select.getOptions().getItem(iSubpart.getItemCount() - 1).getStyle().setPadding(16 * indent, Unit.PX);
						if (request != null) {
							for (Clazz clazz: subpart.getClasses()) {
								if (((MultiRequest)request).getClass(clazz.getId()) != null) {
									iSubpart.setSelectedIndex(iSubpart.getItemCount() - 1);
									break;
								}
							}
						}
					}
				iSubpart.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						subpartChanged();
					}
				});
				iSubpartRow = addRow(MESSAGES.propSchedulingSubpart(), iSubpart);
			}
			if (request != null && request instanceof SingleRequest) {
				SingleRequest sr = (SingleRequest)request;
				if (sr.hasInstructors()) {
					P instructors = new P("instructors");
					for (Long id: sr.getInstructorIds()) {
						InstructorInterface instructor = iProperties.getInstructor(id);
						if (instructor != null) {
							P p = new P("instructor"); p.setText(instructor.getFormattedName()); instructors.add(p);
						}
					}
					addRow(MESSAGES.propAssignedInstructor(), instructors);
				}
			}
			iClasses = new UniTimeTable<Clazz>();
			iClasses.getElement().getStyle().setWidth(100, Unit.PCT);
			List<UniTimeTableHeader> classesHeader = new ArrayList<UniTimeTableHeader>();
			classesHeader.add(new UniTimeTableHeader("&nbsp;"));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colClass()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colExternalId()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colEnrollment()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colLimit()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colTime()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colRoom()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colInstructor()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colAssignInstructor()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colPercentShare()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colInstructorLead()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colCanOverlap()));
			classesHeader.add(new UniTimeTableHeader(MESSAGES.colCommonPart()));
			iClasses.addRow(null, classesHeader);
			iClassesRow = addRow(MESSAGES.propClasses(), iClasses);
			if (request == null || request instanceof MultiRequest) {
				iSubparts = new UniTimeTable<Subpart>();
				iSubparts.getElement().getStyle().setWidth(100, Unit.PCT);
				List<UniTimeTableHeader> subpartsHeader = new ArrayList<UniTimeTableHeader>();
				subpartsHeader.add(new UniTimeTableHeader("&nbsp;"));
				subpartsHeader.add(new UniTimeTableHeader(MESSAGES.colInstructionalType()));
				subpartsHeader.add(new UniTimeTableHeader(MESSAGES.colAssignInstructor()));
				subpartsHeader.add(new UniTimeTableHeader(MESSAGES.colPercentShare()));
				subpartsHeader.add(new UniTimeTableHeader(MESSAGES.colInstructorLead()));
				subpartsHeader.add(new UniTimeTableHeader(MESSAGES.colCanOverlap()));
				subpartsHeader.add(new UniTimeTableHeader(MESSAGES.colCommonPart()));
				iSubparts.addRow(null, subpartsHeader);
				iSubpartsLine = addRow(MESSAGES.propIncludeSubparts(), iSubparts);
			}
			iSameCouse = new ListBox();
			for (PreferenceInterface pref: iProperties.getPreferences()) {
				iSameCouse.addItem(pref.getName(), pref.getId().toString());
				if (request == null) {
					if ("R".equals(pref.getCode())) iSameCouse.setSelectedIndex(iSameCouse.getItemCount() - 1);
				} else {
					if (request.getSameCoursePreference() != null) {
						if (pref.getId().equals(request.getSameCoursePreference()))
							iSameCouse.setSelectedIndex(iSameCouse.getItemCount() - 1);
					} else {
						if ("0".equals(pref.getCode())) iSameCouse.setSelectedIndex(iSameCouse.getItemCount() - 1);
					}
				}
			}
			addRow(MESSAGES.propSameCoursePreference(), iSameCouse);
			iSameCommon = new ListBox();
			for (PreferenceInterface pref: iProperties.getPreferences()) {
				iSameCommon.addItem(pref.getName(), pref.getId().toString());
				if (request == null) {
					if ("0".equals(pref.getCode())) iSameCommon.setSelectedIndex(iSameCommon.getItemCount() - 1);
				} else {
					if (request.getSameCommonPreference() != null) {
						if (pref.getId().equals(request.getSameCommonPreference()))
							iSameCommon.setSelectedIndex(iSameCommon.getItemCount() - 1);
					} else {
						if ("0".equals(pref.getCode())) iSameCommon.setSelectedIndex(iSameCommon.getItemCount() - 1);
					}
				}
			}
			iSameCommonRow = addRow(MESSAGES.propSameCommonPreference(), iSameCommon);
			
			if (!iProperties.getAttributes().isEmpty()) {
				Set<String> types = new TreeSet<String>();
				for (AttributeInterface a: iProperties.getAttributes()) {
					types.add(a.getType().getLabel());
				}
				iAttributes = new HashMap<String, UniTimeTable<Preference>>();
				for (String type: types) {
					iAttributes.put(type, new UniTimeTable<Preference>());
					if (request != null && request.hasAttributePrefernces()) {
						for (Preference p: request.getAttributePreferences()) {
							AttributeInterface a = iProperties.getAttribute(p.getOwnerId());
							if (a != null && type.equals(a.getType().getLabel()))
								addAttributePreferenceLine(type, p);
						}
					}
					addAttributePreferenceLine(type, null);
					addRow(MESSAGES.propAttributeOfTypePrefs(type), iAttributes.get(type));
				}
			}
			if (!iProperties.getInstructors().isEmpty()) {
				iInstructors = new UniTimeTable<Preference>();
				if (request != null && request.hasInstructorPrefernces()) {
					for(Preference p: request.getInstructorPreferences())
						addInstructorPreferenceLine(p);
				}
				addInstructorPreferenceLine(null);
				addRow(MESSAGES.propInstructorPrefs(), iInstructors);
			}
		}
		
		public void setIndex(int index) {
			iRequestHead.setHeaderTitle(MESSAGES.sectTeachingRequest(index));
		}
		
		protected void addAttributePreferenceLine(final String type, Preference p) {
			final UniTimeTable<Preference> attributes = iAttributes.get(type);
			List<Widget> line = new ArrayList<Widget>();
			final ListBox attribute = new ListBox();
			ToolBox.setMinWidth(attribute.getElement().getStyle(), "350px");
			attribute.addItem(MESSAGES.itemSelect(), "-1");
			for (AttributeInterface a: iProperties.getAttributes()) {
				if (!type.equals(a.getType().getLabel())) continue;
				attribute.addItem(a.getName(), a.getId().toString());
				if (p != null && p.getOwnerId().equals(a.getId()))
					attribute.setSelectedIndex(attribute.getItemCount() - 1);
			}
			line.add(attribute);
			final ListBox preference = new ListBox();
			for (PreferenceInterface x: iProperties.getPreferences()) {
				preference.addItem(x.getName(), x.getId().toString());
				if ((p != null && p.getPreferenceId().equals(x.getId())) || (p == null && x.getCode().equals("0")))
					preference.setSelectedIndex(preference.getItemCount() - 1);
			}
			line.add(preference);
			Image delete = new Image(RESOURCES.delete());
			delete.setTitle(MESSAGES.titleDeleteRow());
			delete.getElement().getStyle().setCursor(Cursor.POINTER);
			delete.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (attributes.getRowCount() > 2) {
						attributes.removeRow(attributes.getCellForEvent(event).getRowIndex());
					} else {
						attribute.setSelectedIndex(0);
						preference.setSelectedIndex(3);
					}
				}
			});
			line.add(delete);
			attributes.addRow(p, line);
			attribute.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					int row = attributes.getRowForWidget(attribute);
					if (attributes.getRowCount() - 1 == row && attribute.getSelectedIndex() > 0) {
						addAttributePreferenceLine(type, null);
					}
				}
			});
		}
		
		protected void addInstructorPreferenceLine(Preference p) {
			if (iProperties.getInstructors().isEmpty()) return;
			List<Widget> line = new ArrayList<Widget>();
			final ListBox instructor = new ListBox();
			ToolBox.setMinWidth(instructor.getElement().getStyle(), "350px");
			instructor.addItem(MESSAGES.itemSelect(), "-1");
			for (InstructorInterface a: iProperties.getInstructors()) {
				instructor.addItem(a.getFormattedName(), a.getId().toString());
				if (p != null && p.getOwnerId().equals(a.getId()))
					instructor.setSelectedIndex(instructor.getItemCount() - 1);
			}
			line.add(instructor);
			final ListBox preference = new ListBox();
			for (PreferenceInterface x: iProperties.getPreferences()) {
				preference.addItem(x.getName(), x.getId().toString());
				if ((p != null && p.getPreferenceId().equals(x.getId())) || (p == null && x.getCode().equals("0")))
					preference.setSelectedIndex(preference.getItemCount() - 1);
			}
			line.add(preference);
			Image delete = new Image(RESOURCES.delete());
			delete.setTitle(MESSAGES.titleDeleteRow());
			delete.getElement().getStyle().setCursor(Cursor.POINTER);
			delete.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iInstructors.getRowCount() > 2) {
						iInstructors.removeRow(iInstructors.getCellForEvent(event).getRowIndex());
					} else {
						instructor.setSelectedIndex(0);
						preference.setSelectedIndex(3);
					}
				}
			});
			line.add(delete);
			iInstructors.addRow(p, line); 
			instructor.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					int row = iInstructors.getRowForWidget(instructor);
					if (iInstructors.getRowCount() - 1 == row && instructor.getSelectedIndex() > 0) {
						addInstructorPreferenceLine(null);
					}
				}
			});
			simpleChanged();
		}
		
		protected void changeResponsibilities() {
			boolean coordinator = iCoordinator.getValue();
			if (iResponsibility != null) {
				Responsibility selected = (iRequest == null ? null : iRequest.getTeachingResponsibility());
				if (iResponsibility.getSelectedIndex() > 0)
					selected = iProperties.getResponsibility(Long.valueOf(iResponsibility.getSelectedValue()));
				iResponsibility.clear();
				iResponsibility.addItem(MESSAGES.noTeachingResponsiblitySelected(), "-1");
				for (Responsibility resp: iProperties.getResponsibilities()) {
					if (coordinator && !resp.isCoordinator()) continue;
					if (!coordinator && !resp.isInstructor()) continue;
					iResponsibility.addItem(resp.getName(), resp.getId().toString());
					if (selected != null && resp.equals(selected))
						iResponsibility.setSelectedIndex(iResponsibility.getItemCount() - 1);
				}
				getRowFormatter().setVisible(iResponsibilityRow, iResponsibility.getItemCount() > 1);
			}
		}
		
		protected void simpleChanged() {
			/*if (iCoordinator.getValue()) {
				if (iSubpart != null) getRowFormatter().setVisible(iSubpartRow, false);
				getRowFormatter().setVisible(iSameCommonRow, false);
				getRowFormatter().setVisible(iClassesRow, false);
				if (iSubparts != null) getRowFormatter().setVisible(iSubpartsLine, false);
			} else*/ 
			if ((iSimple != null && iSimple.getValue()) || (iRequest != null && iRequest instanceof MultiRequest)) {
				getRowFormatter().setVisible(iNbrInstructorsRow, false);
				getRowFormatter().setVisible(iCoordinatorRow, false);
				if (iSubpart != null) getRowFormatter().setVisible(iSubpartRow, true);
				if (iSubparts != null) getRowFormatter().setVisible(iSubpartsLine, true);
				subpartChanged();
			} else {
				getRowFormatter().setVisible(iCoordinatorRow, true);
				getRowFormatter().setVisible(iNbrInstructorsRow, true);
				if (iSubpart != null) getRowFormatter().setVisible(iSubpartRow, false);
				if (iSubparts != null) getRowFormatter().setVisible(iSubpartsLine, false);
				iClasses.clearTable(1);
				for (Config config: iProperties.getOffering().getConfigs()) {
					for (Subpart subpart: config.getSubparts()) {
						if (subpart.getParentId() == null)
							addClasses(subpart, null);
					}
				}
				iClasses.setColumnVisible(8, false);
				iClasses.setColumnVisible(9, true);
				iClasses.setColumnVisible(10, true);
				iClasses.setColumnVisible(11, true);
				iClasses.setColumnVisible(12, true);
				iClasses.setColumnVisible(13, true);
				getRowFormatter().setVisible(iClassesRow, iClasses.getRowCount() > 1);
				boolean hasExternalId = false;
				boolean hasAssignment = false;
				for (Config config: iProperties.getOffering().getConfigs())
					for (Subpart subpart: config.getSubparts())
						for (Clazz clazz: subpart.getClasses()) {
							if (clazz.hasExternalId()) hasExternalId = true;
							if (clazz.hasTime() || clazz.hasRoom()) hasAssignment = true;
						}
				iClasses.setColumnVisible(2, hasExternalId);
				iClasses.setColumnVisible(5, hasAssignment);
				iClasses.setColumnVisible(6, hasAssignment);
				iClasses.setColumnVisible(7, hasAssignment);
			}
		}
		
		protected void addClasses(Subpart subpart, Long parent) {
			for (Clazz clazz: subpart.getClasses()) {
				if (parent != null && !parent.equals(clazz.getParentId())) continue;
				addClass(clazz);
				for (Subpart child: subpart.getConfig().getSubparts()) {
					if (subpart.getId().equals(child.getParentId()))
						addClasses(child, clazz.getId());
				}
			}
		}
		
		protected void addClass(Clazz clazz) {
			List<Widget> line = new ArrayList<Widget>();
			final CheckBox select = new CheckBox();
			int indent = clazz.getSubpart().getConfig().getIndent(clazz.getSubpart());
			// select.getElement().getStyle().setPaddingLeft(16 * indent, Unit.PX);
			line.add(select);
			if (clazz.isCancelled()) {
				select.setValue(false); select.setEnabled(false);
			}
			Label name = new Label(clazz.getName());
			name.getElement().getStyle().setPaddingLeft(16 * indent, Unit.PX);
			line.add(name);
			line.add(new Label(clazz.hasExternalId() ? clazz.getExternalId() : ""));
			line.add(new Label(clazz.getEnrollment() == null ? "" : clazz.getEnrollment().toString()));
			line.add(new Label(clazz.getLimit() == null ? "" : clazz.getLimit().toString()));
			line.add(new HTML(clazz.hasTime() ? clazz.getTime() : SECTMSG.arrangeHours()));
			line.add(new HTML(clazz.hasDate() ? clazz.getDate() : SECTMSG.noDate()));
			line.add(new HTML(clazz.hasRoom() ? clazz.getRoom() : SECTMSG.noRoom()));
			line.add(new Label());
			IncludeLine include = null;
			if (iRequest != null && iRequest instanceof SingleRequest) {
				include = ((SingleRequest)iRequest).getClazz(clazz.getId());
			}
			select.setValue(include != null);
			final CheckBox ass = new CheckBox();
			ass.setValue(include != null ? include.isAssign() : true);
			line.add(ass);
			final NumberBox share = new NumberBox(); share.setDecimal(false); share.setNegative(false); share.setWidth("50px");
			share.setValue(include != null ? include.getShare() : 100);
			line.add(share);
			final CheckBox lead = new CheckBox();
			lead.setValue(include != null ? include.isLead() : true);
			line.add(lead);
			final CheckBox overlap = new CheckBox();
			overlap.setValue(include != null ? include.isCanOverlap() : false);
			line.add(overlap);
			final CheckBox common = new CheckBox();
			common.setValue(include != null ? include.isCommon() : false);
			line.add(common);
			ass.setVisible(select.getValue());
			share.setVisible(select.getValue() && ass.getValue());
			lead.setVisible(select.getValue() && ass.getValue());
			overlap.setVisible(select.getValue());
			common.setVisible(select.getValue());
			select.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					ass.setVisible(event.getValue());
					share.setVisible(select.getValue() && ass.getValue());
					lead.setVisible(select.getValue() && ass.getValue());
					overlap.setVisible(select.getValue());
					common.setVisible(select.getValue());
				}
			});
			ass.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					share.setVisible(select.getValue() && ass.getValue());
					lead.setVisible(select.getValue() && ass.getValue());
				}
			});
			if (clazz.isCancelled())
				for (Widget w: line)
					w.addStyleName("cancelled");
			iClasses.addRow(clazz, line);
		}
		
		protected void subpartChanged() {
			if (iSubpart.getSelectedIndex() == 0) {
				getRowFormatter().setVisible(iSameCommonRow, false);
				getRowFormatter().setVisible(iClassesRow, false);
				getRowFormatter().setVisible(iSubpartsLine, false);
			} else {
				getRowFormatter().setVisible(iSameCommonRow, false);
				getRowFormatter().setVisible(iClassesRow, true);
				getRowFormatter().setVisible(iSubpartsLine, false);
				iClasses.clearTable(1);
				iSubparts.clearTable(1);
				Long subpartId = Long.valueOf(iSubpart.getSelectedValue());
				for (Config config: iProperties.getOffering().getConfigs())
					for (Subpart subpart: config.getSubparts()) {
						if (subpartId.equals(subpart.getId())) {
							boolean hasInstructor = false, hasExternalId = false, hasAssignment = false;
							for (Clazz clazz: subpart.getClasses()) {
								if (clazz.hasExternalId()) hasExternalId = true;
								if (clazz.hasTime() || clazz.hasRoom()) hasAssignment = true;
								List<Widget> line = new ArrayList<Widget>();
								NumberBox ch = new NumberBox();
								ch.setWidth("20px");
								line.add(ch);
								line.add(new Label(clazz.getName()));
								line.add(new Label(clazz.hasExternalId() ? clazz.getExternalId() : ""));
								line.add(new Label(clazz.getEnrollment() == null ? "" : clazz.getEnrollment().toString()));
								line.add(new Label(clazz.getLimit() == null ? "" : clazz.getLimit().toString()));
								line.add(new HTML(clazz.hasTime() ? clazz.getTime() : SECTMSG.arrangeHours()));
								line.add(new HTML(clazz.hasDate() ? clazz.getDate() : SECTMSG.noDate()));
								line.add(new HTML(clazz.hasRoom() ? clazz.getRoom() : SECTMSG.noRoom()));
								RequestedClass rc = null;
								if (iRequest != null && iRequest instanceof MultiRequest)
									rc = ((MultiRequest)iRequest).getClass(clazz.getId());
								if (iRequest == null)
									ch.setValue(clazz.isCancelled() ? 0 : 1);
								else if (rc != null)
									ch.setValue(rc.getNbrInstructors());
								if (rc != null && rc.hasInstructors()) {
									P instructors = new P("instructors");
									for (Long id: rc.getInstructorIds()) {
										InstructorInterface i = iProperties.getInstructor(id);
										if (i != null) {
											P p = new P("instructor"); p.setText(i.getFormattedName()); instructors.add(p);
										}
									}
									hasInstructor = true;
									line.add(instructors);
								} else {
									line.add(new Label());
								}
								if (clazz.isCancelled())
									for (Widget w: line)
										w.addStyleName("cancelled");
								iClasses.addRow(clazz, line);
							}
							iClasses.setColumnVisible(2, hasExternalId);
							iClasses.setColumnVisible(5, hasAssignment);
							iClasses.setColumnVisible(6, hasAssignment);
							iClasses.setColumnVisible(7, hasAssignment);
							iClasses.setColumnVisible(8, hasInstructor);
							iClasses.setColumnVisible(9, false);
							iClasses.setColumnVisible(10, false);
							iClasses.setColumnVisible(11, false);
							iClasses.setColumnVisible(12, false);
							iClasses.setColumnVisible(13, false);
							boolean hasSameCommon = false;
							for (Subpart s: config.getSubparts()) {
								List<Widget> line = new ArrayList<Widget>();
								final CheckBox select = new CheckBox();
								line.add(select);
								IncludeLine include = null;
								if (iRequest != null && iRequest instanceof MultiRequest) {
									include = ((MultiRequest)iRequest).getSubpart(s.getId());
								}
								if (s.equals(subpart)) {
									select.setValue(true); select.setEnabled(false);
								} else {
									select.setValue(include != null);
								}
								String name = s.getName();
								boolean defaultOverlap = false;
								if (s.equals(subpart)) {
								} else if (config.isParent(s, subpart)) {
									if (s.getClasses().size() < subpart.getClasses().size())
										hasSameCommon = true;
									name = MESSAGES.subpartNameParent(s.getName());
								} else if (config.isParent(subpart, s)) {
									int[] minMax = config.countChildClasses(subpart, s);
									if (minMax != null) {
										if (minMax[0] == minMax[1]) {
											if (minMax[0] == 1)
												name = MESSAGES.subpartNameOneChildClass(s.getName());
											else
												name = MESSAGES.subpartNameChildrenClases(s.getName(), minMax[0]);
										} else {
											name = MESSAGES.subpartNameChildrenClasesRange(s.getName(), minMax[0], minMax[1]);
										}
									}
								} else {
									if (s.getClasses().size() == 1) {
										name = MESSAGES.subpartNameNoRelationSingleClass(s.getName());
									} else {
										name = MESSAGES.subpartNameNoRelationClasses(s.getName(), s.getClasses().size());
										defaultOverlap = true;
									}
								}
								line.add(new Label(name));
								final CheckBox ass = new CheckBox();
								ass.setValue(include != null ? include.isAssign() : s.equals(subpart) || config.isParent(subpart, s) ? true : false);
								line.add(ass);
								final NumberBox share = new NumberBox(); share.setDecimal(false); share.setNegative(false); share.setWidth("50px");
								share.setValue(include != null ? include.getShare() : s.equals(subpart) || config.isParent(subpart, s) ? 100 : 0);
								line.add(share);
								final CheckBox lead = new CheckBox();
								lead.setValue(include != null ? include.isLead() : true);
								line.add(lead);
								final CheckBox overlap = new CheckBox();
								overlap.setValue(include != null ? include.isCanOverlap() : defaultOverlap);
								line.add(overlap);
								final CheckBox common = new CheckBox();
								common.setValue(include != null ? include.isCommon() : s.equals(subpart) || config.isParent(subpart, s) ? false : true);
								line.add(common);
								ass.setVisible(select.getValue());
								share.setVisible(select.getValue() && ass.getValue());
								lead.setVisible(select.getValue() && ass.getValue());
								overlap.setVisible(select.getValue());
								common.setVisible(select.getValue());
								select.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
									@Override
									public void onValueChange(ValueChangeEvent<Boolean> event) {
										ass.setVisible(event.getValue());
										share.setVisible(select.getValue() && ass.getValue());
										lead.setVisible(select.getValue() && ass.getValue());
										overlap.setVisible(select.getValue());
										common.setVisible(select.getValue());
									}
								});
								ass.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
									@Override
									public void onValueChange(ValueChangeEvent<Boolean> event) {
										share.setVisible(select.getValue() && ass.getValue());
										lead.setVisible(select.getValue() && ass.getValue());
									}
								});
								iSubparts.addRow(s, line);
							}
							getRowFormatter().setVisible(iSubpartsLine, config.getSubparts().size() > 1);
							getRowFormatter().setVisible(iSameCommonRow, hasSameCommon);
						}
					}
				
			}
		}
		
		public Request getRequest() {
			Request ret = null;
			/*
			if (iCoordinator.getValue()) {
				SingleRequest r = new SingleRequest();
				if (iRequest != null && iRequest instanceof SingleRequest) {
					r.setInstructorIds(((SingleRequest)iRequest).getInstructorIds());
					r.setRequestId(((SingleRequest)iRequest).getRequestId());
				}
				ret = r;
			} else */
			if ((iRequest != null && iRequest instanceof SingleRequest) || (iSimple != null && !iSimple.getValue())) {
				SingleRequest r = new SingleRequest();
				if (iRequest != null && iRequest instanceof SingleRequest) {
					r.setInstructorIds(((SingleRequest)iRequest).getInstructorIds());
					r.setRequestId(((SingleRequest)iRequest).getRequestId());
				}
				r.setNbrInstructors(iNbrInstructors.toInteger() == null ? 1 : iNbrInstructors.toInteger());
				r.setAssignCoordinator(iCoordinator.getValue());
				for (int i = 1; i < iClasses.getRowCount(); i++) {
					Clazz clazz = iClasses.getData(i);
					if (clazz == null) continue;
					CheckBox select = (CheckBox)iClasses.getWidget(i, 0);
					if (!select.getValue()) continue;
					CheckBox assign = (CheckBox)iClasses.getWidget(i, 9);
					NumberBox share = (NumberBox)iClasses.getWidget(i, 10);
					CheckBox lead = (CheckBox)iClasses.getWidget(i, 11);
					CheckBox canOverlap = (CheckBox)iClasses.getWidget(i, 12);
					CheckBox common = (CheckBox)iClasses.getWidget(i, 13);
					IncludeLine include = new IncludeLine();
					include.setAssign(assign.getValue());
					include.setCanOverlap(canOverlap.getValue());
					Integer shr = share.toInteger();
					include.setShare(shr == null ? 0 : shr.intValue());
					include.setOwnerId(clazz.getId());
					include.setLead(lead.getValue());
					include.setCommon(common.getValue());
					r.addClass(include);
				}
				if (r.getClasses().isEmpty() && !r.isAssignCoordinator()) return null;
				ret = r;
			} else {
				if (iSubpart.getSelectedIndex() == 0) return null;
				MultiRequest r = new MultiRequest();
				r.setAssignCoordinator(false);
				for (int i = 1; i < iClasses.getRowCount(); i++) {
					Clazz clazz = iClasses.getData(i);
					if (clazz == null) continue;
					NumberBox select = (NumberBox)iClasses.getWidget(i, 0);
					if (select.toInteger() == null || select.toInteger() <= 0) continue;
					RequestedClass rc = new RequestedClass();
					rc.setClassId(clazz.getId());
					rc.setNbrInstructors(select.toInteger());
					if (iRequest != null && iRequest instanceof MultiRequest) {
						RequestedClass old = ((MultiRequest)iRequest).getClass(clazz.getId());
						if (old != null) {
							rc.setInstructorIds(old.getInstructorIds());
							rc.setRequestId(old.getRequestId());
						}
					}
					r.addClass(rc);
				}
				for (int i = 1; i < iSubparts.getRowCount(); i++) {
					Subpart subpart = iSubparts.getData(i);
					if (subpart == null) continue;
					CheckBox select = (CheckBox)iSubparts.getWidget(i, 0);
					if (!select.getValue()) continue;
					CheckBox assign = (CheckBox)iSubparts.getWidget(i, 2);
					NumberBox share = (NumberBox)iSubparts.getWidget(i, 3);
					CheckBox lead = (CheckBox)iSubparts.getWidget(i, 4);
					CheckBox canOverlap = (CheckBox)iSubparts.getWidget(i, 5);
					CheckBox common = (CheckBox)iSubparts.getWidget(i, 6);
					IncludeLine include = new IncludeLine();
					include.setAssign(assign.getValue());
					include.setCanOverlap(canOverlap.getValue());
					Integer shr = share.toInteger();
					include.setShare(shr == null ? 0 : shr.intValue());
					include.setOwnerId(subpart.getId());
					include.setLead(lead.getValue());
					include.setCommon(common.getValue());
					r.addSubpart(include);
				}
				if (r.getClasses().isEmpty() || r.getSubparts().isEmpty()) return null;
				ret = r;
			}
			Double tl = iTeachingLoad.toDouble();
			ret.setTeachingLoad(tl == null ? 0f : tl.floatValue());
			if (iResponsibility != null && iResponsibility.getSelectedIndex() > 0)
				ret.setTeachingResponsibility(iProperties.getResponsibility(Long.valueOf(iResponsibility.getSelectedValue())));
			ret.setSameCoursePreference(Long.valueOf(iSameCouse.getSelectedValue()));
			ret.setSameCommonPreference(Long.valueOf(iSameCommon.getSelectedValue()));
			if (iAttributes != null)
				for (UniTimeTable<Preference> attributes: iAttributes.values()) {
					for (int i = 0; i < attributes.getRowCount(); i++) {
						ListBox attribute = (ListBox)attributes.getWidget(i, 0);
						ListBox preference = (ListBox)attributes.getWidget(i, 1);
						if (attribute.getSelectedIndex() <= 0) continue;
						Preference p = new Preference();
						p.setPreferenceId(Long.valueOf(preference.getSelectedValue()));
						p.setOwnerId(Long.valueOf(attribute.getSelectedValue()));
						ret.addAttributePreference(p);
					}
				}
			if (iInstructors != null)
				for (int i = 0; i < iInstructors.getRowCount(); i++) {
					ListBox instructor = (ListBox)iInstructors.getWidget(i, 0);
					ListBox preference = (ListBox)iInstructors.getWidget(i, 1);
					if (instructor.getSelectedIndex() <= 0) continue;
					Preference p = new Preference();
					p.setPreferenceId(Long.valueOf(preference.getSelectedValue()));
					p.setOwnerId(Long.valueOf(instructor.getSelectedValue()));
					ret.addInstructorPreference(p);
				}
			return ret;
		}
	}
}
