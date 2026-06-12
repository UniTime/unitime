package org.unitime.timetable.gwt.client.exams;

import java.util.ArrayList;
import java.util.List;

import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.MultiSelect;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportFilterRequesponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportFilterRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportResponse;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.PageFilter;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ExamPdfReportPage extends Composite implements ValueChangeHandler<FilterInterface> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static DateTimeFormat sTS = DateTimeFormat.getFormat(CONSTANTS.timeStampFormatShort());
	
	private ExaminationPdfReportFilterRequesponse iConfig;
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	
	private PageFilter iInput, iReports, iOutput, iParameters;
	
	private UniTimeHeaderPanel iQueueHeader, iLogHeader;
	private UniTimeTable<QueueItemInterface> iQueue;
	private HTML iLog;
	private int iQueueRow, iLogRow;
	private int iLastSelectedRow = -1;
	
	public ExamPdfReportPage() {
		iPanel = new SimpleForm(3);
		iPanel.addStyleName("unitime-PageFilter");
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iQueueHeader = new UniTimeHeaderPanel(EXAM.sectReportsInProgress());
		iQueueHeader.addButton("refresh", MESSAGES.buttonRefresh(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshQueue(null, null);
			}
		});
		iQueueRow = iPanel.addHeaderRow(iQueueHeader);
		
		iQueue = new UniTimeTable<ScriptInterface.QueueItemInterface>();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(MESSAGES.colName()));
		header.add(new UniTimeTableHeader(MESSAGES.colStatus()));
		header.add(new UniTimeTableHeader(MESSAGES.colOwner()));
		header.add(new UniTimeTableHeader(MESSAGES.colSession()));
		header.add(new UniTimeTableHeader(MESSAGES.colCreated()));
		header.add(new UniTimeTableHeader(MESSAGES.colStarted()));
		header.add(new UniTimeTableHeader(MESSAGES.colFinished()));
		header.add(new UniTimeTableHeader(MESSAGES.colOutput()));
		header.add(new UniTimeTableHeader(""));
		iQueue.addRow(null, header);
		iQueue.setAllowSelection(true);
		iQueue.addStyleName("unitime-QueueTable");
		iPanel.addRow(iQueue);
		
		iLogHeader = new UniTimeHeaderPanel();
		iLogRow = iPanel.addHeaderRow(iLogHeader);
		iLog = new HTML();
		iPanel.addRow(iLog);
		
		iPanel.getRowFormatter().setVisible(iQueueRow, false);
		iPanel.getRowFormatter().setVisible(iQueueRow + 1, false);
		iPanel.getRowFormatter().setVisible(iLogRow, false);
		iPanel.getRowFormatter().setVisible(iLogRow + 1, false);
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ExamPdfReportsPage");
		initWidget(iRootPanel);
		
		init();
		
		refreshQueue(null, null);
		new Timer() {
			@Override
			public void run() {
				refreshQueue(null, null);
			}
		}.scheduleRepeating(5000);
		
		iQueue.addMouseClickListener(new MouseClickListener<ScriptInterface.QueueItemInterface>() {
			@Override
			public void onMouseClick(TableEvent<QueueItemInterface> event) {
				if (iLastSelectedRow >= 1)
					iQueue.setSelected(iLastSelectedRow, false);
				if (event.getData() != null && iLastSelectedRow != event.getRow()) {
					iQueue.setSelected(event.getRow(), true);
					showLog(event.getData());
					iLastSelectedRow = event.getRow();
				} else {
					showLog(null);
					iLastSelectedRow = -1;
				}
			}
		});
	}

	protected void init() {
		RPC.execute(new ExaminationPdfReportFilterRequest(), new AsyncCallback<ExaminationPdfReportFilterRequesponse>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ExaminationPdfReportFilterRequesponse result) {
				iConfig = result;
				if (result.getInput() != null)
					iInput = addSection(result.getInput(), EXAM.sectInputData());
				if (result.getReports() != null)
					iReports = addSection(result.getReports(), EXAM.sectReport());
				if (result.getParameters() != null)
					iParameters = addSection(result.getParameters(), EXAM.sectParameters());
				if (result.getOutput() != null)
					iOutput = addSection(result.getOutput(), EXAM.sectOutput());
				ValueChangeEvent.fire(iInput, iInput.getValue());
				ValueChangeEvent.fire(iReports, iReports.getValue());
				ValueChangeEvent.fire(iParameters, iParameters.getValue());
				ValueChangeEvent.fire(iOutput, iOutput.getValue());
				if (iOutput.getFilterWidget("subject") != null)
					iPanel.getRowFormatter().addStyleName(iOutput.getFilterRow("subject"), "subject-line");
				
				iInput.getHeader().addButton("generate", EXAM.actionGenerateReport(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						String valid = validate();
						if (valid != null) {
							iInput.getHeader().setErrorMessage(valid);
							return;
						}
						ExaminationPdfReportRequest request = new ExaminationPdfReportRequest();
						for (FilterParameterInterface p: iConfig.getInput().getParameters())
							request.setParameter(p.getName(), (p.getValue() == null ? p.getDefaultValue() : p.getValue()));
						for (FilterParameterInterface p: iConfig.getReports().getParameters())
							request.setParameter(p.getName(), (p.getValue() == null ? p.getDefaultValue() : p.getValue()));
						for (FilterParameterInterface p: iConfig.getParameters().getParameters())
							request.setParameter(p.getName(), (p.getValue() == null ? p.getDefaultValue() : p.getValue()));
						for (FilterParameterInterface p: iConfig.getOutput().getParameters())
							request.setParameter(p.getName(), (p.getValue() == null ? p.getDefaultValue() : p.getValue()));
						iInput.getHeader().showLoading();
						RPC.execute(request, new AsyncCallback<ExaminationPdfReportResponse>() {

							@Override
							public void onFailure(Throwable caught) {
								iInput.getHeader().clearMessage();
								iInput.getHeader().setErrorMessage(caught.getMessage());
								UniTimeNotifications.error(caught.getMessage(), caught);
								ToolBox.checkAccess(caught);
							}

							@Override
							public void onSuccess(ExaminationPdfReportResponse response) {
								iInput.getHeader().clearMessage();
								refreshQueue(null, response.getLogId());
							}
						});
					}
				});
				iPanel.addBottomRow(iInput.getHeader().clonePanel(""));
				
				if (result.hasSolverWarning()) {
					RootPanel cpm = RootPanel.get("UniTimeGWT:CustomPageMessages");
					if (cpm != null) {
						P p = new P("unitime-PageWarn");
						p.setHTML(result.getSolverWarning());
						cpm.add(p);
					}
				}
			}
		});
	}
	
	protected String validate() {
		if (iReports.getValue().getParameterValue("reports", "").isEmpty())
			return EXAM.errorNoReportSelected();
		if (!("1".equals(iInput.getValue().getParameterValue("all"))) && iInput.getValue().getParameterValue("subjects", "").isEmpty())
			return EXAM.errorNoSubjectAreaSelected();
		return null;
	}
	
	protected PageFilter addSection(FilterInterface filter, String label) {
		for (FilterParameterInterface p: filter.getParameters()) {
			String v = Window.Location.getParameter(p.getName());
			if (v != null) p.setValue(v);
		}
		PageFilter ret = new PageFilter(label, false);
		ret.addValueChangeHandler(this);
		ret.populate(iPanel, filter);
		return ret;
	}
	
	@Override
	public void onValueChange(ValueChangeEvent<FilterInterface> event) {
		FilterInterface filter = event.getValue();
		if (filter == null) return;
		if (filter.hasParameter("all")) {
			((ListBox)iInput.getFilterWidget("subjects")).setEnabled(!"1".equals(filter.getParameterValue("all")));
			Widget emailDeputies = iOutput.getFilterWidget("emailDeputies");
			if (emailDeputies != null)
				((CheckBox)emailDeputies).setEnabled(!"1".equals(filter.getParameterValue("all")));
		}
		if (filter.hasParameter("email")) {
			boolean visible = "1".equals(filter.getParameterValue("email"));
			for (int row = iOutput.getFilterRow("addr"); row <= iOutput.getFilterRow("message"); row++)
				iPanel.getRowFormatter().setVisible(row, visible);
		}
		if (filter.hasParameter("reports")) {
			MultiSelect<String> select = (MultiSelect<String>)iReports.getFilterWidget("reports");
			Widget emailStudents = iOutput.getFilterWidget("emailStudents");
			Widget emailInstructors = iOutput.getFilterWidget("emailInstructors");
			if (emailStudents != null)
				((CheckBox)emailStudents).setEnabled(select.isSelected("StudentExamReport"));
			if (emailInstructors != null)
				((CheckBox)emailInstructors).setEnabled(select.isSelected("InstructorExamReport"));
		}
	}
	
	private void showLog(QueueItemInterface item) {
		if (item == null || item.getLog() == null || item.getLog().isEmpty()) {
			iPanel.getRowFormatter().setVisible(iLogRow, false);
			iPanel.getRowFormatter().setVisible(iLogRow + 1, false);
		} else {
			iLogHeader.setHeaderTitle(MESSAGES.sectScriptLog(item.getName()));
			iPanel.getRowFormatter().setVisible(iLogRow, true);
			iPanel.getRowFormatter().setVisible(iLogRow + 1, true);
			iLog.setHTML(item.getLog());
		}
	}
	
	private void refreshQueue(String deleteId, final String selectId) {
		RPC.execute(new ScriptInterface.GetQueueTableRpcRequest(deleteId).setType(QueueType.ExamPdfReport), new AsyncCallback<GwtRpcResponseList<QueueItemInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<QueueItemInterface> result) {
				populate(result, selectId);
			}
		});
	}
	
	private void populate(GwtRpcResponseList<QueueItemInterface> queue, String selectId) {
		if (iQueue.getSelectedRow() > 0 && selectId == null) {
			QueueItemInterface q = iQueue.getData(iQueue.getSelectedRow());
			if (q != null) selectId = q.getId();
		}
		QueueItemInterface selectedQueue = null;
		iQueue.clearTable(1);
		iLastSelectedRow = -1;
		
		for (final QueueItemInterface q: queue) {
			List<Widget> line = new ArrayList<Widget>();
			line.add(new Label(q.getName()));
			line.add(new Label(q.getStatus()));
			line.add(new Label(q.getOwner()));
			line.add(new Label(q.getSession()));
			line.add(new Label(q.getCreated() == null ? "" : sTS.format(q.getCreated())));
			line.add(new Label(q.getStarted() == null ? "" : sTS.format(q.getStarted())));
			line.add(new Label(q.getFinished() == null ? "" : sTS.format(q.getFinished())));
			if (q.getOtuput() != null) {
				line.add(new Anchor(q.getOtuput().substring(1 + q.getOtuput().lastIndexOf('.')), q.getOtuputLink()));
			} else {
				line.add(new Label(""));
			}
			if (q.isCanDelete()) {
				ImageButton delete = new ImageButton(RESOURCES.delete());
				delete.setTitle(MESSAGES.titleDeleteRow());
				delete.setAltText(MESSAGES.titleDeleteRow());
				delete.getElement().getStyle().setCursor(Cursor.POINTER);
				delete.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						refreshQueue(q.getId(), null);
					}
				});
				line.add(delete);
			} else {
				line.add(new Label(""));
			}
			
			iQueue.addRow(q, line);
			
			if (selectId != null && selectId.equals(q.getId())) {
				iQueue.setSelected(iQueue.getRowCount() - 1, true);
				iLastSelectedRow = iQueue.getRowCount() - 1;
				selectedQueue = q;
			}
		}
		
		iPanel.getRowFormatter().setVisible(iQueueRow, iQueue.getRowCount() > 1);
		iPanel.getRowFormatter().setVisible(iQueueRow + 1, iQueue.getRowCount() > 1);
		showLog(selectedQueue);
	}
}
