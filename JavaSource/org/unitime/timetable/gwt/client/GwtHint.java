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
package org.unitime.timetable.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class GwtHint extends PopupPanel {
	private static GwtHint sInstance;

	public GwtHint(String html) {
		super();
		setStyleName("unitime-PopupHint");
	}
	
	public static GwtHint getInstance() {
		if (sInstance == null) {
			sInstance = new GwtHint("");
			Client.addGwtPageChangedHandler(new Client.GwtPageChangedHandler() {
				@Override
				public void onChange(Client.GwtPageChangeEvent event) {
					hideHint();
				}
			});
		}
		return sInstance;
	}
	
	public static native void createTriggers()/*-{
		$wnd.showGwtHint = function(source, content) {
			@org.unitime.timetable.gwt.client.GwtHint::_showHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(source, content);
		};
		$wnd.hideGwtHint = function() {
			@org.unitime.timetable.gwt.client.GwtHint::hideHint()();
		};
	}-*/;
	
	/** Never use from GWT code */
	public static void _showHint(JavaScriptObject source, String content) {
		showHint((Element) source.cast(), content);
	}
	
	public static void showHint(final Element relativeObject, String content) {
		showHint(relativeObject, new HTML(content, false));
	}
	
	public static void showHint(final Element relativeObject, Widget content) {
		showHint(relativeObject, content, true);
	}
	
	public static void showHint(final Element relativeObject, Widget content, final boolean showRelativeToTheObject) {
		getInstance().setWidget(content);
		getInstance().setPopupPositionAndShow(new PositionCallback() {
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
				if (relativeObject != null && showRelativeToTheObject) {
				    int textBoxOffsetWidth = relativeObject.getOffsetWidth();
				    int offsetWidthDiff = offsetWidth - textBoxOffsetWidth;
				    int left;
				    if (LocaleInfo.getCurrentLocale().isRTL()) {
					      int textBoxAbsoluteLeft = relativeObject.getAbsoluteLeft();
					      left = textBoxAbsoluteLeft - offsetWidthDiff;
					      if (offsetWidthDiff > 0) {
						        int windowRight = Window.getClientWidth() + Window.getScrollLeft();
						        int windowLeft = Window.getScrollLeft();
						        int textBoxLeftValForRightEdge = textBoxAbsoluteLeft + textBoxOffsetWidth;
						        int distanceToWindowRight = windowRight - textBoxLeftValForRightEdge;
						        int distanceFromWindowLeft = textBoxLeftValForRightEdge - windowLeft;
						        if (distanceFromWindowLeft < offsetWidth && distanceToWindowRight >= offsetWidthDiff) {
							          left = textBoxAbsoluteLeft;
						        }
					      }
				    } else {
					      left = relativeObject.getAbsoluteLeft();
					      if (offsetWidthDiff > 0) {
						        int windowRight = Window.getClientWidth() + Window.getScrollLeft();
						        int windowLeft = Window.getScrollLeft();
						        int distanceToWindowRight = windowRight - left;
						        int distanceFromWindowLeft = left - windowLeft;
						        if (distanceToWindowRight < offsetWidth && distanceFromWindowLeft >= offsetWidthDiff) {
							          left -= offsetWidthDiff;
						        }
					      }
				    }
				    int top = relativeObject.getAbsoluteTop();
				    int windowTop = Window.getScrollTop();
				    int windowBottom = Window.getScrollTop() + Window.getClientHeight();
				    int distanceFromWindowTop = top - windowTop;
				    int distanceToWindowBottom = windowBottom - (top + relativeObject.getOffsetHeight());
				    if (distanceToWindowBottom < offsetHeight && distanceFromWindowTop >= offsetHeight) {
					      top -= offsetHeight;
				    } else {
					      top += relativeObject.getOffsetHeight();
				    }
				    getInstance().setPopupPosition(left, top);					
				} else {
					int left = Window.getScrollLeft() + 10;
					int top = Window.getScrollTop() + Window.getClientHeight() - offsetHeight - 10;
					if (relativeObject != null && left + offsetWidth >= relativeObject.getAbsoluteLeft() && top <= relativeObject.getAbsoluteBottom())
						left = Window.getScrollLeft() + Window.getClientWidth() - offsetWidth - 10;
					getInstance().setPopupPosition(left, top);
				}

			}
		});
	}
	
	public static void hideHint() {
		getInstance().hide();
	}
}
