-- Table area Initial --

insert into areas (id,area_type,display_order,name,parent_id)
  values('021','CITY', 100, '上海',  null);




insert into areas (id,area_type,display_order,name,parent_id)
  values('02101','DISTRICT', 100, '浦东新区', '021');
insert into areas (id,area_type,display_order,name,parent_id)
  values('02102','DISTRICT', 200, '徐汇区', '021');



insert into areas (id,area_type,display_order,name,parent_id)
  values('02101001','AREA', 100, '陆家嘴', '02101');
insert into areas (id,area_type,display_order,name,parent_id)
  values('02101002','AREA', 200, '96广场', '02101');
insert into areas (id,area_type,display_order,name,parent_id)
  values('02102001','AREA', 300, '徐家汇', '02102');

-- Table brand Initial --
insert into categories (name,display_order,parent_id)
  values('饮食', 100, null);
insert into categories (name,display_order,parent_id)
  values('旅游', 200, null);
insert into categories (name,display_order,parent_id)
  values('渝信川菜', 100, 1);
insert into categories (name,display_order,parent_id)
  values('国旅', 200, 2);

-- Table brand Initial --
insert into brands (name,logo,display_order)
  values('来一份', null, 100);
insert into brands (name,logo,display_order)
  values('豆捞坊', null, 200);

-- Table operate_users Initial --
insert into operate_users(created_at,deleted,encrypted_password,lock_version,
login_name,mobile,password_salt,user_name)
values(now(), 'UNDELETED','1360dceeb96ae52402fb21f954a8268d',0,'admin',13910001000,'mSKHrC','小唐');
insert into operate_users_roles(role_id,user_id)
values(1,1);

