/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.tags;

import net.sf.cpsolver.ifs.util.ToolBox;


/**
 * @author Tomas Muller
 */
public class PropertyNotEquals extends PropertyEquals {
	
	private static final long serialVersionUID = 9199601881914077244L;

	public int doStartTag() {
        try {
            String value = getProperty();
            if (!ToolBox.equals(value,getValue())) return EVAL_BODY_INCLUDE;
        } catch (Exception e) {}
        return SKIP_BODY;
	}
	
}
