package org.unitime.timetable.gwt.client.instructor;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyWidget;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.ListItem;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorDetailRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorDetailResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;

public class InstructorDetailPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private int iPreferencesRow = -1;
	private InstructorDetailResponse iResponse;
	
	public InstructorDetailPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-InstructorDetailPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("instructorId");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoInstructorId());
		} else {
			load(Long.valueOf(id), null);	
		}
		
		iHeader.addButton("edit", COURSE.actionEditInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instructorInfoEdit.action?instructorId=" + iResponse.getInstructorId());
			}
		});
		iHeader.setEnabled("edit", false);
		iHeader.getButton("edit").setAccessKey(COURSE.accessEditInstructor().charAt(0));
		iHeader.getButton("edit").setTitle(COURSE.titleEditInstructor(COURSE.accessEditInstructor()));
		
		iHeader.addButton("assignment", COURSE.actionEditInstructorAssignmentPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instructorAssignmentPref.action?instructorId=" + iResponse.getInstructorId());
			}
		});
		iHeader.setEnabled("assignment", false);
		iHeader.getButton("assignment").setAccessKey(COURSE.accessEditInstructorAssignmentPreferences().charAt(0));
		iHeader.getButton("assignment").setTitle(COURSE.titleEditInstructorAssignmentPreferences(COURSE.accessEditInstructorAssignmentPreferences()));

		iHeader.addButton("preferences", COURSE.actionEditInstructorPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instructorPrefEdit.action?instructorId=" + iResponse.getInstructorId());
			}
		});
		iHeader.setEnabled("preferences", false);
		iHeader.getButton("preferences").setAccessKey(COURSE.accessEditInstructorPreferences().charAt(0));
		iHeader.getButton("preferences").setTitle(COURSE.titleEditInstructorPreferences(COURSE.accessEditInstructorPreferences()));
		
		iHeader.addButton("survey", COURSE.actionInstructorSurvey(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				UniTimeFrameDialog.openDialog(COURSE.actionInstructorSurvey(),
						"instructorSurvey?menu=hide&id=" + iResponse.getInstructorId(),
						"900", "90%");
			}
		});
		iHeader.setEnabled("survey", false);
		iHeader.getButton("survey").setAccessKey(COURSE.accessInstructorSurvey().charAt(0));
		iHeader.getButton("survey").setTitle(COURSE.titleInstructorSurvey(COURSE.accessInstructorSurvey()));

		iHeader.addButton("previous", COURSE.actionPreviousInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instructor?id=" + iResponse.getPreviousId());
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.getButton("previous").setAccessKey(COURSE.accessPreviousInstructor().charAt(0));
		iHeader.getButton("previous").setTitle(COURSE.titlePreviousInstructor(COURSE.accessPreviousInstructor()));
		
		iHeader.addButton("next", COURSE.actionNextInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instructor?id=" + iResponse.getNextId());
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.getButton("next").setAccessKey(COURSE.accessNextInstructor().charAt(0));
		iHeader.getButton("next").setTitle(COURSE.titleNextInstructor(COURSE.accessNextInstructor()));
		
		iHeader.addButton("back", COURSE.actionBackToInstructors(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "back.action?uri=" + URL.encodeQueryString(iResponse.getBackUrl()) +
						"&backId=" + iResponse.getInstructorId() + "&backType=PreferenceGroup");
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToInstructors().charAt(0));

		iFooter = iHeader.clonePanel();
	}
	
	protected void load(Long instructorId, InstructorDetailRequest.Action action) {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		InstructorDetailRequest req = new InstructorDetailRequest();
		req.setInstructorId(instructorId);
		req.setAction(action);
		req.setBackId(Window.Location.getParameter("backId"));
		req.setBackType(Window.Location.getParameter("backType"));
		RPC.execute(req, new AsyncCallback<InstructorDetailResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final InstructorDetailResponse response) {
				iResponse = response;
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				LoadingWidget.getInstance().hide();
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				iHeader.setHeaderTitle(response.getInstructorName());
				if (response.hasExternalId())
					iPanel.addRow(COURSE.propertyExternalId(), new Label(response.getExternalId()));
				if (response.hasDepartmentFilter()) {
					FilterParameterInterface param = response.getDepartmentFilter();
					final ListBox list = new ListBox();
					list.setMultipleSelect(param.isMultiSelect());
					for (ListItem item: param.getOptions()) {
						list.addItem(item.getText(), item.getValue());
						if (param.isMultiSelect())
							list.setItemSelected(list.getItemCount() - 1, param.isDefaultItem(item));
						else if (param.isDefaultItem(item))
							list.setSelectedIndex(list.getItemCount() - 1);
					}
					list.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							if (list.getSelectedIndex() >= 0)
								ToolBox.open(GWT.getHostPageBaseURL() + "instructor?id=" + list.getValue(list.getSelectedIndex()));
						}
					});
					iPanel.addRow(param.getLabel(), list);
				}
				if (response.hasProperties())
					for (PropertyInterface property: response.getProperties().getProperties())
						iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				
				if (response.hasOperation("has-survey"))
					iPreferencesRow = iPanel.addRow(new InstructorSurveyWidget().forInstructorId(response.getInstructorId()));
				
				if (response.hasClasses()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getClasses().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getClasses()));
				}
				
				if (response.hasExaminations()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getExaminations().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getExaminations()));
				}
				
				if (response.hasEvents()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getEvents().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getEvents()));
				}
				
				if (response.hasPreferences()) {
					final UniTimeHeaderPanel hp = new UniTimeHeaderPanel(COURSE.sectionTitlePreferences());
					hp.setCollapsible(!"0".equals(ToolBox.getSessionCookie("Instructor.Preferences")));
					hp.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> e) {
							iPanel.getRowFormatter().setVisible(iPreferencesRow, e.getValue());
							ToolBox.setSessionCookie("Instructor.Preferences", e.getValue() ? "1" : "0");
						}
					});
					iPanel.addHeaderRow(hp);
					iPreferencesRow = iPanel.addRow(new TableWidget(response.getPreferences()));
					iPanel.getRowFormatter().setVisible(iPreferencesRow, hp.isCollapsible());
				}
				
				if (response.hasExternalId()) {
					iPanel.addRow(new TeachingAssignmentsWidget().forInstructorId(response.getInstructorId()));
				}
				
				if (response.hasLastChanges()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getLastChanges().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getLastChanges()));
				}
				
				iPanel.addBottomRow(iFooter);
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						String token = Window.Location.getHash();
						if (token != null && (token.startsWith("#A") || token.equals("#back")) || token.startsWith("#ioc")) {
							Element e = Document.get().getElementById(token.substring(1));
							if (e != null) ToolBox.scrollToElement(e);
						}
						Element e = Document.get().getElementById("back");
						if (e != null)
							ToolBox.scrollToElement(e);
					}
				});
				
				for (String op: iHeader.getOperations())
					iHeader.setEnabled(op, response.hasOperation(op));
				UniTimeNavigation.getInstance().refresh();
			}
		});
	}

}
