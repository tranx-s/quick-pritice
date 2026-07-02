import asyncio
import httpx
import re
from scraper.models import QuestionItem


HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "application/json, text/plain, */*",
    "Referer": "https://www.huatu.com/",
}

HUATU_SUBJECT_MAP = {
    "言语理解": "yycl",
    "数量关系": "slgx",
    "判断推理": "pdtl",
    "政治理论": "zzcs",
}


async def scrape_huatu(module_type: str, page_count: int,
                       task_log: list, on_progress=None) -> list[QuestionItem]:
    """
    华图网校题库爬取
    接口: GET https://tiku.huatu.com/api/v1/questions
    """
    questions = []
    subject = HUATU_SUBJECT_MAP.get(module_type, "yycl")

    async with httpx.AsyncClient(headers=HEADERS, timeout=20, follow_redirects=True) as client:
        for page in range(1, page_count + 1):
            if on_progress:
                on_progress(page, page_count)
            try:
                url = "https://tiku.huatu.com/api/v1/questions"
                params = {"subject": subject, "page": page, "per_page": 20}
                resp = await client.get(url, params=params)

                if resp.status_code != 200:
                    task_log.append(f"[WARN] 华图第{page}页请求失败: {resp.status_code}，跳过")
                    await asyncio.sleep(2)
                    continue

                data = resp.json()
                items = data.get("data") or data.get("questions") or data.get("list") or []

                if not items:
                    task_log.append(f"[INFO] 华图第{page}页无数据，停止")
                    break

                for item in items:
                    try:
                        answer_raw = item.get("answer") or item.get("correct_answer") or "A"
                        correct = _normalize_answer(answer_raw)

                        q = QuestionItem(
                            module_type=module_type,
                            content=_clean(item.get("content") or item.get("question", "")),
                            option_a=_clean(_get_opt(item, "A")),
                            option_b=_clean(_get_opt(item, "B")),
                            option_c=_clean(_get_opt(item, "C")),
                            option_d=_clean(_get_opt(item, "D")),
                            correct_answer=correct,
                            analysis=_clean(item.get("analysis") or item.get("explain", "")),
                            source="huatu",
                            source_id=str(item.get("id", "") or item.get("question_id", ""))
                        )
                        if q.content and q.option_a:
                            questions.append(q)
                    except Exception as e:
                        task_log.append(f"[WARN] 华图题目解析失败: {e}")

                task_log.append(f"[INFO] 华图第{page}页完成，本页{len(items)}题")
                await asyncio.sleep(1.5)

            except Exception as e:
                task_log.append(f"[ERROR] 华图第{page}页异常: {e}")

    return questions


def _get_opt(item: dict, letter: str) -> str:
    """兼容多种选项字段命名"""
    keys = [f"option_{letter.lower()}", f"option{letter}", letter.lower(), f"opt_{letter.lower()}"]
    for k in keys:
        v = item.get(k)
        if v:
            return v
    opts = item.get("options") or []
    idx = ord(letter) - ord("A")
    if isinstance(opts, list) and idx < len(opts):
        o = opts[idx]
        return o.get("content", "") if isinstance(o, dict) else str(o)
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
