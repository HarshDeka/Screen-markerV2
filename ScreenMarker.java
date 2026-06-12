import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScreenMarker extends JFrame {

    private final CanvasPanel canvas;
    private final ToolbarPanel toolbar;

    private Color currentColor = new Color(255, 40, 120);
    private float currentStroke = 4.0f;
    private ToolMode currentMode = ToolMode.PENCIL;

    private enum ToolMode {
        PENCIL, HIGHLIGHTER, LINE, ARROW, RECTANGLE, ELLIPSE, TEXT, ERASER
    }

    public ScreenMarker() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 1)); // Transparent background
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screenSize.width, screenSize.height);
        setLayout(new BorderLayout());

        canvas = new CanvasPanel();
        add(canvas, BorderLayout.CENTER);

        toolbar = new ToolbarPanel();
        add(toolbar, BorderLayout.NORTH);

        setFocusable(true);
    }

    private void setMode(ToolMode mode) {
        currentMode = mode;
        toolbar.refreshSelection();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ScreenMarker app = new ScreenMarker();
            app.setVisible(true);
        });
    }

    class ToolbarPanel extends JPanel {
        private final Map<ToolMode, JToggleButton> modeButtons = new LinkedHashMap<>();
        private final JButton colorButton = new JButton();
        private final JLabel strokeLabel = new JLabel("4 px");
        private final JSlider strokeSlider = new JSlider(1, 24, 4);
        private final JButton eraseAll = new JButton("🗑 Clear");
        private final JButton power = new JButton("❌ Exit");

        ToolbarPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(245, 245, 245));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

            JPanel topRow = new JPanel(new BorderLayout(10, 0));
            topRow.setOpaque(false);
            topRow.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

            JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            brand.setOpaque(false);
            JLabel title = new JLabel("Screen Marker");
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setForeground(new Color(40, 40, 40));
            brand.add(title);

            JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            toolRow.setOpaque(false);

            addMode(toolRow, "✏ Pencil", ToolMode.PENCIL);
            addMode(toolRow, "🖍 Highlight", ToolMode.HIGHLIGHTER);
            addMode(toolRow, "📏 Line", ToolMode.LINE);
            addMode(toolRow, "↗ Arrow", ToolMode.ARROW);
            addMode(toolRow, "⬜ Rect", ToolMode.RECTANGLE);
            addMode(toolRow, "◯ Ellipse", ToolMode.ELLIPSE);
            addMode(toolRow, "🔤 Text", ToolMode.TEXT);
            addMode(toolRow, "🧽 Eraser", ToolMode.ERASER);

            JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightTools.setOpaque(false);

            colorButton.setPreferredSize(new Dimension(32, 32));
            colorButton.setBackground(currentColor);
            colorButton.setToolTipText("Pick color");
            colorButton.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
            colorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            colorButton.addActionListener(e -> {
                Color c = JColorChooser.showDialog(ScreenMarker.this, "Choose Color", currentColor);
                if (c != null) {
                    currentColor = c;
                    colorButton.setBackground(c);
                    currentMode = ToolMode.PENCIL;
                    refreshSelection();
                }
            });

            strokeSlider.setPreferredSize(new Dimension(100, 24));
            strokeSlider.setOpaque(false);
            strokeSlider.addChangeListener(e -> {
                currentStroke = strokeSlider.getValue();
                strokeLabel.setText(currentStroke + " px");
            });

            strokeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            strokeLabel.setPreferredSize(new Dimension(45, 24));

            eraseAll.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            eraseAll.setFocusPainted(false);
            eraseAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
            eraseAll.addActionListener(e -> canvas.clearCanvas());

            power.setFont(new Font("Segoe UI", Font.BOLD, 14));
            power.setForeground(new Color(200, 50, 50));
            power.setFocusPainted(false);
            power.setCursor(new Cursor(Cursor.HAND_CURSOR));
            power.addActionListener(e -> System.exit(0));

            rightTools.add(new JLabel("Size:"));
            rightTools.add(strokeSlider);
            rightTools.add(strokeLabel);
            rightTools.add(colorButton);
            rightTools.add(Box.createHorizontalStrut(10));
            rightTools.add(eraseAll);
            rightTools.add(power);

            topRow.add(brand, BorderLayout.WEST);
            topRow.add(toolRow, BorderLayout.CENTER);
            topRow.add(rightTools, BorderLayout.EAST);

            add(topRow, BorderLayout.CENTER);

            refreshSelection();
        }

        private void addMode(JPanel panel, String text, ToolMode mode) {
            JToggleButton b = new JToggleButton(text);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setMargin(new Insets(4, 8, 4, 8));
            b.addActionListener(e -> setMode(mode));
            modeButtons.put(mode, b);
            panel.add(b);
        }

        void refreshSelection() {
            for (Map.Entry<ToolMode, JToggleButton> entry : modeButtons.entrySet()) {
                entry.getValue().setSelected(entry.getKey() == currentMode);
                if (entry.getKey() == currentMode) {
                    entry.getValue().setBackground(new Color(200, 220, 255));
                } else {
                    entry.getValue().setBackground(UIManager.getColor("ToggleButton.background"));
                }
            }
        }
    }

    class CanvasPanel extends JPanel {
        private BufferedImage image;
        private Graphics2D g2;
        private int oldX, oldY, startX, startY;
        private boolean drawing;

        CanvasPanel() {
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    startX = oldX = e.getX();
                    startY = oldY = e.getY();
                    drawing = true;
                    if (currentMode == ToolMode.TEXT) {
                        drawText(e.getX(), e.getY());
                        drawing = false;
                        repaint();
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if (!drawing || g2 == null) return;
                    if (currentMode == ToolMode.LINE) drawLineShape(startX, startY, e.getX(), e.getY());
                    else if (currentMode == ToolMode.ARROW) drawArrow(startX, startY, e.getX(), e.getY());
                    else if (currentMode == ToolMode.RECTANGLE) drawRect(startX, startY, e.getX(), e.getY());
                    else if (currentMode == ToolMode.ELLIPSE) drawEllipse(startX, startY, e.getX(), e.getY());
                    drawing = false;
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (g2 == null) return;
                    if (currentMode == ToolMode.PENCIL || currentMode == ToolMode.HIGHLIGHTER || currentMode == ToolMode.ERASER) {
                        drawFreeLine(oldX, oldY, e.getX(), e.getY());
                        oldX = e.getX();
                        oldY = e.getY();
                        repaint();
                    }
                }
            });
        }

        private void drawFreeLine(int x1, int y1, int x2, int y2) {
            if (currentMode == ToolMode.ERASER) {
                g2.setComposite(AlphaComposite.Clear);
                g2.setStroke(new BasicStroke(currentStroke * 8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            } else if (currentMode == ToolMode.HIGHLIGHTER) {
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setPaint(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 90));
                g2.setStroke(new BasicStroke(currentStroke * 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            } else {
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setPaint(currentColor);
                g2.setStroke(new BasicStroke(currentStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            g2.drawLine(x1, y1, x2, y2);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        private void drawLineShape(int x1, int y1, int x2, int y2) {
            g2.setPaint(currentColor);
            g2.setStroke(new BasicStroke(currentStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x1, y1, x2, y2);
        }

        private void drawArrow(int x1, int y1, int x2, int y2) {
            drawLineShape(x1, y1, x2, y2);
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int len = 14;
            int xA = (int) (x2 - len * Math.cos(angle - Math.PI / 6));
            int yA = (int) (y2 - len * Math.sin(angle - Math.PI / 6));
            int xB = (int) (x2 - len * Math.cos(angle + Math.PI / 6));
            int yB = (int) (y2 - len * Math.sin(angle + Math.PI / 6));
            g2.drawLine(x2, y2, xA, yA);
            g2.drawLine(x2, y2, xB, yB);
        }

        private void drawRect(int x1, int y1, int x2, int y2) {
            g2.setPaint(currentColor);
            g2.setStroke(new BasicStroke(currentStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
        }

        private void drawEllipse(int x1, int y1, int x2, int y2) {
            g2.setPaint(currentColor);
            g2.setStroke(new BasicStroke(currentStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
        }

        private void drawText(int x, int y) {
            String text = JOptionPane.showInputDialog(ScreenMarker.this, "Enter text:");
            if (text == null || text.trim().isEmpty()) return;
            g2.setPaint(currentColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, (int) currentStroke * 5)));
            g2.drawString(text, x, y);
        }

        public void clearCanvas() {
            if (g2 != null) {
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.SrcOver);
                repaint();
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                g2 = image.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g.drawImage(image, 0, 0, null);
        }
    }
}
