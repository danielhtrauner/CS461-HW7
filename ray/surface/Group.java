package ray.surface;

import java.util.ArrayList;

import ray.IntersectionRecord;
import ray.Ray;
import ray.math.Matrix4;
import ray.math.Point3;
import ray.math.Vector3;

public class Group extends Surface {

    public static final Vector3 X_AXIS = new Vector3(1, 0, 0);
    public static final Vector3 Y_AXIS = new Vector3(0, 1, 0);
    public static final Vector3 Z_AXIS = new Vector3(0, 0, 1);

    /** List of objects under this group. */
    ArrayList<Surface> objs = new ArrayList<Surface>();
        
    /** The transformation matrix associated with this group. */
    private Matrix4 transformMat;
        
    /** A shared temporary matrix */
    static Matrix4 tmp = new Matrix4();
        
    public Group() {
        transformMat = new Matrix4();
        transformMat.setIdentity();
    }
        
    /**
     * Compute tMat, tMatInv, tMatTInv for this group and propagate values to the children under it.
     * @param cMat The transformation matrix of the parent for this node.
     * @param cMatInv The inverse of cMat.
     * @param cMatTInv The inverse of the transpose of cMat.
     */
    public void setTransformation(Matrix4 cMat, Matrix4 cMatInv, Matrix4 cMatTInv) {
        tMat = new Matrix4(transformMat);
        tMat.leftCompose(cMat);
        tMatInv = new Matrix4();
        tMatInv.invert(tMat);
        tMatTInv = new Matrix4();
        tMatTInv.transpose(tMatInv);
        
        for(Surface s : objs)
            s.setTransformation(tMat, tMatInv, tMatTInv);

        computeBoundingBox();
    }
        
    public void setTranslate(Vector3 T) { 
        tmp.setTranslate(T);
        transformMat.rightCompose(tmp);
    }
        
    public void setRotate(Point3 R) {
        tmp.setRotate(R.z, Z_AXIS);
        transformMat.rightCompose(tmp);
        tmp.setRotate(R.y, Y_AXIS);
        transformMat.rightCompose(tmp);
        tmp.setRotate(R.x, X_AXIS);
        transformMat.rightCompose(tmp);
    }
        
    public void setScale(Vector3 S) { 
        tmp.setScale(S);
        transformMat.rightCompose(tmp);
    }
        
    public void addSurface(Surface a) {
        objs.add(a);
    }
        
    public boolean intersect(IntersectionRecord outRecord, Ray ray) { return false; }
    public void computeBoundingBox() {  }
        
    public void addTo(ArrayList<Surface> sceneSurfaces, ArrayList<Surface> groups) {
        for (Surface s : objs) {
            s.addTo(sceneSurfaces, groups);
        }
        groups.add(this); // need to add last so that setTransformation processes children first
    }

}
