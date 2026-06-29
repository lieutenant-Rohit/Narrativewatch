from fastapi import FastAPI
from app.behavioral import AccountFeatures, BehavioralResult, score_accounts
from app.semantic import PostInput, SemanticResult, cluster_posts

app = FastAPI(title="NarrativeWatch ML Service")


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/ml/behavioral", response_model=list[BehavioralResult])
def behavioral_endpoint(accounts: list[AccountFeatures]):
    return score_accounts(accounts)


@app.post("/ml/semantic", response_model=list[SemanticResult])
def semantic_endpoint(posts: list[PostInput]):
    return cluster_posts(posts)
