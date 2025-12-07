package me.ezzi.myimagehostbackend.service;

public interface EmailService {


    void sendVerificationCode(String email, String type);


    boolean validVerificationCode(String email,String verificationCode);

}
