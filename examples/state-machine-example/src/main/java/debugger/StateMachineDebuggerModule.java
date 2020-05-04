package debugger;

import di.DefaultDebuggerModule;

public class StateMachineDebuggerModule extends DefaultDebuggerModule {


    @Override
    protected Class<? extends DefaultDebugger> bindDebugger() {
        return StateMachineDebugger.class;
    }
}
