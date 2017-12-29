-- get top 10 most visit docs
-- desc
SELECT t.doc_id, COUNT(*)
FROM LOG t
GROUP BY t.doc_id
ORDER BY COUNT(*) DESC
LIMIT 10;

-- get top 10 most daily continuously visit docs
-- desc
SELECT tt.doc_id, COUNT(tt.doc_id)
FROM 
(
SELECT t.doc_id, t.log_date, COUNT(*) AS cnt
FROM LOG t
GROUP BY t.doc_id, t.log_date 
)tt
GROUP BY tt.doc_id
ORDER BY COUNT(tt.doc_id) DESC
LIMIT 10

-- get visit detail by date for specific doc
SELECT t.log_date as 'Date', COUNT(*) as 'R/W Count'
FROM LOG t
WHERE t.doc_id = '{doc_id_here}'
GROUP BY t.log_date
ORDER BY t.log_date ASC