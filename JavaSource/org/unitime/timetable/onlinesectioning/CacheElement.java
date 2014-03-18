/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import org.cpsolver.ifs.util.JProf;

/**
 * @author Tomas Muller
 */
public class CacheElement<T> {
	private T iElement;
	private long iCreated;
	
	public CacheElement(T element) {
		iElement = element;
		iCreated = JProf.currentTimeMillis();
	}
	
	public T element() { return iElement; }
	
	public long created() { return iCreated; }
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof CacheElement && ((CacheElement<T>)o).element().equals(element())) return true;
		return o.equals(element());
	}
	
	public int hashCode() {
		return element().hashCode();
	}

}
