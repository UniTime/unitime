package org.unitime.timetable.gwt.client.rooms;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingsDataResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.GetBuildingsRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class BuildingsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iPanel;
	private SimpleForm iListBuildingsForm;
	private UniTimeHeaderPanel iListBuildingsHeader, iListBuildingsFooter;
	private BuildingsTable iBuildingsTable;
	private BuildingsEdit iBuildingsEdit;
	
	public BuildingsPage() {
		iPanel = new SimplePanel();
		
		iListBuildingsForm = new SimpleForm();
		
		iListBuildingsHeader = new UniTimeHeaderPanel(MESSAGES.sectBuildings());
		iListBuildingsHeader.addButton("add", MESSAGES.buttonAddBuilding(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addBuilding();
			}
		});
		iListBuildingsHeader.setEnabled("add", false);
		iListBuildingsHeader.addButton("export", MESSAGES.buttonExportPDF(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			}
		});
		iListBuildingsHeader.setEnabled("export", false);
		iListBuildingsHeader.addButton("updateData", MESSAGES.buttonBuildingsUpdateData(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			}
		});
		iListBuildingsHeader.setEnabled("updateData", false);
		iListBuildingsForm.addHeaderRow(iListBuildingsHeader);
		
		iBuildingsTable = new BuildingsTable();
		iListBuildingsForm.addRow(iBuildingsTable);
		
		iListBuildingsFooter = iListBuildingsHeader.clonePanel("");
		iListBuildingsForm.addBottomRow(iListBuildingsFooter);
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		RPC.execute(new GetBuildingsRequest(), new AsyncCallback<BuildingsDataResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
				iListBuildingsHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(BuildingsDataResponse result) {
				iListBuildingsHeader.setEnabled("add", result.isCanAdd());
				iListBuildingsHeader.setEnabled("export", result.isCanExportPDF());
				iListBuildingsHeader.setEnabled("updateData", result.isCanUpdateData());
				iBuildingsTable.setData(result.getBuildings());
				LoadingWidget.getInstance().hide();
				if (result.getEllipsoid() != null)
					iBuildingsEdit.setCoordinatesFormat(result.getEllipsoid());
			}
		});

		iBuildingsTable.addMouseClickListener(new MouseClickListener<BuildingInterface>() {
			@Override
			public void onMouseClick(TableEvent<BuildingInterface> event) {
				if (event.getData() != null && event.getData().isCanEdit())
					editBuilding(event.getData());
			}
		});
		
		iBuildingsEdit = new BuildingsEdit() {
			@Override
			protected void onBack(boolean refresh, Long buildingId) {
				if (refresh) {
					LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
					RPC.execute(new GetBuildingsRequest(), new AsyncCallback<BuildingsDataResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
							iListBuildingsHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
							ToolBox.checkAccess(caught);
						}

						@Override
						public void onSuccess(BuildingsDataResponse result) {
							iListBuildingsHeader.setEnabled("add", result.isCanAdd());
							iListBuildingsHeader.setEnabled("export", result.isCanExportPDF());
							iListBuildingsHeader.setEnabled("updateData", result.isCanUpdateData());
							iBuildingsTable.setData(result.getBuildings());
							LoadingWidget.getInstance().hide();
							if (result.getEllipsoid() != null)
								iBuildingsEdit.setCoordinatesFormat(result.getEllipsoid());
						}
					});
				} else {
					iPanel.setWidget(iListBuildingsForm);
					UniTimePageLabel.getInstance().setPageName(MESSAGES.pageBuildings());
				}
			}
		};
		
		iPanel.setWidget(iListBuildingsForm);
		initWidget(iPanel);
	}
	
	protected void addBuilding() {
		iBuildingsEdit.setValue(null);
		iPanel.setWidget(iBuildingsEdit);
		iBuildingsEdit.show();		
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddBuilding());
		iBuildingsTable.clearHover();
	}
	
	protected void editBuilding(BuildingInterface building) {
		iBuildingsEdit.setValue(building);
		iPanel.setWidget(iBuildingsEdit);
		iBuildingsEdit.show();
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEditBuilding());
		iBuildingsTable.clearHover();
	}

}
