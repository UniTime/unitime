/*
 * UniTime 3.2 (University Timetabling Application)
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
import java.util.List;

import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
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
import com.google.gwt.user.client.ui.Focusable;
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

public class CurriculaCourseSelectionBox extends Composite implements Validator, Focusable {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private TextBox iTextField;
	private SuggestBox iSuggest;
	private Image iImage;
	private HorizontalPanel iHPanel;
	private VerticalPanel iVPanel;
	private Label iError;
	
	private TextBox iFilter;
	private DialogBox iDialog;
	private ScrollPanel iCoursesPanel;
	private VerticalPanel iDialogPanel;
	private WebTable iCourses, iClasses;
	private String iLastQuery = null;
	private Label iCoursesTip;
	private CourseCurriculaTable iCurricula;
	
	private TabPanel iCourseDetailsTabPanel;
	
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
	
	private List<CurriculumInterface.AcademicClassificationInterface> iClassifications;
	private List<CourseFinderDialogHandler> iCourseFinderDialogHandlers = new ArrayList<CourseFinderDialogHandler>();
		
	public CurriculaCourseSelectionBox(String name, List<CurriculumInterface.AcademicClassificationInterface> classifications) {
		
		iClassifications = classifications;
		
		SuggestOracle courseOfferingOracle = new SuggestOracle() {
			public void requestSuggestions(Request request, Callback callback) {
				if (request.getQuery().equals(iHint)) return;
				iCurriculaService.listCourseOfferings(request.getQuery(), request.getLimit(), new SuggestCallback(request, callback));
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
		
		iSuggest.addSelectionHandler(new SelectionHandler<Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iTextField.getText(), !iTextField.getText().isEmpty());
				for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
					h.onChange(e);
			}
		});
		iTextField.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iTextField.getText(), false);
				for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
					h.onChange(e);
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
							new WebTable.Cell(MESSAGES.colNote(), 1, "300"),
							new WebTable.Cell("Limit", 1, "60"),
							new WebTable.Cell("Last-Like", 1, "60"),
							new WebTable.Cell("Projected", 1, "60"),
							new WebTable.Cell("Enrolled", 1, "60")
							));
			
			iDialogPanel = new VerticalPanel();
			iDialogPanel.setSpacing(5);
			iDialogPanel.add(iFilter);
			iDialogPanel.setCellHorizontalAlignment(iFilter, HasHorizontalAlignment.ALIGN_CENTER);
			
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
			
			iCurricula = new CourseCurriculaTable(false, false);
			iCurricula.setMessage(MESSAGES.courseSelectionNoCourseSelected());
			iCurriculaPanel = new ScrollPanel(iCurricula);
			iCurriculaPanel.setStyleName("unitime-ScrollPanel-inner");
			iCourseDetailsTabPanel.add(iCurriculaPanel, new HTML("<u>C</u>urricula", false));
			
			iCourseDetailsTabPanel.getDeckPanel().setSize("780", "200");
			iCourseDetailsTabPanel.getDeckPanel().setStyleName("unitime-TabPanel");
			
			iCourseDetailsTabPanel.selectTab(sLastSelectedCourseDetailsTab);

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
			
			iDialogPanel.add(iCoursesPanel);
			iDialogPanel.add(iCourseDetailsTabPanel);
			iDialogPanel.add(iCoursesTip);
			
			iDialog.setWidget(iDialogPanel);
			
			iFilter.addKeyUpHandler(new KeyUpHandler() {
				public void onKeyUp(KeyUpEvent event) {
					updateCourses();
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER || event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
						if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
							if (iCourses.getSelectedRow()>=0) {
								WebTable.Row r = iCourses.getRows()[iCourses.getSelectedRow()];
								iTextField.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
								CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iTextField.getText(), true);
								for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
									h.onChange(e);
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
					if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='d' || event.getNativeKeyCode()=='D')) {
						iCourseDetailsTabPanel.selectTab(0);
						event.preventDefault();
					}
					if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='l' || event.getNativeKeyCode()=='L')) {
						iCourseDetailsTabPanel.selectTab(1);
						event.preventDefault();
					}
					if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='c' || event.getNativeKeyCode()=='C')) {
						iCourseDetailsTabPanel.selectTab(2);
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
					iTextField.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
					iDialog.hide();
					iImage.setResource(RESOURCES.search_picker());
					CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iTextField.getText(), true);
					for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
						h.onChange(e);
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
		
		CourseFinderDialogEvent e = new CourseFinderDialogEvent();
		for (CourseFinderDialogHandler h: iCourseFinderDialogHandlers)
			h.onOpen(e);
		
		iImage.setResource(RESOURCES.search_picker_Down());
		iFilter.setText(iTextField.getText().equals(iHint)?"":iTextField.getText());
		iCoursesTip.setText(CONSTANTS.courseTips()[(int)(Math.random() * CONSTANTS.courseTips().length)]);
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

	@Override
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
		if (fireChangeEvent) {
			CourseSelectionChangeEvent e = new CourseSelectionChangeEvent(iTextField.getText(), course != null && !course.isEmpty());
			for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
				h.onChange(e);
		}
	}
	
	public void setEnabled(boolean enabled) {
		if (enabled) {
			iTextField.setEnabled(true);
			iImage.setVisible(true);
			iImage.setResource(RESOURCES.search_picker());
			iTextField.getElement().getStyle().setBorderColor(null);
			iTextField.getElement().getStyle().setBackgroundColor(null);
		} else {
			iTextField.setEnabled(false);
			iImage.setVisible(false);
			iImage.setResource(RESOURCES.search_picker_Disabled());
			iTextField.getElement().getStyle().setBorderColor("transparent");
			iTextField.getElement().getStyle().setBackgroundColor("transparent");
		}
	}
	
	public boolean isEnabled() {
		return iTextField.isEnabled();
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
					for (ClassAssignmentInterface.CourseAssignment record: result) {
						records[idx] = new WebTable.Row(
								record.getSubject(),
								record.getCourseNbr(),
								record.getTitle(),
								record.getNote(),
								record.getLimitString(),
								record.getLastLikeString(),
								record.getProjectedString(),
								record.getEnrollmentString());
						if (!hasEnrl && !record.getEnrollmentString().isEmpty()) hasEnrl = true;
						if (!hasProj && !record.getProjectedString().isEmpty()) hasProj = true;
						if (!hasLastLike && !record.getLastLikeString().isEmpty()) hasLastLike = true;
						records[idx].setId(record.getCourseId().toString());
						idx++;
					}
					iCourses.setData(records);
					iCourses.setColumnVisible(5, hasProj);
					iCourses.setColumnVisible(6, hasEnrl);
					iCourses.setColumnVisible(7, hasLastLike);
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
			iCurriculaService.listCourseOfferings(iFilter.getText(), null, iCourseOfferingsCallback);
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
	
	@Override
	public void setFocus(boolean focus) {
		iTextField.setFocus(focus);
		if (focus) iTextField.selectAll();

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
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", ""));
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}

		public void onSuccess(Collection<ClassAssignmentInterface.CourseAssignment> result) {
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			for (ClassAssignmentInterface.CourseAssignment suggestion: result) {
				String courseName = MESSAGES.courseName(suggestion.getSubject(), suggestion.getCourseNbr());
				String courseNameWithTitle = (suggestion.getTitle() == null ? courseName :
					MESSAGES.courseNameWithTitle(suggestion.getSubject(), suggestion.getCourseNbr(), suggestion.getTitle()));
				suggestions.add(new SimpleSuggestion(courseNameWithTitle, courseName));
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
		public String validate(CurriculaCourseSelectionBox source);
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
	
	public void addFocusHandler(FocusHandler h) {
		iTextField.addFocusHandler(h);
	}

	public void addBlurHandler(BlurHandler h) {
		iTextField.addBlurHandler(h);
	}

	@Override
	public int getTabIndex() {
		return iTextField.getTabIndex();
	}

	@Override
	public void setTabIndex(int index) {
		iTextField.setTabIndex(index);
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
