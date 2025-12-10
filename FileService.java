/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package courserecoverysystem.service;

/**
 *
 * @author seany
 */
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import courserecoverysystem.data.FileData;

/*
Here you need to know is 
    int getHeaderIndex(String filename, String column) - this will just return the index of the header

    String createLineString(List<String> values) - this just make array into a string with | inbetween

    String createContentString(List<String> lines) - this will make things in an Arraylist to have /n inbetween each other

    List<String> parseLine (String line) - does the opposite of createLineString(List<String> values)

    Map<String, String> assignHeaderLine(String filename, List<String> values) - this is should be used during your retrieve so you can get the
    value cleanly


    THIS IS THE IMPORTANT ONES
    List<String> retrieveOneMatchLine(String filename, String searchColumn, String value)
    List<String> retrieveAllMatchLine(String filename, String searchColumn, String value)
    List<String> retrieveAllLine(String filename)
    void deleteOneMatchLine(String filename, String searchColumn, String value)
    void deleteAllMatchLine(String filename, String searchColumn, String value)
    void editOneMatchLine(String filename, String searchColumn, String searchValue, String editColumn, String editValue)
    void editAllMatchLine(String filename, String searchColumn, String searchValue, String editColumn, String editValue)
    void writeAppend(String filename, String content)
    void writeOverride(String filename, String content)
    
    ykw just call me if you got any question - Yap
*/

public class FileService {
    String separator = "\\|";
    final private String[] user = {"uid","username", "password", "role"};
    final private String[] student = {"studentID", "userID", "firstName", "lastName", "major", "year"};
    final private String[] course = {"couseID", "lecturerID", "courseName", "credit", "semester", "examWeight", "assignmentWeight"};
    final private String[] recoveryEnrollment = {"enrollmentID", "studentID", "courseID", "type", "status"};
    //TODO need to add all the headers for the file
    
    public List<String> dbHeaderSearch(String filename) { 
        if (filename.endsWith(".txt")) {
            filename = filename.substring(0, filename.length() - 4);
        }

        switch (filename) {
            case "user":
                return Arrays.asList(user);
                
            case "student":
                return Arrays.asList(student);
                
            case "course":
                return Arrays.asList(course);
                
            case "recoveryEnrollment":
                return Arrays.asList(recoveryEnrollment);
                
            default:
                return new ArrayList<>();
        }
    }
    
    public int getHeaderIndex(String filename, String column) {
        List<String> headers = dbHeaderSearch(filename);
        return headers.indexOf(column);
    }
    
    
    
