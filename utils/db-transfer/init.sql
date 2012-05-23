-- mysql -u root -h 192.168.100.161 --default-character-set=utf8 -pxxxxx reeb < init.sql


--
-- 转存表中的数据 `operate_users`
--

-- INSERT INTO `operate_users` (`id`, `created_at`, `deleted`, `encrypted_password`, `last_login_at`, `last_login_ip`, `lock_version`, `login_name`, `mobile`, `password_salt`, `updated_at`, `user_name`) VALUES
-- (1, '2012-04-10 10:58:02', 0, '1360dceeb96ae52402fb21f954a8268d', '2012-05-15 12:42:41', '0:0:0:0:0:0:0:1', 0, 'admin', '13910001000', 'mSKHrC', '2012-05-15 12:42:41', '管理员');

-- 加入角色
-- INSERT INTO `operate_users_roles` (`role_id`, `user_id`) SELECT `id`, 1 FROM  `operate_roles` ;


--
-- 转存表中的数据 `user_point_config`
--

delete from `user_point_config`;

INSERT INTO `user_point_config` (`id`, `point_type`, `point_number`, `deal_points`, `point_note`, `point_title`) VALUES
(1, 1, '100', 200, '成功注册获得积分', '注册'),
(2, 1, '101', 100, '补充完整会员资料获得积分', '补充完资料'),
(3, 1, '102', 100, '用户每天登录网站/每天只算一次', '登录'),
(4, 1, '103', 0, '用户每月第一笔充值增送积分', '充值'),
(5, 1, '104', 10, '网站或终端打印优惠券，每天计算10张', '打印优惠券/短信优惠券'),
(6, 1, '105', 0, '自由的给商户的评论，每天计算10条', '普通商户评论'),
(7, 1, '106', 0, '给商户上传的图片，每天计算10条', '上传图片'),
(8, 1, '107', 0, '购买消费后给商户的评论', '消费给商户评论'),
(9, 1, '109', 10, '1元获得1积分，优卡会员双倍积分', '购买商品'),
(10, 1, '110', 10, '1元获得1积分，优卡会员双倍积分', '团购'),
(11, 0, '120', 0, '删除图片扣除积分', '删除图片'),
(12, 0, '121', 0, '删除评论扣除积分', '删除评论'),
(13, 1, '122', 2000, '邀请积分', '邀请积分'),
(14, 1, '123', 10, '退款', '退款'),
(15, 1, '124', 1000, '会员帐号绑定手机获得积分, 只计算第一次', '绑定手机'),
(16, 1, '125', 500, '会员帐号绑定邮箱优卡获得积分, 只计算第一次', '绑定邮箱'),
(17, 1, '126', 2000, '会员帐号优卡获得积分，只计算第一次(首次)', '绑定优卡');


--
-- 转存表中的数据 `areas`
--
update `areas` set parent_id=null;
delete from `areas`;
INSERT INTO `areas` (`id`, `area_type`, `display_order`, `name`, `parent_id`) VALUES
('021', 'CITY', 100, '上海', NULL),
('02101', 'DISTRICT', 100, '卢湾区', '021'),
('02101001', 'AREA', 100, '淮海路', '02101'),
('02101002', 'AREA', 200, '新天地', '02101'),
('02101003', 'AREA', 1030, '瑞金宾馆区', '02101'),
('02101004', 'AREA', 1040, '打浦桥', '02101'),
('02102', 'DISTRICT', 200, '徐汇区', '021'),
('02102001', 'AREA', 300, '徐家汇', '02102'),
('02102002', 'AREA', 2020, '万体馆', '02102'),
('02102003', 'AREA', 2030, '衡山路', '02102'),
('02102004', 'AREA', 2040, '复兴西路', '02102'),
('02102005', 'AREA', 2050, '丁香花园', '02102'),
('02102006', 'AREA', 2060, '肇嘉浜沿线', '02102'),
('02102007', 'AREA', 2070, '音乐学院', '02102'),
('02102008', 'AREA', 2080, '龙华', '02102'),
('02102009', 'AREA', 2090, '漕河泾', '02102'),
('0210210', 'AREA', 2100, '田林', '02102'),
('0210211', 'AREA', 2110, '上海南站', '02102'),
('02103', 'DISTRICT', 300, '静安区', '021'),
('02103001', 'AREA', 3010, '南京西路', '02103'),
('02103002', 'AREA', 3020, '静安寺', '02103'),
('02103003', 'AREA', 3030, '曹家渡', '02103'),
('02103004', 'AREA', 3040, '同乐坊', '02103'),
('02103005', 'AREA', 3050, '吴江路', '02103'),
('02104', 'DISTRICT', 400, '长宁区', '021'),
('02104001', 'AREA', 4010, '中山公园', '02104'),
('02104002', 'AREA', 4020, '虹桥', '02104'),
('02104003', 'AREA', 4030, '天山', '02104'),
('02104004', 'AREA', 4040, '古北', '02104'),
('02104005', 'AREA', 4050, '上海影城', '02104');



