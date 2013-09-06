/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2008 - 2011, UniTime LLC
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

create table sectioning_status (
		uniqueid number(20,0) constraint nn_sect_status_uniqueid not null,
		reference varchar2(20) constraint nn_sect_status_reference not null,
		label varchar2(60) constraint nn_sect_status_label not null,
		status number(10,0) constraint nn_sect_status_enabled not null,
		message varchar2(200)
	);
alter table sectioning_status add constraint pk_sect_status primary key (uniqueid);

alter table student add sect_status number(20,0);

alter table student
  	add constraint fk_student_sect_status foreign key (sect_status)
  	references sectioning_status (uniqueid) on delete set null;

alter table sessions add sect_status number(20,0);

alter table sessions
  	add constraint fk_session_sect_status foreign key (sect_status)
  	references sectioning_status (uniqueid) on delete set null;

alter table sessions add wk_enroll number(10,0) default 1;
alter table sessions add constraint nn_session_wk_enroll check (wk_enroll is not null);

alter table sessions add wk_change number(10,0) default 1;
alter table sessions add constraint nn_session_wk_change check (wk_change is not null);

alter table sessions add wk_drop number(10,0) default 1;
alter table sessions add constraint nn_session_wk_drop check (wk_drop is not null);

alter table instructional_offering add wk_enroll number(10,0);

alter table instructional_offering add wk_change number(10,0);

alter table instructional_offering add wk_drop number(10,0);

alter table course_demand add changed_by varchar2(40);

alter table student_class_enrl add changed_by varchar2(40);

alter table student add schedule_emailed date;

insert into sectioning_status (uniqueid, reference, label, status, message) values 
	(ref_table_seq.nextval, 'Enabled', 'Access enabled', 7, null);
insert into sectioning_status (uniqueid, reference, label, status, message) values 
	(ref_table_seq.nextval, 'Disabled', 'Access disabled', 4, null);
insert into sectioning_status (uniqueid, reference, label, status, message) values 
	(ref_table_seq.nextval, 'Not Available', 'Temporarily not available', 6, 'Access is temporarily disabled. Please try again later...');
insert into sectioning_status (uniqueid, reference, label, status, message) values 
	(ref_table_seq.nextval, 'No Email', 'Access enabled, no email notification', 3, null);

alter table student drop constraint fk_student_status_student;
alter table student drop column status_type_id;
alter table student drop column status_change_date;
drop table student_status_type;

/*
 * Update database version
 */

update application_config set value='80' where name='tmtbl.db.version';

commit;
