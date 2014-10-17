/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
import org.unitime.timetable.gwt.client.widgets.CourseSelection;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.Validator;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Tomas Muller
 */
public class CourseRequestsTable extends Composite implements HasValue<CourseRequestInterface> {
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
	
	Validator<CourseSelection> iCheckForDuplicities;
	private boolean iCanWaitList = true;

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
		
		iGrid.getFlexCellFormatter().setColSpan(idx, 1, 4);
		iGrid.getFlexCellFormatter().setStyleName(idx, 1, "unitime-MainTableHeaderNote");
		iGrid.setHTML(idx, 1, MESSAGES.courseRequestsWaitList());
		iGrid.getFlexCellFormatter().getElement(idx, 1).getStyle().setVerticalAlign(VerticalAlign.BOTTOM);
		
		idx++;
		
		iCourses = new ArrayList<CourseSelectionBox[]>();
		iAlternatives = new ArrayList<CourseSelectionBox[]>();
		
		iCheckForDuplicities = new Validator<CourseSelection>() {
			public String validate(CourseSelection source) {
				if (source.getValue().isEmpty() || source.isFreeTime()) return null;
				String course = source.getValue();
				for (CourseSelectionBox[] c: iCourses) {
					for (int i = 0; i < c.length; i++) {
						if (c[i] == source) continue;
						if (c[i].getValue().equals(course)) return MESSAGES.validationMultiple(course);
					}
				}
				for (CourseSelectionBox[] c: iAlternatives) {
					for (int i = 0; i < c.length; i++) {
						if (c[i] == source) continue;
						if (c[i].getValue().equals(course)) return MESSAGES.validationMultiple(course);
					}
				}
				return null;
			}
		};

