package ray;

import java.util.ArrayList;
import java.util.Iterator;

import ray.light.Light;
import ray.shader.Shader;
import ray.surface.Surface;


/**
 * The scene is just a collection of objects that compose a scene. The camera,
 * the list of lights, and the list of surfaces.
 *
 * @author ags, modified by DS 2/2012
 */
public class Scene {
	
	/** The camera for this scene. */
	protected Camera camera;
	public void setCamera(Camera camera) { this.camera = camera; }
	public Camera getCamera() { return this.camera; }
	
	/** The list of lights for the scene. */
	protected ArrayList<Light> lights = new ArrayList<Light>();
	public void addLight(Light toAdd) { lights.add(toAdd); }
	public ArrayList<Light> getLights() { return this.lights; }
	
    /** The list of surfaces for the scene. */
    protected ArrayList<Surface> surfaces = new ArrayList<Surface>();
    public void addSurface(Surface toAdd) { toAdd.addTo(surfaces); }
	
	/** The list of materials in the scene. */
	protected ArrayList<Shader> shaders = new ArrayList<Shader>();
	public void addShader(Shader toAdd) { shaders.add(toAdd); }
	
	/** Image to be produced by the renderer **/
	protected Image outputImage;
	public Image getImage() { return this.outputImage; }
	public void setImage(Image outputImage) { this.outputImage = outputImage; }
	
	/** samples is the number of samples per pixel **/
    protected int samples;
    public int getSamples() { return this.samples==0 ? 1 : this.samples; }
    public void setSamples(int n) {samples = (int)Math.round(Math.sqrt(n)); }

    /** The AABB tree that stores the surfaces */
    protected AABB aabbTree;

    /**
     * Initialize AABB tree from the list of all surfaces in the scene.
     * Send the list to AABB and call createTree.
     */
    public void initializeAABB() {
        Surface[] surfaceArray = new Surface[surfaces.size()];
        int count = 0;
        for (Surface s : surfaces) {
            s.computeBoundingBox();
            surfaceArray[count++] = s;
        }
        AABB.setSurfaces(surfaceArray);
        aabbTree = AABB.createTree(0, surfaceArray.length);
    }
		
	/**
	 * Set outRecord to the first intersection of ray with the scene. Return true
	 * if there was an intersection and false otherwise. If no intersection was
	 * found outRecord is unchanged.
	 *
	 * @param outRecord the output IntersectionRecord
	 * @param ray the ray to intesect
	 * @return true if and intersection is found.
	 */
	public boolean getFirstIntersection(IntersectionRecord outRecord, Ray ray) {
		return aabbTree.intersect(outRecord, ray, false);
	}
	
	/**
	 * Shadow ray calculations can be considerably accelerated by not bothering to find the
	 * first intersection.  This record returns any intersection of the ray and the surfaces
	 * and returns true if one is found.
	 * @param ray the ray to intersect
	 * @return true if any intersection is found
	 */
	public boolean getAnyIntersection(Ray ray) {
		return aabbTree.intersect(null, ray, true);	
	}
	
//	NOW LOCATED IN AABB.JAVA--------------------------------------------------------------------------------------	
//	NOW LOCATED IN AABB.JAVA--------------------------------------------------------------------------------------	
//	NOW LOCATED IN AABB.JAVA--------------------------------------------------------------------------------------	
//	/**
//	 * Set outRecord to the first intersection of ray with the scene. Return true
//	 * if there was an intersection and false otherwise. If no intersection was
//	 * found outRecord is unchanged.
//	 *
//	 * @param outRecord the output IntersectionRecord
//	 * @param ray the ray to intersect
//	 * @param anyIntersection if true, will immediately return when found an intersection, and won't modify outRecord
//	 * @return true if and intersection is found.
//	 */
//	public boolean intersect(IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection) {
//
//		boolean ret = false;
//		IntersectionRecord tmp = new IntersectionRecord();
//		Ray ray = new Ray(rayIn.origin, rayIn.direction);
//		ray.start = rayIn.start;
//		ray.end = rayIn.end;
//		
//		for(Iterator<Surface> iter = surfaces.iterator(); iter.hasNext();) {
//			Surface s = iter.next();
//            if (s.intersect(tmp, ray) && tmp.t < ray.end ) {
//                if(anyIntersection) return true;
//                ret = true;
//                ray.end = tmp.t;
//                if(outRecord != null)
//                    outRecord.set(tmp);
//            }
//			
//		}
//		return ret;
//	}
//	NOW LOCATED IN AABB.JAVA--------------------------------------------------------------------------------------	
//	NOW LOCATED IN AABB.JAVA--------------------------------------------------------------------------------------	
//	NOW LOCATED IN AABB.JAVA--------------------------------------------------------------------------------------	
}