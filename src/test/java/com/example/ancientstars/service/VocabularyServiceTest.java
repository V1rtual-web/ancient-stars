package com.example.ancientstars.service;

import com.example.ancientstars.dto.VocabularyDTO;
import com.example.ancientstars.dto.VocabularyImportRequest;
import com.example.ancientstars.dto.VocabularyImportResult;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.VocabularyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 词汇服务单元测试
 * 验证需求: 2.1, 2.2, 2.5, 2.6
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("词汇服务测试")
class VocabularyServiceTest {

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private WordListService wordListService;

    @InjectMocks
    private VocabularyService vocabularyService;

    private Vocabulary testVocabulary;
    private VocabularyDTO testVocabularyDTO;

    @BeforeEach
    void setUp() {
        testVocabulary = new Vocabulary();
        testVocabulary.setId(1L);
        testVocabulary.setWord("hello");
        testVocabulary.setPhonetic("/həˈloʊ/");
        testVocabulary.setDefinition("A greeting");
        testVocabulary.setTranslation("你好");
        testVocabulary.setExample("Hello, world!");
        testVocabulary.setDifficultyLevel(1);
        testVocabulary.setDeleted(false);
        testVocabulary.setCreatedAt(LocalDateTime.now());
        testVocabulary.setUpdatedAt(LocalDateTime.now());

        testVocabularyDTO = new VocabularyDTO();
        testVocabularyDTO.setWord("hello");
        testVocabularyDTO.setPhonetic("/həˈloʊ/");
        testVocabularyDTO.setDefinition("A greeting");
        testVocabularyDTO.setTranslation("你好");
        testVocabularyDTO.setExample("Hello, world!");
        testVocabularyDTO.setDifficultyLevel(1);
    }

    // ==================== 词汇创建测试 ====================

