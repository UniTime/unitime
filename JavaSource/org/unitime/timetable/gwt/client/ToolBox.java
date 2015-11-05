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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class ToolBox {
	public static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	public static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	public native static void disableTextSelectInternal(Element e)/*-{
		e.ondrag = function () { return false; };
		e.onselectstart = function () { return false; };
		e.style.MozUserSelect="none"
	}-*/;

	public native static int getScrollBarWidth() /*-{
	
		var inner = document.createElement("p");
		inner.style.width = "100%";
		inner.style.height = "200px";
		
		var outer = document.createElement("div");
		outer.style.position = "absolute";
		outer.style.top = "0px";
		outer.style.left = "0px";
		outer.style.visibility = "hidden";
		outer.style.width = "200px";
		outer.style.height = "150px";
		outer.style.overflow = "hidden";
		outer.appendChild (inner);
		
		document.body.appendChild (outer);
		var w1 = inner.offsetWidth;
		outer.style.overflow = "scroll";
		var w2 = inner.offsetWidth;
		if (w1 == w2) w2 = outer.clientWidth;
		
		document.body.removeChild (outer);
		 
		return (w1 - w2);
	}-*/;

	public static native void printw(String html) /*-{
		var win = (html ? $wnd.open("about:blank", "__printingWindow") : $wnd);
		var doc = win.document;
		
		if (html) {
			doc.open(); 
			doc.write(html);
			doc.write("<script type=\"text/javascript\" language=\"javascript\">" + 
				"function invokePrint() { " +
				"if (document.readyState && document.readyState!='complete') " +
				"setTimeout(function() { invokePrint(); }, 50); " +
				"else if (document.body && document.body.innerHTML=='false') " +
				"setTimeout(function() { invokePrint(); }, 50); " +
				"else { focus(); print(); }}" + 
				"setTimeout(function() { invokePrint(); }, 500);" +
				"</script>");
			doc.close();
		}
		
		win.focus();
	}-*/;
	
	public static native void printf(String html) /*-{
		if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
			@org.unitime.timetable.gwt.client.ToolBox::printw(Ljava/lang/String;)(html);
			return;
		}
		
    	var frame = $doc.frames ? $doc.frames['__printingFrame'] : $doc.getElementById('__printingFrame');
        if (!frame) {
			@org.unitime.timetable.gwt.client.ToolBox::printw(Ljava/lang/String;)(html);
            return; 
        }
        
        var doc = null;
        if (frame.contentDocument)
            doc = frame.contentDocument;
        else if (frame.contentWindow)
            doc = frame.contentWindow.document;
        else if (frame.document)
            doc = frame.document;
        if (!doc)  {
			@org.unitime.timetable.gwt.client.ToolBox::printw(Ljava/lang/String;)(html);
            return; 
        }
        
        if (html) {
        	doc.open(); 
        	doc.write(html); 
        	doc.close();
        }
        
        if (doc.readyState && doc.readyState!='complete') {
        	setTimeout(function() {
        		@org.unitime.timetable.gwt.client.ToolBox::printf(Ljava/lang/String;)(null);
        	}, 50);
        } else if (doc.body && doc.body.innerHTML=='false') {
        	setTimeout(function() {
        		@org.unitime.timetable.gwt.client.ToolBox::printf(Ljava/lang/String;)(null);
        	}, 50);
        } else {
        	if (frame.contentWindow) frame = frame.contentWindow;
        	frame.focus();
        	frame.print();
        }
    }-*/;
        
    public static void print(String title, String user, String session, Widget... widgets) {
    	String content = "";
    	for (Widget w: widgets)
    		content += "<div class=\"unitime-PrintedComponent\">" + w.getElement().getString() + "</div>";
    	String html = "<html><header>" +
    		"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
    		"<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT.getHostPageBaseURL() + "unitime/gwt/standard/standard.css\">" +
    		"<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT.getHostPageBaseURL() + "styles/unitime.css\">" +
    	    "<link rel=\"shortcut icon\" href=\"" + GWT.getHostPageBaseURL() + "images/timetabling.ico\">" +
    	    "<title>UniTime " + CONSTANTS.version() + "| University Timetabling Application</title>" +
    		"</header><body class='unitime-Body'>" + 
    	    "<span class='unitime-Page'>" +
    			"<span class='body'>"+
    				"<span class='unitime-PageHeader'>" +
    					"<span class='row'>"+
    						"<span class='logo'><img src='" + GWT.getHostPageBaseURL() + "images/unitime.png' border='0'/></span>"+
    						"<span class='content'>"+
    							"<span id='UniTimeGWT:Title' class='title'>" + title + "</span>"+
    							"<span class='unitime-Header'>" +
    								"<span class='unitime-InfoPanel'>"+
    									"<span class='row'>" +
    										"<span class='cell middle'>" + user + "</span>" +
    										"<span class='cell right'>" + session + "</span>" +
    									"</span>" +
    								"</span>" +
    							"</span>" +
    						"</span>" +
    					"</span>" +
    				"</span>" +
    				"<span class='content'>" + content + "</span>" +
    			"</span>" +
    			"<span class='footer'>" +
    				"<span class='unitime-Footer'>" +
    					"<span class='row'>" +
    						"<span class='cell left'>Printed from UniTime " + CONSTANTS.version() + " | University Timetabling Application</span>" +
    						"<span class='cell middle'>" + CONSTANTS.copyright() + "</span>" +
    						"<span class='cell right'>" + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()) + "</span>" +
    					"</span>" +
    				"</span>" +
    			"</span>" +
    		"</span></body></html>";
    	printf(html);
    }
    
    public static void print(List<Page> pages) {
    	String html = "<html><header>" +
        		"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
        		"<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT.getHostPageBaseURL() + "unitime/gwt/standard/standard.css\">" +
        		"<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT.getHostPageBaseURL() + "styles/unitime.css\">" +
        	    "<link rel=\"shortcut icon\" href=\"" + GWT.getHostPageBaseURL() + "images/timetabling.ico\">" +
        	    "<title>UniTime " + CONSTANTS.version() + "| University Timetabling Application</title>" +
        		"</header><body class='unitime-Body'>";
    	for (Page p: pages) {
    		html += "<span class='unitime-PrintedPage'>" +
    					"<span class='unitime-Page'>" +
    						"<span class='body'>"+
    							"<span class='unitime-PageHeader'>" +
    								"<span class='row'>"+
    									"<span class='logo'><img src='" + GWT.getHostPageBaseURL() + "images/unitime.png' border='0'/></span>"+
    									"<span class='content'>"+
    										"<span id='UniTimeGWT:Title' class='title'>" + p.getName() + "</span>"+
    										"<span class='unitime-Header'>" +
    											"<span class='unitime-InfoPanel'>"+
    												"<span class='row'>" +
    													"<span class='cell middle'>" + p.getUser() + "</span>" +
    													"<span class='cell right'>" + p.getSession() + "</span>" +
    												"</span>" +
    											"</span>" +
    										"</span>" +
    									"</span>" +
    								"</span>" +
    							"</span>" +
    							"<span class='content'>" + p.getBody().getString() + "</span>" +
    						"</span>" +
    						"<span class='footer'>" +
    							"<span class='unitime-Footer'>" +
    								"<span class='row'>" +
    									"<span class='cell left'>Printed from UniTime " + CONSTANTS.version() + " | University Timetabling Application</span>" +
    									"<span class='cell middle'>" + CONSTANTS.copyright() + "</span>" +
    									"<span class='cell right'>" + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()) + "</span>" +
    								"</span>" +
    							"</span>" +
    						"</span>" +
    					"</span>" +
    				"</span>";
    	}
    	html += "</body></html>";
    	printf(html);
    }
    
    public static void print(Page... pages) {
    	List<Page> list = new ArrayList<Page>();
    	for (Page page: pages) list.add(page);
    	print(list);
    }
    
	public native static void open(String url) /*-{
		$wnd.location = url;
	}-*/;
	
	public native static String eval(String script) /*-{
		return eval(script);
	}-*/;
	
	public static void checkAccess(Throwable t) {
		if (t != null && t instanceof GwtRpcException && t.getCause() != null) t = t.getCause();
		if (t != null && t instanceof PageAccessException) {
			open(GWT.getHostPageBaseURL() + "login.do?menu=hide&m=" + URL.encodeQueryString(t.getMessage())+"&target=" + URL.encodeQueryString(Window.Location.getHref()));
			/*
			UniTimeFrameDialog.openDialog("UniTime " + CONSTANTS.version() + "| Log In", "login.do?menu=hide&m=" + URL.encodeQueryString(t.getMessage())
					+"&target=" + URL.encodeQueryString(Window.Location.getHref()), "700px", "420px");
			*/
		}
	}
	
	public native static void setWhiteSpace(Style style, String value) /*-{
		style.whiteSpace = value;
	}-*/;
	
	public native static void setMaxHeight(Style style, String value) /*-{
		style.maxHeight = value;
	}-*/;

	public native static void setMaxWidth(Style style, String value) /*-{
		style.maxWidth = value;
	}-*/;
	
	public native static void setMinWidth(Style style, String value) /*-{
		style.minWidth = value;
	}-*/;
	
	public native static String getMinWidth(Style style) /*-{
		return style.minWidth;
	}-*/;

	public native static int getClientWidth() /*-{
		var sideMenu = $doc.getElementById("unitime-SideMenu");
		if (!sideMenu) return $doc.body.clientWidth;
		var sideMenuSpans = sideMenu.getElementsByTagName("span");
    	if (sideMenuSpans.length > 0) {
    		return $doc.body.clientWidth - sideMenuSpans[0].clientWidth;
    	} else {
    		return $doc.body.clientWidth;
    	}
	}-*/;
	
	public native static void scrollToElement(Element element)/*-{
		element.scrollIntoView();
	}-*/;
	
	public static Throwable unwrap(Throwable e) {
		if (e == null) return null;
		if (e instanceof UmbrellaException) {
			UmbrellaException ue = (UmbrellaException) e;
			if (ue.getCauses().size() == 1) {
				return unwrap(ue.getCauses().iterator().next());
			}
		}
		return e;
	}
	
	public static interface Page {
		public String getName();
		public String getUser();
		public String getSession();
		public Element getBody();
	}
	
	public static String toString(Collection<String> items) {
		if (items == null || items.isEmpty()) return "";
		if (items.size() == 1) return items.iterator().next();
		if (items.size() == 2) {
			Iterator<String> i = items.iterator();
			return MESSAGES.itemSeparatorPair(i.next(), i.next());
		} else {
			Iterator<String> i = items.iterator();
			String list = i.next();
			while (i.hasNext()) {
				String item = i.next();
				if (i.hasNext())
					list = MESSAGES.itemSeparatorMiddle(list, item);
				else
					list = MESSAGES.itemSeparatorLast(list, item);
			}
			return list;
		}
	}
}
