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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class UniTimeBack {
	
	private String iBackUrl = null;
	private List<String[]> iBacks = new ArrayList<String[]>();
	
	public UniTimeBack() {
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() == null || event.getValue().isEmpty() || "back".equals(event.getValue())) {
					open(GWT.getHostPageBaseURL() + "back.do?uri=" + iBackUrl);
				} else {
					String uri = token2uri(event.getValue().replace("%20", " "));
					if (uri != null)
						open(GWT.getHostPageBaseURL() + "back.do?uri=" + uri);
				}
			}
		});
	}
	
	private native void open(String url) /*-{
		$wnd.location = url;
	}-*/;
	
	private String token2uri(String token) {
		for (String[] back: iBacks) {
			if (back[1].equals(token)) return back[0];
		}
		return null;
	}

	
	public void insert(final RootPanel panel) {
		String backs = panel.getElement().getInnerText();
		iBacks.clear();
		for (String back: backs.split("\\&")) {
			String[] b = back.split("\\|");
			iBacks.add(b);
			History.newItem(b[1], false);
		}
		if (iBacks.isEmpty()) return;
		int back = 2;
		String lastUrl = iBacks.get(iBacks.size() - 1)[0];
		if (lastUrl.indexOf('%') >= 0) lastUrl = lastUrl.substring(0, lastUrl.indexOf('%'));
		String currentUrl = Window.Location.getHref();
		if (currentUrl.indexOf('/') >= 0) currentUrl = currentUrl.substring(currentUrl.lastIndexOf('/') + 1);
		if (currentUrl.indexOf('?') >= 0) currentUrl = currentUrl.substring(0, currentUrl.lastIndexOf('?'));
		if (currentUrl.indexOf('#') >= 0) currentUrl = currentUrl.substring(0, currentUrl.lastIndexOf('#'));
		if (!lastUrl.equals(currentUrl) || iBacks.size() < 2) {
			back = 1;
			History.newItem("back", false);
		}
		iBackUrl = iBacks.get(iBacks.size() - back)[0];
	}

}
