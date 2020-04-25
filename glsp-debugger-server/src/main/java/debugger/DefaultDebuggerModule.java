package debugger;

public class DefaultDebuggerModule extends DebuggerModule {

    @Override
    protected Class<? extends DefaultDebugger> bindDebugger() {
        return DefaultDebugger.class;
    }
}
