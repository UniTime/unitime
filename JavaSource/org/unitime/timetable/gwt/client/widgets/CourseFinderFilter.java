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
package org.unitime.timetable.gwt.client.widgets;

import java.util.Date;

import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Filter;
import org.unitime.timetable.gwt.shared.EventInterface.RequestSessionDetails;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;

public class CourseFinderFilter extends SimpleForm implements HasValue<Filter> {
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.patternDateFormat());
	
	private SingleDateSelector iDateFrom, iDateTo;
	private NumberBox iCreditFrom, iCreditTo;
	private StudentSectioningContext iContext;
	private TextBox iInstructor;
	private Timer iChangeTimer;
	private FilterButton iFilterButton;
	private P iFilterLabel;
	AbsolutePanel iDates;
	private P iFilterText;
	
	public CourseFinderFilter(StudentSectioningContext context) {
		iContext = context;
		
		iFilterButton = new FilterButton();
		iFilterButton.addStyleName("filter-button");
		iFilterLabel = new P("filter-label");
		iFilterLabel.setText(MESSAGES.sectCourseFinderFilter());
		P filter = new P("filter-header");
		filter.add(iFilterButton);
		filter.add(iFilterLabel);
		iFilterButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue()) {
					iFilterLabel.setText(MESSAGES.propSchedulingPrefDates());
					for (int row = 1; row < getRowCount(); row++)
						getRowFormatter().setVisible(row, true);
					setWidget(0, 1, iDates);
				} else {
					iFilterLabel.setText(MESSAGES.sectCourseFinderFilter());
					setWidget(0, 1, iFilterText);
					iFilterText.setText(getFilterText());
					for (int row = 1; row < getRowCount(); row++)
						getRowFormatter().setVisible(row, false);
				}
			}
		});
		iFilterText = new P("filter-text");
		
		addStyleName("filter");
		iDates = new AbsolutePanel();
		iDates.setStyleName("dates");
		P from = new P("from"); from.setText(MESSAGES.propSchedulingPrefDatesFrom()); iDates.add(from);
		iDateFrom = new SingleDateSelector(null);
		iDates.add(iDateFrom);
		P to = new P("to"); to.setText(MESSAGES.propSchedulingPrefDatesTo()); iDates.add(to);
		iDateTo = new SingleDateSelector(null);
		iDates.add(iDateTo);
		addRow(filter, iDates);
		
		AbsolutePanel c = new AbsolutePanel();
		c.setStyleName("credit");
		from = new P("from"); from.setText(MESSAGES.propCourseFinderFilterCreditFrom()); c.add(from);
		iCreditFrom = new NumberBox(); c.add(iCreditFrom);
		to = new P("to"); to.setText(MESSAGES.propCourseFinderFilterCreditTo()); c.add(to);
		iCreditTo = new NumberBox(); c.add(iCreditTo);
		addRow(MESSAGES.propCourseFinderFilterCredit(), c);
		
		iInstructor = new AriaTextBox();
		iInstructor.setStyleName("gwt-SuggestBox");
		iInstructor.addStyleName("instructor");
		addRow(MESSAGES.propCourseFinderFilterInstructor(), iInstructor);
		
		iFilterButton.setValue(false, true);
		
		iChangeTimer = new Timer() {
			@Override
			public void run() {
				ValueChangeEvent.fire(CourseFinderFilter.this, getValue());
			}
		};
		
		iDateTo.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iChangeTimer.schedule(500);
			}
		});
		iDateFrom.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iChangeTimer.schedule(500);
			}
		});
		iCreditFrom.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iChangeTimer.schedule(500);
			}
		});
		iCreditTo.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iChangeTimer.schedule(500);
			}
		});
		iInstructor.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iChangeTimer.schedule(500);
			}
		});
	}
	
	public String getFilterText() {
		Filter filter = getValue();
		String ret = "";
		if (filter.hasDates()) {
			if (filter.getClassFrom() != null && filter.getClassTo() != null) {
				ret += MESSAGES.filterClassesBetweenDates(sDF.format(filter.getClassFrom()), sDF.format(filter.getClassTo()));
			} else if (filter.getClassFrom() != null) {
				ret += MESSAGES.filterClassesFromDate(sDF.format(filter.getClassFrom()));
			} else {
				ret += MESSAGES.filterClassesToDate(sDF.format(filter.getClassTo()));
			}
		}
		if (filter.getCreditMin() != null) {
			if (!ret.isEmpty()) ret += ", ";
			if (filter.getCreditMax() != null) {
				if (filter.getCreditMin().equals(filter.getCreditMax()))
					ret += MESSAGES.filterCreditEquals(filter.getCreditMin());
				else
					ret += MESSAGES.filterCreditBetween(filter.getCreditMin(), filter.getCreditMax());
			} else {
				ret += MESSAGES.filterCreditFrom(filter.getCreditMin());
			}
		} else if (filter.getCreditMax() != null) {
			if (!ret.isEmpty()) ret += ", ";
			ret += MESSAGES.filterCreditTo(filter.getCreditMax());
		}
		if (filter.hasInstructor()) {
			if (!ret.isEmpty()) ret += ", ";
			ret += MESSAGES.filterInstructor(filter.getInstructor());
		}
		if (!ret.isEmpty())
			return MESSAGES.messageCourseFinderFilterText(ret);
		return ret;
	}
	
	public void init() {
		String showFilter = Cookies.getCookie("UniTime:CourseFinderFilter");
		iFilterButton.setValue("1".equals(showFilter), true);
		if (iContext.hasSessionDates()) {
			iDateFrom.init(iContext);
			iDateTo.init(iContext);
		} else if (iContext.getAcademicSessionId() != null) {
			final Long sessionId = iContext.getAcademicSessionId();
			RPC.execute(new RequestSessionDetails(sessionId), new AsyncCallback<GwtRpcResponseList<SessionMonth>>() {
				@Override
				public void onFailure(Throwable caught) {
					iDateFrom.setErrorHint(caught.getMessage());
					iDateTo.setErrorHint(caught.getMessage());
				}
				@Override
				public void onSuccess(GwtRpcResponseList<SessionMonth> result) {
					if (sessionId.equals(iContext.getAcademicSessionId()))
						iContext.setSessionDates(result);
					iDateFrom.init(result);
					iDateTo.init(result);
				}
			});
		}
	}
	
	@Override
	public Filter getValue() {
		Filter filter = new Filter();
		filter.setClassFrom(iDateFrom.getValue());
		filter.setClassTo(iDateTo.getValue());
		filter.setCreditMin(iCreditFrom.toFloat());
		filter.setCreditMax(iCreditTo.toFloat());
		filter.setInstructor(iInstructor.getValue());
		return filter;
	}
	
	@Override
	public void setValue(Filter filter) {
		if (filter == null) {
			iDateFrom.setValue(null);
			iDateTo.setValue(null);
			iCreditFrom.setValue((Number)null);
			iCreditTo.setValue((Number)null);
			iInstructor.setValue("");
		} else {
			iDateFrom.setValue(filter.getClassFrom());
			iDateTo.setValue(filter.getClassTo());
			iCreditFrom.setValue(filter.getCreditMin());
			iCreditTo.setValue(filter.getCreditMax());
			iInstructor.setValue(filter.hasInstructor() ? filter.getInstructor() : "");
		}
		iFilterText.setText(getFilterText());
	}

	@Override
	public void setValue(Filter value, boolean fireEvents) {
		setValue(value);
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
	
	public boolean isPopupShowing() {
		return iDateFrom.isPopupShowing() || iDateTo.isPopupShowing();
	}
	
	public boolean isCanSubmit(NativePreviewEvent event) {
		if (iDateFrom.isPopupShowing()) return false;
		if (iDateTo.isPopupShowing()) return false;
		return InputElement.is(event.getNativeEvent().getEventTarget()) || BodyElement.is(event.getNativeEvent().getEventTarget());
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Filter> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
	
	static class FilterButton extends Image implements HasValue<Boolean>, Focusable, HasAllFocusHandlers {
		private boolean iOpened = false;
		
		public FilterButton() {
			super(RESOURCES.treeClosed());
			Roles.getButtonRole().set(getElement());
			sinkEvents(Event.ONKEYUP);
			setAltText(MESSAGES.descCourseFinderFilterClosed());
			setTabIndex(0);
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setValue(!getValue(), true);
					Cookies.setCookie("UniTime:CourseFinderFilter", getValue() ? "1" : "0");
				}
			});
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			super.onBrowserEvent(event);
		    if (event.getTypeInt() == Event.ONKEYUP) {
		    	if (event.getKeyCode() == KeyCodes.KEY_ENTER || event.getKeyCode() == KeyCodes.KEY_SPACE)
		    		onClick();
		    }
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}

		@Override
		public Boolean getValue() {
			return iOpened;
		}

		@Override
		public void setValue(Boolean value) {
			iOpened = value;
			setResource(iOpened ? RESOURCES.treeOpen() : RESOURCES.treeClosed());
			setAltText(iOpened ? MESSAGES.descCourseFinderFilterOpened() : MESSAGES.descCourseFinderFilterClosed());
		}

		@Override
		public void setValue(Boolean value, boolean fireEvents) {
			setValue(value);
			if (fireEvents)
				ValueChangeEvent.fire(FilterButton.this, getValue());
		}

		@Override
		public int getTabIndex() {
			return getElement().getTabIndex();
		}

		@Override
		public void setAccessKey(char key) {
			setAccessKey(getElement(), key);
		}
		
		private native void setAccessKey(Element elem, char key) /*-{
			elem.accessKey = String.fromCharCode(key);
		}-*/;

		@Override
		public void setFocus(boolean focused) {
			if (focused)
				getElement().focus();
			else
				getElement().blur();
		}

		@Override
		public void setTabIndex(int index) {
			getElement().setTabIndex(index);
		}

		@Override
		public HandlerRegistration addFocusHandler(FocusHandler handler) {
			return addDomHandler(handler, FocusEvent.getType());
		}

		@Override
		public HandlerRegistration addBlurHandler(BlurHandler handler) {
			return addDomHandler(handler, BlurEvent.getType());
		}
		
		@Override
		public void setAltText(String altText) {
			super.setAltText(altText);
			if (getTitle() == null || getTitle().isEmpty())
				setTitle(altText);
		}
		
		protected void onClick() {
		    getElement().dispatchEvent(Document.get().createClickEvent(1, 0, 0, 0, 0, false, false, false, false));
		}
	}
}
