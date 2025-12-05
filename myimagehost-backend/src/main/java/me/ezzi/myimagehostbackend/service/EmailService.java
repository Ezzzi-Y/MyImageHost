package me.ezzi.myimagehostbackend.service;

public interface EmailService {


    void sendVerificationCode(String email);


    boolean validVerificationCode(String email,String verificationCode);

}
