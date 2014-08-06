/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public class BackTracker {
	public static int MAX_BACK_STEPS = 10;
	public static String BACK_LIST = "BackTracker.back";
	
    protected static MessageResources getResources(HttpServletRequest request) {
    	return ((MessageResources) request.getAttribute(Globals.MESSAGES_KEY));
    }	
	
	public static Vector getBackList(HttpSession session) {
		synchronized (session) {
			Vector back = (Vector)session.getAttribute(BACK_LIST);
			if (back==null) {
				back = new Vector();
				session.setAttribute("BackTracker.back", back);
			}
			return back;
		}
	}
	
	public static void markForBack(HttpServletRequest request, String uri, String title, boolean back, boolean clear) {
		synchronized (request.getSession()) {
			Vector backList = getBackList(request.getSession());
			if (clear) backList.clear();
			if (back) {
				if (uri==null && request.getAttribute("javax.servlet.forward.request_uri")==null) return;
				Object titleObj = (title==null?request.getAttribute("title"):title);
				String requestURI = (String)request.getAttribute("javax.servlet.forward.request_uri");
				String queryString = (String)request.getAttribute("javax.servlet.forward.query_string");
				if (queryString!=null && queryString.length()>0)
					requestURI += "?"+queryString;
				if (uri!=null)
					requestURI = uri;
				if (!backList.isEmpty()) {
					int found = -1;
					for (int idx = 0; idx<backList.size(); idx++) {
						String[] lastBack = (String[])backList.elementAt(idx);
						if (lastBack[0].equals(requestURI)) {
							found = idx; break;
						}
					}
					while (found>=0 && backList.size()>found)
						backList.removeElementAt(backList.size()-1);
				}
				backList.addElement(new String[]{requestURI,(titleObj==null?null:titleObj.toString())});
				//System.out.println("ADD BACK:"+requestURI+" ("+titleObj+")");
			}
		}
	}
	
	public static void markForBack(SessionContext context, String uri, String title, boolean back, boolean clear) {
		Vector backList = (Vector)context.getAttribute(BACK_LIST);
		if (backList==null) {
			backList = new Vector();
			context.setAttribute("BackTracker.back", backList);
		}
		if (clear) backList.clear();
		if (back) {
			if (!backList.isEmpty()) {
				int found = -1;
				for (int idx = 0; idx<backList.size(); idx++) {
					String[] lastBack = (String[])backList.elementAt(idx);
					if (lastBack[0].equals(uri)) {
						found = idx; break;
					}
				}
				while (found>=0 && backList.size()>found)
					backList.removeElementAt(backList.size()-1);
			}
			backList.addElement(new String[]{uri,title});
		}
	}
	
	private static String mark = "-_.!~*'()\"";
	public static String encodeURL(String url) {
        StringBuffer encodedUrl = new StringBuffer(); // Encoded URL
        int len = url.length();
        // Encode each URL character
        for(int i = 0; i < len; i++) {
            char c = url.charAt(i); // Get next character
            if ((c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z'))
                // Alphanumeric characters require no encoding, append as is
                encodedUrl.append(c);
            else {
                int imark = mark.indexOf(c);
                if (imark >=0) {
                    // Unreserved punctuation marks and symbols require
                    //  no encoding, append as is
                    encodedUrl.append(c);
                } else {
                    // Encode all other characters to Hex, using the format "%XX",
                    //  where XX are the hex digits
                    encodedUrl.append('%'); // Add % character
                    // Encode the character's high-order nibble to Hex
                    encodedUrl.append(Integer.toHexString(c));
                }
            }
        }
        return encodedUrl.toString(); // Return encoded URL
	}
	
	public static String getBackButton(HttpServletRequest request, int nrBackSteps, String name, String title, String accessKey, String style, String clazz, String backType, String backId) {
		MessageResources rsc = getResources(request);
		if (rsc!=null && rsc.getMessage(name)!=null) {
			name = rsc.getMessage(name);
		}
		synchronized (request.getSession()) {
			Vector backList = getBackList(request.getSession());
			if (backList.size()<nrBackSteps) return "";
			String[] backItem = (String[])backList.elementAt(backList.size()-nrBackSteps);
			if (backItem[1]!=null)
				title = title.replaceAll("%%", backItem[1]);
			String backUrl = backItem[0];
			if (backId!=null && backType!=null) {
				if (backUrl.indexOf('?')>0)
					backUrl += "&backType="+backType+"&backId="+backId+"#back";
				else
					backUrl += "?backType="+backType+"&backId="+backId+"#back";
			}
			return "<input type='button'"+
				" value='"+name+"' "+
				(accessKey==null?"":" accesskey=\""+accessKey+"\"")+
				(style==null?"":" style=\""+style+"\"")+
				(clazz==null?"":" class=\""+clazz+"\"")+
				" title=\""+title+"\""+
				" onClick=\"document.location='back.do?uri="+encodeURL(backUrl)+"'"+
				
				";\""+
				"/>";
		}
	}
	
	public static String getGwtBack(HttpServletRequest request, int nrBackSteps) {
		synchronized (request.getSession()) {
			Vector back = getBackList(request.getSession());
			if (back.size()<=1) return "";
			StringBuffer ret = new StringBuffer("");
			for (int i=Math.max(0,back.size()-MAX_BACK_STEPS); i<back.size(); i++) {
				String[] backItem = (String[])back.elementAt(i);
				if (ret.length() > 0) ret.append("&");
				ret.append(encodeURL(backItem[0])+"|"+backItem[1]);
			}
			return "<span id='UniTimeGWT:Back' style='display:none;'>"+ret.toString()+"</span>";
		}
	}
	
	public static boolean hasBack(HttpServletRequest request, int nrBackSteps) {
		synchronized (request.getSession()) {
			Vector backList = getBackList(request.getSession());
			if (backList.size()<nrBackSteps) return false;
			return true;
		}
	}
	
	public static boolean doBack(HttpServletRequest request, HttpServletResponse response) throws IOException {
		synchronized (request.getSession()) {
			String uri = request.getParameter("uri");
			Vector back = getBackList(request.getSession());
			if (back.isEmpty()) return false;
			if (uri==null) {
				uri = ((String[])back.lastElement())[0];
				back.remove(back.size()-1);
			} else {
				String uriNoBack = uri;
				if (uriNoBack.indexOf("backType=")>=0)
					uriNoBack = uriNoBack.substring(0, uriNoBack.indexOf("backType=")-1);
				while (!back.isEmpty() && !uriNoBack.equals(((String[])back.lastElement())[0]))
					back.remove(back.size()-1);
				if (!back.isEmpty())
					back.remove(back.size()-1);
			}
			if (uri.indexOf("backType=")<0 && request.getAttribute("backType")!=null && request.getAttribute("backId")!=null) {
				if (uri.indexOf('?')>0)
					uri += "&backType="+request.getAttribute("backType")+"&backId="+request.getAttribute("backId")+"#back";
				else
					uri += "?backType="+request.getAttribute("backType")+"&backId="+request.getAttribute("backId")+"#back";
				
			}
			response.sendRedirect(response.encodeURL(uri));
			return true;
		}
	}
	
	public static String getBackTree(HttpServletRequest request) {
		synchronized (request.getSession()) {
			Vector back = getBackList(request.getSession());
			if (back.size()<=1) return "";
			StringBuffer ret = new StringBuffer();
			for (int i=Math.max(0,back.size()-MAX_BACK_STEPS); i<back.size(); i++) {
				String[] backItem = (String[])back.elementAt(i);
				//if (!e.hasMoreElements()) continue;
				if (ret.length()>0) ret.append(" &rarr; ");
				ret.append("<A href='back.do?uri="+encodeURL(backItem[0])+"'>"+backItem[1]+"</A>");
			}
			return "&nbsp;"+ret.toString();
		}
	}

}
