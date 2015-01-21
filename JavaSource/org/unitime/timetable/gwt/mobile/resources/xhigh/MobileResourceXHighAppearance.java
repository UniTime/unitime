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
package org.unitime.timetable.gwt.mobile.resources.xhigh;

import org.unitime.timetable.gwt.mobile.resources.MobileResourceHolder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Tomas Muller
 */
public class MobileResourceXHighAppearance implements MobileResourceHolder.MobileResourceAppearance {

	interface Resources extends ClientBundle, Images {
		Resources INSTANCE = GWT.create(Resources.class);
		
		@Source("menu-64x64.png")
	    ImageResource menu();
	}
	
	@Override
	public Images get() {
		return Resources.INSTANCE;
	}
}
