import pymysql
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
from config import DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
from scraper.models import QuestionItem


def get_conn():
    return pymysql.connect(
        host=DB_HOST, port=DB_PORT, db=DB_NAME,
        user=DB_USER, password=DB_PASSWORD,
        charset="utf8mb4", autocommit=True
    )


def upsert_questions(questions: list[QuestionItem], task_log: list[str]) -> tuple[int, int]:
    """
    写入题目，返回 (saved, skipped)
    source_id 不为 None 时用唯一索引去重；为 None 时按内容哈希去重
    """
    saved = skipped = 0
    conn = get_conn()
    try:
        with conn.cursor() as cur:
            for q in questions:
                try:
                    # 内容去重（无 source_id 时）
                    if not q.source_id:
                        cur.execute(
                            "SELECT id FROM question WHERE content = %s AND deleted = 0 LIMIT 1",
                            (q.content,)
                        )
                        if cur.fetchone():
                            skipped += 1
                            continue

                    sql = """
                        INSERT INTO question
                            (module_type, content, option_a, option_b, option_c, option_d,
                             correct_answer, analysis, image_url, source, source_id, is_online)
                        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,1)
                        ON DUPLICATE KEY UPDATE
                            content = VALUES(content),
                            option_a = VALUES(option_a),
                            option_b = VALUES(option_b),
                            option_c = VALUES(option_c),
                            option_d = VALUES(option_d),
                            correct_answer = VALUES(correct_answer),
                            analysis = VALUES(analysis)
                    """
                    cur.execute(sql, (
                        q.module_type, q.content, q.option_a, q.option_b,
                        q.option_c, q.option_d, q.correct_answer, q.analysis,
                        q.image_url, q.source, q.source_id
                    ))
                    if cur.rowcount == 1:
                        saved += 1
                        task_log.append(f"[SUCCESS] 入库: {q.content[:30]}...")
                    else:
                        skipped += 1
                        task_log.append(f"[WARN] 跳过重复: {q.content[:30]}...")
                except Exception as e:
                    task_log.append(f"[ERROR] 写入失败: {e} | {q.content[:30]}")
    finally:
        conn.close()
    return saved, skipped
