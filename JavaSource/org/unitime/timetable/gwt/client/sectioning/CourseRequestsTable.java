/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
import java.util.Collection;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Tomas Muller
 */
public class CourseRequestsTable extends Composite {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private FlexTable iGrid;
	private AcademicSessionProvider iSessionProvider;
	private ArrayList<CourseSelectionBox[]> iCourses;
	private ArrayList<CourseSelectionBox[]> iAlternatives;
	private Label iTip;
	private boolean iOnline;

	public CourseRequestsTable(AcademicSessionProvider sessionProvider, boolean online) {
		iOnline = online;
		iSessionProvider = sessionProvider;
		
		iGrid = new FlexTable();
		iGrid.setStylePrimaryName("unitime-MainTable");
		iGrid.addStyleName("unitime-BottomLine");
		iGrid.setCellPadding(2);
		iGrid.setCellSpacing(0);
		
		int idx = 0;
		
		iGrid.getFlexCellFormatter().setColSpan(idx, 0, 4);
		iGrid.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		iGrid.getRowFormatter().setStyleName(idx, "unitime-MainTableHeaderRow");
		iGrid.setText(idx, 0, MESSAGES.courseRequestsCourses());
		
		iGrid.getFlexCellFormatter().setColSpan(idx, 1, 3);
		iGrid.getFlexCellFormatter().setStyleName(idx, 1, "unitime-MainTableHeaderNote");
		iGrid.setHTML(idx, 1, MESSAGES.courseRequestsWaitList());
		iGrid.getFlexCellFormatter().getElement(idx, 1).getStyle().setVerticalAlign(VerticalAlign.BOTTOM);
		
		idx++;
		
		iCourses = new ArrayList<CourseSelectionBox[]>();
		iAlternatives = new ArrayList<CourseSelectionBox[]>();

		for (int i=0; i<CONSTANTS.numberOfCourses(); i++) {
			iGrid.setText(idx, 0, MESSAGES.courseRequestsPriority(i+1));
			final CourseSelectionBox[] c = new CourseSelectionBox[] {
					new CourseSelectionBox(iSessionProvider, "c"+i, true, true),
					new CourseSelectionBox(iSessionProvider, "c"+i+"a", false, false),
					new CourseSelectionBox(iSessionProvider, "c"+i+"b", false, false)
			};
			c[0].setLabel(ARIA.titleRequestedCourse(1 + i), ARIA.altRequestedCourseFinder(1 + i));
			c[1].setLabel(ARIA.titleRequestedCourseFirstAlternative(1 + i), ARIA.altRequestedCourseFirstAlternativeFinder(1 + i));
			c[2].setLabel(ARIA.titleRequestedCourseSecondAlternative(1 + i), ARIA.altRequestedCourseSecondAlternativeFinder(1 + i));
			if (i < 9)
				c[0].setAccessKey((char)((int)'1'+i));
			else if (i == 9)
				c[0].setAccessKey('0');
			final AriaCheckBox ch = new AriaCheckBox();
			ch.setAriaLabel(ARIA.titleRequestedWaitList(1 + i));
			if (i>0) {
				final CourseSelectionBox[] x = iCourses.get(i - 1);
				for (int j=0; j<3; j++) {
					c[j].setPrev(x[j]);
					x[j].setNext(c[j]);
				}
				final ImageButton up = new ImageButton(RESOURCES.up(), RESOURCES.up_Down(), RESOURCES.up_Over());
				up.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapUp();
					}
				});
				iGrid.setWidget(idx, 5, up);
				up.setAltText(ARIA.altSwapCourseRequest(i + 1, i));
			}
			if (i<=CONSTANTS.numberOfCourses()) {
				final ImageButton down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
				down.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapDown();
					}
				});
				iGrid.setWidget(idx, 6, down);
				down.setAltText(i + 1 == CONSTANTS.numberOfCourses() ? ARIA.altSwapCourseAlternateRequest(i + 1, 1) : ARIA.altSwapCourseRequest(i + 1, i + 2));
			}
			c[0].setWidth("260px");
			c[1].setWidth("170px");
			c[2].setWidth("170px");
			iGrid.setWidget(idx, 1, c[0]);
			iGrid.setWidget(idx, 2, c[1]);
			iGrid.setWidget(idx, 3, c[2]);
			iGrid.setWidget(idx, 4, ch);
			iGrid.getRowFormatter().setVerticalAlign(idx, HasVerticalAlignment.ALIGN_TOP);
			iCourses.add(c);
			c[0].setWaitList(ch);
			idx++;
		}
		iCourses.get(1)[0].setHint(MESSAGES.courseRequestsHint1());
		iCourses.get(3)[0].setHint(MESSAGES.courseRequestsHint3());
		iCourses.get(4)[0].setHint(MESSAGES.courseRequestsHint4());
		iCourses.get(CONSTANTS.numberOfCourses()-1)[0].setHint(MESSAGES.courseRequestsHint8());

		iTip = new Label(CONSTANTS.tips()[(int)(Math.random() * CONSTANTS.tips().length)]);
		ToolBox.disableTextSelectInternal(iTip.getElement());
		iTip.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String oldText = iTip.getText();
				do {
					iTip.setText(CONSTANTS.tips()[(int)(Math.random() * CONSTANTS.tips().length)]);
				} while (oldText.equals(iTip.getText()));
			}
		});
		iGrid.getFlexCellFormatter().setColSpan(idx, 0, 7);
		iGrid.getFlexCellFormatter().setStyleName(idx, 0, "unitime-Hint");
		iGrid.setWidget(idx++, 0, iTip);

		iGrid.getFlexCellFormatter().setColSpan(idx, 0, 2);
		iGrid.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		iGrid.setText(idx, 0, MESSAGES.courseRequestsAlternatives());
		iGrid.getFlexCellFormatter().setColSpan(idx, 1, 5);
		iGrid.getFlexCellFormatter().setStyleName(idx, 1, "unitime-MainTableHeaderNote");
		iGrid.setHTML(idx, 1, MESSAGES.courseRequestsAlternativesNote());
		iGrid.getFlexCellFormatter().getElement(idx, 1).getStyle().setVerticalAlign(VerticalAlign.BOTTOM);
		idx++;

		for (int i=0; i<CONSTANTS.numberOfAlternatives(); i++) {
			iGrid.setText(idx, 0, MESSAGES.courseRequestsAlternative(i+1));
			final CourseSelectionBox[] c = new CourseSelectionBox[] {
					new CourseSelectionBox(iSessionProvider, "a"+i, true, false),
					new CourseSelectionBox(iSessionProvider, "a"+i+"a", false, false),
					new CourseSelectionBox(iSessionProvider, "a"+i+"b", false, false)
			};
			c[0].setLabel(ARIA.titleRequestedAlternate(1 + i, String.valueOf((char)((int)'a'+i))), ARIA.altRequestedAlternateFinder(1 + i));
			c[1].setLabel(ARIA.titleRequestedAlternateFirstAlternative(1 + i), ARIA.altRequestedAlternateFirstFinder(1 + i));
			c[2].setLabel(ARIA.titleRequestedAlternateSecondAlternative(1 + i), ARIA.altRequestedAlternateSecondFinder(1 + i));
			c[0].setAccessKey((char)((int)'a'+i));
			if (i>=0) {
				final CourseSelectionBox[] x = (i==0 ? iCourses.get(CONSTANTS.numberOfCourses() - 1) : iAlternatives.get(i - 1));
				for (int j=0; j<3; j++) {
					c[j].setPrev(x[j]);
					x[j].setNext(c[j]);
				}
				final ImageButton up = new ImageButton(RESOURCES.up(), RESOURCES.up_Down(), RESOURCES.up_Over());
				up.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapUp();
					}
				});
				iGrid.setWidget(idx, 4, up);
				up.setAltText(i == 0 ? ARIA.altSwapCourseAlternateRequest(CONSTANTS.numberOfCourses(), 1) : ARIA.altSwapAlternateRequest(i + 1, i));
			}
			if (i<CONSTANTS.numberOfAlternatives() - 1) {
				final ImageButton down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
				down.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapDown();
					}
				});
				iGrid.setWidget(idx, 5, down);
				down.setAltText(ARIA.altSwapAlternateRequest(i + 1, i + 2));
			}
			c[0].setWidth("260px");
			c[1].setWidth("170px");
			c[2].setWidth("170px");
			iGrid.setWidget(idx, 1, c[0]);
			iGrid.setWidget(idx, 2, c[1]);
			iGrid.setWidget(idx, 3, c[2]);
			iGrid.getRowFormatter().setVerticalAlign(idx, HasVerticalAlignment.ALIGN_TOP);
			iAlternatives.add(c);
			idx++;
		}
		iAlternatives.get(0)[0].setHint(MESSAGES.courseRequestsHintA0());
		
		iSessionProvider.addAcademicSessionChangeHandler(new AcademicSessionProvider.AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionProvider.AcademicSessionChangeEvent event) {
				sessionChanged();
			}
		});
		
		initWidget(iGrid);
		
		initAsync();
		
		sessionChanged();
	}
	
	private void initAsync() {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init();
			}
			public void onFailure(Throwable reason) {
				UniTimeNotifications.error(reason);
			}
		});
	}
	
	private void init() {
		CourseSelectionBox.Validator checkForDuplicities = new CourseSelectionBox.Validator() {
			public String validate(CourseSelectionBox source) {
				if (source.getCourse().isEmpty() || source.isFreeTime()) return null;
				String course = source.getCourse();
				for (CourseSelectionBox[] c: iCourses) {
					for (int i = 0; i < c.length; i++) {
						if (c[i] == source) continue;
						if (c[i].getCourse().equals(course)) return MESSAGES.validationMultiple(course);
					}
				}
				for (CourseSelectionBox[] c: iAlternatives) {
					for (int i = 0; i < c.length; i++) {
						if (c[i] == source) continue;
						if (c[i].getCourse().equals(course)) return MESSAGES.validationMultiple(course);
					}
				}
				return null;
			}
		};
		
		for (final CourseSelectionBox[] c: iCourses) {
			c[0].addCourseSelectionChangeHandler(new CourseSelectionBox.CourseSelectionChangeHandler() {
				public void onChange(String course, boolean valid) {
					if (valid) c[0].hideError();
					if (!c[0].isFreeTime()) {
						c[1].setEnabled(valid || !c[1].getCourse().isEmpty() || !c[2].getCourse().isEmpty());
						if (valid && !c[0].getCourse().isEmpty())
							c[1].setHint(MESSAGES.courseRequestsHintAlt(c[0].getCourse()));
						else
							c[1].setHint("");
					} else {
						c[1].setHint("");
					}
				}
			});
			c[1].addCourseSelectionChangeHandler(new CourseSelectionBox.CourseSelectionChangeHandler() {
				public void onChange(String course, boolean valid) {
					if (valid) c[1].hideError();
					c[2].setEnabled(valid || !c[2].getCourse().isEmpty());
					if (valid && !c[0].getCourse().isEmpty() && !c[1].getCourse().isEmpty())
						c[2].setHint(MESSAGES.courseRequestsHintAlt2(c[0].getCourse(), c[1].getCourse()));
					else
						c[2].setHint("");
				}
			});
			c[2].addCourseSelectionChangeHandler(new CourseSelectionBox.CourseSelectionChangeHandler() {
				public void onChange(String course, boolean valid) {
					if (valid) c[2].hideError();
				}
			});
			c[1].addValidator(new CourseSelectionBox.Validator() {
				public String validate(CourseSelectionBox source) {
					if (!c[1].getCourse().isEmpty() && c[0].getCourse().isEmpty()) {
						return MESSAGES.validationNoCourse();
					}
					if (!c[1].getCourse().isEmpty() && c[0].isFreeTime()) {
						return MESSAGES.validationFreeTimeWithAlt();
					}
					if (c[1].isFreeTime()) {
						return MESSAGES.validationAltFreeTime();
					}
					return null;
				}
			});
			c[2].addValidator(new CourseSelectionBox.Validator() {
				public String validate(CourseSelectionBox source) {
					if (!c[2].getCourse().isEmpty() && c[1].getCourse().isEmpty()) {
						return MESSAGES.validationSecondAltWithoutFirst();
					}
					if (!c[2].getCourse().isEmpty() && c[0].isFreeTime()) {
						return MESSAGES.validationFreeTimeWithAlt();
					}
					if (c[2].isFreeTime()) {
						return MESSAGES.validationAltFreeTime();
					}
					return null;
				}
			});
			c[0].setAlternative(c[1]); c[1].setAlternative(c[2]);
			c[1].setPrimary(c[0]); c[2].setPrimary(c[1]);
			c[0].addValidator(checkForDuplicities);
			c[1].addValidator(checkForDuplicities);
			c[2].addValidator(checkForDuplicities);
		}
		
		for (final CourseSelectionBox[] c: iAlternatives) {
			c[0].addCourseSelectionChangeHandler(new CourseSelectionBox.CourseSelectionChangeHandler() {
				public void onChange(String course, boolean valid) {
					if (valid) c[0].hideError();
					if (!c[0].isFreeTime()) {
						c[1].setEnabled(valid || !c[1].getCourse().isEmpty() || !c[2].getCourse().isEmpty());
						if (valid && !c[0].getCourse().isEmpty())
							c[1].setHint(MESSAGES.courseRequestsHintAlt(c[0].getCourse()));
						else
							c[1].setHint("");
					} else {
						c[1].setHint("");
					}
				}
			});
			c[1].addCourseSelectionChangeHandler(new CourseSelectionBox.CourseSelectionChangeHandler() {
				public void onChange(String course, boolean valid) {
					if (valid) c[1].hideError();
					c[2].setEnabled(valid || !c[2].getCourse().isEmpty());
					if (valid && !c[0].getCourse().isEmpty() && !c[1].getCourse().isEmpty())
						c[2].setHint(MESSAGES.courseRequestsHintAlt2(c[0].getCourse(), c[1].getCourse()));
					else
						c[2].setHint("");
				}
			});
			c[2].addCourseSelectionChangeHandler(new CourseSelectionBox.CourseSelectionChangeHandler() {
				public void onChange(String course, boolean valid) {
					if (valid) c[2].hideError();
				}
			});
			c[0].addValidator(new CourseSelectionBox.Validator() {
				public String validate(CourseSelectionBox source) {
					if (c[0].isFreeTime()) {
						return MESSAGES.validationAltFreeTime();
					}
					return null;
				}
			});
			c[1].addValidator(new CourseSelectionBox.Validator() {
				public String validate(CourseSelectionBox source) {
					if (!c[1].getCourse().isEmpty() && c[0].getCourse().isEmpty()) {
						return MESSAGES.validationNoCourse();
					}
					if (!c[1].getCourse().isEmpty() && c[0].isFreeTime()) {
						return MESSAGES.validationFreeTimeWithAlt();
					}
					if (c[1].isFreeTime()) {
						return MESSAGES.validationAltFreeTime();
					}
					return null;
				}
			});
			c[2].addValidator(new CourseSelectionBox.Validator() {
				public String validate(CourseSelectionBox source) {
					if (!c[2].getCourse().isEmpty() && c[1].getCourse().isEmpty()) {
						return MESSAGES.validationSecondAltWithoutFirst();
					}
					if (!c[2].getCourse().isEmpty() && c[0].isFreeTime()) {
						return MESSAGES.validationFreeTimeWithAlt();
					}
					if (c[2].isFreeTime()) {
						return MESSAGES.validationAltFreeTime();
					}
					return null;
				}
			});
			c[0].setAlternative(c[1]); c[1].setAlternative(c[2]);
			c[1].setPrimary(c[0]); c[2].setPrimary(c[1]);
			c[0].addValidator(checkForDuplicities);
			c[1].addValidator(checkForDuplicities);
			c[2].addValidator(checkForDuplicities);
		}
	}
	
	public void sessionChanged() {
		boolean showWaitLists = (iSessionProvider != null && iSessionProvider.getAcademicSessionInfo() != null && iSessionProvider.getAcademicSessionInfo().isCanWaitListCourseRequests());
		iGrid.setHTML(0, 1, showWaitLists ? MESSAGES.courseRequestsWaitList() : "");
		for (int i = 0; i < iCourses.size(); i++)
			iGrid.getCellFormatter().setVisible(1 + i, 4, showWaitLists);
	}
	
	public void validate(final AsyncCallback<Boolean> callback) {
		String failed = null;
		LoadingWidget.getInstance().show(MESSAGES.courseRequestsValidating());
		for (final CourseSelectionBox[] c: iCourses) {
			for (CourseSelectionBox x: c) {
				String message = x.validate();
				if (message != null) failed = message;
			}
		}
		CourseRequestInterface cr = new CourseRequestInterface();
		cr.setAcademicSessionId(iSessionProvider.getAcademicSessionId());
		fillInCourses(cr); fillInAlternatives(cr);
		final boolean success = (failed == null);
		iSectioningService.checkCourses(iOnline, cr,
				new AsyncCallback<Collection<String>>() {
					public void onSuccess(Collection<String> result) {
						for (String course: result)
							setError(course, MESSAGES.validationCourseNotExists(course));
						LoadingWidget.getInstance().hide();
						callback.onSuccess(success && result.isEmpty());
					}
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						callback.onFailure(caught);
					}
				});
	}
	
	public void setError(String course, String error) {
		GWT.log(error);
		for (CourseSelectionBox[] c: iCourses) {
			for (int i = 0; i < c.length; i++) {
				if (course.equals(c[i].getCourse()) && !c[i].hasError()) c[i].setError(error);			
			}
		}
		for (CourseSelectionBox[] c: iAlternatives) {
			for (int i = 0; i < c.length; i++) {
				if (course.equals(c[i].getCourse()) && !c[i].hasError()) c[i].setError(error);			
			}
		}
	}
	
	public void changeTip() {
		iTip.setText(CONSTANTS.tips()[(int)(Math.random() * CONSTANTS.tips().length)]);
	}
	
	public void fillInCourses(CourseRequestInterface cr) {
		for (CourseSelectionBox[] course: iCourses) {
			CourseRequestInterface.Request req = new CourseRequestInterface.Request();
			course[0].fillInFreeTime(req);
			req.setRequestedCourse(course[0].getCourse());
			req.setFirstAlternative(course[1].getCourse());
			req.setSecondAlternative(course[2].getCourse());
			req.setWaitList(course[0].getWaitList());
			cr.getCourses().add(req);
		}
	}
	
	public void fillInAlternatives(CourseRequestInterface cr) {
		for (CourseSelectionBox[] course: iAlternatives) {
			CourseRequestInterface.Request req = new CourseRequestInterface.Request();
			req.setRequestedCourse(course[0].getCourse());
			req.setFirstAlternative(course[1].getCourse());
			req.setSecondAlternative(course[2].getCourse());
			cr.getAlternatives().add(req);
		}
	}
	
	public CourseRequestInterface getRequest() {
		CourseRequestInterface cr = new CourseRequestInterface();
		cr.setAcademicSessionId(iSessionProvider.getAcademicSessionId());
		fillInCourses(cr);
		fillInAlternatives(cr);
		return cr;
	}
	
	public void setRequest(CourseRequestInterface request) {
		for (int idx = 0; idx < (iCourses.size() < request.getCourses().size() ? iCourses.size() : request.getCourses().size()); idx++) {
			iCourses.get(idx)[0].setCourse(request.getCourses().get(idx).getRequestedCourse(), true);
			iCourses.get(idx)[1].setCourse(request.getCourses().get(idx).getFirstAlternative(), true);
			iCourses.get(idx)[2].setCourse(request.getCourses().get(idx).getSecondAlternative(), true);
			iCourses.get(idx)[0].setWaitList(request.getCourses().get(idx).isWaitList());
		}
		for (int idx = 0; idx < (iAlternatives.size() < request.getAlternatives().size() ? iAlternatives.size() : request.getAlternatives().size()); idx++) {
			iAlternatives.get(idx)[0].setCourse(request.getAlternatives().get(idx).getRequestedCourse(), true);
			iAlternatives.get(idx)[1].setCourse(request.getAlternatives().get(idx).getFirstAlternative(), true);
			iAlternatives.get(idx)[2].setCourse(request.getAlternatives().get(idx).getSecondAlternative(), true);
		}
	}
	
	public void clear() {
		iTip.setText(CONSTANTS.tips()[(int)(Math.random() * CONSTANTS.tips().length)]);
		for (CourseSelectionBox[] c: iCourses) {
			for (int i=0;i<3;i++) {
				c[i].clear();
				if (i>0) {
					c[i].setEnabled(false);
					c[i].setHint("");
				}
			}
			c[0].setWaitList(false);
		}
		for (CourseSelectionBox[] c: iAlternatives) {
			for (int i=0;i<3;i++) {
				c[i].clear();
				if (i>0) {
					c[i].setEnabled(false);
					c[i].setHint("");
				}
			}
		}
	}
	
	public String getFirstError() {
		for (CourseSelectionBox[] c: iCourses) {
			for (int i=0;i<3;i++) {
				if (c[i].hasError()) return c[i].getError();
			}
		}
		for (CourseSelectionBox[] c: iAlternatives) {
			for (int i=0;i<3;i++) {
				if (c[i].hasError()) return c[i].getError();
			}
		}
		return null;
	}
}
