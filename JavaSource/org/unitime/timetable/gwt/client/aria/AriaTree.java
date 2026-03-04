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
package org.unitime.timetable.gwt.client.aria;

import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class AriaTree extends Tree {
	public static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	public AriaTree() {
		super(RESOURCES, true);
		// move tree role to the top-level element
		Roles.getTreeRole().remove(getElement().getFirstChildElement());
		Roles.getTreeRole().set(getElement());
		// keep aria-activedescendant set on the top-level element
		addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				String ad = Roles.getTreeRole().getAriaActivedescendantProperty(getElement().getFirstChildElement());
				if (ad != null) {
					Roles.getTreeRole().removeAriaActivedescendantProperty(getElement().getFirstChildElement());
					Roles.getTreeRole().setAriaActivedescendantProperty(getElement(), Id.of(ad));
				} else {
					Roles.getTreeRole().removeAriaActivedescendantProperty(getElement());
				}
			}
		});
		// fix image alts
		addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				fixImageAlts(event.getTarget(), false);
			}
		});
		addCloseHandler(new CloseHandler<TreeItem>() {
			@Override
			public void onClose(CloseEvent<TreeItem> event) {
				fixImageAlts(event.getTarget(), false);
			}
		});
	}
	
	protected void fixImageAlts(TreeItem it, boolean recursive) {
		if (it == null) return;
		NodeList<Element> imgs = it.getElement().getElementsByTagName("img");
		if (imgs != null && imgs.getLength() > 0) {
			imgs.getItem(0).setPropertyString("alt", it.getChildCount() == 0 ? ARIA.iconTreeLeaf() : it.getState() ? ARIA.iconTreeOpened() : ARIA.iconTreeClosed());
		}
		if (recursive)
			for (int i = 0; i < it.getChildCount(); i++)
				fixImageAlts(it.getChild(i), recursive);
	}
	
	@Override
	public void addItem(TreeItem it) {
		super.addItem(it);
		fixImageAlts(it, true);
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		for (int i = 0; i < getItemCount(); i++)
			fixImageAlts(getItem(i), true);
	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);
		int eventType = DOM.eventGetType(event);
		if (eventType == Event.ONKEYDOWN && event.getKeyCode() == KeyCodes.KEY_ENTER) {
			if (getSelectedItem() != null)
				click(getSelectedItem().getWidget().getElement());
		}
	}
	
	public static native void click(Element elem) /*-{
    	elem.click();
	}-*/;
}
