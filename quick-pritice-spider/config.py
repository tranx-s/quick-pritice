import os
from dotenv import load_dotenv

load_dotenv()

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DB_NAME = os.getenv("DB_NAME", "quick_practice")
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "2102275326")

# 模块类型映射：前端标准名 -> 各平台关键词
MODULE_TYPE_MAP = {
    "言语理解": {
        "fenbi": "言语理解",
        "offcn": "言语理解与表达",
        "huatu": "言语理解",
        "yuantiku": "言语理解"
    },
    "数量关系": {
        "fenbi": "数量关系",
        "offcn": "数量关系",
        "huatu": "数量关系",
        "yuantiku": "数量关系"
    },
    "判断推理": {
        "fenbi": "判断推理",
        "offcn": "判断推理",
        "huatu": "判断推理",
        "yuantiku": "判断推理"
    },
    "政治理论": {
        "fenbi": "政治",
        "offcn": "政治理论",
        "huatu": "政治常识",
        "yuantiku": "政治理论"
    }
}
