drop table report_daily_detail;
create table report_daily_detail
(
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
supplier_id BIGINT not null,
goods_id BIGINT not null,
shop_id BIGINT not null,
buy_count BIGINT not null,
resale_amount decimal,
sale_amount decimal,
original_amount decimal not null,
order_count decimal,
created_at DATE not null);


drop table report_daily_total;
create table report_daily_total
(
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
supplier_id BIGINT not null,
buy_count BIGINT not null,
resale_amount decimal,
sale_amount decimal,
original_amount decimal not null,
order_count decimal,
created_at DATE not null);

drop table report_daily_shop;
create table report_daily_shop
(
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
supplier_id BIGINT not null,
shop_id BIGINT not null,
buy_count BIGINT not null,
resale_amount decimal,
sale_amount decimal,
original_amount decimal not null,
order_count decimal,
created_at DATE not null);

drop table report_daily_goods;
create table report_daily_goods
(
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
supplier_id BIGINT not null,
goods_id BIGINT not null,
buy_count BIGINT not null,
resale_amount decimal,
sale_amount decimal,
original_amount decimal not null,
order_count decimal,
created_at DATE not null);

--运营后台商户报表
drop table report_daily_supplier;
create table report_daily_goods
(
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
supplier_id BIGINT not null,
goods_id BIGINT not null,
buy_count BIGINT not null,
resale_amount decimal,
sale_amount decimal,
original_amount decimal not null,
order_count decimal,
created_at DATE not null);

--采购税务表
drop table report_purchase_tax;
create table report_purchase_tax
(
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
supplier_id BIGINT not null,
goods_id BIGINT not null,
buy_count BIGINT not null,
order_count BIGINT null,
original_amount decimal not null,
tax decimal null,
no_tax_amount decimal null,
created_at DATE not null);
);
