/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.spring;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging;
import org.unitime.timetable.security.permissions.PermissionForRight;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class CustomBeanNameGenerator extends AnnotationBeanNameGenerator {

	@Override
	protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
		AnnotationMetadata amd = annotatedDef.getMetadata();
		Set<String> types = amd.getAnnotationTypes();
		String beanName = null;
		for (String type : types) {
			Map<String, Object> attributes = amd.getAnnotationAttributes(type);
			if (isStereotypeWithNameValue(type, amd.getMetaAnnotationTypes(type), attributes)) {
				String value = null;
				if (PermissionForRight.class.getName().equals(type)) {
					Right right = (Right)attributes.get("value");
					value = "permission" + right.name();
				} else if (GwtRpcImplements.class.getName().equals(type)) {
					Class<?> requestClass = (Class<?>)attributes.get("value");
					value = requestClass.getName();
				} else if (GwtRpcLogging.class.getName().equals(type)) {
					continue;
				} else {
					value = (String) attributes.get("value");
				}
				if (StringUtils.hasLength(value)) {
					if (beanName != null && !value.equals(beanName)) {
						throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
								"component names: '" + beanName + "' versus '" + value + "'");
					}
					beanName = value;
				}
			}
		}
		return beanName;
	}
}
