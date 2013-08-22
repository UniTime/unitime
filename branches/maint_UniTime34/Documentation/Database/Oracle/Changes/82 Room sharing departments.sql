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

update room set pattern = replace(replace(pattern, 'F', '*'), 'X', '#') where pattern is not null;
update non_university_location set pattern = replace(replace(pattern, 'F', '*'), 'X', '#') where pattern is not null;
alter table room modify manager_ids varchar2(3000);
alter table non_university_location modify manager_ids varchar2(3000);

/*
 * Update database version
 */

update application_config set value='82' where name='tmtbl.db.version';

commit;
