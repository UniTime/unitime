/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.shared;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class ToolBox {

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
			@org.unitime.timetable.gwt.shared.ToolBox::printw(Ljava/lang/String;)(html);
			return;
		}
		
    	var frame = $doc.frames ? $doc.frames['__printingFrame'] : $doc.getElementById('__printingFrame');
        if (!frame) {
			@org.unitime.timetable.gwt.shared.ToolBox::printw(Ljava/lang/String;)(html);
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
			@org.unitime.timetable.gwt.shared.ToolBox::printw(Ljava/lang/String;)(html);
            return; 
        }
        
        if (html) {
        	doc.open(); 
        	doc.write(html); 
        	doc.close();
        }
        
        if (doc.readyState && doc.readyState!='complete') {
        	setTimeout(function() {
        		@org.unitime.timetable.gwt.shared.ToolBox::printf(Ljava/lang/String;)(null);
        	}, 50);
        } else if (doc.body && doc.body.innerHTML=='false') {
        	setTimeout(function() {
        		@org.unitime.timetable.gwt.shared.ToolBox::printf(Ljava/lang/String;)(null);
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
    		"<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT.getHostPageBaseURL() + "unitime4/gwt/standard/standard.css\">" +
    		"<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT.getHostPageBaseURL() + "styles/unitime4.css\">" +
    	    "<link rel=\"shortcut icon\" href=\"" + GWT.getHostPageBaseURL() + "images/timetabling.ico\">" +
    	    "<title>UniTime 3.2 | University Timetabling Application</title>" +
    		"</header><body>" + 
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
    		"<td width=\"33%\" align=\"left\" nowrap=\"nowrap\">Printed from UniTime 3.2 | University Timetabling Application</td>" +
    		"<td width=\"34%\" align=\"center\">&copy; 2010 UniTime.org</td>" +
    		"<td width=\"33%\" align=\"right\">" + DateTimeFormat.getMediumDateTimeFormat().format(new Date()) + "</td>" +
    		"</tr></table></td></tr></table>" +
    		"</body></html>";
    	printf(html);
    }
    
	public native static void open(String url) /*-{
		document.location = url;
	}-*/;

}
