#!/usr/bin/env ruby

require 'rubygems'
require 'data_mapper' # requires all the gems listed above

# If you want the logs displayed you have to do this before the call to setup
DataMapper::Logger.new($stdout, :debug)

DataMapper.setup(:default, 'mysql://root:seewidb@192.168.18.244/scott')
DataMapper.setup(:target, 'mysql://root:seewidb@192.168.18.244/reeb')

# Uhuila
class UhlUser
  include DataMapper::Resource

  storage_names[:target] = 'uhl_user'
  storage_names[:default] = 'uhl_user'

  property :user_id, Serial
  property :user_email, String
  property :user_tel, String
  property :user_pws, String
  property :trade_pws, String
  property :user_rand, String
  property :reg_ip, String
  property :group_id, Integer
  property :area_id, Integer
  property :is_verify_email, Integer
  property :is_verify_tel, Integer
  property :verify_rand, String
  property :verify_time, Integer
  property :reg_time, Integer
  property :user_state, Integer
  property :login_time, Decimal
  property :is_del, Integer
  property :card_status, Integer
  property :error_time, DateTime
end


class UhlUserPoint
  include DataMapper::Resource

  storage_names[:target] = 'uhl_user_points'
  storage_names[:default] = 'uhl_user_points'

  propertty :points_id, Serial
  propertty :user_id, Integer
  propertty :points_type, Integer
  propertty :points, Integer
  propertty :trade_type, Integer
  propertty :trade_id, Decimal
  propertty :record_time, Integer
  propertty :is_del, Integer
  propertty :cur_points, Integer
end


class UhlUserInfo
  include DataMapper::Resource

  storage_names[:target] = 'uhl_user_info'
  storage_names[:default] = 'uhl_user_info'

  property :user_id, Integer
  property :rname, String
  property :sex, Integer
  property :qq, String
  property :marr_state, Integer
  property :card_sn, String
  property :bday, String
  property :phone, String
  property :trade, Integer
  property :position, Integer
  property :salary, Integer
  property :interest, String
  property :itother, String
  property :amount, String
  property :amount_buy, Decimal
  property :amount_add, Decimal
  property :amount_rd, Decimal
  property :amount_cou, Decimal
  property :amount_rebate, Decimal
  property :amount_buytuan, Decimal
  property :amount_buyshop, Decimal
  property :point_total, Integer
  property :point, Integer
  property :point_buy, Integer
  property :login_num, Integer
  property :cou_total, Integer
  property :is_del, Integer
  property :lock_amount, Decimal
end

#Reeb
class ReebUser
  include DataMapper =:Resource

  storage_names[:target] = 'users'
  storage_names[:default] = 'users'

  property :id, Serial
  property :created_at, DateTime
  property :last_login_at, DateTime
  property :login_ip, String
  property :email, String
  property :mobile, String
  property :openid_source, String
  property :encrypted_password, String
  property :password_salt, String
  property :status, String
  property :send_mail_at, DateTime
  property :password_token, String
end

class ReebUserInfo
  include DataMapper::Resource

  storage_names[:target] = 'users_info'
  storage_names[:default] = 'users_info'

  property :id, Serial
  propertty :birthday, String
  propertty :intrest, String
  propertty :mobile, String
  propertty :phone, String
  propertty :position, String
  propertty :salary, String
  propertty :user_sex, Integer
  propertty :created_at, DateTime
  propertty :userqq, String
  propertty :industry, String
  propertty :full_name, String
  propertty :user_id, Integer
  propertty :user_name, String
  propertty :bindMobile_at, DateTime
  propertty :otherInfo, String
  propertty :interest, String
  propertty :total_points, Integer
  propertty :marryState, Integer
  propertty :marrState, Integer
end

class ReebUserPoint
  include DataMapper::Resource

  storage_names[:target] = 'user_points'
  storage_names[:default] = 'user_points'

  propertty :id, Serial
  propertty :created_at, DateTime
  propertty :current_points, Integer
  propertty :deal_points, Integer
  propertty :deal_type, String
  propertty :order_id, Integer
  propertty :user_id, Integer
  propertty :point_number, String
end

UhlUser.all.each do |u|
  puts "id=#{u.user_id}"
  DataMapper.repository(:target) {
    puts "u.id=#{u.user_id}"
    ru = ReebUser.get(u.user_id)
    if ru.nil?
      ru = ReebUser.new
    end
    ru.id = u.user_id
    ru.email = u.user_email
    ru.mobile = u.user_tel
    ru.password_salt = u.user_rand
    ru.encrypted_password = u.user_pws
    ru.login_ip = u.reg_ip
    if u.reg_time
      ru.created_at = Time.at(u.reg_time)
    else
      ru.created_at = nil
    end
    ru.last_login_at = Time.at(u.login_time) if u.login_time
    ru.status = (!u.nil? && u.user_state == 1) ? 'NORMAL' : 'FREEZE'
    ru.save
  }
end
