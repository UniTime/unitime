package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class UniTimeVersion extends Composite {
	private final MenuServiceAsync iService = GWT.create(MenuService.class);

	private Label iLabel;
	
	public UniTimeVersion() {
		iLabel = new Label();
		//iLabel.setStyleName("unitime-Footer");
		
		iService.getVersion(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				iLabel.setText(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
		
		initWidget(iLabel);
	}
	
	public void insert(final RootPanel panel) {
		panel.add(this);
		panel.setVisible(true);
	}


}
