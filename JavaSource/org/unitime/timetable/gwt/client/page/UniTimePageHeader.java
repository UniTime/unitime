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
package org.unitime.timetable.gwt.client.page;

import org.unitime.timetable.gwt.client.page.InfoPanelDisplay.Callback;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.SessionInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.SolverInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.UserInfoInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class UniTimePageHeader implements PageHeaderDisplay {
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);

	private PageHeader iHeader;
	private static UniTimePageHeader sInstance = null;
	
	private UniTimePageHeader() {
		iHeader = new PageHeader();

		getLeft().setCallback(new Callback() {
			@Override
			public void execute(Callback callback) {
				reloadSolverInfo(true, callback);
			}
		});

		reloadSessionInfo();
		reloadUserInfo();
		reloadSolverInfo(false, null);
	}
	
	public void insert(final RootPanel panel) {
		if (panel.getWidgetCount() > 0) return;
		panel.add(iHeader);
		panel.setVisible(true);
	}
	
	public static synchronized UniTimePageHeader getInstance() {
		if (sInstance == null) sInstance = new UniTimePageHeader();
		return sInstance;
	}
	
	public void reloadSessionInfo() {
		if (getRight().isPreventDefault()) return;
		RPC.execute(new MenuInterface.SessionInfoRpcRequest(), new AsyncCallback<SessionInfoInterface>() {
			@Override
			public void onSuccess(SessionInfoInterface result) {
				if (getRight().isPreventDefault()) return;
				getRight().setText(result.getSession());
				getRight().setInfo(result);
				getRight().setHint(MESSAGES.hintClickToChangeSession());
				getRight().setUrl("selectPrimaryRole.do?list=Y");
			}
			@Override
			public void onFailure(Throwable caught) {
				if (getRight().isPreventDefault()) return;
				getRight().setVisible(false);
			}
		});
	}

	public void reloadUserInfo() {
		if (getMiddle().isPreventDefault()) return;
		RPC.execute(new MenuInterface.UserInfoRpcRequest(), new AsyncCallback<UserInfoInterface>() {
			@Override
			public void onSuccess(UserInfoInterface result) {
				if (getMiddle().isPreventDefault()) return;
				getMiddle().setText(result.getName());
				getMiddle().setHint(result.getRole());
				getMiddle().setInfo(result);
				if (result.isChameleon()) {
					getMiddle().setUrl("chameleon.do");
				} else {
					getMiddle().setUrl("selectPrimaryRole.do?list=Y");
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (getMiddle().isPreventDefault()) return;
				getMiddle().setVisible(false);
			}
		});
	}
	
	public void reloadSolverInfo(boolean includeSolutionInfo, final Callback callback) {
		if (getLeft().isPreventDefault()) return;
		RPC.execute(new MenuInterface.SolverInfoRpcRequest(includeSolutionInfo), new AsyncCallback<SolverInfoInterface>() {
			@Override
			public void onSuccess(SolverInfoInterface result) {
				if (getLeft().isPreventDefault()) return;
				try {
					if (result != null) {
						getLeft().setText(result.getSolver());
						getLeft().setHint(result.getType());
						getLeft().setInfo(result);
						getLeft().setUrl(result.getUrl());
						getLeft().setVisible(true);
					} else {
						getLeft().setVisible(false);
					}
				} catch (Exception e) {}
				Timer t = new Timer() {
					@Override
					public void run() {
						reloadSolverInfo(getLeft().isPopupShowing(), null);
					}
				};
				t.schedule(result != null ? 1000 : 60000);
				if (callback != null) 
					callback.execute(null);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (getLeft().isPreventDefault()) return;
				Timer t = new Timer() {
					@Override
					public void run() {
						reloadSolverInfo(getLeft().isPopupShowing(), null);
					}
				};
				t.schedule(5000);
				if (callback != null) 
					callback.execute(null);
			}
		});
	}

	@Override
	public InfoPanel getLeft() {
		return iHeader.getLeft();
	}

	@Override
	public InfoPanel getMiddle() {
		return iHeader.getMiddle();
	}

	@Override
	public InfoPanel getRight() {
		return iHeader.getRight();
	}

	@Override
	public Widget asWidget() {
		return iHeader;
	}
}
