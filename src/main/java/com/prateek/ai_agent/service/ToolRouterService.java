package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.ToolHint;
import org.springframework.stereotype.Service;

@Service
public class ToolRouterService {

    public ToolHint determineHint(String prompt) {

        String p = prompt.toLowerCase();


        //DIRECTORY
        if (contains(
                p,
                "directory tree",
                "project structure",
                "folder structure",
                "list all files",
                "show files",
                "show folders",
                "workspace structure")) {

            return new ToolHint(
                    "DirectoryTree",
                    """
            User wants recursive folder structure.
            Extract folder name if mentioned.
            Use empty path for sandbox root.
            """
            );
        }

        if (contains(
                p,
                "list files",
                "show files",
                "folder contents")) {

            return new ToolHint(
                    "ListFiles",
                    "User wants contents of a single directory."
            );
        }


         //READ FILE
        if (contains(p,
                "read file",
                "open file",
                "show contents",
                "display file",
                "view file")) {

            return new ToolHint(
                    "Read",
                    """
                    User wants file contents.
                    Use Read tool.
                    """
            );
        }


        //SEARCH FILES
        if (contains(p,
                "find file",
                "search file",
                "locate file",
                "where is")) {

            return new ToolHint(
                    "SearchFiles",
                    """
                    User wants to locate files.
                    Use SearchFiles.
                    """
            );
        }


        //COMPARE
        if (contains(p,
                "compare files",
                "difference between",
                "compare these")) {

            return new ToolHint(
                    "ReadMultipleFiles",
                    """
                    Read all relevant files before answering.
                    Use ReadMultipleFiles.
                    """
            );
        }

        //PATCH
        if (contains(p,
                "modify",
                "update",
                "change",
                "edit",
                "replace",
                "fix")) {

            return new ToolHint(
                    "ApplyPatchFile",
                    """
                    Read the file first.
                    Apply minimal changes.
                    Use ApplyPatchFile.
                    """
            );
        }


        //WRITE
        if (contains(p,
                "create file",
                "generate file",
                "write file",
                "make file")) {

            return new ToolHint(
                    "Write",
                    """
                    User wants a new file.
                    Use Write tool.
                    """
            );
        }


        //MOVE
        if (contains(p,
                "move file",
                "transfer file")) {

            return new ToolHint(
                    "MoveFile",
                    """
                    Use MoveFile tool.
                    """
            );
        }


        //RENAME
        if (contains(p,
                "rename file",
                "change filename")) {

            return new ToolHint(
                    "RenameFile",
                    """
                    Use RenameFile tool.
                    """
            );
        }


        //FILE INFO
        if (contains(p,
                "file size",
                "last modified",
                "file info",
                "metadata")) {

            return new ToolHint(
                    "GetFileInfo",
                    """
                    Use GetFileInfo tool.
                    """
            );
        }

        //CREATE DIRECTORY
        if (contains(p,
                "create folder",
                "new folder",
                "make directory")) {

            return new ToolHint(
                    "CreateDirectory",
                    """
                    Use CreateDirectory tool.
                    """
            );
        }


        //WEB
        if (contains(p,
                "latest",
                "current",
                "today",
                "search web",
                "internet",
                "recent")) {

            return new ToolHint(
                    "WebSearch",
                    """
                    User requires current information.
                    Use WebSearch or Browser.
                    """
            );
        }


        //BROWSER
        if (contains(p,
                "open url",
                "read article",
                "summarize webpage",
                "visit website")) {

            return new ToolHint(
                    "Browser",
                    """
                    Use Browser tool.
                    Extract webpage content.
                    """
            );
        }

        //BASH
        if (contains(p,
                "cmd",
                "command",
                "terminal")) {

            return new ToolHint(
                    "Bash",
                    """
                    Use Bash only if no dedicated tool exists.
                    """
            );
        }

        if (contains(p, "index project", "scan project", "index my folder", "index folder")) {

            return new ToolHint(
                    "IndexProject",
                    "User wants full project indexing"
            );
        }

        if (contains(p, "find class", "where is class", "locate class")) {

            return new ToolHint(
                    "FindClass",
                    "User is searching for a class in project index"
            );
        }

        return null;
    }

    private boolean contains(String text, String... words) {

        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
