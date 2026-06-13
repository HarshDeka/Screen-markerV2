import java.awt.*;
import javax.swing.*;

public class ColorPalette extends JPanel {
    // Exact 16 colors matched from your image
    private final Color[] colors = {
        new Color(255, 218, 185), new Color(255, 105, 180), new Color(147, 112, 219), new Color(51, 153, 255),
        new Color(102, 204, 102), new Color(255, 255, 0), new Color(255, 153, 0), new Color(220, 20, 60),
        new Color(255, 245, 238), new Color(192, 192, 192), new Color(105, 105, 105), new Color(160, 82, 45),
        new Color(0, 153, 51), new Color(102, 0, 102), new Color(0, 51, 102), new Color(0, 0, 0)
    };

    public ColorPalette(function canvasLogic, ToolbarPanel toolbar) {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(0, 162, 232), 3));

        JPanel grid = new JPanel(new GridLayout(2, 8, 0, 0));
        for (Color c : colors) {
            JButton cb = new JButton();
            cb.setBackground(c);
            cb.setOpaque(true);
            cb.setBorderPainted(false); // FIX: Removes the border so it's a solid block of color
            cb.setFocusPainted(false);
            cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            cb.addActionListener(e -> {
                canvasLogic.setBrushColor(c);
                toolbar.getColorButton().setBackground(c);
                this.setVisible(false);
                toolbar.setVisible(true);
            });
            grid.add(cb);
        }

        // FIX: Reused the Undo PNG for the back button
        JButton backBtn = new JButton();
        java.io.File file = new java.io.File("icons/undo.png");
        if (file.exists()) backBtn.setIcon(new ImageIcon("icons/undo.png"));
        else backBtn.setText("Back");
        
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.LIGHT_GRAY));
        backBtn.setPreferredSize(new Dimension(50, 90));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        backBtn.addActionListener(e -> {
            this.setVisible(false);
            toolbar.setVisible(true);
        });

        add(grid, BorderLayout.CENTER);
        add(backBtn, BorderLayout.EAST);
    }
}