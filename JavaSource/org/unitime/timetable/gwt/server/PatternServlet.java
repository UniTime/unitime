/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cpsolver.coursett.model.TimeLocation;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
public class PatternServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean vertical = "1".equals(request.getParameter("v"));
		RequiredTimeTable rtt = null;
		if (request.getParameter("tp") != null) {
			TimePattern p = TimePatternDAO.getInstance().get(Long.valueOf(request.getParameter("tp")));
			if (p != null) {
				TimeLocation t = null;
				if (request.getParameter("as") != null && request.getParameter("ad") != null) {
					t = new TimeLocation(Integer.parseInt(request.getParameter("ad")), Integer.parseInt(request.getParameter("as")),
							1, 0, 0, null, null, null, 0);
				}
				rtt = new RequiredTimeTable(p.getTimePatternModel(t, true));
			}
		} else if (request.getParameter("loc") != null) {
			Location location = LocationDAO.getInstance().get(Long.valueOf(request.getParameter("loc")));
			if (location != null) {
				if (request.getParameter("xt") != null) {
                    PeriodPreferenceModel px = new PeriodPreferenceModel(location.getSession(), Long.valueOf(request.getParameter("xt")));
                    px.load(location);
                    rtt = new RequiredTimeTable(px);
				} else {
					if ("1".equals(request.getParameter("e")))
						rtt = location.getEventAvailabilityTable();
					else
						rtt = location.getRoomSharingTable();
				}
			}
		} else if (request.getParameter("x") != null) {
			Exam exam = ExamDAO.getInstance().get(Long.valueOf(request.getParameter("x")));
			if (exam != null) {
				ExamPeriod p = null;
				if (request.getParameter("ap") != null)
					p = ExamPeriodDAO.getInstance().get(Long.valueOf(request.getParameter("ap")));
				PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession(), p, exam.getExamType().getUniqueId());
                px.load(exam);
                rtt = new RequiredTimeTable(px);
			}
		} else {
			rtt = new RequiredTimeTable(new TimePattern().getTimePatternModel());
		}
		if (rtt != null) {
			if (request.getParameter("s") != null) {
				try {
					rtt.getModel().setDefaultSelection(Integer.parseInt(request.getParameter("s")));
				} catch (NumberFormatException e) {
					rtt.getModel().setDefaultSelection(request.getParameter("s"));
				}
			} if (request.getParameter("p") != null)
				rtt.getModel().setPreferences(request.getParameter("p"));
			boolean hc = ("1".equals(request.getParameter("hc")));
			
			response.setContentType("image/png");
			response.setHeader( "Content-Disposition", "attachment; filename=\"pattern.png\"" );
			BufferedImage image = rtt.createBufferedImage(vertical, hc);
			if (image != null)
				ImageIO.write(image, "PNG", response.getOutputStream());
		}
	}

}
