import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

public class GenerateStepDefs {

	public static void main(String[] args) throws Exception {
		//get all of our files for the listing
		if ( args.length != 1 ) {
			System.out.println( "Please provide the fiel location for the step definitions" );
			return;			
		}
		File fileDef = new File( args[0] );
		String baseDir = fileDef.getAbsolutePath().substring(0, fileDef.getAbsolutePath().indexOf( "\\src\\" ) + 5 );
//System.out.println( baseDir );
		if( !fileDef.exists() ) {
			System.out.println( "Step defs file does not exist: " + fileDef );
			return;
		}
		HashSet<String> fileDefs = listFilesForFolder( fileDef );
		PrintWriter writer = new PrintWriter("step.js", "UTF-8");
		
		HashSet<String> includes = new HashSet<String>();
		HashSet<String> enumerations = new HashSet<String>();
		
		for( String file : fileDefs ) {
//System.out.println( "Parsing file: " + file );
			BufferedReader br = null;
			String line = "";
			boolean next = false;
			String step = "";
			try {
				br = new BufferedReader( new FileReader( file ) );
				while ( ( line = br.readLine() ) != null ) {
					if ( line.startsWith( "import " ) ) {
						includes.add( line.substring( 7, line.length() - 1 ) );
					}
					if ( next == true ) {
						line = line.substring( line.indexOf( "(" )+1, line.indexOf( ")" ) );
//System.out.println( line );
						if ( line.length() > 0 ) {
					    	String[] objects = line.split(",");
					    	for( String object : objects ) {
					    		object = object.trim();
					    		String[] pieces = object.split(" ");
					    		String type = "";
					    		if ( pieces[0].startsWith("List<" ) && pieces[0].endsWith(">" ) ) {
					    			pieces[0] = pieces[0].substring( 5, pieces[0].length() - 1 );
					    			pieces[1] += "List";
					    		}
					    		if ( pieces[0].equalsIgnoreCase( "long") || pieces[0].equalsIgnoreCase( "int") ) {
					    			type = "\"number\"";
					    		}
					    		else if ( pieces[0].equalsIgnoreCase( "string" ) || pieces[0].equalsIgnoreCase( "char" ) 
					    				|| pieces[0].equalsIgnoreCase( "Integer" ) || pieces[0].equalsIgnoreCase( "Double" ) ) {
					    			type = "\"text\"";
					    		} else {
					    			type = pieces[0];
					    			enumerations.add( type );
//System.out.println( object + ":" + type );
					    		}
					    		step += ", new keypair( \"" + pieces[1] + "\", " + type + " )";
					    	}
						}
				    	step += " ) );";
						next = false;
						writer.println( step );
						step = "";
					}
					line = line.trim();
				    if ( line.startsWith("@Given") || line.startsWith("@When") ) {
				    	line = line.substring( line.indexOf( "^" )+1, line.indexOf( "$" ) );
				    	line = line.replaceAll( "\\(\\?:.*?\\)", "<span class='any'>...</span>" );
				    	line = line.replaceAll( "\\(.*?\\)", "XXXX" );
				    	line = line.replaceAll( "\\[(.*?)\\]\\?", "<span class='opt'>$1</span>" );
//System.out.println( line );
				    	step += "testSteps.whens.push( new step( \"" + line + "\"";
				    	next = true;
				    }
				    if ( line.startsWith("@Then") ) {
				    	line = line.substring( line.indexOf( "^" )+1, line.indexOf( "$" ) );
				    	line = line.replaceAll( "\\(\\?:.*?\\)", "<span class='any'>...</span>" );
				    	line = line.replaceAll( "\\(.*?\\)", "XXXX" );
				    	line = line.replaceAll( "\\[(.*?)\\]\\?", "<span class='opt'>$1</span>" );
//System.out.println( line );
				    	step += "testSteps.thens.push( new step( \"" + line + "\"";
				    	next = true;
				    }
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//close our file
		writer.close();
		
		//to prepend, we must copy, then unlink our old file
		writer = new PrintWriter("steps.js", "UTF-8");
		//write our enumerations
		writer.println( "//our enumerations" );
		for( String enumeration : enumerations) {
			for ( String include : includes ) {
				if ( include.endsWith( "." + enumeration ) ) {
					include = include.substring(0, include.lastIndexOf( "." ) );
					include = include.replaceAll( "\\.", "\\\\" );
					BufferedReader br = null;
					String line = "";
					try {
						br = new BufferedReader( new FileReader( baseDir + include + ".java" ) );
						while ( ( line = br.readLine() ) != null ) {
							line = line.trim();
							if ( line.startsWith( "public enum " + enumeration ) ) {
								String array = "var " + line.substring( 12 );
								//CLUDGE FOR PRODUCTS
								if ( array.equalsIgnoreCase( "var Products {" ) ) {
									array = "var Products { Basic, Basic_POEHB, Classic, Clear, Enhanced, Family_High, Family_Low, HCR_AH, HCR_AL, HCR_PH, HCR_PL, HCR_FBM, HCR_FBP, HCR_FPM, HCR_FPP, HCR_PBM, HCR_PBP, HCR_PPM, HCR_PPP, Pediatric_High, Pediatric_High_SHP, Pediatric_Low, Pediatric_Low_SHP, Preferred, Preferred_POEHB, Premium, Premium_Plus, Traditional };";
								}
								array = array.replace( "{ ", " = new Array( \"" );
								array = array.replace( " }", "\" )" );
								array = array.replace( ", ", "\", \"" );
								writer.println( array );											
							}
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (br != null) {
							try {
								br.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		writer.println( "" );
		//write our old lines
		writer.println( "//our steps" );
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader( new FileReader( "step.js" ) );
			while ( ( line = br.readLine() ) != null ) {
				writer.println( line );
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//close our file
		writer.close();
		new File("step.js").delete();
	}
	
	/**
	 * a method to recursively retrieve all the files 
	 * in a folder
	 * @param folder: the folder to check for files
	 * @return ArrayList<String>: an ArrayList with 
	 * the of multiple files
	 * @throws IOException 
	 */
	public static HashSet<String> listFilesForFolder( File folder ) {
		HashSet<String> files = new HashSet<String>();
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            files.addAll( listFilesForFolder( fileEntry ) );
	        } else {
	            files.add( fileEntry.getPath() );
	        }
	    }
		return files;
	}
}
