/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.client.widgets.FilterBox.SuggestionsProvider;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;

public abstract class UniTimeFilterBox extends Composite implements HasValue<String> {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private AcademicSessionProvider iAcademicSession;
	private UniTimeWidget<FilterBox> iFilter;
	
	public UniTimeFilterBox(AcademicSessionProvider session) {
		iFilter = new UniTimeWidget<FilterBox>(new FilterBox());
		
		iFilter.getWidget().setSuggestionsProvider(new SuggestionsProvider() {
			@Override
			public void getSuggestions(List<Chip> chips, String text, final AsyncCallback<Collection<Suggestion>> callback) {
				Long sessionId = iAcademicSession.getAcademicSessionId();
				if (sessionId == null) {
					callback.onSuccess(null);
					return;
				}
				RPC.execute(createRpcRequest(FilterRpcRequest.Command.SUGGESTIONS, iAcademicSession.getAcademicSessionId(), chips, text), new AsyncCallback<FilterRpcResponse>() {

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
		
		iAcademicSession.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				if (event.isChanged()) init(true, event.getNewAcademicSessionId(), new Command() {
					@Override
					public void execute() {
						setValue(getValue());
					}
				});
			}
		});
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				init(true, iAcademicSession.getAcademicSessionId(), null);
			}
		});
	}
	
	protected void addSuggestion(List<FilterBox.Suggestion> suggestions, FilterRpcResponse.Entity entity) {
		suggestions.add(new FilterBox.Suggestion(entity.getName(), entity.getAbbreviation(), entity.getProperty("hint", null)));
	}

	protected void init(final boolean init, Long academicSessionId, final Command onSuccess) {
		if (academicSessionId == null) {
			iFilter.setHint("No academic session is selected.");
		} else {
			if (init) iFilter.setHint("Loading data for " + iAcademicSession.getAcademicSessionName() + " ...");
			final String value = iFilter.getWidget().getValue();
			RPC.execute(createRpcRequest(FilterRpcRequest.Command.LOAD, academicSessionId, iFilter.getWidget().getChips(null), iFilter.getWidget().getText()), new AsyncCallback<FilterRpcResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					iFilter.setErrorHint(caught.getMessage());
					ToolBox.checkAccess(caught);
				}
				@Override
				public void onSuccess(FilterRpcResponse result) {
					iFilter.clearHint();
					if (!value.equals(iFilter.getWidget().getValue())) return;
					for (FilterBox.Filter filter: iFilter.getWidget().getFilters())
						populateFilter(filter, result.getEntities(filter.getCommand()));
					if (onSuccess != null) onSuccess.execute();
				}
			});
		}
	}
	
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if (filter != null && filter instanceof FilterBox.StaticSimpleFilter) {
			FilterBox.StaticSimpleFilter simple = (FilterBox.StaticSimpleFilter)filter;
			List<FilterBox.Chip> chips = new ArrayList<FilterBox.Chip>();
			if (entities != null) {
				for (FilterRpcResponse.Entity entity: entities)
					chips.add(new FilterBox.Chip(filter.getCommand(), entity.getName(), entity.getCount() <= 0 ? null : "(" + entity.getCount() + ")"));
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
		return iFilter.getWidget().getValue();
	}

	@Override
	public void setValue(String value) {
		iFilter.getWidget().setValue(value);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		iFilter.getWidget().setValue(value, fireEvents);
		if (fireEvents)
			init(false, iAcademicSession.getAcademicSessionId(), new Command() {
				@Override
				public void execute() {
					if (iFilter.getWidget().isFilterPopupShowing())
						iFilter.getWidget().showFilterPopup();
				}
			});
	}
	
	public void clearHint() {
		iFilter.clearHint();
	}
	
	public void setErrorHint(String error) {
		iFilter.setErrorHint(error);
	}

	public void setHint(String hint) {
		iFilter.setHint(hint);
	}
	
	protected abstract FilterRpcRequest createRpcRequest();
	
	protected FilterRpcRequest createRpcRequest(FilterRpcRequest.Command command, Long sessionId, List<FilterBox.Chip> chips, String text) {
		FilterRpcRequest request = createRpcRequest();
		request.setCommand(command);
		request.setSessionId(sessionId);
		if (chips != null)
			for (Chip chip: chips)
				request.addOption(chip.getCommand(), chip.getValue());
		request.setText(text);
		return request;
	}
	
	public void getRooms(final AsyncCallback<List<FilterRpcResponse.Entity>> callback) {
		RPC.execute(createRpcRequest(FilterRpcRequest.Command.ENUMERATE, iAcademicSession.getAcademicSessionId(), iFilter.getWidget().getChips(null), iFilter.getWidget().getText()), new AsyncCallback<FilterRpcResponse>() {

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
	
	public static abstract class FilterRpcRequest implements GwtRpcRequest<FilterRpcResponse> {
		public static enum Command implements IsSerializable {
			LOAD,
			SUGGESTIONS,
			ENUMERATE,
		}
		
		private Command iCommand;
		private Long iSessionId;
		private String iText;
		private HashMap<String, Set<String>> iOptions;
		
		public FilterRpcRequest() {}
		
		public Command getCommand() { return iCommand; }
		public void setCommand(Command command) { iCommand = command; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public String getText() { return iText; }
		public void setText(String text) { iText = text; }
		public Set<String> getOptions(String command) {
			return (iOptions == null ? null : iOptions.get(command));
		}
		public boolean hasOption(String command) {
			Set<String> options = getOptions(command);
			return (options != null && options.size() == 1);
		}
		public String getOption(String command) {
			Set<String> options = getOptions(command);
			return (options == null || options.isEmpty() ? null : options.iterator().next());
		}
		public Map<String, Set<String>> getOptions() {
			return iOptions;
		}
		public boolean hasOptions(String command) {
			Set<String> options = getOptions(command);
			return (options != null && !options.isEmpty());
		}
		public void addOption(String command, String value) {
			if (iOptions == null) iOptions = new HashMap<String, Set<String>>();
			Set<String> options = iOptions.get(command);
			if (options == null) {
				options = new HashSet<String>();
				iOptions.put(command, options);
			}
			options.add(value);
		}
		
		@Override
		public String toString() { return getCommand().name() + "(" + getSessionId() + "," + iOptions + "," + getText() + ")"; }
	}
	
	public static class FilterRpcResponse implements GwtRpcResponse {
		private HashMap<String, ArrayList<Entity>> iEntities = null;
		
		public FilterRpcResponse() {}
		
		public boolean hasEntities(String type) {
			List<Entity> entities = getEntities(type);
			return entities != null && !entities.isEmpty();
		}
		
		public List<Entity> getEntities(String type) {
			return (iEntities == null ? null : iEntities.get(type));
		}
		
		public void add(String type, Entity entity) {
			if (iEntities == null) iEntities = new HashMap<String, ArrayList<Entity>>();
			ArrayList<Entity> entities = iEntities.get(type);
			if (entities == null) {
				entities = new ArrayList<Entity>();
				iEntities.put(type, entities);
			}
			entities.add(entity);
		}
		
		public void add(String type, Collection<Entity> entity) {
			if (iEntities == null) iEntities = new HashMap<String, ArrayList<Entity>>();
			ArrayList<Entity> entities = iEntities.get(type);
			if (entities == null) {
				entities = new ArrayList<Entity>(entity);
				iEntities.put(type, entities);
			} else {
				entities.addAll(entity);
			}
		}
		
		public void addResult(Entity entity) { add("results", entity); }
		public boolean hasResults() { return hasEntities("results"); }
		public List<Entity> getResults() { return getEntities("results"); }
		
		public void addSuggestion(String message, String replacement, String hint) {
			add("suggestion", new Entity(0l, replacement, message, "hint", hint));
		}
		
		public boolean hasSuggestions() { return hasEntities("suggestion"); }
		
		public List<Entity> getSuggestions() { return getEntities("suggestion"); }
		
		public static class Entity implements IsSerializable, Comparable<Entity> {
			private Long iUniqueId;
			private String iAbbv, iName;
			private int iCount = 0;
			private HashMap<String, String> iParams;
			
			public Entity() {}
			
			public Entity(Long uniqueId, String abbv, String name, String... properties) {
				iUniqueId = uniqueId;
				iAbbv = abbv;
				iName = name;
				for (int i = 0; i + 1 < properties.length; i += 2)
					if (properties[i + 1] != null)
						setProperty(properties[i], properties[i + 1]);
			}
			
			public Long getUniqueId() { return iUniqueId; }
			public String getAbbreviation() { return iAbbv; }
			public String getName() { return iName; }
			public int getCount() { return iCount; }
			public void setCount(int count) { iCount = count; }
			public void incCount() { iCount ++; }
			
			public void setProperty(String property, String value) {
				if (iParams == null) iParams = new HashMap<String, String>();
				iParams.put(property, value);
			}
			
			public String getProperty(String property, String defaultValue) {
				String value = (iParams == null ? null : iParams.get(property));
				return (value == null ? defaultValue : value);
			}
			
			public int hasCode() { return getUniqueId().hashCode(); }
			public boolean equals(Object o) {
				if (o == null || !(o instanceof Entity)) return false;
				Entity e = (Entity)o;
				return getUniqueId().equals(e.getUniqueId()) && getName().equals(e.getName());
			}
			public int compareTo(Entity e) {
				if (getUniqueId() < 0) {
					return (e.getUniqueId() >= 0 ? -1 : e.getUniqueId().compareTo(getUniqueId()));
				} else if (e.getUniqueId() < 0) return 1;
				return getName().compareToIgnoreCase(e.getName());
			}
			public String toString() { return getName(); }
		}
		
		public String toString() { return (iEntities == null ? "null" : iEntities.toString()); }
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
	
	protected Long getAcademicSessionId() {
		return iAcademicSession.getAcademicSessionId();
	}
}
