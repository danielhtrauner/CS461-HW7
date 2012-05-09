package ray.surface;

import java.util.ArrayList;

import ray.IntersectionRecord;
import ray.Ray;
import ray.math.Point3;
import ray.math.Vector3;
import ray.shader.Shader;
import ray.math.Matrix4;

/**
 * Abstract base class for all surfaces. Provides access for shader and
 * intersection uniformly for all surfaces.
 * 
 * @author ags, ss932, modified by DS 2/2012
 */
public abstract class Surface {
	
    /* tMat, tMatInv, tMatTInv are calculated and stored in each instance to avoid recomputing */

    /** The transformation matrix. */
    protected Matrix4 tMat;

    /** The inverse of the transformation matrix. */
    protected Matrix4 tMatInv;

    /** The inverse of the transpose of the transformation matrix. */
    protected Matrix4 tMatTInv;

	 /** The average position of the surface. Usually calculated by taking the average of 
     *  all the vertices. This point will be used in AABB tree construction. */
    protected Point3 averagePosition;
    public Point3 getAveragePosition() { return averagePosition; } 
   
    /** The smaller coordinate (x, y, z) of the bounding box of this surface */
    protected Point3 minBound;
    public Point3 getMinBound() { return minBound; }
    
    /** The larger coordinate (x, y, z) of the bounding box of this surface */
    protected Point3 maxBound; 
    public Point3 getMaxBound() { return maxBound; }
      
	/** Shader to be used to shade this surface. */
	protected Shader shader = Shader.DEFAULT_MATERIAL;
	public void setShader(Shader material) { this.shader = material; }
	public Shader getShader() { return shader; }
    
	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 * 
	 * @param outRecord the output IntersectionRecord
	 * @param ray the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public abstract boolean intersect(IntersectionRecord outRecord, Ray ray);

	/**
	 * Add this surface (and possible sub-surfaces) to the scene's list of
	 * surfaces
	 * 
	 * @param sceneSurfaces the list of surfaces in the scene
	 * @param groups the list of non-renderable "containers" in the scene (groups and meshes) 
	 */
    public void addTo(ArrayList<Surface> sceneSurfaces, ArrayList<Surface> groups) { 
		sceneSurfaces.add(this);
	}
	/**
	 * Compute the bounding box and store the result in averagePosition,
	 * minBound, and maxBound.
	 */
	public abstract void computeBoundingBox();

    public void setTransformation(Matrix4 a, Matrix4 aInv, Matrix4 aTInv) {
        //System.out.println("calling setTransformation in Surface.java");
        tMat = a;
        tMatInv = aInv;
        tMatTInv = aTInv;
        computeBoundingBox();
    }
    // a useful helper method below.  You can define additional helper
    // methods in this class, e.g., to transform bounding boxes.
    
    /**
     * Un-transform rayIn using tMatInv 
     * @param rayIn Input ray
     * @return tMatInv * rayIn
     */
    public Ray untransformRay(Ray rayIn) {
        Ray ray = new Ray(rayIn.origin, rayIn.direction);
        ray.start = rayIn.start;
        ray.end = rayIn.end;

        tMatInv.rightMultiply(ray.direction);
        tMatInv.rightMultiply(ray.origin);
        return ray;
    }
    /**
     * Transform rayIn using tMat
     * @param rayIn Input ray
     * @return tMat * rayIn
     */
    public Ray transformRay(Ray rayIn) {
        Ray ray = new Ray(rayIn.origin, rayIn.direction);
        ray.start = rayIn.start;
        ray.end = rayIn.end;

        tMat.rightMultiply(ray.direction);
        tMat.rightMultiply(ray.origin);
        return ray;
    }
    /**
     * Transform Vector
     * @param vector Input vector
     * @return tMat * vector
     */
    public Vector3 transformVector(Vector3 vector) {
    	tMat.rightMultiply(vector);
    	return vector;
    }
    /**
     * Transform Point
     * @param point Input point
     * @return tMat * point
     */
    public Point3 transformPoint(Point3 point) {
    	tMat.rightMultiply(point);
    	return point;
    }

}
