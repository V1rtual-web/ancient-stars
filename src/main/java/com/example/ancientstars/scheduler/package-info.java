/**
 * 调度器包
 * 
 * <p>
 * 包含系统的定时任务调度器，负责执行周期性的后台任务。
 * </p>
 * 
 * <h2>主要组件：</h2>
 * <ul>
 * <li>{@link com.example.ancientstars.scheduler.ReviewScheduler} - 复习提醒调度器，
 * 每天早上8点生成学生自主背单词的复习提醒</li>
 * </ul>
 * 
 * <h2>技术说明：</h2>
 * <ul>
 * <li>使用 Spring 的 @Scheduled 注解实现定时任务</li>
 * <li>需要在主应用类上添加 @EnableScheduling 注解启用调度功能</li>
 * <li>所有调度器都应该包含完善的错误处理和日志记录</li>
 * <li>调度器方法应该捕获所有异常，避免影响下次调度执行</li>
 * </ul>
 * 
 * @since 1.0
 */
package com.example.ancientstars.scheduler;
