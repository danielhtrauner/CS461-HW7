package ray.shader;

import java.util.ArrayList;

import ray.IntersectionRecord;
import ray.Ray;
import ray.RayTracer;
import ray.Scene;
import ray.light.Light;
import ray.math.Color;
import ray.math.Vector3;

public class Glass extends Shader {
        
    protected double refractiveIndex;
    public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
        

    public Glass() { }
        
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

        	// Calculate Reflection Ray
			toEye.normalize();
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

            // Calculate T Ray
            // t = N(d-n(d-n))/Nt - n*sqrt(1-(n^2(1-(d.n)^2))/nt^2)
            Vector3 t = new Vector3();
            Vector3 dir = new Vector3(toEye);
            Vector3 nor = new Vector3(record.normal);
            Vector3 term1 = new Vector3();
            Vector3 term2 = new Vector3();
            double n;
            double nt;
            
            if(internal==false) {
	            n = 1.0;
	            nt = refractiveIndex;
            } else {
                nt = 1.0;
                nor.scale(-1);
                n = refractiveIndex;
            }
            
            dir.scale(-1);
            
            term1.set(nor);
            term1.scale(nor.dot(dir));
            term1.scale(-1);
            term1.add(dir);
            term1.scale(n/nt);
            
            term2.set(nor);
            double coefficientSquared = 1- ( (Math.pow(n,2) * (1 - Math.pow( dir.dot(nor), 2 ) ) ) / Math.pow(nt, 2) );
            term2.scale(Math.sqrt(coefficientSquared));
            t.set(term1);
            t.sub(term2);
            
            Ray tRay = new Ray(record.location,t);
            tRay.start = Ray.EPSILON;
            tRay.end = Double.POSITIVE_INFINITY;
           
	    	if(depth<=MAXDEPTH && contribution>=MINCONTRIBUTION) {

				// Implement Fresnel equation (Shirley 13.1)
				// Scale the vector L by -1 to get R that points away from the surface as the normal does.
				Vector3 r = new Vector3(toEye);
				r.normalize();
				double r0 = Math.pow(((refractiveIndex-1)/(refractiveIndex+1)), 2);
				double cosTheta = record.normal.dot(r);
				
				if (cosTheta<0)
					return;
				
				double reflection = r0 + (1-r0)*Math.pow((1-cosTheta),5);

				Color reflectionColor = new Color(0,0,0);
				Color internalColor = new Color(0,0,0);
				Color totalColor = new Color(0,0,0);
				
				// compute reflected contribution (make a recursive call to shadeRay)
		    	depth++;
		    	
				RayTracer.shadeRay(reflectionColor, scene, reflectionRay, lights, depth, contribution, internal);
				reflectionColor.scale(reflection);
				
				if(coefficientSquared>=0) {
					RayTracer.shadeRay(internalColor, scene, tRay, lights, depth, contribution, !internal);
					internalColor.scale(1-reflection);
				}
				
				totalColor.add(reflectionColor);
				totalColor.add(internalColor);
				
				outColor.set(totalColor);
	    	}
	    	
    }
}
