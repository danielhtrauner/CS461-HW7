package ray.math;

/**
 * This class represents a generic homogeneous transformation. It has convenience
 * methods in it for multiplication with other transformations, for transforming
 * vectors, for creating rotation, scaling, and translational transformations,
 * as well as camera to frame transformations and vice versa.
 * 
 * @author ags
 */
public class Matrix4 {

    // the 16 entries
    public double[][] m = new double[4][4];

    /**
     * The default constructor initializes the matrix to I.
     */
    public Matrix4() {

        setIdentity();
    }
        
    public Matrix4(Matrix4 m) {
        set(m);
    }

    /**
     * Copies the values in the parameter into this matrix.
     * 
     * @param newM The values to copy.
     */
    public void set(double[][] newM) {

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                m[i][j] = newM[i][j];
    }

    /**
     * Copies the values from one matrix into this matrix.
     * 
     * @param newMat The matrix to copy.
     */
    public void set(Matrix4 newMat) {

        set(newMat.m);
    }

    /**
     * Sets this matrix to the identity matrix.
     */
    public void setIdentity() {

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                m[i][j] = (i == j) ? 1.0f : 0.0f;
    }

    /**
     * Sets this matrix to be a camera to frame matrix.
     * 
     * @param u The u vector of the frame.
     * @param v The v vector of the frame.
     * @param w The w vector of the frame.
     * @param p The origin of the frame.
     */
    public void setCtoF(Vector3 u, Vector3 v, Vector3 w, Vector3 p) {

        setIdentity();
        m[0][0] = u.x;
        m[0][1] = u.y;
        m[0][2] = u.z;
        m[1][0] = v.x;
        m[1][1] = v.y;
        m[1][2] = v.z;
        m[2][0] = w.x;
        m[2][1] = w.y;
        m[2][2] = w.z;
        Vector3 temp = new Vector3(p.x, p.y, p.z);
        rightMultiply(temp);
        m[0][3] = -temp.x;
        m[1][3] = -temp.y;
        m[2][3] = -temp.z;
    }

    /**
     * Sets this matrix to be a frame to camera matrix.
     * 
     * @param u The u vector of the frame.
     * @param v The v vector of the frame.
     * @param w The w vector of the frame.
     * @param p The origin of the frame.
     */
    public void setFtoC(Vector3 u, Vector3 v, Vector3 w, Vector3 p) {

        setIdentity();
        m[0][0] = u.x;
        m[0][1] = v.x;
        m[0][2] = w.x;
        m[0][3] = p.x;
        m[1][0] = u.y;
        m[1][1] = v.y;
        m[1][2] = w.y;
        m[1][3] = p.y;
        m[2][0] = u.z;
        m[2][1] = v.z;
        m[2][2] = w.z;
        m[2][3] = p.z;
    }

    /**
     * This sets the current matrix to a rotation about the indicated axis.
     * 
     * @param angle The magnitude (in radians) of the rotation.
     * @param axis The axis about which the rotation will occur.
     */
    public void setRotate(double angle, Vector3 axis) {

        angle = angle * Math.PI/180;
        Vector3 u = new Vector3(axis);
        u.normalize();
        double[] ua = new double[] { u.x, u.y, u.z };
        setIdentity();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                m[i][j] = ua[i] * ua[j];
        double cosTheta = (double) Math.cos(angle);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                m[i][j] += cosTheta * ((i == j ? 1.0f : 0.0f) - ua[i] * ua[j]);
        double sinTheta = (double) Math.sin(angle);
        m[1][2] -= sinTheta * u.x;
        m[2][1] += sinTheta * u.x;
        m[2][0] -= sinTheta * u.y;
        m[0][2] += sinTheta * u.y;
        m[0][1] -= sinTheta * u.z;
        m[1][0] += sinTheta * u.z;
    }

    /**
     * This sets the matrix to a translation transformation.
     * 
     * @param v The vector describing the translation.
     */
    public void setTranslate(Vector3 v) {

        setIdentity();
        m[0][3] = v.x;
        m[1][3] = v.y;
        m[2][3] = v.z;
    }

