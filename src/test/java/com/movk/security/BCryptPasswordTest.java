package com.movk.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密测试类
 * 用于验证和生成 BCrypt 加密密码
 *
 * @author yixuanmiao
 * @date 2025-12-11
 */
class BCryptPasswordTest {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  /**
   * 验证数据库中的默认密码
   */
  @Test
  void testVerifyDefaultPassword() {
    String rawPassword = "Admin@123";
    String encodedPassword = "$2a$10$lAJmlv4iWT8STlRE0SOrhupSgGMyvA9GNmJXrb065U22dnDQvRxqm";

    boolean matches = encoder.matches(rawPassword, encodedPassword);
    System.out.println("========================================");
    System.out.println("验证数据库默认密码:");
    System.out.println("明文密码: " + rawPassword);
    System.out.println("BCrypt 密文: " + encodedPassword);
    System.out.println("验证结果: " + (matches ? "✅ 匹配成功" : "❌ 匹配失败"));
    System.out.println("========================================");
  }

  /**
   * 生成新的 BCrypt 加密密码
   */
  @Test
  void testEncodePassword() {
    String password = "Admin@123";
    String encoded1 = encoder.encode(password);
    String encoded2 = encoder.encode(password);

    System.out.println("========================================");
    System.out.println("生成新的 BCrypt 密码:");
    System.out.println("明文密码: " + password);
    System.out.println("加密密码1: " + encoded1);
    System.out.println("加密密码2: " + encoded2);
    System.out.println("密码1验证: " + (encoder.matches(password, encoded1) ? "✅" : "❌"));
    System.out.println("密码2验证: " + (encoder.matches(password, encoded2) ? "✅" : "❌"));
    System.out.println("说明: 每次加密结果不同是正常的（随机盐值）");
    System.out.println("========================================");
  }

  /**
   * 批量生成用户密码
   */
  @Test
  void testBatchEncodePasswords() {
    String[] passwords = {"Admin@2025#Secure", "User@2025#Normal", "Test@2025#Debug"};
    String[] users = {"admin", "user", "test"};

    System.out.println("========================================");
    System.out.println("批量生成用户密码:");
    for (int i = 0; i < passwords.length; i++) {
      String encoded = encoder.encode(passwords[i]);
      System.out.println(users[i] + " | 明文: " + passwords[i] + " | BCrypt: " + encoded);
    }
    System.out.println("========================================");
  }

  /**
   * 验证错误密码
   */
  @Test
  void testVerifyWrongPassword() {
    String correctPassword = "Admin@123";
    String wrongPassword = "WrongPassword";
    String encodedPassword = "$2a$10$N.zmdr9k7uOCQb8gT4iAieFGFGTrb8YDbI8/8ycwt9xHcPw.v4a7u";

    boolean correctMatch = encoder.matches(correctPassword, encodedPassword);
    boolean wrongMatch = encoder.matches(wrongPassword, encodedPassword);

    System.out.println("========================================");
    System.out.println("密码验证测试:");
    System.out.println("正确密码验证: " + (correctMatch ? "✅ 通过" : "❌ 失败"));
    System.out.println("错误密码验证: " + (wrongMatch ? "❌ 通过（不应该）" : "✅ 正确拒绝"));
    System.out.println("========================================");
  }
}
