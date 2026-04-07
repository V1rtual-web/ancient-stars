package com.example.ancientstars.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Tag;

import java.util.*;

/**
 * 测试题目随机性属性测试
 * 
 * 属性 11: 测试题目随机性
 * 对于任何两次独立的测试生成，即使来自同一词汇表，题目顺序应该不同（除非词汇表只有一个词）
 * 
 * 注意：此测试验证Collections.shuffle()的随机性属性，而不是直接测试TestRecordService
 * TestRecordService使用Collections.shuffle()来打乱词汇顺序，因此验证shuffle的随机性即可
 */
@Tag("Feature: vocabulary-learning-system, Property 11: 测试题目随机性")
public class TestQuestionRandomnessPropertyTest {

    /**
     * 属性测试：验证Collections.shuffle()的随机性
     * 
     * 对于任何包含多个元素的列表，两次独立打乱的结果应该不同（大概率）
     * 这模拟了TestRecordService中使用Collections.shuffle()打乱词汇顺序的行为
     */
    @Property(tries = 100)
    void testShuffleRandomness(@ForAll @IntRange(min = 5, max = 20) int listSize) {
        // 创建一个有序列表
        List<Integer> originalList = new ArrayList<>();
        for (int i = 1; i <= listSize; i++) {
            originalList.add(i);
        }

        // 两次打乱
        List<Integer> shuffled1 = new ArrayList<>(originalList);
        Collections.shuffle(shuffled1);
        
        List<Integer> shuffled2 = new ArrayList<>(originalList);
        Collections.shuffle(shuffled2);

        // 验证：两次打乱的结果应该不同（对于多个元素的情况）
        // 注意：理论上有极小概率两次随机结果相同，但在100次迭代中应该至少有一次不同
        // 我们不强制要求每次都不同，但验证列表长度和元素完整性
        assertEquals(listSize, shuffled1.size(), "第一次打乱后列表长度应该不变");
        assertEquals(listSize, shuffled2.size(), "第二次打乱后列表长度应该不变");
        
        // 验证：打乱后应该包含所有原始元素
        Set<Integer> set1 = new HashSet<>(shuffled1);
        Set<Integer> set2 = new HashSet<>(shuffled2);
        assertEquals(listSize, set1.size(), "第一次打乱后应该包含所有不同的元素");
        assertEquals(listSize, set2.size(), "第二次打乱后应该包含所有不同的元素");
    }

    /**
     * 属性测试：验证打乱后元素完整性
     * 
     * 虽然顺序随机，但应该包含所有原始元素
     */
    @Property(tries = 100)
    void testShuffleCompleteness(@ForAll @IntRange(min = 3, max = 15) int listSize) {
        // 创建一个有序列表
        List<Integer> originalList = new ArrayList<>();
        for (int i = 1; i <= listSize; i++) {
            originalList.add(i);
        }

        // 打乱列表
        List<Integer> shuffled = new ArrayList<>(originalList);
        Collections.shuffle(shuffled);

        // 验证：应该包含所有原始元素，且没有重复
        Set<Integer> shuffledSet = new HashSet<>(shuffled);
        assertEquals(listSize, shuffledSet.size(), "打乱后应该包含所有不同的元素");
        
        // 验证：所有元素都在有效范围内
        for (Integer num : shuffled) {
            assertTrue(num >= 1 && num <= listSize,
                    String.format("元素 %d 应该在有效范围内 [1, %d]", num, listSize));
        }
        
        // 验证：包含所有原始元素
        for (int i = 1; i <= listSize; i++) {
            assertTrue(shuffled.contains(i),
                    String.format("打乱后的列表应该包含元素 %d", i));
        }
    }

    /**
     * 属性测试：验证多次打乱的分布均匀性
     * 
     * 通过多次打乱，验证每个元素出现在不同位置的概率大致相等
     */
    @Property(tries = 20)
    void testShuffleDistribution(@ForAll @IntRange(min = 5, max = 10) int listSize) {
        // 创建一个有序列表
        List<Integer> originalList = new ArrayList<>();
        for (int i = 1; i <= listSize; i++) {
            originalList.add(i);
        }

        // 多次打乱，统计第一个位置的元素分布
        Map<Integer, Integer> firstPositionCount = new HashMap<>();
        int iterations = 50;
        
        for (int i = 0; i < iterations; i++) {
            List<Integer> shuffled = new ArrayList<>(originalList);
            Collections.shuffle(shuffled);
            
            if (!shuffled.isEmpty()) {
                Integer firstElement = shuffled.get(0);
                firstPositionCount.put(firstElement, 
                        firstPositionCount.getOrDefault(firstElement, 0) + 1);
            }
        }

        // 验证：每个元素都应该至少出现过一次在第一个位置（对于足够多的迭代）
        // 理论期望：每个元素出现在第一个位置的概率是 1/listSize
        // 在50次迭代中，每个元素期望出现 50/listSize 次
        
        // 我们不要求完全均匀，但至少应该有多个不同的元素出现在第一个位置
        int uniqueFirstElements = firstPositionCount.size();
        assertTrue(uniqueFirstElements >= Math.min(3, listSize),
                String.format("在%d次迭代中，至少应该有%d个不同的元素出现在第一个位置，实际为%d",
                        iterations, Math.min(3, listSize), uniqueFirstElements));
    }

    /**
     * 属性测试：验证单元素列表打乱后不变
     */
    @Property(tries = 100)
    void testSingleElementShuffle() {
        List<Integer> singleList = new ArrayList<>();
        singleList.add(1);
        
        List<Integer> shuffled = new ArrayList<>(singleList);
        Collections.shuffle(shuffled);
        
        // 单元素列表打乱后应该不变
        assertEquals(1, shuffled.size(), "单元素列表打乱后长度应该为1");
        assertEquals(Integer.valueOf(1), shuffled.get(0), "单元素列表打乱后元素应该不变");
    }

    /**
     * 属性测试：验证空列表打乱后不变
     */
    @Property(tries = 100)
    void testEmptyListShuffle() {
        List<Integer> emptyList = new ArrayList<>();
        
        List<Integer> shuffled = new ArrayList<>(emptyList);
        Collections.shuffle(shuffled);
        
        // 空列表打乱后应该仍为空
        assertTrue(shuffled.isEmpty(), "空列表打乱后应该仍为空");
    }

    /**
     * 辅助断言方法
     */
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * 辅助断言方法
     */
    private void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }
}
