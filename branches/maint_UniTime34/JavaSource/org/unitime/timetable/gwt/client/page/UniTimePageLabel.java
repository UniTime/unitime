/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.page;

import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class UniTimePageLabel extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	public static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	public static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);

	private final MenuServiceAsync iService = GWT.create(MenuService.class);

	private HorizontalPanel iPanel;
	
	private Label iName;
	private Image iHelp;
	
	private String iUrl = null;
	
	private static UniTimePageLabel sInstance = null;
	
	private UniTimePageLabel() {
		iPanel = new HorizontalPanel();
		
        iName = new Label();
        iName.setStyleName("unitime-Title");
		iHelp = new Image(RESOURCES.help());
		iHelp.setVisible(false);
		iHelp.getElement().getStyle().setCursor(Cursor.POINTER);
		
		iPanel.add(iName);
		iPanel.add(iHelp);
		iPanel.setCellVerticalAlignment(iHelp, HasVerticalAlignment.ALIGN_TOP);
				
		initWidget(iPanel);
				
		iHelp.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iUrl == null) return;
				UniTimeFrameDialog.openDialog(MESSAGES.pageHelp(iName.getText()), iUrl);
			}
		});
		
	}
	
	public static UniTimePageLabel getInstance() {
		if (sInstance == null)
			sInstance = new UniTimePageLabel();
		return sInstance;
	}
	
	public void insert(final RootPanel panel) {
		String title = panel.getElement().getInnerText();
		if (title != null && !title.isEmpty())
			setPageName(title);
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}
	
	public void setPageName(final String title) {
		iService.getHelpPageAndLocalizedTitle(title, new AsyncCallback<String[]>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.setTitle("UniTime " + CONSTANTS.version() + "| " + title);
				iName.setText(title);
				iHelp.setTitle(MESSAGES.pageHelp(title));
				iHelp.setVisible(false);
				iUrl = null;
			}
			@Override
			public void onSuccess(String[] result) {
				if (result[0] == null) {
					iHelp.setVisible(false);
					iUrl = null;
				} else {
					iHelp.setVisible(true);
					iUrl = result[0];
				}
				if (result[1] != null) {
					iName.setText(result[1]);
					iHelp.setTitle(MESSAGES.pageHelp(result[1]));
					Window.setTitle("UniTime " + CONSTANTS.version() + "| " + result[1]);
				} else {
					Window.setTitle("UniTime " + CONSTANTS.version() + "| " + title);
					iName.setText(title);
					iHelp.setTitle(MESSAGES.pageHelp(title));
				}
			}
		});
	}
}
