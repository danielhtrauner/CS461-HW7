import java.io.*;
import java.util.Scanner;

public class MshGen {

private Mode mode;
 private enum Mode {
	    VERTICES, FACES, NORMALS, TEXTURES 
	}
  private int num_v = 0; 
  private int num_f = 0; 
  private int num_n = 0;
  private int num_t = 0;
  
  private int vertex_i = 0;
  private int normal_i = 0;
  private int face_i = 0;
  private int texture_i = 0;
  private float[] vertices = new float[2000000];
  private float[] normals = new float[2000000];
  private float[] textures = new float[2000000];
  private int[] faces = new int[2000000];

  
	public static final void main(String[] args) {
		new MshGen();
	}
  /**
   Constructor.
   @param aFileName full name of an existing, readable file.
 * @throws FileNotFoundException 
  */
  public MshGen(String string) throws FileNotFoundException{
	fFile = new File(string);
	processLineByLine();
	writeToFile();
	log("Done.");

  }
  
  public MshGen(){
	 try {
		new MshGen("/home/dtrauner/Download/thinker_complete.obj");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	 fFile = new File("/home/dtrauner/Download/thinker_complete.obj");
  }
  
  public final void processLineByLine() throws FileNotFoundException {
	    Scanner scanner = new Scanner(new FileReader(fFile));
	    try {
	      while ( scanner.hasNextLine() ){
	        processLine( scanner.nextLine() );
	      }
	    }
	    finally {
	      scanner.close();
	    }
	  }
  
  /** 
  * Process line by line
  * @throws FileNotFoundException 
  */
  protected void processLine(String aLine){
    Scanner scanner = new Scanner(aLine);
    scanner.useDelimiter("\\s|//|/");
    while ( scanner.hasNext() ){
	      String tmp = scanner.next();
	      if (tmp.contains("v") & !tmp.contains("n") & !tmp.contains("t")) {mode = Mode.VERTICES;}
	      else if (tmp.contains("vn")) {mode = Mode.NORMALS;}
	      else if (tmp.contains("vt")) {mode = Mode.TEXTURES;}
	      else if (tmp.contains("f")) {mode = Mode.FACES;}
	      else if (mode == Mode.VERTICES){
	    	  vertices[vertex_i++] = Float.parseFloat(tmp);
	    	  vertices[vertex_i++] = Float.parseFloat(scanner.next());
	    	  vertices[vertex_i++] = Float.parseFloat(scanner.next());
	    	  num_v++;
	      } else if (mode == Mode.NORMALS){
	    	  normals[normal_i++] = Float.parseFloat(tmp); 
	    	  normals[normal_i++] = Float.parseFloat(scanner.next());
	    	  normals[normal_i++] = Float.parseFloat(scanner.next());
	    	  num_n++;
	      } else if (mode == Mode.TEXTURES){
	    	  textures[texture_i++] = Float.parseFloat(tmp); 
	    	  textures[texture_i++] = Float.parseFloat(scanner.next());
	    	  num_t++;
	      } else if (mode == Mode.FACES) {// minus 1 to comply to the msh format
	    	  if (num_n == 0 && num_t == 0){
		    	  faces[face_i] = Integer.parseInt(tmp)-1; 
		    	  faces[face_i+1] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+2] = Integer.parseInt(scanner.next())-1; 
		    	  face_i = face_i + 3;
	    	  } else if ((num_n != 0 && num_t == 0) || (num_n == 0 && num_t != 0)) {
		    	  faces[face_i] = Integer.parseInt(tmp)-1; 
		    	  faces[face_i+1] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+2] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+3] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+4] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+5] = Integer.parseInt(scanner.next())-1; 
		    	  face_i = face_i + 6;
	    	  } else if (num_n != 0 && num_t != 0) {
		    	  faces[face_i] = Integer.parseInt(tmp)-1; 
		    	  faces[face_i+1] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+2] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+3] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+4] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+5] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+6] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+7] = Integer.parseInt(scanner.next())-1; 
		    	  faces[face_i+8] = Integer.parseInt(scanner.next())-1; 
		    	  face_i = face_i + 9;
	    	  }
	    	  num_f++;
	      } else {scanner.next();}
    }

  }
  
  protected void writeToFile(){
	  FileWriter fstream;
	try {
		fstream = new FileWriter("/home/dtrauner/Download/thinker_complete.msh");
		  BufferedWriter out = new BufferedWriter(fstream);
		  out.write(num_v+"\n");
		  out.write(num_f+"\n");
		  //write vertices
		  out.write("vertices\n");
		  for (int i = 0; i < num_v*3; i++) {
			  out.write(vertices[i]+"\n");
		  }
		  
		  //write triangles
		  out.write("triangles\n");
		  if (num_n == 0 && num_t == 0) {
			  for (int i = 0; i < num_f*3; i++) {
				  out.write(faces[i]+"\n");
			  }
		  } else if (num_n != 0 && num_t == 0 || num_n == 0 && num_t != 0) {
			  int i = 0;
			  while (i < face_i) {
				  out.write(faces[i]+"\n");
				  out.write(faces[i+2]+"\n");
				  out.write(faces[i+4]+"\n");
				  i = i + 6;
			  }
			  
			  if (num_t != 0) {
				  out.write("texcoords\n");
				  for (int vertex = 0; vertex < num_v; vertex++){
					  i = 0; //reset the counter
					  while (i < face_i) {
						  if (faces[i] == vertex) {
							  out.write(textures[2*faces[i+1]]+"\n");
							  out.write(textures[2*faces[i+1]+1]+"\n");
							  break;
						  }
						  i = i + 2;
					  }
				  }
			  }
			  
			  if (num_n != 0) {
				  out.write("normals\n");
			  
				  for (int vertex = 0; vertex < num_v; vertex++){
					  i = 0; //reset the counter
					  while (i < face_i) {
						  if (faces[i] == vertex) {
							  out.write(normals[3*faces[i+1]]+"\n");
							  out.write(normals[3*faces[i+1]+1]+"\n");
							  out.write(normals[3*faces[i+1]+2]+"\n");
							  break;
						  }
						  i = i + 2;
					  }
					  
				  }
			  }
		  } else if (num_n != 0 && num_t != 0) {
			  int i = 0;
			  while (i < face_i) {
				  out.write(faces[i]+"\n");
				  out.write(faces[i+3]+"\n");
				  out.write(faces[i+6]+"\n");
				  i = i + 9;
			  }
			  out.write("normals\n");

			  for (int vertex = 0; vertex < num_v; vertex++){
				  i = 0; //reset the counter
				  while (i < face_i) {
					  if (faces[i] == vertex) {
						  out.write(normals[3*faces[i+2]]+"\n");
						  out.write(normals[3*faces[i+2]+1]+"\n");
						  out.write(normals[3*faces[i+2]+2]+"\n");
						  break;
					  }
					  i = i + 3;
				  }
			  }
			  
			  out.write("texcoords\n");
			  for (int vertex = 0; vertex < num_v; vertex++){
				  i = 0; //reset the counter
				  while (i < face_i) {
					  if (faces[i] == vertex) {
						  out.write(textures[2*faces[i+1]]+"\n");
						  out.write(textures[2*faces[i+1]+1]+"\n");
						  break;
					  }
					  i = i + 3;
				  }
			  }
		  }
		  out.close();
	} catch (IOException e) {
		e.printStackTrace();
	}

  }
  
  private final File fFile;
  
  private static void log(Object aObject){
    System.out.println(String.valueOf(aObject));
  }
  
} 
