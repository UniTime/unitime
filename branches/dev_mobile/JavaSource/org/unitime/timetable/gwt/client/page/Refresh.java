/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
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
