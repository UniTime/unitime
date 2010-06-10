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
import java.util.TreeSet;

import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.ToolBox;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseInterface;
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

public class CurriculaCourseSelectionBox extends Composite implements Validator {
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
	private WebTable iCourses, iClasses, iCurricula;
	private String iLastQuery = null;
	private Label iCoursesTip;
	
	private TabPanel iCourseDetailsTabPanel;
	
	private HTML iCourseDetails;
	private ScrollPanel iCourseDetailsPanel, iClassesPanel, iCurriculaPanel;
	
	private final CurriculaServiceAsync iSectioningService = GWT.create(CurriculaService.class);
	
	private AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> iCourseOfferingsCallback;
	
	private AsyncCallback<String> iCourseDetailsCallback;
	private AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> iCourseClassesCallback;
	private AsyncCallback<TreeSet<CurriculumInterface>> iCourseCurriculaCallback;

	private ArrayList<CourseSelectionChangeHandler> iCourseSelectionChangeHandlers = new ArrayList<CourseSelectionChangeHandler>();
	
	private ArrayList<Validator> iValitaros = new ArrayList<Validator>();
	
	private String iHint = "";
	
	private String iLastCourseLookup = null;
	
	private static int sLastSelectedCourseDetailsTab = 0;
	
	private List<CurriculumInterface.AcademicClassificationInterface> iClassifications;
		