    /**
     * This sets the matrix to a scaling transformation.
     * 
     * @param v The vector that describes the scaling.
     */
    public void setScale(Vector3 v) {

        setIdentity();
        m[0][0] = v.x;
        m[1][1] = v.y;
        m[2][2] = v.z;
    }

    /** Temporary storage for doing a matrix multiply */
    private double[][] tempM = new double[4][4];

    /**
     * Concatenates t onto this matrix, and places the result in the second
     * parameter. result = t*this. There are no restrictions of the form t !=
     * result, or result != this, etc.
     * 
     * @param t The left operand.
     * @param result The destination for the result.
     */
    public void leftMultiply(Matrix4 t, Matrix4 result) {

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {

                tempM[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    tempM[i][j] += t.m[i][k] * m[k][j];
                }

            }
        }

        result.set(tempM);
    }

    /**
     * Sets this matrix to be the left product of t and itself. This = t*this.
     * 
     * @param t The left hand operand.
     */
    public void leftCompose(Matrix4 t) {

        leftMultiply(t, this);
    }

    /**
     * Concatenates this matrix onto t, and places the result in the second
     * parameter. result = this(t. There are no restrictions of the form t !=
     * result, or result != this, etc.
     * 
     * @param t The right hand operand
     * @param result The destination for the result.
     */
    public void rightMultiply(Matrix4 t, Matrix4 result) {

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tempM[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    tempM[i][j] += m[i][k] * t.m[k][j];
                }
            }
        }

        result.set(tempM);
    }

    /**
     * Sets this matrix to be the right product of t and itself. This = this*t.
     * 
     * @param t The right hand operand.
     */
    public void rightCompose(Matrix4 t) {

        rightMultiply(t, this);
    }

    /**
     * Transforms the vector by the current matrix and stores the result in the
     * given vector.
     * 
     * @param vInOut The vector to transform, and the destination vector.
     */
    public void rightMultiply(Vector3 vInOut) {

        rightMultiply(vInOut, vInOut);
    }

    /**
     * Transforms the v vector by this matrix, and places the result in vOut. v
     * and vOut may reference the same object.  Transforming a vector means 
     * transforming by the upper 3x3.
     * 
     * @param v The vector to transform.
     * @param vOut The destination vector.
     */
    public void rightMultiply(Vector3 v, Vector3 vOut) {

        double x = m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z;
        double y = m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z;
        double z = m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z;
        vOut.set(x, y, z);
    }

    /**
     * Transforms the point by the current matrix and stores the result in the
     * given point.
     * 
     * @param vInOut The point to transform, and the destination point.
     */
    public void rightMultiply(Point3 vInOut) {

        rightMultiply(vInOut, vInOut);
    }

    /**
     * Transforms the v point by this matrix, and places the result in vOut. v
     * and vOut may reference the same object.
     * 
     * @param v The point to transform.
     * @param vOut The destination point.
     */
    public void rightMultiply(Point3 v, Point3 vOut) {

        double x = m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z + m[0][3];
        double y = m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z + m[1][3];
        double z = m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z + m[2][3];
        double w = m[3][0] * v.x + m[3][1] * v.y + m[3][2] * v.z + m[3][3];
        vOut.set(x/w, y/w, z/w);
    }
        
    /**
     * Sets the value of this matrix to the transpose of the passed
     * matrix m1.  Safe when m1 == this.
     * @param m1 the matrix to be transposed
     */
    public void transpose(Matrix4 m1) {
                
        for (int r = 0; r < 3; r++)
            for (int c = r; c < 4; c++) {
                double temp = m1.m[r][c];
                m[r][c] = m1.m[c][r];
                m[c][r] = temp;
            }
    }
        
    /**
     * Transposes this matrix in place.
     */
    public void transpose() {
        transpose(this);
    }

    //
    // The following is re-used from the source code of the vecmath Vector4d class.
    //
        
    /**
     * Sets the value of this matrix to the matrix inverse
     * of the passed matrix m1.
     * @param m1 the matrix to be inverted
     */
    public void invert(Matrix4 m1) {
        invertGeneral(m1);
    }
        
    /**
     * Inverts this matrix in place.
     */
    public void invert() {
        invertGeneral(this);
    }

    /**
     * General invert routine.  Inverts m1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     *
     * Also note that since this routine is slow anyway, we won't worry
     * about allocating a little bit of garbage.
     */
    final void invertGeneral(Matrix4  m1) {
        double result[] = new double[16];
        int row_perm[] = new int[4];
        int i, r, c;

        // Use LU decomposition and backsubstitution code specifically
        // for floating-point 4x4 matrices.
        double[]    tmp = new double[16];  // scratch matrix

        // Copy source matrix to t1tmp 
        for (i=0,r=0; r<4; r++)
            for (c=0; c<4; c++,i++)
                tmp[i] = m1.m[r][c];

        // Calculate LU decomposition: Is the matrix singular? 
        boolean nonsingular = luDecomposition(tmp, row_perm);
        assert nonsingular;

        // Perform back substitution on the identity matrix 
        for(i=0;i<16;i++) result[i] = 0.0;
        result[0] = 1.0; result[5] = 1.0; result[10] = 1.0; result[15] = 1.0;
        luBacksubstitution(tmp, row_perm, result);

        // Copy result into the output array
        for (i=0,r=0; r<4; r++)
            for (c=0; c<4; c++,i++)
                this.m[r][c] = result[i];
    }

    /**
     * Given a 4x4 array "matrix0", this function replaces it with the 
     * LU decomposition of a row-wise permutation of itself.  The input 
     * parameters are "matrix0" and "dimen".  The array "matrix0" is also 
     * an output parameter.  The vector "row_perm[4]" is an output 
     * parameter that contains the row permutations resulting from partial 
     * pivoting.  The output parameter "even_row_xchg" is 1 when the 
     * number of row exchanges is even, or -1 otherwise.  Assumes data 
     * type is always double.
     *
     * This function is similar to luDecomposition, except that it
     * is tuned specifically for 4x4 matrices.
     *
     * @return true if the matrix is nonsingular, or false otherwise.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling, 
    //        _Numerical_Recipes_in_C_, Cambridge University Press, 
    //        1988, pp 40-45.
    //
    static boolean luDecomposition(double[] matrix0,
                                   int[] row_perm) {

        double row_scale[] = new double[4];

        // Determine implicit scaling information by looping over rows 
        {
            int i, j;
            int ptr, rs;
            double big, temp;

            ptr = 0;
            rs = 0;

            // For each row ... 
            i = 4;
            while (i-- != 0) {
                big = 0.0;

                // For each column, find the largest element in the row 
                j = 4;
                while (j-- != 0) {
                    temp = matrix0[ptr++];
                    temp = Math.abs(temp);
                    if (temp > big) {
                        big = temp;
                    }
                }

                // Is the matrix singular? 
                if (big == 0.0) {
                    return false;
                }
                row_scale[rs++] = 1.0 / big;
            }
        }

        {
            int j;
            int mtx;

            mtx = 0;

            // For all columns, execute Crout's method 
            for (j = 0; j < 4; j++) {
                int i, imax, k;
                int target, p1, p2;
                double sum, big, temp;

                // Determine elements of upper diagonal matrix U 
                for (i = 0; i < j; i++) {
                    target = mtx + (4*i) + j;
                    sum = matrix0[target];
                    k = i;
                    p1 = mtx + (4*i);
                    p2 = mtx + j;
                    while (k-- != 0) {
                        sum -= matrix0[p1] * matrix0[p2];
                        p1++;
                        p2 += 4;
                    }
                    matrix0[target] = sum;
                }

                // Search for largest pivot element and calculate
                // intermediate elements of lower diagonal matrix L.
                big = 0.0;
                imax = -1;
                for (i = j; i < 4; i++) {
                    target = mtx + (4*i) + j;
                    sum = matrix0[target];
                    k = j;
                    p1 = mtx + (4*i);
                    p2 = mtx + j;
                    while (k-- != 0) {
                        sum -= matrix0[p1] * matrix0[p2];
                        p1++;
                        p2 += 4;
                    }
                    matrix0[target] = sum;

                    // Is this the best pivot so far? 
                    if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
                        big = temp;
                        imax = i;
                    }
                }

                assert (imax >= 0);

                // Is a row exchange necessary? 
                if (j != imax) {
                    // Yes: exchange rows 
                    k = 4;
                    p1 = mtx + (4*imax);
                    p2 = mtx + (4*j);
                    while (k-- != 0) {
                        temp = matrix0[p1];
                        matrix0[p1++] = matrix0[p2];
                        matrix0[p2++] = temp;
                    }

                    // Record change in scale factor 
                    row_scale[imax] = row_scale[j];
                }

                // Record row permutation 
                row_perm[j] = imax;

                // Is the matrix singular 
                if (matrix0[(mtx + (4*j) + j)] == 0.0) {
                    return false;
                }

                // Divide elements of lower diagonal matrix L by pivot 
                if (j != (4-1)) {
                    temp = 1.0 / (matrix0[(mtx + (4*j) + j)]);
                    target = mtx + (4*(j+1)) + j;
                    i = 3 - j;
                    while (i-- != 0) {
                        matrix0[target] *= temp;
                        target += 4;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostionD4x4 and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a 4x4 matrix of floating-point values.  The procedure takes each
     * column of "matrix2" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     *
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling, 
    //        _Numerical_Recipes_in_C_, Cambridge University Press, 
    //        1988, pp 44-45.
    //
    static void luBacksubstitution(double[] matrix1,
                                   int[] row_perm,
                                   double[] matrix2) {

        int i, ii, ip, j, k;
        int rp;
        int cv, rv;

        //      rp = row_perm;
        rp = 0;

        // For each column vector of matrix2 ... 
        for (k = 0; k < 4; k++) {
            //      cv = &(matrix2[0][k]);
            cv = k;
            ii = -1;

            // Forward substitution 
            for (i = 0; i < 4; i++) {
                double sum;

                ip = row_perm[rp+i];
                sum = matrix2[cv+4*ip];
                matrix2[cv+4*ip] = matrix2[cv+4*i];
                if (ii >= 0) {
                    //              rv = &(matrix1[i][0]);
                    rv = i*4;
                    for (j = ii; j <= i-1; j++) {
                        sum -= matrix1[rv+j] * matrix2[cv+4*j];
                    }
                }
                else if (sum != 0.0) {
                    ii = i;
                }
                matrix2[cv+4*i] = sum;
            }

            // Backsubstitution 
            //      rv = &(matrix1[3][0]);
            rv = 3*4;
            matrix2[cv+4*3] /= matrix1[rv+3];

            rv -= 4;
            matrix2[cv+4*2] = (matrix2[cv+4*2] -
                               matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+2];

            rv -= 4;
            matrix2[cv+4*1] = (matrix2[cv+4*1] -
                               matrix1[rv+2] * matrix2[cv+4*2] -
                               matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+1];

            rv -= 4;
            matrix2[cv+4*0] = (matrix2[cv+4*0] -
                               matrix1[rv+1] * matrix2[cv+4*1] -
                               matrix1[rv+2] * matrix2[cv+4*2] -
                               matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+0];
        }
    }


    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return "Matrix4f: [" + m[0][0] + "  " + m[0][1] + "  " + m[0][2] + "  " + m[0][3] + "\n" + "           " + m[1][0] + "  " + m[1][1] + "  " + m[1][2] + "  " + m[1][3] + "\n" + "           "
            + m[2][0] + "  " + m[2][1] + "  " + m[2][2] + "  " + m[2][3] + "\n" + "           " + m[3][0] + "  " + m[3][1] + "  " + m[3][2] + "  " + m[3][3] + "]\n";
    }

}
