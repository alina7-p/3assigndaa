/**
 * Mission 0. Disjoint set union (DSU).
 *
 * Cells are numbered 0 .. n-1.
 * Union by size (the smaller tree goes under the larger one) and path compression in find.
 */
public class DisjointSet {

    private final int[] parent;
    private final int[] size;
    private int numSets;

    public DisjointSet(int n) {
        parent = new int[n];
        size = new int[n];
        numSets = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    /** The root of the set that contains x. Implements path compression. */
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Path compression
        }
        return parent[x];
    }

    /**
     * Joins the sets of a and b.
     * Returns true if they were different sets, false if they were already one set.
     * Uses union by size.
     */
    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);

        if (rootA == rootB) {
            return false;
        }

        // Union by size: attach smaller tree under larger tree
        if (size[rootA] < size[rootB]) {
            parent[rootA] = rootB;
            size[rootB] += size[rootA];
        } else {
            parent[rootB] = rootA;
            size[rootA] += size[rootB];
        }

        numSets--;
        return true;
    }

    /** Checks if a and b belong to the same set. */
    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    /** Number of sets now. */
    public int count() {
        return numSets;
    }
}