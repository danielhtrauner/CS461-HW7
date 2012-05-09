package ray.surface;

import ray.IntersectionRecord;
import ray.Ray;
import ray.math.Point3;
import ray.math.Vector3;

public class Box extends Surface {
	
	/* The corner of the box with the smallest x, y, and z components. */
	protected final Point3 minPt = new Point3();
	public void setMinPt(Point3 minPt) { this.minPt.set(minPt); }
	
	/* The corner of the box with the largest x, y, and z components. */
	protected final Point3 maxPt = new Point3();
	public void setMaxPt(Point3 maxPt) { this.maxPt.set(maxPt); }
	
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
        Vector3 d = rayIn.direction;
        Vector3 m1 = new Vector3();
        m1.sub(minPt, rayIn.origin); // minPt - e
        Vector3 m2 = new Vector3();
        m2.sub(maxPt, rayIn.origin); // maxPt - e
        
        //e.x + t1x * d.x == minPt.x;
        // t1x = (minPt.x - e.x) / d.x
        
        double t1x = m1.x / d.x, t1y = m1.y / d.y, t1z = m1.z / d.z;
        double t2x = m2.x / d.x, t2y = m2.y / d.y, t2z = m2.z / d.z;
        double minx = Math.min(t1x, t2x);
        double miny = Math.min(t1y, t2y);
        double minz = Math.min(t1z, t2z);
        double maxx = Math.max(t1x, t2x);
        double maxy = Math.max(t1y, t2y);
        double maxz = Math.max(t1z, t2z);
                
        double t1 = Math.max(minx,  Math.max(miny, minz));
        double t2 = Math.min(maxx,  Math.min(maxy, maxz));
  
        if (t1 > t2)
            return false; // no intersection

        // had to fix this code AGAIN (thanks Duncan!)
        
        double nfac = 1.0; // factor for normal direction for incoming vs. outgoing

        if (t1 < rayIn.start || t1 > rayIn.end) { // first intersection not in valid range
            if (t2 < rayIn.start || t2 > rayIn.end) // second intersection not in valid range
                return false;
            else { // interior ray, leaving box
                t1 = t2;
                nfac = -1.0;
            }
        }

        // else intersection at t1
        Point3 p = new Point3(); // intersection point
        rayIn.evaluate(p, t1);
        outRecord.location.set(p);
        outRecord.surface = this;
        outRecord.t = t1;

        // to compute normal, need to figure out which face got intersected
        if (minx >= miny && minx >= minz) { // x face
            if (t1x < t2x)
                outRecord.normal.set(-1, 0, 0);
            else
                outRecord.normal.set(1, 0, 0);
        } else if (miny >= minz) { // y face
            if (t1y < t2y)
                outRecord.normal.set(0, -1, 0);
            else
                outRecord.normal.set(0, 1, 0);
        } else { // z face
            if (t1z < t2z)
                outRecord.normal.set(0, 0, -1);
            else
                outRecord.normal.set(0, 0, 1);
        }
        outRecord.normal.scale(nfac);
        
        return true;
    }


	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Box " + minPt + " " + maxPt + " " + shader + " end";
	}

    public void computeBoundingBox() {
        // Compute the bounding box and store the result in
        // averagePosition, minBound, and maxBound.
        averagePosition = new Point3((minPt.x + maxPt.x)/2, (minPt.y + maxPt.y)/2, (minPt.z + maxPt.z)/2);
        minBound = new Point3(minPt);
        maxBound = new Point3(maxPt);
    }

}

