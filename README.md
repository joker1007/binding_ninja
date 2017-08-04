# BindingNinja

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
end

Foo.new.foo(1, 2) 
# => <Binding of toplevel>
# => 1
# => 2
```

## Development

After checking out the repo, run `bin/setup` to install dependencies. Then, run `rake spec` to run the tests. You can also run `bin/console` for an interactive prompt that will allow you to experiment.

To install this gem onto your local machine, run `bundle exec rake install`. To release a new version, update the version number in `version.rb`, and then run `bundle exec rake release`, which will create a git tag for the version, push git commits and tags, and push the `.gem` file to [rubygems.org](https://rubygems.org).

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/joker1007/binding_ninja.
