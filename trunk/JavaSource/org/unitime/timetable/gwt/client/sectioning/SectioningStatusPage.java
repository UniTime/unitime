/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.HorizontalPanelWithHint;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.EnrollmentInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

public class SectioningStatusPage extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);

	private TextBox iFilter = null;
	private Button iSearch = null;
	private Image iLoadingImage = null;
	
	private SuggestBox iFilterSuggest = null;

	private VerticalPanel iSectioningPanel = null;
	
	private VerticalPanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	
	private UniTimeTable<EnrollmentInfo> iCourseTable = null;
	
	private UniTimeDialogBox iEnrollmentDialog = null;
	private EnrollmentTable iEnrollmentTable = null;
	private ScrollPanel iEnrollmentScroll = null;
	
	private HTML iHint = null, iError = null;
	private String iLastFilter = null;

	public SectioningStatusPage() {
		iPanel = new VerticalPanel();
		
		iSectioningPanel = new VerticalPanel();
		
		iFilterPanel = new HorizontalPanelWithHint(new HTML(MESSAGES.sectioningStatusFilterHint(), false));
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label(MESSAGES.filter());
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		SuggestOracle courseOfferingOracle = new SuggestOracle() {
			@Override
			public void requestDefaultSuggestions(Request request, Callback callback) {
				requestSuggestions(request, callback);
			}
			
			@Override
			public void requestSuggestions(Request request, Callback callback) {
				iSectioningService.querySuggestions(request.getQuery(), request.getLimit(), new SuggestCallback(request, callback));
			}
			
			@Override
			public boolean isDisplayStringHTML() { return true; }			
		};

		iFilter = new UniTimeTextBox();
		iFilter.setWidth("400px");
		iFilterSuggest = new SuggestBox(courseOfferingOracle, iFilter);
		iFilterPanel.add(iFilterSuggest);
		
		iFilter.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				if (iFilter.getText().isEmpty())
					iFilterSuggest.showSuggestionList();
			}
		});
		
		iSearch = new Button(MESSAGES.buttonSearch());
		iSearch.setAccessKey('s');
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);		
		iFilterPanel.setCellVerticalAlignment(iSearch, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.setVisible(false);
		iFilterPanel.add(iLoadingImage);
		iFilterPanel.setCellVerticalAlignment(iLoadingImage, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iSectioningPanel.add(iFilterPanel);
		iSectioningPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iCourseTable = new UniTimeTable<ClassAssignmentInterface.EnrollmentInfo>();
		
		iSectioningPanel.add(iCourseTable);
		
		iSectioningPanel.setWidth("100%");
		
		iPanel.add(iSectioningPanel);
		
		iHint = new HTML(MESSAGES.sectioningStatusHint());
		iHint.setStyleName("unitime-Hint");
		iHint.setVisible(false);
		iPanel.add(iHint);
		iPanel.setCellHorizontalAlignment(iHint, HasHorizontalAlignment.ALIGN_RIGHT);
		
		iError = new HTML();
		iError.setStyleName("unitime-ErrorMessage");
		iError.setVisible(false);
		iPanel.add(iError);
		iPanel.setCellHorizontalAlignment(iError, HasHorizontalAlignment.ALIGN_CENTER);
		
		initWidget(iPanel);
		
		iSearch.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loadCourses();
			}
		});
		
		iFilter.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					if (iFilter.getText().equals(iLastFilter))
						loadCourses();
					else
						iLastFilter = iFilter.getText();
				}
			}
		});
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iFilter.setFocus(true);
			}
		});

		if (Window.Location.getParameter("q") != null) {
			iFilter.setText(Window.Location.getParameter("q"));
			loadCourses();
		} else if (Window.Location.getHash() != null && !Window.Location.getHash().isEmpty()) {
			String hash = URL.decode(Window.Location.getHash().substring(1));
			if (!hash.matches("^[0-9]+\\:[0-9]+$")) {
				iFilter.setText(hash);
				loadCourses();
			}
		} else {
			// retrieve last query
		}
		
		iCourseTable.addMouseClickListener(new MouseClickListener<ClassAssignmentInterface.EnrollmentInfo>() {
			@Override
			public void onMouseClick(final TableEvent<EnrollmentInfo> event) {
				if (event.getData() == null || event.getData().getCourseId() == null) return; // header or footer
				iCourseTable.clearHover();
				setLoading(true);
				final Long id = (event.getData().getConfigId() == null ? event.getData().getOfferingId() : -event.getData().getClazzId());
				iError.setVisible(false);
				if (event.getData().getConfigId() == null)
					LoadingWidget.getInstance().show(MESSAGES.loadingEnrollments(MESSAGES.course(event.getData().getSubject(), event.getData().getCourseNbr())));
				else
					LoadingWidget.getInstance().show(MESSAGES.loadingEnrollments(MESSAGES.clazz(event.getData().getSubject(), event.getData().getCourseNbr(), event.getData().getSubpart(), event.getData().getClazz())));
				iSectioningService.canApprove(id, new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(final Boolean canApprove) {
						iSectioningService.findEnrollments(iFilter.getText(), event.getData().getCourseId(), event.getData().getClazzId(), new AsyncCallback<List<Enrollment>>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().fail(caught.getMessage());
								setLoading(false);
								iError.setHTML(caught.getMessage());
								iError.setVisible(true);
								ToolBox.checkAccess(caught);
							}
							@Override
							public void onSuccess(List<Enrollment> result) {
								LoadingWidget.getInstance().hide();
								setLoading(false);
								iEnrollmentTable.clear(false);
								iEnrollmentTable.setId(id);
								iEnrollmentTable.populate(result, canApprove);
								if (event.getData().getConfigId() == null)
									iEnrollmentDialog.setText(MESSAGES.titleEnrollments(MESSAGES.course(event.getData().getSubject(), event.getData().getCourseNbr())));
								else
									iEnrollmentDialog.setText(MESSAGES.titleEnrollments(MESSAGES.clazz(event.getData().getSubject(), event.getData().getCourseNbr(), event.getData().getSubpart(), event.getData().getClazz())));
								iEnrollmentDialog.center();
							}
						});
					}
					
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().fail(caught.getMessage());
						setLoading(false);
						iError.setHTML(caught.getMessage());
						iError.setVisible(true);
						ToolBox.checkAccess(caught);
					}
				});
			}
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iFilter.setText(event.getValue());
				loadCourses();
			}
		});
		
		iEnrollmentTable = new EnrollmentTable(false);
		iEnrollmentScroll = new ScrollPanel(iEnrollmentTable);
		iEnrollmentScroll.setHeight(((int)(0.8 * Window.getClientHeight())) + "px");
		iEnrollmentScroll.setStyleName("unitime-ScrollPanel");
		iEnrollmentDialog = new UniTimeDialogBox(true, true);
		iEnrollmentDialog.setEscapeToHide(true);
		iEnrollmentDialog.setWidget(iEnrollmentScroll);
		iEnrollmentDialog.addOpenHandler(new OpenHandler<UniTimeDialogBox>() {
			@Override
			public void onOpen(OpenEvent<UniTimeDialogBox> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
				iEnrollmentScroll.setHeight(Math.min(iEnrollmentTable.getElement().getScrollHeight(), Window.getClientHeight() * 80 / 100) + "px");
				iEnrollmentDialog.setPopupPosition(
						Math.max(Window.getScrollLeft() + (Window.getClientWidth() - iEnrollmentDialog.getOffsetWidth()) / 2, 0),
						Math.max(Window.getScrollTop() + (Window.getClientHeight() - iEnrollmentDialog.getOffsetHeight()) / 2, 0));
			}
		});
		iEnrollmentDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
	}
	
	private void setLoading(boolean loading) {
		iLoadingImage.setVisible(loading);
		iSearch.setVisible(!loading);
	}
	
	private void loadCourses() {
		iCourseTable.clearTable();
		iHint.setVisible(false);
		History.newItem(iFilter.getText(), false);
		LoadingWidget.getInstance().show(MESSAGES.loadingData());
		setLoading(true);
		iError.setVisible(false);
		iSectioningService.findEnrollmentInfos(iFilter.getText(), null, new AsyncCallback<List<EnrollmentInfo>>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				setLoading(false);
				iError.setHTML(caught.getMessage());
				iError.setVisible(true);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(List<EnrollmentInfo> result) {
				if (result.isEmpty()) {
					iError.setHTML(MESSAGES.exceptionNoCourses(iFilter.getText()));
					iError.setVisible(true);
				} else {
					populateCourseTable(result);
				}
				setLoading(false);
				LoadingWidget.getInstance().hide();
			}
			
		});
	}
	
	private List<Widget> line(final EnrollmentInfo e) {
		List<Widget> line = new ArrayList<Widget>();
		if (e.getConfigId() == null) {
			if (e.getCourseId() != null) {
				final Image showDetails = new Image(RESOURCES.treeClosed());
				showDetails.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						final int row = iCourseTable.getCellForEvent(event).getRowIndex();
						if (row + 1 == iCourseTable.getRowCount() || iCourseTable.getData(row + 1).getConfigId() == null) { // open
							setLoading(true);
							iError.setVisible(false);
							showDetails.setResource(RESOURCES.treeOpen());
							iSectioningService.findEnrollmentInfos(iFilter.getText(), e.getCourseId(), new AsyncCallback<List<EnrollmentInfo>>() {
								@Override
								public void onFailure(Throwable caught) {
									setLoading(false);
									iError.setHTML(caught.getMessage());
									iError.setVisible(true);
									ToolBox.checkAccess(caught);
								}

								@Override
								public void onSuccess(List<EnrollmentInfo> result) {
									setLoading(false);
									int r = row + 1;
									for (EnrollmentInfo e: result) {
										iCourseTable.insertRow(r);
										iCourseTable.setRow(r, e, line(e));
										r++;
									}
								}
							});
						} else {
							for (int r = row + 1; r < iCourseTable.getRowCount(); r++) {
								if (iCourseTable.getData(r).getConfigId() == null) break;
								iCourseTable.getRowFormatter().setVisible(r, !iCourseTable.getRowFormatter().isVisible(r));
							}
							showDetails.setResource(RESOURCES.treeClosed());
						}
						event.getNativeEvent().stopPropagation();
						event.getNativeEvent().preventDefault();
					}
				});
				line.add(showDetails);				
			} else {
				line.add(new Label());
			}
			line.add(new Label(e.getSubject()));
			line.add(new Label(e.getCourseNbr()));
			line.add(new TitleCell(e.getTitle() == null ? "" : e.getTitle()));
			line.add(new Label(e.getConsent() == null ? "" : e.getConsent()));
		} else {
			line.add(new Label());
			line.add(new HTML("&nbsp;&nbsp;" + (e.getSubpart() == null ? "" : e.getSubpart())));
			line.add(new HTML(e.getClazz() == null ? "" : e.getClazz()));
			line.add(new Label(e.getAssignment().getDays().isEmpty()  ? "" : e.getAssignment().getDaysString(CONSTANTS.shortDays()) + " " + e.getAssignment().getStartString(CONSTANTS.useAmPm()) + " - " + e.getAssignment().getEndString(CONSTANTS.useAmPm())));
			line.add(new Label(!e.getAssignment().hasDatePattern()  ? "" : e.getAssignment().getDatePattern()));
			line.add(new Label(!e.getAssignment().hasRoom() ? "" : e.getAssignment().getRooms(",")));
		}
		if (e.getCourseId() == null)
			line.add(new NumberCell(e.getAvailable(), e.getLimit()));
		else
			line.add(new AvailableCell(e));
		line.add(new NumberCell(null, e.getProjection()));
		line.add(new NumberCell(e.getEnrollment(), e.getTotalEnrollment()));
		line.add(new NumberCell(e.getWaitlist(), e.getTotalWaitlist()));
		line.add(new NumberCell(e.getReservation(), e.getTotalReservation()));
		line.add(new NumberCell(e.getConsentApproved() + e.getConsentNeeded(), e.getTotalConsentApproved() + e.getTotalConsentNeeded()));
		return line;
	}
	
	public void populateCourseTable(List<EnrollmentInfo> result) {
		List<Widget> header = new ArrayList<Widget>();

		UniTimeTableHeader hOperations = new UniTimeTableHeader("");
		header.add(hOperations);

		UniTimeTableHeader hSubject = new UniTimeTableHeader(MESSAGES.colSubject() + "<br>" + MESSAGES.colSubpart());
		header.add(hSubject);
		hSubject.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.SUBJECT));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colSubject());
			}
		});
		
		UniTimeTableHeader hCourse = new UniTimeTableHeader(MESSAGES.colCourse() + "<br>" + MESSAGES.colClass());
		header.add(hCourse);
		hCourse.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.COURSE));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colCourse());
			}
		});

		UniTimeTableHeader hTitleSubpart = new UniTimeTableHeader(MESSAGES.colTitle() + "<br>" + MESSAGES.colTime());
		header.add(hTitleSubpart);
		hTitleSubpart.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.TITLE));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colTitle());
			}
		});

		UniTimeTableHeader hStart = new UniTimeTableHeader("<br>" + MESSAGES.colDate());
		header.add(hStart);

		UniTimeTableHeader hRoom = new UniTimeTableHeader(MESSAGES.colConsent() + "<br>" + MESSAGES.colRoom());
		header.add(hRoom);
		hRoom.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.CONSENT));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colConsent());
			}
		});


		UniTimeTableHeader hLimit = new UniTimeTableHeader(MESSAGES.colAvailable());
		header.add(hLimit);
		hLimit.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.LIMIT));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colAvailable());
			}
		});

		UniTimeTableHeader hProjection = new UniTimeTableHeader(MESSAGES.colProjection());
		header.add(hProjection);
		hProjection.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.PROJECTION));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colProjection());
			}
		});

		UniTimeTableHeader hEnrollment = new UniTimeTableHeader(MESSAGES.colEnrollment());
		header.add(hEnrollment);
		hEnrollment.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.ENROLLMENT));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colEnrollment());
			}
		});

		UniTimeTableHeader hWaitListed = new UniTimeTableHeader(MESSAGES.colWaitListed());
		header.add(hWaitListed);
		hWaitListed.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.WAITLIST));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colWaitListed());
			}
		});

		UniTimeTableHeader hReserved = new UniTimeTableHeader(MESSAGES.colReserved());
		header.add(hReserved);
		hReserved.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.RESERVATION));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colReserved());
			}
		});

		UniTimeTableHeader hConsent = new UniTimeTableHeader(MESSAGES.colConsent());
		header.add(hConsent);
		hConsent.addOperation(new Operation() {
			@Override
			public void execute() {
				iCourseTable.sort(new EnrollmentComparator(EnrollmentComparator.SortBy.NBR_CONSENT));
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colConsent());
			}
		});

		iCourseTable.addRow(null, header);
		
		for (EnrollmentInfo e: result) {
			iCourseTable.addRow(e, line(e));
			iCourseTable.getRowFormatter().getElement(iCourseTable.getRowCount() - 1).getStyle().setBackgroundColor("#f3f3f3");
		}
		
		// Total line
		if (iCourseTable.getRowCount() >= 2) {
			for (int c = 0; c < iCourseTable.getCellCount(iCourseTable.getRowCount() - 1); c++)
				iCourseTable.getCellFormatter().setStyleName(iCourseTable.getRowCount() - 1, c, "unitime-TotalRow");
			iCourseTable.getRowFormatter().getElement(iCourseTable.getRowCount() - 1).getStyle().clearBackgroundColor();
		}
		
		iHint.setVisible(true);
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
	
	public class SuggestCallback implements AsyncCallback<List<String[]>> {
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
			ToolBox.checkAccess(caught);
		}

		public void onSuccess(List<String[]> result) {
			ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
			for (String[] suggestion: result) {
				suggestions.add(new SimpleSuggestion(suggestion[1], suggestion[0]));
			}
			iCallback.onSuggestionsReady(iRequest, new Response(suggestions));
		}
		
	}
	
	public static class NumberCell extends HTML implements HasCellAlignment {
		Integer iValue, iTotal;
		
		public NumberCell(Integer value, Integer total) {
			super();
			iValue = value; iTotal = total;
			if (iValue == null) {
				if (iTotal != null)
					setHTML(iTotal == 0 ? "-" : iTotal < 0 ? "&infin;" : iTotal.toString());
			} else {
				if (iValue.equals(iTotal))
					setHTML(iTotal == 0 ? "-" : iTotal < 0 ? "&infin;" : iTotal.toString());
				else
					setHTML((iValue < 0 ? "&infin;" : iValue.toString()) + " / " + (iTotal < 0 ? "&infin;" : iTotal.toString()));
			}
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class TitleCell extends HTML implements HasColSpan {
		
		public TitleCell(String title) {
			super(title);
		}

		@Override
		public int getColSpan() {
			return 2;
		}
	}
	
	public static class AvailableCell extends HTML implements HasCellAlignment {
		Integer iAvailable, iEnrollment, iLimit, iReserved, iOther;
		
		public AvailableCell(EnrollmentInfo e) {
			super();
			iAvailable = e.getAvailable();
			iEnrollment = e.getTotalEnrollment();
			iLimit = e.getLimit();
			iReserved = e.getTotalReservation();
			iOther = (e.getOther() == null ? 0 : e.getOther());
			if (iLimit == null) {
				setHTML("-");
				setTitle(MESSAGES.availableNoLimit());
			} else if (iLimit < 0) {
				if (iAvailable != null && iAvailable == 0) {
					setHTML("&infin;" + MESSAGES.htmlReservationSign());
					setTitle(MESSAGES.availableUnlimitedWithReservation());
				} else {
					setHTML("&infin;");
					setTitle(MESSAGES.availableUnlimited());
				}
			} else {
				if (iAvailable == iLimit - iEnrollment - iOther) {
					setHTML(iAvailable + " / " + iLimit);
					if (iAvailable == 0)
						setTitle(MESSAGES.availableNot(iLimit));
					else
						setTitle(MESSAGES.available(iAvailable, iLimit));
				} else if (iAvailable == 0 && iLimit > iEnrollment + iOther) {
					setHTML((iLimit - iEnrollment - iOther) + MESSAGES.htmlReservationSign() + " / " + iLimit);
					setTitle(MESSAGES.availableWithReservation(iLimit - iEnrollment - iOther, iLimit));
				} else {
					setHTML(iAvailable + " + " + (iLimit - iEnrollment - iAvailable - iOther) + MESSAGES.htmlReservationSign() + " / " + iLimit);
					setTitle(MESSAGES.availableSomeReservation(iAvailable, iLimit, iLimit - iEnrollment - iAvailable - iOther));
				}
			}
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class EnrollmentComparator implements Comparator<EnrollmentInfo> {
		public enum SortBy {
			SUBJECT,
			COURSE,
			TITLE,
			CONSENT,
			LIMIT,
			PROJECTION,
			ENROLLMENT,
			WAITLIST,
			RESERVATION,
			NBR_CONSENT
		}
		
		private SortBy iSortBy;
		
		public EnrollmentComparator(SortBy sortBy) {
			iSortBy = sortBy;
		}

		@Override
		public int compare(EnrollmentInfo e1, EnrollmentInfo e2) {
			// Totals line is always last
			if (e1.getCourseId() == null) return 1;
			if (e2.getCourseId() == null) return -1;
			
			if (e1.getCourseId().equals(e2.getCourseId())) { // Same course
				// Course line first
				if (e1.getConfigId() == null) return -1;
				if (e2.getConfigId() == null) return -1;
				return compareClasses(e1, e2);
			} else { // Different course
				return compareCourses(e1, e2);
			}
		}
		
		private int compareClasses(EnrollmentInfo e1, EnrollmentInfo e2) {
			return 0;
		}
	
		private int compareCourses(EnrollmentInfo e1, EnrollmentInfo e2) {
			int cmp;
			switch (iSortBy) {
			case SUBJECT:
				break;
			case COURSE:
				cmp = e1.getCourseNbr().compareTo(e2.getCourseNbr());
				if (cmp != 0) return cmp;
				break;
			case TITLE:
				cmp = (e1.getTitle() == null ? "" : e1.getTitle()).compareTo(e2.getTitle() == null ? "" : e2.getTitle());
				if (cmp != 0) return cmp;
				break;
			case CONSENT:
				cmp = (e1.getConsent() == null ? "" : e1.getConsent()).compareTo(e2.getConsent() == null ? "" : e2.getConsent());
				if (cmp != 0) return cmp;
				break;
			case LIMIT:
				cmp = (e1.getAvailable() == null ? new Integer(0) : e1.getAvailable() < 0 ? new Integer(Integer.MAX_VALUE) : e1.getAvailable()).compareTo(
						e2.getAvailable() == null ? 0 : e2.getAvailable() < 0 ? Integer.MAX_VALUE : e2.getAvailable());
				if (cmp != 0) return cmp;
				cmp = (e1.getLimit() == null ? new Integer(0) : e1.getLimit()).compareTo(e2.getLimit() == null ? 0 : e2.getLimit());
				if (cmp != 0) return cmp;
				break;
			case PROJECTION:
				cmp = (e1.getProjection() == null ? new Integer(0) : e1.getProjection()).compareTo(e2.getProjection() == null ? 0 : e2.getProjection());
				if (cmp != 0) return - cmp;
				break;
			case ENROLLMENT:
				cmp = (e1.getEnrollment() == null ? new Integer(0) : e1.getEnrollment()).compareTo(e2.getEnrollment() == null ? 0 : e2.getEnrollment());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalEnrollment() == null ? new Integer(0) : e1.getTotalEnrollment()).compareTo(e2.getTotalEnrollment() == null ? 0 : e2.getTotalEnrollment());
				if (cmp != 0) return - cmp;
				break;
			case WAITLIST:
				cmp = (e1.getWaitlist() == null ? new Integer(0) : e1.getWaitlist()).compareTo(e2.getWaitlist() == null ? 0 : e2.getWaitlist());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalWaitlist() == null ? new Integer(0) : e1.getTotalWaitlist()).compareTo(e2.getTotalWaitlist() == null ? 0 : e2.getTotalWaitlist());
				if (cmp != 0) return - cmp;
				break;
			case RESERVATION:
				cmp = (e1.getReservation() == null ? new Integer(0) : e1.getReservation()).compareTo(e2.getReservation() == null ? 0 : e2.getReservation());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalReservation() == null ? new Integer(0) : e1.getTotalReservation()).compareTo(e2.getTotalReservation() == null ? 0 : e2.getTotalReservation());
				if (cmp != 0) return - cmp;
				break;
			case NBR_CONSENT:
				cmp = (e1.getConsentNeeded() == null ? new Integer(0) : new Integer(e1.getConsentNeeded() + e1.getConsentApproved())).compareTo(
						e2.getConsentNeeded() == null ? 0 : e2.getConsentNeeded() + e2.getConsentApproved());
				if (cmp != 0) return - cmp;
				cmp = (e1.getTotalConsentNeeded() == null ? new Integer(0) : new Integer(e1.getTotalConsentNeeded() + e1.getTotalConsentApproved())).compareTo(
						e2.getTotalConsentNeeded() == null ? 0 : e2.getTotalConsentNeeded() + e2.getTotalConsentApproved());
				if (cmp != 0) return - cmp;
				break;
			}
			
			// Default sort
			cmp = e1.getSubject().compareTo(e2.getSubject());
			if (cmp != 0) return cmp;
			
			cmp = e1.getCourseNbr().compareTo(e2.getCourseNbr());
			if (cmp != 0) return cmp;

			return 0;
		}


		
	}
}
