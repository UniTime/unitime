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
package org.unitime.timetable.util;


/**
 * Customized Message Resources 
 * @author Heston Fernandes, Tomas Muller
 */
public class MessageResourcesFactory extends
		org.apache.struts.util.MessageResourcesFactory {
	private static final long serialVersionUID = -3170113345618008226L;

	@Override
	public MessageResources createResources(String config) {
		return new MessageResources(this, config, this.getReturnNull());
	}

}
