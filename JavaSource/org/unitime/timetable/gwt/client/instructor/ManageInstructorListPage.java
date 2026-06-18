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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignedStaffInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.ManageInstructorListRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.ManageInstructorListResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.ManageInstructorListUpdateRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.StaffInterface;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ManageInstructorListPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private UniTimeTable<StaffInterface> iInstructors;
	private Map<Long, Position> iPositions = new HashMap<Long, Position>();
	private ManageInstructorListResponse iData;
	private Integer iSort = 3;
	
	public ManageInstructorListPage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-ManageInstructorListPage");
		iHeader = new UniTimeHeaderPanel();
		
		iHeader.addButton("update", MESSAGES.buttonUpdate(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(getUpdateRequest(), new AsyncCallback<GwtRpcResponseNull>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.errorFailedToUpdateInstructors(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.errorFailedToUpdateInstructors(caught.getMessage()), caught);
						ToolBox.checkAccess(caught);
					}

					@Override
					public void onSuccess(GwtRpcResponseNull result) {
						ToolBox.open(GWT.getHostPageBaseURL() + "instructors");
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instructors");
			}
		});
		
		iPanel.addHeaderRow(iHeader);
		initWidget(iPanel);
		
		iInstructors = new UniTimeTable<StaffInterface>();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(MESSAGES.colSelection(), 0, HasHorizontalAlignment.ALIGN_CENTER));
		header.add(new UniTimeTableHeader(MESSAGES.colExternalId()));
		header.add(new UniTimeTableHeader(MESSAGES.colNamePerson()));
		header.add(new UniTimeTableHeader(MESSAGES.colPosition()));
		for (int i = 1; i < header.size(); i++) {
			final int sort = i;
			final UniTimeTableHeader h = header.get(i); 
			h.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					sort(h.getOrder() == null || !h.getOrder() ? sort : -sort);
				}
			});
		}
		iInstructors.addRow(null, header);
		for (int i = 0; i < header.size(); i++)
			iInstructors.getCellFormatter().addStyleName(0, i, "unitime-ClickableTableHeader");
		iInstructors.addMouseClickListener(new MouseClickListener<StaffInterface>() {
			@Override
			public void onMouseClick(TableEvent<StaffInterface> event) {
				if (event.getData() != null) {
					CheckBox box = (CheckBox)iInstructors.getWidget(event.getRow(), 0);
					if (box.isEnabled())
						box.setValue(!box.getValue());
				}
			}
		});
		
		String deptId = Window.Location.getParameter("deptId");
		if (deptId == null || deptId.isEmpty())
			iHeader.setErrorMessage(COURSE.errorRequiredDepartment());
		else {
			iHeader.showLoading();
			RPC.execute(new ManageInstructorListRequest(Long.valueOf(deptId)), new AsyncCallback<ManageInstructorListResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
					UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(ManageInstructorListResponse result) {
					iHeader.clearMessage();
					load(result);
				}
			});
		}
		iFooter = iHeader.clonePanel(""); 
	}
	
	protected Set<Long> getDefaultPositions() {
		HashSet<Long> pos = new HashSet<Long>();
		String cookie = ToolBox.getSessionCookie("ManageInstructorList.Positions");
		if (cookie != null && !cookie.isEmpty())
			for (String id: cookie.split(",")) {
				try {
					pos.add(Long.valueOf(id));
				} catch (Exception e) {}
			}
		return pos;
	}
	
	protected void updateDefaultPositions() {
		String dp = null;
		for (Map.Entry<Long, Position> e: iPositions.entrySet()) {
			if (e.getValue().getValue()) {
				if (dp == null)
					dp = e.getKey().toString();
				else
					dp += "," + e.getKey();
			}
		}
		ToolBox.setSessionCookie("ManageInstructorList.Positions", dp);
	}
	
	protected void load(ManageInstructorListResponse result) {
		iData = result;
		iHeader.setHeaderTitle(result.getDepartmentName());
		if (result.hasPositions()) {
			P positions = new P("positions");
			Set<Long> dp = getDefaultPositions();
			for (IdLabel pos: result.getPositions()) {
				Position p = new Position(pos);
				if (dp.isEmpty())
					p.setValue(!result.getAssigned(p.getId()).isEmpty());
				else
					p.setValue(dp.contains(p.getId()));
				positions.add(p);
				iPositions.put(p.getId(), p);
			}
			iPanel.addRow(COURSE.propertyInstructorPosition(), positions);
			UniTimeTableHeader selection = (UniTimeTableHeader)iInstructors.getWidget(0, 0);
			selection.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int row = 1; row < iInstructors.getRowCount(); row++) {
						CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
						if (box.isEnabled() && !box.getValue())
							box.setValue(true);
					}
				}
				
				@Override
				public boolean isApplicable() {
					for (int row = 1; row < iInstructors.getRowCount(); row++) {
						CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
						if (box.isEnabled() && !box.getValue())
							return true;
					}
					return false;
				}
				
				@Override
				public boolean hasSeparator() {
					return false;
				}
				
				@Override
				public String getName() {
					return MESSAGES.opSelectAll();
				}
			});
			selection.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int row = 1; row < iInstructors.getRowCount(); row++) {
						CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
						if (box.isEnabled() && box.getValue())
							box.setValue(false);
					}
				}
				
				@Override
				public boolean isApplicable() {
					for (int row = 1; row < iInstructors.getRowCount(); row++) {
						CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
						if (box.isEnabled() && box.getValue())
							return true;
					}
					return false;
				}
				
				@Override
				public boolean hasSeparator() {
					return false;
				}
				
				@Override
				public String getName() {
					return MESSAGES.opClearSelection();
				}
			});
			for (final IdLabel pos: result.getPositions()) {
				selection.addOperation(new Operation() {
					@Override
					public void execute() {
						for (int row = 1; row < iInstructors.getRowCount(); row++) {
							CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
							StaffInterface staff = iInstructors.getData(row);
							if (pos.getId().equals(staff.getPositionId()) && box.isEnabled() && !box.getValue())
								box.setValue(true);
						}
					}
					
					@Override
					public boolean isApplicable() {
						if (!iPositions.get(pos.getId()).getValue()) return false;
						for (int row = 1; row < iInstructors.getRowCount(); row++) {
							CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
							StaffInterface staff = iInstructors.getData(row);
							if (pos.getId().equals(staff.getPositionId()) && box.isEnabled() && !box.getValue())
								return true;
						}
						return false;
					}
					
					@Override
					public boolean hasSeparator() {
						return false;
					}
					
					@Override
					public String getName() {
						return MESSAGES.opCheck(pos.getLabel());
					}
				});
				selection.addOperation(new Operation() {
					@Override
					public void execute() {
						for (int row = 1; row < iInstructors.getRowCount(); row++) {
							CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
							StaffInterface staff = iInstructors.getData(row);
							if (pos.getId().equals(staff.getPositionId()) && box.isEnabled() && box.getValue())
								box.setValue(false);
						}
					}
					
					@Override
					public boolean isApplicable() {
						if (!iPositions.get(pos.getId()).getValue()) return false;
						for (int row = 1; row < iInstructors.getRowCount(); row++) {
							CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
							StaffInterface staff = iInstructors.getData(row);
							if (pos.getId().equals(staff.getPositionId()) && box.isEnabled() && box.getValue())
								return true;
						}
						return false;
					}
					
					@Override
					public boolean hasSeparator() {
						return false;
					}
					
					@Override
					public String getName() {
						return MESSAGES.opUncheck(pos.getLabel());
					}
				});
			}
		}
		iPanel.addRow(iInstructors);
		iPanel.addBottomRow(iFooter);
		reloadTable();
	}
	
	protected boolean canSelect(Long positionId) {
		for (int row = 1; row < iInstructors.getRowCount(); row++) {
			CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
			StaffInterface staff = iInstructors.getData(row);
			if (positionId.equals(staff.getPositionId()) && box.isEnabled() && !box.getValue())
				return true;
		}
		return false;
	}
	
	protected boolean canDeselect(Long positionId) {
		for (int row = 1; row < iInstructors.getRowCount(); row++) {
			CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
			StaffInterface staff = iInstructors.getData(row);
			if (positionId.equals(staff.getPositionId()) && box.isEnabled() && box.getValue())
				return true;
		}
		return false;
	}
	
	protected ManageInstructorListUpdateRequest getUpdateRequest() {
		ManageInstructorListUpdateRequest request = new ManageInstructorListUpdateRequest();
		request.setDepartmentId(iData.getDepartmentId());
		for (int row = 1; row < iInstructors.getRowCount(); row++) {
			CheckBox box = (CheckBox)iInstructors.getWidget(row, 0);
			StaffInterface staff = iInstructors.getData(row);
			if (staff instanceof AssignedStaffInterface) {
				AssignedStaffInterface ass = (AssignedStaffInterface)staff;
				if (ass.isCanRemove() && !box.getValue())
					request.addUnassignId(ass.getInstructorId());
			} else {
				if (box.getValue())
					request.addAssignExternalId(staff.getExternalId());
			}
		}
		return request;
	}
	
	protected void reloadTable() {
		ManageInstructorListUpdateRequest req = getUpdateRequest();
		iInstructors.clearTable(1);
		if (iData.hasAssigned()) {
			for (AssignedStaffInterface staff: iData.getAssigned()) {
				if (!iPositions.get(staff.getPositionId()).getValue()) continue;
				List<Widget> line = new ArrayList<Widget>();
				Selection ch = new Selection();
				ch.setValue(!req.hasUnassignId(staff.getInstructorId()));
				ch.setEnabled(staff.isCanRemove());
				ch.setAriaLabel(staff.getExternalId() == null ? staff.getName() : staff.getExternalId() + " - " + staff.getName());
				line.add(ch);
				line.add(new Label(staff.getExternalId() == null ? "" : staff.getExternalId()));
				line.add(new Label(staff.getName()));
				line.add(new Label(iData.getPosition(staff.getPositionId())));
				int row = iInstructors.addRow(staff, line);
				iInstructors.getRowFormatter().addStyleName(row, "instructor-line");
			}
		}
		if (iData.hasAvailable()) {
			for (StaffInterface staff: iData.getAvailable()) {
				if (!iPositions.get(staff.getPositionId()).getValue()) continue;
				List<Widget> line = new ArrayList<Widget>();
				Selection ch = new Selection();
				ch.setValue(req.hasAssignExternalId(staff.getExternalId()));
				ch.setAriaLabel(staff.getExternalId() + " - " + staff.getName());
				line.add(ch);
				line.add(new Label(staff.getExternalId()));
				line.add(new Label(staff.getName()));
				line.add(new Label(iData.getPosition(staff.getPositionId())));
				int row = iInstructors.addRow(staff, line);
				iInstructors.getRowFormatter().addStyleName(row, "staff-line");
			}
		}
		sort(iSort);
	}
	
	protected void sort(int column) {
		iSort = column;
		UniTimeTableHeader h = iInstructors.getHeader(Math.abs(iSort)); 
		iInstructors.sort(h, new Comparator<StaffInterface>() {
			public int compareByPosition(StaffInterface s1, StaffInterface s2) {
				int cmp = iData.getPositionIndex(s1.getPositionId()).compareTo(iData.getPositionIndex(s2.getPositionId()));
				if (cmp != 0) return cmp;
				cmp = NaturalOrderComparator.compare(s1.getName(), s2.getName());
				if (cmp != 0) return cmp;
				return NaturalOrderComparator.compare(s1.getExternalIdNotNull(), s2.getExternalIdNotNull());
			}
			
			public int compareByName(StaffInterface s1, StaffInterface s2) {
				int cmp = NaturalOrderComparator.compare(s1.getName(), s2.getName());
				if (cmp != 0) return cmp;
				return NaturalOrderComparator.compare(s1.getExternalIdNotNull(), s2.getExternalIdNotNull());
			}
			
			public int compareById(StaffInterface s1, StaffInterface s2) {
				int cmp = NaturalOrderComparator.compare(s1.getExternalIdNotNull(), s2.getExternalIdNotNull());
				if (cmp != 0) return cmp;
				return NaturalOrderComparator.compare(s1.getName(), s2.getName());
			}

			@Override
			public int compare(StaffInterface s1, StaffInterface s2) {
				switch (Math.abs(iSort)) {
				case 1:
					return compareById(s1, s2);
				case 2:
					return compareByName(s1, s2);
				case 3:
					return compareByPosition(s1, s2);
				default:
					return compareByPosition(s1, s2);
				}
			}
		}, iSort >= 0);
	}
	
	protected class Position extends CheckBox {
		private IdLabel iPosition;
		
		public Position(IdLabel pos) {
			super(pos.getLabel());
			addStyleName("position");
			iPosition = pos;
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					reloadTable();
					updateDefaultPositions();
				}
			});
		}
		
		Long getId() { return iPosition.getId(); }
	}
	
	protected class Selection extends AriaCheckBox implements HasCellAlignment {
		
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
}
