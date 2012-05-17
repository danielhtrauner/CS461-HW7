package ray;

import java.util.Arrays;
import java.util.Comparator;

import ray.math.Color;
import ray.math.Point3;
import ray.math.Vector3;
import ray.surface.Surface;
/**
 * Class for Axis-Aligned-Bounding-Box to speed up the intersection look up time.
 *
 * @author ss932, modified by DS 4/2012
 */
public class AABB {
    
    /** The maximum number of surfaces in a leaf node */
    public static final int MAX_SURFACES_PER_LEAF = 10;
    
    boolean isLeaf;
        
    /** The current bounding box for this tree node.
     *  The bounding box is described by 
     *  (minPt.x, minPt.y, minPt.z) - (maxBound.x, maxBound.y, maxBound.z). */
    Point3 minBound, maxBound;
        
    /** child[0] is the left child. child[1] is the right child. */
    AABB []child;
        
    /** A shared surfaces array that will be used across every node in the tree. */
    static Surface []surfaces;
        
    /** left and right are indices for []surfaces,
     * meaning that this tree node contains a set of surfaces 
     * from surfaces[left] to surfaces[right-1]. */
    int left, right;
        
    /** A comparator class that can sort surfaces by x, y, or z coordinate. */
    static MyComparator cmp = new MyComparator();
        
        
    public AABB(boolean isLeaf, Point3 minBound, Point3 maxBound, AABB leftChild, AABB rightChild, int left, int right) {
        this.isLeaf = isLeaf;
        this.minBound = minBound;
        this.maxBound = maxBound;
        this.child = new AABB[2];
        this.child[0] = leftChild;
        this.child[1] = rightChild;
        this.left = left;
        this.right = right;
    }
        
    public AABB() {     }
        
    /**
     * Set the shared static surfaces that every node in the tree will use by 
     * using left and right indices.
     */
    public static void setSurfaces(Surface []surfaces) {
        AABB.surfaces = surfaces;
    }
        
    /**
     * Create an AABB [sub]tree.  This tree node will be responsible for storing
     * and processing surfaces[left] to surfaces[right-1].
     * @param left The left index of []surfaces
     * @param left The right index of []surfaces
     */
    public static AABB createTree(int left, int right) {
        
        // ==== Step 1 ====
        // Find out the BIG bounding box enclosing all the surfaces in the range [left, right)
        // and store them in minB and maxB.
        // Hint: To find the bounding box for each surface, use getMinBound() and getMaxBound()
        Point3 minB = new Point3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY); 
        Point3 maxB = new Point3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
                
        for(int i = left; i < right; i++) {
            Point3 sminB = surfaces[i].getMinBound();
            Point3 smaxB = surfaces[i].getMaxBound();
            minB.set(Math.min(minB.x, sminB.x), Math.min(minB.y, sminB.y), Math.min(minB.z, sminB.z));
            maxB.set(Math.max(maxB.x, smaxB.x), Math.max(maxB.y, smaxB.y), Math.max(maxB.z, smaxB.z));
        }
                
        // ==== Step 2 ====
        // Check for the base case. 
        // If the range [left, right) is small enough, just return a new leaf node.

        if (right - left < MAX_SURFACES_PER_LEAF) {
            return new AABB(true, minB, maxB, null, null, left, right);
        }

        // ==== Step 3 ====
        // Figure out the widest dimension (x or y or z).
        // If x is the widest, set widestDim = 0. If y, set widestDim = 1. If z, set widestDim = 2.
        int widestDim = 0;

        double dim = maxB.x - minB.x;
        if (dim < maxB.y - minB.y) {
            dim = maxB.y - minB.y;
            widestDim = 1;
        }
        if (dim < maxB.z - minB.z) {
            dim = maxB.z - minB.z;
            widestDim = 2;
        }

        // ==== Step 4 (DONE) ====
        // Sort surfaces according to the widest dimension.
        // You can also implement O(n) randomized splitting algorithm.
        cmp.setIndex(widestDim);
        Arrays.sort(surfaces, left, right, cmp);

        // ==== Step 5 ====
        // Recursively create left and right children.
        AABB leftChild = null, rightChild = null;

        int mid = (left + right) / 2;
        leftChild = createTree(left, mid);
        rightChild = createTree(mid, right);

