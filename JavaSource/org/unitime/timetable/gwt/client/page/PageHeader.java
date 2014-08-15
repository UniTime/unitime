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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Tomas Muller
 */
public class PageHeader extends Composite implements PageHeaderDisplay {
	private PageHeaderDisplay IMPL = GWT.create(PageHeaderDisplay.class);
	
	public PageHeader() {
		initWidget(IMPL.asWidget());
	}

	@Override
	public InfoPanel getLeft() {
		return IMPL.getLeft();
	}

	@Override
	public InfoPanel getMiddle() {
		return IMPL.getMiddle();
	}

	@Override
	public InfoPanel getRight() {
		return IMPL.getRight();
	}
}
