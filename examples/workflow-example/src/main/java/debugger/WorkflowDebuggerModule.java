package debugger;

public class WorkflowDebuggerModule extends DefaultDebuggerModule {


    @Override
    protected Class<? extends DefaultDebugger> bindDebugger() {
        return WorkflowDebugger.class;
    }
}
