package ray.shader;

import java.util.ArrayList;

import ray.IntersectionRecord;
import ray.Ray;
import ray.RayTracer;
import ray.Scene;
import ray.light.Light;
import ray.math.Color;
import ray.math.Vector3;

/**
 * A Glass material. 
 *
 */
public class Glass extends Shader {
        
    protected double refractiveIndex;
    public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
        
        
    public Glass() { }
        
    /**
     * @see ray.shader.Shader#shade(ray.math.Color, Scene,
     *      Lights, ray.math.Vector3, ray.IntersectionRecord, depth, contribution, internal)
     */
    public void shade(Color outColor, Scene scene, ArrayList<Light> lights, Vector3 toEye, 
                      IntersectionRecord record, int depth, double contribution, boolean internal) {
                
        // Implement Snell's law, make a recursive call to shadeRay  
        // for the reflected and the refracted rays. Use Fresnel's equations (Shirley 13.1)
        // to weight the contributions of the corresponding rays.
            
        outColor.set(0, 0, 0);

        if (depth+1 > MAXDEPTH)
            return; // recursion limit exceeded
        
        // compute reflection
        Vector3 d = new Vector3(toEye);
        d.normalize();
        Vector3 n = record.normal;
        n.normalize();
        if (internal)
            n.scale(-1);  // reverse normal if we're coming from the inside
        
        double dn = d.dot(n);
        if (dn < 0)
            return; // hit surface (triangle?) from the back, no contribution
        
        d.scale(-1);
        d.scaleAdd(2*dn, n);

        Ray reflectedRay = new Ray();
        reflectedRay.origin.set(record.location);
        reflectedRay.direction.set(d);
        reflectedRay.makeOffsetRay();
        
        // compute transmitted refracted ray (text Ch 13.1)
        double nnt = internal ? refractiveIndex : 1.0 / refractiveIndex; // ratio of n / n_t
        //double nnt = 1.0 / refractiveIndex; // ratio of n / n_t
        // the expression under the square root on p. 305
        double s = 1.0 - nnt * nnt * (1 - dn * dn);
        if (s < 0) { // total internal reflection, no transmitted ray
            RayTracer.shadeRay(outColor, scene, reflectedRay, lights, depth+1, contribution, internal);
            return;
        }
        // else transmitted ray exists
        Vector3 t = new Vector3(toEye);
        t.normalize();
        t.scale(-1); // d comes from eye!
        t.scaleAdd(dn, n); // (d - n(d . n))  // (d is already negated in dn)
        t.scale(nnt); // first part of eqn on p. 305
        t.scaleAdd(-Math.sqrt(s), n); // second part of eqn
        t.normalize();

        Ray transmittedRay = new Ray();
        transmittedRay.origin.set(record.location);
        transmittedRay.direction.set(t);
        transmittedRay.makeOffsetRay();
       
        // Fresnel factor
        double r0 = (refractiveIndex - 1) / (refractiveIndex + 1);
        r0 *= r0;
        double cosTheta = internal ? Math.abs(t.dot(n)) : dn;
        double r = r0 + (1-r0) * Math.pow(1 - cosTheta, 5); // fraction of reflected light

        if ((1-r)*contribution > MINCONTRIBUTION) {
            Color transmColor = new Color();
            RayTracer.shadeRay(transmColor, scene, transmittedRay, lights, depth+1, (1-r)*contribution, !internal);
            outColor.scaleAdd(1-r, transmColor);
        }

        if (r*contribution > MINCONTRIBUTION) {
            Color reflColor = new Color();
            RayTracer.shadeRay(reflColor, scene, reflectedRay, lights, depth+1, r*contribution, internal);
            outColor.scaleAdd(r, reflColor);
        }
    }
}