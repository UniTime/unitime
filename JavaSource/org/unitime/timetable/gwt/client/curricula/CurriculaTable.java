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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.curricula.CurriculumProjectionRulesPage.ProjectionRulesEvent;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HintProvider;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumFilterRpcRequest;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CurriculaTable extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);

	private UniTimeTable<CurriculumInterface> iTable = null;
	private CurriculumFilterRpcRequest iLastQuery = null;
	private AriaButton iOperations = null;
	
	private AsyncCallback<List<CurriculumClassificationInterface>> iLoadClassifications;
	
	private List<CurriculumClickHandler> iCurriculumClickHandlers = new ArrayList<CurriculumClickHandler>();
	
	private Long iLastCurriculumId = null;
	
	private CurriculaClassifications iClassifications = null;
	private PopupPanel iClassificationsPopup = null;
	
	private HashSet<Long> iSelectedCurricula = new HashSet<Long>();
	
	private boolean iIsAdmin = false;
	private boolean iSessionHasSnapshotData = false;
	
	private EditClassificationHandler iEditClassificationHandler = null;
	
	public CurriculaTable() {
		iOperations = new AriaButton(MESSAGES.buttonMoreOperations());
		iOperations.addStyleName("unitime-NoPrint");

		iTable = new UniTimeTable<CurriculumInterface>();
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (CurriculumColumn column: CurriculumColumn.values()) {
			UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column), getColumnAlignment(column));
			h.setWidth(getColumnWidth(column));
			addOperations(column, h);
			header.add(h);
		}
		
		iTable.addRow(null, header);
		
		iTable.addMouseClickListener(new MouseClickListener<CurriculumInterface>() {
			@Override
			public void onMouseClick(TableEvent<CurriculumInterface> event) {
				if (event.getData() == null) return;
				
				setLastSelectedRow(event.getRow());

				CurriculumClickedEvent e = new CurriculumClickedEvent(event.getData());
				for (CurriculumClickHandler h: iCurriculumClickHandlers) {
					h.onClick(e);
				}
			}
		});

		initWidget(iTable);
		
		iLoadClassifications = new AsyncCallback<List<CurriculumClassificationInterface>>() {
			public void onFailure(Throwable caught) {}
			public void onSuccess(List<CurriculumClassificationInterface> classifications) {
				if (iTable.getRowCount() <= 1) return;
				List<Integer> rows = new ArrayList<Integer>();
				CurriculumInterface last = null;
				clasf: for (CurriculumClassificationInterface clasf: classifications) {
					iSessionHasSnapshotData = clasf.isSessionHasSnapshotData();
					if (last != null && last.getId().equals(clasf.getCurriculumId())) {
						last.addClassification(clasf);
						continue clasf;
					}
					for (int row = 0; row < iTable.getRowCount(); row++) {
						CurriculumInterface c = iTable.getData(row);
						if (c!= null && c.getId().equals(clasf.getCurriculumId())) {
							if (c.hasClassifications()) c.getClassifications().clear();
							c.addClassification(clasf);
							rows.add(row);
							last = c;
							continue clasf;
						}
					}
				}
				for (int row: rows) {
					CurriculumInterface c = iTable.getData(row);
					((Label)iTable.getWidget(row, 5)).setText(c.getLastLikeString());
					((Label)iTable.getWidget(row, 6)).setText(c.getProjectionString());
					if (iTable.getWidget(row, 7) instanceof Image) {
						iTable.setWidget(row, 7, new Label(c.getExpectedString(), false));
						iTable.getFlexCellFormatter().setHorizontalAlignment(row, 7, HasHorizontalAlignment.ALIGN_RIGHT);
					} else {
						((Label)iTable.getWidget(row, 7)).setText(c.getExpectedString());
					}
					((Label)iTable.getWidget(row, 8)).setText(c.getEnrollmentString());
					((Label)iTable.getWidget(row, 9)).setText(c.getRequestedString());
					((Label)iTable.getWidget(row, 10)).setText(!c.isSessionHasSnapshotData()?"":c.getSnapshotExpectedString());
					((Label)iTable.getWidget(row, 11)).setText(!c.isSessionHasSnapshotData()?"":c.getSnapshotProjectionString());
				}
				List<Long> noEnrl = new ArrayList<Long>();
				for (int row = 0; row < iTable.getRowCount(); row++) {
					CurriculumInterface c = iTable.getData(row);
					if (c!= null && !c.hasClassifications()) {
						noEnrl.add(c.getId());
						if (noEnrl.size() == 1) {
							iTable.setWidget(row, 7, new Image(RESOURCES.loading_small()));
							iTable.getFlexCellFormatter().setHorizontalAlignment(row, 7, HasHorizontalAlignment.ALIGN_LEFT);
						}
					}
					if (noEnrl.size() >= 10) break;
				}
				if (!noEnrl.isEmpty())
					iService.loadClassifications(noEnrl, iLoadClassifications);
				else {
					int sortBy = CurriculumCookie.getInstance().getSortBy();
					if (sortBy != 0) {
						int col = Math.abs(sortBy) - 1;
						CurriculumColumn column = CurriculumColumn.values()[col];
						Comparator<CurriculumInterface> cmp = column.getComparator();
						if (cmp != null) {
							boolean asc = sortBy > 0;
							UniTimeTableHeader h = iTable.getHeader(col);
							iTable.sort(h, cmp, asc);
						}
					}
				}
			}
		};
		
		iClassifications = new CurriculaClassifications();
		iClassificationsPopup = new PopupPanel();
		iClassificationsPopup.setWidget(iClassifications);
		iClassificationsPopup.setStyleName("unitime-PopupHint");
		
		iService.isAdmin(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
				iIsAdmin = result;
			}			
		});
		
		iTable.setHintProvider(new HintProvider<CurriculumInterface>() {
			@Override
			public Widget getHint(TableEvent<CurriculumInterface> event) {
				if (event.getData() == null || !event.getData().hasClassifications()) return null;
				iClassifications.populate(event.getData().getClassifications());
				iClassifications.setEnabled(false);
				iClassifications.hideEmptyRows();
				return iClassifications;
			}
		});
		
		iOperations.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				if (!iTable.getHeader(0).setMenu(popup)) return;
				popup.showRelativeTo(iOperations);
				((MenuBar)popup.getWidget()).focus();
			}
		});
	}
	
	public void setLastSelectedRow(int row) {
		for (int r = 1; r < iTable.getRowCount(); r++)
			if ("unitime-TableRowSelected".equals(iTable.getRowFormatter().getStyleName(r)))
				iTable.getRowFormatter().setStyleName(r, null);
		if (row >= 0) {
			CurriculumInterface c = iTable.getData(row);
			if (c != null) {
				iLastCurriculumId = c.getId();
				iTable.getRowFormatter().setStyleName(row, "unitime-TableRowSelected");
			} else {
				iLastCurriculumId = null;
			}
		} else {
			iLastCurriculumId = null;
		}
	}
	
	protected List<CurriculumInterface> selected() {
		List<CurriculumInterface> selected = new ArrayList<CurriculumInterface>();
		for (int row = 0; row < iTable.getRowCount(); row++) {
			CurriculumInterface c = iTable.getData(row);
			if (c != null && c.isEditable() && iSelectedCurricula.contains(c.getId()))
				selected.add(c);
		}
		return selected;
	}
	
	protected Set<Long> markSelected() {
		Set<Long> markedIds = new HashSet<Long>();
		for (int row = 0; row < iTable.getRowCount(); row++) {
			CurriculumInterface c = iTable.getData(row);
			if (c != null && c.isEditable() && (iSelectedCurricula.isEmpty() || iSelectedCurricula.contains(c.getId()))) {
				markedIds.add(c.getId());
				iTable.getRowFormatter().setStyleName(row, "unitime-TableRowProblem");
			}
		}
		return markedIds;
	}
	
	protected void unmarkSelected() {
		for (int row = 0; row < iTable.getRowCount(); row++) {
			CurriculumInterface c = iTable.getData(row);
			if (c != null && c.isEditable() && (iSelectedCurricula.isEmpty() || iSelectedCurricula.contains(c.getId()))) {
				iTable.getRowFormatter().setStyleName(row, c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null);
			}
		}
	}
	
	protected void markAll() {
		for (int row = 0; row < iTable.getRowCount(); row++) {
			CurriculumInterface c = iTable.getData(row);
			if (c != null && c.isEditable()) {
				iTable.getRowFormatter().setStyleName(row, "unitime-TableRowProblem");
			}
		}
	}
	
	protected void unmarkAll() {
		for (int row = 0; row < iTable.getRowCount(); row++) {
			CurriculumInterface c = iTable.getData(row);
			if (c != null && c.isEditable()) {
				iTable.getRowFormatter().setStyleName(row, c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null);
			}
		}
	}
	
	public void setup(List<AcademicClassificationInterface> classifications) {
		iClassifications.setup(classifications);
	}
	
	private void fillRow(CurriculumInterface c) {
		int row = iTable.getRowCount();
		List<Widget> line = new ArrayList<Widget>();
		if (c.isEditable()) {
			CheckBox ch = new CheckBox();
			final Long cid = c.getId();
			ch.setValue(iSelectedCurricula.contains(cid));
			ch.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (event.getValue())
						iSelectedCurricula.add(cid);
					else
						iSelectedCurricula.remove(cid);
				}
			});
			line.add(ch);
		} else {
			line.add(new Label(""));
		}
		iSessionHasSnapshotData = c.isSessionHasSnapshotData();
		DisplayMode m = CurriculumCookie.getInstance().getCurriculaDisplayMode();
		line.add(new Label(m.isCurriculumAbbv() ? c.getAbbv() : c.getName(), false));
		line.add(new Label(m.isAreaAbbv() ? c.getAcademicArea().getAbbv() : c.getAcademicArea().getName(), false));
		line.add(new HTML(m.isMajorAbbv() ? c.getMajorCodes(", ") : c.getMajorNames("<br>"), m.isMajorAbbv()));
		line.add(new Label(m.formatDepartment(c.getDepartment()), false));
		line.add(new Label(c.getLastLike() == null ? "" : c.getLastLikeString(), false));
		line.add(new Label(c.getProjection() == null ? "" : c.getProjectionString(), false));
		line.add(new Label(c.getExpected() == null ? "" : c.getExpectedString(), false));
		line.add(new Label(c.getEnrollment() == null ? "" : c.getEnrollmentString(), false));
		line.add(new Label(c.getRequested() == null ? "" : c.getRequestedString(), false));
		line.add(new Label(!c.isSessionHasSnapshotData() ? "" : (c.getSnapshotExpected() == null ? "" : c.getSnapshotExpectedString()), false));
		line.add(new Label(!c.isSessionHasSnapshotData() ? "" : (c.getSnapshotProjection() == null ? "" : c.getSnapshotProjectionString()), false));
		iTable.setRow(row, c, line);
		iTable.getCellFormatter().addStyleName(row, 0, "unitime-NoPrint");
		iTable.getFlexCellFormatter().setHorizontalAlignment(row, 5, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.getFlexCellFormatter().setHorizontalAlignment(row, 6, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.getFlexCellFormatter().setHorizontalAlignment(row, 7, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.getFlexCellFormatter().setHorizontalAlignment(row, 8, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.getFlexCellFormatter().setHorizontalAlignment(row, 9, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.getFlexCellFormatter().setHorizontalAlignment(row, 10, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.getFlexCellFormatter().setHorizontalAlignment(row, 11, HasHorizontalAlignment.ALIGN_RIGHT);
	}
	
	public void populate(TreeSet<CurriculumInterface> result, boolean editable) {
		iTable.clearTable(1);
		
		if (result.isEmpty()) {
			iTable.setEmptyMessage(MESSAGES.errorNoMatchingCurriculaFound());
			return;
		}
		
		List<Long> ids = new ArrayList<Long>();
		int row = 0;
		int rowToScroll = -1;
		boolean hasEditable = false;
		HashSet<Long> newlySelected = new HashSet<Long>();
		for (CurriculumInterface curriculum: result) {
			if (ids.size() < 10 && !curriculum.hasClassifications()) ids.add(curriculum.getId());
			if (curriculum.isEditable() && editable) hasEditable = true;
			fillRow(curriculum);
			if (curriculum.getId().equals(iLastCurriculumId)) {
				iTable.getRowFormatter().setStyleName(1 + row, "unitime-TableRowSelected");
				rowToScroll = 1 + row;
			}
			if (curriculum.isEditable() && editable && iSelectedCurricula.contains(curriculum.getId()))
				newlySelected.add(curriculum.getId());
			row++;
		}
		if (!ids.isEmpty()) {
			iTable.setWidget(1, 7, new Image(RESOURCES.loading_small()));
			iTable.getFlexCellFormatter().setHorizontalAlignment(1, 7, HasHorizontalAlignment.ALIGN_LEFT);
		}
		iSelectedCurricula.clear();
		iSelectedCurricula.addAll(newlySelected);
		
		if (!hasEditable) {
			for (int r = 0; r < iTable.getRowCount(); r++) {
				iTable.getCellFormatter().setVisible(r, 0, false);
			}
			iOperations.setVisible(false);
		} else {
			iTable.getCellFormatter().setVisible(0, 0, true);
			iOperations.setVisible(true);
		}
		
		if (rowToScroll >= 0) {
			iTable.getRowFormatter().getElement(rowToScroll).scrollIntoView();
		}
		
		if (!ids.isEmpty())
			iService.loadClassifications(ids, iLoadClassifications);
		
		iTable.setColumnVisible(iTable.getCellCount(0) - 7, CurriculumCookie.getInstance().isShowLast());
		iTable.setColumnVisible(iTable.getCellCount(0) - 6, CurriculumCookie.getInstance().isShowProjected());
		iTable.setColumnVisible(iTable.getCellCount(0) - 5, CurriculumCookie.getInstance().isShowExpected());
		iTable.setColumnVisible(iTable.getCellCount(0) - 4, CurriculumCookie.getInstance().isShowEnrolled());
		iTable.setColumnVisible(iTable.getCellCount(0) - 3, CurriculumCookie.getInstance().isShowRequested());
		iTable.setColumnVisible(iTable.getCellCount(0) - 2, (iSessionHasSnapshotData && CurriculumCookie.getInstance().isShowSnapshotExpected()));
		iTable.setColumnVisible(iTable.getCellCount(0) - 1, (iSessionHasSnapshotData && CurriculumCookie.getInstance().isShowSnapshotProjected()));
	}

	public void query(CurriculumFilterRpcRequest filter, final AsyncCallback<TreeSet<CurriculumInterface>> callback) {
		iLastQuery = filter;
		iTable.clearTable(1);
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingCurricula());
		iService.findCurricula(filter, new AsyncCallback<TreeSet<CurriculumInterface>>() {
			
			@Override
			public void onSuccess(TreeSet<CurriculumInterface> result) {
				LoadingWidget.getInstance().hide();
				populate(result, true);
				if (callback != null)
					callback.onSuccess(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iTable.setEmptyMessage(MESSAGES.failedToLoadCurricula(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadCurricula(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
				if (callback != null)
					callback.onFailure(caught);
			}
		});
	}

	public void scrollIntoView() {
		for (int r = 1; r < iTable.getRowCount(); r++)
			if ("unitime-TableRowSelected".equals(iTable.getRowFormatter().getStyleName(r)))
				iTable.getRowFormatter().getElement(r).scrollIntoView();
	}
	
	private void openCurriculumProjectionRules() {
		final DialogBox dialog = new UniTimeDialogBox(true, true);
		dialog.addStyleName("unitime-CurriculumProjectionRulesDialog");
		final CurriculumProjectionRulesPage rules = new CurriculumProjectionRulesPage();
		rules.setAllowClose(true);
		rules.getElement().getStyle().setMarginRight(ToolBox.getScrollBarWidth(), Unit.PX);
		rules.getElement().getStyle().setPaddingLeft(10, Unit.PX);
		rules.getElement().getStyle().setPaddingRight(10, Unit.PX);
		final ScrollPanel panel = new ScrollPanel(rules);
		panel.setHeight(Math.round(0.9 * Window.getClientHeight()) + "px");
		panel.setStyleName("unitime-ScrollPanel");
		dialog.setWidget(panel);
		dialog.setText(MESSAGES.dialogCurriculumProjectionRules());
		dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		rules.addProjectionRulesHandler(new CurriculumProjectionRulesPage.ProjectionRulesHandler() {
			@Override
			public void onRulesSaved(ProjectionRulesEvent evt) {
				dialog.hide();
				query(iLastQuery, null);
			}
			@Override
			public void onRulesLoaded(ProjectionRulesEvent evt) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
				dialog.center();
				//panel.setWidth((ToolBox.getScrollBarWidth() + rules.getOffsetWidth()) + "px");
			}
			@Override
			public void onRulesClosed(ProjectionRulesEvent evt) {
				dialog.hide();
			}
			@Override
			public void onException(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedToOpenCurriculumProjectionRules(caught.getMessage()), caught);
			}
		});
	}
	
	public static class CurriculumClickedEvent {
		private CurriculumInterface iCurriculum;
		
		public CurriculumClickedEvent(CurriculumInterface curriculum) {
			iCurriculum = curriculum;
		}
		
		public CurriculumInterface getCurriculum() {
			return iCurriculum;
		}
	}
	
	public interface CurriculumClickHandler {
		public void onClick(CurriculumClickedEvent evt);
	}
	
	public void addCurriculumClickHandler(CurriculumClickHandler h) {
		iCurriculumClickHandlers.add(h);
	}
	
	public List<CurriculumInterface> getCurricula() {
		return iTable.getData();
	}
	
	public interface EditClassificationHandler {
		public void doEdit(List<CurriculumInterface> curricula);
	}
	
	public void setEditClassificationHandler(EditClassificationHandler h) {
		iEditClassificationHandler = h;
	}
	
	public Button getOperations() {
		return iOperations;
	}
	
	public static enum DeptMode {
		CODE('0', MESSAGES.fieldCode()),
		ABBV('1', MESSAGES.fieldAbbreviation()),
		NAME('2', MESSAGES.fieldName()),
		ABBV_NAME('3', MESSAGES.fieldAbbv() + " - " + MESSAGES.fieldName()),
		CODE_NAME('4', MESSAGES.fieldCode() + " - " + MESSAGES.fieldName());

		private char iCode;
		private String iName;
		
		DeptMode(char code, String name) { iCode = code; iName = name; }
		
		public String getName() { return iName; }
		public char getCode() { return iCode; }
	}
	
	public abstract static class DisplayMode {
		private boolean iCurriculumAbbv = true;
		private boolean iAreaAbbv = false;
		private boolean iMajorAbbv = false;
		private DeptMode iDeptMode = DeptMode.ABBV_NAME;
		
		public boolean isCurriculumAbbv() {
			return iCurriculumAbbv;
		}
		
		public void setCurriculumAbbv(boolean curriculumAbbv) {
			iCurriculumAbbv = curriculumAbbv;
			changed();
		}
		
		public boolean isAreaAbbv() {
			return iAreaAbbv;
		}
		
		public void setAreaAbbv(boolean areaAbbv) {
			iAreaAbbv = areaAbbv;
			changed();
		}
		
		public boolean isMajorAbbv() {
			return iMajorAbbv;
		}
		
		public void setMajorAbbv(boolean majorAbbv) {
			iMajorAbbv = majorAbbv;
			changed();
		}
		
		public DeptMode getDeptMode() {
			return iDeptMode;
		}
		public void setDeptMode(DeptMode deptMode) {
			iDeptMode = deptMode; changed();
		}
		
		public String formatDepartment(DepartmentInterface dept) {
			switch (iDeptMode) {
			case CODE:
				return dept.getCode();
			case ABBV:
				return (dept.getAbbv() == null || dept.getAbbv().isEmpty() ? dept.getCode() : dept.getAbbv());
			case NAME:
				return dept.getName();
			case CODE_NAME:
				return dept.getCode() + " - " + dept.getName();
			default:
				return (dept.getAbbv() == null || dept.getAbbv().isEmpty() ? dept.getCode() : dept.getAbbv()) + " - " + dept.getName();
			}
		}

		public String toString() {
			String ret = "";
			if (iCurriculumAbbv) ret += "c";
			if (iAreaAbbv) ret += "a";
			if (iMajorAbbv) ret += "m";
			ret += iDeptMode.getCode();
			return ret;
		}
		
		public void fromString(String str) {
			iCurriculumAbbv = (str.indexOf('c') >= 0);
			iAreaAbbv = (str.indexOf('a') >= 0);
			iMajorAbbv = (str.indexOf('m') >= 0);
			for (DeptMode m: DeptMode.values())
				if (str.indexOf(m.getCode()) >= 0) { iDeptMode = m; break; }
		}
		
		public abstract void changed();
	}
	
	public CurriculumInterface next(Long id) {
		if (id == null) return null;
		for (int row = 0; row < iTable.getRowCount() - 1; row++) {
			CurriculumInterface c = iTable.getData(row);
			if (c != null && id.equals(c.getId()))
				return iTable.getData(1 + row);
		}
		return null;
	}
	
	public CurriculumInterface previous(Long id) {
		if (id == null) return null;
		for (int row = 1; row < iTable.getRowCount(); row++) {
			CurriculumInterface c = iTable.getData(row);
			if (c != null && id.equals(c.getId()))
				return iTable.getData(row - 1);
		}
		return null;
	}
	
	public void addOperations(final CurriculumColumn column, final UniTimeTableHeader header) {
		switch (column) {
		case SELECT:
			header.addAdditionalStyleName("unitime-NoPrint");
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opSelectAll();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					iSelectedCurricula.clear();
					for (int row = 0; row < iTable.getRowCount(); row++) {
						CurriculumInterface c = iTable.getData(row);
						if (c != null && c.isEditable()) {
							iSelectedCurricula.add(c.getId());
							((CheckBox)iTable.getWidget(row, 0)).setValue(true);
						}
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opClearSelection();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					iSelectedCurricula.clear();
					for (int row = 0; row < iTable.getRowCount(); row++) {
						CurriculumInterface c = iTable.getData(row);
						if (c != null && c.isEditable()) {
							((CheckBox)iTable.getWidget(row, 0)).setValue(false);
						}
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opEditRequestedEnrollments();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					if (iSelectedCurricula.size() <= 1 || iEditClassificationHandler == null) return false;
					for (CurriculumInterface c: selected())
						if (!c.hasClassifications()) return false;
					return true;
				}
				@Override
				public void execute() {
					iEditClassificationHandler.doEdit(selected());
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opDeleteSelectedCurricula();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return !iSelectedCurricula.isEmpty();
				}
				@Override
				public void execute() {
					Set<Long> deleteIds = markSelected();
					if (!deleteIds.isEmpty()) {
						if (Window.confirm(deleteIds.size() == 1 ? MESSAGES.confirmDeleteSelectedCurriculum() : MESSAGES.confirmDeleteSelectedCurricula())) {
							LoadingWidget.getInstance().show(MESSAGES.waitDeletingSelectedCurricula());
							iService.deleteCurricula(deleteIds, new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(MESSAGES.failedToDeleteSelectedCurricula(caught.getMessage()), caught);
									unmarkSelected();
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									iSelectedCurricula.clear();
									query(iLastQuery, null);
								}
							});
						} else {
							unmarkSelected();
						}
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opMergeSelectedCurricula();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					if (iSelectedCurricula.size() <= 1) return false;
					Long areaId = null;
					Long deptId = null;
					for (CurriculumInterface c: selected()) {
						if (areaId == null) {
							areaId = c.getAcademicArea().getId();
						} else if (!areaId.equals(c.getAcademicArea().getId())) {
							return false;
						}
						if (deptId == null) {
							deptId = c.getDepartment().getId();
						} else if (!deptId.equals(c.getDepartment().getId())) {
							return false;
						}
					}
					return true;
				}
				@Override
				public void execute() {
					Set<Long> mergeIds = markSelected();
					if (!mergeIds.isEmpty()) {
						if (Window.confirm(mergeIds.size() == 1 ? MESSAGES.confirmMergeSelectedCurriculum() : MESSAGES.confirmMergeSelectedCurricula())) {
							LoadingWidget.getInstance().show(MESSAGES.waitMergingSelectedCurricula());
							iService.mergeCurricula(mergeIds, new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(MESSAGES.failedToMergeSelectedCurricula(caught.getMessage()), caught);
									unmarkSelected();
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									iSelectedCurricula.clear();
									query(iLastQuery, null);
								}
							});
						} else {
							unmarkSelected();
						}
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opCurriculumProjectionRules();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					openCurriculumProjectionRules();
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opUpdateRequestedEnrollmentByProjectionRules();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					Set<Long> curIds = markSelected();
					if (iSelectedCurricula.isEmpty()) curIds = null;
					if (Window.confirm(curIds == null ? iIsAdmin ? MESSAGES.confirmUpdateAllCurricula() : MESSAGES.confirmUpdateYourCurricula() : curIds.size() == 1 ? MESSAGES.confirmUpdateSelectedCurriculum() : MESSAGES.confirmUpdateSelectedCurricula())) {
						LoadingWidget.getInstance().show(MESSAGES.waitUpdatingCurricula(), 300000);
						iService.updateCurriculaByProjections(curIds, false, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(MESSAGES.failedToUpdateCurricula(caught.getMessage()), caught);
								unmarkSelected();
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								query(iLastQuery, null);
							}
						});
					} else {
						unmarkSelected();
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opUpdateRequestedEnrollmentAndCourseProjections();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					Set<Long> curIds = markSelected();
					if (iSelectedCurricula.isEmpty()) curIds = null;
					if (Window.confirm(curIds == null ? iIsAdmin ? MESSAGES.confirmUpdateAllCurricula() : MESSAGES.confirmUpdateYourCurricula() : curIds.size() == 1 ? MESSAGES.confirmUpdateSelectedCurriculum() : MESSAGES.confirmUpdateSelectedCurricula())) {
						LoadingWidget.getInstance().show(MESSAGES.waitUpdatingCurricula(), 300000);
						iService.updateCurriculaByProjections(curIds, true, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(MESSAGES.failedToUpdateCurricula(caught.getMessage()), caught);
								unmarkSelected();
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								query(iLastQuery, null);
							}
						});
					} else {
						unmarkSelected();
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opPopulateCourseProjectedDemands();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return iIsAdmin;
				}
				@Override
				public void execute() {
					if (Window.confirm(MESSAGES.confirmPopulateProjectedDemands())) {
						LoadingWidget.getInstance().show(MESSAGES.waitPopulatingProjectedDemands());
						iService.populateCourseProjectedDemands(false, new AsyncCallback<Boolean>(){

							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(MESSAGES.failedToPopulateProjectedDemands(caught.getMessage()), caught);
								LoadingWidget.getInstance().hide();
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								iSelectedCurricula.clear();
								query(iLastQuery, null);
							}
							
						});
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opPopulateCourseProjectedDemandsIncludeOther();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return iIsAdmin;
				}
				@Override
				public void execute() {
					if (Window.confirm(MESSAGES.confirmPopulateProjectedDemands())) {
						LoadingWidget.getInstance().show(MESSAGES.waitPopulatingProjectedDemands());
						iService.populateCourseProjectedDemands(true, new AsyncCallback<Boolean>(){
							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(MESSAGES.failedToPopulateProjectedDemands(caught.getMessage()), caught);
								LoadingWidget.getInstance().hide();
							}

							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								iSelectedCurricula.clear();
								query(iLastQuery, null);
							}
							
						});
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return (iTable.getRowCount() > 1 ? MESSAGES.opRecreateCurriculaFromLastLike() : MESSAGES.opCreateCurriculaFromLastLike());
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return iIsAdmin;
				}
				@Override
				public void execute() {
					markAll();
					if (Window.confirm(MESSAGES.confirmDeleteAllCurricula())) {
						if (Window.confirm(MESSAGES.confirmDeleteAllCurriculaSecondWarning())) {
							LoadingWidget.getInstance().show(MESSAGES.waitCreatingAllCurricula(), 300000);
							iService.makeupCurriculaFromLastLikeDemands(true, new AsyncCallback<Boolean>(){

								@Override
								public void onFailure(Throwable caught) {
									UniTimeNotifications.error(MESSAGES.failedToCreateCurricula(caught.getMessage()), caught);
									unmarkAll();
									LoadingWidget.getInstance().hide();
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									iSelectedCurricula.clear();
									query(iLastQuery, null);
								}
								
							});
						} else {
							unmarkAll();
						}
					} else {
						unmarkAll();
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return (iTable.getRowCount() > 1 ? MESSAGES.opRecreateCurriculaFromCourseRequests() : MESSAGES.opCreateCurriculaFromCourseRequests());
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return iIsAdmin;
				}
				@Override
				public void execute() {
					markAll();
					if (Window.confirm(MESSAGES.confirmDeleteAllCurricula())) {
						if (Window.confirm(MESSAGES.confirmDeleteAllCurriculaSecondWarning())) {
							LoadingWidget.getInstance().show(MESSAGES.waitCreatingAllCurricula(), 300000);
							iService.makeupCurriculaFromLastLikeDemands(false, new AsyncCallback<Boolean>(){

								@Override
								public void onFailure(Throwable caught) {
									UniTimeNotifications.error(MESSAGES.failedToCreateCurricula(caught.getMessage()), caught);
									unmarkAll();
									LoadingWidget.getInstance().hide();
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									iSelectedCurricula.clear();
									query(iLastQuery, null);
								}
								
							});
						} else {
							unmarkAll();
						}
					} else {
						unmarkAll();
					}
				}
			});
			break;
		case CURRICULUM:
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().getCurriculaDisplayMode().isCurriculumAbbv() ? MESSAGES.opShowNames() : MESSAGES.opShowAbbreviations();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					boolean abbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isCurriculumAbbv();
					CurriculumCookie.getInstance().getCurriculaDisplayMode().setCurriculumAbbv(abbv);
					for (int row = 0; row < iTable.getRowCount(); row++) {
						CurriculumInterface c = iTable.getData(row);
						if (c != null)
							((Label)iTable.getWidget(row, 1)).setText(abbv ? c.getAbbv() : c.getName());
					}
				}
			});
			break;
		case ACAD_AREA:
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().getCurriculaDisplayMode().isAreaAbbv() ? MESSAGES.opShowNames() : MESSAGES.opShowAbbreviations();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					boolean abbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isAreaAbbv();
					CurriculumCookie.getInstance().getCurriculaDisplayMode().setAreaAbbv(abbv);
					for (int row = 0; row < iTable.getRowCount(); row++) {
						CurriculumInterface c = iTable.getData(row);
						if (c != null)
							((Label)iTable.getWidget(row, 2)).setText(abbv ? c.getAcademicArea().getAbbv() : c.getAcademicArea().getName());
					}
				}
			});
			break;
		case MAJORS:
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().getCurriculaDisplayMode().isMajorAbbv() ? MESSAGES.opShowNames() : MESSAGES.opShowAbbreviations();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					boolean abbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isMajorAbbv();
					CurriculumCookie.getInstance().getCurriculaDisplayMode().setMajorAbbv(abbv);
					for (int row = 0; row < iTable.getRowCount(); row++) {
						CurriculumInterface c = iTable.getData(row);
						if (c != null) {
							((HTML)iTable.getWidget(row, 3)).setHTML(abbv ? c.getMajorCodes(", ") : c.getMajorNames("<br>"));
							((HTML)iTable.getWidget(row, 3)).setWordWrap(abbv);
						}
					}
				}
			});
			break;
		case DEPARTMENT:
			for (final DeptMode m: DeptMode.values()) {
				header.addOperation(new Operation() {
					@Override
					public String getName() {
						return MESSAGES.opShowItem(m.getName());
					}
					@Override
					public boolean hasSeparator() {
						return false;
					}
					@Override
					public boolean isApplicable() {
						return m != CurriculumCookie.getInstance().getCurriculaDisplayMode().getDeptMode();
					}
					@Override
					public void execute() {
						CurriculumCookie.getInstance().getCurriculaDisplayMode().setDeptMode(m);
						DisplayMode dm = CurriculumCookie.getInstance().getCurriculaDisplayMode();
						for (int row = 0; row < iTable.getRowCount(); row++) {
							CurriculumInterface c = iTable.getData(row);
							if (c != null)
								((Label)iTable.getWidget(row, 4)).setText(dm.formatDepartment(c.getDepartment()));
						}
					}
				});
			}
			break;
		}
		
		// Hide ops
		switch (column) {
		case SELECT:
		case LAST_LIKE:
		case PROJECTION:
		case REQUESTED:
		case ENROLLED:
		case SNAPSHOT_REQUESTED:
		case SNAPSHOT_PROJECTION:
		case REGISTERED:
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().isShowLast() ? MESSAGES.opHide(MESSAGES.fieldLastLikeEnrollment()) : MESSAGES.opShow(MESSAGES.fieldLastLikeEnrollment());
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					boolean show = !CurriculumCookie.getInstance().isShowLast();
					int col = iTable.getCellCount(0) - 7;
					CurriculumCookie.getInstance().setShowLast(show);
					iTable.setColumnVisible(col, show);
					if (CurriculumCookie.getInstance().isAllHidden()) {
						CurriculumCookie.getInstance().setShowExpected(true);
						iTable.setColumnVisible(iTable.getCellCount(0) - 5, true);
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().isShowProjected() ? MESSAGES.opHide(MESSAGES.fieldProjectedByRule()) : MESSAGES.opShow(MESSAGES.fieldProjectedByRule());
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					boolean show = !CurriculumCookie.getInstance().isShowProjected();
					int col = iTable.getCellCount(0) - 6;
					CurriculumCookie.getInstance().setShowProjected(show);
					iTable.setColumnVisible(col, show);
					if (CurriculumCookie.getInstance().isAllHidden()) {
						CurriculumCookie.getInstance().setShowExpected(true);
						iTable.setColumnVisible(iTable.getCellCount(0) - 5, true);
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().isShowExpected() ? MESSAGES.opHide(MESSAGES.fieldRequestedEnrollment()) : MESSAGES.opShow(MESSAGES.fieldRequestedEnrollment());
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					boolean show = !CurriculumCookie.getInstance().isShowExpected();
					int col = iTable.getCellCount(0) - 5;
					CurriculumCookie.getInstance().setShowExpected(show);
					iTable.setColumnVisible(col, show);
					if (CurriculumCookie.getInstance().isAllHidden()) {
						CurriculumCookie.getInstance().setShowLast(true);
						iTable.setColumnVisible(iTable.getCellCount(0) - 7, true);
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().isShowEnrolled() ? MESSAGES.opHide(MESSAGES.fieldCurrentEnrollment()) : MESSAGES.opShow(MESSAGES.fieldCurrentEnrollment());
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					boolean show = !CurriculumCookie.getInstance().isShowEnrolled();
					int col = iTable.getCellCount(0) - 4;
					CurriculumCookie.getInstance().setShowEnrolled(show);
					iTable.setColumnVisible(col, show);
					if (CurriculumCookie.getInstance().isAllHidden()) {
						CurriculumCookie.getInstance().setShowExpected(true);
						iTable.setColumnVisible(iTable.getCellCount(0) - 5, true);
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().isShowRequested() ? MESSAGES.opHide(MESSAGES.fieldCourseRequests()) : MESSAGES.opShow(MESSAGES.fieldCourseRequests());
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					boolean show = !CurriculumCookie.getInstance().isShowRequested();
					int col = iTable.getCellCount(0) - 3;
					CurriculumCookie.getInstance().setShowRequested(show);
					iTable.setColumnVisible(col, show);
					if (CurriculumCookie.getInstance().isAllHidden()) {
						CurriculumCookie.getInstance().setShowExpected(true);
						iTable.setColumnVisible(iTable.getCellCount(0) - 5, true);
					}
				}
			});
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().isShowSnapshotExpected() ? MESSAGES.opHide(MESSAGES.fieldSnapshotRequestedEnrollment()) : MESSAGES.opShow(MESSAGES.fieldSnapshotRequestedEnrollment());
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
//					return true;
					return(iSessionHasSnapshotData);
				}
				@Override
				public void execute() {
					boolean show = !(iSessionHasSnapshotData && CurriculumCookie.getInstance().isShowSnapshotExpected());
					int col = iTable.getCellCount(0) - 2;
					CurriculumCookie.getInstance().setShowExpected(show);
					iTable.setColumnVisible(col, show);
					if (CurriculumCookie.getInstance().isAllHidden()) {
						CurriculumCookie.getInstance().setShowExpected(true);
						iTable.setColumnVisible(iTable.getCellCount(0) - 5, true);
					}
				}
			});
			
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return CurriculumCookie.getInstance().isShowSnapshotProjected() ? MESSAGES.opHide(MESSAGES.fieldSnapshotProjectedByRule()) : MESSAGES.opShow(MESSAGES.fieldSnapshotProjectedByRule());
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
//					return true;
					return(iSessionHasSnapshotData);
				}
				@Override
				public void execute() {
					boolean show = (iSessionHasSnapshotData && !CurriculumCookie.getInstance().isShowSnapshotProjected());
					int col = iTable.getCellCount(0) - 1;
					CurriculumCookie.getInstance().setShowSnapshotProjected(show);
					iTable.setColumnVisible(col, show);
					if (CurriculumCookie.getInstance().isAllHidden()) {
						CurriculumCookie.getInstance().setShowExpected(true);
						iTable.setColumnVisible(iTable.getCellCount(0) - 5, true);
					}
				}
			});
		}
		switch (column) {
		case PROJECTION:
			header.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.opCurriculumProjectionRules();
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public void execute() {
					openCurriculumProjectionRules();
				}
			});
		}
		
		if (column.getComparator() != null) {
			header.addOperation(new Operation() {
				@Override
				public void execute() {
					iTable.sort(header, column.getComparator());
					CurriculumCookie.getInstance().setSortBy(header.getOrder() ? 1 + column.ordinal() : -1 - column.ordinal()); 
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					switch (column) {
					case DEPARTMENT:
					case LAST_LIKE:
					case REQUESTED:
					case ENROLLED:
					case REGISTERED:
						return true;
					default:
						return false;
					}
				}
				@Override
				public String getName() {
					return MESSAGES.opSortBy(getFieldName(column));
				}
			});
		}
	}
	
	public String getColumnName(CurriculumColumn column) {
		switch (column) {
		case SELECT:
			return "&otimes;";
		case CURRICULUM:
			return MESSAGES.colCurriculum();
		case ACAD_AREA:
			return MESSAGES.colAcademicArea();
		case MAJORS:
			return MESSAGES.colMajors();
		case DEPARTMENT:
			return MESSAGES.colDepartment();
		case LAST_LIKE:
			return MESSAGES.colLastLikeEnrollment();
		case PROJECTION:
			return MESSAGES.colProjectedByRule();
		case REQUESTED:
			return MESSAGES.colRequestedEnrollment();
		case ENROLLED:
			return MESSAGES.colCurrentEnrollment();
		case REGISTERED:
			return MESSAGES.colCourseRequests();
		case SNAPSHOT_REQUESTED:
			return MESSAGES.colSnapshotRequestedEnrollment();
		case SNAPSHOT_PROJECTION:
			return MESSAGES.colSnapshotProjectedByRule();
		default:
			return null;
		}
	}
	
	public HorizontalAlignmentConstant getColumnAlignment(CurriculumColumn column) {
		switch (column) {
		case SELECT:
			return HasHorizontalAlignment.ALIGN_CENTER;
		case LAST_LIKE:
		case PROJECTION:
		case REQUESTED:
		case ENROLLED:
		case SNAPSHOT_REQUESTED:
		case SNAPSHOT_PROJECTION:
		case REGISTERED:
			return HasHorizontalAlignment.ALIGN_RIGHT;
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	public String getColumnWidth(CurriculumColumn column) {
		switch (column) {
		case SELECT:
			return "10px";
		case CURRICULUM:
			return "150px";
		case ACAD_AREA:
		case MAJORS:
		case DEPARTMENT:
			return "100px";
		case LAST_LIKE:
		case PROJECTION:
		case REQUESTED:
		case ENROLLED:
		case SNAPSHOT_REQUESTED:
		case SNAPSHOT_PROJECTION:
		case REGISTERED:
			return "90px";
		default:
			return null;
		}
	}
	
	public String getFieldName(CurriculumColumn column) {
		switch (column) {
		case LAST_LIKE:
			return MESSAGES.fieldLastLikeEnrollment();
		case PROJECTION:
			return MESSAGES.fieldProjectedByRule();
		case REQUESTED:
			return MESSAGES.fieldRequestedEnrollment();
		case ENROLLED:
			return MESSAGES.fieldCurrentEnrollment();
		case REGISTERED:
			return MESSAGES.fieldCourseRequests();
		case SNAPSHOT_REQUESTED:
			return MESSAGES.fieldSnapshotRequestedEnrollment();
		case SNAPSHOT_PROJECTION:
			return MESSAGES.fieldSnapshotProjectedByRule();
		default:
			return getColumnName(column);
		}
	}
	
	public static enum CurriculumColumn implements IsSerializable {
		SELECT,
		CURRICULUM,
		ACAD_AREA,
		MAJORS,
		DEPARTMENT,
		LAST_LIKE,
		PROJECTION,
		REQUESTED,
		ENROLLED,
		REGISTERED,
		SNAPSHOT_REQUESTED,
		SNAPSHOT_PROJECTION
		;
		
		public Comparator<CurriculumInterface> getComparator() {
			switch (this) {
			case CURRICULUM:
				final boolean curAbbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isCurriculumAbbv();
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						int cmp = (curAbbv ? a.getAbbv() : a.getName()).compareTo(curAbbv ? b.getAbbv() : b.getName());
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case ACAD_AREA:
				final boolean areaAbbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isAreaAbbv();
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						int cmp = (areaAbbv ? a.getAcademicArea().getAbbv() : a.getAcademicArea().getName()).compareTo(
								areaAbbv ? b.getAcademicArea().getAbbv() : b.getAcademicArea().getName());
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case MAJORS:
				final boolean mjAbbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isMajorAbbv();
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						int cmp = (mjAbbv ? a.getMajorCodes("|") : a.getMajorNames("|")).compareTo(
								mjAbbv ? b.getMajorCodes("|") : b.getMajorNames("|"));
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case DEPARTMENT:
				final DisplayMode dm = CurriculumCookie.getInstance().getCurriculaDisplayMode();
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						int cmp = dm.formatDepartment(a.getDepartment()).compareTo(dm.formatDepartment(b.getDepartment()));
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case LAST_LIKE:
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						Integer e = (a.getLastLike() == null ? -1 : a.getLastLike());
						Integer f = (b.getLastLike() == null ? -1 : b.getLastLike());
						int cmp = f.compareTo(e);
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case PROJECTION:
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						Integer e = (a.getProjection() == null ? -1 : a.getProjection());
						Integer f = (b.getProjection() == null ? -1 : b.getProjection());
						int cmp = f.compareTo(e);
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case REQUESTED:
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						Integer e = (a.getExpected() == null ? -1 : a.getExpected());
						Integer f = (b.getExpected() == null ? -1 : b.getExpected());
						int cmp = f.compareTo(e);
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case ENROLLED:
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						Integer e = (a.getEnrollment() == null ? -1 : a.getEnrollment());
						Integer f = (b.getEnrollment() == null ? -1 : b.getEnrollment());
						int cmp = f.compareTo(e);
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case REGISTERED:
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						Integer e = (a.getRequested() == null ? -1 : a.getRequested());
						Integer f = (b.getRequested() == null ? -1 : b.getRequested());
						int cmp = f.compareTo(e);
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case SNAPSHOT_REQUESTED:
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						Integer e = (a.getSnapshotExpected() == null ? -1 : a.getSnapshotExpected());
						Integer f = (b.getSnapshotExpected() == null ? -1 : b.getSnapshotExpected());
						int cmp = f.compareTo(e);
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			case SNAPSHOT_PROJECTION:
				return new Comparator<CurriculumInterface>() {
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						Integer e = (a.getSnapshotProjection() == null ? -1 : a.getSnapshotProjection());
						Integer f = (b.getSnapshotProjection() == null ? -1 : b.getSnapshotProjection());
						int cmp = f.compareTo(e);
						if (cmp != 0) return cmp;
						return a.compareTo(b);
					}
				};
			default:
				return null;
			}
		}
	}	
}
