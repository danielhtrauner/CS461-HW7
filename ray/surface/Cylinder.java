package ray.surface;

import ray.IntersectionRecord;
import ray.Ray;
import ray.math.Point3;
//import ray.math.Vector3;
import ray.math.Vector3;

public class Cylinder extends Surface {
	
	/** The center of the bottom of the cylinder  x , y ,z components. */
	protected final Point3 center = new Point3();
	public void setCenter(Point3 center) { this.center.set(center); }
	
	/** The radius of the cylinder. */
	protected double radius = 1.0;
	public void setRadius(double radius) { this.radius = radius; }
	
	/** The height of the cylinder. */
	protected double height = 1.0;
	public void setHeight(double height) { this.height = height; }
	
	public Cylinder() { }
	
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
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
		rayIn = untransformRay(rayIn);

		 boolean inters = false;
	        double tw = Double.POSITIVE_INFINITY; // t value for intersection with cylinder wall
	        double te = Double.POSITIVE_INFINITY; // t value for intersection with end caps
	        Point3 q = new Point3(); // intersection point

	        // compute ray-cylinder intersection
	        // same as for sphere, except, set all z components to 0
	        Vector3 d = new Vector3(rayIn.direction);
	        Vector3 ec = new Vector3();
	        ec.sub(rayIn.origin, center); // e-c

	        // before setting the z components to 0, save for later
	        double dz = d.z;
	        double ecz = ec.z;
	        d.z = 0;
	        ec.z = 0;

	        // the parameters of the quadratic equation A*t^2 + B*t + C = 0:
	        double A = d.dot(d);
	        double B = 2 * d.dot(ec);
	        double C = ec.dot(ec) - radius * radius;

	        double discr = B*B - 4*A*C;
	        if (discr >= 0.0) { // intersection with cylinder wall
	            double s = Math.sqrt(discr);
	            double t1 = (-B + s) / (2 * A);
	            double t2 = (-B - s) / (2 * A);
	            double t = Double.POSITIVE_INFINITY;
	            if (t1 >= rayIn.start && t1 < t)
	                t = t1;
	            if (t2 >= rayIn.start && t2 < t)
	                t = t2;
	            if (t >= rayIn.start &&  t <= rayIn.end && t < Double.POSITIVE_INFINITY) {
	                rayIn.evaluate(q, t); // intersection point
	                if (Math.abs(q.z - center.z) <= height/2) {  // sidewall of cylinder
	                    inters = true;
	                    tw = t;
	                }
	            }
	        }

	        // check intersection with endcaps

	        // e.z + t * d.z == center.z +/- height/2;
	        // t = (center.z - e.z +/- height/2) / d.z;
	        double te1 = (-ecz - height/2) / dz;
	        double te2 = (-ecz + height/2) / dz;
	        double tem = Math.min(te1, te2);
	        double dir = (te1 < te2) ? -1.0 : 1.0; // whether top or bottom end
	        rayIn.evaluate(q, tem);  // intersection location with endcap
	        Vector3 rad = new Vector3();
	        rad.sub(center, q); // compute radius vector
	        rad.z = 0;          // in x-y plane
	        if (rad.dot(rad) <= radius*radius) { // intersection with end cap
	            if (tem >= rayIn.start &&  tem <= rayIn.end) {
	                inters = true;
	                te = tem;
	            }
	        }

	        if (inters) {
	            double t = Math.min(tw, te);
	            outRecord.t = t;
	            rayIn.evaluate(q, t);
	            outRecord.location.set(q);
	            outRecord.surface = this;
	            if (tw < te) { // intersection with wall
	                outRecord.normal.sub(q, center);
	                outRecord.normal.z = 0; // set z component of normal to 0
	                outRecord.normal.normalize();
	            } else { // intersection with endcap
	                outRecord.normal.set(0, 0, dir);
	            }
	            transformVector(outRecord.normal);
	            transformPoint(outRecord.location);
	            return true;
	        }
        

        return false;
    }
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Cylinder " + center + " " + radius + " " + height + " "+ shader + " end";
	}

	@Override
    public void computeBoundingBox() {
        // Compute the bounding box and store the result in
        // averagePosition, minBound, and maxBound.
        averagePosition = new Point3(center);
        minBound = new Point3(center);
        maxBound = new Point3(center);
        Vector3 offs = new Vector3(radius, radius, height);
        minBound.sub(offs);
        maxBound.add(offs);
        transformBoundingBox();
    }

}

