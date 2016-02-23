import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by thasneem on 2/2/16.
 */
public class ASM extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        MainFrame mMainFrame;

        mMainFrame = new MainFrame(new String[]{});

        mMainFrame.selectDevice();


    }
}
