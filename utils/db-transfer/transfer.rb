#!/usr/bin/env ruby

require 'rubygems'
require 'openssl'
require 'base64'
require 'data_mapper' # requires all the gems listed above

# If you want the logs displayed you have to do this before the call to setup
# DataMapper::Logger.new($stdout, :debug)

DataMapper.setup(:default, 'mysql://root:seewidb@192.168.18.244/scott')
DataMapper.setup(:target, 'mysql://root:seewidb@192.168.18.244/sales')

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

  property :points_id, Serial
  property :user_id, Integer
  property :points_type, Integer
  property :points, Integer
  property :trade_type, Integer
  property :trade_id, Decimal
  property :record_time, Integer
  property :is_del, Integer
  property :cur_points, Integer
end


class UhlUserInfo
  include DataMapper::Resource

  storage_names[:default] = 'uhl_user_info'

  property :user_id, Serial
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
  include DataMapper::Resource

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
  property :birthday, String
  property :intrest, String
  property :mobile, String
  property :phone, String
  property :position, String
  property :salary, String
  property :user_sex, Integer
  property :created_at, DateTime
  property :userqq, String
  property :industry, String
  property :full_name, String
  property :user_id, Integer
  property :user_name, String
  #property :otherInfo, String
  property :interest, String
  property :total_points, Integer
  property :marry_state, Integer
end


class ReebUserPoint
  include DataMapper::Resource

  storage_names[:target] = 'user_points'
  storage_names[:default] = 'user_points'

  property :id, Serial
  property :created_at, DateTime
  property :current_points, Integer
  property :deal_points, Integer
  property :deal_type, String
  property :order_id, Integer
  property :user_id, Integer
  property :point_number, String
end

def transfer_users
  UhlUser.all.each do |u|
    puts "transfer_users: user.id=#{u.user_id}"
    DataMapper.repository(:target) {
      ru = ReebUser.get(u.user_id)
      if ru.nil?
        ru = ReebUser.new
        ru.id = u.user_id
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
end

def transfer_user_info
  UhlUserInfo.all.each do |u|
    DataMapper.repository(:target) {
      puts "u.user_id=#{u.user_id}"
      ru = ReebUserInfo.get(u.user_id)
      user = ReebUser.get(u.user_id)
      unless user.nil?
        if ru.nil?
          ru = ReebUserInfo.new
          ru.id = u.user_id
        end
        ru.birthday = u.bday
        ru.intrest = u.interest
        ru.phone = u.phone
        ru.position = u.position
        ru.salary = u.salary
        ru.user_sex = u.sex
        ru.created_at = user.nil? ? nil : user.created_at
        ru.userqq = u.qq
        ru.industry = u.trade
        ru.full_name = u.rname
        ru.user_id = u.user_id
        #ru.otherInfo = u.itother
        ru.interest = u.interest
        ru.total_points = u.point
        ru.marry_state = u.marr_state

        ru.save
      end
    }
  end
end

def des_decrypt(encrypt_value)
  puts "encrypt_value=#{encrypt_value}"
  bytes = Base64.decode64(encrypt_value)
  c = OpenSSL::Cipher::Cipher.new("des")
  c.decrypt
  c.key = "seewi123"
  c.update(bytes)
  c.final
end


class ReebAccount
  include DataMapper::Resource

  storage_names[:target] = 'accounts'
  storage_names[:default] = 'accounts'

  property :id, Serial
  property :account_type, String
  property :amount, Decimal
  property :created_at, DateTime
  property :status, String
  property :uid, Integer
  property :uncash_amount, Decimal
end

def transfer_user_amount
  UhlUserInfo.all.each do |u|
    DataMapper.repository(:target) {
      puts "u.user_id=#{u.user_id}"
      ru = ReebAccount.first(uid: u.user_id)
      user = ReebUser.get(u.user_id)
      unless user.nil?
        if ru.nil?
          ru = ReebAccount.new
          ru.uid = u.user_id
        end
        ru.status = "NORMAL"
        ru.account_type = "CONSUMER"
        if !u.amount.nil? && u.amount.length < 20
          ru.amount = des_decrypt(u.amount)
        else
          ru.amount = 0
        end
        ru.created_at = Time.now
        ru.uncash_amount = u.lock_amount
        ru.save
      end
    }
  end
end

class UhlUserAddress
  include DataMapper::Resource

  storage_names[:target] = 'uhl_user_addr'
  storage_names[:default] = 'uhl_user_addr'

  property :addr_id, Serial
  property :user_id, Integer
  property :addr_name, String
  property :consignee, String
  property :email, String
  property :province, String
  property :city, String
  property :address, String
  property :zip, String
  property :tel, String
  property :is_buy, Integer
  property :phone, String
  property :county, String
  property :is_del, Integer
end
class ReebUserAddress
  include DataMapper::Resource

  storage_names[:target] = 'address'
  storage_names[:default] = 'address'

  property :id, Serial
  property :address, String
  property :area_code, String
  property :city, String
  property :created_at, DateTime
  property :district, String
  property :is_default, String
  property :lock_version, Integer
  property :mobile, String
  property :name, String
  property :phone_ext_number, String
  property :phone_number, String
  property :postcode, String
  property :province, String
  property :updated_at, DateTime
  property :user_id, Integer
end

def transfer_user_address
  UhlUserAddress.all.each do |u|
    puts "addr_id=#{u.addr_id}"
    DataMapper.repository(:target) {
      ru = ReebUserAddress.first(user_id: u.user_id)
      user = ReebUser.get(u.user_id)
      unless user.nil?
        if ru.nil?
          ru = ReebUserAddress.new
          ru.user_id = u.user_id
        end
        ru.address = u.address
        ru.city = u.city
        ru.district = u.county
        ru.created_at = user.created_at
        ru.is_default = u.is_buy
        ru.mobile = u.tel
        ru.name = u.consignee
        ru.lock_version = 0
        ru.phone_number = u.phone
        ru.postcode = u.zip
        ru.province = u.province
        ru.save
      end
    }
  end
end

def transfer_user_point
  UhlUserPoint.all.each do |u|
    DataMapper.repository(:target) {
      puts "u.point_id=#{u.points_id}"
      ru = ReebUserPoint.get(u.points_id)
      user = ReebUser.get(u.user_id)
      unless user.nil?
        if ru.nil?
          ru = ReebUserPoint.new
          ru.id = u.points_id
        end
        ru.user_id = u.user_id
        ru.current_points = u.cur_points
        ru.deal_points = u.points
        ru.deal_type = u.trade_type
        #ru.order_id = u.trade_id
        ru.point_number = u.points_type
        ru.save
      end
    }
  end
end

transfer_users
transfer_user_info
transfer_user_point
transfer_user_amount
transfer_user_address
