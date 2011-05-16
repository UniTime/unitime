/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.reservations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.curricula.CurriculaCourseSelectionBox;
import org.unitime.timetable.gwt.client.curricula.CurriculaCourseSelectionBox.CourseSelectionChangeEvent;
import org.unitime.timetable.gwt.client.curricula.CurriculaCourseSelectionBox.CourseSelectionChangeHandler;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.ReservationService;
import org.unitime.timetable.gwt.services.ReservationServiceAsync;
import org.unitime.timetable.gwt.shared.ReservationException;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.Clazz;
import org.unitime.timetable.gwt.shared.ReservationInterface.Config;
import org.unitime.timetable.gwt.shared.ReservationInterface.Course;
import org.unitime.timetable.gwt.shared.ReservationInterface.Area;
import org.unitime.timetable.gwt.shared.ReservationInterface.Curriculum;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.Offering;
import org.unitime.timetable.gwt.shared.ReservationInterface.Subpart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Tomas Muller
 *
 */
public class ReservationEdit extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat("MM/dd/yyyy");

	private List<EditFinishedHandler> iEditFinishedHandlers = new ArrayList<EditFinishedHandler>();
	
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iTitleAndButtons;
	private UniTimeWidget<UniTimeTextBox> iLimit;
	private UniTimeWidget<DateBox> iExpirationDate;
	private Tree iStructure;
	private HashMap<Long, ConfigSelection> iConfigs = new HashMap<Long, ConfigSelection>();
	private HashMap<Long, ClassSelection> iClasses = new HashMap<Long, ClassSelection>();
	private HashMap<Long, Area> iAreas = new HashMap<Long, Area>();
	private HashMap<Long, Curriculum> iCurricula = new HashMap<Long, Curriculum>();
	private ListBox iClassifications, iMajors;
	private UniTimeWidget<ListBox> iType, iArea, iCourse, iGroup, iCurriculum;
	private UniTimeWidget<TextArea> iStudents;
	private ReservationInterface iReservation;
	private CurriculaCourseSelectionBox iCourseBox;
	
	private final ReservationServiceAsync iReservationService = GWT.create(ReservationService.class);

	private static TextArea sLastStudents;
	private static TextBox sLastLimit;
	
	private int iGroupLine, iCourseLine, iAreaLine, iStudentsLine, iCurriculumLine;
	
	private Offering iOffering = null;
	
	public ReservationEdit(boolean standAlone) {
		if (standAlone) {
			addEditFinishedHandler(new EditFinishedHandler() {
				@Override
				public void onSave(EditFinishedEvent evt) {
					ToolBox.open("instructionalOfferingDetail.do?io=" + iOffering.getId() + "#reservations");
				}
				
				@Override
				public void onDelete(EditFinishedEvent evt) {
					ToolBox.open("instructionalOfferingDetail.do?io=" + iOffering.getId() + "#reservations");
				}
				
				@Override
				public void onBack(EditFinishedEvent evt) {
					ToolBox.open("instructionalOfferingDetail.do?io=" + iOffering.getId() + "#reservations");
				}

				@Override
				public void onFailure(Throwable caught) {
					new Timer() {
						@Override
						public void run() {
							ToolBox.open("instructionalOfferingDetail.do?io=" + iOffering.getId() + "#reservations");
						}
					}.schedule(5000);
				}
			});
			
			if (Window.Location.getParameter("id") == null && Window.Location.getParameter("offering") == null) {
				throw new ReservationException("Reservation or instructional offering id not provided.");
			}
		}
		
		iPanel = new SimpleForm();
		initWidget(iPanel);

		iTitleAndButtons = new UniTimeHeaderPanel("Reservation Details");
		iTitleAndButtons.addButton("save", "<u>S</u>ave", 's', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iTitleAndButtons.clearMessage();
				ReservationInterface r = validate();
				if (r == null) {
					iTitleAndButtons.setErrorMessage("Validation failed, plaease check the form for warnings.");
				} else {
					LoadingWidget.getInstance().show("Saving reservation...");
					iReservationService.save(r, new AsyncCallback<Long>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iTitleAndButtons.setErrorMessage(caught.getMessage());
						}
						@Override
						public void onSuccess(Long result) {
							LoadingWidget.getInstance().hide();
							EditFinishedEvent e = new EditFinishedEvent(result);
							for (EditFinishedHandler h: iEditFinishedHandlers)
								h.onSave(e);
						}
					});
				}
			}
		});
		iTitleAndButtons.addButton("delete", "<u>D</u>elete", 'd', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iReservation == null) {
					iTitleAndButtons.setErrorMessage("Cannot delete unsaved reservation.");
				} else {
					LoadingWidget.getInstance().show("Deleting reservation...");
					iReservationService.delete(iReservation.getId(), new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iTitleAndButtons.setErrorMessage(caught.getMessage());
						}
						@Override
						public void onSuccess(Boolean result) {
							LoadingWidget.getInstance().hide();
							EditFinishedEvent e = new EditFinishedEvent(iReservation == null ? null : iReservation.getId());
							for (EditFinishedHandler h: iEditFinishedHandlers)
								h.onDelete(e);
						}
					});
				}
			}
		});
		iTitleAndButtons.setEnabled("delete", false);
		iTitleAndButtons.addButton("back", "<u>B</u>ack", 'b', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EditFinishedEvent e = new EditFinishedEvent(iReservation == null ? null : iReservation.getId());
				for (EditFinishedHandler h: iEditFinishedHandlers)
					h.onBack(e);
			}
		});
		
		iPanel.addHeaderRow(iTitleAndButtons);
		
		iCourseBox = new CurriculaCourseSelectionBox("");
		iCourseBox.setWidth("130px");
		iPanel.addRow("Instructional Offering:", iCourseBox);

		iLimit = new UniTimeWidget<UniTimeTextBox>(new UniTimeTextBox(4, ValueBoxBase.TextAlignment.RIGHT));
		iLimit.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iLimit.clearHint();
			}
		});
		iPanel.addRow("Reserved Space:", iLimit);
		
		iExpirationDate = new UniTimeWidget<DateBox>(new DateBox());
		iExpirationDate.getWidget().getTextBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iExpirationDate.clearHint();
			}
		});
		iPanel.addRow("Expiration Date:", iExpirationDate);
		
		iStructure = new Tree(RESOURCES, true);
		iPanel.addRow("Restrictions:", iStructure);
		iPanel.getCellFormatter().setVerticalAlignment(iPanel.getRowCount() - 1, 0, HasVerticalAlignment.ALIGN_TOP);
		
		iType = new UniTimeWidget<ListBox>(new ListBox());
		iType.getWidget().setStyleName("unitime-TextBox");
		iType.getWidget().addItem("Select...", "");
		iType.getWidget().addItem("Individual Reservation", "individual");
		iType.getWidget().addItem("Student Group Reservation", "group");
		iType.getWidget().addItem("Curriculum Reservation", "curriculum");
		iType.getWidget().addItem("Course Reservation", "course");
		iType.getWidget().setSelectedIndex(0);
		iType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iType.clearHint();
			}
		});
		iPanel.addRow("Type:", iType);
		iType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				typeChanged();
			}
		});

		iStudents = new UniTimeWidget<TextArea>(new TextArea());
		iStudents.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iStudents.clearHint();
				iLimit.getWidget().setText(String.valueOf(iStudents.getWidget().getText().split("\n").length));
			}
		});
		sLastStudents = iStudents.getWidget();
		sLastLimit = iLimit.getWidget();
		iStudents.getWidget().setStyleName("unitime-TextArea");
		iStudents.getWidget().setVisibleLines(10);
		iStudents.getWidget().setCharacterWidth(80);
		VerticalPanel students = new VerticalPanel();
		students.add(iStudents);
		Button lookup = new Button("<u>L</u>ookup");
		lookup.setAccessKey('l');
		lookup.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Lookup.getInstance().center();
			}
		});
		Lookup.getInstance().setOptions("mustHaveExternalId,source=students");
		Lookup.getInstance().setCallback(createLookupCallback());
		students.add(lookup);
		students.setCellHorizontalAlignment(lookup, HasHorizontalAlignment.ALIGN_RIGHT);
		iPanel.addRow("Students:", students);
		iPanel.getCellFormatter().setVerticalAlignment(iPanel.getRowCount() - 1, 0, HasVerticalAlignment.ALIGN_TOP);
		iStudentsLine = iPanel.getRowCount() - 1;


		iGroup = new UniTimeWidget<ListBox>(new ListBox());
		iGroup.getWidget().setStyleName("unitime-TextBox");
		iGroup.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iGroup.clearHint();
			}
		});
		iGroup.getWidget().addItem("Select...", "");
		iGroup.getWidget().setSelectedIndex(0);
		iPanel.addRow("Student Group:", iGroup);
		iGroupLine = iPanel.getRowCount() - 1;

		iCourse = new UniTimeWidget<ListBox>(new ListBox());
		iCourse.getWidget().setStyleName("unitime-TextBox");
		iCourse.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCourse.clearHint();
				String cid = iCourse.getWidget().getValue(iCourse.getWidget().getSelectedIndex());
				for (Course course: iOffering.getCourses()) {
					if (course.getId().toString().equals(cid))
						iLimit.getWidget().setText(course.getLimit() == null ? "" : course.getLimit().toString());
				}
			}
		});
		iPanel.addRow("Course:", iCourse);
		iCourseLine = iPanel.getRowCount() - 1;
		
		iCurriculum = new UniTimeWidget<ListBox>(new ListBox());
		iCurriculum.getWidget().setStyleName("unitime-TextBox");
		iCurriculum.getWidget().addItem("None", "");
		iCurriculum.getWidget().setSelectedIndex(0);
		iPanel.addRow("Curriculum:", iCurriculum);
		iCurriculumLine = iPanel.getRowCount() - 1;
		
		iArea = new UniTimeWidget<ListBox>(new ListBox());
		iArea.getWidget().setStyleName("unitime-TextBox");
		iArea.getWidget().addItem("Select...", "");
		iArea.getWidget().setSelectedIndex(0);
		iPanel.addRow("Academic Area:", iArea);
		iAreaLine = iPanel.getRowCount() - 1;
		iClassifications = new ListBox(true);
		iClassifications.setWidth("300px");
		iClassifications.setStyleName("unitime-TextBox");
		iClassifications.setVisibleItemCount(3);
		iClassifications.setHeight("100px");
		iPanel.addRow("Classifications:", iClassifications);
		iMajors = new ListBox(true);
		iMajors.setWidth("300px");
		iMajors.setStyleName("unitime-TextBox");
		iMajors.setVisibleItemCount(3);
		iMajors.setHeight("100px");
		iPanel.addRow("Majors:", iMajors);
		iCurriculum.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				curriculumChanged();
				iCurriculum.clearHint();
			}
		});
		iArea.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				areaChanged();
				iArea.clearHint();
			}
		});
		iClassifications.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String curId = (iCurriculum.getWidget().getSelectedIndex() < 0 ? "" : iCurriculum.getWidget().getValue(iCurriculum.getWidget().getSelectedIndex()));
				if (!curId.isEmpty()) {
					Curriculum c = iCurricula.get(Long.valueOf(curId));
					int limit = 0;
					boolean noneSelected = true;
					classifcations: for (int i = 0; i < iClassifications.getItemCount(); i++) {
						if (iClassifications.isItemSelected(i)) {
							for (IdName f: c.getClassifications()) {
								if (f.getId().toString().equals(iClassifications.getValue(i))) {
									limit += f.getLimit();
									noneSelected = false;
									continue classifcations;
								}
							}
						}
					}
					if (noneSelected) limit = c.getLimit();
					iLimit.getWidget().setText(limit == 0 ? "" : String.valueOf(limit));
				}
			}
		});
		iMajors.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCurriculum.getWidget().setSelectedIndex(0);
			}
		});
		
		iPanel.addNotPrintableBottomRow(iTitleAndButtons.clonePanel(null));
		
		if (standAlone) {
			LoadingWidget.getInstance().show("Loading reservation...");
			if (Window.Location.getParameter("id") == null) {
				initStudentGroupsAndCurricula(new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						loadingFailed(caught);
					}
					@Override
					public void onSuccess(Boolean result) {
						setReservation(null);
						LoadingWidget.getInstance().hide();
					}
				});
			} else {
				iReservationService.getReservation(Long.valueOf(Window.Location.getParameter("id")), new AsyncCallback<ReservationInterface>() {
					public void onFailure(Throwable caught) {
						loadingFailed(caught);
					}
					@Override
					public void onSuccess(final ReservationInterface reservation) {
						if (reservation instanceof ReservationInterface.CurriculumReservation) {
							initCurricula(new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									loadingFailed(caught);
								}
								@Override
								public void onSuccess(Boolean result) {
									setReservation(reservation);
									LoadingWidget.getInstance().hide();
								}
							});
						} else if (reservation instanceof ReservationInterface.GroupReservation) {
							initStudentGroups(new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									loadingFailed(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									setReservation(reservation);
									LoadingWidget.getInstance().hide();
								}
							});
						} else {
							setReservation(reservation);
							LoadingWidget.getInstance().hide();
						}
					};
				});
			}
		} else {
			initStudentGroupsAndCurricula(new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
					iTitleAndButtons.setErrorMessage("Load failed: " + caught.getMessage());
				}
				@Override
				public void onSuccess(Boolean result) {
				}
			});
		}
		
		iCourseBox.addCourseSelectionChangeHandler(new CourseSelectionChangeHandler() {
			@Override
			public void onChange(CourseSelectionChangeEvent evt) {
				if (evt.isValid()) {
					iReservationService.getOfferingByCourseName(evt.getCourse(), new AsyncCallback<Offering>() {
						@Override
						public void onFailure(Throwable caught) {
							iCourseBox.setError(caught.getMessage());
						}
						@Override
						public void onSuccess(Offering result) {
							setOffering(result);
						}
					});
				} else {
					setOffering(null);
				}
			}
		});
	}
	
	private void loadingFailed(Throwable caught) {
		LoadingWidget.getInstance().fail("Load failed: " + caught.getMessage());
		iTitleAndButtons.setErrorMessage("Load failed: " + caught.getMessage());
		for (EditFinishedHandler h: iEditFinishedHandlers)
			h.onFailure(caught);
	}
	
	private void initStudentGroups(final AsyncCallback<Boolean> callback) {
		iReservationService.getStudentGroups(new AsyncCallback<List<IdName>>() {
			@Override
			public void onFailure(Throwable caught) {
				iType.getWidget().removeItem(2);
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(List<IdName> result) {
				if (result.isEmpty()) {
					iType.getWidget().removeItem(2);
				} else {
					for (IdName group: result) {
						iGroup.getWidget().addItem(group.getAbbv() + " - " + group.getName() + " (" + group.getLimit() + ")", group.getId().toString());
					}
				}
				callback.onSuccess(true);
			}
		});
	}
	
	private void initCurricula(final AsyncCallback<Boolean> callback) {
		iReservationService.getAreas(new AsyncCallback<List<Area>>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			
			@Override
			public void onSuccess(List<Area> result) {
				for (Area c: result) {
					iArea.getWidget().addItem(c.getAbbv() + " - " + c.getName(), c.getId().toString());
					iAreas.put(c.getId(), c);
				}
				callback.onSuccess(true);
			}
		});
	}
	
	private void initStudentGroupsAndCurricula(final AsyncCallback<Boolean> callback) {
		initStudentGroups(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(Boolean result) {
				initCurricula(callback);
			}
		});
	}
	
	public void setReservation(ReservationInterface r) {
		iReservation = r;
		UniTimePageLabel.getInstance().setPageName(iReservation == null ? "Add Reservation" : "Edit Reservation");
		iTitleAndButtons.setEnabled("delete", iReservation != null);
		
		Long offeringId = (iReservation == null ? null : iReservation.getOffering().getId());
		if (offeringId == null) {
			if (Window.Location.getParameter("offering") != null)
				offeringId =  Long.valueOf(Window.Location.getParameter("offering"));
		}

		if (offeringId == null) {
			iOffering = null;
			iCourseBox.setEnabled(true);
			iCourseBox.setCourse("", false);
			iLimit.getWidget().setText("");
			iExpirationDate.getWidget().setText("");
			iStructure.clear(); iClasses.clear(); iConfigs.clear();
			iType.getWidget().setSelectedIndex(0);
			iGroup.getWidget().setSelectedIndex(0);
			iArea.getWidget().setSelectedIndex(0);
			iClassifications.clear();
			iStudents.getWidget().setText("");
			iCourse.getWidget().clear();
			iMajors.clear();
			iType.setReadOnly(false);
			iCurricula.clear();
			iCurriculum.getWidget().clear();
			iCurriculum.getWidget().addItem("None", "");
			iCurriculum.getWidget().setSelectedIndex(0);
			areaChanged();
			typeChanged();
		} else {
			iCourseBox.setEnabled(false);
			LoadingWidget.getInstance().show("Loading reservation...");
			iReservationService.getOffering(offeringId, new AsyncCallback<Offering>() {
				@Override
				public void onFailure(Throwable caught) {
					loadingFailed(caught);
				}

				@Override
				public void onSuccess(Offering result) {
					setOffering(result);
					LoadingWidget.getInstance().hide();
				}
			});
		}
	}
	
	public void setOffering(Offering offering) {
		iOffering = offering;
		if (iOffering == null) return;

		if (!iCourseBox.isEnabled())
			iCourseBox.setCourse(iOffering.getAbbv(), false);

		iStructure.clear(); iClasses.clear(); iConfigs.clear();
		for (Config config: iOffering.getConfigs()) {
			TreeItem configItem = new TreeItem(new ConfigSelection(config));
			for (Subpart subpart: config.getSubparts()) {
				if (subpart.getParentId() == null)
					for (TreeItem item: addClasses(subpart, null))
						configItem.addItem(item);
			}
			iStructure.addItem(configItem);
		}
		
		iStudents.getWidget().setText("");

		iCourse.getWidget().clear();
		iCourse.getWidget().addItem("Select...", "");
		for (Course course: iOffering.getCourses()) {
			iCourse.getWidget().addItem(course.getAbbv() + (course.getName() == null || course.getName().isEmpty() ? "" : " - " + course.getName()), course.getId().toString());
		}
		
		typeChanged();
		populate();
		
		iCurricula.clear();
		iCurriculum.getWidget().clear();
		iCurriculum.getWidget().addItem("None", "");
		iCurriculum.getWidget().setSelectedIndex(0);
		iReservationService.getCurricula(iOffering.getId(), new AsyncCallback<List<Curriculum>>() {
			@Override
			public void onFailure(Throwable caught) {
				loadingFailed(caught);
			}
			@Override
			public void onSuccess(List<Curriculum> result) {
				curricula: for (Curriculum curriculum: result) {
					iCurricula.put(curriculum.getId(), curriculum);
					iCurriculum.getWidget().addItem(curriculum.getAbbv() + " - " + curriculum.getName() + " (" + curriculum.getLimit() + ")", curriculum.getId().toString());
					if (curriculum.getArea().getId().toString().equals(iArea.getWidget().getValue(iArea.getWidget().getSelectedIndex()))) {
						majors: for (int i = 0; i < iMajors.getItemCount(); i++) {
							if (iMajors.isItemSelected(i)) {
								for (IdName m: curriculum.getMajors()) {
									if (m.getId().toString().equals(iMajors.getValue(i))) continue majors; 
								}
								continue curricula;
							} else {
								for (IdName m: curriculum.getMajors()) {
									if (m.getId().toString().equals(iMajors.getValue(i))) continue curricula; 
								}
								continue majors;
							}
						}
						iCurriculum.getWidget().setSelectedIndex(iCurriculum.getWidget().getItemCount() - 1);
					}
				}
				String val = iType.getWidget().getValue(iType.getWidget().getSelectedIndex());
				iPanel.getRowFormatter().setVisible(iCurriculumLine, "curriculum".equals(val) && iCurriculum.getWidget().getItemCount() > 1);
			}
		});
	}
	
	private List<TreeItem> addClasses(Subpart subpart, Long parent) {
		List<TreeItem> ret = new ArrayList<TreeItem>();
		for (Clazz clazz: subpart.getClasses()) {
			if (parent != null && !parent.equals(clazz.getParentId())) continue;
			TreeItem clazzItem = new TreeItem(new ClassSelection(clazz));
			for (Subpart child: subpart.getConfig().getSubparts()) {
				if (subpart.getId().equals(child.getParentId()))
					for (TreeItem item: addClasses(child, clazz.getId()))
						clazzItem.addItem(item);
			}
			ret.add(clazzItem);
		}
		return ret;
	}
	
	private void typeChanged() {
		String val = iType.getWidget().getValue(iType.getWidget().getSelectedIndex());
		iPanel.getRowFormatter().setVisible(iStudentsLine, "individual".equals(val));
		iPanel.getRowFormatter().setVisible(iCourseLine, "course".equals(val));
		iPanel.getRowFormatter().setVisible(iGroupLine, "group".equals(val));
		iPanel.getRowFormatter().setVisible(iCurriculumLine, "curriculum".equals(val) && iCurriculum.getWidget().getItemCount() > 1);
		iPanel.getRowFormatter().setVisible(iAreaLine, "curriculum".equals(val));
		iPanel.getRowFormatter().setVisible(1 + iAreaLine, "curriculum".equals(val));
		iPanel.getRowFormatter().setVisible(2 + iAreaLine, "curriculum".equals(val));
		if ("course".equals(val)) {
			iLimit.getWidget().setReadOnly(true);
			iLimit.getWidget().setText("");
			String cid = iCourse.getWidget().getValue(iCourse.getWidget().getSelectedIndex());
			for (Course course: iOffering.getCourses()) {
				if (course.getId().toString().equals(cid))
					iLimit.getWidget().setText(course.getLimit() == null ? "" : course.getLimit().toString());
			}
		} else if ("individual".equals(val)) {
			iLimit.getWidget().setReadOnly(true);
			iLimit.getWidget().setText(String.valueOf(iStudents.getWidget().getText().split("\n").length));
		} else {
			iLimit.getWidget().setReadOnly(false);
		}
	}
	
	private void areaChanged() {
		iMajors.clear();
		iClassifications.clear();
		String id = iArea.getWidget().getValue(iArea.getWidget().getSelectedIndex());
		if (!id.isEmpty()) {
			Area c = iAreas.get(Long.valueOf(id));
			for (IdName major: c.getMajors()) {
				iMajors.addItem(major.getAbbv() + " - " + major.getName(), major.getId().toString());
			}
			for (IdName clasf: c.getClassifications()) {
				iClassifications.addItem(clasf.getAbbv() + " - " + clasf.getName(), clasf.getId().toString());
			}
		}
		String curId = (iCurriculum.getWidget().getSelectedIndex() < 0 ? "" : iCurriculum.getWidget().getValue(iCurriculum.getWidget().getSelectedIndex()));
		if (!curId.isEmpty()) {
			Curriculum c = iCurricula.get(Long.valueOf(curId));
			if (!c.getArea().getId().toString().equals(id))
				iCurriculum.getWidget().setSelectedIndex(0);
		}
	}
	
	private void curriculumChanged() {
		String id = iCurriculum.getWidget().getValue(iCurriculum.getWidget().getSelectedIndex());
		if (!id.isEmpty()) {
			iMajors.clear();
			iClassifications.clear();
			Curriculum c = iCurricula.get(Long.valueOf(id));
			for (int i = 0; i < iArea.getWidget().getItemCount(); i++) {
				if (c.getArea().getId().toString().equals(iArea.getWidget().getValue(i))) {
					iArea.getWidget().setSelectedIndex(i); break;
				}
			}
			areaChanged();
			for (int i = 0; i < iMajors.getItemCount(); i++) {
				String majorId = iMajors.getValue(i);
				boolean hasMajor = false;
				for (IdName m: c.getMajors())
					if (m.getId().toString().equals(majorId)) { hasMajor = true; break; }
				iMajors.setItemSelected(i, hasMajor);
			}
			for (int i = 0; i < iClassifications.getItemCount(); i++) {
				String clasfId = iClassifications.getValue(i);
				boolean hasClasf = false;
				for (IdName m: c.getClassifications())
					if (m.getId().toString().equals(clasfId)) { hasClasf = true; break; }
				iClassifications.setItemSelected(i, hasClasf);
			}
			iLimit.getWidget().setText(c.getLimit() == null ? "" : c.getLimit().toString());
		}
	}
	
	public void populate() {
		if (iReservation == null) return;
		iLimit.getWidget().setText(iReservation.getLimit() == null ? "" : iReservation.getLimit().toString());
		if (iReservation.getExpirationDate() == null) {
			iExpirationDate.getWidget().setText("");
		} else {
			iExpirationDate.getWidget().setText(sDF.format(iReservation.getExpirationDate()));
		}
		for (Clazz c: iReservation.getClasses()) {
			ClassSelection s = iClasses.get(c.getId());
			if (s != null) {
				s.setValue(true);
				s.propagateUp(true);
				s.propagateDown(true);
			}
		}
		for (Config c: iReservation.getConfigs()) {
			ConfigSelection s = iConfigs.get(c.getId());
			if (s != null) {
				s.setValue(true);
				s.propagate(true);
			}
		}
		for (int i = 0; i < iStructure.getItemCount(); i++)
			openNodes(iStructure.getItem(i));
		if (iReservation instanceof ReservationInterface.IndividualReservation) {
			select(iType.getWidget(), "individual");
			String students = "";
			for (IdName student: ((ReservationInterface.IndividualReservation) iReservation).getStudents())
				students += (students.isEmpty() ? "" : "\n") + student.getAbbv() + " " + student.getName();
			iStudents.getWidget().setText(students);
		} else if (iReservation instanceof ReservationInterface.GroupReservation) {
			select(iType.getWidget(), "group");
			select(iGroup.getWidget(), ((ReservationInterface.GroupReservation) iReservation).getGroup().getId().toString());
		} else if (iReservation instanceof ReservationInterface.CourseReservation) {
			select(iType.getWidget(), "course");
			select(iCourse.getWidget(), ((ReservationInterface.CourseReservation) iReservation).getCourse().getId().toString());
		} else if (iReservation instanceof ReservationInterface.CurriculumReservation) {
			select(iType.getWidget(), "curriculum");
			Area curriculum = ((ReservationInterface.CurriculumReservation) iReservation).getCurriculum();
			select(iArea.getWidget(), curriculum.getId().toString());
			areaChanged();
			for (int i = 0; i < iMajors.getItemCount(); i++) {
				Long id = Long.valueOf(iMajors.getValue(i));
				boolean selected = false;
				for (IdName mj: curriculum.getMajors())
					if (mj.getId().equals(id)) selected = true;
				iMajors.setItemSelected(i, selected);
			}
			for (int i = 0; i < iClassifications.getItemCount(); i++) {
				Long id = Long.valueOf(iClassifications.getValue(i));
				boolean selected = false;
				for (IdName clasf: curriculum.getClassifications())
					if (clasf.getId().equals(id)) selected = true;
				iClassifications.setItemSelected(i, selected);
			}
		}
		typeChanged();
		iType.setReadOnly(true);
		iType.setText(iType.getWidget().getItemText(iType.getWidget().getSelectedIndex()));
	}
	
	private void openNodes(TreeItem item) {
		CheckBox ch = (CheckBox)item.getWidget();
		if (!ch.getValue() || ch.isEnabled()) return;
		item.setState(true);
		for (int i = 0; i < item.getChildCount(); i++)
			openNodes(item.getChild(i));
	}
	
	private void select(ListBox l, String value) {
		for (int i = 0; i < l.getItemCount(); i++) {
			if (l.getValue(i).equals(value)) {
				l.setSelectedIndex(i);
				return;
			}
		}
	}
	
	public ReservationInterface validate() {
		boolean ok = true;
		String type = iType.getWidget().getValue(iType.getWidget().getSelectedIndex());
		if (type.isEmpty()) {
			iType.setErrorHint("Reservation type must be selected.");
			ok = false;
		} else {
			iType.clearHint();
		}
		ReservationInterface r;
		if ("individual".equals(type)) {
			r = new ReservationInterface.IndividualReservation();
			if (iStudents.getWidget().getText().isEmpty()) {
				iStudents.setErrorHint("No students provided.");
				ok = false;
			} else {
				RegExp rx = RegExp.compile("^([a-zA-Z0-9]+)[ ,;\\|]?(.*)$");
				for (String student: iStudents.getWidget().getText().split("\\n")) {
					if (student.trim().isEmpty()) continue;
					try {
						MatchResult m = rx.exec(student.trim());
						if (m == null) {
							iStudents.setErrorHint("Line '" + student + "' is not a valid student record.");
							ok = false;
							break;
						}
						IdName s = new IdName();
						s.setAbbv(m.getGroup(1));
						s.setName(m.getGroupCount() <= 2 ? "" : m.getGroup(2).trim());
						((ReservationInterface.IndividualReservation) r).getStudents().add(s);
						if (((ReservationInterface.IndividualReservation) r).getStudents().isEmpty()) {
							iStudents.setErrorHint("No students provided.");
							ok = false;
						}
					} catch (Exception e) {
						iStudents.setErrorHint("Line '" + student + "' is not a valid student record (" + e.getMessage() + ").");
						ok = false;
					}
				}
			}
		} else if ("group".equals(type)) {
			r = new ReservationInterface.GroupReservation();
			String gid = iGroup.getWidget().getValue(iGroup.getWidget().getSelectedIndex());
			if (gid.isEmpty()) {
				iGroup.setErrorHint("A student group must be provided.");
				ok = false;
			} else {
				IdName group = new IdName();
				group.setId(Long.valueOf(gid));
				group.setName(iGroup.getWidget().getItemText(iGroup.getWidget().getSelectedIndex()));
				((ReservationInterface.GroupReservation) r).setGroup(group);
			}
		} else if ("course".equals(type)) {
			r = new ReservationInterface.CourseReservation();
			String cid = iCourse.getWidget().getValue(iCourse.getWidget().getSelectedIndex());
			if (cid.isEmpty()) {
				iCourse.setErrorHint("A course must be provided.");
				ok = false;
			} else {
				Course course = new Course();
				course.setId(Long.valueOf(cid));
				course.setName(iCourse.getWidget().getItemText(iCourse.getWidget().getSelectedIndex()));
				((ReservationInterface.CourseReservation) r).setCourse(course);
			}
		} else if ("curriculum".equals(type)) {
			r = new ReservationInterface.CurriculumReservation();
			String aid = iArea.getWidget().getValue(iArea.getWidget().getSelectedIndex());
			if (aid.isEmpty()) {
				iArea.setErrorHint("An academic area must be provided.");
				ok = false;
			} else {
				Area curriculum = new Area();
				curriculum.setId(Long.valueOf(aid));
				curriculum.setName(iArea.getWidget().getItemText(iArea.getWidget().getSelectedIndex()));
				for (int i = 0; i < iMajors.getItemCount(); i++ ) {
					if (iMajors.isItemSelected(i)) {
						IdName mj = new IdName();
						mj.setId(Long.valueOf(iMajors.getValue(i)));
						mj.setName(iMajors.getItemText(i));
						curriculum.getMajors().add(mj);
					}
				}
				for (int i = 0; i < iClassifications.getItemCount(); i++ ) {
					if (iClassifications.isItemSelected(i)) {
						IdName clasf = new IdName();
						clasf.setId(Long.valueOf(iClassifications.getValue(i)));
						clasf.setName(iClassifications.getItemText(i));
						curriculum.getClassifications().add(clasf);
					}
				}
				((ReservationInterface.CurriculumReservation) r).setCurriculum(curriculum);
			}
		} else {
			iType.setErrorHint("Reservation type not supported.");
			return null;
		}
		if (!iExpirationDate.getWidget().getText().isEmpty()) {
			try {
				r.setExpirationDate(sDF.parse(iExpirationDate.getWidget().getText()));
				iType.clearHint();
			} catch (Exception e) {
				iExpirationDate.setErrorHint("Expiration date is not valid.");
				ok = false;
			}
		}
		if (!"individual".equals(type)) {
			if (iLimit.getWidget().getText().isEmpty()) {
				r.setLimit(null);
			} else {
				try {
					r.setLimit(Integer.parseInt(iLimit.getWidget().getText()));
				} catch (Exception e) {
					iLimit.setErrorHint("Reservation limit is not valid.");
					ok = false;
				}
			}
		}
		if (iReservation != null)
			r.setId(iReservation.getId());
		Offering o = new Offering();
		o.setId(iOffering.getId());
		o.setName(iOffering.getName());
		r.setOffering(o);
		for (ConfigSelection config: iConfigs.values()) {
			if (config.getValue() && config.isEnabled()) {
				Config c = new Config();
				c.setId(config.getConfig().getId());
				c.setName(config.getConfig().getName());
				r.getConfigs().add(c);
			}
		}
		for (ClassSelection clazz: iClasses.values()) {
			if (clazz.getValue() && clazz.isEnabled()) {
				Clazz c = new Clazz();
				c.setId(clazz.getClazz().getId());
				c.setName(clazz.getClazz().getName());
				r.getClasses().add(c);
			}
		}
		return (ok ? r : null);
	}
	
	private native JavaScriptObject createLookupCallback() /*-{
		return function(person) {
			@org.unitime.timetable.gwt.client.reservations.ReservationEdit::personFound(Ljava/lang/String;Ljava/lang/String;)(person[0],
				person[3] + ", " + person[1] + (person[2] == null ? "" : " " + person[2]));
    	};
 	}-*/;
	
	public static void personFound(String externalUniqueId, String name) {
		sLastStudents.setText(sLastStudents.getText() + (sLastStudents.getText().isEmpty() ? "" : "\n") + externalUniqueId + " " + name);
		sLastLimit.setText(String.valueOf(sLastStudents.getText().split("\n").length));
	}

	private class ClassSelection extends CheckBox implements ClickHandler {
		private Clazz iClazz;
		
		public ClassSelection(Clazz clazz) {
			setStyleName("unitime-LabelInsteadEdit");
			setText(clazz.getAbbv() + " (" + clazz.getLimit() + ")");
			iClazz = clazz;
			addClickHandler(this);
			iClasses.put(clazz.getId(), this);
		}
		
		public Clazz getClazz() { return iClazz; }

		@Override
		public void onClick(ClickEvent event) {
			propagateUp(getValue());
			propagateDown(getValue());
			computeLimit();
		}
		
		private void propagateUp(boolean selected) {
			if (getClazz().getParentId() == null) {
				if (!selected) {
					for (Subpart subpart: getClazz().getSubpart().getConfig().getSubparts()) {
						if (subpart.getParentId() != null) continue;
						for (Clazz c: subpart.getClasses()) {
							if (iClasses.get(c.getId()).getValue()) return;
						}
					}
				}
				ConfigSelection config = iConfigs.get(getClazz().getSubpart().getConfig().getId());
				if (config != null) {
					config.setValue(selected);
					config.setEnabled(!selected);
				}
			} else {
				ClassSelection parent = iClasses.get(getClazz().getParentId());
				if (!selected) {
					for (Clazz c: getClazz().getSubpart().getClasses()) {
						if (parent.getClazz().getId().equals(c.getParentId())) {
							if (iClasses.get(c.getId()).getValue()) return;
						}
					}
				}
				parent.setValue(selected);
				parent.setEnabled(!selected);
				parent.propagateUp(selected);
			}
		}
		
		private void propagateDown(boolean selected) {
			for (Subpart subpart: getClazz().getSubpart().getConfig().getSubparts()) {
				if (getClazz().getSubpart().getId().equals(subpart.getParentId())) {
					for (Clazz clazz: subpart.getClasses()) {
						if (getClazz().getId().equals(clazz.getParentId())) {
							ClassSelection child = iClasses.get(clazz.getId());
							child.setValue(selected);
							child.setEnabled(!selected);
							child.propagateDown(selected);
						}
					}
				}
			}
		}
	}

	private class ConfigSelection extends CheckBox implements ClickHandler {
		private Config iConfig;
		
		public ConfigSelection(Config config) {
			setStyleName("unitime-LabelInsteadEdit");
			setText("Configuration " + config.getAbbv() + (config.getLimit() == null ? " (unlimited)" : " (" + config.getLimit() + ")"));
			iConfig = config;
			iConfigs.put(config.getId(), this);
			addClickHandler(this);
		}
		
		public Config getConfig() { return iConfig; }
		
		@Override
		public void onClick(ClickEvent event) {
			propagate(getValue());
			computeLimit();
		}
		
		private void propagate(boolean selected) {
			for (Subpart subpart: getConfig().getSubparts()) {
				for (Clazz clazz: subpart.getClasses()) {
					ClassSelection child = iClasses.get(clazz.getId());
					child.setValue(selected);
					child.setEnabled(!selected);
				}
			}
		}
	}
	
	private void computeLimit() {
		if (iLimit.isReadOnly()) return;
		int total = 0;
		int limit = 0;
		boolean totalUnlimited = false;
		boolean unlimited = false;
		boolean selected = false;
		for (Config config: iOffering.getConfigs()) {
			for (Subpart subpart: config.getSubparts()) {
				for (Clazz clazz: subpart.getClasses()) {
					ClassSelection child = iClasses.get(clazz.getId());
					if (child.getValue() && child.isEnabled()) {
						limit += clazz.getLimit();
						selected = true;
					}
				}
			}
			if (config.getLimit() == null)
				totalUnlimited = true;
			else
				total += config.getLimit();
			ConfigSelection cfg = iConfigs.get(config.getId());
			if (cfg != null) {
				if (cfg.getValue() && cfg.isEnabled()) {
					selected = true;
					if (cfg.getConfig().getLimit() == null)
						unlimited = true;
					else
						limit += cfg.getConfig().getLimit();
				}
			}
		}
		if (selected) {
			iLimit.getWidget().setText(unlimited ? "" : String.valueOf(limit));
		} else {
			iLimit.getWidget().setText(totalUnlimited ? "" : String.valueOf(total));
		}
	}
	
	private static class DateBox extends Composite {
		private static int sIdCounter = 0;
		private String iId;
		private UniTimeTextBox iText;
		private Image iImage;
		
		public DateBox() {
			iId = "date_box_" + (sIdCounter++);
			HorizontalPanel hp = new HorizontalPanel();
			hp.setSpacing(0);
			iText = new UniTimeTextBox();
			iImage = new Image();
			iImage.getElement().getStyle().setMarginLeft(2, Unit.PX);
			iImage.getElement().getStyle().setMarginTop(4, Unit.PX);
			iText.getElement().setId(iId);
			iText.getElement().getStyle().setMarginLeft(-1, Unit.PX);
			iImage.getElement().setId("show_" + iId);
			iImage.getElement().getStyle().setCursor(Cursor.POINTER);
			iImage.setUrl("scripts/jscalendar/calendar_1.gif");
			hp.add(iText);
			hp.add(iImage);
			initWidget(hp);
		}
		
		public String getText() { return iText.getText(); }
		public void setText(String text) { iText.setText(text); }
		public TextBox getTextBox() { return iText; }
		
		@Override
		protected void onAttach() {
			super.onAttach();
			setup(iId);
		}
		
		private native void setup(String id) /*-{
			$wnd.Calendar.setup( {
				cache      : true,
				electric   : false,
				inputField : id,
				ifFormat   : "%m/%d/%Y",
				showOthers : true,
				button     : "show_" + id
			} );			
		}-*/;
	}
	

	public static class EditFinishedEvent {
		private Long iReservationId;
		public EditFinishedEvent(Long reservationId) {
			iReservationId = reservationId;
		}
		public Long getReservationId() {
			return iReservationId;
		}
	}

	public static interface EditFinishedHandler {
		public void onBack(EditFinishedEvent evt);
		public void onDelete(EditFinishedEvent evt);
		public void onSave(EditFinishedEvent evt);
		public void onFailure(Throwable caught);
	}

	public void addEditFinishedHandler(EditFinishedHandler h) {
		iEditFinishedHandlers.add(h);
	}

}
