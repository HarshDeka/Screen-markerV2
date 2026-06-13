import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ScreenMarker extends JFrame {
    private final ToolbarPanel toolbar;
    private final ColorPalette palette; 
    private final function canvasLogic; 
    private final JButton unhideButton; 
    private boolean isDragging = false;
    private Point mouseClickOffset; 

    public ScreenMarker() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); 
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);

        canvasLogic = new function();
        canvasLogic.setVisible(true);
        
        toolbar = new ToolbarPanel();
        
        SwingUtilities.invokeLater(() -> canvasLogic.setMainToolbarFrame(this));
        palette = new ColorPalette(canvasLogic, toolbar); 
        
        int toolbarWidth = 450;  
        int toolbarHeight = 90;
        int startX = (screenSize.width - toolbarWidth) / 2;
        int startY = 20; 
        
        toolbar.setBounds(startX, startY, toolbarWidth, toolbarHeight);
        palette.setBounds(startX, startY, toolbarWidth, toolbarHeight);
        palette.setVisible(false);

        add(palette);
        add(toolbar);

        unhideButton = new JButton("[+]");
        unhideButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        unhideButton.setBackground(Color.BLACK);
        unhideButton.setForeground(Color.WHITE);
        unhideButton.setFocusPainted(false);
        unhideButton.setBorder(BorderFactory.createLineBorder(new Color(0, 162, 232), 2));
        unhideButton.setBounds((screenSize.width - 60) / 2, 0, 60, 25);
        unhideButton.setVisible(false);
        add(unhideButton);

        unhideButton.addActionListener(e -> {
            unhideButton.setVisible(false);
            toolbar.setVisible(true);
        });

        // --- WIRE LINK ENGINE ---
        setupToolListener(toolbar.getPencilButton(), "Pencil");
        setupToolListener(toolbar.getHighlighterButton(), "High");
        setupToolListener(toolbar.getLineButton(), "Line");
        setupToolListener(toolbar.getArrowButton(), "Arrow");
        setupToolListener(toolbar.getRectButton(), "Rect");
        setupToolListener(toolbar.getOvalButton(), "Oval");
        setupToolListener(toolbar.getEraserButton(), "Eraser");

        if (toolbar.getClearButton() != null) toolbar.getClearButton().addActionListener(e -> canvasLogic.clearCanvas());
        if (toolbar.getCapButton() != null) toolbar.getCapButton().addActionListener(e -> canvasLogic.takeSnapshot());
        if (toolbar.getUndoButton() != null) toolbar.getUndoButton().addActionListener(e -> canvasLogic.undoLastAction());
        
        if (toolbar.getHideButton() != null) {
            toolbar.getHideButton().addActionListener(e -> {
                toolbar.setVisible(false);
                palette.setVisible(false); 
                unhideButton.setVisible(true);
                canvasLogic.setDrawingMode("None");
            });
        }

        toolbar.getColorButton().addActionListener(e -> {
            toolbar.setVisible(false);
            palette.setVisible(true);
        });

        if (toolbar.getSmoothButton() != null) {
            toolbar.getSmoothButton().addActionListener(e -> {
                boolean smoothOn = toolbar.getSmoothButton().isSelected();
                canvasLogic.setSmoothingActive(smoothOn);
                if (smoothOn) toolbar.getSmoothButton().setBackground(new Color(230, 245, 255));
                else toolbar.getSmoothButton().setBackground(Color.WHITE);
            });
        }

        MouseWheelListener wheelEngine = e -> {
            canvasLogic.adjustBrushThickness(e.getWheelRotation());
            toolbar.getDotIndicator().setDotSize(canvasLogic.getBrushThickness());
        };
        toolbar.addMouseWheelListener(wheelEngine);
        canvasLogic.addMouseWheelListener(wheelEngine);

        setLocationRelativeTo(null);

        MouseAdapter dragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDragging = true;
                Point mouseOnScreen = e.getLocationOnScreen();
                Point windowLoc = e.getComponent().getLocation(); 
                mouseClickOffset = new Point(mouseOnScreen.x - windowLoc.x, mouseOnScreen.y - windowLoc.y);
            }
            @Override
            public void mouseReleased(MouseEvent e) { isDragging = false; }
        };

        MouseMotionAdapter motionListener = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && mouseClickOffset != null) {
                    Point currentMouse = MouseInfo.getPointerInfo().getLocation();
                    int newX = currentMouse.x - mouseClickOffset.x;
                    int newY = currentMouse.y - mouseClickOffset.y;
                    
                    toolbar.setLocation(newX, newY);
                    palette.setLocation(newX, newY);
                }
            }
        };

        toolbar.addMouseListener(dragListener);
        toolbar.addMouseMotionListener(motionListener);
        palette.addMouseListener(dragListener);
        palette.addMouseMotionListener(motionListener);
    }

    private void setupToolListener(JToggleButton button, String modeName) {
        if (button != null) {
            button.addActionListener(e -> {
                if (button.isSelected()) canvasLogic.setDrawingMode(modeName);
                else canvasLogic.setDrawingMode("None");
            });
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}
        SwingUtilities.invokeLater(() -> new ScreenMarker().setVisible(true));
    }
}