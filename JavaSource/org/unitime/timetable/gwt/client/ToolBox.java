/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
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
package org.unitime.timetable.gwt.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.PageAccessException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class ToolBox {
	public static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	
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
    		content += "<div class=\"unitime-PrintedComponent\">" + DOM.toString(w.getElement()) + "</div>";
    	String html = "<html><header>" +
    		"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
    		"<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT.getHostPageBaseURL() + "unitime/gwt/standard/standard.css\">" +
    		"<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT.getHostPageBaseURL() + "styles/unitime.css\">" +
    	    "<link rel=\"shortcut icon\" href=\"" + GWT.getHostPageBaseURL() + "images/timetabling.ico\">" +
    	    "<title>UniTime " + CONSTANTS.version() + "| University Timetabling Application</title>" +
    		"</header><body class='unitime-Body'>" + 
    		"<table align=\"center\"><tr><td>" +
    		"<table class=\"unitime-Page\"><tr><td>" +
    		"<table id=\"header\" class=\"unitime-MainTable\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">" +
    		"<tr><td rowspan=\"2\"><img src=\"" + GWT.getHostPageBaseURL() + "images/unitime.png\" border=\"0\"/></td>" +
    		"<td nowrap=\"nowrap\" class=\"unitime-Title\" width=\"100%\" align=\"center\" colspan=\"2\">" + title + "</td></tr>" +
    		"<tr><td nowrap=\"nowrap\" class=\"unitime-SubTitle\" width=\"50%\" align=\"center\">" + user + "</td>"+
    		"<td nowrap=\"nowrap\" class=\"unitime-SubTitle\" width=\"50%\" align=\"center\">" + session + "</td></tr></table>" +
    		content + 
    		"</td></tr></table>" +
    		"</td></tr><tr><td>" +
    		"<table class=\"unitime-Footer\"><tr>" +
    		"<td width=\"33%\" align=\"left\" nowrap=\"nowrap\">Printed from UniTime " + CONSTANTS.version() + " | University Timetabling Application</td>" +
    		"<td width=\"34%\" align=\"center\">" + CONSTANTS.copyright() + "</td>" +
    		"<td width=\"33%\" align=\"right\">" + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()) + "</td>" +
    		"</tr></table></td></tr></table>" +
    		"</body></html>";
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
    		html += "<table class=\"unitime-PrintedPage\" align=\"center\"><tr><td>" +
    				"<table class=\"unitime-Page\"><tr><td>" +
    	    		"<table id=\"header\" class=\"unitime-MainTable\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">" +
    	    		"<tr><td rowspan=\"2\"><img src=\"" + GWT.getHostPageBaseURL() + "images/unitime.png\" border=\"0\"/></td>" +
    	    		"<td nowrap=\"nowrap\" class=\"unitime-Title\" width=\"100%\" align=\"center\" colspan=\"2\">" + p.getName() + "</td></tr>" +
    	    		"<tr><td nowrap=\"nowrap\" class=\"unitime-SubTitle\" width=\"50%\" align=\"center\">" + p.getUser() + "</td>"+
    	    		"<td nowrap=\"nowrap\" class=\"unitime-SubTitle\" width=\"50%\" align=\"center\">" + p.getSession() + "</td></tr></table>" +
    	    		DOM.toString(p.getBody()) +
    	    		"</td></tr></table>" +
    	    		"</td></tr><tr><td>" +
    	    		"<table class=\"unitime-Footer\"><tr>" +
    	    		"<td width=\"33%\" align=\"left\" nowrap=\"nowrap\">Printed from UniTime " + CONSTANTS.version() + " | University Timetabling Application</td>" +
    	    		"<td width=\"34%\" align=\"center\">" + CONSTANTS.copyright() + "</td>" +
    	    		"<td width=\"33%\" align=\"right\">" + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(new Date()) + "</td>" +
    	    		"</tr></table></td></tr></table>";
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
			UniTimeFrameDialog.openDialog("UniTime " + CONSTANTS.version() + "| Log In", "login.jsp?menu=hide&m=" + URL.encodeQueryString(t.getMessage()), "700px", "420px");
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
		var sideMenu = $doc.getElementById("unitime-SideMenu").getElementsByTagName("span");
    	if (sideMenu.length > 0) {
    		return $doc.body.clientWidth - sideMenu[0].clientWidth;
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
}
