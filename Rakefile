require "bundler/gem_tasks"
require "rspec/core/rake_task"

RSpec::Core::RakeTask.new(:spec)

require "rake/extensiontask"

task :build => :compile

Rake::ExtensionTask.new("binding_ninja") do |ext|
  ext.lib_dir = "lib/binding_ninja"
end

task :default => [:clobber, :compile, :spec]
