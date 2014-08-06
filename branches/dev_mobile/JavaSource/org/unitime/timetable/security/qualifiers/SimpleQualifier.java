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
package org.unitime.timetable.security.qualifiers;

import java.io.Serializable;

import org.unitime.timetable.security.Qualifiable;

/**
 * @author Tomas Muller
 */
public class SimpleQualifier extends AbstractQualifier {
	private static final long serialVersionUID = 1L;

	
	public SimpleQualifier(Qualifiable qualifiable) {
		super(qualifiable.getQualifierType(), qualifiable.getQualifierId(), qualifiable.getQualifierReference(), qualifiable.getQualifierLabel());
	}
	
	
	public SimpleQualifier(String type, Serializable id, String reference, String label) {
		super(type, id, reference, label);
	}


	public SimpleQualifier(String type, Serializable idOrReference) {
		super(type, (idOrReference instanceof String ? null : idOrReference), (idOrReference instanceof String ? (String)idOrReference : null), null);
	}
}
