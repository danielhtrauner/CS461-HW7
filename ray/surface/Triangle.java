package ray.surface;

import javax.vecmath.Matrix3d;


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
    	Point3 e = new Point3(rayIn.origin);

        Point3 a = owner.getVertex(index[0]);
        Point3 b = owner.getVertex(index[1]);
        Point3 c = owner.getVertex(index[2]);
    	Matrix3d A = new Matrix3d();
    	A.m00 = a.x - b.x;
    	A.m01 = a.y - b.y;
    	A.m02 = a.z - b.z;
    	A.m10 = a.x - c.x;
    	A.m11 = a.y - c.y;
    	A.m12 = a.z - c.z;
    	A.m20 = d.x;
    	A.m21 = d.y;
    	A.m22 = d.z;
    	double det=A.determinant();
    	
    	Matrix3d betaMatrix = new Matrix3d();
    	betaMatrix.m00 = a.x-e.x;
    	betaMatrix.m01 = a.y-e.y;
    	betaMatrix.m02 = a.z-e.z;
    	betaMatrix.m10 = a.x-c.x;
    	betaMatrix.m11 = a.y-c.y;
    	betaMatrix.m12 = a.z-c.z;
    	betaMatrix.m20 = d.x;
    	betaMatrix.m21 = d.y;
    	betaMatrix.m22 = d.z;
    	double beta = betaMatrix.determinant()/det;
    	
    	Matrix3d gammaMatrix = new Matrix3d();
    	gammaMatrix.m00 = a.x-b.x;
    	gammaMatrix.m01 = a.y-b.y;
    	gammaMatrix.m02 = a.z-b.z;
    	gammaMatrix.m10 = a.x-e.x;
    	gammaMatrix.m11 = a.y-e.y;
    	gammaMatrix.m12 = a.z-e.z;    	
    	gammaMatrix.m20 = d.x;
    	gammaMatrix.m21 = d.y;
    	gammaMatrix.m22 = d.z;
    	double gamma = gammaMatrix.determinant()/det;

    	Matrix3d tMatrix = new Matrix3d();
    	tMatrix.m00 = a.x-b.x;
    	tMatrix.m01 = a.y-b.y;
    	tMatrix.m02 = a.z-b.z;
    	tMatrix.m10 = a.x-c.x;
    	tMatrix.m11 = a.y-c.y;
    	tMatrix.m12 = a.z-c.z;    	
    	tMatrix.m20 = a.x-e.x;
    	tMatrix.m21 = a.y-e.y;
    	tMatrix.m22 = a.z-e.z;
    	double t = tMatrix.determinant()/det;
    	
        if(!owner.existsNormals()) {
        	outRecord.normal.set(norm);
        } else {
        	Vector3 normal = new Vector3();
        	
        	Vector3 na = owner.getNormal(index[0]);
        	Vector3 nb = owner.getNormal(index[1]);
        	Vector3 nc = owner.getNormal(index[2]);
        	
        	na.scale(1-beta-gamma);
        	nb.scale(beta);
        	nc.scale(gamma);
        	
        	normal.set(na);
        	normal.add(nb);
        	normal.add(nc);
        	
        	normal.normalize();
        	
        	outRecord.normal.set(normal);
        }
    	
    	if(t<rayIn.start||t>rayIn.end) {
    		return false;
    	}
    	
    	if(gamma<0||gamma>1) {
    		return false;
    	}
    	
    	if(beta<0||beta>(1-gamma)) {
    		return false;
    	}

    	rayIn.evaluate(outRecord.location, t);
    	outRecord.surface = this;
    	outRecord.t = t;
    	
    	return true;
    } 
    
    public String toString() {
        return "Triangle ";
    }

	public void computeBoundingBox() {
		
		maxBound = new Point3(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY);
		minBound = new Point3(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
		
		maxBound.x = Math.max(owner.getVertex(index[0]).x, owner.getVertex(index[1]).x);
		maxBound.x = Math.max(maxBound.x, owner.getVertex(index[2]).x);
		
		maxBound.y = Math.max(owner.getVertex(index[0]).y, owner.getVertex(index[1]).y);
		maxBound.y = Math.max(maxBound.y, owner.getVertex(index[2]).y);
		
		maxBound.z = Math.max(owner.getVertex(index[0]).z, owner.getVertex(index[1]).z);
		maxBound.z = Math.max(maxBound.z, owner.getVertex(index[2]).z);
		
		minBound.x = Math.min(owner.getVertex(index[0]).x, owner.getVertex(index[1]).x);
		minBound.x = Math.min(minBound.x, owner.getVertex(index[2]).x);
		
		minBound.y = Math.min(owner.getVertex(index[0]).y, owner.getVertex(index[1]).y);
		minBound.y = Math.min(minBound.y, owner.getVertex(index[2]).y);
		
		minBound.z = Math.min(owner.getVertex(index[0]).z, owner.getVertex(index[1]).z);
		minBound.z = Math.min(minBound.z, owner.getVertex(index[2]).z);

		averagePosition=new Point3();
		averagePosition.x = (owner.getVertex(index[0]).x+owner.getVertex(index[1]).x+owner.getVertex(index[2]).x)/3;
		averagePosition.y = (owner.getVertex(index[0]).y+owner.getVertex(index[1]).y+owner.getVertex(index[2]).y)/3;
		averagePosition.z = (owner.getVertex(index[0]).z+owner.getVertex(index[1]).z+owner.getVertex(index[2]).z)/3;
	}
}