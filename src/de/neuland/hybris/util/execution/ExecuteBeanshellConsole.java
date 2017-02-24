package de.neuland.hybris.util.execution;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import de.neuland.hybris.http.HybrisHTTPRequest;
import de.neuland.hybris.http.helper.JSessionCsrfPair;

public class ExecuteBeanshellConsole extends ExecuteScriptConsole {

    private static final String[] CONSOLE_DESCRIPTIONS = new String[]{"Hybris Beanshell result", "Hybris Beanshell output", "Hybris Beanshell stacktrace"};

    private static ExecuteBeanshellConsole instance = new ExecuteBeanshellConsole();

    private ExecuteBeanshellConsole() {
    }

    public static ExecuteBeanshellConsole getInstance() {
        return instance;
    }

    public void execute(final String documentContent, final Project project) {
        final HybrisHTTPRequest hybrisHttpRequest = HybrisHTTPRequest.getInstance();
        final JSessionCsrfPair jSessionCsrfPair = executePrepare(project);
        if(jSessionCsrfPair == null) {
            return;
        }
        try {
            startSendRequestAnimationInAllConsoles();
            ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                @Override
                public void run() {
                    String jsonResult = hybrisHttpRequest.executeBeanshellScript(documentContent, jSessionCsrfPair);
                    markRequestAsFinish();
                    fillConsolesWithTheResult(jsonResult);
                }
            });
        } catch (Exception e) {
            fillExceptionConsole(e, project);
        }
        showTabs(project, "Hybris Beanshell Console", CONSOLE_DESCRIPTIONS);
    }

}
