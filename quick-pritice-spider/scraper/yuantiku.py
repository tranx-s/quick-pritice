import asyncio
import httpx
import re
from scraper.models import QuestionItem


HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "application/json, text/plain, */*",
    "Referer": "https://www.yuantiku.com/",
}

YUANTIKU_SUBJECT_MAP = {
    "言语理解": 101,
    "数量关系": 102,
    "判断推理": 103,
    "政治理论": 110,
}


async def scrape_yuantiku(module_type: str, page_count: int,
                          task_log: list, on_progress=None) -> list[QuestionItem]:
    """
    猿题库爬取
    接口: GET https://www.yuantiku.com/api/v3/questions/search
    """
    questions = []
    subject_id = YUANTIKU_SUBJECT_MAP.get(module_type, 101)

    async with httpx.AsyncClient(headers=HEADERS, timeout=20, follow_redirects=True) as client:
        for page in range(1, page_count + 1):
            if on_progress:
                on_progress(page, page_count)
            try:
                url = "https://www.yuantiku.com/api/v3/questions/search"
                params = {
                    "subjectId": subject_id,
                    "page": page,
                    "pageSize": 20,
                    "examType": "xingce"
                }
                resp = await client.get(url, params=params)

                if resp.status_code != 200:
                    task_log.append(f"[WARN] 猿题库第{page}页请求失败: {resp.status_code}，跳过")
                    await asyncio.sleep(2)
                    continue

                data = resp.json()
                items = (
                    data.get("data", {}).get("questions")
                    or data.get("questions")
                    or data.get("list")
                    or []
                )

                if not items:
                    task_log.append(f"[INFO] 猿题库第{page}页无数据，停止")
                    break

                for item in items:
                    try:
                        answer_raw = item.get("answer") or item.get("correctAnswer") or "A"
                        correct = _normalize_answer(answer_raw)

                        q = QuestionItem(
                            module_type=module_type,
                            content=_clean(item.get("content") or item.get("question", "")),
                            option_a=_clean(_extract_opt(item, 0)),
                            option_b=_clean(_extract_opt(item, 1)),
                            option_c=_clean(_extract_opt(item, 2)),
                            option_d=_clean(_extract_opt(item, 3)),
                            correct_answer=correct,
                            analysis=_clean(item.get("analysis") or item.get("explanation", "")),
                            source="yuantiku",
                            source_id=str(item.get("id", "") or item.get("questionId", ""))
                        )
                        if q.content and q.option_a:
                            questions.append(q)
                    except Exception as e:
                        task_log.append(f"[WARN] 猿题库题目解析失败: {e}")

                task_log.append(f"[INFO] 猿题库第{page}页完成，本页{len(items)}题")
                await asyncio.sleep(1.5)

            except Exception as e:
                task_log.append(f"[ERROR] 猿题库第{page}页异常: {e}")

    return questions


def _extract_opt(item: dict, idx: int) -> str:
    letter = ["A", "B", "C", "D"][idx]
    # 尝试直接字段
    for k in [f"option{letter}", f"option_{letter.lower()}", letter.lower()]:
        v = item.get(k)
        if v:
            return str(v)
    # 尝试数组
    for key in ["options", "choices"]:
        opts = item.get(key)
        if isinstance(opts, list) and idx < len(opts):
            o = opts[idx]
            if isinstance(o, dict):
                return o.get("content") or o.get("text") or o.get("value") or ""
            return str(o)
    return ""


def _normalize_answer(raw) -> str:
    if isinstance(raw, int):
        return {0: "A", 1: "B", 2: "C", 3: "D"}.get(raw, "A")
    s = str(raw).strip().upper()
    if s in ("A", "B", "C", "D"):
        return s
    return "A"


def _clean(text: str) -> str:
    if not text:
        return ""
    text = re.sub(r'<[^>]+>', '', str(text))
    text = re.sub(r'\s+', ' ', text).strip()
    return text