--
-- 转存表中的数据 `payment_source`
--
delete from `payment_source`;
INSERT INTO `payment_source` (`id`, `code`, `detail`, `logo`, `name`, `payment_code`, `show_order`) VALUES
(1, 'alipay', '支付宝', 'ali', '支付宝', 'alipay', 1),
(2, 'tenpay', '财付通', 'tenpay', '财付通', 'tenpay', 2),
(3, 'CMB', '招商银行', 'images/bank/bank_cmb.gif', '招商银行', '99bill', 3),
(4, 'ABC', '农业银行', 'images/bank/bank_abc.gif', '农业银行', '99bill', 4),
(5, 'balance', '余额支付', '把我的show_order设置为0吧亲', '余额支付', 'balance', 0),
(6, 'BCOM', '交通银行', 'images/bank/bank_bcom.gif', '交通银行', '99bill', 5),
(7, 'BEA', 'BEA东亚银行', 'images/bank/bank_bea.gif', 'BEA东亚银行', '99bill', 6),
(8, 'BJRCB', '北京农村商业银行', 'images/bank/bank_bjrcb.gif', '北京农村商业银行', '99bill', 7),
(9, 'BOB', '北京银行', 'images/bank/bank_bob.gif', '北京银行', '99bill', 8),
(10, 'BOC', '中国银行', 'images/bank/bank_boc.gif', '中国银行', '99bill', 9),
(11, 'CBHB', '渤海银行', 'images/bank/bank_cbhb.gif', '渤海银行', '99bill', 10),
(12, 'CCB', '中国建设银行', 'images/bank/bank_ccb.gif', '中国建设银行', '99bill', 11),
(13, 'CEB', '中国光大银行', 'images/bank/bank_ceb.gif', '中国光大银行', '99bill', 12),
(14, 'CIB', '兴业银行', 'images/bank/bank_cib.gif', '兴业银行', '99bill', 13),
(15, 'CITIC', '中信银行', 'images/bank/bank_citic.gif', '中信银行', '99bill', 14),
(16, 'CMBC', '中国民生银行', 'images/bank/bank_cmbc.gif', '中国民生银行', '99bill', 15),
(17, 'CZB', '浙商银行', 'images/bank/bank_czb.gif', '浙商银行', '99bill', 16),
(18, 'GDB', '广东发展银行', 'images/bank/bank_gdb.gif', '广东发展银行', '99bill', 17),
(19, 'GZCB', '广州市商业银行', 'images/bank/bank_gzcb.gif', '广州市商业银行', '99bill', 18),
(20, 'GZRCC', '广州市农村信用合作社', 'images/bank/bank_gzrcc.gif', '广州市农村信用合作社', '99bill', 19),
(21, 'HSB', '徽商银行', 'images/bank/bank_hsb.gif', '徽商银行', '99bill', 20),
(22, 'HXB', '华夏银行', 'images/bank/bank_hxb.gif', '华夏银行', '99bill', 21),
(23, 'HZB', '杭州银行', 'images/bank/bank_hzb.gif', '杭州银行', '99bill', 22),
(24, 'ICBC', '中国工商银行', 'images/bank/bank_icbc.gif', '中国工商银行', '99bill', 23),
(25, 'NBCB', '宁波银行', 'images/bank/bank_nbcb.gif', '宁波银行', '99bill', 24),
(26, 'NJCB', '南京银行', 'images/bank/bank_njcb.gif', '南京银行', '99bill', 25),
(27, 'PAB', '平安银行', 'images/bank/bank_pab.gif', '平安银行', '99bill', 26),
(28, 'POST', '中国邮政', 'images/bank/bank_post.gif', '中国邮政', '99bill', 27),
(29, 'SDB', '深圳发展银行', 'images/bank/bank_sdb.gif', '深圳发展银行', '99bill', 28),
(30, 'SHB', '上海银行', 'images/bank/bank_shb.gif', '上海银行', '99bill', 29),
(31, 'SHRCC', '上海农村商业银行', 'images/bank/bank_shrcc.gif', '上海农村商业银行', '99bill', 30),
(32, 'SPDB', '浦发银行', 'images/bank/bank_spdb.gif', '浦发银行', '99bill', 31);

