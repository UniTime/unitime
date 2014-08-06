/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.curricula;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.curricula.CurriculaCourses.Mode;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.DataChangedEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.DataChangedListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasFocus;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasHint;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseOutListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseOverListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class ClassificationsEdit extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);

	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeaderPanel = null;

	private UniTimeTable<CurriculumInterface> iTable;
	
	private List<EditFinishedHandler> iEditFinishedHandlers = new ArrayList<EditFinishedHandler>();

	
	public ClassificationsEdit() {
		iPanel = new SimpleForm();
		
		ClickHandler save = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitSavingData());
				iService.saveClassifications(iTable.getData(), new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeaderPanel.setErrorMessage(MESSAGES.failedToSaveCurricula(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedToSaveCurricula(caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(Boolean result) {
						LoadingWidget.getInstance().hide();
						iHeaderPanel.clearMessage();
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
		
		iHeaderPanel = new UniTimeHeaderPanel();
		
		iHeaderPanel.addButton("save", MESSAGES.buttonSave(), 75, save);
		iHeaderPanel.addButton("back", MESSAGES.buttonBack(), 75, back);
		iHeaderPanel.addButton("print", MESSAGES.buttonPrint(), 75, print);
		
		iPanel.addHeaderRow(iHeaderPanel);

		iTable = new UniTimeTable<CurriculumInterface>();
		iPanel.addRow(iTable);

		iPanel.addNotPrintableBottomRow(iHeaderPanel.clonePanel());

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
		iTable.clearTable();
		
		final TreeSet<AcademicClassificationInterface> academicClassifications = new TreeSet<AcademicClassificationInterface>();
		for (CurriculumInterface curriculum: curricula) {
			if (curriculum.hasClassifications()) {
				for (CurriculumClassificationInterface clasf: curriculum.getClassifications()) {
					academicClassifications.add(clasf.getAcademicClassification());
				}
			}
		}
		
		final List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();

		List<Operation> operations = new ArrayList<Operation>();
		for (final Mode m: Mode.values()) {
			operations.add(new Operation() {
				@Override
				public void execute() {
					CurriculumCookie.getInstance().setCurriculaCoursesMode(m);
					int c = 1;
					for (AcademicClassificationInterface clasf: academicClassifications) {
						header.get(c++).setText(clasf.getCode() + (m == Mode.NONE ? "" : " " + MESSAGES.abbvRequestedEnrollment() + " / " + m.getAbbv()));
					}
					header.get(c).setText(m == Mode.NONE ? MESSAGES.colTotal() : MESSAGES.colTotalOf(MESSAGES.abbvRequestedEnrollment() + " / " + m.getAbbv()));
					updateAll();
				}
				
				@Override
				public boolean isApplicable() {
					return CurriculumCookie.getInstance().getCurriculaCoursesMode() != m;
				}
				
				@Override
				public boolean hasSeparator() {
					return false;
				}
				
				@Override
				public String getName() {
					return m == Mode.NONE ? MESSAGES.opHide(CurriculumCookie.getInstance().getCurriculaCoursesMode().getName()) : MESSAGES.opShow(m.getName());
				}
			});
		}
		operations.add(new Operation() {
			@Override
			public void execute() {
				if (hasEmptyColumns()) {
					hideEmptyColumns();
				} else {
					showAllColumns();
				}
			}
			
			@Override
			public boolean isApplicable() {
				return hasEmptyColumns() || hasHiddenColumn();
			}
			
			@Override
			public boolean hasSeparator() {
				return true;
			}
			
			@Override
			public String getName() {
				return (hasEmptyColumns() ? MESSAGES.opHideEmptyClassifications() : MESSAGES.opShowAllClassifications());
			}
		});
		
		final UniTimeTableHeader hCurriculum = new UniTimeTableHeader(MESSAGES.colCurriculum());
		header.add(hCurriculum);
		hCurriculum.getOperations().addAll(operations);
		hCurriculum.addOperation(new Operation() {
			@Override
			public void execute() {
				iTable.sort(hCurriculum, new Comparator<CurriculumInterface>() {
					@Override
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						return a.compareTo(b);
					}
				});
			}
			
			@Override
			public boolean isApplicable() {
				return true;
			}
			
			@Override
			public boolean hasSeparator() {
				return true;
			}
			
			@Override
			public String getName() {
				return MESSAGES.opSortBy(MESSAGES.colCurriculum());
			}
		});
		
		Mode m = CurriculumCookie.getInstance().getCurriculaCoursesMode();
		for (final AcademicClassificationInterface clasf: academicClassifications) {
			final UniTimeTableHeader hClasf = new UniTimeTableHeader(clasf.getCode() + (m == Mode.NONE ? "" : " " + MESSAGES.abbvRequestedEnrollment() + " / " + m.getAbbv()), HasHorizontalAlignment.ALIGN_CENTER);
			header.add(hClasf);
			hClasf.getOperations().addAll(operations);
			hClasf.addOperation(new Operation() {
				@Override
				public void execute() {
					iTable.sort(hClasf, new Comparator<CurriculumInterface>() {
						@Override
						public int compare(CurriculumInterface a, CurriculumInterface b) {
							CurriculumClassificationInterface f = null, g = null;
							for (CurriculumClassificationInterface c: a.getClassifications()) {
								if (c.getAcademicClassification().equals(clasf)) { f = c; break; }
							}
							for (CurriculumClassificationInterface c: b.getClassifications()) {
								if (c.getAcademicClassification().equals(clasf)) { g = c; break; }
							}
							int t1 = (f.getExpected() == null ? 0 : f.getExpected());
							int t2 = (g.getExpected() == null ? 0 : g.getExpected());
							if (t2 > t1) return 1;
							if (t1 > t2) return -1;
							return a.compareTo(b);
						}
					});
				}
				
				@Override
				public boolean isApplicable() {
					return true;
				}
				
				@Override
				public boolean hasSeparator() {
					return true;
				}
				
				@Override
				public String getName() {
					return MESSAGES.opSortBy(clasf.getCode() + " " + MESSAGES.fieldRequestedEnrollment());
				}
			});
			hClasf.addOperation(new Operation() {
				@Override
				public void execute() {
					iTable.sort(hClasf, new Comparator<CurriculumInterface>() {
						@Override
						public int compare(CurriculumInterface a, CurriculumInterface b) {
							CurriculumClassificationInterface f = null, g = null;
							for (CurriculumClassificationInterface c: a.getClassifications()) {
								if (c.getAcademicClassification().equals(clasf)) { f = c; break; }
							}
							for (CurriculumClassificationInterface c: b.getClassifications()) {
								if (c.getAcademicClassification().equals(clasf)) { g = c; break; }
							}
							int t1 = 0, t2 = 0;
							switch (CurriculumCookie.getInstance().getCurriculaCoursesMode()) {
							case ENRL:
								t1 = (f.getEnrollment() == null ? 0 : f.getEnrollment());
								t2 = (g.getEnrollment() == null ? 0 : g.getEnrollment());
								break;
							case LAST:
								t1 = (f.getLastLike() == null ? 0 : f.getLastLike());
								t2 = (g.getLastLike() == null ? 0 : g.getLastLike());
								break;
							case PROJ:
								t1 = (f.getProjection() == null ? 0 : f.getProjection());
								t2 = (g.getProjection() == null ? 0 : g.getProjection());
								break;
							case REQ:
								t1 = (f.getRequested() == null ? 0 : f.getRequested());
								t2 = (g.getRequested() == null ? 0 : g.getRequested());
								break;
							}
							if (t2 > t1) return 1;
							if (t1 > t2) return -1;
							return a.compareTo(b);
						}
					});
				}
				
				@Override
				public boolean isApplicable() {
					return CurriculumCookie.getInstance().getCurriculaCoursesMode() != Mode.NONE;
				}
				
				@Override
				public boolean hasSeparator() {
					return false;
				}
				
				@Override
				public String getName() {
					return MESSAGES.opSortBy(clasf.getCode() + " " + CurriculumCookie.getInstance().getCurriculaCoursesMode().getName());
				}
			});
		}
		final UniTimeTableHeader hTotal = new UniTimeTableHeader(m == Mode.NONE ? MESSAGES.colTotal() : MESSAGES.colTotalOf(MESSAGES.abbvRequestedEnrollment() + " / " + m.getAbbv()), HasHorizontalAlignment.ALIGN_CENTER);
		header.add(hTotal);
		hTotal.getOperations().addAll(operations);
		hTotal.addOperation(new Operation() {
			@Override
			public void execute() {
				iTable.sort(hTotal, new Comparator<CurriculumInterface>() {
					@Override
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						int t1 = 0, t2 = 0;
						for (CurriculumClassificationInterface clasf: a.getClassifications())
							if (clasf.getExpected() != null) t1 += clasf.getExpected();
						for (CurriculumClassificationInterface clasf: b.getClassifications())
							if (clasf.getExpected() != null) t2 += clasf.getExpected();
						if (t2 > t1) return 1;
						if (t1 > t2) return -1;
						return a.compareTo(b);
					}
				});
			}
			
			@Override
			public boolean isApplicable() {
				return true;
			}
			
			@Override
			public boolean hasSeparator() {
				return true;
			}
			
			@Override
			public String getName() {
				return MESSAGES.opSortBy(MESSAGES.colTotalOf(MESSAGES.fieldRequestedEnrollment()));
			}
		});
		hTotal.addOperation(new Operation() {
			@Override
			public void execute() {
				iTable.sort(hTotal, new Comparator<CurriculumInterface>() {
					@Override
					public int compare(CurriculumInterface a, CurriculumInterface b) {
						int t1 = 0, t2 = 0;
						switch (CurriculumCookie.getInstance().getCurriculaCoursesMode()) {
						case ENRL:
							for (CurriculumClassificationInterface clasf: a.getClassifications())
								if (clasf.getEnrollment() != null) t1 += clasf.getEnrollment();
							for (CurriculumClassificationInterface clasf: b.getClassifications())
								if (clasf.getEnrollment() != null) t2 += clasf.getEnrollment();
							break;
						case LAST:
							for (CurriculumClassificationInterface clasf: a.getClassifications())
								if (clasf.getLastLike() != null) t1 += clasf.getLastLike();
							for (CurriculumClassificationInterface clasf: b.getClassifications())
								if (clasf.getLastLike() != null) t2 += clasf.getLastLike();
							break;
						case PROJ:
							for (CurriculumClassificationInterface clasf: a.getClassifications())
								if (clasf.getProjection() != null) t1 += clasf.getProjection();
							for (CurriculumClassificationInterface clasf: b.getClassifications())
								if (clasf.getProjection() != null) t2 += clasf.getProjection();
							break;
						case REQ:
							for (CurriculumClassificationInterface clasf: a.getClassifications())
								if (clasf.getRequested() != null) t1 += clasf.getRequested();
							for (CurriculumClassificationInterface clasf: b.getClassifications())
								if (clasf.getRequested() != null) t2 += clasf.getRequested();
							break;
						}
						if (t2 > t1) return 1;
						if (t1 > t2) return -1;
						return a.compareTo(b);
					}
				});
			}
			
			@Override
			public boolean isApplicable() {
				return CurriculumCookie.getInstance().getCurriculaCoursesMode() != Mode.NONE;
			}
			
			@Override
			public boolean hasSeparator() {
				return false;
			}
			
			@Override
			public String getName() {
				return MESSAGES.opSortBy(MESSAGES.colTotalOf(CurriculumCookie.getInstance().getCurriculaCoursesMode().getName()));
			}
		});
		iTable.addRow(null, header);
		
		List<MyCell> allCells = new ArrayList<MyCell>();
		HashMap<AcademicClassificationInterface, List<MyCell>> clasf2cells = new HashMap<AcademicClassificationInterface, List<MyCell>>();
		
		for (CurriculumInterface curriculum: curricula) {
			List<Widget> line = new ArrayList<Widget>();
			List<MyCell> cells = new ArrayList<MyCell>();
			
			line.add(new Label(curriculum.getAbbv(), false));

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
				
				MyCell c = new MyCell(curriculum, clasf);
				cells.add(c);
				allCells.add(c);
				List<MyCell> cellsThisClasf = clasf2cells.get(acadClasf);
				if (cellsThisClasf == null) {
					cellsThisClasf = new ArrayList<MyCell>();
					clasf2cells.put(acadClasf, cellsThisClasf);
				}
				cellsThisClasf.add(c);
				line.add(c);
			}
			
			line.add(new MySumCell(cells));
			
			iTable.addRow(curriculum, line);
		}
		
		List<Widget> totals = new ArrayList<Widget>();
		totals.add(new Label("Total", false));
		for (AcademicClassificationInterface clasf: academicClassifications) {
			totals.add(new MySumCell(clasf2cells.get(clasf)));
		}
		totals.add(new MySumCell(allCells));
		iTable.addRow(null, totals);
		
		for (int i = 1; i < iTable.getRowCount() - 1; i++)
			iTable.getCellFormatter().getElement(i, iTable.getCellCount(i) - 1).getStyle().setBackgroundColor("#EEEEEE");
		for (int i = 0; i < iTable.getCellCount(iTable.getRowCount() - 1); i++)
			iTable.getCellFormatter().getElement(iTable.getRowCount() - 1, i).getStyle().setBackgroundColor("#EEEEEE");
		
		iTable.addMouseOverListener(new MouseOverListener<CurriculumInterface>() {
			@Override
			public void onMouseOver(TableEvent<CurriculumInterface> event) {
				iTable.getCellFormatter().getElement(event.getRow(), iTable.getCellCount(event.getRow()) - 1).getStyle().clearBackgroundColor();
			}
		});
		iTable.addMouseOutListener(new MouseOutListener<CurriculumInterface>() {
			@Override
			public void onMouseOut(TableEvent<CurriculumInterface> event) {
				iTable.getCellFormatter().getElement(event.getRow(), iTable.getCellCount(event.getRow()) - 1).getStyle().setBackgroundColor("#EEEEEE");
			}
		});
		iTable.addDataChangedListener(new DataChangedListener<CurriculumInterface>() {
			public void onDataInserted(DataChangedEvent<CurriculumInterface> event) {
			}
			@Override
			public void onDataMoved(List<DataChangedEvent<CurriculumInterface>> events) {
				for (DataChangedEvent<CurriculumInterface> event: events)
					iTable.getCellFormatter().getElement(event.getRow(), iTable.getCellCount(event.getRow()) - 1).getStyle().setBackgroundColor("#EEEEEE");
			}
			@Override
			public void onDataRemoved(DataChangedEvent<CurriculumInterface> event) {
			}
			@Override
			public void onDataSorted(List<DataChangedEvent<CurriculumInterface>> event) {
			}
		});
		
		iHeaderPanel.setVisible(true);
		iTable.setVisible(true);
		hideEmptyColumns();
	}
	
	private interface Updatable {
		public void update();
	}
	
	private class MyCell extends Composite implements Updatable, HasFocus, HasHint {
		private CurriculumInterface iCurriculum;
		private CurriculumClassificationInterface iClasf;
		
		private TextBox iTextBox;
		private HTML iRearLabel;
		private HorizontalPanel iPanel;
		
		
		private List<MySumCell> iSums = new ArrayList<MySumCell>();

		public MyCell(CurriculumInterface curriculum, CurriculumClassificationInterface classification) {
			iCurriculum	= curriculum;
			iClasf = classification;
			
			iPanel = new HorizontalPanel();
			
			iTextBox = new UniTimeTextBox(6, ValueBoxBase.TextAlignment.RIGHT);
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
		}
		
		public String getHint() {
			return 
				MESSAGES.propCurriculum() + " " + iCurriculum.getAbbv() + " - " + iCurriculum.getName() + "<br>" +
				MESSAGES.propAcademicArea() + " " + iCurriculum.getAcademicArea().getAbbv() + " - " + iCurriculum.getAcademicArea().getName() + "<br>" +
				(iCurriculum.hasMajors() ? MESSAGES.propMajor() + " " + iCurriculum.getCodeMajorNames(", ") + "<br>" : "" ) + 
				MESSAGES.propAcademicClassification() + " " + (iClasf.getName() != null ? iClasf.getName() : iClasf.getAcademicClassification().getCode()) + " - " + iClasf.getAcademicClassification().getName();
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
			case REQ: // Course Requests
				if (iClasf.getRequested() == null || iClasf.getRequested() == 0) {
					iRearLabel.setHTML("");
				} else {
					iRearLabel.setHTML(iClasf.getRequested().toString());
				}
				iRearLabel.setVisible(true);
				break;
			}
		}
		
		public CurriculumClassificationInterface getClassification() { return iClasf; }
		
		
		public boolean isEmpty() {
			return iTextBox.getText().isEmpty() && (!iRearLabel.isVisible() || iRearLabel.getHTML().isEmpty());
		}

		@Override
		public boolean focus() {
			iTextBox.setFocus(true);
			iTextBox.selectAll();
			return true;
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
		
		public int sumRequested() {
			int ret = 0;
			for (MyCell c: iCells)
				if (c.getClassification().getRequested() != null)
					ret += c.getClassification().getRequested();
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
			case REQ: // Course Requests
				iRearLabel.setHTML(sumRequested() == 0 ? "" : String.valueOf(sumRequested()));
				iRearLabel.setVisible(true);
				break;
			}
		}
	}
	
	public boolean hasHiddenColumn() {
		for (int c = 1; c < iTable.getCellCount(0) - 1; c++) {
			if (!iTable.isColumnVisible(c)) return true;
		}
		return false;			
	}
	
	public boolean isColumnEmpty(int col) {
		for (int r = 1; r < iTable.getRowCount() - 1; r++) {
			if (!((MyCell)iTable.getWidget(r, col)).isEmpty()) return false;
		}
		return true;
	}

	public boolean hasEmptyColumns() {
		for (int c = 1; c < iTable.getCellCount(0) - 1; c++) {
			if (iTable.isColumnVisible(c) && isColumnEmpty(c)) return true;
		}
		return false;
	}
	
	public void hideEmptyColumns() {
		for (int c = 1; c < iTable.getCellCount(0) - 1; c++) {
			iTable.setColumnVisible(c, !isColumnEmpty(c));
		}
	}

	public void showAllColumns() {
		for (int c = 1; c < iTable.getCellCount(0) - 1; c++) {
			iTable.setColumnVisible(c, true);
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
