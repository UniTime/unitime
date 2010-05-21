/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.Collection;

import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.ToolBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class CourseRequestsTable extends Composite {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private FlexTable iGrid;
	private AcademicSessionProvider iSessionProvider;
	private ValidationErrors iValidator;
	private ArrayList<CourseSelectionBox[]> iCourses;
	private ArrayList<CourseSelectionBox[]> iAlternatives;
	private Label iTip;

	public CourseRequestsTable(AcademicSessionProvider sessionProvider) {
		iSessionProvider = sessionProvider;
		
		iGrid = new FlexTable();
		iGrid.setStylePrimaryName("unitime-MainTable");
		iGrid.addStyleName("unitime-BottomLine");
		iGrid.setCellPadding(2);
		iGrid.setCellSpacing(0);
		
		int idx = 0;
		
		iGrid.getFlexCellFormatter().setColSpan(idx, 0, 6);
		iGrid.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		iGrid.getRowFormatter().setStyleName(idx, "unitime-MainTableHeaderRow");
		iGrid.setText(idx++, 0, MESSAGES.courseRequestsCourses());
		
		iCourses = new ArrayList<CourseSelectionBox[]>();
		iAlternatives = new ArrayList<CourseSelectionBox[]>();

		for (int i=0; i<9; i++) {
			iGrid.setText(idx, 0, MESSAGES.courseRequestsPriority(i+1));
			final CourseSelectionBox[] c = new CourseSelectionBox[] {
					new CourseSelectionBox(iSessionProvider, "c"+i, true, true),
					new CourseSelectionBox(iSessionProvider, "c"+i+"a", false, false),
					new CourseSelectionBox(iSessionProvider, "c"+i+"b", false, false)
			};
			c[0].setAccessKey((char)((int)'1'+i));
			if (i>0) {
				final CourseSelectionBox[] x = iCourses.get(i - 1);
				for (int j=0; j<3; j++) {
					c[j].setPrev(x[j]);
					x[j].setNext(c[j]);
				}
				final Image up = new Image(RESOURCES.up());
				up.addMouseOverHandler(new MouseOverHandler() {
					public void onMouseOver(MouseOverEvent event) {
						up.setResource(RESOURCES.up_Over());
					}
				});
				up.addMouseOutHandler(new MouseOutHandler() {
					public void onMouseOut(MouseOutEvent event) {
						up.setResource(RESOURCES.up());
					}
				});
				up.addMouseDownHandler(new MouseDownHandler() {
					public void onMouseDown(MouseDownEvent event) {
						up.setResource(RESOURCES.up_Down());
					}
				});
				up.addMouseUpHandler(new MouseUpHandler() {
					public void onMouseUp(MouseUpEvent event) {
						up.setResource(RESOURCES.up());
						
					}
				});
				up.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapUp();
					}
				});
				iGrid.setWidget(idx, 4, up);
			}
			if (i<=8) {
				final Image down = new Image(RESOURCES.down());
				down.addMouseOverHandler(new MouseOverHandler() {
					public void onMouseOver(MouseOverEvent event) {
						down.setResource(RESOURCES.down_Over());
					}
				});
				down.addMouseOutHandler(new MouseOutHandler() {
					public void onMouseOut(MouseOutEvent event) {
						down.setResource(RESOURCES.down());
					}
				});
				down.addMouseDownHandler(new MouseDownHandler() {
					public void onMouseDown(MouseDownEvent event) {
						down.setResource(RESOURCES.down_Down());
					}
				});
				down.addMouseUpHandler(new MouseUpHandler() {
					public void onMouseUp(MouseUpEvent event) {
						down.setResource(RESOURCES.down());
						
					}
				});
				down.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapDown();
					}
				});
				iGrid.setWidget(idx, 5, down);
			}
			c[0].setWidth("200");
			c[1].setWidth("120");
			c[2].setWidth("120");
			iGrid.setWidget(idx, 1, c[0]);
			iGrid.setWidget(idx, 2, c[1]);
			iGrid.setWidget(idx, 3, c[2]);
			iGrid.getRowFormatter().setVerticalAlign(idx, HasVerticalAlignment.ALIGN_TOP);
			iCourses.add(c);
			idx++;
		}
		iCourses.get(1)[0].setHint(MESSAGES.courseRequestsHint1());
		iCourses.get(3)[0].setHint(MESSAGES.courseRequestsHint3());
		iCourses.get(4)[0].setHint(MESSAGES.courseRequestsHint4());
		iCourses.get(8)[0].setHint(MESSAGES.courseRequestsHint8());

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
		iGrid.getFlexCellFormatter().setColSpan(idx, 0, 6);
		iGrid.getFlexCellFormatter().setStyleName(idx, 0, "unitime-Hint");
		iGrid.setWidget(idx++, 0, iTip);

		iGrid.getFlexCellFormatter().setColSpan(idx, 0, 6);
		iGrid.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		iGrid.setText(idx++, 0, MESSAGES.courseRequestsAlternatives());

		for (int i=0; i<3; i++) {
			iGrid.setText(idx, 0, MESSAGES.courseRequestsAlternative(i+1));
			final CourseSelectionBox[] c = new CourseSelectionBox[] {
					new CourseSelectionBox(iSessionProvider, "a"+i, true, false),
					new CourseSelectionBox(iSessionProvider, "a"+i+"a", false, false),
					new CourseSelectionBox(iSessionProvider, "a"+i+"b", false, false)
			};
			c[0].setAccessKey((char)((int)'a'+i));
			if (i>=0) {
				final CourseSelectionBox[] x = (i==0 ? iCourses.get(8) : iAlternatives.get(i - 1));
				for (int j=0; j<3; j++) {
					c[j].setPrev(x[j]);
					x[j].setNext(c[j]);
				}
				final Image up = new Image(RESOURCES.up());
				up.addMouseOverHandler(new MouseOverHandler() {
					public void onMouseOver(MouseOverEvent event) {
						up.setResource(RESOURCES.up_Over());
					}
				});
				up.addMouseOutHandler(new MouseOutHandler() {
					public void onMouseOut(MouseOutEvent event) {
						up.setResource(RESOURCES.up());
					}
				});
				up.addMouseDownHandler(new MouseDownHandler() {
					public void onMouseDown(MouseDownEvent event) {
						up.setResource(RESOURCES.up_Down());
					}
				});
				up.addMouseUpHandler(new MouseUpHandler() {
					public void onMouseUp(MouseUpEvent event) {
						up.setResource(RESOURCES.up());
						
					}
				});
				up.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapUp();
					}
				});
				iGrid.setWidget(idx, 4, up);
			}
			if (i<2) {
				final Image down = new Image(RESOURCES.down());
				down.addMouseOverHandler(new MouseOverHandler() {
					public void onMouseOver(MouseOverEvent event) {
						down.setResource(RESOURCES.down_Over());
					}
				});
				down.addMouseOutHandler(new MouseOutHandler() {
					public void onMouseOut(MouseOutEvent event) {
						down.setResource(RESOURCES.down());
					}
				});
				down.addMouseDownHandler(new MouseDownHandler() {
					public void onMouseDown(MouseDownEvent event) {
						down.setResource(RESOURCES.down_Down());
					}
				});
				down.addMouseUpHandler(new MouseUpHandler() {
					public void onMouseUp(MouseUpEvent event) {
						down.setResource(RESOURCES.down());
						
					}
				});
				down.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapDown();
					}
				});
				iGrid.setWidget(idx, 5, down);
			}
			c[0].setWidth("200");
			c[1].setWidth("120");
			c[2].setWidth("120");
			iGrid.setWidget(idx, 1, c[0]);
			iGrid.setWidget(idx, 2, c[1]);
			iGrid.setWidget(idx, 3, c[2]);
			iGrid.getRowFormatter().setVerticalAlign(idx, HasVerticalAlignment.ALIGN_TOP);
			iAlternatives.add(c);
			idx++;
		}
		iAlternatives.get(0)[0].setHint(MESSAGES.courseRequestsHintA0());
		
		initWidget(iGrid);
		
		initAsync();
	}
	
	private void initAsync() {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label(MESSAGES.failedToLoadTheApp(reason.getMessage()));
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("loading").setVisible(false);
				RootPanel.get("body").add(error);
			}
		});
	}
	
	private void init() {
		iValidator = new ValidationErrors(false, MESSAGES.courseRequestsScheduling(), MESSAGES.validationFailed(), false);

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
						c[1].setEnabled(valid);
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
					c[2].setEnabled(valid);
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
			iValidator.addValidator(c[0]);
			iValidator.addValidator(c[1]);
			iValidator.addValidator(c[2]);
		}
		
		for (final CourseSelectionBox[] c: iAlternatives) {
			c[0].addCourseSelectionChangeHandler(new CourseSelectionBox.CourseSelectionChangeHandler() {
				public void onChange(String course, boolean valid) {
					if (valid) c[0].hideError();
					if (!c[0].isFreeTime()) {
						c[1].setEnabled(valid);
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
					c[2].setEnabled(valid);
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
			iValidator.addValidator(c[0]);
			iValidator.addValidator(c[1]);
			iValidator.addValidator(c[2]);
		}
		
		iValidator.addValidator(new Validator() {
			public void validate(final AsyncCallback<String> callback) {
				CourseRequestInterface cr = new CourseRequestInterface();
				cr.setAcademicSessionId(iSessionProvider.getAcademicSessionId());
				fillInCourses(cr); fillInAlternatives(cr);
				iSectioningService.checkCourses(cr,
						new AsyncCallback<Collection<String>>() {
							public void onSuccess(Collection<String> result) {
								for (String course: result)
									setError(course, MESSAGES.validationCourseNotExists(course));
								callback.onSuccess(result.isEmpty() ? null : MESSAGES.validationUnknownCourseNotExists());
							}
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}
						});
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
	
	public ValidationErrors getValidator() {
		return iValidator;
	}
	
	public void fillInCourses(CourseRequestInterface cr) {
		for (CourseSelectionBox[] course: iCourses) {
			CourseRequestInterface.Request req = new CourseRequestInterface.Request();
			course[0].fillInFreeTime(req);
			req.setRequestedCourse(course[0].getCourse());
			req.setFirstAlternative(course[1].getCourse());
			req.setSecondAlternative(course[2].getCourse());
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
		iCourses.get(0)[0].setFocus(true);
	}
}
