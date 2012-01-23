/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC
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

create table saved_hql (
	uniqueid decimal(20,0) primary key not null,
	name varchar(100) not null,
	description varchar(1000) null,
	query longtext binary not null,
	type decimal(10,0) not null	
);

select 32767 * next_hi into @id from hibernate_unique_key;

insert into saved_hql (uniqueid, name, description, query, type) values
	(@id, 'Not-assigned Classes', 'List all classes with a time pattern (i.e., classes that should not be Arrange Hours) which do not have a committed assignment.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, s.itype.abbv || \' \' || c.sectionNumberCache as Section, co.title as Title from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where c.uniqueId in ( select x.uniqueId from Class_ x, TimePref p where (p.owner = x or p.owner = x.schedulingSubpart) and p.prefLevel.prefProlog = \'R\' ) and co.subjectArea.uniqueId in %SUBJECTS% and c.committedAssignment is null order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1),
	(@id + 1, 'Multi/No Room Classes', 'List all classes that either:<ul><li>either require more than one room</li><li>or require no room</li><li>or have zero room ratio</li></ul>',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, s.itype.abbv || \' \' || c.sectionNumberCache as Section, c.nbrRooms as Nbr_Rooms, c.roomRatio as Room_Ratio from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where co.subjectArea.uniqueId in %SUBJECTS% and (c.nbrRooms != 1 or c.roomRatio = 0.0) order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1),
	(@id + 2, 'Schedule Note Classes', 'List of all classes that has something entered in Student Schedule Note.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, s.itype.abbv || \' \' || c.sectionNumberCache as Section, c.schedulePrintNote as Student_Schedule_Note from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where co.subjectArea.uniqueId in %SUBJECTS% and c.schedulePrintNote is not null order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1),
	(@id + 3, 'Request Notes Classes', 'List of all classes that has something entered in Requests / Notes to Schedule Manager.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, s.itype.abbv || \' \' || c.sectionNumberCache as Section, c.notes as Notes_to_Schedule_Manager from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where co.subjectArea.uniqueId in %SUBJECTS% and c.notes is not null order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1),
	(@id + 4, 'Schedule Book Note Courses', 'List of all courses that has something entered in Schedule Book Note.',
	'select co.instructionalOffering.uniqueId as __Offering, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, co.scheduleBookNote as Schedule_Book_Note from CourseOffering co where co.subjectArea.uniqueId in %SUBJECTS% and co.scheduleBookNote is not null order by co.subjectAreaAbbv, co.courseNbr', 1),
	(@id + 5, 'New Courses', 'List of all courses that do not have external unique id and courses that have no title.',
	'select co.instructionalOffering.uniqueId as __Offering, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, co.title as Title from CourseOffering co where co.subjectArea.uniqueId in %SUBJECTS% and (co.externalUniqueId is null or co.title is null) order by co.subjectAreaAbbv, co.courseNbr', 1),
	(@id + 6, 'Arrange Hours Classes', 'List all classes that do not have a time pattern.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, s.itype.abbv || \' \' || c.sectionNumberCache as Section, co.title as Title from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where c.uniqueId not in ( select x.uniqueId from Class_ x, TimePref p where (p.owner = x or p.owner = x.schedulingSubpart) and p.prefLevel.prefProlog = \'R\' ) and co.subjectArea.uniqueId in %SUBJECTS% order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1),
	(@id + 7, 'Cross-listed Courses', 'List all courses of a given subject area (or subject areas) that are cross-listed.',
	'select co.instructionalOffering.uniqueId as __Offering, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, co.title as Course_Title, ctr.subjectAreaAbbv || \' \' || ctr.courseNbr as Controlling, ctr.title as Controlling_Title from CourseOffering co, CourseOffering ctr where co.subjectArea in %SUBJECTS% and co.isControl is false and co.instructionalOffering = ctr.instructionalOffering and ctr.isControl is true order by co.subjectAreaAbbv, co.courseNbr', 1),
	(@id + 8, 'No-conflict Instructors', 'List of instructors (and their classes) that are not checked for instructor conflicts.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, s.itype.abbv || \' \' || c.sectionNumberCache as Section, i.instructor.lastName || \', \' || i.instructor.firstName || \' \' || i.instructor.middleName as Instructor, i.instructor.externalUniqueId as External_Id from ClassInstructor i inner join i.classInstructing c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where i.lead = false and co.subjectArea.uniqueId in %SUBJECTS% order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache, i.instructor.lastName', 1),
	(@id + 9, 'Small Room Classes', 'List all classes that require (or prefer) a room that is too small for the class to fit in.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || \' \' || co.courseNbr as Course, s.itype.abbv || \' \' || c.sectionNumberCache as Section, p.room.building.abbreviation || \' \' || p.room.roomNumber as Room, p.room.capacity as Size, case c.roomRatio when 1.0 then (c.expectedCapacity || \'\') else (floor(c.expectedCapacity * c.roomRatio) || \' (\' || c.roomRatio || \' x \' || c.expectedCapacity || \')\') end as Needed, p.prefLevel.prefName as Preference from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co, RoomPref p where co.subjectArea.uniqueId in %SUBJECTS% and (p.owner = c or p.owner = s) and floor(c.expectedCapacity * c.roomRatio) > p.room.capacity and p.prefLevel.prefProlog in (\'R\', \'-1\', \'-2\') and c.nbrRooms > 0 order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1);

update hibernate_unique_key set next_hi=next_hi+1;

/**
 * Update database version
 */

update application_config set value='71' where name='tmtbl.db.version';

commit;
		