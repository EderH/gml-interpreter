package debugger;

import di.DefaultDebuggerModule;

public class SMMLDebuggerModule extends DefaultDebuggerModule {


    @Override
    protected Class<? extends DefaultDebugger> bindDebugger() {
        return SMMLDebugger.class;
    }
}
