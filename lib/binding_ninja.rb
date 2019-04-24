require "binding_ninja/version"

if /java/ =~ RUBY_PLATFORM
  require_relative 'binding_ninja/binding_ninja.jar'
  Java::IoGithubJoker1007::BindingNinjaService.new.basicLoad(JRuby.runtime)
else
  require "binding_ninja/binding_ninja"
end

module BindingNinja
  def auto_inject_binding_options
    {}
  end

  METHOD_DEFINER = ->(klass) do
    unless klass.method_defined?(:auto_inject_binding_options)
      options = {}
      klass.class_eval do
        @auto_inject_binding_options = options
      end

      klass.define_singleton_method(:auto_inject_binding_options) do
        super().merge(options)
      end
    end
  end

  def inherited(klass)
    super
    METHOD_DEFINER.call(klass)
  end

  def included(klass)
    super
    METHOD_DEFINER.call(klass)
  end

  def self.extended(klass)
    METHOD_DEFINER.call(klass)
  end
end
