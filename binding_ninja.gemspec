# coding: utf-8
lib = File.expand_path("../lib", __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require "binding_ninja/version"

Gem::Specification.new do |spec|
  spec.name          = "binding_ninja"
  spec.version       = BindingNinja::VERSION
  spec.authors       = ["joker1007"]
  spec.email         = ["kakyoin.hierophant@gmail.com"]

  spec.summary       = %q{pass binding of method caller implicitly}
  spec.description   = %q{pass binding of method caller implicitly}
  spec.homepage      = "https://github.com/joker1007/binding_ninja"
  spec.license       = "MIT"

  spec.files         = `git ls-files -z`.split("\x0").reject do |f|
    f.match(%r{^(test|spec|features)/})
  end
  spec.bindir        = "exe"
  spec.executables   = spec.files.grep(%r{^exe/}) { |f| File.basename(f) }
  spec.require_paths = ["lib"]

  case RUBY_PLATFORM
  when /java/
    spec.platform = "java"
    spec.files << "lib/binding_ninja/binding_ninja.jar"
  else
    spec.extensions = ["ext/binding_ninja/extconf.rb"]
  end

  spec.add_development_dependency "bundler", ">= 1.15"
  spec.add_development_dependency "rake", "~> 10.0"
  spec.add_development_dependency "rake-compiler"
  spec.add_development_dependency "rspec", "~> 3.0"
end
