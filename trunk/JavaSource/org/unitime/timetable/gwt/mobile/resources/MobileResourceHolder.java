/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.mobile.resources;

import org.unitime.timetable.gwt.mobile.resources.MobileResourceHolder.MobileResourceAppearance.Images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Tomas Muller
 */
public class MobileResourceHolder {
	private static final MobileResourceAppearance APPEARANCE = GWT.create(MobileResourceAppearance.class);
	
	public interface MobileResourceAppearance {
		public interface Images {
			ImageResource menu();
		}
		
		Images get();
	}
	
	public static Images get() {
		return APPEARANCE.get();
	}
}
