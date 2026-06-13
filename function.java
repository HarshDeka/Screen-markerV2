import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class function extends JWindow { // Turned into a JWindow for absolute full-screen layering
    private BufferedImage drawingBuffer;
    private Graphics2D g2;
    private int lastX, lastY;
    private boolean drawingEnabled = false; 

    public function() {
        // Grab full monitor resolution
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setLocation(0, 0);
        
        // Ensure the window frame itself is completely transparent
        setBackground(new Color(0, 0, 0, 0)); 
        setAlwaysOnTop(true);

        // Internal drawing panel setup
        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Forces the OS to register this as a mouse-intercepting surface
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());

                if (drawingBuffer != null) {
                    g2d.drawImage(drawingBuffer, 0, 0, null);
                }
                g2d.dispose();
            }
        };

        canvasPanel.setOpaque(false);
        // Alpha value 1 catches clicks securely so you don't accidental-click desktop files
        canvasPanel.setBackground(new Color(0, 0, 0, 1)); 

        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (drawingEnabled) {
                    initBufferIfNeeded();
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }
        });

        canvasPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawingEnabled && g2 != null) {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(lastX, lastY, e.getX(), e.getY());
                    lastX = e.getX();
                    lastY = e.getY();
                    repaint(); 
                }
            }
        });

        setContentPane(canvasPanel);
    }

    public void setDrawingEnabled(boolean enabled) {
        this.drawingEnabled = enabled;
        // CRITICAL FIX: If drawing is disabled, let clicks pass through to background files.
        // If drawing is enabled, block input passes so you don't accidentally click files while marking!
        this.setFocusable(enabled);
    }

    private void initBufferIfNeeded() {
        if (drawingBuffer == null || drawingBuffer.getWidth() != getWidth() || drawingBuffer.getHeight() != getHeight()) {
            drawingBuffer = new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()), BufferedImage.TYPE_INT_ARGB);
            g2 = drawingBuffer.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, drawingBuffer.getWidth(), drawingBuffer.getHeight());
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }
}