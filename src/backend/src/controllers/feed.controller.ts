import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import { ValidationError, NotFoundError } from '../utils/errors';
import * as feedRepo from '../repositories/feed.repository';

export async function getFeed(req: Request, res: Response, next: NextFunction) {
  try {
    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const offset = req.query.offset ? Number(req.query.offset) : 0;
    const posts = await feedRepo.getFeedForUser(req.user!.id, limit, offset);
    success(res, posts);
  } catch (e) { next(e); }
}

export async function createPost(req: Request, res: Response, next: NextFunction) {
  try {
    const { postType, content, imageUrl, metadata } = req.body;

    if (!postType) {
      throw new ValidationError('postType is required');
    }

    const post = await feedRepo.createPost(
      req.user!.id,
      postType,
      content ?? null,
      imageUrl ?? null,
      metadata ?? null
    );
    created(res, post);
  } catch (e) { next(e); }
}

export async function deletePost(req: Request, res: Response, next: NextFunction) {
  try {
    const postId = Number(req.params.id);
    if (!postId) throw new ValidationError('Invalid post id');

    const deleted = await feedRepo.deletePost(postId, req.user!.id);
    if (!deleted) {
      throw new NotFoundError('Post');
    }
    success(res, { deleted: true });
  } catch (e) { next(e); }
}

export async function likePost(req: Request, res: Response, next: NextFunction) {
  try {
    const postId = Number(req.params.id);
    if (!postId) throw new ValidationError('Invalid post id');

    // Verify the post exists
    const post = await feedRepo.getPostById(postId, req.user!.id);
    if (!post) throw new NotFoundError('Post');

    // Toggle: unlike if already liked, like otherwise
    const alreadyLiked = await feedRepo.hasUserLiked(postId, req.user!.id);
    if (alreadyLiked) {
      await feedRepo.unlikePost(postId, req.user!.id);
      success(res, { liked: false });
    } else {
      await feedRepo.likePost(postId, req.user!.id);
      success(res, { liked: true });
    }
  } catch (e) { next(e); }
}

export async function getComments(req: Request, res: Response, next: NextFunction) {
  try {
    const postId = Number(req.params.id);
    if (!postId) throw new ValidationError('Invalid post id');

    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const offset = req.query.offset ? Number(req.query.offset) : 0;

    const comments = await feedRepo.getComments(postId, limit, offset);
    success(res, comments);
  } catch (e) { next(e); }
}

export async function addComment(req: Request, res: Response, next: NextFunction) {
  try {
    const postId = Number(req.params.id);
    if (!postId) throw new ValidationError('Invalid post id');

    const { content } = req.body;
    if (!content || !content.trim()) {
      throw new ValidationError('content is required');
    }

    // Verify the post exists
    const post = await feedRepo.getPostById(postId, req.user!.id);
    if (!post) throw new NotFoundError('Post');

    const comment = await feedRepo.addComment(postId, req.user!.id, content.trim());
    created(res, comment);
  } catch (e) { next(e); }
}

export async function getUserPosts(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = Number(req.params.userId);
    if (!userId) throw new ValidationError('Invalid user id');

    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const offset = req.query.offset ? Number(req.query.offset) : 0;

    const posts = await feedRepo.getUserPosts(userId, limit, offset, req.user!.id);
    success(res, posts);
  } catch (e) { next(e); }
}
