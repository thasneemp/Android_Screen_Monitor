import com.adakoda.android.asm.ADB;
import com.adakoda.android.asm.AboutDialog;
import com.adakoda.android.asm.FBImage;
import com.adakoda.android.asm.SelectDeviceDialog;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Created by thasneem on 2/2/16.
 */
public class MainFrame {

    private static final int DEFAULT_WIDTH = 320;
    private static final int DEFAULT_HEIGHT = 480;
    private static final String EXT_PNG = "png";
    private static final int FB_TYPE_XBGR = 0;
    private static final int FB_TYPE_RGBX = 1;
    private static final int FB_TYPE_XRGB = 2;
    private static int[][] FB_OFFSET_LIST = new int[][]{{0, 1, 2, 3}, {3, 2, 1, 0}, {2, 1, 0, 3}};
    private MainFrame.MainPanel mPanel;
    private JPopupMenu mPopupMenu;
    private Preferences mPrefs;
    private int mRawImageWidth = 320;
    private int mRawImageHeight = 480;
    private boolean mPortrait = true;
    private double mZoom = 1.0D;
    private int mFbType = 0;
    private ADB mADB;
    private IDevice[] mDevices;
    private IDevice mDevice;
    private JFrame jFrame;
    private MainFrame.MonitorThread mMonitorThread;
    private MouseListener mMouseListener = new MouseListener() {
        public void mouseReleased(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                MainFrame.this.mPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }

        }
    };
    private WindowListener mWindowListener = new WindowListener() {
        public void windowOpened(WindowEvent arg0) {
        }

        public void windowIconified(WindowEvent arg0) {
        }

        public void windowDeiconified(WindowEvent arg0) {
        }

        public void windowDeactivated(WindowEvent arg0) {
        }

        public void windowClosing(WindowEvent arg0) {
            if (MainFrame.this.mADB != null) {
                MainFrame.this.mADB.terminate();
            }

        }

        public void windowClosed(WindowEvent arg0) {
        }

        public void windowActivated(WindowEvent arg0) {
        }
    };


    public MainFrame(String[] args) {
        this.initialize(args);
    }

    public void startMonitor() {
        this.mMonitorThread = new MainFrame.MonitorThread();
        this.mMonitorThread.start();
    }

    public void stopMonitor() {
        this.mMonitorThread = null;
    }

    public void selectDevice() {
        this.stopMonitor();
        this.mDevices = this.mADB.getDevices();
        if (this.mDevices != null) {
            ArrayList list = new ArrayList();

            for (int dialog = 0; dialog < this.mDevices.length; ++dialog) {
                list.add(this.mDevices[dialog].toString());
            }

            SelectDeviceDialog var4 = new SelectDeviceDialog(jFrame, true, list);
            var4.setLocationRelativeTo(jFrame);
            var4.setVisible(true);
            if (var4.isOK()) {
                int selectedIndex = var4.getSelectedIndex();
                if (selectedIndex >= 0) {
                    this.mDevice = this.mDevices[selectedIndex];
                    this.setImage((FBImage) null);
                }
            }
        }

        this.startMonitor();
    }

    public void setOrientation(boolean portrait) {
        if (this.mPortrait != portrait) {
            this.mPortrait = portrait;
            this.savePrefs();
            this.updateSize();
        }

    }

    public void setZoom(double zoom) {
        if (this.mZoom != zoom) {
            this.mZoom = zoom;
            this.savePrefs();
            this.updateSize();
        }

    }

    public void setFrameBuffer(int fbType) {
        if (this.mFbType != fbType) {
            this.mFbType = fbType;
            this.savePrefs();
        }

    }

    public void saveImage() {
        FBImage inImage = this.mPanel.getFBImage();
        if (inImage != null) {
            BufferedImage outImage = new BufferedImage((int) ((double) inImage.getWidth() * this.mZoom), (int) ((double) inImage.getHeight() * this.mZoom), inImage.getType());
            if (outImage != null) {
                AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(this.mZoom, this.mZoom), 2);
                op.filter(inImage, outImage);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileFilter() {
                    public String getDescription() {
                        return "*.png";
                    }

                    public boolean accept(File f) {
                        String ext = f.getName().toLowerCase();
                        return ext.endsWith(".png");
                    }
                });
                if (fileChooser.showSaveDialog(jFrame) == 0) {
                    try {
                        File ex = fileChooser.getSelectedFile();
                        String path = ex.getAbsolutePath();
                        if (!path.endsWith(".png")) {
                            ex = new File(path + "." + "png");
                        }

                        ImageIO.write(outImage, "png", ex);
                    } catch (Exception var7) {
                        JOptionPane.showMessageDialog(jFrame, "Failed to save a image.", "Save Image", 0);
                    }
                }
            }
        }

    }

    public void about() {
        AboutDialog dialog = new AboutDialog(jFrame, true);
        dialog.setLocationRelativeTo(jFrame);
        dialog.setVisible(true);
    }

    public void updateSize() {
        int width;
        int height;
        if (this.mPortrait) {
            width = this.mRawImageWidth;
            height = this.mRawImageHeight;
        } else {
            width = this.mRawImageHeight;
            height = this.mRawImageWidth;
        }

        Insets insets = jFrame.getInsets();
        int newWidth = (int) ((double) width * this.mZoom) + insets.left + insets.right;
        int newHeight = (int) ((double) height * this.mZoom) + insets.top + insets.bottom;
        if (jFrame.getWidth() != newWidth || jFrame.getHeight() != newHeight) {
            jFrame.setSize(newWidth, newHeight);
        }

    }

    public void setImage(FBImage fbImage) {
        if (fbImage != null) {
            this.mRawImageWidth = fbImage.getRawWidth();
            this.mRawImageHeight = fbImage.getRawHeight();
        }

        this.mPanel.setFBImage(fbImage);
        this.updateSize();
    }

    private void initialize(String[] args) {
        init(args);

    }

    private void init(String[] args) {

        this.mADB = new ADB();
        if (!this.mADB.initialize()) {
            JOptionPane.showMessageDialog(jFrame, "Could not find adb, please install Android SDK and set path to adb.", "Error", 0);
        }
        this.jFrame = new JFrame();
        this.parseArgs(args);
        this.initializePrefs();
        this.initializeFrame();
        this.initializePanel();
        this.initializeMenu();
        this.initializeActionMap();

        jFrame.addMouseListener(this.mMouseListener);
        jFrame.addWindowListener(this.mWindowListener);

        this.setImage((FBImage) null);
    }

    private void parseArgs(String[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                String arg = args[i];
                if (arg.equals("-f0")) {
                    this.mFbType = 0;
                } else if (arg.equals("-f1")) {
                    this.mFbType = 1;
                } else if (arg.equals("-f2")) {
                    this.mFbType = 2;
                }
            }
        }

    }

    private void savePrefs() {
        if (this.mPrefs != null) {
            this.mPrefs.putInt("PrefVer", 1);
            this.mPrefs.putBoolean("Portrait", this.mPortrait);
            this.mPrefs.putDouble("Zoom", this.mZoom);
            this.mPrefs.putInt("FbType", this.mFbType);
        }

    }

    private void initializePrefs() {
        this.mPrefs = Preferences.userNodeForPackage(this.getClass());
        if (this.mPrefs != null) {
            int prefVer = this.mPrefs.getInt("PrefVer", 1);
            if (prefVer == 1) {
                this.mPortrait = this.mPrefs.getBoolean("Portrait", true);
                this.mZoom = this.mPrefs.getDouble("Zoom", 1.0D);
                this.mFbType = this.mPrefs.getInt("FbType", 0);
            }
        }

    }

    private void initializeFrame() {
        jFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(jFrame,
                        "Are you sure to close this window?", "Really Closing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    stopMonitor();
                    mADB.terminate();

                    jFrame.dispose();
                }
            }
        });

        jFrame.setTitle("Android Screen Monitor");
        jFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("icon.png")));
        jFrame.setResizable(false);
        this.jFrame.setLocationRelativeTo((Component) null);
        this.jFrame.setVisible(true);
        jFrame.pack();
        this.jFrame.setVisible(true);
    }

    private void initializePanel() {
        this.mPanel = new MainFrame.MainPanel();
        jFrame.add(this.mPanel);
    }

    private void initializeMenu() {
        this.mPopupMenu = new JPopupMenu();
        this.initializeSelectDeviceMenu();
        this.mPopupMenu.addSeparator();
        this.initializeOrientationMenu();
        this.initializeZoomMenu();
        this.initializeFrameBufferMenu();
        this.mPopupMenu.addSeparator();
        this.initializeSaveImageMenu();
        this.mPopupMenu.addSeparator();
        this.initializeAbout();
        this.mPopupMenu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
    }

    private void initializeSelectDeviceMenu() {
        JMenuItem menuItemSelectDevice = new JMenuItem("Select Device...");
        menuItemSelectDevice.setMnemonic(68);
        menuItemSelectDevice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.selectDevice();
            }
        });
        this.mPopupMenu.add(menuItemSelectDevice);
    }

    private void initializeOrientationMenu() {
        JMenu menuOrientation = new JMenu("Orientation");
        menuOrientation.setMnemonic(79);
        this.mPopupMenu.add(menuOrientation);
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButtonMenuItem radioButtonMenuItemPortrait = new JRadioButtonMenuItem("Portrait");
        radioButtonMenuItemPortrait.setMnemonic(80);
        radioButtonMenuItemPortrait.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setOrientation(true);
            }
        });
        if (this.mPortrait) {
            radioButtonMenuItemPortrait.setSelected(true);
        }

        buttonGroup.add(radioButtonMenuItemPortrait);
        menuOrientation.add(radioButtonMenuItemPortrait);
        JRadioButtonMenuItem radioButtonMenuItemLandscape = new JRadioButtonMenuItem("Landscape");
        radioButtonMenuItemLandscape.setMnemonic(76);
        radioButtonMenuItemLandscape.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setOrientation(false);
            }
        });
        if (!this.mPortrait) {
            radioButtonMenuItemLandscape.setSelected(true);
        }

        buttonGroup.add(radioButtonMenuItemLandscape);
        menuOrientation.add(radioButtonMenuItemLandscape);
    }

    private void initializeZoomMenu() {
        JMenu menuZoom = new JMenu("Zoom");
        menuZoom.setMnemonic(90);
        this.mPopupMenu.add(menuZoom);
        ButtonGroup buttonGroup = new ButtonGroup();
        this.addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 0.1D, "10%", -1, this.mZoom);
        this.addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 0.25D, "25%", -1, this.mZoom);
        this.addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 0.5D, "50%", 53, this.mZoom);
        this.addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 0.75D, "75%", 55, this.mZoom);
        this.addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 1.0D, "100%", 49, this.mZoom);
        this.addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 1.5D, "150%", 48, this.mZoom);
        this.addRadioButtonMenuItemZoom(menuZoom, buttonGroup, 2.0D, "200%", 50, this.mZoom);
    }

    private void addRadioButtonMenuItemZoom(JMenu menuZoom, ButtonGroup buttonGroup, final double zoom, String caption, int nemonic, double currentZoom) {
        JRadioButtonMenuItem radioButtonMenuItemZoom = new JRadioButtonMenuItem(caption);
        if (nemonic != -1) {
            radioButtonMenuItemZoom.setMnemonic(nemonic);
        }

        radioButtonMenuItemZoom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setZoom(zoom);
            }
        });
        if (currentZoom == zoom) {
            radioButtonMenuItemZoom.setSelected(true);
        }

        buttonGroup.add(radioButtonMenuItemZoom);
        menuZoom.add(radioButtonMenuItemZoom);
    }

    private void initializeFrameBufferMenu() {
        JMenu menuZoom = new JMenu("FrameBuffer");
        menuZoom.setMnemonic(70);
        this.mPopupMenu.add(menuZoom);
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButtonMenuItem radioButtonMenuItemFbXBGR = new JRadioButtonMenuItem("XBGR");
        radioButtonMenuItemFbXBGR.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setFrameBuffer(0);
            }
        });
        if (this.mFbType == 0) {
            radioButtonMenuItemFbXBGR.setSelected(true);
        }

        buttonGroup.add(radioButtonMenuItemFbXBGR);
        menuZoom.add(radioButtonMenuItemFbXBGR);
        JRadioButtonMenuItem radioButtonMenuItemFbRGBX = new JRadioButtonMenuItem("RGBX");
        radioButtonMenuItemFbRGBX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setFrameBuffer(1);
            }
        });
        if (this.mFbType == 1) {
            radioButtonMenuItemFbRGBX.setSelected(true);
        }

        buttonGroup.add(radioButtonMenuItemFbRGBX);
        menuZoom.add(radioButtonMenuItemFbRGBX);
        JRadioButtonMenuItem radioButtonMenuItemFbXRGB = new JRadioButtonMenuItem("XRGB");
        radioButtonMenuItemFbXRGB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setFrameBuffer(2);
            }
        });
        if (this.mFbType == 2) {
            radioButtonMenuItemFbXRGB.setSelected(true);
        }

        buttonGroup.add(radioButtonMenuItemFbXRGB);
        menuZoom.add(radioButtonMenuItemFbXRGB);
    }

    private void initializeSaveImageMenu() {
        JMenuItem menuItemSaveImage = new JMenuItem("Save Image...");
        menuItemSaveImage.setMnemonic(83);
        menuItemSaveImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.saveImage();
            }
        });
        this.mPopupMenu.add(menuItemSaveImage);
    }

    private void initializeActionMap() {
        AbstractAction actionSelectDevice = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.selectDevice();
            }
        };
        AbstractAction actionPortrait = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setOrientation(true);
            }
        };
        AbstractAction actionLandscape = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setOrientation(false);
            }
        };
        AbstractAction actionZoom50 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setZoom(0.5D);
            }
        };
        AbstractAction actionZoom75 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setZoom(0.75D);
            }
        };
        AbstractAction actionZoom100 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setZoom(1.0D);
            }
        };
        AbstractAction actionZoom150 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setZoom(1.5D);
            }
        };
        AbstractAction actionZoom200 = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.setZoom(2.0D);
            }
        };
        AbstractAction actionSaveImage = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.saveImage();
            }
        };
        AbstractAction actionAbout = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.about();
            }
        };
        JRootPane targetComponent = jFrame.getRootPane();
        InputMap inputMap = targetComponent.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(68, 128), "Select Device");
        inputMap.put(KeyStroke.getKeyStroke(80, 128), "Portrait");
        inputMap.put(KeyStroke.getKeyStroke(76, 128), "Landscape");
        inputMap.put(KeyStroke.getKeyStroke(53, 128), "50%");
        inputMap.put(KeyStroke.getKeyStroke(55, 128), "75%");
        inputMap.put(KeyStroke.getKeyStroke(49, 128), "100%");
        inputMap.put(KeyStroke.getKeyStroke(48, 128), "150%");
        inputMap.put(KeyStroke.getKeyStroke(50, 128), "200%");
        inputMap.put(KeyStroke.getKeyStroke(83, 128), "Save Image");
        inputMap.put(KeyStroke.getKeyStroke(65, 128), "About ASM");
        targetComponent.setInputMap(1, inputMap);
        targetComponent.getActionMap().put("Select Device", actionSelectDevice);
        targetComponent.getActionMap().put("Portrait", actionPortrait);
        targetComponent.getActionMap().put("Landscape", actionLandscape);
        targetComponent.getActionMap().put("Select Device", actionSelectDevice);
        targetComponent.getActionMap().put("50%", actionZoom50);
        targetComponent.getActionMap().put("75%", actionZoom75);
        targetComponent.getActionMap().put("100%", actionZoom100);
        targetComponent.getActionMap().put("150%", actionZoom150);
        targetComponent.getActionMap().put("200%", actionZoom200);
        targetComponent.getActionMap().put("Save Image", actionSaveImage);
        targetComponent.getActionMap().put("About ASM", actionAbout);
    }

    private void initializeAbout() {
        JMenuItem menuItemAbout = new JMenuItem("About ASM");
        menuItemAbout.setMnemonic(65);
        menuItemAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.about();
            }
        });
        this.mPopupMenu.add(menuItemAbout);
    }

    public class MainPanel extends JPanel {
        private FBImage mFBImage;

        public MainPanel() {
            this.setBackground(Color.BLACK);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (this.mFBImage != null) {
                int srcWidth;
                int srcHeight;
                if (MainFrame.this.mPortrait) {
                    srcWidth = MainFrame.this.mRawImageWidth;
                    srcHeight = MainFrame.this.mRawImageHeight;
                } else {
                    srcWidth = MainFrame.this.mRawImageHeight;
                    srcHeight = MainFrame.this.mRawImageWidth;
                }

                int dstWidth = (int) ((double) srcWidth * MainFrame.this.mZoom);
                int dstHeight = (int) ((double) srcHeight * MainFrame.this.mZoom);
                if (MainFrame.this.mZoom == 1.0D) {
                    g.drawImage(this.mFBImage, 0, 0, dstWidth, dstHeight, 0, 0, srcWidth, srcHeight, (ImageObserver) null);
                } else {
                    Image image = this.mFBImage.getScaledInstance(dstWidth, dstHeight, 4);
                    if (image != null) {
                        g.drawImage(image, 0, 0, dstWidth, dstHeight, 0, 0, dstWidth, dstHeight, (ImageObserver) null);
                    }
                }
            }

        }

        public void setFBImage(FBImage fbImage) {
            this.mFBImage = fbImage;
            this.repaint();
        }

        public FBImage getFBImage() {
            return this.mFBImage;
        }
    }

    public class MonitorThread extends Thread {
        public MonitorThread() {
        }

        public void run() {
            Thread thread = Thread.currentThread();
            if (MainFrame.this.mDevice != null) {
                try {
                    while (MainFrame.this.mMonitorThread == thread) {
                        final FBImage fbImage = this.getDeviceImage();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                MainFrame.this.setImage(fbImage);
                            }
                        });
                    }
                } catch (IOException var3) {
                    ;
                }
            }

        }

        private FBImage getDeviceImage() throws IOException {
            boolean success = true;
            boolean debug = false;
            FBImage fbImage = null;
            RawImage tmpRawImage = null;
            RawImage rawImage = null;
            int imageWidth;
            int imageHeight;
            int redOffset;
            int greenOffset;
            int blueOffset;
            int alphaOffset;
            int redMask;
            if (success) {
                try {
                    tmpRawImage = MainFrame.this.mDevice.getScreenshot();
                    if (tmpRawImage == null) {
                        success = false;
                    } else if (!debug) {
                        rawImage = tmpRawImage;
                    } else {
                        rawImage = new RawImage();
                        rawImage.version = 1;
                        rawImage.bpp = 32;
                        rawImage.size = tmpRawImage.width * tmpRawImage.height * 4;
                        rawImage.width = tmpRawImage.width;
                        rawImage.height = tmpRawImage.height;
                        rawImage.red_offset = 0;
                        rawImage.red_length = 8;
                        rawImage.blue_offset = 16;
                        rawImage.blue_length = 8;
                        rawImage.green_offset = 8;
                        rawImage.green_length = 8;
                        rawImage.alpha_offset = 0;
                        rawImage.alpha_length = 0;
                        rawImage.data = new byte[rawImage.size];
                        imageWidth = 0;
                        imageHeight = 0;

                        for (int buffer = 0; buffer < rawImage.height; ++buffer) {
                            for (redOffset = 0; redOffset < rawImage.width; ++redOffset) {
                                greenOffset = tmpRawImage.data[imageWidth++] & 255;
                                greenOffset |= tmpRawImage.data[imageWidth++] << 8 & '\uff00';
                                blueOffset = (greenOffset >> 11 & 31) << 3;
                                alphaOffset = (greenOffset >> 5 & 63) << 2;
                                redMask = (greenOffset >> 0 & 31) << 3;
                                rawImage.data[imageHeight++] = (byte) blueOffset;
                                rawImage.data[imageHeight++] = (byte) alphaOffset;
                                rawImage.data[imageHeight++] = (byte) redMask;
                                rawImage.data[imageHeight++] = -1;
                            }
                        }
                    }
                } catch (IOException var36) {
                    ;
                } finally {
                    if (rawImage == null || rawImage.bpp != 16 && rawImage.bpp != 32) {
                        success = false;
                    }

                }
            }

            if (success) {
                if (MainFrame.this.mPortrait) {
                    imageWidth = rawImage.width;
                    imageHeight = rawImage.height;
                } else {
                    imageWidth = rawImage.height;
                    imageHeight = rawImage.width;
                }

                fbImage = new FBImage(imageWidth, imageHeight, 1, rawImage.width, rawImage.height);
                byte[] var38 = rawImage.data;
                redOffset = rawImage.red_offset;
                greenOffset = rawImage.green_offset;
                blueOffset = rawImage.blue_offset;
                alphaOffset = rawImage.alpha_offset;
                redMask = this.getMask(rawImage.red_length);
                int greenMask = this.getMask(rawImage.green_length);
                int blueMask = this.getMask(rawImage.blue_length);
                int alphaMask = this.getMask(rawImage.alpha_length);
                int redShift = 8 - rawImage.red_length;
                int greenShift = 8 - rawImage.green_length;
                int blueShift = 8 - rawImage.blue_length;
                int alphaShift = 8 - rawImage.alpha_length;
                int index = 0;
                int x;
                int[] y;
                int r;
                int value;
                int b;
                int g;
                int a;
                int var41;
                if (rawImage.bpp == 16) {
                    byte offset0 = 0;
                    byte offset1 = 1;
                    if (MainFrame.this.mPortrait) {
                        y = new int[rawImage.width];

                        for (x = 0; x < rawImage.height; ++x) {
                            for (value = 0; value < rawImage.width; index += 2) {
                                r = var38[index + offset0] & 255;
                                r |= var38[index + offset1] << 8 & '\uff00';
                                g = (r >>> redOffset & redMask) << redShift;
                                b = (r >>> greenOffset & greenMask) << greenShift;
                                a = (r >>> blueOffset & blueMask) << blueShift;
                                y[value] = -16777216 | g << 16 | b << 8 | a;
                                ++value;
                            }

                            fbImage.setRGB(0, x, rawImage.width, 1, y, 0, rawImage.width);
                        }
                    } else {
                        for (var41 = 0; var41 < rawImage.height; ++var41) {
                            for (x = 0; x < rawImage.width; ++x) {
                                value = var38[index + offset0] & 255;
                                value |= var38[index + offset1] << 8 & '\uff00';
                                r = (value >>> redOffset & redMask) << redShift;
                                g = (value >>> greenOffset & greenMask) << greenShift;
                                b = (value >>> blueOffset & blueMask) << blueShift;
                                value = -16777216 | r << 16 | g << 8 | b;
                                index += 2;
                                fbImage.setRGB(var41, rawImage.width - x - 1, value);
                            }
                        }
                    }
                } else if (rawImage.bpp == 32) {
                    int var39 = MainFrame.FB_OFFSET_LIST[MainFrame.this.mFbType][0];
                    int var40 = MainFrame.FB_OFFSET_LIST[MainFrame.this.mFbType][1];
                    int offset2 = MainFrame.FB_OFFSET_LIST[MainFrame.this.mFbType][2];
                    int offset3 = MainFrame.FB_OFFSET_LIST[MainFrame.this.mFbType][3];
                    if (MainFrame.this.mPortrait) {
                        y = new int[rawImage.width];

                        for (x = 0; x < rawImage.height; ++x) {
                            for (value = 0; value < rawImage.width; index += 4) {
                                r = var38[index + var39] & 255;
                                r |= (var38[index + var40] & 255) << 8;
                                r |= (var38[index + offset2] & 255) << 16;
                                r |= (var38[index + offset3] & 255) << 24;
                                g = (r >>> redOffset & redMask) << redShift;
                                b = (r >>> greenOffset & greenMask) << greenShift;
                                a = (r >>> blueOffset & blueMask) << blueShift;
                                int a1;
                                if (rawImage.alpha_length == 0) {
                                    a1 = 255;
                                } else {
                                    a1 = (r >>> alphaOffset & alphaMask) << alphaShift;
                                }

                                y[value] = a1 << 24 | g << 16 | b << 8 | a;
                                ++value;
                            }

                            fbImage.setRGB(0, x, rawImage.width, 1, y, 0, rawImage.width);
                        }
                    } else {
                        for (var41 = 0; var41 < rawImage.height; ++var41) {
                            for (x = 0; x < rawImage.width; ++x) {
                                value = var38[index + var39] & 255;
                                value |= (var38[index + var40] & 255) << 8;
                                value |= (var38[index + offset2] & 255) << 16;
                                value |= (var38[index + offset3] & 255) << 24;
                                r = (value >>> redOffset & redMask) << redShift;
                                g = (value >>> greenOffset & greenMask) << greenShift;
                                b = (value >>> blueOffset & blueMask) << blueShift;
                                if (rawImage.alpha_length == 0) {
                                    a = 255;
                                } else {
                                    a = (value >>> alphaOffset & alphaMask) << alphaShift;
                                }

                                value = a << 24 | r << 16 | g << 8 | b;
                                index += 4;
                                fbImage.setRGB(var41, rawImage.width - x - 1, value);
                            }
                        }
                    }
                }
            }

            return fbImage;
        }

        public int getMask(int length) {
            int res = 0;

            for (int i = 0; i < length; ++i) {
                res = (res << 1) + 1;
            }

            return res;
        }
    }
}
