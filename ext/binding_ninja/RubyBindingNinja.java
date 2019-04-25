package io.github.joker1007;

import java.util.stream.Stream;
import org.jruby.anno.JRubyMethod;
import org.jruby.anno.JRubyModule;
import org.jruby.ast.util.ArgsUtil;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.Ruby;
import org.jruby.RubyBasicObject;
import org.jruby.RubyBinding;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyMethod;
import org.jruby.RubyModule;
import org.jruby.RubyProc;
import org.jruby.RubySymbol;
import org.jruby.runtime.Block;
import org.jruby.runtime.Constants;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;

@JRubyModule(name = "BindingNinja")
public class RubyBindingNinja {
  static String OPTIONS_ID = "@auto_inject_binding_options";
  static String EXTENSIONS_ID = "@auto_inject_binding_extensions";
  static Integer[] jrubyVersionNums = Stream.of(Constants.VERSION.split("\\.")).map(Integer::parseInt).toArray(Integer[]::new);

  @JRubyMethod(name = "auto_inject_binding", module = true, visibility = Visibility.PRIVATE, required = 1, optional = 1)
  public static IRubyObject autoInjectBinding(ThreadContext context, IRubyObject recv, IRubyObject[] args) {
    final Ruby runtime = context.getRuntime();

    final IRubyObject extensions = runtime.getModule("BindingNinja").getInstanceVariable(EXTENSIONS_ID);
    final IRubyObject methodSym = args[0];

    IRubyObject ivar = ((RubyModule)recv).getInstanceVariable(OPTIONS_ID);
    final IRubyObject options;
    if (ivar.isTrue()) {
      options = ivar;
    } else {
      options = RubyHash.newHash(runtime);
      ((RubyModule)recv).setInstanceVariable(OPTIONS_ID, options);
    }

    IRubyObject extModTmp =
      extensions instanceof RubyHash ?
        ((RubyHash)extensions).op_aref(context, recv) :
        context.nil;
    if (extModTmp.isNil()) {
      extModTmp = RubyModule.newModule(runtime);
      ((RubyHash)extensions).op_aset(context, recv, extModTmp);
    }
    final RubyModule extMod = (RubyModule)extModTmp;
    if (!((RubyModule)recv).hasModuleInHierarchy(extMod)) {
      ((RubyModule)recv).prependModule(extMod);
    }

    IRubyObject cond = RubyBasicObject.UNDEF;
    if (args.length == 2) {
      final RubyHash kwArgs = args[1].convertToHash();
      final IRubyObject[] rets = ArgsUtil.extractKeywordArgs(runtime.getCurrentContext(), kwArgs, "if");
      cond = rets[0];
    }

    if (cond != RubyBasicObject.UNDEF) {
      ((RubyHash)options).op_aset(context, methodSym, cond);
    }

    final String mid = methodSym.asJavaString();
    if (cond == RubyBasicObject.UNDEF) {
      extMod.addMethod(mid, autoInjectBindingInvokeWithoutCond(extMod, mid));
    } else {
      final RubyClass klass = cond.getMetaClass().getRealClass();
      if (klass == runtime.getProc() || klass == runtime.getSymbol()) {
        extMod.addMethod(mid, autoInjectBindingInvoke(extMod, mid));
      } else if (cond.isTrue()) {
        extMod.addMethod(mid, autoInjectBindingInvokeWithoutCond(extMod, mid));
      } else {
        extMod.addMethod(mid, autoInjectBindingInvokeStub(extMod, mid));
      }
    }

    return methodSym;
  }

  private static JavaMethod autoInjectBindingInvoke(final RubyModule extMod, String name) {
    return new JavaMethod(extMod, Visibility.PUBLIC, name) {
      @Override
      public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
        final IRubyObject options = Helpers.invoke(context, self.getMetaClass(), "auto_inject_binding_options");
        IRubyObject cond = ((org.jruby.RubyHash)options).op_aref(context, RubySymbol.newSymbol(context.getRuntime(), name));

        final RubyClass klass = cond.getMetaClass().getRealClass();
        if (klass == context.getRuntime().getProc()) {
          if (Math.abs(((RubyProc)cond).arity().getIntValue()) > 0) {
            cond = ((RubyProc)cond).call(context, new IRubyObject[]{self});
          } else {
            cond = ((RubyProc)cond).call(context, new IRubyObject[]{});
          }
        } else if (klass == context.getRuntime().getSymbol()) {
          final IRubyObject method = ((RubyBasicObject)self).method(cond);
          final IRubyObject proc = ((RubyMethod) method).to_proc(context);
          cond = ((RubyProc)proc).call(context, new IRubyObject[]{});
        }

        final IRubyObject[] unshiftedArgs = new IRubyObject[args.length + 1];
        if (cond.isTrue()) {
          unshiftedArgs[0] = RubyBinding.newBinding(context.getRuntime(), context.currentBinding());
        } else {
          unshiftedArgs[0] = context.nil;
        }
        System.arraycopy(args, 0, unshiftedArgs, 1, args.length);
        if (jrubyVersionNums[0] >= 9 && jrubyVersionNums[1] >= 2 && jrubyVersionNums[2] >= 7) {
          return Helpers.invokeSuper(context, self, clazz, name, unshiftedArgs, block);
        } else {
          return Helpers.invokeSuper(context, self, extMod, name, unshiftedArgs, block);
        }
      }
    };
  }

  private static JavaMethod autoInjectBindingInvokeStub(final RubyModule extMod, String name) {
    return new JavaMethod(extMod, Visibility.PUBLIC, name) {
      @Override
      public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
        final IRubyObject[] unshiftedArgs = new IRubyObject[args.length + 1];
        unshiftedArgs[0] = context.nil;
        System.arraycopy(args, 0, unshiftedArgs, 1, args.length);
        if (jrubyVersionNums[0] >= 9 && jrubyVersionNums[1] >= 2 && jrubyVersionNums[2] >= 7) {
          return Helpers.invokeSuper(context, self, clazz, name, unshiftedArgs, block);
        } else {
          return Helpers.invokeSuper(context, self, extMod, name, unshiftedArgs, block);
        }
      }
    };
  }

  private static JavaMethod autoInjectBindingInvokeWithoutCond(final RubyModule extMod, String name) {
    return new JavaMethod(extMod, Visibility.PUBLIC, name) {
      @Override
      public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
        final IRubyObject[] unshiftedArgs = new IRubyObject[args.length + 1];
        unshiftedArgs[0] = RubyBinding.newBinding(context.getRuntime(), context.currentBinding());
        System.arraycopy(args, 0, unshiftedArgs, 1, args.length);
        if (jrubyVersionNums[0] >= 9 && jrubyVersionNums[1] >= 2 && jrubyVersionNums[2] >= 7) {
          return Helpers.invokeSuper(context, self, clazz, name, unshiftedArgs, block);
        } else {
          return Helpers.invokeSuper(context, self, extMod, name, unshiftedArgs, block);
        }
      }
    };
  }
}
