package di;

import com.google.inject.AbstractModule;
import debugger.DefaultDebugger;

public abstract class DebuggerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DefaultDebugger.class).to(bindDebugger());
    }

    protected abstract Class<? extends DefaultDebugger> bindDebugger();

}
