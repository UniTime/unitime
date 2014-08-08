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
package org.unitime.timetable.gwt.client.page;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Tomas Muller
 */
public class PageLabel extends Composite implements PageLabelDisplay {
	private PageLabelDisplay IMPL = GWT.create(PageLabelDisplay.class);
	
	public PageLabel() {
		initWidget(IMPL.asWidget());
	}

	@Override
	public String getText() {
		return IMPL.getText();
	}

	@Override
	public void setText(String text) {
		IMPL.setText(text);
	}

	@Override
	public String getHelpUrl() {
		return IMPL.getHelpUrl();
	}

	@Override
	public void setHelpUrl(String url) {
		IMPL.setHelpUrl(url);
	}
}