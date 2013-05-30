require 'sequel'
require 'date'

DB = Sequel.connect('mysql2://root:seewidb@192.168.100.161:3306/reeb')

def find_day_range(start_day, end_day, day_of_weeks)
  idate = start_day
  ibegin = start_day
  iend = start_day
  day_range = Array.new
  is_first = true
  while idate <= end_day do
    wday = idate.wday == 0 ? 7 : idate.wday
    if day_of_weeks.include? (wday)
#      puts "   #{idate}, wday=#{wday}  #{day_of_weeks}"
      if is_first
        ibegin = idate
        is_first = false
      end
      iend = idate
      if idate == end_day
#        puts "   last : #{ibegin}, #{idate}"
        day_range << [ibegin, idate]
      end
    else
#      puts "  idate.wday=#{wday} #{idate}"
      unless is_first
        day_range << [ibegin, iend]
      end
      ibegin = idate
      is_first = true
    end

    idate = idate + 1
  end
  day_range
end

DB['select * from ktv_price_schedules'].each do |row|
#DB['select * from ktv_price_schedules where id=99'].each do |row|
  id = row[:id]
  day_of_weeks = row[:day_of_weeks].split(',').collect{|i| i.to_i }
  start_day = row[:start_day].to_date
  end_day = row[:end_day].to_date

  puts "-- row:  #{id}: #{start_day} - #{end_day}  #{day_of_weeks}"

  day_range = find_day_range(start_day, end_day, day_of_weeks)
  day_range.each do |pair_day|
    puts "insert into ktv_date_range_price_schedules (start_day, end_day, schedule_id) values ('#{pair_day[0]}', '#{pair_day[1]}', #{id});"
  end

end
