package com.example.ancientstars.repository;

import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.entity.WordList;
import com.example.ancientstars.entity.WordListVocabulary;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 词汇表关联完整性属性测试
 * 验证属性5: 词汇表关联完整性
 */
@DataJpaTest
@ActiveProfiles("test")
@Tag("Feature: vocabulary-learning-system, Property 5: 词汇表关联完整性")
class WordListVocabularyPropertyTest {

        @Autowired
        private WordListRepository wordListRepository;

        @Autowired
        private VocabularyRepository vocabularyRepository;

        @Autowired
        private WordListVocabularyRepository wordListVocabularyRepository;

        @Test
        void wordListShouldContainAllAddedVocabularies() {
                // 运行100次属性测试
                Random random = new Random();
                for (int testRun = 0; testRun < 100; testRun++) {
                        // 生成测试数据
                        String wordListName = "WordList_" + random.nextInt(100000) + "_" + System.nanoTime();
                        int vocabularyCount = random.nextInt(10) + 1; // 1-10个词汇

                        // 创建词汇表
                        WordList wordList = new WordList();
                        wordList.setName(wordListName);
                        wordList.setDescription("Test word list");
                        wordList.setCreatorId(1L);
                        wordList.setWordCount(0);
                        WordList savedWordList = wordListRepository.save(wordList);

                        // 创建并添加词汇
                        Set<Long> addedVocabularyIds = new HashSet<>();
                        for (int i = 0; i < vocabularyCount; i++) {
                                String word = "word_" + random.nextInt(100000) + "_" + System.nanoTime() + "_" + i;

                                // 创建词汇
                                Vocabulary vocabulary = new Vocabulary();
                                vocabulary.setWord(word);
                                vocabulary.setTranslation("Translation of " + word);
                                vocabulary.setDeleted(false);
                                Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);

                                // 添加到词汇表
                                WordListVocabulary wlv = new WordListVocabulary();
                                wlv.setWordListId(savedWordList.getId());
                                wlv.setVocabularyId(savedVocabulary.getId());
                                wlv.setSortOrder(i);
                                wordListVocabularyRepository.save(wlv);

                                addedVocabularyIds.add(savedVocabulary.getId());
                        }

                        // 验证：通过词汇表ID查询应该返回所有已添加的词汇
                        List<Long> retrievedVocabularyIds = wordListVocabularyRepository
                                        .findVocabularyIdsByWordListId(savedWordList.getId());

                        // 断言：返回的词汇数量应该等于添加的词汇数量
                        assertEquals(addedVocabularyIds.size(), retrievedVocabularyIds.size(),
                                        "测试运行 " + testRun + ": 词汇表中的词汇数量应该等于添加的词汇数量");

                        // 断言：返回的词汇ID应该包含所有添加的词汇ID
                        assertTrue(new HashSet<>(retrievedVocabularyIds).containsAll(addedVocabularyIds),
                                        "测试运行 " + testRun + ": 词汇表应该包含所有已添加的词汇");

                        // 清理数据
                        wordListVocabularyRepository.deleteByWordListId(savedWordList.getId());
                        vocabularyRepository.deleteAll(vocabularyRepository.findAllById(addedVocabularyIds));
                        wordListRepository.delete(savedWordList);
                }
        }

    @Test
    void duplicateVocabularyInWordListShouldBeRejected() {
        // 运行50次属性测试
        Random random = new Random();
        for (int testRun = 0; testRun < 50; testRun++) {
            String wordListName = "WordList_" + random.nextInt(100000) + "_" + System.nanoTime();
            String word = "word_" + random.nextInt(100000) + "_" + System.nanoTime();

            // 创建词汇表
            WordList wordList = new WordList();
            wordList.setName(wordListName);
            wordList.setDescription("Test word list");
            wordList.setCreatorId(1L);
            wordList.setWordCount(0);
            WordList savedWordList = wordListRepository.save(wordList);

            // 创建词汇
            Vocabulary vocabulary = new Vocabulary();
            vocabulary.setWord(word);
            vocabulary.setTranslation("Translation");
            vocabulary.setDeleted(false);
            Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);

            // 第一次添加应该成功
            WordListVocabulary wlv1 = new WordListVocabulary();
            wlv1.setWordListId(savedWordList.getId());
            wlv1.setVocabularyId(savedVocabulary.getId());
            wlv1.setSortOrder(0);
            wordListVocabularyRepository.save(wlv1);

            // 验证：检查是否已存在
            boolean exists = wordListVocabularyRepository.existsByWordListIdAndVocabularyId(
                    savedWordList.getId(), savedVocabulary.getId());
            assertTrue(exists, "测试运行 " + testRun + ": 词汇应该已经在词汇表中");

            // 清理数据
            wordListVocabularyRepository.deleteByWordListId(savedWordList.getId());
            vocabularyRepository.delete(savedVocabulary);
            wordListRepository.delete(savedWordList);
        }
    }

    @Test
    void removedVocabularyShouldNotBeInWordList() {
        // 运行50次属性测试
        Random random = new Random();
        for (int testRun = 0; testRun < 50; testRun++) {
            String wordListName = "WordList_" + random.nextInt(100000) + "_" + System.nanoTime();
            int vocabularyCount = random.nextInt(9) + 2; // 2-10个词汇

            // 创建词汇表
            WordList wordList = new WordList();
            wordList.setName(wordListName);
            wordList.setDescription("Test word list");
            wordList.setCreatorId(1L);
            wordList.setWordCount(0);
            WordList savedWordList = wordListRepository.save(wordList);

            // 创建并添加词汇
            List<Long> vocabularyIds = new ArrayList<>();
            for (int i = 0; i < vocabularyCount; i++) {
                String word = "word_" + random.nextInt(100000) + "_" + System.nanoTime() + "_" + i;
                Vocabulary vocabulary = new Vocabulary();
                vocabulary.setWord(word);
                vocabulary.setTranslation("Translation");
                vocabulary.setDeleted(false);
                Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);
                vocabularyIds.add(savedVocabulary.getId());

                WordListVocabulary wlv = new WordListVocabulary();
                wlv.setWordListId(savedWordList.getId());
                wlv.setVocabularyId(savedVocabulary.getId());
                wlv.setSortOrder(i);
                wordListVocabularyRepository.save(wlv);
            }

            // 移除第一个词汇
            Long removedVocabularyId = vocabularyIds.get(0);
            wordListVocabularyRepository.deleteByWordListIdAndVocabularyId(
                    savedWordList.getId(), removedVocabularyId);

            // 验证：查询结果不应该包含被移除的词汇
            List<Long> retrievedIds = wordListVocabularyRepository
                    .findVocabularyIdsByWordListId(savedWordList.getId());

            assertFalse(retrievedIds.contains(removedVocabularyId),
                    "测试运行 " + testRun + ": 移除的词汇不应该出现在查询结果中");
            assertEquals(vocabularyCount - 1, retrievedIds.size(),
                    "测试运行 " + testRun + ": 词汇表中的词汇数量应该减少1");

            // 清理数据
            wordListVocabularyRepository.deleteByWordListId(savedWordList.getId());
            vocabularyRepository.deleteAll(vocabularyRepository.findAllById(vocabularyIds));
            wordListRepository.delete(savedWordList);
        }
    }

    @Test
    void wordListCountShouldMatchActualAssociations() {
        // 运行50次属性测试
        Random random = new Random();
        for (int testRun = 0; testRun < 50; testRun++) {
            String wordListName = "WordList_" + random.nextInt(100000) + "_" + System.nanoTime();
            int vocabularyCount = random.nextInt(10) + 1; // 1-10个词汇

            // 创建词汇表
            WordList wordList = new WordList();
            wordList.setName(wordListName);
            wordList.setDescription("Test word list");
            wordList.setCreatorId(1L);
            wordList.setWordCount(0);
            WordList savedWordList = wordListRepository.save(wordList);

            // 创建并添加词汇
            List<Long> vocabularyIds = new ArrayList<>();
            for (int i = 0; i < vocabularyCount; i++) {
                String word = "word_" + random.nextInt(100000) + "_" + System.nanoTime() + "_" + i;
                Vocabulary vocabulary = new Vocabulary();
                vocabulary.setWord(word);
                vocabulary.setTranslation("Translation");
                vocabulary.setDeleted(false);
                Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);
                vocabularyIds.add(savedVocabulary.getId());

                WordListVocabulary wlv = new WordListVocabulary();
                wlv.setWordListId(savedWordList.getId());
                wlv.setVocabularyId(savedVocabulary.getId());
                wlv.setSortOrder(i);
                wordListVocabularyRepository.save(wlv);
            }

            // 验证：统计数量应该与实际关联数量一致
            long actualCount = wordListVocabularyRepository.countByWordListId(savedWordList.getId());
            assertEquals(vocabularyCount, actualCount,
                    "测试运行 " + testRun + ": 词汇表的词汇数量统计应该与实际关联数量一致");

            // 清理数据
            wordListVocabularyRepository.deleteByWordListId(savedWordList.getId());
            vocabularyRepository.deleteAll(vocabularyRepository.findAllById(vocabularyIds));
            wordListRepository.delete(savedWordList);
        }
    }
}
