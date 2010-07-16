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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;
import org.unitime.timetable.gwt.widgets.CurriculaCourses.Mode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ClassificationsEdit extends Composite {
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);

	private VerticalPanel iPanel;
	private Button[] iSave = null, iBack = null, iPrint = null;
	private Label[] iErrorMessage = null;
	private HorizontalPanel[] iHeaderPanel = null;

	private MyFlexTable iTable;
	
	private List<CurriculumInterface> iData = null;

	private List<EditFinishedHandler> iEditFinishedHandlers = new ArrayList<EditFinishedHandler>();

	
	public ClassificationsEdit() {
		iPanel = new VerticalPanel();
		iPanel.setWidth("100%");
		
		ClickHandler save = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show("Saving curricula ...");
				iService.saveClassifications(iData, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						showError("Failed to save curricula (" + caught.getMessage() + ").");
					}
					@Override
					public void onSuccess(Boolean result) {
						LoadingWidget.getInstance().hide();
						hideError();
						EditFinishedEvent e = new EditFinishedEvent();
						for (EditFinishedHandler h: iEditFinishedHandlers) {
							h.onSave(e);
						}
					}
				});
			}
		};
		
		ClickHandler back = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EditFinishedEvent e = new EditFinishedEvent();
				for (EditFinishedHandler h: iEditFinishedHandlers) {
					h.onBack(e);
				}
			}
		};
		
		ClickHandler print = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.print();
			}
		};
		
		iHeaderPanel = new HorizontalPanel[] { new HorizontalPanel(), new HorizontalPanel()};
		iHeaderPanel[0].setStyleName("unitime-MainTableHeader");
		iErrorMessage = new Label[] {new Label(), new Label()};
		iSave = new Button[] { new Button("<u>S</u>ave"), new Button("<u>S</u>ave")};
		iBack = new Button[] { new Button("<u>B</u>ack"), new Button("<u>B</u>ack")};
		iPrint = new Button[] { new Button("<u>P</u>rint"), new Button("<u>P</u>rint")};
		
		iSave[0].setAccessKey('s');
		iBack[0].setAccessKey('b');
		iPrint[0].setAccessKey('p');
		
		for (int i = 0; i < 2; i++) {
			iHeaderPanel[i].setSpacing(2);
			iHeaderPanel[i].setWidth("100%");
			iErrorMessage[i].setStyleName("unitime-ErrorMessage");
			iErrorMessage[i].setVisible(false);
			iSave[i].setVisible(true);
			iSave[i].addClickHandler(save);
			iSave[i].addStyleName("unitime-NoPrint");
			iBack[i].setVisible(true);
			iBack[i].addClickHandler(back);
			iBack[i].getElement().getStyle().setMarginLeft(4, Unit.PX);
			iBack[i].addStyleName("unitime-NoPrint");
			iPrint[i].setVisible(true);
			iPrint[i].addClickHandler(print);
			iPrint[i].getElement().getStyle().setMarginLeft(4, Unit.PX);
			iPrint[i].addStyleName("unitime-NoPrint");
			HorizontalPanel buttons = new HorizontalPanel();
			buttons.add(iSave[i]);
			buttons.add(iPrint[i]);
			buttons.add(iBack[i]);
			iHeaderPanel[i].add(iErrorMessage[i]);
			iHeaderPanel[i].setCellWidth(iErrorMessage[i], "100%");
			iHeaderPanel[i].add(buttons);
		}
		iHeaderPanel[0].addStyleName("unitime-NoPrint");
		
		iPanel.add(iHeaderPanel[0]);
		
		iTable = new MyFlexTable();
		iTable.setVisible(true);
		iPanel.add(iTable);

		iPanel.add(iHeaderPanel[1]);

		initWidget(iPanel);
	}

	public void updateAll() {
		for (int row = 1; row < iTable.getRowCount(); row++) {
			for (int col = 1; col < iTable.getCellCount(row); col++) {
				Widget w = iTable.getWidget(row, col);
				if (w != null) ((Updatable)w).update();
			}
		}
	}

	public void setData(List<CurriculumInterface> curricula) {
		iData = curricula;
		
		for (int row = iTable.getRowCount() - 1; row >= 0; row--)
			iTable.removeRow(row);
		
		final TreeSet<AcademicClassificationInterface> academicClassifications = new TreeSet<AcademicClassificationInterface>();
		for (CurriculumInterface curriculum: curricula) {
			if (curriculum.hasClassifications()) {
				for (CurriculumClassificationInterface clasf: curriculum.getClassifications()) {
					academicClassifications.add(clasf.getAcademicClassification());
				}
			}
		}
		
		int row = 1;
		List<MyCell> allCells = new ArrayList<MyCell>();
		HashMap<AcademicClassificationInterface, List<MyCell>> clasf2cells = new HashMap<AcademicClassificationInterface, List<MyCell>>();
		for (CurriculumInterface curriculum: curricula) {
			
			MyRow r = new MyRow(curriculum);
			List<MyCell> cells = new ArrayList<MyCell>();
			
			iTable.setText(row, 0, curriculum.getAbbv());
			iTable.setText(row, 1, curriculum.getName());
			int col = 1;
			for (AcademicClassificationInterface acadClasf: academicClassifications) {
				CurriculumClassificationInterface clasf = null;
				if (curriculum.hasClassifications())
					for (CurriculumClassificationInterface c: curriculum.getClassifications()) {
						if (c.getAcademicClassification().equals(acadClasf)) { clasf = c; break; }
					}
				if (clasf == null) {
					clasf = new CurriculumClassificationInterface();
					clasf.setAcademicClassification(acadClasf);
					curriculum.addClassification(clasf);
				}
				
				MyCell c = new MyCell(r, clasf);
				cells.add(c);
				allCells.add(c);
				List<MyCell> cellsThisClasf = clasf2cells.get(acadClasf);
				if (cellsThisClasf == null) {
					cellsThisClasf = new ArrayList<MyCell>();
					clasf2cells.put(acadClasf, cellsThisClasf);
				}
				cellsThisClasf.add(c);
				iTable.setWidget(row, col++, c);
			}
			
			iTable.setWidget(row, col, new MySumCell(cells));
			iTable.getCellFormatter().getElement(row, col).getStyle().setBackgroundColor("#EEEEEE");
			row++;
		}
		
		ClickHandler menu = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				for (final Mode m: Mode.values()) {
					if (CurriculumCookie.getInstance().getCurriculaCoursesMode() == m) continue;
					menu.addItem(new MenuItem(m == Mode.NONE ? "Hide " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName() : "Show " + m.getName(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							CurriculumCookie.getInstance().setCurriculaCoursesMode(m);
							int c = 1;
							for (AcademicClassificationInterface clasf: academicClassifications) {
								((Label)iTable.getWidget(0, c++)).setText(clasf.getCode() + (m == Mode.NONE ? "" : " Req / " + m.getAbbv()));
							}
							((Label)iTable.getWidget(0, c)).setText("Total" + (m == Mode.NONE ? "" : " Req / " + m.getAbbv()));
							updateAll();
							if (iTable.hasHiddenColumn()) iTable.hasEmptyColumns();
						}
					}));
				}
				menu.setVisible(true);
				menu.addSeparator();
				boolean cx = false;
				if (iTable.hasEmptyColumns()) {
					menu.addItem(new MenuItem("Hide Empty Classifications", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iTable.hideEmptyColumns();
						}
					}));
					cx = true;
				}
				if (iTable.hasHiddenColumn()) {
					menu.addItem(new MenuItem("Show All Classifications", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iTable.showAllColumns();
						}
					}));
					cx = true;
				}
				if (cx) menu.addSeparator();
				int col = iTable.getCellForEvent(event).getCellIndex();
				if (col == 0) {
					menu.addItem(new MenuItem("Sort by Curriculum", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iTable.sort(new Comparator<MyRow>() {
								@Override
								public int compare(MyRow r1, MyRow r2) {
									return r1.getCurriculum().compareTo(r2.getCurriculum());
								}
							});
						}
					}));
				} else if (col == 1 + academicClassifications.size()) {
					menu.addItem(new MenuItem("Sort by Total Requested Enrollment", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iTable.sort(new Comparator<MyRow>() {
								@Override
								public int compare(MyRow r1, MyRow r2) {
									int t1 = 0, t2 = 0;
									for (CurriculumClassificationInterface clasf: r1.getCurriculum().getClassifications())
										if (clasf.getExpected() != null) t1 += clasf.getExpected();
									for (CurriculumClassificationInterface clasf: r2.getCurriculum().getClassifications())
										if (clasf.getExpected() != null) t2 += clasf.getExpected();
									if (t2 > t1) return 1;
									if (t1 > t2) return -1;
									return r1.getCurriculum().compareTo(r2.getCurriculum());
								}
							});
						}
					}));
					if (CurriculumCookie.getInstance().getCurriculaCoursesMode() != Mode.NONE) {
						menu.addItem(new MenuItem("Sort by Total " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								iTable.sort(new Comparator<MyRow>() {
									@Override
									public int compare(MyRow r1, MyRow r2) {
										int t1 = 0, t2 = 0;
										switch (CurriculumCookie.getInstance().getCurriculaCoursesMode()) {
										case ENRL:
											for (CurriculumClassificationInterface clasf: r1.getCurriculum().getClassifications())
												if (clasf.getEnrollment() != null) t1 += clasf.getEnrollment();
											for (CurriculumClassificationInterface clasf: r2.getCurriculum().getClassifications())
												if (clasf.getEnrollment() != null) t2 += clasf.getEnrollment();
											break;
										case LAST:
											for (CurriculumClassificationInterface clasf: r1.getCurriculum().getClassifications())
												if (clasf.getLastLike() != null) t1 += clasf.getLastLike();
											for (CurriculumClassificationInterface clasf: r2.getCurriculum().getClassifications())
												if (clasf.getLastLike() != null) t2 += clasf.getLastLike();
											break;
										case PROJ:
											for (CurriculumClassificationInterface clasf: r1.getCurriculum().getClassifications())
												if (clasf.getProjection() != null) t1 += clasf.getProjection();
											for (CurriculumClassificationInterface clasf: r2.getCurriculum().getClassifications())
												if (clasf.getProjection() != null) t2 += clasf.getProjection();
											break;
										}
										for (CurriculumClassificationInterface clasf: r1.getCurriculum().getClassifications())
											if (clasf.getExpected() != null) t1 += clasf.getExpected();
										for (CurriculumClassificationInterface clasf: r2.getCurriculum().getClassifications())
											if (clasf.getExpected() != null) t2 += clasf.getExpected();
										if (t2 > t1) return 1;
										if (t1 > t2) return -1;
										return r1.getCurriculum().compareTo(r2.getCurriculum());
									}
								});
							}
						}));
					}
				} else {
					int c = 1;
					for (AcademicClassificationInterface ac: academicClassifications) {
						if (c == col) {
							final AcademicClassificationInterface clasf = ac;
							menu.addItem(new MenuItem("Sort by " + ac.getCode() + " Requested Enrollment", true, new Command() {
								@Override
								public void execute() {
									popup.hide();
									iTable.sort(new Comparator<MyRow>() {
										@Override
										public int compare(MyRow r1, MyRow r2) {
											MyCell c1 = r1.getCell(clasf);
											MyCell c2 = r2.getCell(clasf);
											int t1 = (c1.getClassification().getExpected() == null ? 0 : c1.getClassification().getExpected());
											int t2 = (c2.getClassification().getExpected() == null ? 0 : c2.getClassification().getExpected());
											if (t2 > t1) return 1;
											if (t1 > t2) return -1;
											return r1.getCurriculum().compareTo(r2.getCurriculum());
										}
									});
								}
							}));
							if (CurriculumCookie.getInstance().getCurriculaCoursesMode() != Mode.NONE) {
								menu.addItem(new MenuItem("Sort by " + ac.getCode() + " " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName(), true, new Command() {
									@Override
									public void execute() {
										popup.hide();
										iTable.sort(new Comparator<MyRow>() {
											@Override
											public int compare(MyRow r1, MyRow r2) {
												MyCell c1 = r1.getCell(clasf);
												MyCell c2 = r2.getCell(clasf);
												int t1 = 0, t2 = 0;
												switch (CurriculumCookie.getInstance().getCurriculaCoursesMode()) {
												case ENRL:
													t1 = (c1.getClassification().getEnrollment() == null ? 0 : c1.getClassification().getEnrollment());
													t2 = (c2.getClassification().getEnrollment() == null ? 0 : c2.getClassification().getEnrollment());
													break;
												case LAST:
													t1 = (c1.getClassification().getLastLike() == null ? 0 : c1.getClassification().getLastLike());
													t2 = (c2.getClassification().getLastLike() == null ? 0 : c2.getClassification().getLastLike());
													break;
												case PROJ:
													t1 = (c1.getClassification().getProjection() == null ? 0 : c1.getClassification().getProjection());
													t2 = (c2.getClassification().getProjection() == null ? 0 : c2.getClassification().getProjection());
													break;
												}
												if (t2 > t1) return 1;
												if (t1 > t2) return -1;
												return r1.getCurriculum().compareTo(r2.getCurriculum());
											}
										});
									}
								}));
							}
							break;
						}
						c++;
					}
				}
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		};
		
		int col = 1;
		Mode m = CurriculumCookie.getInstance().getCurriculaCoursesMode();
		for (AcademicClassificationInterface clasf: academicClassifications) {
			Label label = new Label(clasf.getCode() + (m == Mode.NONE ? "" : " Req / " + m.getAbbv()), false);
			label.addClickHandler(menu);
			iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
			iTable.setWidget(0, col, label);
			iTable.getCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER);
			col++;
		}
		HTML abbv = new HTML("Curriculum", false);
		abbv.addClickHandler(menu);
		iTable.getFlexCellFormatter().setStyleName(0, 0, "unitime-ClickableTableHeader");
		iTable.setWidget(0, 0, abbv);
		HTML totals = new HTML("Total"+ (m == Mode.NONE ? "" : " Req / " + m.getAbbv()), false);
		totals.addClickHandler(menu);
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER);
		iTable.setWidget(0, col, totals);
		
		iTable.setText(row, 0, "Total");
		iTable.getCellFormatter().getElement(row, 0).getStyle().setBackgroundColor("#EEEEEE");
		col = 1;
		for (AcademicClassificationInterface clasf: academicClassifications) {
			iTable.setWidget(row, col, new MySumCell(clasf2cells.get(clasf)));
			iTable.getCellFormatter().getElement(row, col).getStyle().setBackgroundColor("#EEEEEE");
			col ++;
		}
		iTable.setWidget(row, col, new MySumCell(allCells));
		iTable.getCellFormatter().getElement(row, col).getStyle().setBackgroundColor("#EEEEEE");
		
		iHeaderPanel[0].setVisible(true);
		iTable.setVisible(true);
		iTable.hideEmptyColumns();
	}
	
	private class MyRow {
		private CurriculumInterface iCurriculum = null;
		private HashMap<AcademicClassificationInterface, MyCell> iCells = new HashMap<AcademicClassificationInterface, MyCell>();

		public MyRow(CurriculumInterface curriculum) {
			iCurriculum = curriculum;
		}
		
		public void setCell(AcademicClassificationInterface clasf, MyCell cell) { iCells.put(clasf, cell); }
		public MyCell getCell(AcademicClassificationInterface clasf) { return iCells.get(clasf); }
		
		public CurriculumInterface getCurriculum() { return iCurriculum; }
	}
	
	private interface Updatable {
		public void update();
		public void focus();
	}
	
	private class MyCell extends Composite implements Updatable {
		private MyRow iRow;
		private CurriculumClassificationInterface iClasf;
		
		private TextBox iTextBox;
		private HTML iRearLabel;
		private HorizontalPanel iPanel;
		
		private HTML iHint = null;
		private PopupPanel iHintPanel = null;
		private boolean iCellEditable = true;
		
		private List<MySumCell> iSums = new ArrayList<MySumCell>();

		public MyCell(MyRow row, CurriculumClassificationInterface classification) {
			iRow = row;
			iClasf = classification;
			iRow.setCell(classification.getAcademicClassification(), this);
			
			iPanel = new HorizontalPanel();
			
			iTextBox = new TextBox();
			iTextBox.setWidth("60px");
			iTextBox.setStyleName("unitime-TextBox");
			iTextBox.setMaxLength(6);
			iTextBox.setTextAlignment(TextBox.ALIGN_RIGHT);
			iTextBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					try {
						if (iTextBox.getText().isEmpty()) {
							iClasf.setExpected(null);
						} else {
							iClasf.setExpected(Integer.valueOf(iTextBox.getText()));
						}
					} catch (Exception e) {
						iClasf.setExpected(null);
					}
					update();
					for (MySumCell sum: iSums)
						sum.update();
				}
			});
			
			iRearLabel = new HTML("", false);
			iRearLabel.setWidth("50px");
			iRearLabel.setStyleName("unitime-Label");
			iRearLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			
			iPanel.add(iTextBox);
			iPanel.setCellVerticalAlignment(iTextBox, HasVerticalAlignment.ALIGN_MIDDLE);
			
			iPanel.add(iRearLabel);
			iPanel.setCellVerticalAlignment(iRearLabel, HasVerticalAlignment.ALIGN_MIDDLE);

			initWidget(iPanel);	
			
			update();
			
			iHint = new HTML(
					"Curriculum: " + iRow.getCurriculum().getAbbv() + " - " + iRow.getCurriculum().getName() + "<br>" +
					"Academic Area: " + iRow.getCurriculum().getAcademicArea().getAbbv() + " - " + iRow.getCurriculum().getAcademicArea().getName() + "<br>" +
					(iRow.getCurriculum().hasMajors() ? "Major: " + iRow.getCurriculum().getCodeMajorNames(", ") + "<br>" : "" ) + 
					"Academic Classification: " + (iClasf.getName() != null ? iClasf.getName() : iClasf.getAcademicClassification().getCode()) + " - " + iClasf.getAcademicClassification().getName(),
					false);
			iHintPanel = new PopupPanel();
			iHintPanel.setWidget(iHint);
			iHintPanel.setStyleName("unitime-PopupHint");
			
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONMOUSEMOVE);
		}
		
		public void update() {
			iTextBox.setText(iClasf.getExpected() == null ? "" : iClasf.getExpected().toString());
			switch (CurriculumCookie.getInstance().getCurriculaCoursesMode()) {
			case NONE: // None
				iRearLabel.setHTML("");
				iRearLabel.setVisible(false);
				break;
			case ENRL: // Enrollment
				if (iClasf.getEnrollment() == null || iClasf.getEnrollment() == 0) {
					iRearLabel.setHTML("");
				} else {
					iRearLabel.setHTML(iClasf.getEnrollment().toString());
				}
				iRearLabel.setVisible(true);
				break;
			case LAST: // Last-like
				if (iClasf.getLastLike() == null || iClasf.getLastLike() == 0) {
					iRearLabel.setHTML("");
				} else {
					iRearLabel.setHTML(iClasf.getLastLike().toString());
				}
				iRearLabel.setVisible(true);
				break;
			case PROJ: // Projection
				if (iClasf.getProjection() == null || iClasf.getProjection() == 0) {
					iRearLabel.setHTML("");
				} else {
					iRearLabel.setHTML(iClasf.getProjection().toString());
				}
				iRearLabel.setVisible(true);
				break;
			}
		}
		
		public CurriculumClassificationInterface getClassification() { return iClasf; }
		
		public void focus() {
			iTextBox.setFocus(true);
			DeferredCommand.addCommand(new Command() {
				@Override
				public void execute() {
					iTextBox.selectAll();
				}
			});
		}
		
		public void onBrowserEvent(final Event event) {
			Element tr = getElement();
		    for (; tr != null; tr = DOM.getParent(tr)) {
		        if (DOM.getElementProperty(tr, "tagName").equalsIgnoreCase("tr"))
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
		
		public MyRow getRow() { return iRow; }
		
		public boolean isEmpty() {
			return iTextBox.getText().isEmpty() && (!iRearLabel.isVisible() || iRearLabel.getHTML().isEmpty());
		}
	}
	
	private class MySumCell extends Composite implements Updatable {
		private HTML iTextBox;
		private HTML iRearLabel;
		private HorizontalPanel iPanel;
		
		private List<MyCell> iCells = new ArrayList<MyCell>();

		public MySumCell(List<MyCell> cells) {
			iCells = cells;
			for (MyCell cell: iCells)
				cell.iSums.add(this);
			
			iPanel = new HorizontalPanel();
			
			iTextBox = new HTML("", false);
			iTextBox.setWidth("60px");
			iTextBox.setStyleName("unitime-Label");
			iTextBox.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			
			iRearLabel = new HTML("", false);
			iRearLabel.setWidth("55px");
			iRearLabel.setStyleName("unitime-Label");
			iRearLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			
			iPanel.add(iTextBox);
			iPanel.setCellVerticalAlignment(iTextBox, HasVerticalAlignment.ALIGN_MIDDLE);
			
			iPanel.add(iRearLabel);
			iPanel.setCellVerticalAlignment(iRearLabel, HasVerticalAlignment.ALIGN_MIDDLE);

			initWidget(iPanel);	
			
			update();
		}
		
		public int sumExpected() {
			int ret = 0;
			for (MyCell c: iCells)
				if (c.getClassification().getExpected() != null)
					ret += c.getClassification().getExpected();
			return ret;
		}
		
		public int sumEnrollment() {
			int ret = 0;
			for (MyCell c: iCells)
				if (c.getClassification().getEnrollment() != null)
					ret += c.getClassification().getEnrollment();
			return ret;
		}

		public int sumLastLike() {
			int ret = 0;
			for (MyCell c: iCells)
				if (c.getClassification().getLastLike() != null)
					ret += c.getClassification().getLastLike();
			return ret;
		}

		public int sumProjection() {
			int ret = 0;
			for (MyCell c: iCells)
				if (c.getClassification().getProjection() != null)
					ret += c.getClassification().getProjection();
			return ret;
		}

		public void update() {
			iTextBox.setHTML(sumExpected() == 0 ? "" : String.valueOf(sumExpected()));
			switch (CurriculumCookie.getInstance().getCurriculaCoursesMode()) {
			case NONE: // None
				iRearLabel.setHTML("");
				iRearLabel.setVisible(false);
				break;
			case ENRL: // Enrollment
				iRearLabel.setHTML(sumEnrollment() == 0 ? "" : String.valueOf(sumEnrollment()));
				iRearLabel.setVisible(true);
				break;
			case LAST: // Last-like
				iRearLabel.setHTML(sumLastLike() == 0 ? "" : String.valueOf(sumLastLike()));
				iRearLabel.setVisible(true);
				break;
			case PROJ: // Projection
				iRearLabel.setHTML(sumProjection() == 0 ? "" : String.valueOf(sumProjection()));
				iRearLabel.setVisible(true);
				break;
			}
		}
		
		public void focus() {
		}
	}
	
	public void showError(String error) {
		for (int i = 0; i < iErrorMessage.length; i++) {
			iErrorMessage[i].setText(error);
			iErrorMessage[i].setVisible(true);
			iErrorMessage[i].setStyleName("unitime-ErrorMessage");
		}
	}
	
	public void showMessage(String message) {
		for (int i = 0; i < iErrorMessage.length; i++) {
			iErrorMessage[i].setText(message);
			iErrorMessage[i].setVisible(true);
			iErrorMessage[i].setStyleName("unitime-Message");
		}
	}

	public void hideError() {
		for (int i = 0; i < iErrorMessage.length; i++) {
			iErrorMessage[i].setVisible(false);
		}
	}
	
	private class MyFlexTable extends FlexTable {
		public MyFlexTable() {
			super();
			setCellPadding(2);
			setCellSpacing(0);
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONKEYDOWN);
			setStylePrimaryName("unitime-MainTable");
			addStyleName("unitime-NotPrintableBottomLine");
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
		    return ((MyCell)getWidget(row, 1)).getRow();
		}
		
		public void sort(final Comparator<MyRow> rowComparator) {
			Element body = getBodyElement();
			Element last = getRowFormatter().getElement(getRowCount() - 1);
			TreeSet<Object[]> rows = new TreeSet<Object[]>(new Comparator<Object[]>() {
				public int compare(Object[] a, Object[] b) {
					return rowComparator.compare((MyRow)a[1], (MyRow)b[1]);
				}
			});
			for (int row = getRowCount() - 2; row > 0; row--) {
				Element tr = getRowFormatter().getElement(row);
				MyRow r = getMyRow(row);
				DOM.removeChild(body, tr);
				rows.add(new Object[] {tr, r});
			}
			for (Object[] row: rows)
				DOM.insertBefore(body, (Element)row[0], last);
		}
		
		private void moveRow(Element tr, Element before) {
			Element body = DOM.getParent(tr);
			DOM.removeChild(body, tr);
			DOM.insertBefore(body, tr, before);
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
		    if (r == null) return;

			String style = getRowFormatter().getStyleName(row);

			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				getRowFormatter().setStyleName(row, "unitime-TableRowHover");
				getCellFormatter().getElement(row, DOM.getChildCount(tr) - 1).getStyle().setBackgroundColor(null);
				break;
			case Event.ONMOUSEOUT:
				getRowFormatter().setStyleName(row, null);	
				getCellFormatter().getElement(row, DOM.getChildCount(tr) - 1).getStyle().setBackgroundColor("#EEEEEE");
				break;
			case Event.ONKEYDOWN:
				int oldRow = row, oldCol = col;
				if (event.getKeyCode() == KeyCodes.KEY_RIGHT && (event.getAltKey() || event.getMetaKey())) {
					do {
						col++;
						if (col >= getCellCount(row)) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_LEFT && (event.getAltKey() || event.getMetaKey())) {
					do {
						col--;
						if (col < 0) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP && (event.getAltKey() || event.getMetaKey())) {
					do {
						row--;
						if (row <= 0) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN && (event.getAltKey() || event.getMetaKey())) {
					do {
						row++;
						if (row >= getRowCount()) break;
					} while (!focus(event, oldRow, oldCol, row, col));
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP && event.getCtrlKey()) {
					Updatable u = (Updatable)getWidget(row, col);
				    if (row > 1)
			    		moveRow(tr, DOM.getChild(body, row - 1));
			    	u.focus();
			    	event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN && event.getCtrlKey()) {
					Updatable u = (Updatable)getWidget(row, col);
				    if (row + 2 < getRowCount())
			    		moveRow(DOM.getChild(body, row + 1), tr);
			    	u.focus();
			    	event.stopPropagation();
			    	event.preventDefault();
				}
				break;
		    }
		}
		
		public boolean isColumnVisible(int col) {
			return getCellFormatter().isVisible(0, col);
		}
		
		public void setColumnVisible(int col, boolean visible) {
			for (int r = 0; r < getRowCount(); r++)
				getCellFormatter().setVisible(r, col, visible);
		}
		
		public boolean hasHiddenColumn() {
			for (int c = 1; c < getCellCount(0) - 1; c++) {
				if (!isColumnVisible(c)) return true;
			}
			return false;			
		}
		
		public boolean isColumnEmpty(int col) {
			for (int r = 1; r < getRowCount() - 1; r++) {
				if (!((MyCell)getWidget(r, col)).isEmpty()) return false;
			}
			return true;
		}

		public boolean hasEmptyColumns() {
			for (int c = 1; c < getCellCount(0) - 1; c++) {
				if (isColumnVisible(c) && isColumnEmpty(c)) return true;
			}
			return false;
		}
		
		public void hideEmptyColumns() {
			for (int c = 1; c < getCellCount(0) - 1; c++) {
				setColumnVisible(c, !isColumnEmpty(c));
			}
		}

		public void showAllColumns() {
			for (int c = 1; c < getCellCount(0) - 1; c++) {
				setColumnVisible(c, true);
			}
		}
	}
	
	public static class EditFinishedEvent {
		
	}
	
	public static interface EditFinishedHandler {
		public void onBack(EditFinishedEvent evt);
		public void onSave(EditFinishedEvent evt);
	}

	public void addEditFinishedHandler(EditFinishedHandler h) {
		iEditFinishedHandlers.add(h);
	}

}
