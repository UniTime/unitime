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
package org.unitime.timetable.gwt.client.curricula;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.UserDataInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;
import org.unitime.timetable.gwt.shared.UserDataInterface.GetUserDataRpcRequest;
import org.unitime.timetable.gwt.shared.UserDataInterface.SetUserDataRpcRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CurriculumProjectionRulesPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);
	private static NumberFormat NF = NumberFormat.getFormat("##0.0");

	private MyFlexTable iTable;
	
	private SimpleForm iPanel = null;
	private UniTimeHeaderPanel iHeader = null, iBottom = null;
	
	private boolean iEditable = false;
	
	private HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> iRules = null;
	private HashMap<String, String> iOrder = null;
	
	private List<ProjectionRulesHandler> iProjectionRulesHandlers = new ArrayList<ProjectionRulesHandler>();
	
	public CurriculumProjectionRulesPage() {
		
		iPanel = new SimpleForm();
		
		ClickHandler save = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitSavingCurriculumProjectionRules());
				iService.saveProjectionRules(iRules, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedToSaveCurriculumProjectionRules(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedToSaveCurriculumProjectionRules(caught.getMessage()), caught);
						LoadingWidget.getInstance().hide();
						for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
							h.onException(caught);
						}
					}
					@Override
					public void onSuccess(Boolean result) {
						iHeader.clearMessage();
						LoadingWidget.getInstance().hide();
						ProjectionRulesEvent e = new ProjectionRulesEvent();
						for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
							h.onRulesSaved(e);
						}
						if (!iHeader.isEnabled("close")) {
							iHeader.setEnabled("back", false);
							iHeader.setEnabled("save", false);
							iHeader.setEnabled("edit", true);
							iHeader.setEnabled("print", true);
							iEditable = false;
							updateAll();
						}
					}
				});
			}
		};
		
		ClickHandler close = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ProjectionRulesEvent e = new ProjectionRulesEvent();
				for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
					h.onRulesClosed(e);
				}
			}
		};
		
		ClickHandler print = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.print();
			}
		};
		
		ClickHandler edit = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEditable = true;
				updateAll();
				iHeader.setEnabled("back", true);
				iHeader.setEnabled("save", true);
				iHeader.setEnabled("edit", false);
				iHeader.setEnabled("print", false);
			}
		};

		ClickHandler back = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEditable = false;
				reload();
			}
		};

		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("edit", MESSAGES.buttonEdit(), 75, edit);
		iHeader.addButton("save", MESSAGES.buttonSave(), 75, save);
		iHeader.addButton("print", MESSAGES.buttonPrint(), 75, print);
		iHeader.addButton("close", MESSAGES.buttonClose(), 75, close);
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, back);
				
		iPanel.addHeaderRow(iHeader);
		
		iTable = new MyFlexTable();
		iTable.setVisible(false);
		
		ScrollPanel tableScroll = new ScrollPanel(iTable);
		tableScroll.addStyleName("unitime-ScrollTable");
		iPanel.addRow(tableScroll);
		
		iBottom = iHeader.clonePanel();
		iPanel.addNotPrintableBottomRow(iBottom);

		initWidget(iPanel);
		
		iHeader.setEnabled("close", false);

		reload();
	}
	
	public void reload() {
		iHeader.setEnabled("save", false);
		iHeader.setEnabled("edit", false);
		iHeader.setEnabled("back", false);
		iHeader.setEnabled("print", false);

		LoadingWidget.getInstance().show(MESSAGES.waitLoadingCurriculumProjectionRules());
		iService.loadProjectionRules(new AsyncCallback<HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>>>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToLoadCurriculumProjectionRules(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadCurriculumProjectionRules(caught.getMessage()), caught);
				LoadingWidget.getInstance().hide();
				for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
					h.onException(caught);
				}
				ToolBox.checkAccess(caught);
			}
			@Override
			public void onSuccess(HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> result) {
				iRules = result;
				GetUserDataRpcRequest ordRequest = new GetUserDataRpcRequest();
				ordRequest.add("CurProjRules.Order");
				for (AcademicAreaInterface area: iRules.keySet())
					ordRequest.add("CurProjRules.Order["+area.getAbbv()+"]");
				iHeader.clearMessage();
				RPC.execute(ordRequest, new AsyncCallback<UserDataInterface>() {
					@Override
					public void onSuccess(UserDataInterface result) {
						iOrder = result;
						refreshTableAndAll();
					}
					@Override
					public void onFailure(Throwable caught) {
						refreshTableAndAll();
					}
				});
			}
		});
	}
	
	private void refreshTableAndAll() {
		try {
			refreshTable();
			
			iService.canEditProjectionRules(new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
				}
				@Override
				public void onSuccess(Boolean result) {
					if (result) {
						if (iHeader.isEnabled("close")) {
							iHeader.setEnabled("save", true);
							iEditable = true;
							updateAll();
						} else {
							iHeader.setEnabled("edit", true);
						}
					}
				}
			});
			
			ProjectionRulesEvent e = new ProjectionRulesEvent();
			for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
				h.onRulesLoaded(e);
			}
			
		} catch (Throwable t) {
			iHeader.setErrorMessage(MESSAGES.failedToLoadCurriculumProjectionRules(t.getMessage()));
			UniTimeNotifications.error(MESSAGES.failedToLoadCurriculumProjectionRules(t.getMessage()), t);
			for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
				h.onException(t);
			}
		} finally {
			LoadingWidget.getInstance().hide();
		}
	}
	
	public void setAllowClose(boolean allow) {
		iHeader.setEnabled("close", allow);
	}

	private boolean isUsed(AcademicClassificationInterface c) {
		MajorInterface defaultMajor = new MajorInterface();
		defaultMajor.setId(-1l);

		for (Map.Entry<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> e: iRules.entrySet())
			if (e.getValue().get(defaultMajor).get(c)[1].intValue() > 0) return true;
		
		return true;
	}

	
	private boolean canCombine(AcademicClassificationInterface c1, Set<AcademicClassificationInterface> s2) {
		MajorInterface defaultMajor = new MajorInterface();
		defaultMajor.setId(-1l);

		for (Map.Entry<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> e: iRules.entrySet()) {
			if (e.getValue().get(defaultMajor).get(c1)[1].intValue() <= 0) continue;
			for (AcademicClassificationInterface c2: s2) {
				if (e.getValue().get(defaultMajor).get(c2)[1].intValue() > 0) return false;
			}
		}
		
		return true;
	}
	
	public void updateAll() {
		for (int row = 1; row < iTable.getRowCount(); row++) {
			for (int col = 1; col < iTable.getCellCount(row); col++) {
				Widget w = iTable.getWidget(row, col);
				if (w != null) ((Updatable)w).update();
			}
		}
	}
	
	public void refreshTable() throws CurriculaException {
		for (int row = iTable.getRowCount() - 1; row >= 0; row--)
			iTable.removeRow(row);

		if (iRules == null || iRules.isEmpty())
			throw new CurriculaException(MESSAGES.errorNoAcademicAreasDefined());

		String areaOrd = (iOrder == null ? null : iOrder.get("CurProjRules.Order"));
		TreeSet<AcademicAreaInterface> areas = null;
		if (areaOrd != null && areaOrd.length() > 0) {
			final String ord = "|" + areaOrd + "|";
			areas = new TreeSet<AcademicAreaInterface>(new Comparator<AcademicAreaInterface>() {
				@Override
				public int compare(AcademicAreaInterface a1, AcademicAreaInterface a2) {
					int i1 = ord.indexOf("|" + a1.getAbbv() + "|");
					if (i1 >= 0) {
						int i2 = ord.indexOf("|" + a2.getAbbv() + "|");
						if (i2 >= 0) {
							return (i1 < i2 ? -1 : i1 > i2 ? 1 : a1.compareTo(a2));
						}
					}
					return a1.compareTo(a2);
				}
			});
			areas.addAll(iRules.keySet());
		} else {
			areas = new TreeSet<AcademicAreaInterface>(iRules.keySet());
		}
		TreeSet<AcademicClassificationInterface> classifications = null;
		
		MajorInterface defaultMajor = new MajorInterface();
		defaultMajor.setId(-1l);
		
		List<Set<AcademicClassificationInterface>> col2clasf = new ArrayList<Set<AcademicClassificationInterface>>();
		HashMap<AcademicClassificationInterface, Integer> clasf2col = new HashMap<AcademicClassificationInterface, Integer>();
		HashMap<Integer, List<MyCell>> col2cells = new HashMap<Integer, List<MyCell>>();
		
		int row = 1;
		for (AcademicAreaInterface area: areas) {
			HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>> rules = iRules.get(area);
			
			MyRow rr = new MyRow(area, null, rules.get(defaultMajor));
			if (classifications == null) {
				classifications = rr.getClassifications();
				for (AcademicClassificationInterface clasf: classifications) {
					if (!isUsed(clasf)) continue;
					Integer col = clasf2col.get(clasf);
					if (col == null) {
						for (int c = 0; c < col2clasf.size(); c++) {
							if (canCombine(clasf, col2clasf.get(c))) {
								col2clasf.get(c).add(clasf); 
								clasf2col.put(clasf, c);
								col = c;
								break;
							}
						}
					}
					if (col == null) {
						Set<AcademicClassificationInterface> s = new TreeSet<AcademicClassificationInterface>();
						s.add(clasf);
						col = col2clasf.size();
						col2clasf.add(s);
						clasf2col.put(clasf, col);
					}
				}
			}
			
			if (!rr.hasLastLike()) continue;
			
			iTable.setText(row, 0, area.getAbbv());
			List<MyCell> cells = new ArrayList<MyCell>();
			for (AcademicClassificationInterface clasf: classifications) {
				if (rr.getLastLike(clasf) <= 0) continue;
				Integer col = clasf2col.get(clasf);
				MyCell cell = new MyCell(rr, clasf);
				iTable.setWidget(row, 1 + col, cell);
				cells.add(cell);
				List<MyCell> cellsThisCol = col2cells.get(col);
				if (cellsThisCol == null) {
					cellsThisCol = new ArrayList<MyCell>();
					col2cells.put(col, cellsThisCol);
				}
				cellsThisCol.add(cell);
			}
			iTable.setWidget(row, 1 + col2clasf.size(), new MySumCell(cells, false));
			iTable.getCellFormatter().getElement(row, 1 + col2clasf.size()).getStyle().setBackgroundColor("#EEEEEE");
			row ++;
			
			String majorOrd = (iOrder == null ? null : iOrder.get("CurProjRules.Order["+area.getAbbv()+"]"));
			TreeSet<MajorInterface> majors = null;
			if (majorOrd != null && majorOrd.length() > 0) {
				final String ord = "|" + majorOrd + "|";
				majors = new TreeSet<MajorInterface>(new Comparator<MajorInterface>() {
					@Override
					public int compare(MajorInterface m1, MajorInterface m2) {
						int i1 = ord.indexOf("|" + m1.getCode() + "|");
						if (i1 >= 0) {
							int i2 = ord.indexOf("|" + m2.getCode() + "|");
							if (i2 >= 0) {
								return (i1 < i2 ? -1 : i1 > i2 ? 1 : m1.compareTo(m2));
							}
						}
						return m1.compareTo(m2);
					}
				});
				majors.addAll(iRules.get(area).keySet());
			} else {
				majors = new TreeSet<MajorInterface>(iRules.get(area).keySet());
			}
			
			for (MajorInterface major: majors) {
				if (major.getId() < 0) continue;
				
				MyRow r = new MyRow(area, major, rules.get(major));
				if (!r.hasLastLike()) continue;
				r.setParent(rr); rr.addChild(r);
				
				Label majorLabel = new Label(major.getCode(), false);
				majorLabel.getElement().getStyle().setMarginLeft(10, Unit.PX);
				iTable.setWidget(row, 0, majorLabel);
				List<MyCell> mcells = new ArrayList<MyCell>();
				for (AcademicClassificationInterface clasf: classifications) {
					if (r.getLastLike(clasf) <= 0) continue;
					Integer col = clasf2col.get(clasf);
					MyCell cell = new MyCell(r, clasf);
					mcells.add(cell);
					iTable.setWidget(row, 1 + col, cell);
					List<MyCell> cellsThisCol = col2cells.get(col);
					if (cellsThisCol == null) {
						cellsThisCol = new ArrayList<MyCell>();
						col2cells.put(col, cellsThisCol);
					}
					cellsThisCol.add(cell);
				}
				iTable.setWidget(row, 1 + col2clasf.size(), new MySumCell(mcells, false));
				iTable.getRowFormatter().setVisible(row, r.hasProjection());
				iTable.getCellFormatter().getElement(row, 1 + col2clasf.size()).getStyle().setBackgroundColor("#EEEEEE");
				row ++;
			}
		}
		if (classifications == null || classifications.isEmpty())
			throw new CurriculaException(MESSAGES.errorNoAcademicClassificationsDefined());
		
		ClickHandler menu = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				if (CurriculumCookie.getInstance().getCurriculumProjectionRulesPercent())
					menu.addItem(new MenuItem(MESSAGES.opShowNumbers(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							CurriculumCookie.getInstance().setCurriculumProjectionRulesPercent(false);
							updateAll();
						}
					}));
				else
					menu.addItem(new MenuItem(MESSAGES.opShowPercentages(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							CurriculumCookie.getInstance().setCurriculumProjectionRulesPercent(true);
							updateAll();
						}
					}));
				if (CurriculumCookie.getInstance().getCurriculumProjectionRulesShowLastLike())
					menu.addItem(new MenuItem(MESSAGES.opHide(MESSAGES.fieldLastLikeEnrollment()), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							CurriculumCookie.getInstance().setCurriculumProjectionRulesShowLastLike(false);
							updateAll();
						}
					}));
				else
					menu.addItem(new MenuItem(MESSAGES.opShow(MESSAGES.fieldLastLikeEnrollment()), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							CurriculumCookie.getInstance().setCurriculumProjectionRulesShowLastLike(true);
							updateAll();
						}
					}));
				boolean canCollapse = false, canExpand = false;
				for (int row = 1; row < iTable.getRowCount(); row++) {
					MyRow r = iTable.getMyRow(row);
					if (r != null && r.getMajor() != null && !r.hasProjection()) {
						if (iTable.getRowFormatter().isVisible(row))
							canCollapse = true;
						else
							canExpand = true;
					}
				}
				if (canCollapse) {
					menu.addItem(new MenuItem(MESSAGES.opCollapseAll(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							for (int row = 1; row < iTable.getRowCount(); row++) {
								MyRow r = iTable.getMyRow(row);
								if (r != null && r.getMajor() != null && !r.hasProjection()) {
									if (iTable.getRowFormatter().isVisible(row))
										iTable.getRowFormatter().setVisible(row, false);
								}
							}
						}
					}));
				}
				if (canExpand) {
					menu.addItem(new MenuItem(MESSAGES.opExpandAll(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							for (int row = 1; row < iTable.getRowCount(); row++) {
								MyRow r = iTable.getMyRow(row);
								if (r != null && r.getMajor() != null && !r.hasProjection()) {
									if (!iTable.getRowFormatter().isVisible(row))
										iTable.getRowFormatter().setVisible(row, true);
								}
							}
						}
					}));
				}
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		};
		
		for (int c = 0; c < col2clasf.size(); c++) {
			String text = "";
			for (AcademicClassificationInterface clasf: col2clasf.get(c)) {
				if (!text.isEmpty()) text += " / ";
				text += clasf.getCode();
			}
			Label label = new Label(text, true);
			label.addClickHandler(menu);
			iTable.getFlexCellFormatter().setStyleName(0, c + 1, "unitime-ClickableTableHeader");
			iTable.setWidget(0, c + 1, label);
			iTable.getCellFormatter().setHorizontalAlignment(0, c + 1, HasHorizontalAlignment.ALIGN_CENTER);
		}
		HTML label = new HTML("&nbsp;", false);
		label.addClickHandler(menu);
		iTable.getFlexCellFormatter().setStyleName(0, 0, "unitime-ClickableTableHeader");
		iTable.setWidget(0, 0, label);
		HTML totals = new HTML(MESSAGES.colTotal(), false);
		totals.addClickHandler(menu);
		iTable.getFlexCellFormatter().setStyleName(0, col2clasf.size() + 1, "unitime-ClickableTableHeader");
		iTable.setWidget(0, col2clasf.size() + 1, totals);
		if (row == 1)
			throw new CurriculaException(MESSAGES.errorNoLastLikeEnrollemnts());
		
		iTable.setText(row, 0, MESSAGES.colTotal());
		iTable.getCellFormatter().getElement(row, 0).getStyle().setBackgroundColor("#EEEEEE");
		List<MyCell> cells = new ArrayList<MyCell>();
		for (int c = 0; c < col2clasf.size(); c++) {
			List<MyCell> cellsThisCol = col2cells.get(c);
			if (cellsThisCol == null || cellsThisCol.isEmpty()) continue;
			cells.addAll(cellsThisCol);
			iTable.setWidget(row, 1 + c, new MySumCell(cellsThisCol, true));
			iTable.getCellFormatter().getElement(row, 1 + c).getStyle().setBackgroundColor("#EEEEEE");
		}
		iTable.setWidget(row, 1 + col2clasf.size(), new MySumCell(cells, true));
		iTable.getCellFormatter().getElement(row, 1 + col2clasf.size()).getStyle().setBackgroundColor("#EEEEEE");
		
		for (int r = 1; r < iTable.getRowCount(); r++) {
			for (int c = iTable.getCellCount(r); c < 1 + col2clasf.size(); c++) {
				iTable.setHTML(r, c, "&nbsp;");
			}
		}

		
		iBottom.setVisible(true);
		iTable.setVisible(true);
		if (!iHeader.isEnabled("close")) {
			iHeader.setEnabled("print", true);
		}
	}
	
	private interface Updatable {
		public void update();
		public void focus();
	}
	
	private class MyCell extends Composite implements Updatable {
		private MyRow iRow;
		private AcademicClassificationInterface iClasf;
		
		private UniTimeTextBox iTextBox;
		private HTML iFrontLabel, iRearLabel;
		private HorizontalPanel iPanel;
		
		private HTML iHint = null;
		private PopupPanel iHintPanel = null;
		private boolean iCellEditable = true;
		
		private List<MySumCell> iSums = new ArrayList<MySumCell>();
	
		
		public MyCell(MyRow row, AcademicClassificationInterface clasf) {
			iRow = row;
			iClasf = clasf;
			iRow.setCell(iClasf, this);
			
			iPanel = new HorizontalPanel();
			
			iTextBox = new UniTimeTextBox(6, ValueBoxBase.TextAlignment.RIGHT);
			iTextBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					try {
						if (iTextBox.getText().isEmpty()) {
							iRow.setProjection(iClasf, null);
						} else if (iTextBox.getText().endsWith("%")) {
							iRow.setProjection(iClasf, (float)NF.parse(iTextBox.getText().substring(0, iTextBox.getText().length() - 1)) / 100.0f);
						} else {
							iRow.setProjection(iClasf, (float)NF.parse(iTextBox.getText()) / iRow.getLastLike(iClasf));
						}
					} catch (Exception e) {
						iRow.setProjection(iClasf, null);
					}
					if (iRow.getParent() != null && iRow.getProjection(iClasf) == iRow.getParent().getProjection(iClasf)) {
						iRow.setProjection(iClasf, null);
					}
					update();
					for (MySumCell sum: iSums)
						sum.update();
					for (MyRow r: iRow.getChildren()) {
						if (r.iData.get(iClasf)[0] == null) {
							MyCell c = r.getCell(iClasf);
							if (c != null) {
								c.update();
								for (MySumCell sum: c.iSums)
									sum.update();
							}
						}
					}
				}
			});
			
			iFrontLabel = new HTML(MESSAGES.curriculumProjectionRulesOldValue(iRow.getLastLike(iClasf)), false);
			iFrontLabel.setWidth("55px");
			iFrontLabel.setStyleName("unitime-Label");
			iFrontLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			
			iRearLabel = new HTML(MESSAGES.curriculumProjectionRulesOfTotal(iRow.getLastLike(iClasf)), false);
			iRearLabel.setWidth("55px");
			iRearLabel.setStyleName("unitime-Label");
			iRearLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			
			iPanel.add(iFrontLabel);
			iPanel.setCellVerticalAlignment(iFrontLabel, HasVerticalAlignment.ALIGN_MIDDLE);

			iPanel.add(iTextBox);
			iPanel.setCellVerticalAlignment(iTextBox, HasVerticalAlignment.ALIGN_MIDDLE);
			
			iPanel.add(iRearLabel);
			iPanel.setCellVerticalAlignment(iRearLabel, HasVerticalAlignment.ALIGN_MIDDLE);

			initWidget(iPanel);	
			
			update();
			
			iHint = new HTML(MESSAGES.propAcademicArea() + " " + iRow.getArea().getAbbv() + " - " + iRow.getArea().getName() + "<br>" +
					(iRow.getMajor() == null ? "" : MESSAGES.propMajor() + " " + iRow.getMajor().getCode() + " - " + iRow.getMajor().getName() + "<br>") +
					MESSAGES.propAcademicClassification() + " " + iClasf.getCode() + " - " + iClasf.getName(), false);
			iHintPanel = new PopupPanel();
			iHintPanel.setWidget(iHint);
			iHintPanel.setStyleName("unitime-PopupHint");
			
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONMOUSEMOVE);
		}
		
		public void addSum(MySumCell sum) { iSums.add(sum); }
		
		public List<MySumCell> getSums() { return iSums; }
		
		public void focus() {
			iTextBox.focus();
		}
		
		public void onBrowserEvent(final Event event) {
			Element tr = getElement();
		    for (; tr != null; tr = DOM.getParent(tr)) {
		        if (tr.getPropertyString("tagName").equalsIgnoreCase("tr"))
		        break;
		    }
		    final Element e = tr;

			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				if (!iHintPanel.isShowing()) {
					iHintPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							boolean top = (e.getAbsoluteBottom() - Window.getScrollTop() + 15 + offsetHeight > Window.getClientHeight());
							iHintPanel.setPopupPosition(
									Math.max(Math.min(event.getClientX(), e.getAbsoluteRight() - offsetWidth - 15), e.getAbsoluteLeft() + 15),
									top ? e.getAbsoluteTop() - offsetHeight - 15 : e.getAbsoluteBottom() + 15);
						}
					});
				}
				break;
			case Event.ONMOUSEOUT:
				if (iHintPanel.isShowing()) iHintPanel.hide();
				break;
			case Event.ONMOUSEMOVE:
				if (iHintPanel.isShowing()) {
					boolean top = (e.getAbsoluteBottom() - Window.getScrollTop() + 15 + iHintPanel.getOffsetHeight() > Window.getClientHeight());
					iHintPanel.setPopupPosition(
							Math.max(Math.min(event.getClientX(), e.getAbsoluteRight() - iHintPanel.getOffsetWidth() - 15), e.getAbsoluteLeft() + 15),
							top ? e.getAbsoluteTop() - iHintPanel.getOffsetHeight() - 15 : e.getAbsoluteBottom() + 15);
				}
				break;
			}
		}
		
		public void update() {
			float projection = iRow.getProjection(iClasf);
			int lastLike = iRow.getLastLike(iClasf);
			if (CurriculumCookie.getInstance().getCurriculumProjectionRulesPercent()) {
				iTextBox.setText(NF.format(100.0 * projection) + "%");
			} else {
				iTextBox.setText(String.valueOf(Math.round(projection * lastLike)));
			}
			if (iRow.isDefaultProjection(iClasf))
				iTextBox.addStyleName("unitime-GrayText");
			else
				iTextBox.removeStyleName("unitime-GrayText");
			//iTextBox.getElement().getStyle().setColor(iRow.isDefaultProjection(iClasf) ? "#777777" : null);
			setVisible(lastLike > 0);
			if (iCellEditable != iEditable) {
				iCellEditable = iEditable;
				iTextBox.setReadOnly(!iCellEditable);
				if (iCellEditable) {
					iTextBox.getElement().getStyle().clearBorderColor();
					iTextBox.getElement().getStyle().clearBackgroundColor();
				} else {
					iTextBox.getElement().getStyle().setBorderColor("transparent");
					iTextBox.getElement().getStyle().setBackgroundColor("transparent");
				}
			}
			iFrontLabel.setVisible(CurriculumCookie.getInstance().getCurriculumProjectionRulesShowLastLike() && !CurriculumCookie.getInstance().getCurriculumProjectionRulesPercent());
			iRearLabel.setVisible(CurriculumCookie.getInstance().getCurriculumProjectionRulesShowLastLike() && CurriculumCookie.getInstance().getCurriculumProjectionRulesPercent());
			if (projection == 1.0f) {
				iFrontLabel.setHTML("&nbsp;");
			} else {
				iFrontLabel.setHTML(MESSAGES.curriculumProjectionRulesOldValue(iRow.getLastLike(iClasf)));
			}
		}
		
		public MyRow getRow() { return iRow; }
		public AcademicClassificationInterface getClassification() { return iClasf; }
	}
	
	private class MySumCell extends Composite implements Updatable {
		private List<MyCell> iCells;
		private boolean iVertical;
		
		private UniTimeTextBox iTextBox;
		private HTML iFrontLabel, iRearLabel;
		private HorizontalPanel iPanel;
		
		private boolean iCellEditable = true;
	
		
		public MySumCell(List<MyCell> cells, boolean vertical) {
			iCells = cells;
			for (MyCell cell: iCells)
				cell.addSum(this);
			iVertical = vertical;
			
			iPanel = new HorizontalPanel();
			
			iTextBox = new UniTimeTextBox(6, ValueBoxBase.TextAlignment.RIGHT);
			iTextBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					HashSet<MySumCell> sums = new HashSet<MySumCell>();
					sums.add(MySumCell.this);
					Float projection = null;
					try {
						if (iTextBox.getText().isEmpty()) {
						} else if (iTextBox.getText().endsWith("%")) {
							projection = (float)NF.parse(iTextBox.getText().substring(0, iTextBox.getText().length() - 1)) / 100.0f;
						} else {
							int lastLike = 0;
							for (MyCell cell: iCells) {
								if (iVertical && cell.getRow().getParent() == null) continue;
								lastLike += cell.getRow().getLastLike(cell.getClassification());
							}
							projection = (float)NF.parse(iTextBox.getText()) / lastLike;
						}
					} catch (Exception e) {
					}
					for (MyCell cell: iCells) {
						if (iVertical && cell.getRow().getParent() != null)
							cell.getRow().setProjection(cell.getClassification(), null);
						else
							cell.getRow().setProjection(cell.getClassification(), projection);
						cell.update();
						sums.addAll(cell.getSums());
					}
					if (!iVertical) {
						for (MyCell cell: iCells) {
							for (MyRow r: cell.getRow().getChildren()) {
								if (r.iData.get(cell.getClassification())[0] == null) {
									MyCell c = r.getCell(cell.getClassification());
									if (c != null) {
										c.update();
										sums.addAll(c.getSums());
									}
								}
							}
						}
					}
					for (MySumCell sum: sums)
						sum.update();
				}
			});
			
			int lastLike = 0;
			for (MyCell cell: iCells) {
				if (iVertical && cell.getRow().getParent() == null) continue;
				lastLike += cell.getRow().getLastLike(cell.getClassification());
			}
			
			iFrontLabel = new HTML(MESSAGES.curriculumProjectionRulesOldValue(lastLike), false);
			iFrontLabel.setWidth("55px");
			iFrontLabel.setStyleName("unitime-Label");
			iFrontLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			
			iRearLabel = new HTML(MESSAGES.curriculumProjectionRulesOfTotal(lastLike), false);
			iRearLabel.setWidth("55px");
			iRearLabel.setStyleName("unitime-Label");
			iRearLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			
			iPanel.add(iFrontLabel);
			iPanel.setCellVerticalAlignment(iFrontLabel, HasVerticalAlignment.ALIGN_MIDDLE);

			iPanel.add(iTextBox);
			iPanel.setCellVerticalAlignment(iTextBox, HasVerticalAlignment.ALIGN_MIDDLE);
			
			iPanel.add(iRearLabel);
			iPanel.setCellVerticalAlignment(iFrontLabel, HasVerticalAlignment.ALIGN_MIDDLE);

			initWidget(iPanel);	
			
			update();
		}
		
		public void focus() {
			iTextBox.focus();
		}
		
		public void update() {
			int lastLike = 0;
			float projected = 0.0f;
			boolean allDefault = true;
			for (MyCell cell: iCells) {
				if (allDefault && !cell.getRow().isDefaultProjection(cell.getClassification())) allDefault = false;
				if (iVertical && cell.getRow().getParent() == null) continue;
				lastLike += cell.getRow().getLastLike(cell.getClassification());
				projected += cell.getRow().getProjection(cell.getClassification()) * cell.getRow().getLastLike(cell.getClassification());
			}
			float projection = projected / lastLike;
			if (CurriculumCookie.getInstance().getCurriculumProjectionRulesPercent()) {
				iTextBox.setText(NF.format(100.0 * projection) + "%");
			} else {
				iTextBox.setText(String.valueOf(Math.round(projection * lastLike)));
			}
			if (allDefault)
				iTextBox.addStyleName("unitime-GrayText");
			else
				iTextBox.removeStyleName("unitime-GrayText");
			setVisible(lastLike > 0);
			if (iCellEditable != iEditable) {
				iCellEditable = iEditable;
				iTextBox.setReadOnly(!iCellEditable);
				if (iCellEditable) {
					iTextBox.getElement().getStyle().clearBorderColor();
					iTextBox.getElement().getStyle().clearBackgroundColor();
				} else {
					iTextBox.getElement().getStyle().setBorderColor("transparent");
					iTextBox.getElement().getStyle().setBackgroundColor("transparent");
				}
			}
			iFrontLabel.setVisible(CurriculumCookie.getInstance().getCurriculumProjectionRulesShowLastLike() && !CurriculumCookie.getInstance().getCurriculumProjectionRulesPercent());
			iRearLabel.setVisible(CurriculumCookie.getInstance().getCurriculumProjectionRulesShowLastLike() && CurriculumCookie.getInstance().getCurriculumProjectionRulesPercent());
			if (projection == 1.0f) {
				iFrontLabel.setHTML("&nbsp;");
			} else {
				iFrontLabel.setHTML(MESSAGES.curriculumProjectionRulesOldValue(lastLike));
			}
		}
	}

	private class MyRow {
		private AcademicAreaInterface iArea;
		private MajorInterface iMajor;
		private HashMap<AcademicClassificationInterface, Number[]> iData;
		private MyRow iParent = null;
		private List<MyRow> iChildren = new ArrayList<MyRow>();
		private HashMap<AcademicClassificationInterface, MyCell> iCells = new HashMap<AcademicClassificationInterface, MyCell>();
		
		public MyRow(AcademicAreaInterface area, MajorInterface major, HashMap<AcademicClassificationInterface, Number[]> data) {
			iArea = area;
			iMajor = major;
			iData = data;
		}
		
		public AcademicAreaInterface getArea() { return iArea; }
		public MajorInterface getMajor() { return iMajor; }
		public TreeSet<AcademicClassificationInterface> getClassifications() { return new TreeSet<AcademicClassificationInterface>(iData.keySet()); }
		public float getProjection(AcademicClassificationInterface clasf) {
			Number proj = iData.get(clasf)[0];
			if (proj == null && iParent != null)
				proj = iParent.iData.get(clasf)[0];
			return (proj == null ? 1.0f : proj.floatValue());
		}
		public boolean isDefaultProjection(AcademicClassificationInterface clasf) {
			return (iData.get(clasf)[0] == null || (iMajor == null && iData.get(clasf)[0].floatValue() == 1.0f));
		}
		public void setProjection(AcademicClassificationInterface clasf, Float projection) {
			iData.get(clasf)[0] = projection;
		}
		public int getLastLike(AcademicClassificationInterface clasf) {
			return iData.get(clasf)[1].intValue();
		}
		
		public boolean hasLastLike() {
			for (Number[] n: iData.values()) {
				if (n[1].intValue() > 0) return true;
			}
			return false;
		}
		
		public boolean hasProjection() {
			for (Number[] n: iData.values()) {
				if (n[1].intValue() > 0 && n[0] != null) return true;
				/*
				if (iParent == null) {
					if (iData.get(i)[0].floatValue() != 1.0f) return true;
				} else {
					if (iData.get(i)[0].floatValue() != iParent.iData.get(i)[1].floatValue()) return true;
				} 
				*/
			}
			return false;
		}
		
		public void setParent(MyRow row) { iParent = row; }
		public MyRow getParent() { return iParent; }
		public void addChild(MyRow row) {
			if (iChildren == null) iChildren = new ArrayList<MyRow>();
			iChildren.add(row);
		}
		public List<MyRow> getChildren() { return iChildren; }
		public void setCell(AcademicClassificationInterface clasf, MyCell cell) { iCells.put(clasf, cell); }
		public MyCell getCell(AcademicClassificationInterface clasf) { return iCells.get(clasf); }
	}
	
	private class MyFlexTable extends FlexTable {
		private Timer iTimer = null;
		
		public MyFlexTable() {
			super();
			setCellPadding(2);
			setCellSpacing(0);
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONCLICK);
			sinkEvents(Event.ONKEYDOWN);
			setStylePrimaryName("unitime-MainTable");
			iTimer = new Timer() {
				@Override
				public void run() {
					saveOrder();
				}
			};
		}
		
		private boolean focus(Event event, int oldRow, int oldCol, int row, int col) {
			if (!getRowFormatter().isVisible(row) || col >= getCellCount(row)) return false;
			final Widget w = getWidget(row, col);
			if (w == null || !w.isVisible()) return false;
			if (w instanceof Updatable) {
				((Updatable)w).focus();
				event.stopPropagation();
				return true;
			}
			return false;
		}
		
		public MyRow getMyRow(int row) {
			if (row == 0 || row + 1 >= iTable.getRowCount()) return null;
		    for (int c = 1; c < getCellCount(row) - 1; c ++) {
			    Widget w = getWidget(row, c);
			    if (w != null) return ((MyCell)w).getRow();
		    }
		    return null;
		}
		
		private void moveRow(Element tr, Element before) {
			Element body = DOM.getParent(tr);
			body.removeChild(tr);
			DOM.insertBefore(body, tr, before);
		}
		
		public void saveOrder() {
			iHeader.setMessage(MESSAGES.waitSavingOrder());
			String areaOrd = "";
			HashMap<String, String> area2majorOrd = new HashMap<String, String>();
			for (int i = 1; i < getRowCount() - 1; i++) {
			    MyRow r = getMyRow(i);
			    if (r == null) continue;
			    if (r.getMajor() == null) {
			    	if (!areaOrd.isEmpty()) areaOrd += "|";
			    	areaOrd += r.getArea().getAbbv();
			    } else {
			    	String majorOrd = area2majorOrd.get(r.getArea().getAbbv());
		    		area2majorOrd.put(r.getArea().getAbbv(), (majorOrd == null ? "" : majorOrd + "|") + r.getMajor().getCode());
			    }
			}
			SetUserDataRpcRequest ord = new SetUserDataRpcRequest();
			ord.put("CurProjRules.Order", areaOrd);
			for (Map.Entry<String, String> e: area2majorOrd.entrySet()) {
				ord.put("CurProjRules.Order[" + e.getKey() + "]", e.getValue());
			}
			RPC.execute(ord, new AsyncCallback<GwtRpcResponseNull>() {
				@Override
				public void onFailure(Throwable caught) {
				}
				@Override
				public void onSuccess(GwtRpcResponseNull result) {
					iHeader.clearMessage();
				}
			});
		}
		
		public void onBrowserEvent(Event event) {
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    Element tr = DOM.getParent(td);
			int col = DOM.getChildIndex(tr, td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);
		    if (row == 0) return;
		    
		    MyRow r = getMyRow(row);

			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				getRowFormatter().setStyleName(row, "unitime-TableRowHover");
				if (r != null) getCellFormatter().getElement(row, DOM.getChildCount(tr) - 1).getStyle().clearBackgroundColor();
				if (r == null || r.getChildren().isEmpty()) getRowFormatter().getElement(row).getStyle().setCursor(Cursor.AUTO);
				break;
			case Event.ONMOUSEOUT:
				getRowFormatter().setStyleName(row, null);	
				if (r != null) getCellFormatter().getElement(row, DOM.getChildCount(tr) - 1).getStyle().setBackgroundColor("#EEEEEE");
				break;
			case Event.ONCLICK:
			    if (r == null) break;
				if (r.getMajor() != null) break;
				Element element = DOM.eventGetTarget(event);
				while (element.getPropertyString("tagName").equalsIgnoreCase("div"))
					element = DOM.getParent(element);
				if (element.getPropertyString("tagName").equalsIgnoreCase("td")) {
					if (r.getMajor() == null) {
						boolean canCollapse = false;
						for (int rx = row + 1; rx < getRowCount() - 1; rx++) {
							r = getMyRow(rx);
							if (r == null || r.getMajor() == null) break;
							if (r.hasProjection()) continue;
							if (getRowFormatter().isVisible(rx)) {
								canCollapse = true; break;
							}
						}
						for (int rx = row + 1; rx < getRowCount() - 1; rx++) {
							r = getMyRow(rx);
							if (r == null || r.getMajor() == null) break;
							if (r.hasProjection()) continue;
							getRowFormatter().setVisible(rx, !canCollapse);
						}
					}
				}
				break;
			case Event.ONKEYDOWN:
				int oldRow = row, oldCol = col;
				if (event.getKeyCode() == KeyCodes.KEY_RIGHT && (event.getAltKey() || event.getMetaKey())) {
					do {
						col++;
						if (col >= getCellCount(row)) break;
					} while (!focus(event, oldRow, oldCol, row, col));
					event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_LEFT && (event.getAltKey() || event.getMetaKey())) {
					do {
						col--;
						if (col < 0) break;
					} while (!focus(event, oldRow, oldCol, row, col));
					event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP && (event.getAltKey() || event.getMetaKey())) {
					do {
						row--;
						if (row <= 0) break;
					} while (!focus(event, oldRow, oldCol, row, col));
					event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN && (event.getAltKey() || event.getMetaKey())) {
					do {
						row++;
						if (row >= getRowCount()) break;
					} while (!focus(event, oldRow, oldCol, row, col));
					event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP && event.getCtrlKey()) {
					if (r != null) {
						Updatable u = (Updatable)getWidget(row, col);
						if (r.getMajor() != null) {
						    MyRow p = getMyRow(row - 1);
						    if (p.getMajor() != null) {
						    	moveRow(tr, DOM.getChild(body, row - 1));
						    }
						} else {
						    MyRow p = getMyRow(row - 1);
						    if (p != null && p.getParent() != null) p = p.getParent();
						    if (p != null && p.getMajor() == null) {
						    	Element x = DOM.getChild(body, row - 1 - p.getChildren().size());
						    	for (int i = 0; i <= r.getChildren().size(); i++) {
						    		moveRow(DOM.getChild(body, row + i), x);
						    	}
						    }
						}
						iTimer.schedule(5000);
				    	u.focus();
					}
			    	event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN && event.getCtrlKey()) {
					if (r != null) {
				    	Updatable u = (Updatable)getWidget(row, col);
						if (r.getMajor() != null) {
						    MyRow p = getMyRow(row + 1);
						    if (p.getMajor() != null) {
						    	moveRow(tr, DOM.getChild(body, row + 2));
						    }
						} else {
							MyRow p = getMyRow(1 + row + r.getChildren().size());
							if (p != null && p.getMajor() == null) {
								Element x = DOM.getChild(body, row + 2 + r.getChildren().size() + p.getChildren().size());
						    	for (int i = 0; i <= r.getChildren().size(); i++) {
						    		moveRow(DOM.getChild(body, row), x);
						    	}
							}
						}
						iTimer.schedule(5000);
				    	u.focus();
					}
			    	event.stopPropagation();
			    	event.preventDefault();
				}
				break;
		    }
		}
	}
		
	public static class ProjectionRulesEvent {
	}
	
	public static interface ProjectionRulesHandler {
		public void onRulesLoaded(ProjectionRulesEvent evt);
		public void onException(Throwable caught);
		public void onRulesSaved(ProjectionRulesEvent evt);
		public void onRulesClosed(ProjectionRulesEvent evt);
	}
	
	public void addProjectionRulesHandler(ProjectionRulesHandler h) {
		iProjectionRulesHandlers.add(h);
	}
	
}
