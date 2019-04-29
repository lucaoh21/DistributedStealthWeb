import java.io.File;
import java.io.FileWriter;
import java.lang.String;
import java.lang.Integer;
import java.io.IOException;
public class GenHtml {
    private static String prefix = new String("<html><body><h1>It works ");
    private static String suffix = new String("!</h1></body></html>");
    public static void main(String[] args) {
	for (int i = 0; i < 30; i++){
	    String path = "../html/doc" + Integer.toString(i) + ".html";
	    File file = new File(path);
	    try {
		if (file.createNewFile()){
		    FileWriter writer = new FileWriter(path);
		    writer.write(prefix);
		    writer.write(Integer.toString(i));
		    writer.write(suffix);
		    writer.flush();
		    writer.close();
		}  
	    } catch (IOException e){
	    
	    }
	   
	}
    }
}