package ray.shader;

import java.util.ArrayList;

import ray.IntersectionRecord;
import ray.Scene;
import ray.light.Light;
import ray.math.Color;
import ray.math.Vector3;

/**
 * A Lambertian material scatters light equally in all directions. BRDF value is
 * a constant
 *
 * @author ags, modified by DS 2/2012
 */
public class Lambertian extends Shader {
	
	/** The color of the surface. */
	protected final Color diffuseColor = new Color(1, 1, 1);
	public void setDiffuseColor(Color inDiffuseColor) { diffuseColor.set(inDiffuseColor); }
	
	public Lambertian() { }
	
	/**
	 * Calculate the BRDF value for this material at the intersection described in record.
	 * Returns the BRDF color in outColor.
	 * @param outColor Space for the output color
	 * @param scene The scene
	 * @param lights The lights
	 * @param toEye Vector pointing towards the eye
	 * @param record The intersection record, which hold the location, normal, etc.
	 */
	public void shade(Color outColor, Scene scene, ArrayList<Light> lights, Vector3 toEye, 
			IntersectionRecord record, int depth, double contribution, boolean internal) {
		
        outColor.set(0, 0, 0);
        for (Light li : lights) {
            if (isShadowed(scene, li, record))
                continue;
            Vector3 l = new Vector3();
            l.sub(li.position, record.location); // vector towards light
            l.normalize();
            double ni = Math.max(0, record.normal.dot(l)); // dot product of surf normal and light dir
            Color c = new Color(diffuseColor);
            c.scale(li.intensity);  // elt-wise product of color channels of obj color and light color
            outColor.scaleAdd(ni, c);
        }
		
	}
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		
		return "Lambertian: " + diffuseColor;
	}
	
}