package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasStyleName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EnrollmentTable extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestDateFormat());
	private Long iOfferingId = null;

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private VerticalPanel iEnrollmentPanel;
	private Image iOpenCloseImage, iLoadingImage;
	private UniTimeTable<ClassAssignmentInterface.Enrollment> iEnrollments;
	private Label iErrorLabel;
	
	public EnrollmentTable(boolean showHeader) {
		iEnrollmentPanel = new VerticalPanel();
		iEnrollmentPanel.setWidth("100%");
		
		if (showHeader) {
			HorizontalPanel header = new HorizontalPanel();
			iOpenCloseImage = new Image(SectioningCookie.getInstance().getEnrollmentCoursesDetails() ? RESOURCES.treeOpen() : RESOURCES.treeClosed());
			iOpenCloseImage.getElement().getStyle().setCursor(Cursor.POINTER);
			iOpenCloseImage.setVisible(false);
			header.add(iOpenCloseImage);
			Label curriculaLabel = new Label(MESSAGES.enrollmentsTable(), false);
			curriculaLabel.setStyleName("unitime3-HeaderTitle");
			curriculaLabel.getElement().getStyle().setPaddingLeft(2, Unit.PX);
			header.add(curriculaLabel);
			header.setCellWidth(curriculaLabel, "100%");
			header.setStyleName("unitime3-HeaderPanel");
			iEnrollmentPanel.add(header);
			
			iOpenCloseImage.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					SectioningCookie.getInstance().setEnrollmentCoursesDetails(!SectioningCookie.getInstance().getEnrollmentCoursesDetails());
					iOpenCloseImage.setResource(SectioningCookie.getInstance().getEnrollmentCoursesDetails() ? RESOURCES.treeOpen() : RESOURCES.treeClosed());
					if (iEnrollments.getRowCount() > 2) {
						for (int row = 1; row < iEnrollments.getRowCount() - 1; row++) {
							iEnrollments.getRowFormatter().setVisible(row, SectioningCookie.getInstance().getEnrollmentCoursesDetails());
						}
					}
					if (iEnrollments.getRowCount() == 0)
						refresh();
				}
			});
		}

		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.setVisible(false);
		iLoadingImage.getElement().getStyle().setMarginTop(10, Unit.PX);
		iEnrollmentPanel.add(iLoadingImage);
		iEnrollmentPanel.setCellHorizontalAlignment(iLoadingImage, HasHorizontalAlignment.ALIGN_CENTER);
		iEnrollmentPanel.setCellVerticalAlignment(iLoadingImage, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iEnrollments = new UniTimeTable<ClassAssignmentInterface.Enrollment>();
		iEnrollmentPanel.add(iEnrollments);
		
		iErrorLabel = new Label();
		iErrorLabel.setStyleName("unitime-ErrorMessage");
		iEnrollmentPanel.add(iErrorLabel);
		iErrorLabel.setVisible(false);
				
		initWidget(iEnrollmentPanel);
		
		iEnrollments.addMouseClickListener(new UniTimeTable.MouseClickListener<ClassAssignmentInterface.Enrollment>() {
			@Override
			public void onMouseClick(final UniTimeTable.TableEvent<ClassAssignmentInterface.Enrollment> event) {
				if (event.getData() == null) return;
				LoadingWidget.getInstance().show(MESSAGES.loadingEnrollment(event.getData().getStudent().getName()));
				iSectioningService.getEnrollment(event.getData().getStudent().getId(), new AsyncCallback<ClassAssignmentInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().fail(MESSAGES.failedToLoadEnrollments(caught.getMessage()));
					}

					@Override
					public void onSuccess(ClassAssignmentInterface result) {
						WebTable assignments = new WebTable();
						assignments.setHeader(new WebTable.Row(
								new WebTable.Cell(MESSAGES.colSubject(), 1, "75"),
								new WebTable.Cell(MESSAGES.colCourse(), 1, "75"),
								new WebTable.Cell(MESSAGES.colSubpart(), 1, "50"),
								new WebTable.Cell(MESSAGES.colClass(), 1, "75"),
								new WebTable.Cell(MESSAGES.colLimit(), 1, "60"),
								new WebTable.Cell(MESSAGES.colDays(), 1, "50"),
								new WebTable.Cell(MESSAGES.colStart(), 1, "75"),
								new WebTable.Cell(MESSAGES.colEnd(), 1, "75"),
								new WebTable.Cell(MESSAGES.colDate(), 1, "75"),
								new WebTable.Cell(MESSAGES.colRoom(), 1, "100"),
								new WebTable.Cell(MESSAGES.colInstructor(), 1, "100"),
								new WebTable.Cell(MESSAGES.colParent(), 1, "75")
							));
						
						ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
						for (ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
							if (course.isAssigned()) {
								boolean firstClazz = true;
								for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
									String style = "unitime-ClassRow" + (firstClazz && !rows.isEmpty() ? "First": "");
									final WebTable.Row row = new WebTable.Row(
											new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject() : ""),
											new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr() : ""),
											new WebTable.Cell(clazz.getSubpart()),
											new WebTable.Cell(clazz.getSection()),
											new WebTable.Cell(clazz.getLimitString()),
											new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
											new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
											new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
											new WebTable.Cell(clazz.getDatePattern()),
											(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
											new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
											new WebTable.Cell(clazz.getParentSection()));
									rows.add(row);
									for (WebTable.Cell cell: row.getCells())
										cell.setStyleName(style);
									firstClazz = false;
								}
							} else {
								String style = "unitime-ClassRowRed" + (!rows.isEmpty() ? "First": "");
								WebTable.Row row = null;
								String unassignedMessage = MESSAGES.courseNotAssigned();
								if (course.getOverlaps()!=null && !course.getOverlaps().isEmpty()) {
									unassignedMessage = "";
									for (Iterator<String> i = course.getOverlaps().iterator(); i.hasNext();) {
										String x = i.next();
										if (unassignedMessage.isEmpty())
											unassignedMessage += MESSAGES.conflictWithFirst(x);
										else if (!i.hasNext())
											unassignedMessage += MESSAGES.conflictWithLast(x);
										else
											unassignedMessage += MESSAGES.conflictWithMiddle(x);
										if (i.hasNext()) unassignedMessage += ", ";
									}
									if (course.getInstead() != null)
										unassignedMessage += MESSAGES.conflictAssignedAlternative(course.getInstead());
									unassignedMessage += ".";
								} else if (course.isNotAvailable()) {
									unassignedMessage = MESSAGES.classNotAvailable();
								} else if (course.isLocked()) {
									unassignedMessage = MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr());
								}
								for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
									row = new WebTable.Row(
											new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
											new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
											new WebTable.Cell(clazz.getSubpart()),
											new WebTable.Cell(clazz.getSection()),
											new WebTable.Cell(clazz.getLimitString()),
											new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
											new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
											new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
											new WebTable.Cell(clazz.getDatePattern()),
											new WebTable.Cell(unassignedMessage, 3, null));
									break;
								}
								if (row == null) {
									row = new WebTable.Row(
											new WebTable.Cell(course.getSubject()),
											new WebTable.Cell(course.getCourseNbr()),
											new WebTable.Cell(unassignedMessage, 10, null));
								}
								for (WebTable.Cell cell: row.getCells())
									cell.setStyleName(style);
								row.getCell(row.getNrCells() - 1).setStyleName("unitime-ClassRowProblem" + (!rows.isEmpty() ? "First": ""));
								rows.add(row);
							}
						}
						WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
						int idx = 0;
						for (WebTable.Row row: rows) rowArray[idx++] = row;
						assignments.setData(rowArray);
						LoadingWidget.getInstance().hide();
						UniTimeDialogBox dialog = new UniTimeDialogBox(true, true);
						dialog.setWidget(assignments);
						dialog.setText(MESSAGES.dialogEnrollments(event.getData().getStudent().getName()));
						dialog.setEscapeToHide(true);
						dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
							@Override
							public void onClose(CloseEvent<PopupPanel> event) {
								iEnrollments.clearHover();
							}
						});
						dialog.center();
					}
				});
			}
		});
	}
	
	private static class Number extends HTML implements HasCellAlignment {
		public Number(String text) {
			super(text, false);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	private void refresh() {
		clear(true);
		if (iOfferingId != null) {
			iSectioningService.canApprove(iOfferingId, new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
					setErrorMessage(MESSAGES.failedToLoadEnrollments(caught.getMessage()));
					iLoadingImage.setVisible(false);
				}
				@Override
				public void onSuccess(final Boolean canApprove) {
					iSectioningService.listEnrollments(iOfferingId, new AsyncCallback<List<ClassAssignmentInterface.Enrollment>>() {
						@Override
						public void onFailure(Throwable caught) {
							setErrorMessage(MESSAGES.failedToLoadEnrollments(caught.getMessage()));
							iLoadingImage.setVisible(false);
						}
						@Override
						public void onSuccess(List<ClassAssignmentInterface.Enrollment> result) {
							if (result.isEmpty()) {
								setMessage(iOfferingId >= 0 ? MESSAGES.offeringHasNoEnrollments() : MESSAGES.classHasNoEnrollments());
								iOpenCloseImage.setVisible(false);
							} else {
								populate(result, canApprove);
								if (iEnrollments.getRowCount() > 2) {
									for (int row = 1; row < iEnrollments.getRowCount() - 1; row++) {
										iEnrollments.getRowFormatter().setVisible(row, SectioningCookie.getInstance().getEnrollmentCoursesDetails());
									}
								}
								iOpenCloseImage.setVisible(true);
							}
							iLoadingImage.setVisible(false);
						}
					});					
				}
			});
		}
	}
	
	private void clear(boolean loading) {
		for (int row = iEnrollments.getRowCount() - 1; row >= 0; row--) {
			iEnrollments.removeRow(row);
		}
		iEnrollments.clear(true);
		iLoadingImage.setVisible(loading);
		iErrorLabel.setVisible(false);
	}


	private void populate(List<ClassAssignmentInterface.Enrollment> enrollments, boolean canApprove) {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		Collections.sort(enrollments, new Comparator<ClassAssignmentInterface.Enrollment>() {
			@Override
			public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
				int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
				if (cmp != 0) return cmp;
				return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
			}
		});
		
		UniTimeTableHeader hStudent = new UniTimeTableHeader(MESSAGES.colStudent());
		//hStudent.setWidth("100px");
		header.add(hStudent);
		hStudent.addOperation(new Operation() {
			@Override
			public void execute() {
				iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
					@Override
					public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
						int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
						if (cmp != 0) return cmp;
						return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colStudent());
			}
		});
		
		boolean crosslist = false;
		Long courseId = null;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (courseId == null) courseId = e.getCourseId();
			else if (e.getCourseId() != courseId) { crosslist = true; break; }
		}
		
		if (crosslist) {
			UniTimeTableHeader hCourse = new UniTimeTableHeader(MESSAGES.colCourse());
			//hCourse.setWidth("100px");
			header.add(hCourse);
			hCourse.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getCourseName().compareTo(e2.getCourseName());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colCourse());
				}
			});			
		}
		
		boolean hasPriority = false, hasArea = false, hasMajor = false, hasAlternative = false, hasReservation = false, hasRequestedDate = false, hasEnrolledDate = false;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (e.getPriority() > 0) hasPriority = true;
			if (e.isAlternative()) hasAlternative = true;
			if (e.getStudent().hasArea()) hasArea = true;
			if (e.getStudent().hasMajor()) hasMajor = true;
			if (e.getReservation() != null) hasReservation = true;
			if (e.getRequestedDate() != null) hasRequestedDate = true;
			if (e.getEnrolledDate() != null) hasEnrolledDate = true;
		}

		if (hasPriority) {
			UniTimeTableHeader hPriority = new UniTimeTableHeader(MESSAGES.colPriority());
			//hPriority.setWidth("100px");
			hPriority.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = new Integer(e1.getPriority()).compareTo(e2.getPriority());
							if (cmp != 0) return cmp;
							cmp = e1.getAlternative().compareTo(e2.getAlternative());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colPriority());
				}
			});
			header.add(hPriority);			
		}

		if (hasAlternative) {
			UniTimeTableHeader hAlternative = new UniTimeTableHeader(MESSAGES.colAlternative());
			//hAlternative.setWidth("100px");
			hAlternative.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getAlternative().compareTo(e2.getAlternative());
							if (cmp != 0) return cmp;
							cmp = new Integer(e1.getPriority()).compareTo(e2.getPriority());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colAlternative());
				}
			});
			header.add(hAlternative);
		}
		
		if (hasArea) {
			UniTimeTableHeader hArea = new UniTimeTableHeader(MESSAGES.colArea());
			//hArea.setWidth("100px");
			header.add(hArea);
			hArea.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colArea());
				}
			});
			
			UniTimeTableHeader hClasf = new UniTimeTableHeader(MESSAGES.colClassification());
			//hClasf.setWidth("100px");
			header.add(hClasf);
			hClasf.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getStudent().getClassification("|").compareTo(e2.getStudent().getClassification("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getArea("|").compareTo(e2.getStudent().getArea("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colClassification());
				}
			});
		}

		if (hasMajor) {
			UniTimeTableHeader hMajor = new UniTimeTableHeader(MESSAGES.colMajor());
			//hMajor.setWidth("100px");
			header.add(hMajor);
			hMajor.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colMajor());
				}
			});
		}

		if (hasReservation) {
			UniTimeTableHeader hReservation = new UniTimeTableHeader(MESSAGES.colReservation());
			//hReservation.setWidth("100px");
			hReservation.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = (e1.getReservation() == null ? "" : e1.getReservation()).compareTo(e2.getReservation() == null ? "" : e2.getReservation());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colReservation());
				}
			});
			header.add(hReservation);			
		}
		
		final TreeSet<String> subparts = new TreeSet<String>();
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (e.hasClasses())
				for (ClassAssignmentInterface.ClassAssignment c: e.getClasses())
					subparts.add(c.getSubpart());
		}
		
		for (final String subpart: subparts) {
			UniTimeTableHeader hSubpart = new UniTimeTableHeader(subpart);
			//hSubpart.setWidth("100px");
			final int col = 1 + (crosslist ? 1 : 0) + (hasPriority ? 1 : 0) + (hasAlternative ? 1 : 0) + (hasArea ? 2 : 0) + (hasMajor ? 1 : 0) + (hasReservation ? 1 : 0);
			hSubpart.addOperation(new Operation() {
				@Override
				public void execute() {
					SectioningCookie.getInstance().setShowClassNumbers(false);
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						ClassAssignmentInterface.Enrollment e = iEnrollments.getData(row);
						if (e == null || !e.hasClasses()) continue;
						int idx = 0;
						for (String subpart: subparts) {
							((HTML)iEnrollments.getWidget(row, col + idx)).setHTML(e.getClasses(subpart, ", ", false));
							idx ++;
						}
					}
				}
				@Override
				public boolean isApplicable() {
					return SectioningCookie.getInstance().getShowClassNumbers();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.showExternalIds();
				}
			});
			hSubpart.addOperation(new Operation() {
				@Override
				public void execute() {
					SectioningCookie.getInstance().setShowClassNumbers(true);
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						ClassAssignmentInterface.Enrollment e = iEnrollments.getData(row);
						if (e == null || !e.hasClasses()) continue;
						int idx = 0;
						for (String subpart: subparts) {
							((HTML)iEnrollments.getWidget(row, col + idx)).setHTML(e.getClasses(subpart, ", ", true));
							idx ++;
						}
					}
				}
				@Override
				public boolean isApplicable() {
					return !SectioningCookie.getInstance().getShowClassNumbers();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.showClassNumbers();
				}
			});
			hSubpart.addOperation(new Operation() {
				@Override
				public void execute() {
					final boolean showClassNumbers = SectioningCookie.getInstance().getShowClassNumbers();
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getClasses(subpart, "|", showClassNumbers).compareTo(e2.getClasses(subpart, "|", showClassNumbers));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(subpart);
				}
			});
			header.add(hSubpart);
		}
		
		if (hasRequestedDate) {
			UniTimeTableHeader hTimeStamp = new UniTimeTableHeader(MESSAGES.colRequestTimeStamp());
			//hTimeStamp.setWidth("100px");
			hTimeStamp.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getRequestedDate().compareTo(e2.getRequestedDate());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colRequestTimeStamp());
				}
			});
			header.add(hTimeStamp);			
		}
		
		if (hasEnrolledDate) {
			UniTimeTableHeader hTimeStamp = new UniTimeTableHeader(MESSAGES.colEnrollmentTimeStamp());
			//hTimeStamp.setWidth("100px");
			hTimeStamp.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getEnrolledDate().compareTo(e2.getEnrolledDate());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colEnrollmentTimeStamp());
				}
			});
			header.add(hTimeStamp);			
		}
		
		if (canApprove) {
			UniTimeTableHeader hApproved = new UniTimeTableHeader(MESSAGES.colApproved());
			//hTimeStamp.setWidth("100px");
			hApproved.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox)
							((CheckBox)w).setValue(true);
					}
				}
				@Override
				public boolean isApplicable() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && !((CheckBox)w).getValue())
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
					return MESSAGES.selectAll();
				}
			});
			hApproved.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox)
							((CheckBox)w).setValue(false);
					}
				}
				@Override
				public boolean isApplicable() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
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
					return MESSAGES.clearAll();
				}
			});
			hApproved.addOperation(new Operation() {
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>();
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
							studentIds.add(iEnrollments.getData(row).getStudent().getId());
					}
					iLoadingImage.setVisible(true);
					iSectioningService.approveEnrollments(iOfferingId, studentIds, new AsyncCallback<String>() {
						@Override
						public void onSuccess(String result) {
							iLoadingImage.setVisible(false);
							String[] approval = result.split(":");
							for (int row = 0; row < iEnrollments.getRowCount(); row++) {
								Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
								if (w instanceof CheckBox && ((CheckBox)w).getValue())
									iEnrollments.replaceWidget(row, iEnrollments.getCellCount(row) - 1,
											new HTML(MESSAGES.approval(sDF.format(new Date(Long.valueOf(approval[0]))), approval[2]), false));
							}
						}
						@Override
						public void onFailure(Throwable caught) {
							iLoadingImage.setVisible(false);
							setErrorMessage(MESSAGES.failedToApproveEnrollments(caught.getMessage()));
						}
					});
				}
				@Override
				public boolean isApplicable() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
							return true;
					}
					return false;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return MESSAGES.approveSelectedEnrollments();
				}
			});
			hApproved.addOperation(new Operation() {
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>();
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
							studentIds.add(iEnrollments.getData(row).getStudent().getId());
					}
					iLoadingImage.setVisible(true);
					iSectioningService.rejectEnrollments(iOfferingId, studentIds, new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean result) {
							iLoadingImage.setVisible(false);
							for (int row = 0; row < iEnrollments.getRowCount(); ) {
								Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
								if (w instanceof CheckBox && ((CheckBox)w).getValue())
									iEnrollments.removeRow(row);
								else
									row++;
							}
						}
						@Override
						public void onFailure(Throwable caught) {
							iLoadingImage.setVisible(false);
							setErrorMessage(MESSAGES.failedToApproveEnrollments(caught.getMessage()));
						}
					});
				}
				@Override
				public boolean isApplicable() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
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
					return MESSAGES.rejectSelectedEnrollments();
				}
			});
			hApproved.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = new Long(e1.getApprovedDate() == null ? 0 : e1.getApprovedDate().getTime()).compareTo(e2.getApprovedDate() == null ? 0 : e2.getApprovedDate().getTime());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colApproved());
				}
			});
			header.add(hApproved);	
		}

		
		iEnrollments.addRow(null, header);
		
		int enrolled = 0; int waitlisted = 0;
		boolean suffix = SectioningCookie.getInstance().getShowClassNumbers();
		for (ClassAssignmentInterface.Enrollment enrollment: enrollments) {
			List<Widget> line = new ArrayList<Widget>();
			line.add(new Label(enrollment.getStudent().getName(), false));
			if (crosslist)
				line.add(new Label(enrollment.getCourseName(), false));
			if (hasPriority)
				line.add(new Number(enrollment.getPriority() <= 0 ? "&nbsp;" : MESSAGES.priority(enrollment.getPriority())));
			if (hasAlternative)
				line.add(new Label(enrollment.getAlternative(), false));
			if (hasArea) {
				line.add(new HTML(enrollment.getStudent().getArea("<br>"), false));
				line.add(new HTML(enrollment.getStudent().getClassification("<br>"), false));
			}
			if (hasMajor)
				line.add(new HTML(enrollment.getStudent().getMajor("<br>"), false));
			if (hasReservation)
				line.add(new HTML(enrollment.getReservation() == null ? "&nbsp;" : enrollment.getReservation(), false));
			if (!subparts.isEmpty()) {
				if (!enrollment.hasClasses()) {
					line.add(new WarningLabel(MESSAGES.courseWaitListed(), subparts.size()));
				} else for (String subpart: subparts) {
					line.add(new HTML(enrollment.getClasses(subpart, ", ", suffix), false));
				}
			}
			if (hasRequestedDate)
				line.add(new HTML(enrollment.getRequestedDate() == null ? "&nbsp;" : sDF.format(enrollment.getRequestedDate()), false));
			if (hasEnrolledDate)
				line.add(new HTML(enrollment.getEnrolledDate() == null ? "&nbsp;" : sDF.format(enrollment.getEnrolledDate()), false));
			if (canApprove) {
				if (enrollment.getApprovedDate() == null) {
					CheckBox ch = new CheckBox();
					ch.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.stopPropagation();
						}
					});
					line.add(ch);
				} else { 
					line.add(new HTML(MESSAGES.approval(sDF.format(enrollment.getApprovedDate()), enrollment.getApprovedBy()), false));
				}
			}
			iEnrollments.addRow(enrollment, line);
			iEnrollments.getRowFormatter().setVerticalAlign(iEnrollments.getRowCount() - 1, HasVerticalAlignment.ALIGN_TOP);
			if (enrollment.hasClasses())
				enrolled++;
			else
				waitlisted++;
		}
		
		List<Widget> footer = new ArrayList<Widget>();
		if (waitlisted == 0) {
			footer.add(new TotalLabel(MESSAGES.totalEnrolled(enrolled), header.size()));
		} else if (enrolled == 0) {
			footer.add(new TotalLabel(MESSAGES.totalRequested(waitlisted), header.size()));
		} else {
			footer.add(new TotalLabel(MESSAGES.totalEnrolled(enrolled), header.size() / 2)); 
			footer.add(new TotalLabel(MESSAGES.totalWaitListed(waitlisted), header.size() - (header.size() / 2)));
		}
		iEnrollments.addRow(null, footer);
	}
	
	private static class TotalLabel extends HTML implements HasColSpan, HasStyleName {
		private int iColSpan;
		
		public TotalLabel(String text, int colspan) {
			super(text, false);
			iColSpan = colspan;
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
		
		@Override
		public String getStyleName() {
			return "unitime-TotalRow";
		}
		
	}
	
	private static class WarningLabel extends HTML implements HasColSpan, HasStyleName {
		private int iColSpan;
		
		public WarningLabel(String text, int colspan) {
			super(text, false);
			iColSpan = colspan;
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
		
		@Override
		public String getStyleName() {
			return "unitime-ClassRowRed";
		}
		
	}
	
	public void insert(final RootPanel panel) {
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		if (SectioningCookie.getInstance().getEnrollmentCoursesDetails()) {
			refresh();
		} else {
			clear(false);
			iOpenCloseImage.setVisible(true);
		}
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}

	public void setErrorMessage(String message) {
		iErrorLabel.setStyleName("unitime-ErrorMessage");
		iErrorLabel.setText(message);
		iErrorLabel.setVisible(message != null && !message.isEmpty());
	}
	
	public void setMessage(String message) {
		iErrorLabel.setStyleName("unitime-Message");
		iErrorLabel.setText(message);
		iErrorLabel.setVisible(message != null && !message.isEmpty());
	}
	
	public void scrollIntoView(Long studentId) {
		for (int r = 1; r < iEnrollments.getRowCount(); r++) {
			if (iEnrollments.getData(r) != null && iEnrollments.getData(r).getStudent().getId() == studentId) {
				iEnrollments.getRowFormatter().getElement(r).scrollIntoView();
			}
		}
	}

	public static class ReservationClickedEvent {
		private ReservationInterface iReservation;
		
		public ReservationClickedEvent(ReservationInterface reservation) {
			iReservation = reservation;
		}
		
		public ReservationInterface getReservation() {
			return iReservation;
		}
	}
	
	public interface ReservationClickHandler {
		public void onClick(ReservationClickedEvent evt);
	}
	
	public void select(Long studentId) {
		for (int i = 0; i < iEnrollments.getRowCount(); i++) {
			ClassAssignmentInterface.Enrollment e = iEnrollments.getData(i);
			if (e == null) continue;
			if (e.getStudent().getId() == studentId)
				iEnrollments.getRowFormatter().setStyleName(i, "unitime-TableRowSelected");
			else if ("unitime-TableRowSelected".equals(iEnrollments.getRowFormatter().getStyleName(i)))
				iEnrollments.getRowFormatter().removeStyleName(i, "unitime-TableRowSelected");
		}
	}

}
