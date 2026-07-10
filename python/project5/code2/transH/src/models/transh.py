from __future__ import annotations

import os

os.environ.setdefault("KMP_DUPLICATE_LIB_OK", "TRUE")

import torch
from torch import nn
import torch.nn.functional as F


class TransHModel(nn.Module):
    def __init__(self, num_entities: int, num_relations: int, embedding_dim: int = 64, margin: float = 1.0) -> None:
        super().__init__()
        self.num_entities = num_entities
        self.num_relations = num_relations
        self.embedding_dim = embedding_dim
        self.margin = margin

        self.entity_embeddings = nn.Embedding(num_entities, embedding_dim)
        self.relation_embeddings = nn.Embedding(num_relations, embedding_dim)
        self.normal_vectors = nn.Embedding(num_relations, embedding_dim)
        self.reset_parameters()

    def reset_parameters(self) -> None:
        bound = 6.0 / (self.embedding_dim**0.5)
        nn.init.uniform_(self.entity_embeddings.weight.data, -bound, bound)
        nn.init.uniform_(self.relation_embeddings.weight.data, -bound, bound)
        nn.init.uniform_(self.normal_vectors.weight.data, -bound, bound)
        self.entity_embeddings.weight.data = F.normalize(self.entity_embeddings.weight.data, p=2, dim=1)
        self.normal_vectors.weight.data = F.normalize(self.normal_vectors.weight.data, p=2, dim=1)

    @staticmethod
    def _project(entity: torch.Tensor, normal: torch.Tensor) -> torch.Tensor:
        normal = F.normalize(normal, p=2, dim=-1)
        return entity - torch.sum(entity * normal, dim=-1, keepdim=True) * normal

    def score(self, triples: torch.Tensor) -> torch.Tensor:
        h = self.entity_embeddings(triples[:, 0])
        r = self.relation_embeddings(triples[:, 1])
        t = self.entity_embeddings(triples[:, 2])
        w = F.normalize(self.normal_vectors(triples[:, 1]), p=2, dim=-1)

        h_proj = self._project(h, w)
        t_proj = self._project(t, w)
        return torch.linalg.vector_norm(h_proj + r - t_proj, ord=2, dim=1)

    def forward(
        self,
        pos_triples: torch.Tensor,
        neg_triples: torch.Tensor,
        sample_weights: torch.Tensor | None = None,
    ) -> torch.Tensor:
        pos_scores = self.score(pos_triples)
        neg_scores = self.score(neg_triples)
        losses = F.relu(self.margin + pos_scores - neg_scores)
        if sample_weights is not None:
            weights = sample_weights.to(losses.device).float()
            return (losses * weights).sum() / weights.sum().clamp_min(1e-8)
        return losses.mean()

    @torch.no_grad()
    def score_triples(self, triples: torch.Tensor) -> torch.Tensor:
        was_training = self.training
        self.eval()
        scores = self.score(triples)
        if was_training:
            self.train()
        return scores
