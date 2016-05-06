/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	private Timer iTimer;
	
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
		iTimer = new Timer() {
			@Override
			public void run() {
				reloadSolverInfo(getLeft().isPopupShowing(), null);
			}
		};
		iTimer.schedule(1000);
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
				if (result == null || result.getSession() == null) {
					getRight().setVisible(false);	
				} else {
					getRight().setText(result.getSession());
					getRight().setInfo(result);
					getRight().setHint(MESSAGES.hintClickToChangeSession());
					getRight().setUrl("selectPrimaryRole.do?list=Y");
					getRight().setVisible(true);	
				}
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
				if (result == null || result.getName() == null) {
					getMiddle().setVisible(false);
				} else {
					getMiddle().setText(result.getName());
					getMiddle().setHint(result.getRole());
					getMiddle().setInfo(result);
					if (result.isChameleon()) {
						getMiddle().setUrl("chameleon.do");
					} else {
						getMiddle().setUrl("selectPrimaryRole.do?list=Y");
					}
					getMiddle().setVisible(true);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if (getMiddle().isPreventDefault()) return;
				getMiddle().setVisible(false);
			}
		});
	}
	
	public void reloadSolverInfo() {
		reloadSolverInfo(getLeft().isPopupShowing(), null);
	}
	
	public void reloadSolverInfo(boolean includeSolutionInfo, final Callback callback) {
		if (getLeft().isPreventDefault()) return;
		RPC.execute(new MenuInterface.SolverInfoRpcRequest(includeSolutionInfo), new AsyncCallback<SolverInfoInterface>() {
			@Override
			public void onSuccess(SolverInfoInterface result) {
				if (getLeft().isPreventDefault()) return;
				try {
					if (result != null && result.getSolver() != null) {
						getLeft().setText(result.getSolver());
						getLeft().setHint(result.getType());
						getLeft().setInfo(result);
						getLeft().setUrl(result.getUrl());
						getLeft().setVisible(true);
					} else {
						getLeft().setVisible(false);
					}
				} catch (Exception e) {}
				iTimer.schedule(result != null ? 1000 : 60000);
				if (callback != null) 
					callback.execute(null);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (getLeft().isPreventDefault()) return;
				iTimer.schedule(5000);
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
