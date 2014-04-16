/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class ExaminationEnrollmentTable extends EnrollmentTable {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	public ExaminationEnrollmentTable(boolean showHeader, boolean online) {
		super(showHeader, online);
		getTable().setStyleName("unitime-Enrollments");
	}
	
	@Override
	protected void refresh() {
		clear();
		getHeader().showLoading();
		if (getId() != null) {
			RPC.execute(ExaminationEnrollmentsRpcRequest.getEnrollmentsForExam(getId()), new AsyncCallback<GwtRpcResponseList<ClassAssignmentInterface.Enrollment>>() {
				@Override
				public void onFailure(Throwable caught) {
					getHeader().setErrorMessage(MESSAGES.failedNoEnrollments(caught.getMessage()));
				}

				@Override
				public void onSuccess(GwtRpcResponseList<Enrollment> result) {
					getHeader().clearMessage();
					populate(result, null);
				}
			});
		}
	}
	
	@Override
	public void showStudentSchedule(final ClassAssignmentInterface.Student student, final AsyncCallback<Boolean> callback) {
		RPC.execute(ExaminationScheduleRpcRequest.getScheduleForStudent(getId(), student.getId()), new AsyncCallback<ExaminationScheduleRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(ExaminationScheduleRpcResponse result) {
				callback.onSuccess(true);
				final UniTimeTable<RelatedObjectInterface> table = new UniTimeTable<RelatedObjectInterface>();
				table.setStyleName("unitime-EventOwners");
				List<Widget> ownersHeader = new ArrayList<Widget>();
				ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colCourse()));
				ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colSection()));
				ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colType()));
				ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colTitle()));
				ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colDate()));
				ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colTime()));
				ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colLocation()));
				ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colInstructor()));
				boolean hasConflicts = false;
				for (RelatedObjectInterface obj: result.getExams())
					if (obj.hasConflicts()) {
						ownersHeader.add(new ClickableUniTimeTableHeader(MESSAGES.colConflict()));
						hasConflicts = true; break;
					}
				table.addRow(null, ownersHeader);
				table.addMouseClickListener(new UniTimeTable.MouseClickListener<EventInterface.RelatedObjectInterface>() {
					@Override
					public void onMouseClick(TableEvent<RelatedObjectInterface> event) {
						if (event.getData() != null && event.getData().hasDetailPage())
							ToolBox.open(event.getData().getDetailPage());
						if (event.getData() == null && event.getCol() >= 0) {
							table.sort(event.getCol(), new RelatedObjectComparator(event.getCol()));
							boolean asc = table.getHeader(event.getCol()).getOrder();
							SectioningCookie.getInstance().setRelatedSortBy(asc ? 1 + event.getCol() : -1 - event.getCol());
						}
					}
				});
				for (RelatedObjectInterface obj: result.getExams()) {
					List<Widget> row = new ArrayList<Widget>();
					String course = "";
					if (obj.hasCourseNames()) {
						for (String cn: obj.getCourseNames()) {
							if (course.isEmpty()) {
								course += cn;
							} else {
								course += "<span class='cross-list'>" + cn + "</span>";
							}
						}
					} else {
						course = obj.getName();
					}
					row.add(new HTML(course, false));
					
					String section = "";
					if (obj.hasExternalIds()) {
						for (String ex: obj.getExternalIds()) {
							if (section.isEmpty()) {
								section += ex;
							} else {
								section += "<span class='cross-list'>" + ex + "</span>";
							}
						}
					} else if (obj.hasSectionNumber()) {
						section = obj.getSectionNumber();
					}
					row.add(new HTML(section, false));
					
					String type = (obj.hasInstruction() ? obj.getInstruction() : obj.getType().name());
					row.add(new Label(type, false));
					
					String title = "";
					if (obj.hasCourseTitles()) {
						String last = null;
						for (String ct: obj.getCourseTitles()) {
							if (last != null && !last.isEmpty() && last.equals(ct))
								ct = "";
							else
								last = ct;
							if (title.isEmpty()) {
								title += ct;
							} else {
								title += "<span class='cross-list'>" + ct + "</span>";
							}
						}
					} else {
						title = "";
					}
					row.add(new HTML(title, false));
					
					if (obj.hasDate()) {
						row.add(new Label(obj.getDate(), false));
					} else {
						row.add(new Label());
					}
					
					if (obj.hasTime()) {
						row.add(new Label(obj.getTime(), false));
					} else {
						row.add(new Label());
					}
					
					String location = "";
					if (obj.hasLocations()) {
						for (ResourceInterface loc: obj.getLocations()) {
							location += (location.isEmpty() ? "" : "<br>") + loc.getName();
						}
					}
					row.add(new HTML(location, false));

					if (obj.hasInstructors()) {
						row.add(new HTML(obj.getInstructorNames("<br>", MESSAGES), false));
					} else {
						row.add(new HTML());
					}
					
					if (hasConflicts) {
						if (obj.hasConflicts()) {
							HTML html = new HTML(obj.getConflicts());
							html.addStyleName("conflict");
							row.add(html);
						} else {
							row.add(new HTML("&nbsp;", false));
						}
					}

					int rowNumber = table.addRow(obj, row);
					table.getRowFormatter().addStyleName(rowNumber, "owner-row");
					for (int i = 0; i < table.getCellCount(rowNumber); i++)
						table.getCellFormatter().addStyleName(rowNumber, i, "owner-cell");
				}
				
				int sort = SectioningCookie.getInstance().getRelatedSortBy();
				if (sort > 0)
					table.sort(table.getHeader(sort - 1), new RelatedObjectComparator(sort - 1), true);
				else if (sort < 0)
					table.sort(table.getHeader(-1 - sort), new RelatedObjectComparator(-1 - sort), false);
				
				SimpleForm form = new SimpleForm();
				form.addRow(table);
				final UniTimeHeaderPanel buttons = new UniTimeHeaderPanel();
				form.addBottomRow(buttons);
				final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
				dialog.setWidget(form);
				dialog.setText(MESSAGES.dialogExaminations(result.getExamType(), student.getName()));
				dialog.setEscapeToHide(true);
				buttons.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						dialog.hide();
					}
				});
				dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
					@Override
					public void onClose(CloseEvent<PopupPanel> event) {
						getTable().clearHover();
					}
				});
				dialog.center();
			}
		});		
	}
	
	private static class ClickableUniTimeTableHeader extends UniTimeTableHeader {
		private ClickableUniTimeTableHeader(String title) {
			super(title);
		}
		
		@Override
		public String getStyleName() {
			return "unitime-ClickableTableHeader";
		}
	}
	
	public static class ExaminationEnrollmentsRpcRequest implements GwtRpcRequest<GwtRpcResponseList<ClassAssignmentInterface.Enrollment>> {
		private Long iExamId;
		
		public ExaminationEnrollmentsRpcRequest() {}
		
		public boolean hasExamId() { return iExamId != null; }
		public Long getExamId() { return iExamId; }
		public void setExamId(Long examId) { iExamId = examId; }
		
		@Override
		public String toString() {
			return (hasExamId() ? getExamId().toString() : "NULL");
		}
		
		public static ExaminationEnrollmentsRpcRequest getEnrollmentsForExam(Long examId) {
			ExaminationEnrollmentsRpcRequest request = new ExaminationEnrollmentsRpcRequest();
			request.setExamId(examId);
			return request;
		}
	}
	
	public static class ExaminationScheduleRpcRequest implements GwtRpcRequest<ExaminationScheduleRpcResponse> {
		private Long iExamId, iStudentId;
		
		public ExaminationScheduleRpcRequest() {}
		
		public boolean hasExamId() { return iExamId != null; }
		public Long getExamId() { return iExamId; }
		public void setExamId(Long examId) { iExamId = examId; }

		public boolean hasStudentId() { return iStudentId != null; }
		public Long getStudentId() { return iStudentId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		
		@Override
		public String toString() {
			return (hasExamId() ? getExamId().toString() : "NULL") + "," + (hasStudentId() ? getStudentId().toString() : "NULL");
		}
		
		public static ExaminationScheduleRpcRequest getScheduleForStudent(Long examId, Long studentId) {
			ExaminationScheduleRpcRequest request = new ExaminationScheduleRpcRequest();
			request.setExamId(examId);
			request.setStudentId(studentId);
			return request;
		}
	}
	
	public static class ExaminationScheduleRpcResponse implements GwtRpcResponse {
		private String iExamType;
		private TreeSet<EventInterface.RelatedObjectInterface> iExams;
		
		public ExaminationScheduleRpcResponse() {}
		
		public String getExamType() { return iExamType; }
		public void setExamType(String examType) { iExamType = examType; }
		
		public boolean hasExams() { return iExams != null && !iExams.isEmpty(); }
		public void addExam(EventInterface.RelatedObjectInterface exam) {
			if (iExams == null) iExams = new TreeSet<EventInterface.RelatedObjectInterface>();
			iExams.add(exam);
		}
		public TreeSet<EventInterface.RelatedObjectInterface> getExams() { return iExams; }
		
	}
	
	public static class RelatedObjectComparator implements Comparator<EventInterface.RelatedObjectInterface> {
		private int iColumn;
		
		public RelatedObjectComparator(int column) {
			iColumn = column;
		}
		
		private String course(RelatedObjectInterface obj) {
			String course = "";
			if (obj.hasCourseNames()) {
				for (String cn: obj.getCourseNames()) {
					if (course.isEmpty()) {
						course += cn;
					} else {
						course += "," + cn;
					}
				}
			} else {
				course = obj.getName();
			}
			return course;
		}
		
		private String section(RelatedObjectInterface obj) {
			String section = "";
			if (obj.hasExternalIds()) {
				for (String ex: obj.getExternalIds()) {
					if (section.isEmpty()) {
						section += ex;
					} else {
						section += "," + ex;
					}
				}
			} else if (obj.hasSectionNumber()) {
				section = obj.getSectionNumber();
			}
			return section;
		}
		
		private String type(RelatedObjectInterface obj) {
			return (obj.hasInstruction() ? obj.getInstruction() : obj.getType().name());
		}
		
		private String title(RelatedObjectInterface obj) {
			String title = "";
			if (obj.hasCourseTitles()) {
				String last = null;
				for (String ct: obj.getCourseTitles()) {
					if (last != null && !last.isEmpty() && last.equals(ct))
						ct = "";
					else
						last = ct;
					if (title.isEmpty()) {
						title += ct;
					} else {
						title += "," + ct;
					}
				}
			} else {
				title = "";
			}
			return title;
		}
		
		private String location(RelatedObjectInterface obj) {
			String location = "";
			if (obj.hasLocations()) {
				for (ResourceInterface loc: obj.getLocations()) {
					location += (location.isEmpty() ? "" : "<br>") + loc.getName();
				}
			}
			return location;
		}
		
		private int compare(int column, RelatedObjectInterface o1, RelatedObjectInterface o2) {
			switch (column) {
			case 0:
				return course(o1).compareTo(course(o2));
			case 1:
				return section(o1).compareTo(section(o2));
			case 2:
				return type(o1).compareTo(type(o2));
			case 3:
				return title(o1).compareTo(title(o2));
			case 4:
				int cmp = (o1.hasDayOfYear() ? o1.getDayOfYear() : new Integer(-1)).compareTo(o2.hasDayOfYear() ? o2.getDayOfYear() : new Integer(-1));
				if (cmp != 0) return cmp;
			case 5:
				cmp = (o1.hasStartSlot() ? o1.getStartSlot() : new Integer(-1)).compareTo(o2.hasStartSlot() ? o2.getStartSlot() : new Integer(-1));
				if (cmp != 0) return cmp;
				return (o1.hasEndSlot() ? o1.getEndSlot() : new Integer(-1)).compareTo(o2.hasEndSlot() ? o2.getEndSlot() : new Integer(-1));
			case 6:
				return location(o1).compareTo(location(o2));
			case 7:
				return (o1.hasInstructors() ? o1.getInstructorNames(",", MESSAGES) : "").compareToIgnoreCase(o2.hasInstructors() ? o2.getInstructorNames(",", MESSAGES) : "");
			case 8:
				return (o1.hasConflicts() ? o1.getConflicts() : "").compareToIgnoreCase(o2.hasConflicts() ? o2.getConflicts() : "");
			default:
				return 0;
			}
		}
		
		@Override
		public int compare(RelatedObjectInterface o1, RelatedObjectInterface o2) {
			int cmp = compare(iColumn, o1, o2);
			if (cmp != 0) return cmp;
			cmp = course(o1).compareTo(course(o2));
			if (cmp != 0) return cmp;
			return o1.compareTo(o2);
		}
	}
	
}
