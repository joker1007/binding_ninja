# BindingNinja
[![Gem Version](https://badge.fury.io/rb/binding_ninja.svg)](https://badge.fury.io/rb/binding_ninja)
[![Build Status](https://travis-ci.org/joker1007/binding_ninja.svg?branch=master)](https://travis-ci.org/joker1007/binding_ninja)

This is method wrapper for passing binding of method caller implcitly.
And this is lightweight alternative of [binding_of_caller](https://github.com/banister/binding_of_caller)

## Installation

Add this line to your application's Gemfile:

```ruby
gem 'binding_ninja'
```

And then execute:

    $ bundle

Or install it yourself as:

    $ gem install binding_ninja

## Usage

```ruby
class Foo
  extend BindingNinja

  def foo(binding, arg1, arg2)
    p binding
    p arg1
    p arg2
  end
  auto_inject_binding :foo

  def foo2(binding, arg1, arg2)
    p binding
    p arg1
    p arg2
  end
  auto_inject_binding :foo2, if: ENV["ENABLE_BINDING_NINJA"]
  # or
  auto_inject_binding :foo2, if: ->(obj) { obj.enable_auto_inject_binding? }
  # or
  auto_inject_binding :foo2, if: :enable_auto_inject_binding?

  def enable_auto_inject_binding?
    true
  end
end

Foo.new.foo(1, 2) 
# => <Binding of toplevel>
# => 1
# => 2

# if ENABLE_BINDING_NINJA environment variable is nil or false,
# binding arguments is nil.
Foo.new.foo2(1, 2) 
# => nil
# => 1
# => 2
```

`:if` option can accept Proc object and Symbol object.
If option accepts a proc or symbol, uses result of evaluating the proc or method named by the symbol.

## Compare to binding_of_caller
```ruby
require "benchmark/ips"
require "binding_ninja"
require "binding_of_caller"

class Foo
  extend BindingNinja

  auto_inject_binding def foo1(b)
    b.local_variables
  end

  def foo2
    binding.of_caller(1).local_variables
  end

  def foo3(b)
    b.local_variables
  end
  auto_inject_binding :foo3, if: :enable_auto_inject_binding?

  def enable_auto_inject_binding?
    true
  end
end

foo = Foo.new

p foo.foo1
p foo.foo2
p foo.foo3

Benchmark.ips do |x|
  x.report("binding_ninja") { foo.foo1 }
  x.report("binding_ninja_with_condition") { foo.foo3 }
  x.report("binding_of_caller") { foo.foo2 }

  x.compare!
end
```

```
Warming up --------------------------------------
       binding_ninja   106.598k i/100ms
binding_ninja_with_condition
                        49.660k i/100ms
   binding_of_caller     6.799k i/100ms
Calculating -------------------------------------
       binding_ninja      1.351M (± 0.4%) i/s -      6.822M in   5.051283s
binding_ninja_with_condition
                        566.555k (± 0.3%) i/s -      2.880M in   5.083895s
   binding_of_caller     69.968k (± 0.8%) i/s -    353.548k in   5.053337s

Comparison:
       binding_ninja:  1350619.1 i/s
binding_ninja_with_condition:   566555.1 i/s - 2.38x  slower
   binding_of_caller:    69968.2 i/s - 19.30x  slower

```

13x - 16x faster than binding_of_caller.
And binding_ninja has very simple code base.

## Development

After checking out the repo, run `bin/setup` to install dependencies. Then, run `rake spec` to run the tests. You can also run `bin/console` for an interactive prompt that will allow you to experiment.

To install this gem onto your local machine, run `bundle exec rake install`. To release a new version, update the version number in `version.rb`, and then run `bundle exec rake release`, which will create a git tag for the version, push git commits and tags, and push the `.gem` file to [rubygems.org](https://rubygems.org).

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/joker1007/binding_ninja.

## License

The gem is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).
