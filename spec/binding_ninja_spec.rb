require "spec_helper"

RSpec.describe BindingNinja do
  class Foo
    extend BindingNinja

    def foo(b, arg1, arg2, *rest, abc: 1, **options)
      b.local_variables
    end

    def foo2(b)
      b.local_variable_set("hoge", 100)
      b
    end

    auto_inject_binding :foo
    auto_inject_binding :foo2
  end

  it "inject binding" do
    hoge = 1
    obj = Foo.new
    expect(obj.foo(1, 2, 3, 4, abc: 2, def: 10)).to match_array([:hoge, :obj])
    obj.foo2
    expect(hoge).to eq(100)
  end

  it "create extension module" do
    expect(BindingNinja.send(:instance_variable_get, "@auto_inject_binding_extensions")["Foo"]).to be_a(Module)
  end
end
