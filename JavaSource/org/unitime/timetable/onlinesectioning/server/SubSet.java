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
package org.unitime.timetable.onlinesectioning.server;

import java.util.TreeSet;

/**
 * @author Tomas Muller
 */
public class SubSet<T extends Comparable<T>> extends TreeSet<T> {
	private static final long serialVersionUID = 1L;
	private int iLimit = -1;
	
	public SubSet(Integer limit) {
		super();
		iLimit = (limit == null ? -1 : limit);
	}
	
	@Override
	public boolean add(T e) {
		if (iLimit <= 0 || size() < iLimit) {
			return super.add(e);
		}
		T last = last();
		if (last.compareTo(e) > 0) {
			remove(last);
			return super.add(e);
		} else {
			return false;
		}
	}
	
	public boolean isLimitReached() {
		return (iLimit > 0 && size() >= iLimit);
	}
}
