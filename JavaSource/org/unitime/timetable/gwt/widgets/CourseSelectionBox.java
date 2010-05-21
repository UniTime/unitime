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
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.ToolBox;
import org.unitime.timetable.gwt.widgets.WebTable.RowDoubleClickEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class CourseSelectionBox extends Composite implements Validator {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private AcademicSessionProvider iAcademicSessionProvider;
	
	private TextBox iTextField;
	private SuggestBox iSuggest;
	private Image iImage;
	private HorizontalPanel iHPanel;
	private VerticalPanel iVPanel;
	private Label iError;
	
	private TextBox iFilter;
	private DialogBox iDialog;
	private ScrollPanel iCoursesPanel;
	private VerticalPanel iDialogPanel, iCoursesTab, iFreeTimeTab;
	private WebTable iCourses, iClasses;
	private String iLastQuery = null;
	private FreeTimePicker iFreeTimePicker;
	private Label iFreeTimeError = null, iCoursesTip, iFreeTimeTip;
	
	private TabPanel iTabPanel, iCourseDetailsTabPanel;
	
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
		
		iTextField = new TextBox();
		iTextField.setStyleName("gwt-SuggestBox");
		iTextField.setName(name);
		iSuggest = new SuggestBox(courseOfferingOracle, iTextField);
		
		iTextField.setStyleName("unitime-TextBoxHint");

		iImage = new Image(RESOURCES.search_picker());
		iImage.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				if (iTextField.isEnabled())
					iImage.setResource(RESOURCES.search_picker_Over());
			}
		});
		iImage.addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				if (iTextField.isEnabled())
					iImage.setResource(RESOURCES.search_picker());
			}
		});
		
		iVPanel = new VerticalPanel();
		
		iHPanel = new HorizontalPanel();
		iHPanel.add(iSuggest);
		iHPanel.add(iImage);
		iVPanel.add(iHPanel);
		
		iError = new Label();
		iError.setStyleName("unitime-ErrorHint");
		iError.setVisible(false);
		iVPanel.add(iError);
				
		iImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (iTextField.isEnabled()) {
					openDialogAsync();
				}
			}
		});
		
		if (!enabled) setEnabled(false);
		
		iSuggest.addSelectionHandler(new SelectionHandler<Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
					h.onChange(iTextField.getText(), !iTextField.getText().isEmpty());
			}
		});
		iTextField.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
					h.onChange(iTextField.getText(), false);
			}
		});
		iTextField.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (!iTextField.isEnabled()) return;
				if ((event.getNativeEvent().getKeyCode()=='F' || event.getNativeEvent().getKeyCode()=='f') && event.isControlKeyDown()) {
					iSuggest.hideSuggestionList();
					openDialogAsync();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_ESCAPE) {
					iSuggest.hideSuggestionList();
				}
				if ((event.getNativeEvent().getKeyCode()=='S' || event.getNativeEvent().getKeyCode()=='s') && event.isControlKeyDown()) {
					iSuggest.showSuggestionList();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_DOWN && event.isControlKeyDown() && iNext!=null) {
					iSuggest.hideSuggestionList();
					if (event.isShiftKeyDown()) swapDown();
					iNext.setFocus(true);
					DeferredCommand.addCommand(new Command() {
						public void execute() {
							iNext.hideSuggestionList();
						}
					});
					event.preventDefault();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_UP && event.isControlKeyDown() && iPrev!=null) {
					iSuggest.hideSuggestionList();
					if (event.isShiftKeyDown()) swapUp();
					iPrev.setFocus(true);
					DeferredCommand.addCommand(new Command() {
						public void execute() {
							iPrev.hideSuggestionList();
						}
					});
					event.preventDefault();
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_LEFT && event.isControlKeyDown() && iPrimary!=null) {
					iSuggest.hideSuggestionList();
					iPrimary.setFocus(true);
				}
				if (event.getNativeEvent().getKeyCode()==KeyCodes.KEY_RIGHT && event.isControlKeyDown() && iAlternative!=null && iAlternative.isEnabled()) {
					iSuggest.hideSuggestionList();
					iAlternative.setFocus(true);
				}
			}
		});
		iTextField.addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				if (iTextField.getText().isEmpty()) {
					if (iError.isVisible()) iError.setVisible(false);
					if (iHint!=null) iTextField.setText(iHint);
					iTextField.setStyleName("unitime-TextBoxHint");
				}
			}
		});
		iTextField.addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				iTextField.setStyleName("gwt-SuggestBox");
				if (iTextField.getText().equals(iHint)) iTextField.setText("");
			}
		});
		
		initWidget(iVPanel);
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
	
	private void openDialog() {
		if (iDialog == null) {
			iDialog = new DialogBox();
			iDialog.setText(MESSAGES.courseSelectionDialog());
			iDialog.setAnimationEnabled(true);
			iDialog.setAutoHideEnabled(true);
			iDialog.setGlassEnabled(true);
			iDialog.setModal(true);
			
			iFilter = new TextBox();
			iFilter.setStyleName("gwt-SuggestBox");
			iFilter.setWidth("600");
			
			iCourses = new WebTable();
			iCourses.setHeader(
					new WebTable.Row(
							new WebTable.Cell(MESSAGES.colSubject(), 1, "80"),
							new WebTable.Cell(MESSAGES.colCourse(), 1, "80"),
							new WebTable.Cell(MESSAGES.colTitle(), 1, "300"),
							new WebTable.Cell(MESSAGES.colNote(), 1, "300")
							));
			
			iDialogPanel = new VerticalPanel();
			iDialogPanel.setSpacing(5);
			iDialogPanel.add(iFilter);
			iDialogPanel.setCellHorizontalAlignment(iFilter, HasHorizontalAlignment.ALIGN_CENTER);
			
			iCoursesTab = new VerticalPanel();
			iCoursesTab.setSpacing(10);
			
			iDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
				public void onClose(CloseEvent<PopupPanel> event) {
					iImage.setResource(RESOURCES.search_picker());
					DeferredCommand.addCommand(new Command() {
						public void execute() {
							setFocus(true);
						}
					});
				}
			});
					
			iCoursesPanel = new ScrollPanel(iCourses);
			iCoursesPanel.setSize("780", "200");
			iCoursesPanel.setStyleName("unitime-ScrollPanel");
			
			iCoursesTab.add(iCoursesPanel);
			
			iCourseDetailsTabPanel = new TabPanel();
			
			iCourseDetails = new HTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionNoCourseSelected() + "</td></tr></table>");
			iCourseDetailsPanel = new ScrollPanel(iCourseDetails);
			iCourseDetailsPanel.setStyleName("unitime-ScrollPanel-inner");
			iCourseDetailsTabPanel.add(iCourseDetailsPanel, new HTML(MESSAGES.courseSelectionDetails()));
			
			iClasses = new WebTable();
			iClasses.setHeader(new WebTable.Row(
					new WebTable.Cell(MESSAGES.colSubpart(), 1, "50"),
					new WebTable.Cell(MESSAGES.colClass(), 1, "90"),
					new WebTable.Cell(MESSAGES.colLimit(), 1, "60"),
					new WebTable.Cell(MESSAGES.colDays(), 1, "60"),
					new WebTable.Cell(MESSAGES.colStart(), 1, "60"),
					new WebTable.Cell(MESSAGES.colEnd(), 1, "60"),
					new WebTable.Cell(MESSAGES.colDate(), 1, "100"),
					new WebTable.Cell(MESSAGES.colRoom(), 1, "100"),
					new WebTable.Cell(MESSAGES.colInstructor(), 1, "120"),
					new WebTable.Cell(MESSAGES.colParent(), 1, "90"),
					new WebTable.Cell(MESSAGES.colHighDemand(), 1, "10")
				));
			iClasses.setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
			iClassesPanel = new ScrollPanel(iClasses);
			iClassesPanel.setStyleName("unitime-ScrollPanel-inner");
			iCourseDetailsTabPanel.add(iClassesPanel, new HTML(MESSAGES.courseSelectionClasses(), false));
			iCourseDetailsTabPanel.getDeckPanel().setSize("780", "200");
			iCourseDetailsTabPanel.getDeckPanel().setStyleName("unitime-TabPanel");
			
			iCourseDetailsTabPanel.selectTab(sLastSelectedCourseDetailsTab);

			iCoursesTab.add(iCourseDetailsTabPanel);
			
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
			iCoursesTab.add(iCoursesTip);
			
			iTabPanel = new TabPanel();		
			iTabPanel.add(iCoursesTab, new HTML(MESSAGES.courseSelectionCourses()));
			
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
			
			iTabPanel.add(iFreeTimeTab, new HTML(MESSAGES.courseSelectionFreeTime(), false));
			iTabPanel.selectTab(0);

			if (iAllowFreeTime)
				iDialogPanel.add(iTabPanel);
			else {
				iDialogPanel.add(iCoursesPanel);
				iDialogPanel.add(iCourseDetailsTabPanel);
				iDialogPanel.add(iCoursesTip);
			}
			
			iDialog.setWidget(iDialogPanel);
			
			iFilter.addKeyUpHandler(new KeyUpHandler() {
				public void onKeyUp(KeyUpEvent event) {
					updateCourses();
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER || event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
						if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
							if (iCourses.getSelectedRow()>=0) {
								WebTable.Row r = iCourses.getRows()[iCourses.getSelectedRow()];
								if ("true".equals(r.getId()))
									iTextField.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
								else
									iTextField.setText(MESSAGES.courseNameWithTitle(r.getCell(0).getValue(), r.getCell(1).getValue(), r.getCell(2).getValue()));
								for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
									h.onChange(iTextField.getText(), true);
							} else {
								try {
									iFreeTimePicker.clearFreeTime();
									ArrayList<CourseRequestInterface.FreeTime> freeTimes = parseFreeTime(iFilter.getText());
									iFreeTimePicker.setFreeTime(freeTimes, false);
									iFilter.setText(freeTimesToString(freeTimes));
								} catch (IllegalArgumentException e) {}
								iTextField.setText(iFilter.getText());
							}
						}					
						iDialog.hide();
						iImage.setResource(RESOURCES.search_picker());
						DeferredCommand.addCommand(new Command() {
							public void execute() {
								setFocus(true);
							}
						});
					}
					if (event.getNativeKeyCode()==KeyCodes.KEY_DOWN) {
						iCourses.setSelectedRow(iCourses.getSelectedRow()+1);
						scrollToSelectedRow();
						updateCourseDetails();
					}
					if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
						iCourses.setSelectedRow(iCourses.getSelectedRow()==0?iCourses.getRowsCount()-1:iCourses.getSelectedRow()-1);
						scrollToSelectedRow();
						updateCourseDetails();
					}
					if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='c' || event.getNativeKeyCode()=='C') && !isFreeTime()) {
						iTabPanel.selectTab(0);
						event.preventDefault();
					}
					if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='t' || event.getNativeKeyCode()=='T')) {
						iTabPanel.selectTab(1);
						event.preventDefault();
					}
					if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='d' || event.getNativeKeyCode()=='D')) {
						iCourseDetailsTabPanel.selectTab(0);
						event.preventDefault();
					}
					if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='l' || event.getNativeKeyCode()=='L')) {
						iCourseDetailsTabPanel.selectTab(1);
						event.preventDefault();
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
					if ("true".equals(r.getId()))
						iTextField.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
					else
						iTextField.setText(MESSAGES.courseNameWithTitle(r.getCell(0).getValue(), r.getCell(1).getValue(), r.getCell(2).getValue()));
					iDialog.hide();
					iImage.setResource(RESOURCES.search_picker());
					for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
						h.onChange(iTextField.getText(), true);
					DeferredCommand.addCommand(new Command() {
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
				}
			});
			
			iFilter.addBlurHandler(new BlurHandler() {
				public void onBlur(BlurEvent event) {
					if (iDialog.isShowing()) {
						DeferredCommand.addCommand(new Command() {
							public void execute() {
								iFilter.setFocus(true);
							}
						});
					}
				}
			});
		}
		
		iImage.setResource(RESOURCES.search_picker_Down());
		iFilter.setText(iTextField.getText().equals(iHint)?"":iTextField.getText());
		iTabPanel.selectTab(0);
		iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
		iFreeTimeTip.setText(CONSTANTS.freeTimeTips()[(int)(Math.random() * CONSTANTS.freeTimeTips().length)]);
		iCourseDetailsTabPanel.selectTab(sLastSelectedCourseDetailsTab);
		iDialog.center();
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				iFilter.setFocus(true);
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
		iTextField.setAccessKey(a);
	}
	
	public void setWidth(String width) {
		iSuggest.setWidth(width);
	}
	
	public void clear() {
		iTextField.setText(iHint);
		if (!iHint.isEmpty())
			iTextField.setStyleName("unitime-TextBoxHint");
		iError.setText(""); iError.setVisible(false);
	}
	
	public void setError(String error) {
		iError.setText(error);
		iError.setTitle(null);
		iError.setVisible(!iError.getText().isEmpty());
	}
	
	public boolean hasError() {
		return iError.isVisible();
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
		return (iTextField.getText().equals(iHint) ? "" : iTextField.getText());
	}
	
	public void setCourse(String course, boolean fireChangeEvent) {
		iTextField.setText(course);
		if (iTextField.getText().isEmpty()) {
			if (iHint!=null) iTextField.setText(iHint);
			iTextField.setStyleName("unitime-TextBoxHint");
		} else {
			iTextField.setStyleName("gwt-SuggestBox");
		}
		if (fireChangeEvent)
			for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
				h.onChange(iTextField.getText(), course != null && !course.isEmpty());
	}
	
	public void setNext(CourseSelectionBox next) { iNext = next; }
	public void setPrev(CourseSelectionBox prev) { iPrev = prev; }
	public void setPrimary(CourseSelectionBox primary) { iPrimary = primary; }
	public void setAlternative(CourseSelectionBox alternative) { iAlternative = alternative; }
	
	private void swapWith(CourseSelectionBox other) {
		iSuggest.hideSuggestionList();
		other.iSuggest.hideSuggestionList();
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
			parseFreeTime(iTextField.getText());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public boolean fillInFreeTime(CourseRequestInterface.Request request) {
		try {
			for (CourseRequestInterface.FreeTime ft: parseFreeTime(iTextField.getText()))
				request.addRequestedFreeTime(ft);
			return request.hasRequestedFreeTime();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public void setEnabled(boolean enabled) {
		if (enabled) {
			iTextField.setEnabled(true);
			iImage.setResource(RESOURCES.search_picker());
		} else {
			iTextField.setEnabled(false);
			iImage.setResource(RESOURCES.search_picker_Disabled());
		}
	}
	
	public boolean isEnabled() {
		return iTextField.isEnabled();
	}
	
	private void updateCourses() {
		if (iAllowFreeTime) {
			try {
				iFreeTimePicker.clearFreeTime();
				iFreeTimePicker.setFreeTime(parseFreeTime(iFilter.getText()), false);
				iFreeTimeError.setVisible(false);
				iTabPanel.selectTab(1);
			} catch (IllegalArgumentException e) {
				iFreeTimeError.setText(e.getMessage());
				iFreeTimeError.setVisible(true);
			}
		}
		if (iCourseOfferingsCallback==null) {
			iCourseOfferingsCallback = new AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>>() {
				public void onFailure(Throwable caught) {
					iCourses.clearData(true);
					iCourses.setEmptyMessage(caught.getMessage());
				}
				public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
					WebTable.Row[] records = new WebTable.Row[result.size()];
					int idx = 0;
					for (ClassAssignmentInterface.CourseAssignment record: result) {
						records[idx] = new WebTable.Row(
								record.getSubject(),
								record.getCourseNbr(),
								record.getTitle(),
								record.getNote());
						records[idx].setId(record.hasUniqueName() ? "true" : "false");
						if (result.size()==1 && iFilter.getText().equalsIgnoreCase(record.getSubject() + " " + record.getCourseNbr()))
							iTabPanel.selectTab(0);
						idx++;
					}
					iCourses.setData(records);
					if (records.length == 1) {
						iCourses.setSelectedRow(0);
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
							WebTable.Row row = new WebTable.Row(
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
									new WebTable.Cell(clazz.getStartString()),
									new WebTable.Cell(clazz.getEndString()),
									new WebTable.Cell(clazz.getDatePattern()),
									new WebTable.Cell(clazz.getRooms(", ")),
									new WebTable.Cell(clazz.getInstructors(", ")),
									new WebTable.Cell(clazz.getParentSection()),
									(clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")));
							row.setId(clazz.getClassId().toString());
							String styleName = "unitime-ClassRow";
							if (lastSubpartId != null && !clazz.getSubpartId().equals(lastSubpartId))
								styleName += "First";
							if (!clazz.isAvailable())
								styleName += "Unavail";
							for (WebTable.Cell cell: row.getCells())
								cell.setStyleName(styleName);
							rows[idx++] = row;
							lastSubpartId = clazz.getSubpartId();
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
		iTextField.setFocus(focus);
		if (focus) iTextField.selectAll();

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
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			if (iAllowFreeTime) {
				try {
					suggestions.add(new SimpleSuggestion(freeTimesToString(parseFreeTime(iRequest.getQuery()))));
				} catch (IllegalArgumentException e) {
					if (iRequest.getQuery().toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) {
						suggestions.add(new SimpleSuggestion("<font color='red'>"+e.getMessage()+"</font>", ""));
					} else {
						suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", ""));
					}
				}
			} else {
				suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", ""));
			}
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}

		public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			for (ClassAssignmentInterface.CourseAssignment suggestion: result) {
				String courseName = MESSAGES.courseName(suggestion.getSubject(), suggestion.getCourseNbr());
				String courseNameWithTitle = (suggestion.getTitle() == null ? courseName :
					MESSAGES.courseNameWithTitle(suggestion.getSubject(), suggestion.getCourseNbr(), suggestion.getTitle()));
				if (suggestion.hasUniqueName())
					suggestions.add(new SimpleSuggestion(courseNameWithTitle, courseName));
				else
					suggestions.add(new SimpleSuggestion(courseNameWithTitle, courseNameWithTitle));
			}
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}
		
	}
	
	public static class SimpleSuggestion implements Suggestion {
		private String iDisplay, iReplace;

		public SimpleSuggestion(String display, String replace) {
			iDisplay = display;
			iReplace = replace;
		}
		
		public SimpleSuggestion(String replace) {
			this(replace, replace);
		}

		public String getDisplayString() {
			return iDisplay;
		}

		public String getReplacementString() {
			return iReplace;
		}
	}
	
	public void setHint(String hint) {
		if (iTextField.getText().equals(iHint)) {
			iTextField.setText(hint);
			iTextField.setStyleName("unitime-TextBoxHint");
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
	
	public void validate(final AsyncCallback<String> callback) {
		if (iTextField.getText().isEmpty() || iTextField.getText().equals(iHint)) {
			iError.setVisible(false);
			callback.onSuccess(null);
			return;
		}
		if (iAllowFreeTime) {
			try {
				parseFreeTime(iTextField.getText());
				iError.setVisible(false);
				callback.onSuccess(null);
				return;
			} catch (IllegalArgumentException e) {
				if (iTextField.getText().toLowerCase().startsWith(CONSTANTS.freePrefix().toLowerCase())) {
					iError.setText(MESSAGES.invalidFreeTime());
					iError.setTitle(e.getMessage());
					iError.setVisible(true);
					callback.onSuccess(e.getMessage());
					return;
				}
			}
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
	
	public ArrayList<CourseRequestInterface.FreeTime> parseFreeTime(String text) throws IllegalArgumentException {
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
			String days = ft.getDaysString(CONSTANTS.shortDays());
			if (ft.getDays().size() == CONSTANTS.freeTimeDays().length && !ft.getDays().contains(5) && !ft.getDays().contains(6)) days = "";
			ret += (days.isEmpty() || days.equals(lastDays) ? "" : days + " ") + ft.getStartString() + " - " + ft.getEndString();
			lastDays = days;
		}
		return CONSTANTS.freePrefix() + ret;
	}
}
