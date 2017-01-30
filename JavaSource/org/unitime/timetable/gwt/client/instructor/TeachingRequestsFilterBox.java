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
package org.unitime.timetable.gwt.client.instructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.events.UniTimeFilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsFilterRpcRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * @author Tomas Muller
 */
public class TeachingRequestsFilterBox extends UniTimeFilterBox<TeachingRequestsFilterRpcRequest> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private Boolean iAssigned;
	private ListBox iDepartments;
	private String iDepartmentAttribute;
	private String iDepartmentTag;
	private AriaSuggestBox iCourse;
	private Chip iLastCourse;
	private AriaSuggestBox iInstructor;
	private Chip iLastInstructor;
	
	public TeachingRequestsFilterBox(Boolean assigned) {
		super(null);
		iAssigned = assigned;
		
		
		setShowSuggestionsOnFocus(false);

		iDepartmentAttribute = (assigned != null ? "subject" : "department"); 
		iDepartmentTag = (assigned != null ? MESSAGES.tagSubjectArea() : MESSAGES.tagDepartment());
				
		iDepartments = new ListBox();
		iDepartments.setMultipleSelect(false);
		iDepartments.setWidth("100%");
		
		addFilter(new FilterBox.CustomFilter(iDepartmentAttribute, iDepartmentTag, iDepartments) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					Chip oldChip = getChip(iDepartmentAttribute);
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iDepartments.getItemCount(); i++) {
						Chip chip = new Chip(iDepartmentAttribute, iDepartments.getValue(i)).withTranslatedCommand(iDepartmentTag);
						String name = iDepartments.getItemText(i);
						if (iDepartments.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						} else if (text.length() > 2 && (name.toLowerCase().contains(" " + text.toLowerCase()) || name.toLowerCase().contains(" (" + text.toLowerCase()))) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
		});
		iDepartments.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				Chip oldChip = getChip(iDepartmentAttribute);
				Chip newChip = (iDepartments.getSelectedIndex() <= 0 ? null : new Chip(iDepartmentAttribute, iDepartments.getValue(iDepartments.getSelectedIndex())).withTranslatedCommand(iDepartmentTag));
				if (oldChip != null) {
					if (newChip == null) {
						removeChip(oldChip, true);
					} else {
						if (!oldChip.getValue().equals(newChip.getValue())) {
							removeChip(oldChip, false);
							addChip(newChip, true);
						}
					}
				} else {
					if (newChip != null)
						addChip(newChip, true);
				}
			}
		});
		
		addFilter(new FilterBox.StaticSimpleFilter("course", MESSAGES.tagCourse()));
		
		Label courseLab = new Label(MESSAGES.propCourse());
		iCourse = new AriaSuggestBox(new CourseOracle());
		iCourse.setStyleName("unitime-TextArea");
		iCourse.setWidth("200px");
		addFilter(new FilterBox.StaticSimpleFilter("course", MESSAGES.tagCourse()));
		iCourse.getValueBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				courseChanged(true);
			}
		});
		iCourse.getValueBox().addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						courseChanged(false);
					}
				});
			}
		});
		iCourse.getValueBox().addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							courseChanged(false);
						}
					});
			}
		});
		iCourse.getValueBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				courseChanged(true);
			}
		});
		iCourse.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<com.google.gwt.user.client.ui.SuggestOracle.Suggestion> event) {
				courseChanged(true);
			}
		});
		
		Label instructorLab = new Label(MESSAGES.propInstructor());
		instructorLab.getElement().getStyle().setMarginLeft(10, Unit.PX);
		iInstructor = new AriaSuggestBox(new InstructorOracle());
		iInstructor.setStyleName("unitime-TextArea");
		iInstructor.setWidth("200px");
		addFilter(new FilterBox.StaticSimpleFilter("instructor", MESSAGES.tagInstructor()));
		iInstructor.getValueBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				instructorChanged(true);
			}
		});
		iInstructor.getValueBox().addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						instructorChanged(false);
					}
				});
			}
		});
		iInstructor.getValueBox().addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							instructorChanged(false);
						}
					});
			}
		});
		iInstructor.getValueBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				instructorChanged(true);
			}
		});
		iInstructor.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<com.google.gwt.user.client.ui.SuggestOracle.Suggestion> event) {
				instructorChanged(true);
			}
		});
		
		addFilter(new FilterBox.CustomFilter("Other", MESSAGES.tagOther(), courseLab, iCourse, instructorLab, iInstructor));
		
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iLastCourse = getChip("course");
				iLastInstructor = getChip("instructor");
				if (!isFilterPopupShowing()) {
					iDepartments.setSelectedIndex(0);
					for (int i = 1; i < iDepartments.getItemCount(); i++) {
						String value = iDepartments.getValue(i);
						if (hasChip(new Chip(iDepartmentAttribute, value))) {
							iDepartments.setSelectedIndex(i);
							break;
						}
					}

					Chip course = getChip("course");
					if (course == null)
						iCourse.setText("");
					else
						iCourse.setText(course.getValue());

					Chip instructor = getChip("instructor");
					if (instructor == null)
						iInstructor.setText("");
					else
						iInstructor.setText(instructor.getValue());					
				}
				init(false, getAcademicSessionId(), new Command() {
					@Override
					public void execute() {
						if (isFilterPopupShowing())
							showFilterPopup();
					}
				});
			}
		});
	}
	
	private void courseChanged(boolean fireChange) {
		Chip oldChip = getChip("course");
		if (iCourse.getText().isEmpty()) {
			if (oldChip != null)
				removeChip(oldChip, fireChange);
		} else {
			Chip newChip = new Chip("course", iCourse.getText()).withTranslatedCommand(MESSAGES.tagCourse());
			if (oldChip != null) {
				if (newChip.equals(oldChip)) {
					if (fireChange && !newChip.equals(iLastCourse)) fireValueChangeEvent();
					return;
				}
				removeChip(oldChip, false);
			}
			addChip(newChip, fireChange);
		}
	}
	
	private void instructorChanged(boolean fireChange) {
		Chip oldChip = getChip("instructor");
		if (iInstructor.getText().isEmpty()) {
			if (oldChip != null)
				removeChip(oldChip, fireChange);
		} else {
			Chip newChip = new Chip("instructor", iInstructor.getText()).withTranslatedCommand(MESSAGES.tagInstructor());
			if (oldChip != null) {
				if (newChip.equals(oldChip)) {
					if (fireChange && !newChip.equals(iLastInstructor)) fireValueChangeEvent();
					return;
				}
				removeChip(oldChip, false);
			}
			addChip(newChip, fireChange);
		}
	}
	
	@Override
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if (iDepartmentAttribute.equals(filter.getCommand())) {
			iDepartments.clear();
			iDepartments.addItem("department".equals(iDepartmentAttribute) ? MESSAGES.itemAllDepartments() : MESSAGES.itemAllSubjectAreas(), "");
			if (entities != null)
				for (FilterRpcResponse.Entity entity: entities)
					iDepartments.addItem(entity.getName() + (entity.getCount() > 0 ? " (" + entity.getCount() + ")" : ""), entity.getAbbreviation());
			
			iDepartments.setSelectedIndex(0);
			Chip dept = getChip(iDepartmentAttribute);
			if (dept != null)
				for (int i = 1; i < iDepartments.getItemCount(); i++)
					if (dept.getValue().equals(iDepartments.getValue(i))) {
						iDepartments.setSelectedIndex(i);
						break;
					}
			return true;
		} else if (filter != null && filter instanceof FilterBox.StaticSimpleFilter) {
			FilterBox.StaticSimpleFilter simple = (FilterBox.StaticSimpleFilter)filter;
			List<FilterBox.Chip> chips = new ArrayList<FilterBox.Chip>();
			if (entities != null) {
				for (FilterRpcResponse.Entity entity: entities)
					chips.add(new FilterBox.Chip(filter.getCommand(), entity.getAbbreviation())
							.withLabel(entity.getName())
							.withCount(entity.getCount())
							.withTranslatedCommand(filter.getLabel())
							.withTranslatedValue(entity.getProperty("translated-value", null)));
			}
			simple.setValues(chips);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public TeachingRequestsFilterRpcRequest createRpcRequest() {
		TeachingRequestsFilterRpcRequest req = new TeachingRequestsFilterRpcRequest();
		if (iAssigned != null)
			req.setOption("assigned", iAssigned ? "true" : "false");
		return req;
	}
	
	@Override
	protected void onLoad(FilterRpcResponse result) {
		if (!result.hasEntities()) return;
		boolean added = false;
		types: for (String type: result.getTypes()) {
			if ("department".equals(type) || "subject".equals(type) || "assigned".equals(type)) continue;
			for (FilterBox.Filter filter: iFilter.getWidget().getFilters()) {
				if (filter.getCommand().equals(type)) continue types;
			}
			iFilter.getWidget().getFilters().add(iFilter.getWidget().getFilters().size() - 1, new FilterBox.StaticSimpleFilter(type, null));
			added = true;
		}
		if (added) setValue(getValue(), false);
	}
	
	public class CourseSuggestion implements SuggestOracle.Suggestion {
		private FilterBox.Suggestion iSuggestion;
		
		CourseSuggestion(FilterBox.Suggestion suggestion) {
			iSuggestion = suggestion;
		}

		@Override
		public String getDisplayString() {
			return iSuggestion.getChipToAdd().getLabel() + (iSuggestion.getChipToAdd().hasToolTip() ? " <span class='item-hint'>" + iSuggestion.getChipToAdd().getToolTip() + "</span>" : "");
		}

		@Override
		public String getReplacementString() {
			return iSuggestion.getChipToAdd().getValue();
		}
	}
	
	public class CourseOracle extends SuggestOracle {

		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			if (!request.getQuery().isEmpty()) {
				iFilter.getWidget().getSuggestionsProvider().getSuggestions(iFilter.getWidget().getChips(null), request.getQuery(), new AsyncCallback<Collection<FilterBox.Suggestion>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Collection<FilterBox.Suggestion> result) {
						if (result == null) return;
						List<CourseSuggestion> suggestions = new ArrayList<CourseSuggestion>();
						for (FilterBox.Suggestion suggestion: result) {
							if (suggestion.getChipToAdd() != null && "course".equals(suggestion.getChipToAdd().getCommand())) {
								suggestions.add(new CourseSuggestion(suggestion));
							}
						}
						callback.onSuggestionsReady(request, new Response(suggestions));
					}
				});
			}
		}
		
		@Override
		public boolean isDisplayStringHTML() {
			return true;
		}
	}
	
	public class InstructorSuggestion implements SuggestOracle.Suggestion {
		private FilterBox.Suggestion iSuggestion;
		
		InstructorSuggestion(FilterBox.Suggestion suggestion) {
			iSuggestion = suggestion;
		}

		@Override
		public String getDisplayString() {
			return iSuggestion.getChipToAdd().getLabel();
		}

		@Override
		public String getReplacementString() {
			return iSuggestion.getChipToAdd().getValue();
		}
	}
	
	public class InstructorOracle extends SuggestOracle {

		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			if (!request.getQuery().isEmpty()) {
				iFilter.getWidget().getSuggestionsProvider().getSuggestions(iFilter.getWidget().getChips(null), request.getQuery(), new AsyncCallback<Collection<FilterBox.Suggestion>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Collection<FilterBox.Suggestion> result) {
						if (result == null) return;
						List<InstructorSuggestion> suggestions = new ArrayList<InstructorSuggestion>();
						for (FilterBox.Suggestion suggestion: result) {
							if (suggestion.getChipToAdd() != null && "instructor".equals(suggestion.getChipToAdd().getCommand())) {
								suggestions.add(new InstructorSuggestion(suggestion));
							}
						}
						callback.onSuggestionsReady(request, new Response(suggestions));
					}
				});
			}
		}
	}
}
