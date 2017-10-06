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
package org.unitime.timetable.gwt.client.solver;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.solver.SolverPage.SolverStatus;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.ListSolutionsRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.ListSolutionsResponse;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionOperation;
import org.unitime.timetable.gwt.shared.SolverInterface.InfoPair;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.SolverInterface.ProgressMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.SolutionInfo;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverConfiguration;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverOwner;
import org.unitime.timetable.gwt.shared.TableInterface.TableRowInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author Tomas Muller
 */
public class ListSolutionsPage extends SimpleForm {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private UniTimeHeaderPanel iTableHeader;
	private DataTable iTable;
	private ListSolutionsResponse iResponse;
	private TextArea iCurrentSolutionNote;
	private ListBox iSolverConfig = null;
	private ListBox iSolverOwner = null;
	private ListBox iSolverHost = null;
	private int iSolutionLine, iSolutionButtonsLine, iSolutionsTableHeaderLine;
	private Map<Long, Integer> iSelectedSolutionLine = new HashMap<Long, Integer>();
	private Map<Long, Integer> iSelectedSolutionButtonsLine = new HashMap<Long, Integer>();
	
	public ListSolutionsPage() {
		addStyleName("unitime-ListSolutionsPage");
		iTableHeader = new UniTimeHeaderPanel(MESSAGES.sectSavedSolutions());
		addHeaderRow(iTableHeader);
		execute(iTableHeader, SolutionOperation.INIT);
	}
	
	protected void execute(final UniTimeHeaderPanel header, SolutionOperation operation) {
		execute(header, operation, null, null);
	}
	
	protected void execute(final UniTimeHeaderPanel header, SolutionOperation operation, Long solutionId) {
		execute(header, operation, null, solutionId);
	}
	
