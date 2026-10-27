import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet dsu = new DisjointSet(w.cells());
        List<Integer> broken = new ArrayList<>();

        for (int i = 0; i < w.count(); i++) {
            if (dsu.union(w.a()[i], w.b()[i])) {
                broken.add(i);
            }
        }

        return broken.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] wallIndexes = new Integer[w.count()];
        for (int i = 0; i < w.count(); i++) {
            wallIndexes[i] = i;
        }

        // Sort wall indexes by their breaking cost
        Arrays.sort(wallIndexes, Comparator.comparingLong(i -> w.cost()[i]));

        DisjointSet dsu = new DisjointSet(w.cells());
        List<Integer> broken = new ArrayList<>();

        for (int idx : wallIndexes) {
            if (dsu.union(w.a()[idx], w.b()[idx])) {
                broken.add(idx);
            }
        }

        return broken.stream().mapToInt(Integer::intValue).toArray();
    }

    // Helper class for Prim's algorithm
    private static class Edge {
        int to;
        long cost;

        Edge(int to, long cost) {
            this.to = to;
            this.cost = cost;
        }
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        if (n == 0) return 0;

        // Build adjacency list
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        for (int i = 0; i < w.count(); i++) {
            int u = w.a()[i];
            int v = w.b()[i];
            long cost = w.cost()[i];
            adj.get(u).add(new Edge(v, cost));
            adj.get(v).add(new Edge(u, cost));
        }

        boolean[] visited = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingLong(e -> e.cost));

        long totalCost = 0;
        int visitedCount = 0;

        // Start from cell 0
        visited[0] = true;
        visitedCount++;
        pq.addAll(adj.get(0));

        while (!pq.isEmpty() && visitedCount < n) {
            Edge edge = pq.poll();
            if (visited[edge.to]) continue;

            visited[edge.to] = true;
            visitedCount++;
            totalCost += edge.cost;

            for (Edge nextEdge : adj.get(edge.to)) {
                if (!visited[nextEdge.to]) {
                    pq.add(nextEdge);
                }
            }
        }

        return totalCost;
    }
}