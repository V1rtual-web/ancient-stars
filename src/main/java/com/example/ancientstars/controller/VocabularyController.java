package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.VocabularyDTO;
import com.example.ancientstars.dto.VocabularyImportRequest;
import com.example.ancientstars.dto.VocabularyImportResult;
import com.example.ancientstars.security.PermissionValidator;
import com.example.ancientstars.service.VocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 词汇管理控制器
 */
@RestController
@RequestMapping("/vocabulary")
@Tag(name = "词汇管理", description = "词汇的增删改查、搜索、批量导入等接口")
public class VocabularyController {

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private PermissionValidator permissionValidator;

    /**
     * 创建词汇
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "创建词汇", description = "教师创建新词汇")
    public ApiResponse<VocabularyDTO> createVocabulary(@Valid @RequestBody VocabularyDTO dto) {
        Long userId = permissionValidator.getCurrentUserId();
        VocabularyDTO created = vocabularyService.createVocabulary(dto, userId);
        return ApiResponse.success(created);
    }

    /**
     * 更新词汇
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "更新词汇", description = "教师更新词汇信息")
    public ApiResponse<VocabularyDTO> updateVocabulary(
            @Parameter(description = "词汇ID") @PathVariable Long id,
            @Valid @RequestBody VocabularyDTO dto) {
        VocabularyDTO updated = vocabularyService.updateVocabulary(id, dto);
        return ApiResponse.success(updated);
    }

    /**
     * 删除词汇（软删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "删除词汇", description = "教师删除词汇（软删除）")
    public ApiResponse<Void> deleteVocabulary(@Parameter(description = "词汇ID") @PathVariable Long id) {
        vocabularyService.deleteVocabulary(id);
        return ApiResponse.success(null);
    }

    /**
     * 根据ID查询词汇
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询词汇详情", description = "根据ID查询词汇详细信息")
    public ApiResponse<VocabularyDTO> getVocabularyById(@Parameter(description = "词汇ID") @PathVariable Long id) {
        VocabularyDTO vocabulary = vocabularyService.getVocabularyById(id);
        return ApiResponse.success(vocabulary);
    }

    /**
     * 分页查询所有词汇
     */
    @GetMapping
    @Operation(summary = "分页查询词汇", description = "分页查询所有词汇")
    public ApiResponse<Page<VocabularyDTO>> getAllVocabularies(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<VocabularyDTO> vocabularies = vocabularyService.getAllVocabularies(pageable);
        return ApiResponse.success(vocabularies);
    }

    /**
     * 搜索词汇（模糊查询），keyword 为空时返回全部
     */
    @GetMapping("/search")
    @Operation(summary = "搜索词汇", description = "根据关键词模糊搜索词汇，keyword为空时返回全部")
    public ApiResponse<Page<VocabularyDTO>> searchVocabularies(
            @Parameter(description = "搜索关键词") @RequestParam(required = false, defaultValue = "") String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<VocabularyDTO> vocabularies = vocabularyService.searchVocabularies(keyword, pageable);
        return ApiResponse.success(vocabularies);
    }

    /**
     * 根据难度级别查询
     */
    @GetMapping("/difficulty/{level}")
    @Operation(summary = "按难度查询", description = "根据难度级别查询词汇")
    public ApiResponse<List<VocabularyDTO>> getVocabulariesByDifficulty(
            @Parameter(description = "难度级别") @PathVariable Integer level) {
        List<VocabularyDTO> vocabularies = vocabularyService.getVocabulariesByDifficulty(level);
        return ApiResponse.success(vocabularies);
    }

    /**
     * 批量导入词汇（JSON格式）
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "批量导入词汇", description = "教师批量导入词汇（JSON格式）")
    public ApiResponse<VocabularyImportResult> importVocabularies(@Valid @RequestBody VocabularyImportRequest request) {
        Long userId = permissionValidator.getCurrentUserId();
        VocabularyImportResult result = vocabularyService.importVocabularies(request, userId);
        return ApiResponse.success(result);
    }

    @PostMapping("/import/csv")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "从CSV导入词汇", description = "教师从CSV文件导入词汇")
    public ApiResponse<VocabularyImportResult> importFromCSV(
            @Parameter(description = "CSV文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "词汇表ID（可选）") @RequestParam(required = false) Long wordListId) throws IOException {
        Long userId = permissionValidator.getCurrentUserId();
        VocabularyImportResult result = vocabularyService.importFromCSV(file.getInputStream(), wordListId, userId);
        return ApiResponse.success(result);
    }
}
