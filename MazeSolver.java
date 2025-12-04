package Maze;
import java.util.*;
import java.util.function.Consumer;
// import javax.swing.SwingUtilities;

/**
 * MazeSolver
 *  - 0 = passage, 1 = wall, 3 = path, 4 = visited
 *  - Supports animated solving with a callback that receives the maze each step.
 */
public class MazeSolver {

    private static final int PASSAGE = 0;
    private static final int WALL = 1;
    private static final int PATH = 3;
    private static final int VISITED = 4;

    private final int[][] dirs = {{1,0},{0,1},{-1,0},{0,-1}};

    // Immediate (no animation)
    public boolean solve(int[][] maze, int sx, int sy, int ex, int ey) {
        if (maze == null) return false;
        return dfs(maze, sx, sy, ex, ey);
    }

    private boolean dfs(int[][] maze, int x, int y, int ex, int ey) {
        if (!in(maze,x,y) || maze[x][y] == WALL || maze[x][y] == VISITED) return false;
        if (x == ex && y == ey) {
            maze[x][y] = PATH;
            return true;
        }
        maze[x][y] = VISITED;
        for (int[] d : dirs) {
            if (dfs(maze, x + d[0], y + d[1], ex, ey)) {
                maze[x][y] = PATH;
                return true;
            }
        }
        return false;
    }

    // Animated iterative DFS (original)
    public boolean solveAnimated(int[][] maze, int sx, int sy, int ex, int ey, int delayMs, Consumer<int[][]> stepCallback) {
        if (maze == null) return false;
        int rows = maze.length, cols = maze[0].length;
        boolean[][] visited = new boolean[rows][cols];
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(new Node(sx, sy, null));

        while (!stack.isEmpty()) {
            Node cur = stack.pop();
            int r = cur.r, c = cur.c;
            if (!in(maze,r,c) || visited[r][c] || maze[r][c] == WALL) continue;
            visited[r][c] = true;

            if (maze[r][c] == PASSAGE) maze[r][c] = VISITED;
            if (stepCallback != null) stepCallback.accept(maze);
            sleep(delayMs);

            if (r == ex && c == ey) {
                Node p = cur;
                while (p != null) {
                    maze[p.r][p.c] = PATH;
                    p = p.prev;
                }
                if (stepCallback != null) stepCallback.accept(maze);
                return true;
            }

            for (int i = 0; i < dirs.length; i++) {
                int nr = r + dirs[i][0], nc = c + dirs[i][1];
                if (in(maze,nr,nc) && !visited[nr][nc] && maze[nr][nc] != WALL) {
                    stack.push(new Node(nr, nc, cur));
                }
            }
        }
        return false;
    }

    /* -----------------------------------------------------------
     *  NEW FEATURE #1: RECURSIVE DFS WITH ANIMATION
     * ----------------------------------------------------------- */
    public boolean solveAnimatedDFS(int[][] maze, int sx, int sy, int ex, int ey, int delayMs, Consumer<int[][]> cb) {
        boolean[][] visited = new boolean[maze.length][maze[0].length];
        return dfsAnim(maze, sx, sy, ex, ey, visited, delayMs, cb);
    }

    private boolean dfsAnim(int[][] maze, int x, int y, int ex, int ey,
                            boolean[][] visited, int delay, Consumer<int[][]> cb) {

        if (!in(maze,x,y) || visited[x][y] || maze[x][y] == WALL) return false;

        visited[x][y] = true;
        if (maze[x][y] == PASSAGE) maze[x][y] = VISITED;
        if (cb != null) cb.accept(maze);
        sleep(delay);

        if (x == ex && y == ey) {
            maze[x][y] = PATH;
            return true;
        }

        for (int[] d : dirs) {
            if (dfsAnim(maze, x + d[0], y + d[1], ex, ey, visited, delay, cb)) {
                maze[x][y] = PATH;
                return true;
            }
        }
        return false;
    }



    /* -----------------------------------------------------------
     *  NEW FEATURE #2: BFS WITH ANIMATION (Shortest Path)
     * ----------------------------------------------------------- */
    public boolean solveAnimatedBFS(int[][] maze, int sx, int sy, int ex, int ey, int delayMs, Consumer<int[][]> cb) {
        int rows = maze.length, cols = maze[0].length;
        boolean[][] visited = new boolean[rows][cols];
        Queue<Node> q = new ArrayDeque<>();
        q.add(new Node(sx, sy, null));

        while (!q.isEmpty()) {
            Node cur = q.poll();
            int r = cur.r, c = cur.c;

            if (!in(maze,r,c) || visited[r][c] || maze[r][c] == WALL) continue;
            visited[r][c] = true;

            if (maze[r][c] == PASSAGE) maze[r][c] = VISITED;
            if (cb != null) cb.accept(maze);
            sleep(delayMs);

            if (r == ex && c == ey) {
                // reconstruct shortest path
                Node p = cur;
                while (p != null) {
                    maze[p.r][p.c] = PATH;
                    p = p.prev;
                }
                if (cb != null) cb.accept(maze);
                return true;
            }

            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (in(maze,nr,nc) && !visited[nr][nc] && maze[nr][nc] != WALL) {
                    q.add(new Node(nr, nc, cur));
                }
            }
        }
        return false;
    }


    /* ----------------------------------------------------------- */

    private boolean in(int[][] maze, int r, int c) {
        return r >= 0 && r < maze.length && c >= 0 && c < maze[0].length;
    }

    private void sleep(int ms) {
        try { Thread.sleep(Math.max(0, ms)); } catch (InterruptedException ignored) {}
    }

    private static class Node {
        int r, c;
        Node prev;
        Node(int r, int c, Node p) { this.r = r; this.c = c; this.prev = p; }
    }
}
