package me.ezzi.myimagehostbackend.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.pojo.vo.TestVO;
import me.ezzi.myimagehostbackend.service.InformationService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InformationServiceImpl implements InformationService {
    private Boolean isTesting;
    private String testMessage;


    @Override
    public TestVO getTestInf() {
        return TestVO.builder()
                .testStatus(isTesting)
                .testMessage(testMessage)
                .build();
    }

    @Override
    public void startTest(String message) {
        isTesting = true;
        this.testMessage = message;
    }

    @Override
    public void stopTest() {
        isTesting = false;
        this.testMessage = "";
    }
}
