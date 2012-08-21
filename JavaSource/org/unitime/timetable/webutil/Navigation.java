/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.unitime.commons.Debug;
import org.unitime.timetable.security.SessionContext;


/**
 * @author Tomas Muller
 */
public class Navigation {
	public static String sLastDisplayedIdsSessionAttribute = "lastDispIds";
	public static int sNrLevels = 3;
	public static int sInstructionalOfferingLevel = 0;
	public static int sSchedulingSubpartLevel = 1;
	public static int sClassLevel = 2;
	
	public static Long getNext(SessionContext context, int level, Long id) {
		Vector[] ids = (Vector[])context.getAttribute(sLastDisplayedIdsSessionAttribute);
		int idx = (ids==null?-1:ids[level].indexOf(id));
		if (idx>=0) {
			try {
				return (Long)ids[level].elementAt(idx+1);
			} catch (ArrayIndexOutOfBoundsException e) {
				return new Long(-1);
			}
		}
		return null;
	}

	public static Long getPrevious(SessionContext context, int level, Long id) {
		Vector[] ids = (Vector[])context.getAttribute(sLastDisplayedIdsSessionAttribute);
		int idx = (ids==null?-1:ids[level].indexOf(id));
		if (idx>=0) {
			try {
				return (Long)ids[level].elementAt(idx-1);
			} catch (ArrayIndexOutOfBoundsException e) {
				return new Long(-1);
			}
		}
		return null;
	}
	
	public static void set(SessionContext session, int level, Collection entities) {
		Vector[] ids = (Vector[])session.getAttribute(sLastDisplayedIdsSessionAttribute);
		if (ids==null) {
			ids = new Vector[sNrLevels];
			for (int i=0;i<sNrLevels;i++)
				ids[i] = new Vector();
			session.setAttribute(sLastDisplayedIdsSessionAttribute, ids);
		}
		for (int i=level;i<sNrLevels;i++)
			ids[i].clear();
		if (entities==null || entities.isEmpty()) return;
		for (Iterator i=entities.iterator();i.hasNext();) {
			Object o = i.next();
			if (o instanceof Long) {
				ids[level].add(o);
			} else {
				try {
					if (o.getClass().isArray())
						ids[level].add(((Object[])o)[0].getClass().getMethod("getUniqueId", new Class[]{}).invoke(((Object[])o)[0],new Object[]{}));
					else
						ids[level].add(o.getClass().getMethod("getUniqueId", new Class[]{}).invoke(o,new Object[]{}));
				} catch (Exception e) {
					Debug.error(e);
				}
			}
		}
		//System.out.println("SET["+level+"]:"+ids[level]);
	}
}
