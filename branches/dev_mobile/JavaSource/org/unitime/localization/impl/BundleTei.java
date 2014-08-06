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
package org.unitime.localization.impl;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import org.unitime.localization.impl.Localization;

/**
 * @author Tomas Muller
 */
public class BundleTei extends TagExtraInfo {
	
	public VariableInfo [] getVariableInfo(TagData data) {
		String name = data.getAttributeString("name");
		String id = data.getAttributeString("id");
		return new VariableInfo[] {
			new VariableInfo(id == null ? BundleTag.DEFAULT_ID : id, Localization.ROOT + name, true, VariableInfo.NESTED)
		};
	}

}
