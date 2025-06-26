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
package org.unitime.timetable.gwt.client.offerings;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.events.SessionDatesSelector;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.SubpartEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.SubpartEditResponse;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SearchableListBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;

public class SubpartEditPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private SubpartEditResponse iData;
	private CreditTable iCredits;
	
	private ListBox iInstructionalType;
	private ListBox iDatePattern;
	
	private PreferenceEditWidget iPreferences;
		
	public SubpartEditPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ClassEditPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("ssuid");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoSubpartId());
		} else {
			load(Long.valueOf(id), Operation.GET, true, null);
		}
		
		iHeader.addButton("update", COURSE.actionUpdatePreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (validate()) {
					load(iData.getId(), Operation.UPDATE, true, null);
				}
			}
		});
		iHeader.getButton("update").setTitle(COURSE.titleUpdatePreferences(COURSE.accessUpdatePreferences()));
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdatePreferences().charAt(0));
		iHeader.setEnabled("update", false);

		iHeader.addButton("clear", COURSE.actionClearSubpartPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (iCredits != null) iCredits.update(iData);
				load(iData.getId(), Operation.CLEAR_CLASS_PREFS, true, null);
			}
		});
		iHeader.getButton("clear").setTitle(COURSE.titleClearSubpartPreferences(COURSE.accessClearSubpartPreferences()));
		iHeader.getButton("clear").setAccessKey(COURSE.accessClearSubpartPreferences().charAt(0));
		iHeader.setEnabled("clear", false);
		
		iHeader.addButton("previous", COURSE.actionPreviousSubpart(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (validate()) {
					load(iData.getId(), Operation.PREVIOUS, true, null);
				}
			}
		});
		iHeader.getButton("previous").setTitle(COURSE.titlePreviousSubpartWithUpdate(COURSE.accessPreviousSubpart()));
		iHeader.getButton("previous").setAccessKey(COURSE.accessPreviousSubpart().charAt(0));
		iHeader.setEnabled("previous", false);
		
		iHeader.addButton("next", COURSE.actionNextSubpart(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPreferences.update();		
				if (validate()) {
					load(iData.getId(), Operation.NEXT, true, null);
				}
			}
		});
		iHeader.getButton("next").setTitle(COURSE.titleNextSubpartWithUpdate(COURSE.accessNextSubpart()));
		iHeader.getButton("next").setAccessKey(COURSE.accessNextSubpart().charAt(0));
		iHeader.setEnabled("next", false);
		
		iHeader.addButton("back", COURSE.actionBackToDetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "subpart?id=" + iData.getId());
			}
		});
		iHeader.getButton("back").setTitle(COURSE.titleBackToDetail(COURSE.accessBackToDetail()));
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToDetail().charAt(0));
		
		iFooter = iHeader.clonePanel();
	}
	
	protected void load(Long subpartId, final Operation op, final boolean showLoading, final Command command) {
		if (showLoading) LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		SubpartEditRequest req = new SubpartEditRequest();
		req.setOperation(op);
		if (op != null && iData != null) {
			iPreferences.update();
			if (iCredits != null) iCredits.update(iData);
			req.setPayLoad(iData);
		}
		req.setId(subpartId);
		RPC.execute(req, new AsyncCallback<SubpartEditResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				if (showLoading) LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final SubpartEditResponse response) {
				iData = response;
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				if (showLoading) LoadingWidget.getInstance().hide();
				
				if (op == Operation.DATE_PATTERN) {
					iPreferences.setValue(response);
					return;
				}
				
				iPanel.clear();
				iHeader.setHeaderTitle(response.getName());
				iPanel.addHeaderRow(iHeader);
				for (PropertyInterface property: response.getProperties().getProperties())
					iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				
				iInstructionalType = new ListBox();
				boolean ext = false;
				if (response.hasExtInstructionalTypes())
					for (IdLabel itype: response.getExtInstructionalTypes()) {
						if (itype.getId().equals(response.getInstructionalTypeId())) {
							ext = true;
							break;
						}
					}
				if (ext) {
					for (IdLabel itype: response.getExtInstructionalTypes()) {
						iInstructionalType.addItem(itype.getLabel(), itype.getId().toString());
						if (itype.getId().equals(response.getInstructionalTypeId())) {
							iInstructionalType.setSelectedIndex(iInstructionalType.getItemCount() - 1);
						}
					}
					if (response.hasInstructionalTypes())
						iInstructionalType.addItem(COURSE.selectLessOptions(), "less");
				} else {
					for (IdLabel itype: response.getInstructionalTypes()) {
						iInstructionalType.addItem(itype.getLabel(), itype.getId().toString());
						if (itype.getId().equals(response.getInstructionalTypeId())) {
							iInstructionalType.setSelectedIndex(iInstructionalType.getItemCount() - 1);
						}
					}
					if (response.hasExtInstructionalTypes())
						iInstructionalType.addItem(COURSE.selectMoreOptions(), "more");
				}
				iPanel.addRow(COURSE.filterInstructionalType(), iInstructionalType);
				iInstructionalType.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent e) {
						if ("less".equals(iInstructionalType.getSelectedValue())) {
							iInstructionalType.clear();
							for (IdLabel itype: response.getInstructionalTypes()) {
								iInstructionalType.addItem(itype.getLabel(), itype.getId().toString());
							}
							iInstructionalType.addItem(COURSE.selectMoreOptions(), "more");
							iInstructionalType.setSelectedIndex(0);
						} else if ("more".equals(iInstructionalType.getSelectedValue())) {
							iInstructionalType.clear();
							for (IdLabel itype: response.getExtInstructionalTypes()) {
								iInstructionalType.addItem(itype.getLabel(), itype.getId().toString());
							}
							iInstructionalType.addItem(COURSE.selectLessOptions(), "less");
							iInstructionalType.setSelectedIndex(0);
						}
						iData.setInstructionalTypeId(Long.valueOf(iInstructionalType.getSelectedValue()));
					}
				});
				
				if (response.hasDatePatterms()) {
					iDatePattern = new ListBox();
					for (IdLabel dp: response.getDatePatterns()) {
						iDatePattern.addItem(dp.getLabel(), dp.getId().toString());
						if (dp.getId().equals(response.getDatePatternId()))
							iDatePattern.setSelectedIndex(iDatePattern.getItemCount() - 1);
					}
					final P datePatternPanel = new P("date-pattern");
					if (response.isSearchableDatePattern()) {
						datePatternPanel.add(new SearchableListBox(iDatePattern));
					} else {
						datePatternPanel.add(iDatePattern);
					}
					final ImageButton cal = new ImageButton(RESOURCES.datepattern());
					iDatePattern.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							Long id = Long.valueOf(iDatePattern.getSelectedValue());
							iData.setDatePatternId(id);
							IdLabel dp = response.getDatePattern(id);
							cal.setVisible(dp != null && dp.getDescription() != null);
							load(subpartId, Operation.DATE_PATTERN, false, null);
						}
					});
					cal.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Long id = Long.valueOf(iDatePattern.getSelectedValue());
							IdLabel dp = response.getDatePattern(id);
							if (dp != null && dp.getDescription() != null) {
								final UniTimeDialogBox box = new UniTimeDialogBox(true, true);
								SessionDatesSelector w = new SessionDatesSelector().forDatePattern(response.getDatePattern(id).getDescription(),
										new Command() {
									@Override
									public void execute() {
										box.center();
									}
								});
								w.getElement().getStyle().setProperty("width", "80vw");
								box.setWidget(w);
								box.setText(COURSE.sectPreviewOfDatePattern(iDatePattern.getSelectedItemText()));
							}
						}
					});
					datePatternPanel.add(cal);
					iPanel.addRow(COURSE.propertyDatePattern(), datePatternPanel);
				}
				
				CheckBox autoSpread = new CheckBox(COURSE.descriptionAutomaticSpreadInTime());
				autoSpread.setValue(response.isAutoSpreadInTime());
				autoSpread.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
				iPanel.addRow(COURSE.propertyAutomaticSpreadInTime(), autoSpread);
				autoSpread.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iData.setAutoSpreadInTime(event.getValue());
					}
				});
				
				CheckBox canOverlap = new CheckBox(COURSE.descriptionStudentOverlaps());
				canOverlap.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
				canOverlap.setValue(response.isStudentsCanOverlap());
				iPanel.addRow(COURSE.propertyStudentOverlaps(), canOverlap);
				canOverlap.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iData.setStudentsCanOverlap(event.getValue());
					}
				});
				
				if (response.hasCreditFormats()) {
					iCredits = new CreditTable(response);
					iPanel.addRow(COURSE.propertySubpartCredit(), iCredits);
				} else {
					iCredits = null;
				}
				
				iPreferences = new PreferenceEditWidget();
				iPreferences.setValue(response);
				iPanel.addRow(iPreferences);
				
				iPanel.addHeaderRow(iFooter);
				
				UniTimeNavigation.getInstance().refresh();
				
				iHeader.setEnabled("update", true);
				iHeader.setEnabled("previous", response.getPreviousId() != null);
				iHeader.setEnabled("next", response.getNextId() != null);
				iHeader.setEnabled("clear", response.canClearPrefs());
				
				if (command != null)
					command.execute();
			}
		});
	}
	
	public boolean validate() {
		iHeader.clearMessage();

		String error = iPreferences.validate();
		if (error != null) {
			iHeader.setErrorMessage(error);
			return false;
		}
		return true;
	}
	
	public static class CreditTable extends SimpleForm {
		ListBox iFormat, iType, iUnitType;
		NumberBox iUnits, iMaxUnits;
		CheckBox iFractions;
		
		public CreditTable(SubpartEditResponse response) {
			removeStyleName("unitime-NotPrintableBottomLine");
			iFormat = new ListBox();
			iFormat.addItem(MESSAGES.itemSelect(), "");
			if (response.hasCreditFormats()) {
				for (IdLabel item: response.getCreditFormats()) {
					iFormat.addItem(item.getLabel(), item.getDescription());
					if (item.getId().equals(response.getCreditFormatId()))
						iFormat.setSelectedIndex(iFormat.getItemCount() - 1);
				}
			}
			iFormat.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					formatChanged();
				}
			});
			addRow(iFormat);
			iType = new ListBox();
			if (response.hasCreditTypes())
				for (IdLabel item: response.getCreditTypes()) {
					iType.addItem(item.getLabel(), item.getId().toString());
					if (item.getId().equals(response.getCreditTypeId()))
						iType.setSelectedIndex(iType.getItemCount() - 1);
				}
			addRow(MESSAGES.propCreditType(), iType);
			iUnitType = new ListBox();
			if (response.hasCreditUnitTypes())
				for (IdLabel item: response.getCreditUnitTypes()) {
					iUnitType.addItem(item.getLabel(), item.getId().toString());
					if (item.getId().equals(response.getCreditUnitTypeId()))
						iUnitType.setSelectedIndex(iUnitType.getItemCount() - 1);
				}
			addRow(MESSAGES.propCreditUnitType(), iUnitType);
			iUnits = new NumberBox(); iUnits.setDecimal(true); iUnits.setNegative(false);
			iUnits.setValue(response.getCreditUnits());
			addRow(MESSAGES.propUnits(), iUnits);
			iMaxUnits = new NumberBox(); iMaxUnits.setDecimal(true); iMaxUnits.setNegative(false);
			iMaxUnits.setValue(response.getCreditMaxUnits());
			addRow(MESSAGES.propMaxUnits(), iMaxUnits);
			iFractions = new CheckBox();
			iFractions.setValue(response.isCreditFractionsAllowed());
			addRow(MESSAGES.propMaxUnits(), iFractions);
			formatChanged();
		}
		
		protected void formatChanged() {
			String format = iFormat.getSelectedValue();
			if ("fixedUnit".equals(format)) {
				getRowFormatter().setVisible(1, true); // credit type
				getRowFormatter().setVisible(2, true); // credit unit type
				getRowFormatter().setVisible(3, true); // units
				getRowFormatter().setVisible(4, false); // max units
				getRowFormatter().setVisible(5, false); // fractional
			} else if ("arrangeHours".equals(format)) {
				getRowFormatter().setVisible(1, true); // credit type
				getRowFormatter().setVisible(2, true); // credit unit type
				getRowFormatter().setVisible(3, false); // units
				getRowFormatter().setVisible(4, false); // max units
				getRowFormatter().setVisible(5, false); // fractional
			} else if ("variableRange".equals(format)) {
				getRowFormatter().setVisible(1, true); // credit type
				getRowFormatter().setVisible(2, true); // credit unit type
				getRowFormatter().setVisible(3, true); // units
				getRowFormatter().setVisible(4, true); // max units
				getRowFormatter().setVisible(5, true); // fractional
			} else if ("variableMinMax".equals(format)) {
				getRowFormatter().setVisible(1, true); // credit type
				getRowFormatter().setVisible(2, true); // credit unit type
				getRowFormatter().setVisible(3, true); // units
				getRowFormatter().setVisible(4, true); // max units
				getRowFormatter().setVisible(5, false); // fractional
			} else {
				getRowFormatter().setVisible(1, false); // credit type
				getRowFormatter().setVisible(2, false); // credit unit type
				getRowFormatter().setVisible(3, false); // units
				getRowFormatter().setVisible(4, false); // max units
				getRowFormatter().setVisible(5, false); // fractional
			}
		}
		
		public void update(SubpartEditResponse response) {
			IdLabel format = response.getCreditFormat(iFormat.getSelectedValue());
			if (format == null) {
				response.setCreditFormatId(null);
				response.setCreditTypeId(null);
				response.setCreditUnitTypeId(null);
				response.setCreditUnits(null);
				response.setCreditMaxUnits(null);
				response.setCreditFractionsAllowed(false);
				return;
			} else if ("fixedUnit".equals(format.getDescription())) {
				response.setCreditFormatId(format.getId());
				response.setCreditTypeId(Long.valueOf(iType.getSelectedValue()));
				response.setCreditUnitTypeId(Long.valueOf(iUnitType.getSelectedValue()));
				response.setCreditUnits(iUnits.toFloat());
				response.setCreditMaxUnits(null);
				response.setCreditFractionsAllowed(false);
			} else if ("arrangeHours".equals(format.getDescription())) {
				response.setCreditFormatId(format.getId());
				response.setCreditTypeId(Long.valueOf(iType.getSelectedValue()));
				response.setCreditUnitTypeId(Long.valueOf(iUnitType.getSelectedValue()));
				response.setCreditUnits(null);
				response.setCreditMaxUnits(null);
				response.setCreditFractionsAllowed(false);
			} else if ("variableRange".equals(format.getDescription())) {
				response.setCreditFormatId(format.getId());
				response.setCreditTypeId(Long.valueOf(iType.getSelectedValue()));
				response.setCreditUnitTypeId(Long.valueOf(iUnitType.getSelectedValue()));
				response.setCreditUnits(iUnits.toFloat());
				response.setCreditMaxUnits(iMaxUnits.toFloat());
				response.setCreditFractionsAllowed(iFractions.getValue());
			} else if ("variableMinMax".equals(format.getDescription())) {
				response.setCreditFormatId(format.getId());
				response.setCreditTypeId(Long.valueOf(iType.getSelectedValue()));
				response.setCreditUnitTypeId(Long.valueOf(iUnitType.getSelectedValue()));
				response.setCreditUnits(iUnits.toFloat());
				response.setCreditMaxUnits(iMaxUnits.toFloat());
				response.setCreditFractionsAllowed(false);
			} else {
				response.setCreditFormatId(null);
				response.setCreditTypeId(null);
				response.setCreditUnitTypeId(null);
				response.setCreditUnits(null);
				response.setCreditMaxUnits(null);
				response.setCreditFractionsAllowed(false);
			}
		}
		
	}
}
