import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The ready method Graphs.open(w, broken) gives you the open neighbours of every cell, in no particular order:
 * to visit them in the required order, go through the four cells above one by one and take those that are in the list.
 * The program must not overflow the stack on a maze of 1000 by 1000 cells.
 */
public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells();
        int cols = w.cols();
        int target = n - 1;

        // Get adjacency list for open passages in the maze
        int[][] openAdj = Graphs.open(w, broken);

        // 1. Count dead ends (cells with exactly 1 open neighbour)
        int deadEnds = 0;
        for (int i = 0; i < n; i++) {
            if (openAdj[i].length == 1) {
                deadEnds++;
            }
        }

        // 2. Iterative DFS to avoid StackOverflowError
        boolean[] visited = new boolean[n];
        int[] parent = new int[n];
        int[] nextNeighborIdx = new int[n];

        // Prepare ordered neighbors for each cell: Up, Right, Down, Left
        int[][] orderedNeighbors = new int[n][];
        for (int i = 0; i < n; i++) {
            Set<Integer> openSet = new HashSet<>();
            for (int neighbor : openAdj[i]) {
                openSet.add(neighbor);
            }

            int row = i / cols;
            int col = i % cols;

            List<Integer> list = new ArrayList<>();
            // Up
            if (row > 0 && openSet.contains(i - cols)) list.add(i - cols);
            // Right
            if (col < cols - 1 && openSet.contains(i + 1)) list.add(i + 1);
            // Down
            if (row < w.rows() - 1 && openSet.contains(i + cols)) list.add(i + cols);
            // Left
            if (col > 0 && openSet.contains(i - 1)) list.add(i - 1);

            orderedNeighbors[i] = list.stream().mapToInt(Integer::intValue).toArray();
        }

        Deque<Integer> stack = new ArrayDeque<>();
        int visitedCount = 0;

        stack.push(0);
        visited[0] = true;
        visitedCount++;

        boolean foundExit = false;

        while (!stack.isEmpty()) {
            int current = stack.peek();

            if (current == target) {
                foundExit = true;
                break;
            }

            int idx = nextNeighborIdx[current];
            if (idx < orderedNeighbors[current].length) {
                int next = orderedNeighbors[current][idx];
                nextNeighborIdx[current]++;

                if (!visited[next]) {
                    visited[next] = true;
                    visitedCount++;
                    parent[next] = current;
                    stack.push(next);
                }
            } else {
                stack.pop();
            }
        }

        // Reconstruct path if exit found
        if (!foundExit) {
            return new Result(new int[0], visitedCount, deadEnds);
        }

        List<Integer> pathList = new ArrayList<>();
        int curr = target;
        while (curr != 0) {
            pathList.add(curr);
            curr = parent[curr];
        }
        pathList.add(0);

        int[] path = new int[pathList.size()];
        for (int i = 0; i < pathList.size(); i++) {
            path[i] = pathList.get(pathList.size() - 1 - i);
        }

        return new Result(path, visitedCount, deadEnds);
    }
}