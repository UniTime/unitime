package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.TreeSet;

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
import org.unitime.timetable.gwt.widgets.WebTable.RowClickEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CourseCurriculaTable extends Composite {

	private final CurriculaServiceAsync iCurriculaService = GWT.create(CurriculaService.class);

	private VerticalPanel iCurriculaPanel;
	private WebTable iCurricula;
	private DialogBox iDialog;
	private CurriculumEdit iCurriculumEdit;
	
	private AsyncCallback<TreeSet<CurriculumInterface>> iCourseCurriculaCallback = null;
	private TreeSet<AcademicClassificationInterface> iClassifications = null;
	
	private TreeSet<CourseInterface> iCourses = new TreeSet<CourseInterface>();
	
	private Long iOfferingId = null;
	
	public CourseCurriculaTable() {
		iCurricula = new WebTable();
		iCurricula.setHeader(new WebTable.Row(
				new WebTable.Cell("Curriculum", 1, "100"),
				new WebTable.Cell("Area", 1, "100"),
				new WebTable.Cell("Major(s)", 1, "100"),
				new WebTable.Cell("Curricula...", 1, "200")));
		iCurricula.setEmptyMessage("No data.");
		
		iCurriculaPanel = new VerticalPanel();
		iCurriculaPanel.add(iCurricula);
		Label h = new Label("Expected / Enrolled / Last-Like Students");
		h.setStyleName("unitime-Hint");
		iCurriculaPanel.add(h);
		iCurriculaPanel.setCellHorizontalAlignment(h, HasHorizontalAlignment.ALIGN_RIGHT);
		
		initWidget(iCurriculaPanel);
		
		iCurricula.addRowClickHandler(new WebTable.RowClickHandler() {
			@Override
			public void onRowClick(RowClickEvent event) {
				if (event.getRow().getId() != null) {
					LoadingWidget.getInstance().show();
					iCurriculaService.loadCurriculum(Long.valueOf(event.getRow().getId()), new AsyncCallback<CurriculumInterface>() {

						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
						}

						@Override
						public void onSuccess(CurriculumInterface result) {
							openDialog(result);
						}
					});
				} else {
					// Any action for Other and Total lines?
				}
			}
		});
	}
	
	private void openDialog(final CurriculumInterface curriculum) {
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
					iDialog.hide();
				}
			});
			iCurriculumEdit.setupClassifications(iClassifications);
			iCurriculaService.loadAcademicAreas(new AsyncCallback<TreeSet<AcademicAreaInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
				}
				@Override
				public void onSuccess(TreeSet<AcademicAreaInterface> result) {
					iCurriculumEdit.setupAreas(result);
					iCurriculaService.loadDepartments(new AsyncCallback<TreeSet<DepartmentInterface>>() {
						@Override
						public void onFailure(Throwable caught) {
						}
						@Override
						public void onSuccess(TreeSet<DepartmentInterface> result) {
							iCurriculumEdit.setupDepartments(result);
							iDialog.setText(curriculum.getName());
							iCurriculumEdit.edit(curriculum, false);
							iCurriculumEdit.showOnlyCourses(iCourses);
							iDialog.center();
							LoadingWidget.getInstance().hide();
						}
					});
				}
			});
		} else {
			iDialog.setText(curriculum.getName());
			iCurriculumEdit.edit(curriculum, false);
			iCurriculumEdit.showOnlyCourses(iCourses);
			iDialog.center();
			LoadingWidget.getInstance().hide();
		}
	}
	
	native void redirect(String url) /*-{
		$wnd.location.replace(url);
	}-*/;
	
	private void initHeader(final Command next) {
		iCurriculaService.loadAcademicClassifications(new AsyncCallback<TreeSet<AcademicClassificationInterface>>() {
			@Override
			public void onSuccess(TreeSet<AcademicClassificationInterface> result) {
				iClassifications = result;
				ArrayList<WebTable.Cell> header = new ArrayList<WebTable.Cell>();
				header.add(new WebTable.Cell("Curriculum", 1, "100"));
				header.add(new WebTable.Cell("Area", 1, "100"));
				header.add(new WebTable.Cell("Major(s)", 1, "100"));
				for (AcademicClassificationInterface clasf: result) {
					header.add(new WebTable.Cell(clasf.getCode(), 1, "75"));
				}
				for (int c = 3; c < header.size(); c++) {
					header.get(c).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				}
				iCurricula.setHeader(new WebTable.Row(header));
				if (next != null) next.execute();
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	private void initCallbacks() {
		if (iCourseCurriculaCallback == null) {
			iCourseCurriculaCallback = new AsyncCallback<TreeSet<CurriculumInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
					iCurricula.setEmptyMessage(caught.getMessage());
					iCourses.clear();
				}
				@Override
				public void onSuccess(TreeSet<CurriculumInterface> result) {
					iCourses.clear();
					if (!result.isEmpty()) {
						WebTable.Row[] rows = new WebTable.Row[1 + result.size()];
						int idx = 0;
						CurriculumInterface other = null;
						boolean[] used = new boolean[iClassifications.size()];
						for (int i = 0; i < used.length; i++)
							used[i] = false;
						int[][] total = new int[iClassifications.size()][];
						for (int i = 0; i <total.length; i++)
							total[i] = new int[] {0, 0, 0};
						for (CurriculumInterface curriculum: result) {
							for (CourseInterface course: curriculum.getCourses()) {
								CourseInterface cx = new CourseInterface();
								cx.setId(course.getId()); cx.setCourseName(course.getCourseName());
								iCourses.add(cx);
							}
							if (curriculum.getId() == null) { other = curriculum; continue; }
							ArrayList<WebTable.Cell> row = new ArrayList<WebTable.Cell>();
							row.add(new WebTable.Cell(curriculum.getAbbv()));
							row.add(new WebTable.Cell(curriculum.getAcademicArea().getAbbv()));
							row.add(new WebTable.Cell(curriculum.getMajorCodes(", ")));
							int col = 0;
							for (AcademicClassificationInterface clasf: iClassifications) {
								CurriculumClassificationInterface f = null;
								for (CurriculumClassificationInterface x: curriculum.getClassifications()) {
									if (x.getAcademicClassification().getId().equals(clasf.getId())) { f = x; break; }
								}
								int exp = 0, last = 0, enrl = 0;
								for (CourseInterface course: curriculum.getCourses()) {
									CurriculumCourseInterface cx = course.getCurriculumCourse(col);
									if (cx != null) {
										used[col] = true;
										exp += (f == null || f.getExpected() == null ? 0 : Math.round(f.getExpected() * cx.getShare()));
										last += (cx.getLastLike() == null ? 0 : cx.getLastLike());
										enrl += (cx.getEnrollment() == null ? 0 : cx.getEnrollment());
									}
								}
								total[col][0] += exp;
								total[col][1] += last;
								total[col][2] += enrl;
								String s = "";
								if (exp > 0 || enrl > 0 || last > 0)
									s = (exp > 0 ? exp : "-") + " / " + (enrl > 0 ? enrl : "-") + " / " + (last > 0 ? last : "-");
								row.add(new WebTable.WidgetCell(new Label(s, false), s));
								col++;
							}
							for (int c = 3; c < row.size(); c++) {
								row.get(c).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
							}
							rows[idx] = new WebTable.Row(row);
							rows[idx++].setId(curriculum.getId().toString());
						}
						if (other != null && other.hasCourses()) {
							ArrayList<WebTable.Cell> row = new ArrayList<WebTable.Cell>();
							row.add(new WebTable.Cell("<i>Other</i>"));
							row.add(new WebTable.Cell("-"));
							row.add(new WebTable.Cell("-"));
							int col = 0;
							for (AcademicClassificationInterface clasf: iClassifications) {
								int exp = 0, last = 0, enrl = 0;
								for (CourseInterface course: other.getCourses()) {
									CurriculumCourseInterface cx = course.getCurriculumCourse(col);
									if (cx != null) {
										used[col] = true;
										exp += 0;
										last += (cx.getLastLike() == null ? 0 : cx.getLastLike());
										enrl += (cx.getEnrollment() == null ? 0 : cx.getEnrollment());
									}
								}
								total[col][0] += exp;
								total[col][1] += last;
								total[col][2] += enrl;
								String s = "";
								if (exp > 0 || enrl > 0 || last > 0)
									s = (exp > 0 ? exp : "-") + " / " + (enrl > 0 ? enrl : "-") + " / " + (last > 0 ? last : "-");
								row.add(new WebTable.WidgetCell(new Label(s, false), s));
								col++;
							}
							for (int c = 3; c < row.size(); c++) {
								row.get(c).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
							}
							rows[idx++] = new WebTable.Row(row);
						}
						ArrayList<WebTable.Cell> row = new ArrayList<WebTable.Cell>();
						row.add(new WebTable.Cell("<b>Total</b>"));
						row.add(new WebTable.Cell("&nbsp;"));
						int[] tx = new int[] {0, 0, 0};
						for (int i = 0; i < total.length; i ++)
							for (int j = 0; j < 3; j++)
								tx[j] += total[i][j];
						row.add(new WebTable.Cell((tx[0] > 0 ? tx[0] : "-") + " / " + (tx[2] > 0 ? tx[2] : "-") + " / " + (tx[1] > 0 ? tx[1] : "-")));
						int col = 0;
						for (AcademicClassificationInterface clasf: iClassifications) {
							int exp = total[col][0];
							int last = total[col][1];
							int enrl = total[col][2];
							String s = (exp > 0 ? exp : "-") + " / " + (enrl > 0 ? enrl : "-") + " / " + (last > 0 ? last : "-");
							row.add(new WebTable.WidgetCell(new Label(s, false), s));
							col++;
						}
						for (int c = 0; c < row.size(); c++) {
							row.get(c).setStyleName("unitime-ClassRowFirst");
							if (c >= 3) row.get(c).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
						}
						rows[idx++] = new WebTable.Row(row);
						iCurricula.setData(rows);
						for (int i = 0; i < used.length; i++)
							iCurricula.setColumnVisible(3 + i, used[i]);
					} else {
						iCurricula.setEmptyMessage("The selected course has no curricula.");
					}
				}
			};			
		}
	}
	
	public void refresh() {
		Command populate = new Command() {
			@Override
			public void execute() {
				iCurricula.clearData(true);
				iCurricula.setEmptyMessage("Loading data...");
				iCurriculaService.findCurriculaForAnInstructionalOffering(iOfferingId, iCourseCurriculaCallback);
			}
		};
		if (iClassifications == null) {
			initHeader(populate);
		} else {
			populate.execute();
		}
	}
	
	public void insert(final RootPanel panel) {
		initCallbacks();
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		refresh();
		panel.getElement().setInnerText(null);
		panel.add(this);
	}
}
