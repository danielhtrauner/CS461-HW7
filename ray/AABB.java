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


        if(right-left<0) return new AABB(true,minB,maxB,null,null,0,0);




        // ==== Step 3 ====
        // Figure out the widest dimension (x or y or z).
        // If x is the widest, set widestDim = 0. If y, set widestDim = 1. If z, set widestDim = 2.

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

        leftChild = new AABB(true, AABB.surfaces[0].getMinBound(), AABB.surfaces[0].getMinBound(), null, null, leftChild.left, leftChild.right);
        rightChild = new AABB(true, AABB.surfaces[0].getMinBound(), AABB.surfaces[0].getMinBound(), null, null, rightChild.left, rightChild.right);
        
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
    public boolean intersect(IntersectionRecord outRecord, Ray ray, boolean anyIntersection) {
        // TODO: fill in this function.
    	// If the ray doesn't intersect this bounding box, return false right away.
        // For a leaf node, use a normal linear search as before in Scene.java.
        // Otherwise, search in the left and right children.
   
		if(doesIntersect(ray)) { 
			if(isLeaf) {
				boolean ret = false;
				IntersectionRecord tmp = new IntersectionRecord();
				Ray r = new Ray(ray.origin, ray.direction);
				r.start = ray.start;
				r.end = ray.end;
				
				for(int i=0; i<surfaces.length;i++) {
					Surface s = surfaces[i];
		            if (s.intersect(tmp, r) && tmp.t < r.end ) {
		                if(anyIntersection) return true;
		                ret = true;
		                r.end = tmp.t;
		                if(outRecord != null)
		                    outRecord.set(tmp);
		            }
				}
				return ret;
			} else {

				IntersectionRecord child1Record=new IntersectionRecord();
				IntersectionRecord child2Record=new IntersectionRecord();
				boolean child0=child[0].intersect(child1Record, ray, anyIntersection);
				boolean child1=child[1].intersect(child2Record, ray, anyIntersection);
				if(child0||child1) {
					if(child1Record.t>child2Record.t)
						outRecord.set(child2Record);
					else outRecord.set(child1Record);
				}
				return child0 || child1;
			}
		} else return false;
    }
        
    /** 
     * Check if the ray intersects this bounding box.
     * @param ray
     * @return true if ray intersects this bounding box
     */
    private boolean doesIntersect(Ray ray) {
        Vector3 d = ray.direction;
        Vector3 m1 = new Vector3();
        m1.sub(minBound, ray.origin); // minPt - e
        Vector3 m2 = new Vector3();
        m2.sub(maxBound, ray.origin); // maxPt - e
                
        double t1x = m1.x / d.x, t1y = m1.y / d.y, t1z = m1.z / d.z;
        double t2x = m2.x / d.x, t2y = m2.y / d.y, t2z = m2.z / d.z;
        double minx = Math.min(t1x, t2x);
        double miny = Math.min(t1y, t2y);
        double minz = Math.min(t1z, t2z);
        double maxx = Math.max(t1x, t2x);
        double maxy = Math.max(t1y, t2y);
        double maxz = Math.max(t1z, t2z);
                                
        double t1 = Math.max(minx,  Math.max(miny, minz));
        double t2 = Math.min(maxx,  Math.min(maxy, maxz));
                
        if (t1 > t2)
            return false; // no intersection

        if (t1 < ray.start || t1 > ray.end) // intersection not in valid range
            return false;

        // else intersection at t1
        Point3 p = new Point3(); // intersection point
        ray.evaluate(p, t1);

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
