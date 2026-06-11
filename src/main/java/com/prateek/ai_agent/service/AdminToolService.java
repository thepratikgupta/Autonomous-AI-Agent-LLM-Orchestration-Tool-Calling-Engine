package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.RoleType;
import com.prateek.ai_agent.entity.User;
import com.prateek.ai_agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.prateek.ai_agent.service.FileService.ROOT;

@Service
@RequiredArgsConstructor
public class AdminToolService {
    private final ExecutorService executor;
    private static final long COMMAND_TIMEOUT_SECONDS2 = 10;
    private final UserRepository userRepository;


    protected String executeApprovedCommand(String cmd){
        try {

            Process p = new ProcessBuilder("cmd.exe", "/c", cmd)
                    .directory(ROOT.toFile())
                    .redirectErrorStream(true)
                    .start();
            System.out.println("RAW CMD: [" + cmd + "]");
            InputStream is = p.getInputStream();

            Future<String> futureOutput = executor.submit(() ->
                    new String(is.readAllBytes())
            );

            boolean finished = p.waitFor(COMMAND_TIMEOUT_SECONDS2, java.util.concurrent.TimeUnit.SECONDS);

            if (!finished) {
                p.destroyForcibly();
                p.waitFor();
                futureOutput.cancel(true);
                return "Command timed out after " + COMMAND_TIMEOUT_SECONDS2 + " seconds";
            }

            String output;
            try {
                output = futureOutput.get(2, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                futureOutput.cancel(true);
                return "Output read timeout";
            }

            int code = p.exitValue();
            return code == 0 ? output : "Failed (" + code + "): " + output;

        } catch (Exception e) {
            e.printStackTrace();
            return "[Execution error from AdminToolService.]";
        }
    }


    public User makeAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(RoleType.ADMIN);
//        //debugging
//        System.out.println("Before: " + user.getRole());
//        user.setRole(RoleType.ADMIN);
//        User saved = userRepository.save(user);
//        System.out.println("After: " + saved.getRole());
//        //debugging
        return userRepository.save(user);
    }
}
