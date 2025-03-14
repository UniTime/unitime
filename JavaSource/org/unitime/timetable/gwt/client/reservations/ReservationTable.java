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
package org.unitime.timetable.gwt.client.reservations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasStyleName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.ReservationService;
import org.unitime.timetable.gwt.services.ReservationServiceAsync;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface.Clazz;
import org.unitime.timetable.gwt.shared.ReservationInterface.Config;
import org.unitime.timetable.gwt.shared.ReservationInterface.Course;
import org.unitime.timetable.gwt.shared.ReservationInterface.CourseReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.Areas;
import org.unitime.timetable.gwt.shared.ReservationInterface.CurriculumReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.GroupReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.IndividualReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.LCReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.ReservationFilterRpcRequest;
import org.unitime.timetable.gwt.shared.ReservationInterface.UniversalReservation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class ReservationTable extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private final ReservationServiceAsync iReservationService = GWT.create(ReservationService.class);
	private static DateTimeFormat sDF = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private Long iOfferingId = null;
	
	private SimpleForm iReservationPanel;
	private UniTimeTable<ReservationInterface> iReservations;
	private UniTimeHeaderPanel iHeader;
	
	private AsyncCallback<List<ReservationInterface>> iLoadCallback = null;
	
	private List<ReservationClickHandler> iReservationClickHandlers = new ArrayList<ReservationClickHandler>();
	
	private ReservationFilterRpcRequest iLastQuery = null;

	public ReservationTable(boolean editable, boolean showHeader) {
		iReservationPanel = new SimpleForm();
		iReservationPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iHeader = new UniTimeHeaderPanel(showHeader ? MESSAGES.sectReservations() : "");
		iHeader.setCollapsible(showHeader ? ReservationCookie.getInstance().getReservationCoursesDetails() : null);
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ReservationCookie.getInstance().setReservationCoursesDetails(event.getValue());
				if (iReservations.getRowCount() == 0)
					refresh();
				else if (iReservations.getRowCount() > 2) {
					for (int row = 1; row < iReservations.getRowCount() - 1; row++) {
						iReservations.getRowFormatter().setVisible(row, event.getValue());
					}
				}
			}
		});
		
		if (showHeader) {
			iReservationPanel.addHeaderRow(iHeader);
			iHeader.getElement().getStyle().setMarginTop(10, Unit.PX);
			if (editable) {
				iHeader.addButton("add", MESSAGES.buttonAddReservation(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=reservation&offering=" + iOfferingId);
					}
				});
			}
		}

		
		iReservations = new UniTimeTable<ReservationInterface>();
		iReservationPanel.addRow(iReservations);
		

		if (!showHeader)
			iReservationPanel.addRow(iHeader);

		initWidget(iReservationPanel);
		
		if (editable) {
			iReservations.addMouseClickListener(new MouseClickListener<ReservationInterface>() {
				@Override
				public void onMouseClick(TableEvent<ReservationInterface> event) {
					if (event.getData() != null && event.getData().isEditable()) {
						ReservationClickedEvent e = new ReservationClickedEvent(event.getData());
						for (ReservationClickHandler h: iReservationClickHandlers) {
							h.onClick(e);
						}
					}
				}
			});
		}
	}
	
	private void initCallbacks() {
		if (iLoadCallback == null) {
			iLoadCallback = new AsyncCallback<List<ReservationInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
					iHeader.setErrorMessage(MESSAGES.failedToLoadReservations(caught.getMessage()));
					iHeader.setCollapsible(null);
					ReservationCookie.getInstance().setReservationCoursesDetails(false);
				}
				@Override
				public void onSuccess(List<ReservationInterface> result) {
					if (result.isEmpty()) {
						iHeader.setMessage(MESSAGES.hintOfferingHasNoReservations());
						iHeader.setCollapsible(null);
					} else {
						populate(result);
						if (iReservations.getRowCount() > 2) {
							for (int row = 1; row < iReservations.getRowCount() - 1; row++) {
								iReservations.getRowFormatter().setVisible(row, ReservationCookie.getInstance().getReservationCoursesDetails());
							}
						}
						iHeader.clearMessage();
					}
				}
			};			
		}
	}
	
	private void refresh() {
		clear(true);
		if (iOfferingId != null) {
			iReservationService.getReservations(iOfferingId, iLoadCallback);
		} else {
			query(iLastQuery, null);
		}
	}
	
	private void clear(boolean loading) {
		for (int row = iReservations.getRowCount() - 1; row >= 0; row--) {
			iReservations.removeRow(row);
		}
		iReservations.clear(true);
		if (loading)
			iHeader.showLoading();
		else
			iHeader.clearMessage();
	}
	
	public void populate(List<ReservationInterface> reservations) {
		populate(reservations, -1);
	}


	public void populate(List<ReservationInterface> reservations, int owLimit) {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		for (final ReservationColumn column: ReservationColumn.values()) {
			if (column == ReservationColumn.OFFERING && iOfferingId != null) continue;
			final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column));
			h.setWidth(getColumnWidth(column));
			final Comparator<ReservationInterface> cmp = column.getComparator();
			if (cmp != null) {
				h.addOperation(new Operation() {
					@Override
					public void execute() {
						iReservations.sort(h, cmp);
						ReservationCookie.getInstance().setSortBy(h.getOrder() ? 1 + column.ordinal() : -1 - column.ordinal()); 
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
						return MESSAGES.opSortBy(getFieldName(column));
					}
				});
			}
			header.add(h);
		}
		
		iReservations.addRow(null, header);
		
		int total = 0, lastLike = 0, projection = 0, enrollment = 0;
		boolean unlimited = false;
		for (ReservationInterface reservation: reservations) {
			List<Widget> line = new ArrayList<Widget>();
			if (iOfferingId == null) {
				VerticalPanel courses = new VerticalPanel();
				if (reservation instanceof CourseReservation) {
					Course course = ((CourseReservation) reservation).getCourse();
					courses.add(new Label(course.getAbbv(), false));
				} else if (reservation instanceof LCReservation) {
					Course course = ((LCReservation) reservation).getCourse();
					courses.add(new Label(course.getAbbv(), false));
				} else {
					courses.add(new Label(reservation.getOffering().getAbbv(), false));
					for (Course course: reservation.getOffering().getCourses()) {
						if (course.getAbbv().equals(reservation.getOffering().getAbbv())) continue;
						Label l = new Label(course.getAbbv(), false);
						l.getElement().getStyle().setMarginLeft(10, Unit.PX);
						l.getElement().getStyle().setColor("gray");
						courses.add(l);
					}
				}
				line.add(courses);

			}
			
			String flags = "";
			if (reservation.isAllowOverlaps())
				flags += "\n  " + MESSAGES.checkCanOverlap();
			if (reservation.isOverLimit())
				flags += "\n  " + MESSAGES.checkCanOverLimit();
			if (reservation.isMustBeUsed())
				flags += "\n  " + MESSAGES.checkMustBeUsed();
			if (reservation.isAlwaysExpired())
				flags += "\n  " + MESSAGES.checkAllwaysExpired();
			Integer limit = reservation.getReservationLimit();
			if (reservation instanceof CourseReservation) {
				Label label = new Label(MESSAGES.reservationCourseAbbv() + flags);
				label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
				line.add(label);
				Course course = ((CourseReservation) reservation).getCourse();
				limit = course.getLimit();
				line.add(new Label(course.getAbbv(), false));
			} else if (reservation instanceof IndividualReservation) {
				if (reservation.isOverride()) {
					Label label = new Label(MESSAGES.reservationIndividualOverrideAbbv() + flags);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				} else if (reservation instanceof OverrideReservation) {
					String type = CONSTANTS.reservationOverrideTypeAbbv()[((OverrideReservation)reservation).getType().ordinal()];
					Label label = new Label(type == null ? ((OverrideReservation)reservation).getType().name() + flags : type);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				} else {
					Label label = new Label(MESSAGES.reservationIndividualAbbv() + flags);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				}
				VerticalPanel students = new VerticalPanel();
				if (limit == null)
					limit = ((IndividualReservation) reservation).getStudents().size();
				for (IdName student: ((IndividualReservation) reservation).getStudents()) {
					students.add(new Label(student.getName(), false));
				}
				if (owLimit > 0 && students.getWidgetCount() > owLimit + 1) {
					int more = students.getWidgetCount() - owLimit;
					while (students.getWidgetCount() > owLimit)
						students.remove(owLimit);
					Label l = new Label(MESSAGES.moreItems(more), false); l.getElement().getStyle().setMarginLeft(10, Unit.PX);
					students.add(l);
				}
				line.add(students);
			} else if (reservation instanceof GroupReservation) {
				if (reservation.isOverride()) {
					Label label = new Label(MESSAGES.reservationStudentGroupOverrideAbbv() + flags);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				} else {
					Label label = new Label(MESSAGES.reservationStudentGroupAbbv() + flags);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				}
				IdName group = ((GroupReservation) reservation).getGroup();
				line.add(new Label(group.getAbbv() + " - " + group.getName() + " (" + group.getLimit() + ")", false));
			} else if (reservation instanceof LCReservation) {
				if (reservation.getOffering().getCourses().size() > 1 && iOfferingId != null) {
					Course course = ((LCReservation) reservation).getCourse();
					Label label = new Label(MESSAGES.reservationLearningCommunityAbbv() + "\n  " + course.getAbbv() + flags);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				} else {
					Label label = new Label(MESSAGES.reservationLearningCommunityAbbv() + flags);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				}
				IdName group = ((LCReservation) reservation).getGroup();
				line.add(new Label(group.getAbbv() + " - " + group.getName() + " (" + group.getLimit() + ")", false));
			} else if (reservation instanceof CurriculumReservation) {
				if (reservation.isOverride()) {
					Label label = new Label(MESSAGES.reservationCurriculumOverride() + flags);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				} else {
					Label label = new Label(MESSAGES.reservationCurriculumAbbv() + flags);
					label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					line.add(label);
				}
				Areas curriculum = ((CurriculumReservation) reservation).getCurriculum();
				VerticalPanel owner = new VerticalPanel();
				for (IdName area: curriculum.getAreas()) {
					Label l = new Label(area.getAbbv() + " - " + area.getName());
					owner.add(l);
					for (IdName major: curriculum.getMajors()) {
						if (!area.getId().equals(major.getParentId())) continue;
						Label o = new Label(major.getAbbv() + " - " + major.getName());
						o.getElement().getStyle().setMarginLeft(10, Unit.PX);
						owner.add(o);
						for (IdName conc: curriculum.getConcentrations()) {
							if (!major.getId().equals(conc.getParentId())) continue;
							Label c = new Label(conc.getAbbv() + " - " + conc.getName());
							c.getElement().getStyle().setMarginLeft(20, Unit.PX);
							owner.add(c);
						}
					}
					for (IdName minor: curriculum.getMinors()) {
						if (!area.getId().equals(minor.getParentId())) continue;
						Label o = new Label(minor.getAbbv() + " - " + minor.getName());
						o.getElement().getStyle().setMarginLeft(10, Unit.PX);
						owner.add(o);
					}
				}
				boolean firstClasf = true;
				for (IdName clasf: curriculum.getClassifications()) {
					Label l = new Label(clasf.getAbbv() + " - " + clasf.getName());
					if (curriculum.getAreas().size() == 1)
						l.getElement().getStyle().setMarginLeft(10, Unit.PX);
					else if (firstClasf) {
						l.getElement().getStyle().setMarginTop(2, Unit.PX);
						firstClasf = false;
					}
					owner.add(l);
				}
				for (IdName major: curriculum.getMajors()) {
					if (major.getParentId() != null) continue;
					Label l = new Label(major.getAbbv() + " - " + major.getName());
					l.getElement().getStyle().setMarginLeft(10, Unit.PX);
					owner.add(l);
					for (IdName conc: curriculum.getConcentrations()) {
						if (!major.getId().equals(conc.getParentId())) continue;
						Label c = new Label(conc.getAbbv() + " - " + conc.getName());
						c.getElement().getStyle().setMarginLeft(20, Unit.PX);
						owner.add(c);
					}
				}
				for (IdName minor: curriculum.getMinors()) {
					if (minor.getParentId() != null) continue;
					Label l = new Label(minor.getAbbv() + " - " + minor.getName());
					l.getElement().getStyle().setMarginLeft(10, Unit.PX);
					owner.add(l);
				}
				if (owLimit > 0 && owner.getWidgetCount() > owLimit + 1) {
					int more = owner.getWidgetCount() - owLimit;
					while (owner.getWidgetCount() > owLimit)
						owner.remove(owLimit);
					Label l = new Label(MESSAGES.moreItems(more), false); l.getElement().getStyle().setMarginLeft(10, Unit.PX);
					owner.add(l);
				}
				line.add(owner);
			} else if (reservation instanceof UniversalReservation) {
				Label label = new Label(MESSAGES.reservationUniversalOverrideAbbv() + flags);
				label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
				line.add(label);
				String filter = ((UniversalReservation) reservation).getFilter();
				line.add(new Label(filter == null ? "" : filter, false));
			} else {
				Label label = new Label(MESSAGES.reservationUnknownAbbv() + flags);
				label.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
				line.add(label);
				line.add(new Label());
			}
			VerticalPanel restrictions = new VerticalPanel();
			for (Config config: reservation.getConfigs()) {
				restrictions.add(new Label(MESSAGES.selectionConfiguration(config.getName(), config.getLimit() == null ? MESSAGES.configUnlimited() : config.getLimit().toString())));
			}
			for (Clazz clazz: reservation.getClasses()) {
				restrictions.add(new Label(clazz.getName() + (clazz.getLimit() == null ? "" : " (" + clazz.getLimit() + ")"), false));
			}
			if (owLimit > 0 && restrictions.getWidgetCount() > owLimit + 1) {
				int more = restrictions.getWidgetCount() - owLimit;
				while (restrictions.getWidgetCount() > owLimit)
					restrictions.remove(owLimit);
				Label l = new Label(MESSAGES.moreItems(more), false); l.getElement().getStyle().setMarginLeft(10, Unit.PX);
				restrictions.add(l);
			}
			line.add(restrictions);
			if (reservation.hasInclusive() && reservation.isInclusive()) {
				restrictions.addStyleName("unitime-ReservationInclusive");
				restrictions.setTitle(MESSAGES.reservationInclusiveTrue());
			} else if (reservation.hasInclusive() && !reservation.isInclusive()) {
				restrictions.addStyleName("unitime-ReservationExclusive");
				restrictions.setTitle(MESSAGES.reservationInclusiveFalse());
			}
			line.add(new Number(limit == null ? MESSAGES.infinity() : String.valueOf(limit)));
			if (limit == null)
				unlimited = true;
			else
				total += limit;
			
			if (reservation.getLastLike() != null) {
				line.add(new Number(reservation.getLastLike().toString()));
				lastLike += reservation.getLastLike();
			} else {
				line.add(new Label(""));
			}
			
			if (reservation.getProjection() != null) {
				line.add(new Number(reservation.getProjection().toString()));
				projection += reservation.getProjection();
			} else {
				line.add(new Label(""));
			}

			
			if (reservation.getEnrollment() != null) {
				line.add(new Number(reservation.getEnrollment().toString()));
				enrollment += reservation.getEnrollment();
			} else {
				line.add(new Label(""));
			}

			if (reservation instanceof OverrideReservation && !((OverrideReservation)reservation).getType().isCanHaveExpirationDate()) {
				line.add(new Label(MESSAGES.reservationOverrideAbbv()));
				line.add(new Label(MESSAGES.reservationOverrideAbbv()));
			} else if (reservation.isOverride() && reservation.isAlwaysExpired()) {
				line.add(new Label(MESSAGES.reservationOverrideAbbv()));
				line.add(new Label(MESSAGES.reservationOverrideAbbv()));
			} else {
				line.add(new Label(reservation.getStartDate() == null ? "" : sDF.format(reservation.getStartDate())));
				line.add(new Label(reservation.getExpirationDate() == null ? "" : sDF.format(reservation.getExpirationDate())));
			}
			iReservations.addRow(reservation, line);
			iReservations.getRowFormatter().setVerticalAlign(iReservations.getRowCount() - 1, HasVerticalAlignment.ALIGN_TOP);
			if (reservation.isExpired())
				for (Widget w: line)
					w.addStyleName("unitime-ReservationExpired");
			if (reservation.isEditable())
				for (Widget w: line)
					w.addStyleName("unitime-Editable");
			else
				for (Widget w: line)
					w.addStyleName("unitime-ReservationDisabled");
		}
		
		if (iOfferingId != null) {
			List<Widget> footer = new ArrayList<Widget>();
			footer.add(new TotalLabel(MESSAGES.totalReservedSpace(), 3)); 
			footer.add(new TotalNumber(unlimited ? MESSAGES.infinity() : String.valueOf(total)));
			footer.add(new TotalNumber(lastLike <= 0 ? "" : String.valueOf(lastLike)));
			footer.add(new TotalNumber(projection <= 0 ? "" : String.valueOf(projection)));
			footer.add(new TotalNumber(enrollment <= 0 ? "" : String.valueOf(enrollment)));
			footer.add(new TotalLabel("&nbsp;", 1));
			footer.add(new TotalLabel("&nbsp;", 1));
			iReservations.addRow(null, footer);
		} else if (reservations.isEmpty()) {
			iHeader.setErrorMessage(MESSAGES.errorNoMatchingReservation());
		}
		
		int sortBy = ReservationCookie.getInstance().getSortBy();
		if (sortBy != 0) {
			int col = Math.abs(sortBy) - 1;
			ReservationColumn column = ReservationColumn.values()[col];
			Comparator<ReservationInterface> cmp = column.getComparator();
			if (cmp != null) {
				boolean asc = sortBy > 0;
				UniTimeTableHeader h = null;
				if (iOfferingId == null)
					h = header.get(col);
				else if (Math.abs(sortBy) > 1)
					h = header.get(col - 1);
				iReservations.sort(h, cmp, asc);
			}
		}
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
	
	private static class TotalNumber extends Number implements HasStyleName {
		public TotalNumber(String text) {
			super(text);
		}

		@Override
		public String getStyleName() {
			return "unitime-TotalRow";
		}
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
	
	public void insert(final RootPanel panel) {
		initCallbacks();
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		if (ReservationCookie.getInstance().getReservationCoursesDetails()) {
			refresh();
		} else {
			clear(false);
			iHeader.clearMessage();
			iHeader.setCollapsible(false);
		}
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
		addReservationClickHandler(new ReservationClickHandler() {
			@Override
			public void onClick(ReservationClickedEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=reservation&id=" + evt.getReservation().getId() + "&reservations=" + getReservationIds());
			}
		});
	}
	
	public ReservationTable forOfferingId(Long offeringId) {
		initCallbacks();
		iOfferingId = offeringId;
		if (ReservationCookie.getInstance().getReservationCoursesDetails()) {
			refresh();
		} else {
			clear(false);
			iHeader.clearMessage();
			iHeader.setCollapsible(false);
		}
		addReservationClickHandler(new ReservationClickHandler() {
			@Override
			public void onClick(ReservationClickedEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=reservation&id=" + evt.getReservation().getId() + "&reservations=" + getReservationIds());
			}
		});
		return this;
	}

	public void scrollIntoView(Long reservationId) {
		for (int r = 1; r < iReservations.getRowCount(); r++) {
			if (iReservations.getData(r) != null && iReservations.getData(r).getId().equals(reservationId)) {
				iReservations.getRowFormatter().getElement(r).scrollIntoView();
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
	
	public void addReservationClickHandler(ReservationClickHandler h) {
		iReservationClickHandlers.add(h);
	}

	public void query(ReservationFilterRpcRequest filter, final AsyncCallback<List<ReservationInterface>> callback) {
		iLastQuery = filter;
		clear(true);
		iReservationService.findReservations(filter, new AsyncCallback<List<ReservationInterface>>() {
			
			@Override
			public void onSuccess(List<ReservationInterface> result) {
				populate(result);
				iHeader.clearMessage();
				if (callback != null)
					callback.onSuccess(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToLoadReservations(caught.getMessage()));
				ToolBox.checkAccess(caught);
				if (callback != null)
					callback.onFailure(caught);
			}
		});
	}
	
	public void select(Long curriculumId) {
		for (int i = 0; i < iReservations.getRowCount(); i++) {
			ReservationInterface r = iReservations.getData(i);
			if (r == null) continue;
			if (r.getId().equals(curriculumId))
				iReservations.getRowFormatter().setStyleName(i, "unitime-TableRowSelected");
			else if ("unitime-TableRowSelected".equals(iReservations.getRowFormatter().getStyleName(i)))
				iReservations.getRowFormatter().removeStyleName(i, "unitime-TableRowSelected");
				
		}
	}
	
	public void setErrorMessage(String message) {
		iHeader.setErrorMessage(message);
	}
	
	public ReservationInterface getNext(Long reservationId) {
		for (int row = 1; row < iReservations.getRowCount() - 1; row++) {
			ReservationInterface r = iReservations.getData(row);
			if (r != null && r.getId().equals(reservationId))
				return iReservations.getData(row + 1);
		}
		return null;
	}
	
	public ReservationInterface getPrevious(Long reservationId) {
		for (int row = 2; row < iReservations.getRowCount(); row++) {
			ReservationInterface r = iReservations.getData(row);
			if (r != null && r.getId().equals(reservationId))
				return iReservations.getData(row - 1);
		}
		return null;
	}
	
	public String getReservationIds() {
		String ret = "";
		for (int row = 1; row < iReservations.getRowCount(); row++) {
			ReservationInterface r = iReservations.getData(row);
			if (r != null && r.getId() != null)
				ret += (ret.isEmpty() ? "" : ",") + r.getId();
		}
		return ret;
	}
	
	public String getColumnWidth(ReservationColumn column) {
		switch (column) {
		case OFFERING:
		case TYPE:
			return "100px";
		case OWNER:
			return "250px";
		case RESTRICTIONS:
			return "160px";
		case RESERVED_SPACE:
		case LAST_LIKE:
		case PROJECTED_BY_RULE:
		case CURRENT_ENROLLMENT:
		case EXPIRATION_DATE:
		case START_DATE:
			return "80px";
		default:
			return null;
		}
	}
	
	public String getColumnName(ReservationColumn column) {
		switch (column) {
		case OFFERING:
			return MESSAGES.colInstructionalOffering();
		case TYPE:
			return MESSAGES.colReservationType();
		case OWNER:
			return MESSAGES.colOwner();
		case RESTRICTIONS:
			return MESSAGES.colRestrictions();
		case RESERVED_SPACE:
			return MESSAGES.colReservedSpace();
		case LAST_LIKE:
			return MESSAGES.colLastLikeEnrollment();
		case PROJECTED_BY_RULE:
			return MESSAGES.colProjectedByRule();
		case CURRENT_ENROLLMENT:
			return MESSAGES.colCurrentEnrollment();
		case EXPIRATION_DATE:
			return MESSAGES.colExpirationDate();
		case START_DATE:
			return MESSAGES.colStartDate();
		default:
			return null;
		}
	}
	
	public String getFieldName(ReservationColumn column) {
		switch (column) {
		case OFFERING:
			return MESSAGES.fieldInstructionalOffering();
		case TYPE:
			return MESSAGES.fieldReservationType();
		case OWNER:
			return MESSAGES.fieldOwner();
		case RESTRICTIONS:
			return MESSAGES.colRestrictions();
		case RESERVED_SPACE:
			return MESSAGES.fieldReservedSpace();
		case LAST_LIKE:
			return MESSAGES.fieldLastLikeEnrollment();
		case PROJECTED_BY_RULE:
			return MESSAGES.fieldProjectedByRule();
		case CURRENT_ENROLLMENT:
			return MESSAGES.fieldCurrentEnrollment();
		case EXPIRATION_DATE:
			return MESSAGES.fieldExpirationDate();
		case START_DATE:
			return MESSAGES.fieldStartDate();
		default:
			return null;
		}
	}
	
	public static enum ReservationColumn implements IsSerializable {
		OFFERING,
		TYPE,
		OWNER,
		RESTRICTIONS,
		RESERVED_SPACE,
		LAST_LIKE,
		PROJECTED_BY_RULE,
		CURRENT_ENROLLMENT,
		START_DATE,
		EXPIRATION_DATE,
		;
		
		public Comparator<ReservationInterface> getComparator() {
			switch (this) {
			case OFFERING:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						return r1.compareTo(r2);
					}
				};
			case TYPE:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = Integer.valueOf(r1.getPriority()).compareTo(r2.getPriority());
						if (cmp != 0) return cmp;
						return r1.compareTo(r2);
					}
				};
			case OWNER:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = Integer.valueOf(r1.getPriority()).compareTo(r2.getPriority());
						if (cmp != 0) return cmp;
						cmp = r1.toString().compareTo(r2.toString());
						if (cmp != 0) return cmp;
						return r1.compareTo(r2);
					}
				};
			case RESTRICTIONS:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = r1.getOffering().getAbbv().compareTo(r2.getOffering().getAbbv());
						if (cmp != 0) return cmp;
						cmp = r1.getConfigs().toString().compareTo(r2.getConfigs().toString());
						if (cmp != 0) return cmp;
						cmp = r1.getClasses().toString().compareTo(r2.getClasses().toString());
						if (cmp != 0) return cmp;
						return r1.compareTo(r2);
					}
				};
			case RESERVED_SPACE:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = Integer.valueOf(r1.getLimit() == null ? Integer.MAX_VALUE : r1.getLimit()).compareTo(r2.getLimit() == null ? Integer.MAX_VALUE : r2.getLimit());
						if (cmp != 0) return -cmp;
						return r1.compareTo(r2);
					}
				};
			case LAST_LIKE:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = Integer.valueOf(r1.getLastLike() == null ? -1 : r1.getLastLike()).compareTo(r2.getLastLike() == null ? -1 : r2.getLastLike());
						if (cmp != 0) return -cmp;
						return r1.compareTo(r2);
					}
				};
			case PROJECTED_BY_RULE:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = Integer.valueOf(r1.getProjection() == null ? -1 : r1.getProjection()).compareTo(r2.getProjection() == null ? -1 : r2.getProjection());
						if (cmp != 0) return -cmp;
						return r1.compareTo(r2);
					}
				};
			case CURRENT_ENROLLMENT:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = Integer.valueOf(r1.getEnrollment() == null ? -1 : r1.getEnrollment()).compareTo(r2.getEnrollment() == null ? -1 : r2.getEnrollment());
						if (cmp != 0) return -cmp;
						return r1.compareTo(r2);
					}
				};
			case EXPIRATION_DATE:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = Long.valueOf(r1.getExpirationDate() == null ? Long.MAX_VALUE : r1.getExpirationDate().getTime()).compareTo(r2.getExpirationDate() == null ? Long.MAX_VALUE : r2.getExpirationDate().getTime());
						if (cmp != 0) return cmp;
						return r1.compareTo(r2);
					}
				};
			case START_DATE:
				return new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = Long.valueOf(r1.getStartDate() == null ? Long.MIN_VALUE : r1.getStartDate().getTime()).compareTo(r2.getStartDate() == null ? Long.MIN_VALUE : r2.getStartDate().getTime());
						if (cmp != 0) return cmp;
						return r1.compareTo(r2);
					}
				};
			default:
				return null;
			}
		}
	}
	
	public UniTimeTable<ReservationInterface> getTable() { return iReservations; }
}
