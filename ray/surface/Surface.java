package ray.surface;

import java.util.ArrayList;

import ray.IntersectionRecord;
import ray.Ray;
import ray.math.Point3;
import ray.shader.Shader;

/**
 * Abstract base class for all surfaces. Provides access for shader and
 * intersection uniformly for all surfaces.
 * 
 * @author ags, ss932, modified by DS 2/2012
 */
public abstract class Surface {

	/**
	 * The average position of the surface. Usually calculated by taking the
	 * average of all the vertices. This point will be used in AABB tree
	 * construction.
	 */
	protected Point3 averagePosition;

	/** The smaller coordinate (x, y, z) of the bounding box of this surface */
	protected Point3 minBound;

	/** The larger coordinate (x, y, z) of the bounding box of this surface */
	protected Point3 maxBound;

	/** Shader to be used to shade this surface. */
	protected Shader shader = Shader.DEFAULT_MATERIAL;

	public void setShader(Shader material) {
		this.shader = material;
	}

	public Shader getShader() {
		return shader;
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 * 
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param ray
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public abstract boolean intersect(IntersectionRecord outRecord, Ray ray);

	/**
	 * Add this surface (and possible sub-surfaces) to the scene's list of
	 * surfaces
	 * 
	 * @param sceneSurfaces
	 *            the list of surfaces in the scene
	 */
	public void addTo(ArrayList<Surface> sceneSurfaces) {
		sceneSurfaces.add(this);
	}

	public Point3 getAveragePosition() {
		return averagePosition;
	}

	public Point3 getMinBound() {
		return minBound;
	}

	public Point3 getMaxBound() {
		return maxBound;
	}

	/**
	 * Compute the bounding box and store the result in averagePosition,
	 * minBound, and maxBound.
	 */
	public abstract void computeBoundingBox();

}
