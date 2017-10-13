#include "binding_ninja.h"

VALUE rb_mBindingNinja;
static VALUE auto_inject_binding_invoke(int argc, VALUE *argv, VALUE self);
static VALUE auto_inject_binding_invoke_without_cond(int argc, VALUE *argv, VALUE self);
static VALUE auto_inject_binding_invoke_stub(int argc, VALUE *argv, VALUE self);

static ID options_id;

static VALUE
auto_inject_binding(int argc, VALUE *argv, VALUE mod)
{
    ID mid;
    static ID extensions_id;
    static ID keyword_ids[1];
    VALUE extensions, ext_mod, method_sym, options, opt, cond;

    cond = Qundef;

    if (!keyword_ids[0]) {
      keyword_ids[0] = rb_intern("if");
    }

    if (!extensions_id) {
      extensions_id = rb_intern("@auto_inject_binding_extensions");
    }

    if (rb_ivar_defined(mod, options_id)) {
      options = rb_ivar_get(mod, options_id);
    } else {
      options = rb_hash_new();
      rb_ivar_set(mod, options_id, options);
    }

    rb_scan_args(argc, argv, "1:", &method_sym, &opt);
    if (!NIL_P(opt)) {
      rb_get_kwargs(opt, keyword_ids, 0, 1, &cond);
      if (cond != Qundef) {
        rb_hash_aset(options, method_sym, cond);
      }
    }

    mid = SYM2ID(method_sym);
    if (abs(rb_mod_method_arity(mod, mid)) < 1) {
      rb_raise(rb_eArgError, "target method receives 1 or more arguments");
    }

    extensions = rb_ivar_get(rb_mBindingNinja, extensions_id);
    ext_mod = rb_hash_aref(extensions, mod);
    if (NIL_P(ext_mod)) {
      ext_mod = rb_module_new();
      rb_hash_aset(extensions, mod, ext_mod);
    }

    if (rb_mod_include_p(mod, ext_mod) == Qfalse) {
      rb_prepend_module(mod, ext_mod);
    }

    if (cond == Qundef) {
      rb_define_method_id(ext_mod, mid, auto_inject_binding_invoke_without_cond, -1);
    } else if (rb_obj_is_proc(cond) || SYMBOL_P(cond)) {
      rb_define_method_id(ext_mod, mid, auto_inject_binding_invoke, -1);
    } else {
      if (RTEST(cond)) {
        rb_define_method_id(ext_mod, mid, auto_inject_binding_invoke_without_cond, -1);
      } else {
        rb_define_method_id(ext_mod, mid, auto_inject_binding_invoke_stub, -1);
      }
    }

    return method_sym;
}

static VALUE
auto_inject_binding_invoke(int argc, VALUE *argv, VALUE self)
{
  VALUE method_sym, ext_mod, options, binding, args_ary, cond;
  static VALUE dummy_proc_args, dummy_method_arg[0];

  if (!dummy_proc_args) {
    dummy_proc_args = rb_ary_new();
    rb_obj_freeze(dummy_proc_args);
  }

  method_sym = ID2SYM(rb_frame_this_func());
  options = rb_funcall(CLASS_OF(self), rb_intern("auto_inject_binding_options"), 0);

  cond = rb_hash_lookup2(options, method_sym, Qtrue);

  if (rb_obj_is_proc(cond)) {
    if (abs(rb_proc_arity(cond)) > 0) {
      cond = rb_proc_call(cond, rb_ary_new_from_args(1, self));
    } else {
      cond = rb_proc_call(cond, dummy_proc_args);
    }
  } else if (SYMBOL_P(cond)) {
    cond = rb_method_call(0, dummy_method_arg, rb_obj_method(self, cond));
  }

  args_ary = rb_ary_new_from_values(argc, argv);
  if (RTEST(cond)) {
    binding = rb_binding_new();
    rb_ary_unshift(args_ary, binding);
  } else {
    rb_ary_unshift(args_ary, Qnil);
  }
  return rb_call_super(argc + 1, RARRAY_CONST_PTR(args_ary));
}


static VALUE
auto_inject_binding_invoke_without_cond(int argc, VALUE *argv, VALUE self)
{
  VALUE args_ary, binding;
  args_ary = rb_ary_new_from_values(argc, argv);
  binding = rb_binding_new();
  rb_ary_unshift(args_ary, binding);
  return rb_call_super(argc + 1, RARRAY_CONST_PTR(args_ary));
}

static VALUE
auto_inject_binding_invoke_stub(int argc, VALUE *argv, VALUE self)
{
  VALUE args_ary;
  args_ary = rb_ary_new_from_values(argc, argv);
  rb_ary_unshift(args_ary, Qnil);
  return rb_call_super(argc + 1, RARRAY_CONST_PTR(args_ary));
}

void
Init_binding_ninja(void)
{
  rb_mBindingNinja = rb_define_module("BindingNinja");
  options_id = rb_intern("@auto_inject_binding_options");
  rb_ivar_set(rb_mBindingNinja, rb_intern("@auto_inject_binding_extensions"), rb_hash_new());
  rb_define_private_method(rb_mBindingNinja, "auto_inject_binding", auto_inject_binding, -1);
}
