package com.prateek.ai_agent.service.PromptService.ToolSelectionService;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.ToolHint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ToolRouterService {

    public List<ToolHint> determineHint(String prompt) {

        String p = prompt.toLowerCase();
        List<ToolHint> toolHints = new ArrayList<>();

        //DIRECTORY
        if (contains(
                p,
                "search code",
                "search in code",
                "search codebase",
                "search project",
                "find code",
                "where is the code",
                "where is implemented",
                "find implementation",
                "find logic",
                "search implementation")) {

            toolHints.add(
                    ToolHint.builder()
                            .tool("CodeSearch")
                            .instruction("""
                        User wants to search the indexed source code.

                        Use CodeSearch when the user is asking about
                        code behavior, implementation, or logic and the
                        exact class or method is not known.

                        After finding relevant files, use Read if the
                        actual implementation needs to be inspected.
                        """)
                            .build()
            );
        }

        if (contains(
                p,
                "directory tree",
                "project structure",
                "folder structure",
                "list all files",
                "show files",
                "show folders",
                "workspace structure")) {

            toolHints.add(ToolHint.builder()
                            .tool("DirectoryTree")
                            .instruction("""
            User wants recursive folder structure.
            Extract folder name if mentioned.
            Use empty path for sandbox root.
            """).build()
            );
        }

        if (contains(
                p,
                "list files",
                "show files",
                "folder contents")) {
            toolHints.add(ToolHint.builder()
                    .tool("ListFiles")
                    .instruction("User wants contents of a single directory.").build()
            );
        }

         //READ FILE
        if (contains(p,
                "read file",
                "open file",
                "show contents",
                "display file",
                "view file")) {

            toolHints.add(ToolHint.builder()
                    .tool("Read")
                    .instruction("""
                    User wants file contents.
                    Use Read tool.
                    """).build()
            );
        }


        //SEARCH FILES
        if (contains(p,
                "find file",
                "search file",
                "locate file",
                "where is")) {

            toolHints.add(ToolHint.builder()
                    .tool("SearchFiles")
                    .instruction("""
                    User wants to locate files.
                    Use SearchFiles.
                    """).build()
            );

        }

        //COMPARE
        if (contains(p,
                "compare files",
                "difference between",
                "compare these")) {

            toolHints.add(ToolHint.builder()
                    .tool("ReadMultipleFiles")
                    .instruction("""
                    Read all relevant files before answering.
                    Use ReadMultipleFiles.
                    """).build()
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

            toolHints.add(ToolHint.builder()
                    .tool("ApplyPatchFile")
                    .instruction("""
                    Read the file first.
                    Apply minimal changes.
                    Use ApplyPatchFile.
                    """).build()
            );
        }


        //WRITE
        if (contains(p,
                "create file",
                "generate file",
                "write file",
                "make file")) {

            toolHints.add(ToolHint.builder()
                    .tool("Write")
                    .instruction("""
                    User wants a new file.
                    Use Write tool.
                    """).build()
            );
        }


        //MOVE
        if (contains(p,
                "move file",
                "transfer file")) {

            toolHints.add(ToolHint.builder()
                    .tool("MoveFile")
                    .instruction("""
                    Use MoveFile tool.
                    """).build()
            );
        }


        //RENAME
        if (contains(p,
                "rename file",
                "change filename")) {

            toolHints.add(ToolHint.builder()
                    .tool("RenameFile")
                    .instruction("""
                    Use RenameFile tool.
                    """).build()
            );
        }


        //FILE INFO
        if (contains(p,
                "file size",
                "last modified",
                "file info",
                "metadata")) {

            toolHints.add(ToolHint.builder()
                    .tool("GetFileInfo")
                    .instruction("""
                    Use GetFileInfo tool.
                    """).build()
            );
        }

        //CREATE DIRECTORY
        if (contains(p,
                "create folder",
                "new folder",
                "make directory")) {

            toolHints.add(ToolHint.builder()
                    .tool("CreateDirectory")
                    .instruction("""
                    Use CreateDirectory tool.
                    """).build()
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

            toolHints.add(ToolHint.builder()
                    .tool("WebSearch")
                    .instruction("""
                    User requires current information.
                    Use WebSearch or Browser.
                    """).build()
            );
        }


        //BROWSER
        if (contains(p,
                "open url",
                "read article",
                "summarize webpage",
                "visit website")) {

            toolHints.add(ToolHint.builder()
                    .tool("Browser")
                    .instruction("""
                    Use Browser tool.
                    Extract webpage content.
                    """).build()
            );
        }

        //BASH
        if (contains(p,
                "cmd",
                "command",
                "terminal")) {

            toolHints.add(ToolHint.builder()
                    .tool("Bash")
                    .instruction("""
                    Use Bash only if no dedicated tool exists.
                    """).build()
            );
        }

        if (contains(p, "index project", "scan project", "index my folder", "index folder")) {

            toolHints.add(ToolHint.builder()
                    .tool("IndexProject")
                    .instruction("User wants full project indexing").build()
            );
        }

        if (contains(p, "find class", "where is class", "locate class")) {

            toolHints.add(ToolHint.builder()
                    .tool("FindClass")
                    .instruction("User is searching for a class in project index").build()
            );
        }
        return toolHints;
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
