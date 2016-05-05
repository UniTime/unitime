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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameRpcRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.InfoPair;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.SolverInterface.SolutionInfo;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverConfiguration;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverOperation;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverOwner;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverParameter;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class SolverPage extends SimpleForm {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static DateTimeFormat sLoadDateFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private Map<String, SolverConfiguration> iConfigurations;
	
	private SolverType iType;
	private int iLoadDateRow, iSolverStatusRow, iSolverProgressRow, iSolverConfigRow, iButtonsRow;
	private UniTimeHeaderPanel iSolverHeader, iSolverButtons;
	private Label iLoadDate = null, iSolverProgress = null;
	private SolverStatus iSolverStatus = null; 
	private ListBox iSolverConfig = null;
	private List<Parameter> iParameters = null;
	private ListBox iSolverOwner = null;
	private ListBox iSolverHost = null;
	
	public SolverPage() {
		addStyleName("unitime-SolverPage");
		iType = SolverType.valueOf(Location.getParameter("type").toUpperCase());
		switch (iType) {
		case COURSE:
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageCourseTimetablingSolver());
			break;
		case EXAM:
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageExaminationTimetablingSolver());
			break;
		case INSTRUCTOR:
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageInstructorSchedulingSolver());
			break;
		case STUDENT:
			UniTimePageLabel.getInstance().setPageName(MESSAGES.pageStudentSchedulingSolver());
			break;
		}
		iSolverHeader = new UniTimeHeaderPanel(CONSTANTS.solverType()[iType.ordinal()]);
		addHeaderRow(iSolverHeader);
		iSolverButtons = new UniTimeHeaderPanel();
		iSolverButtons.addButton("load", MESSAGES.opSolverLoad(), createClickHandler(SolverOperation.LOAD));
		iSolverButtons.addButton("start", MESSAGES.opSolverStart(), createClickHandler(SolverOperation.START));
		iSolverButtons.addButton("stop", MESSAGES.opSolverStop(), createClickHandler(SolverOperation.STOP));
		iSolverButtons.addButton("sectioning", MESSAGES.opSolverStudentSectioning(), createClickHandler(SolverOperation.STUDENT_SECTIONING));
		iSolverButtons.addButton("reload", MESSAGES.opSolverReload(), createClickHandler(SolverOperation.RELOAD));
		iSolverButtons.addButton("save", MESSAGES.opSolverSave(), createClickHandler(SolverOperation.SAVE));
		iSolverButtons.addButton("save new", MESSAGES.opSolverSaveAsNew(), createClickHandler(SolverOperation.SAVE_AS_NEW));
		iSolverButtons.addButton("clear", MESSAGES.opSolverClear(), createClickHandler(SolverOperation.CLEAR));
		iSolverButtons.addButton("csv", MESSAGES.opSolverExportCSV(), createClickHandler(SolverOperation.EXPORT_CSV));
		iSolverButtons.addButton("unload", MESSAGES.opSolverUnload(), createClickHandler(SolverOperation.UNLOAD));
		iSolverButtons.addButton("refresh", MESSAGES.opSolverRefresh(), createClickHandler(SolverOperation.CHECK));
		
		execute(SolverOperation.INIT);
	}
	
	protected ClickHandler createClickHandler(final SolverOperation operation) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				execute(operation);
			}
		};
	}
	
	protected void execute(final SolverOperation operation) {
		switch (operation) {
		case EXPORT_CSV:
			ToolBox.open(GWT.getHostPageBaseURL() + "export?output=solution.csv&type=" + iType.name());
			return;
		case EXPORT_XML:
			ToolBox.open(GWT.getHostPageBaseURL() + "export?output=solution.xml&type=" + iType.name());
			return;
		}
		final SolverPageRequest request = new SolverPageRequest(iType, operation);
		if (iSolverOwner != null && iSolverOwner.isEnabled()) {
			if (iSolverOwner.isMultipleSelect()) {
				for (int i = 0; i < iSolverOwner.getItemCount(); i++) {
					if (iSolverOwner.isItemSelected(i))
						request.addOwnerId(Long.valueOf(iSolverOwner.getValue(i)));
				}
				if (!request.hasOwerIds() && operation != SolverOperation.CHECK) {
					iSolverHeader.setErrorMessage(MESSAGES.errorSolverNoOwnerSelected());
					return;
				}
			} else {
				request.addOwnerId(Long.valueOf(iSolverOwner.getValue(iSolverOwner.getSelectedIndex())));
			}
		}
		if (iSolverHost != null && iSolverHost.isEnabled()) {
			request.setHost(iSolverHost.getItemText(iSolverHost.getSelectedIndex()));
		}
		if (iSolverConfig != null && iSolverConfig.isEnabled()) {
			request.setConfigurationId(Long.valueOf(iSolverConfig.getValue(iSolverConfig.getSelectedIndex())));
		}
		if (iParameters != null) {
			for (Parameter p: iParameters) {
				if (p.isEnabled()) request.addParameter(p.getParameterId(), p.getParameterValue());
			}
		}
		String confirmation = null;
		switch (operation) {
		case UNLOAD:
			if (iType == SolverType.STUDENT)
				confirmation = MESSAGES.confirmStudentSolverUnload();
			else
				confirmation = MESSAGES.confirmSolverUnload();
			break;
		case CLEAR:
			if (iType == SolverType.STUDENT)
				confirmation = MESSAGES.confirmStudentSolverClear();
			else
				confirmation = MESSAGES.confirmSolverClear();
			break;
		case SAVE:
			if (iType == SolverType.STUDENT)
				confirmation = MESSAGES.confirmStudentSolverSave();
			else
				confirmation = MESSAGES.confirmSolverSave();
			break;
		case SAVE_AS_NEW:
			if (iType == SolverType.STUDENT)
				confirmation = MESSAGES.confirmStudentSolverSaveAsNew();
			else
				confirmation = MESSAGES.confirmSolverSaveAsNew();
			break;
		case SAVE_COMMIT:
			confirmation = MESSAGES.confirmSolverSaveCommit();
			break;
		case SAVE_AS_NEW_COMMIT:
			confirmation = MESSAGES.confirmSolverSaveAsNewCommit();
			break;
		}
		final Command command = new Command() {
			@Override
			public void execute() {
				if (operation == SolverOperation.INIT || operation == SolverOperation.CHECK)
					iSolverHeader.showLoading();
				else
					LoadingWidget.showLoading(MESSAGES.waitSolverExecution());
				RPC.execute(request, new AsyncCallback<SolverPageResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.hideLoading();
						ToolBox.checkAccess(caught);
						UniTimeNotifications.error(caught.getMessage());
						iSolverHeader.setErrorMessage(caught.getMessage());
					}
					@Override
					public void onSuccess(SolverPageResponse result) {
						LoadingWidget.hideLoading();
						iSolverHeader.clearMessage();
						populate(result);
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
	
	protected void populate(SolverPageResponse response) {
		if (iLoadDate == null) {
			clear();
			addHeaderRow(iSolverHeader);
			iLoadDate = new Label();
			iLoadDateRow = addRow(MESSAGES.propSolverLoadDate(), iLoadDate);
			iSolverStatus = new SolverStatus();
			iSolverStatusRow = addRow(MESSAGES.propSolverStatus(), iSolverStatus);
			iSolverProgress = new Label();
			iSolverProgressRow = addRow(MESSAGES.propSolverProgress(), iSolverProgress);
			if (response.hasSolverOwners()) {
				iSolverOwner = new ListBox();
				if (response.isAllowMultipleOwners()) {
					iSolverOwner.setMultipleSelect(true);
					iSolverOwner.setVisibleItemCount(response.getSolverOwners().size() < 5 ? response.getSolverOwners().size() : 5);
				} else {
					iSolverOwner.setMultipleSelect(false);
				}
				for (SolverOwner owner: response.getSolverOwners()) {
					iSolverOwner.addItem(owner.getName(), owner.getId().toString());
				}
			}
			if (iType == SolverType.EXAM && iSolverOwner != null)
				addRow(MESSAGES.propExamSolverOwner(), iSolverOwner);
			iSolverConfig = new ListBox();
			iSolverConfigRow = addRow(MESSAGES.propSolverProgress(), iSolverConfig);
			iConfigurations = new HashMap<String, SolverConfiguration>();
			if (response.hasConfigurations()) {
				for (SolverConfiguration config: response.getConfigurations()) {
					iSolverConfig.addItem(config.getName(), config.getId().toString());
					iConfigurations.put(config.getId().toString(), config);
				}
				iSolverConfig.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						SolverConfiguration config = iConfigurations.get(iSolverConfig.getValue(iSolverConfig.getSelectedIndex()));
						for (Parameter p: iParameters) {
							String value = config.getParameter(p.getParameterId());
							p.setParameterValue(value != null ? value : p.getParameterDefaultValue());
						}
					}
				});
			}
			iParameters = new ArrayList<Parameter>();
			if (response.hasParameters()) {
				for (SolverParameter parameter: response.getParameters()) {
					Parameter p = null;
					if ("boolean".equalsIgnoreCase(parameter.getType())) {
						p = new BooleanParameter(parameter);
					} else if ("double".equalsIgnoreCase(parameter.getType())) {
						p = new DoubleParameter(parameter);
					} else if ("integer".equalsIgnoreCase(parameter.getType())) {
						p = new IntegerParameter(parameter);
					} else if (parameter.getType() != null && parameter.getType().toLowerCase().startsWith("enum(") && parameter.getType().endsWith(")")) {
						p = new EnumParameter(parameter);
					} else {
						p = new TextParameter(parameter);
					}
					iParameters.add(p);
					addRow(parameter.getName() + ":", (Widget)p); 
				}
			}
			if (iType != SolverType.EXAM && iSolverOwner != null)
				addRow(MESSAGES.propSolverOwner(), iSolverOwner);			
			if (response.hasHosts()) {
				iSolverHost = new ListBox();
				for (String host: response.getHosts())
					iSolverHost.addItem(host);
				addRow(MESSAGES.propSolverHost(), iSolverHost);
			}
			iButtonsRow = addBottomRow(iSolverButtons);
		}
		if (response.getLoadDate() == null) {
			getRowFormatter().setVisible(iLoadDateRow, false);
		} else {
			getRowFormatter().setVisible(iLoadDateRow, true);
			iLoadDate.setText(sLoadDateFormat.format(response.getLoadDate()));
		}
		if (response.getSolverStatus() == null) {
			getRowFormatter().setVisible(iSolverStatusRow, false);
		} else {
			getRowFormatter().setVisible(iSolverStatusRow, true);
			iSolverStatus.setStatus(response.getSolverStatus());
		}
		if (response.getSolverProgress() == null) {
			getRowFormatter().setVisible(iSolverProgressRow, false);
		} else {
			getRowFormatter().setVisible(iSolverProgressRow, true);
			iSolverProgress.setText(response.getSolverProgress());
		}
		if (iSolverConfig.getItemCount() == 0) {
			getRowFormatter().setVisible(iSolverConfigRow, false);
		} else {
			getRowFormatter().setVisible(iSolverConfigRow, true);
			iSolverConfig.setEnabled(!response.isWorking());
			if (response.isRefresh()) {
				if (response.getConfigurationId() != null) {
					for (int i = 0; i < iSolverConfig.getItemCount(); i++)
						if (iSolverConfig.getValue(i).equals(response.getConfigurationId().toString())) {
							iSolverConfig.setSelectedIndex(i); break;						
						}
				} else {
					iSolverConfig.setSelectedIndex(0);
				}
			}
		}
		for (int i = 0; i < iParameters.size(); i++) {
			Parameter p = iParameters.get(i);
			p.setEnabled(!response.isWorking());
			if (response.isRefresh()) {
				SolverParameter parameter = response.getParameter(p.getParameterId());
				p.setParameterValue(parameter != null ? (parameter.getValue() != null ? parameter.getValue() : parameter.getDefaultValue()) : p.getParameterDefaultValue());
			}
		}
		if (iSolverOwner != null) {
			iSolverOwner.setEnabled(!response.isWorking() && response.getLoadDate() == null);
			if (response.isRefresh()) {
				if (iSolverOwner.isMultipleSelect()) {
					for (int i = 0; i < iSolverOwner.getItemCount(); i++) {
						iSolverOwner.setItemSelected(i, response.hasOwerIds() && response.getOwnerIds().contains(Long.valueOf(iSolverOwner.getValue(i))));
					}
				} else {
					for (int i = 0; i < iSolverOwner.getItemCount(); i++) {
						if (response.hasOwerIds() && response.getOwnerIds().contains(Long.valueOf(iSolverOwner.getValue(i)))) {
							iSolverOwner.setSelectedIndex(i); break;
						}
					}
				}
			}
		}
		if (iSolverHost != null) {
			iSolverHost.setEnabled(!response.isWorking() && response.getLoadDate() == null);
			if (response.isRefresh()) {
				for (int i = 0; i < iSolverHost.getItemCount(); i++) {
					if (iSolverHost.getValue(i).equals(response.getHost())) {
						iSolverHost.setSelectedIndex(i); break;
					}
				}
			}
		}
		iSolverButtons.setEnabled("load", response.canExecute(SolverOperation.LOAD));
		iSolverButtons.setEnabled("start", response.canExecute(SolverOperation.START));
		iSolverButtons.setEnabled("stop", response.canExecute(SolverOperation.STOP));
		iSolverButtons.setEnabled("sectioning", response.canExecute(SolverOperation.STUDENT_SECTIONING));
		iSolverButtons.setEnabled("reload", response.canExecute(SolverOperation.RELOAD));
		iSolverButtons.setEnabled("save", iType != SolverType.COURSE && response.canExecute(SolverOperation.SAVE));
		iSolverButtons.setEnabled("save new", iType != SolverType.COURSE && response.canExecute(SolverOperation.SAVE_AS_NEW));
		iSolverButtons.setEnabled("clear", response.canExecute(SolverOperation.CLEAR));
		iSolverButtons.setEnabled("csv", response.canExecute(SolverOperation.EXPORT_CSV));
		iSolverButtons.setEnabled("unload", response.canExecute(SolverOperation.UNLOAD));
		iSolverButtons.setEnabled("refresh", response.canExecute(SolverOperation.CHECK));
		for (int row = getRowCount() - 1; row > iButtonsRow; row--)
			removeRow(row);
		if (response.hasBestSolution()) {
			addHeaderRow(iType == SolverType.STUDENT ? MESSAGES.sectStudentSolverBestSolution() : MESSAGES.sectSolverBestSolution());
			for (InfoPair pair: response.getBestSolution().getPairs())
				addRow(new HTML(pair.getName()), new HTML(pair.getValue()));
			if (iType == SolverType.COURSE && (response.canExecute(SolverOperation.SAVE) || response.canExecute(SolverOperation.SAVE_AS_NEW))) {
				UniTimeHeaderPanel buttons = new UniTimeHeaderPanel();
				if (response.canExecute(SolverOperation.SAVE))
					buttons.addButton("save", MESSAGES.opSolverSave(), createClickHandler(SolverOperation.SAVE));
				if (response.canExecute(SolverOperation.SAVE_AS_NEW))
					buttons.addButton("save new", MESSAGES.opSolverSaveAsNew(), createClickHandler(SolverOperation.SAVE_AS_NEW));
				if (response.canExecute(SolverOperation.SAVE_COMMIT))
					buttons.addButton("commit", MESSAGES.opSolverSaveCommit(), createClickHandler(SolverOperation.SAVE_COMMIT));
				if (response.canExecute(SolverOperation.SAVE_AS_NEW_COMMIT))
					buttons.addButton("new commit", MESSAGES.opSolverSaveAsNewCommit(), createClickHandler(SolverOperation.SAVE_AS_NEW_COMMIT));
				addBottomRow(buttons);
			}
		}
		if (response.hasCurrentSolution()) {
			addHeaderRow(iType == SolverType.STUDENT ? MESSAGES.sectStudentSolverCurrentSolution() : MESSAGES.sectSolverCurrentSolution());
			for (InfoPair pair: response.getCurrentSolution().getPairs())
				addRow(new HTML(pair.getName()), new HTML(pair.getValue()));
			if (response.canExecute(SolverOperation.SAVE_BEST) || response.canExecute(SolverOperation.RESTORE_BEST) || response.canExecute(SolverOperation.EXPORT_XML)) {
				UniTimeHeaderPanel buttons = new UniTimeHeaderPanel();
				if (response.canExecute(SolverOperation.SAVE_BEST))
					buttons.addButton("save best", MESSAGES.opSolverSaveBest(), createClickHandler(SolverOperation.SAVE_BEST));
				if (response.canExecute(SolverOperation.RESTORE_BEST))
					buttons.addButton("restore best", MESSAGES.opSolverRestorBest(), createClickHandler(SolverOperation.RESTORE_BEST));
				if (response.canExecute(SolverOperation.EXPORT_XML))
					buttons.addButton("xml", MESSAGES.opSolverExportXML(), createClickHandler(SolverOperation.EXPORT_XML));
				addBottomRow(buttons);
			}
		}
		if (response.hasLog()) {
			addHeaderRow(MESSAGES.sectSolverWarnings());
			addRow(new HTML(response.getLog()));
		}
		if (response.hasSelectedSolutions()) {
			for (SolutionInfo selected: response.getSelectedSolutions()) {
				addHeaderRow(MESSAGES.sectSolverSelectedSolution(selected.getName()));
				for (InfoPair pair: selected.getPairs())
					addRow(new HTML(pair.getName()), new HTML(pair.getValue()));
				if (selected.hasLog()) {
					addHeaderRow(MESSAGES.sectSolverSelectedWarnings(selected.getName()));
					addRow(new HTML(selected.getLog()));
				}
			}
		}
		
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
								if (pm.hasUrl() && !pm.getUrl().startsWith("gwt.jsp?page=solver"))
									ToolBox.open(GWT.getHostPageBaseURL() + pm.getUrl());
								else
									execute(SolverOperation.CHECK);
							}
						});
					}
					cpm.add(p);
				}
			}
		}
	}
	
	@Override
	public int addHeaderRow(String text) {
		int row = super.addHeaderRow(text);
		getRowFormatter().addStyleName(row, "row-above");
		return row;
	}
	
	@Override
	public int addBottomRow(Widget widget) {
		int row = super.addBottomRow(widget, false);
		getFlexCellFormatter().removeStyleName(row, 0, "unitime-TopLine");
		return row;
	}
	
	protected static class SolverStatus extends P {
		private P iStatus;
		private Image iIcon;
		
		public SolverStatus() {
			super("unitime-SolverStatus");
			iStatus = new P("status-label");
			iIcon = new Image(RESOURCES.helpIcon()); iIcon.addStyleName("status-icon");
			iIcon.setVisible(false);
			add(iStatus); add(iIcon);
			RPC.execute(new PageNameRpcRequest("Solver Status"), new AsyncCallback<PageNameInterface>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(final PageNameInterface result) {
					iIcon.setTitle(MESSAGES.pageHelp(result.getName()));
					iIcon.setVisible(true);
					iIcon.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (result.getHelpUrl() == null || result.getHelpUrl().isEmpty()) return;
							UniTimeFrameDialog.openDialog(MESSAGES.pageHelp(result.getName()), result.getHelpUrl());
						}
					});
				}
			});
		}
		
		public void setStatus(String status) {
			iStatus.setText(status);
		}
	}
	
	protected static interface Parameter extends HasEnabled {
		public Long getParameterId();
		public String getParameterValue();
		public String getParameterDefaultValue();
		public void setParameterValue(String value);
	}
	
	public static class TextParameter extends TextBox implements Parameter {
		private SolverParameter iParameter;
		
		public TextParameter(SolverParameter parameter) {
			iParameter = parameter;
			setStyleName("unitime-TextBox");
			addStyleName("parameter-text");
			setParameterValue(parameter.getValue() != null ? parameter.getValue() : parameter.getDefaultValue());
		}

		@Override
		public Long getParameterId() {
			return iParameter.getId();
		}

		@Override
		public String getParameterValue() {
			return getText();
		}

		@Override
		public void setParameterValue(String value) {
			setText(value == null ? "" : value);
		}

		@Override
		public String getParameterDefaultValue() {
			return iParameter.getDefaultValue();
		}
	}
	
	public static class IntegerParameter extends NumberBox implements Parameter {
		private SolverParameter iParameter;
		
		public IntegerParameter(SolverParameter parameter) {
			iParameter = parameter;
			addStyleName("parameter-number");
			setDecimal(false); setNegative(true);
			setParameterValue(parameter.getValue() != null ? parameter.getValue() : parameter.getDefaultValue());
		}

		@Override
		public Long getParameterId() {
			return iParameter.getId();
		}

		@Override
		public String getParameterValue() {
			return getText();
		}

		@Override
		public void setParameterValue(String value) {
			setText(value == null ? "" : value);
		}

		@Override
		public String getParameterDefaultValue() {
			return iParameter.getDefaultValue();
		}
	}
	
	public static class DoubleParameter extends NumberBox implements Parameter {
		private SolverParameter iParameter;
		
		public DoubleParameter(SolverParameter parameter) {
			iParameter = parameter;
			addStyleName("parameter-number");
			setDecimal(true); setNegative(true);
			setParameterValue(parameter.getValue() != null ? parameter.getValue() : parameter.getDefaultValue());
		}

		@Override
		public Long getParameterId() {
			return iParameter.getId();
		}

		@Override
		public String getParameterValue() {
			return getText();
		}

		@Override
		public void setParameterValue(String value) {
			setText(value == null ? "" : value);
		}

		@Override
		public String getParameterDefaultValue() {
			return iParameter.getDefaultValue();
		}
	}
	
	public static class BooleanParameter extends CheckBox implements Parameter {
		private SolverParameter iParameter;
		
		public BooleanParameter(SolverParameter parameter) {
			iParameter = parameter;
			addStyleName("parameter-text");
			setParameterValue(parameter.getValue() != null ? parameter.getValue() : parameter.getDefaultValue());
		}

		@Override
		public Long getParameterId() {
			return iParameter.getId();
		}

		@Override
		public String getParameterValue() {
			return super.getValue() ? "true" : "false"; 
		}

		@Override
		public void setParameterValue(String value) {
			setValue("true".equalsIgnoreCase(value));
		}

		@Override
		public String getParameterDefaultValue() {
			return iParameter.getDefaultValue();
		}
	}
	
	public static class EnumParameter extends ListBox implements Parameter {
		private SolverParameter iParameter;
		
		public EnumParameter(SolverParameter parameter) {
			iParameter = parameter;
			addStyleName("parameter-list");
			for (String item: parameter.getType().substring(parameter.getType().indexOf('(') + 1, parameter.getType().lastIndexOf(')')).split(","))
				addItem(item);
			setParameterValue(parameter.getValue() != null ? parameter.getValue() : parameter.getDefaultValue());
		}

		@Override
		public Long getParameterId() {
			return iParameter.getId();
		}

		@Override
		public String getParameterValue() {
			if (getSelectedIndex() < 0) return null;
			return getValue(getSelectedIndex());
		}

		@Override
		public void setParameterValue(String value) {
			for (int i = 0; i < getItemCount(); i++) {
				if (getValue(i).equals(value)) {
					setSelectedIndex(i); break;
				}
			}
		}

		@Override
		public String getParameterDefaultValue() {
			return iParameter.getDefaultValue();
		}
	}
	
}
