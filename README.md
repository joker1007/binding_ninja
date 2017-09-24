# BindingNinja
[![Build Status](https://travis-ci.org/joker1007/binding_ninja.svg?branch=master)](https://travis-ci.org/joker1007/binding_ninja)

This is method wrapper for passing binding of method caller implcitly.

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

## Development

After checking out the repo, run `bin/setup` to install dependencies. Then, run `rake spec` to run the tests. You can also run `bin/console` for an interactive prompt that will allow you to experiment.

To install this gem onto your local machine, run `bundle exec rake install`. To release a new version, update the version number in `version.rb`, and then run `bundle exec rake release`, which will create a git tag for the version, push git commits and tags, and push the `.gem` file to [rubygems.org](https://rubygems.org).

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/joker1007/binding_ninja.
