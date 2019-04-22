require "bundler/gem_tasks"
require "rspec/core/rake_task"

RSpec::Core::RakeTask.new(:spec)

task :build => :compile

case RUBY_PLATFORM
when /java/
  require 'rake/javaextensiontask'
  Rake::JavaExtensionTask.new('binding_ninja') do |ext|
    ext.lib_dir = "lib/binding_ninja"
  end
else
  require 'rake/extensiontask'
  Rake::ExtensionTask.new("binding_ninja") do |ext|
    ext.lib_dir = "lib/binding_ninja"
  end
end

task :default => [:clobber, :compile, :spec]
