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
