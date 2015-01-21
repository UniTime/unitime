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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class MenuInterface implements IsSerializable {
	private String iName = null;
	private String iTitle = null;
	private String iPage = null;
	private String iHash = null;
	private Map<String, List<String>> iParameters = null;
	private String iTarget = null;
	private boolean iGWT = false;
	private List<MenuInterface> iSubMenus = null;

	public MenuInterface() {}
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }
	
	public boolean hasPage() { return iPage != null && !iPage.isEmpty(); }
	public String getPage() { return iPage; }
	public void setPage(String page) { iPage = page; }
	
	public boolean hasTarget() { return iTarget != null && !iTarget.isEmpty(); }
	public String getTarget() { return iTarget; }
	public void setTarget(String target) { iTarget = target; }
	
	public void setGWT(boolean gwt) { iGWT = gwt; }
	public boolean isGWT() { return iGWT; }
	public boolean isSeparator() { return getName() == null; }
	
	public String getHash() { return iHash; }
	public void setHash(String hash) { iHash = hash; }
	public boolean hasHash() { return iHash != null && !iHash.isEmpty(); }
	
	public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
	public void addParameter(String name, String value) {
		if (iParameters == null) iParameters = new HashMap<String, List<String>>();
		List<String> values = iParameters.get(name);
		if (values == null) {
			values = new ArrayList<String>();
			iParameters.put(name, values);
		}
		values.add(value);
	}
	public String getParameters(ValueEncoder encoder) {
		String ret = "";
		if (iParameters != null)
			for (Map.Entry<String, List<String>> values: iParameters.entrySet()) {
				for (String value: values.getValue()) {
					if (!ret.isEmpty()) ret += "&";
					ret += values.getKey() + "=" + (encoder == null ? value : encoder.encode(value));
				}
			}
		return ret;
	}
	
	public boolean hasSubMenus() { return iSubMenus != null && !iSubMenus.isEmpty(); }
	public List<MenuInterface> getSubMenus() { return iSubMenus; }
	public void addSubMenu(MenuInterface menu) {
		if (iSubMenus == null) iSubMenus = new ArrayList<MenuInterface>();
		iSubMenus.add(menu);
	}
	
	public String getURL(ValueEncoder encoder) {
		if (isGWT())
			return "gwt.jsp?page=" + getPage() + (hasParameters() ? "&" + getParameters(encoder) : "") + (hasHash() ? "#" + getHash() : "");
		else
			return getPage() + (hasParameters() ? "?" + getParameters(encoder) : "") + (hasHash() ? "#" + getHash() : "");
	}
	
	public static interface ValueEncoder {
		public String encode(String value);
	}
	
	public static class MenuRpcRequest implements GwtRpcRequest<GwtRpcResponseList<MenuInterface>> {
		@Override
		public String toString() { return null; }
	}
	
	public static class InfoPairInterface implements IsSerializable {
		private String iName, iValue;
		private boolean iSeparator = false;
		
		public InfoPairInterface() {}
		
		public InfoPairInterface(String name, String value) {
			iName = name;
			iValue = value;
		}
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		public void setValue(String value) { iValue = value; }
		public String getValue() { return iValue; }
		
		public boolean hasSeparator() { return iSeparator; }
		public void setSeparator(boolean separator) { iSeparator = separator; }
		
		@Override
		public String toString() { return iName + ": " + iValue; }
	}
	
	public static class InfoInterface implements GwtRpcResponse {
		private List<InfoPairInterface> iPairs = new ArrayList<MenuInterface.InfoPairInterface>();
		
		public InfoInterface() {}
		
		public InfoPairInterface addPair(String name, String value) {
			InfoPairInterface pair = new InfoPairInterface(name, value); 
			iPairs.add(pair);
			return pair;
		}
		public List<InfoPairInterface> getPairs() { return iPairs; }
		
		public boolean isEmpty() { return iPairs.isEmpty(); }
		
		@Override
		public String toString() { return iPairs.toString(); }
	}
	
	public static class UserInfoInterface extends InfoInterface {
		private boolean iChameleon = false;
		private String iName, iRole;
		
		public void setChameleon(boolean chameleon) { iChameleon = chameleon; }
		public boolean isChameleon() { return iChameleon; }
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		public void setRole(String role) { iRole = role; }
		public String getRole() { return iRole; }
	}
	
	public static class UserInfoRpcRequest implements GwtRpcRequest<UserInfoInterface> {
		@Override
		public String toString() { return null; }
	}
	
	public static class VersionInfoInterface implements GwtRpcResponse {
		private String iVersion;
		private String iBuildNumber;
		private String iReleaseDate;
		
		public VersionInfoInterface() {}
		
		public String getVersion() { return iVersion; }
		public void setVersion(String version) { iVersion = version; }

		public String getBuildNumber() { return iBuildNumber; }
		public void setBuildNumber(String buildNumber) { iBuildNumber = buildNumber; }

		public String getReleaseDate() { return iReleaseDate; }
		public void setReleaseDate(String releaseDate) { iReleaseDate = releaseDate; }

		@Override
		public String toString() { return iVersion; }
	}
	
	public static class VersionInfoRpcRequest implements GwtRpcRequest<VersionInfoInterface> {
		@Override
		public String toString() { return null; }
	}
	
	public static class SessionInfoInterface extends InfoInterface {
		private String iSession = null;
		
		public String getSession() { return iSession; }
		public void setSession(String session) { iSession = session; }
	}
	
	public static class SessionInfoRpcRequest implements GwtRpcRequest<SessionInfoInterface> {
		@Override
		public String toString() { return null; }
	}
	
	public static class SolverInfoInterface extends InfoInterface {
		private String iSolver, iType, iUrl;
		
		public String getSolver() { return iSolver; }
		public void setSolver(String solver) { iSolver = solver; }
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		public String getUrl() { return iUrl; }
		public void setUrl(String url) { iUrl = url; }
	}
		
	public static class SolverInfoRpcRequest implements GwtRpcRequest<SolverInfoInterface> {
		private boolean iIncludeSolutionInfo = false;

		public SolverInfoRpcRequest() {}
		public SolverInfoRpcRequest(boolean includeSolutionInfo) {
			iIncludeSolutionInfo = includeSolutionInfo;
		}
		
		public boolean isIncludeSolutionInfo() { return iIncludeSolutionInfo; }
		public void setIncludeSolutionInfo(boolean incldueSolutionInfo) { iIncludeSolutionInfo = incldueSolutionInfo; }
	}
	
	public static class PageNameInterface implements GwtRpcResponse {
		private String iHelpUrl, iName;
		
		public PageNameInterface() {}
		public PageNameInterface(String name, String helpUrl) {
			iName = name; iHelpUrl = helpUrl;
		}
		public PageNameInterface(String name) {
			iName = name; iHelpUrl = null;
		}
		
		public String getHelpUrl() { return iHelpUrl; }
		public void setHelpUrl(String url) { iHelpUrl = url; }
		public boolean hasHelpUrl() { return iHelpUrl != null && !iHelpUrl.isEmpty(); }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		
		@Override
		public String toString() { return iName; }
	}
	
	public static class PageNameRpcRequest implements GwtRpcRequest<PageNameInterface> {
		private String iName;
		
		public PageNameRpcRequest() {}
		public PageNameRpcRequest(String name) { iName = name; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		@Override
		public String toString() { return iName; }
	}
}
