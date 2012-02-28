# --- !Ups
 
insert into areas (id,area_type,display_order,name,parent_id)
  values('021','CITY', 100, '上海',  null);




insert into areas (id,area_type,display_order,name,parent_id)
  values('02101','AREA', 100, '浦东新区', '021');
insert into areas (id,area_type,display_order,name,parent_id)
  values('02102','AREA', 200, '徐汇区', '021');



insert into areas (id,area_type,display_order,name,parent_id)
  values('02101001','AREA', 100, '陆家嘴', '02101');
insert into areas (id,area_type,display_order,name,parent_id)
  values('02101002','AREA', 200, '96广场', '02101');
insert into areas (id,area_type,display_order,name,parent_id)
  values('02102001','AREA', 300, '徐家汇', '02102');

# --- !Downs
delete from areas;