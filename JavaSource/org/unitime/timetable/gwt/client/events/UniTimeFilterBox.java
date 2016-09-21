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
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.HasAriaLabel;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.client.widgets.FilterBox.SuggestionsProvider;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author Tomas Muller
 */
public abstract class UniTimeFilterBox<T extends FilterRpcRequest> extends Composite implements HasValue<String>, Focusable, HasAllKeyHandlers, HasAllFocusHandlers, HasAriaLabel {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private AcademicSessionProvider iAcademicSession;
	protected UniTimeWidget<FilterBox> iFilter;
	private boolean iInitialized = false;
	
	public UniTimeFilterBox(AcademicSessionProvider session) {
		iFilter = new UniTimeWidget<FilterBox>(new FilterBox());
		iFilter.addStyleName("unitime-FilterBoxContainer");
		
		iFilter.getWidget().setSuggestionsProvider(new SuggestionsProvider() {
			@Override
			public void getSuggestions(List<Chip> chips, String text, final AsyncCallback<Collection<Suggestion>> callback) {
				Long sessionId = (iAcademicSession == null ? null : iAcademicSession.getAcademicSessionId());
				if (sessionId == null && iAcademicSession != null) {
					callback.onSuccess(null);
					return;
				}
				RPC.execute(createRpcRequest(FilterRpcRequest.Command.SUGGESTIONS, iAcademicSession == null ? null : iAcademicSession.getAcademicSessionId(), chips, text), new AsyncCallback<FilterRpcResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					@Override
					public void onSuccess(FilterRpcResponse result) {
						List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
						if (result.hasSuggestions()) {
							for (FilterRpcResponse.Entity s: result.getSuggestions())
								addSuggestion(suggestions, s);
						}
						callback.onSuccess(suggestions);
					}
					
				});
			}
		});
		
		initWidget(iFilter);
		
		iAcademicSession = session;
		
