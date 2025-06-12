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
package org.unitime.timetable.webutil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public class BackTracker {
	public static int MAX_BACK_STEPS = 10;
	public static String BACK_LIST = SessionAttribute.Back.key();
	
	public static List<BackItem> getBackList(HttpSession session) {
		synchronized (session) {
			List<BackItem> back = (List<BackItem>)session.getAttribute(SessionAttribute.Back.key());
			if (back==null) {
				back = Collections.synchronizedList(new ArrayList<BackItem>());
				session.setAttribute(SessionAttribute.Back.key(), back);
			}
			return back;
		}
	}
	
	public static List<BackItem> getBackList(SessionContext context) {
		synchronized (context) {
			List<BackItem> back = (List<BackItem>)context.getAttribute(SessionAttribute.Back);
			if (back==null) {
				back = Collections.synchronizedList(new ArrayList<BackItem>());
				context.setAttribute(SessionAttribute.Back.key(), back);
			}
			return back;
		}
	}
	
	public static void markForBack(HttpServletRequest request, String uri, String title, boolean back, boolean clear) {
		synchronized (request.getSession()) {
			List<BackItem> backList = getBackList(request.getSession());
			if (clear) backList.clear();
			if (back) {
				if (uri==null && request.getAttribute("jakarta.servlet.forward.request_uri")==null) return;
				Object titleObj = (title==null?request.getAttribute("title"):title);
				String requestURI = (String)request.getAttribute("jakarta.servlet.forward.request_uri");
				String queryString = (String)request.getAttribute("jakarta.servlet.forward.query_string");
				if (queryString!=null && queryString.length()>0)
					requestURI += "?"+queryString;
				if (uri!=null)
					requestURI = uri;
				if (!backList.isEmpty()) {
					int found = -1;
					for (int idx = 0; idx<backList.size(); idx++) {
						BackItem lastBack = backList.get(idx);
						if (lastBack.getUrl().equals(requestURI)) {
							found = idx; break;
						}
					}
					while (found>=0 && backList.size()>found)
						backList.remove(backList.size()-1);
				}
				backList.add(new BackItem(requestURI, (titleObj==null?null:titleObj.toString())));
			}
		}
	}
	
	public static void markForBack(SessionContext context, String uri, String title, boolean back, boolean clear) {
		synchronized (context) {
			List<BackItem> backList = getBackList(context);
			if (clear) backList.clear();
			if (back) {
				if (!backList.isEmpty()) {
					int found = -1;
					for (int idx = 0; idx<backList.size(); idx++) {
						BackItem lastBack = backList.get(idx);
						if (lastBack.getUrl().equals(uri)) {
							found = idx; break;
						}
					}
					while (found>=0 && backList.size()>found)
						backList.remove(backList.size()-1);
				}
				backList.add(new BackItem(uri, title));
			}
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
		synchronized (request.getSession()) {
			List<BackItem> backList = getBackList(request.getSession());
			if (backList.size()<nrBackSteps) return "";
			BackItem backItem = (BackItem)backList.get(backList.size()-nrBackSteps);
			if (backItem.getTitle() != null)
				title = title.replaceAll("%%", backItem.getTitle());
			String backUrl = backItem.getUrl();
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
				" onClick=\"document.location='back.action?uri="+encodeURL(backUrl)+"'"+
				
				";\""+
				"/>";
		}
	}
	
	public static String getGwtBack(HttpServletRequest request, int nrBackSteps) {
		synchronized (request.getSession()) {
			List<BackItem> back = getBackList(request.getSession());
			if (back.size()<=1) return "";
			StringBuffer ret = new StringBuffer("");
			for (int i=Math.max(0,back.size()-MAX_BACK_STEPS); i<back.size(); i++) {
				BackItem backItem = (BackItem)back.get(i);
				if (ret.length() > 0) ret.append("&");
				ret.append(encodeURL(backItem.getUrl())+"|"+backItem.getTitle());
			}
			return "<span id='UniTimeGWT:Back' style='display:none;'>"+ret.toString()+"</span>";
		}
	}
	
	public static BackItem getBackItem(SessionContext context, int nrBackSteps) {
		synchronized (context) {
			List<BackItem> back = getBackList(context);
			if (back.isEmpty()) return null;
			return back.get(Math.max(0, back.size() - nrBackSteps));
		}
	}
	
	public static boolean hasBack(HttpServletRequest request, int nrBackSteps) {
		synchronized (request.getSession()) {
			List<BackItem> backList = getBackList(request.getSession());
			if (backList.size()<nrBackSteps) return false;
			return true;
		}
	}

	public static boolean hasBack(SessionContext context, int nrBackSteps) {
		synchronized (context) {
			List<BackItem> backList = getBackList(context);
			if (backList.size()<nrBackSteps) return false;
			return true;
		}
	}

	public static boolean doBack(HttpServletRequest request, HttpServletResponse response) throws IOException {
		synchronized (request.getSession()) {
			String uri = request.getParameter("uri");
			List<BackItem> back = getBackList(request.getSession());
			if (back.isEmpty()) {
				if (uri != null) {
					response.sendRedirect(response.encodeURL(uri));
					return true;
				}
				return false;
			}
			if (uri==null) {
				uri = back.get(back.size() - 1).getUrl();
				back.remove(back.size() - 1);
			} else {
				String uriNoBack = uri;
				if (uriNoBack.indexOf("backType=")>=0)
					uriNoBack = uriNoBack.substring(0, uriNoBack.indexOf("backType=")-1);
				while (!back.isEmpty() && !uriNoBack.equals(back.get(back.size() - 1).getUrl()))
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
			List<BackItem> back = getBackList(request.getSession());
			if (back.size()<=1) return "";
			StringBuffer ret = new StringBuffer();
			for (int i=Math.max(0,back.size()-MAX_BACK_STEPS); i<back.size(); i++) {
				BackItem backItem = back.get(i);
				if (ret.length()>0) ret.append(" &rarr; ");
				ret.append("<span class='item'><A href='back.action?uri="+encodeURL(backItem.getUrl())+"'>"+backItem.getTitle()+"</A></span>");
			}
			return "&nbsp;"+ret.toString();
		}
	}

	public static class BackItem implements Serializable {
		private static final long serialVersionUID = -1723774866087870562L;
		String iUrl; String iTitle;
		public BackItem() {}
		public BackItem(String url, String title) {
			iUrl = url; iTitle = title;
		}
		public String getUrl() { return iUrl; }
		public void setUrl(String url) { iUrl = url; }
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		
		@Override
		public String toString() { return getTitle() + " (" + getUrl() + ")"; }
		@Override
		public int hashCode() { return getUrl().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof BackItem)) return false;
			return getUrl().equals(((BackItem)o).getUrl());
		}
	}
}
