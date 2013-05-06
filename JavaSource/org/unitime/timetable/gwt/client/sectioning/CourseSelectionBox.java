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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTabPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.client.widgets.WebTable.RowDoubleClickEvent;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class CourseSelectionBox extends Composite {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static Logger sLogger = Logger.getLogger(CourseSelectionBox.class.getName());
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private AcademicSessionProvider iAcademicSessionProvider;
	
	private AriaSuggestBox iSuggest;
	private String iLastSuggestion;
	private ImageButton iFinderButton;
	private HorizontalPanel iHPanel;
	private VerticalPanel iVPanel;
	private Label iError;
	private Set<String> iValidCourseNames = new HashSet<String>();
	
	private AriaTextBox iFilter;
	private AriaButton iFilterSelect;
	private DialogBox iDialog;
	private ScrollPanel iCoursesPanel;
	private VerticalPanel iDialogPanel, iCoursesTab, iFreeTimeTab;
	private WebTable iCourses, iClasses;
	private String iLastQuery = null;
	private FreeTimePicker iFreeTimePicker;
	private Label iFreeTimeError = null, iCoursesTip, iFreeTimeTip;
	
	private UniTimeTabPanel iTabPanel, iCourseDetailsTabPanel = null;
	
	private HTML iCourseDetails;
	private ScrollPanel iCourseDetailsPanel, iClassesPanel;
	
	private boolean iAllowFreeTime;
		
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> iCourseOfferingsCallback;
	
	private AsyncCallback<String> iCourseDetailsCallback;
	private AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> iCourseClassesCallback;

	private ArrayList<CourseSelectionChangeHandler> iCourseSelectionChangeHandlers = new ArrayList<CourseSelectionChangeHandler>();
	
	private ArrayList<Validator> iValitaros = new ArrayList<Validator>();
	
	private String iHint = "";
	
	private CourseSelectionBox iPrev, iNext;
	private CourseSelectionBox iPrimary, iAlternative;
	
	private CheckBox iWaitList;
	
	private String iLastCourseLookup = null;
	
	private static int sLastSelectedCourseDetailsTab = 0;
	
	public CourseSelectionBox(AcademicSessionProvider acadSession, String name, boolean enabled, boolean allowFreeTime) {
		iAcademicSessionProvider = acadSession;
		iAllowFreeTime = allowFreeTime;
		
		SuggestOracle courseOfferingOracle = new SuggestOracle() {
			public void requestSuggestions(Request request, Callback callback) {
				if (request.getQuery().equals(iHint)) return;
				iSectioningService.listCourseOfferings(iAcademicSessionProvider.getAcademicSessionId(), request.getQuery(), request.getLimit(), new SuggestCallback(request, callback));
			}
			public boolean isDisplayStringHTML() { return true; }			
		};
		
		iSuggest = new AriaSuggestBox(courseOfferingOracle);
		iSuggest.setStyleName("unitime-TextBoxHint");

		iFinderButton = new ImageButton(RESOURCES.search_picker(), RESOURCES.search_picker_Down(), RESOURCES.search_picker_Over(), RESOURCES.search_picker_Disabled());
		iFinderButton.setTabIndex(-1);
		
		iVPanel = new VerticalPanel();
		
		iHPanel = new HorizontalPanel();
		iHPanel.add(iSuggest);
		iHPanel.add(iFinderButton);
		iVPanel.add(iHPanel);
		
		iError = new Label();
		iError.setStyleName("unitime-ErrorHint");
		iError.setVisible(false);
		Roles.getPresentationRole().setAriaHiddenState(iError.getElement(), true);
		iVPanel.add(iError);
				
		iFinderButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (iSuggest.isEnabled()) {
					openDialogAsync();
				}
			}
		});
		
		if (!enabled) setEnabled(false);
		
		iSuggest.addSelectionHandler(new SelectionHandler<Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				String text = event.getSelectedItem().getReplacementString();
				iLastSuggestion = text;
				for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
					h.onChange(text, !text.isEmpty());
			}
		});
		iSuggest.getValueBox().addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				boolean valid = false;
				String text = iSuggest.getText();
				if (text.equalsIgnoreCase(iLastSuggestion))
					valid = true;
				else for (String course: iValidCourseNames) {
					if (course.equalsIgnoreCase(text)) {
						valid = true; break;
					}
				}
				for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
					h.onChange(text, valid);
			}
		});
		iSuggest.getValueBox().addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (!iSuggest.isEnabled()) return;
				if ((event.getNativeEvent().getKeyCode()=='F' || event.getNativeEvent().getKeyCode()=='f') && (event.isControlKeyDown() || event.isAltKeyDown())) {
					hideSuggestionList();
					openDialogAsync();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_ESCAPE) {
					hideSuggestionList();
				}
				if ((event.getNativeEvent().getKeyCode()=='L' || event.getNativeEvent().getKeyCode()=='l') && (event.isControlKeyDown() || event.isAltKeyDown())) {
					iSuggest.showSuggestionList();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_DOWN && event.isControlKeyDown() && iNext!=null) {
					hideSuggestionList();
					if (event.isShiftKeyDown()) swapDown();
					iNext.setFocus(true);
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iNext.hideSuggestionList();
						}
					});
					event.preventDefault();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_UP && event.isControlKeyDown() && iPrev!=null) {
					hideSuggestionList();
					if (event.isShiftKeyDown()) swapUp();
					iPrev.setFocus(true);
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						public void execute() {
							iPrev.hideSuggestionList();
						}
					});
					event.preventDefault();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_LEFT && event.isControlKeyDown() && iPrimary!=null) {
					hideSuggestionList();
					iPrimary.setFocus(true);
			    	event.preventDefault();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_RIGHT && event.isControlKeyDown() && iAlternative!=null && iAlternative.isEnabled()) {
					hideSuggestionList();
					iAlternative.setFocus(true);
			    	event.preventDefault();
				}
			}
		});
		iSuggest.getValueBox().addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				if (iSuggest.getText().isEmpty()) {
					if (iError.isVisible()) iError.setVisible(false);
					if (iHint!=null) iSuggest.setText(iHint);
					iSuggest.setStyleName("unitime-TextBoxHint");
				}
			}
		});
		iSuggest.getValueBox().addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				iSuggest.setStyleName("gwt-SuggestBox");
				if (iSuggest.getText().equals(iHint)) iSuggest.setText("");
				if (!iError.getText().isEmpty())
					AriaStatus.getInstance().setText(iError.getText());
			}
		});
		
		initWidget(iVPanel);
	}
	
	public void setLabel(String title, String finderTitle) {
		iSuggest.setAriaLabel(title);
		iFinderButton.setAltText(finderTitle);
	}
	
	private void openDialogAsync() {
		GWT.runAsync(new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				openDialog();
			}
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(caught);
			}
		});
	}
	
	private void courseSelectionChanged() {
		if (iCourses.getSelectedRow() >= 0 && iCourses.getRows() != null && iCourses.getSelectedRow() < iCourses.getRows().length) {
			WebTable.Row row = iCourses.getRows()[iCourses.getSelectedRow()];
			if (row != null) {
				String title = row.getCell(3).getValue();
				String note = row.getCell(4).getValue();
				if (title.isEmpty()) {
					if (note.isEmpty()) {
						AriaStatus.getInstance().setHTML(ARIA.courseFinderSelected(1 + iCourses.getSelectedRow(), iCourses.getRowsCount(), row.getCell(0).getValue(), row.getCell(1).getValue()));
					} else {
						AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithNote(1 + iCourses.getSelectedRow(), iCourses.getRowsCount(), row.getCell(0).getValue(), row.getCell(1).getValue(), note));
					}
				} else {
					if (note.isEmpty()) {
						AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithTitle(1 + iCourses.getSelectedRow(), iCourses.getRowsCount(), row.getCell(0).getValue(), row.getCell(1).getValue(), title));
					} else {
						AriaStatus.getInstance().setHTML(ARIA.courseFinderSelectedWithTitleAndNote(1 + iCourses.getSelectedRow(), iCourses.getRowsCount(), row.getCell(0).getValue(), row.getCell(1).getValue(), title, note));
					}
				}
			}
		} else {
			AriaStatus.getInstance().setHTML(ARIA.courseFinderNoCourse());
		}
	}
	
	private void openDialog() {
		if (iDialog == null) {
			iDialog = new UniTimeDialogBox(true, false);
			iDialog.setText(MESSAGES.courseSelectionDialog());
			
			iFilter = new AriaTextBox();
			iFilter.setStyleName("gwt-SuggestBox");
			iFilter.getElement().getStyle().setWidth(600, Unit.PX);
			iFilter.setAriaLabel(iAllowFreeTime ? ARIA.courseFinderFilterAllowsFreeTime() : ARIA.courseFinderFilter());
			
			iFilterSelect = new AriaButton(MESSAGES.buttonSelect());
			iFilterSelect.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iCourses.getSelectedRow()>=0 && iCourses.getRows()!=null && iCourses.getSelectedRow() < iCourses.getRows().length && iTabPanel.getSelectedTab() == 0) {
						WebTable.Row r = iCourses.getRows()[iCourses.getSelectedRow()];
						if ("true".equals(r.getId()))
							iSuggest.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
						else
							iSuggest.setText(MESSAGES.courseNameWithTitle(r.getCell(0).getValue(), r.getCell(1).getValue(), r.getCell(2).getValue()));
						for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
							h.onChange(iSuggest.getText(), true);
					} else {
						try {
							iFreeTimePicker.clearFreeTime();
							ArrayList<CourseRequestInterface.FreeTime> freeTimes = parseFreeTime(iFilter.getText());
							iFreeTimePicker.setFreeTime(freeTimes, false);
							iFilter.setText(freeTimesToString(freeTimes));
						} catch (IllegalArgumentException e) {}
						iSuggest.setText(iFilter.getText());
					}
					iDialog.hide();
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						public void execute() {
							setFocus(true);
						}
					});
				}
			});
			
			iCourses = new WebTable();
			iCourses.setHeader(
					new WebTable.Row(
							new WebTable.Cell(MESSAGES.colSubject(), 1, "80px"),
							new WebTable.Cell(MESSAGES.colCourse(), 1, "80px"),
							new WebTable.Cell(MESSAGES.colLimit(), 1, "60px"),
							new WebTable.Cell(MESSAGES.colTitle(), 1, "300px"),
							new WebTable.Cell(MESSAGES.colNote(), 1, "300px")
							));
			
			HorizontalPanel filterWithSelect = new HorizontalPanel();
			filterWithSelect.add(iFilter);
			filterWithSelect.add(iFilterSelect);
			filterWithSelect.setCellVerticalAlignment(iFilter, HasVerticalAlignment.ALIGN_MIDDLE);
			filterWithSelect.setCellVerticalAlignment(iFilterSelect, HasVerticalAlignment.ALIGN_MIDDLE);
			iFilterSelect.getElement().getStyle().setMarginLeft(5, Unit.PX);
			
			iDialogPanel = new VerticalPanel();
			iDialogPanel.setSpacing(5);
			iDialogPanel.add(filterWithSelect);
			iDialogPanel.setCellHorizontalAlignment(filterWithSelect, HasHorizontalAlignment.ALIGN_CENTER);
						
			iDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
				public void onClose(CloseEvent<PopupPanel> event) {
					RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						public void execute() {
							setFocus(true);
						}
					});
				}
			});
					
			iCoursesPanel = new ScrollPanel(iCourses);
			iCoursesPanel.getElement().getStyle().setWidth(780, Unit.PX);
			iCoursesPanel.getElement().getStyle().setHeight(200, Unit.PX);
			iCoursesPanel.setStyleName("unitime-ScrollPanel");
			
			iCourseDetails = new HTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionNoCourseSelected() + "</td></tr></table>");
			iCourseDetailsPanel = new ScrollPanel(iCourseDetails);
			iCourseDetailsPanel.setStyleName("unitime-ScrollPanel-inner");
			iCourseDetailsPanel.getElement().getStyle().setWidth(780, Unit.PX);
			iCourseDetailsPanel.getElement().getStyle().setHeight(200, Unit.PX);
			
			iClasses = new WebTable();
			iClasses.setHeader(new WebTable.Row(
					new WebTable.Cell(MESSAGES.colSubpart(), 1, "50px"),
					new WebTable.Cell(MESSAGES.colClass(), 1, "90px"),
					new WebTable.Cell(MESSAGES.colLimit(), 1, "60px"),
					new WebTable.Cell(MESSAGES.colDays(), 1, "60px"),
					new WebTable.Cell(MESSAGES.colStart(), 1, "60px"),
					new WebTable.Cell(MESSAGES.colEnd(), 1, "60px"),
					new WebTable.Cell(MESSAGES.colDate(), 1, "100px"),
					new WebTable.Cell(MESSAGES.colRoom(), 1, "100px"),
					new WebTable.Cell(MESSAGES.colInstructor(), 1, "120px"),
					new WebTable.Cell(MESSAGES.colParent(), 1, "90px"),
					new WebTable.Cell(MESSAGES.colHighDemand(), 1, "10px"),
					new WebTable.Cell(MESSAGES.colNoteIcon(), 1, "10px")
				));
			iClasses.setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
			iClassesPanel = new ScrollPanel(iClasses);
			iClassesPanel.setStyleName("unitime-ScrollPanel-inner");
			iClassesPanel.getElement().getStyle().setWidth(780, Unit.PX);
			iClassesPanel.getElement().getStyle().setHeight(200, Unit.PX);

			iCoursesTip = new Label(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
			iCoursesTip.setStyleName("unitime-Hint");
			ToolBox.disableTextSelectInternal(iCoursesTip.getElement());
			iCoursesTip.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					String oldText = iCoursesTip.getText();
					do {
						iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
					} while (oldText.equals(iCoursesTip.getText()));
				}
			});
			
			iTabPanel = new UniTimeTabPanel();	
			
			iCourseDetailsTabPanel = new UniTimeTabPanel();
			iCourseDetailsTabPanel.setDeckStyleName("unitime-TabPanel");
			iCourseDetailsTabPanel.add(iCourseDetailsPanel, MESSAGES.courseSelectionDetails(), true);
			iCourseDetailsTabPanel.add(iClassesPanel, MESSAGES.courseSelectionClasses(), true);
			iCourseDetailsTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
				public void onSelection(SelectionEvent<Integer> event) {
					sLastSelectedCourseDetailsTab = event.getSelectedItem();
				}
			});
			iCoursesTab = new VerticalPanel();
			iCoursesTab.setSpacing(10);
			iCoursesTab.add(iCoursesPanel);
			iCoursesTab.add(iCourseDetailsTabPanel);
			iCoursesTab.add(iCoursesTip);
			iTabPanel.add(iCoursesTab, MESSAGES.courseSelectionCourses(), true);
			
			iFreeTimeTab = new VerticalPanel();
			iFreeTimeTab.setSpacing(10);
					
			iFreeTimePicker = new FreeTimePicker();
			iFreeTimePicker.addFreeTimeChangeHandler(new FreeTimePicker.FreeTimeChangeHandler() {
				public void onFreeTimeChange(FreeTimePicker.FreeTimeChangeEvent event) {
					iFilter.setText(freeTimesToString(event.getFreeTime()));
					iFreeTimeError.setVisible(false);
				}
			});
			iFreeTimeTab.add(iFreeTimePicker);

			iFreeTimeTip = new Label(CONSTANTS.freeTimeTips()[(int)(Math.random() * CONSTANTS.freeTimeTips().length)]);
			iFreeTimeTip.setStyleName("unitime-Hint");
			ToolBox.disableTextSelectInternal(iFreeTimeTip.getElement());
			iFreeTimeTip.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					String oldText = iFreeTimeTip.getText();
					do {
						iFreeTimeTip.setText(CONSTANTS.freeTimeTips()[(int)(Math.random() * CONSTANTS.freeTimeTips().length)]);
					} while (oldText.equals(iFreeTimeTip.getText()));
				}
			});
			iFreeTimeTab.add(iFreeTimeTip);

			iFreeTimeError = new Label();
			iFreeTimeError.setStyleName("unitime-ErrorMessage");
			iFreeTimeError.setVisible(false);
			iFreeTimeTab.add(iFreeTimeError);
			
			if (iAllowFreeTime) {
				iTabPanel.add(iFreeTimeTab, MESSAGES.courseSelectionFreeTime(), true);
				iDialogPanel.add(iTabPanel);
			} else {
				iDialogPanel.add(iCoursesPanel);
				iDialogPanel.add(iCourseDetailsTabPanel);
				iDialogPanel.add(iCoursesTip);
			}
			
			iDialog.setWidget(iDialogPanel);
			
			final Timer finderTimer = new Timer() {
				@Override
				public void run() {
					updateCourses();
				}
			};
			
			iFilter.addKeyUpHandler(new KeyUpHandler() {
				public void onKeyUp(KeyUpEvent event) {
					finderTimer.schedule(250);
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER || event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
						if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
							if (iCourses.getSelectedRow()>=0 && iCourses.getRows()!=null && iCourses.getSelectedRow() < iCourses.getRows().length && iTabPanel.getSelectedTab() == 0) {
								WebTable.Row r = iCourses.getRows()[iCourses.getSelectedRow()];
								if ("true".equals(r.getId()))
									iSuggest.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
								else
									iSuggest.setText(MESSAGES.courseNameWithTitle(r.getCell(0).getValue(), r.getCell(1).getValue(), r.getCell(2).getValue()));
								for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
									h.onChange(iSuggest.getText(), true);
							} else {
								try {
									iFreeTimePicker.clearFreeTime();
									ArrayList<CourseRequestInterface.FreeTime> freeTimes = parseFreeTime(iFilter.getText());
									iFreeTimePicker.setFreeTime(freeTimes, false);
									iFilter.setText(freeTimesToString(freeTimes));
								} catch (IllegalArgumentException e) {}
								iSuggest.setText(iFilter.getText());
							}
						}					
						iDialog.hide();
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							public void execute() {
								setFocus(true);
							}
						});
					}
					if (event.getNativeKeyCode()==KeyCodes.KEY_DOWN) {
						iCourses.setSelectedRow(iCourses.getSelectedRow()+1);
						scrollToSelectedRow();
						updateCourseDetails();
						courseSelectionChanged();
					}
					if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
						iCourses.setSelectedRow(iCourses.getSelectedRow()==0?iCourses.getRowsCount()-1:iCourses.getSelectedRow()-1);
						scrollToSelectedRow();
						updateCourseDetails();
						courseSelectionChanged();
					}
					if ((event.getNativeKeyCode()=='c' || event.getNativeKeyCode()=='C') && !isFreeTime() && (event.isControlKeyDown() || event.isAltKeyDown())) {
						iTabPanel.selectTab(0);
						event.preventDefault();
						AriaStatus.getInstance().setText(ARIA.courseFinderCoursesTab());
					}
					if (iAllowFreeTime && (event.getNativeKeyCode()=='t' || event.getNativeKeyCode()=='T') && (event.isControlKeyDown() || event.isAltKeyDown())) {
						iTabPanel.selectTab(iCourseDetailsTabPanel != null ? 1 : 3);
						event.preventDefault();
						AriaStatus.getInstance().setText(ARIA.courseFinderFreeTimeTab());
					}
					if ((event.getNativeKeyCode()=='d' || event.getNativeKeyCode()=='D') && (event.isControlKeyDown() || event.isAltKeyDown())) {
						if (iCourseDetailsTabPanel == null)
							iTabPanel.selectTab(1);
						else
							iCourseDetailsTabPanel.selectTab(0);
						event.preventDefault();
						if (iCourses.getSelectedRow() >= 0 && iCourses.getRows() != null && iCourses.getSelectedRow() < iCourses.getRows().length)
							AriaStatus.getInstance().setHTML(iCourseDetails.getHTML());
						else
							AriaStatus.getInstance().setHTML(ARIA.courseFinderNoCourse());
					}
					if ((event.getNativeKeyCode()=='l' || event.getNativeKeyCode()=='L') && (event.isControlKeyDown() || event.isAltKeyDown())) {
						if (iCourseDetailsTabPanel == null)
							iTabPanel.selectTab(2);
						else
							iCourseDetailsTabPanel.selectTab(1);
						event.preventDefault();
						courseSelectionChanged();
					}
				}
			});
			iFilter.addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					updateCourses();
				}
			});
			iCourses.addRowDoubleClickHandler(new WebTable.RowDoubleClickHandler() {
				public void onRowDoubleClick(RowDoubleClickEvent event) {
					WebTable.Row r = event.getRow();
					if ("true".equals(r.getId()))
						iSuggest.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
					else
						iSuggest.setText(MESSAGES.courseNameWithTitle(r.getCell(0).getValue(), r.getCell(1).getValue(), r.getCell(2).getValue()));
					iDialog.hide();
					for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
						h.onChange(iSuggest.getText(), true);
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						public void execute() {
							setFocus(true);
						}
					});
				}
			});
			iCourses.addRowClickHandler(new WebTable.RowClickHandler() {
				public void onRowClick(WebTable.RowClickEvent event) {
					iCourses.setSelectedRow(event.getRowIdx());
					updateCourseDetails();
					courseSelectionChanged();
				}
			});
			
			iFilter.addBlurHandler(new BlurHandler() {
				public void onBlur(BlurEvent event) {
					if (iDialog.isShowing()) {
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							public void execute() {
								iFilter.setFocus(true);
							}
						});
					}
				}
			});
		}

		iFilter.setText(iSuggest.getText().equals(iHint)?"":iSuggest.getText());
		iTabPanel.selectTab(0);
		iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
		iFreeTimeTip.setText(CONSTANTS.freeTimeTips()[(int)(Math.random() * CONSTANTS.freeTimeTips().length)]);
		if (iCourseDetailsTabPanel != null)
			iCourseDetailsTabPanel.selectTab(sLastSelectedCourseDetailsTab);
		AriaStatus.getInstance().setText(ARIA.courseFinderDialogOpened());
		iDialog.center();
		RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				iFilter.setFocus(true);
				updateCourses();
			}
		});
	}
	
	public void hideSuggestionList() {
		iSuggest.hideSuggestionList();
	}
	
	public void showSuggestionList() {
		iSuggest.showSuggestionList();
	}

	public void setAccessKey(char a) {
		iSuggest.setAccessKey(a);
	}
	
	public void setWidth(String width) {
		iSuggest.setWidth(width);
	}
	
	public void clear() {
		iSuggest.setText(iHint);
		if (!iHint.isEmpty())
			iSuggest.setStyleName("unitime-TextBoxHint");
		iError.setText(""); iError.setVisible(false);
	}
	
	public void setError(String error) {
		iError.setText(error);
		iError.setTitle(null);
		iError.setVisible(!iError.getText().isEmpty());
		iSuggest.setStatus(error);
		AriaStatus.getInstance().setText(error);
	}
	
	public boolean hasError() {
		return iError.isVisible();
	}
	
	public String getError() {
		return iError.getText();
	}
	
	private void scrollToSelectedRow() {
		if (iCourses.getSelectedRow()<0) return;
		
		Element scroll = iCoursesPanel.getElement();
		
		com.google.gwt.dom.client.Element item = iCourses.getTable().getRowFormatter().getElement(iCourses.getSelectedRow());
		if (item==null) return;
		
		int realOffset = 0;
		while (item !=null && !item.equals(scroll)) {
			realOffset += item.getOffsetTop();
			item = item.getOffsetParent();
		}
		
		scroll.setScrollTop(realOffset - scroll.getOffsetHeight() / 2);
	}

	
	public void addCourseSelectionChangeHandler(CourseSelectionChangeHandler CourseSelectionChangeHandler) {
		iCourseSelectionChangeHandlers.add(CourseSelectionChangeHandler);
	}
	
	public String getCourse() {
		return (iSuggest.getText().equals(iHint) ? "" : iSuggest.getText());
	}
	
	public void setCourse(String course, boolean fireChangeEvent) {
		iSuggest.setText(course);
		if (iSuggest.getText().isEmpty()) {
			if (iHint!=null) iSuggest.setText(iHint);
			iSuggest.setStyleName("unitime-TextBoxHint");
		} else {
			iSuggest.setStyleName("gwt-SuggestBox");
		}
		if (fireChangeEvent)
			for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
				h.onChange(iSuggest.getText(), course != null && !course.isEmpty());
	}
	
	public void setNext(CourseSelectionBox next) { iNext = next; }
	public void setPrev(CourseSelectionBox prev) { iPrev = prev; }
	public void setPrimary(CourseSelectionBox primary) { iPrimary = primary; }
	public void setAlternative(CourseSelectionBox alternative) { iAlternative = alternative; }
	public void setWaitList(CheckBox waitList) { iWaitList = waitList; }
	
	public void setWaitList(boolean waitList) { if (iWaitList != null) iWaitList.setValue(waitList); }
	public Boolean getWaitList() { return iWaitList == null ? null : iWaitList.getValue(); }
	
	private void swapWith(CourseSelectionBox other) {
		hideSuggestionList();
		other.hideSuggestionList();
		String x = iError.getText();
		iError.setText(other.iError.getText());
		other.iError.setText(x);
		boolean b = iError.isVisible();
		iError.setVisible(other.iError.isVisible());
		other.iError.setVisible(b);
		b = isEnabled();
		setEnabled(other.isEnabled());
		other.setEnabled(b);
		if (iPrimary != null) {
			x = getHint();
			setHint(other.getHint());
			other.setHint(x);
		}
		x = getCourse();
		setCourse(other.getCourse(), false);
		other.setCourse(x, false);
		if (iAlternative!=null) iAlternative.swapWith(other.iAlternative);
		if (iWaitList != null && other.iWaitList != null) {
			Boolean ch = iWaitList.getValue(); iWaitList.setValue(other.iWaitList.getValue()); other.iWaitList.setValue(ch);
		}
	}
	
	private void clearAllAlternatives() {
		if (iPrimary != null) {
			iHint = "";
		}
		clear();
		if (iAlternative!=null) iAlternative.clearAllAlternatives();
	}
	
	private void clearAll() {
		if (iPrimary != null) iPrimary.clearAll();
		else clearAllAlternatives();
	}
	
	public void moveDown() {
		if (iPrimary!=null) {
			iPrimary.moveDown();
		} else {
			if (iNext==null) {
				clearAll();
			} else {
				iNext.moveDown();
				swapWith(iNext);
			}
		}
	}
	
	public void moveUp() {
		if (iPrimary!=null) {
			iPrimary.moveUp();
		} else {
			if (iPrev==null) {
				clearAll();
			} else {
				iPrev.moveUp();
				swapWith(iPrev);
			}
		}
	}
	
	public void swapDown() {
		if (iPrimary!=null) {
			iPrimary.swapDown();
		} else {
			swapWith(iNext);
		}
	}

	public void swapUp() {
		if (iPrimary!=null) {
			iPrimary.swapUp();
		} else {
			swapWith(iPrev);
		}
	}

	public boolean isFreeTime() {
		try {
			parseFreeTime(iSuggest.getText());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public boolean fillInFreeTime(CourseRequestInterface.Request request) {
		try {
			for (CourseRequestInterface.FreeTime ft: parseFreeTime(iSuggest.getText()))
				request.addRequestedFreeTime(ft);
			return request.hasRequestedFreeTime();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public void setEnabled(boolean enabled) {
		if (enabled) {
			iSuggest.setEnabled(true);
			iFinderButton.setEnabled(true);
			iFinderButton.setTabIndex(0);
		} else {
			iSuggest.setEnabled(false);
			iFinderButton.setEnabled(false);
			iFinderButton.setTabIndex(-1);
		}
	}
	
	public boolean isEnabled() {
		return iSuggest.isEnabled();
	}
	
	private void updateCourses() {
		if (iAllowFreeTime) {
			try {
				iFreeTimePicker.clearFreeTime();
				ArrayList<CourseRequestInterface.FreeTime> freeTimes = parseFreeTime(iFilter.getText()); 
				iFreeTimePicker.setFreeTime(freeTimes, false);
				iFreeTimeError.setVisible(false);
				iTabPanel.selectTab(iTabPanel.getTabCount() - 1);
				String status = "";
				for (CourseRequestInterface.FreeTime ft: freeTimes) {
					status += ft.toAriaString(CONSTANTS.longDays(), CONSTANTS.useAmPm()) + " ";
				}
				if (!status.isEmpty())
					AriaStatus.getInstance().setText(ARIA.courseFinderSelectedFreeTime(status));
			} catch (IllegalArgumentException e) {
				iFreeTimeError.setText(e.getMessage());
				iFreeTimeError.setVisible(true);
				if (iTabPanel.getSelectedTab() != 0)
					AriaStatus.getInstance().setText(e.getMessage());
			}
		}
		if (iCourseOfferingsCallback==null) {
			iCourseOfferingsCallback = new AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>>() {
				public void onFailure(Throwable caught) {
					iCourses.clearData(true);
					iCourses.setEmptyMessage(caught.getMessage());
					if (iTabPanel.getSelectedTab() == 0)
						AriaStatus.getInstance().setText(caught.getMessage());
				}
				public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
					WebTable.Row[] records = new WebTable.Row[result.size()];
					int idx = 0;
					int selectRow = -1;
					for (ClassAssignmentInterface.CourseAssignment record: result) {
						records[idx] = new WebTable.Row(
								record.getSubject(),
								record.getCourseNbr(),
								(record.getLimit() == null || record.getLimit() == 0 || record.getEnrollment() == null ? "" : record.getLimit() < 0 ? "&infin;" : (record.getLimit() - record.getEnrollment()) + " / " + record.getLimit()),
								(record.getTitle() == null ? "" : record.getTitle()),
								(record.getNote() == null ? "" : record.getNote()));
						records[idx].setId(record.hasUniqueName() ? "true" : "false");
						if (iFilter.getText().equalsIgnoreCase(record.getSubject() + " " + record.getCourseNbr()))
							selectRow = idx;
						if (record.getTitle() == null || record.getTitle().isEmpty()) {
							if (record.getNote() == null || record.getNote().isEmpty()) {
								records[idx].setAriaLabel(ARIA.courseFinderCourse(record.getSubject(), record.getCourseNbr()));
							} else {
								records[idx].setAriaLabel(ARIA.courseFinderCourseWithNote(record.getSubject(), record.getCourseNbr(), record.getNote()));
							}
						} else {
							if (record.getNote() == null || record.getNote().isEmpty()) {
								records[idx].setAriaLabel(ARIA.courseFinderCourseWithTitle(record.getSubject(), record.getCourseNbr(), record.getTitle()));
							} else {
								records[idx].setAriaLabel(ARIA.courseFinderCourseWithTitleAndNote(record.getSubject(), record.getCourseNbr(), record.getTitle(), record.getNote()));
							}
						}
						idx++;
					}
					iCourses.setData(records);
					if (records.length == 1)
						selectRow = 0;
					if (selectRow >= 0) {
						iCourses.setSelectedRow(selectRow);
						if (iTabPanel.getSelectedTab() != 0)
							iTabPanel.selectTab(0);
						updateCourseDetails();
						courseSelectionChanged();
					}
				}
	        };
		}
		if (iFilter.getText().equals(iLastQuery)) return;
		if (iFilter.getText().isEmpty()) {
			iCourses.setEmptyMessage(MESSAGES.courseSelectionNoCourseFilter());
		} else {
			iCourses.setEmptyMessage(MESSAGES.courseSelectionLoadingCourses());
			iSectioningService.listCourseOfferings(iAcademicSessionProvider.getAcademicSessionId(), iFilter.getText(), null, iCourseOfferingsCallback);
		}
		iLastQuery = iFilter.getText();
	}
	
	private void updateCourseDetails() {
		if (iCourseDetailsCallback==null) {
			iCourseDetailsCallback = new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
					iCourseDetails.setHTML("<table width='100%'></tr><td class='unitime-TableEmpty'><font color='red'>"+caught.getMessage()+"</font></td></tr></table>");
				}
				public void onSuccess(String result) {
					iCourseDetails.setHTML(result);
				}
			};
		}
		if (iCourseClassesCallback==null) {
			iCourseClassesCallback = new AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>>() {
				public void onFailure(Throwable caught) {
					iClasses.setEmptyMessage(caught.getMessage());
				}
				public void onSuccess(Collection<ClassAssignmentInterface.ClassAssignment> result) {
					if (!result.isEmpty()) {
						WebTable.Row[] rows = new WebTable.Row[result.size()];
						int idx = 0;
						Long lastSubpartId = null;
						for (ClassAssignmentInterface.ClassAssignment clazz: result) {
							WebTable.Row row = null;
							if (clazz.isAssigned()) {
								row = new WebTable.Row(
										new WebTable.Cell(clazz.getSubpart()),
										new WebTable.Cell(clazz.getSection()),
										new WebTable.Cell(clazz.getLimitString()),
										new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
										new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
										new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
										new WebTable.Cell(clazz.getDatePattern()),
										new WebTable.Cell(clazz.getRooms(", ")),
										new WebTable.Cell(clazz.getInstructors(", ")),
										new WebTable.Cell(clazz.getParentSection()),
										(clazz.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), null, null) : clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")),
										clazz.hasNote() ? new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), "") : new WebTable.Cell(""));
							} else {
								row = new WebTable.Row(
										new WebTable.Cell(clazz.getSubpart()),
										new WebTable.Cell(clazz.getSection()),
										new WebTable.Cell(clazz.getLimitString()),
										new WebTable.Cell(MESSAGES.arrangeHours(), 4, null),
										new WebTable.Cell(clazz.getRooms(", ")),
										new WebTable.Cell(clazz.getInstructors(", ")),
										new WebTable.Cell(clazz.getParentSection()),
										(clazz.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), null, null) : clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")),
										clazz.hasNote() ? new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), "") : new WebTable.Cell(""));

							}
							row.setId(clazz.getClassId().toString());
							String styleName = "";
							if (lastSubpartId != null && !clazz.getSubpartId().equals(lastSubpartId))
								styleName += "top-border-dashed";
							if (!clazz.isSaved() && !clazz.isAvailable())
								styleName += " text-gray";
							for (WebTable.Cell cell: row.getCells())
								cell.setStyleName(styleName.trim());
							rows[idx++] = row;
							lastSubpartId = clazz.getSubpartId();
							if (!clazz.isSaved() && !clazz.isAvailable())
								row.setAriaLabel(ARIA.courseFinderClassNotAvailable(
										MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
										clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
							else
								row.setAriaLabel(ARIA.courseFinderClassAvailable(
										MESSAGES.clazz(clazz.getSubject(), clazz.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
										clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours(),
										clazz.getLimitString()));
						}
						iClasses.setData(rows);
					} else {
						iClasses.setEmptyMessage(MESSAGES.courseSelectionNoClasses(iFilter.getText()));
					}
				}
			};
		}
		if (iCourses.getSelectedRow()<0) {
			iCourseDetails.setHTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionNoCourseSelected() + "</td></tr></table>");
			iClasses.setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
			iClasses.clearData(true);
		} else {
			WebTable.Row row = iCourses.getRows()[iCourses.getSelectedRow()];
			String courseName = MESSAGES.courseName(row.getCell(0).getValue(), row.getCell(1).getValue());
			if ("false".equals(row.getId()))
				courseName = MESSAGES.courseNameWithTitle(row.getCell(0).getValue(), row.getCell(1).getValue(), row.getCell(2).getValue());
			if (courseName.equals(iLastCourseLookup)) return;
			iCourseDetails.setHTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionLoadingDetails() + "</td></tr></table>");
			iCourseDetailsPanel.setVisible(true);
			iClasses.setEmptyMessage(MESSAGES.courseSelectionLoadingClasses());
			iClasses.clearData(true);
			iSectioningService.retrieveCourseDetails(iAcademicSessionProvider.getAcademicSessionId(), courseName, iCourseDetailsCallback);
			iSectioningService.listClasses(iAcademicSessionProvider.getAcademicSessionId(), courseName, iCourseClassesCallback);
			iLastCourseLookup = courseName;
		}
	}
	
	public void setFocus(boolean focus) {
		iSuggest.setFocus(focus);
		if (focus) iSuggest.getValueBox().selectAll();

	}
		
	public interface CourseSelectionChangeHandler{
		public void onChange(String course, boolean valid);
	}
	
	public class SuggestCallback implements AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> {
		private Request iRequest;
		private Callback iCallback;
		
		public SuggestCallback(Request request, Callback callback) {
			iRequest = request;
			iCallback = callback;
		}
		
		public void onFailure(Throwable caught) {
			iValidCourseNames.clear();
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			if (iAllowFreeTime) {
				try {
					ArrayList<CourseRequestInterface.FreeTime> freeTimes = parseFreeTime(iRequest.getQuery());
					String status = "Free time ";
					for (CourseRequestInterface.FreeTime ft: freeTimes) {
						status += ft.toAriaString(CONSTANTS.longDays(), CONSTANTS.useAmPm()) + " ";
					}
					String ft = freeTimesToString(freeTimes);
					Suggestion suggestion = new SimpleSuggestion(ft, ft, status); 
					suggestions.add(suggestion);
					// setStatus(status + " matches the entered text. Press enter to select it.");
				} catch (IllegalArgumentException e) {
					if (iRequest.getQuery().toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) {
						suggestions.add(new SimpleSuggestion("<font color='red'>"+e.getMessage()+"</font>", "", e.getMessage()));
						// setStatus(e.getMessage());
					} else {
						suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", "", caught.getMessage()));
						// setStatus(caught.getMessage());
					}
				}
			} else {
				suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", "", caught.getMessage()));
				// setStatus(caught.getMessage());
			}
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}

		public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			iValidCourseNames.clear();
			for (ClassAssignmentInterface.CourseAssignment suggestion: result) {
				String courseName = MESSAGES.courseName(suggestion.getSubject(), suggestion.getCourseNbr());
				String courseNameWithTitle = (suggestion.getTitle() == null ? courseName : MESSAGES.courseNameWithTitle(suggestion.getSubject(), suggestion.getCourseNbr(), suggestion.getTitle()));
				if (suggestion.hasUniqueName()) {
					suggestions.add(new SimpleSuggestion(courseNameWithTitle, courseName, suggestion.getTitle() == null ? courseName : courseName + " " + suggestion.getTitle()));
					iValidCourseNames.add(courseName);
				} else {
					suggestions.add(new SimpleSuggestion(courseNameWithTitle, courseNameWithTitle, suggestion.getTitle() == null ? courseName : courseName + " " + suggestion.getTitle()));
					iValidCourseNames.add(courseNameWithTitle);
				}
			}
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}
		
	}
	
	public static class SimpleSuggestion implements Suggestion, AriaSuggestBox.HasStatus {
		private String iDisplay, iReplace, iStatus;

		public SimpleSuggestion(String display, String replace, String status) {
			iDisplay = display;
			iReplace = replace;
			iStatus = status;
		}
		
		public SimpleSuggestion(String display, String replace) {
			this(display, replace, display);
		}
		
		public SimpleSuggestion(String replace) {
			this(replace, replace, replace);
		}

		@Override
		public String getDisplayString() {
			return iDisplay;
		}

		@Override
		public String getReplacementString() {
			return iReplace;
		}

		@Override
		public String getStatusString() {
			return iStatus;
		}
	}
	
	public void setHint(String hint) {
		if (iSuggest.getText().equals(iHint)) {
			iSuggest.setText(hint);
			iSuggest.setStyleName("unitime-TextBoxHint");
		}
		iHint = hint;
	}
	
	public String getHint() {
		return iHint;
	}
	
	public static interface Validator {
		public String validate(CourseSelectionBox source);
	}
	
	public void addValidator(Validator validator) {
		iValitaros.add(validator);
	}
	
	public void hideError() {
		iError.setVisible(false);
	}
	
	public String validate() {
		if (iSuggest.getText().isEmpty() || iSuggest.getText().equals(iHint)) {
			iError.setVisible(false);
			return null;
		}
		if (iAllowFreeTime) {
			try {
				parseFreeTime(iSuggest.getText());
				iError.setVisible(false);
				return null;
			} catch (IllegalArgumentException e) {
				if (iSuggest.getText().toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) {
					iError.setText(MESSAGES.invalidFreeTime());
					iError.setTitle(e.getMessage());
					iError.setVisible(true);
					return e.getMessage();
				}
			}
		}
		for (Validator validator: iValitaros) {
			String message = validator.validate(this);
			if (message!=null) {
				iError.setText(message);
				iError.setTitle(null);
				iError.setVisible(true);
				return message;
			}
		}
		iError.setVisible(false);
		return null;
	}
	
	public ArrayList<CourseRequestInterface.FreeTime> parseFreeTime(String text) throws IllegalArgumentException {
		if (!iAllowFreeTime)
			throw new IllegalArgumentException(MESSAGES.freeTimeNotAllowed());
		if (iValidCourseNames.contains(text))
			throw new IllegalArgumentException(MESSAGES.notFreeTimeIsCourse(text));
		ArrayList<CourseRequestInterface.FreeTime> ret = new ArrayList<CourseRequestInterface.FreeTime>();
		if (text.isEmpty()) throw new IllegalArgumentException(MESSAGES.courseSelectionNoFreeTime());
		ArrayList<Integer> lastDays = new ArrayList<Integer>();
		String tokens[] = text.split("[,;]");
		for (String token: tokens) {
			String original = token;
			if (token.toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) token = token.substring(CONSTANTS.freePrefix().length());
			ArrayList<Integer> days = new ArrayList<Integer>();
			while (token.startsWith(" ")) token = token.substring(1);
			boolean found = false;
			do {
				found = false;
				for (int i=0; i<CONSTANTS.longDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.longDays()[i].toLowerCase())) {
						days.add(i);
						token = token.substring(CONSTANTS.longDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].toLowerCase())) {
						days.add(i);
						token = token.substring(CONSTANTS.days()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].substring(0,2).toLowerCase())) {
						days.add(i);
						token = token.substring(2);
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.shortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.shortDays()[i].toLowerCase())) {
						days.add(i);
						token = token.substring(CONSTANTS.shortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.freeTimeShortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.freeTimeShortDays()[i].toLowerCase())) {
						days.add(i);
						token = token.substring(CONSTANTS.freeTimeShortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
			} while (found);
			int startHour = 0, startMin = 0;
			while (token.startsWith(" ")) token = token.substring(1);
			String number = "";
			while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
			if (number.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeExpectedDayOrNumber(original, 1 + original.lastIndexOf(token)));
			if (number.length()>2) {
				startHour = Integer.parseInt(number) / 100;
				startMin = Integer.parseInt(number) % 100;
			} else {
				startHour = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			if (token.startsWith(":")) {
				token = token.substring(1);
				while (token.startsWith(" ")) token = token.substring(1);
				number = "";
				while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
				if (number.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeExpectedNumber(original, 1 + original.lastIndexOf(token)));
				startMin = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			boolean hasAmOrPm = false;
			if (token.toLowerCase().startsWith("am")) {token = token.substring(2); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("a")) {token = token.substring(1); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (startHour < 7 && !hasAmOrPm) startHour += 12;
			//if (startMin < 29) startMin = 0; else startMin = 30;
			if (startMin % 5 != 0) startMin = 5 * ((startMin + 2)/ 5);
			if (startHour == 7 && startMin == 0 && !hasAmOrPm) startHour += 12;
			int startTime = (60 * startHour + startMin) / 5; // (60 * startHour + startMin) / 30 - 15
			int endTime = startTime;
			while (token.startsWith(" ")) token = token.substring(1);
			if (token.startsWith("-")) {
				int endHour = 0, endMin = 0;
				token = token.substring(1);
				while (token.startsWith(" ")) token = token.substring(1);
				number = "";
				while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
				if (number.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeExpectedNumber(original, 1 + original.lastIndexOf(token)));
				if (number.length()>2) {
					endHour = Integer.parseInt(number) / 100;
					endMin = Integer.parseInt(number) % 100;
				} else {
					endHour = Integer.parseInt(number);
				}
				while (token.startsWith(" ")) token = token.substring(1);
				if (token.startsWith(":")) {
					token = token.substring(1);
					while (token.startsWith(" ")) token = token.substring(1);
					number = "";
					while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
					if (number.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeExpectedNumber(original, 1 + original.lastIndexOf(token)));
					endMin = Integer.parseInt(number);
				}
				while (token.startsWith(" ")) token = token.substring(1);
				hasAmOrPm = false;
				if (token.toLowerCase().startsWith("am")) { token = token.substring(2); hasAmOrPm = true; if (endHour == 12) endHour += 12; }
				if (token.toLowerCase().startsWith("a")) { token = token.substring(1); hasAmOrPm = true; if (endHour == 12) endHour += 12; }
				if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (endHour < 12) endHour += 12; }
				if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (endHour < 12) endHour += 12; }
				if (endHour <= 7 && !hasAmOrPm) endHour += 12;
				// if (endMin < 29) endMin = 0; else endMin = 30;
				if (endMin % 5 != 0) endMin = 5 * ((endMin + 2)/ 5);
				endTime = (60 * endHour + endMin) / 5; // (60 * endHour + endMin) / 30 - 15
			}
			while (token.startsWith(" ")) token = token.substring(1);
			if (!token.isEmpty()) throw new IllegalArgumentException(MESSAGES.invalidFreeTimeGeneric(original, 1 + original.lastIndexOf(token)));
			if (days.isEmpty()) days = lastDays;
			if (days.isEmpty()) {
				for (int i=0; i<CONSTANTS.freeTimeDays().length; i++)
					days.add(i);
			}
			if (startTime == endTime) {
				endTime += 6;
				if ((days.contains(0) || days.contains(2) || days.contains(4)) && !days.contains(1) && !days.contains(3)) {
					if (startTime % 12 == 6) endTime += 6;
				} else if ((days.contains(1) || days.contains(3)) && !days.contains(0) && !days.contains(2) && !days.contains(4)) {
					if (startTime % 18 == 0) endTime += 12;
					else if (startTime % 18 == 6) endTime += 6;
				}
			}
			if (startTime < 0 || startTime > 24 * 12)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeInvalidStartTime(original));
			if (endTime < 0 || endTime > 24 * 12)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeInvalidEndTime(original));
			/*
			if (startTime < 0)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeStartBeforeFirst(original, CONSTANTS.freeTimePeriods()[0]));
			if (startTime >= CONSTANTS.freeTimePeriods().length - 1)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeStartAfterLast(original, CONSTANTS.freeTimePeriods()[CONSTANTS.freeTimePeriods().length - 2]));
			if (endTime < 0)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeEndBeforeFirst(original, CONSTANTS.freeTimePeriods()[0]));
			if (endTime >= CONSTANTS.freeTimePeriods().length) 
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeEndAfterLast(original, CONSTANTS.freeTimePeriods()[CONSTANTS.freeTimePeriods().length - 1]));
			*/
			if (startTime >= endTime)
				throw new IllegalArgumentException(MESSAGES.invalidFreeTimeStartNotBeforeEnd(original));
			CourseRequestInterface.FreeTime f = new CourseRequestInterface.FreeTime();
			for (int day: days)
				f.addDay(day);
			f.setStart(startTime); // 6 * (startTime + 15));
			f.setLength(endTime - startTime); // 6 * (endTime - startTime));
			ret.add(f);
			lastDays = days;
		}
		return ret;
	}
	
	public String freeTimesToString(ArrayList<CourseRequestInterface.FreeTime> freeTimes) {
		String ret = "";
		String lastDays = null;
		for (CourseRequestInterface.FreeTime ft: freeTimes) {
			if (ret.length() > 0) ret += ", ";
			String days = ft.getDaysString(CONSTANTS.shortDays(), "");
			if (ft.getDays().size() == CONSTANTS.freeTimeDays().length && !ft.getDays().contains(5) && !ft.getDays().contains(6)) days = "";
			ret += (days.isEmpty() || days.equals(lastDays) ? "" : days + " ") + ft.getStartString(CONSTANTS.useAmPm()) + " - " + ft.getEndString(CONSTANTS.useAmPm());
			lastDays = days;
		}
		return CONSTANTS.freePrefix() + ret;
	}
}
