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
package org.unitime.timetable.gwt.client.exams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamEditRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamEditResponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamLookupClasses;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamLookupCourses;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamLookupSubparts;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamObjectInterface;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.offerings.PreferenceEditWidget;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;

public class ExamEditPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ExamEditResponse iData;
	private UniTimeTable<IdLabel> iInstructors;
	private UniTimeTable<ExamObjectInterface> iOwners;
	
	private PreferenceEditWidget iPreferences;
		
	public ExamEditPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ExamEditPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("examId");
		boolean clone = ("true".equalsIgnoreCase(Window.Location.getParameter("clone")));
		
		load(id != null && !id.isEmpty() ? Long.valueOf(id) : null, clone ? Operation.CLONE_EXAM : Operation.GET, true, null);
		
		iHeader.addButton("save", EXAM.actionExamSave(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				update();
				if (validate()) {
					load(iData.getId(), Operation.UPDATE, true, null);
				}
			}
		});
		iHeader.getButton("save").setTitle(EXAM.titleExamSave());
		iHeader.getButton("save").setAccessKey(EXAM.actionExamSave().charAt(0));
		iHeader.setEnabled("save", false);
		
		iHeader.addButton("update", EXAM.actionExamUpdate(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				update();
				if (validate()) {
					load(iData.getId(), Operation.UPDATE, true, null);
				}
			}
		});
		iHeader.getButton("update").setTitle(EXAM.titleExamUpdate());
		iHeader.getButton("update").setAccessKey(EXAM.actionExamUpdate().charAt(0));
		iHeader.setEnabled("update", false);

		iHeader.addButton("clear", EXAM.actionClearExamPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				update();
				load(iData.getId(), Operation.CLEAR_EXAM_PREFS, true, null);
			}
		});
		iHeader.getButton("clear").setTitle(EXAM.titleClearExamPreferences());
		iHeader.getButton("clear").setAccessKey(EXAM.accessClearExamPreferences().charAt(0));
		iHeader.setEnabled("clear", false);
		
		iHeader.addButton("previous", EXAM.actionExamPrevious(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				update();
				if (validate()) {
					load(iData.getId(), Operation.PREVIOUS, true, null);
				}
			}
		});
		iHeader.getButton("previous").setTitle(EXAM.titleExamPrevious());
		iHeader.getButton("previous").setAccessKey(EXAM.accessExamPrevious().charAt(0));
		iHeader.setEnabled("previous", false);
		
		iHeader.addButton("next", EXAM.actionExamNext(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				update();
				if (validate()) {
					load(iData.getId(), Operation.NEXT, true, null);
				}
			}
		});
		iHeader.getButton("next").setTitle(EXAM.titleExamNext());
		iHeader.getButton("next").setAccessKey(EXAM.accessExamNext().charAt(0));
		iHeader.setEnabled("next", false);
		
		iHeader.addButton("back", EXAM.actionBatckToDetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iData.hasBackUrl()) 
					ToolBox.open(GWT.getHostPageBaseURL() + iData.getBackUrl());
				else if (iData.getId() != null)
					ToolBox.open(GWT.getHostPageBaseURL() + "examination?id=" + iData.getId());
				else
					ToolBox.open(GWT.getHostPageBaseURL() + "examinations");
			}
		});
		iHeader.getButton("back").setTitle(EXAM.titleExamBack().replace("%%", id == null || id.isEmpty() ? MESSAGES.pageExaminations() : MESSAGES.pageExaminationDetail()));
		iHeader.getButton("back").setAccessKey(EXAM.accessExamBack().charAt(0));
		
		iFooter = iHeader.clonePanel();
	}
	
	protected void update() {
		if (iPreferences != null)
			iPreferences.update();
		if (iInstructors != null)
			iData.setExamInstructors(iInstructors.getData());
		if (iData.hasExamObjects()) iData.getExamObjects().clear();
		if (iOwners != null)
			for (ExamObjectInterface doi: iOwners.getData())
				iData.addExamObject(doi);
	}
	
	protected void load(Long examId, final Operation op, final boolean showLoading, final Command command) {
		if (showLoading) LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		ExamEditRequest req = new ExamEditRequest();
		req.setOperation(op);
		if (op != null && iData != null) {
			update();
			req.setPayLoad(iData);
			req.setId(iData.getId());
		}
		if (iData == null) {
			req.setId(examId);
			req.setFirstType(Window.Location.getParameter("firstType"));
			req.setFirstId(Window.Location.getParameter("firstId"));
		}
		RPC.execute(req, new AsyncCallback<ExamEditResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				if (showLoading) LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ExamEditResponse response) {
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				if (showLoading) LoadingWidget.getInstance().hide();
				
				if (op == Operation.EXAM_OWNERS) {
					iData.setExamInstructors(response.getExamInstructors());
					iData.setInstructors(response.getInstructors());
					iInstructors.clearTable();
					IdLabel last = null;
					if (iData.hasInstructors()) {
						if (iData.hasExamInstructors()) {
							for (IdLabel instructor: iData.getExamInstructors()) {
								iInstructors.addRow(instructor, toLine(instructor));
								last = instructor;
							}
						}
					}
					if (last == null || last.getId() != null) {
						IdLabel blank = new IdLabel();
						iInstructors.addRow(blank, toLine(blank));
					}
					return;
				}
				
				iData = response;
				
				iPanel.clear();
				iHeader.setHeaderTitle(response.getLabel() == null ? "" : response.getLabel());
				iPanel.addHeaderRow(iHeader);
				if (response.hasProperties())
					for (PropertyInterface property: response.getProperties().getProperties())
						iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				
				TextBox name = new TextBox();
				if (response.getName() != null)
					name.setText(response.getName());
				name.setMaxLength(100); name.setWidth("400px");
				iPanel.addRow(EXAM.propExamName(), name);
				name.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> e) {
						iData.setName(e.getValue());
					}
				});
				
				if (iData.hasExamTypes()) {
					final ListBox examType = new ListBox();
					for (IdLabel id: iData.getExamTypes()) {
						examType.addItem(id.getLabel(), id.getId().toString());
						if (id.getId().equals(iData.getExamTypeId()))
							examType.setSelectedIndex(examType.getItemCount() - 1);
					}
					iPanel.addRow(EXAM.propExamType(), examType);
					examType.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent e) {
							iData.setExamType(Long.valueOf(examType.getSelectedValue()));
							load(iData.getId(), Operation.EXAM_TYPE, false, null);
						}
					});
				} else if (iData.getExamType() != null) {
					iPanel.addRow(EXAM.propExamType(), new Label(iData.getExamType().getLabel()));
				}
				
				final NumberBox length = new NumberBox();
				length.setDecimal(false); length.setNegative(false);
				length.setValue(iData.getLength());
				iPanel.addRow(EXAM.propExamLength(), length);
				length.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> e) {
						iData.setLength(length.toInteger());
					}
				});
				
				final ListBox seatingType = new ListBox();
				seatingType.addItem(EXAM.seatingNormal());
				seatingType.addItem(EXAM.seatingExam());
				seatingType.setSelectedIndex(iData.isExamSeating() ? 1 : 0);
				iPanel.addRow(EXAM.propExamSeatingType(), seatingType);
				seatingType.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent e) {
						iData.setExamSeating(seatingType.getSelectedIndex() == 1);
						load(iData.getId(), Operation.EXAM_SEATING, false, null);
					}
				});
				
				final NumberBox maxRooms = new NumberBox();
				maxRooms.setDecimal(false); maxRooms.setNegative(false);
				maxRooms.setValue(iData.getMaxRooms());
				iPanel.addRow(EXAM.propExamMaxRooms(), maxRooms);
				maxRooms.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> e) {
						iData.setMaxRooms(maxRooms.toInteger());
					}
				});
				
				final NumberBox size = new NumberBox();
				size.setDecimal(false); size.setNegative(false);
				size.setValue(iData.getSize());
				P sizeBox = new P("exam-size");
				sizeBox.add(size);
				sizeBox.add(new Label(iData.isSizeUseLimitInsteadOfEnrollment() ? EXAM.noteBlankSizeEnrolledStudents() : EXAM.noteBlankSizeEnrolledStudents()));
				iPanel.addRow(EXAM.propExamSize(), sizeBox);
				size.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> e) {
						iData.setSize(size.toInteger());
					}
				});
				
				final NumberBox printOffset = new NumberBox();
				printOffset.setDecimal(false); printOffset.setNegative(true);
				printOffset.setValue(iData.getPrintOffset());
				P printOffsetBox = new P("print-offset");
				printOffsetBox.add(printOffset);
				printOffsetBox.add(new Label(EXAM.noteExamPrintOffset()));
				iPanel.addRow(EXAM.propExamPrintOffset(), printOffsetBox);
				printOffset.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> e) {
						iData.setPrintOffset(printOffset.toInteger());
					}
				});
				if (iInstructors == null) {
					iInstructors = new UniTimeTable<IdLabel>();
				} else {
					iInstructors.clearTable();
				}
				if (iData.hasInstructors()) {
					IdLabel last = null;
					if (iData.hasExamInstructors()) {
						for (IdLabel instructor: iData.getExamInstructors()) {
							iInstructors.addRow(instructor, toLine(instructor));
							last = instructor;
						}
					}
					if (last == null || last.getId() != null) {
						IdLabel blank = new IdLabel();
						iInstructors.addRow(blank, toLine(blank));
					}
				}
				iPanel.addRow(EXAM.propExamInstructors(), iInstructors);
				
				UniTimeHeaderPanel notesPanel = new UniTimeHeaderPanel(EXAM.sectExamNotes());
				iPanel.addHeaderRow(notesPanel);
				TextArea reqestNotes = new TextArea();
				reqestNotes.setHeight("66px");
				reqestNotes.setWidth("100%");
				if (response.hasNotes()) reqestNotes.setText(response.getNotes());
				iPanel.addRow(reqestNotes);
				reqestNotes.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iData.setNotes(event.getValue());
					}
				});
				
				if (response.hasTimetable()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getTimetable().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getTimetable()));
				}
								
				iOwners = new UniTimeTable<ExamObjectInterface>();
				UniTimeHeaderPanel header = new UniTimeHeaderPanel(EXAM.sectExamOwners());
				iPanel.addHeaderRow(header);
				header.addButton("add", EXAM.actionAddObject(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						ExamObjectInterface doi = new ExamObjectInterface();
						iOwners.addRow(doi, toClassRow(doi));
					}
				});
				iPanel.addRow(iOwners);
				
				if (iData.hasExamObjects()) {
					for (ExamObjectInterface doi: iData.getExamObjects())
						iOwners.addRow(doi, toClassRow(doi));
				}
				if (iOwners.getRowCount() == 0) {
					ExamObjectInterface doi = new ExamObjectInterface();
					iOwners.addRow(doi, toClassRow(doi));
				}
				
				iPreferences = new PreferenceEditWidget();
				iPreferences.setValue(response);
				iPanel.addRow(iPreferences);
				
				iPanel.addBottomRow(iFooter);
				
				UniTimeNavigation.getInstance().refresh();
				
				iHeader.setEnabled("save", response.getId() == null);
				iHeader.setEnabled("update", response.getId() != null);
				iHeader.setEnabled("previous", response.getPreviousId() != null);
				iHeader.setEnabled("next", response.getNextId() != null);
				iHeader.setEnabled("clear", response.canClearPrefs());
				
				if (op == Operation.EXAM_TYPE) {
					length.setFocus(true);
				} else if (op == Operation.EXAM_SEATING) {
					maxRooms.setFocus(true);
				}
				
				if (response.hasBackTitle())
					iHeader.getButton("back").setTitle(EXAM.titleExamBack().replace("%%", response.getBackTitle()));
				
				if (command != null)
					command.execute();
			}
		});
	}
	
	protected List<Widget> toLine(final IdLabel ci) {
		List<Widget> ret = new ArrayList<Widget>();
		final ListBox instructor = new ListBox();
		instructor.addItem("-", "");
		for (IdLabel i: iData.getInstructors()) {
			instructor.addItem(i.getLabel(), i.getId().toString());
			if (i.getId().equals(ci.getId()))
				instructor.setSelectedIndex(instructor.getItemCount() - 1);
		}
		instructor.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				String id = instructor.getSelectedValue();
				if (id.isEmpty()) {
					ci.setId(null);
					ci.setLabel(null);
				} else {
					ci.setId(Long.valueOf(id));
					ci.setLabel(instructor.getSelectedItemText());
					int row = iInstructors.getRowForWidget(instructor);
					if (row == iInstructors.getRowCount() - 1) {
						IdLabel blank = new IdLabel();
						iInstructors.addRow(blank, toLine(blank));
					}
				}
			}
		});
		ret.add(instructor);
		final ImageButton delete = new ImageButton(RESOURCES.delete());
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int row = iInstructors.getRowForWidget(delete);
				if (row >= 0) iInstructors.removeRow(row);
				if (iInstructors.getRowCount() == 0 || iInstructors.getData(iInstructors.getRowCount() - 1).getId() != null) {
					IdLabel blank = new IdLabel();
					iInstructors.addRow(blank, toLine(blank));
				}
			}
		});
		ret.add(delete);
		return ret;
	}
	
	public boolean validate() {
		iHeader.clearMessage();
		if (iData.getLength() == null || iData.getLength() <= 0) {
			iHeader.setErrorMessage(EXAM.errorZeroExamLength());
			return false;
		}
		if (iData.getMaxRooms() != null && iData.getMaxRooms() < 0) {
			iHeader.setErrorMessage(EXAM.errorNegativeMaxNbrRooms());
			return false;
		}
		if (iData.hasNotes() && iData.getNotes().length() > 999) {
			iHeader.setErrorMessage(EXAM.errorNotesLongerThan999());
			return false;
		}
		if (iInstructors != null) {
			Set<Long> instructors = new HashSet<Long>();
			for (IdLabel ci: iInstructors.getData()) {
				UniTimeNotifications.info("HERE " + ci);
				if (ci.getId() == null) continue;
				if (!instructors.add(ci.getId())) {
					iHeader.setErrorMessage(EXAM.errorDuplicateExamInstructors());
					return false;
				}
			}
		}
		int count = 0;
		Set<String> selections = new HashSet<String>();
		for (int row = 0; row < iOwners.getRowCount(); row++) {
			ExamObjectInterface doi = iOwners.getData(row);
			if (doi.isValid()) {
				if (!selections.add(doi.getId())) {
					iHeader.setErrorMessage(EXAM.errorInvalidOwnerSelectionDP());
					return false;
				}
				count ++;
			} else if (row + 1 < iOwners.getRowCount()) {
				iHeader.setErrorMessage(EXAM.errorInvalidOwnerSelectionDP());
				return false;
			}
		}
		if (count <= 0) {
			iHeader.setErrorMessage(EXAM.errorNoExamOwners());
			return false;
		}
		String error = iPreferences.validate();
		if (error != null) {
			iHeader.setErrorMessage(error);
			return false;
		}
		return true;
	}
	
	protected List<Widget> toClassRow(final ExamObjectInterface doi) {
		final List<Widget> row = new ArrayList<Widget>();
		final ListBox subject = new ListBox();
		subject.addItem("-", ""); subject.setWidth("90px");
		row.add(subject);
		final ListBox course = new ListBox();
		course.addItem("-", ""); course.setWidth("470px");
		row.add(course);		
		final ListBox subpart = new ListBox();
		subpart.addItem("-", ""); subpart.setWidth("150px");
		row.add(subpart);
		final ListBox clazz = new ListBox();
		clazz.addItem("-", ""); clazz.setWidth("150px");
		row.add(clazz);
		for (IdLabel s: iData.getSubjects()) {
			subject.addItem(s.getLabel(), s.getId().toString());
			if (s.getId().equals(doi.getSubjectId()))
				subject.setSelectedIndex(subject.getItemCount() - 1);
		}
		if (doi.getSubjectId() != null && subject.getSelectedIndex() <= 0) {
			subject.addItem(doi.getSubject(), doi.getSubjectId().toString());
			subject.setSelectedIndex(subject.getItemCount() - 1);
			subject.setEnabled(false);
			course.setEnabled(false);
			subject.setEnabled(false);
			clazz.setEnabled(false);
		}
		subject.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setSubjectId(subject.getSelectedIndex() <= 0 ? null : Long.valueOf(subject.getSelectedValue()));
				doi.setCourseId(null); doi.setSubpartId(null); doi.setClassId(null);
				subjectChanged(row, doi);
			}
		});
		course.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setCourseId(course.getSelectedIndex() <= 0 ? null : Long.valueOf(course.getSelectedValue()));
				doi.setSubpartId(null); doi.setClassId(null);
				courseChanged(row, doi);
				load(iData.getId(), Operation.EXAM_OWNERS, false, null);
			}
		});
		subpart.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setSubpartId(subpart.getSelectedIndex() <= 0 ? null : Long.valueOf(subpart.getSelectedValue()));
				doi.setClassId(null);
				subpartChanged(row, doi);
			}
		});
		clazz.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setClassId(clazz.getSelectedIndex() <= 0 ? null : Long.valueOf(clazz.getSelectedValue()));
				if (doi.getClassId() != null && iOwners.getRowForWidget(clazz) == iOwners.getRowCount() - 1) {
					ExamObjectInterface doi = new ExamObjectInterface();
					iOwners.addRow(doi, toClassRow(doi));
				}
			}
		});
		if (subject.getSelectedIndex() <= 0 && subject.getItemCount() == 2) {
			subject.setSelectedIndex(1);
			doi.setSubjectId(subject.getSelectedIndex() <= 0 ? null : Long.valueOf(subject.getSelectedValue()));
		}
		if (subject.getSelectedIndex() > 0)
			subjectChanged(row, doi);
		
		ImageButton delete = new ImageButton(RESOURCES.delete());
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				int row = iOwners.getRowForWidget(delete);
				iOwners.removeRow(row);
				load(iData.getId(), Operation.EXAM_OWNERS, false, null);
			}
		});
		row.add(delete);
		
		return row;
	}
	
	protected void subjectChanged(List<Widget> row, ExamObjectInterface doi) {
		final ListBox course = (ListBox) row.get(1);
		final ListBox subpart = (ListBox) row.get(2);
		final ListBox clazz = (ListBox) row.get(3);
		course.clear();
		course.addItem("-", "");
		subpart.clear();
		subpart.addItem("-", "");
		clazz.clear();
		clazz.addItem("-", "");
		if (doi.getSubjectId() != null) {
			ExamLookupCourses req = new ExamLookupCourses();
			req.setSubjectId(doi.getSubjectId());
			RPC.execute(req, new AsyncCallback<GwtRpcResponseList<IdLabel>>() {
				@Override
				public void onFailure(Throwable e) {
					UniTimeNotifications.error(e.getMessage(), e);
				}
				@Override
				public void onSuccess(GwtRpcResponseList<IdLabel> list) {
					course.clear();
					course.addItem("-", "");
					for (IdLabel item: list) {
						course.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(doi.getCourseId())) {
							course.setSelectedIndex(course.getItemCount() - 1);
							courseChanged(row, doi);
						}
					}
					if (course.getSelectedIndex() <= 0 && course.getItemCount() == 2) {
						course.setSelectedIndex(1);
						doi.setCourseId(course.getSelectedIndex() <= 0 ? null : Long.valueOf(course.getSelectedValue()));
						courseChanged(row, doi);
						load(iData.getId(), Operation.EXAM_OWNERS, false, null);
					}
				}
			});
		}
	}
	
	protected void courseChanged(List<Widget> row, ExamObjectInterface doi) {
		final ListBox subpart = (ListBox) row.get(2);
		final ListBox clazz = (ListBox) row.get(3);
		subpart.clear();
		subpart.addItem("-", "");
		clazz.clear();
		clazz.addItem("-", "");
		if (doi.getCourseId() != null) {
			ExamLookupSubparts req = new ExamLookupSubparts();
			req.setCourseId(doi.getCourseId());
			RPC.execute(req, new AsyncCallback<GwtRpcResponseList<IdLabel>>() {
				@Override
				public void onFailure(Throwable e) {
					UniTimeNotifications.error(e.getMessage(), e);
				}
				@Override
				public void onSuccess(GwtRpcResponseList<IdLabel> list) {
					subpart.clear();
					subpart.addItem("-", "");
					for (IdLabel item: list) {
						subpart.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(doi.getSubpartId())) {
							subpart.setSelectedIndex(subpart.getItemCount() - 1);
							subpartChanged(row, doi);
						}
					}
					if (subpart.getSelectedIndex() <= 0) {
						subpart.setSelectedIndex(1);
						doi.setSubpartId(subpart.getSelectedIndex() <= 0 ? null : Long.valueOf(subpart.getSelectedValue()));
						subpartChanged(row, doi);
					}
				}
			});
		}
	}
	
	protected void subpartChanged(List<Widget> row, ExamObjectInterface doi) {
		final ListBox clazz = (ListBox) row.get(3);
		clazz.clear();
		clazz.addItem(EXAM.examOwnerNotApplicable(), "");
		if (doi.getSubpartId() != null) {
			if (doi.getSubpartId() >= 0) {
				ExamLookupClasses req = new ExamLookupClasses();
				req.setCourseId(doi.getCourseId());
				req.setSubpartId(doi.getSubpartId());
				RPC.execute(req, new AsyncCallback<GwtRpcResponseList<IdLabel>>() {
					@Override
					public void onFailure(Throwable e) {
						UniTimeNotifications.error(e.getMessage(), e);
					}
					@Override
					public void onSuccess(GwtRpcResponseList<IdLabel> list) {
						clazz.clear();
						clazz.addItem("-", "");
						for (IdLabel item: list) {
							clazz.addItem(item.getLabel(), item.getId().toString());
							if (item.getId().equals(doi.getClassId()))
								clazz.setSelectedIndex(clazz.getItemCount() - 1);
						}
						if (clazz.getSelectedIndex() <= 0 && clazz.getItemCount() == 2) {
							clazz.setSelectedIndex(1);
							doi.setClassId(clazz.getSelectedIndex() <= 0 ? null : Long.valueOf(clazz.getSelectedValue()));
							if (doi.getClassId() != null && iOwners.getRowForWidget(clazz) == iOwners.getRowCount() - 1) {
								ExamObjectInterface doi = new ExamObjectInterface();
								iOwners.addRow(doi, toClassRow(doi));
							}
						}
					}
				});
			} else {
				if (doi.isValid() && iOwners.getRowForWidget(clazz) == iOwners.getRowCount() - 1) {
					ExamObjectInterface blank = new ExamObjectInterface();
					iOwners.addRow(blank, toClassRow(blank));
				}
			}
		}
	}
}
