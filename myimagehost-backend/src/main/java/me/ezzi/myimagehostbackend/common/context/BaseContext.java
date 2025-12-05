package me.ezzi.myimagehostbackend.common.context;

public class BaseContext {

    // 存储用户ID
    private static final ThreadLocal<Long> threadLocalId = new ThreadLocal<>();
    // 存储用户角色
    private static final ThreadLocal<String> threadLocalRole = new ThreadLocal<>();

    // ---------- ID ----------
    public static void setCurrentId(Long id) {
        threadLocalId.set(id);
    }

    public static Long getCurrentId() {
        return threadLocalId.get();
    }

    public static void removeCurrentId() {
        threadLocalId.remove();
    }

    // ---------- ROLE ----------
    public static void setCurrentRole(String role) {
        threadLocalRole.set(role);
    }

    public static String getCurrentRole() {
        return threadLocalRole.get();
    }

    public static void removeCurrentRole() {
        threadLocalRole.remove();
    }

    // ---------- 清理 ----------
    public static void clear() {
        removeCurrentId();
        removeCurrentRole();
    }
}
