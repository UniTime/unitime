/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class CourseNumbersSuggestBox extends SuggestOracle {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private String iConfiguration = null;
	private RegExp iRegExp = RegExp.compile("\\$\\{([a-zA-Z_0-9]+)\\}");
	
	public CourseNumbersSuggestBox(String configuration) {
		iConfiguration = configuration;
	}
	
	public static AriaSuggestBox insert(RootPanel panel) {
		String config = panel.getElement().getAttribute("configuration");
		final String onchange = panel.getElement().getAttribute("onchange");
		AriaTextBox text = new AriaTextBox(panel.getElement().getFirstChildElement());
		text.getElement().setAttribute("autocomplete", "off");
		AriaSuggestBox box = new AriaSuggestBox(text, new CourseNumbersSuggestBox(config));
		if (onchange != null)
			box.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					ToolBox.eval(onchange);
				}
			});
		panel.add(box);
		return box;
	}
	
	private String getConfiguration() {
		String conf = iConfiguration;
		for (MatchResult matcher = iRegExp.exec(conf); matcher != null; matcher = iRegExp.exec(conf)) {
			Element element = DOM.getElementById(matcher.getGroup(1));
			String value = "";
			if ("select".equalsIgnoreCase(element.getTagName())) {
				ListBox list = ListBox.wrap(element);
				for (int i = 0; i < list.getItemCount(); i++) {
					if (list.isItemSelected(i))
						value += (value.isEmpty() ? "" : ",") + list.getValue(i);
				}
			} else if ("input".equalsIgnoreCase(element.getTagName())) {
				TextBox text = TextBox.wrap(element);
				value = text.getText();
			} else {
				Hidden hidden = Hidden.wrap(element);
				value = hidden.getValue();
			}
			conf = conf.replace("${" + matcher.getGroup(1) + "}", value);
		}
		return conf;
	}

	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		RPC.execute(new SuggestionRpcRequest(request, getConfiguration()), new AsyncCallback<GwtRpcResponseList<SuggestionInterface>>() {

			@Override
			public void onFailure(Throwable caught) {
				List<Suggestion> suggestions = new ArrayList<Suggestion>();
				suggestions.add(new SuggestionInterface("<font color='red'>"+caught.getMessage()+"</font>", ""));
				callback.onSuggestionsReady(request, new Response(suggestions));
			}
			
			@Override
			public void onSuccess(GwtRpcResponseList<SuggestionInterface> result) {
				callback.onSuggestionsReady(request, new Response(result));
			}
		});
	}
	
	public static class SuggestionInterface implements IsSerializable, Suggestion {
		private String iDisplayString;
		private String iReplacementString;
		
		public SuggestionInterface() {}
		public SuggestionInterface(String display) {
			iDisplayString = display; iReplacementString = display;
		}
		public SuggestionInterface(String display, String replace) {
			iDisplayString = display; iReplacementString = replace;
		}
		
		@Override
		public String getDisplayString() { return iDisplayString; }

		@Override
		public String getReplacementString() { return iReplacementString; }
	}
	
	public static class SuggestionRpcRequest extends Request implements GwtRpcRequest<GwtRpcResponseList<SuggestionInterface>> {
		private String iConfiguration = null;
		
		public SuggestionRpcRequest() {
			super();
		}
		public SuggestionRpcRequest(Request request, String configuration) { 
			super(request.getQuery(), request.getLimit());
			iConfiguration = configuration;
		}
		
		public String getConfiguration() { return iConfiguration; }
		public void setConfiguration(String configuration) { iConfiguration = configuration; }
		
		@Override
		public String toString() { return getConfiguration() + ";q=" + getQuery() + ";limit=" + getLimit(); }
	}

	public boolean isDisplayStringHTML() { return true; }			

}
