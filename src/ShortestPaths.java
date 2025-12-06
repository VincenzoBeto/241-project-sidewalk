import java.util.HashMap;
import java.util.LinkedList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.PriorityQueue;

/** Inner class representing data used by Dijkstra's algorithm in the
 * process of computing shortest paths from a given source node. */
class PathData implements Comparable<PathData> {
    double distance; // distance of the shortest path from source
    PathData previous; // previous node in the path from the source
    Node node; // Reference to node

    public PathData(double dist, Node self) {
        this.node = self;
        distance = dist;
        previous = null;
    }
    public PathData(double dist, Node self, PathData prev) {
        this(dist, self);
        previous = prev;
    }

    @Override
    public int compareTo(PathData o) {
        // When inserting into heap, paths are sorted based on distance to the root node
        return Double.compare(o.distance, distance);
    }
}


/** Provides an implementation of Dijkstra's single-source shortest paths
 * algorithm.
 * Sample usage:
 *   Graph g = // create your graph
 *   ShortestPaths sp = new ShortestPaths();
 *   Node a = g.getNode("A");
 *   sp.compute(a);
 *   Node b = g.getNode("B");
 *   LinkedList<Node> abPath = sp.getShortestPath(b);
 *   double abPathLength = sp.getShortestPathLength(b);
 *   */
public class ShortestPaths {
    // Stores auxiliary data associated with each node for the shortest
    //  path computation. This is internal and should not be accessed
    //  outside the class: use getter methods instead
    private final HashMap<Node,PathData> _paths = new HashMap<>();

    /** Compute the shortest path to all nodes from origin using Dijkstra's
     * algorithm. Fill in the paths field, which associates each Node with its
     * PathData record, storing total distance from the source, and the
     * back pointer to the previous node on the shortest path.
     * Precondition: origin is a node in the Graph.*/
    public void compute(Node origin) {
        _paths.clear();

        PriorityQueue<PathData> q = new PriorityQueue<>();
        // Initial value for origin node is a distance of 0, with a previous value of null to signal that it is the root
        q.add(new PathData(0, origin));
        _paths.put(origin, new PathData(0, origin));

        while (!q.isEmpty()) {
            PathData curPathData = q.remove();
            Node cur = curPathData.node;

            for (Map.Entry<Node, Double> entry : cur.getNeighbors().entrySet()) {
                PathData pathData = _paths.get(entry.getKey());

                if (pathData == null) {
                    // If node has not yet been seen, update path map and add to queue
                    PathData newPath = new PathData(curPathData.distance + entry.getValue(), entry.getKey(), curPathData);
                    _paths.put(entry.getKey(), newPath);
                    q.add(newPath);
                    continue;
                }

                // Otherwise, node has already been visited in this compute call
                //  Compare distance and, if shorter, add to queue again so paths are updated
                if (curPathData.distance + entry.getValue() < pathData.distance) {
                    // This is a faster way to get to the neighboring node, update it to point to this path instead
                    pathData.previous = curPathData;
                    pathData.distance = curPathData.distance + entry.getValue();
                    q.add(curPathData);
                }
                // If the above statement did not run, this is not the shortest path to the node
                //  and can therefore be safely ignored
            }
        }
    }

    /** Returns the length of the shortest path from the origin to destination.
     * If no path exists, return Double.POSITIVE_INFINITY.
     * Precondition: destination is a node in the graph, and compute(origin)
     * has been called. */
    public double shortestPathLength(Node destination) {
        PathData data = _paths.get(destination);
        if (data == null) {
            // If compute did not find the destination node, it is not reachable
            return Double.POSITIVE_INFINITY;
        }

        return data.distance;
    }

    /** Returns a LinkedList of the nodes along the shortest path from origin
     * to destination. This path includes the origin and destination. If origin
     * and destination are the same node, it is included only once.
     * If no path to it exists, return null.
     * Precondition: destination is a node in the graph, and compute(origin)
     * has been called. */
    public LinkedList<Node> shortestPath(Node destination) {
        PathData data = _paths.get(destination);
        if (data == null) {
            // If compute did not find the destination node, it is not reachable
            return null;
        }

        LinkedList<Node> path = new LinkedList<>();
        while (data != null) {
            path.addFirst(data.node);
            data = data.previous;
        }

        return path;
    }

    /**
     * Place a node in the internal structure of the path data. This method should be used for testing only.
     * Useful to verify that shortestPath and shortestPathLength are functioning as they should be.
     * @param id String: Node ID for debugging purposes.
     * @param previous Node: Previous node that points to this, or null if this is the root node.
     * @param dist double: the distance from the root node.
     * @return Node: newly added node.
     */
    public Node test_addInternalNode(String id, Node previous, double dist) {
        Node n = new Node(id);
        PathData data = new PathData(dist, n, _paths.get(previous));
        _paths.put(n, data);
        return n;
    }

    /** Static helper method to open and parse a file containing graph
     * information. Can parse either a basic file or a CSV file with
     * sidewalk data. See GraphParser, BasicParser, and DBParser for more.*/
    protected static Graph parseGraph(String fileType, String fileName) throws
        FileNotFoundException {
        // create an appropriate parser for the given file type
        GraphParser parser;
        if (fileType.equals("basic")) {
            parser = new BasicParser();
        } else if (fileType.equals("db")) {
            parser = new DBParser();
        } else {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + fileType);
        }

        // open the given file
        parser.open(new File(fileName));

        // parse the file and return the graph
        return parser.parse();
    }

    public static void main(String[] args) {
        // read command line args
        String fileType = args[0];
        String fileName = args[1];
        String sidewalkOrigCode = args[2];

        String sidewalkDestCode = null;
        if (args.length == 4) {
          sidewalkDestCode = args[3];
        }

        // parse a graph with the given type and filename
        Graph graph;
        try {
            graph = parseGraph(fileType, fileName);
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file " + fileName);
            return;
        }
        graph.report();

        Node origin = graph.getNode(sidewalkOrigCode);

        ShortestPaths pathFinder = new ShortestPaths();
        pathFinder.compute(origin);

        if (sidewalkDestCode == null) {
            // destCode was not given, print out the distances of all nodes from the origin
            System.out.println("---ALL NODE DISTANCES---");
            for (Node n : graph.getNodes().values()) {
                double dist = pathFinder.shortestPathLength(n);

                if (dist == Double.POSITIVE_INFINITY) {
                    // If this node was not reachable, do not print it out
                    continue;
                }
                System.out.println("  " + n + "-->" + dist);
            }
        } else {
            // destCode was given, print out the path and total distance
            Node dest = graph.getNode(sidewalkDestCode);
            double pathLen = pathFinder.shortestPathLength(dest);
            LinkedList<Node> path = pathFinder.shortestPath(dest);
            System.out.println("---SHORTEST PATH FROM " + sidewalkOrigCode + " TO " + sidewalkDestCode);
            for (Node n : path) {
                System.out.println("  " + n);
            }
            System.out.println();
            System.out.println("TOTAL DISTANCE=" + pathLen);
        }
    }
}
