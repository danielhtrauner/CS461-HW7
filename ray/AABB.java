package ray;

import java.util.Arrays;
import java.util.Comparator;
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
        // TODO: fill in this function.

        // ==== Step 1 ====
        // Find out the BIG bounding box enclosing all the surfaces in the range [left, right)
        // and store them in minB and maxB.
        // Hint: To find the bounding box for each surface, use getMinBound() and getMaxBound() */

    	// TODO DONE
        Point3 minB = new Point3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY); 
        Point3 maxB = new Point3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        minB.set(surfaces[0].getMinBound());
        maxB.set(surfaces[0].getMaxBound());
        
        for(int i=1;i<surfaces.length;i++) {
			if(surfaces[i].getMaxBound().x>=maxB.x) {
				maxB.x = surfaces[i].getMaxBound().x;
			}
			if(surfaces[i].getMaxBound().y>=maxB.y) {
				maxB.y = surfaces[i].getMaxBound().y;
			}
			if(surfaces[i].getMaxBound().z>=maxB.z) {
				maxB.z = surfaces[i].getMaxBound().z;
			}
			if(surfaces[i].getMinBound().x<=minB.x) {
				minB.x = surfaces[i].getMinBound().x;
			}
			if(surfaces[i].getMinBound().y<=minB.y) {
				minB.y = surfaces[i].getMinBound().y;
			}
			if(surfaces[i].getMinBound().z<=minB.z) {
				minB.z = surfaces[i].getMinBound().z;
			}
        	
        }

        // ==== Step 2 ====
        // Check for the base case. 
        // If the range [left, right) is small enough (constant at top of this file), just return a new leaf node.


        if(right-left<MAX_SURFACES_PER_LEAF) return new AABB();




        // ==== Step 3 ====
        // Figure out the widest dimension (x or y or z).
        // If x is the widest, set widestDim = 0. If y, set widestDim = 1. If z, set widestDim = 2.

        //TODO DONE
        int widestDim;
        if(Math.abs(maxB.x-minB.x)>Math.abs(maxB.y-minB.y)) {
        	if(Math.abs(maxB.x-minB.x)>Math.abs(maxB.z-minB.z)) {
        		widestDim=0;
        	} else widestDim=2;
        } else {
        	if(Math.abs(maxB.y-minB.y)>Math.abs(maxB.z-minB.z)) {
        		widestDim=1;
        	} else widestDim=2;        	
        }



        // ==== Step 4 (DONE) ====
        // Sort surfaces according to the widest dimension.
        // You can also implement O(n) randomized splitting algorithm.
        cmp.setIndex(widestDim);
        Arrays.sort(surfaces, left, right, cmp);
                
                
        // ==== Step 5 ====
        // Recursively create left and right children.
        AABB leftChild = null, rightChild = null;


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
        // TODO: fill in this function.
	// If the ray doesn't intersect this bounding box, return false right away.
        // For a leaf node, use a normal linear search as before in Scene.java.
        // Otherwise, search in the left and right children.
            
        return false;

    }
        
    /** 
     * Check if the ray intersects this bounding box.
     * @param ray
     * @return true if ray intersects this bounding box
     */
    private boolean doesIntersect(Ray ray) {
        // TODO: fill in this function.
        // Hint: reuse your code from box intersection.

        return false;

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
