# More info at https://github.com/guard/guard#readme
interactor :readline

notification :notifysend

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
