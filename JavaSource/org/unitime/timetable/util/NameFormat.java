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
	TITLE_FIRST_MIDDLE_LAST("title-first-middle-last", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return (name.getAcademicTitle() != null && !name.getAcademicTitle().isEmpty() ? name.getAcademicTitle() + " ": "") +
					Constants.toInitialCase(
					(name.getFirstName() != null && !name.getFirstName().isEmpty() ? name.getFirstName() : "") +
					(name.getMiddleName() != null && !name.getMiddleName().isEmpty() ? " " + name.getMiddleName() : "") +
					(name.getLastName() != null && !name.getLastName().isEmpty() ? " " + name.getLastName() : "")
					).trim();
		}
	}),
	LAST_FIRST_MIDDLE_TITLE("last-first-middle-title", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return Constants.toInitialCase(
					(name.getLastName() != null && !name.getLastName().isEmpty() ? name.getLastName() : "") +
					((name.getFirstName() != null && !name.getFirstName().isEmpty()) || (name.getMiddleName() != null && !name.getMiddleName().isEmpty()) ? "," : "") +
					(name.getFirstName() != null && !name.getFirstName().isEmpty() ? " " + name.getFirstName() : "") +
					(name.getMiddleName() != null && !name.getMiddleName().isEmpty() ? " " + name.getMiddleName() : "")
					).trim() + 
					(name.getAcademicTitle() != null && !name.getAcademicTitle().isEmpty() ? " " + name.getAcademicTitle(): "");
		}
	}),
	TITLE_INITIAL_LAST("title-initial-last", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return (name.getAcademicTitle() != null && !name.getAcademicTitle().isEmpty() ? name.getAcademicTitle() + " ": "") +
					((name.getFirstName() != null && !name.getFirstName().isEmpty() ? name.getFirstName().trim().substring(0, 1).toUpperCase() : "") +
					(name.getMiddleName() != null && !name.getMiddleName().isEmpty() ? " " + name.getMiddleName().trim().substring(0, 1).toUpperCase() : "") +
					(name.getLastName() != null && !name.getLastName().isEmpty() ? " " + Constants.toInitialCase(name.getLastName()) : "")).trim();
		}
	}),
	TITLE_LAST_INITIAL("title-last-initial", new Formatter() {
		@Override
		public String format(NameInterface name) {
			return (name.getAcademicTitle() != null && !name.getAcademicTitle().isEmpty() ? name.getAcademicTitle() + " ": "") +
					(name.getLastName() != null && !name.getLastName().isEmpty() ? Constants.toInitialCase(name.getLastName()) : "").trim() +
					((name.getFirstName() != null && !name.getFirstName().isEmpty()) || (name.getMiddleName() != null && !name.getMiddleName().isEmpty()) ? "," : "") +
					(name.getFirstName() != null && !name.getFirstName().isEmpty() ? " " + name.getFirstName().substring(0, 1).toUpperCase() : "") +
					(name.getMiddleName() != null && !name.getMiddleName().isEmpty() ? " " + name.getMiddleName().substring(0, 1).toUpperCase() : "");
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
