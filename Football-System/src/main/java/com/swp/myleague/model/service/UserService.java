package com.swp.myleague.model.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.repo.UserRepo;

@Service
public class UserService {

    private static final String LOCATION_DIRECTORY = "src/main/resources/static/images/Storage-Files";

    @Autowired
    UserRepo userRepo;

    // private PasswordEncoder encoder;

    // public UserService(PasswordEncoder encoder) {
    //     this.encoder = encoder;
    // }

    public List<User> getUser() {
        return userRepo.findAll();
    }

    public User getUserById(String userId) {
        return userRepo.findById(UUID.fromString(userId)).orElseThrow();
    }

    public User save(User user) {
        return userRepo.save(user);
    }

    public User delete(String userId) {
        return null;
    }

    @Transactional
    public void saveFile(MultipartFile file, String userId) {
        File newFile = new File(LOCATION_DIRECTORY + File.separator + file.getOriginalFilename());
        try {

            Files.copy(file.getInputStream(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            User user = getUserById(userId);
            user.setImgPath("/images/Storage-Files/" + file.getOriginalFilename());
            save(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username).orElseThrow();
    }

}
