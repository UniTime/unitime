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
package org.unitime.timetable.gwt.client.solver;

import java.util.Date;

import org.unitime.timetable.gwt.shared.SolverInterface.ProgressLogLevel;

import com.google.gwt.user.client.Cookies;

/**
 * @author Tomas Muller
 */
public class SolverCookie {
	private static SolverCookie sInstance = null;
	private int iLogLevel = ProgressLogLevel.INFO.ordinal();
	
	private SolverCookie() {
		try {
			String cookie = Cookies.getCookie("UniTime:Solver");
			if (cookie != null) {
				String[] params = cookie.split("\\|");
				int idx = 0;
				iLogLevel = Integer.valueOf(params[idx++]);
			}
		} catch (Exception e) {
		}
	}
	
	private void save() {
		String cookie = iLogLevel + "";
		Date expires = new Date(new Date().getTime() + 604800000l); // expires in 7 days
		Cookies.setCookie("UniTime:Solver", cookie, expires);
	}
	
	public static SolverCookie getInstance() { 
		if (sInstance == null)
			sInstance = new SolverCookie();
		return sInstance;
	}
	
	public int getLogLevel() { return iLogLevel; }
	public void setLogLevel(int level) {
		iLogLevel = level; save();
	}

}
