import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by thasneem on 23/2/16.
 */
public class MyAboutDialog extends JDialog {
    public MyAboutDialog(JFrame jFrame, boolean modal) {
        super(jFrame, modal);
        this.setTitle("About");
        this.setBounds(0, 0, 370, 160);
        this.setResizable(false);
        JLabel labelApp = new JLabel("Android Screen Monitor Studio Plugin");
        JLabel labelCopy = new JLabel("Copyright (C) 2009-2013 adakoda Al rights reserved.");
        JLabel labelCopyright = new JLabel("https://www.linkedin.com/in/muhammedthasneem");
        JTextField labelUrl = new JTextField("muhammed.thasneem@yahoo.com");
        labelUrl.setEditable(false);
        labelUrl.setBorder(new EmptyBorder(0, 0, 0, 0));
        labelUrl.addMouseListener(new MouseListener() {
            public void mouseReleased(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseClicked(MouseEvent arg0) {
                JTextField textField = (JTextField) arg0.getSource();
                textField.selectAll();
            }
        });
        JButton buttonOK = new JButton("OK");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MyAboutDialog.this.onOK();
            }
        });
        Container container1 = new Container();
        FlowLayout flowLayout = new FlowLayout(1, 5, 5);
        container1.setLayout(flowLayout);
        container1.add(labelApp);
        container1.add(labelCopyright);
        container1.add(labelCopy);
        container1.add(labelUrl);
        container1.add(buttonOK);
        Container containger = this.getContentPane();
        containger.add(container1, "Center");
        containger.add(buttonOK, "South");
        AbstractAction actionOK = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MyAboutDialog.this.onOK();
            }
        };
        AbstractAction actionCancel = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MyAboutDialog.this.onCancel();
            }
        };
        JRootPane targetComponent = this.getRootPane();
        InputMap inputMap = targetComponent.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(10, 0), "OK");
        inputMap.put(KeyStroke.getKeyStroke(27, 0), "Cancel");
        targetComponent.setInputMap(1, inputMap);
        targetComponent.getActionMap().put("OK", actionOK);
        targetComponent.getActionMap().put("Cancel", actionCancel);
    }

    private void onOK() {
        this.dispose();
    }

    private void onCancel() {
        this.dispose();
    }
}
