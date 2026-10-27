import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Missions 4 and 5. The slides: a directed graph.
 */
public class Zones {

    /**
     * Mission 4. Kosaraju algorithm to find strongly connected components (zones).
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] slidesFrom = Graphs.slidesFrom(s);
        int[][] slidesInto = Graphs.slidesInto(s);

        // First pass: Iterative DFS to record post-order completion
        boolean[] visited = new boolean[n];
        List<Integer> finishedOrder = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                Deque<Integer> stack = new ArrayDeque<>();
                Deque<Integer> neighborIdxStack = new ArrayDeque<>();

                stack.push(i);
                neighborIdxStack.push(0);
                visited[i] = true;

                while (!stack.isEmpty()) {
                    int curr = stack.peek();
                    int idx = neighborIdxStack.pop();

                    if (idx < slidesFrom[curr].length) {
                        neighborIdxStack.push(idx + 1);
                        int slideIdx = slidesFrom[curr][idx];
                        int next = s.to()[slideIdx];

                        if (!visited[next]) {
                            visited[next] = true;
                            stack.push(next);
                            neighborIdxStack.push(0);
                        }
                    } else {
                        stack.pop();
                        finishedOrder.add(curr);
                    }
                }
            }
        }

        // Second pass: Iterative DFS on reversed graph (slidesInto)
        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int currentZone = 0;

        for (int i = finishedOrder.size() - 1; i >= 0; i--) {
            int startNode = finishedOrder.get(i);
            if (zoneOf[startNode] == -1) {
                Deque<Integer> stack = new ArrayDeque<>();
                stack.push(startNode);
                zoneOf[startNode] = currentZone;

                while (!stack.isEmpty()) {
                    int curr = stack.pop();

                    for (int slideIdx : slidesInto[curr]) {
                        int prevNode = s.from()[slideIdx];
                        if (zoneOf[prevNode] == -1) {
                            zoneOf[prevNode] = currentZone;
                            stack.push(prevNode);
                        }
                    }
                }
                currentZone++;
            }
        }

        return zoneOf;
    }

    /**
     * Mission 5. Graph of zones and cheapest route/flow stages.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        int n = s.n();

        // Build DAG of zones with cheapest edge costs
        Map<Long, Long> minCostEdgeMap = new HashMap<>();
        List<List<Integer>> zoneAdj = new ArrayList<>();
        for (int i = 0; i < zoneCount; i++) {
            zoneAdj.add(new ArrayList<>());
        }

        for (int i = 0; i < s.count(); i++) {
            int u = zoneOf[s.from()[i]];
            int v = zoneOf[s.to()[i]];

            if (u != v) {
                long key = (((long) u) << 32) | (v & 0xffffffffL);
                long cost = s.time()[i];

                if (!minCostEdgeMap.containsKey(key)) {
                    minCostEdgeMap.put(key, cost);
                    zoneAdj.get(u).add(v);
                } else {
                    minCostEdgeMap.put(key, Math.min(minCostEdgeMap.get(key), cost));
                }
            }
        }

        // Longest path in DAG (Flow stages)
        int[] maxStages = new int[zoneCount];
        Arrays.fill(maxStages, 1);

        for (int u = 0; u < zoneCount; u++) {
            for (int v : zoneAdj.get(u)) {
                maxStages[v] = Math.max(maxStages[v], maxStages[u] + 1);
            }
        }

        int flowStages = 0;
        for (int stage : maxStages) {
            flowStages = Math.max(flowStages, stage);
        }

        // Cheapest route from zone of start (cell 0) to zone of exit (cell n - 1)
        int startZone = zoneOf[0];
        int endZone = zoneOf[n - 1];

        long[] minRouteCost = new long[zoneCount];
        Arrays.fill(minRouteCost, Long.MAX_VALUE / 2); // Avoid overflow on addition
        minRouteCost[startZone] = 0;

        for (int u = startZone; u < zoneCount; u++) {
            if (minRouteCost[u] == Long.MAX_VALUE / 2) continue;

            for (int v : zoneAdj.get(u)) {
                long key = (((long) u) << 32) | (v & 0xffffffffL);
                long edgeCost = minCostEdgeMap.get(key);

                if (minRouteCost[u] + edgeCost < minRouteCost[v]) {
                    minRouteCost[v] = minRouteCost[u] + edgeCost;
                }
            }
        }

        long fastestRouteCost = minRouteCost[endZone];
        return new long[]{flowStages, fastestRouteCost};
    }
}
