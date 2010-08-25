/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.client;

import com.google.gwt.user.client.ui.Widget;

/**
 * Register GWT pages here.
 * @author Tomas Muller
 *
 */
public enum Pages {
	curricula("Curricula", new PageFactory() { public Widget create() { return new Curricula(); } }),
	curprojrules("Curriculum Projection Rules", new PageFactory() { public Widget create() { return new CurriculumProjectionRules(); } }),
	sectioning("Student Scheduling Assistant", new PageFactory() { public Widget create() { return new StudentSectioning(); } }),
	admin("Administration", new PageFactory() { public Widget create() { return new SimpleEdit(); } });
	
	private String iTitle;
	private PageFactory iFactory;
	
	Pages(String title, PageFactory factory) { iTitle = title; iFactory = factory; }
	public String title() { return iTitle; }
	public Widget widget() { return iFactory.create(); }
	
	public interface PageFactory {
		Widget create();
	}
}
