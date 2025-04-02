package stud.atmt.atmtlite;
import java.io.*;
import java.util.*;

/**
 * Generalized graph class supporting any type of data. Working only with positive weights
 *
 * @param <T> the type of the data stored in the vertices
 */
class Graph<T> implements Cloneable {
    private int MAX_GRAPH_SIZE = 1000; // Maximum allowed number of vertices.

    // List of vertices
    private final List<T> vertexList;

    // Adjacency matrix to store edge weights
    private final List<List<Double>> edges;

    // Current number of vertices
    private int graphSize;

    public List<List<Double>> getEdges() {
        return edges;
    }

    public List<T> getVertexList() {
        return vertexList;
    }

    /**
     * Create new graph with size = 1000. O(n)
     */
    public Graph() {
        vertexList = new ArrayList<>();
        edges = new ArrayList<>(MAX_GRAPH_SIZE);

        // Initialize adjacency matrix with zeros
        for (int i = 0; i < MAX_GRAPH_SIZE; i++) {
            edges.add(new ArrayList<>(Collections.nCopies(MAX_GRAPH_SIZE, 0.0)));
        }

        graphSize = 0;
    }

    /**
     * Create graph with pointed size. O(n)
     * @param maxGraphSize
     */
    public Graph(int maxGraphSize) {
        vertexList = new ArrayList<>();
        this.MAX_GRAPH_SIZE = maxGraphSize;
        edges = new ArrayList<>(MAX_GRAPH_SIZE);

        // Initialize adjacency matrix with zeros
        for (int i = 0; i < MAX_GRAPH_SIZE; i++) {
            edges.add(new ArrayList<>(Collections.nCopies(MAX_GRAPH_SIZE, 0.0)));
        }

        graphSize = 0;
    }

    /**
     * Finds the position of the vertex in the vertex list.
     *
     * @param vertex the vertex to search for
     * @return the index of the vertex, or -1 if not found
     */
    private int findVertex(T vertex) {
        return vertexList.indexOf(vertex); // O(n) for n vertices
    }

    /**
     * Returns the position of the vertex in the list, or throws IllegalArgumentException if not found.
     */
    private int getVertexPos(T vertex) {
        int pos = findVertex(vertex);
        if (pos == -1) {
            throw new IllegalArgumentException("Vertex not found: " + vertex);
        }
        return pos;
    }

    /**
     * Checks if the graph is empty.
     *
     * @return true if no vertices exist
     */
    public boolean isEmpty() {
        return graphSize == 0; // O(1)
    }

    /**
     * Checks if the graph has reached its maximum capacity. O(1)
     *
     * @return true if the graph is full
     */
    public boolean isFull() {
        return graphSize >= MAX_GRAPH_SIZE; // O(1)
    }

    /**
     * Returns the current number of vertices. O(1)
     */
    public int numberOfVertices() {
        return graphSize; // O(1)
    }

    /**
     * Returns the total number of edges. O(n)
     */
    public int numberOfEdges() {
        int count = 0;
        for (List<Double> row : edges) {
            count += (int) row.stream().filter(weight -> weight != 0).count(); // O(n)
        }
        return count / 2; // For undirected graphs, divide by 2
    }

    /**
     * Gets the weight of the edge between two vertices. O(1)
     * throws IllegalArgumentException if vertex not found
     * @return the weight, or 0 if no edge exists
     */
    public double getWeight(T vertex1, T vertex2) {
        int pos1 = getVertexPos(vertex1); // O(n)
        int pos2 = getVertexPos(vertex2); // O(n)
        return edges.get(pos1).get(pos2); // O(1)
    }

    /**
     * Retrieves all neighbors of a given vertex. O(n)
     *
     * @return a list of neighboring vertices
     */
    public List<T> getNeighbors(T vertex) {
        int pos = getVertexPos(vertex); // O(n)

        List<T> neighbors = new ArrayList<>();
        for (int i = 0; i < graphSize; i++) { // O(n)
            if (edges.get(pos).get(i) != 0) {
                neighbors.add(vertexList.get(i));
            }
        }
        return neighbors; // O(n)
    }

    /**
     * Inserts a new vertex into the graph.
     * Throws IllegalArgumentException if vertex exists or the graph is full (IllegalStateException)
     * Complexity: O(n) for matrix resizing.
     */
    public void insertVertex(T vertex) {
        if (findVertex(vertex) != -1) {
            throw new IllegalArgumentException("Vertex already exists: " + vertex);
        }

        if (isFull()) {
            throw new IllegalStateException("Graph is full");
        }

        vertexList.add(vertex);
        graphSize++;

        // Expand adjacency matrix for the new vertex
        for (List<Double> row : edges) {
            row.add(0.0); // O(1) per row
        }
        List<Double> newRow = new ArrayList<>(Collections.nCopies(MAX_GRAPH_SIZE, 0.0));
        edges.add(newRow); // O(1)
    }

