/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC
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

alter table meeting add approval_status bigint(10) not null default 0;
-- alter table meeting add approval_status number(10) default 0;
-- alter table meeting add constraint nn_meeting_approval_status check (approval_status is not null);
-- alter table meeting rename column approved_date to approval_date;
alter table meeting change column approved_date approval_date date null;

update meeting set approval_status = 1 where approval_date is not null;

insert into rights (role_id, value)
	select distinct r.role_id, 'EventMeetingDelete'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventMeetingEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'EventMeetingCancel'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventMeetingEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'EventMeetingInquire'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventMeetingApprove';

/*
 * Update database version
 */

update application_config set value='100' where name='tmtbl.db.version';

commit;
