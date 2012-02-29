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

-- Table brand Initial --
insert into brands (name,logo,display_order,category_id)
  values('来一份', null, 100, 1);
insert into brands (name,logo,display_order,category_id)
  values('豆捞坊', null, 200, 1);