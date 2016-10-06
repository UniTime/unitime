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
		try {
			Class.forName(Localization.ROOT + name);
			return new VariableInfo[] {
					new VariableInfo(id == null ? BundleTag.DEFAULT_ID : id, Localization.ROOT + name, true, VariableInfo.NESTED)
				};
		} catch (ClassNotFoundException e) {
			return new VariableInfo[] {
					new VariableInfo(id == null ? BundleTag.DEFAULT_ID : id, name, true, VariableInfo.NESTED)
				};
		}
	}

}
