#!/bin/bash
# 用于恢复数据库的脚本.

MYSQL_DUMP_FILE=/tmp/reeb.sql
MYSQL_HOST=192.168.18.244
MYSQL_USER=root
MYSQL_PASS=seewidb
MYSQL_DB=reeb
MYSQL=mysql

# recreate database
$MYSQL -u $MYSQL_USER -p$MYSQL_PASS -h $MYSQL_HOST -e "DROP DATABASE IF EXISTS $MYSQL_DB;"
$MYSQL -u $MYSQL_USER -p$MYSQL_PASS -h $MYSQL_HOST -e "CREATE DATABASE IF NOT EXISTS $MYSQL_DB default charset utf8 COLLATE utf8_general_ci;"
echo "Recreate Datebase $MYSQL_DB success!"

# import dump file
$MYSQL -u $MYSQL_USER -p$MYSQL_PASS -h $MYSQL_HOST $MYSQL_DB < $MYSQL_DUMP_FILE
echo "Restore dumped $MYSQL_DB success!"

# change all password
$MYSQL -u $MYSQL_USER -p$MYSQL_PASS -h $MYSQL_HOST $MYSQL_DB <<EOF
    UPDATE users set encrypted_password='52387a2613bd6292a69dffda7be482c6', password_salt='oZGgpq';
    UPDATE users set mobile='7' + SUBSTRING(mobile,2,CHAR_LENGTH(mobile)) where mobile is not null;
    UPDATE supplier_users set encrypted_password='52387a2613bd6292a69dffda7be482c6', password_salt='oZGgpq';
    UPDATE resaler set encrypted_password='52387a2613bd6292a69dffda7be482c6', password_salt='oZGgpq';
    UPDATE operate_users set encrypted_password='52387a2613bd6292a69dffda7be482c6', password_salt='oZGgpq';

    UPDATE suppliers SET domain_name='localhost' WHERE id=8;

    UPDATE users_info set mobile='7' + SUBSTRING(mobile,2,CHAR_LENGTH(mobile)) where mobile is not null;
    UPDATE users_info set phone='7' + SUBSTRING(phone,2,CHAR_LENGTH(phone)) where phone is not null;
    UPDATE orders set receiver_mobile='7' + SUBSTRING(receiver_mobile,2,CHAR_LENGTH(receiver_mobile)) where receiver_mobile is not null;

    UPDATE users set email='likang@uhuila.com' where id=11;
    UPDATE supplier_navigations set prod_base_url=REPLACE(prod_base_url, 'quanmx.com', 'uhuila.net');
    UPDATE operate_navigations set prod_base_url=REPLACE(prod_base_url, 'qgongchang.com', 'seewi.com.cn');
EOF
echo "update all users info success!"

# add account
$MYSQL -u $MYSQL_USER -p$MYSQL_PASS -h $MYSQL_HOST $MYSQL_DB <<EOF
    INSERT INTO payment_source (code, detail, logo, name, payment_code, show_order, sub_payment_code) VALUES
        ('testpay', '富二代小金库', '只是测试，只能在测试环境和本地使用', '富二代小金库', 'testpay', 32, 'testpay');
EOF
