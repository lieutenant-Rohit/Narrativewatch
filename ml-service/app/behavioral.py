import numpy as np
from sklearn.ensemble import IsolationForest
from pydantic import BaseModel


class AccountFeatures(BaseModel):
    accountId: str
    avgIntervalSec: float
    postCount: int
    followerCount: int
    topicCount: int


class BehavioralResult(BaseModel):
    accountId: str
    anomalyScore: float


def score_accounts(accounts: list[AccountFeatures]) -> list[BehavioralResult]:
    if len(accounts) < 3:
        return [BehavioralResult(accountId=a.accountId, anomalyScore=0.0)
                for a in accounts]

    features = np.array([
        [a.avgIntervalSec, a.postCount, a.followerCount, a.topicCount]
        for a in accounts
    ])

    model = IsolationForest(
        n_estimators=100,
        contamination=0.2,
        random_state=42,
    )

    scores = model.fit_transform(features)
    scores = -scores
    scores = (scores - scores.min()) / (scores.max() - scores.min() + 1e-10)

    return [BehavioralResult(accountId=a.accountId, anomalyScore=float(s))
            for a, s in zip(accounts, scores)]
