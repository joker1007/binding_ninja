package io.github.joker1007;

import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.runtime.load.BasicLibraryService;

public class BindingNinjaService implements BasicLibraryService {
  @Override
  public boolean basicLoad(final Ruby runtime) throws IOException {
    RubyModule bindingNinja = runtime.defineModule("BindingNinja");
    bindingNinja.setInstanceVariable("@auto_inject_binding_extensions", RubyHash.newHash(runtime));
    bindingNinja.defineAnnotatedMethods(RubyBindingNinja.class);
    return true;
  }
}
