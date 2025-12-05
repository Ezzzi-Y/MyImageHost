package me.ezzi.myimagehostbackend.task;

import me.ezzi.myimagehostbackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserEnableTask {

    @Autowired
    private UserMapper userMapper;

    /**
     * 每天凌晨 4 点执行批量恢复用户
     * Cron 表达式：秒 分 时 日 月 星期
     * "0 0 4 * * ?" 表示每天 4:00:00 执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void recoverDisabledUsers() {
        userMapper.recoverUsers();
    }
}
