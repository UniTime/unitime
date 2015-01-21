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
