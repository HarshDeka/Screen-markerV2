import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class ToolbarPanel extends JPanel {
    private final Map<String, JToggleButton> modeButtons = new LinkedHashMap<>();
    private final Map<String, JButton> actionButtons = new LinkedHashMap<>();
    private final JButton colorButton = new JButton();
    private final DotIndicator dotIndicator; 

    public ToolbarPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(0, 162, 232), 3)); 

        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        brandPanel.setBackground(Color.BLACK);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        JLabel title1 = new JLabel("Screen Marker", SwingConstants.LEFT);
        title1.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 14));
        title1.setForeground(Color.WHITE);
        JLabel title2 = new JLabel("& Tools", SwingConstants.LEFT);
        title2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 12));
        title2.setForeground(Color.WHITE);
        brandPanel.add(title1);
        brandPanel.add(title2);
        add(brandPanel, BorderLayout.WEST);

        JPanel gridPanel = new JPanel(new GridLayout(2, 8, 2, 2));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        addToggleTool(gridPanel, "icons/pencil.png", "Pencil", "Pencil Tool");
        addToggleTool(gridPanel, "icons/highlighter.png", "High", "Highlighter Tool");
        addToggleTool(gridPanel, "icons/line.png", "Line", "Line Tool");
        addToggleTool(gridPanel, "icons/arrow.png", "Arrow", "Arrow Tool");
        addToggleTool(gridPanel, "icons/rect.png", "Rect", "Rectangle Tool");
        addToggleTool(gridPanel, "icons/ellipse.png", "Oval", "Ellipse Tool");
        addToggleTool(gridPanel, "icons/eraser.png", "Eraser", "Eraser Brush Modifier"); 
        
        gridPanel.add(new JLabel("")); 

        addActionButton(gridPanel, "icons/hide.png", "Hide", "Toggle UI Overlay");
        addActionButton(gridPanel, "icons/undo.png", "Undo", "Undo Action");
        addActionButton(gridPanel, "icons/trash.png", "Clear", "Clear Canvas");
        addActionButton(gridPanel, "icons/camera.png", "Cap", "Snapshot Tool");
        addToggleTool(gridPanel, "icons/smooth.png", "Smooth", "Smoothing Filter");
        
        dotIndicator = new DotIndicator();
        gridPanel.add(dotIndicator);

        colorButton.setBackground(Color.RED); 
        colorButton.setContentAreaFilled(false);
        colorButton.setOpaque(true);
        colorButton.setPreferredSize(new Dimension(36, 36));
        colorButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        colorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gridPanel.add(colorButton);
        
        JButton exitBtn = new JButton();
        styleActionButton(exitBtn, "icons/exit.png", "Exit", "Terminate Application");
        exitBtn.setForeground(Color.RED);
        exitBtn.addActionListener(e -> System.exit(0));
        gridPanel.add(exitBtn);

        add(gridPanel, BorderLayout.CENTER);
    }

    public class DotIndicator extends JPanel {
        private int dotSize = 10;
        public DotIndicator() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
            setPreferredSize(new Dimension(36, 36));
        }
        public void setDotSize(int size) {
            this.dotSize = Math.min(size, 32); 
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = dotSize / 2;
            g2.fillOval(centerX - radius, centerY - radius, dotSize, dotSize);
        }
    }

    private void addToggleTool(JPanel panel, String iconPath, String fallbackText, String tooltip) {
        ImageIcon icon = loadIcon(iconPath);
        JToggleButton b = (icon != null) ? new JToggleButton(icon) : new JToggleButton(fallbackText);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        b.setToolTipText(tooltip);
        b.setPreferredSize(new Dimension(36, 36));
        
        b.addActionListener(e -> {
            if (!"Smooth".equals(fallbackText)) {
                modeButtons.forEach((name, btn) -> {
                    if (btn != b && !"Smooth".equals(name)) {
                        btn.setSelected(false);
                        btn.setBackground(Color.WHITE);
                    }
                });
            }
            if (b.isSelected()) {
                if ("Smooth".equals(fallbackText)) b.setBackground(new Color(230, 245, 255)); 
                else b.setBackground(new Color(255, 230, 240)); 
            } else {
                b.setBackground(Color.WHITE);
            }
        });
        modeButtons.put(fallbackText, b);
        panel.add(b);
    }

    private void addActionButton(JPanel panel, String iconPath, String fallbackText, String tooltip) {
        JButton b = new JButton();
        styleActionButton(b, iconPath, fallbackText, tooltip);
        actionButtons.put(fallbackText, b);
        panel.add(b);
    }

    private void styleActionButton(JButton b, String iconPath, String fallbackText, String tooltip) {
        ImageIcon icon = loadIcon(iconPath);
        if (icon != null) b.setIcon(icon); else b.setText(fallbackText);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        b.setToolTipText(tooltip);
        b.setPreferredSize(new Dimension(36, 36));
        
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { b.setBackground(new Color(255, 230, 240)); }
            @Override
            public void mouseReleased(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
    }

    private ImageIcon loadIcon(String path) {
        java.io.File file = new java.io.File(path);
        if (file.exists()) return new ImageIcon(path);
        return null;
    }

    public JToggleButton getPencilButton() { return modeButtons.get("Pencil"); }
    public JToggleButton getHighlighterButton() { return modeButtons.get("High"); }
    public JToggleButton getLineButton() { return modeButtons.get("Line"); }
    public JToggleButton getArrowButton() { return modeButtons.get("Arrow"); }
    public JToggleButton getRectButton() { return modeButtons.get("Rect"); }
    public JToggleButton getOvalButton() { return modeButtons.get("Oval"); }
    public JToggleButton getEraserButton() { return modeButtons.get("Eraser"); }
    public JToggleButton getSmoothButton() { return modeButtons.get("Smooth"); }
    
    public JButton getHideButton() { return actionButtons.get("Hide"); }
    public JButton getClearButton() { return actionButtons.get("Clear"); }
    public JButton getCapButton() { return actionButtons.get("Cap"); }
    public JButton getUndoButton() { return actionButtons.get("Undo"); }
    
    public DotIndicator getDotIndicator() { return dotIndicator; }
    public JButton getColorButton() { return colorButton; }
}