        return new AABB(false, minB, maxB, leftChild, rightChild, left, right);
    }

        
    /**
     * Set outRecord to the first intersection of ray with the scene. Return true
     * if there was an intersection and false otherwise. If no intersection was
     * found outRecord is unchanged.
     *
     * @param outRecord the output IntersectionRecord
     * @param ray the ray to intersect
     * @param anyIntersection if true, will immediately return when found an intersection
     * @return true if and intersection is found.
     */
    public boolean intersect(IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection) {
        // If the ray doesn't intersect this bounding box, return false right away.
        // For a leaf node, use a normal linear search as before in Scene.java.
        // Otherwise, search in the left and right children.

        if (!doesIntersect(rayIn))
            return false;

        if (isLeaf) {
            boolean ret = false;
            IntersectionRecord tmp = new IntersectionRecord();
            Ray ray = new Ray(rayIn.origin, rayIn.direction);
            ray.start = rayIn.start;
            ray.end = rayIn.end;
            for(int i = left; i < right; i++) {
                if(surfaces[i].intersect(tmp, ray) && tmp.t < ray.end ) {
                    if(anyIntersection) return true;
                    ret = true;
                    ray.end = tmp.t;
                    if(outRecord != null)
                    	if(tmp.textureColor!=null) 
                    		outRecord.textureColor=new Color(tmp.textureColor);
                    	//System.out.println(tmp.textureColor);
                        outRecord.set(tmp);
                }
            }
            return ret;
        }
        else { // not leaf
            if (anyIntersection)
                return child[0].intersect(outRecord, rayIn, anyIntersection)
                    || child[1].intersect(outRecord, rayIn, anyIntersection);
            else {
                IntersectionRecord tmp = new IntersectionRecord();
                if (child[0].intersect(tmp, rayIn, anyIntersection)) {
                    Ray shorterRay = new Ray(rayIn.origin, rayIn.direction);
                    shorterRay.start = rayIn.start;
                    shorterRay.end = tmp.t;
                    if (child[1].intersect(outRecord, shorterRay, anyIntersection))
                        return true; // intersects both, but child[1] closer
                    // otherwise child[0] closer
                    outRecord.set(tmp);
                    return true;
                } else { // didn't intersect child[0], so check for intersection with child[1]
                    return  child[1].intersect(outRecord, rayIn, anyIntersection);
                }
            }
        }

    }

        
    /** 
     * Check if the ray intersects this bounding box.
     * @param ray
     * @return true if ray intersects this bounding box
     */
    private boolean doesIntersect(Ray ray) {
        
        Point3 e = ray.origin;
        Vector3 d = ray.direction;
        double tmin = ray.start;
        double tmax = ray.end;

        // better box intersection code, see text section 12.3.1
        
        // check for x
        double txmin, txmax;
        double ax = 1/d.x;
        if (ax >= 0) {
            txmin = ax * (minBound.x - e.x);
            txmax = ax * (maxBound.x - e.x);
        } else {
            txmin = ax * (maxBound.x - e.x);
            txmax = ax * (minBound.x - e.x);
        }

        if (tmin > txmax || txmin > tmax)
            return false;
        tmin = Math.max(tmin, txmin);
        tmax = Math.min(tmax, txmax);

        // check for y
        double tymin, tymax;
        double ay = 1/d.y;
        if (ay >= 0) {
            tymin = ay * (minBound.y - e.y);
            tymax = ay * (maxBound.y - e.y);
        } else {
            tymin = ay * (maxBound.y - e.y);
            tymax = ay * (minBound.y - e.y);
        }

        if (tmin > tymax || tymin > tmax)
            return false;
        tmin = Math.max(tmin, tymin);
        tmax = Math.min(tmax, tymax);

        // check for z
        double tzmin, tzmax;
        double az = 1/d.z;
        if (az >= 0) {
            tzmin = az * (minBound.z - e.z);
            tzmax = az * (maxBound.z - e.z);
        } else {
            tzmin = az * (maxBound.z - e.z);
            tzmax = az * (minBound.z - e.z);
        }

        if (tmin > tzmax || tzmin > tmax)
            return false;
        // no need to update these since they won't be used
        //tmin = Math.max(tmin, tzmin);
        //tmax = Math.min(tmax, tzmax);
    
        return true;
    }
}

class MyComparator implements Comparator<Surface> {
    int index;
    public MyComparator() {     }
        
    public void setIndex(int index) {
        this.index = index;
    }
        
    public int compare(Surface o1, Surface o2) {
        double v1 = o1.getAveragePosition().getE(index);
        double v2 = o2.getAveragePosition().getE(index);
        if(v1 < v2) return 1;
        if(v1 > v2) return -1;
        return 0;
    }
        
}

