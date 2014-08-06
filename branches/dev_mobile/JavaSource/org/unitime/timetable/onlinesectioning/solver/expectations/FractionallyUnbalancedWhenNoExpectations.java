/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.solver.expectations;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;


/**
 * @author Tomas Muller
 */
public class FractionallyUnbalancedWhenNoExpectations extends FractionallyOverExpected {
	private Double iDisbalance = 0.1;
	private boolean iBalanceUnlimited = false;

	public FractionallyUnbalancedWhenNoExpectations(DataProperties config) {
		super(config);
		iDisbalance = config.getPropertyDouble("OverExpected.Disbalance", iDisbalance);
		iBalanceUnlimited = config.getPropertyBoolean("General.BalanceUnlimited", iBalanceUnlimited);
	}
	
	public FractionallyUnbalancedWhenNoExpectations(Double percentage, Double maximum, Double disbalance) {
		super(percentage, maximum);
		iDisbalance = disbalance;
	}
	
	public FractionallyUnbalancedWhenNoExpectations(Double percentage, Double maximum) {
		this(percentage, maximum, null);
	}
	
	public FractionallyUnbalancedWhenNoExpectations(Double percentage) {
		this(percentage, null, null);
	}
	
	public FractionallyUnbalancedWhenNoExpectations() {
		this(null, null, null);
	}
	
	public Double getDisbalance() {
		return iDisbalance;
	}
	
	public boolean isBalanceUnlimited() {
		return iBalanceUnlimited;
	}

	public double getMaximum(Section section, double defaultValue) {
		return getMaximum() == null || getMaximum() <= 0.0 ? defaultValue : getMaximum();
	}
	
	@Override
	public double getOverExpected(Assignment<Request, Enrollment> assignment, Section section, Request request) {
		Subpart subpart = section.getSubpart();

		if (hasExpectations(subpart) && section.getLimit() > 0)
			return super.getOverExpected(assignment, section, request);
		
		if (getDisbalance() == null || getDisbalance() < 0.0) return 0.0;
		
		double enrlConfig = request.getWeight() + getEnrollment(assignment, subpart.getConfig(), request);
		int subparts = section.getSubpart().getConfig().getSubparts().size();
		int limit = getLimit(section);
    	double enrl = request.getWeight() + getEnrollment(assignment, section, request);

		if (limit > 0) {
            // sections have limits -> desired size is section limit x (total enrollment / total limit)
            double desired = (enrlConfig / getLimit(subpart)) * limit;
            if (enrl - desired >= Math.max(1.0, getDisbalance() * limit)) {
            	double max = getMaximum(section, limit);
            	return Math.min(max, enrl - desired) / (max * subparts);
            }
        } else if (isBalanceUnlimited()) {
            // unlimited sections -> desired size is total enrollment / number of sections
        	double desired = enrlConfig / subpart.getSections().size();
        	if (enrl - desired >= Math.max(1.0, getDisbalance() * desired)) {
            	double max = getMaximum(section, desired);
        		return Math.min(max, enrl - desired) / (max * subparts);
        	}
        }
		
		return 0.0;
	}
	
	@Override
	public String toString() {
		return "fbal(" + getPercentage() + "," + getMaximum() + "," + getDisbalance() + ")";
	}
}
