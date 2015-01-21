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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.client.aria.HasAriaLabel;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class UniTimeWidget<T extends Widget> extends Composite implements HasAriaLabel {
	private T iWidget;
	private HTML iReadOnly = null, iPrint = null;
	private HTML iHint;
	private VerticalPanel iPanel;
	private Element iAriaLabel = null;
	
	public UniTimeWidget(T widget, String hint) {
		iPanel = new VerticalPanel();
		
		iWidget = widget;
		iPanel.add(iWidget);
		
		iHint = new HTML(hint == null ? "" : hint, false);
		iHint.setStyleName("unitime-NotClickableHint");
		if (hint == null || hint.isEmpty())
			iHint.setVisible(false);
		iPanel.add(iHint);
		
		initWidget(iPanel);
	}
	
	@Override
	public void setAriaLabel(String text) {
		if (iWidget instanceof HasAriaLabel) {
			((HasAriaLabel)iWidget).setAriaLabel(text);
		} else {
			if (iAriaLabel == null) {
				iAriaLabel = DOM.createLabel();
				iAriaLabel.setId(DOM.createUniqueId());
				iAriaLabel.setClassName("unitime-AriaHiddenLabel");
				DOM.appendChild(getElement(), iAriaLabel);
				Roles.getCheckboxRole().setAriaLabelledbyProperty(iWidget.getElement(), Id.of(iAriaLabel));
			}
			iAriaLabel.setInnerText(text);
		}
	}
	
	@Override
	public String getAriaLabel() {
		if (iWidget instanceof HasAriaLabel)
			return ((HasAriaLabel)iWidget).getAriaLabel();
		else
			return (iAriaLabel == null ? "" : iAriaLabel.getInnerText());
	}
	
	public void setText(String html) {
		if (iReadOnly == null) {
			iReadOnly = new HTML(html, false);
			iReadOnly.setStyleName("unitime-LabelInsteadEdit");
			iReadOnly.setVisible(!getWidget().isVisible());
			iReadOnly.addStyleName("unitime-NoPrint");
			iPanel.insert(iReadOnly, 1);
		} else {
			iReadOnly.setHTML(html);
		}
	}

	public void setPrintText(String html) {
		if (iPrint == null) {
			iPrint = new HTML(html, true);
			iPrint.setStyleName("unitime-LabelInsteadEdit");
			iPrint.addStyleName("unitime-Print");
			getWidget().addStyleName("unitime-NoPrint");
			iPanel.insert(iPrint, 1);
		} else {
			iPrint.setHTML(html);
		}
	}

	public boolean showReadOnly() {
		return iWidget instanceof ListBox;
	}
	
	public UniTimeWidget(T widget) {
		this(widget, null);
	}
	
	public T getWidget() {
		return iWidget;
	}
	
	public void clearHint() {
		iHint.setHTML("");
		iHint.setVisible(false);
		setAriaLabel("");
	}
	
	public void setErrorHint(String error) {
		if (error == null || error.isEmpty()) {
			clearHint();
		} else {
			iHint.setStyleName("unitime-ErrorHint");
			iHint.setHTML(error);
			iHint.setVisible(true);
			setAriaLabel(error);
		}
	}

	public void setHint(String hint) {
		if (hint == null || hint.isEmpty()) {
			clearHint();
		} else {
			iHint.setStyleName("unitime-NotClickableHint");
			iHint.setHTML(hint);
			iHint.setVisible(true);
			setAriaLabel(hint);
		}
	}
	
	@Deprecated
	public void setVisible(boolean visible) {
		getWidget().setVisible(visible);
		if (iReadOnly != null)
			iReadOnly.setVisible(!visible);
	}
	
	@Deprecated
	public void clear() {
		if (getWidget() instanceof ListBox)
			((ListBox)getWidget()).clear();
		if (getWidget() instanceof Panel)
			((Panel)getWidget()).clear();
	}
	
	public void setReadOnly(boolean readOnly) {
		if (getWidget() instanceof UniTimeTextBox) {
			((UniTimeTextBox)getWidget()).setReadOnly(readOnly);
		} else {
			getWidget().setVisible(!readOnly);
			if (iReadOnly != null)
				iReadOnly.setVisible(readOnly);
		}
	}
	
	public boolean isReadOnly() {
		if (getWidget() instanceof UniTimeTextBox) {
			return ((UniTimeTextBox)getWidget()).isReadOnly();
		} else {
			return iReadOnly != null && iReadOnly.isVisible();
		}
	}
	
	protected VerticalPanel getPanel() { return  iPanel; }
}
