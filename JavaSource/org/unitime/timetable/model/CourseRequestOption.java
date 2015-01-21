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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseCourseRequestOption;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;

import com.google.protobuf.InvalidProtocolBufferException;



/**
 * @author Tomas Muller
 */
public class CourseRequestOption extends BaseCourseRequestOption {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseRequestOption () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseRequestOption (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/

	
    public OnlineSectioningLog.CourseRequestOption.OptionType getType() {
    	return (getOptionType() == null ? null : OnlineSectioningLog.CourseRequestOption.OptionType.valueOf(getOptionType()));
    }
        
    public OnlineSectioningLog.CourseRequestOption getOption() throws InvalidProtocolBufferException {
    	return OnlineSectioningLog.CourseRequestOption.parseFrom(getValue());
    }
    
    public void setOption(OnlineSectioningLog.CourseRequestOption option) {
    	setValue(option.toByteArray());
    	setOptionType(option.getType().getNumber());
    }

}
