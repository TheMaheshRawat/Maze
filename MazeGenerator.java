package Maze;
import java.util.*;
import java.util.function.Consumer;

/**
 * MazeGenerator
 *  - Uses recursive DFS backtracking on a grid.
 *  - Cells: 1 = wall, 0 = passage
 *  - Supports an animated generation callback:
 *      generateAnimated(startR,startC,delayMs,stepCallback)
 *    where stepCallback.accept(maze) will be invoked periodically on the generator thread;
 *    UI should re-render via SwingUtilities.invokeLater.
 */
public class MazeGenerator {

    private final int N;
    private final int M;
    private int[][] maze;
    private final Random rand = new Random();

    public MazeGenerator(int n, int m) {
        if (n < 3) n = 3;
        if (m < 3) m = 3;
        this.N = n;
        this.M = m;
        initMaze();
    }

    private void initMaze() {
        maze = new int[N][M];
        for (int i = 0; i < N; i++)
            Arrays.fill(maze[i], 1);
    }

    public int[][] generateMaze(int startR, int startC) {
        initMaze();
        carve(startR, startC);
        return maze;
    }

    // Animated generation runs on caller thread. Use SwingUtilities.invokeLater from callback.
    public void generateAnimated(int startR, int startC, int delayMs, Consumer<int[][]> stepCallback) {
        initMaze();
        // Use iterative stack to avoid deep recursion for large mazes and to emit steps easily
        Deque<int[]> stack = new ArrayDeque<>();
        maze[startR][startC] = 0;
        stack.push(new int[]{startR, startC});
        if (stepCallback != null) stepCallback.accept(maze);

        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int r = cur[0], c = cur[1];
            List<int[]> neighbors = new ArrayList<>();

            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] d : dirs) {
                int nr = r + d[0]*2;
                int nc = c + d[1]*2;
                if (within(nr,nc) && maze[nr][nc] == 1) neighbors.add(d);
            }

            if (!neighbors.isEmpty()) {
                int[] d = neighbors.get(rand.nextInt(neighbors.size()));
                int betweenR = r + d[0];
                int betweenC = c + d[1];
                int nextR = r + d[0]*2;
                int nextC = c + d[1]*2;

                maze[betweenR][betweenC] = 0;
                maze[nextR][nextC] = 0;

                // optional marker for current cursor (value 5)
                maze[r][c] = 0;
                maze[betweenR][betweenC] = 0;
                maze[nextR][nextC] = 5; // cursor

                if (stepCallback != null) stepCallback.accept(maze);

                try { Thread.sleep(Math.max(0, delayMs)); } catch (InterruptedException ignored) {}

                // turn cursor back to passage, push next
                maze[nextR][nextC] = 0;
                stack.push(new int[]{nextR, nextC});
            } else {
                // backtrack; pop
                stack.pop();
                if (stepCallback != null) {
                    // small delay so user sees backtracking
                    try { Thread.sleep(Math.max(0, delayMs/2)); } catch (InterruptedException ignored) {}
                    stepCallback.accept(maze);
                }
            }
        }
        // ensure cursor removed
        for (int i = 0; i < N; i++) for (int j = 0; j < M; j++) if (maze[i][j] == 5) maze[i][j] = 0;
        if (stepCallback != null) stepCallback.accept(maze);
    }

    private boolean within(int r, int c) {
        return r >= 0 && r < N && c >= 0 && c < M;
    }

    // Simple recursive carve used by quick generation (non-animated)
    private void carve(int r, int c) {
        maze[r][c] = 0;
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        shuffle(dirs);
        for (int[] d : dirs) {
            int nr = r + d[0]*2;
            int nc = c + d[1]*2;
            if (within(nr,nc) && maze[nr][nc] == 1) {
                maze[r + d[0]][c + d[1]] = 0;
                carve(nr,nc);
            }
        }
    }

    private void shuffle(int[][] a) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int[] t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }
}
