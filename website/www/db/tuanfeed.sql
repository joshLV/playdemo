
                                <!-- 标签，从食品饮料，化妆品，家居用品，服装鞋袜，配件饰品，孕婴儿童，其他网购，游乐游艺，运动健身，摄影写真， 电影票，温泉洗浴，养生按摩，健康护理，赛事演出，聚会欢畅，数码家电，美容塑形，其他娱乐，教育培训，报刊杂志，地方菜系，
                                        海鲜，蛋糕甜品，日韩亚系，西餐国际，火锅烧烤，快餐休闲，旅游，酒店，车房，其他生活，自助，门票郊游，文体户外，其他， 0元抽奖 里选择，可多选，用','(半角逗号)分开
                                        [必填] -->


update `categories` set `tuan800name`='聚会欢畅,地方菜系,火锅烧烤' where id=3;
update `categories` set `tuan800name`='聚会欢畅,快餐休闲' where id=4;
update `categories` set `tuan800name`='食品饮料,西餐国际' where id=5;
update `categories` set `tuan800name`='食品饮料' where id=6;
update `categories` set `tuan800name`='美容塑形' where id=8;
update `categories` set `tuan800name`='聚会欢畅,地方菜系,火锅烧烤' where id=9;
update `categories` set `tuan800name`='聚会欢畅,地方菜系,海鲜' where id=10;
update `categories` set `tuan800name`='旅游' where id=12;
update `categories` set `tuan800name`='家居用品' where id=14;
update `categories` set `tuan800name`='酒店' where id=15;
update `categories` set `tuan800name`='旅游,酒店,门票郊游' where id=18;
update categories set tuan800name='蛋糕甜品' where id=17;
INSERT INTO `categories` (`id`, `display_order`, `name`, `parent_id`, `keywords`, `tuan800name`) VALUES (NULL, '3', 'KTV', '7', 'KTV', '聚会欢畅');
