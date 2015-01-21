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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.command.client.GwtRpc;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class LoadingWidget extends Composite {
	public static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	public static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private static LoadingWidget sInstance = null;

	private AbsolutePanel iPanel = null;
	private Image iImage = null;
	private int iCount = 0;
	private Timer iWarningTimer = null;
	private HTML iWarning;
	private HTML iMessage = null;
	private HTML iCancel;
	private Timer iCancelTimer = null;
	private Long iExecutionId = null;
	
	public LoadingWidget() {
		iPanel = new AbsolutePanel();
		iPanel.setStyleName("unitime-LoadingPanel");
		iImage = new Image(RESOURCES.loading());
		iImage.setStyleName("unitime-LoadingIcon");
		initWidget(iPanel);
		Window.addWindowScrollHandler(new Window.ScrollHandler() {
			@Override
			public void onWindowScroll(Window.ScrollEvent event) {
				if (iCount > 0) {
					iPanel.getElement().getStyle().setProperty("left", String.valueOf(event.getScrollLeft()));
					iPanel.getElement().getStyle().setProperty("top", String.valueOf(event.getScrollTop()));
					iImage.getElement().getStyle().setProperty("left", String.valueOf(event.getScrollLeft() + Window.getClientWidth() / 2));
					iImage.getElement().getStyle().setProperty("top", String.valueOf(event.getScrollTop() + Window.getClientHeight() / 2));
					iWarning.getElement().getStyle().setProperty("left", String.valueOf(event.getScrollLeft() + Window.getClientWidth() / 2 - 225));
					iWarning.getElement().getStyle().setProperty("top", String.valueOf(event.getScrollTop() + 5 * Window.getClientHeight() / 12));
					iMessage.getElement().getStyle().setProperty("left", String.valueOf(event.getScrollLeft() + Window.getClientWidth() / 2 - 225));
					iMessage.getElement().getStyle().setProperty("top", String.valueOf(event.getScrollTop() + Window.getClientHeight() / 3));
					iCancel.getElement().getStyle().setProperty("left", String.valueOf(event.getScrollLeft() + Window.getClientWidth() / 2 - 225));
					iCancel.getElement().getStyle().setProperty("top", String.valueOf(event.getScrollTop() + 5 * Window.getClientHeight() / 12));
				}
			}
		});
		iWarning = new HTML(MESSAGES.warnLoadingTooLong(), true);
		iWarning.setStyleName("unitime-Notification");
		iWarning.addStyleName("unitime-NotificationError");
		iCancel = new HTML(MESSAGES.warnLoadingTooLongCanCancel());
		iCancel.setStyleName("unitime-Notification");
		iCancel.addStyleName("unitime-NotificationWarning");
		iCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iExecutionId != null) GwtRpc.cancel(iExecutionId);
			}
		});
		iMessage = new HTML("", true);
		iMessage.setStyleName("unitime-Notification");
		iMessage.addStyleName("unitime-NotificationInfo");
		iWarningTimer = new Timer() {
			@Override
			public void run() {
				RootPanel.get().add(iWarning, Window.getScrollLeft() + Window.getClientWidth() / 2 - 225, Window.getScrollTop() + 5 * Window.getClientHeight() / 12);
			}
		};
		iCancelTimer = new Timer() {
			@Override
			public void run() {
				RootPanel.get().add(iCancel, Window.getScrollLeft() + Window.getClientWidth() / 2 - 225, Window.getScrollTop() + 5 * Window.getClientHeight() / 12);
			}
		};
	}
	
	public void show() {
		show(null);
	}
	
	public void show(String message) {
		show(message, 120000);
	}
	
	protected void showCancel(Long executionId) {
		iExecutionId = executionId;
		iWarningTimer.cancel();
		iCancelTimer.schedule(2500);
	}
	
	protected void hideCancel() {
		iCancelTimer.cancel();
		RootPanel.get().remove(iCancel);
	}

	public void show(String message, int warningDelayInMillis) {
		if (iCount == 0) {
			RootPanel.get().add(this, Window.getScrollLeft(), Window.getScrollTop());
			RootPanel.get().add(iImage, Window.getScrollLeft() + Window.getClientWidth() / 2, Window.getScrollTop() + Window.getClientHeight() / 2);
			iWarningTimer.schedule(warningDelayInMillis);
		}
		if (message != null) {
			boolean showing = (iCount > 0 && !iMessage.getText().isEmpty());
			iMessage.setHTML(message);
			if (!showing && !iMessage.getText().isEmpty()) {
				RootPanel.get().add(iMessage, Window.getScrollLeft() + Window.getClientWidth() / 2 - 225, Window.getScrollTop() + Window.getClientHeight() / 3);
			} else if (showing && iMessage.getText().isEmpty()) {
				RootPanel.get().remove(iMessage);
			}
			AriaStatus.getInstance().setText(message.replace("...", "."));
		}
		iCount ++;
	}
	
	public void setMessage(String message) {
		iMessage.setHTML(message);
	}

	public void hide() {
		if (iCount > 0) iCount --;
		if (iCount == 0) {
			RootPanel.get().remove(iImage);
			RootPanel.get().remove(this);
			iWarningTimer.cancel();
			iCancelTimer.cancel();
			RootPanel.get().remove(iWarning);
			RootPanel.get().remove(iMessage);
			RootPanel.get().remove(iCancel);
			iMessage.setHTML("");
		}
	}
	
	public boolean isShowing() {
		return iCount > 0;
	}

	public static LoadingWidget getInstance() {
		if (sInstance == null) sInstance = new LoadingWidget();
		return sInstance;
	}
	
	public static native void createTriggers()/*-{
		$wnd.showGwtLoading = function(message) {
			@org.unitime.timetable.gwt.client.widgets.LoadingWidget::showLoading(Ljava/lang/String;)(message);
		};
		$wnd.hideGwtLoading = function() {
			@org.unitime.timetable.gwt.client.widgets.LoadingWidget::hideLoading()();
		};
	}-*/;
	
	public static void showLoading(String message) {
		getInstance().show(message == null || message.isEmpty() ? null : message);
	}
	
	public static void hideLoading() {
		getInstance().hide();
	}
	
	public static <T extends GwtRpcResponse> void execute(GwtRpcRequest<T> request, final AsyncCallback<T> callback, final String loadingMessage) {
		showLoading(loadingMessage);
		GwtRpc.execute(request, new GwtRpc.CancellableCallback<T>() {

			@Override
			public void onFailure(Throwable caught) {
				hideLoading();
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(T result) {
				hideLoading();
				callback.onSuccess(result);
			}

			@Override
			public void onExecution(Long executionId) {
				getInstance().showCancel(executionId);
			}
		});
	}
}
