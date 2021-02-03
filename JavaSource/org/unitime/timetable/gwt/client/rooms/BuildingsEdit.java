package org.unitime.timetable.gwt.client.rooms;

import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

public class BuildingsEdit extends Composite implements TakesValue<BuildingInterface>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private UniTimeWidget<TextBox> iName;
	private UniTimeWidget<TextBox> iAbbreviation;
	private TextBox iExternalId;
	private NumberBox iX, iY;
	private UniTimeWidget<P> iCoordinates;
	private P iCoordinatesFormat;
	private MapWidget iMap = null;
	private BuildingInterface iBuilding = null;
	
	public BuildingsEdit() {
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-BuildingEdit");
		
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("save", MESSAGES.buttonSave(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
		iHeader.addButton("update", MESSAGES.buttonUpdate(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
		iHeader.addButton("delete", MESSAGES.buttonDelete(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onBack(false, null);
			}
		});
		iForm.addHeaderRow(iHeader);
		
		iName = new UniTimeWidget<TextBox>(new TextBox());
		iName.getWidget().setStyleName("unitime-TextBox");
		iName.getWidget().setMaxLength(100);
		iName.getWidget().setWidth("600px");
		iName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iName.clearHint();
				iHeader.clearMessage();
			}
		});
		iForm.addRow(MESSAGES.propName(), iName);
		
		iAbbreviation = new UniTimeWidget<TextBox>(new TextBox());
		iAbbreviation.getWidget().setStyleName("unitime-TextBox");
		iAbbreviation.getWidget().setMaxLength(20);
		iAbbreviation.getWidget().setWidth("100px");
		iAbbreviation.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iAbbreviation.clearHint();
				iHeader.clearMessage();
			}
		});
		iForm.addRow(MESSAGES.propAbbreviation(), iAbbreviation);
		
		iExternalId = new TextBox();
		iExternalId.setStyleName("unitime-TextBox");
		iExternalId.setMaxLength(40);
		iExternalId.setWidth("200px");
		iForm.addRow(MESSAGES.propExternalId(), iExternalId);
		
		iX =new NumberBox();
		iX.setMaxLength(12);
		iX.setWidth("80px");
		iX.setDecimal(true);
		iX.setNegative(true);
		iX.addStyleName("number");
		iY = new NumberBox();
		iY.setMaxLength(12);
		iY.setWidth("80px");
		iY.setDecimal(true);
		iY.setNegative(true);
		iY.addStyleName("number");
		iX.getElement().setId("coordX");
		iY.getElement().setId("coordY");
		iCoordinates = new UniTimeWidget<P>(new P("coordinates"));
		iCoordinates.getWidget().add(iX);
		P comma = new P("comma"); comma.setText(", ");
		iCoordinates.getWidget().add(comma);
		iCoordinates.getWidget().add(iY);
		iCoordinatesFormat = new P("format");
		iCoordinates.getWidget().add(iCoordinatesFormat);
		iForm.addRow(MESSAGES.propCoordinates(), iCoordinates);
		
		iFooter = iHeader.clonePanel("");

		MapWidget.createWidget(iX, iY, new AsyncCallback<MapWidget>() {
			@Override
			public void onSuccess(MapWidget result) {
				iMap = result;
				if (iMap != null) {
					iMap.setEnabled(true);
					iForm.addRow(iMap);
				}
				iForm.addBottomRow(iFooter);
			}
			@Override
			public void onFailure(Throwable caught) {
				iForm.addBottomRow(iFooter);
			}
		});
		
		initWidget(iForm);
	}
	
	protected void onBack(boolean refresh, Long buildingId) {}
	
	public void show() {
		if (iMap != null) iMap.onShow();
	}
	
	public void setCoordinatesFormat(String format) {
		iCoordinatesFormat.setText(format);
	}

	@Override
	public void setValue(BuildingInterface building) {
		if (building == null) {
			iHeader.setHeaderTitle(MESSAGES.sectAddBuilding());
			iHeader.setEnabled("save", true);
			iHeader.setEnabled("update", false);
			iHeader.setEnabled("delete", false);
			iHeader.setEnabled("back", true);
			iName.getWidget().setText("");
			iAbbreviation.getWidget().setText("");
			iExternalId.setText("");
			iX.setValue((Number)null); iY.setValue((Number)null);
			if (iMap != null) iMap.setMarker();
			iBuilding = new BuildingInterface();
		} else {
			iHeader.setHeaderTitle(MESSAGES.sectEditBuilding());
			iHeader.setEnabled("save", false);
			iHeader.setEnabled("update", true);
			iHeader.setEnabled("delete", building.isCanDelete());
			iHeader.setEnabled("back", true);
			iName.getWidget().setText(building.getName() == null ? "" : building.getName());
			iAbbreviation.getWidget().setText(building.getAbbreviation() == null ? "" : building.getAbbreviation());
			iExternalId.setText(building.getExternalId() == null ? "" : building.getExternalId());
			iX.setValue(building.getX()); iY.setValue(building.getY());
			if (iMap != null) iMap.setMarker();
			iBuilding = building;
		}
	}

	@Override
	public BuildingInterface getValue() {
		iBuilding.setName(iName.getWidget().getText());
		iBuilding.setAbbreviation(iAbbreviation.getWidget().getText());
		iBuilding.setExternalId(iExternalId.getText());
		iBuilding.setX(iX.toDouble());
		iBuilding.setY(iY.toDouble());
		return iBuilding;
	}

}
