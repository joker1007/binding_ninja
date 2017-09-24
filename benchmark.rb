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
