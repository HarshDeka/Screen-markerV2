import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ScreenMarker extends JFrame {
    private final ToolbarPanel toolbar;
    private final function canvasLogic; // Controls the separate overlay window
    private boolean isDragging = false;
    private int dragX, dragY;

    public ScreenMarker() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); 
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Launch the independent full-screen canvas backdrop window
        canvasLogic = new function();
        canvasLogic.setVisible(true);
        
        // 2. Initialize and attach the UI toolbar locally
        toolbar = new ToolbarPanel();
        add(toolbar, BorderLayout.CENTER);

        // 3. Connect the selection link cleanly between the separate windows
        JToggleButton pencil = toolbar.getPencilButton();
        if (pencil != null) {
            pencil.addActionListener(e -> {
                canvasLogic.setDrawingEnabled(pencil.isSelected());
            });
        }

        // Size the window to strictly encapsulate the toolbar panel size limits
        pack();
        setSize(460, 95); 
        setLocationRelativeTo(null); // Starts perfectly centered on your screen

        // FIXED DRAGGING LOGIC: Uses mouse location components relative to screen space
        toolbar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDragging = true;
                dragX = e.getX();
                dragY = e.getY();
            }
            @Override
            public void mouseReleased(MouseEvent e) { 
                isDragging = false; 
            }
        });

        toolbar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    // Moves ONLY the small toolbar frame across your monitor
                    setLocation(e.getXOnScreen() - dragX, e.getYOnScreen() - dragY);
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            new ScreenMarker().setVisible(true);
        });
    }
}