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
package org.unitime.timetable.gwt.client.curricula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTabPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.client.widgets.WebTable.RowDoubleClickEvent;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Focusable;
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
public class CurriculaCourseSelectionBox extends Composite implements Focusable {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final GwtMessages MESSAGESGWT = GWT.create(GwtMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);

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
	private VerticalPanel iDialogPanel;
	private WebTable iCourses, iClasses;
	private String iLastQuery = null;
	private Label iCoursesTip;
	private CourseCurriculaTable iCurricula;
	
	private UniTimeTabPanel iCourseDetailsTabPanel;
	
	private HTML iCourseDetails;
	private ScrollPanel iCourseDetailsPanel, iClassesPanel, iCurriculaPanel;
	
	private final CurriculaServiceAsync iCurriculaService = GWT.create(CurriculaService.class);
	
	private AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> iCourseOfferingsCallback;
	
	private AsyncCallback<String> iCourseDetailsCallback;
	private AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> iCourseClassesCallback;

	private ArrayList<CourseSelectionChangeHandler> iCourseSelectionChangeHandlers = new ArrayList<CourseSelectionChangeHandler>();
	
	private ArrayList<Validator> iValitaros = new ArrayList<Validator>();
	
	private String iHint = "";
	
	private String iLastCourseLookup = null;
	
	private static int sLastSelectedCourseDetailsTab = 0;
	
	private List<CourseFinderDialogHandler> iCourseFinderDialogHandlers = new ArrayList<CourseFinderDialogHandler>();
		
	public CurriculaCourseSelectionBox(String name) {
		
		SuggestOracle courseOfferingOracle = new SuggestOracle() {
			public void requestSuggestions(Request request, Callback callback) {
				if (request.getQuery().equals(iHint)) return;
				iCurriculaService.listCourseOfferings(request.getQuery(), request.getLimit(), new SuggestCallback(request, callback));
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
		iVPanel.add(iError);
				
		iFinderButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (iSuggest.isEnabled()) {
					openDialogAsync();
				}
			}
		});
		
