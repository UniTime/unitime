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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Simple application context wrapper to be used for accessing Spring beans from legacy code.
 *
 * @author Tomas Muller
 */
@Service("springApplicationContext")
public class SpringApplicationContextHolder implements ApplicationContextAware {
	private static ApplicationContext sApplicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		sApplicationContext = applicationContext;
	}
	
	public static Object getBean(String beanName) {
		return sApplicationContext.getBean(beanName);
	}
	
	public static boolean isInitialized() {
		return sApplicationContext != null;
	}
}
