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


		if(depth>MAXDEPTH || contribution<MINCONTRIBUTION)
			return;
		toEye.normalize();

		// Viewing Vector (i)
		Vector3 i = new Vector3(toEye);
		i.scale(-1);
		
		// Normal Vector (norm)
		Vector3 norm = new Vector3(record.normal);

		
		// Calculate Reflection Ray
		Vector3 l = new Vector3(toEye);
		l.scale(-1);
		double d = -2*l.dot(norm);
		norm.scale(d);
		l.add(norm);
		l.normalize();
		Ray reflectionRay = new Ray(record.location,l);
		reflectionRay.start = Ray.EPSILON;
		reflectionRay.end = Double.POSITIVE_INFINITY;

		// Calculate T Ray
		// t = N(d-n(d.n))/Nt - n*sqrt(1-(n^2(1-(d.n)^2))/nt^2)
		Vector3 t = new Vector3();

		//Reset normal
		norm = new Vector3(record.normal);
		Vector3 term1 = new Vector3();
		Vector3 term2 = new Vector3();
		double n;
		double nt;

		if(internal==false) {
			n = 1.0;
			nt = refractiveIndex;
		} else {
			norm.scale(-1);
			n = refractiveIndex;
			nt = 1.0;
		}



		term1.set(norm);
		term1.scale(norm.dot(i));
		term1.scale(-1);
		term1.add(i);
		term1.scale(n/nt);

		term2.set(norm);
		double coefficientSquared = 1 - ( (Math.pow(n/nt,2) * (1 - Math.pow( i.dot(norm), 2 ) ) ) );
		term2.scale(Math.sqrt(coefficientSquared));
		t.set(term1);
		term2.scale(-1);
		t.add(term2);

		Ray tRay = new Ray(record.location,t);
		tRay.start = Ray.EPSILON;
		tRay.end = Double.POSITIVE_INFINITY;

		
		/* Implement Fresnel equation (Shirley 13.1)
		 * Scale the vector L by -1 to get R that points away from the surface as the normal does.
		 * Calculate Schlick Constant
		 */
		double r0 = Math.pow(((refractiveIndex-1)/(refractiveIndex+1)), 2);
		double cosTheta = norm.dot(toEye);

		if (cosTheta<0)
			return;

		double R = r0 + (1-r0)*Math.pow((1-cosTheta),5);

		// Define new Colors
		Color reflectionColor = new Color(0,0,0);
		Color refractionColor = new Color(0,0,0);
		Color totalColor = new Color(0,0,0);

		// compute reflected contribution (make a recursive call to shadeRay)
		depth++;

		RayTracer.shadeRay(reflectionColor, scene, reflectionRay, lights, depth, contribution, internal);
		reflectionColor.scale(R);

		if(coefficientSquared>=0) {
			RayTracer.shadeRay(refractionColor, scene, tRay, lights, depth, contribution, !internal);
			refractionColor.scale(1-R);
		}

		totalColor.add(reflectionColor);
		totalColor.add(refractionColor);

		outColor.set(totalColor);


	}
}