    public List<String> trim(List<String> values) {
        List<String> trimmedValues = new ArrayList<>();
        
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                continue;
            }
            value = value.strip();
            trimmedValues.add(value);
        }
        return trimmedValues;   
    }
    
    
    
    public boolean lengthCheck(List<String> header, List<String> line) {
        return header.size() == line.size();
    }    
    

    public String createLineString(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        
        return String.join("|", trim(values));
    }
    
    
    
    public String createContentString(List<String> lines) {
        StringBuilder content = new StringBuilder();
        lines = trim(lines);
        
        for (String line : lines) {
            content.append(line).append("\n");
        }    
        return content.toString();
    }    
    
    
    
    public List<String> parseLine (String line) {
        String[] parts = line.split(separator);
        return Arrays.asList(parts);
    }
    
    
    
    public Map<String, String> assignHeaderLine(String filename, List<String> values) {
        List<String> header = dbHeaderSearch(filename);
        Map<String, String> mappedValues = new HashMap<>();
        
        if (!lengthCheck(header, values)) {
            return mappedValues; //TODO add error handling, like this one really needs one
        }
        
        for (int i = 0; i < values.size(); i++) {
            mappedValues.put(header.get(i), values.get(i));
        }
        return mappedValues;
    }
            
    
    
    //This is just helper method you dont need to read 
    private List<String> getHeaderAndLines(String filename, String searchColumn, List<String> lines, boolean checkEmpty) {
        List<String> header = dbHeaderSearch(filename);
        int columnIndex = header.indexOf(searchColumn);
        if (columnIndex == -1) ExceptionService.invalidColumn(searchColumn);

        if (lines == null) lines = new FileData().fileRead(filename);
        if (checkEmpty && lines.isEmpty()) ExceptionService.emptyFile(filename);
        return header;
    }

    private List<List<String>> parseLines(List<String> lines, List<String> header) {
        List<List<String>> parsed = new ArrayList<>();
        for (String line : lines) {
            List<String> values = parseLine(line);
            if (!lengthCheck(header, values)) ExceptionService.invalidLength("File");
            parsed.add(values);
        }
        return parsed;
    }
       
    private void deleteLines(String filename, String searchColumn, String value, boolean deleteAll) {
        FileData data = new FileData();
        List<String> lines = data.fileRead(filename);
        List<String> header = getHeaderAndLines(filename, searchColumn, lines, true);
        int colIndex = header.indexOf(searchColumn);

        List<String> newLines = new ArrayList<>();
        boolean deleted = false;
        for (List<String> values : parseLines(lines, header)) {
            if ((!deleteAll && !deleted && values.get(colIndex).equals(value))) {
                deleted = true;
                continue;
            }
            if (deleteAll && values.get(colIndex).equals(value)) continue;
            newLines.add(createLineString(values));
        }
        data.fileOverrrideWrite(filename, createContentString(newLines));
    }
    
    //TODO
    private void editLines(String filename, String searchColumn, String searchValue, String editColumn, String editValue, List<String> newRow, boolean editAll) {
        FileData data = new FileData();
        List<String> lines = data.fileRead(filename);
        List<String> header = getHeaderAndLines(filename, searchColumn, lines, true);
        int searchIndex = header.indexOf(searchColumn);
        int editIndex = editColumn != null ? header.indexOf(editColumn) : -1;

        if (searchIndex == -1 || (editColumn != null && editIndex == -1)) return;

        if (newRow != null && !lengthCheck(header, newRow)) {
            ExceptionService.invalidLength(filename);
        }

        List<String> newLines = new ArrayList<>();
        boolean edited = false;

        for (List<String> values : parseLines(lines, header)) {
            boolean match = values.get(searchIndex).equals(searchValue);
            if (!match || (edited && !editAll)) {
                newLines.add(createLineString(values));
                continue;
            }

            if (newRow != null) {
                newLines.add(createLineString(newRow));
            } else {
                values.set(editIndex, editValue);
                newLines.add(createLineString(values));
            }
            edited = true;
        }

        data.fileOverrrideWrite(filename, createContentString(newLines));
    }
    
    
    
    public List<String> retrieveOneSubstringMatchLine(String filename, String searchColumn, String substring) {
        List<String> lines = new FileData().fileRead(filename);
        List<String> header = getHeaderAndLines(filename, searchColumn, lines, true);
        int colIndex = header.indexOf(searchColumn);

        for (List<String> values : parseLines(lines, header)) {
            if (values.get(colIndex).contains(substring)) {
                return values;
            }
        }
        return new ArrayList<>();
    }

    public List<String> retrieveAllSubstringMatchLine(String filename, String searchColumn, String substring) {
        List<String> lines = new FileData().fileRead(filename);
        List<String> header = getHeaderAndLines(filename, searchColumn, lines, true);
        int colIndex = header.indexOf(searchColumn);
        List<String> matched = new ArrayList<>();

        for (List<String> values : parseLines(lines, header)) {
            if (values.get(colIndex).contains(substring)) {
                matched.add(createLineString(values));
            }
        }
        return matched;
    }
    
    public List<String> retrieveOneMatchLine(String filename, String searchColumn, String value) {
            List<String> lines = new FileData().fileRead(filename);
            List<String> header = getHeaderAndLines(filename, searchColumn, lines, true);
            int colIndex = header.indexOf(searchColumn);

            for (List<String> values : parseLines(lines, header)) {
                if (values.get(colIndex).equals(value)) return values;
            }
            return new ArrayList<>();
        }

    public List<String> retrieveAllMatchLine(String filename, String searchColumn, String value) {
        List<String> lines = new FileData().fileRead(filename);
        List<String> header = getHeaderAndLines(filename, searchColumn, lines, true);
        int colIndex = header.indexOf(searchColumn);
        List<String> matched = new ArrayList<>();

        for (List<String> values : parseLines(lines, header)) {
            if (values.get(colIndex).equals(value)) matched.add(createLineString(values));
        }
        return matched;
    }
        
    public List<String> retrieveAllLine(String filename) {
        FileData data = new FileData();
        return data.fileRead(filename);
    } 


    
    public void deleteOneMatchLine(String filename, String searchColumn, String value) {
        deleteLines(filename, searchColumn, value, false);
    }

    public void deleteAllMatchLine(String filename, String searchColumn, String value) {
        deleteLines(filename, searchColumn, value, true);
    }


    
    public void editOneMatchLine(String filename, String searchColumn, String searchValue, String editColumn, String editValue) {
        editLines(filename, searchColumn, searchValue, editColumn, editValue, null, false);
    }

    public void editAllMatchLine(String filename, String searchColumn, String searchValue, String editColumn, String editValue) {
        editLines(filename, searchColumn, searchValue, editColumn, editValue, null, true);
    }

    public void editWholeRow(String filename, String searchColumn, String searchValue, List<String> newValues) {
        FileData data = new FileData();
        List<String> lines = data.fileRead(filename);
        List<String> header = getHeaderAndLines(filename, searchColumn, lines, true);
        int searchIndex = header.indexOf(searchColumn);

        if (!lengthCheck(header, newValues)) {
            ExceptionService.invalidLength(filename);
        }

        List<String> newLines = new ArrayList<>();
        boolean edited = false;

        for (List<String> values : parseLines(lines, header)) {
            if (!edited && values.get(searchIndex).equals(searchValue)) {
                newLines.add(createLineString(newValues));
                edited = true;
            } else {
                newLines.add(createLineString(values));
            }
        }

        data.fileOverrrideWrite(filename, createContentString(newLines));
    }
    
    
    
    public void writeAppend(String filename, String content) {
        FileData data = new FileData();
        data.fileAppendWrite(filename, content);
    }
    
    public void writeOverride(String filename, String content) {
        FileData data = new FileData();
        data.fileOverrrideWrite(filename, content);
    }
    
    //TODO add string match
}