	public CurriculaCourseSelectionBox(String name, List<CurriculumInterface.AcademicClassificationInterface> classifications) {
		
		iClassifications = classifications;
		
		SuggestOracle courseOfferingOracle = new SuggestOracle() {
			public void requestSuggestions(Request request, Callback callback) {
				if (request.getQuery().equals(iHint)) return;
				iSectioningService.listCourseOfferings(request.getQuery(), request.getLimit(), new SuggestCallback(request, callback));
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
							new WebTable.Cell("Limit", 1, "50"),
							new WebTable.Cell("Projected", 1, "50"),
							new WebTable.Cell("Enrollment", 1, "50"),
							new WebTable.Cell("Last-Like", 1, "50")
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
			
			iCurricula = new WebTable();
			ArrayList<WebTable.Cell> header = new ArrayList<WebTable.Cell>();
			header.add(new WebTable.Cell("Curriculum", 1, "100"));
			header.add(new WebTable.Cell("Area", 1, "100"));
			header.add(new WebTable.Cell("Major(s)", 1, "100"));
			for (AcademicClassificationInterface clasf: iClassifications) {
				header.add(new WebTable.Cell(clasf.getCode(), 1, "75"));
			}
			for (int c = 3; c < header.size(); c++) {
				header.get(c).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			}
			iCurricula.setHeader(new WebTable.Row(header));
			iCurricula.setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
			VerticalPanel vp = new VerticalPanel();
			vp.add(iCurricula);
			Label h = new Label("Expected / Enrolled / Last-Like Students");
			h.setStyleName("unitime-Hint");
			vp.add(h);
			vp.setCellHorizontalAlignment(h, HasHorizontalAlignment.ALIGN_RIGHT);
			iCurriculaPanel = new ScrollPanel(vp);
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
								if ("true".equals(r.getId()))
									iTextField.setText(MESSAGES.courseName(r.getCell(0).getValue(), r.getCell(1).getValue()));
								else
									iTextField.setText(MESSAGES.courseNameWithTitle(r.getCell(0).getValue(), r.getCell(1).getValue(), r.getCell(2).getValue()));
								for (CourseSelectionChangeHandler h : iCourseSelectionChangeHandlers)
									h.onChange(iTextField.getText(), true);
							} else {
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
								record.getProjectedString(),
								record.getEnrollmentString(),
								record.getLastLikeString());
						if (!hasEnrl && !record.getEnrollmentString().isEmpty()) hasEnrl = true;
						if (!hasProj && !record.getProjectedString().isEmpty()) hasProj = true;
						if (!hasLastLike && !record.getLastLikeString().isEmpty()) hasLastLike = true;
						records[idx].setId(record.hasUniqueName() ? "true" : "false");
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
			iSectioningService.listCourseOfferings(iFilter.getText(), null, iCourseOfferingsCallback);
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
		if (iCourseCurriculaCallback == null) {
			iCourseCurriculaCallback = new AsyncCallback<TreeSet<CurriculumInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
					iCurricula.setEmptyMessage(caught.getMessage());
				}
				@Override
				public void onSuccess(TreeSet<CurriculumInterface> result) {
					if (!result.isEmpty()) {
						WebTable.Row[] rows = new WebTable.Row[1 + result.size()];
						int idx = 0;
						CurriculumInterface other = null;
						boolean[] used = new boolean[iClassifications.size()];
						for (int i = 0; i < used.length; i++)
							used[i] = false;
						int[][] total = new int[iClassifications.size()][];
						for (int i = 0; i <total.length; i++)
							total[i] = new int[] {0, 0, 0};
						for (CurriculumInterface curriculum: result) {
							if (curriculum.getId() == null) { other = curriculum; continue; }
							ArrayList<WebTable.Cell> row = new ArrayList<WebTable.Cell>();
							row.add(new WebTable.Cell(curriculum.getAbbv()));
							row.add(new WebTable.Cell(curriculum.getAcademicArea().getAbbv()));
							row.add(new WebTable.Cell(curriculum.getMajorCodes(", ")));
							CourseInterface course = curriculum.getCourses().first();
							int col = 0;
							for (AcademicClassificationInterface clasf: iClassifications) {
								CurriculumClassificationInterface f = null;
								for (CurriculumClassificationInterface x: curriculum.getClassifications()) {
									if (x.getAcademicClassification().getId().equals(clasf.getId())) { f = x; break; }
								}
								CurriculumCourseInterface cx = course.getCurriculumCourse(col);
								String s = "";
								if (cx != null) {
									used[col] = true;
									int exp = (f == null || f.getExpected() == null ? 0 : Math.round(f.getExpected() * cx.getShare()));
									int last = (cx.getLastLike() == null ? 0 : cx.getLastLike());
									int enrl = (cx.getEnrollment() == null ? 0 : cx.getEnrollment());
									s = (exp > 0 ? exp : "-") + " / " + (enrl > 0 ? enrl : "-") + " / " + (last > 0 ? last : "-");
									total[col][0] += exp;
									total[col][1] += last;
									total[col][2] += enrl;
								}
								row.add(new WebTable.WidgetCell(new Label(s, false), s));
								col++;
							}
							for (int c = 3; c < row.size(); c++) {
								row.get(c).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
							}
							rows[idx++] = new WebTable.Row(row);
						}
						if (other != null && other.hasCourses()) {
							ArrayList<WebTable.Cell> row = new ArrayList<WebTable.Cell>();
							row.add(new WebTable.Cell("<i>Other</i>"));
							row.add(new WebTable.Cell("-"));
							row.add(new WebTable.Cell("-"));
							CourseInterface course = other.getCourses().first();
							int col = 0;
							for (AcademicClassificationInterface clasf: iClassifications) {
								CurriculumCourseInterface cx = course.getCurriculumCourse(col);
								String s = "";
								if (cx != null) {
									used[col] = true;
									int exp = 0;
									int last = (cx.getLastLike() == null ? 0 : cx.getLastLike());
									int enrl = (cx.getEnrollment() == null ? 0 : cx.getEnrollment());
									total[col][0] += exp;
									total[col][1] += last;
									total[col][2] += enrl;
									s = (exp > 0 ? exp : "-") + " / " + (enrl > 0 ? enrl : "-") + " / " + (last > 0 ? last : "-");
								}
								row.add(new WebTable.WidgetCell(new Label(s, false), s));
								col++;
							}
							for (int c = 3; c < row.size(); c++) {
								row.get(c).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
							}
							rows[idx++] = new WebTable.Row(row);
						}
						ArrayList<WebTable.Cell> row = new ArrayList<WebTable.Cell>();
						row.add(new WebTable.Cell("<b>Total</b>"));
						row.add(new WebTable.Cell("&nbsp;"));
						int[] tx = new int[] {0, 0, 0};
						for (int i = 0; i < total.length; i ++)
							for (int j = 0; j < 3; j++)
								tx[j] += total[i][j];
						row.add(new WebTable.Cell((tx[0] > 0 ? tx[0] : "-") + " / " + (tx[2] > 0 ? tx[2] : "-") + " / " + (tx[1] > 0 ? tx[1] : "-")));
						int col = 0;
						for (AcademicClassificationInterface clasf: iClassifications) {
							int exp = total[col][0];
							int last = total[col][1];
							int enrl = total[col][2];
							total[col][0] += exp;
							total[col][1] += last;
							total[col][2] += enrl;
							String s = (exp > 0 ? exp : "-") + " / " + (enrl > 0 ? enrl : "-") + " / " + (last > 0 ? last : "-");
							row.add(new WebTable.WidgetCell(new Label(s, false), s));
							col++;
						}
						for (int c = 0; c < row.size(); c++) {
							row.get(c).setStyleName("unitime-ClassRowFirst");
							if (c >= 3) row.get(c).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
						}
						rows[idx++] = new WebTable.Row(row);
						iCurricula.setData(rows);
						for (int i = 0; i < used.length; i++)
							iCurricula.setColumnVisible(3 + i, used[i]);
					} else {
						iCurricula.setEmptyMessage("The selected course has no curricula.");
					}
				}
			};			
		}
		if (iCourses.getSelectedRow()<0) {
			iCourseDetails.setHTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionNoCourseSelected() + "</td></tr></table>");
			iClasses.setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
			iClasses.clearData(true);
			iCurricula.setEmptyMessage(MESSAGES.courseSelectionNoCourseSelected());
			iCurricula.clearData(true);
		} else {
			WebTable.Row row = iCourses.getRows()[iCourses.getSelectedRow()];
			String courseName = MESSAGES.courseName(row.getCell(0).getValue(), row.getCell(1).getValue());
			if ("false".equals(row.getId()))
				courseName = MESSAGES.courseNameWithTitle(row.getCell(0).getValue(), row.getCell(1).getValue(), row.getCell(2).getValue());
			if (courseName.equals(iLastCourseLookup)) return;
			iCourseDetails.setHTML("<table width='100%'></tr><td class='unitime-TableEmpty'>" + MESSAGES.courseSelectionLoadingDetails() + "</td></tr></table>");
			iCourseDetailsPanel.setVisible(true);
			iClasses.clearData(true);
			iClasses.setEmptyMessage(MESSAGES.courseSelectionLoadingClasses());
			iSectioningService.retrieveCourseDetails(courseName, iCourseDetailsCallback);
			iSectioningService.listClasses(courseName, iCourseClassesCallback);
			iCurricula.clearData(true);
			iCurricula.setEmptyMessage("Loading curricula ...");
			iSectioningService.findCurriculaForACourse(courseName, iCourseCurriculaCallback);
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
			suggestions.add(new SimpleSuggestion("<font color='red'>"+caught.getMessage()+"</font>", ""));
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
}