	protected void execute(final UniTimeHeaderPanel header, final SolutionOperation operation, final HasText note, final Long solutionId) {
		switch (operation) {
		case EXPORT:
			ToolBox.open(GWT.getHostPageBaseURL() + "export?output=solution.csv&type=course" + (solutionId == null ? "" : "&solution=" + solutionId));
			return;
		}
		final ListSolutionsRequest request = new ListSolutionsRequest(operation);
		String confirmation = null;
		switch (operation) {
		case UNLOAD:
			confirmation = MESSAGES.confirmSolverUnload();
			break;
		case SAVE:
			confirmation = MESSAGES.confirmSolverSave();
			if (iCurrentSolutionNote != null)
				request.setNote(iCurrentSolutionNote.getText());
			break;
		case SAVE_AS_NEW:
			confirmation = MESSAGES.confirmSolverSaveAsNew();
			if (iCurrentSolutionNote != null)
				request.setNote(iCurrentSolutionNote.getText());
			break;
		case SAVE_COMMIT:
			confirmation = MESSAGES.confirmSolverSaveCommit();
			if (iCurrentSolutionNote != null)
				request.setNote(iCurrentSolutionNote.getText());
			break;
		case SAVE_AS_NEW_COMMIT:
			confirmation = MESSAGES.confirmSolverSaveAsNewCommit();
			if (iCurrentSolutionNote != null)
				request.setNote(iCurrentSolutionNote.getText());
			break;
		case UNCOMMIT:
			request.setNote(note.getText());
			request.addSolutionId(solutionId);
			confirmation = MESSAGES.confirmSolverUncommit();
			break;
		case COMMIT:
			request.setNote(note.getText());
			request.addSolutionId(solutionId);
			confirmation = MESSAGES.confirmSolverCommit();
			break;
		case DELETE:
			request.addSolutionId(solutionId);
			confirmation = MESSAGES.confirmSolverDelete();
			break;
		case RELOAD:
			if (iCurrentSolutionNote != null)
				request.setNote(iCurrentSolutionNote.getText());
			break;
		case SELECT:
		case DESELECT:
			request.addSolutionId(solutionId);
			break;
		case UPDATE_NOTE:
			request.setNote(note.getText());
			request.addSolutionId(solutionId);
			break;
		case LOAD:
			request.setConfigurationId(Long.valueOf(iSolverConfig.getSelectedValue()));
			request.setHost(iSolverHost == null ? null : iSolverHost.getSelectedValue());
			break;
		case LOAD_EMPTY:
			request.setConfigurationId(Long.valueOf(iSolverConfig.getSelectedValue()));
			request.setHost(iSolverHost == null ? null : iSolverHost.getSelectedValue());
			request.setOwnerId(Long.valueOf(iSolverOwner.getSelectedValue()));
			break;
		}
		final Command command = new Command() {
			@Override
			public void execute() {
				if (operation == SolutionOperation.INIT || operation == SolutionOperation.CHECK)
					header.showLoading();
				else
					LoadingWidget.showLoading(MESSAGES.waitSolverExecution());
				RPC.execute(request, new AsyncCallback<ListSolutionsResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.hideLoading();
						header.setErrorMessage(caught.getMessage());
						UniTimeNotifications.error(caught.getMessage(), caught);
						ToolBox.checkAccess(caught);
					}

					@Override
					public void onSuccess(ListSolutionsResponse response) {
						LoadingWidget.hideLoading();
						header.clearMessage();
						populate(request, response);
						UniTimePageHeader.getInstance().reloadSolverInfo();
					}
				});
			}
		};
		if (confirmation == null) {
			command.execute();
		} else {
			UniTimeConfirmationDialog.confirm(confirmation, command);
		}
	}
	
	protected ClickHandler createClickHandler(final UniTimeHeaderPanel header, SolutionOperation operation) {
		return createClickHandler(header, operation, null, null);
	}
	
	protected ClickHandler createClickHandler(final UniTimeHeaderPanel header, SolutionOperation operation, Long solutionId) {
		return createClickHandler(header, operation, null, solutionId);
	}
	
	protected ClickHandler createClickHandler(final UniTimeHeaderPanel header, final SolutionOperation operation, final HasText parameter, final Long solutionId) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				execute(header, operation, parameter, solutionId);
			}
		};
	}
	
	protected void populate(ListSolutionsRequest request, ListSolutionsResponse response) {
		clear();
		iResponse = response;
		
		RootPanel cpm = RootPanel.get("UniTimeGWT:CustomPageMessages");
		if (cpm != null) {
			cpm.clear();
			if (response.hasPageMessages()) {
				for (final PageMessage pm: response.getPageMessages()) {
					P p = new P(pm.getType() == PageMessageType.ERROR ? "unitime-PageError" : pm.getType() == PageMessageType.WARNING ? "unitime-PageWarn" : "unitime-PageMessage");
					p.setHTML(pm.getMessage());
					if (pm.hasUrl()) {
						p.addStyleName("unitime-ClickablePageMessage");
						p.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (pm.hasUrl()) ToolBox.open(GWT.getHostPageBaseURL() + pm.getUrl());
							}
						});
					}
					cpm.add(p);
				}
			}
		}
		
		iSolutionLine = -1;
		iSolutionButtonsLine = -1;
		if (response.hasCurrentSolution()) {
			UniTimeHeaderPanel header = new UniTimeHeaderPanel(MESSAGES.sectListSolutionsCurrentSolution());
			iSolutionLine = addHeaderRow(header);
			if (response.getSolverStatus() != null) {
				SolverStatus status = new SolverStatus(); status.setStatus(response.getSolverStatus());
				addRow(MESSAGES.propSolverStatus(), status);
			}
			if (response.getSolverProgress() != null)
				addRow(MESSAGES.propSolverProgress(), new Label(response.getSolverProgress()));
			SolutionInfo solution = response.getCurrentSolution();
			if (solution.getCreated() != null)
				addRow(new HTML(MESSAGES.propSolutionCreated()), new HTML(solution.getCreated()));
			if (solution.getOwner() != null)
				addRow(new HTML(MESSAGES.propSolutionOwner()), new HTML(solution.getOwner()));
			if (solution.getCommitted() != null)
				addRow(new HTML(MESSAGES.propSolutionCommitted()), new HTML(solution.getCommitted()));
			if (response.canExecute(-1l, SolutionOperation.UPDATE_NOTE)) {
				iCurrentSolutionNote = new TextArea();
				iCurrentSolutionNote.setStyleName("unitime-TextArea");
				iCurrentSolutionNote.setVisibleLines(5);
				iCurrentSolutionNote.setCharacterWidth(80);
				iCurrentSolutionNote.setText(solution.getNote());
				addRow(new HTML(MESSAGES.propSolutionNote()), iCurrentSolutionNote);
			} else if (!solution.getNote().isEmpty()) {
				HTML html = new HTML(solution.getNote()); html.addStyleName("note");
				addRow(new HTML(MESSAGES.propSolutionNote()), html);
			}
			for (InfoPair pair: solution.getPairs())
				addRow(new HTML(pair.getName()), new HTML(pair.getValue()));
			if (response.hasLog())
				addRow(new HTML(MESSAGES.propSolutionLog()), new ProgressLog(response.getLog()));
			if (response.canExecute(-1l, SolutionOperation.UNLOAD) || response.canExecute(-1l, SolutionOperation.CHECK)) {
				UniTimeHeaderPanel buttons = header.clonePanel(null);
				if (response.canExecute(-1l, SolutionOperation.SAVE))
					buttons.addButton("save", MESSAGES.opSolverSave(), createClickHandler(header, SolutionOperation.SAVE));
				if (response.canExecute(-1l, SolutionOperation.SAVE_AS_NEW))
					buttons.addButton("save new", MESSAGES.opSolverSaveAsNew(), createClickHandler(header, SolutionOperation.SAVE_AS_NEW));
				if (response.canExecute(-1l, SolutionOperation.SAVE_COMMIT))
					buttons.addButton("save commit", MESSAGES.opSolverSaveCommit(), createClickHandler(header, SolutionOperation.SAVE_COMMIT));
				if (response.canExecute(-1l, SolutionOperation.SAVE_AS_NEW_COMMIT))
					buttons.addButton("save new commit", MESSAGES.opSolverSaveAsNewCommit(), createClickHandler(header, SolutionOperation.SAVE_AS_NEW_COMMIT));
				if (response.canExecute(-1l, SolutionOperation.RELOAD))
					buttons.addButton("reload", MESSAGES.opSolverReload(), createClickHandler(header, SolutionOperation.RELOAD));
				if (response.canExecute(-1l, SolutionOperation.UNLOAD))
					buttons.addButton("unload", MESSAGES.opSolverUnload(), createClickHandler(header, SolutionOperation.UNLOAD));
				if (response.canExecute(-1l, SolutionOperation.CHECK))
					buttons.addButton("refresh", MESSAGES.opSolverRefresh(), createClickHandler(header, SolutionOperation.CHECK));
				iSolutionButtonsLine = addBottomRow(buttons);
				getFlexCellFormatter().removeStyleName(iSolutionButtonsLine, 0, "unitime-TopLine");
			}
		}
		
		if (response.hasConfigurations()) {
			iSolverConfig = new ListBox();
			for (SolverConfiguration config: response.getConfigurations()) {
				iSolverConfig.addItem(config.getName(), config.getId().toString());
				if (config.getId().equals(response.getConfigurationId()))
					iSolverConfig.setSelectedIndex(iSolverConfig.getItemCount() - 1);
			}
		} else {
			iSolverConfig = null;
		}
		if (response.hasHosts()) {
			iSolverHost = new ListBox();
			for (String host: response.getHosts()) {
				iSolverHost.addItem(host);
				if (host.equals(response.getHost()))
					iSolverHost.setSelectedIndex(iSolverHost.getItemCount() - 1);
			}
		} else {
			iSolverHost = null;
		}

		iSelectedSolutionLine.clear(); iSelectedSolutionButtonsLine.clear();
		boolean load = false;
		if (response.hasSelectedSolutions()) {
			load = true;
			for (final SolutionInfo selected: response.getSelectedSolutions()) {
				UniTimeHeaderPanel header = new UniTimeHeaderPanel(MESSAGES.sectSolverSelectedSolution(selected.getName()));
				iSelectedSolutionLine.put(selected.getId(), addHeaderRow(header));
				if (selected.getCreated() != null)
					addRow(new HTML(MESSAGES.propSolutionCreated()), new HTML(selected.getCreated()));
				if (selected.getOwner() != null)
					addRow(new HTML(MESSAGES.propSolutionOwner()), new HTML(selected.getOwner()));
				if (selected.getCommitted() != null)
					addRow(new HTML(MESSAGES.propSolutionCommitted()), new HTML(selected.getCommitted()));
				TextArea note = null;
				if (response.canExecute(selected.getId(), SolutionOperation.UPDATE_NOTE)) {
					note = new TextArea();
					note.setStyleName("unitime-TextArea");
					note.setVisibleLines(5);
					note.setCharacterWidth(80);
					note.setText(selected.getNote());
					addRow(new HTML(MESSAGES.propSolutionNote()), note);
				} else if (!selected.getNote().isEmpty()) {
					HTML html = new HTML(selected.getNote()); html.addStyleName("note");
					addRow(new HTML(MESSAGES.propSolutionNote()), html);
				}
				for (InfoPair pair: selected.getPairs())
					addRow(new HTML(pair.getName()), new HTML(pair.getValue()));
				if (selected.hasLog())
					addRow(new HTML(MESSAGES.propSolutionLog()), new ProgressLog(selected.getLog()));
				UniTimeHeaderPanel buttons = header.clonePanel(null);
				buttons.addButton("deselect", MESSAGES.opSolutionDeselect(), createClickHandler(header, SolutionOperation.DESELECT, selected.getId()));
				if (response.canExecute(selected.getId(), SolutionOperation.UPDATE_NOTE))
					buttons.addButton("update note", MESSAGES.opSolutionUpdateNote(), createClickHandler(header, SolutionOperation.UPDATE_NOTE, note, selected.getId()));
				if (response.canExecute(selected.getId(), SolutionOperation.COMMIT))
					buttons.addButton("commit", MESSAGES.opSolutionCommit(), createClickHandler(header, SolutionOperation.COMMIT, note, selected.getId()));
				if (response.canExecute(selected.getId(), SolutionOperation.UNCOMMIT))
					buttons.addButton("uncommit", MESSAGES.opSolutionUncommit(), createClickHandler(header, SolutionOperation.UNCOMMIT, note, selected.getId()));
				if (response.canExecute(selected.getId(), SolutionOperation.EXPORT))
					buttons.addButton("export", MESSAGES.opSolutionExport(), createClickHandler(header, SolutionOperation.EXPORT, selected.getId()));
				if (response.canExecute(selected.getId(), SolutionOperation.DELETE))
					buttons.addButton("delete", MESSAGES.opSolutionDelete(), createClickHandler(header, SolutionOperation.DELETE, selected.getId()));
				if (!response.canExecute(selected.getId(), SolutionOperation.LOAD))
					load = false;
				int row = addBottomRow(buttons);
				getFlexCellFormatter().removeStyleName(row, 0, "unitime-TopLine");
				iSelectedSolutionButtonsLine.put(selected.getId(), row);
			}
			if (load && iSolverConfig != null) {
				P loadPanel = new P("load-panel");
				P configLabel = new P("config-label"); configLabel.setText(MESSAGES.propSolverConfig()); loadPanel.add(configLabel);
				loadPanel.add(iSolverConfig);
				if (iSolverHost != null) {
					P hostLabel = new P("host-label"); hostLabel.setText(MESSAGES.propSolverHost()); loadPanel.add(hostLabel);
					loadPanel.add(iSolverHost);
				}
				AriaButton button = new AriaButton(MESSAGES.opSolverLoad()); button.addClickHandler(createClickHandler(iTableHeader, SolutionOperation.LOAD));
				loadPanel.add(button);
				addRow(MESSAGES.propLoadInteractiveSolver(), loadPanel);
			}
		}
		
		iTableHeader.clearMessage();
		iSolutionsTableHeaderLine = addHeaderRow(iTableHeader);
		if (response.hasMessage()) {
			iTableHeader.setMessage(response.getMessage());
		}
		if (response.getRows().isEmpty()) {
			if (!response.hasMessage())
				iTableHeader.setMessage(MESSAGES.errorListSolutionsNoDataReturned());
		} else {
			if (iTable == null) {
				iTable = new DataTable(response);
				iTable.addValueChangeHandler(new ValueChangeHandler<Integer>() {
					@Override
					public void onValueChange(ValueChangeEvent<Integer> event) {
						SolverCookie.getInstance().setListSolutionsSort(event.getValue() == null ? 0 : event.getValue().intValue());
					}
				});
				iTable.addMouseClickListener(new MouseClickListener<TableRowInterface>() {
					@Override
					public void onMouseClick(TableEvent<TableRowInterface> event) {
						if (event.getData() != null) {
							boolean selected = false;
							if (iResponse.hasSelectedSolutions())
								for (SolutionInfo solution: iResponse.getSelectedSolutions())
									if (event.getData().getId().equals(solution.getId())) selected = true;
							execute(iTableHeader, selected ? SolutionOperation.DESELECT : SolutionOperation.SELECT, event.getData().getId());
						}
					}
				});
			} else {
				iTable.populate(response);
			}
			iTable.setValue(SolverCookie.getInstance().getListSolutionsSort());
			addRow(iTable);
		}
		
		if (!load && iSolverConfig != null && response.canExecute(-1l, SolutionOperation.LOAD_EMPTY) && response.hasSolverOwners()) {
			P loadPanel = new P("load-panel");
			iSolverOwner = new ListBox();
			for (SolverOwner owner: response.getSolverOwners()) {
				iSolverOwner.addItem(owner.getName(), owner.getId().toString());
			}
			P ownerLabel = new P("owner-label"); ownerLabel.setText(MESSAGES.propSolverOwner()); loadPanel.add(ownerLabel);
			loadPanel.add(iSolverOwner);
			P configLabel = new P("config-label"); configLabel.setText(MESSAGES.propSolverConfig()); loadPanel.add(configLabel);
			loadPanel.add(iSolverConfig);
			if (iSolverHost != null) {
				P hostLabel = new P("host-label"); hostLabel.setText(MESSAGES.propSolverHost()); loadPanel.add(hostLabel);
				loadPanel.add(iSolverHost);
			}
			AriaButton button = new AriaButton(MESSAGES.opSolverLoadEmptySolution()); button.addClickHandler(createClickHandler(iTableHeader, SolutionOperation.LOAD_EMPTY));
			loadPanel.add(button);
			addRow(MESSAGES.propLoadInteractiveSolver(), loadPanel);
		}
		
		if (response.hasErrors()) {
			UniTimeConfirmationDialog.alert(response.getErrorMessage("\n"));
			if (request.hasSolutionIds()) {
				for (Long id: request.getSolutionIds()) {
					Integer row = iSelectedSolutionButtonsLine.get(id);
					if (row != null)
						((UniTimeHeaderPanel)getWidget(row, 0)).setErrorMessage(response.getErrorMessage("<br>"));
				}
			}
		}
		
		switch (response.getOperation()) {
		case LOAD:
		case LOAD_EMPTY:
			if (iSolutionLine >= 0)
				getRowFormatter().getElement(iSolutionLine).scrollIntoView();
			break;
		case SELECT:
			if (request.hasSolutionIds()) {
				for (Long id: request.getSolutionIds()) {
					Integer row = iSelectedSolutionLine.get(id);
					if (row != null) {
						getRowFormatter().getElement(row).scrollIntoView();
						break;
					}
				}
			}
			break;
		case DESELECT:
		case UNLOAD:
			if (iSolutionsTableHeaderLine >= 0)
				getRowFormatter().getElement(iSolutionsTableHeaderLine).scrollIntoView();
			break;
		}
	}
	
	public static class ProgressLog extends HTML implements TakesValue<List<ProgressMessage>>{
		private List<ProgressMessage> iLog;
		
		public ProgressLog(List<ProgressMessage> log) {
			setValue(log);
		}
		
		public static String htmlTraceLog(ProgressMessage m) {
			String trace[] = m.getStackTrace();
			if (trace == null || trace.length == 0) return "";
			StringBuffer ret = new StringBuffer("<BR>" + trace[0]);
            for (int i = 1; i < trace.length; i++)
                ret.append("<BR>&nbsp;&nbsp;&nbsp;&nbsp;at " + trace[i]);
            return ret.toString();
		}
		
		public static String toString(ProgressMessage m) {
			switch (m.getLevel()) {
			case WARN:
				return "<span style='color:orange;font-weight:bold;'>" + CONSTANTS.progressLogLevel()[m.getLevel().ordinal()].toUpperCase() + ":</span> " + m.getMessage() + htmlTraceLog(m);
			case ERROR:
				return "<span style='color:red;font-weight:bold;'>" + CONSTANTS.progressLogLevel()[m.getLevel().ordinal()].toUpperCase() + ":</span> " + m.getMessage() + htmlTraceLog(m);
			case FATAL:
				return "<span style='color:red;font-weight:bold;'>&gt;&gt;&gt;" + CONSTANTS.progressLogLevel()[m.getLevel().ordinal()].toUpperCase() + ": " + m.getMessage() + " &lt;&lt;&lt;</span>" + htmlTraceLog(m);
			default:
				return null;
			}
		}

		@Override
		public void setValue(List<ProgressMessage> value) {
			iLog = value;
			StringBuffer s = new StringBuffer();
			if (iLog != null) {
				for (ProgressMessage m: iLog) {
					String html = toString(m);
					if (html == null || html.isEmpty()) continue;
					if (s.length() > 0) s.append("<br>");
					s.append(html);
				}
			}
			setHTML(s.toString());
		}

		@Override
		public List<ProgressMessage> getValue() {
			return iLog;
		}
		
		public Date getLastDate() {
			if (iLog == null || iLog.isEmpty()) return null;
			return iLog.get(iLog.size() - 1).getDate();
		}
		
		public void append(List<ProgressMessage> log) {
			StringBuffer s = new StringBuffer(getHTML());
			for (ProgressMessage m: log) {
				iLog.add(m);
				if (s.length() > 0) s.append("<br>");
				s.append(toString(m));
			}
			setHTML(s.toString());
		}
	}

}
