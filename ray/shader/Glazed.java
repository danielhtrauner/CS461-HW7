package ray.shader;

import java.util.ArrayList;

import ray.IntersectionRecord;
import ray.Ray;
import ray.RayTracer;
import ray.Scene;
import ray.light.Light;
import ray.math.Color;
import ray.math.Vector3;

public class Glazed extends Shader {
        
    protected double refractiveIndex;
    public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
        
    protected Shader substrate;
    public void setSubstrate(Shader substrate) { this.substrate = substrate; }
        
    public Glazed() { }
        
    /**
     * Calculate the BRDF value for this material at the intersection described in record.
     * Returns the BRDF color in outColor.
     * @param outColor Space for the output color
     * @param scene The scene
     * @param lights The lights
     * @param toEye Vector pointing towards the eye
     * @param record The intersection record, which hold the location, normal, etc.
     * @param depth The depth of recursive calls.
     * @param contribution The contribution of the current ray.
     * @param internal You can ignore this for glazed.
     */
    public void shade(Color outColor, Scene scene, ArrayList<Light> lights, Vector3 toEye, 
                      IntersectionRecord record, int depth, double contribution, boolean internal) {

        for (Light li : lights) {
        	//Calculate Reflection Ray
            Vector3 l = new Vector3();
            l.sub(li.position, record.location); // unit vector towards light
            l.normalize();
            Vector3 norm = new Vector3(record.normal);
            double d = -2*l.dot(norm);
            norm.scale(d);
            l.sub(norm);
            l.normalize();
            Ray reflectionRay = new Ray(record.location,l);
            
	    	if(depth<=MAXDEPTH&&contribution>=MINCONTRIBUTION) {

				// Implement Fresnel equation (Shirley 13.1)
				// Scale the vector L by -1 to get R that points away from the surface as the normal does.
				Vector3 r = new Vector3(l);
				r.scale(-1);
				r.normalize();
				
				double nt = this.refractiveIndex;
				double r0 = Math.pow(((nt-1)/(nt+1)), 2);
				double theta = Math.acos(record.normal.dot(r)/(record.normal.length()*r.length()));
				double reflection = r0 + Math.pow((1-r0)*(1-Math.cos(theta)),5);
				
				// WHAT TO DO WITH CONTRIBUTION??????

				// compute reflected contribution (make a recursive call to shadeRay)
				RayTracer.shadeRay(outColor, scene, reflectionRay, lights, depth, contribution, internal);
				
				// compute substrate contribution (call substrate.shade(...))
				substrate.shade(outColor, scene, lights, toEye, record, depth, contribution, internal);
		    	depth++;
	    	}
        }
    }
}
