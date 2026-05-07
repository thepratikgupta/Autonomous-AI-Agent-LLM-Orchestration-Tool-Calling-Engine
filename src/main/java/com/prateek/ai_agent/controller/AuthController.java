package com.prateek.ai_agent.controller;

public class AuthController {
}

/// /
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final JwtService jwtService;
//    private final UserRepository userRepository;
//
//    @PostMapping("/register")
//    public AuthResponsedto register(@RequestBody RegisterRequestdto req) {
//
//        User user = new User();
//        user.setEmail(req.getEmail());
//        user.setPassword(req.getPassword()); // hash later
//
//        userRepository.save(user);
//
//        String token = jwtService.generateToken(user.getEmail());
//
//        return new AuthResponsedto(token);
//    }
//
//    @PostMapping("/login")
//    public AuthResponsedto login(@RequestBody LoginRequestdto req) {
//
//        User user = userRepository.findByEmail(req.getEmail())
//                .orElseThrow();
//
//        String token = jwtService.generateToken(user.getEmail());
//
//        return new AuthResponsedto(token);
//    }
//}
