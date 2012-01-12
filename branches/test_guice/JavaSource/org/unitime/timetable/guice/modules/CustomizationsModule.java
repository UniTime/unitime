/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.guice.modules;

import org.unitime.timetable.security.customization.CurriculumEditAlwaysDenyPermission;
import org.unitime.timetable.security.customization.CurriculumMgrRoleNoAddDelete;
import org.unitime.timetable.security.permissions.CurriculumPermissions;
import org.unitime.timetable.security.roles.CurriculumMgrRole;

import com.google.inject.AbstractModule;

public class CustomizationsModule extends AbstractModule {

	@Override
	protected void configure() {
		// This can be done using a config file or database
		// or in an institution-specific module
		bind(CurriculumPermissions.CanEdit.class).to(CurriculumEditAlwaysDenyPermission.class);
		bind(CurriculumMgrRole.class).to(CurriculumMgrRoleNoAddDelete.class);
	}
}
