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
package org.unitime.timetable.gwt.client.solver.suggestions;

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.SimpleForm.HasMobileScroll;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.CBSNode;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * @author Tomas Muller
 */
public class ConflictBasedStatisticsTree extends Tree implements TakesValue<List<CBSNode>>, HasMobileScroll {
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static final GwtMessages MESSAGES =  GWT.create(GwtMessages.class);
	private List<CBSNode> iValue = null;
	private SuggestionProperties iProperties;
	
	public ConflictBasedStatisticsTree(SuggestionProperties properties) {
		super(RESOURCES, true);
		iProperties = properties;
		addStyleName("unitime-ConflictBasedStatistics");
	}
	
	
	protected void onClick(ClickEvent event, CBSNode node) {
		if (node.hasLink()) {
			ToolBox.open(GWT.getHostPageBaseURL() + node.getLink());
		} else if (node.hasClassId()) {
			UniTimeFrameDialog.openDialog(MESSAGES.pageSuggestions(), "gwt.jsp?page=suggestions&menu=hide&id=" + node.getClassId(), "900", "85%");
		} else if (node.hasSelection()) {
			SelectedAssignment selection = node.getSelection();
			UniTimeFrameDialog.openDialog(MESSAGES.pageSuggestions(), "gwt.jsp?page=suggestions&menu=hide&id=" + selection.getClassId()
				+ "&days=" + selection.getDays() + "&slot=" + selection.getStartSlot() + "&room=" + selection.getRoomIds(",")
				+ "&pid=" + selection.getPatternId() + "&did=" + selection.getDatePatternId(), "900", "85%");
		}
	}

	@Override
	public void setValue(List<CBSNode> value) {
		clear();
		iValue = value;
		if (value != null)
			for (CBSNode node: value)
				addItem(generate(node));
	}
	
	protected TreeItem generate(final CBSNode node) {
		P widget = new P("cbs-node");
		Label counter = new Label(); counter.setText(node.getCount() + " \u00D7");
		widget.add(counter);
		if (node.getHTML() != null) {
			HTML html = new HTML(node.getHTML());
			widget.add(html);
		} else {
			Label name = new Label(); name.setText(node.getName());
			if (node.getPref() != null && iProperties != null) {
				PreferenceInterface pref = iProperties.getPreference(node.getPref());
				if (pref != null)
					name.getElement().getStyle().setColor(pref.getColor());
			}
			widget.add(name);
		}
		widget.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ConflictBasedStatisticsTree.this.onClick(event, node);
			}
		});
		TreeItem item = new TreeItem(widget);
		if (node.hasNodes()) {
			for (CBSNode child: node.getNodes()) {
				item.addItem(generate(child));
			}
		}
		return item;
	}

	@Override
	public List<CBSNode> getValue() {
		return iValue;
	}
}
