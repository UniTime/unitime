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
package org.unitime.timetable.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Tomas Muller
 */
public class GwtHint extends PopupPanel {
	private HTML iContent;
	private static GwtHint sInstance;

	public GwtHint(String html) {
		super();
		setStyleName("unitime-PopupHint");
		iContent = new HTML(html, false);
		setWidget(iContent);
	}
	
	public void setContent(String html) {
		iContent.setHTML(html);
	}
	
	public static GwtHint getInstance() {
		if (sInstance == null) {
			sInstance = new GwtHint("");
		}
		return sInstance;
	}
	
	public static native void createTriggers()/*-{
		$wnd.showGwtHint = function(source, content) {
			@org.unitime.timetable.gwt.client.GwtHint::showHint(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(source, content);
		};
		$wnd.hideGwtHint = function() {
			@org.unitime.timetable.gwt.client.GwtHint::hideHint()();
		};
	}-*/;
	
	public static void showHint(JavaScriptObject source, String content) {
		showHint(source.cast(), content);
	}
	
	public static void showHint(final Element relativeObject, String content) {
		getInstance().setContent(content);
		getInstance().setPopupPositionAndShow(new PositionCallback() {
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
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
			}
		});
	}
	
	public static void hideHint() {
		getInstance().hide();
	}
}
