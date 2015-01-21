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
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.curricula.CurriculumEdit.EditFinishedEvent;
import org.unitime.timetable.gwt.client.curricula.CurriculumEdit.EditFinishedHandler;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumCourseInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CourseCurriculaTable extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private final CurriculaServiceAsync iCurriculaService = GWT.create(CurriculaService.class);

	private SimpleForm iCurriculaPanel;
	private MyFlexTable iCurricula;
	private DialogBox iDialog;
	private CurriculumEdit iCurriculumEdit;
	private Label iHint;
	
	private AsyncCallback<TreeSet<CurriculumInterface>> iCourseCurriculaCallback = null;
	
	private TreeSet<AcademicClassificationInterface> iClassifications = null;
	
	private TreeSet<CourseInterface> iCourses = new TreeSet<CourseInterface>();
	private List<ChainedCommand> iRowClicks = new ArrayList<ChainedCommand>();
	private List<Integer> iRowTypes = new ArrayList<Integer>();
	private List<Long> iRowAreaId = new ArrayList<Long>();
	
	private Long iOfferingId = null;
	private String iCourseName = null;
	private boolean[] iUsed = null;
	private HashSet<Long> iExpandedAreas = new HashSet<Long>();
	private HashSet<Long> iAllAreas = new HashSet<Long>();
	private int iSelectedRow = -1;
	private boolean iEditable = true;
	
	private ClickHandler iMenu;
	private UniTimeHeaderPanel iHeader;
	
	public static enum Type {
		EXP (MESSAGES.shortRequestedEnrollment()),
		ENRL (MESSAGES.shortCurrentEnrollment()),
		LAST (MESSAGES.shortLastLikeEnrollment()),
		PROJ (MESSAGES.shortProjectedByRule()),
		REQ (MESSAGES.shortCourseRequests()),
		EXP2ENRL (MESSAGES.shortRequestedEnrollment() + " / " + MESSAGES.shortCurrentEnrollment()),
		EXP2LAST (MESSAGES.shortRequestedEnrollment() + " / " + MESSAGES.shortLastLikeEnrollment()),
		EXP2PROJ (MESSAGES.shortRequestedEnrollment() + " / " + MESSAGES.shortProjectedByRule()),
		LAST2ENRL (MESSAGES.shortLastLikeEnrollment() + " / " + MESSAGES.shortCurrentEnrollment()),
		PROJ2ENRL (MESSAGES.shortProjectedByRule() + " / " + MESSAGES.shortCurrentEnrollment()),
		EXP2REQ (MESSAGES.shortRequestedEnrollment() + " / " + MESSAGES.shortCourseRequests()),
		LAST2REQ (MESSAGES.shortLastLikeEnrollment() + " / " + MESSAGES.shortCourseRequests()),
		PROJ2REQ (MESSAGES.shortProjectedByRule() + " / " + MESSAGES.shortCourseRequests()),
		ENRL2REQ (MESSAGES.shortCurrentEnrollment() + " / " + MESSAGES.shortCourseRequests()),
		;

		private String iName;
		
		Type(String name) { iName = name; }
		
		public String getName() { return iName; }
	}

	private static int sRowTypeHeader = 0;
	private static int sRowTypeArea = 1;
	private static int sRowTypeCurriculum = 2;
	private static int sRowTypeOtherArea = 3;
	private static int sRowTypeOther = 4;
	private static int sRowTypeTotal = 5;
	
	public CourseCurriculaTable(boolean editable, final boolean showHeader) {
		iEditable = editable;
		
		iCurriculaPanel = new SimpleForm();
		iCurriculaPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iMenu = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PopupPanel popup = new PopupPanel(true);
				MenuBar menu = createMenu(popup, showHeader);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
				menu.focus();
			}
		};
		
		iHeader = new UniTimeHeaderPanel(showHeader ? MESSAGES.headerCurricula() : "");
		iHeader.setCollapsible(showHeader ? CurriculumCookie.getInstance().getCurriculaCoursesDetails() : null);
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		if (showHeader) {
			iHeader.addButton("operations", MESSAGES.buttonCurriculaOperations(), (Integer)null, iMenu);
			iHeader.setEnabled("operations", false);
			iHeader.getElement().getStyle().setMarginTop(10, Unit.PX);
		}
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				CurriculumCookie.getInstance().setCurriculaCoursesDetails(event.getValue());
				if (iCurricula.getRowCount() == 0) {
					refresh();
				} else if (iCurricula.getRowCount() > 2) {
					for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
						int rowType = iRowTypes.get(row);
						if (CurriculumCookie.getInstance().getCurriculaCoursesDetails() && (rowType == sRowTypeCurriculum || rowType == sRowTypeOtherArea)) continue;
						iCurricula.getRowFormatter().setVisible(row, CurriculumCookie.getInstance().getCurriculaCoursesDetails());
					}
					for (int col = 0; col < iClassifications.size()  + 2; col++) {
						iCurricula.getCellFormatter().setStyleName(iCurricula.getRowCount() - 1, col, CurriculumCookie.getInstance().getCurriculaCoursesDetails() ? "unitime-TotalRow" : null );
					}
				}
			}
		});
		
		if (showHeader)
			iCurriculaPanel.addHeaderRow(iHeader);

		VerticalPanel tableAndHint = new VerticalPanel();
		
		iCurricula = new MyFlexTable();
		tableAndHint.add(iCurricula);
		
		iHint = new Label(MESSAGES.hintEnrollmentOfType(CurriculumCookie.getInstance().getCourseCurriculaTableType().getName()));
		iHint.setStyleName("unitime-Hint");
		iHint.setVisible(false);
		tableAndHint.add(iHint);
		tableAndHint.setCellHorizontalAlignment(iHint, HasHorizontalAlignment.ALIGN_RIGHT);
		iCurriculaPanel.addRow(tableAndHint);
		iHint.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				for (int i = 1; i < Type.values().length; i++) {
					Type type = Type.values()[(CurriculumCookie.getInstance().getCourseCurriculaTableType().ordinal() + i) % Type.values().length];
					if (isAvailable(type)) {
						CurriculumCookie.getInstance().setCourseCurriculaTableType(type);
						break;
					}
				}
				iHint.setText(MESSAGES.hintEnrollmentOfType(CurriculumCookie.getInstance().getCourseCurriculaTableType().getName()));
				if (iCurricula.getRowCount() > 1) {
					for (int row = 1; row < iCurricula.getRowCount(); row++) {
						for (int col = 0; col <= iClassifications.size(); col++) {
							((MyLabel)iCurricula.getWidget(row, getHeaderCols(row) + col)).refresh();
						}
					}
					//((MyLabel)iCurricula.getWidget(iCurricula.getRowCount() - 1, 1)).refresh();
					((Label)iCurricula.getWidget(iCurricula.getRowCount() - 1, 0)).setText(MESSAGES.totalEnrollmentOfType(CurriculumCookie.getInstance().getCourseCurriculaTableType().getName()));
				}
			}
		});
		
		if (!showHeader)
			iCurriculaPanel.addRow(iHeader);
		
		initWidget(iCurriculaPanel);
	}
	
	private MenuBar createMenu(final PopupPanel popup, final boolean showHeader) {
		MenuBar menu = new MenuBar(true);
		MenuItem showHide = new MenuItem(CurriculumCookie.getInstance().getCurriculaCoursesDetails() ? MESSAGES.opHideDetails() : MESSAGES.opShowDetails(), true, new Command() {
			@Override
			public void execute() {
				popup.hide();
				CurriculumCookie.getInstance().setCurriculaCoursesDetails(!CurriculumCookie.getInstance().getCurriculaCoursesDetails());
				iHeader.setCollapsible(showHeader ? CurriculumCookie.getInstance().getCurriculaCoursesDetails() : null);
				if (iCurricula.getRowCount() > 2) {
					for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
						int rowType = iRowTypes.get(row);
						if (CurriculumCookie.getInstance().getCurriculaCoursesDetails() && (rowType == sRowTypeCurriculum || rowType == sRowTypeOtherArea)) continue;
						iCurricula.getRowFormatter().setVisible(row, CurriculumCookie.getInstance().getCurriculaCoursesDetails());
					}
					for (int col = 0; col < iClassifications.size()  + 2; col++) {
						iCurricula.getCellFormatter().setStyleName(iCurricula.getRowCount() - 1, col, CurriculumCookie.getInstance().getCurriculaCoursesDetails() ? "unitime-TotalRow" : null );
					}
				}
			}
		});
		showHide.getElement().getStyle().setCursor(Cursor.POINTER);
		menu.addItem(showHide);
		if (iCurricula.getRowCount() > 2 && CurriculumCookie.getInstance().getCurriculaCoursesDetails()) {
			boolean canExpand = false, canCollapse = false;
			for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
				int rowType = iRowTypes.get(row);
				if (rowType == sRowTypeArea || rowType == sRowTypeOther) {
					if (iCurricula.getRowFormatter().isVisible(row))
						canExpand = true;
					else 
						canCollapse = true;
				}
			}
			if (canExpand) {
				MenuItem expandAll = new MenuItem(MESSAGES.opExpandAll(), true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
							int rowType = iRowTypes.get(row);
							boolean visible = (rowType != sRowTypeArea && rowType != sRowTypeOther);
							iCurricula.getRowFormatter().setVisible(row, visible);
							iExpandedAreas.clear();
							iExpandedAreas.addAll(iAllAreas);
						}
					}
				});
				expandAll.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(expandAll);
			}
			if (canCollapse) {
				MenuItem collapseAll = new MenuItem(MESSAGES.opCollapseAll(), true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
							int rowType = iRowTypes.get(row);
							boolean visible = (rowType != sRowTypeCurriculum && rowType != sRowTypeOtherArea);
							iCurricula.getRowFormatter().setVisible(row, visible);
							iExpandedAreas.clear();
						}
					}
				});
				collapseAll.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(collapseAll);
			}
		}
		menu.addSeparator();
		for (final Type t : Type.values()) {
			if (!isAvailable(t)) continue;
			MenuItem item = new MenuItem(
					MESSAGES.opShowEnrollmentByType(t.getName()),
					true,
					new Command() {
						@Override
						public void execute() {
							popup.hide();
							CurriculumCookie.getInstance().setCourseCurriculaTableType(t);
							iHint.setText(MESSAGES.hintEnrollmentOfType(t.getName()));
							if (iCurricula.getRowCount() > 1) {
								for (int row = 1; row < iCurricula.getRowCount(); row++) {
									int hc = getHeaderCols(row);
									for (int col = 0; col <= iClassifications.size(); col++) {
										((MyLabel)iCurricula.getWidget(row, hc + col)).refresh();
									}
								}
								//((MyLabel)iCurricula.getWidget(iCurricula.getRowCount() - 1, 1)).refresh();
								((Label)iCurricula.getWidget(iCurricula.getRowCount() - 1, 0)).setText(MESSAGES.totalEnrollmentOfType(t.getName()));
							}
						}
					});
			if (t == CurriculumCookie.getInstance().getCourseCurriculaTableType())
				item.getElement().getStyle().setColor("#666666");
			item.getElement().getStyle().setCursor(Cursor.POINTER);
			menu.addItem(item);
		}
		menu.addSeparator();
		MenuItem populateProjectedDemands = new MenuItem(MESSAGES.opPopulateCourseProjectedDemands(), true, new Command() {
			@Override
			public void execute() {
				popup.hide();
				LoadingWidget.getInstance().show(MESSAGES.waitPopulateCourseProjectedDemands());
				iCurriculaService.populateCourseProjectedDemands(false, iOfferingId, new AsyncCallback<Boolean>(){

					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedPopulateCourseProjectedDemands(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedPopulateCourseProjectedDemands(caught.getMessage()), caught);
						LoadingWidget.getInstance().hide();
					}

					@Override
					public void onSuccess(Boolean result) {
						ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?io=" + iOfferingId);
					}
					
				});
			}
		});
		populateProjectedDemands.getElement().getStyle().setCursor(Cursor.POINTER);
		menu.addItem(populateProjectedDemands);
		MenuItem populateProjectedDemands2 = new MenuItem(MESSAGES.opPopulateCourseProjectedDemandsIncludeOther(), true, new Command() {
			@Override
			public void execute() {
				popup.hide();
				LoadingWidget.getInstance().show(MESSAGES.waitPopulateCourseProjectedDemands());
				iCurriculaService.populateCourseProjectedDemands(true, iOfferingId, new AsyncCallback<Boolean>(){
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedPopulateCourseProjectedDemands(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedPopulateCourseProjectedDemands(caught.getMessage()), caught);
						LoadingWidget.getInstance().hide();
					}

					@Override
					public void onSuccess(Boolean result) {
						ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.do?io=" + iOfferingId);
					}
					
				});
			}
		});
		populateProjectedDemands2.getElement().getStyle().setCursor(Cursor.POINTER);
		menu.addItem(populateProjectedDemands2);
		menu.setVisible(true);
		menu.setFocusOnHoverEnabled(true);
		return menu;
	}
	
	private void openDialog(final CurriculumInterface curriculum, final ConditionalCommand next) {
		if (iDialog == null) {
			iDialog = new UniTimeDialogBox(true, true);
			iCurriculumEdit = new CurriculumEdit(null);
			ScrollPanel panel = new ScrollPanel(iCurriculumEdit);
			// panel.setSize(Math.round(0.9 * Window.getClientWidth()) + "px", Math.round(0.9 * Window.getClientHeight()) + "px");
			panel.setStyleName("unitime-ScrollPanel");
			iDialog.setWidget(panel);
			iCurriculumEdit.addEditFinishedHandler(new CurriculumEdit.EditFinishedHandler() {
				@Override
				public void onSave(EditFinishedEvent evt) {
					iDialog.hide();
					refresh();
				}
				@Override
				public void onDelete(EditFinishedEvent evt) {
					iDialog.hide();
					refresh();
				}
				@Override
				public void onBack(EditFinishedEvent evt) {
					if (iSelectedRow >= 0) {
						iCurricula.getRowFormatter().setStyleName(iSelectedRow, null);	
					}
					iDialog.hide();
				}
			});
			iCurriculumEdit.setupClassifications(iClassifications);
			iCurriculaService.loadAcademicAreas(new AsyncCallback<TreeSet<AcademicAreaInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
					iHeader.setErrorMessage(MESSAGES.failedToLoadAcademicAreas(caught.getMessage()));
					UniTimeNotifications.error(MESSAGES.failedToLoadAcademicAreas(caught.getMessage()), caught);
					next.executeOnFailure();
				}
				@Override
				public void onSuccess(TreeSet<AcademicAreaInterface> result) {
					iCurriculumEdit.setupAreas(result);
					iCurriculaService.loadDepartments(new AsyncCallback<TreeSet<DepartmentInterface>>() {
						@Override
						public void onFailure(Throwable caught) {
							iHeader.setErrorMessage(MESSAGES.failedToLoadDepartments(caught.getMessage()));
							UniTimeNotifications.error(MESSAGES.failedToLoadDepartments(caught.getMessage()), caught);
							next.executeOnFailure();
						}
						@Override
						public void onSuccess(TreeSet<DepartmentInterface> result) {
							iCurriculumEdit.setupDepartments(result);
							iDialog.setText(curriculum.getName());
							iCurriculumEdit.edit(curriculum, false);
							iCurriculumEdit.showOnlyCourses(iCourses);
							iDialog.center();
							next.executeOnSuccess();
						}
					});
				}
			});
		} else {
			iDialog.setText(curriculum.getName());
			iCurriculumEdit.edit(curriculum, false);
			iCurriculumEdit.showOnlyCourses(iCourses);
			iDialog.center();
			next.executeOnSuccess();
		}
		iCurriculumEdit.addEditFinishedHandler(new EditFinishedHandler() {
			@Override
			public void onSave(EditFinishedEvent evt) {
				refresh();
			}
			@Override
			public void onDelete(EditFinishedEvent evt) {
			}
			@Override
			public void onBack(EditFinishedEvent evt) {
			}
		});

	}
	
	protected void ensureInitialized(final AsyncCallback<Boolean> callback) {
		if (iClassifications != null)
			callback.onSuccess(true);
		iCurriculaService.loadAcademicClassifications(new AsyncCallback<TreeSet<AcademicClassificationInterface>>() {
			@Override
			public void onSuccess(TreeSet<AcademicClassificationInterface> result) {
				iClassifications = result;
				if (callback != null) callback.onSuccess(true);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedToLoadClassifications(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadClassifications(caught.getMessage()), caught);
				if (callback != null) callback.onFailure(caught);
			}
		});
	}
	
	public void clear(boolean loading) {
		for (int row = iCurricula.getRowCount() - 1; row >= 0; row--) {
			iCurricula.removeRow(row);
		}
		iCurricula.clear(true);
		if (loading)
			iHeader.showLoading();
		else
			iHeader.clearMessage();
		iHint.setVisible(false);
	}
	
	protected void populate(TreeSet<CurriculumInterface> curricula) {
		// Create header
		int col = 0;
		final Label curriculumLabel = new Label(MESSAGES.colCurriculum(), false);
		curriculumLabel.addClickHandler(iMenu);
		iCurricula.setWidget(0, col, curriculumLabel);
		iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iCurricula.getFlexCellFormatter().setWidth(0, col, "100px");
		col++;
		
		final Label areaLabel = new Label(MESSAGES.colAcademicArea(), false);
		areaLabel.addClickHandler(iMenu);
		iCurricula.setWidget(0, col, areaLabel);
		iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iCurricula.getFlexCellFormatter().setWidth(0, col, "100px");
		col++;
		
		final Label majorLabel = new Label(MESSAGES.colMajors(), false);
		majorLabel.addClickHandler(iMenu);
		iCurricula.setWidget(0, col, majorLabel);
		iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iCurricula.getFlexCellFormatter().setWidth(0, col, "100px");
		col++;
		
		for (AcademicClassificationInterface clasf: iClassifications) {
			final Label clasfLabel = new Label(clasf.getCode());
			clasfLabel.addClickHandler(iMenu);
			iCurricula.setWidget(0, col, clasfLabel);
			iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
			iCurricula.getFlexCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_RIGHT);
			iCurricula.getFlexCellFormatter().setWidth(0, col, "75px");
			col++;
		}
		
		final Label totalLabel = new Label(MESSAGES.colTotal(), false);
		totalLabel.addClickHandler(iMenu);
		iCurricula.setWidget(0, col, totalLabel);
		iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iCurricula.getFlexCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_RIGHT);
		iCurricula.getFlexCellFormatter().setWidth(0, col, "75px");
		col++;

		// Create body
		iCourses.clear();
		iRowClicks.clear();
		iRowClicks.add(null); // for header row
		iRowTypes.clear();
		iRowTypes.add(sRowTypeHeader);
		iRowAreaId.clear();
		iRowAreaId.add(-2l);
		
		int row = 0;
		List<CurriculumInterface> otherCurricula = new ArrayList<CurriculumInterface>();
		List<CurriculumInterface> lastArea = new ArrayList<CurriculumInterface>();
		iAllAreas.clear();
		iUsed = new boolean[iClassifications.size()];
		for (int i = 0; i < iUsed.length; i++)
			iUsed[i] = false;
		int[][] total = new int[iClassifications.size()][];
		for (int i = 0; i <total.length; i++)
			total[i] = new int[] {0, 0, 0, 0, 0};
		int[][] totalThisArea = new int[iClassifications.size()][];
		for (int i = 0; i <totalThisArea.length; i++)
			totalThisArea[i] = new int[] {0, 0, 0, 0, 0};
		
		for (final CurriculumInterface curriculum: curricula) {
			for (CourseInterface course: curriculum.getCourses()) {
				CourseInterface cx = new CourseInterface();
				cx.setId(course.getId()); cx.setCourseName(course.getCourseName());
				iCourses.add(cx);
			}
			if (curriculum.getId() == null) { otherCurricula.add(curriculum); continue; }
			
			iAllAreas.add(curriculum.getAcademicArea().getId());
			if (lastArea.isEmpty() || lastArea.get(0).getAcademicArea().equals(curriculum.getAcademicArea())) {
				lastArea.add(curriculum);
			} else if (!lastArea.equals(curriculum.getAcademicArea())) {
				col = 0; row++;
				iCurricula.getFlexCellFormatter().setColSpan(row, col, 3);
				//iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
				iCurricula.setWidget(row, col++, new HTML("<i>" + lastArea.get(0).getAcademicArea().getAbbv() + " - " + lastArea.get(0).getAcademicArea().getName() + " (" + lastArea.size() + ")</i>", false));
				int tExp = 0, tLast = 0, tEnrl = 0, tProj = 0, tReq = 0;
				for (int clasfIdx = 0; clasfIdx < iClassifications.size(); clasfIdx++) {
					int exp = totalThisArea[clasfIdx][0];
					int last = totalThisArea[clasfIdx][1];
					int enrl = totalThisArea[clasfIdx][2];
					int proj = totalThisArea[clasfIdx][3];
					int req = totalThisArea[clasfIdx][4];
					tExp += exp;
					tLast += last;
					tEnrl += enrl;
					tProj += proj;
					tReq += req;
					iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj, req));
					iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
					col++;
				}
				iCurricula.setWidget(row, col, new MyLabel(tExp, tEnrl, tLast, tProj, tReq));
				iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				
				final int finalRow = row;
				final int lastAreas = lastArea.size();
				final Long lastAreaId = lastArea.get(0).getAcademicArea().getId();
				iRowClicks.add(new ChainedCommand() {
					@Override
					public void execute(final ConditionalCommand next) {
						iExpandedAreas.add(lastAreaId);
						iCurricula.getRowFormatter().setVisible(finalRow, false);
						for (int row = 1; row <= lastAreas; row++) {
							iCurricula.getRowFormatter().setVisible(finalRow - row, true);
						}
						if (next != null)
							next.executeOnSuccess();
					}

					@Override
					public String getLoadingMessage() {
						return null;
					}
				});
				iRowTypes.add(sRowTypeArea);
				iRowAreaId.add(lastAreaId);
				lastArea.clear();
				for (int i = 0; i <totalThisArea.length; i++)
					totalThisArea[i] = new int[] {0, 0, 0, 0, 0};
				lastArea.add(curriculum);
			}
			col = 0; row++;
			iCurricula.setText(row, col++, curriculum.getAbbv());
			iCurricula.setText(row, col++, curriculum.getAcademicArea().getAbbv());
			iCurricula.setText(row, col++, curriculum.getMajorCodes(", "));
			int clasfIdx = 0;
			int tExp = 0, tLast = 0, tEnrl = 0, tProj = 0, tReq = 0;
			for (AcademicClassificationInterface clasf: iClassifications) {
				CurriculumClassificationInterface f = null;
				for (CurriculumClassificationInterface x: curriculum.getClassifications()) {
					if (x.getAcademicClassification().getId().equals(clasf.getId())) { f = x; break; }
				}
				int exp = 0, last = 0, enrl = 0, proj = 0, req = 0;
				for (CourseInterface course: curriculum.getCourses()) {
					CurriculumCourseInterface cx = course.getCurriculumCourse(clasfIdx);
					if (cx != null) {
						iUsed[clasfIdx] = true;
						exp += (f == null || f.getExpected() == null ? 0 : Math.round(f.getExpected() * cx.getShare()));
						last += (cx.getLastLike() == null ? 0 : cx.getLastLike());
						enrl += (cx.getEnrollment() == null ? 0 : cx.getEnrollment());
						proj += (cx.getProjection() == null ? 0 : cx.getProjection());
						req += (cx.getRequested() == null ? 0 : cx.getRequested());
					}
				}
				total[clasfIdx][0] += exp;
				total[clasfIdx][1] += last;
				total[clasfIdx][2] += enrl;
				total[clasfIdx][3] += proj;
				total[clasfIdx][4] += req;
				totalThisArea[clasfIdx][0] += exp;
				totalThisArea[clasfIdx][1] += last;
				totalThisArea[clasfIdx][2] += enrl;
				totalThisArea[clasfIdx][3] += proj;
				totalThisArea[clasfIdx][4] += req;
				tExp += exp;
				tLast += last;
				tEnrl += enrl;
				tProj += proj;
				tReq += req;
				iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj, req));
				iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				col++;
				clasfIdx++;
			}
			iCurricula.setWidget(row, col, new MyLabel(tExp, tEnrl, tLast, tProj, tReq));
			iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
			if (iEditable) {
				iRowClicks.add(new ChainedCommand() {
					@Override
					public void execute(final ConditionalCommand next) {
						iCurriculaService.loadCurriculum(curriculum.getId(), new AsyncCallback<CurriculumInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								iHeader.setErrorMessage(MESSAGES.failedLoadDetails(curriculum.getAbbv(), caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedLoadDetails(curriculum.getAbbv(), caught.getMessage()), caught);
								next.executeOnFailure();
							}
							@Override
							public void onSuccess(CurriculumInterface result) {
								openDialog(result, next);
							}
						});
					}
					@Override
					public String getLoadingMessage() {
						return MESSAGES.waitLoadingDetailsOf(curriculum.getName());
					}
				});
			} else {
				final Long lastAreaId = curriculum.getAcademicArea().getId();
				final int finalRow = row;
				iRowClicks.add(new ChainedCommand() {
					@Override
					public void execute(final ConditionalCommand next) {
						int row = finalRow;
						while (row > 0 && iRowTypes.get(row) == sRowTypeCurriculum) {
							iCurricula.getRowFormatter().setVisible(row, false);
							row --;
						}
						row = finalRow + 1;
						while (iRowTypes.get(row) == sRowTypeCurriculum) {
							iCurricula.getRowFormatter().setVisible(row, false);
							row ++;
						}
						iCurricula.getRowFormatter().setVisible(row, true);
						iExpandedAreas.remove(lastAreaId);
						if (next != null)
							next.executeOnSuccess();
					}
					@Override
					public String getLoadingMessage() {
						return null;
					}
				});
			}
			iRowTypes.add(sRowTypeCurriculum);
			iRowAreaId.add(curriculum.getAcademicArea().getId());
		}
		if (!lastArea.isEmpty()) {
			col = 0; row++;
			iCurricula.getFlexCellFormatter().setColSpan(row, col, 3);
			//iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
			iCurricula.setWidget(row, col++, new HTML("<i>" + lastArea.get(0).getAcademicArea().getAbbv() + " - " + lastArea.get(0).getAcademicArea().getName() + " (" + lastArea.size() + ")</i>", false));
			int tExp = 0, tLast = 0, tEnrl = 0, tProj = 0, tReq = 0;
			for (int clasfIdx = 0; clasfIdx < iClassifications.size(); clasfIdx++) {
				int exp = totalThisArea[clasfIdx][0];
				int last = totalThisArea[clasfIdx][1];
				int enrl = totalThisArea[clasfIdx][2];
				int proj = totalThisArea[clasfIdx][3];
				int req = totalThisArea[clasfIdx][4];
				tExp += exp;
				tLast += last;
				tEnrl += enrl;
				tProj += proj;
				tReq += req;
				iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj, req));
				iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				col++;
			}
			iCurricula.setWidget(row, col, new MyLabel(tExp, tEnrl, tLast, tProj, tReq));
			iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
			final int finalRow = row;
			final int lastAreas = lastArea.size();
			final Long lastAreaId = lastArea.get(0).getAcademicArea().getId();
			iRowClicks.add(new ChainedCommand() {
				@Override
				public void execute(final ConditionalCommand next) {
					iExpandedAreas.add(lastAreaId);
					iCurricula.getRowFormatter().setVisible(finalRow, false);
					for (int row = 1; row <= lastAreas; row++) {
						iCurricula.getRowFormatter().setVisible(finalRow - row, true);
					}
					if (next != null)
						next.executeOnSuccess();
				}
				@Override
				public String getLoadingMessage() {
					return null;
				}
			});
			iRowTypes.add(sRowTypeArea);
			iRowAreaId.add(lastAreaId);
		}
		
		// Other line
		if (!otherCurricula.isEmpty()) {
			int[][] totalOther = new int[iClassifications.size()][];
			for (int i = 0; i <totalOther.length; i++)
				totalOther[i] = new int[] {0, 0, 0, 0, 0};
			for (CurriculumInterface other: otherCurricula) {
				col = 0; row++;
				iCurricula.getFlexCellFormatter().setColSpan(row, col, 3);
				//iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
				iCurricula.setHTML(row, col, "<i>" + other.getAbbv() + " - " + other.getName() + "</i>");
				iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
				col++;
				int tExp = 0, tLast = 0, tEnrl = 0, tProj = 0, tReq = 0;
				for (int clasfIdx = 0; clasfIdx < iClassifications.size(); clasfIdx++) {
					int exp = 0, last = 0, enrl = 0, proj = 0, req = 0;
					for (CourseInterface course: other.getCourses()) {
						CurriculumCourseInterface cx = course.getCurriculumCourse(clasfIdx);
						if (cx != null) {
							iUsed[clasfIdx] = true;
							exp += 0;
							last += (cx.getLastLike() == null ? 0 : cx.getLastLike());
							enrl += (cx.getEnrollment() == null ? 0 : cx.getEnrollment());
							proj += (cx.getProjection() == null ? 0 : cx.getProjection());
							req += (cx.getRequested() == null ? 0 : cx.getRequested());
						}
					}
					total[clasfIdx][0] += exp;
					total[clasfIdx][1] += last;
					total[clasfIdx][2] += enrl;
					total[clasfIdx][3] += proj;
					total[clasfIdx][4] += req;
					totalOther[clasfIdx][0] += exp;
					totalOther[clasfIdx][1] += last;
					totalOther[clasfIdx][2] += enrl;
					totalOther[clasfIdx][3] += proj;
					totalOther[clasfIdx][4] += req;
					tExp += exp;
					tLast += last;
					tEnrl += enrl;
					tProj += proj;
					tReq += req;
					iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj, req));
					iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
					iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
					col++;
				}
				iCurricula.setWidget(row, col, new MyLabel(tExp, tEnrl, tLast, tProj, tReq));
				iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
				iRowTypes.add(sRowTypeOtherArea);
				iRowAreaId.add(-1l);
				final int finalRow = row;
				iRowClicks.add(new ChainedCommand() {
					@Override
					public void execute(final ConditionalCommand next) {
						int row = finalRow;
						while (row > 0 && iRowTypes.get(row) == sRowTypeOtherArea) {
							iCurricula.getRowFormatter().setVisible(row, false);
							row --;
						}
						row = finalRow + 1;
						while (iRowTypes.get(row) == sRowTypeOtherArea) {
							iCurricula.getRowFormatter().setVisible(row, false);
							row ++;
						}
						iCurricula.getRowFormatter().setVisible(row, true);
						iExpandedAreas.remove(-1l);
						if (next != null)
							next.executeOnSuccess();
					}
					@Override
					public String getLoadingMessage() {
						return null;
					}
				});
			}
			col = 0; row++;
			iCurricula.getFlexCellFormatter().setColSpan(row, col, 3);
			//iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
			iCurricula.setWidget(row, col, new HTML("<i>" + MESSAGES.colOtherStudents() + "</i>", false));
			iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
			col++;
			int tExp = 0, tLast = 0, tEnrl = 0, tProj = 0, tReq = 0;
			for (int clasfIdx = 0; clasfIdx < iClassifications.size(); clasfIdx++) {
				int exp = totalOther[clasfIdx][0];
				int last = totalOther[clasfIdx][1];
				int enrl = totalOther[clasfIdx][2];
				int proj = totalOther[clasfIdx][3];
				int req = totalOther[clasfIdx][4];
				tExp += exp;
				tLast += last;
				tEnrl += enrl;
				tProj += proj;
				tReq += req;
				iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj, req));
				iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
				col++;
			}
			iCurricula.setWidget(row, col, new MyLabel(tExp, tEnrl, tLast, tProj, tReq));
			iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
			iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
			final int finalRow = row;
			final int lastAreas = otherCurricula.size();
			iAllAreas.add(-1l);
			iRowClicks.add(new ChainedCommand() {
				@Override
				public void execute(final ConditionalCommand next) {
					iExpandedAreas.add(-1l);
					iCurricula.getRowFormatter().setVisible(finalRow, false);
					for (int row = 1; row <= lastAreas; row++) {
						iCurricula.getRowFormatter().setVisible(finalRow - row, true);
					}
					if (next != null)
						next.executeOnSuccess();
				}
				@Override
				public String getLoadingMessage() {
					return null;
				}
			});
			iRowTypes.add(sRowTypeOther);
			iRowAreaId.add(-1l);
		}
		
		// Total line
		col = 0; row++;
		iRowClicks.add(new ChainedCommand() {
			@Override
			public void execute(ConditionalCommand next) {
				CurriculumCookie.getInstance().setCurriculaCoursesDetails(!CurriculumCookie.getInstance().getCurriculaCoursesDetails());
				if (iHeader.isCollapsible() != null)
					iHeader.setCollapsible(CurriculumCookie.getInstance().getCurriculaCoursesDetails());
				if (iCurricula.getRowCount() > 2) {
					for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
						int rowType = iRowTypes.get(row);
						if (CurriculumCookie.getInstance().getCurriculaCoursesDetails() && (rowType == sRowTypeCurriculum || rowType == sRowTypeOtherArea)) continue;
						iCurricula.getRowFormatter().setVisible(row, CurriculumCookie.getInstance().getCurriculaCoursesDetails());
					}
					for (int col = 0; col < iClassifications.size()  + 2; col++) {
						iCurricula.getCellFormatter().setStyleName(iCurricula.getRowCount() - 1, col, CurriculumCookie.getInstance().getCurriculaCoursesDetails() ? "unitime-TotalRow" : null );
					}
				}
				if (next != null)
					next.executeOnSuccess();
			}
			@Override
			public String getLoadingMessage() {
				return null;
			}
		});
		iRowTypes.add(sRowTypeTotal);
		iRowAreaId.add(-3l);
		iCurricula.getFlexCellFormatter().setColSpan(row, col, 3);
		iCurricula.setWidget(row, col, new Label(MESSAGES.totalEnrollmentOfType(CurriculumCookie.getInstance().getCourseCurriculaTableType().getName()), false));
		iCurricula.getCellFormatter().setStyleName(row, col, "unitime-TotalRow");
		col++;
		for (int clasfIdx = 0; clasfIdx < iClassifications.size(); clasfIdx++) {
			int exp = total[clasfIdx][0];
			int last = total[clasfIdx][1];
			int enrl = total[clasfIdx][2];
			int proj = total[clasfIdx][3];
			int req = total[clasfIdx][4];
			iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj, req));
			iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
			iCurricula.getCellFormatter().setStyleName(row, col, "unitime-TotalRow");
			col++;
		}
		int[] tx = new int[] {0, 0, 0, 0, 0};
		for (int i = 0; i < total.length; i ++)
			for (int j = 0; j < 5; j++)
				tx[j] += total[i][j];
		iCurricula.setWidget(row, col, new MyLabel(tx[0], tx[2], tx[1], tx[3], tx[4]));
		iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
		iCurricula.getCellFormatter().setStyleName(row, col, "unitime-TotalRow");
		
		// Hide all lines if requested
		if (!CurriculumCookie.getInstance().getCurriculaCoursesDetails()) {
			for (int r = 1; r < iCurricula.getRowCount() - 1; r++) {
				iCurricula.getRowFormatter().setVisible(r, false);
			}
			int r = iCurricula.getRowCount() - 1;
			int hc = getHeaderCols(r);
			for (int c = 0; c < hc + iClassifications.size(); c++) {
				iCurricula.getCellFormatter().setStyleName(r, c, null);
			}
		} else {
			// else collapse all
			for (int r = 1; r < iCurricula.getRowCount() - 1; r++) {
				int rowType = iRowTypes.get(r);
				boolean visible = (rowType != sRowTypeCurriculum && rowType != sRowTypeOtherArea);
				if (iExpandedAreas.contains(iRowAreaId.get(r)))
					visible = !visible;
				iCurricula.getRowFormatter().setVisible(r, visible);
			}
		}

		// Hide not-used classifications
		for (int i = 0; i < iUsed.length; i++) {
			for (int r = 0; r < iCurricula.getRowCount(); r++) {
				if (!iUsed[i]) {
					iCurricula.getCellFormatter().setVisible(r, getHeaderCols(r) + i, false);
				}
			}
		}
		
		boolean typeChanged = false;
		Type type = CurriculumCookie.getInstance().getCourseCurriculaTableType();
		if (type == Type.EXP && tx[0] == 0) {
			if (tx[2] > 0) {
				type = Type.ENRL;
				typeChanged = true;
			} else if (tx[1] > 0) {
				type = Type.LAST;
				typeChanged = true;
			}
		}
		if (type == Type.ENRL && tx[2] == 0) {
			if (tx[0] > 0) {
				type = Type.EXP;
				typeChanged = true;
			} else if (tx[1] > 0) {
				type = Type.LAST;
				typeChanged = true;
			}
		}
		if (type == Type.LAST && tx[1] == 0) {
			if (tx[0] > 0) {
				type = Type.EXP;
				typeChanged = true;
			} else if (tx[2] > 0) {
				type = Type.ENRL;
				typeChanged = true;
			}
		}
		if (type == Type.PROJ && tx[3] == 0) {
			if (tx[0] > 0) {
				type = Type.EXP;
				typeChanged = true;
			} else if (tx[1] > 0) {
				type = Type.ENRL;
				typeChanged = true;
			} else if (tx[2] > 0) {
				type = Type.LAST;
				typeChanged = true;
			}
		}
		if (type == Type.REQ && tx[4] == 0) {
			if (tx[0] > 0) {
				type = Type.EXP;
				typeChanged = true;
			} else if (tx[1] > 0) {
				type = Type.ENRL;
				typeChanged = true;
			} else if (tx[2] > 0) {
				type = Type.LAST;
				typeChanged = true;
			} else if (tx[3] > 0) {
				type = Type.PROJ;
				typeChanged = true;
			}
		}
		if (typeChanged) {
			CurriculumCookie.getInstance().setCourseCurriculaTableType(type);
			iHint.setText(MESSAGES.hintEnrollmentOfType(type.getName()));
			if (iCurricula.getRowCount() > 1) {
				for (int r = 1; r < iCurricula.getRowCount(); r++) {
					int hc = getHeaderCols(r);
					for (int c = 0; c <= iClassifications.size(); c++) {
						((MyLabel)iCurricula.getWidget(r, hc + c)).refresh();
					}
				}
				//((MyLabel)iCurricula.getWidget(iCurricula.getRowCount() - 1, 1)).refresh();
				((Label)iCurricula.getWidget(iCurricula.getRowCount() - 1, 0)).setText(MESSAGES.totalEnrollmentOfType(type.getName()));
			}
		}
		
		iHeader.clearMessage();
		iHeader.setEnabled("operations", true);
		
		iHint.setVisible(true);
	}
	
	private int getHeaderCols(int row) {
		int col = 0;
		int left = 3;
		while (left > 0) {
			left -= iCurricula.getFlexCellFormatter().getColSpan(row, col);
			col ++;
		}
		return col;
	}
	
	private void initCallbacks() {
		if (iCourseCurriculaCallback == null) {
			iCourseCurriculaCallback = new AsyncCallback<TreeSet<CurriculumInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
					iHeader.setErrorMessage(MESSAGES.failedToLoadCurricula(caught.getMessage()));
					UniTimeNotifications.error(MESSAGES.failedToLoadCurricula(caught.getMessage()), caught);
					iHeader.setCollapsible(null);
					CurriculumCookie.getInstance().setCurriculaCoursesDetails(false);
				}
				@Override
				public void onSuccess(TreeSet<CurriculumInterface> result) {
					if (result.isEmpty()) {
						iHeader.setMessage(MESSAGES.offeringHasNoCurricula());
						iHeader.setEnabled("operations", false);
						iHeader.setCollapsible(null);
					} else {
						iHeader.clearMessage();
						iHeader.setEnabled("operations", true);
						populate(result);
					}
				}
			};			
		}
	}
	
	public void refresh() {
		ensureInitialized(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(Boolean result) {
				clear(true);
				if (iOfferingId != null)
					iCurriculaService.findCurriculaForAnInstructionalOffering(iOfferingId, iCourseCurriculaCallback);
				else
					iCurriculaService.findCurriculaForACourse(iCourseName, iCourseCurriculaCallback);
			}
		});
	}
	
	public void insert(final RootPanel panel) {
		initCallbacks();
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		iCourseName = null;
		if (CurriculumCookie.getInstance().getCurriculaCoursesDetails()) {
			refresh();
		} else {
			iHeader.clearMessage();
			iHeader.setCollapsible(false);
		}
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}
	
	public void setCourseName(String courseName) {
		initCallbacks();
		iOfferingId = null;
		iCourseName = courseName;
		refresh();
	}
	
	public boolean isAvailable(Type type) {
		MyLabel label = (MyLabel)iCurricula.getWidget(iCurricula.getRowCount() - 1, iCurricula.getCellCount(iCurricula.getRowCount() - 1) - 1);
		switch (type) {
		case EXP:
			return label.getExpected() > 0;
		case ENRL:
			return label.getEnrolled() > 0;
		case LAST:
			return label.getLastLike() > 0;
		case PROJ:
			return label.getProjected() > 0;
		case EXP2LAST:
			return label.getExpected() > 0 && label.getLastLike() > 0;
		case EXP2ENRL:
			return label.getExpected() > 0 && label.getEnrolled() > 0;
		case EXP2PROJ:
			return label.getExpected() > 0 && label.getProjected() > 0;
		case LAST2ENRL:
			return label.getLastLike() > 0 && label.getEnrolled() > 0;
		case PROJ2ENRL:
			return label.getProjected() > 0 && label.getEnrolled() > 0;
		case REQ:
			return label.getRequested() > 0;
		case EXP2REQ:
			return label.getExpected() > 0 && label.getRequested() > 0;
		case ENRL2REQ:
			return label.getEnrolled() > 0 && label.getRequested() > 0;
		case LAST2REQ:
			return label.getLastLike() > 0 && label.getRequested() > 0;
		case PROJ2REQ:
			return label.getProjected() > 0 && label.getRequested() > 0;
		default:
			return false;
		}
	}
	
	public class MyLabel extends HTML {
		private int iExp, iLast, iEnrl, iProj, iReq;
		
		public MyLabel(int exp, int enrl, int last, int proj, int req) {
			//super(exp > 0 || enrl > 0 || last > 0 ? ((exp > 0 ? exp : "-") + " / " + (enrl > 0 ? enrl : "-") + " / " + (last > 0 ? last : "-")) : "", false);
			super("&nbsp;", false);
			iExp = exp;
			iLast = last;
			iEnrl = enrl;
			iProj = proj;
			iReq = req;
			refresh();
		}
		
		public int getExpected() { return iExp; }
		public int getLastLike() { return iLast; }
		public int getEnrolled() { return iEnrl; }
		public int getProjected() { return iProj; }
		public int getRequested() { return iReq; }
		
		public void showExpected() {
			setHTML(iExp > 0 ? String.valueOf(iExp) : "&nbsp;");
		}
		
		public void showEnrolled() {
			setHTML(iEnrl > 0 ? String.valueOf(iEnrl) : "&nbsp;");
		}

		public void showLastLike() {
			setHTML(iLast > 0 ? String.valueOf(iLast) : "&nbsp;");
		}
		
		public void showProjected() {
			setHTML(iProj > 0 ? String.valueOf(iProj) : "&nbsp;");
		}
		
		public void showRequested() {
			setHTML(iReq > 0 ? String.valueOf(iReq) : "&nbsp;");
		}

		public void showExpectedEnrolled() {
			if (iExp > 0 || iEnrl > 0)
				setHTML((iExp > 0 ? String.valueOf(iExp) : "-") + " / " + (iEnrl > 0 ? String.valueOf(iEnrl) : "-"));
			else
				setHTML("&nbsp;");
		}
		
		public void showExpectedLastLike() {
			if (iExp > 0 || iLast > 0)
				setHTML((iExp > 0 ? String.valueOf(iExp) : "-") + " / " + (iLast > 0 ? String.valueOf(iLast) : "-"));
			else
				setHTML("&nbsp;");
		}

		public void showExpectedProjected() {
			if (iExp > 0 || iProj > 0)
				setHTML((iExp > 0 ? String.valueOf(iExp) : "-") + " / " + (iProj > 0 ? String.valueOf(iProj) : "-"));
			else
				setHTML("&nbsp;");
		}

		public void showLastLikeEnrolled() {
			if (iLast > 0 || iEnrl > 0)
				setHTML((iLast > 0 ? String.valueOf(iLast) : "-") + " / " + (iEnrl > 0 ? String.valueOf(iEnrl) : "-"));
			else
				setHTML("&nbsp;");
		}
		
		public void showProjectedEnrolled() {
			if (iProj > 0 || iEnrl > 0)
				setHTML((iProj > 0 ? String.valueOf(iProj) : "-") + " / " + (iEnrl > 0 ? String.valueOf(iEnrl) : "-"));
			else
				setHTML("&nbsp;");
		}
		
		public void showExpectedRequested() {
			if (iExp > 0 || iReq > 0)
				setHTML((iExp > 0 ? String.valueOf(iExp) : "-") + " / " + (iReq > 0 ? String.valueOf(iReq) : "-"));
			else
				setHTML("&nbsp;");
		}
		
		public void showLastLikeRequested() {
			if (iLast > 0 || iReq > 0)
				setHTML((iLast > 0 ? String.valueOf(iLast) : "-") + " / " + (iReq > 0 ? String.valueOf(iReq) : "-"));
			else
				setHTML("&nbsp;");
		}
		
		public void showProjectedRequested() {
			if (iProj > 0 || iReq > 0)
				setHTML((iProj > 0 ? String.valueOf(iProj) : "-") + " / " + (iReq > 0 ? String.valueOf(iReq) : "-"));
			else
				setHTML("&nbsp;");
		}
		
		public void showEnrolledRequested() {
			if (iEnrl > 0 || iReq > 0)
				setHTML((iEnrl > 0 ? String.valueOf(iEnrl) : "-") + " / " + (iReq > 0 ? String.valueOf(iReq) : "-"));
			else
				setHTML("&nbsp;");
		}

		public void refresh() {
			switch (CurriculumCookie.getInstance().getCourseCurriculaTableType()) {
			case EXP:
				showExpected();
				break;
			case ENRL:
				showEnrolled();
				break;
			case LAST:
				showLastLike();
				break;
			case PROJ:
				showProjected();
				break;
			case EXP2LAST:
				showExpectedLastLike();
				break;
			case EXP2ENRL:
				showExpectedEnrolled();
				break;
			case EXP2PROJ:
				showExpectedProjected();
				break;
			case LAST2ENRL:
				showLastLikeEnrolled();
				break;
			case PROJ2ENRL:
				showProjectedEnrolled();
				break;
			case REQ:
				showRequested();
				break;
			case EXP2REQ:
				showExpectedRequested();
				break;
			case ENRL2REQ:
				showEnrolledRequested();
				break;
			case LAST2REQ:
				showLastLikeRequested();
				break;
			case PROJ2REQ:
				showProjectedRequested();
				break;
			}
		}
		
	}
	
	public class MyFlexTable extends FlexTable {

		public MyFlexTable() {
			super();
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONCLICK);
			setCellPadding(2);
			setCellSpacing(0);
		}
		
		public void onBrowserEvent(Event event) {
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    Element tr = DOM.getParent(td);
		    Element body = DOM.getParent(tr);
		    final int row = DOM.getChildIndex(body, tr);

		    final ChainedCommand command = iRowClicks.get(row);
		    
		    switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				getRowFormatter().setStyleName(row, "unitime-TableRowHover");
				if (command == null) getRowFormatter().getElement(row).getStyle().setCursor(Cursor.AUTO);
				break;
			case Event.ONMOUSEOUT:
				getRowFormatter().setStyleName(row, null);	
				break;
			case Event.ONCLICK:
				if (command == null) break;
				if (command.getLoadingMessage() != null)
					LoadingWidget.getInstance().show(command.getLoadingMessage());
				getRowFormatter().setStyleName(row, "unitime-TableRowSelected");
				iSelectedRow = row;
				command.execute(new ConditionalCommand() {
					@Override
					public void executeOnSuccess() {
						//getRowFormatter().setStyleName(row, null);	
						if (command.getLoadingMessage() != null)
							LoadingWidget.getInstance().hide();
					}
					@Override
					public void executeOnFailure() {
						getRowFormatter().setStyleName(row, "unitime-TableRowHover");	
						if (command.getLoadingMessage() != null)
							LoadingWidget.getInstance().hide();
					}
				});
				break;
			}
		}
	}
	
	public static interface ChainedCommand {
		public void execute(ConditionalCommand command);
		public String getLoadingMessage();
	}

	public static interface ConditionalCommand {
		public void executeOnSuccess();
		public void executeOnFailure();
	}
	
	public void setMessage(String message) {
		iHeader.setMessage(message);
	}
}
