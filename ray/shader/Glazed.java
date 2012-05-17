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

    		toEye.normalize();
        	//Calculate Reflection Ray
    		// r = -toEye - 2(-toEye.dot(n))*n;
            Vector3 l = new Vector3(toEye);
            l.scale(-1);
            Vector3 norm = new Vector3(record.normal);
            double d = -2*l.dot(norm);
            norm.scale(d);
            l.add(norm);
            l.normalize();
            Ray reflectionRay = new Ray(record.location,l);
            reflectionRay.start = Ray.EPSILON;
            reflectionRay.end = Double.POSITIVE_INFINITY;

	    	if(depth<=MAXDEPTH&&contribution>=MINCONTRIBUTION) {

				// Implement Fresnel equation (Shirley 13.1)
				// Scale the vector L by -1 to get R that points away from the surface as the normal does.
				Vector3 r = new Vector3(toEye);

				r.normalize();

				double r0 = Math.pow(((refractiveIndex-1)/(refractiveIndex+1)), 2);
				
				double cosTheta = record.normal.dot(r);
				
				if (cosTheta<0)
					return;
				
				double reflection = r0 + (1-r0)*Math.pow((1-cosTheta),5);
				//System.out.println(reflection);

				Color reflectionColor = new Color(0,0,0);
				Color substrateColor = new Color(0,0,0);
				
				// compute reflected contribution (make a recursive call to shadeRay)
		    	depth++;
				RayTracer.shadeRay(reflectionColor, scene, reflectionRay, lights, depth, contribution, internal);
				
				// compute substrate contribution (call substrate.shade(...))
				substrate.shade(substrateColor, scene, lights, toEye, record, depth, contribution, internal);
				reflectionColor.scale(reflection);
				substrateColor.scale(1-reflection);
				reflectionColor.add(substrateColor);
				outColor.set(reflectionColor);


	    	}
	    	
    }
}
