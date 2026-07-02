from dataclasses import dataclass, field
from typing import Optional


@dataclass
class QuestionItem:
    module_type: str
    content: str
    option_a: str
    option_b: str
    option_c: str
    option_d: str
    correct_answer: str          # A/B/C/D
    analysis: Optional[str] = None
    image_url: Optional[str] = None
    source: str = "manual"
    source_id: Optional[str] = None
