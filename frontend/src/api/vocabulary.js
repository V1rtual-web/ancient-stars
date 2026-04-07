import request from '../utils/request'

export const vocabularyAPI = {
  // 创建词汇
  createVocabulary(data) {
    return request.post('/vocabulary', data)
  },

  // 更新词汇
  updateVocabulary(id, data) {
    return request.put(`/vocabulary/${id}`, data)
  },

  // 删除词汇
  deleteVocabulary(id) {
    return request.delete(`/vocabulary/${id}`)
  },

  // 获取词汇详情
  getVocabulary(id) {
    return request.get(`/vocabulary/${id}`)
  },

  // 搜索词汇（keyword为空时返回全部）
  searchVocabulary(keyword = '', page = 0, size = 10, sortBy = 'createdAt', direction = 'DESC') {
    return request.get('/vocabulary/search', {
      params: { keyword, page, size, sortBy, direction }
    })
  },

  // 按难度查询
  getVocabulariesByDifficulty(level) {
    return request.get(`/vocabulary/difficulty/${level}`)
  },

  // 批量导入词汇（CSV文件）
  importVocabulary(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/vocabulary/import/csv', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 300000  // 5分钟，大文件导入需要时间
    })
  },

  // 创建词汇表
  createWordList(data) {
    return request.post('/wordlist', data)
  },

  // 更新词汇表
  updateWordList(id, data) {
    return request.put(`/wordlist/${id}`, data)
  },

  // 删除词汇表
  deleteWordList(id) {
    return request.delete(`/wordlist/${id}`)
  },

  // 获取词汇表列表
  getWordLists() {
    return request.get('/wordlist')
  },

  // 获取词汇表详情
  getWordListDetail(id) {
    return request.get(`/wordlist/${id}/detail`)
  },

  // 为词汇表批量添加词汇
  addVocabularyToWordList(wordListId, vocabularyIds) {
    return request.post(`/wordlist/${wordListId}/vocabularies`, vocabularyIds)
  },

  // 从词汇表移除词汇
  removeVocabularyFromWordList(wordListId, vocabularyId) {
    return request.delete(`/wordlist/${wordListId}/vocabulary/${vocabularyId}`)
  }
}
