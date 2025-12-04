package Maze;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MazePanel extends JPanel {

    private int[][] maze;
    private int cellSize = 16;
    private boolean drawGrid = true;

    // Start/End positions
    private int startX = 1, startY = 1;
    private int endX = 1, endY = 1;

    public MazePanel() {
        setBackground(Color.BLACK);
    }

    public void setMaze(int[][] maze) {
        if (maze == null) return;
        this.maze = maze;
        this.endX = maze.length - 2;
        this.endY = maze[0].length - 2;
        repaint();
    }

    public void setCellSize(int size) {
        this.cellSize = size;
        revalidate();
        repaint();
    }

    public int getCellSize() {
        return cellSize;
    }

    public void setDrawGrid(boolean draw) {
        this.drawGrid = draw;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (maze == null) return new Dimension(400, 400);
        return new Dimension(maze[0].length * cellSize, maze.length * cellSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (maze == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float pulse = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 200.0));
        int glowLayers = 6;
        int glowSpread = 8;

        for (int r = 0; r < maze.length; r++) {
            for (int c = 0; c < maze[0].length; c++) {
                int x = c * cellSize;
                int y = r * cellSize;

                Color baseColor = null;
                Color glowColor = null;

                switch (maze[r][c]) {
                    case 1: baseColor = new Color(0x1A1A1D); break;      // WALL
                    case 0: baseColor = new Color(0xF2F2F3); break;      // PASSAGE
                    case 4: baseColor = new Color(0x00AEEF); glowColor = new Color(0,174,239); break; // VISITED
                    case 3: baseColor = new Color(0xFF3366); glowColor = new Color(255,51,102); break; // PATH
                }

                // Start/End overrides
                if (r == startX && c == startY) { 
                    baseColor = new Color(0x6CFF6C); 
                    glowColor = new Color(108,255,108); 
                }
                if (r == endX && c == endY) { 
                    baseColor = new Color(0xFFEA00); 
                    glowColor = new Color(255,234,0); 
                }

                // --- Bloom / Glow ---
                if (glowColor != null) {
                    for (int i = glowLayers; i >= 1; i--) {
                        float alpha = (0.08f * pulse) / i;
                        alpha = Math.min(alpha, 1.0f);
                        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int)(alpha * 255)));
                        int offset = (glowSpread * i) / glowLayers;
                        g2.fillRoundRect(x - offset, y - offset, cellSize + 2*offset, cellSize + 2*offset, cellSize/2, cellSize/2);
                    }
                }

                // --- Draw main tile ---
                g2.setColor(baseColor);
                g2.fillRect(x, y, cellSize, cellSize);

                // Optional grid
                if (drawGrid) {
                    g2.setColor(new Color(50,50,50,80));
                    g2.drawRect(x, y, cellSize, cellSize);
                }
            }
        }

        g2.dispose();

        // Trigger repaint for animation
        if (maze != null) repaint();
    }

    // Optional: save panel as image
    public boolean saveAsImage(String path) {
        if (maze == null) return false;
        int w = maze[0].length * cellSize;
        int h = maze.length * cellSize;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        paintComponent(g2);
        g2.dispose();
        try {
            javax.imageio.ImageIO.write(img, "png", new java.io.File(path));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
