package assignment1;

import java.util.LinkedList;

public class BinarySearchTreeImpl<T> {
    protected static class Node<T> {
        public Node(int key, T value)
        {
            this.key = key;
            this.value = value;
        }
        public int key;
        public T value;
        public Node<T> parent = null;
        public Node<T> left = null;
        public Node<T> right = null;
    }
    protected Node<T> root = null;

    protected void insert(Node<T> x, int key, T value) {
        if (key == x.key) {
            x.value = value;
        }
        else if (key < x.key) {
            if (x.left == null) {
                x.left = new Node<>(key, value);
                x.left.parent = x;
            }
            else {
                insert(x.left, key, value);
            }
        } else {
            if (x.right == null) {
                x.right = new Node<>(key, value);
                x.right.parent = x;
            } else {
                insert(x.right, key, value);
            }
        }
    }

    protected LinkedList<T> inorderTreeWalk(Node<T> x)
    {
        if (x != null) {
            LinkedList<T> L = inorderTreeWalk(x.left);
            LinkedList<T> R = inorderTreeWalk(x.right);

            LinkedList<T> result = new LinkedList<>();
            result.addAll(L);
            result.add(x.value);
            result.addAll(R);

            return result;
        } else {
            return new LinkedList<>();
        }
    }

    protected Node<T> search(Node<T> x, int key) {
        if (x == null || x.key == key)
            return  x;
        if (key < x.key)
            return search(x.left, key);
        else
            return search(x.right, key);
    }

    protected int depth(Node<T> x) {
//        Depth of the certain node:
//        int depth = 0;
//        while (x != null) {
//            depth++;
//        }
//        return depth;

        // Height of the subtree, where node x is root (post-order):
        if (x == null) {
            return -1;
        }

        int leftHeight = depth(x.left);
        int rightHeight = depth(x.right);

        if (leftHeight > rightHeight)
            return leftHeight + 1;
        else
            return rightHeight + 1;
    }

    protected Node<T> minimum(Node<T> x) {
        while (x.left != null)
            x = x.left;
        return x;
    }

    protected Node<T> maximum(Node<T> x) {
        while (x.right != null)
            x = x.right;
        return x;
    }

    protected Node<T> successor(Node<T> x)
    {
        if (x.right != null) {
            return minimum(x.right);
        }
        Node<T> y = x.parent;
        while (y != null && x == y.right) {
            x = y;
            y = y.parent;
        }
        return y;
    }

    protected Node<T> predecessor(Node<T> x)
    {
        if (x.left != null) {
            return maximum(x.left);
        }
        Node<T> y = x.parent;
        while (y != null && x == y.left) {
            x = y;
            y = y.parent;
        }
        return y;
    }

    protected void delete(Node<T> z)
    {
        if (z.left == null) {
            transplant(z, z.right);
        } else if (z.right == null) {
            transplant(z, z.left);
        } else {
            Node<T> y = minimum(z.right);
            if (y != z.right) {
                transplant(y, y.right);
                 y.right = z.right;
                 z.right.parent = y;
            }
            transplant(z, y);
            y.left = z.left;
            z.left.parent = y;
        }
    }

    private void transplant(Node<T> u, Node<T> v) {
        if (u.parent == null) {
            root = v;
        } else {
            if (u == u.parent.left) {
                u.parent.left = v;
            } else {
                u.parent.right = v;
            }
        }
        if (v != null) {
            v.parent = u.parent;
        }
    }
}
