package com.prateek.ai_agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final String SANDBOX_ROOT = "C:\\sandbox\\";
    static final Path ROOT;

    static {
        try {
            ROOT = Path.of(SANDBOX_ROOT)
                    .toAbsolutePath()
                    .normalize()
                    .toRealPath();
        } catch (IOException e) {
            throw new RuntimeException("Invalid sandbox root", e);
        }
    }
    public Path getRoot() {
        return ROOT;
    }

    public Path getSafeReadPath(String filePath) {
        try {
            Path path = ROOT
                    .resolve(filePath)
                    .normalize()
                    .toRealPath();

            if (!path.startsWith(ROOT)) {
                throw new RuntimeException("Access denied");
            }

            return path;

        } catch (IOException e) {
            throw new RuntimeException("Invalid path", e);
        }
    }

    public Path getSafeWritePath(String filePath) {
        Path path = ROOT.resolve(filePath).normalize(); //not adding .toRealPath() as it requires that file must exist.

        if (!path.startsWith(ROOT)) {
            throw new RuntimeException("Access denied");
        }

        try {
            if (path.getParent() != null) {
                Path realParent = path.getParent().toRealPath();
                if (!realParent.startsWith(ROOT)) {
                    throw new RuntimeException("Access denied");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Invalid path", e);
        }

        return path;
    }

}