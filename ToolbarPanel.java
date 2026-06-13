import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class ToolbarPanel extends JPanel {
    private final Map<String, JToggleButton> modeButtons = new LinkedHashMap<>();
    private final JButton colorButton = new JButton();

    public ToolbarPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(0, 162, 232), 3)); 

        // --- LEFT SIDE: Black Branding Block ---
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        brandPanel.setBackground(Color.BLACK);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        JLabel title1 = new JLabel("Screen Marker", SwingConstants.LEFT);
        title1.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 14));
        title1.setForeground(Color.WHITE);
        JLabel title2 = new JLabel("& Recorder", SwingConstants.LEFT);
        title2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 12));
        title2.setForeground(Color.WHITE);
        brandPanel.add(title1);
        brandPanel.add(title2);
        add(brandPanel, BorderLayout.WEST);

        // --- CENTER/RIGHT SIDE: The Uniform 2-Row Grid ---
        JPanel gridPanel = new JPanel(new GridLayout(2, 10, 2, 2));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // ROW 1: Drawing Tools
        addToggleTool(gridPanel, "icons/pencil.png", "Pencil", "Pencil Tool");
        addToggleTool(gridPanel, "icons/highlighter.png", "High", "Highlighter Tool");
        addToggleTool(gridPanel, "icons/line.png", "Line", "Line Tool");
        addToggleTool(gridPanel, "icons/arrow.png", "Arrow", "Arrow Tool");
        addToggleTool(gridPanel, "icons/rect.png", "Rect", "Rectangle Tool");
        addToggleTool(gridPanel, "icons/ellipse.png", "Oval", "Ellipse Tool");
        addToggleTool(gridPanel, "icons/text.png", "Text", "Text Box Tool");
        addToggleTool(gridPanel, "icons/eraser.png", "Eraser", "Eraser Brush Modifier"); 
        
        gridPanel.add(new JLabel("")); 
        gridPanel.add(new JLabel(""));

        // ROW 2: Utilities
        addRegularButton(gridPanel, "icons/hide.png", "Hide", "Toggle UI Overlay");
        addRegularButton(gridPanel, "icons/monitor.png", "Disp", "Target Display");
        addRegularButton(gridPanel, "icons/undo.png", "Undo", "Undo Action");
        addRegularButton(gridPanel, "icons/trash.png", "Clear", "Clear Canvas");
        addRegularButton(gridPanel, "icons/camera.png", "Cap", "Snapshot Tool");
        addRegularButton(gridPanel, "icons/video.png", "Rec", "Video Recorder");
        addRegularButton(gridPanel, "icons/smooth.png", "Smooth", "Smoothing Filter");
        
        JLabel sizeDot = new JLabel("•", SwingConstants.CENTER);
        sizeDot.setFont(new Font("Segoe UI", Font.BOLD, 26));
        gridPanel.add(sizeDot);

        colorButton.setBackground(Color.RED); 
        colorButton.setContentAreaFilled(false);
        colorButton.setOpaque(true);
        colorButton.setPreferredSize(new Dimension(36, 36));
        colorButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        gridPanel.add(colorButton);
        
        JButton exitBtn = new JButton();
        styleButton(exitBtn, "icons/exit.png", "Exit", "Terminate Application");
        exitBtn.setForeground(Color.RED);
        exitBtn.addActionListener(e -> System.exit(0));
        gridPanel.add(exitBtn);

        add(gridPanel, BorderLayout.CENTER);
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
            modeButtons.values().forEach(btn -> {
                if (btn != b) {
                    btn.setSelected(false);
                    btn.setBackground(Color.WHITE);
                }
            });
            if (b.isSelected()) b.setBackground(new Color(255, 230, 240));
            else b.setBackground(Color.WHITE);
        });

        modeButtons.put(fallbackText, b);
        panel.add(b);
    }

    private void addRegularButton(JPanel panel, String iconPath, String fallbackText, String tooltip) {
        JButton b = new JButton();
        styleButton(b, iconPath, fallbackText, tooltip);
        panel.add(b);
    }

    private void styleButton(JButton b, String iconPath, String fallbackText, String tooltip) {
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
    }

    private ImageIcon loadIcon(String path) {
        java.io.File file = new java.io.File(path);
        if (file.exists()) return new ImageIcon(path);
        return null;
    }

    public JToggleButton getPencilButton() {
        return modeButtons.get("Pencil");
    }
}