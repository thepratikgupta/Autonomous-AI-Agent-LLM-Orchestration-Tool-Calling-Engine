package com.prateek.ai_agent.service.PromptService.ToolSelectionService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolDescriptionService {

    public String getDescriptionOfRead(){
        return ("""
                Read and return the complete contents of a file.
                
                Use this tool when:
                - user wants to open a file
                - user wants to read a file
                - user asks to show file contents
                - user asks what is inside a file
                - user wants to inspect code
                - user wants to analyze configuration files
                
                Examples:
                - open application.properties
                - read UserService.java
                - show config.json
                - what is inside notes.txt
                
                Do not use:
                - for modifying files
                - for searching files by name
                - for listing directories
                
                Always verify the file exists before answering.
                """);
    }
    public String getDescriptionOfWrite(){
        return ("""
                Always create required files before writing the content if
                files/folders do not exist.
                
                Create a new file or completely replace an existing file.
                
                Use this tool when:
                - user wants to create a new file
                - user wants to generate an entire file
                - user wants to overwrite all content
                
                Examples:
                - create notes.txt
                - generate Dockerfile
                - write README.md
                
                Do not use:
                - for small edits
                - for code modifications
                - for replacing a few lines
                
                Prefer ApplyPatchFile for partial updates.
                """);
    }
    public String getDescriptionOfBash(){
        return ("""
            Execute a single safe Windows CMD command.
            
            Use ONLY if no dedicated tool can accomplish the task.
            
            Allowed examples:
            - dir
            - echo hello
            - type file.txt
            
            Restrictions:
            - single command only
            - no pipes
            - no chaining
            - no redirection
            - no privileged commands
            
            Always prefer:
            DirectoryTree,
            Read,
            SearchFiles,
            MoveFile,
            RenameFile,
            CreateDirectory
            
            Bash is the last option.
            """);
    }
    public String getDescriptionOfListFiles(){
        return ("""
            Display files and folders directly inside a directory.
            
            Use when:
            - list files
            - show files
            - show folder contents
            - what files are inside a folder
            - list current directory
            - display directory contents
            
            Examples:
            - list files
            - show files in config
            - what is inside src
            
            Only returns immediate children.
            Does not scan subdirectories.
            
            Use DirectoryTree for complete project structure.
            """);
    }
    public String getDescriptionOfCreateDirectory(){
        return ("""
            Create a new folder.
            
            Examples:
            - create logs folder
            - make backup directory
            - create src folder
            - create folder named data
            
            Used only for directories.
            """);
    }
    public String getDescriptionOfRenameFile(){
        return ("""
            Rename an existing file.
            
            Examples:
            - rename test.txt to notes.txt
            - change filename
            
            Do not modify file contents.
            Only changes the file name.
            """);
    }
    public String getDescriptionOfGetFileInfo(){
        return ("""
            Retrieve metadata about a file.
            
            Returns:
            - size
            - last modified time
            - creation time
            - file type
            
            Examples:
            - show file size
            - when was config.txt modified
            - file information
            
            Do not use to read file contents.
            """);
    }
    public String getDescriptionOfSearchFiles(){
        return ("""
            Search for files by name.
            If folder is not specified, search inside sandbox directory.
            Use when:
            - user wants to locate a file
            - user knows only part of the filename
            - user asks where a file is located
            
            Examples:
            - find UserService
            - search config file
            - locate application.properties
            
            Do not use:
            - for reading file contents
            - for searching text inside files
            
            Returns matching file paths.
            """);
    }
    public String getDescriptionOfMoveFile(){
        return ("""
            Move a file to another folder.
            Move multiple files to a folder.
            
            Examples:
            - move notes.txt to backup
            - move config.json to settings folder
            
            Does not modify file contents.
            Only changes file location.
            """);
    }
    public String getDescriptionOfReadMultipleFiles(){
        return ("""
            Read multiple files at once.
            
            Use when:
            - compare files
            - analyze several files
            - understand relationships
            - inspect multiple classes
            
            Examples:
            - compare UserService and AuthService
            - read controller and service classes
            - analyze config files
            
            Useful before code modifications.
            """);
    }
    public String getDescriptionOfApplyPatchFile(){
        return ("""
            Apply a small targeted modification to an existing file.
            
            Use when:
            - modify code
            - update configuration
            - replace text
            - change methods
            - fix bugs
            - edit existing files
            
            Workflow:
            1. Read the file first.
            2. Identify exact text.
            3. Replace only necessary content.
            4. Read the file again
            5. Identify other files that could have been affected.
            6. Use CodeSearch tool and then again ApplyPatchFile tool if needed.
            
            Examples:
            - change port 8080 to 8081
            - rename variable
            - fix method implementation
            - update database URL
            
            Always Prefer minimal changes.
            Never rewrite the entire file if only a small edit is needed.
            """);
    }
    public String getDescriptionOfWebSearch(){
        return ("""
           Search the internet and return relevant URLs.
           
           Use when:
                - user asks latest information
                - user asks news
                - user asks recent events
                - user asks current versions
                - user asks today's information
                - user asks internet search
         
                Search results are ordered by relevance.
                Result #1 is usually the most relevant result.
         
            Prefer:
            - official documentation
            - government websites
            - company websites
            - reputable sources
           
            Avoid:
            - low-quality blogs
            - duplicate results

                IMPORTANT:
                    - This tool returns search results.
                    - Each result contains title, URL and snippet.
                    - Choose the most relevant result.
                    - Use OpenWebPage to read webpage contents.
                    - Do not answer solely from snippets.
              
           Do not use:
            - when information already exists in files
            - when user asks about local project code
           
            Web information may change over time.
           """);
    }
    public String getDescriptionOfOpenWebPage(){
        return ("""
            Read and extract text from a webpage.
            
            Use when:
            - WebSearch returned URLs.
            - user provides a URL
            - Information from a webpage is needed.
            - analyze webpage
            - Article analysis is needed.
            - Summarization is needed.
            
                Guidelines:
                - Open only the most relevant pages.
                - Prefer official websites and trusted sources.
                - Usually 1 or 2 pages are sufficient.
                - Open additional pages only if necessary.
                - If enough information is available, answer the user.
            
                Usually read one page first.
                Read additional pages only if required.
            """);
    }
    public String getDescriptionOfDirectoryTree(){
        return ("""
            Display the recursive folder and file structure.

        Parameters:
        - path: relative directory path inside sandbox.

        Rules:
        - path="" means entire sandbox.
        - path="folderone" means only that folder.
        - path="backend/src" means that directory only.
        
            Use when:
            - show project structure
            - show directory tree
            - display all files
            - show entire workspace
            - visualize folders
            - inspect project hierarchy
        
            Examples:
                User: list all files
                path: ""
        
                User: show project structure
                path: ""
        
                User: show directory tree of foldertwo
                path: "foldertwo"
        
                User: show folder structure of backend
                path: "backend"
        
                User: display src tree
                path: "src"
            
            Returns all folders and files including nested directories.
            """);
    }

    public String getDescriptionOfIndexProject() {
        return """
        Index the entire codebase.

        Use when:
        - First time project analysis
        - Before searching classes or methods
        - When project structure is unknown

        This builds the project knowledge graph.
        """;
    }

    public String getDescriptionOfFindClass() {
        return """
        Find a class inside the indexed project.

        Use when:
        - locating service/controller/repository
        - refactoring code
        - understanding dependencies

        Requires project to be indexed first.
        """;
    }

    public String getDescriptionOfFindMethod() {
        return """
            Finds methods in the indexed project.

            Use this tool before reading files when the user asks:
            - where a method exists
            - modify a method
            - explain a method
            - refactor a method

            Returns file information and method metadata.
            """;
    }

    public String getDescriptionOfRollbackFile(){
        return ("""
                Restore the most recent snapshot of a file
                """);
    }
    public String getDescriptionOfCodeSearch() {
        return """
        Search the indexed project codebase using structured code-search queries.

        IMPORTANT:
        The query MUST be written using the supported search syntax.
        Do NOT convert a structured search into natural language.

        SUPPORTED STRUCTURED FIELDS:

        - class:<ClassName>
          Search for a class.
          Example:
          class:UserService

        - method:<methodName>
          Search for a method.
          Example:
          method:login

        - variable:<variableName>
          Search for a variable.
          Example:
          variable:userRepository

        - file:<fileName>
          Search for a file.
          Example:
          file:UserService.java

        - language:<LANGUAGE>
          Search by programming language.
          Example:
          language:JAVA

        - package:<packageName>
          Search by package.
          Example:
          package:com.prateek.ai_agent.service

        - import:<importName>
          Search files containing an import.
          Example:
          import:RedisTemplate
        
        - methodcall:<methodcall>
          Search for a methodcall.
          Example:
          methodcall:RedisTemplate
        
        - object:<objectName>
          Search for an object.
          Example:
          object:RedisTemplate
        
        - css:<cssSelectorName>
          Search for a cssSelector.
          Example:
          css:RedisTemplate
        
        - cssvariable:<cssVariableName>
          Search for a cssVariable.
          Example:
          cssvariable:RedisTemplate
        
        ALWAYS USE UPPERCASE LANGUAGE NAME WHILE SEARCHING BY LANGUAGE:
        Example:
          language:JAVA
        Example:
          language:HTML
        Example:
          language:CSS
        Example:
          language:JAVASCRIPT
        
        NORMAL TEXT SEARCH:If the user does not specify a structured field,use normal text search.

        Normal text search searches across:
        - file path
        - file name
        - package
        - class names
        - method names
        - variables
        - imports
        - method calls
        - objects
        - CSS selectors
        - CSS variables
        - language

        Example:authentication
        Example:JAVA

        COMBINED SEARCH:Multiple structured fields can be combined in one query.
        IMPORTANT:Multiple fields are combined using AND / MUST semantics.

        Therefore:
        class:UserService method:login

        means:
        class MUST match UserService
        AND
        method MUST match login

        The result must satisfy ALL specified conditions.

        Other examples:

        language:JAVA class:UserService

        file:UserService.java method:login

        package:com.prateek.ai_agent.service class:UserService

        import:RedisTemplate class:UserService


        QUOTED VALUES:

        Use quotes when a value contains spaces or when an exact
        value should be kept together.

        Examples:

        class:"UserService"

        file:"UserService.java"

        package:"com.prateek.ai_agent.service"

        Quoted and unquoted values are both valid.


        QUERY SELECTION RULES:

        1. If the user explicitly asks for a class, use:
           class:<name>

        2. If the user explicitly asks for a method, use:
           method:<name>

        3. If the user explicitly asks for a variable, use:
           variable:<name>

        4. If the user explicitly asks for a filename, use:
           file:<name>

        5. If the user specifies a programming language, use:
           language:<LANGUAGE>

        6. If the user specifies a package, use:
           package:<package>

        7. If the user asks about an import, use:
           import:<import>

        8. If multiple conditions are specified, combine them
           in the SAME query using spaces.

        9. Do not use commas between structured fields.

        10. Do not use JSON for the query.

        11. Do not invent unsupported field names.

        12. Supported field names are ONLY:
            class
            method
            variable
            file
            filePath
            language
            package
            import
            css
            cssVariable

        
        EXAMPLES:

        User: Find class UserService
        Query:
        class:UserService

        User: Find the login method
        Query:
        method:login

        User: Find userRepository
        Query:
        variable:userRepository

        User: Find UserService.java
        Query:
        file:UserService.java

        User: Find Java files
        Query:
        language:JAVA

        User: Find UserService in the service package
        Query:
        package:com.prateek.ai_agent.service class:UserService

        User: Find UserService's login method
        Query:
        class:UserService method:login

        User: Find Java UserService classes
        Query:
        language:JAVA class:UserService

        User: Find files importing RedisTemplate
        Query:
        import:RedisTemplate

        User: Find authentication service
        Query:
        authentication service


        PROJECT ISOLATION:
        If no matching indexed documents are found, do not invent results.
        Return the search result provided by the tool.


        IMPORTANT:

        CodeSearch searches the indexed project.

        Use CodeSearch when the user wants to:
        - locate code
        - find classes
        - find methods
        - find variables
        - find imports
        - search code
        - search across multiple files
        - locate where functionality is implemented
        - investigate project structure through code metadata

        After CodeSearch returns matching files or metadata,
        use Read or ReadMultipleFiles when the actual source code
        contents are required. Use File_Path returned by codeSearch as file path for read.

        Do not use SearchFiles for searching inside source code.
        SearchFiles searches filenames, while CodeSearch searches
        indexed code and metadata.
        """;
    }

}

