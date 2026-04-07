package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.WordListDTO;
import com.example.ancientstars.dto.WordListDetailDTO;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.service.WordListService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 词汇表管理控制器
 */
@RestController
@RequestMapping("/wordlist")
@Tag(name = "词汇表管理", description = "词汇表的创建、管理、词汇添加等接口")
public class WordListController {

    @Autowired
    private WordListService wordListService;

    /**
     * 创建词汇表
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "创建词汇表", description = "教师创建新词汇表")
    public ApiResponse<WordListDTO> createWordList(
            @Valid @RequestBody WordListDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        WordListDTO created = wordListService.createWordList(dto, userId);
        return ApiResponse.success(created);
    }

    /**
     * 更新词汇表
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "更新词汇表", description = "教师更新词汇表信息")
    public ApiResponse<WordListDTO> updateWordList(
            @Parameter(description = "词汇表ID") @PathVariable Long id,
            @Valid @RequestBody WordListDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        WordListDTO updated = wordListService.updateWordList(id, dto, userId);
        return ApiResponse.success(updated);
    }

    /**
     * 删除词汇表
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "删除词汇表", description = "教师删除词汇表")
    public ApiResponse<Void> deleteWordList(
            @Parameter(description = "词汇表ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        wordListService.deleteWordList(id, userId);
        return ApiResponse.success(null);
    }

    /**
     * 根据ID查询词汇表
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询词汇表", description = "根据ID查询词汇表基本信息")
    public ApiResponse<WordListDTO> getWordListById(@Parameter(description = "词汇表ID") @PathVariable Long id) {
        WordListDTO wordList = wordListService.getWordListById(id);
        return ApiResponse.success(wordList);
    }

    /**
     * 查询词汇表详情（包含词汇列表）
     */
    @GetMapping("/{id}/detail")
    @Operation(summary = "查询词汇表详情", description = "查询词汇表及其包含的所有词汇")
    public ApiResponse<WordListDetailDTO> getWordListDetail(@Parameter(description = "词汇表ID") @PathVariable Long id) {
        WordListDetailDTO detail = wordListService.getWordListDetail(id);
        return ApiResponse.success(detail);
    }

    /**
     * 分页查询所有词汇表
     */
    @GetMapping
    @Operation(summary = "分页查询词汇表", description = "分页查询所有词汇表")
    public ApiResponse<Page<WordListDTO>> getAllWordLists(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<WordListDTO> wordLists = wordListService.getAllWordLists(pageable);
        return ApiResponse.success(wordLists);
    }

    /**
     * 查询我的词汇表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "查询我的词汇表", description = "教师查询自己创建的词汇表")
    public ApiResponse<Page<WordListDTO>> getMyWordLists(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WordListDTO> wordLists = wordListService.getWordListsByCreator(userId, pageable);
        return ApiResponse.success(wordLists);
    }

    /**
     * 搜索词汇表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索词汇表", description = "根据名称搜索词汇表")
    public ApiResponse<Page<WordListDTO>> searchWordLists(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WordListDTO> wordLists = wordListService.searchWordLists(keyword, pageable);
        return ApiResponse.success(wordLists);
    }

    /**
     * 添加词汇到词汇表
     */
    @PostMapping("/{wordListId}/vocabulary/{vocabularyId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "添加词汇到词汇表", description = "教师将词汇添加到词汇表")
    public ApiResponse<Void> addVocabularyToWordList(
            @Parameter(description = "词汇表ID") @PathVariable Long wordListId,
            @Parameter(description = "词汇ID") @PathVariable Long vocabularyId) {
        wordListService.addVocabularyToWordList(wordListId, vocabularyId);
        return ApiResponse.success(null);
    }

    /**
     * 从词汇表移除词汇
     */
    @DeleteMapping("/{wordListId}/vocabulary/{vocabularyId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "从词汇表移除词汇", description = "教师从词汇表中移除词汇")
    public ApiResponse<Void> removeVocabularyFromWordList(
            @Parameter(description = "词汇表ID") @PathVariable Long wordListId,
            @Parameter(description = "词汇ID") @PathVariable Long vocabularyId) {
        wordListService.removeVocabularyFromWordList(wordListId, vocabularyId);
        return ApiResponse.success(null);
    }

    /**
     * 批量添加词汇到词汇表
     */
    @PostMapping("/{wordListId}/vocabularies")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "批量添加词汇", description = "教师批量添加词汇到词汇表")
    public ApiResponse<Void> addVocabulariesToWordList(
            @Parameter(description = "词汇表ID") @PathVariable Long wordListId,
            @RequestBody List<Long> vocabularyIds) {
        wordListService.addVocabulariesToWordList(wordListId, vocabularyIds);
        return ApiResponse.success(null);
    }

    /**
     * 从UserDetails获取用户ID
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof com.example.ancientstars.security.CustomUserDetails) {
            return ((com.example.ancientstars.security.CustomUserDetails) userDetails).getUserId();
        }
        throw new RuntimeException("无法获取用户ID");
    }
}
