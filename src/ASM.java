import com.adakoda.android.asm.MainFrame;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;
import java.awt.*;

/**
 * Created by thasneem on 2/2/16.
 */
public class ASM extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        SwingUtilities.invokeLater(new Runnable() {
            public MainFrame mMainFrame;

            @Override
            public void run() {
                this.mMainFrame = new MainFrame(new String[]{});
                this.mMainFrame.setLocationRelativeTo((Component) null);
                this.mMainFrame.setVisible(true);
                this.mMainFrame.selectDevice();
            }
        });

    }
}
