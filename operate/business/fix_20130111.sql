SELECT MAX( id ) 
FROM  `account_sequence`;

INSERT INTO `reeb`.`account_sequence` (
`created_at` ,
`order_id` ,
`remark` ,
`sequence_flag` ,
`serial_number` ,
`trade_id` ,
`account_id` ,
`balance` ,
`cash_balance` ,
`change_amount` ,
`trade_type` ,
`uncash_balance` ,
`promotion_balance` ,
`promotion_change_amount` ,
`settlement_status` ,
`withdrawBill_id` ,
`prepayment_id` ,
`comment` ,
`operated_by`)
VALUES ('2012-05-28 10:50:50', '40', '充值', 'NOSTRO', '2012052810505098384680', '74', '48181', '-88.00', '-88.00', '-88.00', 'CHARGE', '0.00', '0.00', '0.00', 'UNCLEARED', NULL , NULL , NULL , NULL);

INSERT INTO  `reeb`.`account_sequence` (
`created_at` ,
`order_id` ,
`remark` ,
`sequence_flag` ,
`serial_number` ,
`trade_id` ,
`account_id` ,
`balance` ,
`cash_balance` ,
`change_amount` ,
`trade_type` ,
`uncash_balance` ,
`promotion_balance` ,
`promotion_change_amount` ,
`settlement_status` ,
`withdrawBill_id` ,
`prepayment_id` ,
`comment` ,
`operated_by`
)
VALUES (
'77215',  '2012-06-06 20:51:26',  '55',  '充值',  'NOSTRO',  '2012060608512634189722',  '77',  '48181',  '-212.40',  '-212.40',  '-124.40',  'CHARGE',  '0.00', NULL , NULL ,  'UNCLEARED', NULL , NULL , NULL , NULL
);

INSERT INTO  `reeb`.`account_sequence` (
`created_at` ,
`order_id` ,
`remark` ,
`sequence_flag` ,
`serial_number` ,
`trade_id` ,
`account_id` ,
`balance` ,
`cash_balance` ,
`change_amount` ,
`trade_type` ,
`uncash_balance` ,
`promotion_balance` ,
`promotion_change_amount` ,
`settlement_status` ,
`withdrawBill_id` ,
`prepayment_id` ,
`comment` ,
`operated_by`
)
VALUES (
'2012-06-10 15:37:17',  '62',  '充值',  'NOSTRO',  '2012061003371782103625',  '83',  '48181',  '-292.40',  '-292.40',  '-80.00',  'CHARGE',  '0.00', NULL , NULL ,  'UNCLEARED', NULL , NULL , NULL , NULL
);


//  withdrawBill_id字段改名:新版上了之后执行。
update account_sequence set withdraw_bill_id=withdrawBill_id where withdrawBill is not null;

// 补填红玫瑰结算的withdrawbill_id到trade_bill.
update trade_bill set withdraw_bill_id=118 where id=38166 or id=38167;
