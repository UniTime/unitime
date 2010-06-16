package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumClassificationInterface;

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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
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
	
	public CurriculaTable() {
		iTable = new MyFlexTable();
		
		int col = 0;
		
		HTML selectHtml = new HTML("&otimes;");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "10px");
		iTable.getCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER);
		iTable.setWidget(0, col, selectHtml);
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
									iService.deleteCurricula(deleteIds, new AsyncCallback<Boolean>() {

										@Override
										public void onFailure(Throwable caught) {
											setError("Unable to delete selected curricula (" + caught.getMessage() + ")");
										}

										@Override
										public void onSuccess(Boolean result) {
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
						MenuItem delete = new MenuItem("Merge Selected Curricula", true, new Command() {
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
										iService.mergeCurricula(mergeIds, new AsyncCallback<Boolean>() {

											@Override
											public void onFailure(Throwable caught) {
												setError("Unable to merge selected curricula (" + caught.getMessage() + ")");
											}

											@Override
											public void onSuccess(Boolean result) {
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

		Label expLabel = new Label("Expected Students");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "50px");
		iTable.setWidget(0, col, expLabel);
		col++;
		expLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem sort = new MenuItem("Sort by Expected Students", true, new Command() {
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
		
		Label enrlLabel = new Label("Enrolled Students");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "50px");
		iTable.setWidget(0, col, enrlLabel);
		col++;
		enrlLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem sort = new MenuItem("Sort by Enrolled Students", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(6);
					}
				});
				sort.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sort);
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		});
		
		Label lastLabel = new Label("Last-Like Students");
		iTable.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iTable.getFlexCellFormatter().setWidth(0, col, "50px");
		iTable.setWidget(0, col, lastLabel);
		col++;
		lastLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem sort = new MenuItem("Sort by Last-Like Students", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						sort(6);
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
					iTable.setText(1 + c.getRow(), 5, c.getExpectedString());
					iTable.setText(1 + c.getRow(), 6, c.getEnrollmentString());
					iTable.setText(1 + c.getRow(), 7, c.getLastLikeString());
				}
				List<Long> noEnrl = new ArrayList<Long>();
				for (CurriculumInterface c: iData) {
					if (!c.hasClassifications()) {
						noEnrl.add(c.getId());
						if (noEnrl.size() == 1) {
							iTable.setWidget(1 + c.getRow(), 6, new Image(RESOURCES.loading_small()));
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
		iTable.setText(1 + c.getRow(), col++, c.getAbbv());
		iTable.setText(1 + c.getRow(), col++, c.getAcademicArea().getName());
		iTable.setHTML(1 + c.getRow(), col++, (c.hasMajors() && c.getMajors().size() > 3 ? c.getMajorCodes(", ") : c.getMajorNames("<br>")));
		iTable.setText(1 + c.getRow(), col++, c.getDepartment().getLabel());
		iTable.setText(1 + c.getRow(), col++, (c.getExpected() == null ? "": c.getExpectedString()));
		iTable.setText(1 + c.getRow(), col++, (c.getEnrollment() == null ? "" : c.getEnrollmentString()));		
		iTable.setText(1 + c.getRow(), col++, (c.getLastLike() == null ? "" : c.getLastLikeString()));
	}
	
	public void populate(TreeSet<CurriculumInterface> result) {
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
			if (curriculum.isEditable()) hasEditable = true;
			fillRow(curriculum);
			if (curriculum.getId().equals(iLastCurriculumId)) {
				iTable.getRowFormatter().setStyleName(1 + row, "unitime-TableRowSelected");
				rowToScroll = 1 + row;
			}
			if (curriculum.isEditable() && iSelectedCurricula.contains(curriculum.getId()))
				newlySelected.add(curriculum.getId());
			row++;
		}
		iTable.setWidget(1, 5, new Image(RESOURCES.loading_small()));
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
				populate(result);
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
			final boolean lastSevenRows = (row > 10 && row + 7 >= getRowCount());

			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				if ("unitime-TableRowProblem".equals(style)) {
				} else if ("unitime-TableRowSelected".equals(style)) {
					getRowFormatter().setStyleName(row, "unitime-TableRowSelectedHover");	
				} else {
					getRowFormatter().setStyleName(row, "unitime-TableRowHover");
				}
				iClassifications.populate(curriculum.getClassifications());
				iClassifications.setEnabled(false);
				final int x = event.getClientX();
				iClassificationsPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						iClassificationsPopup.setPopupPosition(
								Math.min(Math.max(x, tr.getAbsoluteLeft() + 15), tr.getAbsoluteRight() - offsetWidth - 15),
								lastSevenRows ? tr.getAbsoluteTop() - offsetHeight - 15 : tr.getAbsoluteBottom() + 15);
					}
				});
				break;
			case Event.ONMOUSEMOVE:
				if (iClassificationsPopup.isShowing()) {
					iClassificationsPopup.setPopupPosition(
							Math.min(Math.max(event.getClientX(), tr.getAbsoluteLeft() + 15),
									tr.getAbsoluteRight() - iClassificationsPopup.getOffsetWidth() - 15),
									lastSevenRows ? tr.getAbsoluteTop() - iClassificationsPopup.getOffsetHeight() - 15 : tr.getAbsoluteBottom() + 15);
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

}
