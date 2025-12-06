import static org.junit.Assert.*;

import org.junit.Test;

import java.io.FileNotFoundException;

import java.util.LinkedList;

public class ShortestPathsTest {


    /* Returns the Graph loaded from the file with filename fn. */
    private Graph loadBasicGraph(String fn) {
        Graph result = null;
        try {
          result = ShortestPaths.parseGraph("basic", fn);
        } catch (FileNotFoundException e) {
          fail("Could not find graph " + fn);
        }
        return result;
    }

    /** Dummy test case demonstrating syntax to create a graph from scratch.
     * TODO Write your own tests below. */
    @Test
    public void test00Nothing() {
        Graph g = new Graph();
        Node a = g.getNode("A");
        Node b = g.getNode("B");
        g.addEdge(a, b, 1);

        // sample assertion statements:
        assertTrue(true);
        assertEquals(2+2, 4);
    }

    /** Minimal test case to check the path from A to B in Simple0.txt */
    @Test
    public void test01Simple0() {
        Graph g = loadBasicGraph("data/Simple0.txt");
        g.report();
        ShortestPaths sp = new ShortestPaths();
        Node a = g.getNode("A");
        sp.compute(a);
        Node b = g.getNode("B");
        LinkedList<Node> abPath = sp.shortestPath(b);
        assertEquals(abPath.size(), 2);
        assertEquals(abPath.getFirst(), a);
        assertEquals(abPath.getLast(),  b);
        assertEquals(sp.shortestPathLength(b), 1.0, 1e-6);
    }

    @Test
    public void test02Simple1() {
        Graph g = loadBasicGraph("data/Simple1.txt");
        g.report();
        ShortestPaths sp = new ShortestPaths();
        Node s = g.getNode("S");
        Node c = g.getNode("C");
        Node d = g.getNode("D");
        Node a = g.getNode("A");
        // Test general path from A to S
        sp.compute(a);
        LinkedList<Node> path = sp.shortestPath(s);

        assertEquals(4, path.size());
        assertEquals(path.getFirst(), a);
        assertEquals(path.get(1), c);
        assertEquals(path.get(2), d);
        assertEquals(path.getLast(), s);

        // Ensure path lengths are calculated correctly
        assertEquals(0.0, sp.shortestPathLength(a), 1e-6);
        assertEquals(2.0, sp.shortestPathLength(c), 1e-6);
        assertEquals(4.0, sp.shortestPathLength(d), 1e-6);
        assertEquals(5.0, sp.shortestPathLength(s), 1e-6);


        // Test to ensure the fastest path is being selected with weights in mind
        //  While S->A is only 2 nodes, it has a length of 10 and is longer than S->C->A with a length of 8
        sp.compute(s);
        path = sp.shortestPath(a);

        assertEquals(3, path.size());
        assertEquals(8.0, sp.shortestPathLength(a), 1e-6);
    }

    @Test
    public void test03Simple2() {
        Graph g = loadBasicGraph("data/Simple2.txt");
        g.report();
        ShortestPaths sp = new ShortestPaths();
        Node d = g.getNode("D");
        // "gn" = g node, too lazy to find a better naming scheme
        Node gn = g.getNode("G");
        // Test general path from D to G
        sp.compute(d);
        LinkedList<Node> path = sp.shortestPath(gn);

        assertEquals(7, path.size());
        // To keep namespace clear, nodes used for assertions only are not kept for longer paths
        assertEquals(path.get(1), g.getNode("A"));
        assertEquals(path.get(2), g.getNode("E"));
        assertEquals(path.get(3), g.getNode("F"));
        assertEquals(path.get(4), g.getNode("I"));
        assertEquals(path.get(5), g.getNode("J"));

        // Ensure path lengths are calculated correctly
        assertEquals(0.0, sp.shortestPathLength(d), 1e-6);
        assertEquals(4.0, sp.shortestPathLength(g.getNode("A")), 1e-6);
        assertEquals(5.0, sp.shortestPathLength(g.getNode("E")), 1e-6);
        assertEquals(8.0, sp.shortestPathLength(g.getNode("F")), 1e-6);
        assertEquals(9.0, sp.shortestPathLength(g.getNode("I")), 1e-6);
        assertEquals(11.0, sp.shortestPathLength(g.getNode("J")), 1e-6);
        assertEquals(12.0, sp.shortestPathLength(gn), 1e-6);


        // Test to ensure unreachable nodes are marked as such
        sp.compute(gn);
        path = sp.shortestPath(d);

        // G->D does not exist
        assertNull(path);
        assertEquals(Double.POSITIVE_INFINITY, sp.shortestPathLength(d), 1e-6);
        // G->A also does not exist
        assertEquals(Double.POSITIVE_INFINITY, sp.shortestPathLength(g.getNode("A")), 1e-6);

        Node b = g.getNode("B");
        Node h = g.getNode("H");
        sp.compute(b);
        path = sp.shortestPath(h);

        // B->H does not exist
        assertNull(path);
        assertEquals(Double.POSITIVE_INFINITY, sp.shortestPathLength(h), 1e-6);
    }

    @Test
    public void test04ShortestPathRaw() {
        // Test to make sure shortestPath and shortestPathLength function properly
        ShortestPaths sp = new ShortestPaths();
        // Format: a -> b -> c d  (d is disconnected)
        //  Initializes path to a known state by altering internal list directly
        //  This is a state that compute() might create, which means there must be at most 1 path per node
        //  Also why the test cases are fairly simple for this, as there are not very many possible states
        Node a = sp.test_addInternalNode("A", null, 0);
        Node b = sp.test_addInternalNode("B", a, 2.0);
        Node c = sp.test_addInternalNode("C", b, 5.0);
        Node d = new Node("D");

        // Test basic path
        // Node path is correct?
        LinkedList<Node> path = sp.shortestPath(c);
        assertEquals(3, path.size());
        assertEquals(a, path.getFirst());
        assertEquals(b, path.get(1));
        assertEquals(c, path.getLast());
        // Distances along path are correct?
        assertEquals(0.0, sp.shortestPathLength(a), 1e-6);
        assertEquals(2.0, sp.shortestPathLength(b), 1e-6);
        assertEquals(5.0, sp.shortestPathLength(c), 1e-6);

        // Test unreachable nodes
        assertEquals(Double.POSITIVE_INFINITY, sp.shortestPathLength(d), 1e-6);
        path = sp.shortestPath(d);
        assertNull(path);
    }

    /* Pro tip: unless you include @Test on the line above your method header,
     * JUnit will not run it! This gets me every time. */
}
