package ray.shader;

import java.util.ArrayList;

import ray.IntersectionRecord;
import ray.Scene;
import ray.light.Light;
import ray.math.Color;
import ray.math.Vector3;

/**
 * A Phong material. Uses the Modified Blinn-Phong model which is energy
 * preserving and reciprocal.
 *
 * @author ags, modified by DS 2/2012
 */
public class Phong extends Shader {
	
	/** The color of the diffuse reflection. */
	protected final Color diffuseColor = new Color(1, 1, 1);
	public void setDiffuseColor(Color diffuseColor) { this.diffuseColor.set(diffuseColor); }
	
	/** The color of the specular reflection. */
	protected final Color specularColor = new Color(1, 1, 1);
	public void setSpecularColor(Color specularColor) { this.specularColor.set(specularColor); }
	
	/** The exponent controlling the sharpness of the specular reflection. */
	protected double exponent = 1.0;
	public void setExponent(double exponent) { this.exponent = exponent; }
	
	public Phong() { }
	
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
            l.sub(li.position, record.location); // unit vector towards light
            l.normalize();
                        
            // Lambertian component
            double ni = Math.max(0, record.normal.dot(l)); // dot product of surf normal and light dir
            Color c = new Color(diffuseColor);
            c.scale(li.intensity);  // elt-wise product of color channels of diffuse obj color and light color
            outColor.scaleAdd(ni, c);
                        
            // specular component
            Vector3 h = new Vector3(toEye);
            h.normalize();
            h.add(l);
            h.scale(0.5); // h is half vector between view dir and light dir
            h.normalize();
            double nh = Math.max(0, record.normal.dot(h)); // dot product of surf normal and h
            nh = Math.pow(nh, exponent);
            c.set(specularColor);
            c.scale(li.intensity);  // elt-wise product of color channels of specular obj color and light color
            outColor.scaleAdd(nh, c);
                        
        }
		
	}
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		
		return "phong " + diffuseColor + " " + specularColor + " " + exponent + " end";
	}
}