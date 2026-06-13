import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.*;

public class function extends JWindow {
    private BufferedImage drawingBuffer;
    private Graphics2D g2;
    private final JPanel canvasPanel; 

    private int lastX, lastY;
    private int startX, startY; 
    private int previewX, previewY; 
    private boolean isDrawingShape = false;
    private boolean drawingEnabled = false; 
    private String currentMode = "None";
    
    private JFrame mainToolbarFrame;
    private String savedSnapshotDirectory = null;

    private final Stack<BufferedImage> undoStack = new Stack<>();

    private boolean isSmoothingEnabled = false;
    private double smoothedX, smoothedY;
    private final double SMOOTH_FACTOR = 0.25; 

    private Color baseSelectedColor = Color.RED; 
    private Color currentBrushColor = Color.RED; 
    private float currentBrushThickness = 10.0f;

    public function() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setLocation(0, 0);
        
        setBackground(new Color(0, 0, 0, 0)); 
        setAlwaysOnTop(true);

        canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());

                if (drawingBuffer != null) {
                    g2d.drawImage(drawingBuffer, 0, 0, null);
                }

                if (isDrawingShape) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(currentBrushColor);
                    g2d.setStroke(new BasicStroke(currentBrushThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    int x = Math.min(startX, previewX);
                    int y = Math.min(startY, previewY);
                    int w = Math.abs(startX - previewX);
                    int h = Math.abs(startY - previewY);

                    switch (currentMode) {
                        case "Line" -> g2d.drawLine(startX, startY, previewX, previewY);
                        case "Arrow" -> drawArrowHead(g2d, startX, startY, previewX, previewY);
                        case "Rect" -> g2d.drawRect(x, y, w, h);
                        case "Oval" -> g2d.drawOval(x, y, w, h);
                    }
                }
                g2d.dispose();
            }
        };

        canvasPanel.setOpaque(false);
        // FIX: Start with shield down so PC is usable immediately
        canvasPanel.setBackground(new Color(0, 0, 0, 0)); 

        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (drawingEnabled) {
                    initBufferIfNeeded();
                    undoStack.push(cloneImage(drawingBuffer));
                    if (undoStack.size() > 20) undoStack.remove(0); 

                    startX = e.getX();
                    startY = e.getY();
                    lastX = startX;
                    lastY = startY;
                    smoothedX = startX;
                    smoothedY = startY;
                    
                    if (isShapeMode(currentMode)) {
                        isDrawingShape = true;
                        previewX = startX;
                        previewY = startY;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drawingEnabled && isDrawingShape && g2 != null) {
                    isDrawingShape = false;
                    g2.setColor(currentBrushColor);
                    g2.setStroke(new BasicStroke(currentBrushThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    int x = Math.min(startX, e.getX());
                    int y = Math.min(startY, e.getY());
                    int w = Math.abs(startX - e.getX());
                    int h = Math.abs(startY - e.getY());

                    switch (currentMode) {
                        case "Line" -> g2.drawLine(startX, startY, e.getX(), e.getY());
                        case "Arrow" -> drawArrowHead(g2, startX, startY, e.getX(), e.getY());
                        case "Rect" -> g2.drawRect(x, y, w, h);
                        case "Oval" -> g2.drawOval(x, y, w, h);
                    }
                    
                    repaint();
                }
            }
        });

        canvasPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawingEnabled && g2 != null) {
                    if (isShapeMode(currentMode)) {
                        previewX = e.getX();
                        previewY = e.getY();
                        repaint();
                    } else {
                        Composite oldComposite = g2.getComposite();
                        if ("High".equals(currentMode)) g2.setComposite(AlphaComposite.Src); 
                        else if ("Eraser".equals(currentMode)) g2.setComposite(AlphaComposite.Clear); 
                        
                        g2.setColor(currentBrushColor);
                        g2.setStroke(new BasicStroke(currentBrushThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        
                        int nextX = e.getX();
                        int nextY = e.getY();
                        
                        if (isSmoothingEnabled && !"Eraser".equals(currentMode)) {
                            smoothedX = smoothedX + SMOOTH_FACTOR * (nextX - smoothedX);
                            smoothedY = smoothedY + SMOOTH_FACTOR * (nextY - smoothedY);
                            int currentX = (int) Math.round(smoothedX);
                            int currentY = (int) Math.round(smoothedY);
                            g2.drawLine(lastX, lastY, currentX, currentY);
                            lastX = currentX;
                            lastY = currentY;
                        } else {
                            g2.drawLine(lastX, lastY, nextX, nextY);
                            lastX = nextX;
                            lastY = nextY;
                        }
                        g2.setComposite(oldComposite); 
                        repaint(); 
                    }
                }
            }
        });

        setContentPane(canvasPanel);
    }

    public void setMainToolbarFrame(JFrame frame) {
        this.mainToolbarFrame = frame;
    }

    private boolean isShapeMode(String mode) {
        return "Line".equals(mode) || "Arrow".equals(mode) || "Rect".equals(mode) || "Oval".equals(mode);
    }

    public void setBrushColor(Color newColor) {
        this.baseSelectedColor = newColor;
        setDrawingMode(this.currentMode); 
    }

    public void setDrawingMode(String mode) {
        this.currentMode = mode;
        if ("None".equals(mode)) {
            this.drawingEnabled = false;
            // FIX: Drops the invisible shield completely! Clicks pass through to VS Code/Desktop.
            if (canvasPanel != null) canvasPanel.setBackground(new Color(0, 0, 0, 0)); 
        } else {
            this.drawingEnabled = true;
            // Raises the shield to catch your mouse clicks for drawing
            if (canvasPanel != null) canvasPanel.setBackground(new Color(0, 0, 0, 1)); 
            
            currentBrushColor = switch (mode) {
                case "Eraser" -> new Color(0, 0, 0, 0);
                case "High" -> new Color(baseSelectedColor.getRed(), baseSelectedColor.getGreen(), baseSelectedColor.getBlue(), 80);
                default -> baseSelectedColor;
            };
        }
        if (canvasPanel != null) canvasPanel.repaint();
    }

    public void adjustBrushThickness(int rotation) {
        currentBrushThickness -= rotation * 2.0f;
        if (currentBrushThickness < 2.0f) currentBrushThickness = 2.0f;
        if (currentBrushThickness > 80.0f) currentBrushThickness = 80.0f;
    }

    public int getBrushThickness() {
        return (int) currentBrushThickness;
    }

    public void undoLastAction() {
        if (drawingBuffer != null && !undoStack.isEmpty()) {
            g2.dispose();
            drawingBuffer = undoStack.pop();
            g2 = drawingBuffer.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            repaint();
        }
    }

    public void setSmoothingActive(boolean active) {
        this.isSmoothingEnabled = active;
    }

    public void clearCanvas() {
        if (g2 != null) {
            initBufferIfNeeded();
            undoStack.push(cloneImage(drawingBuffer)); 
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, drawingBuffer.getWidth(), drawingBuffer.getHeight());
            g2.setComposite(AlphaComposite.SrcOver);
            repaint();
        }
    }

    private BufferedImage cloneImage(BufferedImage img) {
        ColorModel cm = img.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster r = img.copyData(img.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, r, isAlphaPremultiplied, null);
    }

    public void takeSnapshot() {
        try {
            this.setAlwaysOnTop(false);
            if (mainToolbarFrame != null) mainToolbarFrame.setAlwaysOnTop(false);
            this.setVisible(false);
            if (mainToolbarFrame != null) mainToolbarFrame.setVisible(false);
            Thread.sleep(150); 
            
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
            
            if (drawingBuffer != null) {
                Graphics2D sg = screenFullImage.createGraphics();
                sg.drawImage(drawingBuffer, 0, 0, null);
                sg.dispose();
            }

            if (savedSnapshotDirectory == null) {
                FileDialog fileDialog = new FileDialog((Frame) null, "Select Snapshot Default Folder", FileDialog.SAVE);
                fileDialog.setFile("Select This Folder.png");
                fileDialog.setVisible(true);
                String directory = fileDialog.getDirectory();
                if (directory != null) savedSnapshotDirectory = directory; 
            }

            if (savedSnapshotDirectory != null) {
                String autoFileName = "Snapshot_" + System.currentTimeMillis() + ".png";
                File fileToSave = new File(savedSnapshotDirectory, autoFileName);
                ImageIO.write(screenFullImage, "png", fileToSave);
            }
            
        } catch (AWTException | IOException | InterruptedException ex) {
            System.err.println("Error capturing screenshot: " + ex.getMessage());
        } finally {
            this.setVisible(true);
            this.setAlwaysOnTop(true);
            
            if (mainToolbarFrame != null) {
                mainToolbarFrame.setVisible(true);
                mainToolbarFrame.setAlwaysOnTop(true);
                mainToolbarFrame.toFront();
                mainToolbarFrame.requestFocus();
            }
            this.toFront();
        }
    }

    private void drawArrowHead(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int arrowSize = 18; 
        int x3 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));
        g2d.drawLine(x2, y2, x3, y3);
        g2d.drawLine(x2, y2, x4, y4);
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