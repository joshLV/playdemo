# encoding:utf-8
sql_file = File.new('area.sql','w')
i=0
File.open('area.txt','r').each_line do |line|
  districts= line.split(/：/)
  i+=1
  a=i>9?"":"0"
  id = "021#{a}#{i}"
  sql_file.puts "insert into areas (id,area_type,display_order,name,parent_id) values('#{id}','DISTRICT',#{i*100},'#{districts[0]}','021');"
  areas = districts[1].split("|")
  j=0
  areas.each do |area|
      j+=1
      b=j>9?"":"00"
      area_id="#{id}#{b}#{j}"
 
      sql_file.puts "insert into areas (id,area_type,display_order,name,parent_id) values ('#{area_id}','AREA',#{i*1000+j*10},'#{area.strip}','#{id}');"
  end
end
