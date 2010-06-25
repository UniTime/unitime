package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

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
import org.unitime.timetable.gwt.widgets.CurriculumEdit.EditFinishedEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CourseCurriculaTable extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);

	private final CurriculaServiceAsync iCurriculaService = GWT.create(CurriculaService.class);

	private VerticalPanel iCurriculaPanel;
	private Image iCurriculaImage, iLoadingImage;
	private MyFlexTable iCurricula;
	private DialogBox iDialog;
	private CurriculumEdit iCurriculumEdit;
	private Label iErrorLabel, iHint;
	
	private AsyncCallback<TreeSet<CurriculumInterface>> iCourseCurriculaCallback = null;
	
	private TreeSet<AcademicClassificationInterface> iClassifications = null;
	
	private TreeSet<CourseInterface> iCourses = new TreeSet<CourseInterface>();
	private List<ChainedCommand> iRowClicks = new ArrayList<ChainedCommand>();
	private List<Integer> iRowTypes = new ArrayList<Integer>();
	
	private Long iOfferingId = null;
	private String iCourseName = null;
	private boolean iVisible = true;
	private boolean[] iUsed = null;
	private HashSet<Long> iExpandedAreas = new HashSet<Long>();
	private HashSet<Long> iAllAreas = new HashSet<Long>();
	private int iSelectedRow = -1;
	private boolean iEditable = true;
	
	
	public static enum Type {
		EXP ("Planned"),
		ENRL ("Current"),
		LAST ("Last-Like"),
		PROJ ("Projected by Rule"),
		EXP2ENRL ("Planned / Current"),
		EXP2LAST ("Planned / Last-Like"),
		EXP2PROJ ("Planned / Projected"),
		LAST2ENRL ("Last-Like / Current"),
		PROJ2ENRL ("Projected / Current");

		private String iName;
		
		Type(String name) { iName = name; }
		
		public String getName() { return iName; }
	}

	private Type iType = Type.EXP;
	
	private static int sRowTypeHeader = 0;
	private static int sRowTypeArea = 1;
	private static int sRowTypeCurriculum = 2;
	private static int sRowTypeOtherArea = 3;
	private static int sRowTypeOther = 4;
	private static int sRowTypeTotal = 5;
	
	public CourseCurriculaTable(boolean visible, boolean editable, boolean showHeader) {
		iVisible = visible;
		iEditable = editable;
		
		iCurriculaPanel = new VerticalPanel();
		iCurriculaPanel.setWidth("100%");

		if (showHeader) {
			HorizontalPanel header = new HorizontalPanel();
			iCurriculaImage = new Image(iVisible ? RESOURCES.collapse() : RESOURCES.expand());
			iCurriculaImage.getElement().getStyle().setCursor(Cursor.POINTER);
			iCurriculaImage.setVisible(false);
			header.add(iCurriculaImage);
			Label curriculaLabel = new Label("Curricula", false);
			curriculaLabel.setStyleName("unitime3-HeaderTitle");
			curriculaLabel.getElement().getStyle().setPaddingLeft(2, Unit.PX);
			header.add(curriculaLabel);
			header.setCellWidth(curriculaLabel, "100%");
			header.setStyleName("unitime3-HeaderPanel");
			iCurriculaPanel.add(header);
			
			iCurriculaImage.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					iVisible = !iVisible;
					iCurriculaImage.setResource(iVisible ? RESOURCES.collapse() : RESOURCES.expand());
					if (iCurricula.getRowCount() > 2) {
						for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
							int rowType = iRowTypes.get(row);
							if (iVisible && (rowType == sRowTypeCurriculum || rowType == sRowTypeOtherArea)) continue;
							int hc = getHeaderCols(row);
							for (int col = 0; col < iClassifications.size()  + hc; col++) {
								iCurricula.getCellFormatter().setVisible(row, col, iVisible && (col < hc || iUsed[col - hc]));
							}
						}
						for (int col = 0; col < iClassifications.size()  + 2; col++) {
							iCurricula.getCellFormatter().setStyleName(iCurricula.getRowCount() - 1, col, iVisible ? "unitime-TotalRow" : null );
						}
					}
				}
			});
		}

		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.setVisible(false);
		iLoadingImage.getElement().getStyle().setMarginTop(10, Unit.PX);
		iCurriculaPanel.add(iLoadingImage);
		iCurriculaPanel.setCellHorizontalAlignment(iLoadingImage, HasHorizontalAlignment.ALIGN_CENTER);
		iCurriculaPanel.setCellVerticalAlignment(iLoadingImage, HasVerticalAlignment.ALIGN_MIDDLE);
		
		VerticalPanel tableAndHint = new VerticalPanel();
		
		iCurricula = new MyFlexTable();
		tableAndHint.add(iCurricula);
		
		iHint = new Label("Showing " + iType.getName() + " Enrollment");
		iHint.setStyleName("unitime-Hint");
		iHint.setVisible(false);
		tableAndHint.add(iHint);
		tableAndHint.setCellHorizontalAlignment(iHint, HasHorizontalAlignment.ALIGN_RIGHT);
		iCurriculaPanel.add(tableAndHint);
		iHint.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iType = Type.values()[(iType.ordinal() + 1) % Type.values().length];
				iHint.setText("Showing " + iType.getName() + " Enrollment");
				if (iCurricula.getRowCount() > 1) {
					for (int row = 1; row < iCurricula.getRowCount(); row++) {
						for (int col = 0; col < iClassifications.size(); col++) {
							((MyLabel)iCurricula.getWidget(row, getHeaderCols(row) + col)).refresh();
						}
					}
					((MyLabel)iCurricula.getWidget(iCurricula.getRowCount() - 1, 1)).refresh();
					((Label)iCurricula.getWidget(iCurricula.getRowCount() - 1, 0)).setText("Total " + iType.getName() + " Enrollment");
				}
			}
		});
		
		
		iErrorLabel = new Label("Oooops, something went wrong.");
		iErrorLabel.setStyleName("unitime-ErrorMessage");
		iCurriculaPanel.add(iErrorLabel);
		iErrorLabel.setVisible(false);
		
		initWidget(iCurriculaPanel);
	}
	
	private void openDialog(final CurriculumInterface curriculum, final ConditionalCommand next) {
		if (iDialog == null) {
			iDialog = new DialogBox();
			iDialog.setAnimationEnabled(true);
			iDialog.setAutoHideEnabled(true);
			iDialog.setGlassEnabled(true);
			iDialog.setModal(true);
			iCurriculumEdit = new CurriculumEdit();
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
					setErrorMessage("Failed to load academic areas (" + caught.getMessage() + ")");
					next.executeOnFailure();
				}
				@Override
				public void onSuccess(TreeSet<AcademicAreaInterface> result) {
					iCurriculumEdit.setupAreas(result);
					iCurriculaService.loadDepartments(new AsyncCallback<TreeSet<DepartmentInterface>>() {
						@Override
						public void onFailure(Throwable caught) {
							setErrorMessage("Failed to load departments (" + caught.getMessage() + ")");
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
	}
	
	native void redirect(String url) /*-{
		$wnd.location.replace(url);
	}-*/;
	
	private void init(final Command next) {
		iCurriculaService.loadAcademicClassifications(new AsyncCallback<TreeSet<AcademicClassificationInterface>>() {
			@Override
			public void onSuccess(TreeSet<AcademicClassificationInterface> result) {
				iClassifications = result;
				if (next != null) next.execute();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				setErrorMessage("Failed to load classifications (" + caught.getMessage() + ").");
			}
		});
	}
	
	public void clear(boolean loading) {
		for (int row = iCurricula.getRowCount() - 1; row >= 0; row--) {
			iCurricula.removeRow(row);
		}
		iCurricula.clear(true);
		iLoadingImage.setVisible(loading);
		iErrorLabel.setVisible(false);
		iHint.setVisible(false);
	}
	
	private void populate(TreeSet<CurriculumInterface> curricula) {
		// Menu
		ClickHandler menu = new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				MenuItem showHide = new MenuItem(iVisible ? "Hide Details" : "Show Details", true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						iVisible = !iVisible;
						if (iCurriculaImage != null)
							iCurriculaImage.setResource(iVisible ? RESOURCES.collapse() : RESOURCES.expand());
						if (iCurricula.getRowCount() > 2) {
							for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
								int rowType = iRowTypes.get(row);
								if (iVisible && (rowType == sRowTypeCurriculum || rowType == sRowTypeOtherArea)) continue;
								int hc = getHeaderCols(row);
								for (int col = 0; col < iClassifications.size()  + hc; col++) {
									iCurricula.getCellFormatter().setVisible(row, col, iVisible && (col < hc || iUsed[col - hc]));
								}
							}
							for (int col = 0; col < iClassifications.size()  + 2; col++) {
								iCurricula.getCellFormatter().setStyleName(iCurricula.getRowCount() - 1, col, iVisible ? "unitime-TotalRow" : null );
							}
						}
					}
				});
				showHide.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(showHide);
				if (iCurricula.getRowCount() > 2 && iVisible) {
					boolean canExpand = false, canCollapse = false;
					for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
						int rowType = iRowTypes.get(row);
						if (rowType == sRowTypeArea) {
							if (iCurricula.getCellFormatter().isVisible(row, 0))
								canExpand = true;
							else 
								canCollapse = true;
						}
					}
					if (canExpand) {
						MenuItem expandAll = new MenuItem("Expand All", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
									int rowType = iRowTypes.get(row);
									boolean visible = (rowType != sRowTypeArea && rowType != sRowTypeOther);
									int hc = getHeaderCols(row);
									for (int col = 0; col < iClassifications.size()  + hc; col++) {
										iCurricula.getCellFormatter().setVisible(row, col, visible && (col < hc || iUsed[col - hc]));
									}
									iExpandedAreas.clear();
									iExpandedAreas.addAll(iAllAreas);
								}
							}
						});
						expandAll.getElement().getStyle().setCursor(Cursor.POINTER);
						menu.addItem(expandAll);
					}
					if (canCollapse) {
						MenuItem collapseAll = new MenuItem("Collapse All", true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
									int rowType = iRowTypes.get(row);
									boolean visible = (rowType != sRowTypeCurriculum && rowType != sRowTypeOtherArea);
									int hc = getHeaderCols(row);
									for (int col = 0; col < iClassifications.size()  + hc; col++) {
										iCurricula.getCellFormatter().setVisible(row, col, visible && (col < hc || iUsed[col - hc]));
									}
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
					MenuItem item = new MenuItem(
							"Show " + t.getName() + " Enrollment",
							true,
							new Command() {
								@Override
								public void execute() {
									popup.hide();
									iType = t;
									iHint.setText("Showing " + t.getName() + " Enrollment");
									if (iCurricula.getRowCount() > 1) {
										for (int row = 1; row < iCurricula.getRowCount(); row++) {
											int hc = getHeaderCols(row);
											for (int col = 0; col < iClassifications.size(); col++) {
												((MyLabel)iCurricula.getWidget(row, hc + col)).refresh();
											}
										}
										((MyLabel)iCurricula.getWidget(iCurricula.getRowCount() - 1, 1)).refresh();
										((Label)iCurricula.getWidget(iCurricula.getRowCount() - 1, 0)).setText("Total " + t.getName() + " Enrollment");
									}
								}
							});
					if (t == iType)
						item.getElement().getStyle().setColor("#666666");
					item.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(item);
				}
				menu.setVisible(true);
				popup.add(menu);
				popup.showRelativeTo((Widget)event.getSource());
			}
		};
		
		// Create header
		int col = 0;
		final Label curriculumLabel = new Label("Curriculum", false);
		curriculumLabel.addClickHandler(menu);
		iCurricula.setWidget(0, col, curriculumLabel);
		iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iCurricula.getFlexCellFormatter().setWidth(0, col, "100px");
		col++;
		
		final Label areaLabel = new Label("Area", false);
		areaLabel.addClickHandler(menu);
		iCurricula.setWidget(0, col, areaLabel);
		iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iCurricula.getFlexCellFormatter().setWidth(0, col, "100px");
		col++;
		
		final Label majorLabel = new Label("Major(s)", false);
		majorLabel.addClickHandler(menu);
		iCurricula.setWidget(0, col, majorLabel);
		iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
		iCurricula.getFlexCellFormatter().setWidth(0, col, "100px");
		col++;
		
		for (AcademicClassificationInterface clasf: iClassifications) {
			final Label clasfLabel = new Label(clasf.getCode());
			clasfLabel.addClickHandler(menu);
			iCurricula.setWidget(0, col, clasfLabel);
			iCurricula.getFlexCellFormatter().setStyleName(0, col, "unitime-ClickableTableHeader");
			iCurricula.getFlexCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_RIGHT);
			iCurricula.getFlexCellFormatter().setWidth(0, col, "75px");
			col++;
		}
		
		// Create body
		iCourses.clear();
		iRowClicks.clear();
		iRowClicks.add(null); // for header row
		iRowTypes.clear();
		iRowTypes.add(sRowTypeHeader);
		
		int row = 0;
		List<CurriculumInterface> otherCurricula = new ArrayList<CurriculumInterface>();
		List<CurriculumInterface> lastArea = new ArrayList<CurriculumInterface>();
		iAllAreas.clear();
		iUsed = new boolean[iClassifications.size()];
		for (int i = 0; i < iUsed.length; i++)
			iUsed[i] = false;
		int[][] total = new int[iClassifications.size()][];
		for (int i = 0; i <total.length; i++)
			total[i] = new int[] {0, 0, 0, 0};
		int[][] totalThisArea = new int[iClassifications.size()][];
		for (int i = 0; i <totalThisArea.length; i++)
			totalThisArea[i] = new int[] {0, 0, 0, 0};
		
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
				int clasfIdx = 0;
				for (AcademicClassificationInterface clasf: iClassifications) {
					int exp = totalThisArea[clasfIdx][0];
					int last = totalThisArea[clasfIdx][1];
					int enrl = totalThisArea[clasfIdx][2];
					int proj = totalThisArea[clasfIdx][3];
					iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj));
					iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
					col++; clasfIdx++;
				}
				final int finalRow = row;
				final int lastAreas = lastArea.size();
				final Long lastAreaId = lastArea.get(0).getAcademicArea().getId();
				iRowClicks.add(new ChainedCommand() {
					@Override
					public void execute(final ConditionalCommand next) {
						iExpandedAreas.add(lastAreaId);
						int hc = getHeaderCols(finalRow);
						for (int col = 0; col < hc + iClassifications.size(); col++) {
							iCurricula.getCellFormatter().setVisible(finalRow, col, false);
						}
						for (int row = 1; row <= lastAreas; row++) {
							hc = getHeaderCols(finalRow - row);
							for (int col = 0; col < hc + iClassifications.size(); col++) {
								iCurricula.getCellFormatter().setVisible(finalRow - row, col, (col < hc || iUsed[col - hc]));
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
				iRowTypes.add(sRowTypeArea);
				if (iExpandedAreas.contains(lastAreaId))
					iRowClicks.get(row).execute(null);
				lastArea.clear();
				for (int i = 0; i <totalThisArea.length; i++)
					totalThisArea[i] = new int[] {0, 0, 0, 0};
				lastArea.add(curriculum);
			}
			col = 0; row++;
			iCurricula.setText(row, col++, curriculum.getAbbv());
			iCurricula.setText(row, col++, curriculum.getAcademicArea().getAbbv());
			iCurricula.setText(row, col++, curriculum.getMajorCodes(", "));
			for (int c = 0; c < 3; c++)
				iCurricula.getCellFormatter().setVisible(row, c, false);
			int clasfIdx = 0;
			for (AcademicClassificationInterface clasf: iClassifications) {
				CurriculumClassificationInterface f = null;
				for (CurriculumClassificationInterface x: curriculum.getClassifications()) {
					if (x.getAcademicClassification().getId().equals(clasf.getId())) { f = x; break; }
				}
				int exp = 0, last = 0, enrl = 0, proj = 0;
				for (CourseInterface course: curriculum.getCourses()) {
					CurriculumCourseInterface cx = course.getCurriculumCourse(clasfIdx);
					if (cx != null) {
						iUsed[clasfIdx] = true;
						exp += (f == null || f.getExpected() == null ? 0 : Math.round(f.getExpected() * cx.getShare()));
						last += (cx.getLastLike() == null ? 0 : cx.getLastLike());
						enrl += (cx.getEnrollment() == null ? 0 : cx.getEnrollment());
						proj += (cx.getProjection() == null ? 0 : cx.getProjection());
					}
				}
				total[clasfIdx][0] += exp;
				total[clasfIdx][1] += last;
				total[clasfIdx][2] += enrl;
				total[clasfIdx][3] += proj;
				totalThisArea[clasfIdx][0] += exp;
				totalThisArea[clasfIdx][1] += last;
				totalThisArea[clasfIdx][2] += enrl;
				totalThisArea[clasfIdx][3] += proj;
				iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj));
				iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				iCurricula.getCellFormatter().setVisible(row, col, false);
				col++;
				clasfIdx++;
			}
			if (iEditable) {
				iRowClicks.add(new ChainedCommand() {
					@Override
					public void execute(final ConditionalCommand next) {
						iCurriculaService.loadCurriculum(curriculum.getId(), new AsyncCallback<CurriculumInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								setErrorMessage("Failed to load details for " + curriculum.getAbbv() + " (" + caught.getMessage() + ")");
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
						return "Loading details for " + curriculum.getName() + " ...";
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
							int hc = getHeaderCols(row);
							for (int col = 0; col < iClassifications.size()  + hc; col++) {
								iCurricula.getCellFormatter().setVisible(row, col, false);
							}
							row --;
						}
						row = finalRow + 1;
						while (iRowTypes.get(row) == sRowTypeCurriculum) {
							int hc = getHeaderCols(row);
							for (int col = 0; col < iClassifications.size()  + hc; col++) {
								iCurricula.getCellFormatter().setVisible(row, col, false);
							}
							row ++;
						}
						int hc = getHeaderCols(row);
						for (int col = 0; col < iClassifications.size()  + hc; col++) {
							iCurricula.getCellFormatter().setVisible(row, col, (col < hc || iUsed[col - hc]));
						}
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
		}
		if (!lastArea.isEmpty()) {
			col = 0; row++;
			iCurricula.getFlexCellFormatter().setColSpan(row, col, 3);
			//iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
			iCurricula.setWidget(row, col++, new HTML("<i>" + lastArea.get(0).getAcademicArea().getAbbv() + " - " + lastArea.get(0).getAcademicArea().getName() + " (" + lastArea.size() + ")</i>", false));
			int clasfIdx = 0;
			for (AcademicClassificationInterface clasf: iClassifications) {
				int exp = totalThisArea[clasfIdx][0];
				int last = totalThisArea[clasfIdx][1];
				int enrl = totalThisArea[clasfIdx][2];
				int proj = totalThisArea[clasfIdx][3];
				iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj));
				iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				col++; clasfIdx++;
			}
			final int finalRow = row;
			final int lastAreas = lastArea.size();
			final Long lastAreaId = lastArea.get(0).getAcademicArea().getId();
			iRowClicks.add(new ChainedCommand() {
				@Override
				public void execute(final ConditionalCommand next) {
					iExpandedAreas.add(lastAreaId);
					int hc = getHeaderCols(finalRow);
					for (int col = 0; col < hc + iClassifications.size(); col++) {
						iCurricula.getCellFormatter().setVisible(finalRow, col, false);
					}
					for (int row = 1; row <= lastAreas; row++) {
						hc = getHeaderCols(finalRow - row);
						for (int col = 0; col < hc + iClassifications.size(); col++) {
							iCurricula.getCellFormatter().setVisible(finalRow - row, col, (col < hc || iUsed[col - hc]));
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
			iRowTypes.add(sRowTypeArea);
			if (iExpandedAreas.contains(lastAreaId))
				iRowClicks.get(row).execute(null);
		}
		
		// Other line
		if (!otherCurricula.isEmpty()) {
			int[][] totalOther = new int[iClassifications.size()][];
			for (int i = 0; i <totalOther.length; i++)
				totalOther[i] = new int[] {0, 0, 0, 0};
			for (CurriculumInterface other: otherCurricula) {
				col = 0; row++;
				iCurricula.getFlexCellFormatter().setColSpan(row, col, 3);
				//iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
				iCurricula.setHTML(row, col, "<i>" + other.getAbbv() + " - " + other.getName() + "</i>");
				iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
				iCurricula.getCellFormatter().setVisible(row, col, false);
				col++;
				int clasfIdx = 0;
				for (AcademicClassificationInterface clasf: iClassifications) {
					int exp = 0, last = 0, enrl = 0, proj = 0;;
					for (CourseInterface course: other.getCourses()) {
						CurriculumCourseInterface cx = course.getCurriculumCourse(clasfIdx);
						if (cx != null) {
							iUsed[clasfIdx] = true;
							exp += 0;
							last += (cx.getLastLike() == null ? 0 : cx.getLastLike());
							enrl += (cx.getEnrollment() == null ? 0 : cx.getEnrollment());
							proj += (cx.getProjection() == null ? 0 : cx.getProjection());
						}
					}
					total[clasfIdx][0] += exp;
					total[clasfIdx][1] += last;
					total[clasfIdx][2] += enrl;
					total[clasfIdx][3] += proj;
					totalOther[clasfIdx][0] += exp;
					totalOther[clasfIdx][1] += last;
					totalOther[clasfIdx][2] += enrl;
					totalOther[clasfIdx][3] += proj;
					iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj));
					iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
					iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
					iCurricula.getCellFormatter().setVisible(row, col, false);
					col++;
					clasfIdx++;
				}
				iRowTypes.add(sRowTypeOtherArea);
				final int finalRow = row;
				iRowClicks.add(new ChainedCommand() {
					@Override
					public void execute(final ConditionalCommand next) {
						int row = finalRow;
						while (row > 0 && iRowTypes.get(row) == sRowTypeOtherArea) {
							int hc = getHeaderCols(row);
							for (int col = 0; col < iClassifications.size()  + hc; col++) {
								iCurricula.getCellFormatter().setVisible(row, col, false);
							}
							row --;
						}
						row = finalRow + 1;
						while (iRowTypes.get(row) == sRowTypeOtherArea) {
							int hc = getHeaderCols(row);
							for (int col = 0; col < iClassifications.size()  + hc; col++) {
								iCurricula.getCellFormatter().setVisible(row, col, false);
							}
							row ++;
						}
						int hc = getHeaderCols(row);
						for (int col = 0; col < iClassifications.size()  + hc; col++) {
							iCurricula.getCellFormatter().setVisible(row, col, (col < hc || iUsed[col - hc]));
						}
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
			iCurricula.setWidget(row, col, new HTML("<i>Students without curricula</i>", false));
			iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
			col++;
			int clasfIdx = 0;
			for (AcademicClassificationInterface clasf: iClassifications) {
				int exp = totalOther[clasfIdx][0];
				int last = totalOther[clasfIdx][1];
				int enrl = totalOther[clasfIdx][2];
				int proj = totalOther[clasfIdx][3];
				iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj));
				iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				iCurricula.getCellFormatter().setStyleName(row, col, "unitime-OtherRow");
				col++; clasfIdx++;
			}
			final int finalRow = row;
			final int lastAreas = otherCurricula.size();
			iAllAreas.add(-1l);
			iRowClicks.add(new ChainedCommand() {
				@Override
				public void execute(final ConditionalCommand next) {
					iExpandedAreas.add(-1l);
					int hc = getHeaderCols(finalRow);
					for (int col = 0; col < hc + iClassifications.size(); col++) {
						iCurricula.getCellFormatter().setVisible(finalRow, col, false);
					}
					for (int row = 1; row <= lastAreas; row++) {
						hc = getHeaderCols(finalRow - row);
						for (int col = 0; col < hc + iClassifications.size(); col++) {
							iCurricula.getCellFormatter().setVisible(finalRow - row, col, (col < hc || iUsed[col - hc]));
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
			iRowTypes.add(sRowTypeOther);
			if (iExpandedAreas.contains(-1l))
				iRowClicks.get(row).execute(null);
		}
		
		// Total line
		col = 0; row++;
		iRowClicks.add(new ChainedCommand() {
			@Override
			public void execute(ConditionalCommand next) {
				iVisible = !iVisible;
				if (iCurriculaImage != null)
					iCurriculaImage.setResource(iVisible ? RESOURCES.collapse() : RESOURCES.expand());
				if (iCurricula.getRowCount() > 2) {
					for (int row = 1; row < iCurricula.getRowCount() - 1; row++) {
						int rowType = iRowTypes.get(row);
						if (iVisible && (rowType == sRowTypeCurriculum || rowType == sRowTypeOtherArea)) continue;
						int hc = getHeaderCols(row);
						for (int col = 0; col < iClassifications.size()  + hc; col++) {
							iCurricula.getCellFormatter().setVisible(row, col, iVisible && (col < hc || iUsed[col - hc]));
						}
					}
					for (int col = 0; col < iClassifications.size()  + 2; col++) {
						iCurricula.getCellFormatter().setStyleName(iCurricula.getRowCount() - 1, col, iVisible ? "unitime-TotalRow" : null );
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
		iCurricula.getFlexCellFormatter().setColSpan(row, col, 2);
		iCurricula.setWidget(row, col++, new Label("Total " + iType.getName() + " Enrollment", false));
		int[] tx = new int[] {0, 0, 0, 0};
		for (int i = 0; i < total.length; i ++)
			for (int j = 0; j < 4; j++)
				tx[j] += total[i][j];
		iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
		iCurricula.setWidget(row, col++, new MyLabel(tx[0], tx[2], tx[1], tx[3]));
		for (int c = 0; c < 2; c++)
			iCurricula.getCellFormatter().setStyleName(row, c, "unitime-TotalRow");
		int clasfIdx = 0;
		for (AcademicClassificationInterface clasf: iClassifications) {
			int exp = total[clasfIdx][0];
			int last = total[clasfIdx][1];
			int enrl = total[clasfIdx][2];
			int proj = total[clasfIdx][3];
			iCurricula.setWidget(row, col, new MyLabel(exp, enrl, last, proj));
			iCurricula.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
			iCurricula.getCellFormatter().setStyleName(row, col, "unitime-TotalRow");
			col++; clasfIdx++;
		}
		
		// Hide all lines if requested
		if (!iVisible) {
			for (int r = 1; r < iCurricula.getRowCount() - 1; r++) {
				int hc = getHeaderCols(r);
				for (int c = 0; c < hc + iClassifications.size(); c++) {
					iCurricula.getCellFormatter().setVisible(r, c, false);
				}
			}
			int r = iCurricula.getRowCount() - 1;
			int hc = getHeaderCols(r);
			for (int c = 0; c < hc + iClassifications.size(); c++) {
				iCurricula.getCellFormatter().setStyleName(r, c, null);
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
		if (iType == Type.EXP && tx[0] == 0) {
			if (tx[2] > 0) {
				iType = Type.ENRL;
				typeChanged = true;
			} else if (tx[1] > 0) {
				iType = Type.LAST;
				typeChanged = true;
			}
		}
		if (iType == Type.ENRL && tx[2] == 0) {
			if (tx[0] > 0) {
				iType = Type.EXP;
				typeChanged = true;
			} else if (tx[1] > 0) {
				iType = Type.LAST;
				typeChanged = true;
			}
		}
		if (iType == Type.LAST && tx[1] == 0) {
			if (tx[0] > 0) {
				iType = Type.EXP;
				typeChanged = true;
			} else if (tx[2] > 0) {
				iType = Type.ENRL;
				typeChanged = true;
			}
		}
		if (iType == Type.PROJ && tx[3] == 0) {
			if (tx[0] > 0) {
				iType = Type.EXP;
				typeChanged = true;
			} else if (tx[1] > 0) {
				iType = Type.ENRL;
				typeChanged = true;
			} else if (tx[2] > 0) {
				iType = Type.LAST;
				typeChanged = true;
			}
		}
		if (typeChanged) {
			iHint.setText("Showing " + iType.getName() + " Enrollment");
			if (iCurricula.getRowCount() > 1) {
				for (int r = 1; r < iCurricula.getRowCount(); r++) {
					int hc = getHeaderCols(r);
					for (int c = 0; c < iClassifications.size(); c++) {
						((MyLabel)iCurricula.getWidget(r, hc + c)).refresh();
					}
				}
				((MyLabel)iCurricula.getWidget(iCurricula.getRowCount() - 1, 1)).refresh();
				((Label)iCurricula.getWidget(iCurricula.getRowCount() - 1, 0)).setText("Total " + iType.getName() + " Enrollment");
			}
		}
		
		iLoadingImage.setVisible(false);
		iHint.setVisible(true);
		iCurriculaImage.setVisible(true);
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
					setErrorMessage("Failed to load curricula (" + caught.getMessage() + ").");
					iLoadingImage.setVisible(false);
				}
				@Override
				public void onSuccess(TreeSet<CurriculumInterface> result) {
					if (result.isEmpty()) {
						setMessage("The selected offering has no curricula.");
					} else {
						populate(result);
					}
					iLoadingImage.setVisible(false);
				}
			};			
		}
	}
	
	public void refresh() {
		Command populate = new Command() {
			@Override
			public void execute() {
				clear(true);
				if (iOfferingId != null)
					iCurriculaService.findCurriculaForAnInstructionalOffering(iOfferingId, iCourseCurriculaCallback);
				else
					iCurriculaService.findCurriculaForACourse(iCourseName, iCourseCurriculaCallback);
			}
		};
		if (iClassifications == null) {
			init(populate);
		} else {
			populate.execute();
		}
	}
	
	public void insert(final RootPanel panel) {
		initCallbacks();
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		iCourseName = null;
		refresh();
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
	
	public class MyLabel extends Label {
		private int iExp, iLast, iEnrl, iProj;
		
		public MyLabel(int exp, int enrl, int last, int proj) {
			//super(exp > 0 || enrl > 0 || last > 0 ? ((exp > 0 ? exp : "-") + " / " + (enrl > 0 ? enrl : "-") + " / " + (last > 0 ? last : "-")) : "", false);
			super("", false);
			iExp = exp;
			iLast = last;
			iEnrl = enrl;
			iProj = proj;
			refresh();
		}
		
		public void showExpected() {
			setText(iExp > 0 ? String.valueOf(iExp) : "");
		}
		
		public void showEnrolled() {
			setText(iEnrl > 0 ? String.valueOf(iEnrl) : "");
		}

		public void showLastLike() {
			setText(iLast > 0 ? String.valueOf(iLast) : "");
		}
		
		public void showProjected() {
			setText(iProj > 0 ? String.valueOf(iProj) : "");
		}

		public void showExpectedEnrolled() {
			if (iExp > 0 || iEnrl > 0)
				setText((iExp > 0 ? String.valueOf(iExp) : "-") + " / " + (iEnrl > 0 ? String.valueOf(iEnrl) : "-"));
			else
				setText("");
		}
		
		public void showExpectedLastLike() {
			if (iExp > 0 || iLast > 0)
				setText((iExp > 0 ? String.valueOf(iExp) : "-") + " / " + (iLast > 0 ? String.valueOf(iLast) : "-"));
			else
				setText("");
		}

		public void showExpectedProjected() {
			if (iExp > 0 || iProj > 0)
				setText((iExp > 0 ? String.valueOf(iExp) : "-") + " / " + (iProj > 0 ? String.valueOf(iProj) : "-"));
			else
				setText("");
		}

		public void showLastLikeEnrolled() {
			if (iLast > 0 || iEnrl > 0)
				setText((iLast > 0 ? String.valueOf(iLast) : "-") + " / " + (iEnrl > 0 ? String.valueOf(iEnrl) : "-"));
			else
				setText("");
		}
		
		public void showProjectedEnrolled() {
			if (iProj > 0 || iEnrl > 0)
				setText((iProj > 0 ? String.valueOf(iProj) : "-") + " / " + (iEnrl > 0 ? String.valueOf(iEnrl) : "-"));
			else
				setText("");
		}

		public void refresh() {
			switch (iType) {
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
	
	public void setErrorMessage(String message) {
		iErrorLabel.setStyleName("unitime-ErrorMessage");
		iErrorLabel.setText(message);
		iErrorLabel.setVisible(message != null && !message.isEmpty());
	}
	
	public void setMessage(String message) {
		iErrorLabel.setStyleName("unitime-Message");
		iErrorLabel.setText(message);
		iErrorLabel.setVisible(message != null && !message.isEmpty());
	}
	
}
