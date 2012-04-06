# More info at https://github.com/guard/guard#readme
interactor :readline

case RbConfig::CONFIG['host_os'].downcase
when /linux/
  # notification :libnotify
  notification :notifysend, :t => 10000
  # notification :gntp
when /darwin/
  notification :growl_notify
end

guard 'play', app_path: "website/www" do
  watch(%r{^website/www/app/})
  watch(%r{^website/www/conf/})
  watch(%r{^website/www/test/})
  watch(%r{^module/})
end

guard 'play', app_path: "website/home" do
  watch(%r{^website/home/app/})
  watch(%r{^website/home/conf/})
  watch(%r{^website/home/test/})
  watch(%r{^module/})
end

guard 'play', app_path: "traders/order" do
  watch(%r{^traders/order/app/})
  watch(%r{^traders/order/conf/})
  watch(%r{^traders/order/test/})
  watch(%r{^module/})
end

guard 'play', app_path: "traders/sales" do
  watch(%r{^traders/sales/app/})
  watch(%r{^traders/sales/conf/})
  watch(%r{^traders/sales/test/})
  watch(%r{^module/})
end

guard 'play', app_path: "traders/admin" do
  watch(%r{^traders/admin/app/})
  watch(%r{^traders/admin/conf/})
  watch(%r{^traders/admin/test/})
  watch(%r{^module/})
end

guard 'play', app_path: "traders/home" do
  watch(%r{^traders/home/app/})
  watch(%r{^traders/home/conf/})
  watch(%r{^traders/home/test/})
  watch(%r{^module/})
end

guard 'play', app_path: "cdn/image-server" do
  watch(%r{^cdn/image-server/app/})
  watch(%r{^cdn/image-server/conf/})
  watch(%r{^cdn/image-server/test/})
  watch(%r{^module/})
end

guard 'play', app_path: "operate/admin" do
  watch(%r{^operate/admin/app/})
  watch(%r{^operate/admin/conf/})
  watch(%r{^operate/admin/test/})
  watch(%r{^module/})
end


guard 'play', app_path: "operate/business" do
  watch(%r{^operate/business/app/})
  watch(%r{^operate/business/conf/})
  watch(%r{^operate/business/test/})
  watch(%r{^module/})
end


guard 'play', app_path: "resale/home" do
  watch(%r{^resale/home/app/})
  watch(%r{^resale/home/conf/})
  watch(%r{^resale/home/test/})
  watch(%r{^module/})
end


guard 'play', app_path: "cdn/image-server" do
  watch(%r{^cdn/image-server/app/})
  watch(%r{^cdn/image-server/conf/})
  watch(%r{^cdn/image-server/test/})
  watch(%r{^module/})
end

