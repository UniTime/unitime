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
