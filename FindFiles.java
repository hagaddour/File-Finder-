import java.io.*;
import java.util.*;
import java.util.regex.Matcher; 
import java.util.regex.Pattern; 

public class FindFiles {

    // Print command-line syntax
    static void helpMessage() {
        System.out.println("Usage: java FindFiles filetofind [-option arg]");
        System.out.println("-help                     :: print out a help page and exit the program.");
        System.out.println("-r                        :: execute the command recursively in subfiles.");
        System.out.println("-reg                      :: treat `filetofind` as a regular expression when searching.");
        System.out.println("-dir [directory]          :: find files starting in the specified directory.");
        System.out.println("-ext [ext1,ext2,...]      :: find files matching [filetofind] with extensions [ext1, ext2,...].");
    }
    // if file exists within the speificed dir return the abs path
    // otherwise do nothing
    static void getFilePath(String dir, String file) {
        try {  
            File f = new File(dir+file);
            System.out.println("looking for " + file + " in " + dir);
            if (f.exists() && f.isFile()) {
                String canonical = f.getCanonicalPath(); 
                System.out.println(canonical);
            } 
        } 
        catch (Exception e) { 
            System.err.println(e.getMessage()); 
        }
    }
    // Function used for -r option
    // checks if a file is dir and then calls getFile 
    static void searchSubDir(String dir, String filename) {
        File specifiedDir = new File(dir);
        File[] files = specifiedDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                continue;
            }
            if(file.isDirectory()) {
               String newDir = dir + file.getName() + "/" ;
               getFilePath(newDir, filename);
               searchSubDir(newDir, filename);
            }
        }
    }
    // Function for option -r checks if a file matches rexp req
    static void searchRegexp(String dir, String rexp) {
        File specifiedDir = new File(dir);
        Pattern pat = Pattern.compile(rexp);
        File[] files = specifiedDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                Matcher match = pat.matcher(file.getName());
                if (match.matches()) {
                	getFilePath(dir, file.getName());
                }
            }
        } 
    }
    // function when -r and -reg are combined
    static void searchSubReg(String dir, String rexp) {
        File specifiedDir = new File(dir);
        Pattern pat = Pattern.compile(rexp);
        File[] files = specifiedDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                Matcher match = pat.matcher(file.getName());
                if (match.matches()) {
                    getFilePath(dir, file.getName());
                }
            }
            else if(file.isDirectory()) {
               searchSubReg(dir + file.getName() + "/", rexp);
            }
        } 
    }
    // Driver code
    public static void main (String[] args) {
        // in case no arg is provided
    	if (args.length == 0) {
            helpMessage();
            System.exit(0);
        }
        // Assumed user provided a file/regex to return path for
        String filetofind = args[0];
        String direct = "./";
        // Handling the case when user just calls help
        if (filetofind.equals("-help")) {
            helpMessage();
            System.exit(0);
        }
        // parse optional arguments
        java.util.HashMap<String, String> options = parse(args);
        // If none provided just return the file path if it exists in the current dir
        if (options.size() == 0) {
            getFilePath(direct, filetofind);
        }
        else {
        	// updating the dir
            if (options.containsKey("dir")) {
                direct = options.get("dir") + "/";
            }
            // checking if the user provided -reg option
            if (options.containsKey("reg")) {
                if (options.containsKey("r")) {
                    if (options.containsKey("ext")) {
                        String [] extensions = options.get("ext").replaceAll("\\s+","").split(",");
                        for (String extension : extensions) {
                            searchSubReg(direct, filetofind+ "." + extension);
                        }
                    }
                    else {
                        searchSubReg(direct, filetofind);
                    }
                }
                else if (options.containsKey("ext")) {
                    String [] extensions = options.get("ext").replaceAll("\\s+","").split(",");
                    for (String extension : extensions) {
                        searchRegexp(direct, filetofind+  "." + extension);
                    }
                }
                else {
                    searchRegexp(direct, filetofind);
                }
            }
            // checking if the user provided -r option
            else if (options.containsKey("r")) {
                if (options.containsKey("ext")) {
                    String [] extensions = options.get("ext").replaceAll("\\s+","").split(",");
                    for (String extension : extensions) {
                        getFilePath(direct, filetofind +  "." + extension);
                        searchSubDir(direct, filetofind +  "." + extension);
                    }
                }
                else {
                    getFilePath(direct, filetofind);
                    searchSubDir(direct, filetofind);
                }
            }
            else {
                if (options.containsKey("ext")) {
                    String [] extensions = options.get("ext").replaceAll("\\s+","").split(",");
                    for (String extension : extensions) {
                        getFilePath(direct, filetofind +  "." + extension);
                    }
                }
                else {
                    getFilePath(direct, filetofind);
                }
            }
        }
    }

    // Build a dictionary of options and possible parameters
    static HashMap<String, String> parse(String[] args) {
        HashMap<String, String> arguments = new HashMap<>();
        String key = null;
        String value = null;

        // process each argument as either a key or value in the pair
        for(String entry : args) {
            // skipping first entry as that's assumed to be the file
            if (entry == args[0]) continue;
            else if (entry.startsWith("-")) {
                // if we already have a key, and then find a second key 
                // before we've found the corresponding value, it's an error.
                if (entry.equals("-help")) {
                    helpMessage();
                    System.exit(0);
                }
                else if (entry.equals("-r")) {
                    arguments.put("r", "True");
                }
                else if (entry.equals("-reg")) {
                    arguments.put("reg", "True");
                }
                else if (entry.equals("-dir") || entry.equals("-ext")) {
                    key = entry.substring(1);   // skip leading "-"
                }
                // user provided an invalid optino printing an error message and exiting
                else {
                    System.out.println(entry + " is an invalid option. Please supply valid options from the following list...");
                    helpMessage();
                    System.exit(0);
                }
            } else {
                // User did not provide dir or ext or didn't provide a file
                if (key == null) {
                    System.out.println("Please supply minimum number of arguments from the following");
                    helpMessage();
                    System.exit(0);
                }
                value = entry;
            }
            if (key != null && value != null) {
                arguments.put(key, value);
                key = null;
                value = null;
            }
        }
        //  check final values
        if (key != null) {
        	System.out.println(key + " is missing an arg. Please supply valid arg");
        	helpMessage();
            System.exit(0);
        }
        if (value != null) {
        	System.out.println(key + " is missing an arg. Please supply valid arg from the following list... ");
            helpMessage();
            System.exit(0);
        }
        // return dictionary
        return arguments;
    }
}
