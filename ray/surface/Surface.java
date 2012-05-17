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
     */
    public void transformVector(Vector3 vector) {
    	vector.normalize();
    	tMatTInv.rightMultiply(vector);
    	vector.normalize();
    }
    /**
     * Transform Point
     * @param point Input point
     */
    public void transformPoint(Point3 point) {
    	tMat.rightMultiply(point);
    }
    /**
     * Transform Bounding Box
     */
    public void transformBoundingBox() {
    	
    	Point3 frontRightTop = new Point3(maxBound.x, maxBound.y, minBound.z);
    	Point3 frontRightBottom = new Point3(maxBound.x, minBound.y, minBound.z);
    	Point3 frontLeftTop = new Point3(minBound.x, maxBound.y, minBound.z);
    	Point3 frontLeftBottom = new Point3(minBound);
    	
    	Point3 backRightTop = new Point3(maxBound);
    	Point3 backRightBottom = new Point3(maxBound.x, maxBound.y, minBound.z);
    	Point3 backLeftTop = new Point3(minBound.x, maxBound.y, maxBound.z);
    	Point3 backLeftBottom = new Point3(minBound.x, minBound.y, maxBound.z);
    	
    	transformPoint(frontRightTop);
    	transformPoint(frontRightBottom);
    	transformPoint(frontLeftTop);
    	transformPoint(frontLeftBottom);
    	
    	transformPoint(backRightTop);
    	transformPoint(backRightBottom);
    	transformPoint(backLeftTop);
    	transformPoint(backLeftBottom);	
    	
    	maxBound.x = Math.max(frontRightTop.x, frontRightBottom.x);
    	maxBound.x = Math.max(maxBound.x, frontLeftTop.x);
    	maxBound.x = Math.max(maxBound.x, frontLeftBottom.x);
    	maxBound.x = Math.max(maxBound.x, backRightTop.x);
    	maxBound.x = Math.max(maxBound.x, backRightBottom.x);
    	maxBound.x = Math.max(maxBound.x, backLeftTop.x);
    	maxBound.x = Math.max(maxBound.x, backLeftBottom.x);
    	
    	maxBound.y = Math.max(frontRightTop.y, frontRightBottom.y);
    	maxBound.y = Math.max(maxBound.y, frontLeftTop.y);
    	maxBound.y = Math.max(maxBound.y, frontLeftBottom.y);
    	maxBound.y = Math.max(maxBound.y, backRightTop.y);
    	maxBound.y = Math.max(maxBound.y, backRightBottom.y);
    	maxBound.y = Math.max(maxBound.y, backLeftTop.y);
    	maxBound.y = Math.max(maxBound.y, backLeftBottom.y);

    	maxBound.z = Math.max(frontRightTop.z, frontRightBottom.z);
    	maxBound.z = Math.max(maxBound.z, frontLeftTop.z);
    	maxBound.z = Math.max(maxBound.z, frontLeftBottom.z);
    	maxBound.z = Math.max(maxBound.z, backRightTop.z);
    	maxBound.z = Math.max(maxBound.z, backRightBottom.z);
    	maxBound.z = Math.max(maxBound.z, backLeftTop.z);
    	maxBound.z = Math.max(maxBound.z, backLeftBottom.z);

    	minBound.x = Math.min(frontRightTop.x, frontRightBottom.x);
    	minBound.x = Math.min(minBound.x, frontLeftTop.x);
    	minBound.x = Math.min(minBound.x, frontLeftBottom.x);
    	minBound.x = Math.min(minBound.x, backRightTop.x);
    	minBound.x = Math.min(minBound.x, backRightBottom.x);
    	minBound.x = Math.min(minBound.x, backLeftTop.x);
    	minBound.x = Math.min(minBound.x, backLeftBottom.x);
    	
    	minBound.y = Math.min(frontRightTop.y, frontRightBottom.y);
    	minBound.y = Math.min(minBound.y, frontLeftTop.y);
    	minBound.y = Math.min(minBound.y, frontLeftBottom.y);
    	minBound.y = Math.min(minBound.y, backRightTop.y);
    	minBound.y = Math.min(minBound.y, backRightBottom.y);
    	minBound.y = Math.min(minBound.y, backLeftTop.y);
    	minBound.y = Math.min(minBound.y, backLeftBottom.y);

    	minBound.z = Math.min(frontRightTop.z, frontRightBottom.z);
    	minBound.z = Math.min(minBound.z, frontLeftTop.z);
    	minBound.z = Math.min(minBound.z, frontLeftBottom.z);
    	minBound.z = Math.min(minBound.z, backRightTop.z);
    	minBound.z = Math.min(minBound.z, backRightBottom.z);
    	minBound.z = Math.min(minBound.z, backLeftTop.z);
    	minBound.z = Math.min(minBound.z, backLeftBottom.x);
    	
    	Point3 tmp1 = new Point3(minBound);
    	Vector3 tmp2 = new Vector3(maxBound);
    	tmp1.add(tmp2);
    	tmp1.scale(0.5);
    	
    	averagePosition = tmp1;
    }

}
