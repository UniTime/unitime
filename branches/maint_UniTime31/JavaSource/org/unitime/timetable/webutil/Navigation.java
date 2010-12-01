/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.unitime.commons.Debug;


/**
 * @author Tomas Muller
 */
public class Navigation {
	public static String sLastDisplayedIdsSessionAttribute = "lastDispIds";
	public static int sNrLevels = 3;
	public static int sInstructionalOfferingLevel = 0;
	public static int sSchedulingSubpartLevel = 1;
	public static int sClassLevel = 2;
	
	public static Long getNext(HttpSession session, int level, Long id) {
		Vector[] ids = (Vector[])session.getAttribute(sLastDisplayedIdsSessionAttribute);
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

	public static Long getPrevious(HttpSession session, int level, Long id) {
		Vector[] ids = (Vector[])session.getAttribute(sLastDisplayedIdsSessionAttribute);
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
	
	public static void set(HttpSession session, int level, Collection entities) {
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
					ids[level].add(o.getClass().getMethod("getUniqueId", new Class[]{}).invoke(o,new Object[]{}));
				} catch (Exception e) {
					Debug.error(e);
				}
			}
		}
		//System.out.println("SET["+level+"]:"+ids[level]);
	}
}
