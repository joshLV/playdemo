source 'http://rubygems.org'

gem 'guard'
gem 'guard-play'

group :linux do
  gem 'rb-inotify', require: nil
  gem 'libnotify', require: nil
end

group :darwin do
  gem 'rb-fsevent', require: nil
  gem 'growl_notify', require: nil
end
