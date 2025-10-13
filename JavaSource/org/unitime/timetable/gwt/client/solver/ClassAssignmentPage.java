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

import java.util.ArrayList;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomFilterBox;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.ClassAssignmentPageRequest;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.ClassAssignmentPageResponse;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.DomainItem;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.Operation;
import org.unitime.timetable.gwt.shared.ClassAssignmentPageInterface.RoomOrder;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFilterRpcRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;

public class ClassAssignmentPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ClassAssignmentPageRequest iRequest;
	private CheckBox iKeepConflicts = new CheckBox();
	private CheckBox iAllowRoomConflicts = new CheckBox();
	private CheckBox iShowStudentConflicts = new CheckBox();
	private ListBox iRoomOrder = new ListBox();
	private RoomFilterBox iRoomFilter = null;
	private Long iLastClassId = null;
	
	public ClassAssignmentPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ClassAssignmentPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		if (hasParent())
			iHeader.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent e) {
					closeDialog();
				}
			});
		else
			iHeader.addButton("close", MESSAGES.buttonBack(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent e) {
					ToolBox.open(GWT.getHostPageBaseURL() + "clazz?id=" + iRequest.getSelectedClassId());
				}
			});
		
		iKeepConflicts.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> e) {
				ToolBox.setSessionCookie("ClassAssignment.KeepConflicts", e.getValue() ? "1" : "0");
				load(Operation.UPDATE);
			}
		});
		// if ("1".equals(ToolBox.getSessionCookie("ClassAssignment.KeepConflicts")))
		//	iKeepConflicts.setValue(true);
		
		iShowStudentConflicts.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> e) {
				ToolBox.setSessionCookie("ClassAssignment.ShowStudentConflicts", e.getValue() ? "1" : "0");
				load(Operation.UPDATE);
			}
		});
		if ("1".equals(ToolBox.getSessionCookie("ClassAssignment.ShowStudentConflicts")))
			iShowStudentConflicts.setValue(true);
		
		iFooter = iHeader.clonePanel();
		
		iRoomOrder.addItem(COURSE.sortRoomNameAsc(), RoomOrder.NAME_ASC.name());
		iRoomOrder.addItem(COURSE.sortRoomNameDesc(), RoomOrder.NAME_DESC.name());
		iRoomOrder.addItem(COURSE.sortRoomSizeAsc(), RoomOrder.SIZE_ASC.name());
		iRoomOrder.addItem(COURSE.sortRoomSizeDesc(), RoomOrder.SIZE_DESC.name());
		iRoomOrder.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				iRequest.setRoomOrder(RoomOrder.valueOf(iRoomOrder.getSelectedValue()));
				ToolBox.setSessionCookie("ClassAssignment.RoomOrder", iRoomOrder.getSelectedValue());
			}
		});
		String selectedRoomOrder = ToolBox.getSessionCookie("ClassAssignment.RoomOrder");
		if (selectedRoomOrder == null || selectedRoomOrder.isEmpty())
			selectedRoomOrder = RoomOrder.NAME_ASC.name();
		for (int i = 0; i < iRoomOrder.getItemCount(); i++)
			if (iRoomOrder.getValue(i).equals(selectedRoomOrder)) {
				iRoomOrder.setSelectedIndex(i); break;
			}
		if ("1".equals(ToolBox.getSessionCookie("ClassAssignment.AllowConflicts")))
			iAllowRoomConflicts.setValue(true);
		iAllowRoomConflicts.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> e) {
				ToolBox.setSessionCookie("ClassAssignment.AllowConflicts", e.getValue() ? "1" : "0");
			}
		});
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("classId");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoClassId());
		} else {
			iRequest = new ClassAssignmentPageRequest();
			iRequest.setSelectedClassId(Long.valueOf(id));
			load(Operation.INIT);
		}
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				Operation op = Operation.UPDATE;
				for (String token: event.getValue().split("&")) {
					if (token.indexOf('=') >= 0) {
						String key = token.substring(0, token.indexOf('='));
						String val = token.substring(token.indexOf('=') + 1);
						if ("time".equals(key)) {
							iRequest.getChange(iRequest.getSelectedClassId()).setTime(val);
						} else if ("date".equals(key)) {
							iRequest.getChange(iRequest.getSelectedClassId()).setDate(val);
						} else if ("room".equals(key)) {
							iRequest.getChange(iRequest.getSelectedClassId()).setRoom(val);
						} else if ("class".equals(key) || "id".equals(key)) {
							iRequest.setSelectedClassId(Long.valueOf(val));
						} else if ("delete".equals(key)) {
							iRequest.removeChange(Long.valueOf(val));
							if (!iRequest.hasChange(iRequest.getSelectedClassId()) && iRequest.hasChanges())
								iRequest.setSelectedClassId(iRequest.getChanges().get(0).getClassId());
						} else if ("lock".equals(key)) {
							if (Window.confirm(COURSE.confirmCourseLock())) {
								iRequest.setOfferingId(Long.valueOf(val));
								op = Operation.LOCK;
							}
						}
					}
				}
				load(op);
			}
		});
		
		checkParent();
	}
	
	protected void load(final Operation op) {
		GwtHint.hideHint();
		final Timer timer = new Timer() {
			@Override
			public void run() {
				LoadingWidget.getInstance().show(op == Operation.ASSIGN ? MESSAGES.waitSavingData() : MESSAGES.waitLoadingData());
			}
		};
		timer.schedule(200);
		iRequest.setOperation(op);
		iRequest.setPreviousClassId(iLastClassId);
		iRequest.setKeepConflictingAssignments(iKeepConflicts.getValue());
		iRequest.setRoomAllowConflicts(iAllowRoomConflicts.getValue());
		if (ToolBox.getSessionCookie("ClassAssignment.ShowStudentConflicts") != null)
			iRequest.setShowStudentConflicts("1".equals(ToolBox.getSessionCookie("ClassAssignment.ShowStudentConflicts")));
		if (iRoomFilter != null)
			iRequest.setRoomFilter(iRoomFilter.getElementsRequest());
		else {
			String rf = ToolBox.getSessionCookie("ClassAssignment.RoomFilter");
			if (rf != null && !rf.isEmpty()) {
				RoomFilterRpcRequest filter = new RoomFilterRpcRequest();
				filter.setText(rf);
				iRequest.setRoomFilter(filter);
			}
		}
		RPC.execute(iRequest, new AsyncCallback<ClassAssignmentPageResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				timer.cancel();
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ClassAssignmentPageResponse response) {
				if (response.hasUrl()) {
					if (hasParent()) {
						closeDialog();
						openParent(GWT.getHostPageBaseURL() + response.getUrl());
					} else {
						ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					}
					return;
				}
				timer.cancel();
				if (History.getToken() != null && !History.getToken().isEmpty())
					History.newItem("", false);
				LoadingWidget.getInstance().hide();
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				if (response.isShowStudentConflicts() != iShowStudentConflicts.getValue()) {
					iShowStudentConflicts.setValue(response.isShowStudentConflicts());
					ToolBox.setSessionCookie("ClassAssignment.ShowStudentConflicts", response.isShowStudentConflicts() ? "1" : "0");
				}
				
				iHeader.getHeaderTitlePanel().clear();
				Anchor anchor = new Anchor(COURSE.sectClass(response.getClassName()));
				anchor.setHref("clazz?id=" + response.getSelectedClassId());
				anchor.setTitle(COURSE.titleOpenClassDetail(response.getClassName()));
				anchor.setStyleName("l8");
				anchor.setTarget("_blank");
				iHeader.getHeaderTitlePanel().add(anchor);
				iHeader.clearMessage();
				if (response.hasErrorMessage())
					iHeader.setErrorMessage(response.getErrorMessage());

				if (response.hasProperties())
					for (PropertyInterface property: response.getProperties().getProperties())
						iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				
				iKeepConflicts.setValue(response.isKeepConflictingAssignments());
				if (response.hasAssignments()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getAssignments().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getAssignments()));
					iPanel.addRow(COURSE.toggleDoNotUnassignConflictingClasses(), iKeepConflicts);
					if (response.isCanAssign()) {
						hp.addButton("assign", COURSE.actionClassAssign(), new ClickHandler() {
							@Override
							public void onClick(ClickEvent e) {
								UniTimeConfirmationDialog.confirm(COURSE.confirmClassAssignment(), new Command() {
									@Override
									public void execute() {
										load(Operation.ASSIGN);
									}
								});
							}
						});
					}
				}

				if (response.hasStudentConflicts()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getStudentConflicts().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getStudentConflicts()));
					if (response.isUseRealStudents()) {
						Label a = new Label(COURSE.studentConflictsShowingActualConflicts());
						a.setStyleName("unitime-ClassAssignmentLink");
						a.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent e) {
								iRequest.setUseRealStudents(false);
								load(Operation.UPDATE);
							}
						});
						int r = iPanel.addRow(a);
						iPanel.getRowFormatter().getElement(r).getStyle().setTextAlign(TextAlign.CENTER);
					} else {
						Label a = new Label(COURSE.studentConflictsShowingSolutionConflicts());
						a.setStyleName("unitime-ClassAssignmentLink");
						a.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent e) {
								iRequest.setUseRealStudents(true);
								load(Operation.UPDATE);
							}
						});
						int r = iPanel.addRow(a);
						iPanel.getRowFormatter().getElement(r).getStyle().setTextAlign(TextAlign.CENTER);
					}
				}

				if (response.hasDates() || response.hasDatesErrorMessage()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(COURSE.sectionTitleAvailableDatesForClass(response.getClassName()));
					iPanel.addHeaderRow(hp);
					if (response.hasDates()) {
						Items dates = new Items(response.getDates(), 1, "date");
						iPanel.addRow(dates);
						if (dates.hasSelection())
							iRequest.getChange(response.getSelectedClassId()).setDate(dates.getSelection());
					}
					if (response.hasErrorMessage()) {
						P error = new P("unitime-ErrorMessage");
						error.setText(response.getDatesErrorMessage());
						int r = iPanel.addRow(error);
						iPanel.getRowFormatter().getElement(r).getStyle().setTextAlign(TextAlign.CENTER);
					}
				}
				
				if (response.hasTimes() || response.hasTimesErrorMessage()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(COURSE.sectionTitleAvailableTimesForClass(response.getClassName()));
					iPanel.addHeaderRow(hp);
					if (response.hasTimes()) {
						Items times = new Items(response.getTimes(), 1, "time");
						iPanel.addRow(times);
						if (times.hasSelection())
							iRequest.getChange(response.getSelectedClassId()).setTime(times.getSelection());
					}
					if (response.hasTimesErrorMessage()) {
						P error = new P("unitime-ErrorMessage");
						error.setText(response.getTimesErrorMessage());
						int r = iPanel.addRow(error);
						iPanel.getRowFormatter().getElement(r).getStyle().setTextAlign(TextAlign.CENTER);
					}
					iPanel.addRow(COURSE.propertyShowStudentConflicts(), iShowStudentConflicts);
				}

				if (response.hasRooms() || response.hasRoomsErrorMessage()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(COURSE.sectionTitleAvailableRoomsForClass(response.getClassName()));
					iPanel.addHeaderRow(hp);
					Label selected = null;
					if (response.getNbrRooms() > 1 && Boolean.TRUE.equals(response.isRoomSplitAttendance())) {
						selected = new Label();
						selected.addStyleName("selected-total");
						hp.getHeaderTitlePanel().add(selected);
					}
					if (iRoomFilter == null) {
						AcademicSessionProvider session = new AcademicSessionProvider() {
							@Override
							public Long getAcademicSessionId() {
								return response.getSessionId();
							}
							@Override
							public String getAcademicSessionName() {
								return "Current Session";
							}
							@Override
							public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
							}
							@Override
							public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
							}
							@Override
							public AcademicSessionInfo getAcademicSessionInfo() {
								return null;
							}
						};
						iRoomFilter = new RoomFilterBox(session);
						String f = ToolBox.getSessionCookie("ClassAssignment.RoomFilter");
						if (f != null && !f.isEmpty())
							iRoomFilter.setValue(f);
						iRoomFilter.addChip(new Chip("department", response.getManagingDeptCode()), false);
						if (response.getMinRoomCapacity() != null && (response.getNbrRooms() <= 1 || !Boolean.TRUE.equals(response.isRoomSplitAttendance())))
							iRoomFilter.addChip(new Chip("size", ">=" + response.getMinRoomCapacity()), false);
						iRoomFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
							@Override
							public void onValueChange(ValueChangeEvent<String> e) {
								String ret = "";
								for (Chip chip: iRoomFilter.getChips(null))
									if (!"department".equals(chip.getCommand()) && !"size".equals(chip.getCommand()))
										ret += chip.toString() + " ";
								ret += iRoomFilter.getText();
								ToolBox.setSessionCookie("ClassAssignment.RoomFilter", ret);
							}
						});
					} else {
						if (iLastClassId != null && !iLastClassId.equals(response.getSelectedClassId())) {
							Chip dept = iRoomFilter.getChip("department");
							if (dept != null && !dept.getValue().equals(response.getManagingDeptCode())) {
								iRoomFilter.removeChip(dept, false);
								iRoomFilter.addChip(new Chip("department", response.getManagingDeptCode()), false);
							}
							Chip size = iRoomFilter.getChip("size");
							if (size != null) iRoomFilter.removeChip(size, false);
							if (response.getMinRoomCapacity() != null && (response.getNbrRooms() <= 1 || !Boolean.TRUE.equals(response.isRoomSplitAttendance())))
								iRoomFilter.addChip(new Chip("size", ">=" + response.getMinRoomCapacity()), false);
						}
					}
					iLastClassId = response.getSelectedClassId();
					iPanel.addRow(COURSE.properyRoomFilter(), iRoomFilter);
					hp.addButton("apply", COURSE.buttonApply(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent e) {
							load(Operation.UPDATE);
						}
					});
					iPanel.addRow(COURSE.properyRoomAllowConflicts(), iAllowRoomConflicts);
					iPanel.addRow(COURSE.propertyRoomOrder(), iRoomOrder);
					if (response.hasRooms()) {
						Items rooms = new Items(response.getRooms(), response.getNbrRooms(), "room", selected, response.getMinRoomCapacity());
						iPanel.addRow(rooms);
						if (rooms.hasSelection(response.getNbrRooms()))
							iRequest.getChange(response.getSelectedClassId()).setRoom(rooms.getSelection());
					}
					if (response.hasRoomsErrorMessage()) {
						P error = new P("unitime-ErrorMessage");
						error.setText(response.getRoomsErrorMessage());
						int r = iPanel.addRow(error);
						iPanel.getRowFormatter().getElement(r).getStyle().setTextAlign(TextAlign.CENTER);
					}
				}
				
				iPanel.addBottomRow(iFooter);
			}
		});
	}
	
	class Items extends P {
		int iCount;
		String iKey;
		List<Item> iSelection = new ArrayList<Item>();
		Label iTotal;
		Integer iDesired;
		
		Items(List<DomainItem> items, int count, String key) {
			this(items, count, key, null, null);
		}
		
		Items(List<DomainItem> items, int count, String key, Label total, Integer desired) {
			iKey = key; iCount = count;
			iTotal = total; iDesired = desired;
			setStyleName("domain-items");
			for (DomainItem item: items) {
				final Item w = new Item(item);
				add(w);
				if (w.isSelected()) {
					iSelection.add(w); 
				}
				w.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						// if selected >> deselect
						if (w.isSelected()) {
							w.setSelected(false);
							iSelection.remove(w);
							updateTotal();
							return;
						}
						
						// full selection already >> clear current selection
						if (iSelection.size() >= iCount) {
							for (Item x: iSelection)
								x.setSelected(false);
							iSelection.clear();
						}
						
						// add to selection
						w.setSelected(true);
						iSelection.add(w);
						updateTotal();
						
						// full selection >> update
						if (iSelection.size() == iCount) {
							if ("date".equals(key)) {
								iRequest.getChange(iRequest.getSelectedClassId()).setDate(getSelection());
							}
							if ("time".equals(key)) {
								iRequest.getChange(iRequest.getSelectedClassId()).setTime(getSelection());
								iRequest.getChange(iRequest.getSelectedClassId()).setRoom(null);
								if ("null".equals(getSelection()))
									iRequest.getChange(iRequest.getSelectedClassId()).setDate(null);
							}
							if ("room".equals(key)) {
								iRequest.getChange(iRequest.getSelectedClassId()).setRoom(getSelection());
							}
							load(Operation.UPDATE);
						}	
					}
				});
			}
			updateTotal();
		}
		
		public String getSelection() {
			String id = null;
			for (Item x: iSelection)
				if (id == null)
					id = x.getValue().getId();
				else
					id += ":" + x.getValue().getId();
			return id;
		}
		
		public boolean hasSelection(int count) {
			return iSelection.size() == count;
		}
		
		public boolean hasSelection() {
			return !iSelection.isEmpty();
		}
		
		void updateTotal() {
			if (iTotal == null) return;
			if (iSelection.isEmpty()) {
				iTotal.setText("");
				return;
			}
			int total = 0;
			for (Item s: iSelection)
				total += s.getValue().getValue();
			iTotal.setText("(" + COURSE.messageSelectedSize() + " " + total + (iDesired == null ? "" : " " + COURSE.messageSelectedSizeOf() + " " + iDesired) + ")");
		}
	}

	static class Item extends P {
		DomainItem iItem;
		
		Item(DomainItem item) {
			iItem = item;
			setStyleName("domain-item");
			P first = new P("domain-item-label");
			first.add(new TableWidget.CellWidget(item.getCell()));
			add(first);
			if (iItem.isAssigned())
				first.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
			if (item.getExtra() != null) {
				P extra = new P("domain-item-extra");
				extra.add(new TableWidget.CellWidget(item.getExtra()));
				add(extra);
				if (iItem.isAssigned())
					extra.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
			}
			if (iItem.isSelected())
				getElement().getStyle().setBackgroundColor("#92c1f0");
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent e) {
					getElement().getStyle().setBackgroundColor("#d0e4f6");
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent e) {
					if (iItem.isSelected())
						getElement().getStyle().setBackgroundColor("#92c1f0");
					else
						getElement().getStyle().clearBackgroundColor();
				}
			});
		}
		
		public DomainItem getValue() { return iItem; }
		
		public boolean isSelected() { return iItem.isSelected(); }
		public void setSelected(boolean selected) {
			iItem.setSelected(selected);
			if (!"#92c1f0".equals(getElement().getStyle().getBackgroundColor())) {
				if (iItem.isSelected())
					getElement().getStyle().setBackgroundColor("#92c1f0");
				else
					getElement().getStyle().clearBackgroundColor();
			}
		}
	}
	
	public static native void checkParent() /*-{
		if ($wnd.parent)
			$wnd.parent.hideGwtHint();
	}-*/;
	
	public static native boolean hasParent() /*-{
		return $wnd.parent != null;
	}-*/;

	public static native void closeDialog() /*-{
		$wnd.parent.hideGwtDialog();
	}-*/;
	public native static void openParent(String url) /*-{
		$wnd.parent.location = url;
}-*/;
}