    @Test
    @DisplayName("创建词汇成功 - 验证需求 2.1")
    void testCreateVocabularySuccess() {
        // 模拟行为
        when(vocabularyRepository.existsByWord("hello")).thenReturn(false);
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(testVocabulary);

        // 执行测试
        VocabularyDTO result = vocabularyService.createVocabulary(testVocabularyDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("hello", result.getWord());
        assertEquals("/həˈloʊ/", result.getPhonetic());
        assertEquals("A greeting", result.getDefinition());
        assertEquals("你好", result.getTranslation());
        assertEquals("Hello, world!", result.getExample());
        assertEquals(1, result.getDifficultyLevel());

        // 验证方法调用
        verify(vocabularyRepository).existsByWord("hello");
        verify(vocabularyRepository).save(any(Vocabulary.class));
    }

    @Test
    @DisplayName("创建词汇失败 - 单词已存在")
    void testCreateVocabularyDuplicate() {
        // 模拟行为
        when(vocabularyRepository.existsByWord("hello")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> vocabularyService.createVocabulary(testVocabularyDTO));

        // 验证方法调用
        verify(vocabularyRepository).existsByWord("hello");
        verify(vocabularyRepository, never()).save(any());
    }

    @Test
    @DisplayName("创建词汇 - 最小必填字段")
    void testCreateVocabularyMinimalFields() {
        // 准备最小数据
        VocabularyDTO minimalDTO = new VocabularyDTO();
        minimalDTO.setWord("test");
        minimalDTO.setTranslation("测试");

        Vocabulary minimalVocab = new Vocabulary();
        minimalVocab.setId(2L);
        minimalVocab.setWord("test");
        minimalVocab.setTranslation("测试");
        minimalVocab.setDeleted(false);

        // 模拟行为
        when(vocabularyRepository.existsByWord("test")).thenReturn(false);
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(minimalVocab);

        // 执行测试
        VocabularyDTO result = vocabularyService.createVocabulary(minimalDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("test", result.getWord());
        assertEquals("测试", result.getTranslation());

        verify(vocabularyRepository).save(any(Vocabulary.class));
    }

    // ==================== 词汇更新测试 ====================

    @Test
    @DisplayName("更新词汇成功 - 验证需求 2.2")
    void testUpdateVocabularySuccess() {
        // 准备更新数据
        VocabularyDTO updateDTO = new VocabularyDTO();
        updateDTO.setWord("hello");
        updateDTO.setPhonetic("/həˈloʊ/");
        updateDTO.setDefinition("An updated greeting");
        updateDTO.setTranslation("你好（更新）");
        updateDTO.setExample("Hello, updated world!");
        updateDTO.setDifficultyLevel(2);

        Vocabulary updatedVocab = new Vocabulary();
        updatedVocab.setId(1L);
        updatedVocab.setWord("hello");
        updatedVocab.setPhonetic("/həˈloʊ/");
        updatedVocab.setDefinition("An updated greeting");
        updatedVocab.setTranslation("你好（更新）");
        updatedVocab.setExample("Hello, updated world!");
        updatedVocab.setDifficultyLevel(2);

        // 模拟行为
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(updatedVocab);

        // 执行测试
        VocabularyDTO result = vocabularyService.updateVocabulary(1L, updateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("An updated greeting", result.getDefinition());
        assertEquals("你好（更新）", result.getTranslation());
        assertEquals(2, result.getDifficultyLevel());

        // 验证方法调用
        verify(vocabularyRepository).findById(1L);
        verify(vocabularyRepository).save(any(Vocabulary.class));
    }

    @Test
    @DisplayName("更新词汇失败 - 词汇不存在")
    void testUpdateVocabularyNotFound() {
        // 模拟行为
        when(vocabularyRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> vocabularyService.updateVocabulary(999L, testVocabularyDTO));

        // 验证方法调用
        verify(vocabularyRepository).findById(999L);
        verify(vocabularyRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新词汇失败 - 新单词已被其他词汇使用")
    void testUpdateVocabularyDuplicateWord() {
        // 准备数据
        VocabularyDTO updateDTO = new VocabularyDTO();
        updateDTO.setWord("world"); // 尝试改成已存在的单词

        // 模拟行为
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        when(vocabularyRepository.existsByWord("world")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> vocabularyService.updateVocabulary(1L, updateDTO));

        // 验证方法调用
        verify(vocabularyRepository).findById(1L);
        verify(vocabularyRepository).existsByWord("world");
        verify(vocabularyRepository, never()).save(any());
    }

    // ==================== 词汇搜索测试 ====================

    @Test
    @DisplayName("搜索词汇成功 - 验证需求 2.5")
    void testSearchVocabulariesSuccess() {
        // 准备测试数据
        List<Vocabulary> vocabularies = new ArrayList<>();
        vocabularies.add(testVocabulary);
        Page<Vocabulary> page = new PageImpl<>(vocabularies);
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟行为
        when(vocabularyRepository.searchByKeyword("hello", pageable)).thenReturn(page);

        // 执行测试
        Page<VocabularyDTO> result = vocabularyService.searchVocabularies("hello", pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("hello", result.getContent().get(0).getWord());

        // 验证方法调用
        verify(vocabularyRepository).searchByKeyword("hello", pageable);
    }

    @Test
    @DisplayName("搜索词汇 - 空关键词返回所有词汇")
    void testSearchVocabulariesEmptyKeyword() {
        // 准备测试数据
        List<Vocabulary> vocabularies = new ArrayList<>();
        vocabularies.add(testVocabulary);
        Page<Vocabulary> page = new PageImpl<>(vocabularies);
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟行为
        when(vocabularyRepository.findAll(pageable)).thenReturn(page);

        // 执行测试
        Page<VocabularyDTO> result = vocabularyService.searchVocabularies("", pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // 验证方法调用
        verify(vocabularyRepository).findAll(pageable);
        verify(vocabularyRepository, never()).searchByKeyword(any(), any());
    }

    @Test
    @DisplayName("搜索词汇 - null关键词返回所有词汇")
    void testSearchVocabulariesNullKeyword() {
        // 准备测试数据
        List<Vocabulary> vocabularies = new ArrayList<>();
        vocabularies.add(testVocabulary);
        Page<Vocabulary> page = new PageImpl<>(vocabularies);
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟行为
        when(vocabularyRepository.findAll(pageable)).thenReturn(page);

        // 执行测试
        Page<VocabularyDTO> result = vocabularyService.searchVocabularies(null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // 验证方法调用
        verify(vocabularyRepository).findAll(pageable);
    }

    @Test
    @DisplayName("搜索词汇 - 无结果")
    void testSearchVocabulariesNoResults() {
        // 准备测试数据
        Page<Vocabulary> emptyPage = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟行为
        when(vocabularyRepository.searchByKeyword("nonexistent", pageable)).thenReturn(emptyPage);

        // 执行测试
        Page<VocabularyDTO> result = vocabularyService.searchVocabularies("nonexistent", pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        // 验证方法调用
        verify(vocabularyRepository).searchByKeyword("nonexistent", pageable);
    }

    // ==================== 批量导入测试 ====================

    @Test
    @DisplayName("批量导入词汇成功 - 验证需求 2.6")
    void testImportVocabulariesSuccess() {
        // 准备测试数据
        List<VocabularyDTO> vocabularies = new ArrayList<>();
        vocabularies.add(testVocabularyDTO);

        VocabularyImportRequest request = new VocabularyImportRequest();
        request.setVocabularies(vocabularies);

        // 模拟行为
        when(vocabularyRepository.existsByWord("hello")).thenReturn(false);
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(testVocabulary);

        // 执行测试
        VocabularyImportResult result = vocabularyService.importVocabularies(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getErrors().isEmpty());

        // 验证方法调用
        verify(vocabularyRepository).existsByWord("hello");
        verify(vocabularyRepository).save(any(Vocabulary.class));
    }

    @Test
    @DisplayName("批量导入词汇 - 空列表")
    void testImportVocabulariesEmptyList() {
        // 准备测试数据
        VocabularyImportRequest request = new VocabularyImportRequest();
        request.setVocabularies(new ArrayList<>());

        // 执行测试
        VocabularyImportResult result = vocabularyService.importVocabularies(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());

        // 验证没有调用保存方法
        verify(vocabularyRepository, never()).save(any());
    }

    @Test
    @DisplayName("批量导入词汇 - 部分重复")
    void testImportVocabulariesPartialDuplicate() {
        // 准备测试数据
        List<VocabularyDTO> vocabularies = new ArrayList<>();
        vocabularies.add(testVocabularyDTO); // 重复的

        VocabularyDTO newDTO = new VocabularyDTO();
        newDTO.setWord("world");
        newDTO.setTranslation("世界");
        vocabularies.add(newDTO); // 新的

        VocabularyImportRequest request = new VocabularyImportRequest();
        request.setVocabularies(vocabularies);

        Vocabulary newVocab = new Vocabulary();
        newVocab.setId(2L);
        newVocab.setWord("world");
        newVocab.setTranslation("世界");

        // 模拟行为
        when(vocabularyRepository.existsByWord("hello")).thenReturn(true); // 第一个重复
        when(vocabularyRepository.existsByWord("world")).thenReturn(false); // 第二个不重复
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(newVocab);

        // 执行测试
        VocabularyImportResult result = vocabularyService.importVocabularies(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("单词已存在"));

        // 验证方法调用
        verify(vocabularyRepository, times(1)).save(any(Vocabulary.class));
    }

    @Test
    @DisplayName("批量导入词汇 - 全部重复")
    void testImportVocabulariesAllDuplicate() {
        // 准备测试数据
        List<VocabularyDTO> vocabularies = new ArrayList<>();
        vocabularies.add(testVocabularyDTO);

        VocabularyImportRequest request = new VocabularyImportRequest();
        request.setVocabularies(vocabularies);

        // 模拟行为
        when(vocabularyRepository.existsByWord("hello")).thenReturn(true);

        // 执行测试
        VocabularyImportResult result = vocabularyService.importVocabularies(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertFalse(result.getErrors().isEmpty());

        // 验证没有保存
        verify(vocabularyRepository, never()).save(any());
    }

    @Test
    @DisplayName("批量导入词汇 - 包含词汇表ID")
    void testImportVocabulariesWithWordList() {
        // 准备测试数据
        List<VocabularyDTO> vocabularies = new ArrayList<>();
        vocabularies.add(testVocabularyDTO);

        VocabularyImportRequest request = new VocabularyImportRequest();
        request.setVocabularies(vocabularies);
        request.setWordListId(100L);

        // 模拟行为
        when(vocabularyRepository.existsByWord("hello")).thenReturn(false);
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(testVocabulary);
        doNothing().when(wordListService).addVocabularyToWordList(anyLong(), anyLong());

        // 执行测试
        VocabularyImportResult result = vocabularyService.importVocabularies(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getSuccessCount());

        // 验证方法调用
        verify(wordListService).addVocabularyToWordList(100L, 1L);
    }

    // ==================== CSV导入测试 ====================

    @Test
    @DisplayName("CSV导入成功 - 验证需求 2.6")
    void testImportFromCSVSuccess() throws IOException {
        // 准备CSV数据
        String csvContent = "word,phonetic,definition,translation,example,difficultyLevel\n" +
                "hello,/həˈloʊ/,A greeting,你好,Hello world,1\n" +
                "world,/wɜːrld/,The earth,世界,Hello world,2\n";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        // 模拟行为
        when(vocabularyRepository.existsByWord(anyString())).thenReturn(false);
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(testVocabulary);

        // 执行测试
        VocabularyImportResult result = vocabularyService.importFromCSV(inputStream, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());

        // 验证方法调用
        verify(vocabularyRepository, times(2)).save(any(Vocabulary.class));
    }

    @Test
    @DisplayName("CSV导入 - 空文件")
    void testImportFromCSVEmptyFile() throws IOException {
        // 准备空CSV数据（只有标题行）
        String csvContent = "word,phonetic,definition,translation,example,difficultyLevel\n";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        // 执行测试
        VocabularyImportResult result = vocabularyService.importFromCSV(inputStream, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());

        // 验证没有保存
        verify(vocabularyRepository, never()).save(any());
    }

    @Test
    @DisplayName("CSV导入 - 格式错误（字段不足）")
    void testImportFromCSVInvalidFormat() throws IOException {
        // 准备格式错误的CSV数据
        String csvContent = "word,phonetic,definition,translation,example,difficultyLevel\n" +
                "hello\n" + // 只有一个字段
                "world,/wɜːrld/,The earth,世界,Hello world,2\n";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        // 模拟行为
        when(vocabularyRepository.existsByWord("world")).thenReturn(false);
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(testVocabulary);

        // 执行测试
        VocabularyImportResult result = vocabularyService.importFromCSV(inputStream, null);

        // 验证结果 - 无效行被跳过，有效行被导入
        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());

        // 验证只保存了有效的行
        verify(vocabularyRepository, times(1)).save(any(Vocabulary.class));
    }

    @Test
    @DisplayName("CSV导入 - 包含重复数据")
    void testImportFromCSVWithDuplicates() throws IOException {
        // 准备包含重复数据的CSV
        String csvContent = "word,phonetic,definition,translation,example,difficultyLevel\n" +
                "hello,/həˈloʊ/,A greeting,你好,Hello world,1\n" +
                "hello,/həˈloʊ/,A greeting,你好,Hello world,1\n"; // 重复
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        // 模拟行为 - 第一次不存在，第二次存在
        when(vocabularyRepository.existsByWord("hello"))
                .thenReturn(false)
                .thenReturn(true);
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(testVocabulary);

        // 执行测试
        VocabularyImportResult result = vocabularyService.importFromCSV(inputStream, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertFalse(result.getErrors().isEmpty());

        // 验证只保存了一次
        verify(vocabularyRepository, times(1)).save(any(Vocabulary.class));
    }

    // ==================== 其他功能测试 ====================

    @Test
    @DisplayName("根据ID查询词汇成功")
    void testGetVocabularyByIdSuccess() {
        // 模拟行为
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));

        // 执行测试
        VocabularyDTO result = vocabularyService.getVocabularyById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("hello", result.getWord());

        // 验证方法调用
        verify(vocabularyRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID查询词汇失败 - 不存在")
    void testGetVocabularyByIdNotFound() {
        // 模拟行为
        when(vocabularyRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> vocabularyService.getVocabularyById(999L));

        // 验证方法调用
        verify(vocabularyRepository).findById(999L);
    }

    @Test
    @DisplayName("删除词汇成功")
    void testDeleteVocabularySuccess() {
        // 模拟行为
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        doNothing().when(vocabularyRepository).delete(any(Vocabulary.class));

        // 执行测试
        vocabularyService.deleteVocabulary(1L);

        // 验证方法调用
        verify(vocabularyRepository).findById(1L);
        verify(vocabularyRepository).delete(testVocabulary);
    }

    @Test
    @DisplayName("删除词汇失败 - 不存在")
    void testDeleteVocabularyNotFound() {
        // 模拟行为
        when(vocabularyRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> vocabularyService.deleteVocabulary(999L));

        // 验证方法调用
        verify(vocabularyRepository).findById(999L);
        verify(vocabularyRepository, never()).delete(any());
    }
}
