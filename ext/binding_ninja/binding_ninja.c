#include "binding_ninja.h"

VALUE rb_mBindingNinja;
static VALUE auto_inject_binding_invoke(int argc, VALUE *argv, VALUE self);

static VALUE
auto_inject_binding(VALUE mod, VALUE method_sym)
{
    ID mid;
    VALUE mod_name, extensions, ext_mod;

    mid = SYM2ID(method_sym);
    int arity = rb_mod_method_arity(mod, mid);
    if (abs(arity) < 1) {
      rb_raise(rb_eArgError, "target method receives 1 or more arguments");
    }

    mod_name = rb_mod_name(mod);
    extensions = rb_ivar_get(rb_mBindingNinja, rb_intern("@auto_inject_binding_extensions"));
    ext_mod = rb_hash_aref(extensions, mod_name);
    if (ext_mod == Qnil) {
      ext_mod = rb_module_new();
      rb_hash_aset(extensions, mod_name, ext_mod);
    }
    if (rb_mod_include_p(mod, ext_mod) == Qfalse) {
      rb_prepend_module(mod, ext_mod);
    }

    rb_define_method_id(ext_mod, SYM2ID(method_sym), auto_inject_binding_invoke, -1);

    return method_sym;
}

static VALUE
auto_inject_binding_invoke(int argc, VALUE *argv, VALUE self)
{
  VALUE binding, args_ary;

  binding = rb_binding_new();
  args_ary = rb_ary_new_from_values(argc, argv);
  rb_ary_unshift(args_ary, binding);

  return rb_call_super(argc + 1, RARRAY_CONST_PTR(args_ary));
}

void
Init_binding_ninja(void)
{
  rb_mBindingNinja = rb_define_module("BindingNinja");
  rb_ivar_set(rb_mBindingNinja, rb_intern("@auto_inject_binding_extensions"), rb_hash_new());
  rb_define_private_method(rb_mBindingNinja, "auto_inject_binding", auto_inject_binding, 1);
}
