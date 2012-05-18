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
