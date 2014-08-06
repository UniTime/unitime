/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.defaults;

/**
 * @author Tomas Muller
 */
public enum CommonValues {
	NoteAsIcon("icon"),
	NoteAsFullText("full text"),
	NoteAsShortText("shortened text"),
	
	HorizontalGrid("horizontal"),
	VerticalGrid("vertical"),
	TextGrid("text"),
	
	NameLastFirst("last-first"),
	NameFirstLast("first-last"),
	NameInitialLast("initial-last"),
	NameLastInitial("last-initial"),
	NameFirstMiddleLast("first-middle-last"),
	NameShort("short"),
	
	Yes("yes"),
	No("no"),
	
	Ask("ask"),
	Always("always"),
	Never("never"),
	
	SortByLastName("Always by Last Name"),
	SortAsDisplayed("Natural Order (as displayed)"),
	
	;

	String iValue;
	CommonValues(String value) {
		iValue = value;
	}
	
	public String value() { return iValue; }
	
	public boolean eq(String value) { return iValue.equals(value); }
	
	public boolean ne(String value) { return !eq(value); }
}
