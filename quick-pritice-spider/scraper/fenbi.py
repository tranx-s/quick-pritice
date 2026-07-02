import asyncio
import httpx
import re
from scraper.models import QuestionItem


HEADERS_BASE = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "application/json, text/plain, */*",
    "Accept-Language": "zh-CN,zh;q=0.9",
    "Referer": "https://www.fenbi.com/",
}

# 粉笔题库科目代码映射
FENBI_SUBJECT_MAP = {
    "言语理解": "sydw_yycl",
    "数量关系": "sydw_slgx",
    "判断推理": "sydw_pdtl",
    "政治理论": "sydw_zzzs",
}

ANSWER_MAP = {0: "A", 1: "B", 2: "C", 3: "D"}


def _parse_options(options_raw: list) -> tuple:
    """从粉笔选项数组提取 A/B/C/D 文本"""
    result = ["", "", "", ""]
    for i, opt in enumerate(options_raw[:4]):
        result[i] = opt.get("content", "") if isinstance(opt, dict) else str(opt)
    return tuple(result)


async def scrape_fenbi(module_type: str, page_count: int, cookie: str,
                       task_log: list, on_progress=None) -> list[QuestionItem]:
    """
    粉笔题库爬取
    接口: GET https://tiku.fenbi.com/api/sydw/questions
    需要登录态 Cookie
    """
    questions = []
    subject = FENBI_SUBJECT_MAP.get(module_type, "sydw_yycl")
    headers = {**HEADERS_BASE, "Cookie": cookie}

    async with httpx.AsyncClient(headers=headers, timeout=20, follow_redirects=True) as client:
        for page in range(1, page_count + 1):
            if on_progress:
                on_progress(page, page_count)
            try:
                # 粉笔接口：分页获取练习题
                url = f"https://tiku.fenbi.com/api/{subject}/questions"
                params = {"page": page, "size": 20, "type": 1}
                resp = await client.get(url, params=params)

                if resp.status_code == 401:
                    task_log.append("[ERROR] 粉笔 Cookie 已过期，请重新登录获取")
                    break
                if resp.status_code != 200:
                    task_log.append(f"[WARN] 粉笔第{page}页请求失败: {resp.status_code}")
                    continue

                data = resp.json()
                items = data.get("questions") or data.get("list") or data.get("data") or []

                if not items:
                    task_log.append(f"[INFO] 粉笔第{page}页无数据，停止")
                    break

                for item in items:
                    try:
                        opts_raw = item.get("accessories") or item.get("options") or []
                        # 粉笔选项在 accessories 里，type=1 是文字选项
                        text_opts = [o for o in opts_raw if isinstance(o, dict) and o.get("type") == 1]
                        if not text_opts:
                            text_opts = opts_raw

                        oa, ob, oc, od = _parse_options(text_opts)
                        correct_idx = item.get("correctAnswer") or item.get("answer", {}).get("correct", 0)
                        if isinstance(correct_idx, list):
                            correct_idx = correct_idx[0] if correct_idx else 0

                        q = QuestionItem(
                            module_type=module_type,
                            content=_clean(item.get("content", "")),
                            option_a=_clean(oa),
                            option_b=_clean(ob),
                            option_c=_clean(oc),
                            option_d=_clean(od),
                            correct_answer=ANSWER_MAP.get(int(correct_idx), "A"),
                            analysis=_clean(item.get("solution", {}).get("solution", "") if isinstance(item.get("solution"), dict) else ""),
                            source="fenbi",
                            source_id=str(item.get("id", ""))
                        )
                        if q.content and q.option_a:
                            questions.append(q)
                    except Exception as e:
                        task_log.append(f"[WARN] 粉笔题目解析失败: {e}")

                task_log.append(f"[INFO] 粉笔第{page}页完成，本页{len(items)}题")
                await asyncio.sleep(1.5)

            except Exception as e:
                task_log.append(f"[ERROR] 粉笔第{page}页异常: {e}")

    return questions


def _clean(text: str) -> str:
    if not text:
        return ""
    text = re.sub(r'<[^>]+>', '', str(text))
    text = re.sub(r'\s+', ' ', text).strip()
    return text
