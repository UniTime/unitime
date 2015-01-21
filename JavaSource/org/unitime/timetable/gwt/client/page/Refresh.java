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
package org.unitime.timetable.gwt.client.page;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * @author Tomas Muller
 */
public class Refresh {
	
	public static native void createTriggers()/*-{
	$wnd.refreshPage = function(message) {
		@org.unitime.timetable.gwt.client.page.Refresh::refreshPage()();
	};
	@org.unitime.timetable.gwt.client.page.Refresh::scrollDown()();
	}-*/;
	
	public static void refreshPage() {
		String url = Window.Location.getHref();
		if (url.indexOf('#') >= 0)
			url = url.substring(0, url.lastIndexOf('#'));
		url += "#" + Window.getScrollLeft() + ":" + Window.getScrollTop();
		Window.Location.assign(url);
		new Timer() {
			@Override
			public void run() {
				Window.Location.reload();
			}
		}.schedule(100);
	}
		
	public static void scrollDown() {
		String hash = Window.Location.getHash();
		if (hash != null && hash.matches("#[0-9]+:[0-9]+")) {
			String[] scroll = hash.substring(1).split(":");
			Window.scrollTo(Integer.parseInt(scroll[0]), Integer.parseInt(scroll[1]));
		}
	}

}
