import asyncio
import uuid
from typing import Optional
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from scraper.fenbi import scrape_fenbi
from scraper.offcn import scrape_offcn
from scraper.huatu import scrape_huatu
from scraper.yuantiku import scrape_yuantiku
from scraper.writer import upsert_questions

app = FastAPI(title="极速刷题爬虫服务", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# 内存任务存储（重启后清空，轻量够用）
tasks: dict[str, dict] = {}


class ScrapeRequest(BaseModel):
    source: str           # fenbi / offcn / huatu / yuantiku
    module_type: str      # 言语理解 / 数量关系 / 判断推理 / 政治理论
    page_count: int = 5
    cookie: Optional[str] = None  # 粉笔需要


@app.post("/scrape/start")
async def start_scrape(req: ScrapeRequest):
    task_id = str(uuid.uuid4())[:8]
    task = {
        "task_id": task_id,
        "source": req.source,
        "module_type": req.module_type,
        "status": "running",
        "scraped": 0,
        "saved": 0,
        "skipped": 0,
        "failed": 0,
        "current_page": 0,
        "total_pages": req.page_count,
        "count": 0,
        "logs": [f"[INFO] 任务启动: {req.source} - {req.module_type}，共{req.page_count}页"]
    }
    tasks[task_id] = task
    asyncio.create_task(_run_task(task_id, req))
    return {"task_id": task_id, "status": "running"}


async def _run_task(task_id: str, req: ScrapeRequest):
    task = tasks[task_id]
    log = task["logs"]

    def on_progress(cur, total):
        task["current_page"] = cur
        task["total_pages"] = total

    try:
        if req.source == "fenbi":
            if not req.cookie:
                log.append("[ERROR] 粉笔需要提供 Cookie")
                task["status"] = "error"
                return
            questions = await scrape_fenbi(req.module_type, req.page_count, req.cookie, log, on_progress)
        elif req.source == "offcn":
            questions = await scrape_offcn(req.module_type, req.page_count, log, on_progress)
        elif req.source == "huatu":
            questions = await scrape_huatu(req.module_type, req.page_count, log, on_progress)
        elif req.source == "yuantiku":
            questions = await scrape_yuantiku(req.module_type, req.page_count, log, on_progress)
        else:
            log.append(f"[ERROR] 未知来源: {req.source}")
            task["status"] = "error"
            return

        task["scraped"] = len(questions)
        log.append(f"[INFO] 爬取完成，共{len(questions)}题，开始写入数据库...")

        saved, skipped = upsert_questions(questions, log)
        task["saved"] = saved
        task["skipped"] = skipped
        task["count"] = saved
        task["status"] = "done"
        log.append(f"[SUCCESS] 全部完成！入库{saved}题，跳过重复{skipped}题")

    except Exception as e:
        log.append(f"[ERROR] 任务异常: {e}")
        task["status"] = "error"
        task["failed"] += 1


@app.get("/scrape/status/{task_id}")
async def get_status(task_id: str):
    task = tasks.get(task_id)
    if not task:
        return {"status": "not_found"}
    return {k: v for k, v in task.items() if k != "logs"}


@app.get("/scrape/logs/{task_id}")
async def get_logs(task_id: str):
    task = tasks.get(task_id)
    if not task:
        return {"logs": []}
    return {"logs": task.get("logs", [])}


@app.get("/scrape/tasks")
async def list_tasks():
    return {
        "tasks": [
            {k: v for k, v in t.items() if k != "logs"}
            for t in reversed(list(tasks.values()))
        ]
    }


@app.get("/health")
async def health():
    return {"status": "ok", "task_count": len(tasks)}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8002, reload=True)
