/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
import org.unitime.timetable.gwt.shared.ReservationInterface.Area;
import org.unitime.timetable.gwt.shared.ReservationInterface.CurriculumReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.GroupReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.IdName;
import org.unitime.timetable.gwt.shared.ReservationInterface.IndividualReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideReservation;
import org.unitime.timetable.gwt.shared.ReservationInterface.ReservationFilterRpcRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
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


	private void populate(List<ReservationInterface> reservations) {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		if (iOfferingId == null) {
			final UniTimeTableHeader hOffering = new UniTimeTableHeader(MESSAGES.colInstructionalOffering());
			hOffering.setWidth("100px");
			header.add(hOffering);
			hOffering.addOperation(new Operation() {
				@Override
				public void execute() {
					iReservations.sort(hOffering, new Comparator<ReservationInterface>() {
						@Override
						public int compare(ReservationInterface r1, ReservationInterface r2) {
							return r1.compareTo(r2);
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
					return MESSAGES.opSortBy(MESSAGES.fieldInstructionalOffering());
				}
			});
		}

		final UniTimeTableHeader hType = new UniTimeTableHeader(MESSAGES.colReservationType());
		hType.setWidth("100px");
		hType.addOperation(new Operation() {
			@Override
			public void execute() {
				iReservations.sort(hType, new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = new Integer(r1.getPriority()).compareTo(r2.getPriority());
						if (cmp != 0) return cmp;
						return r1.compareTo(r2);
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
				return MESSAGES.opSortBy(MESSAGES.fieldReservationType());
			}
		});
		header.add(hType);
		
		final UniTimeTableHeader hOwner = new UniTimeTableHeader(MESSAGES.colOwner());
		hOwner.setWidth("250px");
		header.add(hOwner);
		hOwner.addOperation(new Operation() {
			@Override
			public void execute() {
				iReservations.sort(hOwner, new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = new Integer(r1.getPriority()).compareTo(r2.getPriority());
						if (cmp != 0) return cmp;
						cmp = r1.toString().compareTo(r2.toString());
						if (cmp != 0) return cmp;
						return r1.compareTo(r2);
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
				return MESSAGES.opSortBy(MESSAGES.colOwner());
			}
		});

		final UniTimeTableHeader hRestrict = new UniTimeTableHeader(MESSAGES.colRestrictions());
		hRestrict.setWidth("160px");
		hRestrict.addOperation(new Operation() {
			@Override
			public void execute() {
				iReservations.sort(hRestrict, new Comparator<ReservationInterface>() {
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
				return MESSAGES.opSortBy(MESSAGES.colRestrictions());
			}
		});
		header.add(hRestrict);

		final UniTimeTableHeader hLimit = new UniTimeTableHeader(MESSAGES.colReservedSpace());
		hLimit.setWidth("80px");
		header.add(hLimit);
		hLimit.addOperation(new Operation() {
			@Override
			public void execute() {
				iReservations.sort(hLimit, new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = new Integer(r1.getLimit() == null ? Integer.MAX_VALUE : r1.getLimit()).compareTo(r2.getLimit() == null ? Integer.MAX_VALUE : r2.getLimit());
						if (cmp != 0) return -cmp;
						return r1.compareTo(r2);
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
				return MESSAGES.opSortBy(MESSAGES.fieldReservedSpace());
			}
		});

		final UniTimeTableHeader hLastLike = new UniTimeTableHeader(MESSAGES.colLastLikeEnrollment());
		hLastLike.setWidth("80px");
		header.add(hLastLike);
		hLastLike.addOperation(new Operation() {
			@Override
			public void execute() {
				iReservations.sort(hLastLike, new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = new Integer(r1.getLastLike() == null ? -1 : r1.getLastLike()).compareTo(r2.getLastLike() == null ? -1 : r2.getLastLike());
						if (cmp != 0) return -cmp;
						return r1.compareTo(r2);
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
				return MESSAGES.opSortBy(MESSAGES.fieldLastLikeEnrollment());
			}
		});

		final UniTimeTableHeader hProjected = new UniTimeTableHeader(MESSAGES.colProjectedByRule());
		hProjected.setWidth("80px");
		header.add(hProjected);
		hProjected.addOperation(new Operation() {
			@Override
			public void execute() {
				iReservations.sort(hProjected, new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = new Integer(r1.getProjection() == null ? -1 : r1.getProjection()).compareTo(r2.getProjection() == null ? -1 : r2.getProjection());
						if (cmp != 0) return -cmp;
						return r1.compareTo(r2);
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
				return MESSAGES.opSortBy(MESSAGES.fieldProjectedByRule());
			}
		});

		final UniTimeTableHeader hEnrollment = new UniTimeTableHeader(MESSAGES.colCurrentEnrollment());
		hEnrollment.setWidth("80px");
		header.add(hEnrollment);
		hEnrollment.addOperation(new Operation() {
			@Override
			public void execute() {
				iReservations.sort(hEnrollment, new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = new Integer(r1.getEnrollment() == null ? -1 : r1.getEnrollment()).compareTo(r2.getEnrollment() == null ? -1 : r2.getEnrollment());
						if (cmp != 0) return -cmp;
						return r1.compareTo(r2);
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
				return MESSAGES.opSortBy(MESSAGES.fieldCurrentEnrollment());
			}
		});
		
		final UniTimeTableHeader hExpiration = new UniTimeTableHeader(MESSAGES.colExpirationDate());
		hExpiration.setWidth("80px");
		header.add(hExpiration);
		hExpiration.addOperation(new Operation() {
			@Override
			public void execute() {
				iReservations.sort(hExpiration, new Comparator<ReservationInterface>() {
					@Override
					public int compare(ReservationInterface r1, ReservationInterface r2) {
						int cmp = new Long(r1.getExpirationDate() == null ? Long.MAX_VALUE : r1.getExpirationDate().getTime()).compareTo(r2.getExpirationDate() == null ? Long.MAX_VALUE : r2.getExpirationDate().getTime());
						if (cmp != 0) return cmp;
						return r1.compareTo(r2);
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
				return MESSAGES.opSortBy(MESSAGES.fieldExpirationDate());
			}
		});
		
		iReservations.addRow(null, header);
		
		int total = 0, lastLike = 0, projection = 0, enrollment = 0;
		boolean unlimited = false;
		for (ReservationInterface reservation: reservations) {
			List<Widget> line = new ArrayList<Widget>();
			if (iOfferingId == null) {
				VerticalPanel courses = new VerticalPanel();
				courses.add(new Label(reservation.getOffering().getAbbv(), false));
				for (Course course: reservation.getOffering().getCourses()) {
					if (course.getAbbv().equals(reservation.getOffering().getAbbv())) continue;
					Label l = new Label(course.getAbbv(), false);
					l.getElement().getStyle().setMarginLeft(10, Unit.PX);
					l.getElement().getStyle().setColor("gray");
					courses.add(l);
				}
				if (reservation.isExpired())
					courses.addStyleName("unitime-Disabled");
				if (reservation.isEditable())
					courses.addStyleName("unitime-Editable");
				line.add(courses);

			}
			
			Integer limit = reservation.getLimit();
			if (reservation instanceof CourseReservation) {
				line.add(new Label(MESSAGES.reservationCourseAbbv()));
				Course course = ((CourseReservation) reservation).getCourse();
				limit = course.getLimit();
				line.add(new Label(course.getAbbv(), false));
			} else if (reservation instanceof IndividualReservation) {
				if (reservation instanceof OverrideReservation) {
					line.add(new Label(CONSTANTS.reservationOverrideTypeAbbv()[((OverrideReservation)reservation).getType().ordinal()]));
				} else {
					line.add(new Label(MESSAGES.reservationIndividualAbbv()));
				}
				VerticalPanel students = new VerticalPanel();
				limit = ((IndividualReservation) reservation).getStudents().size();
				for (IdName student: ((IndividualReservation) reservation).getStudents()) {
					students.add(new Label(student.getName(), false));
				}
				if (reservation.isExpired())
					students.addStyleName("unitime-Disabled");
				if (reservation.isEditable())
					students.addStyleName("unitime-Editable");
				line.add(students);
			} else if (reservation instanceof GroupReservation) {
				line.add(new Label(MESSAGES.reservationStudentGroupAbbv()));
				IdName group = ((GroupReservation) reservation).getGroup();
				line.add(new Label(group.getAbbv() + " - " + group.getName() + " (" + group.getLimit() + ")", false));				
			} else if (reservation instanceof CurriculumReservation) {
				line.add(new Label(MESSAGES.reservationCurriculumAbbv()));
				Area curriculum = ((CurriculumReservation) reservation).getCurriculum();
				VerticalPanel owner = new VerticalPanel();
				owner.add(new Label(curriculum.getAbbv() + " - " + curriculum.getName()));
				for (IdName clasf: curriculum.getClassifications()) {
					Label l = new Label(clasf.getAbbv() + " - " + clasf.getName());
					l.getElement().getStyle().setMarginLeft(10, Unit.PX);
					owner.add(l);
				}
				for (IdName major: curriculum.getMajors()) {
					Label l = new Label(major.getAbbv() + " - " + major.getName());
					l.getElement().getStyle().setMarginLeft(10, Unit.PX);
					owner.add(l);
				}
				if (reservation.isExpired())
					owner.addStyleName("unitime-Disabled");
				if (reservation.isEditable())
					owner.addStyleName("unitime-Editable");
				line.add(owner);
			} else {
				line.add(new Label(MESSAGES.reservationUnknownAbbv()));
				line.add(new Label());
			}
			VerticalPanel restrictions = new VerticalPanel();
			for (Config config: reservation.getConfigs()) {
				restrictions.add(new Label(MESSAGES.selectionConfiguration(config.getName(), config.getLimit() == null ? MESSAGES.configUnlimited() : config.getLimit().toString())));
			}
			for (Clazz clazz: reservation.getClasses()) {
				restrictions.add(new Label(clazz.getName() + " (" + clazz.getLimit() + ")", false));
			}
			line.add(restrictions);
			if (reservation.isExpired())
				restrictions.addStyleName("unitime-Disabled");
			if (reservation.isEditable())
				restrictions.addStyleName("unitime-Editable");
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

			if (reservation instanceof OverrideReservation && !((OverrideReservation)reservation).getType().isCanHaveExpirationDate())
				line.add(new Label(MESSAGES.reservationOverrideAbbv()));
			else
				line.add(new Label(reservation.getExpirationDate() == null ? "" : sDF.format(reservation.getExpirationDate())));
			iReservations.addRow(reservation, line);
			iReservations.getRowFormatter().setVerticalAlign(iReservations.getRowCount() - 1, HasVerticalAlignment.ALIGN_TOP);
			if (reservation.isExpired())
				iReservations.getRowFormatter().addStyleName(iReservations.getRowCount() - 1, "unitime-Disabled");
			if (reservation.isEditable())
				iReservations.getRowFormatter().addStyleName(iReservations.getRowCount() - 1, "unitime-Editable");
		}
		
		if (iOfferingId != null) {
			List<Widget> footer = new ArrayList<Widget>();
			footer.add(new TotalLabel(MESSAGES.totalReservedSpace(), 3)); 
			footer.add(new TotalNumber(unlimited ? MESSAGES.infinity() : String.valueOf(total)));
			footer.add(new TotalNumber(lastLike <= 0 ? "" : String.valueOf(lastLike)));
			footer.add(new TotalNumber(projection <= 0 ? "" : String.valueOf(projection)));
			footer.add(new TotalNumber(enrollment <= 0 ? "" : String.valueOf(enrollment)));
			footer.add(new TotalLabel("&nbsp;", 1));
			iReservations.addRow(null, footer);
		} else if (reservations.isEmpty()) {
			iHeader.setErrorMessage(MESSAGES.errorNoMatchingReservation());
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
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=reservation&id=" + evt.getReservation().getId());
				
			}
		});
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

	public void query(ReservationFilterRpcRequest filter, final Command next) {
		iLastQuery = filter;
		clear(true);
		iReservationService.findReservations(filter, new AsyncCallback<List<ReservationInterface>>() {
			
			@Override
			public void onSuccess(List<ReservationInterface> result) {
				populate(result);
				iHeader.clearMessage();
				if (next != null)
					next.execute();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToLoadReservations(caught.getMessage()));
				ToolBox.checkAccess(caught);
				if (next != null)
					next.execute();
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
}
