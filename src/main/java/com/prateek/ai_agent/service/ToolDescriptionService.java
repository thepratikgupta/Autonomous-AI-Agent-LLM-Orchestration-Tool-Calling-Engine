package com.prateek.ai_agent.service;

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
            
            Examples:
            - change port 8080 to 8081
            - rename variable
            - fix method implementation
            - update database URL
            
            Prefer minimal changes.
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
}
