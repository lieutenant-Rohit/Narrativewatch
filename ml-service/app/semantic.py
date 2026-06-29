import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import DBSCAN
from sklearn.metrics.pairwise import cosine_similarity
from pydantic import BaseModel


class PostInput(BaseModel):
    postId: str
    text: str


class SemanticResult(BaseModel):
    postId: str
    clusterId: int
    similarityScore: float


def cluster_posts(posts: list[PostInput]) -> list[SemanticResult]:
    if len(posts) < 2:
        return [SemanticResult(postId=p.postId, clusterId=-1, similarityScore=0.0)
                for p in posts]

    texts = [p.text for p in posts]

    vectorizer = TfidfVectorizer(
        max_features=500,
        stop_words="english",
        ngram_range=(1, 2),
    )
    tfidf_matrix = vectorizer.fit_transform(texts)

    model = DBSCAN(eps=0.5, min_samples=2, metric="cosine")
    cluster_labels = model.fit_predict(tfidf_matrix)

    similarity_matrix = cosine_similarity(tfidf_matrix)

    results = []
    for i, label in enumerate(cluster_labels):
        if label == -1:
            score = 0.0
        else:
            in_cluster = [j for j in range(len(posts)) if cluster_labels[j] == label and j != i]
            score = float(np.mean([similarity_matrix[i][j] for j in in_cluster])) if in_cluster else 0.0

        results.append(SemanticResult(
            postId=posts[i].postId,
            clusterId=int(label),
            similarityScore=round(score, 4),
        ))

    return results