		iSuggest.addSelectionHandler(new SelectionHandler<Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				String text = event.getSelectedItem().getReplacementString();
				iLastSuggestion = text;
				CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(text, !text.isEmpty());
				for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
					h.onChange(e);
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
				CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iSuggest.getText(), valid);
				for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
					h.onChange(e);
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
			public void onSuccess() {
				openDialog();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label(MESSAGES.failedToLoadTheApp(reason.getMessage()));
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("loading").setVisible(false);
				RootPanel.get("body").add(error);
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
			iFilter.setAriaLabel(ARIA.courseFinderFilter());
			
			iFilterSelect = new AriaButton(MESSAGES.buttonSelect());
			iFilterSelect.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iCourses.getSelectedRow()>=0 && iCourses.getRows()!=null && iCourses.getSelectedRow() < iCourses.getRows().length) {
						WebTable.Row r = iCourses.getRows()[iCourses.getSelectedRow()];
						if ("true".equals(r.getId()))
							iSuggest.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
						else
							iSuggest.setText(MESSAGES.courseNameWithTitle(r.getCell(0).getValue(), r.getCell(1).getValue(), r.getCell(2).getValue()));
						CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iSuggest.getText(), true);
						for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
							h.onChange(e);
					} else {
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
							new WebTable.Cell(MESSAGES.colTitle(), 1, "400px"),
							new WebTable.Cell(MESSAGESGWT.colLimit(), 1, "60px"),
							new WebTable.Cell(MESSAGESGWT.colLastLike(), 1, "60px"),
							new WebTable.Cell(MESSAGESGWT.colProjected(), 1, "60px"),
							new WebTable.Cell(MESSAGESGWT.colEnrolled(), 1, "60px")
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
			
			iCourseDetailsTabPanel = new UniTimeTabPanel();
			
			iCourseDetails = new HTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionNoCourseSelected() + "</td></tr></table>");
			iCourseDetailsPanel = new ScrollPanel(iCourseDetails);
			iCourseDetailsPanel.setStyleName("unitime-ScrollPanel-inner");
			iCourseDetailsPanel.getElement().getStyle().setWidth(780, Unit.PX);
			iCourseDetailsPanel.getElement().getStyle().setHeight(200, Unit.PX);
			iCourseDetailsTabPanel.add(iCourseDetailsPanel, MESSAGES.courseSelectionDetails(), true);
			final Character chDetails = UniTimeHeaderPanel.guessAccessKey(MESSAGES.courseSelectionDetails());
			
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
			iCourseDetailsTabPanel.add(iClassesPanel, MESSAGES.courseSelectionClasses(), true);
			final Character chClasses = UniTimeHeaderPanel.guessAccessKey(MESSAGES.courseSelectionClasses());
			
			iCurricula = new CourseCurriculaTable(false, false);
			iCurricula.setMessage(MESSAGES.courseSelectionNoCourseSelected());
			iCurriculaPanel = new ScrollPanel(iCurricula);
			iCurriculaPanel.setStyleName("unitime-ScrollPanel-inner");
			iCurriculaPanel.getElement().getStyle().setWidth(780, Unit.PX);
			iCurriculaPanel.getElement().getStyle().setHeight(200, Unit.PX);
			iCourseDetailsTabPanel.add(iCurriculaPanel, MESSAGESGWT.tabCurricula(), true);
			final Character chCurricula = UniTimeHeaderPanel.guessAccessKey(MESSAGESGWT.tabCurricula());
						
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
			
			iCourseDetailsTabPanel.setDeckStyleName("unitime-TabPanel");
			iDialogPanel.add(iCoursesPanel);
			iDialogPanel.add(iCourseDetailsTabPanel);
			iDialogPanel.add(iCoursesTip);
			
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
							if (iCourses.getSelectedRow()>=0 && iCourses.getRows()!=null && iCourses.getSelectedRow() < iCourses.getRows().length) {
								WebTable.Row r = iCourses.getRows()[iCourses.getSelectedRow()];
								iSuggest.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
								CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iSuggest.getText(), true);
								for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
									h.onChange(e);
							} else {
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
					if (chDetails != null && (event.getNativeKeyCode()==Character.toLowerCase(chDetails) || event.getNativeKeyCode()==Character.toUpperCase(chDetails)) && (event.isControlKeyDown() || event.isAltKeyDown())) {
						iCourseDetailsTabPanel.selectTab(0);
						event.preventDefault();
						if (iCourses.getSelectedRow() >= 0 && iCourses.getRows() != null && iCourses.getSelectedRow() < iCourses.getRows().length)
							AriaStatus.getInstance().setHTML(iCourseDetails.getHTML());
						else
							AriaStatus.getInstance().setHTML(ARIA.courseFinderNoCourse());
					}
					if (chClasses != null && (event.getNativeKeyCode()==Character.toLowerCase(chClasses) || event.getNativeKeyCode()==Character.toUpperCase(chClasses)) && (event.isControlKeyDown() || event.isAltKeyDown())) {
						iCourseDetailsTabPanel.selectTab(1);
						event.preventDefault();
						courseSelectionChanged();
					}
					if (chCurricula != null && (event.getNativeKeyCode()==Character.toLowerCase(chCurricula) || event.getNativeKeyCode()==Character.toUpperCase(chCurricula)) && (event.isControlKeyDown() || event.isAltKeyDown())) {
						iCourseDetailsTabPanel.selectTab(2);
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
			iCourseDetailsTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
				public void onSelection(SelectionEvent<Integer> event) {
					sLastSelectedCourseDetailsTab = event.getSelectedItem();
				}
			});
			
			iCourses.addRowDoubleClickHandler(new WebTable.RowDoubleClickHandler() {
				public void onRowDoubleClick(RowDoubleClickEvent event) {
					WebTable.Row r = event.getRow();
					iSuggest.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
					iDialog.hide();
					CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iSuggest.getText(), true);
					for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
						h.onChange(e);
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
							@Override
							public void execute() {
								iFilter.setFocus(true);
							}
						});
					}
				}
			});
		}
		
		CourseFinderDialogEvent e = new CourseFinderDialogEvent();
		for (CourseFinderDialogHandler h: iCourseFinderDialogHandlers)
			h.onOpen(e);
		
		iFilter.setText(iSuggest.getText().equals(iHint)?"":iSuggest.getText());
		iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
		iCourseDetailsTabPanel.selectTab(sLastSelectedCourseDetailsTab);
		AriaStatus.getInstance().setText(ARIA.courseFinderDialogOpened());
		iDialog.center();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iFilter.setFocus(true);
				updateCourses();
			}
		});

		updateCourses();
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
		
		Element item = iCourses.getTable().getRowFormatter().getElement(iCourses.getSelectedRow());
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
		if (fireChangeEvent) {
			CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iSuggest.getText(), course != null && !course.isEmpty());
			for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
				h.onChange(e);
		}
	}
	
	public void setEnabled(boolean enabled) {
		if (enabled) {
			iSuggest.setEnabled(true);
			iFinderButton.setEnabled(true);
			iFinderButton.setTabIndex(0);
			iFinderButton.setVisible(true);
		} else {
			iSuggest.setEnabled(false);
			iFinderButton.setEnabled(false);
			iFinderButton.setTabIndex(-1);
			iFinderButton.setVisible(false);
			iSuggest.getElement().getStyle().setBorderColor("transparent");
			iSuggest.getElement().getStyle().setBackgroundColor("transparent");
		}
	}
	
	public boolean isEnabled() {
		return iSuggest.isEnabled();
	}
	
	private void updateCourses() {
		if (iCourseOfferingsCallback==null) {
			iCourseOfferingsCallback = new AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>>() {
				public void onFailure(Throwable caught) {
					iCourses.clearData(true);
					iCourses.setEmptyMessage(caught.getMessage());
				}
				public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
					WebTable.Row[] records = new WebTable.Row[result.size()];
					int idx = 0;
					boolean hasProj = false, hasEnrl = false, hasLastLike = false;
					int selectRow = -1;
					for (ClassAssignmentInterface.CourseAssignment record: result) {
						records[idx] = new WebTable.Row(
								record.getSubject(),
								record.getCourseNbr(),
								record.getTitle(),
								record.getLimitString(),
								record.getLastLikeString(),
								record.getProjectedString(),
								record.getEnrollmentString());
						if (!hasEnrl && !record.getEnrollmentString().isEmpty()) hasEnrl = true;
						if (!hasProj && !record.getProjectedString().isEmpty()) hasProj = true;
						if (!hasLastLike && !record.getLastLikeString().isEmpty()) hasLastLike = true;
						records[idx].setId(record.getCourseId().toString());
						if (iFilter.getText().equalsIgnoreCase(record.getSubject() + " " + record.getCourseNbr()))
							selectRow = idx;
						idx++;
					}
					iCourses.setData(records);
					iCourses.setColumnVisible(5, hasProj);
					iCourses.setColumnVisible(6, hasEnrl);
					iCourses.setColumnVisible(7, hasLastLike);
					if (records.length == 1) selectRow = 0;
					if (selectRow >= 0) {
						iCourses.setSelectedRow(selectRow);
						updateCourseDetails();
					}
				}
	        };
		}
		if (iFilter.getText().equals(iLastQuery)) return;
		if (iFilter.getText().isEmpty()) {
			iCourses.setEmptyMessage(MESSAGES.courseSelectionNoCourseFilter());
		} else {
			iCourses.setEmptyMessage(MESSAGES.courseSelectionLoadingCourses());
			iCurriculaService.listCourseOfferings(iFilter.getText(), 100, iCourseOfferingsCallback);
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
							WebTable.Row row = new WebTable.Row(
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
									(clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")),
									clazz.hasNote() ? new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), "") : new WebTable.Cell(""));
							row.setId(clazz.getClassId().toString());
							String styleName = "";
							if (lastSubpartId != null && !clazz.getSubpartId().equals(lastSubpartId))
								styleName += " .top-border-dashed";
							if (!clazz.isAvailable())
								styleName += " .text-gray";
							for (WebTable.Cell cell: row.getCells())
								cell.setStyleName(styleName.trim());
							rows[idx++] = row;
							lastSubpartId = clazz.getSubpartId();
						}
						iClasses.setData(rows);
					} else {
						String courseName = iFilter.getText();
						try {
							WebTable.Row row = iCourses.getRows()[iCourses.getSelectedRow()];
							courseName = MESSAGES.courseName(row.getCell(0).getValue(), row.getCell(1).getValue());
						} catch (Exception e) {}
						iClasses.setEmptyMessage(MESSAGES.courseSelectionNoClasses(courseName));
					}
				}
			};
		}
		if (iCourses.getSelectedRow()<0) {
			iCourseDetails.setHTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionNoCourseSelected() + "</td></tr></table>");
			iClasses.setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
			iClasses.clearData(true);
			iCurricula.setMessage(MESSAGES.courseSelectionNoCourseSelected());
			iCurricula.clear(false);
		} else {
			WebTable.Row row = iCourses.getRows()[iCourses.getSelectedRow()];
			String courseName = MESSAGES.courseName(row.getCell(0).getValue(), row.getCell(1).getValue());
			if (courseName.equals(iLastCourseLookup)) return;
			iCourseDetails.setHTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionLoadingDetails() + "</td></tr></table>");
			iCourseDetailsPanel.setVisible(true);
			iClasses.clearData(true);
			iClasses.setEmptyMessage(MESSAGES.courseSelectionLoadingClasses());
			iCurriculaService.retrieveCourseDetails(courseName, iCourseDetailsCallback);
			iCurriculaService.listClasses(courseName, iCourseClassesCallback);
			iCurricula.setCourseName(courseName);
			iLastCourseLookup = courseName;
		}
	}
	
	public void setFocus(boolean focus) {
		iSuggest.setFocus(focus);
		if (focus) iSuggest.getValueBox().selectAll();

	}
	
	public class CourseSelectionChangeEvent{
		private String iCourse;
		private boolean iValid;
		
		public CourseSelectionChangeEvent(String course, boolean valid) {
			iCourse = course;
			iValid = valid;
		}
		
		public String getCourse() { return iCourse; }
		public boolean isValid() { return iValid; }
		public CurriculaCourseSelectionBox getSource() { return CurriculaCourseSelectionBox.this; }
	}
		
	public interface CourseSelectionChangeHandler{
		public void onChange(CourseSelectionChangeEvent evt);
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
			suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", ""));
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}

		public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			iValidCourseNames.clear();
			for (ClassAssignmentInterface.CourseAssignment suggestion: result) {
				String courseName = MESSAGES.courseName(suggestion.getSubject(), suggestion.getCourseNbr());
				String courseNameWithTitle = (suggestion.getTitle() == null ? courseName :
					MESSAGES.courseNameWithTitle(suggestion.getSubject(), suggestion.getCourseNbr(), suggestion.getTitle()));
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
		public String validate(CurriculaCourseSelectionBox source);
	}
	
	public void addValidator(Validator validator) {
		iValitaros.add(validator);
	}
	
	public void hideError() {
		iError.setVisible(false);
	}
	
	public void validate(final AsyncCallback<String> callback) {
		if (iSuggest.getText().isEmpty() || iSuggest.getText().equals(iHint)) {
			iError.setVisible(false);
			callback.onSuccess(null);
			return;
		}
		for (Validator validator: iValitaros) {
			String message = validator.validate(this);
			if (message!=null) {
				iError.setText(message);
				iError.setTitle(null);
				iError.setVisible(true);
				callback.onSuccess(message);
				return ;
			}
		}
		iError.setVisible(false);
		callback.onSuccess(null);
	}
	
	@Override
	public int getTabIndex() {
		return iSuggest.getTabIndex();
	}

	@Override
	public void setTabIndex(int index) {
		iSuggest.setTabIndex(index);
	}
	
	public class CourseFinderDialogEvent {
		public CurriculaCourseSelectionBox getSource() { 
			return CurriculaCourseSelectionBox.this;
		}
	}
	
	public interface CourseFinderDialogHandler {
		public void onOpen(CourseFinderDialogEvent e);
	}
	
	public void addCourseFinderDialogHandler(CourseFinderDialogHandler h) {
		iCourseFinderDialogHandlers.add(h);
	}
}
