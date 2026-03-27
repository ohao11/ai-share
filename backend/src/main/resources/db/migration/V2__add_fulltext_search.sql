-- =============================================
-- V2: 添加全文搜索支持
-- =============================================

-- 添加全文搜索向量列
ALTER TABLE articles ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- 创建更新搜索向量的函数
CREATE OR REPLACE FUNCTION update_article_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('simple', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(NEW.summary, '')), 'B') ||
        setweight(to_tsvector('simple', COALESCE(NEW.content, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS article_search_vector_update ON articles;
CREATE TRIGGER article_search_vector_update
    BEFORE INSERT OR UPDATE ON articles
    FOR EACH ROW
    EXECUTE FUNCTION update_article_search_vector();

-- 更新现有数据的搜索向量
UPDATE articles SET search_vector =
    setweight(to_tsvector('simple', COALESCE(title, '')), 'A') ||
    setweight(to_tsvector('simple', COALESCE(summary, '')), 'B') ||
    setweight(to_tsvector('simple', COALESCE(content, '')), 'C');

-- 创建 GIN 索引加速全文搜索
CREATE INDEX IF NOT EXISTS idx_articles_search_vector ON articles USING gin(search_vector);