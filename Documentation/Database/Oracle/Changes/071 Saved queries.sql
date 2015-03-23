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

create table saved_hql (
	uniqueid number(20,0) constraint nn_saved_hql_uniqueid not null,
	name varchar2(100) constraint nn_saved_hql_name not null,
	description varchar2(1000),
	query clob constraint nn_saved_hql_query not null,
	type number(10,0) constraint nn_saved_hql_type not null
);
alter table saved_hql add constraint pk_saved_hql primary key (uniqueid);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Not-assigned Classes', 'List all classes with a time pattern (i.e., classes that should not be Arrange Hours) which do not have a committed assignment.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, s.itype.abbv || '' '' || c.sectionNumberCache as Section, co.title as Title from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where c.uniqueId in ( select x.uniqueId from Class_ x, TimePref p where (p.owner = x or p.owner = x.schedulingSubpart) and p.prefLevel.prefProlog = ''R'' ) and co.subjectArea.uniqueId in %SUBJECTS% and c.committedAssignment is null order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Multi/No Room Classes', 'List all classes that either:<ul><li>either require more than one room</li><li>or require no room</li><li>or have zero room ratio</li></ul>',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, s.itype.abbv || '' '' || c.sectionNumberCache as Section, c.nbrRooms as Nbr_Rooms, c.roomRatio as Room_Ratio from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where co.subjectArea.uniqueId in %SUBJECTS% and (c.nbrRooms != 1 or c.roomRatio = 0.0) order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Schedule Note Classes', 'List of all classes that has something entered in Student Schedule Note.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, s.itype.abbv || '' '' || c.sectionNumberCache as Section, c.schedulePrintNote as Student_Schedule_Note from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where co.subjectArea.uniqueId in %SUBJECTS% and c.schedulePrintNote is not null order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Request Notes Classes', 'List of all classes that has something entered in Requests / Notes to Schedule Manager.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, s.itype.abbv || '' '' || c.sectionNumberCache as Section, c.notes as Notes_to_Schedule_Manager from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where co.subjectArea.uniqueId in %SUBJECTS% and c.notes is not null order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Schedule Book Note Courses', 'List of all courses that has something entered in Schedule Book Note.',
	'select co.instructionalOffering.uniqueId as __Offering, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, co.scheduleBookNote as Schedule_Book_Note from CourseOffering co where co.subjectArea.uniqueId in %SUBJECTS% and co.scheduleBookNote is not null order by co.subjectAreaAbbv, co.courseNbr', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'New Courses', 'List of all courses that do not have external unique id and courses that have no title.',
	'select co.instructionalOffering.uniqueId as __Offering, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, co.title as Title from CourseOffering co where co.subjectArea.uniqueId in %SUBJECTS% and (co.externalUniqueId is null or co.title is null) order by co.subjectAreaAbbv, co.courseNbr', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Arrange Hours Classes', 'List all classes that do not have a time pattern.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, s.itype.abbv || '' '' || c.sectionNumberCache as Section, co.title as Title from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where c.uniqueId not in ( select x.uniqueId from Class_ x, TimePref p where (p.owner = x or p.owner = x.schedulingSubpart) and p.prefLevel.prefProlog = ''R'' ) and co.subjectArea.uniqueId in %SUBJECTS% order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Cross-listed Courses', 'List all courses of a given subject area (or subject areas) that are cross-listed.',
	'select co.instructionalOffering.uniqueId as __Offering, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, co.title as Course_Title, ctr.subjectAreaAbbv || '' '' || ctr.courseNbr as Controlling, ctr.title as Controlling_Title from CourseOffering co, CourseOffering ctr where co.subjectArea in %SUBJECTS% and co.isControl is false and co.instructionalOffering = ctr.instructionalOffering and ctr.isControl is true order by co.subjectAreaAbbv, co.courseNbr', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'No-conflict Instructors', 'List of instructors (and their classes) that are not checked for instructor conflicts.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, s.itype.abbv || '' '' || c.sectionNumberCache as Section, i.instructor.lastName || '', '' || i.instructor.firstName || '' '' || i.instructor.middleName as Instructor, i.instructor.externalUniqueId as External_Id from ClassInstructor i inner join i.classInstructing c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where i.lead = false and co.subjectArea.uniqueId in %SUBJECTS% order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache, i.instructor.lastName', 1);

insert into saved_hql (uniqueid, name, description, query, type) values
	(pref_group_seq.nextval, 'Small Room Classes', 'List all classes that require (or prefer) a room that is too small for the class to fit in.',
	'select c.uniqueId as __Class, co.subjectAreaAbbv || '' '' || co.courseNbr as Course, s.itype.abbv || '' '' || c.sectionNumberCache as Section, p.room.building.abbreviation || '' '' || p.room.roomNumber as Room, p.room.capacity as Size, case c.roomRatio when 1.0 then (c.expectedCapacity || '''') else (floor(c.expectedCapacity * c.roomRatio) || '' ('' || c.roomRatio || '' x '' || c.expectedCapacity || '')'') end as Needed, p.prefLevel.prefName as Preference from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co, RoomPref p where co.subjectArea.uniqueId in %SUBJECTS% and (p.owner = c or p.owner = s) and floor(c.expectedCapacity * c.roomRatio) > p.room.capacity and p.prefLevel.prefProlog in (''R'', ''-1'', ''-2'') and c.nbrRooms > 0 order by co.subjectAreaAbbv, co.courseNbr, s.itype.abbv, c.sectionNumberCache', 1);
			
/**
 * Update database version
 */

update application_config set value='71' where name='tmtbl.db.version';

commit;
		