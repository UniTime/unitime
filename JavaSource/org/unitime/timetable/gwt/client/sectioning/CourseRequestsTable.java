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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.CourseSelection;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.Validator;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Tomas Muller
 */
public class CourseRequestsTable extends P implements HasValue<CourseRequestInterface> {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private AcademicSessionProvider iSessionProvider;
	private ArrayList<CourseSelectionBox[]> iCourses;
	private ArrayList<CourseSelectionBox[]> iAlternatives;
	private Label iTip;
	private boolean iOnline;
	
	Validator<CourseSelection> iCheckForDuplicities;
	private boolean iCanWaitList = true;
	private P iHeader, iHeaderTitle, iHeaderWaitlist;
	private P iAltHeader, iAltHeaderTitle, iAltHeaderNote;
	private List<P> iLines = new ArrayList<P>();
	private List<P> iAltLines = new ArrayList<P>();

	public CourseRequestsTable(AcademicSessionProvider sessionProvider, boolean online) {
		super("unitime-CourseRequests");
		iOnline = online;
		iSessionProvider = sessionProvider;
		
		iHeader = new P("header");
		iHeaderTitle = new P("title"); iHeaderTitle.setText(MESSAGES.courseRequestsCourses());
		iHeaderWaitlist = new P("waitlist"); iHeaderWaitlist.setHTML(MESSAGES.courseRequestsWaitList());
		iHeader.add(iHeaderTitle);
		iHeader.add(iHeaderWaitlist);
		add(iHeader);

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
			P line = new P("line");
			
			P title = new P("title"); title.setText(MESSAGES.courseRequestsPriority(i+1));
			line.add(title);

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
			c[0].addStyleName("course");
			c[1].addStyleName("alternative");
			c[2].addStyleName("alternative");
			line.add(c[0]);
			line.add(c[1]);
			line.add(c[2]);
			
			P buttons = new P("buttons");
			final AriaCheckBox ch = new AriaCheckBox();
			ch.setAriaLabel(ARIA.titleRequestedWaitList(1 + i));
			ch.addStyleName("wait-list");
			ch.setVisible(iCanWaitList);
			buttons.add(ch);
			
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
				up.addStyleName("up");
				up.setAltText(ARIA.altSwapCourseRequest(i + 1, i));
				buttons.add(up);
			} else {
				P p = new P("blank");
				buttons.add(p);
			}
			if (i<=CONSTANTS.numberOfCourses()) {
				final ImageButton down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
				down.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapDown();
					}
				});
				down.addStyleName("down");
				down.setAltText(i + 1 == CONSTANTS.numberOfCourses() ? ARIA.altSwapCourseAlternateRequest(i + 1, 1) : ARIA.altSwapCourseRequest(i + 1, i + 2));
				buttons.add(down);;
			} else {
				P p = new P("blank");
				buttons.add(p);
			}
			
			final ImageButton delete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].remove();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			delete.addStyleName("delete");
			delete.setAltText(ARIA.altDeleteRequest(i + 1));
			buttons.add(delete);
			line.add(buttons);
			
			iCourses.add(c);
			c[0].setWaitList(ch);
			
			iLines.add(line);
			add(line);
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
		iTip.addStyleName("tip");
		add(iTip);
		
		iAltHeader = new P("alt-header");
		iAltHeaderTitle = new P("title"); iAltHeaderTitle.setText(MESSAGES.courseRequestsAlternatives());
		iAltHeaderNote = new P("note"); iAltHeaderNote.setText(MESSAGES.courseRequestsAlternativesNote());
		iAltHeader.add(iAltHeaderTitle);
		iAltHeader.add(iAltHeaderNote);
		add(iAltHeader);

		for (int i=0; i<CONSTANTS.numberOfAlternatives(); i++) {
			P line = new P("line", "alternative");
			
			P title = new P("title"); title.setText(MESSAGES.courseRequestsAlternative(i+1));
			line.add(title);
			
			final CourseSelectionBox[] c = new CourseSelectionBox[] {
					new CourseSelectionBox(iSessionProvider, true, false),
					new CourseSelectionBox(iSessionProvider, false, false),
					new CourseSelectionBox(iSessionProvider, false, false)
			};
			c[0].setLabel(ARIA.titleRequestedAlternate(1 + i, String.valueOf((char)((int)'a'+i))), ARIA.altRequestedAlternateFinder(1 + i));
			c[1].setLabel(ARIA.titleRequestedAlternateFirstAlternative(1 + i), ARIA.altRequestedAlternateFirstFinder(1 + i));
			c[2].setLabel(ARIA.titleRequestedAlternateSecondAlternative(1 + i), ARIA.altRequestedAlternateSecondFinder(1 + i));
			c[0].setAccessKey((char)((int)'a'+i));
			c[0].addStyleName("course");
			c[1].addStyleName("alternative");
			c[2].addStyleName("alternative");
			line.add(c[0]);
			line.add(c[1]);
			line.add(c[2]);
			
			P buttons = new P("buttons");
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
				up.addStyleName("up");
				up.setAltText(i == 0 ? ARIA.altSwapCourseAlternateRequest(CONSTANTS.numberOfCourses(), 1) : ARIA.altSwapAlternateRequest(i + 1, i));
				buttons.add(up);
			} else {
				P p = new P("blank");
				buttons.add(p);
			}
			
			if (i<CONSTANTS.numberOfAlternatives() - 1) {
				final ImageButton down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
				down.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						c[0].swapDown();
					}
				});
				down.addStyleName("down");
				down.setAltText(ARIA.altSwapAlternateRequest(i + 1, i + 2));
				buttons.add(down);
			} else {
				P p = new P("blank");
				buttons.add(p);
			}
			
			final ImageButton delete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].remove();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			delete.addStyleName("delete");
			delete.setAltText(ARIA.altDeleteAlternateRequest(i + 1));
			buttons.add(delete);
			line.add(buttons);
			
			
			iAlternatives.add(c);
			iAltLines.add(line);
			add(line);
		}
		iAlternatives.get(0)[0].setHint(MESSAGES.courseRequestsHintA0());
		
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
		P line = new P("line");
		
		P title = new P("title"); title.setText(MESSAGES.courseRequestsPriority(i+1));
		line.add(title);
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
		c[0].addStyleName("course");
		c[1].addStyleName("alternative");
		c[2].addStyleName("alternative");
		line.add(c[0]);
		line.add(c[1]);
		line.add(c[2]);
		
		P buttons = new P("buttons");
		
		final AriaCheckBox ch = new AriaCheckBox();
		ch.setAriaLabel(ARIA.titleRequestedWaitList(1 + i));
		ch.addStyleName("wait-list");
		buttons.add(ch);
		if (i > 0) {
			final ImageButton up = new ImageButton(RESOURCES.up(), RESOURCES.up_Down(), RESOURCES.up_Over());
			up.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].swapUp();
				}
			});
			up.setAltText(ARIA.altSwapCourseRequest(i + 1, i));
			up.addStyleName("up");
			buttons.add(up);
		} else {
			P blank = new P("blank");
			buttons.add(blank);
		}
		final ImageButton down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
		down.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				c[0].swapDown();
			}
		});
		down.setAltText(ARIA.altSwapCourseAlternateRequest(i + 1, 1));
		down.addStyleName("down");
		buttons.add(down);
		((ImageButton)((P)iLines.get(i - 1).getWidget(4)).getWidget(2)).setAltText(ARIA.altSwapCourseRequest(i, i + 1));
		((ImageButton)((P)iAltLines.get(0).getWidget(4)).getWidget(0)).setAltText(ARIA.altSwapCourseAlternateRequest(i + 1, 1));
		
		final ImageButton delete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
		delete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				c[0].remove();
				ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
			}
		});
		delete.setAltText(ARIA.altDeleteRequest(i + 1));
		delete.addStyleName("delete");
		buttons.add(delete);
		line.add(buttons);
		
		c[1].setEnabled(false);
		c[2].setEnabled(false);
		iCourses.add(c);
		c[0].setWaitList(ch);
		insert(line, 1 + iLines.size());
		iLines.add(line);
		
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
		P line = new P("line", "alternative");
		
		P title = new P("title"); title.setText(MESSAGES.courseRequestsAlternative(i+1));
		line.add(title);
		
		final CourseSelectionBox[] c = new CourseSelectionBox[] {
				new CourseSelectionBox(iSessionProvider, true, false),
				new CourseSelectionBox(iSessionProvider, false, false),
				new CourseSelectionBox(iSessionProvider, false, false)
		};
		c[0].setLabel(ARIA.titleRequestedAlternate(1 + i, String.valueOf((char)((int)'a'+i))), ARIA.altRequestedAlternateFinder(1 + i));
		c[1].setLabel(ARIA.titleRequestedAlternateFirstAlternative(1 + i), ARIA.altRequestedAlternateFirstFinder(1 + i));
		c[2].setLabel(ARIA.titleRequestedAlternateSecondAlternative(1 + i), ARIA.altRequestedAlternateSecondFinder(1 + i));
		c[0].setAccessKey((char)((int)'a'+i));
		c[0].addStyleName("course");
		c[1].addStyleName("alternative");
		c[2].addStyleName("alternative");
		line.add(c[0]);
		line.add(c[1]);
		line.add(c[2]);
		
		P buttons = new P("buttons");
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
			up.setAltText(i == 0 ? ARIA.altSwapCourseAlternateRequest(CONSTANTS.numberOfCourses(), 1) : ARIA.altSwapAlternateRequest(i + 1, i));
			up.addStyleName("up");
			buttons.add(up);
			
			final ImageButton down = new ImageButton(RESOURCES.down(), RESOURCES.down_Down(), RESOURCES.down_Over());
			down.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					x[0].swapDown();
				}
			});
			down.setAltText(ARIA.altSwapAlternateRequest(i, i + 1));
			down.addStyleName("down");
			((P)iAltLines.get(i - 1).getWidget(4)).remove(1);
			((P)iAltLines.get(i - 1).getWidget(4)).insert(down, 1);
			P p = new P("blank");
			buttons.add(p);
			
			final ImageButton delete = new ImageButton(RESOURCES.delete(), RESOURCES.delete_Down(), RESOURCES.delete_Over());
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					c[0].remove();
					ValueChangeEvent.fire(CourseRequestsTable.this, getRequest());
				}
			});
			delete.setAltText(ARIA.altDeleteAlternateRequest(i + 1));
			delete.addStyleName("delete");
			buttons.add(delete);
			line.add(buttons);
		}
		c[1].setEnabled(false);
		c[2].setEnabled(false);
		iAlternatives.add(c);
		iAltLines.add(line);
		add(line);
		
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
		iHeaderWaitlist.setVisible(canWaitList);
		for (int i = 0; i < iLines.size(); i++)
			((P)iLines.get(0).getWidget(4)).getWidget(0).setVisible(iCanWaitList);
	}

	public void validate(final AsyncCallback<Boolean> callback) {
		validate(null, callback);
	}

	public void validate(Boolean updateLastRequest, final AsyncCallback<Boolean> callback) {
		try {
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
			if (cr.getAcademicSessionId() == null)
				throw new SectioningException(MESSAGES.sessionSelectorNoSession());
			fillInCourses(cr); fillInAlternatives(cr);
			if (updateLastRequest != null)
				cr.setUpdateLastRequest(updateLastRequest);
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
		} catch (Exception e) {
			callback.onFailure(e);
		}
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
			if (!course[0].isEnabled())
				req.setReadOnly(course[2].isSaved() ? 2 : course[1].isSaved() ? 1 : 0);
			cr.getCourses().add(req);
		}
	}
	
	public void fillInAlternatives(CourseRequestInterface cr) {
		for (CourseSelectionBox[] course: iAlternatives) {
			CourseRequestInterface.Request req = new CourseRequestInterface.Request();
			req.setRequestedCourse(course[0].getValue());
			req.setFirstAlternative(course[1].getValue());
			req.setSecondAlternative(course[2].getValue());
			if (!course[0].isEnabled())
				req.setReadOnly(course[2].isSaved() ? 2 : course[1].isSaved() ? 1 : 0);
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
		clear();
		while (iCourses.size() < request.getCourses().size()) addCourseLine();
		for (int idx = 0; idx < request.getCourses().size(); idx++) {
			iCourses.get(idx)[0].setValue(request.getCourses().get(idx).getRequestedCourse(), true);
			iCourses.get(idx)[1].setValue(request.getCourses().get(idx).getFirstAlternative(), true);
			iCourses.get(idx)[2].setValue(request.getCourses().get(idx).getSecondAlternative(), true);
			iCourses.get(idx)[0].setWaitList(request.getCourses().get(idx).isWaitList());
			if (request.getCourses().get(idx).isReadOnly()) {
				iCourses.get(idx)[0].setSaved(request.getCourses().get(idx).isRequestedCourseReadOnly());
				iCourses.get(idx)[1].setSaved(request.getCourses().get(idx).isFirstAlternativeReadOnly());
				iCourses.get(idx)[2].setSaved(request.getCourses().get(idx).isSecondAlternativeReadOnly());
				iCourses.get(idx)[0].setEnabled(false);
				iCourses.get(idx)[1].setEnabled(false); iCourses.get(idx)[1].setHint("");
				iCourses.get(idx)[2].setEnabled(false); iCourses.get(idx)[2].setHint("");
				iCourses.get(idx)[0].setWaitListEnabled(false);
			} else {
				iCourses.get(idx)[0].setSaved(false);
				iCourses.get(idx)[1].setSaved(false);
				iCourses.get(idx)[2].setSaved(false);
			}
		}
		while (iAlternatives.size() < request.getAlternatives().size()) addAlternativeLine();
		for (int idx = 0; idx < request.getAlternatives().size(); idx++) {
			iAlternatives.get(idx)[0].setValue(request.getAlternatives().get(idx).getRequestedCourse(), true);
			iAlternatives.get(idx)[1].setValue(request.getAlternatives().get(idx).getFirstAlternative(), true);
			iAlternatives.get(idx)[2].setValue(request.getAlternatives().get(idx).getSecondAlternative(), true);
			if (request.getAlternatives().get(idx).isReadOnly()) {
				iAlternatives.get(idx)[0].setSaved(request.getAlternatives().get(idx).isRequestedCourseReadOnly());
				iAlternatives.get(idx)[1].setSaved(request.getAlternatives().get(idx).isFirstAlternativeReadOnly());
				iAlternatives.get(idx)[2].setSaved(request.getAlternatives().get(idx).isSecondAlternativeReadOnly());
				iAlternatives.get(idx)[0].setEnabled(false);
				iAlternatives.get(idx)[1].setEnabled(false); iAlternatives.get(idx)[1].setHint("");
				iAlternatives.get(idx)[2].setEnabled(false); iAlternatives.get(idx)[2].setHint("");				
			} else {
				iAlternatives.get(idx)[0].setSaved(false);
				iAlternatives.get(idx)[1].setSaved(false);
				iAlternatives.get(idx)[2].setSaved(false);
			}
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
				c[i].setSaved(false);
				if (i>0) {
					c[i].setEnabled(false);
					c[i].setHint("");
				} else {
					c[i].setEnabled(true);
				}
			}
			c[0].setWaitList(false);
			c[0].setWaitListEnabled(true);
		}
		for (CourseSelectionBox[] c: iAlternatives) {
			for (int i=0;i<3;i++) {
				c[i].setValue("");
				c[i].setSaved(false);
				if (i>0) {
					c[i].setEnabled(false);
					c[i].setHint("");
				} else {
					c[i].setEnabled(true);
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

	protected void clear(CourseSelectionBox[] c) {
		for (int i=0;i<3;i++) {
			c[i].setValue("");
			c[i].setSaved(false);
			if (i>0) {
				c[i].setEnabled(false);
				c[i].setHint("");
			} else {
				c[i].setEnabled(true);
			}
		}
		c[0].setWaitList(false);
		c[0].setWaitListEnabled(true);
	}

	protected void clearErrors() {
		for (CourseSelectionBox[] c: iCourses) {
			for (int i=0;i<3;i++)
				c[i].setError(null);
		}
		for (CourseSelectionBox[] c: iAlternatives) {
			for (int i=0;i<3;i++)
				c[i].setError(null);
		}
	}

	public Command addCourse(String text) {
		for (final CourseSelectionBox[] course: iCourses) {
			if (course[0].getValue().isEmpty()) {
				clear(course);
				course[0].setValue(text, true);
				return new Command() {
					@Override
					public void execute() {
						course[0].setValue(null, true);
						clearErrors();
					}
				};
			}
		}
		addCourseLine();
		final CourseSelectionBox[] course = iCourses.get(iCourses.size() - 1);
		course[0].setValue(text, true);
		return new Command() {
			@Override
			public void execute() {
				course[0].setValue(null, true);
				clearErrors();
			}
		};
	}

	public boolean hasCourse(String text) {
		for (final CourseSelectionBox[] course: iCourses) {
			if (text.equalsIgnoreCase(course[0].getValue()) || text.equalsIgnoreCase(course[1].getValue()) || text.equalsIgnoreCase(course[2].getValue()))
				return true;
		}
		for (final CourseSelectionBox[] course: iAlternatives) {
			if (text.equalsIgnoreCase(course[0].getValue()) || text.equalsIgnoreCase(course[1].getValue()) || text.equalsIgnoreCase(course[2].getValue()))
				return true;
		}
		return false;
	}

	public void dropCourse(String text) {
		for (final CourseSelectionBox[] course: iCourses) {
			if (text.equalsIgnoreCase(course[0].getValue()) || text.equalsIgnoreCase(course[1].getValue()) || text.equalsIgnoreCase(course[2].getValue())) {
				course[0].remove();
				return;
			}
		}
		for (final CourseSelectionBox[] course: iAlternatives) {
			if (text.equalsIgnoreCase(course[0].getValue()) || text.equalsIgnoreCase(course[1].getValue()) || text.equalsIgnoreCase(course[2].getValue())) {
				course[0].remove();
				return;
			}
		}
	}

	public void dropCourse(ClassAssignmentInterface.ClassAssignment assignment) {
		if (assignment.isFreeTime()) {
			String free = assignment.getTimeString(new String[] {"M","T","W","R","F","S","X"}, true, "");
			for (final CourseSelectionBox[] course: iCourses) {
				try {
					boolean changed = false;
					String text = null;
					if (course[0].getFreeTimes() != null)
						for (CourseRequestInterface.FreeTime ft: course[0].getFreeTimes().parseFreeTime(course[0].getValue()))
							if (free.equals(ft.toString(new String[] {"M","T","W","R","F","S","X"}, true))) {
								changed = true;
							} else {
								if (text == null)
									text = CONSTANTS.freePrefix() + ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
								else
									text += ", " + ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
							}
					if (changed) {
						if (text == null) {
							course[0].remove();
						} else {
							course[0].setValue(text, true);
						}
						return;
					}
				} catch (Exception e) {}
			}
		} else {
			for (final CourseSelectionBox[] course: iCourses) {
				if (assignment.equalsIgnoreCase(course[0].getValue()) || assignment.equalsIgnoreCase(course[1].getValue()) || assignment.equalsIgnoreCase(course[2].getValue())) {
					course[0].remove();
					return;
				}
			}
			for (final CourseSelectionBox[] course: iAlternatives) {
				if (assignment.equalsIgnoreCase(course[0].getValue()) || assignment.equalsIgnoreCase(course[1].getValue()) || assignment.equalsIgnoreCase(course[2].getValue())) {
					course[0].remove();
					return;
				}
			}
		}
	}
}
