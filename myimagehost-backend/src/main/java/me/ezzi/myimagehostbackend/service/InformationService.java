package me.ezzi.myimagehostbackend.service;

import me.ezzi.myimagehostbackend.pojo.vo.TestVO;

public interface InformationService {
    TestVO getTestInf();
    void startTest(String message);
    void stopTest();
}
