package org.unitime.timetable.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.CurriculaServiceAsync;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;
import org.unitime.timetable.gwt.widgets.LoadingWidget;

import com.google.gwt.core.client.GWT;
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

public class CurriculumProjectionRules extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private final CurriculaServiceAsync iService = GWT.create(CurriculaService.class);
	private static NumberFormat NF = NumberFormat.getFormat("##0.0");

	private MyFlexTable iTable;
	
	private VerticalPanel iPanel = null;
	private Button[] iSave = null, iClose = null;
	private Label[] iErrorMessage = null;
	private HorizontalPanel[] iHeaderPanel = null;
	
	private boolean iEditable = false;
	private boolean iPercent = true;
	private boolean iShowLastLike = true;
	
	private HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> iRules = null;
	
	private List<ProjectionRulesHandler> iProjectionRulesHandlers = new ArrayList<ProjectionRulesHandler>();
	
	public CurriculumProjectionRules() {
		
		iPanel = new VerticalPanel();
		iPanel.setWidth("100%");
		
		ClickHandler save = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show("Saving curriculum projection rules ...");
				iService.saveProjectionRules(iRules, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						showError("Failed to curricula projection rules (" + caught.getMessage() + ")");
						LoadingWidget.getInstance().hide();
						for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
							h.onException(caught);
						}
					}
					@Override
					public void onSuccess(Boolean result) {
						hideError();
						LoadingWidget.getInstance().hide();
						ProjectionRulesEvent e = new ProjectionRulesEvent();
						for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
							h.onRulesSaved(e);
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
				
		iHeaderPanel = new HorizontalPanel[] { new HorizontalPanel(), new HorizontalPanel()};
		iHeaderPanel[0].setStyleName("unitime-MainTableHeader");
		iErrorMessage = new Label[] {new Label(), new Label()};
		iSave = new Button[] { new Button("<u>S</u>ave"), new Button("<u>S</u>ave")};
		iClose = new Button[] { new Button("<u>C</u>lose"), new Button("<u>C</u>lose")};
		
		iSave[0].setAccessKey('s');
		iClose[0].setAccessKey('c');
		
		for (int i = 0; i < 2; i++) {
			iHeaderPanel[i].setSpacing(2);
			iHeaderPanel[i].setWidth("100%");
			iErrorMessage[i].setStyleName("unitime-ErrorMessage");
			iErrorMessage[i].setVisible(false);
			iSave[i].setVisible(false);
			iSave[i].addClickHandler(save);
			iSave[i].addStyleName("unitime-NoPrint");
			iClose[i].setVisible(false);
			iClose[i].addClickHandler(close);
			iClose[i].getElement().getStyle().setMarginLeft(4, Unit.PX);
			iClose[i].addStyleName("unitime-NoPrint");
			HorizontalPanel buttons = new HorizontalPanel();
			buttons.add(iSave[i]);
			buttons.add(iClose[i]);
			iHeaderPanel[i].add(iErrorMessage[i]);
			iHeaderPanel[i].setCellWidth(iErrorMessage[i], "100%");
			iHeaderPanel[i].add(buttons);
		}
		iHeaderPanel[0].setVisible(false);
		iHeaderPanel[0].addStyleName("unitime-NoPrint");
		
		iPanel.add(iHeaderPanel[0]);
		
		iTable = new MyFlexTable();
		iTable.setVisible(false);
		iPanel.add(iTable);

		iPanel.add(iHeaderPanel[1]);

		initWidget(iPanel);

		LoadingWidget.getInstance().show("Loading curriculum projection rules ...");
		iService.loadProjectionRules(new AsyncCallback<HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>>>() {
			@Override
			public void onFailure(Throwable caught) {
				showError("Loading failed (" + caught.getMessage() + ")");
				LoadingWidget.getInstance().hide();
				for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
					h.onException(caught);
				}
			}
			@Override
			public void onSuccess(HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> result) {
				try {
					iRules = result;
					hideError();
					refreshTable();
					
					iService.canEditProjectionRules(new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
						}
						@Override
						public void onSuccess(Boolean result) {
							if (result) {
								for (int i = 0; i < iSave.length; i++)
									iSave[i].setVisible(true);
								iEditable = true;
								updateAll();
							}
						}
					});
					
					ProjectionRulesEvent e = new ProjectionRulesEvent();
					for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
						h.onRulesLoaded(e);
					}
				} catch (Throwable t) {
					showError("Loading failed (" + t.getMessage() + ")");
					for (ProjectionRulesHandler h: iProjectionRulesHandlers) {
						h.onException(t);
					}
				} finally {
					LoadingWidget.getInstance().hide();
				}
			}
		});
	}
	
	public void showError(String error) {
		for (int i = 0; i < iErrorMessage.length; i++) {
			iErrorMessage[i].setText(error);
			iErrorMessage[i].setVisible(true);
		}
	}
	
	public void hideError() {
		for (int i = 0; i < iErrorMessage.length; i++) {
			iErrorMessage[i].setVisible(false);
		}
	}
	
	public void setAllowClose(boolean allow) {
		for (int i = 0; i < iClose.length; i++) {
			iClose[i].setVisible(allow);
		}
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
				if (w != null) ((MyCell)w).update();
			}
		}
	}
	
	public void refreshTable() throws CurriculaException {
		for (int row = iTable.getRowCount() - 1; row >= 0; row--)
			iTable.removeRow(row);

		if (iRules == null || iRules.isEmpty())
			throw new CurriculaException("No academic areas defined.");

		TreeSet<AcademicAreaInterface> areas = new TreeSet<AcademicAreaInterface>(iRules.keySet());
		TreeSet<AcademicClassificationInterface> classifications = null;
		
		MajorInterface defaultMajor = new MajorInterface();
		defaultMajor.setId(-1l);
		
		List<Set<AcademicClassificationInterface>> col2clasf = new ArrayList<Set<AcademicClassificationInterface>>();
		HashMap<AcademicClassificationInterface, Integer> clasf2col = new HashMap<AcademicClassificationInterface, Integer>();
		
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
			for (AcademicClassificationInterface clasf: classifications) {
				if (rr.getLastLike(clasf) <= 0) continue;
				Integer col = clasf2col.get(clasf);
				iTable.setWidget(row, 1 + col, new MyCell(rr, clasf));
			}
			row ++;
			
			for (MajorInterface major: new TreeSet<MajorInterface>(iRules.get(area).keySet())) {
				if (major.getId() < 0) continue;
				
				MyRow r = new MyRow(area, major, rules.get(major));
				if (!r.hasLastLike()) continue;
				r.setParent(rr); rr.addChild(r);
				
				Label majorLabel = new Label(major.getCode(), false);
				majorLabel.getElement().getStyle().setMarginLeft(10, Unit.PX);
				iTable.setWidget(row, 0, majorLabel);
				for (AcademicClassificationInterface clasf: classifications) {
					if (r.getLastLike(clasf) <= 0) continue;
					Integer col = clasf2col.get(clasf);
					iTable.setWidget(row, 1 + col, new MyCell(r, clasf));
				}
				iTable.getRowFormatter().setVisible(row, r.hasProjection());
				row ++;
			}
		}
		if (classifications == null || classifications.isEmpty())
			throw new CurriculaException("No academic classifications defined.");
		
		ClickHandler menu = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				if (iPercent)
					menu.addItem(new MenuItem("Show Numbers", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iPercent = false;
							updateAll();
						}
					}));
				else
					menu.addItem(new MenuItem("Show Percentages", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iPercent = true;
							updateAll();
						}
					}));
				if (iShowLastLike)
					menu.addItem(new MenuItem("Hide Last-Like Enrollments", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iShowLastLike = false;
							updateAll();
						}
					}));
				else
					menu.addItem(new MenuItem("Show Last-Like Enrollments", true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iShowLastLike = true;
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
					menu.addItem(new MenuItem("Collapse All", true, new Command() {
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
					menu.addItem(new MenuItem("Expand All", true, new Command() {
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
		if (row == 1)
			throw new CurriculaException("No last-like enrollments.");
		
		for (int r = 1; r < iTable.getRowCount(); r++) {
			for (int c = iTable.getCellCount(r); c < 1 + col2clasf.size(); c++) {
				iTable.setHTML(r, c, "&nbsp;");
			}
		}
		
		iHeaderPanel[0].setVisible(true);
		iTable.setVisible(true);
	}
	
	private class MyCell extends Composite {
		private MyRow iRow;
		private AcademicClassificationInterface iClasf;
		
		private TextBox iTextBox;
		private HTML iFrontLabel, iRearLabel;
		private HorizontalPanel iPanel;
		
		private HTML iHint = null;
		private PopupPanel iHintPanel = null;
		private boolean iCellEditable = true;
	
		
		public MyCell(MyRow row, AcademicClassificationInterface clasf) {
			iRow = row;
			iClasf = clasf;
			iRow.setCell(iClasf, this);
			
			iPanel = new HorizontalPanel();
			
			iTextBox = new TextBox();
			iTextBox.setWidth("60px");
			iTextBox.setStyleName("gwt-SuggestBox");
			iTextBox.setMaxLength(6);
			iTextBox.setTextAlignment(TextBox.ALIGN_RIGHT);
			iTextBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					float oldValue = iRow.getProjection(iClasf);
					try {
						if (iTextBox.getText().isEmpty()) {
							iRow.setProjection(iClasf, null);
						} else if (iTextBox.getText().endsWith("%")) {
							iRow.setProjection(iClasf, Float.valueOf(iTextBox.getText().substring(0, iTextBox.getText().length() - 1)) / 100.0f);
						} else {
							iRow.setProjection(iClasf, Float.valueOf(iTextBox.getText()) / iRow.getLastLike(iClasf));
						}
					} catch (Exception e) {
						iRow.setProjection(iClasf, null);
					}
					if (iRow.getParent() != null && iRow.getProjection(iClasf) == iRow.getParent().getProjection(iClasf)) {
						iRow.setProjection(iClasf, null);
					}
					update();
					if (iRow.getChildren() != null) {
						for (MyRow r: iRow.getChildren()) {
							if (r.iData.get(iClasf)[0] == null) {
								MyCell c = r.getCell(iClasf);
								if (c != null) c.update();
							}
						}
					}
				}
			});
			
			iFrontLabel = new HTML(String.valueOf(iRow.getLastLike(iClasf)) + " &rarr;&nbsp;");
			iFrontLabel.setWidth("50px");
			iFrontLabel.setStyleName("unitime-Label");
			iFrontLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			
			iRearLabel = new HTML("&nbsp;(of&nbsp;" + String.valueOf(iRow.getLastLike(iClasf))+")");
			iRearLabel.setWidth("50px");
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
			
			iHint = new HTML("Academic Area: " + iRow.getArea().getAbbv() + " - " + iRow.getArea().getName() + "<br>" +
					(iRow.getMajor() == null ? "" : "Major: " + iRow.getMajor().getCode() + " - " + iRow.getMajor().getName() + "<br>") +
					"Academic Classification: " + iClasf.getCode() + " - " + iClasf.getName(), false);
			iHintPanel = new PopupPanel();
			iHintPanel.setWidget(iHint);
			iHintPanel.setStyleName("unitime-PopupHint");
			
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONMOUSEMOVE);
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
		
		public void update() {
			float projection = iRow.getProjection(iClasf);
			int lastLike = iRow.getLastLike(iClasf);
			if (iPercent) {
				iTextBox.setText(NF.format(100.0 * projection) + "%");
			} else {
				iTextBox.setText(String.valueOf(Math.round(projection * lastLike)));
			}
			iTextBox.getElement().getStyle().setColor(iRow.isDefaultProjection(iClasf) ? "#777777" : null);
			setVisible(lastLike > 0);
			if (iCellEditable != iEditable) {
				iCellEditable = iEditable;
				iTextBox.setEnabled(iCellEditable);
				if (iCellEditable) {
					iTextBox.getElement().getStyle().setBorderColor(null);
					iTextBox.getElement().getStyle().setBackgroundColor(null);
				} else {
					iTextBox.getElement().getStyle().setBorderColor("transparent");
					iTextBox.getElement().getStyle().setBackgroundColor("transparent");
				}
			}
			iFrontLabel.setVisible(iShowLastLike && !iPercent);
			iRearLabel.setVisible(iShowLastLike && iPercent);
			if (projection == 1.0f) {
				iFrontLabel.setHTML("&nbsp;");
			} else {
				iFrontLabel.setHTML(String.valueOf(iRow.getLastLike(iClasf)) + " &rarr;&nbsp;");
			}
		}
		
		public MyRow getRow() { return iRow; }
		public AcademicClassificationInterface getClassification() { return iClasf; }
	}
	
	private class MyRow {
		private AcademicAreaInterface iArea;
		private MajorInterface iMajor;
		private HashMap<AcademicClassificationInterface, Number[]> iData;
		private MyRow iParent = null;
		private List<MyRow> iChildren = null;
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
			return iData.get(clasf)[0] == null;
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
		
		public MyFlexTable() {
			super();
			setCellPadding(2);
			setCellSpacing(0);
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONCLICK);
			sinkEvents(Event.ONKEYDOWN);
			setStylePrimaryName("unitime-MainTable");
			addStyleName("unitime-NotPrintableBottomLine");
		}
		
		private boolean focus(Event event, int oldRow, int oldCol, int row, int col) {
			if (!getRowFormatter().isVisible(row) || col >= getCellCount(row)) return false;
			final Widget w = getWidget(row, col);
			if (w == null || !w.isVisible()) return false;
			if (w instanceof MyCell) {
				((MyCell)w).iTextBox.setFocus(true);
				DeferredCommand.addCommand(new Command() {
					@Override
					public void execute() {
						((MyCell)w).iTextBox.selectAll();
					}
				});
				event.stopPropagation();
				return true;
			}
			return false;
		}
		
		public MyRow getMyRow(int row) {
			if (row == 0) return null;
		    for (int c = 1; c < getCellCount(row); c ++) {
			    Widget w = getWidget(row, c);
			    if (w != null) return ((MyCell)w).getRow();
		    }
		    return null;
		}
		
		public void onBrowserEvent(Event event) {
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    Element tr = DOM.getParent(td);
			int col = DOM.getChildIndex(tr, td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);
		    
		    MyRow r = getMyRow(row);
		    if (r == null) return;

			String style = getRowFormatter().getStyleName(row);

			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				getRowFormatter().setStyleName(row, "unitime-TableRowHover");
				if (r.getMajor() != null) getRowFormatter().getElement(row).getStyle().setCursor(Cursor.AUTO);
				break;
			case Event.ONMOUSEOUT:
				getRowFormatter().setStyleName(row, null);	
				break;
			case Event.ONCLICK:
				if (r.getMajor() != null) break;
				Element element = DOM.eventGetTarget(event);
				while (DOM.getElementProperty(element, "tagName").equalsIgnoreCase("div"))
					element = DOM.getParent(element);
				if (DOM.getElementProperty(element, "tagName").equalsIgnoreCase("td")) {
					if (r.getMajor() == null) {
						boolean canCollapse = false;
						for (int rx = row + 1; rx < getRowCount(); rx++) {
							r = getMyRow(rx);
							if (r == null || r.getMajor() == null) break;
							if (r.hasProjection()) continue;
							if (getRowFormatter().isVisible(rx)) {
								canCollapse = true; break;
							}
						}
						for (int rx = row + 1; rx < getRowCount(); rx++) {
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
