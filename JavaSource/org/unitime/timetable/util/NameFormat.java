/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

/**
 * @author Tomas Muller
 */
public enum NameFormat {
	LAST_FIRST("last-first", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return Constants.toInitialCase(
					(name.getLastName() != null && !name.getLastName().isEmpty() ? name.getLastName() : "") +
					(name.getFirstName() != null && !name.getFirstName().isEmpty() ? ", " + name.getFirstName() : "")
					).trim();
		}
	}),
	FIRST_LAST("first-last", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return Constants.toInitialCase(
					(name.getFirstName() != null && !name.getFirstName().isEmpty() ? name.getFirstName() : "") +
					(name.getLastName() != null && !name.getLastName().isEmpty() ? " " + name.getLastName() : "")
					).trim();
		}
	}),
	INITIAL_LAST("initial-last", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return ((name.getFirstName() != null && !name.getFirstName().isEmpty() ? name.getFirstName().trim().substring(0, 1).toUpperCase() : "") +
					(name.getMiddleName() != null && !name.getMiddleName().isEmpty() ? " " + name.getMiddleName().trim().substring(0, 1).toUpperCase() : "") +
					(name.getLastName() != null && !name.getLastName().isEmpty() ? " " + Constants.toInitialCase(name.getLastName()) : "")).trim();
		}
	}),
	LAST_INITIAL("last-initial", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return (name.getLastName() != null && !name.getLastName().isEmpty() ? Constants.toInitialCase(name.getLastName()) : "").trim() +
					((name.getFirstName() != null && !name.getFirstName().isEmpty()) || (name.getMiddleName() != null && !name.getMiddleName().isEmpty()) ? "," : "") +
					(name.getFirstName() != null && !name.getFirstName().isEmpty() ? " " + name.getFirstName().substring(0, 1).toUpperCase() : "") +
					(name.getMiddleName() != null && !name.getMiddleName().isEmpty() ? " " + name.getMiddleName().substring(0, 1).toUpperCase() : "");
		}
	}),
	FIRST_MIDDLE_LAST("first-middle-last", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return Constants.toInitialCase(
					(name.getFirstName() != null && !name.getFirstName().isEmpty() ? name.getFirstName() : "") +
					(name.getMiddleName() != null && !name.getMiddleName().isEmpty() ? " " + name.getMiddleName() : "") +
					(name.getLastName() != null && !name.getLastName().isEmpty() ? " " + name.getLastName() : "")
					).trim();
		}
	}),
	LAST_FIRST_MIDDLE("last-first-middle", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return Constants.toInitialCase(
					(name.getLastName() != null && !name.getLastName().isEmpty() ? name.getLastName() : "") +
					((name.getFirstName() != null && !name.getFirstName().isEmpty()) || (name.getMiddleName() != null && !name.getMiddleName().isEmpty()) ? "," : "") +
					(name.getFirstName() != null && !name.getFirstName().isEmpty() ? " " + name.getFirstName() : "") +
					(name.getMiddleName() != null && !name.getMiddleName().isEmpty() ? " " + name.getMiddleName() : "")
					).trim();
		}
	}),
	SHORT("short", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return (name.getFirstName() != null && !name.getFirstName().isEmpty() ? name.getFirstName().substring(0, 1).toUpperCase() + ". " : "") +
					(name.getLastName() != null && !name.getLastName().isEmpty() ? name.getLastName().substring(0, 1).toUpperCase() + name.getLastName().substring(1, Math.min(10, name.getLastName().length())).toLowerCase() : "").trim();
		}
	}),
	;
		
	private String iRefernence;
	private Formatter iFormatter;
	NameFormat(String reference, Formatter formatter) {
		iRefernence = reference;
		iFormatter = formatter;
	}
	
	public String reference() { return iRefernence; }
	public static NameFormat defaultFormat() { return LAST_FIRST_MIDDLE; }
	
	public static NameFormat fromReference(String reference) {
		for (NameFormat f: values())
			if (f.reference().equals(reference)) return f;
		return defaultFormat();
	}
	
	public String format(NameInterface name) {
		return iFormatter.format(name);
	}

	private static interface Formatter {
		public String format(NameInterface name);
	}
}
