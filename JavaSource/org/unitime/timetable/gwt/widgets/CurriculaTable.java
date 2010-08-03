/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.CurriculumProjectionRules;
import org.unitime.timetable.gwt.client.CurriculumProjectionRules.ProjectionRulesEvent;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.ToolBox;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.DepartmentInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CurriculaTable extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);

	private VerticalPanel iPanel = null;
	private Image iLoadingImage = null;
	private Label iErrorLabel = null;
	private MyFlexTable iTable = null;
	private String iLastQuery = null;

	private List<CurriculumInterface> iData = new ArrayList<CurriculumInterface>();
	
	private AsyncCallback<List<CurriculumClassificationInterface>> iLoadClassifications;
	
	private List<CurriculumClickHandler> iCurriculumClickHandlers = new ArrayList<CurriculumClickHandler>();
	
	private int iLastSort = 0;
	
	private Long iLastCurriculumId = null;
	
	private CurriculaClassifications iClassifications = null;
	private PopupPanel iClassificationsPopup = null;
	
	private HashSet<Long> iSelectedCurricula = new HashSet<Long>();
	
	private boolean iIsAdmin = false;
	
	private EditClassificationHandler iEditClassificationHandler = null;
	
	public CurriculaTable() {
		iTable = new MyFlexTable();
		
		int col = 0;
		
		HTML selectHtml = new HTML("&otimes;");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "10px");
		iTable.getCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER);
		iTable.setWidget(0, col, selectHtml);
		iTable.getCellFormatter().addStyleName(0, 0, "unitime-NoPrint");
		col++;
		selectHtml.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem select = new MenuItem("Select All", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						iSelectedCurricula.clear();
						for (CurriculumInterface c: iData)
							if (c.isEditable()) {
								iSelectedCurricula.add(c.getId());
								((CheckBox)iTable.getWidget(1 + c.getRow(), 0)).setValue(true);
							}
					}
				});
				select.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(select);
				MenuItem clear = new MenuItem("Clear All", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						iSelectedCurricula.clear();
						for (CurriculumInterface c: iData)
							if (c.isEditable()) {
								((CheckBox)iTable.getWidget(1 + c.getRow(), 0)).setValue(false);
							}
					}
				});
				clear.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(clear);
				if (!iSelectedCurricula.isEmpty()) {
					menu.addSeparator();
				}
				if (iSelectedCurricula.size() > 1) {
					boolean allHasClassifications = true;
					for (CurriculumInterface c: iData)
						if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
							if (!c.hasClassifications()) allHasClassifications = false;
						}
					if (iEditClassificationHandler != null && allHasClassifications) {
						MenuItem editClasf = new MenuItem("Edit Requested Enrollments", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								List<CurriculumInterface> selected = new ArrayList<CurriculumInterface>();
								for (CurriculumInterface c: iData)
									if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
										selected.add(c);
									}
								iEditClassificationHandler.doEdit(selected);
							}
						});
						editClasf.getElement().getStyle().setCursor(Cursor.POINTER);
						menu.addItem(editClasf);
					}
				}
				if (!iSelectedCurricula.isEmpty()) {
					MenuItem delete = new MenuItem("Delete Selected Curricula", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							HashSet<Long> deleteIds = new HashSet<Long>();
							for (CurriculumInterface c: iData)
								if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
									deleteIds.add(c.getId());
									iTable.getRowFormatter().setStyleName(1 + c.getRow(), "unitime-TableRowProblem");
								}
							if (!deleteIds.isEmpty()) {
								if (Window.confirm("Do you realy want to delete the selected " + (deleteIds.size() == 1 ? "curriculum" : "curricula") + "?")) {
									LoadingWidget.getInstance().show("Deleting selected curricula ...");
									iService.deleteCurricula(deleteIds, new AsyncCallback<Boolean>() {

										@Override
										public void onFailure(Throwable caught) {
											LoadingWidget.getInstance().hide();
											setError("Unable to delete selected curricula (" + caught.getMessage() + ")");
											for (CurriculumInterface c: iData)
												if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
													iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
												}
										}

										@Override
										public void onSuccess(Boolean result) {
											LoadingWidget.getInstance().hide();
											iSelectedCurricula.clear();
											query(iLastQuery, null);
										}
									});
								} else {
									for (CurriculumInterface c: iData)
										if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
											iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
										}
								}
							}
						}
					});
					delete.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(delete);
				}
				if (iSelectedCurricula.size() > 1) {
					Long areaId = null;
					Long deptId = null;
					boolean canMerge = true;
					for (CurriculumInterface c: iData)
						if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
							if (areaId == null) {
								areaId = c.getAcademicArea().getId();
							} else if (!areaId.equals(c.getAcademicArea().getId())) {
								canMerge = false; break;
							}
							if (deptId == null) {
								deptId = c.getDepartment().getId();
							} else if (!deptId.equals(c.getDepartment().getId())) {
								canMerge = false; break;
							}
						}
					if (canMerge) {
						MenuItem merge = new MenuItem("Merge Selected Curricula", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								HashSet<Long> mergeIds = new HashSet<Long>();
								for (CurriculumInterface c: iData)
									if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
										mergeIds.add(c.getId());
										iTable.getRowFormatter().setStyleName(1 + c.getRow(), "unitime-TableRowProblem");
									}
								if (!mergeIds.isEmpty()) {
									if (Window.confirm("Do you realy want to merge the selected " + (mergeIds.size() == 1 ? "curriculum" : "curricula") + "?")) {
										LoadingWidget.getInstance().show("Merging selected curricula ...");
										iService.mergeCurricula(mergeIds, new AsyncCallback<Boolean>() {

											@Override
											public void onFailure(Throwable caught) {
												LoadingWidget.getInstance().hide();
												setError("Unable to merge selected curricula (" + caught.getMessage() + ")");
												for (CurriculumInterface c: iData)
													if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
														iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
													}
											}

											@Override
											public void onSuccess(Boolean result) {
												LoadingWidget.getInstance().hide();
												iSelectedCurricula.clear();
												query(iLastQuery, null);
											}
										});
									} else {
										for (CurriculumInterface c: iData)
											if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
												iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
											}
									}
								}
							}
						});
						merge.getElement().getStyle().setCursor(Cursor.POINTER);
						menu.addItem(merge);						
					}
				}
				menu.addSeparator();
				MenuItem rules = new MenuItem("Curriculum Projection Rules", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						openCurriculumProjectionRules();
					}
				});
				rules.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(rules);
				MenuItem updateRequested = new MenuItem("Update Requested Enrollment by Projection Rules", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						HashSet<Long> curIds = null;
						if (iSelectedCurricula.isEmpty()) {
							for (CurriculumInterface c: iData)
								if (c.isEditable()) {
									iTable.getRowFormatter().setStyleName(1 + c.getRow(), "unitime-TableRowProblem");
								}
						} else {
							curIds = new HashSet<Long>();
							for (CurriculumInterface c: iData)
								if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
									curIds.add(c.getId());
									iTable.getRowFormatter().setStyleName(1 + c.getRow(), "unitime-TableRowProblem");
								}
						}
						if (Window.confirm("Do you realy want to update " + (curIds == null ? "all " + (iIsAdmin ? "": "your ") + "curricula" : "the selected " + (curIds.size() == 1 ? "curriculum" : "curricula")) + "?")) {
							LoadingWidget.getInstance().show("Updating " + (curIds == null ? "all " + (iIsAdmin ? "": "your ") + "curricula" : "the selected " + (curIds.size() == 1 ? "curriculum" : "curricula")) + " ... " +
									"&nbsp;&nbsp;&nbsp;&nbsp;This could take a while ...", 300000);
							iService.updateCurriculaByProjections(curIds, false, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									setError("Unable to update curricula (" + caught.getMessage() + ")");
									for (CurriculumInterface c: iData)
										if (c.isEditable()) {
											iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
										}
									LoadingWidget.getInstance().hide();
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									query(iLastQuery, null);
								}
							});
						} else {
							for (CurriculumInterface c: iData)
								if (c.isEditable()) {
									iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
								}
						}
					}
				});
				updateRequested.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(updateRequested);
				MenuItem updateRequestedInclCourses = new MenuItem("Update Requested Enrollment And Course Projections", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						HashSet<Long> curIds = null;
						if (iSelectedCurricula.isEmpty()) {
							for (CurriculumInterface c: iData)
								if (c.isEditable()) {
									iTable.getRowFormatter().setStyleName(1 + c.getRow(), "unitime-TableRowProblem");
								}
						} else {
							curIds = new HashSet<Long>();
							for (CurriculumInterface c: iData)
								if (c.isEditable() && iSelectedCurricula.contains(c.getId())) {
									curIds.add(c.getId());
									iTable.getRowFormatter().setStyleName(1 + c.getRow(), "unitime-TableRowProblem");
								}
						}
						if (Window.confirm("Do you realy want to update " + (curIds == null ? "all " + (iIsAdmin ? "": "your ") + "curricula" : "the selected " + (curIds.size() == 1 ? "curriculum" : "curricula")) + "?")) {
							LoadingWidget.getInstance().show("Updating " + (curIds == null ? "all " + (iIsAdmin ? "": "your ") + "curricula" : "the selected " + (curIds.size() == 1 ? "curriculum" : "curricula")) + " ... " +
									"&nbsp;&nbsp;&nbsp;&nbsp;This could take a while ...", 300000);
							iService.updateCurriculaByProjections(curIds, true, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									setError("Unable to update curricula (" + caught.getMessage() + ")");
									for (CurriculumInterface c: iData)
										if (c.isEditable()) {
											iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
										}
									LoadingWidget.getInstance().hide();
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									query(iLastQuery, null);
								}
							});
						} else {
							for (CurriculumInterface c: iData)
								if (c.isEditable()) {
									iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
								}
						}
					}
				});
				updateRequestedInclCourses.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(updateRequestedInclCourses);
				if (iIsAdmin) {
					menu.addSeparator();
					MenuItem populateProjectedDemands = new MenuItem("Populate Course Projected Demands", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							if (Window.confirm("Do you really want to populate projected demands for all courses?")) {
								LoadingWidget.getInstance().show("Populating projected demands for all courses ...");
								iService.populateCourseProjectedDemands(false, new AsyncCallback<Boolean>(){

									@Override
									public void onFailure(Throwable caught) {
										setError("Unable to populate course projected demands (" + caught.getMessage() + ")");
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
					populateProjectedDemands.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(populateProjectedDemands);
					MenuItem populateProjectedDemands2 = new MenuItem("Populate Course Projected Demands (Include Other Students)", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							if (Window.confirm("Do you really want to populate projected demands for all courses?")) {
								LoadingWidget.getInstance().show("Populating projected demands for all courses ... " +
										"&nbsp;&nbsp;&nbsp;&nbsp;You may also go grab a coffee ... &nbsp;&nbsp;&nbsp;&nbsp;This will take a while ...", 300000);
								iService.populateCourseProjectedDemands(true, new AsyncCallback<Boolean>(){
									@Override
									public void onFailure(Throwable caught) {
										setError("Unable to populate course projected demands (" + caught.getMessage() + ")");
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
					populateProjectedDemands2.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(populateProjectedDemands2);
					menu.addSeparator();
					MenuItem makupCurricula = new MenuItem((iTable.getRowCount() > 1 ? "Recreate" : "Create") + " Curricula from Last-Like Enrollments &amp; Projections", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							for (int r = 1; r < iTable.getRowCount(); r++)
								iTable.getRowFormatter().setStyleName(r, "unitime-TableRowProblem");
							if (Window.confirm("This will delete all existing curricula and create them from scratch. Are you sure you want to do it?")) {
								if (Window.confirm("Are you REALLY sure you want to recreate all curricula?")) {
									LoadingWidget.getInstance().show((iTable.getRowCount() > 1 ? "Recreating" : "Creating") + " all curricula ... " +
											"&nbsp;&nbsp;&nbsp;&nbsp;You may also go grab a coffee ... &nbsp;&nbsp;&nbsp;&nbsp;This will take a while ...", 300000);
									iService.makeupCurriculaFromLastLikeDemands(true, new AsyncCallback<Boolean>(){

										@Override
										public void onFailure(Throwable caught) {
											setError("Unable to create curricula (" + caught.getMessage() + ")");
											for (CurriculumInterface c: iData)
												iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
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
									for (CurriculumInterface c: iData)
										iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
								}
							} else {
								for (CurriculumInterface c: iData)
									iTable.getRowFormatter().setStyleName(1 + c.getRow(), (c.getId().equals(iLastCurriculumId) ? "unitime-TableRowSelected" : null));
							}
						}
					});
					makupCurricula.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(makupCurricula);						
				}
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		Label curriculumLabel = new Label("Curriculum");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "100px");
		iTable.setWidget(0, col, curriculumLabel);
		col++;
		curriculumLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem disp = new MenuItem(CurriculumCookie.getInstance().getCurriculaDisplayMode().isCurriculumAbbv() ? "Show Names" : "Show Abbreviations", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						boolean abbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isCurriculumAbbv();
						CurriculumCookie.getInstance().getCurriculaDisplayMode().setCurriculumAbbv(abbv);
						for (int i = 0; i < iData.size(); i ++) {
							CurriculumInterface c = iData.get(i);
							iTable.setText(1 + c.getRow(), 1, abbv ? c.getAbbv() : c.getName());
						}
					}
				});
				disp.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(disp);
				MenuItem sort = new MenuItem("Sort by Curricula", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(1);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		Label areaLabel = new Label("Academic Area");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "150px");
		iTable.setWidget(0, col, areaLabel);
		col++;
		areaLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem disp = new MenuItem(CurriculumCookie.getInstance().getCurriculaDisplayMode().isAreaAbbv() ? "Show Names" : "Show Abbreviations", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						boolean abbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isAreaAbbv();
						CurriculumCookie.getInstance().getCurriculaDisplayMode().setAreaAbbv(abbv);
						for (int i = 0; i < iData.size(); i ++) {
							CurriculumInterface c = iData.get(i);
							iTable.setText(1 + c.getRow(), 2, abbv ? c.getAcademicArea().getAbbv() : c.getAcademicArea().getName());
						}
					}
				});
				disp.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(disp);
				MenuItem sort = new MenuItem("Sort by Academic Area", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(2);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		Label majorsLabel = new Label("Major(s)");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "200px");
		iTable.setWidget(0, col, majorsLabel);
		col++;
		majorsLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem disp = new MenuItem(CurriculumCookie.getInstance().getCurriculaDisplayMode().isMajorAbbv() ? "Show Names" : "Show Codes", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						boolean abbv = !CurriculumCookie.getInstance().getCurriculaDisplayMode().isMajorAbbv();
						CurriculumCookie.getInstance().getCurriculaDisplayMode().setMajorAbbv(abbv);
						for (int i = 0; i < iData.size(); i ++) {
							CurriculumInterface c = iData.get(i);
							iTable.setHTML(1 + c.getRow(), 3, abbv ? c.getMajorCodes(", ") : c.getMajorNames("<br>"));
							if (abbv)
								iTable.getCellFormatter().addStyleName(1 + c.getRow(), 3, "unitime-Wrap");
							else
								iTable.getCellFormatter().removeStyleName(1 + c.getRow(), 3, "unitime-Wrap");
						}
					}
				});
				disp.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(disp);
				MenuItem sort = new MenuItem("Sort by Major(s)", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(3);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		Label deptLabel = new Label("Department");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "250px");
		iTable.setWidget(0, col, deptLabel);
		col++;
		deptLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				for (final DeptMode m: DeptMode.values()) {
					if (m == CurriculumCookie.getInstance().getCurriculaDisplayMode().getDeptMode()) continue;
					MenuItem disp = new MenuItem("Show " + m.getName(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							CurriculumCookie.getInstance().getCurriculaDisplayMode().setDeptMode(m);
							for (int i = 0; i < iData.size(); i ++) {
								CurriculumInterface c = iData.get(i);
								iTable.setText(1 + c.getRow(), 4, CurriculumCookie.getInstance().getCurriculaDisplayMode().formatDepartment(c.getDepartment()));
							}
						}
					});
					disp.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(disp);
				}
				MenuItem sort = new MenuItem("Sort by Department", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(4);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		HTML lastLabel = new HTML("Last-Like<br>Enrollment", false);
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "60px");
		iTable.setWidget(0, col, lastLabel);
		col++;
		lastLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem sort = new MenuItem("Sort by Last-Like Enrollment", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(5);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		HTML projLabel = new HTML("Projection<br>by&nbsp;Rule", false);
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "60px");
		iTable.setWidget(0, col, projLabel);
		col++;
		projLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem sort = new MenuItem("Sort by Projection by Rule", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(6);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				MenuItem rules = new MenuItem("Curriculum Projection Rules", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						openCurriculumProjectionRules();
					}
				});
				rules.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(rules);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		HTML expLabel = new HTML("Requested<br>Enrollment", false);
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "60px");
		iTable.setWidget(0, col, expLabel);
		col++;
		expLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem sort = new MenuItem("Sort by Requested Enrollment", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(7);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		HTML enrlLabel = new HTML("Current<br>Enrollment", false);
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "60px");
		iTable.setWidget(0, col, enrlLabel);
		col++;
		enrlLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem sort = new MenuItem("Sort by Current Enrollment", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(8);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		iPanel = new VerticalPanel();
		
		iPanel.add(iTable);
		
		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.setVisible(false);
		iLoadingImage.getElement().getStyle().setMargin(20, Unit.PX);
		iPanel.add(iLoadingImage);
		iPanel.setCellHorizontalAlignment(iLoadingImage, HasHorizontalAlignment.ALIGN_CENTER);
		iPanel.setCellVerticalAlignment(iLoadingImage, HasVerticalAlignment.ALIGN_MIDDLE);

		iErrorLabel = new Label("No data.");
		iErrorLabel.setStyleName("unitime-Message");
		iPanel.add(iErrorLabel);
		iErrorLabel.setVisible(true);

		initWidget(iPanel);
		
		iLoadClassifications = new AsyncCallback<List<CurriculumClassificationInterface>>() {
			public void onFailure(Throwable caught) {}
			public void onSuccess(List<CurriculumClassificationInterface> classifications) {
				if (iTable.getRowCount() <= 1) return;
				List<CurriculumInterface> curricula = new ArrayList<CurriculumInterface>();
				CurriculumInterface last = null;
				clasf: for (CurriculumClassificationInterface clasf: classifications) {
					if (last != null && last.getId().equals(clasf.getCurriculumId())) {
						last.addClassification(clasf);
						continue clasf;
					}
					for (CurriculumInterface c: iData) {
						if (c.getId().equals(clasf.getCurriculumId())) {
							if (c.hasClassifications()) c.getClassifications().clear();
							c.addClassification(clasf);
							curricula.add(c);
							last = c;
							continue clasf;
						}
					}
				}
				for (CurriculumInterface c: curricula) {
					iTable.setText(1 + c.getRow(), 5, c.getLastLikeString());
					iTable.getFlexCellFormatter().setColSpan(1 + c.getRow(), 5, 1);
					iTable.setText(1 + c.getRow(), 6, c.getProjectionString());
					iTable.setText(1 + c.getRow(), 7, c.getExpectedString());
					iTable.getFlexCellFormatter().setHorizontalAlignment(1 + c.getRow(), 7, HasHorizontalAlignment.ALIGN_RIGHT);
					iTable.setText(1 + c.getRow(), 8, c.getEnrollmentString());
				}
				List<Long> noEnrl = new ArrayList<Long>();
				for (CurriculumInterface c: iData) {
					if (!c.hasClassifications()) {
						noEnrl.add(c.getId());
						if (noEnrl.size() == 1) {
							iTable.setWidget(1 + c.getRow(), 7, new Image(RESOURCES.loading_small()));
							iTable.getFlexCellFormatter().setHorizontalAlignment(1 + c.getRow(), 7, HasHorizontalAlignment.ALIGN_LEFT);
						}
					}
					if (noEnrl.size() >= 10) break;
				}
				if (!noEnrl.isEmpty())
					iService.loadClassifications(noEnrl, iLoadClassifications);
				else if (iLastSort != 0)
					sort(iLastSort);
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
	}
	
	public void setup(List<AcademicClassificationInterface> classifications) {
		iClassifications.setup(classifications);
	}
	
	public void setMessage(String message) {
		iErrorLabel.setStyleName("unitime-Message");
		iErrorLabel.setText(message == null ? "" : message);
		iErrorLabel.setVisible(message != null && !message.isEmpty());
		if (iErrorLabel.isVisible())
			iErrorLabel.getElement().scrollIntoView();
	}
	
	public void setError(String message) {
		iErrorLabel.setStyleName("unitime-ErrorMessage");
		iErrorLabel.setText(message == null ? "" : message);
		iErrorLabel.setVisible(message != null && !message.isEmpty());
		if (iErrorLabel.isVisible())
			iErrorLabel.getElement().scrollIntoView();
	}
	
	public void clear() {
		for (int row = iTable.getRowCount() - 1; row >= 1; row--) {
			iTable.removeRow(row);
		}
		iData.clear();
	}
	
	private void fillRow(CurriculumInterface c) {
		int col = 0;
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
			iTable.setWidget(1 + c.getRow(), col++, ch);
		} else {
			iTable.setText(1 + c.getRow(), col++, "");
		}
		iTable.getCellFormatter().addStyleName(1 + c.getRow(), 0, "unitime-NoPrint");
		DisplayMode m = CurriculumCookie.getInstance().getCurriculaDisplayMode();
		iTable.setText(1 + c.getRow(), col++, m.isCurriculumAbbv() ? c.getAbbv() : c.getName());
		iTable.setText(1 + c.getRow(), col++, m.isAreaAbbv() ? c.getAcademicArea().getAbbv() : c.getAcademicArea().getName());
		
		iTable.setHTML(1 + c.getRow(), col, m.isMajorAbbv() ? c.getMajorCodes(", ") : c.getMajorNames("<br>"));
		if (m.isMajorAbbv())
			iTable.getCellFormatter().addStyleName(1 + c.getRow(), col, "unitime-Wrap");
		col++;
				
		iTable.setText(1 + c.getRow(), col++, m.formatDepartment(c.getDepartment()));
		iTable.getFlexCellFormatter().setHorizontalAlignment(1 + c.getRow(), col, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.setText(1 + c.getRow(), col++, (c.getLastLike() == null ? "" : c.getLastLikeString()));
		iTable.getFlexCellFormatter().setHorizontalAlignment(1 + c.getRow(), col, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.setText(1 + c.getRow(), col++, (c.getProjection() == null ? "" : c.getProjectionString()));
		iTable.getFlexCellFormatter().setHorizontalAlignment(1 + c.getRow(), col, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.setText(1 + c.getRow(), col++, (c.getExpected() == null ? "": c.getExpectedString()));
		iTable.getFlexCellFormatter().setHorizontalAlignment(1 + c.getRow(), col, HasHorizontalAlignment.ALIGN_RIGHT);
		iTable.setText(1 + c.getRow(), col++, (c.getEnrollment() == null ? "" : c.getEnrollmentString()));
	}
	
	public void populate(TreeSet<CurriculumInterface> result, boolean editable) {
		clear();
		
		if (result.isEmpty()) {
			setError("No curricula matching the above filter found.");
			return;
		}
		iData.addAll(result);
		
		setMessage(null);
		
		List<Long> ids = new ArrayList<Long>();
		int row = 0;
		int rowToScroll = -1;
		boolean hasEditable = false;
		HashSet<Long> newlySelected = new HashSet<Long>();
		for (CurriculumInterface curriculum: iData) {
			if (ids.size() < 10 && !curriculum.hasClassifications()) ids.add(curriculum.getId());
			curriculum.setRow(row);
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
		} else {
			iTable.getCellFormatter().setVisible(0, 0, true);
		}
		
		if (rowToScroll >= 0) {
			iTable.getRowFormatter().getElement(rowToScroll).scrollIntoView();
		}
		
		if (!ids.isEmpty())
			iService.loadClassifications(ids, iLoadClassifications);
	}

	public void query(String filter, final Command next) {
		iLastQuery = filter;
		clear();
		setMessage(null);
		iLoadingImage.setVisible(true);
		iService.findCurricula(filter, new AsyncCallback<TreeSet<CurriculumInterface>>() {
			
			@Override
			public void onSuccess(TreeSet<CurriculumInterface> result) {
				iLoadingImage.setVisible(false);
				populate(result, true);
				if (next != null)
					next.execute();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iLoadingImage.setVisible(false);
				setError("Unable to retrieve curricula (" + caught.getMessage() + ").");
				if (next != null)
					next.execute();
			}
		});
	}
	
	public void sort(final int column) {
		iLastSort = column;
		Integer[] x = new Integer[iTable.getRowCount() - 1];
		for (int i = 0; i < x.length; i ++) x[i] = i;
		Arrays.sort(x, new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				int cmp = compareTwoRows(column, a, b);
				if (cmp != 0) return cmp;
				return compareTwoRows(1, a, b);
			}
		});
		for (int i = 0; i < x.length; i ++) {
			int j = x[i];
			while (j < i) j = x[j];
			swap(i, j);
		}
	}
	
	public int compareTwoRows(int column, int r0, int r1) {
		String a = iTable.getText(1 + r0, column);
		String b = iTable.getText(1 + r1, column);
		if (column <= 4)
			return a.compareToIgnoreCase(b);
		Integer ai = (a == null || a.isEmpty() ? 0 : Integer.parseInt(a));
		Integer bi = (b == null || b.isEmpty() ? 0 : Integer.parseInt(b));
		return ai.compareTo(bi);
	}
	
	public void swap(int r0, int r1) {
		CurriculumInterface c0 = iData.get(r0);
		CurriculumInterface c1 = iData.get(r1);
		c0.setRow(r1);
		c1.setRow(r0);
		iData.set(r0, c1);
		iData.set(r1, c0);
		fillRow(c0);
		fillRow(c1);
	}
	
	public class MyFlexTable extends FlexTable {

		public MyFlexTable() {
			super();
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONCLICK);
			sinkEvents(Event.ONMOUSEMOVE);
			setCellPadding(2);
			setCellSpacing(0);
			getElement().getStyle().setProperty("whiteSpace", "nowrap");
		}
		
		public void onBrowserEvent(Event event) {
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    final Element tr = DOM.getParent(td);
		    Element body = DOM.getParent(tr);
		    final int row = DOM.getChildIndex(body, tr);
		    
		    CurriculumInterface curriculum = (row == 0 || row > iData.size() ? null : iData.get(row - 1));
		    if (curriculum == null) return;
		    
			String style = getRowFormatter().getStyleName(row);

			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				if ("unitime-TableRowProblem".equals(style)) {
				} else if ("unitime-TableRowSelected".equals(style)) {
					getRowFormatter().setStyleName(row, "unitime-TableRowSelectedHover");	
				} else {
					getRowFormatter().setStyleName(row, "unitime-TableRowHover");
				}
				if (curriculum.hasClassifications()) {
					iClassifications.populate(curriculum.getClassifications());
					iClassifications.setEnabled(false);
					final int x = event.getClientX();
					iClassificationsPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							boolean top = (tr.getAbsoluteBottom() - Window.getScrollTop() + 15 + offsetHeight > Window.getClientHeight());
							iClassificationsPopup.setPopupPosition(
									Math.min(Math.max(x, tr.getAbsoluteLeft() + 15), tr.getAbsoluteRight() - offsetWidth - 15),
									top ? tr.getAbsoluteTop() - offsetHeight - 15 : tr.getAbsoluteBottom() + 15);
						}
					});
				}
				break;
			case Event.ONMOUSEMOVE:
				if (iClassificationsPopup.isShowing()) {
					boolean top = (tr.getAbsoluteBottom() - Window.getScrollTop() + 15 + iClassificationsPopup.getOffsetHeight() > Window.getClientHeight());
					iClassificationsPopup.setPopupPosition(
							Math.min(Math.max(event.getClientX(), tr.getAbsoluteLeft() + 15),
									tr.getAbsoluteRight() - iClassificationsPopup.getOffsetWidth() - 15),
									top ? tr.getAbsoluteTop() - iClassificationsPopup.getOffsetHeight() - 15 : tr.getAbsoluteBottom() + 15);
				}
				break;
			case Event.ONMOUSEOUT:
				if (iClassificationsPopup.isShowing())
					iClassificationsPopup.hide();
				if ("unitime-TableRowProblem".equals(style)) {
				} else if ("unitime-TableRowHover".equals(style)) {
					getRowFormatter().setStyleName(row, null);	
				} else if ("unitime-TableRowSelectedHover".equals(style)) {
					getRowFormatter().setStyleName(row, "unitime-TableRowSelected");
				}
				break;
			case Event.ONCLICK:
				for (int r = 1; r < getRowCount(); r++)
					if ("unitime-TableRowSelected".equals(getRowFormatter().getStyleName(r)))
						getRowFormatter().setStyleName(r, null);
				
				boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
				getRowFormatter().setStyleName(row, "unitime-TableRowSelected" + (hover ? "Hover" : ""));

				iLastCurriculumId = curriculum.getId();
				CurriculumClickedEvent e = new CurriculumClickedEvent(curriculum);
				for (CurriculumClickHandler h: iCurriculumClickHandlers) {
					h.onClick(e);
				}
				break;
			}
		}
	}
	
	public void scrollIntoView() {
		for (int r = 1; r < iTable.getRowCount(); r++)
			if ("unitime-TableRowSelected".equals(iTable.getRowFormatter().getStyleName(r)))
				iTable.getRowFormatter().getElement(r).scrollIntoView();
	}
	
	private void openCurriculumProjectionRules() {
		final DialogBox dialog = new DialogBox();
		dialog.setAnimationEnabled(true);
		dialog.setAutoHideEnabled(true);
		dialog.setGlassEnabled(true);
		dialog.setModal(true);
		final CurriculumProjectionRules rules = new CurriculumProjectionRules();
		rules.setAllowClose(true);
		rules.getElement().getStyle().setMarginRight(ToolBox.getScrollBarWidth(), Unit.PX);
		rules.getElement().getStyle().setPaddingLeft(10, Unit.PX);
		rules.getElement().getStyle().setPaddingRight(10, Unit.PX);
		final ScrollPanel panel = new ScrollPanel(rules);
		panel.setHeight(Math.round(0.9 * Window.getClientHeight()) + "px");
		panel.setStyleName("unitime-ScrollPanel");
		dialog.setWidget(panel);
		dialog.setText("Curriculum Projection Rules");
		rules.addProjectionRulesHandler(new CurriculumProjectionRules.ProjectionRulesHandler() {
			@Override
			public void onRulesSaved(ProjectionRulesEvent evt) {
				dialog.hide();
				query(iLastQuery, null);
			}
			@Override
			public void onRulesLoaded(ProjectionRulesEvent evt) {
				dialog.center();
				//panel.setWidth((ToolBox.getScrollBarWidth() + rules.getOffsetWidth()) + "px");
			}
			@Override
			public void onRulesClosed(ProjectionRulesEvent evt) {
				dialog.hide();
			}
			@Override
			public void onException(Throwable caught) {
				setError("Unable to open curriculum projection rules (" + caught.getMessage() + ")");
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
		return iData;
	}
	
	public interface EditClassificationHandler {
		public void doEdit(List<CurriculumInterface> curricula);
	}
	
	public void setEditClassificationHandler(EditClassificationHandler h) {
		iEditClassificationHandler = h;
	}
	
	public static enum DeptMode {
		CODE('0', "Code"),
		ABBV('1', "Abbreviation"),
		NAME('2', "Name"),
		ABBV_NAME('3', "Abbv - Name"),
		CODE_NAME('4', "Code - Name");

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
			case ABBV_NAME:
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
}
