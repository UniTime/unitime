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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDetails;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionMatcher;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorCourseRequestSubmission;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Tomas Muller
 */
public class AdvisorCourseRequestsPage extends SimpleForm implements TakesValue<CourseRequestInterface> {
	private static final SectioningServiceAsync sSectioningService = GWT.create(SectioningService.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	private UniTimeHeaderPanel header, footer;
	private Lookup iLookupDialog = null;
	private Label iStudentName, iStudentExternalId, iTerm;
	private Label iAdvisorEmail = null;
	private AdvisorAcademicSessionSelector iSession = null;
	private SpecialRegistrationContext iSpecRegCx = null;
	private Label iTotalCredit = null;
	private ListBox iStatus = null;
	
	private ArrayList<AdvisorCourseRequestLine> iCourses;
	private ArrayList<AdvisorCourseRequestLine> iAlternatives;
	private AdvisingStudentDetails iDetails;

	private DegreePlansSelectionDialog iDegreePlansSelectionDialog = null;
	private DegreePlanDialog iDegreePlanDialog = null;
	private AriaMultiButton iDegreePlan = null;
	
	private ScheduleStatus iStatusBox = null;
	
	public AdvisorCourseRequestsPage() {
		super(6);
		UniTimePageHeader.getInstance().getLeft().setVisible(false);
		UniTimePageHeader.getInstance().getLeft().setPreventDefault(true);
		addStyleName("unitime-AdvisorCourseRequests");

		header = new UniTimeHeaderPanel();
		header.addStyleName("unitime-PageHeaderFooter");
		header.addButton("lookup", MESSAGES.buttonLookupStudent(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isPageChanged()) {
					UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
						@Override
						public void execute() {
							lookupStudent();
						}
					});
				} else {
					lookupStudent();
				}
			}
		});
		header.addButton("print", MESSAGES.buttonPrint(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		header.setEnabled("print", false);
		header.addButton("submit", MESSAGES.buttonSubmitPrint(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		header.setEnabled("submit", false);
		
		addHeaderRow(header);
		
		iStudentName = new Label(); iStudentName.addStyleName("student-name");
		iStudentExternalId = new Label(); iStudentExternalId.addStyleName("student-id");
		
		addDoubleRow(MESSAGES.propStudentName(), iStudentName, 1,
				MESSAGES.propStudentExternalId(), iStudentExternalId, 3);
		
		iSession = new AdvisorAcademicSessionSelector();
		
		iAdvisorEmail = new Label(); iAdvisorEmail.addStyleName("advisor-email");
		
		iTerm = new Label(); iTerm.addStyleName("term");
		addDoubleRow(MESSAGES.propAdvisorEmail(), iAdvisorEmail, 1,
				MESSAGES.propAcademicSession(), iTerm, 3);
		iSession.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				iTerm.setText(iSession.getAcademicSessionName() == null ? "" : iSession.getAcademicSessionName());
				iLookupDialog.setOptions("mustHaveExternalId,source=students,session=" + event.getNewAcademicSessionId());
				header.setEnabled("submit", false);
				header.setEnabled("print", false);
				iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
				iStatusBox.clear();
				LoadingWidget.getInstance().show(MESSAGES.loadingAdvisorRequests(iStudentName.getText()));
				sSectioningService.getStudentAdvisingDetails(iSession.getAcademicSessionId(), iStudentExternalId.getText(), new AsyncCallback<AdvisingStudentDetails>() {
					@Override
					public void onSuccess(AdvisingStudentDetails result) {
						LoadingWidget.getInstance().hide();
						iDetails = result;
						header.setEnabled("submit", result.isCanUpdate());
						header.setEnabled("print", !result.isCanUpdate());
						iDegreePlan.setVisible(result.isDegreePlan()); iDegreePlan.setEnabled(result.isDegreePlan());
						iAdvisorEmail.setText(result.getAdvisorEmail() == null ? "" : result.getAdvisorEmail());
						iStudentName.setText(result.getStudentName());
						iStudentExternalId.setText(result.getStudentExternalId());
						iStatus.clear();
						if (result.getStatus() != null) {
							iStatus.addItem(result.getStatus().getLabel(), result.getStatus().getReference());
						} else {
							iStatus.addItem("", "");
						}
						iStatus.setSelectedIndex(0);
						if (result.hasStatuses())
							for (StudentStatusInfo status: result.getStatuses()) {
								if (!status.equals(result.getStatus()))
									iStatus.addItem(status.getLabel(), status.getReference());
							}
						setRequest(result.getRequest());
					}
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iStatusBox.error(MESSAGES.advisorRequestsLoadFailed(caught.getMessage()), caught);
					}
				});
			}
		});
		iTerm.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iSession.selectSession();
			}
		});
		iStudentName.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isPageChanged()) {
					UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
						@Override
						public void execute() {
							lookupStudent();
						}
					});
				} else {
					lookupStudent();
				}
			}
		});
		iStudentExternalId.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isPageChanged()) {
					UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
						@Override
						public void execute() {
							lookupStudent();
						}
					});
				} else {
					lookupStudent();
				}
			}
		});
		
		iStatus = new ListBox();
		iStatus.addStyleName("status");
		addDoubleRow("", new Label(), 1,
				MESSAGES.propStudentStatus(), iStatus, 3);
		
		iSpecRegCx = new SpecialRegistrationContext();
		
		iCourses = new ArrayList<AdvisorCourseRequestLine>();
		iAlternatives = new ArrayList<AdvisorCourseRequestLine>();
		
		UniTimeHeaderPanel requests = new UniTimeHeaderPanel(MESSAGES.courseRequestsCourses());
		requests.setMessage(MESSAGES.headCreditHoursNotes());
		requests.addStyleName("requests-header");
		addHeaderRow(requests);
		
		for (int i = 0; i < 9; i++) {
			final AdvisorCourseRequestLine line = new AdvisorCourseRequestLine(iSession, i, false, null, iSpecRegCx);
			line.insert(this, getRowCount());
			iCourses.add(line);
			if (i > 0) {
				AdvisorCourseRequestLine prev = iCourses.get(i - 1);
				line.setPrevious(prev); prev.setNext(line);
			}
			line.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Request>() {
				@Override
				public void onValueChange(ValueChangeEvent<Request> event) {
					if (event.getValue() != null && iCourses.indexOf(line) + 1 == iCourses.size())
						addCourseLine();
					updateTotalCredits();
				}
			});
		}
		
		Label l = new Label(MESSAGES.labelTotalPriorityCreditHours()); l.addStyleName("total-credit-label");
		int row = getRowCount();
		setWidget(row, 0, l);
		getFlexCellFormatter().setColSpan(row, 0, 2);
		iTotalCredit = new Label(MESSAGES.credit(0f)); iTotalCredit.addStyleName("total-credit-value");
		setWidget(row, 1, iTotalCredit);
		
		UniTimeHeaderPanel alternatives = new UniTimeHeaderPanel(MESSAGES.courseRequestsAlternatives());
		alternatives.setMessage(MESSAGES.courseRequestsAlternativesNote());
		addHeaderRow(alternatives);
		for (int i = 0; i < 2; i++) {
			final AdvisorCourseRequestLine line = new AdvisorCourseRequestLine(iSession, i, true, null, iSpecRegCx);
			line.insert(this, getRowCount());
			iAlternatives.add(line);
			if (i == 0) {
				AdvisorCourseRequestLine prev = iCourses.get(iCourses.size() - 1);
				line.setPrevious(prev); prev.setNext(line);
			} else {
				AdvisorCourseRequestLine prev = iAlternatives.get(i - 1);
				line.setPrevious(prev); prev.setNext(line);
			}
			line.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Request>() {
				@Override
				public void onValueChange(ValueChangeEvent<Request> event) {
					if (event.getValue() != null && iAlternatives.indexOf(line) + 1 == iAlternatives.size())
						addAlternativeLine();
				}
			});
		}
		
		iCourses.get(1).getCourses().get(0).setHint(MESSAGES.courseRequestsHint1());
		iCourses.get(3).getCourses().get(0).setHint(MESSAGES.courseRequestsHint3());
		iCourses.get(4).getCourses().get(0).setHint(MESSAGES.courseRequestsHint4());
		iCourses.get(iCourses.size()-1).getCourses().get(0).setHint(MESSAGES.courseRequestsHint8());
		iAlternatives.get(0).getCourses().get(0).setHint(MESSAGES.courseRequestsHintA0());
		
		footer = header.clonePanel();
		footer.addStyleName("unitime-PageHeaderFooter");
		addBottomRow(footer);
		
		iStatusBox = new ScheduleStatus();
		addRow(iStatusBox);
		
		iDegreePlan = new AriaMultiButton(MESSAGES.buttonDegreePlan());
		iDegreePlan.setTitle(MESSAGES.hintDegreePlan());
		iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
		header.insertLeft(iDegreePlan, true);
		footer.insertLeft(iDegreePlan.createClone(), true);
		
		
		Window.addWindowClosingHandler(new Window.ClosingHandler() {
			@Override
			public void onWindowClosing(ClosingEvent event) {
				if (isPageChanged()) {
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().hide();
					event.setMessage(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave());
				}
			}
		});
		
		iLookupDialog = new Lookup();
		iLookupDialog.setText(MESSAGES.dialogStudentLookup());
		iLookupDialog.setOptions("mustHaveExternalId,source=students");
		iLookupDialog.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				studentSelected(event.getValue());
			}
		});

		if (Location.getParameter("student") != null) {
			iStudentExternalId.setText(Location.getParameter("student"));
			iSession.selectSession(new AcademicSessionMatcher() {
				protected boolean matchCampus(AcademicSessionInfo info, String campus) {
					if (info.hasExternalCampus() && campus.equalsIgnoreCase(info.getExternalCampus())) return true;
					return campus.equalsIgnoreCase(info.getCampus());
				}

				protected boolean matchTerm(AcademicSessionInfo info, String term) {
					if (info.hasExternalTerm() && term.equalsIgnoreCase(info.getExternalTerm())) return true;
					return term.equalsIgnoreCase(info.getTerm() + info.getYear()) || term.equalsIgnoreCase(info.getYear() + info.getTerm()) || term.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getCampus());
				}

				protected boolean matchSession(AcademicSessionInfo info, String session) {
					if (info.hasExternalTerm() && info.hasExternalCampus() && session.equalsIgnoreCase(info.getExternalTerm() + info.hasExternalCampus())) return true;
					return session.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getCampus()) || session.equalsIgnoreCase(info.getTerm() + info.getYear()) || session.equals(info.getSessionId().toString());
				}

				@Override
				public boolean match(AcademicSessionInfo info) {
					String campus = Location.getParameter("campus");
					if (campus != null && !matchCampus(info, campus)) return false;
					String term = Location.getParameter("term");
					if (term != null && !matchTerm(info, term)) return false;
					String session = Location.getParameter("session");
					if (session != null && !matchSession(info, session)) return false;
					return true;
				}
			}, new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(Boolean result) {}
			});
		} else {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					lookupStudent();
				}
			});
		}
		
		iDegreePlan.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitListDegreePlans());
				sSectioningService.listDegreePlans(true, iSession.getAcademicSessionId(), iDetails.getStudentId(), new AsyncCallback<List<DegreePlanInterface>>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						if (caught instanceof SectioningException) {
							SectioningException s = (SectioningException)caught;
							if (s.isInfo())
								iStatusBox.info(s.getMessage());
							else if (s.isWarning())
								iStatusBox.warning(s.getMessage());
							else if (s.isError())
								iStatusBox.error(s.getMessage());
							else
								iStatusBox.error(MESSAGES.failedListDegreePlans(s.getMessage()), s);
						} else {
							iStatusBox.error(MESSAGES.failedListDegreePlans(caught.getMessage()), caught);
						}
					}
					@Override
					public void onSuccess(List<DegreePlanInterface> result) {
						LoadingWidget.getInstance().hide();
						if (result == null || result.isEmpty()) {
							iStatusBox.info(MESSAGES.failedNoDegreePlans());
						} else {
							CourseFinderDetails details = new CourseFinderDetails();
							details.setDataProvider(new DataProvider<CourseAssignment, String>() {
								@Override
								public void getData(CourseAssignment source, AsyncCallback<String> callback) {
									sSectioningService.retrieveCourseDetails(iSession.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
								}
							});
							CourseFinderClasses classes = new CourseFinderClasses(false, iSpecRegCx);
							classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
								@Override
								public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
									sSectioningService.listClasses(true, iSession.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
								}
							});
							if (iDegreePlanDialog == null) {
								iDegreePlanDialog = new DegreePlanDialog(StudentSectioningPage.Mode.REQUESTS, AdvisorCourseRequestsPage.this, null, details, classes) {
									protected void doBack() {
										super.doBack();
										iDegreePlansSelectionDialog.show();
									}
								};
							}
							if (iDegreePlansSelectionDialog == null) {
								iDegreePlansSelectionDialog = new DegreePlansSelectionDialog() {
									public void doSubmit(DegreePlanInterface plan) {
										super.doSubmit(plan);
										iDegreePlanDialog.open(plan, true);
									}
								};
							}
							if (result.size() == 1)
								iDegreePlanDialog.open(result.get(0), false);
							else
								iDegreePlansSelectionDialog.open(result);
						}
					}
				});
				
			}
		});
	}
	
	private void updateTotalCredits() {
		float min = 0, max = 0;
		for (AdvisorCourseRequestLine line: iCourses) {
			min += line.getCreditMin();
			max += line.getCreditMax();
		}
		if (min < max)
			iTotalCredit.setText(MESSAGES.creditRange(min, max));
		else
			iTotalCredit.setText(MESSAGES.credit(min));
	}
	
	private void clearRequests() {
		for (AdvisorCourseRequestLine line: iCourses)
			line.setValue(null);
		for (AdvisorCourseRequestLine line: iAlternatives)
			line.setValue(null);
		iStatusBox.clear();
		updateTotalCredits();
	}
	
	private void addCourseLine() {
		int i = iCourses.size();
		final AdvisorCourseRequestLine line = new AdvisorCourseRequestLine(iSession, i, false, null, iSpecRegCx);
		iCourses.add(line);
		AdvisorCourseRequestLine prev = iCourses.get(i - 1);
		prev.getCourses().get(0).setHint("");
		line.getCourses().get(0).setHint(MESSAGES.courseRequestsHint8());
		AdvisorCourseRequestLine next = (iAlternatives.isEmpty() ? null : iAlternatives.get(0));
		line.setPrevious(prev); prev.setNext(line);
		if (next != null) {
			line.setNext(next); next.setPrevious(line);
		}
		line.insert(this, insertRow(4 + iCourses.size()));
		line.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Request>() {
			@Override
			public void onValueChange(ValueChangeEvent<Request> event) {
				if (event.getValue() != null && iCourses.indexOf(line) + 1 == iCourses.size())
					addCourseLine();
				updateTotalCredits();
			}
		});
	}
	
	private void addAlternativeLine() {
		int i = iAlternatives.size();
		final AdvisorCourseRequestLine line = new AdvisorCourseRequestLine(iSession, i, true, null, iSpecRegCx);
		iAlternatives.add(line);
		AdvisorCourseRequestLine prev = (i == 0 ? iCourses.get(iCourses.size() - 1) : iAlternatives.get(i - 1));
		if (prev != null) {
			line.setPrevious(prev); prev.setNext(line);
		}
		line.insert(this, insertRow(4 + iCourses.size() + 2 + iAlternatives.size()));
		line.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Request>() {
			@Override
			public void onValueChange(ValueChangeEvent<Request> event) {
				if (event.getValue() != null && iAlternatives.indexOf(line) + 1 == iAlternatives.size())
					addAlternativeLine();
			}
		});
	}
	
	protected boolean isPageChanged() {
		if (iDetails == null || iDetails.getRequest() == null)
			return !getRequest().isEmpty();
		else {
			return !iDetails.getRequest().equals(getRequest());
		}
	}
	
	protected void lookupStudent() {
		iLookupDialog.center();
	}
	
	protected void studentSelected(PersonInterface person) {
		if (person == null || person.getId() == null || person.getId().isEmpty()) {
			iStudentName.setText("");
			iStudentExternalId.setText("");
			iTerm.setText("");
			header.setEnabled("submit", false);
			header.setEnabled("print", false);
			iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
			clearRequests();
		} else {
			iStudentName.setText(person.getName());
			iStudentExternalId.setText(person.getId());
			clearRequests();
			iSession.selectSessionNoCheck();
		}
	}
	
	private class AdvisorAcademicSessionSelector extends AcademicSessionSelector {
		public AdvisorAcademicSessionSelector() {
			super(UniTimePageHeader.getInstance().getRight(), StudentSectioningPage.Mode.REQUESTS);
		}

		public void selectSession() {
			if (isPageChanged()) {
				UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
					@Override
					public void execute() {
						selectSessionNoCheck();
					}
				});
			} else {
				selectSessionNoCheck();
			}
		}
		
		@Override
		protected void listAcademicSessions(AsyncCallback<Collection<AcademicSessionInfo>> callback) {
			sSectioningService.getStudentSessions(iStudentExternalId.getText(), callback);
		}
		
		public void selectSessionNoCheck() {
			header.setEnabled("submit", false);
			header.setEnabled("print", false);
			iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
			super.selectSession();
		}		
	}
	
	public void fillInCourses(CourseRequestInterface cr) {
		for (AdvisorCourseRequestLine line: iCourses) {
			CourseRequestInterface.Request req = line.getValue();
			if (req != null) cr.getCourses().add(req);
		}
	}
	
	public void fillInAlternatives(CourseRequestInterface cr) {
		for (AdvisorCourseRequestLine line: iAlternatives) {
			CourseRequestInterface.Request req = line.getValue();
			if (req != null) cr.getAlternatives().add(req);
		}
	}
	
	public CourseRequestInterface getRequest() {
		CourseRequestInterface cr = new CourseRequestInterface();
		cr.setAcademicSessionId(iSession.getAcademicSessionId());
		fillInCourses(cr);
		fillInAlternatives(cr);
		return cr;
	}
	
	public void setRequest(CourseRequestInterface request) {
		clearRequests();
		if (request != null) {
			while (iCourses.size() < request.getCourses().size()) addCourseLine();
			for (int idx = 0; idx < request.getCourses().size(); idx++)
				iCourses.get(idx).setValue(request.getCourses().get(idx), true);
			while (iAlternatives.size() < request.getAlternatives().size()) addAlternativeLine();;
			for (int idx = 0; idx < request.getAlternatives().size(); idx++)
				iAlternatives.get(idx).setValue(request.getAlternatives().get(idx), true);
		}
		updateTotalCredits();
	}
	
	public static final native String download(byte[] bytes, String name) /*-{
		var data = new Uint8Array(bytes);
		var blob = new Blob([data], {type: "application/pdf"});
		var link = $doc.createElement("a");
		link.href = $wnd.URL.createObjectURL(blob);
		link.download = name + ".pdf";
		link.click();
	}-*/;
	
	protected void submit() {
		final AdvisingStudentDetails details = new AdvisingStudentDetails(iDetails);
		details.setRequest(getRequest());
		details.setStatus(iDetails.getStatus(iStatus.getSelectedValue()));
		LoadingWidget.getInstance().show(MESSAGES.advisorCourseRequestsSaving());
		sSectioningService.submitAdvisingDetails(details, new AsyncCallback<AdvisorCourseRequestSubmission>() {
			@Override
			public void onSuccess(AdvisorCourseRequestSubmission result) {
				LoadingWidget.getInstance().hide();
				iDetails = details;
				download(result.getPdf(), "crf-" + iStudentExternalId.getText());
				if (result.isUpdated())
					iStatusBox.info(MESSAGES.advisorRequestsSubmitOK());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iStatusBox.error(MESSAGES.advisorRequestsSubmitFailed(caught.getMessage()), caught);
			}
		});
	}

	@Override
	public void setValue(CourseRequestInterface value) {
		setRequest(value);
	}

	@Override
	public CourseRequestInterface getValue() {
		return getRequest();
	}
}
