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
import org.unitime.timetable.gwt.client.events.SessionDatesSelector;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.ClassEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.ClassEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.ClassInstr;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.InheritInstructorPrefs;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SearchableListBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

public class ClassEditPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ClassEditResponse iData;
	private UniTimeTable<ClassInstr> iInstructors;
	
	private ListBox iDatePattern;
	
	private PreferenceEditWidget iPreferences;
		
	public ClassEditPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ClassEditPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("cid");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoClassId());
		} else {
			load(Long.valueOf(id), Operation.GET, true, null);
		}
		
		iHeader.addButton("update", COURSE.actionUpdatePreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (iInstructors != null)
					iData.setClassInstructors(iInstructors.getData());
				if (validate()) {
					load(iData.getId(), Operation.UPDATE, true, null);
				}
			}
		});
		iHeader.getButton("update").setTitle(COURSE.titleUpdatePreferences(COURSE.accessUpdatePreferences()));
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdatePreferences().charAt(0));
		iHeader.setEnabled("update", false);

		iHeader.addButton("clear", COURSE.actionClearClassPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (iInstructors != null)
					iData.setClassInstructors(iInstructors.getData());
				load(iData.getId(), Operation.CLEAR_CLASS_PREFS, true, null);
			}
		});
		iHeader.getButton("clear").setTitle(COURSE.titleClearClassPreferences(COURSE.accessClearClassPreferences()));
		iHeader.getButton("clear").setAccessKey(COURSE.accessClearClassPreferences().charAt(0));
		iHeader.setEnabled("clear", false);
		
		iHeader.addButton("previous", COURSE.actionPreviousClass(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (iInstructors != null)
					iData.setClassInstructors(iInstructors.getData());
				if (validate()) {
					load(iData.getId(), Operation.PREVIOUS, true, null);
				}
			}
		});
		iHeader.getButton("previous").setTitle(COURSE.titlePreviousClassWithUpdate(COURSE.accessPreviousClass()));
		iHeader.getButton("previous").setAccessKey(COURSE.accessPreviousClass().charAt(0));
		iHeader.setEnabled("previous", false);
		
		iHeader.addButton("next", COURSE.actionNextClass(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (iInstructors != null)
					iData.setClassInstructors(iInstructors.getData());
				if (validate()) {
					load(iData.getId(), Operation.NEXT, true, null);
				}
			}
		});
		iHeader.getButton("next").setTitle(COURSE.titleNextClassWithUpdate(COURSE.accessNextClass()));
		iHeader.getButton("next").setAccessKey(COURSE.accessNextClass().charAt(0));
		iHeader.setEnabled("next", false);
		
		iHeader.addButton("back", COURSE.actionBackToDetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "clazz?id=" + iData.getId());
			}
		});
		iHeader.getButton("back").setTitle(COURSE.titleBackToDetail(COURSE.accessBackToDetail()));
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToDetail().charAt(0));
		
		iFooter = iHeader.clonePanel();
	}
	
	protected void load(Long classId, final Operation op, final boolean showLoading, final Command command) {
		if (showLoading) LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		ClassEditRequest req = new ClassEditRequest();
		req.setOperation(op);
		if (op != null && iData != null) {
			iPreferences.update();		
			if (iInstructors != null)
				iData.setClassInstructors(iInstructors.getData());
			req.setPayLoad(iData);
		}
		req.setId(classId);
		RPC.execute(req, new AsyncCallback<ClassEditResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				if (showLoading) LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ClassEditResponse response) {
				iData = response;
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				if (showLoading) LoadingWidget.getInstance().hide();
				
				if (op == Operation.DATE_PATTERN || op == Operation.INSTRUCTORS) {
					iPreferences.setValue(response);
					return;
				}
				
				iPanel.clear();
				iHeader.setHeaderTitle(response.getName());
				iPanel.addHeaderRow(iHeader);
				for (PropertyInterface property: response.getProperties().getProperties())
					iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				
				if (response.hasDatePatterms()) {
					iDatePattern = new ListBox();
					for (IdLabel dp: response.getDatePatterns()) {
						iDatePattern.addItem(dp.getLabel(), dp.getId().toString());
						if (dp.getId().equals(response.getDatePatternId()))
							iDatePattern.setSelectedIndex(iDatePattern.getItemCount() - 1);
					}
					final P datePatternPanel = new P("date-pattern");
					if (response.isSearchableDatePattern()) {
						datePatternPanel.add(new SearchableListBox(iDatePattern));
					} else {
						datePatternPanel.add(iDatePattern);
					}
					final ImageButton cal = new ImageButton(RESOURCES.datepattern());
					iDatePattern.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							Long id = Long.valueOf(iDatePattern.getSelectedValue());
							iData.setDatePatternId(id);
							IdLabel dp = response.getDatePattern(id);
							cal.setVisible(dp != null && dp.getDescription() != null);
							load(classId, Operation.DATE_PATTERN, false, null);
						}
					});
					cal.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Long id = Long.valueOf(iDatePattern.getSelectedValue());
							IdLabel dp = response.getDatePattern(id);
							if (dp != null && dp.getDescription() != null) {
								final UniTimeDialogBox box = new UniTimeDialogBox(true, true);
								SessionDatesSelector w = new SessionDatesSelector().forDatePattern(response.getDatePattern(id).getDescription(),
										new Command() {
									@Override
									public void execute() {
										box.center();
									}
								});
								w.getElement().getStyle().setProperty("width", "80vw");
								box.setWidget(w);
								box.setText(COURSE.sectPreviewOfDatePattern(iDatePattern.getSelectedItemText()));
							}
						}
					});
					datePatternPanel.add(cal);
					iPanel.addRow(COURSE.propertyDatePattern(), datePatternPanel);
				}
				
				CheckBox displayInstructor = new CheckBox();
				displayInstructor.setValue(response.isDisplayInstructors());
				iPanel.addRow(COURSE.propertyDisplayInstructors(), displayInstructor);
				displayInstructor.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iData.setDisplayInstructors(event.getValue());
					}
				});
				
				CheckBox studentScheduling = new CheckBox();
				studentScheduling.setValue(response.isStudentScheduling());
				iPanel.addRow(COURSE.propertyEnabledForStudentScheduling(), studentScheduling);
				studentScheduling.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iData.setStudentScheduling(event.getValue());
					}
				});
				
				TextArea scheduleNote = new TextArea();
				scheduleNote.setHeight("66px");
				scheduleNote.setWidth("100%");
				if (response.hasScheduleNote()) scheduleNote.setText(response.getScheduleNote());
				iPanel.addRow(COURSE.propertyStudentScheduleNote(), scheduleNote);
				scheduleNote.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iData.setScheduleNote(event.getValue());
					}
				});
				
				if (response.hasTimetable()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getTimetable().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getTimetable()));
				}
				
				
				UniTimeHeaderPanel notesPanel = new UniTimeHeaderPanel(COURSE.sectionTitleNotesToScheduleManager());
				iPanel.addHeaderRow(notesPanel);
				TextArea reqestNotes = new TextArea();
				reqestNotes.setHeight("66px");
				reqestNotes.setWidth("100%");
				if (response.hasRequestNote()) reqestNotes.setText(response.getRequestNote());
				iPanel.addRow(reqestNotes);
				reqestNotes.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iData.setRequestNote(event.getValue());
					}
				});
				
				iInstructors = new UniTimeTable<ClassInstr>();
				if (response.hasInstructors()) {
					UniTimeHeaderPanel instrPanel = new UniTimeHeaderPanel(COURSE.sectionTitleInstructors());
					iPanel.addHeaderRow(instrPanel);
					iPanel.addRow(iInstructors);
					instrPanel.addButton("add-instructor", COURSE.actionAddInstructor(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent evt) {
							ClassInstr ci = new ClassInstr();
							int percent = 0;
							for (int r = 1; r < iInstructors.getRowCount(); r++)
								percent += iInstructors.getData(r).getPercentShare();
							ci.setPercentShare(Math.max(0, 100 - percent));
							iInstructors.addRow(ci, toLine(ci));
						}
					});
					List<UniTimeTableHeader> instrHeader = new ArrayList<UniTimeTableHeader>();
					instrHeader.add(new UniTimeTableHeader(COURSE.columnInstructorName()));
					if (response.hasResponsibilities())
						instrHeader.add(new UniTimeTableHeader(COURSE.columnTeachingResponsibility()));
					UniTimeTableHeader hShare = new UniTimeTableHeader(COURSE.columnInstructorShare());
					hShare.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					instrHeader.add(hShare);
					UniTimeTableHeader hConf = new UniTimeTableHeader(COURSE.columnInstructorCheckConflicts());
					hConf.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
					instrHeader.add(hConf);
					instrHeader.add(new UniTimeTableHeader(""));
					iInstructors.addRow(null, instrHeader);
					if (response.hasClassInstructors())
						for (ClassInstr ci: response.getClassInstructors())
							iInstructors.addRow(ci, toLine(ci));
					if (iInstructors.getRowCount() <= 1) {
						ClassInstr ci = new ClassInstr(); ci.setPercentShare(100);
						iInstructors.addRow(ci, toLine(ci));
					}
				}
				
				iPreferences = new PreferenceEditWidget();
				iPreferences.setValue(response);
				iPanel.addRow(iPreferences);
				
				iPanel.addBottomRow(iFooter);
				
				UniTimeNavigation.getInstance().refresh();
				
				iHeader.setEnabled("update", true);
				iHeader.setEnabled("previous", response.getPreviousId() != null);
				iHeader.setEnabled("next", response.getNextId() != null);
				iHeader.setEnabled("clear", response.canClearPrefs());
				
				if (command != null)
					command.execute();
			}
		});
	}
	
	protected void instructorsChanged(Long instructorId, Long oldId, final Focusable widget) {
		if (iData.getInheritInstructorPrefs() == InheritInstructorPrefs.NEVER) return;
		boolean hasPrefs = false;
		if (instructorId != null) {
			IdLabel instructor = iData.getInstructor(instructorId);
			if (instructor != null && "1".equals(instructor.getDescription())) hasPrefs = true;
		}
		if (oldId != null) {
			IdLabel instructor = iData.getInstructor(oldId);
			if (instructor != null && "1".equals(instructor.getDescription())) hasPrefs = true;
		}
		if (!hasPrefs) return;
		if (iData.getInheritInstructorPrefs() == InheritInstructorPrefs.ALWAYS) {
			load(iData.getId(), Operation.INSTRUCTORS, false, null);
		} else if (iData.getInheritInstructorPrefs() == InheritInstructorPrefs.ASK) {
			UniTimeConfirmationDialog.confirm(instructorId == null ? COURSE.confirmRemoveInstructorPreferencesFromClass() : COURSE.confirmApplyInstructorPreferencesToClass(), new Command() {
				@Override
				public void execute() {
					load(iData.getId(), Operation.INSTRUCTORS, false, new Command() {
						@Override
						public void execute() {
							if (widget != null) widget.setFocus(true);
						}
					});
				}
			});
		}
	}
	
	protected List<Widget> toLine(final ClassInstr ci) {
		List<Widget> ret = new ArrayList<Widget>();
		final ListBox instructor = new ListBox();
		final CheckBox conflict = new CoflictBox();
		instructor.addItem("-", "");
		for (IdLabel i: iData.getInstructors()) {
			instructor.addItem(i.getLabel(), i.getId().toString());
			if (i.getId().equals(ci.getInstructorId()))
				instructor.setSelectedIndex(instructor.getItemCount() - 1);
		}
		instructor.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent ce) {
				String id = instructor.getSelectedValue();
				Long oldId = ci.getInstructorId();
				if (id.isEmpty()) {
					ci.setInstructorId(null);
					if (oldId != null && conflict.getValue()) instructorsChanged(null, oldId, instructor);
					conflict.setValue(false, false); ci.setCheckConflicts(false);
					conflict.setEnabled(false);
				} else {
					ci.setInstructorId(Long.valueOf(id));
					conflict.setEnabled(true);
					if (oldId == null) {
						conflict.setValue(true, false); ci.setCheckConflicts(true);
					}
					if (!ci.getInstructorId().equals(oldId) && conflict.getValue())
						instructorsChanged(ci.getInstructorId(), oldId, instructor);
				}
			}
		});
		ret.add(instructor);
		if (iData.hasResponsibilities()) {
			final ListBox responsibility = new ListBox();
			if (iData.getDefaultResponsibilityId() == null)
				responsibility.addItem("-", "");
			for (IdLabel i: iData.getResponsibilities()) {
				responsibility.addItem(i.getLabel(), i.getId().toString());
				if (i.getId().equals(ci.getResponsibilityId()) || (ci.getResponsibilityId() == null && i.getId().equals(iData.getDefaultResponsibilityId())))
					responsibility.setSelectedIndex(responsibility.getItemCount() - 1);
			}
			ret.add(responsibility);
			responsibility.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent ce) {
					String id = responsibility.getSelectedValue();
					if (id.isEmpty())
						ci.setResponsibilityId(null);
					else
						ci.setResponsibilityId(Long.valueOf(id));
				}
			});
		}
		NumberBox percent = new PercentBox();
		percent.setValue(ci.getPercentShare());
		percent.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				try {
					ci.setPercentShare(Integer.valueOf(event.getValue()));
				} catch (Exception e) {
					ci.setPercentShare(0);
				}
			}
		});
		ret.add(percent);
		conflict.setValue(ci.getInstructorId() != null && ci.isCheckConflicts());
		conflict.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ci.setCheckConflicts(event.getValue());
				instructorsChanged(ci.getInstructorId(), null, conflict);
			}
		});
		ret.add(conflict);
		conflict.setEnabled(ci.getInstructorId() != null);
		final ImageButton delete = new ImageButton(RESOURCES.delete());
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int row = iInstructors.getRowForWidget(delete);
				if (row >= 0) iInstructors.removeRow(row);
				if (conflict.getValue())
					instructorsChanged(null, ci.getInstructorId(), null);
			}
		});
		ret.add(delete);
		return ret;
	}
	
	private static class PercentBox extends NumberBox implements HasCellAlignment {
		public PercentBox() {
			setDecimal(false);
			setNegative(false);
		}
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	private static class CoflictBox extends CheckBox implements HasCellAlignment {
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
	
	public boolean validate() {
		iHeader.clearMessage();
		if (iData.hasRequestNote() && iData.getRequestNote().length() > 999) {
			iHeader.setErrorMessage(COURSE.errorNotesLongerThan999());
			return false;
		}
		if (iData.hasScheduleNote() && iData.getScheduleNote().length() > 1999) {
			iHeader.setErrorMessage(COURSE.errorSchedulePrintNoteLongerThan1999());
			return false;
		}
		
		if (iInstructors != null) {
			Set<ClassInstr> instructors = new HashSet<ClassInstr>();
			for (ClassInstr ci: iInstructors.getData()) {
				if (ci.getInstructorId() == null) continue;
				if (!instructors.add(ci)) {
					iHeader.setErrorMessage(COURSE.errorInvalidInstructors());
					return false;
				}
			}
		}
		String error = iPreferences.validate();
		if (error != null) {
			iHeader.setErrorMessage(error);
			return false;
		}
		return true;
	}
}
