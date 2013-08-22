/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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

insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'OnFlySectioning', 'On Fly Student Sectioning', '', -1);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='OnFlySectioning';
	
insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'OnFlySectioning.Enabled' as NAME, 
		'false' as DEFAULT_VALUE, 
		'Enable on fly sectioning (if enabled, students will be resectioned after each iteration)' as DESCRIPTION, 
		'boolean' as TYPE, 
		0 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='OnFlySectioning');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'OnFlySectioning.Recursive' as NAME, 
		'true' as DEFAULT_VALUE, 
		'Recursively resection lectures affected by a student swap' as DESCRIPTION, 
		'boolean' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='OnFlySectioning');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'OnFlySectioning.ConfigAsWell' as NAME, 
		'false' as DEFAULT_VALUE, 
		'Resection students between configurations as well' as DESCRIPTION, 
		'boolean' as TYPE, 
		2 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='OnFlySectioning');
		
/** 
-- Uncomment this insert to enable on fly student sectioning in the default solver configuration
insert into SOLVER_PARAMETER
	(select SOLVER_PARAMETER_SEQ.nextval as UNIQUEID,
	 'on' as VALUE,
	 d.UNIQUEID as SOLVER_PARAM_DEF_ID,
	 NULL as SOLUTION_ID,
	 s.UNIQUEID as SOLVER_PREDEF_SETTING_ID
	 from SOLVER_PARAMETER_DEF d, SOLVER_PREDEF_SETTING s where d.NAME='OnFlySectioning.Enabled' and s.NAME='Default.Solver');
*/

commit;
