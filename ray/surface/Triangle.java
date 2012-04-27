package ray.surface;

import ray.IntersectionRecord;
import ray.Ray;
import ray.math.Point3;
import ray.math.Vector3;
import ray.shader.Shader;

/**
 * @author ags, modified by DS 4/2012
 */
public class Triangle extends Surface {
    /** The derived normal vector of this triangle, if not loaded from file  */
    Vector3 norm;
        
    /** The mesh that contains this triangle  */
    Mesh owner;
        
    /** 3 indices to the vertices of this triangle. */
    int []index;
        
    public Triangle(Mesh owner, int index0, int index1, int index2, Shader material) {
        this.owner = owner;
        index = new int[3];
        index[0] = index0;
        index[1] = index1;
        index[2] = index2;
                
        if(!owner.existsNormals()) {
            Point3 v0 = owner.getVertex(index0);
            Point3 v1 = owner.getVertex(index1);
            Point3 v2 = owner.getVertex(index2);
                
            Vector3 e0 = new Vector3(), e1 = new Vector3();
            e0.sub(v1, v0);
            e1.sub(v2, v0);
            norm = new Vector3();
            norm.cross(e0, e1);
            norm.normalize();
        } 
                                
        this.setShader(material);
    }

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

    	Vector3 d = new Vector3(rayIn.direction);
    	Point3 p = new Point3(rayIn.origin);
    	Vector3 n = new Vector3();
    	
    	if (owner.existsNormals()) {
            Vector3 na = owner.getNormal(index[0]);
            Vector3 nb = owner.getNormal(index[1]);
            Vector3 nc = owner.getNormal(index[2]);

            Vector3 total = new Vector3(na);
            total.add(nb);
            total.add(nc);
            total.scale(1/3);
            n.set(total);
            
    	} else
    		n.set(norm);
    	
        Point3 a = owner.getVertex(index[0]);
        Point3 b = owner.getVertex(index[1]);
        Point3 c = owner.getVertex(index[2]);
        
        // Calculate t ("Parts 1&2")
        Vector3 ap = new Vector3();
        ap.sub(a, p);
        double t = ap.dot(n) / d.dot(n);
        
        if (t<0)
        	return false;
        
        // Calculate intersection point x
        Vector3 dTmp = new Vector3();
        dTmp.set(d);
        dTmp.scale(t);
        Point3 x = new Point3();
        x.set(p);
        x.add(dTmp);
        
        // "Part 3"
        Vector3 ba = new Vector3();
        ba.sub(b, a);
        Vector3 cb = new Vector3();
        cb.sub(c, b);
        Vector3 ac = new Vector3();
        ac.sub(a, c);
        
        Vector3 xa = new Vector3();
        xa.sub(x, a);
        Vector3 xb = new Vector3();
        xb.sub(x, b);
        Vector3 xc = new Vector3();
        xc.sub(x, c);
        
        Vector3 baCrossxa = new Vector3();
        baCrossxa.cross(ba, xa);
        Vector3 cbCrossxb = new Vector3();
        cbCrossxb.cross(cb, xb);
        Vector3 acCrossxc = new Vector3();
        acCrossxc.cross(ac, xc);
        
        double q1, q2, q3;
        
        q1 = baCrossxa.dot(n);
        q2 = cbCrossxb.dot(n);
        q3 = acCrossxc.dot(n);
        
        if (q1<=0)
        	return false;
        if (q2<=0)
        	return false;
        if (q3<=0)
        	return false;
        
        rayIn.evaluate(outRecord.location, t);
        outRecord.normal.set(n);
        return true;
    }

    public String toString() {
        return "Triangle ";
    }
}