    /**
     * Inserts an edge between two vertices with the given weight. Throws IllegalArgumentException if edge weight is negative. O(1)
     */
    public void insertEdge(T vertex1, T vertex2, double weight) {
        if (weight <= 0.0) {
            throw new IllegalArgumentException("Edge weight must be positive");
        }

        int pos1 = getVertexPos(vertex1);
        int pos2 = getVertexPos(vertex2);

        edges.get(pos1).set(pos2, weight);
        edges.get(pos2).set(pos1, weight);
    }

    /**
     * Delete pointed vertex. O(n)
     */
    public void deleteVertex(T vertex) {
        int pos = getVertexPos(vertex);

        // Remove the vertex and update adjacency matrix
        vertexList.remove(pos);
        graphSize--;

        // Remove related edges
        for (int i = 0; i < graphSize; i++) {
            edges.get(i).set(pos, 0.0);
            edges.get(pos).set(i, 0.0);
        }
    }

    /**
     * Delete edge between two pointed vertices. O(1)
     */
    public void deleteEdge(T vertex1, T vertex2) {
        int pos1 = getVertexPos(vertex1);
        int pos2 = getVertexPos(vertex2);

        edges.get(pos1).set(pos2, 0.0);
        edges.get(pos2).set(pos1, 0.0);
    }

    /**
     * Depth-first search based on stack. Throws IllegalArgumentException if graph is empty. O(n)
     */
    public List<T> depthFirstSearch(T startVertex) {
        if (graphSize == 0) {
            throw new IllegalArgumentException("Graph is empty. Cannot perform DFS.");
        }

        int startPos = getVertexPos(startVertex);

        boolean[] visited = new boolean[MAX_GRAPH_SIZE];
        List<T> result = new ArrayList<>(); // путь
        Stack<Integer> stack = new Stack<>(); // для возврата назад

        // алгоритм прохода по вершинам и возврата назад
        stack.push(startPos);
        while (!stack.isEmpty()) {
            int current = stack.pop();
            if (!visited[current]) { // если посетили, то дальше не идём, если нет, то идём
                visited[current] = true;
                result.add(vertexList.get(current));
                for (int i = graphSize - 1; i >= 0; i--) {
                    if (edges.get(current).get(i) != 0 && !visited[i]) {
                        stack.push(i);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Breadth-first search based on queue. Throws exception if graph is empty. O(n)
     */
    public List<T> breadthFirstSearch(T startVertex) {
        if (graphSize == 0) {
            throw new IllegalArgumentException("Graph is empty. Cannot perform BFS.");
        }

        int startPos = getVertexPos(startVertex);

        boolean[] visited = new boolean[MAX_GRAPH_SIZE];
        List<T> result = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(startPos);
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (!visited[current]) {
                visited[current] = true;
                result.add(vertexList.get(current));
                for (int i = 0; i < graphSize; i++) {
                    if (edges.get(current).get(i) != 0 && !visited[i]) {
                        queue.add(i);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Searches for optimal paths in the graph. Throws exception if error in algorithm O(n^2)
     */
    public Map<T, Double> dijkstra(T startVertex) {
        Map<T, Double> result = new HashMap<>();
        try {
            int startPos = getVertexPos(startVertex); // edited int -> double

            double[] distances = new double[MAX_GRAPH_SIZE];
            boolean[] visited = new boolean[MAX_GRAPH_SIZE];
            Arrays.fill(distances, Integer.MAX_VALUE); // initialize array with infinity
            distances[startPos] = 0;

            PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingDouble(pos -> distances[pos])); // set priority on comparing distances between vertices
            queue.add(startPos); // add first vertex

            while (!queue.isEmpty()) { // what this cycle do
                int current = queue.poll(); // poll nearest vertex
                if (visited[current]) continue; // skip if visited
                visited[current] = true;

                for (int i = 0; i < graphSize; i++) { // what this cycle do
                    if (edges.get(current).get(i) != 0 && !visited[i]) {
                        double newDist = distances[current] + edges.get(current).get(i);
                        if (newDist < distances[i]) {
                            distances[i] = newDist;
                            queue.add(i); // add neighbors
                        }
                    }
                }
            }

            for (int i = 0; i < graphSize; i++) {
                // Only add vertices that are reachable
                if (distances[i] != Integer.MAX_VALUE) {
                    result.put(vertexList.get(i), distances[i]);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error in Dijkstra algorithm: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unexpected error in Dijkstra algorithm", e);
        }
        return result;
    }

    /**
     * Return true if vertex exists in vertex list
     */
    public boolean containsVertex(T a) {
        return vertexList.contains(a);
    }


    /**
     * @return cloned graph or assertion error (if clone not supported)
     */
    @Override
    public Graph<T> clone() {
        try {
            Graph<T> clone = (Graph<T>) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void save(String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeInt(MAX_GRAPH_SIZE);
            oos.writeInt(graphSize);
            oos.writeObject(vertexList);
            oos.writeObject(edges);
        }
    }

    @SuppressWarnings("unchecked") // Removes yellow underlines from unchecked casts
    public void load(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            MAX_GRAPH_SIZE = ois.readInt();
            graphSize = ois.readInt();
            vertexList.clear();
            vertexList.addAll((List<T>) ois.readObject());
            edges.clear();
            edges.addAll((List<List<Double>>) ois.readObject());
        }
    }
}