		if (iAcademicSession != null)
			iAcademicSession.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
				@Override
				public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
					if (event.isChanged()) init(true, event.getNewAcademicSessionId(), null);
				}
			});
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				init(true, iAcademicSession == null ? null : iAcademicSession.getAcademicSessionId(), null);
			}
		});
	}
	
	protected void addSuggestion(List<FilterBox.Suggestion> suggestions, FilterRpcResponse.Entity entity) {
		String command = entity.getProperty("command", null);
		if (command == null) {
			suggestions.add(new FilterBox.Suggestion(entity.getName(), entity.getAbbreviation(), entity.getProperty("hint", null)));
		} else {
			Chip old = ("true".equals(entity.getProperty("single", "true")) ? getChip(command) : null);
			suggestions.add(new FilterBox.Suggestion(new Chip(command, entity.getAbbreviation()).withLabel(entity.getName()).withToolTip(entity.getProperty("hint", null)), old));
		}
	}
	
	protected void initAsync() {
		setValue(getValue());
	}

	protected void init(final boolean init, Long academicSessionId, final Command onSuccess) {
		if (academicSessionId == null && iAcademicSession != null) {
			setHint(MESSAGES.hintNoSession());
		} else {
			if (init) {
				setHint(MESSAGES.waitLoadingDataForSession(iAcademicSession == null ? "" : iAcademicSession.getAcademicSessionName()));
				iInitialized = false;
			}
			final String value = iFilter.getWidget().getValue();
			RPC.execute(createRpcRequest(FilterRpcRequest.Command.LOAD, academicSessionId, iFilter.getWidget().getChips(null), iFilter.getWidget().getText()), new AsyncCallback<FilterRpcResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					setErrorHint(caught.getMessage());
					ToolBox.checkAccess(caught);
				}
				@Override
				public void onSuccess(FilterRpcResponse result) {
					clearHint();
					if (!value.equals(iFilter.getWidget().getValue())) return;
					onLoad(result);
					for (FilterBox.Filter filter: iFilter.getWidget().getFilters())
						populateFilter(filter, result.getEntities(filter.getCommand()));
					if (onSuccess != null) onSuccess.execute();
					if (init) {
						iInitialized = true;
						initAsync();
					}
				}
			});
		}
	}
	
	protected void onLoad(FilterRpcResponse result) {}
	
	public boolean isInitialized() { return iInitialized; }
	
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if (filter != null && filter instanceof FilterBox.StaticSimpleFilter) {
			FilterBox.StaticSimpleFilter simple = (FilterBox.StaticSimpleFilter)filter;
			List<Chip> chips = new ArrayList<FilterBox.Chip>();
			if (entities != null) {
				for (FilterRpcResponse.Entity entity: entities) {
					FilterBox.Chip chip = new FilterBox.Chip(filter.getCommand(), entity.getAbbreviation())
							.withLabel(entity.getName())
							.withCount(entity.getCount())
							.withTranslatedCommand(filter.getLabel())
							.withTranslatedValue(entity.getProperty("translated-value", null));
					chips.add(chip);
					if (!chip.getValue().equals(chip.getTranslatedValue()))
						iFilter.getWidget().fixLabel(chip);
				}
			}
			simple.setValues(chips);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return iFilter.getWidget().addValueChangeHandler(handler);
	}

	@Override
	public String getValue() {
		return iFilter.getWidget().getValue().trim();
	}

	@Override
	public void setValue(String value) {
		iFilter.getWidget().setValue(value);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		iFilter.getWidget().setValue(value, fireEvents);
		if (fireEvents)
			init(false, iAcademicSession == null ? null : iAcademicSession.getAcademicSessionId(), new Command() {
				@Override
				public void execute() {
					if (iFilter.getWidget().isFilterPopupShowing())
						iFilter.getWidget().showFilterPopup();
				}
			});
	}
	
	public void clearHint() {
		iFilter.clearHint();
		iFilter.getWidget().setAriaLabel(toAriaString());
	}
	
	public void setErrorHint(String error) {
		iFilter.setErrorHint(error);
	}

	public void setHint(String hint) {
		iFilter.setHint(hint);
	}
	
	protected abstract T createRpcRequest();
	
	protected T createRpcRequest(FilterRpcRequest.Command command, Long sessionId, List<FilterBox.Chip> chips, String text) {
		T request = createRpcRequest();
		request.setCommand(command);
		request.setSessionId(sessionId);
		if (chips != null)
			for (Chip chip: chips)
				request.addOption(chip.getCommand(), chip.getValue());
		request.setText(text);
		return request;
	}
	
	public void getElements(final AsyncCallback<List<FilterRpcResponse.Entity>> callback) {
		RPC.execute(getElementsRequest(), new AsyncCallback<FilterRpcResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(FilterRpcResponse result) {
				callback.onSuccess(result.getResults());
			}
		});
	}
	
	public T getElementsRequest() {
		return createRpcRequest(FilterRpcRequest.Command.ENUMERATE, iAcademicSession == null ? null : iAcademicSession.getAcademicSessionId(), iFilter.getWidget().getChips(null), iFilter.getWidget().getText());
	}
	
	public void addFilter(FilterBox.Filter filter) {
		iFilter.getWidget().addFilter(filter);
	}
	
	public Chip getChip(String command) {
		return iFilter.getWidget().getChip(command);
	}
	
	public void addChip(FilterBox.Chip chip, boolean fireEvents) {
		iFilter.getWidget().addChip(chip, fireEvents);
	}
	
	public boolean removeChip(FilterBox.Chip chip, boolean fireEvents) {
		return iFilter.getWidget().removeChip(chip, fireEvents);
	}
	
	public boolean hasChip(FilterBox.Chip chip) {
		return iFilter.getWidget().hasChip(chip);
	}
	
	protected void fireValueChangeEvent() {
		ValueChangeEvent.fire(iFilter.getWidget(), iFilter.getWidget().getValue());
	}
	
	public boolean isFilterPopupShowing() {
		return iFilter.getWidget().isFilterPopupShowing();
	}
	
	public void showFilterPopup() {
		iFilter.getWidget().showFilterPopup();
	}
	
	public void hideFilterPopup() {
		iFilter.getWidget().hideFilterPopup();
	}
	
	protected Long getAcademicSessionId() {
		return iAcademicSession == null ? null : iAcademicSession.getAcademicSessionId();
	}
	
	@Override
	public int getTabIndex() {
		return iFilter.getWidget().getTabIndex();
	}


	@Override
	public void setTabIndex(int index) {
		iFilter.getWidget().setTabIndex(index);
	}

	@Override
	public void setAccessKey(char key) {
		iFilter.getWidget().setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		iFilter.getWidget().setFocus(focused);
	}

	@Override
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return iFilter.getWidget().addKeyUpHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return iFilter.getWidget().addKeyDownHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return iFilter.getWidget().addKeyPressHandler(handler);
	}
	
	@Override
	public HandlerRegistration addFocusHandler(FocusHandler handler) {
		return iFilter.getWidget().addFocusHandler(handler);
	}
	
	@Override
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return iFilter.getWidget().addBlurHandler(handler);
	}
	
	public boolean isShowSuggestionsOnFocus() {
		return iFilter.getWidget().isShowSuggestionsOnFocus();
	}
	
	public void setShowSuggestionsOnFocus(boolean showSuggestionsOnFocus) {
		iFilter.getWidget().setShowSuggestionsOnFocus(showSuggestionsOnFocus);
	}
	
	public String toAriaString() {
		return iFilter.getWidget().toAriaString();
	}
	
	@Override
	public String getAriaLabel() {
		return iFilter.getWidget().getAriaLabel();
	}
	
	@Override
	public void setAriaLabel(String text) {
		iFilter.getWidget().setAriaLabel(text);
	}
	
	public void setDefaultValueProvider(TakesValue<String> defaultValue) {
		iFilter.getWidget().setDefaultValueProvider(defaultValue);
	}
	
	public TakesValue<String> getDefaultValueProvider() {
		return iFilter.getWidget().getDefaultValueProvider();
	}
}
