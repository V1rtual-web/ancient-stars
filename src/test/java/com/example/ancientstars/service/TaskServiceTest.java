package com.example.ancientstars.service;

import com.example.ancientstars.dto.TaskDTO;
import com.example.ancientstars.dto.TaskProgressUpdateRequest;
import com.example.ancientstars.entity.Task;
import com.example.ancientstars.entity.TaskAssignment;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.entity.WordList;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.TaskAssignmentRepository;
import com.example.ancientstars.repository.TaskRepository;
import com.example.ancientstars.repository.UserRepository;
import com.example.ancientstars.repository.WordListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务服务测试类
 */
@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordListRepository wordListRepository;

    private User teacher;
    private User student1;
    private User student2;
    private WordList wordList;

    @BeforeEach
    void setUp() {
        // 创建测试教师
        teacher = new User();
        teacher.setUsername("teacher_test");
        teacher.setPassword("password");
        teacher.setRealName("测试教师");
        teacher.setRole(User.Role.TEACHER);
        teacher.setStatus(User.Status.ACTIVE);
        teacher = userRepository.save(teacher);

        // 创建测试学生1
        student1 = new User();
        student1.setUsername("student_test1");
        student1.setPassword("password");
        student1.setRealName("测试学生1");
        student1.setRole(User.Role.STUDENT);
        student1.setStatus(User.Status.ACTIVE);
        student1 = userRepository.save(student1);

        // 创建测试学生2
        student2 = new User();
        student2.setUsername("student_test2");
        student2.setPassword("password");
        student2.setRealName("测试学生2");
        student2.setRole(User.Role.STUDENT);
        student2.setStatus(User.Status.ACTIVE);
        student2 = userRepository.save(student2);

        // 创建测试词汇表
        wordList = new WordList();
        wordList.setName("测试词汇表");
        wordList.setDescription("用于测试的词汇表");
        wordList.setCreatorId(teacher.getId());
        wordList.setWordCount(10);
        wordList = wordListRepository.save(wordList);
    }

