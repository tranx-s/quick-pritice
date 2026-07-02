import asyncio
import httpx
import re
from scraper.models import QuestionItem


HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "application/json, text/plain, */*",
    "Referer": "https://www.offcn.com/",
}

# 中公题库科目ID映射（通过抓包获取的实际ID）
OFFCN_SUBJECT_MAP = {
    "言语理解": "2",
    "数量关系": "3",
    "判断推理": "4",
    "政治理论": "10",
}

ANSWER_MAP = {"1": "A", "2": "B", "3": "C", "4": "D", 1: "A", 2: "B", 3: "C", 4: "D"}


async def scrape_offcn(module_type: str, page_count: int,
                       task_log: list, on_progress=None) -> list[QuestionItem]:
    """
    中公教育题库爬取
    接口: GET https://tiku.offcn.com/api/question/list
    无需登录，公开接口
    """
    questions = []
    subject_id = OFFCN_SUBJECT_MAP.get(module_type, "2")

    async with httpx.AsyncClient(headers=HEADERS, timeout=20, follow_redirects=True) as client:
        for page in range(1, page_count + 1):
            if on_progress:
                on_progress(page, page_count)
            try:
                url = "https://tiku.offcn.com/api/question/list"
                params = {
                    "subjectId": subject_id,
                    "pageNum": page,
                    "pageSize": 20,
                    "examType": 1   # 1=行测
                }
                resp = await client.get(url, params=params)

                if resp.status_code != 200:
                    task_log.append(f"[WARN] 中公第{page}页请求失败: {resp.status_code}")
                    # 降级：尝试备用解析方式
                    questions.extend(await _scrape_offcn_page_html(client, module_type, page, task_log))
                    await asyncio.sleep(2)
                    continue

                data = resp.json()
                rows = (data.get("data") or {}).get("list") or data.get("list") or []

                if not rows:
                    task_log.append(f"[INFO] 中公第{page}页无数据，停止")
                    break

                for item in rows:
                    try:
                        q = QuestionItem(
                            module_type=module_type,
                            content=_clean(item.get("questionContent", "") or item.get("content", "")),
                            option_a=_clean(item.get("optionA", "") or item.get("a", "")),
                            option_b=_clean(item.get("optionB", "") or item.get("b", "")),
                            option_c=_clean(item.get("optionC", "") or item.get("c", "")),
                            option_d=_clean(item.get("optionD", "") or item.get("d", "")),
                            correct_answer=ANSWER_MAP.get(
                                item.get("answer") or item.get("rightAnswer"), "A"
                            ),
                            analysis=_clean(item.get("analysis", "") or item.get("explain", "")),
                            source="offcn",
                            source_id=str(item.get("id", "") or item.get("questionId", ""))
                        )
                        if q.content and q.option_a:
                            questions.append(q)
                    except Exception as e:
                        task_log.append(f"[WARN] 中公题目解析失败: {e}")

                task_log.append(f"[INFO] 中公第{page}页完成，本页{len(rows)}题")
                await asyncio.sleep(1.5)

            except Exception as e:
                task_log.append(f"[ERROR] 中公第{page}页异常: {e}")

    return questions


async def _scrape_offcn_page_html(client, module_type, page, task_log):
    """备用：解析中公题库HTML页面（接口失败时使用）"""
    questions = []
    try:
        url = f"https://tiku.offcn.com/xinzheng/xingce/index.html?page={page}"
        resp = await client.get(url)
        html = resp.text

        # 简单正则提取题目块（实际使用时按真实HTML结构调整）
        blocks = re.findall(
            r'<div class="question-content">(.*?)</div>.*?'
            r'<span class="answer">(.*?)</span>',
            html, re.S
        )
        for content, answer in blocks[:20]:
            task_log.append(f"[WARN] HTML解析（粗略），建议检查: {_clean(content)[:20]}")
    except Exception as e:
        task_log.append(f"[WARN] 中公HTML备用解析失败: {e}")
    return questions


def _clean(text: str) -> str:
    if not text:
        return ""
    text = re.sub(r'<[^>]+>', '', str(text))
    text = re.sub(r'\s+', ' ', text).strip()
    return text
