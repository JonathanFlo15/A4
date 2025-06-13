import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.Stack;

public class KdTreeST<Value> {

    // root node for tree
    private Node root;
    // keep track of the size of the tree
    private int size;


    // node class to represent node in kd tree
    private class Node {
        // point corresponding to node
        private Point2D p;
        // value to be associated with point
        private Value val;
        // the left/bottom subtree
        // the right/top subtree
        private Node lb, rt;
        // rect bounds for a rectangle
        private RectHV rect;


        // creating new node for kd tree
        private Node(Point2D p, Value val, RectHV rect, boolean vertical) {
            this.p = p;
            this.val = val;
            this.rect = rect;


            lb = null;
            rt = null;
        }
    }


    // construct an empty symbol table of points
    public KdTreeST() {
        root = null;
        size = 0;
    }

    // is the symbol table empty?
    public boolean isEmpty() {
        return size == 0;
    }

    // number of points
    public int size() {
        return size;

    }

    // associate the value val with point p
    public void put(Point2D p, Value val) {
        if (p == null || val == null) {
            throw new IllegalArgumentException("there exist no value");
        }
        // root will have a rectangle that includes everything
        root = put(root, p, val, new RectHV(Double.NEGATIVE_INFINITY,
                                            Double.NEGATIVE_INFINITY,
                                            Double.POSITIVE_INFINITY,
                                            Double.POSITIVE_INFINITY),
                   true);

    }

    // helper method for put
    private Node put(Node node, Point2D p, Value val, RectHV rect, boolean vertical) {
        if (node == null) {
            size++;
            return new Node(p, val, rect, vertical);
        }
        if (node.p.equals(p)) {
            // update the value
            node.val = val;
            return node;
        }
        int cmp;

        // rectangles corresponding to left/bottom and right/top
        RectHV leftRect, rightRect;
        // vertical comparison, compares x-coordinates
        if (vertical) {
            // variable to determine direction of insertion
            cmp = Double.compare(p.x(), node.p.x());
            // minimum of x and y parent
            leftRect = new RectHV(node.rect.xmin(), node.rect.ymin(),
                                  // x-coordinate of x, max coordinate of y
                                  node.p.x(), node.rect.ymax());
            // gets the min x coordinate of the node and y min
            rightRect = new RectHV(node.p.x(), node.rect.ymin(),
                                   // max coordinates of x and y
                                   node.rect.xmax(), node.rect.ymax());
            // horizontal comparison, compares y-coordinates
        }
        else {
            // comparing the y coordinates
            cmp = Double.compare(p.y(), node.p.y());
            leftRect = new RectHV(node.rect.xmin(), node.rect.ymin(),
                                  node.rect.xmax(), node.p.y());
            rightRect = new RectHV(node.rect.xmin(), node.p.y(),
                                   node.rect.xmax(), node.rect.ymax());
        }
        // determine whether to put in left/bottom or right/top
        if (cmp < 0) {
            // left bottom since less than zero not vertical
            node.lb = put(node.lb, p, val, leftRect, !vertical);
        }
        else {
            // right top
            node.rt = put(node.rt, p, val, rightRect, !vertical);
        }

        return node;
    }

    // helper method to get the value associated with p
    private Value get(Node node, Point2D p, boolean vertical) {
        // return value associated with p
        // return null if no value is present
        if (node == null) return null;
        // if node contains p simply return value of that node
        if (node.p.equals(p)) return node.val;

        if (vertical) {
            // lies on the left side
            // get the x value for point p compare it to nearest node
            if (p.x() < node.p.x()) {
                return get(node.lb, p, !vertical);
            }
            else return get(node.rt, p, !vertical);
        }
        else {
            if (p.y() < node.p.y()) {
                return get(node.lb, p, !vertical);
            }
            else return get(node.rt, p, !vertical);
        }

    }

    // value associated with point p
    public Value get(Point2D p) {
        if (p == null) {
            throw new IllegalArgumentException("p is null");
        }
        return get(root, p, true);
    }

    // does the tree contain point p
    public boolean contains(Point2D p) {
        if (p == null) {
            throw new IllegalArgumentException("arg to contain() is null");
        }
        return get(p) != null;
    }


    public Iterable<Point2D> points() {
        // creates queues for nodes and points
        Queue<Node> queue = new Queue<>();
        Queue<Point2D> points = new Queue<>();
        // enqueue the root node
        queue.enqueue(root);
        // loop that adds nodes from left bottom and right top subtrees
        // which adds all points
        while (!queue.isEmpty()) {
            Node node = queue.dequeue();
            if (node == null) {
                continue;
            }
            points.enqueue(node.p);
            queue.enqueue(node.lb);
            queue.enqueue(node.rt);
        }

        return points;
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) {
            throw new IllegalArgumentException("rectangle is null");
        }
        Stack<Point2D> stack = new Stack<>();
        range(root, rect, stack); // recursively finds all points in rectangle
        return stack;
    }

    // helper method for range
    private void range(Node node, RectHV rect, Stack<Point2D> stack) {
        if (node == null) {
            return;
        }
        if (!rect.intersects(node.rect)) return;
        // if a point is in rectangle, add it to stack
        if (rect.contains(node.p)) {
            stack.push(node.p);
        }
        // recursively find points in rectangle
        range(node.lb, rect, stack);
        range(node.rt, rect, stack);
    }

    // a nearest neighbor of point p; null if the symbol table is empty
    public Point2D nearest(Point2D p) {
        if (p == null) throw new IllegalArgumentException(" to nearest() is null");
        return nearest(root, p, null, Double.POSITIVE_INFINITY, true);
    }

    // helper method to find the nearest point
    private Point2D nearest(Node node, Point2D p, Point2D nearest, double nearestDist,
                            boolean orientation) {
        if (node == null) return nearest;
        // distance calculated using distance squared
        double distance = node.p.distanceSquaredTo(p);
        if (distance < nearestDist) {
            nearestDist = distance;
            nearest = node.p;
        }

        Node one, two;
        double cmp;
        if (orientation) {
            cmp = p.x() - node.p.x();
        }
        else {
            cmp = p.y() - node.p.y();
        }
        // finds closest node based off position of query point
        if (cmp < 0) {
            one = node.lb;
            two = node.rt;
        }
        else {
            one = node.rt;
            two = node.lb;
        }
        // if statements that find newest nearest neighbor
        if (one != null && one.rect.distanceSquaredTo(p) < nearestDist) {
            nearest = nearest(one, p, nearest, nearestDist, !orientation);
            nearestDist = nearest.distanceSquaredTo(p);
        }
        if (two != null && two.rect.distanceSquaredTo(p) < nearestDist) {
            nearest = nearest(two, p, nearest, nearestDist, !orientation);
        }

        return nearest;
    }


    // unit testing
    public static void main(String[] args) {

    }

}