@Test
    void testCreateTask() {
        // 准备测试数据
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("测试任务");
        taskDTO.setDescription("这是一个测试任务");
        taskDTO.setWordListId(wordList.getId());
        taskDTO.setCreatedBy(teacher.getId());
        taskDTO.setDeadline(LocalDateTime.now().plusDays(7));
        taskDTO.setStudentIds(Arrays.asList(student1.getId(), student2.getId()));

        // 执行创建任务
        TaskDTO result = taskService.createTask(taskDTO);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("测试任务", result.getTitle());
        assertEquals(wordList.getId(), result.getWordListId());
        assertEquals(teacher.getId(), result.getCreatedBy());

        // 验证任务分配记录
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTaskId(result.getId());
        assertEquals(2, assignments.size());
    }

    @Test
    void testAssignTaskToStudents() {
        // 先创建任务
        Task task = new Task();
        task.setTitle("测试任务");
        task.setWordListId(wordList.getId());
        task.setCreatedBy(teacher.getId());
        task.setDeadline(LocalDateTime.now().plusDays(7));
        task = taskRepository.save(task);

        // 分配任务给学生
        taskService.assignTaskToStudents(task.getId(), Arrays.asList(student1.getId(), student2.getId()));

        // 验证分配记录
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTaskId(task.getId());
        assertEquals(2, assignments.size());
        
        for (TaskAssignment assignment : assignments) {
            assertEquals(TaskAssignment.AssignmentStatus.NOT_STARTED, assignment.getStatus());
            assertEquals(0, assignment.getProgress());
        }
    }

    @Test
    void testUpdateTaskProgress() {
        // 创建任务和分配记录
        Task task = new Task();
        task.setTitle("测试任务");
        task.setWordListId(wordList.getId());
        task.setCreatedBy(teacher.getId());
        task = taskRepository.save(task);

        TaskAssignment assignment = new TaskAssignment();
        assignment.setTaskId(task.getId());
        assignment.setStudentId(student1.getId());
        assignment.setProgress(0);
        assignment.setStatus(TaskAssignment.AssignmentStatus.NOT_STARTED);
        assignment = taskAssignmentRepository.save(assignment);

        // 更新进度到50%
        TaskProgressUpdateRequest request = new TaskProgressUpdateRequest();
        request.setStudentId(student1.getId());
        request.setProgress(50);
        taskService.updateTaskProgress(task.getId(), request);

        // 验证进度更新
        TaskAssignment updated = taskAssignmentRepository.findById(assignment.getId()).orElseThrow();
        assertEquals(50, updated.getProgress());
        assertEquals(TaskAssignment.AssignmentStatus.IN_PROGRESS, updated.getStatus());

        // 更新进度到100%
        request.setProgress(100);
        taskService.updateTaskProgress(task.getId(), request);

        // 验证完成状态
        updated = taskAssignmentRepository.findById(assignment.getId()).orElseThrow();
        assertEquals(100, updated.getProgress());
        assertEquals(TaskAssignment.AssignmentStatus.COMPLETED, updated.getStatus());
        assertNotNull(updated.getCompletedAt());
    }

    @Test
    void testGetTaskById() {
        // 创建任务
        Task task = new Task();
        task.setTitle("测试任务");
        task.setDescription("测试描述");
        task.setWordListId(wordList.getId());
        task.setCreatedBy(teacher.getId());
        task = taskRepository.save(task);

        // 获取任务详情
        TaskDTO result = taskService.getTaskById(task.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        assertEquals("测试任务", result.getTitle());
        assertEquals("测试描述", result.getDescription());
    }

    @Test
    void testGetTasksByTeacher() {
        // 创建多个任务
        Task task1 = new Task();
        task1.setTitle("任务1");
        task1.setWordListId(wordList.getId());
        task1.setCreatedBy(teacher.getId());
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("任务2");
        task2.setWordListId(wordList.getId());
        task2.setCreatedBy(teacher.getId());
        taskRepository.save(task2);

        // 获取教师的任务列表
        List<TaskDTO> tasks = taskService.getTasksByTeacher(teacher.getId());

        // 验证结果
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
    }

    @Test
    void testGetStudentTasks() {
        // 创建任务
        Task task = new Task();
        task.setTitle("测试任务");
        task.setWordListId(wordList.getId());
        task.setCreatedBy(teacher.getId());
        task = taskRepository.save(task);

        // 创建分配记录
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTaskId(task.getId());
        assignment.setStudentId(student1.getId());
        taskAssignmentRepository.save(assignment);

        // 获取学生的任务列表
        var studentTasks = taskService.getStudentTasks(student1.getId());

        // 验证结果
        assertNotNull(studentTasks);
        assertEquals(1, studentTasks.size());
        assertEquals(task.getId(), studentTasks.get(0).getTaskId());
    }

    @Test
    void testCheckExpiredTasks() {
        // 创建已过期的任务
        Task expiredTask = new Task();
        expiredTask.setTitle("过期任务");
        expiredTask.setWordListId(wordList.getId());
        expiredTask.setCreatedBy(teacher.getId());
        expiredTask.setDeadline(LocalDateTime.now().minusDays(1));
        expiredTask.setStatus(Task.TaskStatus.ACTIVE);
        expiredTask = taskRepository.save(expiredTask);

        // 执行过期检查
        taskService.checkExpiredTasks();

        // 验证任务状态已更新
        Task updated = taskRepository.findById(expiredTask.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.EXPIRED, updated.getStatus());
    }

    @Test
    void testCreateTaskWithInvalidWordList() {
        // 准备测试数据（使用不存在的词汇表ID）
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("测试任务");
        taskDTO.setWordListId(99999L);
        taskDTO.setCreatedBy(teacher.getId());

        // 验证抛出异常
        assertThrows(BusinessException.class, () -> {
            taskService.createTask(taskDTO);
        });
    }

    @Test
    void testAssignTaskToInvalidStudent() {
        // 创建任务
        Task task = new Task();
        task.setTitle("测试任务");
        task.setWordListId(wordList.getId());
        task.setCreatedBy(teacher.getId());
        task = taskRepository.save(task);

        Long taskId = task.getId();

        // 验证分配给不存在的学生时抛出异常
        assertThrows(BusinessException.class, () -> {
            taskService.assignTaskToStudents(taskId, Arrays.asList(99999L));
        });
    }
}