		for (int i=0; i<CONSTANTS.numberOfCourses(); i++) {
			iGrid.setText(idx, 0, MESSAGES.courseRequestsPriority(i+1));
			final CourseSelectionBox[] c = new CourseSelectionBox[] {
					new CourseSelectionBox(iSessionProvider, true, true),
					new CourseSelectionBox(iSessionProvider, false, false),
					new CourseSelectionBox(iSessionProvider, false, false)
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
			final ImageButton delete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].remove();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			iGrid.setWidget(idx, 7, delete);
			delete.setAltText(ARIA.altDeleteRequest(i + 1));
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
		iGrid.getFlexCellFormatter().setColSpan(idx, 0, 8);
		iGrid.getFlexCellFormatter().setStyleName(idx, 0, "unitime-Hint");
		iGrid.setWidget(idx++, 0, iTip);

		iGrid.getFlexCellFormatter().setColSpan(idx, 0, 2);
		iGrid.getFlexCellFormatter().setStyleName(idx, 0, "unitime-MainTableHeader");
		iGrid.setText(idx, 0, MESSAGES.courseRequestsAlternatives());
		iGrid.getFlexCellFormatter().setColSpan(idx, 1, 6);
		iGrid.getFlexCellFormatter().setStyleName(idx, 1, "unitime-MainTableHeaderNote");
		iGrid.setHTML(idx, 1, MESSAGES.courseRequestsAlternativesNote());
		iGrid.getFlexCellFormatter().getElement(idx, 1).getStyle().setVerticalAlign(VerticalAlign.BOTTOM);
		idx++;

		for (int i=0; i<CONSTANTS.numberOfAlternatives(); i++) {
			iGrid.setText(idx, 0, MESSAGES.courseRequestsAlternative(i+1));
			final CourseSelectionBox[] c = new CourseSelectionBox[] {
					new CourseSelectionBox(iSessionProvider, true, false),
					new CourseSelectionBox(iSessionProvider, false, false),
					new CourseSelectionBox(iSessionProvider, false, false)
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
			final ImageButton delete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].remove();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			iGrid.setWidget(idx, 6, delete);
			delete.setAltText(ARIA.altDeleteAlternateRequest(i + 1));
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
		
		initWidget(iGrid);
		
		initAsync();
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
	
	private void addCourseLine() {
		int i = iCourses.size();
		int idx = 1 + i;
		iGrid.insertRow(idx);
		iGrid.setText(idx, 0, MESSAGES.courseRequestsPriority(i+1));
		final CourseSelectionBox[] c = new CourseSelectionBox[] {
				new CourseSelectionBox(iSessionProvider, true, true),
				new CourseSelectionBox(iSessionProvider, false, false),
				new CourseSelectionBox(iSessionProvider, false, false)
		};
		c[0].setHint(MESSAGES.courseRequestsHint8());
		c[0].setLabel(ARIA.titleRequestedCourse(1 + i), ARIA.altRequestedCourseFinder(1 + i));
		c[1].setLabel(ARIA.titleRequestedCourseFirstAlternative(1 + i), ARIA.altRequestedCourseFirstAlternativeFinder(1 + i));
		c[2].setLabel(ARIA.titleRequestedCourseSecondAlternative(1 + i), ARIA.altRequestedCourseSecondAlternativeFinder(1 + i));
		if (i < 9)
			c[0].setAccessKey((char)((int)'1'+i));
		else if (i == 9)
			c[0].setAccessKey('0');
		final AriaCheckBox ch = new AriaCheckBox();
		ch.setAriaLabel(ARIA.titleRequestedWaitList(1 + i));
		CourseSelectionBox[] x = iCourses.get(i - 1);
		x[0].setHint("");
		for (int j=0; j<3; j++) {
			c[j].setPrev(x[j]);
			x[j].setNext(c[j]);
		}
		CourseSelectionBox[] y = iAlternatives.get(0);
		for (int j=0; j<3; j++) {
			c[j].setNext(y[j]);
			y[j].setPrev(c[j]);
		}
		if (i > 0) {
			final ImageButton up = new ImageButton(RESOURCES.up(), RESOURCES.up_Down(), RESOURCES.up_Over());
			up.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].swapUp();
				}
			});
			iGrid.setWidget(idx, 5, up);
			up.setAltText(ARIA.altSwapCourseRequest(i + 1, i));
			final ImageButton down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
			down.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].swapDown();
				}
			});
			iGrid.setWidget(idx, 6, down);
			down.setAltText(ARIA.altSwapCourseAlternateRequest(i + 1, 1));
			((ImageButton)iGrid.getWidget(idx - 1, 6)).setAltText(ARIA.altSwapCourseRequest(i, i + 1));
			((ImageButton)iGrid.getWidget(idx + 3, 4)).setAltText(ARIA.altSwapCourseAlternateRequest(iCourses.size(), 1));
			
			final ImageButton delete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].remove();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			iGrid.setWidget(idx, 7, delete);
			delete.setAltText(ARIA.altDeleteRequest(i + 1));
		}
		c[0].setWidth("260px");
		c[1].setWidth("170px");
		c[2].setWidth("170px");
		c[1].setEnabled(false);
		c[2].setEnabled(false);
		iGrid.setWidget(idx, 1, c[0]);
		iGrid.setWidget(idx, 2, c[1]);
		iGrid.setWidget(idx, 3, c[2]);
		iGrid.setWidget(idx, 4, ch);
		iGrid.getCellFormatter().setVisible(idx, 4, iCanWaitList);
		iGrid.getRowFormatter().setVerticalAlign(idx, HasVerticalAlignment.ALIGN_TOP);
		iCourses.add(c);
		c[0].setWaitList(ch);
		
		c[0].addCourseSelectionHandler(new CourseSelectionHandler() {
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				if (event.isValid()) c[0].setError("");
				if (!c[0].isFreeTime()) {
					c[1].setEnabled(event.isValid() || !c[1].getValue().isEmpty() || !c[2].getValue().isEmpty());
					if (event.isValid() && !c[0].getValue().isEmpty())
						c[1].setHint(MESSAGES.courseRequestsHintAlt(c[0].getValue()));
					else
						c[1].setHint("");
				} else {
					c[1].setHint("");
				}
				if (event.isValid() && c == iCourses.get(iCourses.size() - 1)) addCourseLine();
				ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
			}
		});
		c[1].addCourseSelectionHandler(new CourseSelectionHandler() {
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				if (event.isValid()) c[1].setError("");
				c[2].setEnabled(event.isValid() || !c[2].getValue().isEmpty());
				if (event.isValid() && !c[0].getValue().isEmpty() && !c[1].getValue().isEmpty())
					c[2].setHint(MESSAGES.courseRequestsHintAlt2(c[0].getValue(), c[1].getValue()));
				else
					c[2].setHint("");
				ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
			}
		});
		c[2].addCourseSelectionHandler(new CourseSelectionHandler() {
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				if (event.isValid()) c[2].setError("");
				ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
			}
		});
		c[1].addValidator(new Validator<CourseSelection>() {
			public String validate(CourseSelection source) {
				if (!c[1].getValue().isEmpty() && c[0].getValue().isEmpty()) {
					return MESSAGES.validationNoCourse();
				}
				if (!c[1].getValue().isEmpty() && c[0].isFreeTime()) {
					return MESSAGES.validationFreeTimeWithAlt();
				}
				if (c[1].isFreeTime()) {
					return MESSAGES.validationAltFreeTime();
				}
				return null;
			}
		});
		c[2].addValidator(new Validator<CourseSelection>() {
			public String validate(CourseSelection source) {
				if (!c[2].getValue().isEmpty() && c[1].getValue().isEmpty()) {
					return MESSAGES.validationSecondAltWithoutFirst();
				}
				if (!c[2].getValue().isEmpty() && c[0].isFreeTime()) {
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
		c[0].addValidator(iCheckForDuplicities);
		c[1].addValidator(iCheckForDuplicities);
		c[2].addValidator(iCheckForDuplicities);
	}
	
	private void addAlternativeLine() {
		int i = iAlternatives.size();
		int idx = 3 + iCourses.size() + i;
		iGrid.insertRow(idx);
		iGrid.setText(idx, 0, MESSAGES.courseRequestsAlternative(i+1));
		final CourseSelectionBox[] c = new CourseSelectionBox[] {
				new CourseSelectionBox(iSessionProvider, true, false),
				new CourseSelectionBox(iSessionProvider, false, false),
				new CourseSelectionBox(iSessionProvider, false, false)
		};
		c[0].setLabel(ARIA.titleRequestedAlternate(1 + i, String.valueOf((char)((int)'a'+i))), ARIA.altRequestedAlternateFinder(1 + i));
		c[1].setLabel(ARIA.titleRequestedAlternateFirstAlternative(1 + i), ARIA.altRequestedAlternateFirstFinder(1 + i));
		c[2].setLabel(ARIA.titleRequestedAlternateSecondAlternative(1 + i), ARIA.altRequestedAlternateSecondFinder(1 + i));
		c[0].setAccessKey((char)((int)'a'+i));
		if (i>=0) {
			final CourseSelectionBox[] x = iAlternatives.get(i - 1);
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
			final ImageButton down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
			down.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					x[0].swapDown();
				}
			});
			iGrid.setWidget(idx - 1, 5, down);
			down.setAltText(ARIA.altSwapAlternateRequest(i, i + 1));
			final ImageButton delete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].remove();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			iGrid.setWidget(idx, 6, delete);
			delete.setAltText(ARIA.altDeleteAlternateRequest(i + 1));
		}
		c[0].setWidth("260px");
		c[1].setWidth("170px");
		c[2].setWidth("170px");
		c[1].setEnabled(false);
		c[2].setEnabled(false);
		iGrid.setWidget(idx, 1, c[0]);
		iGrid.setWidget(idx, 2, c[1]);
		iGrid.setWidget(idx, 3, c[2]);
		iGrid.getRowFormatter().setVerticalAlign(idx, HasVerticalAlignment.ALIGN_TOP);
		iAlternatives.add(c);
		
		c[0].addCourseSelectionHandler(new CourseSelectionHandler() {
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				if (event.isValid()) c[0].setError("");
				if (!c[0].isFreeTime()) {
					c[1].setEnabled(event.isValid() || !c[1].getValue().isEmpty() || !c[2].getValue().isEmpty());
					if (event.isValid() && !c[0].getValue().isEmpty())
						c[1].setHint(MESSAGES.courseRequestsHintAlt(c[0].getValue()));
					else
						c[1].setHint("");
				} else {
					c[1].setHint("");
				}
				if (event.isValid() && c == iAlternatives.get(iAlternatives.size() - 1)) addAlternativeLine();
				ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
			}
		});
		c[1].addCourseSelectionHandler(new CourseSelectionHandler() {
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				if (event.isValid()) c[1].setError("");
				c[2].setEnabled(event.isValid() || !c[2].getValue().isEmpty());
				if (event.isValid() && !c[0].getValue().isEmpty() && !c[1].getValue().isEmpty())
					c[2].setHint(MESSAGES.courseRequestsHintAlt2(c[0].getValue(), c[1].getValue()));
				else
					c[2].setHint("");
				ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
			}
		});
		c[2].addCourseSelectionHandler(new CourseSelectionHandler() {
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				if (event.isValid()) c[2].setError("");
				ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
			}
		});
		c[0].addValidator(new Validator<CourseSelection>() {
			public String validate(CourseSelection source) {
				if (c[0].isFreeTime()) {
					return MESSAGES.validationAltFreeTime();
				}
				return null;
			}
		});
		c[1].addValidator(new Validator<CourseSelection>() {
			public String validate(CourseSelection source) {
				if (!c[1].getValue().isEmpty() && c[0].getValue().isEmpty()) {
					return MESSAGES.validationNoCourse();
				}
				if (!c[1].getValue().isEmpty() && c[0].isFreeTime()) {
					return MESSAGES.validationFreeTimeWithAlt();
				}
				if (c[1].isFreeTime()) {
					return MESSAGES.validationAltFreeTime();
				}
				return null;
			}
		});
		c[2].addValidator(new Validator<CourseSelection>() {
			public String validate(CourseSelection source) {
				if (!c[2].getValue().isEmpty() && c[1].getValue().isEmpty()) {
					return MESSAGES.validationSecondAltWithoutFirst();
				}
				if (!c[2].getValue().isEmpty() && c[0].isFreeTime()) {
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
		c[0].addValidator(iCheckForDuplicities);
		c[1].addValidator(iCheckForDuplicities);
		c[2].addValidator(iCheckForDuplicities);
	}
	
	private void init() {
		for (final CourseSelectionBox[] c: iCourses) {
			c[0].addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					if (event.isValid()) c[0].setError("");
					if (!c[0].isFreeTime()) {
						c[1].setEnabled(event.isValid() || !c[1].getValue().isEmpty() || !c[2].getValue().isEmpty());
						if (event.isValid() && !c[0].getValue().isEmpty())
							c[1].setHint(MESSAGES.courseRequestsHintAlt(c[0].getValue()));
						else
							c[1].setHint("");
					} else {
						c[1].setHint("");
					}
					if (event.isValid() && c == iCourses.get(iCourses.size() - 1)) addCourseLine();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			c[1].addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					if (event.isValid()) c[1].setError("");
					c[2].setEnabled(event.isValid() || !c[2].getValue().isEmpty());
					if (event.isValid() && !c[0].getValue().isEmpty() && !c[1].getValue().isEmpty())
						c[2].setHint(MESSAGES.courseRequestsHintAlt2(c[0].getValue(), c[1].getValue()));
					else
						c[2].setHint("");
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			c[2].addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					if (event.isValid()) c[2].setError("");
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			c[1].addValidator(new Validator<CourseSelection>() {
				public String validate(CourseSelection source) {
					if (!c[1].getValue().isEmpty() && c[0].getValue().isEmpty()) {
						return MESSAGES.validationNoCourse();
					}
					if (!c[1].getValue().isEmpty() && c[0].isFreeTime()) {
						return MESSAGES.validationFreeTimeWithAlt();
					}
					if (c[1].isFreeTime()) {
						return MESSAGES.validationAltFreeTime();
					}
					return null;
				}
			});
			c[2].addValidator(new Validator<CourseSelection>() {
				public String validate(CourseSelection source) {
					if (!c[2].getValue().isEmpty() && c[1].getValue().isEmpty()) {
						return MESSAGES.validationSecondAltWithoutFirst();
					}
					if (!c[2].getValue().isEmpty() && c[0].isFreeTime()) {
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
			c[0].addValidator(iCheckForDuplicities);
			c[1].addValidator(iCheckForDuplicities);
			c[2].addValidator(iCheckForDuplicities);
		}
		
		for (final CourseSelectionBox[] c: iAlternatives) {
			c[0].addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					if (event.isValid()) c[0].setError("");
					if (!c[0].isFreeTime()) {
						c[1].setEnabled(event.isValid() || !c[1].getValue().isEmpty() || !c[2].getValue().isEmpty());
						if (event.isValid() && !c[0].getValue().isEmpty())
							c[1].setHint(MESSAGES.courseRequestsHintAlt(c[0].getValue()));
						else
							c[1].setHint("");
					} else {
						c[1].setHint("");
					}
					if (event.isValid() && c == iAlternatives.get(iAlternatives.size() - 1)) addAlternativeLine();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			c[1].addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					if (event.isValid()) c[1].setError("");
					c[2].setEnabled(event.isValid() || !c[2].getValue().isEmpty());
					if (event.isValid() && !c[0].getValue().isEmpty() && !c[1].getValue().isEmpty())
						c[2].setHint(MESSAGES.courseRequestsHintAlt2(c[0].getValue(), c[1].getValue()));
					else
						c[2].setHint("");
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			c[2].addCourseSelectionHandler(new CourseSelectionHandler() {
				@Override
				public void onCourseSelection(CourseSelectionEvent event) {
					if (event.isValid()) c[2].setError("");
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			c[0].addValidator(new Validator<CourseSelection>() {
				public String validate(CourseSelection source) {
					if (c[0].isFreeTime()) {
						return MESSAGES.validationAltFreeTime();
					}
					return null;
				}
			});
			c[1].addValidator(new Validator<CourseSelection>() {
				public String validate(CourseSelection source) {
					if (!c[1].getValue().isEmpty() && c[0].getValue().isEmpty()) {
						return MESSAGES.validationNoCourse();
					}
					if (!c[1].getValue().isEmpty() && c[0].isFreeTime()) {
						return MESSAGES.validationFreeTimeWithAlt();
					}
					if (c[1].isFreeTime()) {
						return MESSAGES.validationAltFreeTime();
					}
					return null;
				}
			});
			c[2].addValidator(new Validator<CourseSelection>() {
				public String validate(CourseSelection source) {
					if (!c[2].getValue().isEmpty() && c[1].getValue().isEmpty()) {
						return MESSAGES.validationSecondAltWithoutFirst();
					}
					if (!c[2].getValue().isEmpty() && c[0].isFreeTime()) {
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
			c[0].addValidator(iCheckForDuplicities);
			c[1].addValidator(iCheckForDuplicities);
			c[2].addValidator(iCheckForDuplicities);
		}
	}
	
	public void setCanWaitList(boolean canWaitList) {
		iCanWaitList = canWaitList;
		iGrid.setHTML(0, 1, iCanWaitList ? MESSAGES.courseRequestsWaitList() : "");
		for (int i = 0; i < iCourses.size(); i++)
			iGrid.getCellFormatter().setVisible(1 + i, 4, iCanWaitList);
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
				if (course.equals(c[i].getValue()) && c[i].getError() == null) c[i].setError(error);			
			}
		}
		for (CourseSelectionBox[] c: iAlternatives) {
			for (int i = 0; i < c.length; i++) {
				if (course.equals(c[i].getValue()) && c[i].getError() == null) c[i].setError(error);			
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
			req.setRequestedCourse(course[0].getValue());
			req.setFirstAlternative(course[1].getValue());
			req.setSecondAlternative(course[2].getValue());
			req.setWaitList(course[0].getWaitList());
			cr.getCourses().add(req);
		}
	}
	
	public void fillInAlternatives(CourseRequestInterface cr) {
		for (CourseSelectionBox[] course: iAlternatives) {
			CourseRequestInterface.Request req = new CourseRequestInterface.Request();
			req.setRequestedCourse(course[0].getValue());
			req.setFirstAlternative(course[1].getValue());
			req.setSecondAlternative(course[2].getValue());
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
		while (iCourses.size() < request.getCourses().size()) addCourseLine();
		for (int idx = 0; idx < request.getCourses().size(); idx++) {
			iCourses.get(idx)[0].setValue(request.getCourses().get(idx).getRequestedCourse(), true);
			iCourses.get(idx)[1].setValue(request.getCourses().get(idx).getFirstAlternative(), true);
			iCourses.get(idx)[2].setValue(request.getCourses().get(idx).getSecondAlternative(), true);
			iCourses.get(idx)[0].setWaitList(request.getCourses().get(idx).isWaitList());
		}
		while (iAlternatives.size() < request.getAlternatives().size()) addAlternativeLine();
		for (int idx = 0; idx < request.getAlternatives().size(); idx++) {
			iAlternatives.get(idx)[0].setValue(request.getAlternatives().get(idx).getRequestedCourse(), true);
			iAlternatives.get(idx)[1].setValue(request.getAlternatives().get(idx).getFirstAlternative(), true);
			iAlternatives.get(idx)[2].setValue(request.getAlternatives().get(idx).getSecondAlternative(), true);
		}
	}
	
	public Boolean getWaitList(String course) {
		if (iCanWaitList)
			for (CourseSelectionBox[] line: iCourses)
				if (course.equals(line[0].getValue()) || course.equals(line[1].getValue()) || course.equals(line[2].getValue()))
					return line[0].getWaitList();
		return null;
	}
	
	public void setWaitList(String course, boolean waitList) {
		for (CourseSelectionBox[] line: iCourses) {
			if (course.equals(line[0].getValue()) || course.equals(line[1].getValue()) || course.equals(line[2].getValue()))
				line[0].setWaitList(waitList);
		}
	}
	
	public void clear() {
		iTip.setText(CONSTANTS.tips()[(int)(Math.random() * CONSTANTS.tips().length)]);
		for (CourseSelectionBox[] c: iCourses) {
			for (int i=0;i<3;i++) {
				c[i].setValue("");
				if (i>0) {
					c[i].setEnabled(false);
					c[i].setHint("");
				}
			}
			c[0].setWaitList(false);
		}
		for (CourseSelectionBox[] c: iAlternatives) {
			for (int i=0;i<3;i++) {
				c[i].setValue("");
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
				if (c[i].getError() != null) return c[i].getError();
			}
		}
		for (CourseSelectionBox[] c: iAlternatives) {
			for (int i=0;i<3;i++) {
				if (c[i].getError() != null) return c[i].getError();
			}
		}
		return null;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CourseRequestInterface> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public CourseRequestInterface getValue() {
		return getRequest();
	}

	@Override
	public void setValue(CourseRequestInterface value) {
		setValue(value, false);
	}

	@Override
	public void setValue(CourseRequestInterface value, boolean fireEvents) {
		setRequest(value);
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
}
